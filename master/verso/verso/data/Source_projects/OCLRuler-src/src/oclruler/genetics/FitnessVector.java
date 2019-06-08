package oclruler.genetics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;

import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.FireMap;
import oclruler.metamodel.Model;
import oclruler.rule.Program;
import oclruler.rule.struct.Constraint;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class FitnessVector {
	public final static Logger LOGGER = Logger.getLogger(FitnessVector.class.getName());
	
	static boolean COMPARE_LEAVES = true;

	
	/**
	 * Avoid a potential 1-rule program to win the fitness race
	 */
	private static final int ABSOLUTE_SIZE_THRESHOLD = 2;
	private static final float TFIDF_NORMALIZE = 10;

	public static int[] EXPECTED_NUMBER_OF_RULES = new int[]{10, 20};//Magnitude
	
	public static int NUMBER_OF_OBJECTIVES = 3;//-> Pos - Neg - Size
	public static int OBJECTIVES_CONSIDERED = 3;//-> Pos - Neg - Size
	public static double OBJECTIVE_SIGMA[] = new double[]{0.0001, 0.0001, 0.0001};
	public static double OBJECTIVE_SIGMA_DEFAULT = 0.0001;
	
	public static String csvHeader = "%POS;%NEG;%SIZ;#RUL";//Par exemple

	
	public static void loadConfig() {
		String s = Config.getStringParam("EXPECTED_SIZE");
		try {
			int min = Integer.decode(s.split(" ")[0]);
			if(min == 0)
				LOGGER.warning("MIN-EXPECTED_SIZE is 0 ! Attention, this may lead to unexpected results.");
			EXPECTED_NUMBER_OF_RULES = new int[]{min, Integer.decode(s.split(" ")[1])};
		} catch (Exception e) {
			LOGGER.severe("Expected program size undefined : '"+s+"'\n Expected syntax is '<int> <int>' (Upper and lower bounds are required)" );
			System.exit(1);
		}
		
		OBJECTIVES_CONSIDERED = Config.getIntParam("OBJECTIVES_CONSIDERED");	
		//TODO
		switch (OBJECTIVES_CONSIDERED) {
		case 1:
			csvHeader = "%ACC";
			break;
		case 2:
			csvHeader = "%ACC;%SIZ";
			break;
		case 3:
			csvHeader = "%POS;%NEG";
			break;
		default:
			LOGGER.severe("OBJECTIVES_CONSIDERED ("+OBJECTIVES_CONSIDERED+") out of bounds. Must be in [1..3]");
			System.exit(1);
			break;
		}
		
		if(Config.TFIDF.isObjective()) {
			NUMBER_OF_OBJECTIVES++;
			OBJECTIVES_CONSIDERED++;
			csvHeader += ";TFIDF";
		}
		csvHeader += ";#RUL";
// //Cas d'ecole de factorisation
//			if(Config.OBJECTIVE_TFIDF_ON) {
//			OBJECTIVES_CONSIDERED++;
//			switch (OBJECTIVES_CONSIDERED) {
//			case 2:
//				csvHeader = "%ACC;#RUL;TFIDF";
//				break;
//			case 3:
//				csvHeader = "%ACC;%SIZ;#RUL;TFIDF";
//				break;
//			case 4:
//				csvHeader = "%POS;%NEG;#RUL;TFIDF";
//				break;
//			default:
//				LOGGER.severe("OBJECTIVES_CONSIDERED ("+OBJECTIVES_CONSIDERED+") out of bounds. Must be in [1..3]");
//				System.exit(1);
//				break;
//			}
//		} else {
//			switch (OBJECTIVES_CONSIDERED) {
//			case 1:
//				csvHeader = "%ACC;#RUL";
//				break;
//			case 2:
//				csvHeader = "%ACC;%SIZ;#RUL";
//				break;
//			case 3:
//				csvHeader = "%POS;%NEG;#RUL";
//				break;
//			default:
//				LOGGER.severe("OBJECTIVES_CONSIDERED ("+OBJECTIVES_CONSIDERED+") out of bounds. Must be in [1..3]");
//				System.exit(1);
//				break;
//			}
//		}
//		
		
		String objSigmas = Config.getStringParam("OBJECTIVE_SIGMA");
		String[] OBJECTIVE_SIGMA_s = objSigmas.split(" ");
		if (OBJECTIVE_SIGMA_s.length == 1) {
			try {
				for (int j = 0; j < OBJECTIVES_CONSIDERED; j++)
					OBJECTIVE_SIGMA[j] = Double.parseDouble(OBJECTIVE_SIGMA_s[0]);
			} catch (NumberFormatException e) {
				LOGGER.warning("OBJECTIVE_SIGMA '" + objSigmas + "' is not valid. Expected is either 1 unique or " + OBJECTIVES_CONSIDERED + " different values (double). " + e.getMessage());
				for (int j = 0; j < OBJECTIVES_CONSIDERED; j++)
					OBJECTIVE_SIGMA[j] = OBJECTIVE_SIGMA_DEFAULT;
			}
		} else {

			OBJECTIVE_SIGMA = new double[Math.max(OBJECTIVES_CONSIDERED, OBJECTIVE_SIGMA_s.length)];
			boolean warningMalformed = false;
			for (int j = 0; j < OBJECTIVES_CONSIDERED; j++) {
				try {
					OBJECTIVE_SIGMA[j] = Double.parseDouble(OBJECTIVE_SIGMA_s[j]);
				} catch (Exception e) {
					if(!warningMalformed){
						LOGGER.warning("OBJECTIVE_SIGMA '"+objSigmas+"' is not valid. Expected is either 1 unique or "+OBJECTIVES_CONSIDERED+" different values (double).");
						warningMalformed = true;
					}
					LOGGER.warning("OBJECTIVE["+j+"]_SIGMA set to default = "+OBJECTIVE_SIGMA_DEFAULT);
					OBJECTIVE_SIGMA[j] = OBJECTIVE_SIGMA_DEFAULT;
				}
			}
		}
	}

	
	/**
	 * Objective values
	 */
	private float[] values;
	
	/**
	 * Number of rules in the entity
	 */
	private int programSize = -1;
	private int programLeaves = -1;
	
	/**
	 * Flag
	 */
	private boolean modified = true;
	
	/**
	 * [P]ositive/[N]egative count : pp = Positive oracle and Positive entity, NP = Negative oracle and Positive entity, ...
	 * Total number (pp+np+pn+nn) is the number of examples used.
	 */
	/** pp = Positive oracle and Positive entity */
	int pp = 0;
	/** np = Negative oracle and Positive entity */
	int np = 0;
	/** pn = Positive oracle and Negative entity */
	int pn = 0;
	/** nn = Negative oracle and Negative entity */
	int nn = 0;
	
	/**
	 * Comparison between the associated entity and oracle decisions
	 */
	HashMap<Model, Oracle.OracleCmp> rawDiscrimination;
	
	/**
	 * Relation between constraints/patterns/rules and the entity
	 */
	FireMap fires;
	
	
	private FitnessVector() {
		values = new float[NUMBER_OF_OBJECTIVES];
		for (int i = 0; i < values.length; i++) 
			values[i] = -1;
		fires = new FireMap();
		modified = true;
		rawDiscrimination = new HashMap<>(ExampleSet.getExamplesBeingUsed().size());
	}
	
	public FitnessVector(Program prg) {
		this();
		programSize = prg.size();
		programLeaves = prg.getNumberOfLeaves();
	}
	
	public void addFire(Model m, Constraint p){
		LOGGER.fine("FitnessVector.addFire("+p+", "+ p.getFires()+")");
		HashMap<Constraint, Integer> mFires = null;
		if(fires.get(m) == null)
			fires.put(m, mFires = new HashMap<Constraint, Integer>());
		else 
			mFires = fires.get(m);
		
		if(mFires.get(p) == null)
			mFires.put(p, 0);
		
		mFires.put(p, mFires.get(p)+p.getFires());
		modified = modified || p.getFires()>0;
	}
	
	public ArrayList<Model> getInvalidModels() {
		ArrayList<Model> res = new ArrayList<>(3);
		for (Model m : rawDiscrimination.keySet()) {
			if(!rawDiscrimination.get(m).isRight())
				res.add(m);
		}
		return res;
	}
	
	/**
	 * !!! <b>Must be followed by a call to TFIDF related objective value assignement.<b>
	 */
	private void updateValues() {
		rawDiscrimination = new HashMap<>(ExampleSet.getExamplesBeingUsed().size());
		Model[] ms = (Model[]) ExampleSet.getExamplesBeingUsed().toArray(new Model[ExampleSet.getExamplesBeingUsed().size()]);
		for (Model m : ms) {
			boolean oValid = m.isValid();
			boolean prgValid = true;
			HashMap<Constraint, Integer> hm = fires.get(m);
			if(hm != null) {
				for (Constraint pt : hm.keySet()) {
					if(hm.get(pt) != 0){
						prgValid = false;
						break;
					}
				}
			}
			Oracle.OracleCmp res = Oracle.OracleCmp.getCmp(oValid, prgValid);
			rawDiscrimination.put(m, res);
			switch (res) {
			case NN:
				nn++;			//Oracle -, Prg -
				break;
			case NP:
				np++;			//Oracle +, Prg -
				break;
			case PN:
				pn++;			//Oracle -, Prg +
				break;
			case PP:
				pp++; 			//Oracle + / Prg +
				break;
			default:
				break;
			}
			
		}
//		LOGGER.fine(" : "+pp+"/"+np+" | "+pn+"/"+nn);
		
		//TODO OBJECTIVE_TFIDF impacts
		int offset = Config.TFIDF.isObjective()? -1 : 0;
		switch (OBJECTIVES_CONSIDERED+offset) {
		case 3:
			values[0] = ((float)pp)/ (float)(pp+np);//might be NaN if no positive Example : (pp+np) = 0 => If so, execution is stopped.
			values[1] = ((float)nn)/ (float)(nn+pn);
			values[2] = computeSizeValue(programSize, programLeaves);
			break;
		case 2:
			float positiveRate = ((float)pp)/ (float)(pp+pn);//True positive rate
			float negativeRate = ((float)nn)/ (float)(nn+np);//True negative rate
			
			
			values[0] = (positiveRate * ExampleSet.POSITIVES_CONSIDERED + negativeRate * ExampleSet.NEGATIVES_CONSIDERED) 
					/ (ExampleSet.POSITIVES_CONSIDERED + ExampleSet.NEGATIVES_CONSIDERED);
			// values[0] = (float) getMonoValue(); //Monovalue uses values[0,1]
			values[1] = computeSizeValue(programSize, programLeaves);
			break;
		default:
			LOGGER.severe("OBJECTIVES_CONSIDERED out of bounds. Must be 1, 2 or 3.");
			break;
		}
		modified = false;
	}
	
	/**
	 * Compute a value for the size objective :<br/>
	 * 1.0.... [ > >  EXPECTED_NUMBER_OF_RULES > >   ]0.0......
	 * @param numberOfRules 
	 * @return A float included in [0.0 .. 1.0]
	 */
	public static float computeSizeValue(int numberOfRules, int numberOfLeaves){
		int comparator = COMPARE_LEAVES?numberOfLeaves:numberOfRules;
		
		if(comparator < EXPECTED_NUMBER_OF_RULES[0] && comparator > ABSOLUTE_SIZE_THRESHOLD)
			return 1.0f;
		if(comparator > EXPECTED_NUMBER_OF_RULES[1] || comparator < ABSOLUTE_SIZE_THRESHOLD)
			return 0.000f;
		float res = 1-(float)(comparator-EXPECTED_NUMBER_OF_RULES[0])/(float)(EXPECTED_NUMBER_OF_RULES[1]-EXPECTED_NUMBER_OF_RULES[0]);
		if(res > 1 ) res = 1;
		return res;
	}

	public FireMap getFires() {
		return fires;
	}
	
	
	public void setFires(FireMap fm) {
		fires = new FireMap();
		fires.merge(fm);
		modified = true;
	}

	
	public HashSet<Constraint> getFiredConstraints() {
		HashSet<Constraint> ptsFired = new HashSet<>();
		for (Model m :  ExampleSet.getExamplesBeingUsed()) {
			if(fires.get(m) != null)
				for (Constraint ct : fires.get(m).keySet()) {
					if(fires.get(m).get(ct) > 0)
						ptsFired.add(ct);
				}
		}
		return ptsFired;
	}
	
	public void setProgramSize(int programSize) {
		this.programSize = programSize;
	}
	public int getProgramSize() {
		return programSize;
	}

	
	public boolean dominates(FitnessVector vv){
		boolean res = true;
		for (int i = 0; i < OBJECTIVES_CONSIDERED; i++)  //QQ soit i | vi >= vvi
			res &= values[i] >= (vv.values[i] + OBJECTIVE_SIGMA[i]);
		if(res)
			for (int i = 0; i < OBJECTIVES_CONSIDERED; i++)
				if(values[i] > (vv.values[i] + OBJECTIVE_SIGMA[i])) //Il existe i | vi > vvi
					return true;
		return false;
	}
	
	/**
	 * Average between %+ et %- 
	 * @return
	 */
	public double getMonoValue() {
		if(values[0] < 0){
			LOGGER.warning("Access mono computation before initialization !");
			return 0.0;
		}
		
		switch (OBJECTIVES_CONSIDERED) {
		case 3:
		case 2:
			float positiveRate = ((float)pp)/ (float)(pp+pn);//True positive rate
			float negativeRate = ((float)nn)/ (float)(nn+np);//True negative rate
			return (positiveRate * ExampleSet.POSITIVES_CONSIDERED + negativeRate * ExampleSet.NEGATIVES_CONSIDERED) 
					/ (ExampleSet.POSITIVES_CONSIDERED + ExampleSet.NEGATIVES_CONSIDERED);
		case 1:
			return (values[0]+values[1])/(2);
		default:
			LOGGER.severe("OBJECTIVES_CONSIDERED out of bounds. Must be 1, 2 or 3.");
			break;
		}
		return (values[0]+values[1]+values[2])/(3);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		FitnessVector fv = new FitnessVector();
		fv.setFires(this.getFires());
		for (int i = 0; i < values.length; i++) 
			fv.values[i] = values[i];
		
		fv.nn = nn;
		fv.np = np;
		fv.pn = pn;
		fv.pp = pp;
		fv.rawDiscrimination.putAll(rawDiscrimination);
		
		fv.programSize = programSize;
		fv.programLeaves = programLeaves;
		fv.update();
		return fv;
	}
	
	public float[] getAllValues() {
		return values;
	}
	public float[] getValues_Considered() {
		return Arrays.copyOf(values, FitnessVector.OBJECTIVES_CONSIDERED);
	}
	
	public double getValue(int objective) {
		return values[objective];
	}
	
	public void update() {
		if(modified)
			updateValues();
	}
	
	
	public String prettyPrint() {
		String log = toString();
		for (Model m : ExampleSet.getExamplesBeingUsed()) {
			HashMap<Constraint, Integer> hm = fires.get(m);
			if (hm != null) {
				log += "\n    + " + m + " -> {";
				short i = 0;
				for (Constraint pt : hm.keySet()) {
					if (hm.get(pt) > 0) {
						log += pt + " " + hm.get(pt) + ", ";
						i++;
					}
				}
				if (i > 0)
					log = log.substring(0, log.length() - 2);
			}
			log += "}";
		}
		return log;
	}
	
	public String printFires(Model m) {
		return printFires(m, true);
	}
	
	public String printFires(Model m, boolean printAffectedEObject) {
		String log = "";
		HashMap<Constraint, Integer> hm = fires.get(m);
		if (hm != null) {
			for (Constraint ct : hm.keySet()) {
				if (hm.get(ct) > 0) {
					log += "----#" + ct.getId() + " " + hm.get(ct) + " object" + (hm.get(ct) > 1 ? "s" : "") + " affected\n" + ct.getRawOCLConstraint();
					ArrayList<EObject> eos = fires.getFiredObjects(m, ct);
					if (printAffectedEObject && !eos.isEmpty()) {
						int i = 1;
						for (EObject eo : eos) {
							log += "\n-- " + (eos.size() > 1 ? i + "/" + eos.size() : "1") + " (" 	+ ct.getName() + ")\n" + ToolBox.printEStructuralFeatures(eo);
							i++;
						}
					}
					log += "\n----fin-" + ct.getId() +"\n\n";
				}
			}
		} else {
			log += "No fire.";
		}
		log += "";
		return log;
	}

	/**
	 * "[pp|pn np|nn]"
	 * @return
	 */
	public String printExpandedStat(){
		String res = "["+pp+"|"+pn+" "+np+"|"+nn+"]";
		return res;
	}
	/**
	 * "pp;pn;np;nn"
	 * @return
	 */
	public String printCSVStat(){
		String res = ""+pp+";"+pn+";"+np+";"+nn+"";
		return res;
	}

	
	@Override
	public String toString() {
		String res = "fv("+ToolBox.format2Decimals(values[0]);
		for (int i = 1; i < OBJECTIVES_CONSIDERED; i++) {
			res+= ","+ToolBox.format2Decimals(values[i]);
		}
		return res+") "+printExpandedStat();
	}
	
	public HashMap<Model, Oracle.OracleCmp> getCmps() {
		return rawDiscrimination;
	}
	public Oracle.OracleCmp getCmp(Model m) {
		return rawDiscrimination.get(m);
	}

	public void setTFIDFValue(Double value) {
		// TODO Field ? Values[2] ? (Dont't forget the size in values[1])
//		System.out.println("FitnessVector.setTFIDFValue("+(OBJECTIVES_CONSIDERED-1)+") "+Arrays.toString(values));
		values[OBJECTIVES_CONSIDERED-1] = value.floatValue()/TFIDF_NORMALIZE;
	}
	

}

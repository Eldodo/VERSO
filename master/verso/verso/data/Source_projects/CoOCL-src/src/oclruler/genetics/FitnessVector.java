package oclruler.genetics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.swing.plaf.metal.MetalIconFactory.FolderIcon16;
import javax.swing.text.StyledEditorKit.ForegroundAction;

import coocl.ocl.Contrainte;
import coocl.ocl.Program;
import utils.Config;
import utils.Utils;

public class FitnessVector {
	public final static Logger LOGGER = Logger.getLogger(FitnessVector.class.getName());


	public static int PONDERATION_NB_CHANGES = -1;
	public static int PONDERATION_SYNTAX_ERR = -1;
	public static int PONDERATION_INFO_LOSS = -1;
	

	public static int NUMBER_OF_OBJECTIVES = 3;//-> Pos - Neg - Size
	public static double OBJECTIVE_SIGMA[] = new double[]{0.0001, 0.0001, 0.0001};
	public static double OBJECTIVE_SIGMA_DEFAULT = 0.0001;
	
	public static String csvHeader = "";

	
	public static String getObjectiveDescription(int i){
		return OBJECTIVE.values()[i].getDescription();
	}
	
	public static void loadConfig() {
				
		csvHeader = "";
		switch (NUMBER_OF_OBJECTIVES) {
		case 3:
			csvHeader = OBJECTIVE.values()[2].getShortName();
		case 2:
			csvHeader = OBJECTIVE.values()[1].getShortName()+";"+csvHeader;
		case 1:
			csvHeader = OBJECTIVE.values()[0].getShortName()+";"+csvHeader;
			break;
		default:
			LOGGER.severe("NUMBER_OF_OBJECTIVES  out of bounds. Must be  < 3");
			System.exit(1);
			break;
		}
		
		
		PONDERATION_NB_CHANGES = Config.getIntParam("PONDERATION_NB_CHANGES");
		OBJECTIVE.NUMBER_OF_CHANGES.ponderation = PONDERATION_NB_CHANGES;
		PONDERATION_SYNTAX_ERR = Config.getIntParam("PONDERATION_SYNTAX_ERR");
		OBJECTIVE.NUMBER_OF_SYNTAX_ERRORS.ponderation = PONDERATION_SYNTAX_ERR;
		PONDERATION_INFO_LOSS  = Config.getIntParam("PONDERATION_INFO_LOSS");
		OBJECTIVE.INFORMATION_LOSS.ponderation = PONDERATION_INFO_LOSS;

		
		String objSigmas = Config.getStringParam("OBJECTIVE_SIGMA");
		String[] OBJECTIVE_SIGMA_s = objSigmas.split(" ");
		if (OBJECTIVE_SIGMA_s.length == 1) {
			try {
				for (int j = 0; j < NUMBER_OF_OBJECTIVES; j++)
					OBJECTIVE_SIGMA[j] = Double.parseDouble(OBJECTIVE_SIGMA_s[0]);
			} catch (NumberFormatException e) {
				LOGGER.warning("OBJECTIVE_SIGMA '" + objSigmas + "' is not valid. Expected is either 1 unique or " + NUMBER_OF_OBJECTIVES + " different values (double). " + e.getMessage());
				for (int j = 0; j < NUMBER_OF_OBJECTIVES; j++)
					OBJECTIVE_SIGMA[j] = OBJECTIVE_SIGMA_DEFAULT;
			}
		} else {

			OBJECTIVE_SIGMA = new double[Math.max(NUMBER_OF_OBJECTIVES, OBJECTIVE_SIGMA_s.length)];
			boolean warningMalformed = false;
			for (int j = 0; j < NUMBER_OF_OBJECTIVES; j++) {
				try {
					OBJECTIVE_SIGMA[j] = Double.parseDouble(OBJECTIVE_SIGMA_s[j]);
				} catch (Exception e) {
					if(!warningMalformed){
						LOGGER.warning("OBJECTIVE_SIGMA '"+objSigmas+"' is not valid. Expected is either 1 unique or "+NUMBER_OF_OBJECTIVES+" different values (double).");
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
	
	
	private FitnessVector() {
		values = new float[NUMBER_OF_OBJECTIVES];
		for (int i = 0; i < values.length; i++) 
			values[i] = -1;
	}
	
	public FitnessVector(Program prg) {
		this();
		updateValues(prg);
	}
	
	private void updateValues(Program prg) {
		for (OBJECTIVE o : OBJECTIVE.values()) 
			values[o.getIdx()] = computeValue(o, prg);
	}

	public enum OBJECTIVE { 
		NUMBER_OF_CHANGES(0, "chgs", "Number of changes", PONDERATION_NB_CHANGES), 
		NUMBER_OF_SYNTAX_ERRORS(1, "errs", "Number of syntax errors", PONDERATION_SYNTAX_ERR), 
		INFORMATION_LOSS(2, "loss", "Information loss", PONDERATION_INFO_LOSS);
		
		private int idx;
		private String shortName;
		private String description;
		private int ponderation;
		
		OBJECTIVE(int idx, String shortName, String description, int ponderation){
			this.idx = idx;
			this.shortName = shortName;
			this.description = description;
			this.ponderation = ponderation;
		}
		
		public String getShortName() {
			return shortName;
		}
		public String getDescription() {
			return description;
		}
		public int getIdx() {
			return idx;
		}
		public int getPonderation() {
			return ponderation;
		}
		
		static OBJECTIVE getObjective(int idx){
			for (OBJECTIVE o : values()) 
				if(o.getIdx() == idx)
					return o;
			throw new IllegalArgumentException("Index '"+idx+"' out of scope = [0..2].");
		}
	}
	
	private float computeValue(OBJECTIVE objective, Program prg) {
		switch (objective) {
		case NUMBER_OF_CHANGES:
			float sum = 0;
			for (int n : prg.getNumberOfChanges()) 
				sum += n;
			return sum;
			
		case NUMBER_OF_SYNTAX_ERRORS:
			return prg.getSyntaxErrorCount();
			
		case INFORMATION_LOSS:
			sum = 0;
			for (int n : prg.getInformationLoss()) 
				sum += n;
			return sum;
		}
		return 0.0f;
	}
	
	
	public boolean dominates(FitnessVector vv){
		boolean res = true;
		for (int i = 0; i < NUMBER_OF_OBJECTIVES; i++)  //QQ soit i | vi >= vvi
			res &= values[i] <= (vv.values[i] + OBJECTIVE_SIGMA[i]);
		if(res)
			for (int i = 0; i < NUMBER_OF_OBJECTIVES; i++)
				if(values[i] < (vv.values[i] + OBJECTIVE_SIGMA[i])) //Il existe i | vi > vvi
					return true;
		return false;
	}
	
	/**
	 * Average between objectives
	 * @return
	 */
	public float getMonoValue() {
		if(values[0] < 0){
			LOGGER.warning("Access mono computation before initialization !");
			return 0.0f;
		}
		float sum = 0;
		int ponderationSum = 0;
		for (int i = 0; i < NUMBER_OF_OBJECTIVES; i++) {
			sum += values[i] * OBJECTIVE.getObjective(i).getPonderation();
			ponderationSum += OBJECTIVE.getObjective(i).getPonderation();
		}
		return sum / ponderationSum;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		FitnessVector fv = new FitnessVector();
		for (int i = 0; i < values.length; i++) 
			fv.values[i] = values[i];
		return fv;
	}
	
	public float[] getAllValues() {
		return values;
	}
	
	public double getValue(int objective) {
		return values[objective];
	}
	
	
	public String prettyPrint() {
		String log = toString();
		return log;
	}

	public String expandedStat(){
		String res = OBJECTIVE.getObjective(0).getShortName()+"="+String.format ("%.02f",values[0]);
		for (int i = 1 ; i < values.length ; i++) {
			res += ", "+OBJECTIVE.getObjective(i).getShortName()+"="+String.format ("%.02f",values[i]);
		}
		return "[("+String.format ("%.02f",getMonoValue())+") "+res+"]";
	}
	
	@Override
	public String toString() {
		String res = "fv["+String.format ("%.02f",getMonoValue())+"]("+String.format ("%.02f",values[0]);
		for (int i = 1; i < NUMBER_OF_OBJECTIVES; i++) {
			res+= ";"+String.format ("%.02f",values[i]);
		}
		return res+")";
	}

	public double euclidianToOptimum() {
		double res = 0.0;
		for (double d : values) {
			res += (d * d);
		}
		res = Math.sqrt(res);
		return res;
	}
	
}

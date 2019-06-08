package genetic.fitness;

import genetic.Entity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import models.Model;

import partition.composition.ModelFragment;
import utils.Config;

public class FitnessVector  {
	public final static int COVERAGE = 1, DISSIMILARITY = 0;
	
	public static String csvHeader = getObjectivesCsvHeader(';');//"DIS,COV"+((Config.NUMBER_OF_OBJECTIVES > 2)?",SIZ":"");
	
	
	static String[] objectivesName;
	static String[] objectivesType;
	static double[][] objectivesBrackets;
	
	Double[] values;

	private static String getObjectivesCsvHeader(char separator){
		String res = "";
		for (int i = 0 ; i < Config.NUMBER_OF_OBJECTIVES ; i++) {
			res+= getObjectiveName(i)+separator;
		}
		if(res.endsWith(""+separator))
			res = res.substring(0, res.length() - 1);
		return res;
	}
	
	 
	public FitnessVector(Entity e, HashMap<ModelFragment, Integer> covertureMap, float avgSizeModels) {
		this(e);
		computeFromModelFragment(covertureMap, e, avgSizeModels);
	}
	public FitnessVector(Entity e) {
		values = new Double[Config.NUMBER_OF_OBJECTIVES];
	}
	private FitnessVector(){
		values = new Double[Config.NUMBER_OF_OBJECTIVES];
	}
	public Double[] getValues() {
		return values;
	}
	
	public double getValue(int i){
		if(i<0 || i >= Config.NUMBER_OF_OBJECTIVES)
			throw new IllegalArgumentException("Wrong value : '"+i+"' when "+Config.NUMBER_OF_OBJECTIVES+" objectives available.");
		return values[i];
	}
	public double setValue(int obj, double v){
		if(obj<0 || obj >= Config.NUMBER_OF_OBJECTIVES)
			throw new IllegalArgumentException("Wrong value : '"+obj+"' when "+Config.NUMBER_OF_OBJECTIVES+" objectives available.");
		return values[obj] =  v;//cutSignificativeDecimals(v, NUMBER_OF_SIGNIFICATIVE_DECIMALS);
	}

	public double getCoverage() {
		return values[COVERAGE];
	}
	public void setCoverage(double d) {
		
		values[COVERAGE] = d;//cutSignificativeDecimals(d, NUMBER_OF_SIGNIFICATIVE_DECIMALS);
	}
	
	public double getDissimilarity() {
		return values[DISSIMILARITY];
	}
	public void setDissimilarity(double d) {
		values[DISSIMILARITY] = d;//cutSignificativeDecimals(d, NUMBER_OF_SIGNIFICATIVE_DECIMALS);
	}
	

	
	public int compareTo(FitnessVector fv) {
		System.out.println("FitnessVector.compareTo()");//Never accessed
		if(getCoverage() == fv.getCoverage())
			if(getDissimilarity() == fv.getDissimilarity())
				return 0;
			else
				return Double.compare(getDissimilarity(), fv.getDissimilarity());
		return Double.compare(getCoverage(), fv.getCoverage());
	}
	
	@Override
	public FitnessVector clone() {
		FitnessVector clone = new FitnessVector();
		for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) {
			clone.values[i] = this.values[i];
		}
		return clone;
	}

	@Override
	public String toString() {
		int c = (int)(getCoverage()*100);
		int d = (int)(getDissimilarity()*100);
		
		return "(COV:"+(c==100?"":"")+c+",DIS:"+(d==100?"":"")+d+")";
	}
	
	/**
	 * 
	 * @param covertureMap [ (MF -> how many times covered), (MF2 -> how many times covered)...]
	 */
	public void computeFromModelFragment(HashMap<ModelFragment, Integer> covertureMap, Entity e, float avgSizeModels) {
		int nbFrags = covertureMap.keySet().size();
		int nbFragsCovered = 0;
		double excessCovering = 0;
		
		ModelFragment[] mfs = (ModelFragment[]) covertureMap.keySet().toArray(new ModelFragment[covertureMap.keySet().size()]);
		
		Arrays.sort(mfs, new Comparator<ModelFragment>() {
			@Override
			public int compare(ModelFragment o1, ModelFragment o2) {
				return o1.prettyPrint().compareTo(o2.prettyPrint());
			}
		});
		for (ModelFragment mf : mfs) {
			int cover = covertureMap.get(mf);
//			System.out.println("FitnessVector.compute\t"+mf.prettyPrint()+" : \t"+cover);
			nbFragsCovered += (cover>0)?1:0;
			excessCovering += (cover>1)?cover-1:0;
		}
//		System.out.println("  -> covered :   "+nbFragsCovered);
//		System.out.println("  -> uncovered : "+(nbFrags-nbFragsCovered));
		excessCovering = (nbFragsCovered!=0)?excessCovering/nbFragsCovered:0.0;
		double coverage = (((double)nbFragsCovered)/nbFrags);
		setCoverage(coverage);
		
		float mfrt = (float)(nbFrags*Config.MFRT_COEF);
		
		double dissimilarity1 = (nbFragsCovered==0)?0.0: Math.max(0, 1-(excessCovering/(mfrt*(float)nbFragsCovered)));
		double dissimilarity2 =  Math.min(1, 1 - (double)((e.size()-Config.SIZE_MARGIN)*Config.SIZE_EMPHASIS )/(double)(nbFrags ));
		double dissimilarityRefined =  Math.min(1, 1 - (double)((e.sizeRefined()-Config.SIZE_MARGIN)*Config.SIZE_EMPHASIS-1)/(double)(nbFrags*avgSizeModels));

		switch(Config.DIS_OR_MIN){
			case "DIS":setDissimilarity(dissimilarity1);break;
			case "MIN":setDissimilarity(dissimilarity2);break;
			case "MIN_REFINED":setDissimilarity(dissimilarityRefined);break;
			default:
				throw new IllegalArgumentException("DIS_OR_MIN parameter must be 'DIS' or 'MIN'.");
		}
	}
	
	public boolean valueInBrackets(double v, String objName){
		return v >= FitnessObjectiveFactory.getMin(objName) && v <= FitnessObjectiveFactory.getMax(objName);
	}
	public double distanceToBrackets(int i) {
		double res = 0.0;
		if(values[i] <= objectivesBrackets[i][0] )
			res = objectivesBrackets[i][0] - values[i];
		else
			res = values[i] - objectivesBrackets[i][1];
		return res;
	}
	
	public boolean dominates(FitnessVector fv) {
		boolean res = true;
		for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) {
			
			switch (objectivesType[i]) {
				case FitnessObjectiveFactory.FO_TYPE_MAX:
					res &= values[i] >= fv.values[i]+Config.EPSILON;//QQst i this.Obj[i] >= fv.Obj[i]
					break;
				case FitnessObjectiveFactory.FO_TYPE_BRACKET:
					boolean v1 = valueInBrackets(values[i], objectivesName[i]);
					boolean v2 = valueInBrackets(fv.values[i], objectivesName[i]);
					res &= v1 || (!v1 && !v2 && (distanceToBrackets(i) <= fv.distanceToBrackets(i)) ) ;
					break;
				default:
					break;
			}
		}
		if(res) {
			for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) {
				switch (objectivesType[i]) {
				case FitnessObjectiveFactory.FO_TYPE_MAX:
					for (int j = 0; j < Config.NUMBER_OF_OBJECTIVES; j++)
						if( values[j] > fv.values[j]+Config.EPSILON)//Exist i this.Obj[i] > fv.Obj[i]
							return true;			
					break;
					
				case FitnessObjectiveFactory.FO_TYPE_BRACKET:
					for (int j = 0; j < Config.NUMBER_OF_OBJECTIVES; j++){
						boolean v1 = valueInBrackets(values[j], objectivesName[j]);
						boolean v2 = valueInBrackets(fv.values[j], objectivesName[j]);

						if(v1){
							if(!v2){// v1 and !v2 in brackets -> v1 dominates v2
								return true;
							}// else : v1 and v2 in brackets ->   !(v1 dominates v2) (not in the STRICT definition)
						} else {
							if (v2) {// !v1 and v2 in brackets
								return false;
							} else { //!v1 and !v2 in brackets
								if( distanceToBrackets(j) > fv.distanceToBrackets(j)){//Exist i this.Obj[i] > fv.Obj[i]
									return true;	
								}
							}
						}
					}	
					break;
				default:
					break;
				}
			}
		}
//		model_00018.xmi,model_00028.xmi,model_00041.xmi,model_00043.xmi,model_00085.xmi,model_00086.xmi,model_00094.xmi
		return false;
	}

	public static String getObjectiveName(int obj) {
		switch (obj) {
			case COVERAGE :
				return "COV";
			case DISSIMILARITY : 
				return Config.DIS_OR_MIN;//"DIS";
			default :
				throw new IllegalArgumentException("Invalid objective number : '"+obj+"' Must be 0, 1");
		}
	}
	
	/**
	 * @param d
	 * @param nbSign
	 * @return
	 */
	private static double cutSignificativeDecimals(double d, int nbSignificativeDecimals) {
		if(nbSignificativeDecimals <= 0) return d;
		int testInt = (int)Math.round(d * Math.pow(10, nbSignificativeDecimals));
		double testRes = testInt /  Math.pow(10, nbSignificativeDecimals);
		return testRes;
	}

	public static void setObjectiveType(int i, String foType) {
		if(objectivesType == null)
			objectivesType = new String[Config.NUMBER_OF_OBJECTIVES];
		objectivesType[i] = foType;
	}


	public static void setObjectiveBrackets(int i, double min, double max) {
		if(objectivesBrackets == null)
			objectivesBrackets = new double[Config.NUMBER_OF_OBJECTIVES][2];
		objectivesBrackets[i][0] = min;
		objectivesBrackets[i][1] = max;
	}


	public static void setObjectiveName(int i, String name) {
		if(objectivesName == null)
			objectivesName = new String[Config.NUMBER_OF_OBJECTIVES];
		objectivesName[i] = name;
	}

	
}

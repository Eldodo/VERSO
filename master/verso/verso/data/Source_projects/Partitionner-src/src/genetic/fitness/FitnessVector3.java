package genetic.fitness;

import genetic.Entity;

import java.util.HashMap;

import partition.composition.ModelFragment;
import utils.Config;

public class FitnessVector3  {
	public final static int COVERAGE = 1, DISSIMILARITY = 0, SIZE = 2;
	
	public static String csvHeader = getObjectivesCsvHeader(';');//"DIS,COV"+((Config.NUMBER_OF_OBJECTIVES > 2)?",SIZ":"");
	
	private static String getObjectivesCsvHeader(char separator){
		String res = "";
		for (int i = 0 ; i < Config.NUMBER_OF_OBJECTIVES ; i++) {
			res+= getObjectiveName(i)+separator;
		}
		if(res.endsWith(""+separator))
			res = res.substring(0, res.length() - 1);
		return res;
	}
	
	FitnessObjective[] values;
	 
	public FitnessVector3(Entity e, HashMap<ModelFragment, Integer> covertureMap) {
		this(e);
		computeFromModelFragment(covertureMap, e);
	}
	
	public FitnessVector3(Entity e) {
		values = FitnessObjectiveFactory.getNewVector();
		double size = e.size();
		if(Config.NUMBER_OF_OBJECTIVES > 2)
			setSize(size);
	}
	private FitnessVector3(){
		values = new FitnessObjectiveMax[Config.NUMBER_OF_OBJECTIVES];
	}
	public FitnessObjective[] getValues() {
		return values;
	}
	
	public double getValue(int i){
		if(i<0 || i >= Config.NUMBER_OF_OBJECTIVES)
			throw new IllegalArgumentException("Wrong value : '"+i+"' when "+Config.NUMBER_OF_OBJECTIVES+" objectives available.");
		return values[i].getValue();
	}
	public double setValue(int obj, double v){
		if(obj<0 || obj >= Config.NUMBER_OF_OBJECTIVES)
			throw new IllegalArgumentException("Wrong value : '"+obj+"' when "+Config.NUMBER_OF_OBJECTIVES+" objectives available.");
		return values[obj].setValue(v);//cutSignificativeDecimals(v, NUMBER_OF_SIGNIFICATIVE_DECIMALS);
	}

	public double getCoverage() {
		return values[COVERAGE].getValue();
	}
	public void setCoverage(double d) {
		
		values[COVERAGE].setValue(d);//cutSignificativeDecimals(d, NUMBER_OF_SIGNIFICATIVE_DECIMALS);
	}
	
	public double getDissimilarity() {
		return values[DISSIMILARITY].getValue();
	}
	public void setDissimilarity(double d) {
		values[DISSIMILARITY].setValue(d);//cutSignificativeDecimals(d, NUMBER_OF_SIGNIFICATIVE_DECIMALS);
	}
	
	public double getSize() {
		return values[SIZE].getValue();
	}
	public void setSize(double d) {
		values[SIZE].setValue(d);//cutSignificativeDecimals(d, NUMBER_OF_SIGNIFICATIVE_DECIMALS);
	}
	
	
	public int compareTo(FitnessVector3 fv) {
		System.out.println("FitnessVector3.compareTo()");//Never accessed
		if(getCoverage() == fv.getCoverage())
			if(getDissimilarity() == fv.getDissimilarity())
				if(Config.NUMBER_OF_OBJECTIVES > 2)
					return Double.compare(getSize(), fv.getSize());
				else 
				return 0;
			else
				return Double.compare(getDissimilarity(), fv.getDissimilarity());
		return Double.compare(getCoverage(), fv.getCoverage());
	}
	
	@Override
	public FitnessVector3 clone() {
		FitnessVector3 clone = new FitnessVector3();
		for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) {
			clone.values[i].setValue(this.values[i].getValue());
		}
		return clone;
	}

	@Override
	public String toString() {
		int c = (int)(getCoverage()*100);
		int d = (int)(getDissimilarity()*100);
		
		
		String res = "(COV:"+(c==100?"":"")+c+",DIS:"+(d==100?"":"")+d;
		if(Config.NUMBER_OF_OBJECTIVES > 2)
			return res+",SIZ:"+((int)(getSize()*100)==100?"":".")+(int)(getSize()*100)+")";
		else
			return res +")";
	}
	
	/**
	 * 
	 * @param covertureMap [ (MF -> how many times covered), (MF2 -> how many times covered)...]
	 */
	public void computeFromModelFragment(HashMap<ModelFragment, Integer> covertureMap, Entity e) {
		int nbFrags = covertureMap.keySet().size();
		int nbFragsCovered = 0;
		double excessCovering = 0;
		for (ModelFragment mf : covertureMap.keySet()) {
			int cover = covertureMap.get(mf);
			nbFragsCovered += (cover>0)?1:0;
			excessCovering += (cover>1)?cover-1:0;
		}
		excessCovering = (nbFragsCovered!=0)?excessCovering/nbFragsCovered:0.0;
		setCoverage((((double)nbFragsCovered)/nbFrags));
		
		float mfrt = (float)(nbFrags*Config.MFRT_COEF);
		
		double dissimilarity1 = (nbFragsCovered==0)?0.0: Math.max(0, 1-(excessCovering/(mfrt*(float)nbFragsCovered)));
		double dissimilarity2 =  Math.min(1, 1 - (double)((e.size()-Config.SIZE_MARGIN)*Config.SIZE_EMPHASIS - 1)/(double)(nbFrags - 1));
		switch(Config.DIS_OR_MIN){
			case "DIS":setDissimilarity(dissimilarity1);break;
			case "MIN":setDissimilarity(dissimilarity2);break;
			default:
				throw new IllegalArgumentException("DIS_OR_MIN parameter must be 'DIS' or 'MIN'.");
		}
	}
	
	
	public boolean dominates(FitnessVector3 fv) {
		boolean res = true;
		
		
		for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) {
			//res &= values[i] >= fv.values[i]+Config.EPSILON;//QQst i this.Obj[i] >= fv.Obj[i]
			res &= values[i].dominatesRelax(fv.values[i].getValue());
		}
		
		if(res) {
			for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++)
				if(  values[i].dominates(fv.values[i].getValue()))//(values[i] > fv.values[i]+Config.EPSILON)//Exist i this.Obj[i] > fv.Obj[i]
					return true;
		}
		
		return false;
	}

	public static String getObjectiveName(int obj) {
		switch (obj) {
			case COVERAGE :
				return "COV";
			case DISSIMILARITY : 
				return Config.DIS_OR_MIN;//"DIS";
			case SIZE : 
				return "SIZ";
			default :
				throw new IllegalArgumentException("Invalid objective number : '"+obj+"' Must be 0, 1 or 2");
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


	
}


package oclruler.genetics;

import java.util.ArrayList;
import java.util.Comparator;


import utils.Utils;

public abstract class GeneticEntity implements Comparable<GeneticEntity> {
	protected FitnessVector fitnessVector;

	double distance;
	int rank;
	double monoValue = 0.0;

	public GeneticEntity() {
	}

	public abstract GeneticEntity[] crossover(GeneticEntity e) throws UnstableStateException;
	public abstract GeneticEntity[] crossoverDeep(GeneticEntity e) throws UnstableStateException;

	public abstract boolean mutate() throws UnstableStateException;

	public abstract int size();

	public abstract String prettyPrint(String tab);
	public abstract String simplePrint(String tab);
	public String prettyPrint() {
		return prettyPrint("");
	}
	public String simplePrint() {
		return simplePrint("");
	}

	/**
	 * Print result [time +separator ] COV + separator + DIS + separator + nbModels [endChar]
	 * @param separator
	 * @param endChar 
	 * @param timeStamp
	 * @return
	 */
	public String printNumericResult(String separator, String timeStamp, String elapsedTime, boolean formatDecimals){
		String res = "";
		if(fitnessVector == null){
			Evolutioner.LOGGER.warning("Printing result : No results yet.");
			return "";
		}
		if(timeStamp != null && !timeStamp.isEmpty())
			res = timeStamp+separator;
		
		for (int i = 0; i < FitnessVector.NUMBER_OF_OBJECTIVES; i++) {
			if(formatDecimals)	res += String.format ("%.02f", (float)fitnessVector.getValue(i))+separator;
			else res += fitnessVector.getValue(i)+separator;
		}
		
		return res;//+"%+;%-;elapsedTime;nbRules";
	}
	
	public static String printResultHeader(String separator) {
		return "[TimeStamp"+separator+"]%+"+separator+"%-"+separator+"[elapsedTime"+separator+"]nbRules\n";
	}
	
	
	public FitnessVector getFitnessVector() {
		return fitnessVector;
	}
	public void clearFitnessVector() {
		this.fitnessVector = null;
	}
	public boolean hasFitnessVector(){
		return fitnessVector != null;
	}

	public void setFitnessVector(FitnessVector fitnessVector) {
		this.fitnessVector = fitnessVector;
		
		setMonoValue(fitnessVector);
		modified = false;
	}

	public double getMonoValue() {
		return fitnessVector.getMonoValue();
	}

	public void setMonoValue(FitnessVector fv) {
		this.monoValue = fv.getMonoValue();
	}

	public abstract ArrayList<? extends Gene> getGenes() ;

	public abstract boolean addGene(Gene g) ;
	
	public abstract double getDistance(GeneticEntity g);

	@Override
	public int compareTo(GeneticEntity o) {
		return fitnessVector.dominates(o.fitnessVector) ? 1 : 0;
	}

	@Override
	public abstract GeneticEntity clone();

	public boolean dominates(GeneticEntity ent2) {
		if (ent2 == null) 
			return true;
		return fitnessVector.dominates(ent2.fitnessVector);
	}

	@Override
	public String toString() {
		return "<"+getName()+": mono="+(fitnessVector != null ? String.format ("%.02f",fitnessVector.getMonoValue()):"<no evaluation yet>")+">";
	}

	public static Comparator<GeneticEntity> getDescendantRankComparator() {
		return new Comparator<GeneticEntity>() {
			@Override
			public int compare(GeneticEntity o1, GeneticEntity o2) {
				boolean res = o1.rank == o2.rank;
				if (res)
					return Double.compare(o2.distance, o1.distance);
				return Double.compare(o1.rank, o2.rank);
			}
		};
	}

	public static Comparator<GeneticEntity> getDistanceComparator() {
		return new Comparator<GeneticEntity>() {
			@Override
			public int compare(GeneticEntity o1, GeneticEntity o2) {
				return Double.compare(o1.distance, o2.distance);
			}
		};
	}

	public static Comparator<GeneticEntity> getMonoValueComparator() {
		return new Comparator<GeneticEntity>() {
			@Override
			public int compare(GeneticEntity o1, GeneticEntity o2) {
				if(o2 == null && o1 == null) return 0;;
				return Double.compare(o2.getMonoValue(), o1.getMonoValue());
			}
		};
	}
	public static Comparator getMinMaxValueComparator(double[] maxs) {
		return new Comparator<GeneticEntity>() {
			@Override
			public int compare(GeneticEntity o1, GeneticEntity o2) {
				if(o2 == null && o1 == null) return 0;
				
				double v1 = 0, v2 = 0;
				for (int i = 0; i < FitnessVector.NUMBER_OF_OBJECTIVES; i++) {
					v1 += (o1.getFitnessVector().getValue(i) / maxs[i]);
					v2 += (o2.getFitnessVector().getValue(i) / maxs[i]);
				}
				v1 = v1 / FitnessVector.NUMBER_OF_OBJECTIVES;
				v2 = v2 / FitnessVector.NUMBER_OF_OBJECTIVES;
				return Double.compare(v2, v1);
			}
		};
	}

	public int getRank() {
		return rank;
	}

	public double getDistance() {
		return distance;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String printStats() {
		int d = (int) (100 * distance);
		return fitnessVector + " rd(" + rank + "," + d + ")";
	}
	
	public String printRank() {
		String d = ""+ (int) (100 * distance);
		if(distance == Integer.MAX_VALUE)
			d = "MAX";
		return " rd(" + rank + "," + d + ")";
	}

	public String printFV() {
		return fitnessVector.toString();
	}

	public abstract String printResultPane() ;

	/**
	 * A la creation, program IS modified
	 */
	protected boolean modified = true;
	public boolean isModified() {
		return modified;
	}

	public boolean isEmpty() {
		return getGenes().isEmpty();
	}

	public abstract String getName() ;


}


package oclruler.genetics;

import java.util.ArrayList;
import java.util.Comparator;

import oclruler.utils.ToolBox;

public abstract class GeneticIndividual implements Comparable<GeneticIndividual> {
	protected FitnessVector fitnessVector;

	double distance;
	int rank;
	double monoValue = 0.0;
	
	/**
	 * A la creation, program IS modified
	 */
	protected boolean modified = true;


	public GeneticIndividual() {
	}

	public abstract GeneticIndividual[] crossover(GeneticIndividual e) throws UnstableStateException;
	public abstract GeneticIndividual[] crossoverDeep(GeneticIndividual e) throws UnstableStateException;

	public abstract boolean mutate() throws UnstableStateException;

	public abstract int size();
	public abstract int getNumberOfLeaves();

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
		for (int i = 0; i < FitnessVector.OBJECTIVES_CONSIDERED; i++) {
			if(formatDecimals)	res += ToolBox.format2Decimals((float)fitnessVector.getValue(i))+separator;
			else res += fitnessVector.getValue(i)+separator;
		}
		
		res += getNumberOfLeaves();
		
//		int totalRules = 0;
//		ArrayList<Gene> ms = new ArrayList<>(getGenes());
//		for (Gene m : ms) {
//			totalRules += m.size();
//		}
//		float avgRules = (float)totalRules / size();
//		
//		if(formatDecimals)
//			res += separator+Utils.format2Decimals(avgRules);
//		else
//			res +=  separator+avgRules;
		
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
		fv.setProgramSize(size());
		this.monoValue = fv.getMonoValue();
	}

	public abstract ArrayList<? extends Gene> getGenes() ;

	public abstract boolean addGene(Gene g) ;

	@Override
	public int compareTo(GeneticIndividual o) {
		return fitnessVector.dominates(o.fitnessVector) ? 1 : 0;
	}

	@Override
	public abstract GeneticIndividual clone();

	public boolean dominates(GeneticIndividual ent2) {
		if (ent2 == null) {
			return true;
		}
		return fitnessVector.dominates(ent2.fitnessVector);
	}

	@Override
	public String toString() {
		return "(" + getGenes().size() + " genes, FV=" + fitnessVector + ")";
	}

	public static Comparator<GeneticIndividual> getDescendantRankComparator() {
		return new Comparator<GeneticIndividual>() {
			@Override
			public int compare(GeneticIndividual o1, GeneticIndividual o2) {
				boolean res = o1.rank == o2.rank;
				if (res)
					return Double.compare(o2.distance, o1.distance);
				return Double.compare(o1.rank, o2.rank);
			}
		};
	}

	public static Comparator<GeneticIndividual> getDistanceComparator() {
		return new Comparator<GeneticIndividual>() {
			@Override
			public int compare(GeneticIndividual o1, GeneticIndividual o2) {
				return Double.compare(o1.distance, o2.distance);
			}
		};
	}

	public static Comparator<GeneticIndividual> getMonoValueComparator() {
		return new Comparator<GeneticIndividual>() {
			@Override
			public int compare(GeneticIndividual o1, GeneticIndividual o2) {
				if(o2 == null && o1 == null) return 0;;
				return Double.compare(o2.getMonoValue(), o1.getMonoValue());
			}
		};
	}
	public static Comparator<GeneticIndividual> getValueComparator(int obj) {
		return new Comparator<GeneticIndividual>() {
			@Override
			public int compare(GeneticIndividual o1, GeneticIndividual o2) {
				if(o2 == null && o1 == null) return 0;
				if(o2.getFitnessVector() == null || o1.getFitnessVector() == null) return 0;
				return Double.compare(o2.getFitnessVector().getValue(obj), o1.getFitnessVector().getValue(obj));
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
		return fitnessVector.toString();
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

	public abstract String printResultPane(String tab) ;
	public String printResultPane() {
		return printResultPane(ToolBox.TAB_CHAR);
	}

	public boolean isModified() {
		return modified;
	}

	public boolean isEmpty() {
		return getGenes().isEmpty();
	}

	public abstract String getName() ;

	public void setModified() {
		modified = true;
	}

}

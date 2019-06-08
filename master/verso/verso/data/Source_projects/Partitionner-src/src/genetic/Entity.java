package genetic;

import genetic.fitness.FitnessVector;

import java.util.ArrayList;
import java.util.Comparator;

public abstract class Entity implements Comparable<Entity> {
	protected FitnessVector fitnessVector;
	private ArrayList<Gene> genes;

	double distance;
	int rank;
	double monoValue = 0.0;
	
	public Entity() {
		genes = new ArrayList<>();
	}
	
	
	public abstract  Entity[] crossover(Entity e);
	public abstract  boolean mutate();
	public abstract  FitnessVector evaluate(Evaluator ev);
	public abstract  int size();
	public abstract  int sizeRefined();
	public abstract  String printResult(String separator, String timeStamp, String elapsedTime, boolean formatDecimals);
	public abstract  String printResultHeader(String separator);
	

	
	public FitnessVector getFitnessVector() {
		return fitnessVector;
	}
	
	public void setFitnessVector(FitnessVector fitnessVector) {
		this.fitnessVector = fitnessVector;
		setMonoValue(fitnessVector);
	}

	public double getMonoValue() {
		return monoValue;
	}
	public void setMonoValue(FitnessVector fv) {
		this.monoValue = (fv.getCoverage() + fv.getDissimilarity() )/2;
	}
	public static String getMonoValueString() {
		return "(COV+DIS)/2";
	}

	
	public ArrayList<? extends Gene> getGenes() {
		return genes;
	}
	
	public void setGenes(ArrayList<Gene> genes) {
		this.genes = genes;
	}
	
	public boolean addGene(Gene g){
		return genes.add(g);
	}
	
	@Override
	public int compareTo(Entity o) {
		return fitnessVector.dominates(o.fitnessVector)?1:0;
	}
	
	@Override
	public abstract Entity clone() ;
	

	public boolean dominates(Entity ent2) {
		if(ent2 == null){
			return true;
		}
		return fitnessVector.dominates(ent2.fitnessVector);
	}
	
	@Override
	public String toString() {
		return "("+genes.size()+" genes, FV="+fitnessVector+")";
	}
	
	
	public static Comparator<Entity> getDescendantRankComparator(){
		return new Comparator<Entity>() {
			@Override
			public int compare(Entity o1, Entity o2) {
				boolean res = o1.rank == o2.rank;
				if(res)
					return Double.compare(o2.distance, o1.distance);
				return Double.compare(o1.rank, o2.rank);
			}
		};
	}
	public static Comparator<Entity> getDistanceComparator(){
		return new Comparator<Entity>() {
			@Override
			public int compare(Entity o1, Entity o2) {
				return Double.compare(o1.distance, o2.distance);
			}
		};
	}
	public static Comparator<Entity> getMonoValueComparator(){
		return new Comparator<Entity>() {
			@Override
			public int compare(Entity o1, Entity o2) {
				return Double.compare(o2.getMonoValue(), o1.getMonoValue());
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
		int d = (int)(100*distance);
		return fitnessVector+" rd("+rank+","+d+")";
	}


	public void nullify() {
		genes = null;
		fitnessVector = null;
	}






}

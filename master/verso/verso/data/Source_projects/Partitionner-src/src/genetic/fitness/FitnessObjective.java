package genetic.fitness;

import utils.Config;

public abstract class FitnessObjective {
	
	
	double value;
	String name;
	
	public FitnessObjective(String name) {
		this.name = name;
	}
	public FitnessObjective() {
		this.name = "NO_NAME";
	}
	public double getValue() {
		return value;
	}
	public String getName() {
		return name;
	}
	
	public double setValue(double v) {
		this.value = v;
		return v;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	abstract public boolean dominatesRelax(double v) ;
	
	abstract public  boolean dominates(double v) ;
	
	
}

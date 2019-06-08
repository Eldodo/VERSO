package genetic.fitness;

import utils.Config;

public class FitnessObjectiveMax extends FitnessObjective {
	
	public FitnessObjectiveMax() {
		super();
	}
	public FitnessObjectiveMax(String name) {
		super(name);
	}
	
	@Override
	public  boolean dominatesRelax(double value2) {
		boolean res = false;
		res = getValue() >= value2 + Config.EPSILON;
		return res;
	}
	
	@Override
	public boolean dominates(double value2) {
		boolean res = false;
		res = (getValue() > value2 + Config.EPSILON);//Exist i this.Obj[i] > fv.Obj[i]
		
		return res;
	}
	
	
}

package genetic.fitness;

import utils.Config;

public class FitnessObjectiveBracket extends FitnessObjective {
	
	double min, max;	
	public FitnessObjectiveBracket() {
		super();
	}

	
	public FitnessObjectiveBracket(String name) {
		super(name);
		min = FitnessObjectiveFactory.getMin(name);
		max = FitnessObjectiveFactory.getMax(name);
	}
	
	public void setMin(double min) {
		this.min = min;
	}
	public void setMax(double max) {
		this.max = max;
	}
	
	public double getMin() {
		return min;
	}
	public double getMax() {
		return max;
	}
	
	
	@Override
	public  boolean dominatesRelax(double v2) {
		boolean res = false;
		double v1 = getValue();
		
		boolean v1InBrackets = v1 >= min && v1 <= max;
		boolean v2InBrackets = v2 >= min && v2 <= max;
		
		
		if ( v1InBrackets ) return true;					// Si v1 est dans les brackets --> true
		else if	( v2InBrackets ) return false;
		else if	( !v2InBrackets ){	// Si v1 out of brackets et v2 out aussi. (== égalité ?)
			double centre = (max+min)/2;
			return  Math.abs(v1-centre) >= Math.abs(v2-centre); // Comparaison de la distance au centre du bracket.
		}
		return false;//ARBITRAIRE ATTENTION. Supposed to be right.
		//res = getValue() >= v2 + Config.EPSILON;// <-- MAX
	}
	
	@Override
	public boolean dominates(double v2) {
		double v1 = getValue();
		
		boolean v1InBrackets = v1 >= min && v1 <= max;
		boolean v2InBrackets = v2 >= min && v2 <= max;
		
		
		if (v1InBrackets && !v2InBrackets) return true;	// Si v1 in brackets et v2 out --> true
		else if (v1InBrackets && v2InBrackets) return false;	// Pas de dominaton stricte --> false
		else if (!v1InBrackets && v2InBrackets) return false;	// Si v1 out et v2 in --> false
		
		else if	(!v1InBrackets && !v2InBrackets){	// Si v1 out of brackets et v2 out aussi. (== égalité ?)
			double centre = (max+min)/2;
			return  Math.abs(v1-centre) > Math.abs(v2-centre); // Comparaison de la distance au centre du bracket.
		}
		return false;
	}
	
	
}

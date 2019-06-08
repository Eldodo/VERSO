package genetic;

import java.util.HashSet;

import partition.composition.ModelFragment;

import genetic.fitness.FitnessVector;


public interface Evaluator {
	/**
	 * Evaluates the coverage and affect the fitnessVector to the entity passed in parameter.
	 * @param entity
	 * @return
	 */
	public FitnessVector evaluateCoverage(Entity entity);
	public HashSet<ModelFragment> getUncovereds();
}

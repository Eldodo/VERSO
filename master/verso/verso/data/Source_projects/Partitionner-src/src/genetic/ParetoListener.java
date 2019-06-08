package genetic;

public interface ParetoListener {
	void notify(Population p, int generation, float avgGenes, float avgGeneSize, long timePassed);
	void notifyEnd(Population currentPopulation, int currentGeneration, float avgGenes, float avgGeneSize, long timePassed);
}

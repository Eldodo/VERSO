package ui;

import genetic.Entity;
import genetic.Gene;
import genetic.ParetoListener;
import genetic.Population;
import genetic.Population.Pareto;

import java.util.ArrayList;

import utils.Config;

public class XYLineChartTime extends XYLineChart2 implements ParetoListener {
	private static final long serialVersionUID = 5213168864997366910L;

	public XYLineChartTime(String metamodelName, String timeStamp) {
		super(metamodelName, timeStamp);
		xAxisLabel = "Time";
	}

	@Override
	public void notify(Population p, int generation, float avgGenes, float avgGeneSize, long timePassed) {
		
		Entity best = p.getBest();
		if(best == null) {
			Ui.LOGGER.warning("Best entity not affected yet.");
			return;
		}

		Pareto pa = p.getFrontPareto();
		
		
		Entity[] ets;
		if(pa == null){
			ets = new Entity[p.size()];
			p.getEntities().toArray(ets);
		} else {
			ets = new Entity[pa.size()];
			pa.getEntities().toArray(ets);
		}
		
		
		int withinEval = 0;
		ArrayList<Entity> etsClean = new ArrayList<>(ets.length);
		for (Entity e : p.getEntities()) 
			if(e.getFitnessVector() == null){
				withinEval++;
			} else 
				etsClean.add(e);
		
		if(withinEval > 0 )
			Ui.LOGGER.warning(withinEval+ " entities not evaluated yet");
		
		
		float avgs[] = new float[Config.NUMBER_OF_OBJECTIVES];
		
		float[] sums = new float[Config.NUMBER_OF_OBJECTIVES];
		avgs = new float[Config.NUMBER_OF_OBJECTIVES];

		int 	nbEntities = etsClean.size();

		int 	sumGenes = 0;
		int 	sumGenesSize = 0;
		float 	avgGenes2 = 0;

		for (int i = 0; i < sums.length; i++) {
			sums[i] = 0; 
			avgs[i] = 0;
			for (Entity e : etsClean) {//Mono = ALL population entities | Multi = Pareto-front entities
				sumGenes += e.getGenes().size();
				for (Gene g : e.getGenes()) 
					sumGenesSize += g.size();
				
				double v = e.getFitnessVector().getValue(i);

				sums[i] += v;
			}
		}
		for (int i = 0; i < sums.length; i++) 
			avgs[i] = sums[i] / nbEntities;


		avgGenes2 = sumGenes / (float)nbEntities;
		float avgGeneSize2 = sumGenesSize / (float)sumGenes;
		//			System.out.println(timePassed+" "+generation+":"+ best.getFitnessVector()+"  avgGenes:"+avgGenes);

		for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++){ 
			addObjective((int)timePassed/1000, avgs[i], best!=null?best.getFitnessVector().getValue(i):0.0, i);//timePassed is passed in millis. On chart, better in seconds.
		}
		
		addToGraph2(generation, avgGeneSize2);
		addToGraph3(generation, avgGenes2);
		addToGraph4(generation, p.getParetos().size());
		addToGraph5(generation, nbEntities);

	}

	@Override
	public void notifyEnd(Population currentPopulation, int currentGeneration, float avgGenes, float avgGeneSize, long timePassed) {
		notify(currentPopulation, currentGeneration, avgGenes, avgGeneSize, timePassed);
	}

}

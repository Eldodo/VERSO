import genetic.Entity;
import genetic.Evolutioner;
import genetic.Gene;
import genetic.Population;
import genetic.fitness.FitnessVector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import models.Model;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import partition.PartitionModel;
import partition.composition.FragmentSet;
import partition.ocl.OCLPartitionModel;
import utils.Config;
import utils.Utils;

public class MainRawRandom {
	public final static Logger LOGGER = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {
		Utils.init();
		int nbEnt = Population.NB_ENTITIES_IN_POP;
		if(Population.NB_ENTITIES_IN_POP > 2){
			Population.NB_ENTITIES_IN_POP = 2;
			LOGGER.warning("Number of individus in pop is set to 1");
		}


		for (int j = 0; j < 50; j++) {
			Utils.resourceSet = new ResourceSetImpl();//Used to load XMI and create model representations
			for (EPackage ep : Utils.ePackages()) {
				Utils.resourceSet.getPackageRegistry().put(
				    		ep.getNsURI(), ep
			        );
			}
			
			LOGGER.info("Entering Partitioner.");

			if(!Config.VERBOSE_ON_UI)
				LOGGER.warning("Graph verbose disabled.");


			PartitionModel partitionModel;
			if(Config.FRAGMENT_WITH_OCL)
				partitionModel = new OCLPartitionModel();
			else
				partitionModel = new PartitionModel();

			partitionModel.extractPartition();

			FragmentSet evo = Config.loadFragmentSet(partitionModel);

			
			long startTime = System.currentTimeMillis();
			double[] maxObjs = new double[Config.NUMBER_OF_OBJECTIVES];;
			Entity[] maxModels = new Entity[Config.NUMBER_OF_OBJECTIVES];
			Entity maxAllModel = null;
			float avgGenes =0, distanceAvg =0;
			Population p = null, pBest = null;
			
			 
			int bound = Population.NB_GENES_IN_ENTITIES*2;
			float[] sums = new float[Config.NUMBER_OF_OBJECTIVES];
			float[] avgs = new float[Config.NUMBER_OF_OBJECTIVES];
			float[] avgs_buf = new float[Config.NUMBER_OF_OBJECTIVES];
			ResourceSet rsGeneration;
			Entity bestGModel = null;
			
			for (int generation = 1; generation <= Evolutioner.GENERATION_MAX*(nbEnt/10); generation++) {
				
		 		rsGeneration = new ResourceSetImpl();
				for (EPackage ep : Utils.ePackages()) {
					rsGeneration.getPackageRegistry().put(
					    		ep.getNsURI(), ep
				        );
				}


				Population.NB_GENES_IN_ENTITIES = Utils.getRandomInt(1,bound);
				p  = Population.createRandomPopulation(rsGeneration);

				p.evaluate(evo);

				p.fastNonDominantSort();
				p.crowdingDistanceAssignement();

				if(generation ==1)
					bestGModel = p.getBest();
				
				Entity maxGModel = null;
				
				/*
				 * LOG, FILE and UI
				 */
					int nbEntities = p.getFrontPareto().size();
					int 	sumGenes = 0;
					int 	sumGenesSize = 0;
					distanceAvg = 0;
					int 	nbDist = 0;//Used to remove the boundaries (2*nb_objectives in fitFunc)
					for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) {
						maxModels[i] =p.getEntities().get(0);
					}
					for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) {
						avgs_buf[i] = avgs[i];//get rid of the falling curbs (when #models == 0)
						sums[i] = 0; 
						avgs[i] = 0;
	
						for (Entity e : p.getFrontPareto().getEntities()) {
							sumGenes += e.getGenes().size();
							for (Gene g : e.getGenes()) 
								sumGenesSize += g.size();
	
							sums[i] += e.getFitnessVector().getValue(i);
							if(i==0 && e.getDistance() < (Integer.MAX_VALUE-1000) ){
								nbDist++;
								distanceAvg += e.getDistance();
							}
	
							if(e.getFitnessVector().getValue(i) > maxObjs[i]){
								maxModels[i] = e;
								maxObjs[i] = e.getFitnessVector().getValue(i);
							}
	
							if(e.dominates(maxGModel))
								maxGModel = e;
							if(e.getMonoValue() > bestGModel.getMonoValue()){
								bestGModel=e;
								LOGGER.config("Generation "+generation+" : new best : "+maxAllModel+" : "+maxAllModel.printStats());
							}
						}
						if(maxGModel.dominates(maxAllModel)){
							maxAllModel = maxGModel;
							LOGGER.config("Generation "+generation+" : new max : "+maxAllModel+" : "+maxAllModel.printStats());
						}
					}
					avgGenes = sumGenes / (float)nbEntities;
					float avgGeneSize = sumGenesSize / (float)sumGenes;
					distanceAvg = distanceAvg / nbDist;
					for (int i = 0; i < sums.length; i++) 
						avgs[i] = sums[i] / nbEntities;
	
	
					if((generation/Evolutioner.CHECK_POINT_GENERATIONS) != 0 && generation%Evolutioner.CHECK_POINT_GENERATIONS==0 && LOGGER.isLoggable(Level.CONFIG)){
						String log2 = "G_"+generation+": \n   avgs = ";
						for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) 
							log2 += ((int)((avgs[i]!=0?avgs[i]:avgs_buf[i])*1000))/1000.0+", ";
						if(log2.endsWith(", "))
							log2 = log2.substring(0, log2.length()-2);
	
						log2 += "\n   maxs";
						for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) 
							log2 += "\n     "+FitnessVector.getObjectiveName(i) + " : " + maxModels[i].toString()+" : "+maxModels[i].printStats();
						log2 +=     "\n     MAX : "+maxAllModel.toString()+" : "+maxAllModel.printStats();
	
						log2 +=     "\n     Best : "+bestGModel.toString()+" : "+bestGModel.printStats();
						LOGGER.config(log2);
					}//end LOGS
				//UI
//				if (Config.VERBOSE_ON_GRAPH && chart != null){
//					for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) {
//						chart.addObjective(generation,(float)/* maxModels[i].getFitnessVector().getValue(i)(avgs[i])*/maxAllModel.getFitnessVector().getValue(i), maxGModel.getFitnessVector().getValue(i), i);
//					}
//					chart.addToGraph2(generation, avgGeneSize);
//					chart.addToGraph3(generation, avgGenes);
//					chart.addToGraph4(generation, p.getParetos().size());
//					chart.addToGraph5(generation, p.getFrontPareto().size());
//				}//end UI
			}

			String log2 = "Results: \n   avgs = ";
			for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) 
				log2 += ((int)(avgs[i]*1000))/1000.0+", ";
			if(log2.endsWith(", "))
				log2 = log2.substring(0, log2.length()-2);

			log2 += "\n   maxs";
			for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) 
				log2 += "\n     "+FitnessVector.getObjectiveName(i) + " : " + maxModels[i].toString()+" : "+maxModels[i].printStats();
			log2 +=     "\n     MAX : "+maxAllModel.toString()+" : "+maxAllModel.printStats();
			LOGGER.config(log2);


			LOGGER.warning("Seed reminder : "+Config.SEED);
			LOGGER.info("Result = "+maxAllModel.getFitnessVector());


			String ev = evo.getClass().getSimpleName();
			if(ev.endsWith("FragmentSet")) ev = ev.substring(0, ev.indexOf("FragmentSet"));
			String fileOutName = Config.DIR_NUM+""+Config.METAMODEL_NAME+"_"+ev+"_"+Config.DIS_OR_MIN+"_"+Evolutioner.GENERATION_MAX+"_"+nbEnt+"_rnd.res";
			
			File f = new File(fileOutName);
			
			Utils.storeNumericResults(f, maxAllModel, new SimpleDateFormat("yyyyMMdd-Hmm", Locale.FRANCE).format( new Date() ), Utils.formatMillis(System.currentTimeMillis()-startTime));
//			Utils.storeResult(f, bestGModel, new SimpleDateFormat("yyyyMMdd-Hmm", Locale.FRANCE).format( new Date() )+"_B", Utils.formatMillis(System.currentTimeMillis()-startTime));
			LOGGER.info("Exit.");
			p = null;
			evo = null;
			partitionModel = null;
			maxAllModel = null;
			Model.clean();
			System.gc();
		}
		//end LOGS
	}
}

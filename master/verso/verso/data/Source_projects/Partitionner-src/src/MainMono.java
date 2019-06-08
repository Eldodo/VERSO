import genetic.Entity;
import genetic.Evolutioner;
import genetic.Population;

import java.io.File;
import java.util.logging.Logger;

import models.Model;
import partition.PartitionModel;
import partition.composition.FragmentSet;
import partition.ocl.OCLPartitionModel;
import utils.Config;
import utils.EvolutionTimeNotifierThread;
import utils.Utils;


public class MainMono {
	public final static Logger LOGGER = Logger.getLogger(MainMono.class.getName());

	public static void main(String[] args) {

		LOGGER.info("Entering Partitioner.");
		Utils.init();

		int max = Config.NUMBER_OF_EXECUTIONS;
		if(Config.VERBOSE_ON_UI){
			LOGGER.warning("In UI mode, number of executions is set to 1.");
			max = 1;
		}
		
		for (int i = 0; i < max; i++) {
			Population.nbPop = 0;//Multi execution concern.
			LOGGER.info("Run "+(i+1)+"/"+max+".");

			//		File f = new File(Config.DIR_INSTANCES+"/"+Config.METAMODEL_NAME+"/model_9987.xmi");
			//		URI fileURIm = URI.createFileURI(f.getAbsolutePath());
			//		System.out.println(fileURIm);
			//		Resource resource =  Utils.resourceSet.createResource(fileURIm);
			//		Model m = new Model(resource);
			//		System.exit(1);



			PartitionModel partitionModel;
			if(Config.FRAGMENT_WITH_OCL)
				partitionModel = new OCLPartitionModel();
			else
				partitionModel = new PartitionModel();

			partitionModel.extractPartition();

			FragmentSet instance = Config.loadFragmentSet(partitionModel);
			Population pop = Population.createRandomPopulation(Utils.resourceSet);
			Evolutioner evo = new Evolutioner(instance, pop);
			
//			ParetoListenerImpl plImpl = new ParetoListenerImpl();
//			evo.subscribeParetoListener(plImpl);
			

			EvolutionTimeNotifierThread timeStampThread = new EvolutionTimeNotifierThread(Evolutioner.CHECK_POINT_TIME, evo);	
			
			long startTime = System.currentTimeMillis();
			Population p = evo.evolutionateMono();
//			evo.unsubscribeParetoListener(plImpl);
			
			timeStampThread.end();
			
			String elapsedTime = Utils.formatMillis(System.currentTimeMillis()-startTime);
			
			Entity maxE = p.getBest();
			
			
			LOGGER.warning("Seed reminder : "+Config.SEED);
			LOGGER.info("Result = "+maxE.getFitnessVector());


			String ev = evo.getEvaluator().getClass().getSimpleName();
			if(ev.endsWith("FragmentSet")) ev = ev.substring(0, ev.indexOf("FragmentSet"));
			String fileOutName = Config.DIR_NUM+""+Config.METAMODEL_NAME+"_"+ev+"_"+Config.DIS_OR_MIN+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_mono.res";
			File f = new File(fileOutName);

			Utils.storeNumericResults(f, maxE, evo.getLastTimeStamp(), elapsedTime);
			
			LOGGER.info("Exit.");
			p = null;
			pop = null;
			evo = null;
			partitionModel = null;
			maxE = null;
			Model.clean();
			System.gc();
			LOGGER.info("End run "+(i+1)+"/"+max+".");
		}
		LOGGER.info("Exit.");
	}


}

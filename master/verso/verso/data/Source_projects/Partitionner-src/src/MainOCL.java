import genetic.Evolutioner;
import genetic.Population;

import java.util.logging.Logger;

import partition.composition.AllPartitionsFragmentSet;
import partition.composition.AllRangesFragmentSet;
import partition.composition.ClassSumFragmentSet;
import partition.ocl.OCLPartitionModel;
import utils.Config;
import utils.Utils;


public class MainOCL {
	public final static Logger LOGGER = Logger.getLogger(MainOCL.class.getName());
	
	
	public static void main(String[] args) {
		Utils.init();
		
		
		OCLPartitionModel partitionModel = new OCLPartitionModel();
		partitionModel.extractPartition();
		
		AllRangesFragmentSet allRanges = new AllRangesFragmentSet(partitionModel);
		AllPartitionsFragmentSet allPartitions = new AllPartitionsFragmentSet(partitionModel);
		ClassSumFragmentSet classSum = new ClassSumFragmentSet(partitionModel);
		
		Population pop = Population.createRandomPopulation(Utils.resourceSet);
		
		
		
		Evolutioner evo = new Evolutioner(allPartitions, pop);
		Population p = evo.evolutionate(pop);
		
		LOGGER.warning("Seed reminder : "+Config.SEED);
		LOGGER.info("Exit.");
	}
}

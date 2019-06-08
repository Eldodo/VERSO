package oclruler.a_test;
import java.util.logging.Logger;

import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Model;
import oclruler.utils.ToolBox;
import partitioner.partition.PartitionModel;
import partitioner.partition.composition.AllPartitionsFragmentSet;
import partitioner.partition.composition.AllRangesFragmentSet;


public class MainRawTesting {
	public final static Logger LOGGER = Logger.getLogger("Test");
	
	
	public static void main(String args[]) {
		LOGGER.info("Entering OCLRuler - 'rule' package oclruler.Testing - FOs diffusion\n");
		ToolBox.init();
		PartitionModel partitionModel;
		partitionModel = new PartitionModel();
		partitionModel.extractPartition();
		
		AllRangesFragmentSet allRangesFS = new AllRangesFragmentSet(partitionModel);
		AllPartitionsFragmentSet allPartitionFS = new AllPartitionsFragmentSet(partitionModel);
		
		for (Model m : ExampleSet.getExamplesBeingUsed()) {
			double cov = allRangesFS.evaluateCoverage(m);
			double cov2 = allPartitionFS.evaluateCoverage(m);
			System.out.println(" - "+m.getName()+": "+ cov +" / "+ cov2);
		}
		System.out.println("Sum: "+allRangesFS.evaluateCoverage(ExampleSet.getExamplesBeingUsed()));
		System.out.println("Sum: "+allPartitionFS.evaluateCoverage(ExampleSet.getExamplesBeingUsed()));
		
		System.exit(0);

		
	}

}

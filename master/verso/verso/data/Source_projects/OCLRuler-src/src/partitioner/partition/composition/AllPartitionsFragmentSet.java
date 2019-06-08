package partitioner.partition.composition;

import partitioner.partition.Partition;
import partitioner.partition.PartitionModel;
import partitioner.partition.Range;

public class AllPartitionsFragmentSet extends FragmentSet {
	
	public AllPartitionsFragmentSet(PartitionModel partitionModel) {
		super();
		
		for (Partition p : partitionModel.getPartitions()) {
			ModelFragment mf = new ModelFragment(this);
			ObjectFragment of = new ObjectFragment(mf);
			for (Range r : p.getRanges()) {
				PropertyConstraint pc = new PropertyConstraint(r.getPartition().getClassName(), r.getPartition().getFeatureName(), of, r);
				of.addPropertyConstraint(pc);
			}
			mf.addObjectFragment(of);
			addFragment(mf);
		}
		LOGGER.config("AllPartitions : "+getFragments().size()+" fragments, "+countProperties() + " properties, "+countObject());
		LOGGER.finest(prettyPrint());
	}
	
	@Override
	public String toString() {
		return "AllPartitions:MF="+getFragments().size()+"";
	}

}

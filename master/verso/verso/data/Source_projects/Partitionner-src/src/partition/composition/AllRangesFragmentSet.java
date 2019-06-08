package partition.composition;

import partition.PartitionModel;
import partition.Range;

public class AllRangesFragmentSet extends FragmentSet {
	
	public AllRangesFragmentSet(PartitionModel partition) {
		super();
		for (Range r : partition.getRanges()) {
			ModelFragment mf = new ModelFragment(this);
			ObjectFragment of = new ObjectFragment(mf);
			mf.addObjectFragment(of);
			PropertyConstraint pc = new PropertyConstraint(r.getPartition().getClassName(), r.getPartition().getFeatureName(), of, r);
			of.addPropertyConstraint(pc);
			addFragment(mf);
		}
		LOGGER.config("AllRanges :     "+getFragments().size()+" fragments, "+countProperties() + " properties, "+countObject());
		LOGGER.finest(prettyPrint());

	}
	@Override
	public String toString() {
		return "AllRanges:MF="+getFragments().size()+"";
	}

}

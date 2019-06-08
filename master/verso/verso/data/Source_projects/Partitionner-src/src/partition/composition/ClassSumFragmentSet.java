package partition.composition;

import java.util.HashMap;
import java.util.HashSet;

import partition.PartitionModel;
import partition.Range;

public class ClassSumFragmentSet extends FragmentSet {

	public ClassSumFragmentSet(PartitionModel partition) {
		super();
		LOGGER.config("ClassSum : Not yet implemented, On work !");
		HashMap<String, HashSet<Range>> listByClass = ClassByClassComposer.getCombinationsOneMFPerClass(partition.getPartitions());
		//		for (HashSet<Range> listRange : listByClass) {
		//			ModelFragment mf = new ModelFragment(this);
		//			for (Range r : listRange) {
		//				ObjectFragment of = new ObjectFragment(mf);
		//				PropertyConstraint pc = new PropertyConstraint(r.getPartition().getClassName(), r.getPartition().getFeatureName(), of, r);
		//				of.addPropertyConstraint(pc);
		//				mf.addObjectFragment(of);
		//			}
		//			addFragment(mf);
		//		}

		HashMap<String, HashSet<Range>> listByComb = ClassByClassComposer.getCombinationsOneMFPerCombination(partition.getPartitions());
		//		for (HashSet<Range> listRange : listByComb) {
		//			
		//			for (Range r : listRange) {
		//				ModelFragment mf = new ModelFragment(this);
		//				ObjectFragment of = new ObjectFragment(mf);
		//				PropertyConstraint pc = new PropertyConstraint(r.getPartition().getClassName(), r.getPartition().getFeatureName(), of, r);
		//				of.addPropertyConstraint(pc);
		//				mf.addObjectFragment(of);
		//				addFragment(mf);
		//			}
		//			
		//		}
//		for (String className : listByClass.keySet()) {
			//Liste de Range associer à une classe (by name)
//			HashSet<Range> listRangeCl = listByClass.get(className);
			//			System.out.println(className);
			//		HashMap<String, HashSet<Range>> listByComb = ClassByClassComposer.getCombinationsOneMFPerCombination(partition.getPartitions());
//			System.out.println("Class : "+listByClass);
			//		System.out.println("Comb : "+listByComb);
//			//		for (String className : listByClass.keySet()) {
//			//			HashSet<Range> listRangeCl = listByClass.get(className);	
//			for (HashSet<Range> rs : ClassByClassComposer.getCombinationOneRange(partition.getPartitions())) {
//				ModelFragment mf = new ModelFragment(this);
//
//				for (HashSet<Range> rs2 : ClassByClassComposer.getCombinationOneRange(partition.getPartitions())) {
//					ObjectFragment of = new ObjectFragment(mf);
//					for (Range r : rs2) {
//						//					for (Range r2 : listRangeCl) {
//						PropertyConstraint pc = new PropertyConstraint(r.getPartition().getClassName(), r.getPartition().getFeatureName(), of, r);
//						of.addPropertyConstraint(pc);
//						//					}
//					}
//					mf.addObjectFragment(of);
//				}
//			}
//
//				addFragment(mf);	
//				//		}
//				cleanFragments();
//			}
//			//			for (String key : listByComb.keySet()) {
			//				
			//				
			//				ObjectFragment of = new ObjectFragment(mf);
			//				if(key.startsWith(className)){
			//					HashSet<Range> listRangCb = listByComb.get(key);
			//					for (Range r : listRangCb) {
			//						if(r.getPartition().getClassName().equals(className)){
			//							PropertyConstraint pc = new PropertyConstraint(r.getPartition().getClassName(), r.getPartition().getFeatureName(), of, r);
			//							of.addPropertyConstraint(pc);
			//						}
			//					}
			//					mf.addObjectFragment(of);
			//					if(!mf.isEmpty())
			//					addFragment(mf);
			//				}
			//			}

	}
	@Override
	public String toString() {
		return "ClassSum:MF="+getFragments().size()+"";
	}

}

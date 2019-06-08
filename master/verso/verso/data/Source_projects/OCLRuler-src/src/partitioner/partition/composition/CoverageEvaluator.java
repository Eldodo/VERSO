package partitioner.partition.composition;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import oclruler.metamodel.Model;

public class CoverageEvaluator {

	protected static double coverageEvaluation(List<Model> modelSet, HashMap<ModelFragment, Integer> covertureMap, float avgSizeModels) {
		int nbFrags = covertureMap.keySet().size();
		int nbFragsCovered = 0;
		double excessCovering = 0;
		
		ModelFragment[] mfs = (ModelFragment[]) covertureMap.keySet().toArray(new ModelFragment[covertureMap.keySet().size()]);
		
		Arrays.sort(mfs, new Comparator<ModelFragment>() {
			@Override
			public int compare(ModelFragment o1, ModelFragment o2) {
				return o1.prettyPrint().compareTo(o2.prettyPrint());
			}
		});
		for (ModelFragment mf : mfs) {
			int cover = covertureMap.get(mf);
//			System.out.println("FitnessVector.compute\t"+mf.prettyPrint()+" : \t"+cover);
			nbFragsCovered += (cover>0)?1:0;
			excessCovering += (cover>1)?cover-1:0;
		}
//		System.out.println("  -> covered :   "+nbFragsCovered);
//		System.out.println("  -> uncovered : "+(nbFrags-nbFragsCovered));
		excessCovering = (nbFragsCovered!=0)?excessCovering/nbFragsCovered:0.0;
		double coverage = (((double)nbFragsCovered)/nbFrags);
		return coverage;
	}
}

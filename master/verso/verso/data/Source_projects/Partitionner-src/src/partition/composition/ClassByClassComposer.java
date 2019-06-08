package partition.composition;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import partition.Partition;
import partition.PartitionModel;
import partition.Range;

public class ClassByClassComposer {
	PartitionModel partition;
	
	public ClassByClassComposer(PartitionModel partition) {
		this.partition = partition;
	}
	
	
	public static HashMap<String, HashSet<Range>> getCombinationsOneMFPerClass(Collection<Partition> partitions){
		HashMap<String, HashSet<Range>> res = new HashMap<>();
		HashMap<String, HashSet<Range>> classNames = new HashMap<>();
				
		for (Partition p : partitions) {
			if(!classNames.keySet().contains(p.getClassName()))
				classNames.put(p.getClassName(), new HashSet<Range>());
			classNames.get(p.getClassName()).addAll(p.getRanges());
		}
		
//		for (String className : classNames.keySet()) {
//			HashSet<Range> res2 = new HashSet<>();
//			res2.addAll(classNames.get(className));
//			res.get(className).addAll(res2);
//		}
		
		return classNames;
	}
	public static HashMap<String, HashSet<Range>> getCombinationsOneMFPerCombination(Collection<Partition> partitions){
		HashSet<HashSet<Range>> res = new HashSet<>();
		HashMap<String, HashSet<Range>> classFeatureNames = new HashMap<>();
		
		for (Partition p : partitions) {
			String key = p.getClassName()+"."+p.getFeatureName();
			if(!classFeatureNames.keySet().contains(key))
				classFeatureNames.put(key, new HashSet<Range>());
			classFeatureNames.get(key).addAll(p.getRanges());
		}
		
//		for (String classFName : classFeatureNames.keySet()) {
//			res.add(classFeatureNames.get(classFName));
//		}
		return classFeatureNames;
	}
	
	public static HashSet<HashSet<Range>> getCombinationOneRange(HashSet<Partition> partitions){
		HashSet<HashSet<Range>> res = new HashSet<>();
		HashMap<String, HashSet<Range>> togo = getCombinationsOneMFPerCombination(partitions);
		for (HashSet<Range> hashSet : togo.values()) {
			res.add(hashSet);
		}
		
		return res;
	}
}

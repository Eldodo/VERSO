package partitioner.partition.composition;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import partitioner.partition.Partition;
import partitioner.partition.PartitionModel;
import partitioner.partition.Range;

public class ClassByClassComposer {
	PartitionModel partition;
	
	public ClassByClassComposer(PartitionModel partition) {
		this.partition = partition;
	}
	
	public static HashMap<String, HashSet<Range>> getCombinationsOneMFPerClass(Collection<Partition> partitions) {
		HashMap<String, HashSet<Range>> classNames = new HashMap<>();

		for (Partition p : partitions) {
			if (!classNames.keySet().contains(p.getClassName()))
				classNames.put(p.getClassName(), new HashSet<Range>());
			classNames.get(p.getClassName()).addAll(p.getRanges());
		}
		return classNames;
	}

	public static HashMap<String, HashSet<Range>> getCombinationsOneMFPerCombination(Collection<Partition> partitions) {
		HashMap<String, HashSet<Range>> classFeatureNames = new HashMap<>();

		for (Partition p : partitions) {
			String key = p.getClassName() + "." + p.getFeatureName();
			if (!classFeatureNames.keySet().contains(key))
				classFeatureNames.put(key, new HashSet<Range>());
			classFeatureNames.get(key).addAll(p.getRanges());
		}

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

package partition.composition;

import java.util.HashSet;

import models.Model;
import partition.Range;

public class PropertyConstraint {
	Range range;
	ObjectFragment objectFragment;
	
	String featureName, className;
	
	public PropertyConstraint(String className, String featureName, ObjectFragment object, Range range) {
		this.className = className;
		this.featureName = featureName;
		this.objectFragment = object;
		this.range = range;
	}

	public String prettyPrint() {
		String res =  className+"("+(range.isMultiplicity()?"#":"")+featureName+"="+range+")";
		return res;
	}

	public int isCoveredBy(Model m) {
		HashSet<String> values = m.getFeatureValues(className, featureName);
		int res = 0;
//		System.out.println(m+ ": ("+className+","+ featureName+ ")"+values+"  :" + range);
		for (String v : values) {
			res += range.isContained(v)?1:0;
//			System.out.println(" - " +v + " :" + res);
		}
		return res;
	}
	
	@Override
	public String toString() {
		return className+"."+featureName+":"+range;
	}
}

package partitioner.partition;

import java.util.ArrayList;

public class MultiplicityPartition extends Partition {
	ArrayList<IntegerRange> ranges;

	public MultiplicityPartition(PropertyPartition propertyPartition) {
		super(propertyPartition);
		ranges = new ArrayList<>();
	}
	
	public MultiplicityPartition(PropertyPartition propertyPartition,int lower, int upper) {
		this(propertyPartition);
		ranges = IntegerRange.createRanges(this, lower, upper);
	}

	@Override
	public ArrayList<? extends Range> getRanges() {
		return ranges;
	}

	public boolean addRange(int lower, int upper){
		return ranges.add(new IntegerRange(this, lower, upper));
	}

	public String prettyPrint() {
		String res = "("+getClassName()+"::#"+getFeatureName()+":";
		res += "{";
		for (Range r : getRanges()) {
			res += r + ", ";
		}
		if(res.endsWith(", "))
			res = res.substring(0, res.length() - 2);
		res +="})";
		return res;
	}

	@Override
	public String getTypeName() {
		return "multiplicity";
	}
}

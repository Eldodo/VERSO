package partitioner.partition;

public abstract class Range {
	boolean multiplicityRange = false;
	
	public Range(Partition p) {
		if(p instanceof ValuePartition)
			valuePartition = (ValuePartition) p;
		else if( p instanceof MultiplicityPartition){
			multiplicityPartition = (MultiplicityPartition) p;
			multiplicityRange = true;
		}
		
	}
	
	ValuePartition valuePartition;
	MultiplicityPartition multiplicityPartition;
	
	public abstract Partition getPartition() ;
	
	public abstract boolean isContained(Object o);

	public boolean isMultiplicity() {
		return multiplicityRange;
	}

	@Override
	public abstract boolean equals(Object r);
	
	public abstract String getTypeName();
	
	public abstract String printValue();
}

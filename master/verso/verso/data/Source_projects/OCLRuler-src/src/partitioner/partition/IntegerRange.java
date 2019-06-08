package partitioner.partition;

import java.util.ArrayList;

import org.eclipse.emf.ecore.ETypedElement;

public class IntegerRange extends Range {
	int lower, upper;
	
	
	public IntegerRange(Partition p, int lower, int upper) {
		super(p);
		this.lower = lower;
		this.upper = upper;
	}
	
	/**
	 * DEFAULT : null = 0.0
	 */
	@Override
	public boolean isContained(Object o) {
		if(o instanceof String){
			if(((String)o).compareTo("null")==0)
				o = 0;
			else{
				if(((String)o).compareTo("null")==0)
					o = 0;
				else
					try {
						o = Integer.parseInt((String)o);
					} catch (Exception e) {
						throw new IllegalArgumentException("Integer expected, found '"+o.getClass().getName()+"' : '"+o+"'");
					}
			}
		}
		if(o instanceof Integer){
			int oi = ((Number)o).intValue();
			return oi >= lower && oi <= ((upper==ETypedElement.UNBOUNDED_MULTIPLICITY || upper == ETypedElement.UNSPECIFIED_MULTIPLICITY)?Integer.MAX_VALUE:upper);
		} else 
			throw new IllegalArgumentException("Integer expected, found '"+o.getClass().getName()+"'.");
	}
	
	@Override
	public String toString() {
		if(lower == upper)
			return ""+lower;
		return "["+lower+","+((upper == ETypedElement.UNBOUNDED_MULTIPLICITY || upper == ETypedElement.UNSPECIFIED_MULTIPLICITY)?"*":upper)+"]";
	}

	@Override
	public Partition getPartition() {
		if(multiplicityPartition != null)
			return multiplicityPartition;
		else 
			return valuePartition;
	}

	public static ArrayList<IntegerRange> createRanges(Partition p, int absoluteLower, int absoluteUpper) {
		ArrayList<IntegerRange> res = new ArrayList<>();
			boolean unsetUpper = absoluteUpper == ETypedElement.UNBOUNDED_MULTIPLICITY || absoluteUpper == ETypedElement.UNSPECIFIED_MULTIPLICITY;
			res.add(new IntegerRange(p, absoluteLower, absoluteLower));
			if(unsetUpper || absoluteUpper >= absoluteLower+1)
				res.add(new IntegerRange(p, absoluteLower+1, absoluteLower+1));
			if(unsetUpper || absoluteUpper >= absoluteLower+2)
				res.add(new IntegerRange(p, absoluteLower+2, absoluteUpper));
			return res;
	}
	
	@Override
	public boolean equals(Object r) {
		if(r != null && r instanceof IntegerRange){
			IntegerRange dr = (IntegerRange)r;
			return (dr.upper == upper && dr.lower == lower);
		}
		return false;
	}
	@Override
	public String getTypeName() {
		return "integer";
	}
	@Override
	public String printValue() {
		return toString()+"";
	}

}

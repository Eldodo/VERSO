package partition;

import java.util.ArrayList;

import org.eclipse.emf.ecore.ETypedElement;

public class DoubleRange extends Range {
	double lower, upper;
	
	
	public DoubleRange(Partition p, double lower, double upper) {
		super(p);
		this.lower = lower;
		this.upper = upper;
	}
	
	@Override
	public boolean isContained(Object o) {
		if(o instanceof String){
			if(isMultiplicity() && ((String)o).compareTo("null")==0)
				o = 0;
			else
				try {
					o = Double.parseDouble((String)o);
				} catch (Exception e) {
					throw new IllegalArgumentException("Number expected, found '"+o.getClass().getName()+"' : '"+o+"'");
				}
		}
		if(o instanceof Number){
			double oi = ((Number)o).doubleValue();
			return oi >= lower && oi <= ((upper==ETypedElement.UNBOUNDED_MULTIPLICITY || upper == ETypedElement.UNSPECIFIED_MULTIPLICITY)?Integer.MAX_VALUE:upper);
		} else 
			throw new IllegalArgumentException("Double expected, found '"+o.getClass().getName()+"'.");
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

	public static ArrayList<DoubleRange> createRanges(Partition p, int absoluteLower, int absoluteUpper) {
		ArrayList<DoubleRange> res = new ArrayList<>();
			boolean unsetUpper = absoluteUpper == ETypedElement.UNBOUNDED_MULTIPLICITY || absoluteUpper == ETypedElement.UNSPECIFIED_MULTIPLICITY;
			res.add(new DoubleRange(p, absoluteLower, absoluteLower));
			if(unsetUpper || absoluteUpper >= absoluteLower+1)
				res.add(new DoubleRange(p, absoluteLower+1, absoluteLower+1));
			if(unsetUpper || absoluteUpper >= absoluteLower+2)
				res.add(new DoubleRange(p, absoluteLower+2, absoluteUpper));
			return res;
	}
	
	@Override
	public boolean equals(Object r) {
		if(r != null && r instanceof DoubleRange){
			DoubleRange dr = (DoubleRange)r;
			return (dr.upper == upper && dr.lower == lower);
		}
		return false;
	}
	@Override
	public String getTypeName() {
		return "double";
	}
	@Override
	public String printValue() {
		return toString()+"";
	}

}

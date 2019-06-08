package partitioner.partition;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class DateRange extends Range {
	Date lower, upper;
	
	
	public DateRange(Partition p, Date lower, Date upper) {
		super(p);
		this.lower = lower;
		this.upper = upper;
	}
	
	@Override
	public boolean isContained(Object o) {
		if(o instanceof String){
			if(isMultiplicity() && ((String)o).compareTo("null")==0)
				o = 0;
			else{
				if(((String)o).compareTo("null")==0)
					o = 0;
				else
					try {
						o = DateFormat.getInstance().parse((String)o);
					} catch (Exception e) {
						throw new IllegalArgumentException("Integer expected, found '"+o.getClass().getName()+"' : '"+o+"'");
					}
			}
		}
		if(o instanceof Date){
			Date oi = ((Date)o);
			return lower.before(oi) && ((upper == null) || (upper != null && oi.before(upper)));
		} else 
			throw new IllegalArgumentException("Date expected, found '"+o.getClass().getName()+"'.");
	}
	
	@Override
	public String toString() {
		if(lower == upper)
			return ""+lower;
		return "["+lower+","+((upper != null)?upper:"*")+"]";
	}

	@Override
	public Partition getPartition() {
		if(multiplicityPartition != null)
			return multiplicityPartition;
		else 
			return valuePartition;
	}

	public static ArrayList<DateRange> createRanges(Partition p, Date absoluteLower, Date absoluteUpper) {
		ArrayList<DateRange> res = new ArrayList<>();
			boolean unsetUpper = absoluteUpper == null;
			res.add(new DateRange(p, absoluteLower, absoluteLower));
			if(unsetUpper || absoluteUpper.after(tomorrow(absoluteLower)))
				res.add(new DateRange(p, tomorrow(absoluteLower), tomorrow(absoluteLower)));
			if(unsetUpper || absoluteUpper.after(tomorrow(tomorrow(absoluteLower))))
				res.add(new DateRange(p, tomorrow(tomorrow(absoluteLower)), absoluteUpper));
			return res;
	}
	
	@Override
	public boolean equals(Object r) {
		if(r != null && r instanceof DateRange){
			DateRange dr = (DateRange)r;
			return (dr.upper == upper && dr.lower == lower);
		}
		return false;
	}
	
	public static Date tomorrow(Date d){
		Date res = (Date) d.clone();
		res.setTime(res.getTime()+86400);
		return res;
	}
	public static Date yesterday(Date d){
		Date res = (Date) d.clone();
		res.setTime(res.getTime()-86400);
		return res;
	}
	@Override
	public String getTypeName() {
		return "date";
	}
	@Override
	public String printValue() {
		return toString()+"";
	}

}

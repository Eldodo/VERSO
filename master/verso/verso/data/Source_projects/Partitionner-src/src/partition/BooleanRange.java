package partition;

public class BooleanRange extends Range {
	boolean boolValue;
	
	
	public BooleanRange(Partition p, Boolean b) {
		super(p);
		boolValue = b;
	}
	
	@Override
	public boolean isContained(Object o) {
		if(o instanceof String){
			try {
				o = Boolean.parseBoolean((String)o);
			} catch (Exception e) {
				throw new IllegalArgumentException("Boolean expected, found '"+o.getClass().getName()+"'.");
			}
		}
		if(o instanceof Boolean){
			Boolean os = ((Boolean)o);
			return os.booleanValue() == boolValue;
		} else 
			throw new IllegalArgumentException("Boolean expected, found '"+o.getClass().getName()+"'.");
	}
	
	@Override
	public String toString() {
		return ""+boolValue+"";
	}

	@Override
	public Partition getPartition() {
		return valuePartition;
	}
	
	@Override
	public boolean equals(Object r) {
		if(r != null && r instanceof BooleanRange){
			BooleanRange br = (BooleanRange)r;
			return (br.boolValue == boolValue);
		}
		return false;
	}

	@Override
	public String getTypeName() {
		return "boolean";
	}
	
	@Override
	public String printValue() {
		return boolValue+"";
	}
}

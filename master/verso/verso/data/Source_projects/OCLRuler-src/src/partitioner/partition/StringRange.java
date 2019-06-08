package partitioner.partition;

public class StringRange extends Range {
	String regex;
	
	public StringRange(Partition p, String regexp) {
		super(p);
		this.regex = regexp;
	}
	
	@Override
	public boolean isContained(Object o) {
		if(o instanceof String){
			String os = ((String)o);
			if(regex.compareTo("") == 0)
				return os.compareTo("") == 0 || os.compareTo("null") == 0;
			return os.matches(regex);
		} else 
			throw new IllegalArgumentException("String expected, found '"+o.getClass().getName()+"'.");
	}
	
	@Override
	public String toString() {
		return "\""+regex+"\"";
	}
	
	

	@Override
	public Partition getPartition() {
		return valuePartition;
	}
	
	@Override
	public boolean equals(Object r) {
		if(r != null && r instanceof StringRange){
			StringRange sr = (StringRange)r;
			return sr.regex.compareTo(regex) == 0;
		}
		return false;
	}
	@Override
	public String getTypeName() {
		return "string";
	}
	@Override
	public String printValue() {
		return regex+"";
	}

}

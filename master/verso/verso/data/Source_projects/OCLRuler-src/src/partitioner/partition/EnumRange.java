package partitioner.partition;

import java.util.ArrayList;

import org.eclipse.emf.ecore.EEnumLiteral;

public class EnumRange extends Range {
	ArrayList<EEnumLiteral> options;
	
	public EnumRange(Partition p) {
		super(p);
		this.options = new ArrayList<>();
	}
	
	
	public EnumRange(Partition p, EEnumLiteral option) {
		this(p);
		options.add(option);
	}
	public EnumRange(Partition p, ArrayList<EEnumLiteral> options) {
		this(p);
		for (EEnumLiteral eel : options) {
			this.options.add(eel);
		}
	}
	
	@Override
	public boolean isContained(Object o) {
		if(o instanceof String){
			String eel = ((String)o);
			for (EEnumLiteral eel2 : options) {
				if(eel.compareTo(eel2.getLiteral()) == 0)
					return true;
			}
			return false;
		} else 
			throw new IllegalArgumentException("EnumLiteral expected, found '"+o.getClass().getName()+"'.");
	}
	


	@Override
	public Partition getPartition() {
		
		return valuePartition;
	}

	@Override
	public boolean equals(Object r) {
		if(r != null && r instanceof EnumRange){
			EnumRange er = (EnumRange)r;
			boolean b = true;
			for (EEnumLiteral eEnumLiteral : options) 
				b &= er.options.contains(eEnumLiteral);
			
			b &= er.options.size() == options.size();
			return b;
		}
		return false;
	}
	
	@Override
	public String getTypeName() {
		return "enumn";
	}
	
	@Override
	public String toString() {
		return ""+options+"";
	}
	
	@Override
	public String printValue() {
		return toString()+"";
	}

}

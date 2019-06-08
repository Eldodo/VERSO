package partition;

import java.util.ArrayList;
import java.util.Date;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.ETypedElement;

public class ValuePartition extends Partition {
	ArrayList<Range> ranges;
	
	public ValuePartition(	PropertyPartition propertyPartition) {
		super(propertyPartition);
		ranges = new ArrayList<>();
	}
	
	public ValuePartition(	PropertyPartition propertyPartition, EDataType eDataType) {
		this(propertyPartition);
		String name = eDataType.getInstanceClassName();
		if(eDataType instanceof EEnum){
			addEnumRanges((EEnum) eDataType);
		} else if(name.compareTo(String.class.getName()) == 0 || eDataType.getName().compareTo("EString") == 0){
			addStringRange("");
			addStringRange(".*");
		} else if(name.compareTo(Boolean.class.getName()) == 0 || eDataType.getName().compareTo("EBoolean") == 0){
			addBooleanRanges();
		} else if(name.compareTo(Integer.class.getName()) == 0 || eDataType.getName().compareTo("EInteger") == 0 || eDataType.getName().compareTo("EInt") == 0){
			for (IntegerRange ir : IntegerRange.createRanges(this, 0, ETypedElement.UNBOUNDED_MULTIPLICITY)) 
				addRange(ir);
			
		} else if(name.compareTo(Double.class.getName()) == 0 || eDataType.getName().compareTo("EDouble") == 0){
			for (DoubleRange ir : DoubleRange.createRanges(this, 0, ETypedElement.UNBOUNDED_MULTIPLICITY)) 
				addRange(ir);
		} else if(name.compareTo(Float.class.getName()) == 0 || eDataType.getName().compareTo("EFloat") == 0){
			for (DoubleRange ir : DoubleRange.createRanges(this, 0, ETypedElement.UNBOUNDED_MULTIPLICITY)) 
				addRange(ir);
		} else if(name.compareTo(Long.class.getName()) == 0 || eDataType.getName().compareTo("ELong") == 0){
			for (DoubleRange ir : DoubleRange.createRanges(this, 0, ETypedElement.UNBOUNDED_MULTIPLICITY)) 
				addRange(ir);
		} else if(name.compareTo(Date.class.getName()) == 0 || eDataType.getName().compareTo("Date") == 0){
			
		} else {
			throw new IllegalArgumentException("Invalid data type : '"+eDataType.getName()+"'. Availables are : EString, EBoolean, EEnum*, EInt ");
		}
	}
	

	private boolean addRange(Range range) {
		return ranges.add(range);
	}

	@Override
	public ArrayList<? extends Range> getRanges() {
		return ranges;
	}

	
	public boolean addStringRange(String regex){
		return ranges.add(new StringRange(this, regex));
	}
	public boolean addBooleanRanges(){
		boolean b = ranges.add(new BooleanRange(this, true));
		b &= ranges.add(new BooleanRange(this, false));
		return b;
	}
	
	private void addEnumRanges(EEnum eEnum) {
		ArrayList<EEnumLiteral> list = new ArrayList<>();
		for (EEnumLiteral eel : eEnum.getELiterals()) {
			addRange(new EnumRange(this, eel));
			list.add(eel);
		}
		addRange(new EnumRange(this, list));
	}

	@Override
	public boolean isObjectInRange(Object o, Range r) {
		return r.isContained(o);
	}
	
	
	public String prettyPrint() {
		String res = "("+getClassName()+"::"+getFeatureName()+":";
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
		return "value";
	}

}

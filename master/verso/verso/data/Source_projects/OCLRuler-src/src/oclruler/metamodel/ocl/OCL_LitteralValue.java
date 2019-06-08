package oclruler.metamodel.ocl;

public class OCL_LitteralValue extends OCL_Element {
	public OCL_LitteralValue(Number value) {
		this("OCL_LitteralValue", value);
	}	
	
	public OCL_LitteralValue(String string, Object value) {
		super(string);
		this.value = value;
	}

	@Override
	public String prettyPrint() {
		return "("+name + " : "+value.toString()+")";
	}

	@Override
	public String simplePrint() {
		return "\""+value.toString()+"\"";
	}

	@Override
	public boolean equals(Object o) {
		if(o == null || (getClass() != o.getClass()))
			return false;
		OCL_LitteralValue p = (OCL_LitteralValue)o;
		return value.equals(p.getValue());
	}

}
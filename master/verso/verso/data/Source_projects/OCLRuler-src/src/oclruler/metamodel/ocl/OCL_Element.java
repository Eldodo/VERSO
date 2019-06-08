package oclruler.metamodel.ocl;

import oclruler.metamodel.MMElement;

public abstract class OCL_Element extends MMElement {
	Object value;
	public OCL_Element(Object value) {
		this("OCL_Element", value);
	}	
	
	public OCL_Element(String string, Object value) {
		super(string);
		this.value = value;
	}

	@Override
	public String prettyPrint(String tab) {
		return tab+"("+name + " : "+value.toString()+")";
	}

	@Override
	public String simplePrint() {
		return value.toString();
	}

	public void setValue(Object i) {
		value = i;
	}
	
	public Object getValue() {
		return value;
	}
	@Override
	public boolean equals(Object o) {
		if(o == null || (getClass() != o.getClass()))
			return false;
		OCL_Element p = (OCL_Element)o;
		return value.equals(p.getValue());
	}

}
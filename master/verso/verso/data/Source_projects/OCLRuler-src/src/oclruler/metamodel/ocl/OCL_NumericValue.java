package oclruler.metamodel.ocl;

import oclruler.utils.ToolBox;

public class OCL_NumericValue extends OCL_Element {
	
	static int NUMERIC_RANGE = 3;
	Number[] range;
	
	public OCL_NumericValue(Number value) {
		this("OCL_NumericValue", value);
	}	
	
	public OCL_NumericValue(String string, Number value) {
		super(string);
		this.value = value;
	}
	
	public OCL_NumericValue() {
		this("OCL_NumericValue", getRandomValue(null));
	}

	public void setRange(Number[] range) {
		this.range = range;
	}

	@Override
	public String prettyPrint() {
		return "("+name + " : "+value.toString()+(range!=null?"["+range[0]+","+range[1]+"]":"")+")";
	}
	
	public void mutate() {
		value = getRandomValue(range, (Number)getValue());
	}

	
	/**
	 * 
	 * @param range [Can be null]
	 * @return
	 */
	public static Number getRandomValue(Number[] range){
		if(range!= null)
			return ToolBox.getRandomInt(range[0].intValue(), range[1].intValue());
		else 
			return  ToolBox.getRandomInt(NUMERIC_RANGE);
	}
	
	public static Number getRandomValue(Number[] range, Number exclus){
		if(exclus == null)
			return getRandomValue(range);
		Number res = exclus;
		while(res.equals(exclus)){
			if(range != null)
				res = ToolBox.getRandomInt(range[0].intValue(), range[1].intValue());
			else 
				res = ToolBox.getRandomInt(NUMERIC_RANGE);
		}
		return res;
	}
	
	@Override
	public String simplePrint() {
		return value.toString();
	}
	@Override
	public boolean equals(Object o) {
		if(o == null || (getClass() != o.getClass()))
			return false;
		OCL_NumericValue p = (OCL_NumericValue)o;
		return value.equals(p.getValue());
	}

}
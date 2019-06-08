package verso.model.metric;


public abstract class Metric<Type>
{
	protected Type value;
	
	abstract public String getName();
	abstract public void setName(String name);
	
	public Type getValue()
	{
		return this.value;
	}
	
	public String getTextualValue()
	{
		return "" + this.value;
	}
	public void setValue(Type val)
	{
		this.value = val;
	}

	abstract public double getNormalizedValue();
	abstract public String getMinString();
	abstract public String getMaxString();
	abstract public String getTypeString();
	
	public String toString()
	{
		String toReturn = "";
		toReturn += this.getName();
		toReturn += ":";
		toReturn += this.getValue();
		return toReturn;
	}
			
	abstract public LegendDescriptor getLegendDescriptor();

}

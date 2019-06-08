package verso.model.metric;


public abstract class NumberMetric<Type extends Number> extends Metric<Type>{
	
	protected IntervaleMetricDescriptor<Type> md;
	
	public NumberMetric(IntervaleMetricDescriptor<Type> metric, Type val)
	{
		this.value = val;
		this.md = metric;
		
	}
	
	public String getName()
	{
		return this.md.getName();
	}

	public void setName(String name)
	{
		this.md.setName(name);
	}
	
	abstract public double getNormalizedValue(Type value);
	

	public double getNormalizedValue() 
	{
		return Math.min(1.0,(this.value.doubleValue() - this.md.getMin().doubleValue())/(this.md.getMax().doubleValue() - this.md.getMin().doubleValue()));
	}
	
	public Type getMax()
	{
		return md.getMax();
	}
	
	public Type getMin()
	{
		return md.getMin();
	}
	public int getValeur()
	{
		return this.getValue().intValue();
	}


	public LegendDescriptor getLegendDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	

	
	
	
}

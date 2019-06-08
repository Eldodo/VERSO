package verso.model.metric;

public class IntervaleMetricDescriptor<Type> extends MetricDescriptor{

	private Type min;
	private Type max;
	public IntervaleMetricDescriptor(String name, Type min, Type max)
	{
		super(name);
		this.min = min;
		this.max = max;
	}
	
	public Type getMin()
	{
		return this.min;
	}
	
	public Type getMax()
	{
		return this.max;
	}
	
}

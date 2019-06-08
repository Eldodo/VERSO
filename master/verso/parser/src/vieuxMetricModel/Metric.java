package vieuxMetricModel;

public class Metric {

	private String metricName;
	private double metricValue;
	
	public Metric(String metricName, double metricValue)
	{
		this.metricName = metricName;
		this.metricValue = metricValue;
	}
	
	public String getName()
	{
		return this.metricName;
	}
	
	public void setName(String name)
	{
		this.metricName = name;
	}
	
	public double getValue()
	{
		return this.metricValue;
	}
	
	public void setValue(double value)
	{
		this.metricValue = value;
	}
}

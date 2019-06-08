package vieuxMetricModel;

import java.util.Collection;
import java.util.Hashtable;

public class Element {

	protected Hashtable<String,Metric> metrics = new Hashtable<String,Metric>();
	protected String name;
	
	public Element(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		if (name == null)
		{
			return "null";
		}
		else
		{
			return this.name;
		}
	}
	public void addMetric(Metric m)
	{
		metrics.put(m.getName(), m);
	}
	
	public Metric getMetric(String metricName)
	{
		return this.metrics.get(metricName);
	}
	
	public Collection<Metric> getMetrics()
	{
		return this.metrics.values();
	}
}

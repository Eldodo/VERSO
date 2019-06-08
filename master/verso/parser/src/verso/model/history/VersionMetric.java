package verso.model.history;

import java.util.Collection;
import java.util.Hashtable;
import verso.model.metric.Metric;
import verso.model.metric.NullMetric;

public class VersionMetric {
	
	private Hashtable<String,Metric> metricList = new Hashtable<String,Metric>();
	
	public Metric getMetric(String metricName)
	{
		Metric m = metricList.get(metricName);
		if (m == null)
		{
			m = new NullMetric();
			m.setValue(0);
		}
		return m;
	}
	
	public Collection<Metric> getMetrics()
	{
		return this.metricList.values();
	}
	public void setMetrics(Collection<Metric> metrics)
	{
		for (Metric m : metrics)
		{
			metricList.put(m.getName(), m);
		}
	}
	
	public void addMetric(Metric m)
	{
		metricList.put(m.getName(), m);
	}
	
	public Hashtable<String,Metric> getMetList()
	{
		return this.metricList;
	}

}

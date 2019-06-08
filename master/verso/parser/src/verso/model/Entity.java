package verso.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import verso.model.history.VersionMetric;
import verso.model.metric.Metric;
import verso.model.metric.NullMetric;

public class Entity {
	private String name;
	private Hashtable<String, Metric<?>> metricList;
	private Hashtable<Long, VersionMetric> versionList;
	private static long currentVersion = -1;
	protected List<Bug> bugs;
	
	public static Map<String, Entity> entities = new HashMap<>();

	public Entity(String name) {
		this.name = name;
		metricList = new Hashtable<String, Metric<?>>();
		versionList = new Hashtable<Long, VersionMetric>();
		this.bugs = new ArrayList<Bug>();
		entities.put(name, this);
	}

	@SuppressWarnings("rawtypes")
	public Metric getMetric(String metricName) {
		Metric m = metricList.get(metricName);
		if (m == null) {
			m = new NullMetric();
		}
		return m;
	}

	public Collection<Metric<?>> getMetrics() {
		return this.metricList.values();
	}

	public void setMetrics(Collection<Metric<?>> metrics) {
		for (Metric<?> m : metrics) {
			metricList.put(m.getName(), m);
		}
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("rawtypes")
	public void addMetric(Metric m) {
		if (currentVersion != -1)		{
			if (versionList.get(currentVersion) == null)
				versionList.put(currentVersion, new VersionMetric());
			versionList.get(currentVersion).addMetric(m);
		}
		metricList.put(m.getName(), m);
	}
	
	public Object accept(Visitor v)
	{
		return v.visit(this);
	}
	
	
	
	public static void setCurrentVersion(long l)
	{
		currentVersion = l;
	}
	
	public void addBug(Bug b)
	{
		this.bugs.add(b);
	}
	
	public void clearBugs()
	{
		this.bugs.clear();
	}
	
	public List<Bug> getBugs()
	{
		return this.bugs;
	}
	public String getBugString()
	{
		String toReturn = "";
		for (Bug b : this.bugs)
		{
			toReturn += " - " + b.toString();
		}
		return toReturn;
	}
}

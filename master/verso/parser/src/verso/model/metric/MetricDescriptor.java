package verso.model.metric;

public class MetricDescriptor {
	
	String name = "";
	
	MetricDescriptor(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

}

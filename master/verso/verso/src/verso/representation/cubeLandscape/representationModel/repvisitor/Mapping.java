package verso.representation.cubeLandscape.representationModel.repvisitor;

public class Mapping {

	String metric  ="";
	String graphicalValue = "";
	
	public Mapping(String metric, String gValue)
	{
		this.metric = metric;
		this.graphicalValue = gValue;
	}
	
	public String getMetric()
	{
		return this.metric;
	}
	
	public String getGvalue()
	{
		return this.graphicalValue;
	}
	
	public void setMetric(String metric)
	{
		this.metric = metric;
	}
	
	public void setGValue(String gValue)
	{
		this.graphicalValue = gValue;
	}
}

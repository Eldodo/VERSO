package verso.model.metric;

public class LegendValue {

	private double normalizedValue;
	private String valueName;
	
	public LegendValue(double val, String valName)
	{
		this.normalizedValue = val;
		this.valueName = valName;
	}
	
	public double getNormalizedValue()
	{
		return normalizedValue;
	}
	
	public String getValueName()
	{
		return valueName;
	}
	
	
}

package verso.representation.Lines.visitor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;



public class LineMapping {
	
	public final int BLUECOLOR = 5;
	
	String graphicalValue = "BlueColor";
	String metricName = "";
	List<Color> colorMapping = new ArrayList<Color>();
	
	public void addColor(Color c)
	{
		this.colorMapping.add(c);
	}
	
	public Color getColor(int i)
	{
		if (i >= colorMapping.size())
			return new Color(255,255,255);
		return colorMapping.get(i);
	}

	public void setMetricName(String metricName)
	{
		this.metricName = metricName;
	}
	
	public String getMetricName()
	{
		return this.metricName;
	}
	
	public void setGraphicalValue(String graph)
	{
		this.graphicalValue = graph;
	}
	
	public String getGraphicalValue()
	{
		return graphicalValue;
	}
}

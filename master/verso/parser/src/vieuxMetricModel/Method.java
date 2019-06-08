package vieuxMetricModel;

import java.util.List;
import java.util.Set;

public class Method extends Element{

	private String returnType = "";
	private List<String> over;
	private Set<String> targets;
	public Method(String name)
	{
		super(name);
	}
	
	public void setReturnType(String s)
	{
		this.returnType = s;
	}
	
	public String getReturnType()
	{
		return this.returnType;
	}
	
	public void setOverridenFrom(List<String> over)
	{
		this.over = over;
	}
	
	public String getOverridenFromString()
	{
		String toReturn = "";
		for (String s : over)
		{
			toReturn += s + ",";
		}
		if (toReturn.length() > 0)
			toReturn = toReturn.substring(0,toReturn.length()-1);
		return toReturn;
	}
	
	public void setTargets(Set<String> targets)
	{
		this.targets = targets;
	}
	
	public String getTargetString()
	{
		String toReturn = "";
		for (String s : targets)
		{
			toReturn += s + ",";
		}
		if (toReturn.length() > 0)
			toReturn = toReturn.substring(0,toReturn.length()-1);
		return toReturn;
	}
	
	
	public String toString()
	{
		String toReturn = "";
		toReturn += "\t" + this.getName() + "\n";
		toReturn += "\t" + "  " + "Return Type : " + this.returnType + "\n";
		toReturn += "\t" + "  " + "Overriden From : " + this.over + "\n";
		toReturn += "\t" + "  " + "Targets : " + this.targets + "\n";
		for (Metric m : this.metrics.values())
		{
			toReturn += "\t" + "  " + "Metric " + m.getName() + " : " + m.getValue() + "\n";
		}
		
		return toReturn;
	}
	
	public String toStringSpecial(int indent)
	{
		String space = "";
		for (int i = 0; i < indent; i++)
		{
			space += "  ";
		}
		String toReturn = "";
		toReturn += space + "Method Name : " + this.getName() + "\n";
		toReturn += space + "  " + "Return Type : " + this.returnType + "\n";
		toReturn += space + "  " + "Overriden From : " + this.over + "\n";
		toReturn += space + "  " + "Targets : " + this.targets + "\n";
		for (Metric m : this.metrics.values())
		{
			toReturn += space + "  " + "Metric " + m.getName() + " : " + m.getValue() + "\n";
		}
		
		return toReturn;
	}
}

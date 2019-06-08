package vieuxMetricModel;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class Class extends Element{

	public final static int INTERFACE = 1;
	public final static int CLASS = 2;
	
	private int type;
	private String parent ="";
	private String pack ="";
	private List<String> ancestors;
	private List<String> interfaces;
	private Set<String> targets;
	private Hashtable<String,Method> methods = new Hashtable<String,Method>();
	
	public Class(String name)
	{
		super(name);
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	public String getType()
	{
		if (type == Class.CLASS)
		{
			return "Class";
		}
		else
		{
			return "Interface";
		}
	}
	
	public void setParent(String s)
	{
		this.parent = s;
	}
	
	public String getParent()
	{
		return this.parent;
	}
	public void setPackage(String pack)
	{
		this.pack = pack;
	}
	
	public String getPackage()
	{
		return this.pack;
	}
	
	public void setAncestors(List<String> ancestors)
	{
		this.ancestors = ancestors;
	}
	
	public void setInterfaces(List<String> interfaces)
	{
		this.interfaces = interfaces;
	}
	public String getInterfacesString()
	{
		String toReturn = "";
		for (String s : this.interfaces)
		{
			toReturn += s + ",";
		}
		if (toReturn.length() > 0)
			toReturn = toReturn.substring(0,toReturn.length()-1);
		return toReturn;
	}
	
	public String getAncestorsString()
	{
		String toReturn = "";
		for (String s : this.ancestors)
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
	
	public void addMethod(Method m)
	{
		this.methods.put(m.getName(), m);
	}
	
	public Set<String> getTargets()
	{
		return this.targets;
	}
	
	public String getTargetString()
	{
		String toReturn = "";
		for (String s : this.targets)
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
		toReturn += this.name + "\n";
		toReturn += "  " + "SuperClass : " + this.parent + "\n";
		toReturn += "  " + "Ancestors : " + this.ancestors + "\n";
		toReturn += "  " + "Targets : " + this.targets + "\n";
		for (Metric m : this.metrics.values())
		{
			toReturn += "  " + "Metric " + m.getName() + " : " + m.getValue() + "\n";
		}
		for (Method met: this.methods.values())
		{
			toReturn += met.toString();
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
		toReturn += space + "Class Name : " + this.name + "\n";
		toReturn += space + "  " + "SuperClass : " + this.parent + "\n";
		toReturn += space + "  " + "Ancestors : " + this.ancestors + "\n";
		toReturn += space + "  " + "Targets : " + this.targets + "\n";
		for (Metric m : this.metrics.values())
		{
			toReturn += space + "  " + "Metric " + m.getName() + " : " + m.getValue() + "\n";
		}
		for (Method met: this.methods.values())
		{
			toReturn += met.toStringSpecial(indent + 1);
		}
		return toReturn;
	}
}

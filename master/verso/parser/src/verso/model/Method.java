package verso.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import verso.model.metric.Metric;

public class Method extends LowLevelElement{

	String returnType = null;
	String signature = "";
	Set<String> targets = new HashSet<String>();
	List<String> overridenFrom = null;
	List<Line> lstLines = new ArrayList<Line>();
	
	public Method(String name)
	{
		super(name);
	}
	
	public void setReturnType(String type)
	{
		this.returnType = type;
	}
	public void setSignature(String sig)
	{
		this.signature = sig;
	}
	
	public String getSignature()
	{
		return signature.replaceAll("[;]", "'");
	}
	
	public String getReturnType()
	{
		return this.returnType;
	}
	
	public void addTarget(String target)
	{
		targets.add(target);
	}
	public Set<String> getTargets()
	{
		return this.targets;
	}
	
	public void setOveriddenFrom(List<String> mets)
	{
		this.overridenFrom = mets;
	}
	
	public void setTargets(Set<String> tars)
	{
		for (String s : tars)
		{
			this.addTarget(s);
		}
	}
	
	public String getTargetString()
	{
		String toReturn = "";
		
		for (String s : this.targets)
		{
			toReturn += s + ":";
		}
		if (toReturn.length() > 1)
			toReturn = toReturn.substring(0, toReturn.length()-1);
		return toReturn;
	}
	
	public String toStringSpecial(int indent)
	{
		String toReturn = "";
		String space = "";
		for (int i =0; i < indent; i++)
		{
			 space += "  ";
		}
		toReturn += space + "+" + this.getName() + "\n";
		for (Metric m : this.getMetrics())
		{
			toReturn += space + "  " + m.getName() + " : " + m.getValue() + "\n";
		}
		return toReturn;
	}
	
	public Object accept(Visitor v)
	{
		return v.visit(this);
	}
	
	public List<Line> getLines()
	{
		return this.lstLines;
	}
	
	public void addLine(Line l)
	{
		this.lstLines.add(l);
	}
	
	public void setLines(List<Line> lstLines)
	{
		this.lstLines = lstLines;
	}
	
	public void computeBugMetrics(SystemDef sys)
	{
		//à compléter
	}
	
	
}

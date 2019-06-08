package verso.model;

import java.util.Iterator;
import java.util.List;

import verso.model.metric.Metric;

public class LibDef extends Element {
	
	
	public LibDef(String name)
	{
		super(name);
	}
	
	
	public String toString()
	{
		String toReturn = "LIB:";
		toReturn += this.getName();
		toReturn += " : ";
		Iterator<Metric<?>> i = this.getMetrics().iterator();
		while (i.hasNext())
		{
			toReturn += i.next().toString();
			toReturn += ",";
		}
		return toReturn;
	}
	
	public Object accept(Visitor v)
	{
		return v.visit(this);
	}


	@Override
	public String getInterfacesString() {
		return "";
	}


	@Override
	public void setInterfaces(List<String> liste) {
		
	}

}

package verso.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import verso.model.metric.Metric;

public class ClassDef extends Element{
	
	private HashMap<String,InterfaceDef> interfaces = new HashMap<String,InterfaceDef>(); 
	private HashSet<String> textInterfaces = new HashSet<String>();
//	private HashMap<String, Method> methods = new HashMap<String, Method>();
//	private HashMap<String, Attribute> attributes = new HashMap<String, Attribute>();
	
	public ClassDef(String name) {
		super(name);
	}

	public void addInterface(InterfaceDef inter) {
		this.interfaces.put(inter.getName(), inter);
	}

	public void addInterface(String inter) {
		this.textInterfaces.add(inter);
	}

	public void setInterfaces(List<String> interfaces) {
		for (String s : interfaces) {
			this.addInterface(s);
		}
	}
	
	public String toString() {
		String toReturn = "";
		toReturn += this.getName();
		toReturn += " : ";
		Iterator<Metric<?>> i = this.getMetrics().iterator();
		while (i.hasNext()) {
			toReturn += i.next().toString();
			toReturn += ",";
		}
		return toReturn;
	}

	public Object accept(Visitor v) {
		return v.visit(this);
	}

	public String getInterfacesString() {
		String toReturn = "";

		for (String s : this.textInterfaces) {
			toReturn += s + ",";
		}
		if (toReturn.length() > 1)
			toReturn = toReturn.substring(0, toReturn.length() - 2);
		return toReturn;
	}
}

package vieuxMetricModel;


public class Package extends PackageContainer{

	
	
	String NameFragment = "";
	
	public Package(String name)
	{
		super(name);
	}
	
	public void setNameFrag(String nameFrag)
	{
		this.NameFragment = nameFrag;
	}
	
	public void addParent(PackageContainer p)
	{
		this.parent = p;
	}
	
	public String toStringSpecial(int indent)
	{
		String space = "";
		for (int i = 0; i < indent; i++)
		{
			space += "  ";
		}
		
		String toReturn = "";
		toReturn += space + "PackageName : " + this.getFullName();
		toReturn += "\n";
		
		for (Metric m : this.metrics.values())
		{
			toReturn += space + "  " + "Metric " + m.getName() + " : " + m.getValue() + "\n";
		}
		
		for (Class c : this.types.values())
		{
			toReturn += c.toStringSpecial(indent + 1);
		}
		//toReturn += "]\n";
		
		for (Package p : this.packages.values())
		{
			toReturn += p.toStringSpecial(indent+1);
		}
		
		return toReturn;
	}
	
}

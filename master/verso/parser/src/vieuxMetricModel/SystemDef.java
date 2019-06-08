package vieuxMetricModel;

import java.util.Collection;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class SystemDef extends PackageContainer{

	Hashtable<String,Class> allClasses = new Hashtable<String,Class>();
	Hashtable<String,Method> allMethods = new Hashtable<String,Method>();
	Hashtable<String,Attribute> allAttributes = new Hashtable<String,Attribute>();
	
	public SystemDef(String name)
	{
		super(name);
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	public String getName()
	{
		if (this.name == null)
			return "null";
		return this.name;
	}
	
	
	
	public void addClass(Class c)
	{
		allClasses.put(c.getName(),c);
		PackageContainer currPack = this;
		Package secondPack = null;
		//Introduce class in the hierarchy
		String className = c.getPackage();
		String packageFrag = "";
		StringTokenizer st = new StringTokenizer(className, ".");
		for(;st.hasMoreTokens();)
		{
			packageFrag = st.nextToken();
			if (currPack.getHashPackages().containsKey(packageFrag))
			{
				currPack = currPack.getPackage(packageFrag);
			}
			else
			{
				secondPack = new Package(packageFrag);
				secondPack.addParent(currPack);
				currPack.addPackage(secondPack);
				currPack = secondPack;
			}
			
			if(!st.hasMoreTokens())
			{
				currPack.addType(c);
			}
		}
	}
	
	public void addMethod(Method m)
	{
		allMethods.put(m.getName(), m);
	}
	
	public void addAttribute(Attribute a)
	{
		allAttributes.put(a.getName(), a);
	}
	
	public String toStringSpecial(int indent)
	{
		String toReturn = "";
		for (int i = 0 ; i < indent; i++)
		{
			toReturn += "  ";
		}
		toReturn += "ProjectName : " + this.getName();
		toReturn += " [";
		for (Class c : this.types.values())
		{
			toReturn += c.getName() + ",";
		}
		toReturn += "]\n";
		
		for (Package p : this.packages.values())
		{
			toReturn += p.toStringSpecial(indent+1) + "\n";
		}
		return toReturn;
	}
	
	public Collection<Class> getClasses()
	{
		return this.allClasses.values();
	}
	
	public Collection<Method> getMethods()
	{
		return this.allMethods.values();
	}
}

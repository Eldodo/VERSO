package vieuxMetricModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class PackageContainer extends Element{
	protected Hashtable<String,Package> packages = new Hashtable<String,Package>();
	protected Hashtable<String,Class> types = new Hashtable<String,Class>();
	
	protected PackageContainer parent;
	public PackageContainer(String name)
	{
		super(name);
	}
	
	public Package getPackage(String name)
	{
		return packages.get(name);
	}
	
	public Collection<Package> getPackages()
	{
		return packages.values();
	}
	public Hashtable<String,Package> getHashPackages()
	{
		return this.packages;
	}
	
	public Set<Package> getAllPackages()
	{
		Set<Package> allpac = new HashSet<Package>();
		allpac.addAll(this.packages.values());
		for (Package p : packages.values())
		{
			allpac.addAll(p.getAllPackages());
		}
		
		return allpac;
	}
	
	public void addPackage(Package p)
	{
		packages.put(p.getName(), p);
	}
	
	public void addType(Class c)
	{
		this.types.put(c.getName(), c);
	}
	
	public String getFullName()
	{
		if (this.parent == null || this.parent instanceof SystemDef)
		{
			return this.getName();
		}
		else
		{
			return this.parent.getFullName() + "." + this.getName();
		}
		
	}
	
	//Metrics
	//Nombre de targets à l'extérieur du package
	protected int CouputePackageCoupling()
	{
		Set<String> targetMatches = new HashSet<String>();
		Set<Class> classes = getAllClasses();
		Set<String> tars;
		for (Class c : classes)
		{
			tars = c.getTargets();
			for (String tar : tars)
			{
				//System.out.print("Full Name : " + this.getFullName() +" --> " + "Target : " + tar);
				if (!tar.contains(this.getFullName()))
				{
					//System.out.println("[Added]");
					targetMatches.add(tar);
				}
				/*
				else
				{
					//System.out.println("[NOT Added]");
				}
				*/
			}
		}
		return targetMatches.size();
	}
	
	public void computeCoupling()
	{
		this.addMetric(new Metric("PackageCoupling", this.CouputePackageCoupling()));
		for (Package p : this.packages.values())
		{
			p.computeCoupling();
		}
	}
	
	protected Set<Class> getAllClasses()
	{
		Set<Class> allclasses = new HashSet<Class>();
		for (Package p : packages.values())
		{
			allclasses.addAll(p.getAllClasses());
		}
		allclasses.addAll(this.types.values());
		return allclasses;
	}
	//Average DIT of classes
	protected double computePackageDIT()
	{
		int counter =0;
		double dit =0.0;
		for (Class c : this.getAllClasses())
		{
			counter++;
			dit+= c.getMetric("DIT").getValue();
		}
		return dit/(double)counter;
	}
	
	public void computeDIT()
	{
		this.addMetric(new Metric("PackageDIT", this.computePackageDIT()));
		for (Package p : this.packages.values())
		{
			p.computeDIT();
		}
	}
	
	//Sum of complexity of classes
	protected  int computePackageComplexity()
	{
		int complex =0;
		for (Class c : this.getAllClasses())
		{
			complex += c.getMetric("WMC").getValue();
		}
		return complex;
	}
	
	public void computeComplexity()
	{
		this.addMetric(new Metric("PackageComplexity", this.computePackageComplexity()));
		for (Package p : this.packages.values())
		{
			p.computeComplexity();
		}
	}
}

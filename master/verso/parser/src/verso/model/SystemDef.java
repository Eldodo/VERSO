package verso.model;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import verso.model.history.Commit;
import verso.model.metric.IntervaleMetricDescriptor;
import verso.model.metric.NominaleMetricDescriptor;
import verso.model.metric.paquetage.MetricQualPackageVisitor;
import verso.model.metric.paquetage.MetricQualPackageVisitorPartial;
import verso.model.metric.paquetage.SVNPackageVisitor;
import verso.model.metric.paquetage.SVNPackageVisitorPartial;
import verso.saving.csv.CsvBugParser;
import verso.saving.csv.IRConceptParser;

public class SystemDef {
	
	public static final int  QUALITYMETRIC =1;
	public static final int SVNMETRIC = 2;
	
	//private long lastKnownRevision = 0;
	private HashMap<File,Long> fragRootsRevision;
	private long lastVersion = 0;
	private String name;
	private HashMap<String,Element> allClasses;
	private HashMap<String,Package> packages;
	private HashMap<String,Package> allPackages;
	private HashMap<String,Method> allMethods;
	private TreeMap<Long,SystemDef> allVersions;
	private HashMap<Long,Bug> allBugs;
	private List<File> rootLocations;
	
	//SVN Stuff
	private Set<String> allAuthors;
	public static IntervaleMetricDescriptor<Double> NUMBEROFAUTHORSDESCRIPTOR = new IntervaleMetricDescriptor<Double>("NumberOfAutors", 0.0,25.0);
	public static IntervaleMetricDescriptor<Double> NUMBEROFCOMMITSDESCRIPTOR = new IntervaleMetricDescriptor<Double>("NumberOfCommits", 0.0,100.0);
	public static IntervaleMetricDescriptor<Date> LASTUPDATEDESCRIPTOR = new IntervaleMetricDescriptor<Date>("LastUpdate", getDate1990(), new Date(System.currentTimeMillis()));
	public static IntervaleMetricDescriptor<Date> DATEINTRODESCRIPTOR = new IntervaleMetricDescriptor<Date>("DateIntro", getDate1990(), new Date(System.currentTimeMillis()));
	public static IntervaleMetricDescriptor<Double> PACKAGEDITDESCRIPTOR = new IntervaleMetricDescriptor<Double>("PackageDIT", 0.0,10.0);
	public static IntervaleMetricDescriptor<Double> PACKAGECOMPLEXITYDESCRIPTOR = new IntervaleMetricDescriptor<Double>("PackageComplexity", 0.0,1000.0);
	public static IntervaleMetricDescriptor<Double> PACKAGECOUPLINGDESCRIPTOR = new IntervaleMetricDescriptor<Double>("PackageCoupling", 0.0,50.0);
	public static IntervaleMetricDescriptor<Date> PACKAGEDATEINTRODESCRIPTOR = new IntervaleMetricDescriptor<Date>("PackageIntroDate", getDate1990(), new Date(System.currentTimeMillis()));
	public static IntervaleMetricDescriptor<Date> PACKAGEUPDATEDESCRIPTOR = new IntervaleMetricDescriptor<Date>("PackageUpDate", getDate1990(), new Date(System.currentTimeMillis()));
	public static IntervaleMetricDescriptor<Double> PACKAGENUMBEROFCOMMITSDESCRIPTOR = new IntervaleMetricDescriptor<Double>("PackageNumberOfCommits", 0.0,1000.0);
	public static IntervaleMetricDescriptor<Double> PACKAGENUMBEROFAUTHORSDESCRIPTOR = new IntervaleMetricDescriptor<Double>("PackageNumberOfAuthors", 0.0,1000.0);
	
	//Bug Info
	public static IntervaleMetricDescriptor<Double> NUMBEROFOPENBUG = new IntervaleMetricDescriptor<Double>("NumberOfOpenBugs", 0.0,10.0);
	public static IntervaleMetricDescriptor<Double> TOTALNUMBEROFBUG = new IntervaleMetricDescriptor<Double>("TotalNumberOfBugs", 0.0,25.0);
	public static IntervaleMetricDescriptor<Date> LASTCLOSEBUG = new IntervaleMetricDescriptor<Date>("LastClosedBug", getDate1990(), new Date(System.currentTimeMillis()));
	public static IntervaleMetricDescriptor<Date> FIRSTINTROBUG = new IntervaleMetricDescriptor<Date>("FirstIntroBug", getDate1990(), new Date(System.currentTimeMillis()));
	public static IntervaleMetricDescriptor<Long> AVERAGETIMEABUGISOPEN = new IntervaleMetricDescriptor<Long>("TimeToRepairABug", 0l, 31622400000l);
	private NominaleMetricDescriptor<String> MAINAUTHORDESCRIPTOR = null;
	//private NominaleMetricDescriptor<String> PACKAGEMAINAUTHORDESCRIPTOR = null;
	private NominaleMetricDescriptor<String> MAINBUGPROGRAMMER = null;
	private NominaleMetricDescriptor<String> BUGAUTHOR = null;
	
	//Bug Package
	public static IntervaleMetricDescriptor<Double> PACKAGENUMBEROFOPENBUG = new IntervaleMetricDescriptor<Double>("PackageNumberOfOpenBugs", 0.0, 25.0);
	public static IntervaleMetricDescriptor<Double> PACKAGETOTALNUMBEROFBUG = new IntervaleMetricDescriptor<Double>("PackageTotalNumberOfBugs", 0.0, 25.0);
	public static IntervaleMetricDescriptor<Long> PACKAGEAVERAGETIMEABUGISOPEN = new IntervaleMetricDescriptor<Long>("PackageTimeToRepairABug", 0l, 31622400000l);
	public static IntervaleMetricDescriptor<Date> PACKAGELASTCLOSEBUG = new IntervaleMetricDescriptor<Date>("PackageLastClosedBug", getDate1990(), new Date(System.currentTimeMillis()));
	public static IntervaleMetricDescriptor<Date> PACKAGEDATEINTROBUG = new IntervaleMetricDescriptor<Date>("PackageFirstIntroBug", getDate1990(), new Date(System.currentTimeMillis()));
	private NominaleMetricDescriptor<String> PACKAGEMAINAUTHORDESCRIPTOR = null;
	private NominaleMetricDescriptor<String> PACKAGEMAINBUGPROGRAMMER = null;
	private NominaleMetricDescriptor<String> PACKAGEBUGAUTHOR = null;
	
	public static Date getDate1990()
	{
		Date d = new Date();
		try{
			d = DateFormat.getInstance().parse("1990-01-01  00:00:00");
		}catch(Exception e){System.out.println(e);}
		return d;
	}
	
	public SystemDef(String name)
	{
		this.name = name;
		allClasses = new HashMap<String,Element>();
		packages = new HashMap<String,Package>();
		allPackages = new HashMap<String,Package>();
		allMethods = new HashMap<String,Method>();
		allAuthors = new HashSet<String>();
		allBugs = new HashMap<Long, Bug>();
		fragRootsRevision = new HashMap<File,Long>();
		this.MAINAUTHORDESCRIPTOR = new NominaleMetricDescriptor<String>("MainAuthor", new HashSet<String>());
		this.PACKAGEMAINAUTHORDESCRIPTOR = new NominaleMetricDescriptor<String>("PackageMainAuthor", new HashSet<String>());
		this.BUGAUTHOR = new NominaleMetricDescriptor<String>("BugAuthor", new HashSet<String>());
		this.PACKAGEBUGAUTHOR = new  NominaleMetricDescriptor<String>("PackageBugAuthor", new HashSet<String>());
		this.MAINBUGPROGRAMMER = new NominaleMetricDescriptor<String>("MainBugProgrammer", new HashSet<String>());
		this.PACKAGEMAINBUGPROGRAMMER = new NominaleMetricDescriptor<String>("PackageMainBugProgrammer", new HashSet<String>());
		//this.MAINBUGPROGRAMMER = new NominaleMetricDescriptor<String>("MainBugProgrammer", new HashSet<String>());
		allVersions = new TreeMap<Long,SystemDef>();
		this.rootLocations = new ArrayList<File>();
	}

	public void addSystemVersion(Long versionName, SystemDef s)
	{
		this.allVersions.put(versionName, s);
	}
	
	public SystemDef getVersion(Long version)
	{
		return this.allVersions.get(version);
	}
	
	public Set<Long> getAllVersionName()
	{
		return this.allVersions.keySet();
	}
	
	public Collection<Element> getAllElements()
	{
		return this.allClasses.values();
	}
	
	public Collection<Bug> getAllBugs()
	{
		return this.allBugs.values();
	}
	
	public HashMap<Long,Bug> getBugMap()
	{
		return this.allBugs;
	}
	
	public NominaleMetricDescriptor<String> getMainAuthorDescriptor()
	{
		return MAINAUTHORDESCRIPTOR;
	}
	
	public NominaleMetricDescriptor<String> getPackageMainAuthorDescriptor()
	{
		return PACKAGEMAINAUTHORDESCRIPTOR;
	}
	
	public NominaleMetricDescriptor<String> getMainBugProgrammeurDescriptor()
	{
		return MAINBUGPROGRAMMER;
	}
	
	public NominaleMetricDescriptor<String> getBugAuthorDescriptor()
	{
		return this.BUGAUTHOR;
	}
	
	public NominaleMetricDescriptor<String> getPackageMainBugProgrammeurDescriptor()
	{
		return this.PACKAGEMAINBUGPROGRAMMER;
	}
	
	public NominaleMetricDescriptor<String> getPackageBugAuthorDescriptor()
	{
		return this.PACKAGEBUGAUTHOR;
	}
	
	public String getName()
	{
		return this.name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setRevision0()
	{
		this.fragRootsRevision.clear();
	}
	
	public void setRevision(long rev, File f)
	{
		this.fragRootsRevision.put(f, rev);
		//this.lastKnownRevision = rev;
	}
	
	public long getRevision(File f)
	{
		Long l = this.fragRootsRevision.get(f);
		if (l != null)
			return l;
		return 0;
	}
	
	public void setVersion(long ver)
	{
		this.lastVersion = ver;
	}
	
	public long getVersion()
	{
		return this.lastVersion;
	}
	
	public void addPackage(Package p)
	{
		String[] packagesString = p.getName().split("[.]");
		//allPackages.put(p.getName(), p);
		Package pac = introduceOrCopy(p);
		allPackages.put(pac.getName(), pac);
		Package tempPac = null;
		String tempPacStr = "";
		for (int i = 0; i < packagesString.length-1; i++)
		{
			tempPacStr += packagesString[i];
			tempPac = new Package(tempPacStr);
			tempPac = introduceOrCopy(tempPac);
			allPackages.put(tempPac.getName(), tempPac);
			tempPacStr += ".";
		}
	}
	
	private Package introduceOrCopy(Package p)
	{
		String packageName = p.getName();
		if (packageName.indexOf('.') == -1)
		{
			if (this.packages.containsKey(packageName))
			{
				this.packages.get(packageName).setMetrics(p.getMetrics());
				return this.packages.get(packageName);
			}
			else
			{
				this.packages.put(p.getName(), p);
				return p;
			}
		}
		else
		{
			String firstFragment = packageName.substring(0,packageName.indexOf('.'));
			if (!this.packages.containsKey(firstFragment))
			{
				this.packages.put(firstFragment,(new Package(firstFragment)));
			}
			return this.packages.get(firstFragment).introduceOrCopy(p);
		}
	}
	
	public void addElement(Element e)
	{
		if (e.getName().indexOf(".") == -1)
		{
			e.setName("default." + e.getName());
			e.setPackage("default");
		}
		Element oldE = allClasses.get(e.getName());
		List<Commit> oldCommits = null;
		if (oldE != null)
		{
			oldCommits = oldE.getCommits();
		}
		if (oldCommits != null)
		{
			for (Commit c : oldCommits)
			{
				e.addCommit(c);
			}
		}
		allClasses.put(e.getName(), e);
		String pkgName = e.getPackage();
		if (pkgName.compareTo("")==0)
			pkgName = "default";
		Package p = new Package(pkgName);
		this.addPackage(p);
		this.allPackages.get(pkgName).addElement(e);	
		
	}
	
	public Element getElement(String name)
	{
		return allClasses.get(name);
	}
	
	public Package getPackage(String name)
	{
		return allPackages.get(name);
	}
	
	public Method getMethod(String name)
	{
		return allMethods.get(name);
	}
	
	public void removeElement(String elemName)
	{
		allClasses.remove(elemName);
		String packName = "";
		if (elemName.contains("."))
			packName= elemName.substring(0, elemName.lastIndexOf("."));
		else
			packName = "default";
		Package p = this.allPackages.get(packName);
		p.removeElement(elemName);
		if (p.elemlist.size() == 0 && p.packagelist.size() == 0)
		{
			this.allPackages.remove(packName);
			this.removePackage(packName);
		}
	}
	public void removePackage(String packName)
	{
		if (!packName.contains("."))
		{
			this.packages.remove(packName);
		}
		else
		{
			String parentName = packName.substring(0, packName.lastIndexOf("."));
			Package p = this.allPackages.get(parentName);
			p.removePackage(packName);
			if (p.packagelist.size() ==0 && p.elemlist.size() ==0)
				removePackage(parentName);
		}
	}
	
	public void addMethod(Method m)
	{
		allMethods.put(m.getName(), m);
		String methodName = m.getName().substring(0,m.getName().indexOf("("));
		String className = methodName.substring(0,methodName.lastIndexOf("."));
		if (className.indexOf(".") == -1)
			className = "default." + className;
		this.allClasses.get(className).addMethod(m);
	}
	
	public void addRootLocation(File f)
	{
		this.rootLocations.add(f);
	}
	
	public List<File> getRoots()
	{
		return this.rootLocations;
	}
	
	public Collection<Method> getMethods()
	{
		return this.allMethods.values();
	}
	
	public Collection<Package> getPackages()
	{
		return this.packages.values();
	}
	public Collection<Package> getAllPackages()
	{
		return this.allPackages.values();
	}
	public Collection<Element> getClasses()
	{
		return this.allClasses.values();
	}
	
	public String toString()
	{
		String toReturn = "Model :" + this.getName() + "\n";
		//toReturn += "salut";
		System.out.println(allClasses.values().size());
		Iterator<Element> i = allClasses.values().iterator();
		while (i.hasNext())
		{
			//toReturn += "checkpoint";
			toReturn += i.next().toString();
			toReturn += "\n";
		}
		return toReturn;
	}
	
	public String ToStringSpecial(int indent)
	{
		System.out.println("------------------------------------");
		String toReturn = "";
		for (int i =0; i < indent; i++)
		{
			toReturn += "  ";
		}
		toReturn += "" + this.getName() + "\n";
		for (Package p : this.packages.values())
		{
			toReturn += p.toStringSpecial(indent+1);
		}
		return toReturn;
	}
	
	public void addAuthor(String auth)
	{
		this.allAuthors.add(auth);
		this.MAINAUTHORDESCRIPTOR.addValueInSet(auth);
		this.PACKAGEMAINAUTHORDESCRIPTOR.addValueInSet(auth);
	}
	
	public void addBugAuthor(String auth)
	{
		this.BUGAUTHOR.addValueInSet(auth);
		this.PACKAGEBUGAUTHOR.addValueInSet(auth);
	}
	
	public void addBugProgrammer(String prog)
	{
		this.MAINBUGPROGRAMMER.addValueInSet(prog);
		this.PACKAGEMAINBUGPROGRAMMER.addValueInSet(prog);
	}
	
	public void addBug(Bug b)
	{
		this.allBugs.put(b.getNumberID(), b);
		this.addBugAuthor(b.getFirstAuthor());
		this.addBugProgrammer(b.getMainProg());
	}
	public String getAllAuthors()
	{
		String toReturn = "";
		for (String s : this.allAuthors)
		{
			toReturn += s + ":";
		}
		toReturn = toReturn.substring(0,toReturn.length()-1);
		return toReturn;
	}
	
	public void computePackageMetricsQual()
	{
		for (Package p : this.packages.values())
		{
			p.accept(new MetricQualPackageVisitor());
		}
	}
	public void computePackageMetricsQualPartial()
	{
		for (Package p : this.packages.values())
		{
			p.accept(new MetricQualPackageVisitorPartial());
		}
	}
	
	public void setDirtyPackages(String pac, int metricType)
	{
		String currPath = "";
		String[] splitPac = pac.split("[.]");
		int pathSegment = splitPac.length;
		for (int i = pathSegment -1; i >= 0; i--)
		{
			currPath = "";
			//path creation
			for (int j = 0; j <= i; j++)
			{
				currPath += splitPac[j] + ".";
			}
			if (currPath.length() > 1)
			{
				currPath = currPath.substring(0, currPath.length()-1);
			}
			if (this.allPackages.containsKey(currPath))
			{
				switch (metricType)
				{
					case QUALITYMETRIC :
					{
						this.allPackages.get(currPath).setDirtyMetricQual();
						break;
					}
					case SVNMETRIC : 
					{
						this.allPackages.get(currPath).setDirtyMetricSVN();
						break;
					}
				}
			}
		}
	}
	
	public void removeCommits()
	{
		for (Element e : allClasses.values())
		{
			e.getCommits().clear();
		}
	}
	public void computeSVNMetrics()
	{
		for (Element e : this.allClasses.values())
		{
			e.computeSVNMetrics(this);
		}
		for (Package p : this.packages.values())
		{
			SVNPackageVisitor svnpv = new SVNPackageVisitor();
			svnpv.setSystem(this);
			p.accept(svnpv);
		}
	}
	public void computeSVNMetricsPartial()
	{
		
		for (Package p : this.packages.values())
		{
			SVNPackageVisitorPartial svnpvp = new SVNPackageVisitorPartial();
			svnpvp.setSystem(this);
			p.accept(svnpvp);
		}
	}
	
	public void computePackageMetricsSVN(String pac)
	{
		Package p = null;
		while (pac.length() > 1)
		{
			p = this.allPackages.get(pac);
			if (p != null)
			{
				SVNPackageVisitor v = new SVNPackageVisitor();
				v.setSystem(this);
				p.accept(v);
			}
			if (pac.lastIndexOf(".") != -1)
				pac = pac.substring(0, pac.lastIndexOf("."));
			else
				pac = "";
		}
	}
	
	public void clearBugs()
	{
		for (Element e : allClasses.values())
		{
			e.clearBugs();
		}
	}
	
	public void computeBugMetrics()
	{
		this.computeBugMetrics(((IProject) ResourcesPlugin.getWorkspace().getRoot().getProject(this.getName())));
		//refaire le display
	}
	public void computeBugMetrics(IProject p)
	{
		this.clearBugs();
		IPath path = p.getLocation().append(new Path("save.bug"));
		if (path.toFile().exists())
		{
			CsvBugParser.parseBugFile(path.toFile().toString(), this);
			for (Package pac : allPackages.values())
			{
				pac.computeBugMetrics(this);
			}
			for (Element e : allClasses.values())
			{
				e.computeBugMetrics(this);
			}
			for (Method m : allMethods.values())
			{
				m.computeBugMetrics(this);
			}
		}
	}
	
	public Object accept(Visitor v)
	{
		return v.visit(this);
	}
	
	/**
	 * Parse IR folder to add concepts to the classes
	 */
	public void addConceptsToClasses() {
		IRConceptParser.parse(allClasses);
	}
}

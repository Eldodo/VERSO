package verso.saving.csv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import verso.model.ClassDef;
import verso.model.Element;
import verso.model.Method;
import verso.model.Package;
import verso.model.SystemDef;
import verso.model.metric.Metric;
public class CsvWriter {

	SystemDef sysdef;
	PrintStream p = null;
	List<String> metricNamePac = new ArrayList<String>();
	List<String> metricNameClasse = new ArrayList<String>();
	List<String> metricNameMethode = new ArrayList<String>();
	
	public CsvWriter(SystemDef sysdef)
	{
		this.sysdef = sysdef;
		try{
			p = new PrintStream(new FileOutputStream("C:\\Users\\vandammd\\Documents\\" + sysdef.getName() + ".csv"));
		}catch(Exception e){System.out.println(e);}
	}
	
	public CsvWriter(SystemDef sysdef, String path)
	{
		this.sysdef = sysdef;
		try{
			p = new PrintStream(new FileOutputStream(path));
		}catch(Exception e){System.out.println(e);}
	}
	
	public void print()
	{
		printRevision();
		printPackages();
		printClasses();
		printMethods();
	}
	
	private void printRevision()
	{
		p.print("LastVersion;" + this.sysdef.getVersion());
		for (File f : sysdef.getRoots())
		{
			p.print(";LastKnownRevision;" + this.sysdef.getRevision(f));
		}
		p.println();
		
	}
	
	private void printPackages()
	{
		Collection<Package> pacs = sysdef.getAllPackages();
		Package pac = pacs.iterator().next();
		printPackageHead(pac);
		for (Package p : pacs)
		{
			printEachPackage(p);
		}
	}
	
	private void printClasses()
	{
		Collection<Element> classes = sysdef.getClasses();
		Element el = classes.iterator().next();
		printClassHead(el);
		for (Element c : classes)
		{
			printEachClass(c);
		}
	}
	
	private void printMethods()
	{
		Collection<Method> methods = sysdef.getMethods();
		if (methods.size() == 0)
			return;
		Method met = methods.iterator().next();
		printMethodHead(met);
		for (Method m : methods)
		{
			printEachMethod(m);
		}
	}
	
	private void printPackageHead(Package pac)
	{
		String head = "Head;Package;";
		for (Metric m : pac.getMetrics())
		{
			head += m.getName() + ";";
			metricNamePac.add(m.getName());
		}
		head = head.substring(0, head.length()-1);
		head += "\n";
		/*
		p.println("Head;Package;" +
				"PackageDIT;" +
				"PackageCoupling;" +
				"PackageComplexity;" +
				"PackageIntroDate;" +
				"PackageUpDate;" +
				"PackageNumberOfCommits;" +
				"PackageNumberOfAuthors;" +
				"PackageMainAuthor");*/
		head += "Type;string;";
		
		for (Metric m : pac.getMetrics()) {
			head += m.getTypeString() + ";";
		}
		head = head.substring(0, head.length()-1);
		head += "\n";	
		
		//p.println("Type;String;double;double;double;Date;Date;double;double;String");
		
		head += "Min;null;";
		for (Metric m : pac.getMetrics())
		{
			head += m.getMinString() + ";";
		}
		head = head.substring(0, head.length()-1);
		head += "\n";
		/*
		p.println("Min;null;" +
				"0;" +
				"0;" +
				"0;" + 
				SystemDef.PACKAGEDATEINTRODESCRIPTOR.getMin().getTime() + ";" + 
				SystemDef.PACKAGEUPDATEDESCRIPTOR.getMin().getTime() + ";" + 
				SystemDef.PACKAGENUMBEROFCOMMITSDESCRIPTOR.getMin() + ";" + 
				SystemDef.PACKAGENUMBEROFAUTHORSDESCRIPTOR.getMin() + ";" + 
				"null");
		*/
		head += "Max;null;";
		for (Metric m : pac.getMetrics())
		{
			head += m.getMaxString() + ";";
		}
		head = head.substring(0, head.length()-1);
		/*
		p.println("Max;null;" +
				"10;" +
				"50;" +
				"1000;" + 
				SystemDef.PACKAGEDATEINTRODESCRIPTOR.getMax().getTime() + ";" + 
				SystemDef.PACKAGEUPDATEDESCRIPTOR.getMax().getTime() + ";" + 
				SystemDef.PACKAGENUMBEROFCOMMITSDESCRIPTOR.getMax() + ";" + 
				SystemDef.NUMBEROFAUTHORSDESCRIPTOR.getMax() + ";" + 
				sysdef.getPackageMainAuthorDescriptor().getSetString());
				*/
		p.println(head);
	}
	
	private void printEachPackage(Package pac)
	{
		String pacString = "";
		System.out.println(pac.getName());
		for (Metric m :pac.getMetrics())
		{
			System.out.println("   " + m.getName() + ":" + m.getValue());
		}
		try{
			pacString = "Data;" + pac.getName() + ";";
			for (String s : metricNamePac)
			{
				pacString += pac.getMetric(s).getTextualValue() + ";";
			}
			pacString = pacString.substring(0, pacString.length()-1);
			/*
			p.println("Data;" + pac.getName() + ";" + 
					pac.getMetric("PackageDIT").getTextualValue() + ";" + 
					pac.getMetric("PackageCoupling").getTextualValue() + ";" + 
					pac.getMetric("PackageComplexity").getTextualValue() + ";" + 
					pac.getMetric(SystemDef.PACKAGEDATEINTRODESCRIPTOR.getName()).getTextualValue() + ";" + 
					pac.getMetric(SystemDef.PACKAGEUPDATEDESCRIPTOR.getName()).getTextualValue() + ";" + 
					pac.getMetric(SystemDef.PACKAGENUMBEROFCOMMITSDESCRIPTOR.getName()).getTextualValue() + ";" + 
					pac.getMetric(SystemDef.PACKAGENUMBEROFAUTHORSDESCRIPTOR.getName()).getTextualValue() + ";" + 
					pac.getMetric(sysdef.getPackageMainAuthorDescriptor().getName()).getTextualValue());
			*/
			p.println(pacString);
		}catch(Exception e){System.out.println(e);}
	}
	
	private void printClassHead(Element c) {
		String head = "Head;Class;Type;Package;SuperClass;Interfaces;Targets;Commits;";
		for (Metric m : c.getMetrics()) {
			head += m.getName() + ";";
			metricNameClasse.add(m.getName());
		}
		head = head.substring(0, head.length()-1);
		head += "\n";
		/*
		p.println("Head;Class;Type;Package;SuperClass;Interfaces;Targets;Commits;" +
				"WMC;" +
				"CBO;" +
				"LCOM5;" +
				"DIT;" +
				"NumberOfAutors;" +
				"NumberOfCommits;" +
				"LastUpdate;" +
				"DateIntro;" +
				"MainAuthor");
		*/
		head += "Type;string;string;string;string;stringList;stringList;list;";
		for (Metric m : c.getMetrics()) {
			head += m.getTypeString() + ";";
		}
		head = head.substring(0, head.length()-1);
		head += "\n";
		//p.println("Type;String;String;String;String;StringList;StringList;List;double;double;double;double;double;double;Date;Date;String");
		
		head += "Min;null;null;null;null;null;null;null;";
		for (Metric m : c.getMetrics()) {
			head += m.getMinString() + ";";
		}
		head = head.substring(0, head.length()-1);
		head += "\n";
		//p.println("Min;null;null;null;null;null;null;null;0;0;0;0;" + SystemDef.NUMBEROFAUTHORSDESCRIPTOR.getMin() + ";" + SystemDef.NUMBEROFCOMMITSDESCRIPTOR.getMin() + ";" + SystemDef.LASTUPDATEDESCRIPTOR.getMin().getTime() + ";" + SystemDef.DATEINTRODESCRIPTOR.getMin().getTime() + ";" + "null");
		head += "Max;null;null;null;null;null;null;null;";
		for (Metric m : c.getMetrics()) {
			head += m.getMaxString() + ";";
		}
		head = head.substring(0, head.length()-1);
		//p.println("Max;null;null;null;null;null;null;null;100;25;2;10;" + SystemDef.NUMBEROFAUTHORSDESCRIPTOR.getMax() + ";" + SystemDef.NUMBEROFCOMMITSDESCRIPTOR.getMax() + ";" + SystemDef.LASTUPDATEDESCRIPTOR.getMax().getTime() + ";" + SystemDef.DATEINTRODESCRIPTOR.getMax().getTime() + ";" + sysdef.getMainAuthorDescriptor().getSetString());
		p.println(head);
	}
	
	private void printEachClass(Element c)
	{
		String classeString = "";
		String type = "";
		if (c instanceof ClassDef)
			type = "Class";
		else
			type = "Interface";
		
		classeString = "Data;" + c.getName() + ";" + type + ";" + c.getPackage() + ";" + c.getParentText() + ";" + c.getInterfacesString() + ";" + c.getTargetString() + ";" + c.getCommitString().replaceAll("[\\n]", " ").replaceAll("[\\r]", " ") + ";";
		
		for (String s : metricNameClasse)
		{
			classeString += c.getMetric(s).getTextualValue() + ";";
		}
		classeString = classeString.substring(0, classeString.length()-1);
		/*
		p.println("Data;" + c.getName() + ";" + 
				type + ";" +
				c.getPackage() + ";" +
				c.getParent() + ";" +
				/*c.getAncestorsString()+ ";" +*/
		/*
				c.getInterfacesString() + ";" +
				c.getTargetString() + ";" +
				c.getCommitString() + ";" +
				c.getMetric("WMC").getTextualValue() + ";" + 
				c.getMetric("CBO").getTextualValue() + ";" + 
				c.getMetric("LCOM5").getTextualValue() + ";" + 
				c.getMetric("DIT").getTextualValue() + ";" +
				c.getMetric(SystemDef.NUMBEROFAUTHORSDESCRIPTOR.getName()).getTextualValue() + ";" +
				c.getMetric(SystemDef.NUMBEROFCOMMITSDESCRIPTOR.getName()).getTextualValue() + ";" +
				c.getMetric(SystemDef.LASTUPDATEDESCRIPTOR.getName()).getTextualValue() + ";" + 
				c.getMetric(SystemDef.DATEINTRODESCRIPTOR.getName()).getTextualValue() + ";" + 
				c.getMetric(sysdef.getMainAuthorDescriptor().getName()).getTextualValue());
			*/
		p.println(classeString);
	}
	
	private void printMethodHead(Method met)
	{
		String head = "Head;Method;ReturnType;Signature;Targets;";
		for (Metric m : met.getMetrics())
		{
			head += m.getName() + ";";
			metricNameMethode.add(m.getName());
		}
		head = head.substring(0, head.length()-1);
		head += "\n";
		//p.println("Head;Method;ReturnType;Targets;MCMethod;CohMethod;DITMethod;CouplingMethod");
		head += "Type;string;string;string;stringList;";
		for (Metric m : met.getMetrics())
		{
			head += m.getTypeString() + ";";
		}
		head = head.substring(0, head.length()-1);
		head += "\n";
		//p.println("Type;String;String;StringList;double;double;double;double");
		head += "Min;null;null;null;null;";
		for (Metric m : met.getMetrics())
		{
			head += m.getMinString() + ";";
		}
		head = head.substring(0, head.length()-1);
		head += "\n";
		//p.println("Min;null;null;null;0;0;0;0");
		head += "Max;null;null;null;null;";
		for (Metric m : met.getMetrics())
		{
			head += m.getMaxString() + ";";
		}
		head = head.substring(0, head.length()-1);
		//p.println("Max;null;null;null;50;1;10;25");
		p.println(head);
	}
	
	private void printEachMethod(Method m) {
		String methodString = "";
		methodString = "Data;" + m.getName() + ";" + m.getReturnType() + ";" + m.getSignature().replaceAll(";", ":") + ";" + m.getTargetString()
				+ ";";
		for (String s : metricNameMethode) {
			methodString += m.getMetric(s).getTextualValue() + ";";
		}
		methodString = methodString.substring(0, methodString.length() - 1);
		/*
		 * p.println("Data;" + m.getName() + ";" + m.getReturnType() + ";" + /*m.getOverridenFromString()+ ";" +
		 *//*
			 * m.getTargetString() + ";" + m.getMetric("MCMethod").getTextualValue() + ";" + m.getMetric("CohMethod").getTextualValue() + ";" +
			 * m.getMetric("DITMethod").getTextualValue() + ";" + m.getMetric("CouplingMethod").getTextualValue());
			 */
		p.println(methodString);
	}
}

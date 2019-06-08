package verso.saving.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import verso.model.ClassDef;
import verso.model.Element;
import verso.model.Entity;
import verso.model.Method;
import verso.model.Package;
import verso.model.SystemDef;
import verso.model.history.Commit;
import verso.model.metric.DateMetric;
import verso.model.metric.DecimalNumberMetric;
import verso.model.metric.IntegralNumberMetric;
import verso.model.metric.IntervaleMetricDescriptor;
import verso.model.metric.Metric;
import verso.model.metric.MetricDescriptor;
import verso.model.metric.NominaleMetric;
import verso.model.metric.NominaleMetricDescriptor;

public class CsvParser2 {

	private static SystemDef sys;
	private static BufferedReader input = null;
	private static ELEMENT_TYPE currentState = ELEMENT_TYPE.PACKAGE;
	
	public enum ELEMENT_TYPE {
		PACKAGE, CLASS, METHOD
	}
	
	private static List<MetricDescriptor> metricDescList;
	private static List<String> metricList;
	private static List<String> typeList;
	private static List<String> metricMin;
	private static List<String> metricMax;
	
	
	public static SystemDef parseFile(File f) throws Exception {
		sys = new SystemDef(f.getAbsolutePath());
		String line = null;
		int i = 1;
		input = new BufferedReader(new FileReader(f));
		while ((line = input.readLine()) != null) {
			i += processLine(line, i);
		}
		input.close();
		return sys;
	}

	private static int processLine(String line, int linenumber) {
		// System.out.println(line);
		line = line.trim();
		String firstToken = line.substring(0, line.indexOf(';'));
		if (firstToken.compareTo("LastVersion") == 0) {
			return processRevision(line);
		} else if (firstToken.compareTo("Head") == 0) {
			return processHead(line, linenumber);
		} else {
			return processData(line, linenumber);
		}
	}
	
	private static int processRevision(String line) {
		String[] revs = line.split(";");
		sys.setVersion(Long.parseLong(revs[1]));
		for (int i = 2; i < revs.length; i = i + 2)
			sys.setRevision(Long.parseLong(revs[i + 1]), new File(revs[i]));
		return 1; // 1 line read
	}

	private static int processHead(String line, int linenumber) {
		String[] linetab = line.split(";");
		String headType = linetab[1];
		if (headType.compareToIgnoreCase("Package") == 0) {
			return processHeadPackage(line, linenumber);
		}else if (headType.compareToIgnoreCase("Class") == 0) {
			return processHeadClass(line, linenumber);
		}else if (headType.compareToIgnoreCase("Method") == 0) {
			return processHeadMethod(line, linenumber);
		} else 
			throw new IllegalArgumentException("Line "+linenumber+": '" + headType + "' is not a valid Head type.");
	}

	private static int processData(String line, int linenumber) {
		switch (currentState) {
		case PACKAGE:
			return processPackage(line, linenumber);
		case CLASS:
			return processClass(line, linenumber);
		case METHOD:
			return processMethod(line, linenumber);
		default:
			throw new IllegalStateException("Line "+linenumber+": '" + currentState + "' is not a valid state.");
		}
	}

	private static int processHeadPackage(String line, int linenumber) {
		currentState = ELEMENT_TYPE.PACKAGE;
		metricDescList = new ArrayList<MetricDescriptor>();

		metricList = new ArrayList<String>();
		typeList = new ArrayList<String>();
		metricMin = new ArrayList<String>();
		metricMax = new ArrayList<String>();
		
		

		String[] packageTab = line.split(";");
		for (int stringIndex = 2 ; stringIndex < packageTab.length; stringIndex++) {
			metricList.add(packageTab[stringIndex]);
		}
		try {
			line = input.readLine();
		} catch (Exception e) {
			System.out.println(e);
		}
		packageTab = line.split(";");
		for (int stringIndex = 2 ; stringIndex < packageTab.length; stringIndex++) {
			typeList.add(packageTab[stringIndex]);
		}
		try {
			line = input.readLine();
		} catch (Exception e) {
			System.out.println(e);
		}
		packageTab = line.split(";");
		for (int stringIndex = 2 ; stringIndex < packageTab.length; stringIndex++) {
			metricMin.add(packageTab[stringIndex]);
		}
		try {
			line = input.readLine();
		} catch (Exception e) {
			System.out.println(e);
		}
		packageTab = line.split(";");
		for (int stringIndex = 2 ; stringIndex < packageTab.length; stringIndex++) {
			metricMax.add(packageTab[stringIndex]);
		}

		for (int i = 0; i < metricList.size(); i++) {
			Set<String> values;
			if (typeList.get(i).compareTo("int") == 0)
				metricDescList.add(new IntervaleMetricDescriptor<Integer>(metricList.get(i),
						Integer.valueOf(metricMin.get(i)), Integer.valueOf(metricMax.get(i))));
			if (typeList.get(i).compareTo("double") == 0)
				metricDescList.add(new IntervaleMetricDescriptor<Double>(metricList.get(i),
						Double.valueOf(metricMin.get(i)), Double.valueOf(metricMax.get(i))));
			if (typeList.get(i).compareTo("string") == 0) {
				values = new HashSet<String>();
				if (metricMax.get(i).compareTo("") != 0) {
					String[] splitted = metricMax.get(i).split(":");

					for (int j = 0; j < splitted.length; j++) {
						values.add(splitted[j]);
					}
				}
				metricDescList.add(new NominaleMetricDescriptor<String>(metricList.get(i), values));
			}
			if (typeList.get(i).compareTo("date") == 0)
				try {
					metricDescList.add(new IntervaleMetricDescriptor<Date>(metricList.get(i),
							new Date(Long.valueOf(metricMin.get(i))), new Date(Long.parseLong(metricMax.get(i)))));
				} catch (Exception e) {
					System.out.println(i + " " +line);
					e.printStackTrace();;

				}
		}
		return 4; // 4 lines read
	}
	
	@SuppressWarnings("unchecked")
	private static int processPackage(String line, int linenumber)
	{
		Package p;
		String packageTab[] = line.split(";");
		int stringIndex = 0;
		stringIndex++;
		p = new Package(packageTab[stringIndex]);
		stringIndex++;
		for (int i = 0; stringIndex < packageTab.length; i++, stringIndex++) {
//			System.out.println("CsvParser2.processPackage - Package typeList: "+typeList);
//			System.out.println(line);
			if (typeList.get(i).compareTo("int") == 0)
				p.addMetric(
						new IntegralNumberMetric<Integer>((IntervaleMetricDescriptor<Integer>) (metricDescList.get(i)),
								Integer.valueOf(packageTab[stringIndex])));
			if (typeList.get(i).compareTo("double") == 0)
				p.addMetric(new DecimalNumberMetric<Double>((IntervaleMetricDescriptor<Double>) (metricDescList.get(i)),
						Double.valueOf(packageTab[stringIndex])));
			if (typeList.get(i).compareTo("String") == 0)
				p.addMetric(new NominaleMetric<String>((NominaleMetricDescriptor<String>) (metricDescList.get(i)),
						packageTab[stringIndex]));
			if (typeList.get(i).compareTo("Date") == 0) {
				try {
					p.addMetric(new DateMetric((IntervaleMetricDescriptor<Date>) (metricDescList.get(i)),
							new Date(Long.parseLong(packageTab[stringIndex]))));
				} catch (Exception e) {
					System.out.println(i + " " +line);
					e.printStackTrace();;

				}
			}
		}
		sys.addPackage(p);
		return 1; // 1 line read
	}
	
	static int SKIP_NAME_AND_LIST = 9;
	static boolean CLASSES_HAVE_URL = false;
	
	private static int processHeadClass(String line, int linenumber)
	{
		currentState = ELEMENT_TYPE.CLASS;
		metricDescList = new ArrayList<MetricDescriptor>();
		typeList = new ArrayList<String>();
		metricList = new ArrayList<String>();
		metricMin = new ArrayList<String>();
		metricMax = new ArrayList<String>();
		String[] classTab = line.split(";");
		
		
		for (int i = 0; i < classTab.length; i++) {
			if(classTab[i].compareToIgnoreCase("url") == 0) 
				CLASSES_HAVE_URL = true;
		}
		
		SKIP_NAME_AND_LIST = CLASSES_HAVE_URL?9:8;
		
		// Skipping names and lists (8)
		for (int stringIndex = SKIP_NAME_AND_LIST ; stringIndex < classTab.length; stringIndex++) {
			metricList.add(classTab[stringIndex]);
		}
		try {
			line = input.readLine();
		} catch (Exception e) {
			throw new IllegalArgumentException("TYPE defnition line incorrect.\n"+e.getMessage());
		}
		classTab = line.split(";");
		for (int stringIndex = SKIP_NAME_AND_LIST ; stringIndex < classTab.length; stringIndex++) {
			typeList.add(classTab[stringIndex]);
		}
		try {
			line = input.readLine();
		} catch (Exception e) {
			throw new IllegalArgumentException("MIN defnition line incorrect.\n"+e.getMessage());
		}
		classTab = line.split(";");
		for (int stringIndex = SKIP_NAME_AND_LIST ; stringIndex < classTab.length; stringIndex++) {
			metricMin.add(classTab[stringIndex]);
		}
		try {
			line = input.readLine();
		} catch (Exception e) {
			throw new IllegalArgumentException("MAX defnition line incorrect.\n"+e.getMessage());
		}
		classTab = line.split(";");
		for (int stringIndex = SKIP_NAME_AND_LIST ; stringIndex < classTab.length; stringIndex++) {
			metricMax.add(classTab[stringIndex]);
		}
 		
		for (int i = 0; i < metricList.size(); i++) {

			Set<String> values;
			switch (typeList.get(i)) {
			case "int":
				metricDescList.add(new IntervaleMetricDescriptor<Integer>(metricList.get(i), Integer.valueOf(metricMin.get(i)),
						Integer.valueOf(metricMax.get(i))));
				break;
			case "double":
				metricDescList.add(
						new IntervaleMetricDescriptor<Double>(metricList.get(i), Double.valueOf(metricMin.get(i)), Double.valueOf(metricMax.get(i))));
				break;
			case "string":
				values = new HashSet<String>();
				if (metricMax.get(i).compareTo("") != 0) {
					String[] splitted = metricMax.get(i).split(":");

					for (int j = 0; j < splitted.length; j++) {
						values.add(splitted[j]);
					}
				}
				metricDescList.add(new NominaleMetricDescriptor<String>(metricList.get(i), values));
				break;
			case "date":
				try {
					metricDescList.add(new IntervaleMetricDescriptor<Date>(metricList.get(i), new Date(Long.parseLong(metricMin.get(i))),
							new Date(Long.parseLong(metricMax.get(i)))));
				} catch (Exception e) {
					System.out.println(i + " " +Arrays.toString(classTab));
					e.printStackTrace();;
				}
				break;
			default:
				throw new IllegalArgumentException("'" + typeList.get(i) + "' is not a valid metric type");
			}
		}
		return 4; // 4 lines read
	}
	
	private static int processClass(String line, int linenumber) {
		// System.out.println("CsvParser2.processClass "+linenumber+" : "+line);

		Element e;
		String[] classTab = line.split("[;]");

		int stringIndex = 1;
		String elementName = classTab[stringIndex]; // Name

		stringIndex++;
		String typeStr = classTab[stringIndex]; // Type
		e = Element.buildElement(elementName, typeStr);

		stringIndex++;
		if (CLASSES_HAVE_URL) {
			// TODO the case of URL, hard coded.
			String url = classTab[stringIndex]; // URL
			e.setUrl(url);
			stringIndex++;
		}
		e.setPackage(classTab[stringIndex]); // Package

		stringIndex++;
		String superClass = classTab[stringIndex]; // Super
		if (superClass.compareTo("null") != 0) {
			e.setParent(Entity.entities.get(superClass));
		}

		stringIndex++;
		String interfaceStr = classTab[stringIndex]; // Interfaces
		parseInterfaces(e, interfaceStr);

		stringIndex++;
		String targetStr = classTab[stringIndex]; // Targets
		parseTargets(e, targetStr);

		stringIndex++;
		String commitStr = classTab[stringIndex]; // Commits
		parseCommits(e, commitStr);

		stringIndex++; //Metrics 
		e.setMetrics(parseMetrics(classTab));
		sys.addElement(e);
		return 1; // 1 line read
	}

	@SuppressWarnings("unchecked")
	private static Collection<Metric<?>> parseMetrics(String[] classTab) {
		ArrayList<Metric<?>> res = new ArrayList<>();
		for (int i = SKIP_NAME_AND_LIST; i < classTab.length; i++) {
			
			int metricI = i - SKIP_NAME_AND_LIST;
			switch(typeList.get(metricI)) {
			case "int":
				res.add(
					new IntegralNumberMetric<Integer>((IntervaleMetricDescriptor<Integer>) (metricDescList.get(metricI)),
						Integer.valueOf(classTab[i])));
				break;
			case "double":
				res.add(new DecimalNumberMetric<Double>((IntervaleMetricDescriptor<Double>) (metricDescList.get(metricI)),
						Double.valueOf(classTab[i])));
				break;
			case "string":
				res.add(new NominaleMetric<String>((NominaleMetricDescriptor<String>) (metricDescList.get(metricI)),
						classTab[i]));
				break;
			case "date":
				try {
					res.add(new DateMetric((IntervaleMetricDescriptor<Date>) (metricDescList.get(metricI)),
							new Date(Long.parseLong(classTab[i]))));
				} catch (Exception ex) {
					System.out.println(i + " " +Arrays.toString(classTab));
					ex.printStackTrace();;
					
				}
				break;
			default:
				throw new IllegalArgumentException("'"+ typeList.get(metricI) + "' is not a valid metric type");
			}
			
		}
		return res;
	}

	private static void parseInterfaces(Element e, String interfaceStr) {
		if (interfaceStr.compareTo("") != 0) {
			String[] interTab = interfaceStr.split("[,]");
			if (e instanceof ClassDef) {
				for (int interIndex = 0; interIndex < interTab.length; interIndex++) {
					((ClassDef) e).addInterface(interTab[interIndex]);
				}
			}
		}
	}

	private static void parseTargets(Element e, String targetStr) {
		if (targetStr.compareTo("") != 0) {
			String[] targetTab = targetStr.split("[,]");
			for (int targetIndex = 0; targetIndex < targetTab.length; targetIndex++) {
				e.addTarget(targetTab[targetIndex]);
			}
		}
	}

	private static void parseCommits(Element e, String commitStr) {
		if (commitStr.compareTo("") != 0) {
			String[] commitTab = commitStr.split("[:]");
			String commitText = "";
			String[] commitProp;
			for (int i = 0; i < commitTab.length; i++) {
				commitText = commitTab[i];
				commitProp = commitText.split("[,]");
				try {
					e.addCommit(new Commit(Long.parseLong(commitProp[0])/* ,commitProp[1].charAt(0) */, commitProp[1],
							new Date(Long.parseLong(commitProp[2])), commitProp[3]));
					sys.addAuthor(commitProp[1]);
				} catch (Exception ex) {
					System.out.println(ex);
				}
			}
		}
	}

	private static int processHeadMethod(String line, int linenumber) {
		currentState = ELEMENT_TYPE.METHOD;
		typeList.clear();
		metricDescList = new ArrayList<MetricDescriptor>();
		metricList = new ArrayList<String>();
		metricMin = new ArrayList<String>();
		metricMax = new ArrayList<String>();
		String[] methodTab = line.split(";");
		// Skipping names and lists (5)
		for (int stringIndex = 5; stringIndex < methodTab.length; stringIndex++) {
			metricList.add(methodTab[stringIndex]);
		}
		try {
			line = input.readLine();
		} catch (Exception e) {
			System.out.println(e);
		}
		methodTab = line.split(";");
		for (int stringIndex = 5; stringIndex < methodTab.length; stringIndex++) {
			typeList.add(methodTab[stringIndex]);
		}
		try {
			line = input.readLine();
		} catch (Exception e) {
			System.out.println(e);
		}
		methodTab = line.split(";");
		for (int stringIndex = 5; stringIndex < methodTab.length; stringIndex++) {
			metricMin.add(methodTab[stringIndex]);
		}
		try {
			line = input.readLine();
		} catch (Exception e) {
			System.out.println(e);
		}
		methodTab = line.split(";");
		for (int stringIndex = 5; stringIndex < methodTab.length; stringIndex++) {
			metricMax.add(methodTab[stringIndex]);
		}

		for (int i = 0; i < metricList.size(); i++) {
			Set<String> values;
			if (typeList.get(i).compareTo("double") == 0)
				metricDescList.add(new IntervaleMetricDescriptor<Double>(metricList.get(i),
						Double.valueOf(metricMin.get(i)), Double.valueOf(metricMax.get(i))));
			if (typeList.get(i).compareTo("string") == 0) {
				values = new HashSet<String>();
				if (metricMax.get(i).compareTo("") != 0) {
					String[] splitted = metricMax.get(i).split(":");

					for (int j = 0; j < splitted.length; j++) {
						values.add(splitted[j]);
					}
				}
				metricDescList.add(new NominaleMetricDescriptor<String>(metricList.get(i), values));
			}
			if (typeList.get(i).compareTo("date") == 0)
				try {
					metricDescList.add(new IntervaleMetricDescriptor<Date>(metricList.get(i),
							new Date(Long.parseLong(metricMin.get(i))), new Date(Long.parseLong(metricMax.get(i)))));
				} catch (Exception e) {
					System.out.println(e);
				}
		}
		return 4;
	}

	@SuppressWarnings("unchecked")
	private static int processMethod(String line, int linenumber) {
		Method m;
		String[] methodTab = line.split(";");
		int stringIndex = 0;
		
		stringIndex++;
		m = new Method(methodTab[stringIndex]);
		
		stringIndex++;
		m.setReturnType(methodTab[stringIndex]);
		
		stringIndex++;
		m.setSignature(methodTab[stringIndex].replaceAll(":", ";"));
		
		stringIndex++;
		String targetStr = methodTab[stringIndex];
		if (targetStr.compareTo("") != 0) {
			String[] targetTab = targetStr.split(":");
			for (int targetIndex = 0; targetIndex < targetTab.length; targetIndex++) {
				m.addTarget(targetTab[targetIndex]);
			}
		}
		
		stringIndex++;
		for (int i = 0; stringIndex < methodTab.length; i++, stringIndex++) {
			if (typeList.get(i).compareTo("double") == 0)
				m.addMetric(new DecimalNumberMetric<Double>((IntervaleMetricDescriptor<Double>) (metricDescList.get(i)),
						Double.valueOf(methodTab[stringIndex])));
			if (typeList.get(i).compareTo("string") == 0)
				m.addMetric(new NominaleMetric<String>((NominaleMetricDescriptor<String>) (metricDescList.get(i)),
						methodTab[stringIndex]));
			if (typeList.get(i).compareTo("date") == 0) {
				try {
					m.addMetric(new DateMetric((IntervaleMetricDescriptor<Date>) (metricDescList.get(i)),
							new Date(Long.parseLong(methodTab[stringIndex]))));
				} catch (Exception ex) {
					System.out.println(ex);
				}
			}
		}
		sys.addMethod(m);
		return 1; // 1 line read
	}
}

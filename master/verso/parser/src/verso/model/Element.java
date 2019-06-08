package verso.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import verso.model.history.Commit;
import verso.model.metric.DateMetric;
import verso.model.metric.DecimalNumberMetric;
import verso.model.metric.Metric;
import verso.model.metric.NominaleMetric;

public abstract class Element extends Entity {

	private static final String NULL_PARENT_TEXT = "<No_parent>";
	private Hashtable<String, Method> childMethods;
	private Hashtable<String, Attribute> childAttributes;
	private Hashtable<String, Element> targets;
	private HashSet<String> textTargets;
	private String packageStr;
	private Hashtable<String, Element> children;
	private HashSet<String> textChildren;
	private List<Comment> comments;
	private List<MethodInvocationText> methodCalls;
	private List<ConditionStatement> conditions;
	private List<Line> lines;
	private HashSet<Concept> concepts;//Concepts used for IR filter

	private File resourceLoc = null;

	// SVN stuff
	private Date dateIntro = null;
	private Date lastUpdate = new Date(0);
	private List<Commit> commits = new ArrayList<Commit>();
	private Map<String, Integer> authors = new HashMap<String, Integer>();
	private String url;
	
	private Entity parent;

	public Element(String name) {
		super(name);
		try {
			parent = entities.get(name.substring(0, name.lastIndexOf(".")));
		}
		catch(IndexOutOfBoundsException e) {
			parent = entities.get("root");
		}
		childMethods = new Hashtable<String, Method>();
		childAttributes = new Hashtable<String, Attribute>();
		targets = new Hashtable<String, Element>();
		textTargets = new HashSet<String>();
		children = new Hashtable<String, Element>();
		textChildren = new HashSet<String>();
		comments = new ArrayList<Comment>();
		methodCalls = new ArrayList<MethodInvocationText>();
		conditions = new ArrayList<ConditionStatement>();
		lines = new ArrayList<Line>();
		concepts = new HashSet<Concept>();
	}
	
	public static Element buildElement(String elementName, String typeStr) {
		Element e = null;
		switch (typeStr) {
		case "Class":
		case "CLASS":
		case "class":
			e = new ClassDef(elementName);
			break;
		case "Interface":
		case "INTERFACE":
		case "interface":
			e = new InterfaceDef(elementName);
			break;
		case "Library":
		case "LIBRARY":
		case "library":
			e = new LibDef(elementName);
			break;

		default:
			throw new IllegalArgumentException("'" + typeStr + "' is not a valid Type.");
		}

		return e;
	}

	public void setFileLocation(File icu) {
		this.resourceLoc = icu;
	}

	public File getFileLocation() {
		return this.resourceLoc;
	}

	public void addComment(Comment comment) {
		comments.add(comment);
	}

	public List<Comment> getComments() {
		return this.comments;
	}

	public void addMethodCall(MethodInvocationText metInvText) {
		methodCalls.add(metInvText);
	}

	public List<MethodInvocationText> getMethodCalls() {
		return this.methodCalls;
	}

	public void addCondition(ConditionStatement cs) {
		conditions.add(cs);
	}

	public List<ConditionStatement> getConditions() {
		return this.conditions;
	}

	public void addMethod(Method m) {
		this.childMethods.put(m.getName(), m);
	}

	public void addAttribute(Attribute a) {
		this.childAttributes.put(a.getName(), a);
	}

	public void addTarget(Element target) {
		this.targets.put(target.getName(), target);
	}

	public void addTarget(String target) {
		this.textTargets.add(target);
	}

	public void addCommit(Commit c) {
		this.commits.add(c);
		if (authors.containsKey(c.getAuthor())) {
			authors.put(c.getAuthor(), authors.get(c.getAuthor()) + 1);
		} else {
			authors.put(c.getAuthor(), 1);
		}

		if (this.commits.size() == 1) {
			dateIntro = c.getDate();
			if (c.getDate().compareTo(this.lastUpdate) > 0) {
				this.lastUpdate = c.getDate();
			}
		} else {
			if (c.getDate().compareTo(this.lastUpdate) > 0) {
				this.lastUpdate = c.getDate();
			}
		}
	}
	
	public void addConcept(Concept concept) {
		concepts.add(concept);
	}
	
	public HashSet<Concept> getConcepts() {
		return concepts;
	}

	public void setTargets(Set<String> targets) {
		for (String s : targets) {
			this.addTarget(s);
		}
	}

	public void setLines(List<Line> lines) {
		this.lines = lines;
	}

	public List<Line> getLines() {
		return this.lines;
	}

	public Line getLine(int i) {
		return this.lines.get(i);
	}

	public Set<String> getTargets() {
		return this.textTargets;
	}

	public Set<String> getAllPackageTargets() {
		Set<String> toReturn = new HashSet<String>();
		for (String s : this.getTargets()) {
			String[] splited = s.split("[.]");
			String toAdd = "";
			for (int i = 0; i < splited.length - 1; i++) {
				toAdd += splited[i];
				toReturn.add(toAdd);
				toAdd += ".";
			}
		}
		return toReturn;
	}

	public Entity getParent() {
		return parent;
	}
	public void setParent(Entity parent) {
		this.parent = parent;
	}
	public String getParentText() {
		if(parent != null)
			return parent.getName();
		return NULL_PARENT_TEXT;
	}

	public void setPackage(String p) {
		this.packageStr = p;
	}

	public String getPackage() {
		return this.packageStr;
	}

	public void addChild(Element child) {
		this.children.put(child.getName(), child);
	}

	public void addChild(String child) {
		this.textChildren.add(child);
	}

	public Collection<Method> getMethods() {
		return childMethods.values();
	}

	public Collection<Attribute> getAttributes() {
		return childAttributes.values();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String toStringSpecial(int indent) {
		String toReturn = "";
		String space = "";
		for (int i = 0; i < indent; i++) {
			space += "  ";
		}
		toReturn += space + "-" + this.getName() + "\n";
		for (@SuppressWarnings("rawtypes")Metric m : this.getMetrics()) {
			toReturn += space + "  " + m.getName() + " : " + m.getValue() + "\n";
		}
		for (Method m : this.childMethods.values()) {
			toReturn += m.toStringSpecial(indent + 1);
		}
		return toReturn;
	}

	public abstract String getInterfacesString();

	public abstract void setInterfaces(List<String> liste);

	public String getTargetString() {
		String toReturn = "";

		for (String s : this.textTargets) {
			toReturn += s + ",";
		}
		if (toReturn.length() > 1)
			toReturn = toReturn.substring(0, toReturn.length() - 1);
		return toReturn;
	}

	public List<Commit> getCommits() {
		return this.commits;
	}

	public String getCommitString() {
		String toReturn = "";
		for (Commit c : commits) {
			toReturn += c.getName() /* + "," + c.getAction() */ + "," + c.getAuthor() + "," + c.getTextualDate() + ","
					+ c.getComment().replaceAll("[;]", "*").replaceAll("[:]", "*").replaceAll("[,]", "*") + ":";
		}
		if (toReturn.length() > 1)
			toReturn = toReturn.substring(0, toReturn.length() - 1);
		return toReturn;
	}

	public Set<String> getAuthors() {
		return this.authors.keySet();
	}

	public void computeSVNMetrics(SystemDef sys) {
		this.addMetric(
				new DecimalNumberMetric<Double>(SystemDef.NUMBEROFAUTHORSDESCRIPTOR, (double) getNumberOfAuthors()));
		this.addMetric(
				new DecimalNumberMetric<Double>(SystemDef.NUMBEROFCOMMITSDESCRIPTOR, (double) getNumberOfCommits()));
		this.addMetric(new DateMetric(SystemDef.LASTUPDATEDESCRIPTOR, getLastUpdate()));
		this.addMetric(new DateMetric(SystemDef.DATEINTRODESCRIPTOR, getDateIntro()));
		this.addMetric(new NominaleMetric<String>(sys.getMainAuthorDescriptor(), computeMainAuthor()));
	}

	private int getNumberOfAuthors() {
		return this.authors.size();
	}

	private int getNumberOfCommits() {
		return this.commits.size();
	}

	private Date getLastUpdate() {
		return this.lastUpdate;
	}

	public Date getDateIntro() {
		if (this.dateIntro != null)
			return this.dateIntro;
		else {
			Date mindate = new Date(System.currentTimeMillis());
			for (Commit c : commits) {
				if (c.getDate().compareTo(mindate) < 0) {
					mindate = c.getDate();
				}
			}
			return mindate;
		}
	}

	public String computeMainAuthor() {
		String maxauthor = "";
		int maxintauthor = 0;
		for (String author : this.authors.keySet()) {
			if (authors.get(author) > maxintauthor) {
				maxintauthor = authors.get(author);
				maxauthor = author;
			}
		}
		return maxauthor;
	}

	public Object accept(Visitor v) {
		return v.visit(this);
	}

	public Commit getLastCommit() {
		if (this.commits.size() >= 1)
			return this.commits.get(commits.size() - 1);
		else
			return null;
	}

	public void computeBugMetrics(SystemDef sys) {
		this.addMetric(new DecimalNumberMetric<Double>(SystemDef.NUMBEROFOPENBUG, (double) getNumberOfOpenBug()));
		this.addMetric(new DecimalNumberMetric<Double>(SystemDef.TOTALNUMBEROFBUG, (double) getTotalNumberofBug()));
		this.addMetric(new DecimalNumberMetric<Long>(SystemDef.AVERAGETIMEABUGISOPEN, (long) tempsMoyenBug()));
		this.addMetric(new DateMetric(SystemDef.LASTCLOSEBUG, dateFermeture()));
		this.addMetric(new DateMetric(SystemDef.DATEINTRODESCRIPTOR, dateIntro()));
		this.addMetric(new NominaleMetric<String>(sys.getMainBugProgrammeurDescriptor(), getMainBugProgrammer()));
		this.addMetric(new NominaleMetric<String>(sys.getBugAuthorDescriptor(), getMainAuthor()));
	}

	private String getMainBugProgrammer() {
		String auth = "";
		int authFreq = 0;
		HashMap<String, Integer> authors = new HashMap<String, Integer>();
		for (Bug b : this.bugs) {
			if (authors.containsKey(b.getMainProg())) {
				authors.put(b.getMainProg(), authors.get(b.getMainProg()) + 1);
			} else {
				authors.put(b.getMainProg(), 1);
			}
		}
		for (String author : authors.keySet()) {
			if (authors.get(author) > authFreq) {
				auth = author;
				authFreq = authors.get(author);
			}
		}
		return auth;
	}

	private String getMainAuthor() {
		String auth = "";
		int authFreq = 0;
		HashMap<String, Integer> authors = new HashMap<String, Integer>();
		for (Bug b : this.bugs) {
			if (authors.containsKey(b.getFirstAuthor())) {
				authors.put(b.getFirstAuthor(), authors.get(b.getFirstAuthor()) + 1);
			} else {
				authors.put(b.getFirstAuthor(), 1);
			}
		}
		for (String author : authors.keySet()) {
			if (authors.get(author) > authFreq) {
				auth = author;
				authFreq = authors.get(author);
			}
		}
		return auth;
	}

	private double tempsMoyenBug() {
		double tempMoy = 0.0;
		for (Bug b : this.bugs) {
			tempMoy += b.getLengthMili() / this.bugs.size();
		}
		return tempMoy;
	}

	private int getTotalNumberofBug() {
		return this.bugs.size();
	}

	private int getNumberOfOpenBug() {
		int nbBugOuvert = 0;
		for (Bug b : this.bugs) {
			if (b.isOpen())
				nbBugOuvert++;
		}
		return nbBugOuvert;
	}

	private Date dateIntro() {
		Date d = new Date(System.currentTimeMillis());
		for (Bug b : this.bugs) {
			if (d.getTime() > b.getDateIntro().getTime()) {
				d = b.getDateIntro();
			}
		}
		return d;
	}

	private Date dateFermeture() {
		Date d = SystemDef.getDate1990();
		for (Bug b : this.bugs) {
			if (b.isOpen()) {
				d = new Date(System.currentTimeMillis());
			}
			if (d.getTime() < b.getClosingDate().getTime()) {
				d = b.getClosingDate();
			}
		}
		return d;
	}
}

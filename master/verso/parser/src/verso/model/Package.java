package verso.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import verso.model.history.Commit;
import verso.model.metric.DateMetric;
import verso.model.metric.DecimalNumberMetric;
import verso.model.metric.Metric;
import verso.model.metric.NominaleMetric;
import verso.model.metric.paquetage.PackageVisitor;

public class Package extends Entity{
	
	Hashtable<String,Element> elemlist; 
	Hashtable<String,Package> packagelist;
	Hashtable<String,Element> allClasses;
	
	boolean dirtyMetricQual = false;
	boolean dirtyMetricSVN = false;
	
	public Package(String name) {
		super(name);
		elemlist = new Hashtable<String, Element>();
		packagelist = new Hashtable<String, Package>();
	}

	public boolean containsPackage(String pakName) {
		return packagelist.containsKey(pakName);
	}

	public void addPackage(Package p) {
		this.packagelist.put(p.getName(), p);
	}

	public Package getPackage(String packageName) {
		return this.packagelist.get(packageName);
	}
	
	public void addElement(Element e) {
		this.elemlist.put(e.getName(), e);
	}

	public boolean removeElement(String e) {
		return this.elemlist.remove(e) != null;
	}

	public boolean removePackage(String p) {
		return this.packagelist.remove(p) != null;
	}

	public Collection<Package> getSubPackages() {
		return this.packagelist.values();
	}

	public Collection<Element> getSubElements() {
		return this.elemlist.values();
	}
	
	public int getNumberOfClasses() {
		int number = 0;
		for (Package p : this.packagelist.values()) {
			number += p.getNumberOfClasses();
		}
		return number + this.elemlist.size();
	}

	public Set<String> getAllPackageTargets() {
		Set<String> toReturn = new HashSet<String>();
		for (String s : this.getAllTargets()) {
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

	public Set<String> getAllTargets() {
		Set<String> toReturn = new HashSet<String>();
		for (Package p : this.packagelist.values()) {
			toReturn.addAll(p.getAllTargets());
		}
		for (Element e : this.elemlist.values()) {
			toReturn.addAll(e.getTargets());
		}
		return toReturn;
	}

	public List<Commit> getAllCommits() {
		List<Commit> toReturn = new LinkedList<Commit>();
		for (Package p : this.packagelist.values()) {
			toReturn.addAll(p.getAllCommits());
		}
		for (Element e : this.elemlist.values()) {
			toReturn.addAll(e.getCommits());
		}
		return toReturn;
	}

	public Set<String> getAllAuthors() {
		Set<String> toReturn = new HashSet<String>();
		for (Package p : this.packagelist.values()) {
			toReturn.addAll(p.getAllAuthors());
		}
		for (Element e : this.elemlist.values()) {
			toReturn.addAll(e.getAuthors());
		}
		return toReturn;
	}

	public List<Element> getDescendantElements() {
		List<Element> toReturn = new ArrayList<Element>();
		toReturn.addAll(this.elemlist.values());
		for (Package p : this.packagelist.values()) {
			toReturn.addAll(p.getDescendantElements());
		}
		return toReturn;
	}

	public List<Entity> getDescendantElementsAndPackage() {
		List<Entity> toReturn = new ArrayList<Entity>();
		toReturn.add(this);
		toReturn.addAll(this.elemlist.values());
		for (Package p : this.packagelist.values()) {
			toReturn.addAll(p.getDescendantElementsAndPackage());
		}
		return toReturn;
	}
	
	public Package introduceOrCopy(Package p) {
		String remainning = p.getName().substring(this.getName().length() + 1);
		if (remainning.indexOf('.') == -1) {
			if (!this.packagelist.containsKey(p.getName())) {
				this.packagelist.put(p.getName(), p);
				return p;
			} else {
				this.packagelist.get(p.getName()).setMetrics(p.getMetrics());
				return this.packagelist.get(p.getName());
			}
		} else {
			String nextFragment = remainning.substring(0, remainning.indexOf('.'));
			if (!this.packagelist.containsKey(this.getName() + "." + nextFragment)) {
				this.addPackage(new Package(this.getName() + "." + nextFragment));
			}
			return this.packagelist.get(this.getName() + "." + nextFragment).introduceOrCopy(p);
		}
	}
	
	public boolean isDirtyMetricQual() {
		return this.dirtyMetricQual;
	}

	public void setDirtyMetricQual() {
		this.dirtyMetricQual = true;
	}

	public void unsetDirtyMetricQual() {
		this.dirtyMetricQual = false;
	}

	public boolean isDirtyMetricSVN() {
		return this.dirtyMetricSVN;
	}

	public void setDirtyMetricSVN() {
		this.dirtyMetricSVN = true;
	}

	public void unsetDirtyMetricSVN() {
		this.dirtyMetricSVN = false;
	}
	
	public void computeBugMetrics(SystemDef sys) {
		this.addMetric(new DecimalNumberMetric<Double>(SystemDef.PACKAGENUMBEROFOPENBUG, (double) getPackageNumberOfOpenBug()));
		this.addMetric(new DecimalNumberMetric<Double>(SystemDef.PACKAGETOTALNUMBEROFBUG, (double) getPackageTotalNumberofBug()));
		this.addMetric(new DecimalNumberMetric<Long>(SystemDef.PACKAGEAVERAGETIMEABUGISOPEN, (long) packageTempsMoyenBug()));
		this.addMetric(new DateMetric(SystemDef.PACKAGELASTCLOSEBUG, packageDateFermeture()));
		this.addMetric(new DateMetric(SystemDef.PACKAGEDATEINTRODESCRIPTOR, packageDateIntro()));
		this.addMetric(new NominaleMetric<String>(sys.getPackageMainBugProgrammeurDescriptor(), getPackageMainBugProgrammer()));
		this.addMetric(new NominaleMetric<String>(sys.getPackageBugAuthorDescriptor(), getPackageMainAuthor()));
	}
	
	private String getPackageMainAuthor() {
		String auth = "";
		int authFreq = 0;
		HashMap<String, Integer> authors = new HashMap<String, Integer>();
		List<Bug> bugListes = new ArrayList<Bug>();
		for (Entity enti : this.getDescendantElementsAndPackage()) {
			bugListes.addAll(enti.getBugs());
		}
		for (Bug b : bugListes) {
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

	private String getPackageMainBugProgrammer() {
		String auth = "";
		int authFreq = 0;
		HashMap<String, Integer> authors = new HashMap<String, Integer>();
		List<Bug> bugListes = new ArrayList<Bug>();
		for (Entity enti : this.getDescendantElementsAndPackage()) {
			bugListes.addAll(enti.getBugs());
		}
		for (Bug b : bugListes) {
			if (b.getMainProg() != "") {
				if (authors.containsKey(b.getMainProg())) {
					authors.put(b.getMainProg(), authors.get(b.getMainProg()) + 1);
				} else {
					authors.put(b.getMainProg(), 1);
				}
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

	private Date packageDateIntro() {
		Date d = new Date(System.currentTimeMillis());
		List<Bug> bugListes = new ArrayList<Bug>();
		for (Entity enti : this.getDescendantElementsAndPackage()) {
			bugListes.addAll(enti.getBugs());
		}
		for (Bug b : bugListes) {
			if (d.getTime() > b.getDateIntro().getTime()) {
				d = b.getDateIntro();
			}
		}
		return d;
	}

	private Date packageDateFermeture() {
		Date d = SystemDef.getDate1990();
		List<Bug> bugListes = new ArrayList<Bug>();
		for (Entity enti : this.getDescendantElementsAndPackage()) {
			bugListes.addAll(enti.getBugs());
		}
		for (Bug b : bugListes) {
			if (b.isOpen()) {
				d = new Date(System.currentTimeMillis());
			}
			if (d.getTime() < b.getClosingDate().getTime()) {
				d = b.getClosingDate();
			}
		}
		return d;
	}

	private double packageTempsMoyenBug() {
		double tempMoy = 0.0;
		List<Bug> bugListes = new ArrayList<Bug>();
		for (Entity enti : this.getDescendantElementsAndPackage()) {
			bugListes.addAll(enti.getBugs());
		}
		if (this.bugs.size() != 0) {
			for (Bug b : bugListes) {
				tempMoy += b.getLengthMili() / this.bugs.size();
			}
		}
		return tempMoy;
	}

	private double getPackageTotalNumberofBug() {
		List<Bug> bugListes = new ArrayList<Bug>();
		for (Entity enti : this.getDescendantElementsAndPackage()) {
			bugListes.addAll(enti.getBugs());
		}
		return bugListes.size();
	}

	private double getPackageNumberOfOpenBug() {
		List<Bug> bugListes = new ArrayList<Bug>();
		for (Entity enti : this.getDescendantElementsAndPackage()) {
			bugListes.addAll(enti.getBugs());
		}
		int nbBugOuvert = 0;
		for (Bug b : bugListes) {
			if (b.isOpen())
				nbBugOuvert++;
		}
		return nbBugOuvert;
	}

	public String toStringSpecial(int indent) {
		String toReturn = "";
		String space = "";
		for (int i = 0; i < indent; i++) 
			space += "  ";
		
		toReturn += space + this.getName() + "\n";

		for (Metric m : this.getMetrics()) {
			toReturn += space + "  " + m.getName() + " : " + m.getValue() + "\n";
		}

		for (Element e : this.elemlist.values()) {
			toReturn += e.toStringSpecial(indent + 1);
		}
		for (Package p : this.packagelist.values()) {
			toReturn += p.toStringSpecial(indent + 1);
		}
		return toReturn;
	}
	
	public Object accept(Visitor v) {
		return v.visit(this);
	}

	public void accept(PackageVisitor v)
	{
		v.visit(this);
	}
}

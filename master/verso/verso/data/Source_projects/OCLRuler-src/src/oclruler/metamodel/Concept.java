package oclruler.metamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EReference;

import oclruler.metamodel.StructuralFeature.SlotMultiplicity;
import oclruler.rule.MMMatch;
import oclruler.rule.PatternFactory.PatternType;
import oclruler.rule.patterns.Pattern;
import oclruler.utils.ToolBox;

public class Concept extends MMElement {
	
	EClassifier classifier;

	private ArrayList<Reference> references;
	private ArrayList<Attribute> attributes;	

	private HashMap<String, EReference> ereferences;
	private HashMap<String, EAttribute> eattributes;	

	private ArrayList<Concept> supers;
	private ArrayList<Concept> descendants;
	
	/**
	 * EInt, EString etc. don't have package...
	 */
	private String packageName;
	
	
	/**
	 * WARNING ! == getName for now.<br/>
	 * Might need to consider the package name someday...
	 */
	@Override
	public String getFullName() {
		return getName();
	}
	
	static ArrayList<Concept> instances = new ArrayList<>();
	public static ArrayList<Concept> getInstances(){
		return instances;
	}
	
	public Concept(String name, EClassifier ec) {
		super(name);
		this.classifier = ec;
		if(ec != null)
			packageName = ec.getEPackage().getName();
		references = new ArrayList<>(5);
		attributes = new ArrayList<>(5);
		ereferences = new HashMap<>();
		eattributes = new HashMap<>();
		paths = new PathMap();
		instances.add(this);
	}
	
	public Concept(String name) {
		this(name, null);
	}
	
	private PathMap paths;
	
	/**
	 * TESTING PURPOSE ONLY
	 * @return Paths already explored.
	 */
	public PathMap getPaths() {
		return paths;
	}
	
	
	/**
	 * Return, if any, existing paths (i.e., chain of references) from this concept to the concept passed in parameter.<br/>
	 * Maximum depth is <code>MAX_PATHS_DEPTH</code>.
	 * @param end Concept to reach.
	 * @return
	 */
	public ArrayList<Path> getPaths(Concept end){
		return getPaths(end, Path.MAX_PATHS_DEPTH);
	}
	
	/**
	 * Return, if any, existing paths (i.e., chain of references) from this concept to the concept passed in parameter.
	 * @param end Concept to reach.
	 * @param depth Maximum length of the path (MAX = <code>MAX_PATHS_DEPTH</code>)
	 * @return existing Paths (i.e., chain of references) from this concept to the concept passed in parameter.
	 */
	public ArrayList<Path> getPaths( Concept end, int depth){
		return getPaths(end, depth, false);
	}
	
	
	/**
	 * Return, if any, existing paths (i.e., chain of references) from this concept to the concept passed in parameter.
	 * @param end Concept to reach.
	 * @param depth Maximum length of the path (MAX = <code>MAX_PATHS_DEPTH</code>)
	 * @param force computing (if true, buffer updates)
	 * @return existing Paths (i.e., chain of references) from this concept to the concept passed in parameter.
	 */
	public ArrayList<Path> getPaths( Concept end, int depth, boolean force){
		ArrayList<Path> res = this.paths.get(this, end);
		if(res != null && !force) {
			return this.paths.get(this, end);
		}
		
		if (depth == 1) {
			res = new ArrayList<Path>();
			for (Reference r : references) {
				if (r.getType().equalNames(end))
					res.add(new Path(this, r));
			}
		} else if (depth == 2) {
			res = new ArrayList<Path>();
			for (Reference r0 : references) {
				Concept step1 = r0.getType();
				for (Reference r1 : step1.getReferences()) {
					if (r1.getType().equalNames(end))
						res.add(new Path(this, r0, r1));
				}
			}
		} else if (depth == 3) {
			res = new ArrayList<Path>();
			for (Reference r0 : references) {
				Concept step1 = r0.getType();
				for (Reference r1 : step1.getReferences()) {
					Concept step2 = r1.getType();
					for (Reference r2 : step2.getReferences()) {
						if (r2.getType().equalNames(end))
							res.add(new Path(this, r0, r1, r2));
					}
				}
			}
		} else if (depth == 4) {
			res = new ArrayList<Path>();
			for (Reference r0 : references) {
				Concept step1 = r0.getType();
				for (Reference r1 : step1.getReferences()) {
					Concept step2 = r1.getType();
					for (Reference r2 : step2.getReferences()) {
						Concept step3 = r2.getType();
						for (Reference r3 : step3.getReferences()) {
							if (r3.getType().equalNames(end))
								res.add(new Path(this, r0, r1, r2, r3));
						}
					}
				}
			}
		} else {
			throw new IllegalArgumentException("Maximum depth is "+Path.MAX_PATHS_DEPTH+" : "+depth+" > "+Path.MAX_PATHS_DEPTH);
		}
		//Go recursive
		if(depth > 1)
			res.addAll(getPaths(end, depth-1, force));
		this.paths.putAll(res);
		return res;
	}
	
	/**
	 * 
	 * @return All paths of length <= 2; from this concept 
	 */
	public ArrayList<Path> getSimplePaths() {
		ArrayList<Path> res = new ArrayList<Path>();
		for (Path path : getFirstDegreePath()) 
			res.add(path);
		for (Path path : getSecondDegreePath()) 
			res.add(path);
		this.paths.putAll(res);
		return res;
	}
	/**
	 * 
	 * @return All paths of length == 1; from this concept 
	 */
	public ArrayList<Path> getFirstDegreePath() {
		ArrayList<Path> res = new ArrayList<Path>();
		for (Reference r : references) 
			res.add(new Path(this, r));
		return res;
	}
	
	/**
	 * 
	 * @return All paths of length == 2; from this concept
	 */
	public ArrayList<Path> getSecondDegreePath() {
		ArrayList<Path> res = new ArrayList<Path>();
		for (Reference r0 : references) {
			Concept step1 = r0.getType();
			for (Reference r1 : step1.getReferences()) {
				res.add(new Path(this, r0, r1));
			}
		}
		return res;
	}
	
	/**
	 * Combinatorial explosion !
	 * @return
	 */
	public ArrayList<Path> getThirdDegreePath() {
		ArrayList<Path> res = new ArrayList<Path>();
		for (Reference r0 : references) {
			Concept step1 = r0.getType();
			for (Reference r1 : step1.getReferences()) {
				Concept step2 = r1.getType();
				for (Reference r2 : step2.getReferences()) {
					res.add(new Path(this, r0, r1, r2));
				}
			}
		}
		return res;
	}
	
	
	public EClassifier getEClassifier() {
		return classifier;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public boolean putAttribute(String name, Concept source, Concept type, SlotMultiplicity multiplicity){
		Attribute att = new Attribute(name, source, type, multiplicity);
		return putAttribute(name, att);
	}
	public boolean putAttribute(String name, Attribute att){
		if(att.hasEStructuralFeature())
			eattributes.put(name, (EAttribute)att.geteStructuralFeature());
		return attributes.add(att);
	}
	
	public boolean putReference(String name, Concept source, Concept rclass, SlotMultiplicity multiplicity){
		Reference r = new Reference(name, source, rclass, multiplicity);
		return putReference(name, r);
	}
	public boolean putReference(String name, Reference r){
		if(r.hasEStructuralFeature())
			ereferences.put(name, (EReference)r.geteStructuralFeature());
		return references.add( r);
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Reference> getReferences() {
		return references;
	}
	
	public Reference getReference(String name) {
		for (Reference r : references) 
			if(r.getName().equals(name))
				return r;
		return null;
	}
	
	public Reference getRandomReference(){
		if(getReferences().isEmpty())
			return null;
		return ToolBox.getRandom(getReferences());
	}	

	public ArrayList<Attribute> getAttributes() {
		return attributes;
	}
	public Attribute getAttribute(String name) {
		for (Attribute a : attributes) 
			if(a.getName().equals(name))
				return a;
		return null;

	}
	public Attribute getRandomAttribute(){
		if(getAttributes().isEmpty())
			return null;
		return ToolBox.getRandom(getAttributes());
	}	
	
	
	ArrayList<StructuralFeature> sfs = null;
	public ArrayList<StructuralFeature> getStructuralFeatures(){
		if(sfs == null){
			sfs = new ArrayList<StructuralFeature>();
			sfs.addAll(references);
			sfs.addAll(attributes);
			if(sfs.size() != attributes.size()+references.size())
				throw new IllegalStateException("Name conflict :"+references+"/"+attributes);
			}
		return sfs;
	}
	public StructuralFeature getStructuralFeature(String name) {
		for (StructuralFeature a : getAllStructuralFeatures()) 
			if(a.getName().equals(name))
				return a;
		return null;

	}
	
	public StructuralFeature getRandomStructuralFeature(){
		return ToolBox.getRandom(getStructuralFeatures());
	}	
	
	public StructuralFeature getRandomStructuralFeature(SlotMultiplicity m){
		ArrayList<StructuralFeature> sfsT = Metamodel.get_T_StructuralFeatures(getStructuralFeatures(), null, m);
		return ToolBox.getRandom(sfsT);
	}	

	
	
	public boolean hasSuper(Concept c){
		return getAllSupers().contains(c);
	}
	
	/**
	 * Only used for metamodel jessification. (If used more often, refactoring required.)
	 * @return
	 */
	public HashSet<Concept> getSupers() {
		HashSet<Concept> res = new HashSet<Concept>();
		HashSet<String> ns = Metamodel.getDirectInheritageParentNames().get(name);
		if(ns!= null)
			for (String n : ns) 
				res.add(Metamodel.getConcept(n));
		return res;
	}
	
	public ArrayList<Concept> getAllSupers() {
		if(supers == null) {
			supers = new ArrayList<Concept>();
			HashSet<String> ns = Metamodel.getInheritageAllSuperNames().get(name);
			if(ns!= null)
				for (String n : ns) 
					supers.add(Metamodel.getConcept(n));
		}  
		return supers;
	}
	public ArrayList<Concept> getDescendants() {
		if(descendants == null) {
			descendants = new ArrayList<Concept>();
			Metamodel.getInheritageNames();
			for (String n : Metamodel.getInheritageNames().get(name)) {
				descendants.add(Metamodel.getConcept(n));
			}
		}  
		return descendants;
	}
	
	public boolean instanceOf(Concept cType){
		if(cType == null) return false;
		return cType.getName().equals(getName());
	}
	public boolean isTypeOf(Concept cType){
		if(cType == null) return false;
		return this.instanceOf(cType) || hasSuper(cType);
	}

	public boolean isNumeric() {
		return getName().equalsIgnoreCase("eint") ||
				getName().equalsIgnoreCase("einteger") ||
				getName().equalsIgnoreCase("edouble") ||
				getName().equalsIgnoreCase("efloat") ||
				getName().equalsIgnoreCase("elong") ||
				getName().equalsIgnoreCase("edouble") ;
	}

	public boolean isBoolean() {
		return getName().equalsIgnoreCase("eboolean") ;
	}

	@Override
	public String toString() {
		String res = "["+name+":";
//		for (String s : getStructuralFeatures().keySet()) {
//			res += s + "("+getStructuralFeatures().get(s).getClassName()+"),";
//		}
		return res.substring(0, res.length()-1)+"]";
	}

	@Override
	public String prettyPrint(String tab) {
		String res = tab+"["+name+":";
		for (StructuralFeature sf : getStructuralFeatures()) 
			res += sf.getName() + "("+sf.getTypeName()+"),";
		return res.substring(0, res.length()-1)+"]";
	}
	
	@Override
	public String simplePrint() {
		return "["+name+"]";
	}

	@Override
	public boolean equals(Object o) {
		if(o == null || (getClass() != o.getClass()))
			return false;
		return ((Concept)o).getName().equals(getName());
	}
	
	private Collection<StructuralFeature> allSfs = null;
	public Collection<StructuralFeature> getAllStructuralFeatures() {
		if(allSfs == null){
			allSfs = new ArrayList<>(5);
			for (Concept sup : getAllSupers()) {
				for (StructuralFeature structuralFeature : sup.getAllStructuralFeatures()) {
					if(!allSfs.contains(structuralFeature))
						allSfs.add(structuralFeature);
				}
			}
			for (StructuralFeature structuralFeature : getStructuralFeatures()) {
				if(!allSfs.contains(structuralFeature))
					allSfs.add(structuralFeature);
			}
		}
		return allSfs;
	}
	
	private ArrayList<Pattern> instantiablePatterns;
	public ArrayList<Pattern> getInstatiablePatterns() {
		if(  instantiablePatterns == null){
			instantiablePatterns = new ArrayList<>();
			for (PatternType pt : PatternType.enabledValues()) {
				ArrayList<MMMatch> list = Pattern.getMatches(pt.getInstanciationClass(), this);
				for (MMMatch m : list) {
					Pattern p = Pattern.newInstance(pt.getInstanciationClass(), m);
					instantiablePatterns.add(p);
				}
			}
		}
		return instantiablePatterns;
	}
	
	public void reinitializeInstantiablePatternList(){
		instantiablePatterns = null;
		getInstatiablePatterns();
	}

	public static void reinitializeInstanciablePatternList() {
		for (Concept concept : instances) {
			concept.reinitializeInstantiablePatternList();
		}
	}
}

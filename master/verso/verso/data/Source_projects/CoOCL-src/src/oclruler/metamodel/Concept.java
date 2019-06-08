package oclruler.metamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EReference;

import oclruler.metamodel.MetamodelMerger.DIFF_TYPE;
import oclruler.metamodel.StructuralFeature.SlotMultiplicity;
import utils.Utils;

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
	
	public Concept(Metamodel metamodel, String name, EClassifier ec) {
		super(metamodel, name);
		this.classifier = ec;
		if(ec != null)
			packageName = ec.getEPackage().getName();
		references = new ArrayList<>(5);
		attributes = new ArrayList<>(5);
		ereferences = new HashMap<>();
		eattributes = new HashMap<>();
		paths = new PathMap();
		refInstance = registerInstance();
	}
	
	public Concept(Metamodel metamodel, String name) {
		this(metamodel, name, null);
	}
	
	public boolean isEnum(){
		return false;
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
	
	@Override
	public EClassifier getEModelElement() {
		return classifier;
	}
	
//	public EClassifier getEClassifier() {
//		return classifier;
//	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public boolean putAttribute(String name, Attribute att){
		if(att.hasEStructuralFeature())
			eattributes.put(name, att.getEModelElement());
		return attributes.add(att);
	}
	
	public boolean putReference(String name, Reference r){
		if(r.hasEStructuralFeature())
			ereferences.put(name, r.getEModelElement());
		return references.add( r);
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Reference> getReferences() {
		return references;
	}
	public ArrayList<Reference> getReferences(Concept typeRef) {
		ArrayList<Reference> res = new ArrayList<>();
		for (Reference reference : references) {
			if(reference.getType().equalNames(typeRef))
				res.add(reference);
		}
		return res;
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
		return Utils.getRandom(getReferences());
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
		return Utils.getRandom(getAttributes());
	}	

	public ArrayList<StructuralFeature> getStructuralFeatures(){
		ArrayList<StructuralFeature> res = new ArrayList<StructuralFeature>();
		res.addAll(references);
		res.addAll(attributes);
		if(res.size() != attributes.size()+references.size())
			throw new IllegalStateException("Name conflict :"+references+"/"+attributes);
		return res;
	}
	
	public StructuralFeature getStructuralFeature(String name) {
		StructuralFeature res = getReference(name);
		if(res == null)
			res = getAttribute(name);
		return res;
	}

	public StructuralFeature getRandomStructuralFeature(){
		return Utils.getRandom(getStructuralFeatures());
	}	
	
	public StructuralFeature getRandomStructuralFeature(SlotMultiplicity m){
		ArrayList<StructuralFeature> sfsT = metamodel.get_T_StructuralFeatures(getStructuralFeatures(), null, m);
		return Utils.getRandom(sfsT);
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
		HashSet<String> ns = metamodel.getDirectInheritageParentNames().get(name);
		if(ns!= null)
			for (String n : ns) 
				res.add(metamodel.getConcept(n));
		return res;
	}
	
	public ArrayList<Concept> getAllSupers() {
		if(supers == null) {
			supers = new ArrayList<Concept>();
			HashSet<String> ns = metamodel.getInheritageAllSuperNames().get(name);
			if(ns!= null)
				for (String n : ns) 
					supers.add(metamodel.getConcept(n));
		}  
		return supers;
	}
	public ArrayList<Concept> getDescendants() {
		if(descendants == null) {
			descendants = new ArrayList<Concept>();
			metamodel.getInheritageNames();
			for (String n : metamodel.getInheritageNames().get(name)) {
				descendants.add(metamodel.getConcept(n));
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
		return  "["+name+"]";
	}
	
	public String refInstance() {
		return name;
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

	public Collection<StructuralFeature> getAllStructuralFeatures() {
		ArrayList<StructuralFeature> sfs = new ArrayList<>();
		for (Concept sup : getAllSupers()) {
			sfs.addAll(sup.getStructuralFeatures());
		}
		sfs.addAll(getStructuralFeatures());
//		System.out.println("Concept.getAllStructuralFeatures("+name+") "+sfs);
		return sfs;
	}
	
	public HashMap<DIFF_TYPE, ArrayList<StructuralFeature>> diffStructuralFeatures(Concept c2) {
		HashMap<DIFF_TYPE, ArrayList<StructuralFeature>> dif = new HashMap<>();
		ArrayList<StructuralFeature> adds = new ArrayList<>();
		ArrayList<StructuralFeature> remove = new ArrayList<>();
		ArrayList<StructuralFeature> card_up = new ArrayList<>();
		ArrayList<StructuralFeature> card_down = new ArrayList<>();
		for (StructuralFeature sf : getStructuralFeatures()) {
			if(!c2.getStructuralFeatures().contains(sf) && !remove.contains(sf))
				remove.add(sf);
			else {
				if(c2.getStructuralFeatures().contains(sf)){
					StructuralFeature sf_c2 = c2.getStructuralFeature(sf.getName());
					if(sf_c2.isMany() && !sf.isMany() && !card_up.contains(sf))
						card_up.add(sf);
					else if(!sf_c2.isMany() && sf.isMany() && !card_down.contains(sf))
						card_down.add(sf);
				}
			}
		}
		dif.put(DIFF_TYPE.REMOVE, remove);
		dif.put(DIFF_TYPE.CARDINALITY_UP, 	card_up);
		dif.put(DIFF_TYPE.CARDINALITY_DOWN, card_down);
		
		for (StructuralFeature sf : c2.getStructuralFeatures()) {
			if(!getStructuralFeatures().contains(sf) && !adds.contains(sf))
				adds.add(sf);
		}
		dif.put(DIFF_TYPE.ADD, adds);
		
		return dif;
	}

	@Override
	public String registerInstance() {
		instances.put(toString(), this);
		return toString();
	}

	public void setMetamodel(Metamodel mm2) {
		this.metamodel = mm2;
	}
}

package oclruler.metamodel;

import java.util.ArrayList;

import oclruler.utils.ToolBox;

public class Path extends NamedEntity  {
	
	private Concept start;
	private ArrayList<StructuralFeature> path;
	public static final int MAX_PATHS_DEPTH = 3;
	
	public Path(Concept start, ArrayList<StructuralFeature> path) {
		super("Path");
		this.start = start;
		this.path = path;
	}
	
	public Path(Concept start, StructuralFeature... path) {
		this(start);
		this.start = start;
		for (StructuralFeature reference : path) {
			this.path.add(reference);
		}
	}
	
	public Path(Concept start, Reference first) {
		this(start);
		path.add(first);
	}
	
	public Path(Concept start) {
		this(start, new ArrayList<StructuralFeature>(3));
	}
	
	
	public void append(Path path){
		if(!path.getStart().equalNames(getEndType()))
			throw new IllegalArgumentException("Paths don't stick : "+path.getRelativeStringPath()+" does not start with a "+getEndType().getName());
		for (StructuralFeature reference : path.getStructuralFeaturesPath()) {
			this.path.add(reference);
		}
	}
	public void append(StructuralFeature newTail){
		if(!newTail.getSourceConcept().equalNames(getEndType()))
			throw new IllegalArgumentException("Paths don't stick : "+newTail+" does not start from "+getEndType().getName());
		this.path.add(newTail);
	}
	
//	public void append(Attribute newTail){
//		if(!newTail.getSourceConcept().equalNames(getEndType()))
//			throw new IllegalArgumentException("Paths don't stick : "+newTail+" does not start from "+getEndType().getName());
//		this.path.add(newTail);
//	}
	
	public ArrayList<StructuralFeature> getStructuralFeaturesPath() {
		return path;
	}
	
	/**
	 * 
	 * @param pathStr start.ref0. ... .refn
	 * @return A path : Concept-start -> ref0 -> ... -> refn -> Concept-end (null if references tips do not fit with each other's)
	 */
	public static Path getPath(String pathStr){
		String[] pathStrs = pathStr.split("\\.");
		if(pathStrs.length == 0 )
			return null;
		Concept start = Metamodel.getConcept(pathStrs[0]);
		if(start != null && pathStr.length() >1){
			Path p = new Path(start);
			for (int i = 1; i < pathStrs.length; i++) {
				StructuralFeature sf = p.getEndType().getStructuralFeature(pathStrs[i]);
				if(sf != null)
					p.append(sf);
			}
			return p;
		}
		return null;
	}
	
	public void setPath(ArrayList<StructuralFeature> path) {
		this.path = path;
	}
	
	public void setStart(Concept start) {
		this.start = start;
	}
	
	/**
	 * 
	 * @return The type (Concept) of the last segement of the path (i.e., the last reference type).
	 */
	public Concept getEndType(){
		if(path.isEmpty())
			return start;
		return path.get(path.size()-1).getType();
	}
	
	public MMElement getEnd(){
		if(path.isEmpty())
			return start;
		return path.get(path.size()-1);
	}
	public Concept getStart(){
		return start;
	}
	
	/**
	 * 
	 * @return Number of references chained together to form the path
	 */
	public int length(){
		return path.size();
	}
	
	/**
	 * 
	 * @return Number of references chained together to form the path
	 */
	private int size() {
		return path.size();
	}

	/**
	 * 
	 * @return Reference chain as a string : "r0.r1.r2"
	 */
	public String getStringPath() {
		String res = "";
		int endi = path.size();
		int i = 0;
		for (StructuralFeature reference : path)
			res += reference.getName() + ((++i < endi) ? "." : "");
		return res;
	}

	/**
	 * 
	 * @return c :: Concept-start | {@link #getStringPath()}
	 */
	public String getRelativeStringPath() {
		String res = start.getName().toLowerCase() + getNumericId() + "::" + start.getName() + " | ";
		return res+getStringPath();
	}

	/**
	 * 
	 * @param index
	 * @return reference at index : "start.ref0.ref1.ref2".get(1) == ref1
	 */
	private StructuralFeature get(int i) {
		return path.get(i);
	}

	
	/**
	 * o Must be a Path of same start Concept, and same size with same references.<vr/>
	 * Compares references in the chain with their name : assuming names are unambiguous.
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null || (getClass() != o.getClass()))
			return false;
		Path p = (Path) o;
		if(!start.equalNames(p.start)) return false;
		if(p.size() != this.size())	return false;
		boolean res = true;
		for (int i = 0; i < path.size() && res; i++) 
			res &= get(i).equals(p.get(i));
		
		return res;
	}
	
	public Path clone() {
		Path p = new Path(start, path);
		return p;
	}
	
	@Override
	public String toString() {
		return getId()+prettyPrint();//"<"+getStringPath()+">)";
	}

	@Override
	public String prettyPrint(String tab) {
		return tab+"["+start.getName()+">"+getStringPath()+">"+getEndType().getName()+"]";
	}
	@Override
	public String simplePrint() {
		return "["+start.getName()+">"+length()+">"+getEndType().getName()+"]";
	}

	public boolean mutate() {
		Path p = null;
		int maxLoops = 10, i = 0;
		do{ 
			p = ToolBox.getRandom(start.getPaths(getEndType()));
		} while (p.equals(this) && i++ < maxLoops);
		boolean res = i < maxLoops && !p.equals(this);
		if(res)
			setPath(p.path);
		return res;
	}
	
	
}

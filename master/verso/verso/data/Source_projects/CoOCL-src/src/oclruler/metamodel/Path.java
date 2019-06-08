package oclruler.metamodel;

import java.util.ArrayList;

import utils.Utils;

public class Path extends NamedEntity  {
	
	private Concept start;
	private ArrayList<Reference> path;
	public static final int MAX_PATHS_DEPTH = 3;
	
	public Path(Concept start, ArrayList<Reference> path) {
		super("Path");
		this.start = start;
		this.path = path;
	}
	
	public Path(Concept start, Reference... path) {
		this(start);
		this.start = start;
		for (Reference reference : path) {
			this.path.add(reference);
		}
	}
	
	public Path(Concept start, Reference first) {
		this(start);
		path.add(first);
	}
	
	public Path(Concept start) {
		this(start, new ArrayList<Reference>(3));
	}
	
	
	public void append(Path path){
		if(!path.getStart().equalNames(getEnd()))
			throw new IllegalArgumentException("Paths don't stick : "+path.getRelativeStringPath()+" does not start with a "+getEnd().getName());
		for (Reference reference : path.getPath()) {
			this.path.add(reference);
		}
	}
	
	public ArrayList<Reference> getPath() {
		return path;
	}
	
	public void setPath(ArrayList<Reference> path) {
		this.path = path;
	}
	
	public void setStart(Concept start) {
		this.start = start;
	}
	
	/**
	 * 
	 * @return The type (Concept) of the last segement of the path (i.e., the last reference type).
	 */
	public Concept getEnd(){
		if(path.isEmpty())
			return null;
		return path.get(path.size()-1).getType();
	}
	
	public Concept getStart(){
		return start;
	}
	
	public int length(){
		return path.size();
	}

	/**
	 * 
	 * @return Reference chain as a Path : "r0.r1.r2"
	 */
	public String getStringPath() {
		String res = "";
		int endi = path.size();
		int i = 0;
		for (Reference reference : path)
			res += reference.getName() + ((++i < endi) ? "." : "");
		return res;
	}

	/**
	 * 
	 * @return "(c :: Class | {@link #getStringPath()}
	 */
	public String getRelativeStringPath() {
		String res = start.getName().toLowerCase() + getNumericId() + "::" + start.getName() + " | ";
		return res+getStringPath();
	}

	private Reference get(int i) {
		return path.get(i);
	}

	private int size() {
		return path.size();
	}
	
	/**
	 * o Must be a Path of same size with same references.<vr/>
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
		return "("+ getId()+prettyPrint();//"<"+getStringPath()+">)";
	}

	@Override
	public String prettyPrint(String tab) {
		return tab+"["+start.getName()+">"+getStringPath()+">"+getEnd().getName()+"]";
	}
	@Override
	public String simplePrint() {
		// TODO Auto-generated method stub
		return "["+start.getName()+">"+length()+">"+getEnd().getName()+"]";
	}

	public boolean mutate() {
		Path p = null;
		int maxLoops = 10, i = 0;
		do{ 
			p = Utils.getRandom(start.getPaths(getEnd()));
		} while (p.equals(this) && i++ < maxLoops);
		boolean res = i < maxLoops && !p.equals(this);
		if(res)
			setPath(p.path);
		return res;
	}
}

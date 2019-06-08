package verso.model;

import java.util.ArrayList;

import verso.representation.cubeLandscape.representationModel.EntityRepresentation;

public class Filter {
	
	//Characteristics of the Filter
	private String name;
	
	//List and children
	private ArrayList<EntityRepresentation> classList;
	private Filter child1=null;
	private Filter child2=null;
	private String currentOperator = null;
	
	//Operator, current and diffrent type of operators
	public static final String UNION = "Union";
	public static final String INTERSECTION = "Intersection";
	public static final String DIFFERENCE = "Difference";
	public static final String DIFFERENCE_SYMETRIQUE = "Difference symetrique";
	public static final String PLUS = "Plus";
	public static final String MINUS = "Minus";
	
	
	
	
	public Filter(String n) {
		this.name = n;
	}
	
	public Filter(String n, ArrayList<EntityRepresentation> l) {
		this(n);
		this.classList = l;
	}
	
	public Filter(String n, String o, Filter a, Filter b) {
		this(n);
		this.operate(o, a, b);
	}
	
	
	private ArrayList<EntityRepresentation> operate(String o, Filter a, Filter b){
		this.currentOperator = o;
		this.child1 = a;
		this.child2 = b;
		switch (o){
			case UNION:
				this.classList = this.union(this.child1, this.child2);
				break;
			case INTERSECTION:
				this.classList = this.inter(this.child1, this.child2);
				break;
			case DIFFERENCE:
				this.classList = this.diff(this.child1, this.child2);
				break;
			case DIFFERENCE_SYMETRIQUE:
				this.classList = this.diff_sym(this.child1, this.child2);
				break;
			case PLUS:
				this.classList = this.plus(this.child1, this.child2);
				break;
			case MINUS:
				this.classList = this.minus(this.child1, this.child2);
				break;
		}
		
		return this.classList;
	}
	
	/**
	 * Get potential result of the union between Filter A and B
	 * @param a
	 * @param b
	 * @return potential filter
	 */
	public ArrayList<EntityRepresentation> union(Filter a, Filter b){
		ArrayList<EntityRepresentation> ret = new ArrayList<EntityRepresentation>();
		ret.addAll(a.getClassList());
		for(EntityRepresentation element : b.getClassList()) {
			if(!a.getClassList().contains(element)) {
				ret.add(element);
			}
		}
		return ret;
	}
	
	/**
	 * Get potential result of the intersection between Filter A and B
	 * @param a
	 * @param b
	 * @return potential filter
	 */
	public ArrayList<EntityRepresentation> inter(Filter a, Filter b){
		ArrayList<EntityRepresentation> ret = new ArrayList<EntityRepresentation>();
		for(EntityRepresentation element : b.getClassList()) {
			if(a.getClassList().contains(element)) {
				ret.add(element);
			}
		}
		return ret;
	}
	
	/**
	 * Get potential result of the diffrence between Filter A and B
	 * @param a
	 * @param b
	 * @return potential filter
	 */
	public ArrayList<EntityRepresentation> diff(Filter a, Filter b){
		ArrayList<EntityRepresentation> ret = new ArrayList<EntityRepresentation>();
		for(EntityRepresentation element : b.getClassList()) {
			if(!a.getClassList().contains(element)) {
				ret.add(element);
			}
		}
		return ret;
	}
	
	/**
	 * Get potential result of the symetric diffrence between Filter A and B
	 * @param a
	 * @param b
	 * @return potential filter
	 */
	public ArrayList<EntityRepresentation> diff_sym(Filter a, Filter b){
		ArrayList<EntityRepresentation> ret = new ArrayList<EntityRepresentation>();
		ret.addAll(a.getClassList());
		for(EntityRepresentation element : b.getClassList()) {
			if(!a.getClassList().contains(element)) {
				ret.add(element);
			}
			else {
				ret.remove(element);
			}
		}
		return ret;
	}
	
	
	/**
	 * Get potential result of the add between Filter A and B
	 * @param a
	 * @param b
	 * @return potential filter
	 */
	public ArrayList<EntityRepresentation> plus(Filter a, Filter b){
		ArrayList<EntityRepresentation> ret = new ArrayList<EntityRepresentation>();
		ret.addAll(a.getClassList());
		for(EntityRepresentation element : b.getClassList()) {
			if(!a.getClassList().contains(element)) {
				ret.add(element);
			}
		}
		return ret;
	}
	
	/**
	 * Get potential result of the minus between Filter A and B
	 * @param a
	 * @param b
	 * @return potential filter
	 */
	public ArrayList<EntityRepresentation> minus(Filter a, Filter b){
		ArrayList<EntityRepresentation> ret = new ArrayList<EntityRepresentation>();
		ret.addAll(a.getClassList());
		for(EntityRepresentation element : b.getClassList()) {
			if(a.getClassList().contains(element)) {
				ret.remove(element);
			}
		}
		return ret;
	}
	
	/**
	 * Get the list of classes
	 * @return list of classes
	 */
	public ArrayList<EntityRepresentation> getClassList(){
		return this.classList;
	}
	
	/**
	 * Get the first child
	 * @return child
	 */
	public Filter getChild1() {
		return this.child1;
	}
	
	/**
	 * Get the second child
	 * @return child
	 */
	public Filter getChild2() {
		return this.child2;
	}
	
	/**
	 * Get the operator. It's the operator between child 1 and 2 who gives the result of this filter.
	 * @return operator
	 */
	public String getOperator() {
		return this.currentOperator;
	}
	
	/**
	 * To know if te filter has children or not
	 * @return True if it has children, else false.
	 */
	public boolean hasChildren() {
		return child1!=null;
	}
	
	/**
	 * Return true if this filter is a son of the filter given in param.
	 * @param f Potentially the father
	 * @return true if this filter is a son of the filter given in param, false else.
	 */
	public boolean isASonOf(Filter f) {
		if(!f.hasChildren())
			return false;
		else if(f.getChild1().equals(this)||f.getChild2().equals(this))
			return true;
		else
			return (this.isASonOf(f.getChild1()) || this.isASonOf(f.getChild2()));
	}
	
	/**
	 * To know if the filter is the father of the filter given in param.
	 * @param f
	 * @return True if the filter is the father, else false.
	 */
	public boolean isParentOf(Filter f) {
		if(!this.hasChildren()) return false;
		else if(this.child1.equals(f)) return true;
		else if(this.child2.equals(f)) return true;
		else return false;
	}
	
	/**
	 * Make the filter flat
	 */
	public void flat() {
		child1=null;
		child2=null;
		currentOperator = null;
	}
	
	public Filter duplicate(String name) {
		Filter ret = new Filter(name);
		ret.child1 = this.child1;
		ret.child2 = this.child2;
		ret.currentOperator = this.currentOperator;
		ret.classList = (ArrayList<EntityRepresentation>)this.classList.clone();
		return ret;
	}
	
	/**
	 * Get all the classes in string
	 * @return
	 */
	public String getClassListToString() {
		String ret = "Nombre de résultats : "+this.classList.size();
		for(int i=0; i<this.classList.size(); i++) {
			ret+="\n"+this.classList.get(i);
		}
		return ret;
	}
	
	/**
	 * Get the name of the filter
	 * @return name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Set the name of the filter
	 * @param n New name
	 */
	public void setName(String n) {
		this.name = n;
	}
	
	@Override
	public Filter clone() {
		Filter ret = new Filter(this.name, (ArrayList<EntityRepresentation>)this.classList.clone());
		if(this.hasChildren()) {
			ret.child1 = this.child1.clone();
			ret.child2 = this.child2.clone();
			ret.currentOperator = this.currentOperator;
		}
		return ret;
	}
	
	
	
	@Override
	public boolean equals(Object o) {
		if(o==null) return false;
		return ((Filter)o).getName().equals(this.name);

	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	
}

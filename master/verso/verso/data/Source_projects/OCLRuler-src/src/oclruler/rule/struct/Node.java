package oclruler.rule.struct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import oclruler.genetics.UnstableStateException;
import oclruler.metamodel.Concept;
import oclruler.metamodel.MMElement;
import oclruler.metamodel.NamedEntity;
import oclruler.rule.patterns.Pattern;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class Node  extends NamedEntity {
	
	
	/**
	 * 3 integers to ponderate possible options in deep mutation operation
	 * <ul>
	 *   <li>[0] Specific rate</li>
	 *   <li>[1] Change for new random node rate</li>
	 *   <li>[2] Nest in FO rate</li>
	 *   <li>[3] Nest in NOT rate</li>
	 * </ul>
	 */
	public static int[] DEEP_MUTATION_OPTIONS_RATES;

	public static void loadConfig(){
		String s = Config.getStringParam("DEEP_MUTATION_OPTIONS_RATES");
		try {
			DEEP_MUTATION_OPTIONS_RATES = new int[]{Integer.decode(s.split(" ")[0]), Integer.decode(s.split(" ")[1]), Integer.decode(s.split(" ")[2]), Integer.decode(s.split(" ")[3])};
		} catch (Exception e) {
			Logger.getGlobal().severe("'DEEP_MUTATION_CHOICES_RATES' undefined : '"+s+"'\n Expected syntax is '<int> <int> <int> <int>' (respectively \"Specific\", \"Change for new node\", \"Nest in FO\" and \"Nest in NOT\" choices' rates)" );
			System.exit(1);
		}
	}
	
	
	protected ArrayList<Node> children;
	protected Node parent;
	protected Type type;
	protected Concept context;
	
	
	public Node(Concept context, Type type) {
		this(null, context, type);
	}
	
	public Node(Node parent, Concept context,  Type type) {
		super("Node_"+type.getName());
		this.parent = parent;
		if(parent != null){
			parent.addChild(this);
		}
		this.context = context;
		children = new ArrayList<>(2);
		this.type = type;
		
	}
	
	
	/**
	 * @return true if : same context, same parent, same type, children->forAll(equals(o.children))
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null || (getClass() != o.getClass()))
			return false;
		Node n = (Node) o;
		if((context != null && n.context != null) && !n.context.equals(context))	return false;
		
		if( ((parent != null) && (n.parent == null)) || ((parent == null) && (n.parent != null)) ) return false;
		
//		if(parent != null && parent != n.parent) return false; //Deep or not deep ?!
		
		if(type != n.type)	return false;
		
		
		if((children != null) != (n.children != null))
			return false;
			
		if(children != null && n.children != null)
			if(children.size() != n.children.size()) return false;
		
		
		for (int i = 0; i < children.size(); i++) 
			if(!children.get(i).equals(n.children.get(i)))	return false;
		
		return true;
	}
	
	public void prune() throws CollapsingException{
		for (Node node : children) 
			node.prune();
		
		if(isType(Type.NOT)){
			if(getChild(0).isType(Type.NOT) && getChild(0).toBePruned()){
				Node mid = collapseChildren(getChild(0));
				setChild(0, collapseChildren(mid));
				getChild(0).setParent(this);
			}
		} else {
			Node[] ns = new Node[children.size()];
			children.toArray(ns);
			for (int i = 0; i < ns.length ; i++) {
				if(ns[i].toBePruned()){
					if(getChild(0).isType(Type.NOT)){
						Node mid = ns[i].isType(Type.NOT)?collapseChildren(ns[i]) : ns[i];
						setChild(i, collapseChildren(mid));
						
					} else {
							setChild(i, collapseChildren(ns[i]));
					}
				}
			}
		}
	}
	
	protected boolean toBePruned(){
		if(isType(Type.NOT)){
			boolean res = getChild(0).isType(Type.NOT) || getChild(0).isType(Type.TRUE);
			return res;
		}
		if(isType(Type.FO)){
			boolean res = getChild(0).isType(Type.TRUE);
			if(res)
				return res;
		}
		boolean prune = children.size()>1;
		int i = 1;
		for (i = 1; i < children.size() && prune; i++) 
			prune &= (children.get(i-1).equals(children.get(i)) || children.get(i-1).isType(Type.TRUE) || children.get(i).isType(Type.TRUE));
		return prune;
	}
	
	
	public int getChildIndex(){
		if(parent == null) 
			return -1;
		return parent.children.size()==1?0:parent.children.indexOf(this);
	}
	
	/**
	 * Collapse first child
	 * @param n
	 * @return
	 */
	static int loop2 = 1;
	public static Node collapseChildren(Node n) throws CollapsingException{
		if(n.type == Type.DEFAULT)
			return n;
		if(n.type == Type.TRUE){
			return n;
		}
		Node gdparent = n.getParent();
		Node res =  n.getChild(0);
		res.setParent(gdparent);
		if(gdparent != null){
			int idx = n.getChildIndex();
			if(idx < 0 ) idx = 0;
			gdparent.setChild(idx, res);//idx <- gdparent.children.indexOf(n)
		}
		return res;
	}
	
	/**
	 * Cross this node with the node pased in parameter.<br/>
	 * If there is (sub)contexts in common, one is picked randomly and a subnode with that context in each 'this' and 'n' are swaped.
	 * @param n
	 * @return
	 */
	public boolean cross(Node n){
		Set<Concept> ctxts = this.getComonContexts(n);
		if(ctxts.isEmpty())
			return false;
		//Prendre un ctxt randomly
		Concept ctxt = ToolBox.getRandom(ctxts);
		//Pick randomly a subnode with that ctxt in both ctxt lists
		Node cut1 = ToolBox.getRandom(this.getAllContexts().get(ctxt));
		Node cut2 = ToolBox.getRandom(n.getAllContexts().get(ctxt));
		
		//Swap the subnodes
		return swapNodes(cut1, cut2);
	}
	
	/**
	 * @param n Node referent 2
	 * @param cut1 subNode from this to swap
	 * @param cut2 subNode from referent2 to swap with cut1
	 */
	public static boolean swapNodes(Node cut1, Node cut2) {
		Node pcut1 = cut1.getParent();
		Node pcut2 = cut2.getParent();

		if(pcut1 == null && pcut2 == null){
			return false;
		} 
		
		if(cut1.equals(cut2))
			return false;
		
		if(pcut1 != null){
			int i = pcut1.getChildren().indexOf(cut1);
			pcut1.setChild(i, cut2);
		}
		
		if(pcut2 != null){
			int j = pcut2.getChildren().indexOf(cut2);
			pcut2.setChild(j, cut1);
		}
		
		cut1.setParent(pcut2);
		cut2.setParent(pcut1);
		return true;
	}

	protected void updateSurnames() {
		Node_FO fo = inFO();
		String up = (fo!=null)?fo.getSubName():"self";
		setSelfOrSubName(up);
		for (Node child : children) {
			child.updateSurnames(up);
		}
	}
	
	protected void updateSurnames(String subname) {
		for (Node child : children) {
			child.updateSurnames(subname);
		}
	}

	/**
	 * Return a newly instantiated node with <em>same parent</em>, same context, same type and <em>cloned children</em>.
	 */
	public Node clone(){
		Node n = NodeFactory.createEmptyNode(null, context, type);
		if(parent != null){
			Node p = parent.clone();
			n.setParent(p);
			p.setChild(getChildIndex(), n);
		}
		
		Node[] ns = new Node[children.size()];
		children.toArray(ns);
		for (Node child : ns) {
			Node clone = child.cloneNoParent();
			clone.setParent(n);
			n.addChild(clone);
		}
		return n;
	}
	
	public Node cloneNoParent(){
		Node n = NodeFactory.createEmptyNode(null, context, type);
		for (Node child : children) {
			Node clone = child.cloneNoParent();
			clone.setParent(n);
			n.addChild(clone);
		}
		return n;
	}
	
	public void setSelfOrSubName(String subname){
		for (Node node : children) {
			node.setSelfOrSubName(subname);
		}
	}
	
	
	
	public Node getParent() {
		return parent;
	}
	
	public Node getRoot(){
		if(parent == null)
			return this;
		return parent.getRoot();
	}
	
	public Node_FO inFO(){
		if(parent == null)
			return null;
		if(parent.type == Type.FO)
			return (Node_FO)parent;
		return parent.inFO();
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	public Set<Concept> getComonContexts(Node n){
		Set<Concept> res = new HashSet<>(5);
		Set<Concept> c1 = getAllContexts().keySet();
		for (Concept c : c1) {
			if( n.getAllContexts().keySet().contains(c) && !res.contains(c))
				res.add(c);
		}
		return res;
	}
	
	
	public Concept getContext() {
		return context;
	}
	
	public ArrayList<Node> getChildren() {
		return children;
	}
	public ArrayList<Node> getChildren(int minNumberOfChildren) {
		if(minNumberOfChildren <= 0)
			return getChildren();
		ArrayList<Node> res = new ArrayList<>(2);
		for (Node node : children) {
			if(node.getExpectedNumberOfChildren() >= minNumberOfChildren)
				res.add(node);
		}
		return res;
	}
	
	public int getExpectedNumberOfChildren() {
		return type.getNumberOfChildren();
	}

	public Node getChild(int idx){
		return children.get(idx);
	}
	
	public Node setChild(int idx, Node child){
		return children.set(idx, child);
	}
	
	public boolean addChild(Node child){
		if(children.size()>=type.numberOfChildren) //NEVER DO THAT !!!!!
			System.out.println("Node.addChild() --> ZZZ-ZZZ-ZZZ-ZZZzzz... toMany kids !!! "+children.size()+" already.\n"+this);
		return children.add(child);
	}
	public void removeChild(int idx) {
		 children.remove(idx);
	}
	public boolean removeChild(Node child) {
		return children.remove(child);
	}

	public Type getType() {
		return type;
	}
	public int size(){
		int res = children.size();
		for (Node node : children) 
			res += node.size();
		return res;
	}

	/**
	 * Length of descendance tree from this node.
	 * @return Length of descendance tree from this node.
	 */
	public int depth() {
		int maxKids = 0;
		for (Node node : children) 
			if(node.depth() > maxKids)
				maxKids = node.depth();
		return maxKids+1;
	}
	
	public int getNumberOfLeaves(){
		int res = 0;
		for (Node node : children) 
			res += node.getNumberOfLeaves();
		return res;
	}
	
	public ArrayList<Pattern> getLeafs(){
		ArrayList<Pattern> res = new ArrayList<>(2);
		for (Node node : children) 
			res.addAll(node.getLeafs());
		return res;
	}
	
	/**
	 * Passing call to children only... To be overriden in subclasses !
	 * @return
	 */
	public Collection<MMElement> getMMElements()	{
		HashSet<MMElement> res = new HashSet<>(2);
		for (Node node : children) {
			res.addAll(node.getMMElements());
		}
		return res;
	}
	
	/**
	 * 
	 * @return The node express in OCL.
	 */
	public String getOCL(){
		updateSurnames();
		return getOCL("");
	}

	/**
	 * @param tab String tabulation from left column (for a pretty print).
	 * @return The node express in OCL.
	 */
	public String getOCL(String tab){
		String res = "";
		switch (type) {
		case IMPLIES:
		case OR:
		case AND:
			return tab+"(\n"+getChild(0).getOCL(tab+ToolBox.TAB_CHAR) + "\n"+tab+type.getName().toLowerCase()+ "\n"+getChild(1).getOCL(tab+ToolBox.TAB_CHAR)+"\n"+tab+")";
		case NOT:
			return tab+"not ( "+getChild(0).getOCL(tab+ToolBox.TAB_CHAR) + ") ";
		default:
			int i = 0;
			for (Node node : children) {
				res += tab+"( "+tab+"\n"+node.getOCL(tab+ToolBox.TAB_CHAR) + " ) "+(++i<children.size()?"\n"+tab+ ") "+type.getName()+"":tab+") ");
			}
			return res;
		}
	}

	public String prettyPrint() {
		return prettyPrint("");
	}

	public String prettyPrint(String tab) {
		String res = tab+type+"_"+numid+"("+context.getName()+")"+" {";
		for (Node node : children) {
			res += "\n"+ node.prettyPrint(tab+ToolBox.TAB_CHAR);
		}
		res += "\n"+tab+"}";
		return res;
	}
	
	public String printXML() {
		return printXML("");
	}
	
	protected String XML_HeaderAttributes(){
		return " id="+getNumericId()+(parent!=null?" parent="+parent.getNumericId():"")+" context=\""+(context==null?"null":context.getName())+"\" ";
	}
	public String printXML(String tab){
		String res = tab+"<"+type+XML_HeaderAttributes()+">";
		for (Node node : children) 
			res += "\n" + node.printXML(tab+"  ");
		if(!res.endsWith("\n")) res += "\n";
		return res + tab+"</"+type+">";
	}
	
	public String printSimpleXML() {
		return printSimpleXML("");
	}
	public String printSimpleXML(String tab){
		String res = tab+"<"+type+" id="+getNumericId()+">";
		for (Node node : children) 
			res += "\n" + node.printSimpleXML(tab+"  ");
		if(!res.endsWith("\n")) res += "\n";
		return res + tab+"</"+type+">";
	}
	public String printOnlyIds() {
		return printOnlyIds("");
	}
	public String printOnlyIds(String tab){
		String res = tab+getNumericId()+"_"+type;
		for (Node node : children) 
			res += "\n" + node.printOnlyIds(tab+"  ");
		if(!res.endsWith("\n")) res += "\n";
		return res ;
	}
	


	@Override
	public String simplePrint() {
		return getId()+"("+context.getName()+")";
	}

	@Override
	public String toString() {
		return printXML("");
	}
	

	public WeakHashMap<Concept, ArrayList<Node>> getAllContexts() {
		return visitAllContexts(new WeakHashMap<Concept, ArrayList<Node>>(5));
	}
	
	public WeakHashMap<Concept, ArrayList<Node>> visitAllContexts(WeakHashMap<Concept, ArrayList<Node>> contexts) {
		ArrayList<Node> nds = contexts.get(this.getContext());
		if(nds == null)
			contexts.put(this.getContext(), nds = new ArrayList<>(5));
		if( !nds.contains(this))
			nds.add(this);
		for (Node c : this.getChildren()) {
			c.visitAllContexts(contexts);
		}
		return contexts;
	}

	public boolean isType(Type default1) {
		return type == default1;
	}

	/**
	 * Make a simple mutation on the Node. Type must not be a FO or DEFAULT (overriden in their own class)<br/>
	 * <ul>
	 * 	<li>AND and OR : switch type</li>
	 * 	<li>IMPLISE : change type (to OR or AND) or change children order</li>
	 * 	<li>NOT : make it NOT NOT. If parent != null -> pruning ; if parent == null -> actual NOT NOT</li>
	 * 	<li>FO and DEFAULT : rise exception</li>
	 * </ul>
	 * @return
	 */
	static int loop = 1;
	public boolean mutateSimple() throws UnstableStateException {
//		System.out.println(getId()+".mutate simple ");
		loop++;
		switch (type) {
		case AND:
		case OR:
			type = ToolBox.getRandom(type.mutants());
			return true;
		case IMPLIES:
			int key = ToolBox.getRandomInt(2);
			switch (key) {
			case 0:	
				//Change operator
				type = ToolBox.getRandom(type.mutants());
				return true;
			case 1:	
				//Change children's order
				if(isType(Type.IMPLIES)){
					Node tmp = getChild(0);
					setChild(0, getChild(1));
					setChild(1, tmp);
					return true;
				}
				break;
			}
		case NOT:
			if(parent != null){
				int idx = getChildIndex();
				if(idx < 0) idx = 0;
				try {
					Node n = Node.collapseChildren(this);
					parent.setChild(idx, n);
					n.setParent(parent);
				} catch (CollapsingException e) {
					//this hasn't changed
				} 
			} else {
				Node child = getChild(0);
				Node n = new Node(null, context, Type.NOT);
				n.addChild( child);
				child.setParent(n);
				setChild(0, n);
			}
			return true;
			
		case FO:
		case DEFAULT:
			throw new IllegalStateException("Node_"+type+".mutateSimple() MUST BE CALLED !");
			//Not supposed to be reach -> polymorphism calls FO.applyMutation()
		case TRUE:
			break;//A TRUE 
		}

		return false;
	}
	
	public enum DeepMutationType {
		Specific(DEEP_MUTATION_OPTIONS_RATES[0]),
		ChangeForRandomNode(DEEP_MUTATION_OPTIONS_RATES[1]),
		NestInFO(DEEP_MUTATION_OPTIONS_RATES[2]),
		NestInNOT(DEEP_MUTATION_OPTIONS_RATES[3]);
		
		
		DeepMutationType(int pond){
			initialyseListProba(this, pond);
		}
		
		void initialyseListProba(DeepMutationType dmt,int pond) {
			for (int i = 0; i < pond; i++) {
				listProba.add(dmt);
				if(dmt != DeepMutationType.Specific)
					listProbaDEFAULT.add(dmt);
			}
		}
		
		static DeepMutationType getRandom() {
			return ToolBox.getRandom(listProba);
		}
		
		static DeepMutationType getRandom(Type typeNode) {
			if(typeNode == Type.DEFAULT)
				return ToolBox.getRandom(listProbaDEFAULT);
			return ToolBox.getRandom(listProba);
		}
		
	}
	static ArrayList<DeepMutationType> listProba = new ArrayList<>();
	static ArrayList<DeepMutationType> listProbaDEFAULT = new ArrayList<>();

	public Node mutateDeep() throws UnstableStateException  {
		DeepMutationType key = DeepMutationType.getRandom(type);
//		System.out.println(getId()+"\tmutateDeep : "+key);
		Node mutant = mutateDeep(key);
		return mutant;
	}
	

	public Node mutateDeep(DeepMutationType type) {
//		System.out.println(getId()+"\tmutateDeep : "+type);
		
		switch (type) {
		case Specific:
		case ChangeForRandomNode://Change node for a random one
			Node n = NodeFactory.createRandomNode(null, context, inFO());
			if(parent != null){
				int idx = getChildIndex();
				if(idx < 0)	idx = 0;
				parent.setChild(idx, n);
				n.setParent(parent);
			}
//			this.nullify();
			this.setParent(null);
			return n;
		case NestInFO://put the node in a FO
			Node_FO nFO = (Node_FO)NodeFactory.createEmptyNode(null, context, Type.FO);
			
			if(nFO.randomCompletion(this.getContext())){ // If completion works (i.e., there is a path (length <=3) between parent.context and this.context in which the FO can occur).
				
				if(parent != null){
					int idx = getChildIndex();
	//				if(idx < 0)	idx = 0;
					parent.setChild(idx, nFO);
					nFO.setParent(parent);
				}
				
				nFO.addChild(this);
				this.setParent(nFO);
				return nFO; 
			}											 // If completion doesn't work (i.e., there is no path) a NOT node is created instead.
//			nFO.nullify();
			nFO = null;
		case NestInNOT://put the node in a NOT
			
			Node n2 = NodeFactory.createEmptyNode(null, context, Type.NOT);
			if(parent != null) {
				int idx = getChildIndex();
				if(idx < 0)	idx = 0;
				parent.setChild(idx, n2);
				n2.setParent(parent);
			}
			
			n2.addChild(this);
			this.setParent(n2);
			return n2;
		default:
			break;
		}
		return this;
	}
	
	public void nullify() {
		parent = null;
		context = null;
		children = null;
		type = null;
	}
	
	/**
	 * 
	 * Existing types of node : AND OR IMPLIES, NOT, FO and DEFAULT. They correspond to the <code>"Node_..."</code> class.
	 * 
	 * @author Batot
	 *
	 */
	public enum Type {
		AND("AND", 2),
		OR("OR", 2),
		IMPLIES("IMPLIES", 2),
		NOT("NOT", 1),
		FO("FO", 1),
		TRUE("TRUE", 0),
		DEFAULT("DEFAULT", 0);
		
		private String name = "";
		private int numberOfChildren = 0;
		// Constructeur
		Type(String name, int numberOfChildren) {
			this.name = name;
			this.numberOfChildren = numberOfChildren;
		}

		
		public ArrayList<Type> mutants(){
			switch (this) {
			case AND:
				return new ArrayList<Type>(Arrays.asList(OR, IMPLIES));
			case OR:
				return new ArrayList<Type>(Arrays.asList(AND, IMPLIES));
			case NOT:
				return new ArrayList<Type>(Arrays.asList());
			case IMPLIES:
				return new ArrayList<Type>(Arrays.asList(AND, OR));
			case FO:
			case TRUE:
			case DEFAULT:
				return new ArrayList<Type>();
			default:
				throw new IllegalStateException("Type inconsequent : "+this);
			}
		}
		public boolean isOperator(){
			switch (this) {
			case AND:
			case OR:
			case NOT:
			case IMPLIES:
				return true;
			default:
				return false;
			}
		}
		
		public static Type getTypeFromCompleteName(String name) {
			return Type.valueOf(name.substring(name.indexOf("_")+1, name.length()));
		}

		public String toString() {
				return name;
		}
		public int getNumberOfChildren() {
			return numberOfChildren;
		}
		
		public String getName() {
			return name;
		}
		static String PREFIX_CLASSNAME = null;
		public String getCompleteName(){
			if(PREFIX_CLASSNAME == null)
				PREFIX_CLASSNAME = getClass().getName().substring(0, getClass().getName().lastIndexOf("."));
			return PREFIX_CLASSNAME+".Node_"+name+"";
		}
		
		@SuppressWarnings("unchecked")
		public Class<? extends Node> getNodeClass(){
			Class<? extends Node> c = null;
			try {
				c = (Class<? extends Node>)Class.forName(getCompleteName());
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Class not found : '"+getCompleteName()+"'.");
			}
			return c;
		}
		
		/**
		 * 
		 * @return Types of kind "Operator" : { AND, OR, IMPLIES, NOT }
		 */
		public static ArrayList<Type> valuesOP(){
			return new ArrayList<Type>(Arrays.asList(AND, OR, IMPLIES, NOT));
		}
	
		
		public static ArrayList<Type> valuesNot(Type... exclusion){
			ArrayList<Type> res = new ArrayList<>(values().length);
			for (Type type : values()) {
				boolean add = true;
				for (Type t2 : exclusion) {
					if(type == t2) add = false;
				}
				if(add)
					res.add(type);
			}
			return res;
		}
		
		public static ArrayList<Type> valuesNot(ArrayList<Type> exclusion){
			ArrayList<Type> res = new ArrayList<>(values().length);
			for (Type type : values()) {
				boolean add = true;
				for (Type t2 : exclusion) {
					if(type == t2) add = false;
				}
				if(add)
					res.add(type);
			}
			return res;
		}
	}


	
}

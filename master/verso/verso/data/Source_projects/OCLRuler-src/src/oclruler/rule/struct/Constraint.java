package oclruler.rule.struct;


import java.util.ArrayList;
import java.util.Collection;
import java.util.WeakHashMap;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.ocl.expressions.PropertyCallExp;

import oclruler.genetics.Gene;
import oclruler.genetics.UnstableStateException;
import oclruler.metamodel.Concept;
import oclruler.metamodel.MMElement;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Model;
import oclruler.metamodel.NamedEntity;
import oclruler.metamodel.StructuralFeature;
import oclruler.rule.patterns.Pattern;
import oclruler.rule.struct.Node.Type;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class Constraint extends NamedEntity implements Gene {
	
	private static double DEEP_MUTATION_RATE = 0.3;

	public static void loadConfig() {
		DEEP_MUTATION_RATE 		= Config.getDoubleParam("DEEP_MUTATION_RATE");
	}
	
	Concept mainContext;
	WeakHashMap<Concept, ArrayList<Node>> contexts;
	Node root;
	
	
	public Constraint(Concept context) {
		super("Cst_"+(context != null ? context.getName() : "null"));
		
		nbInstances ++;
		this.mainContext = context;
		contexts = new WeakHashMap<>();
	}

	public Constraint(Node root) {
		this(root.getContext());
		this.root = root;
	}

	public Constraint() {
		super("Cst_");
		contexts = new WeakHashMap<>();
	}

	public WeakHashMap<Concept, ArrayList<Node>> getContexts(){
		return getRoot().getAllContexts();
	}
	
	/**
	 * Random depth between [1..{@link NodeFactory.CREATION_DEPTH}], fixed context. (CREATION_SIZE might be forced : see {@link NodeFactory.FORCE_CREATION_DEPTH})
	 */
	public static Constraint createRandomConstraint(Concept context) {
		int depth = ToolBox.getRandomInt(NodeFactory.FORCE_CREATION_DEPTH?NodeFactory.CREATION_DEPTH:1, NodeFactory.CREATION_DEPTH);
		return createRandomConstraint(context, depth);
	}

	/**
	 * Fixed depth, fixed context
	 */
	public static Constraint createRandomConstraint(Concept context, int depth) {
		Node n = NodeFactory.createRandomNode(null, context, depth, null);
		if(n == null){
			System.out.println("Constraint.createRandomConstraint("+context+","+depth+")");
			System.out.println(n);
		}
		if(n != null)
			return new Constraint( n);
		return null;
	}
	
	/**
	 * Random depth between [1..{@link NodeFactory.CREATION_SIZE}], random context
	 * @return
	 */
	public static Constraint createRandomConstraint() {
		Constraint c = null;
		int maxLoops = 10, i = 0;
		do{
		   c = createRandomConstraint(ToolBox.getRandom(Metamodel.getAuthorizedConcepts()), -1);
		}while(c == null && i++ < maxLoops);
		return c;
	}
	
	public Concept getContext() {
		return mainContext;
	}
	public Node getRoot() {
		return root.getRoot();
	}
	public void setRoot(Node root) {
		this.root = root;
		this.mainContext = root.getContext();
	}

	static int loop = 1;
	@Override
	public boolean mutate() throws UnstableStateException {
		/*
		 * Simple mutation :   inside node
		 * Complexe mutation : change node's nature
		 * 		Change the parent's child(index(this))
		 * 		Change the root !
		 * 
		 * Node.mutate should return a Node ? this or the new one + check en sortie si la node est la même (return n.mutate == n)
		 */
		
		Node n = pickRandomNode();
		
		boolean res = false;
		int key = ToolBox.getRandomDouble() < DEEP_MUTATION_RATE ? 1 : 0;
		switch (key) {
		case 0:
			res = n.mutateSimple();
			break;
		case 1:
			try {
				Node n2 = n.mutateDeep();
				res = n.equals(n2);
				if(n == getRoot()){
					setRoot(n2.getRoot());
				}
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			break;
		default:
			break;
		}
		try {
			prune();
		} catch (Exception e) { 
			e.printStackTrace();
		}
		synchroContextWithRoot();
		getRoot().updateSurnames();
		return res;
	}
	
	private void synchroContextWithRoot() {
		this.mainContext = getRoot().getContext();
	}

	/**
	 * 
	 * @param minNumberOfChild Number of child a node must have to be collected
	 * @param minDepth Minimum depth considered
	 * @return
	 */
	public Node pickRandomNode(int minNumberOfChild, int minDepth){
		int depth = Math.max(minDepth, ToolBox.getRandomInt(getRoot().depth()));
		boolean find = false;
		Node n2 = getRoot();
		for(int i = 0; i < depth && !find ; i++){
			if(i >= minDepth){
				if(n2.getChildren().isEmpty())
					find = true;
				else {
					Node n = ToolBox.getRandom(n2.getChildren(minNumberOfChild));
					if(n != null)
						n2 = n;
				}
			}
		}		
		return n2;
	}
	public Node pickRandomNode(){
		return pickRandomNode(-1, 0);
	}

	@Override
	public int size() {
		return getRoot().size();
	}



	/**
	 * Return two constraints :
	 * <ul>
	 * 	<li>if no common context between originals, same constraints returned, order changed (return {cs2, this})</li>
	 * 	<li>if common context exists : cross between two nodes with same context randomly picked in the tree.</li>
	 * </ul>
	 * @param cs2
	 * @return
	 */
	public Constraint[] cross(Constraint cs2) {
		Constraint res1 = clone();
		Constraint res2 = cs2.clone();
		
		
		boolean cross = res1.getRoot().cross(res2.getRoot());
		res1.updateSurnames();
		res2.updateSurnames();
		res1.synchroContextWithRoot();
		res2.synchroContextWithRoot();
		if(!cross)
			return new Constraint[]{res2, res1};
		
		return new Constraint[]{res1, res2};
	}

	public void updateSurnames() {
		getRoot().updateSurnames();
	}

	public Constraint clone() {
		return new Constraint(getRoot().clone());
	}


	public String printOnlyIds(String tab) {
		return tab+getId()+" {\n"+getRoot().printOnlyIds(tab+"  ")+"\n"+tab+"}";
	}
	
	public String printSimpleXML(String tab) {
		return tab+getId()+" {\n"+getRoot().printSimpleXML(tab+"  ")+"\n"+tab+"}";
	}

	public String printXML() {
		return printXML("");
	}
	public String printXML(String tab) {
		return tab+"<CST id="+numid+">\n"+getRoot().printXML(tab+ToolBox.TAB_CHAR)+"\n"+tab+"</CST>";
	}
	
	public String prettyPrint(String tab) {
		String res = tab + "context: "+mainContext.simplePrint()+" --("+getRoot().depth()+")\n";
		res += getRoot().prettyPrint(tab);
		return res;
	}
	
	@Override
	public String prettyPrint() {
		return prettyPrint("");
	}

	@Override
	public String printResultPane(String tab) {
		return printXML(tab);
	}


	@Override
	public String simplePrint() {
		return getId()+"{"+mainContext+", "+getRoot().getId()+"("+getRoot().depth()+", "+getRoot().getNumberOfLeaves()+")"+"}";
	}

	public String getOCL_inv() {
		String res = " inv "+getId()+ " : \n"+getRoot().getOCL();
		return res;
	}

	public String getOCL() {
		return getRoot().getOCL();
	}

	public String getRawOCLConstraint() {
		return getRoot().getOCL();
	}

	@Override
	public boolean equals(Object o) {
		if(o == null || (getClass() != o.getClass()))
			return false;
		Constraint c = (Constraint) o;
		if( ((mainContext == null) != (c.mainContext == null)))
			return false;
		
		if(mainContext != null && !c.mainContext.equalNames(mainContext)) return false;
		return getRoot().equals(c.getRoot());
	}


	public void prune() throws Exception {
		getRoot().prune();
		if(getRoot().toBePruned()){
			Node mid = (getRoot().type == Type.NOT) ? Node.collapseChildren( getRoot()) : getRoot();
			setRoot(Node.collapseChildren( mid  ));
		}
	}
	
	int fires;
	public int addFires(int fires) {
		this.fires += fires;
		return this.fires;
	}

	public int getFires() {
		return fires;
	}
	
	public int addFire(){
		fires++;
		return fires;
	}
	public void cleanFires() {
		fires = 0;
	}



	public void setFires(int fires) {
		this.fires = fires;
	}
	
	
	static int nbInstances = 0;
	public static int numberOfInstances(){
		return nbInstances;
	}
	
	/**
	 * Not implemented. Requires <code>Model</code>s to be filled with content - and not only a Jess executable string.<br/>
	 * Requires model files to be parsed.
	 * @param m
	 * @return
	 */
	public int check(Model m) {
		int res = 0;
		return res;
	}

	public int getNumberOfLeaves() {
		return getRoot().getNumberOfLeaves();
	}
	
	public ArrayList<Pattern> getLeafs() {
		return getRoot().getLeafs();
	}


	public int depth() {
		return getRoot().depth();
	}

	public void cutNode(Node n) {
		if(n.parent != null){
			if(n.parent.getExpectedNumberOfChildren() == 1){
//				System.out.println("Constraint.cutNode()");
//				System.out.println("------------> escaped children.size = 1 :"+n.parent.getId());
			} else {
				n.parent.removeChild(n);
				try {
					n.parent = Node.collapseChildren(n.parent);
					setRoot(n.parent.getRoot());
				} catch (CollapsingException e) {
					e.printStackTrace();
				}
			}
			try {
				prune();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			//System.out.println("Cut the root... Is that a clone ?");
		}
	}

	public Collection<MMElement> getMMElements() {
//		HashSet<MMElement> mmes = new HashSet<>(getLeafs().size());
//		System.out.println("root: "+root.getType());
//		System.out.println("root: "+root.getOCL("  "));
		return getRoot().getMMElements();
//		for (Pattern p : getRoot().getMMElements()) {
//			System.out.println("p:    "+p);
//			System.out.println("p:    "+p.getOCL_inv());
//			System.out.println("Elts: "+p.getMMELements());
//			System.out.println();
//			mmes.addAll(p.getMMELements());
//		}
//		return mmes;
	}
	
	public StructuralFeature getStructuralFeatureInvolved(PropertyCallExp<EClassifier, EStructuralFeature> te ){
		StructuralFeature res = null;
		EStructuralFeature esfReferred = ((PropertyCallExp<EClassifier, EStructuralFeature>)te).getReferredProperty();
		Concept esfType = Metamodel.getConcept(esfReferred.getEContainingClass().getName());
		
		res = Metamodel.getStructuralFeature(esfReferred.getName());
		if(res == null) {
//			Metamodel mm = esfType.getMetamodel() == Metamodel.getMm2()? Metamodel.getMm2()
			esfType = Metamodel.getConcept(esfReferred.getEContainingClass().getName());
			res = esfType.getStructuralFeature(esfReferred.getName());
		}
		return res;
	}



}

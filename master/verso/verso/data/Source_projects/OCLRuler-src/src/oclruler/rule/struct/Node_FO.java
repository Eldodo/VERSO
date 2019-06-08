package oclruler.rule.struct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import oclruler.metamodel.Concept;
import oclruler.metamodel.MMElement;
import oclruler.metamodel.Path;
import oclruler.utils.ToolBox;

/**
 * 
 * 
 * Node of first ordrer. <br/>
 * Namely : forAll or exists, see {@link Node_FO.ACTION}. <br/>
 * General form :
 * <code>{@link Concept context}.{@link Path}->{@link Node_FO.ACTION}({@link Node_FO#subname c}|{@link Concept C} | {@link Node#getOCL() Node without sub FOs})</code>
 * <br/>
 * Concept C is the {@link Path#getEndType() end} of the path.
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 * 
 */
public class Node_FO extends Node {
	static int counter = 0;

	Path path;
	ACTION action;

	/**
	 * subname given to references to the end of the path in the embedded nodes.
	 */
	String subname;
	/**
	 * Coming from ascendants : is the FO starting on a self or on an other FO ?
	 */
	String surname;

	protected Node_FO(Node parent, Concept context) {
		super(parent, context, Type.FO);
	}

	/**
	 * General form :
	 * <code>Context.Path->ACTION(subName|Path.GetEnd | {@link Node#getOCL() Node without sub FOs})</code>
	 * 
	 * @param parent
	 *            Node
	 * @param context
	 *            Concept
	 * @param path
	 *            Path
	 * @param action
	 *            action
	 */
	public Node_FO(Node parent, Concept context, Path path, ACTION action) {
		this(parent, context);
		if (!path.getStart().equalNames(context))
			throw new IllegalStateException("Path must start with coherent context. \n    Path : " + path + "\n Context : " + context);
		this.path = path;
		this.action = action;
		subname = path.getEndType().getName().substring(0, 1).toLowerCase() + (counter++);
	}

		
	
	@Override
	public void prune() throws CollapsingException {
		Node child = getChild(0);
		child.prune();
		if (child.toBePruned()) {
			// System.out.println("Node_FO.prune loop :" + loop++);
			Node mid = (child.type == Type.NOT) ? Node.collapseChildren(child) : child;
			setChild(0, Node.collapseChildren(mid));
		}
		updateSurnames();
	}

	@Override
	public boolean mutateSimple() {
		int key = ToolBox.getRandomInt(2);
		// System.out.println(getId()+".mutate simple "+key);
		switch (key) {
		case 0:// Mutate path
			if (path.mutate())
				return true; // If Path.mutate fails, change action.
		case 1:// Mutate action
			action = ToolBox.getRandom(action.mutants());
			return true;
		}
		return false;
	}
	
	@Override
	public Collection<MMElement> getMMElements() {
		HashSet<MMElement> res = new HashSet<>();
		res.addAll(path.getStructuralFeaturesPath());
		res.addAll(	super.getMMElements() );
		return res;
	}

	@Override
	public void setSelfOrSubName(String subname) {
		surname = subname;
	}

	@Override
	protected void updateSurnames() {
		Node_FO fo = inFO();
		surname = (fo != null) ? fo.getSubName() : "self";
		for (Node node : children) {
			node.updateSurnames(this.subname);
		}
	}

	@Override
	protected void updateSurnames(String surname) {
		setSelfOrSubName(surname);
		for (Node node : children) {
			node.updateSurnames(this.subname);
		}
	}

	public String getSubName() {
		return subname;
	}

	public Path getPath() {
		return path;
	}

	public String getStringPath() {
		return (surname != null ? surname : "self") + "." + (path != null ? path.getStringPath() : "null");
	}

	/**
	 * Create randomly a path and pick randomly an action
	 * 
	 * @return
	 */
	public boolean randomCompletion() {
		Concept end = path != null ? path.getEndType() : null;
		;
		return randomCompletion(end);
	}

	public boolean randomCompletion(Concept end) {
		Path pth = null;
		if (end != null) {
			pth = ToolBox.getRandom(context.getPaths(end, 2));
			if (pth == null)
				pth = ToolBox.getRandom(context.getPaths(end, 2));
			if (pth == null)
				return false;
		} else {
			if (path != null)
				path.mutate();
			else
				pth = ToolBox.getRandom(context.getSimplePaths());
		}
		if (pth != null)
			setPath(pth);

		if (getPath() == null) {
			return false;
		}
		setAction(ToolBox.getRandom(ACTION.values()));
		return true;
	}

	@Override
	public Node clone() {
		Node_FO n = (Node_FO) super.clone();
		n.setPath(path.clone());
		n.setAction(action);
		return n;
	}

	@Override
	public Node cloneNoParent() {
		Node_FO n = (Node_FO) super.cloneNoParent();
		n.setPath(path);
		n.setAction(action);
		return n;
	}

	public void setAction(ACTION action) {
		this.action = action;
	}

	public void setPath(Path path) {
		this.path = path;
		subname = path.getEndType().getName().substring(0, 1).toLowerCase() + (counter++);
		for (Node node : children) {
			node.setSelfOrSubName(subname);
		}
	}

	@Override
	public String getOCL(String tab) {
		String res = tab;
		Node child = getChild(0);
		res += getStringPath();
		String embed = subname + ":" + path.getEndType().getName() + " | ";
		res += "->" + action + "(" + embed + "\n" + child.getOCL(tab + ToolBox.TAB_CHAR) + "\n" + tab + ")";
		return res;
	}

	@Override
	public String prettyPrint(String tab) {
		String embed = subname + ":" + getChild(0).getContext().getName() + " | ";
		String res = tab + "FO_" + numid + "(" + context.getName() + "):" + getStringPath();
		res += "->" + action + "(" + embed + "\n" + getChild(0).prettyPrint(tab + ToolBox.TAB_CHAR) + ")";
		return res;
	}

	@Override
	public String printXML(String tab) {
		String end = (path != null ? path.getEndType().getName() : "pathIsNull");
		String res = tab + "<" + type + XML_HeaderAttributes() + " action=\"" + action + "\" path=\"" + getStringPath() + "\"" + " end=\"" + end + ":" + subname
				+ "\">";
		for (Node node : children) {
			res += "\n" + node.printXML(tab + ToolBox.TAB_CHAR);
		}
		if (!res.endsWith("\n"))
			res += "\n";
		if (context == null)
			System.out.println(getId() + ".printXML(context == null) \n" + this);
		return res + tab + "</" + type + ">";
	}

	/**
	 * <code>subname</code> is not to be considered.
	 */
	@Override
	public boolean equals(Object o) {
		boolean res = super.equals(o);
		if (res) {
			if ((path == null) != (((Node_FO) o).path == null))
				return false;
			if ((path != null) && !path.equals(((Node_FO) o).path))
				return false;

			if ((action == null) != (((Node_FO) o).action == null))
				return false;
			if ((action != null) && !action.equals(((Node_FO) o).action))
				return false;
			// if(! subName.equals(((Node_FO)o).subName) ) return false;//No matter the aliases
		}
		return res;
	}

	public enum ACTION {
		EXISTS("exists"), FORALL("forAll");
		String name;

		ACTION(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		public ArrayList<ACTION> mutants() {
			switch (this) {
			case EXISTS:
				return new ArrayList<ACTION>(Arrays.asList(FORALL));
			case FORALL:
				return new ArrayList<ACTION>(Arrays.asList(EXISTS));
			default:
				throw new IllegalStateException("Type inconsequent : " + this);
			}
		}
	}

}

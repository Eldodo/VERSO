package oclruler.rule.struct;

import java.util.ArrayList;
import java.util.Collection;

import oclruler.metamodel.Concept;
import oclruler.metamodel.MMElement;
import oclruler.rule.PatternFactory;
import oclruler.rule.patterns.Pattern;

/**
 * Leaf of the OCL tree.
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class Node_DEFAULT extends Node {

	private Pattern pattern;
	
	@Override
	public ArrayList<Pattern> getLeafs() {
		ArrayList<Pattern> res = new ArrayList<Pattern>(1);
		res.add(pattern);
		return res;
	}
	
	@Override
	public Collection<MMElement> getMMElements() {
		return pattern.getMMElements();
	}
	
	public Node_DEFAULT(Node parent, Concept context) {
		super(parent, context, Type.DEFAULT);
	}

	public Node_DEFAULT(Node parent, Pattern pattern) {
		super(parent, pattern.getContext(), Type.DEFAULT);
		this.pattern = pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
		this.context = pattern.getContext();
		setName("Node_" + pattern.getType().shortName());
	}

	public Pattern getPattern() {
		return pattern;
	}

	protected void updateSurnames() {
		setSelfOrSubName("self");
	}

	protected void updateSurnames(String subname) {
		setSelfOrSubName(subname);
	}

	@Override
	public void setSelfOrSubName(String subname) {
		if (pattern != null)
			pattern.setSelfOrSubname(subname);
	}

	@Override
	protected boolean toBePruned() {
		return hasNullPattern();
	}

	public void prune() {
		// A fruit is not to be pruned !
	}

	@Override
	public int getNumberOfLeaves() {
		return 1;
	}

	@Override
	public boolean mutateSimple() {
		boolean res = pattern.mutate();
		return res;
	}

	public Node mutateDeep(DeepMutationType type) {
		switch (type) {
		case Specific:// Change pattern
			Pattern p = null;
			int maxtry = 10, i = 0;
			do {
				p = PatternFactory.createRandomPattern(pattern.getContext());
			} while (pattern.equals(p) && i++ < maxtry);
			if (p != null)
				setPattern(p);
			break;
		default:
			return super.mutateDeep(type);
		}
		return this;
	}

	public boolean randomCompletion() {
		Pattern p = null;
		int maxLoops = 10, i = 0;
		do {
			p = PatternFactory.createRandomPattern(context);// If only one match (exemple context Vertex) duplicate of
															// the same pattern.
		} while (p == null && i++ < maxLoops);
		if (p != null) {
			setPattern(p);
			return true;
		}
		return false;
	}

	public Node_DEFAULT clone() {
		Node_DEFAULT n = (Node_DEFAULT) super.clone();
		n.setPattern(pattern.clone());
		return n;
	}

	@Override
	public Node cloneNoParent() {
		Node_DEFAULT n = (Node_DEFAULT) super.cloneNoParent();
		n.setPattern(pattern.clone());
		return n;
	}

	public boolean hasNullPattern() {
		return pattern == null;
	}

	@Override
	public String getOCL(String tab) {
		if (pattern == null)
			return "-- PATTERN_IS_NULL";
		return tab + pattern.getRawOCLConstraint();
	}

	@Override
	public String prettyPrint(String tab) {
		String res = tab + name + "{" + "(" + context.getName() + ")" + pattern.getRawOCLConstraint() + "}";
		return res;
	}

	@Override
	public String printXML(String tab) {
		String name = pattern != null ? pattern.getType().shortName() : "DEFAULT";
		return tab + "<" + name + XML_HeaderAttributes() + "/>" + getOCL("") + "</" + name + ">";
		// String res = tab+"<"+pattern.getType()+" id="+getNumericId()+(parent!=null?"
		// parent="+parent.getNumericId():"")+"> ";
		// res += pattern.simplePrint();
		// return res + " </"+pattern.getType()+">";
	}

	@Override
	public String printSimpleXML(String tab) {
		return tab + "<" + type + " id=" + getNumericId() + "/>";
	}

	public String printOnlyIds(String tab) {
		String res = tab + getNumericId() + "_" + name;
		for (Node node : children)
			res += "\n" + node.printOnlyIds(tab + "  ");
		if (!res.endsWith("\n"))
			res += "\n";
		return res;
	}

	@Override
	public boolean equals(Object o) {
		boolean res = super.equals(o);
		if (res) {
			if ((pattern == null) != (((Node_DEFAULT) o).pattern == null))
				return false;
			if ((pattern != null) && !pattern.equals(((Node_DEFAULT) o).pattern))
				return false;
			// if(pattern.getType() == PatternType.A8_CollectionsSize)
			// System.out.println("Node_DEFAULT.equals(\n - "+pattern+"\n - "+((Node_DEFAULT)o).pattern+")\n"+res);
		}
		return res;
	}

}

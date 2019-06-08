package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Attribute;
import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.StructuralFeature.SlotMultiplicity;
import oclruler.metamodel.ocl.OCL_Comparator;
import oclruler.metamodel.ocl.OCL_Comparator.COMPARATOR;
import oclruler.metamodel.ocl.OCL_NumericValue;
import oclruler.rule.MMMatch;
import oclruler.utils.ToolBox;

/**
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A11_AttributeValueComparison extends Pattern {

	static int INITIAL_VALUE = 1;
	static COMPARATOR INIT_COMPARATOR = COMPARATOR.EQ;

	/**
	 * 
	 * @param coll
	 */
	public A11_AttributeValueComparison(Concept context, Attribute att, COMPARATOR c, Number value) {
		super("A11_AttributeValueComparison", context);
		if (att.isMany() || !att.isNumeric())
			throw new IllegalArgumentException("Attribute '" + att.getName() + "' must be a number.");
		addParameter(att);
		addParameter(new OCL_Comparator(c));
		addParameter(new OCL_NumericValue(value));
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A11_AttributeValueComparison(getContext(), getAtt(), (COMPARATOR) getComparator().getValue(), (Number) getTestValue().getValue());
		return clone;
	}

	public A11_AttributeValueComparison(MMMatch match) {
		this((Concept) match.get(0), (Attribute) match.get(1), INIT_COMPARATOR, INITIAL_VALUE);
	}

	private Attribute getAtt() {
		return (Attribute) getParameter(0);
	}

	private OCL_Comparator getComparator() {
		return (OCL_Comparator) getParameter(1);
	}

	private OCL_NumericValue getTestValue() {
		return (OCL_NumericValue) getParameter(2);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getAtt().getName() + " " + getComparator().simplePrint() + " " + getTestValue().simplePrint();
	}

	@Override
	public boolean mutateSimple() {
		if (ToolBox.getRandomDouble() > .5)
			getComparator().mutate();
		else
			getTestValue().mutate();
		return true;
	}

	/**
	 * 
	 * @return
	 */
	public static ArrayList<MMMatch> getMatches() {
		ArrayList<MMMatch> res = new ArrayList<MMMatch>();
		for (Concept c : Metamodel.getAuthorizedConcepts()) {
			for (MMMatch m : getMatches(c)) {
				if (!res.contains(m))
					res.add(m);
			}
		}
		return res;
	}

	static HashMap<Concept, ArrayList<MMMatch>> matches = new HashMap<>();;

	public static ArrayList<MMMatch> getMatches(Concept c) {
		ArrayList<MMMatch> res = matches.get(c);
		if (res == null) {
			res = new ArrayList<MMMatch>();
			ArrayList<Attribute> coll1s = Metamodel.get_T_StructuralFeatures(c.getAttributes(), Metamodel.Integer, SlotMultiplicity.single);// Integer
																																			// attributes
			for (Attribute coll1 : coll1s) {
				MMMatch m = new MMMatch(1);
				m.add(coll1.getSourceConcept());
				m.add(coll1);
				res.add(m);
			}
			matches.put(c, res);
		}
		return res;
	}
}

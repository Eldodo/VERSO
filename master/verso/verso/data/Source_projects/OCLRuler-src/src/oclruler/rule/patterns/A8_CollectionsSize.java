package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Reference;
import oclruler.metamodel.StructuralFeature;
import oclruler.metamodel.StructuralFeature.SlotMultiplicity;
import oclruler.metamodel.ocl.OCL_Comparator;
import oclruler.metamodel.ocl.OCL_Comparator.COMPARATOR;
import oclruler.metamodel.ocl.OCL_NumericValue;
import oclruler.rule.MMMatch;
import oclruler.utils.ToolBox;

/**
 * 
 * Independent from context.
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A8_CollectionsSize extends Pattern {

	int size = 1;
	static int INITIAL_VALUE = 1;
	static COMPARATOR INIT_COMPARATOR = COMPARATOR.EQ;

	/**
	 * 
	 * @param coll
	 */
	public A8_CollectionsSize(StructuralFeature coll, COMPARATOR c, Integer v) {
		super("A8_CollectionsSize", coll.getSourceConcept());
		if (!coll.isMany())
			throw new IllegalArgumentException("Reference must be MANY.");

		addParameter(coll);
		addParameter(new OCL_Comparator(c));
		addParameter(new OCL_NumericValue(v));
	}

	// public A8_CollectionsSize(StructuralFeature coll) {
	// this(coll, OCL_Comparator.getRandomComparator(), (Integer)OCL_NumericValue.getRandomValue(null));
	// }
	public A8_CollectionsSize(StructuralFeature coll) {
		this(coll, INIT_COMPARATOR, INITIAL_VALUE);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A8_CollectionsSize(getColl(), (COMPARATOR) getComparator().getValue(), (Integer) getTestValue().getValue());
		return clone;
	}

	public A8_CollectionsSize(MMMatch match) {
		this((StructuralFeature) match.get(1));
		if (match.size() > 2) {// 0 is the "coll" ; -1 is the context
			setParameter(1, match.get(2));
			setParameter(2, match.get(3));
		}
	}

	private Reference getColl() {
		return (Reference) getParameter(0);
	}

	private OCL_Comparator getComparator() {
		return (OCL_Comparator) getParameter(1);
	}

	private OCL_NumericValue getTestValue() {
		return (OCL_NumericValue) getParameter(2);
	}

	@Override
	public boolean mutateSimple() {
		if (ToolBox.getRandomDouble() > .5)
			getComparator().mutate();
		else
			getTestValue().mutate();
		modified = true;
		return true;
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getColl().getName() + "->size() " + getComparator().simplePrint() + " " + getTestValue().simplePrint() + " ";
	}

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
			ArrayList<Reference> coll1s = Metamodel.get_T_StructuralFeatures(c.getReferences(), null, SlotMultiplicity.multi);// Refs
																																// MANY
																																// :
																																// potential
																																// collection
																																// 1
			for (StructuralFeature coll1 : coll1s) {
				MMMatch m = new MMMatch(3);
				m.add(coll1.getSourceConcept());
				m.add(coll1);
				res.add(m);
			}
			matches.put(c, res);
		}
		return res;
	}
}

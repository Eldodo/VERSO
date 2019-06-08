package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Reference;
import oclruler.metamodel.StructuralFeature;
import oclruler.metamodel.StructuralFeature.SlotMultiplicity;
import oclruler.rule.MMMatch;

/**
 * It occurs when a class in a domain structure contains two equally typed collections. THe OCL expression states that
 * one of the collection is a subset of the other.
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A5_CollectionIsSubset extends Pattern {

	private Concept context_coll2;

	/**
	 * 
	 * @param coll1
	 * @param coll2
	 */
	public A5_CollectionIsSubset(Concept context_coll1, Concept context_coll2, StructuralFeature coll1, StructuralFeature coll2) {
		super("A5_CollectionIsSubset", context_coll1);
		if (!coll1.instanceOf(coll2.getType()))
			throw new IllegalArgumentException("References must be of the same type. (" + coll1.getTypeName() + " != " + coll2.getTypeName() + ")");
		if (!coll1.isMany() || !coll2.isMany())
			throw new IllegalArgumentException("References ('" + coll1.getSourceClassName() + "' and '" + coll2.getSourceClassName() + "') must be MANY. ");

		this.context_coll2 = context_coll2;
		addParameter(context_coll2);
		addParameter(coll1);
		addParameter(coll2);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A5_CollectionIsSubset(getContext(), context_coll2, getColl1(), getColl2());
		return clone;
	}

	public A5_CollectionIsSubset(MMMatch match) {
		this((Concept) match.get(0), (Concept) match.get(1), (StructuralFeature) match.get(2), (StructuralFeature) match.get(3));
	}

	private Reference getColl1() {
		return (Reference) getParameter(1);
	}

	private Reference getColl2() {
		return (Reference) getParameter(2);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getColl1().getName() + "->includesAll(" + getColl2().getName() + ")";
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
			// Ref MANY of 'c' : potential collection 1
			ArrayList<StructuralFeature> coll1s = Metamodel.get_T_StructuralFeatures(c.getStructuralFeatures(), null, SlotMultiplicity.multi);
			for (StructuralFeature coll1 : coll1s) {
				// Ref MANY of 'c' of same type as collection 1 : potential collection 2
				ArrayList<StructuralFeature> coll2s = Metamodel.get_T_StructuralFeatures(c.getStructuralFeatures(), coll1.getSourceConcept(),
						SlotMultiplicity.multi);
				for (StructuralFeature coll2 : coll2s) {
					if (coll1 != coll2) {
						MMMatch m = new MMMatch(3);
						m.add(c);
						m.add(coll2.getSourceConcept());
						m.add(coll1);
						m.add(coll2);
						if (!res.contains(m))
							res.add(m);
					}
				}
			}
			matches.put(c, res);
		}
		return res;
	}
}

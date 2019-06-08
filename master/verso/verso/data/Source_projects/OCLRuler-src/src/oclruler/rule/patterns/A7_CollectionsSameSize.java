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
 * It occurs when a class contains two collections, regardless of their types. The OCL expression specifiesthat both
 * collections are of the same type.
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A7_CollectionsSameSize extends Pattern {

	public A7_CollectionsSameSize(MMMatch match) {
		this((StructuralFeature) match.get(1), (StructuralFeature) match.get(2));
	}

	/**
	 * 
	 * @param coll1
	 * @param coll2
	 */
	public A7_CollectionsSameSize(StructuralFeature coll1, StructuralFeature coll2) {
		super("A7_CollectionsSameSize", coll1.getSourceConcept());
		if (!coll1.isMany() || !coll2.isMany())
			throw new IllegalArgumentException("references must be MANY.");
		addParameter(coll1);
		addParameter(coll2);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A7_CollectionsSameSize(getColl1(), getColl2());
		return clone;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof A7_CollectionsSameSize))
			return false;
		A7_CollectionsSameSize p = (A7_CollectionsSameSize) o;
		if (getParameters().size() != p.getParameters().size())
			return false;
		return (getColl1().getSourceConcept().equals(p.getColl1().getSourceConcept()) && getColl2().getSourceConcept().equals(p.getColl2().getSourceConcept()));
	}

	private Reference getColl1() {
		return (Reference) getParameter(0);
	}

	private Reference getColl2() {
		return (Reference) getParameter(1);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getColl1().getName() + "->size() = " + selfOrSubname + "." + getColl2().getName() + "->size()";
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
			ArrayList<Reference> coll1s = Metamodel.get_T_StructuralFeatures(c.getReferences(), null, SlotMultiplicity.multi);// Ref
																																// MANY
																																// of
																																// 'c'
																																// :
																																// potential
																																// collection
																																// 1
			for (StructuralFeature coll1 : coll1s) {
				ArrayList<Reference> coll2s = Metamodel.get_T_StructuralFeatures(c.getReferences(), null, SlotMultiplicity.multi);// Ref
																																	// MANY
																																	// of
																																	// 'c'
																																	// :
																																	// potential
																																	// collection
																																	// 2
				coll2s.remove(coll1);
				for (StructuralFeature coll2 : coll2s) {
					MMMatch m = new MMMatch(2);
					m.add(c);
					m.add(coll1);
					m.add(coll2);
					if (!res.contains(m))
						res.add(m);
				}
			}
			matches.put(c, res);
		}
		return res;
	}
}

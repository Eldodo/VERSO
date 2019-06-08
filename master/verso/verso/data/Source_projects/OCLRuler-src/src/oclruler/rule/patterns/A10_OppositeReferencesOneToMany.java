package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Reference;
import oclruler.metamodel.StructuralFeature.SlotMultiplicity;
import oclruler.rule.MMMatch;

/**
 * Similar to A9_OppositeReferenceOneToOne, but one of the reference has an upper bound higher than one.
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A10_OppositeReferencesOneToMany extends Pattern {

	/**
	 * 
	 * @param classA
	 *            (has a reference bs->classB)
	 * @param classB
	 *            (has a reference as->classA)
	 * @param bs
	 *            -many-
	 * @param as
	 *            -single-
	 */
	public A10_OppositeReferencesOneToMany(Concept classA, Concept classB, Reference bs, Reference as) {
		super("A10_OppositeReferencesOneToMany", classA);
		if (as.isMany() || !bs.isMany())
			throw new IllegalArgumentException("Reference 'as' must NOT be many, and 'bs' must be many.");

		addParameter(classB);
		addParameter(bs);
		addParameter(as);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A10_OppositeReferencesOneToMany(getClassA(), getClassB(), getBs(), getAs());
		return clone;
	}

	public A10_OppositeReferencesOneToMany(MMMatch match) {
		this((Concept) match.get(0), (Concept) match.get(1), (Reference) match.get(2), (Reference) match.get(3));
	}

	private Concept getClassA() {
		return context;
	}

	private Concept getClassB() {
		return (Concept) getParameter(0);
	}

	private Reference getBs() {
		return (Reference) getParameter(1);
	}

	private Reference getAs() {
		return (Reference) getParameter(2);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getBs().getName() + "->forAll(b : " + getBs().getTypeName() + " | b." + getAs().getName() + " = " + selfOrSubname + ")";
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
			//references multi sortant de c
			ArrayList<Reference> bss = Metamodel.get_T_StructuralFeatures(c.getReferences(), null, SlotMultiplicity.multi);
			for (Reference bs : bss) {
				//reference single sortant de bs.source de type c
				ArrayList<Reference> ass = Metamodel.get_T_StructuralFeatures(bs.getType().getReferences(), c, SlotMultiplicity.single);
				for (Reference as : ass) {
					MMMatch m = new MMMatch(3);
					m.add(c);
					m.add(bs.getType());
					m.add(bs);
					m.add(as);
					res.add(m);
				}
			}
			matches.put(c, res);
		}
		return res;
	}
}

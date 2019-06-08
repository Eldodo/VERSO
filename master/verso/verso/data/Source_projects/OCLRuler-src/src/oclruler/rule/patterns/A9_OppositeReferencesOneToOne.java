package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Reference;
import oclruler.metamodel.StructuralFeature.SlotMultiplicity;
import oclruler.rule.MMMatch;

/**
 * It occurs when two classes are present in the domain structure, so that both conatain references typed with the
 * other. The OCL expression state that the value of these references are opposite.
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A9_OppositeReferencesOneToOne extends Pattern {

	/**
	 */
	public A9_OppositeReferencesOneToOne(Concept classA, Concept classB, Reference bs, Reference as) {
		super("A9_OppositeReferencesOneToOne", classA);
		if (as.isMany() || bs.isMany())
			throw new IllegalArgumentException("References must NOT be many. ");

		addParameter(classB);
		addParameter(bs);
		addParameter(as);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A9_OppositeReferencesOneToOne(getClassA(), getClassB(), getBs(), getAs());
		return clone;
	}

	public A9_OppositeReferencesOneToOne(MMMatch match) {
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
		return selfOrSubname + "." + getBs().getName() + "." + getAs().getName() + " = " + selfOrSubname;
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
			ArrayList<Reference> bss = Metamodel.get_T_StructuralFeatures(c.getReferences(), null, SlotMultiplicity.single);
			for (Reference bs : bss) {
				ArrayList<Reference> ass = Metamodel.get_T_StructuralFeatures(bs.getType().getReferences(), c, SlotMultiplicity.single);
				for (Reference as : ass) {
					MMMatch m = new MMMatch(3);
					m.add(c);
					m.add(bs.getSourceConcept());
					m.add(bs);
					m.add(as);
					if (!res.contains(m))
						res.add(m);
				}
			}
			matches.put(c, res);
		}
		return res;
	}
}

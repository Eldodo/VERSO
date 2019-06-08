package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Reference;
import oclruler.metamodel.StructuralFeature.SlotMultiplicity;
import oclruler.rule.MMMatch;

/**
 * Occurs when a class contains a reference typed with itself. The OCL expression states that the value of this
 * reference cannot be the same instance object that contains it.
 * 
 * <br/>
 * <br/>
 * <i>WARNING !! NAME IS CONFOUNDING !</i><br/>
 * The rule fires when the reference is THE SAME instance object, indicating a mis-behavior.
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A14_ReferenceDifferentFromSelf extends Pattern {
	/**
	 * 
	 * @param coll
	 */
	public A14_ReferenceDifferentFromSelf(Concept context, Reference ref) {
		super("A14_ReferenceDifferentFromSelf", context);
		if (ref.isMany())
			throw new IllegalArgumentException("The reference (" + ref + ") must NOT be many.");

		addParameter(ref);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A14_ReferenceDifferentFromSelf(getContext(), getRef());
		return clone;
	}

	public A14_ReferenceDifferentFromSelf(MMMatch match) {
		this((Concept) match.get(0), (Reference) match.get(1));
	}

	private Reference getRef() {
		return (Reference) getParameter(0);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getRef().getName() + " <> " + selfOrSubname;
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
			ArrayList<Reference> bss = Metamodel.get_T_StructuralFeatures(c.getReferences(), c, SlotMultiplicity.single);
			for (Reference bs : bss) {
				MMMatch m = new MMMatch(1);
				m.add(c);
				m.add(bs);
				if (!res.contains(m))
					res.add(m);
			}
			matches.put(c, res);
		}
		return res;
	}
}

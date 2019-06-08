package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.rule.MMMatch;

/**
 * Occurs when a class has a subclass. The OCL expression state that the concrete data type of instances of this class
 * must be the subclass
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A18_SelfIsSubtype extends Pattern {
	/**
	 * 
	 * @param coll
	 */
	public A18_SelfIsSubtype(Concept context, Concept subtype) {
		super("A18_SelfIsSubtype", context);
		if (!subtype.hasSuper(context))
			throw new IllegalArgumentException("'" + subtype + "' must be a subtype of '" + context + "'");
		addParameter(subtype);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A18_SelfIsSubtype(getContext(), getSubtype());
		return clone;
	}

	public A18_SelfIsSubtype(MMMatch match) {
		this((Concept) match.get(0), (Concept) match.get(1));
	}

	private Concept getSubtype() {
		return (Concept) getParameter(0);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + ".oclIsKindOf(" + getSubtype().getName() + ")";
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
			for (Concept c2 : c.getAllSupers()) {
				MMMatch m = new MMMatch(1);
				m.add(c2);
				m.add(c);
				if (!res.contains(m))
					res.add(m);
			}
			matches.put(c, res);
		}
		return res;
	}
}

package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Reference;
import oclruler.rule.MMMatch;

/**
 * Occurs when a class contains a collection typed with itself. The OCL expression states that an instance of this class
 * also makes part of this contained collection.
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A13_CollectionIncludesSelf extends Pattern {
	/**
	 * 
	 * @param coll
	 */
	public A13_CollectionIncludesSelf(Concept context, Reference coll) {
		super("A13_CollectionIncludesSelf", context);
		addParameter(coll);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A13_CollectionIncludesSelf(getContext(), getColl());
		return clone;
	}

	public A13_CollectionIncludesSelf(MMMatch match) {
		this((Concept) match.get(0), (Reference) match.get(1));
	}

	private Reference getColl() {
		return (Reference) getParameter(0);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getColl().getName() + "->includes(" + selfOrSubname + ")";
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

	static HashMap<Concept, ArrayList<MMMatch>> matches = new HashMap<>();

	public static ArrayList<MMMatch> getMatches(Concept c) {
		ArrayList<MMMatch> res = matches.get(c);
		if (res == null) {
			res = new ArrayList<MMMatch>();
			ArrayList<Reference> bss = Metamodel.get_T_StructuralFeatures(c.getReferences(), c, null);
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

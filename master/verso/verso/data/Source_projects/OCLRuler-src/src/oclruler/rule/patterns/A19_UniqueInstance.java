package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.rule.MMMatch;

/**
 * Occurs for every class in the domain structure. The OCL expression specifies that only one object of this class may
 * exist in a model
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A19_UniqueInstance extends Pattern {
	/**
	 * 
	 * @param coll
	 */
	public A19_UniqueInstance(Concept context) {
		super("A19_UniqueInstance", context);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A19_UniqueInstance(getContext());
		return clone;
	}

	public A19_UniqueInstance(MMMatch match) {
		this((Concept) match.get(0));
	}

	@Override
	public String getRawOCLConstraint() {
		return context.getName() + ".allInstances()->size() = 1";
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
			MMMatch m = new MMMatch(0);
			m.add(c);
			res.add(m);
			matches.put(c, res);
		}
		return res;
	}
}

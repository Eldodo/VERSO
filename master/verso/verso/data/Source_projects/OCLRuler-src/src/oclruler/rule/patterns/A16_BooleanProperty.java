package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Attribute;
import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.rule.MMMatch;

public class A16_BooleanProperty extends Pattern {
	/**
	 * 
	 * @param context,
	 *            Concept
	 * @param att,
	 *            boolean attribute to test : Boolean nature is to be ensured before call as
	 */
	public A16_BooleanProperty(Concept context, Attribute att) {
		super("A16_BooleanProperty", context);
		if (att.isMany() || !att.isBoolean())
			throw new IllegalArgumentException("Attribute '" + att.getName() + "' must be a boolean");
		addParameter(att);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A16_BooleanProperty(getContext(), getAtt());
		return clone;
	}

	public A16_BooleanProperty(MMMatch match) {
		this((Concept) match.get(0), (Attribute) match.get(1));
	}

	private Attribute getAtt() {
		return (Attribute) getParameter(0);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getAtt().getName();
	}

	/**
	 * Returns all the couple (Concept.BooleanAttribute found in the Metamodel).
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
			ArrayList<Attribute> attsBoolean = Metamodel.get_T_StructuralFeatures(c.getAttributes(), Metamodel.Boolean, null);
			for (Attribute attribute : attsBoolean) {
				MMMatch m = new MMMatch(1);
				m.add(c);
				m.add(attribute);
				if (!res.contains(m))
					res.add(m);
			}
			matches.put(c, res);
		}
		return res;
	}
}

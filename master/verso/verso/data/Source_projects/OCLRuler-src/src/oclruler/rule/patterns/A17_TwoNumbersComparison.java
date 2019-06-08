package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Attribute;
import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.rule.MMMatch;

public class A17_TwoNumbersComparison extends Pattern {
	/**
	 * 
	 * @param coll
	 */
	public A17_TwoNumbersComparison(Concept context, Attribute att1, Attribute att2) {
		super("A17_TwoNumbersComparison", context);
		if (att1.isMany() || !att1.isNumeric())
			throw new IllegalArgumentException("Attribute '" + att1.getName() + "' must be a number.");
		if (att2.isMany() || !att2.isNumeric())
			throw new IllegalArgumentException("Attribute '" + att2.getName() + "' must be a number.");

		addParameter(att1);
		addParameter(att2);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A17_TwoNumbersComparison(getContext(), getAtt1(), getAtt2());
		return clone;
	}

	public A17_TwoNumbersComparison(MMMatch match) {
		this((Concept) match.get(0), (Attribute) match.get(1), (Attribute) match.get(2));
	}

	private Attribute getAtt1() {
		return (Attribute) getParameter(0);
	}

	private Attribute getAtt2() {
		return (Attribute) getParameter(1);
	}

	/**
	 * VERIFIED
	 */
	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getAtt1().getName() + " <> " + selfOrSubname + "." + getAtt2().getName();
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
			ArrayList<Attribute> int1s = Metamodel.get_T_StructuralFeatures(c.getAttributes(), Metamodel.Integer, null);
			for (Attribute int1 : int1s) {
				ArrayList<Attribute> int2s = Metamodel.get_T_StructuralFeatures(c.getAttributes(), Metamodel.Integer, null);
				for (Attribute int2 : int2s) {
					if (!int1.equals(int2)) {
						MMMatch m = new MMMatch(2);
						m.add(c);
						m.add(int1);
						m.add(int2);
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

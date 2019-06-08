package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Attribute;
import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.StructuralFeature;
import oclruler.rule.MMMatch;

/**
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A12_AttributeUndefined extends Pattern {
	/**
	 * 
	 * @param coll
	 */
	public A12_AttributeUndefined(Concept context, Attribute att) {
		super("A12_AttributeUndefined", context);
		if (att.isMany())
			throw new IllegalArgumentException("Attribute must not be many.");
		addParameter(att);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A12_AttributeUndefined(getContext(), getAtt());
		return clone;
	}

	public A12_AttributeUndefined(MMMatch match) {
		this((Concept) match.get(0), (Attribute) match.get(1));
	}

	private Attribute getAtt() {
		return (Attribute) getParameter(0);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getAtt().getName() + ".oclIsUndefined()";
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

	/**
	 * 
	 * @return
	 */
	static HashMap<Concept, ArrayList<MMMatch>> matches = new HashMap<>();;

	public static ArrayList<MMMatch> getMatches(Concept c) {
		ArrayList<MMMatch> res = matches.get(c);
		if (res == null) {
			res = new ArrayList<MMMatch>();
			ArrayList<Attribute> coll1s = Metamodel.get_T_StructuralFeatures(c.getAttributes(), null, null);// Integer
																											// attributes
			for (StructuralFeature coll1 : coll1s) {
				MMMatch m = new MMMatch(1);
				m.add(coll1.getSourceConcept());
				m.add(coll1);
				if (!res.contains(m))
					res.add(m);
			}
			matches.put(c, res);
		}
		return res;
	}
}

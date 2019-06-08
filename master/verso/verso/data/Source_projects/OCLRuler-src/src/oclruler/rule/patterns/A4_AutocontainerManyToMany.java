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
 * Similar to A2_AutocontainerOneToMany, but in ths case an element can have more than one container. The OCL expression
 * specifies that the element must exist in the content of all its containers.
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A4_AutocontainerManyToMany extends Pattern {

	public A4_AutocontainerManyToMany(Concept context, Reference contents, Reference containedIn) {
		super("A4_AutocontainerManyToMany", context);
		if (!contents.isMany() || !containedIn.isMany())
			throw new IllegalArgumentException("'contents' and 'containedIn' references must be many.");
		addParameter(contents);
		addParameter(containedIn);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A4_AutocontainerManyToMany(getContext(), getContents(), getContainedIn());
		return clone;
	}

	public A4_AutocontainerManyToMany(MMMatch match) {
		this((Concept) match.get(0), (Reference) match.get(1), (Reference) match.get(2));
	}

	private Reference getContents() {
		return (Reference) getParameter(0);
	}

	private Reference getContainedIn() {
		return (Reference) getParameter(1);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getContents().getName() + "->forAll(a : " + getContents().getTypeName() + " | a." + getContainedIn().getName()
				+ "->includes(" + selfOrSubname + "))";
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
			ArrayList<StructuralFeature> contentss = Metamodel.get_T_StructuralFeatures(c.getStructuralFeatures(), c, SlotMultiplicity.multi);
			for (StructuralFeature contents : contentss) {
				ArrayList<StructuralFeature> containedIns = Metamodel.get_T_StructuralFeatures(c.getStructuralFeatures(), c, SlotMultiplicity.multi);
				for (StructuralFeature containedIn : containedIns) {
					MMMatch m = new MMMatch(2);
					m.add(c);
					m.add(contents);
					m.add(containedIn);
					if (!res.contains(m))
						res.add(m);
				}
			}
			matches.put(c, res);
		}
		return res;
	}
}

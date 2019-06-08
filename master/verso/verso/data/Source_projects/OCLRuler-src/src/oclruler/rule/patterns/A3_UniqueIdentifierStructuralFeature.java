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
 * In this case, classA is the class whose objects have an identifying attribute, called idReference. THe OCL expression
 * does not enforce a particular datatype for this attribute. ClassB is the context, which contains a collection (as) of
 * two or more objects objects of type ClassA.
 * 
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A3_UniqueIdentifierStructuralFeature extends Pattern {

	public A3_UniqueIdentifierStructuralFeature(Concept context, StructuralFeature as, StructuralFeature idRef) {
		super("A3_UniqueIdentifierStructuralReference", context);

		if (!as.isMany())
			throw new IllegalArgumentException("'as' reference must be many.");

		addParameter(as);
		addParameter(idRef);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A3_UniqueIdentifierStructuralFeature(getContext(), getAs(), getIdRef());
		return clone;
	}

	public A3_UniqueIdentifierStructuralFeature(MMMatch match) {
		this((Concept) match.get(0), (StructuralFeature) match.get(1), (StructuralFeature) match.get(2));
	}

	private StructuralFeature getAs() {
		return (StructuralFeature) getParameter(0);
	}

	private StructuralFeature getIdRef() {
		return (StructuralFeature) getParameter(1);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getAs().getName() + "->forAll(a1 : " + getAs().getTypeName() + ", a2 : " + getAs().getTypeName() + " | a1."
				+ getIdRef().getName() + " = a2." + getIdRef().getName() + " implies a1 = a2)";
	}

	public static ArrayList<MMMatch> getMatches() {
		ArrayList<MMMatch> res = new ArrayList<MMMatch>();
		for (Concept c : Metamodel.getAuthorizedConcepts()) {// potential contexts
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
			// Refs MANY outing 'c' = potential 'as's
			ArrayList<Reference> ass = Metamodel.get_T_StructuralFeatures(c.getReferences(), null, SlotMultiplicity.multi);
			for (StructuralFeature as : ass) {// potential 'as'

				// Refs SINGLE outing 'c' = potential 'idReference's
				ArrayList<StructuralFeature> idRefs = Metamodel.get_T_StructuralFeatures(as.getType().getStructuralFeatures(), c, SlotMultiplicity.single);
				for (StructuralFeature idRef : idRefs) {
					MMMatch m = new MMMatch(2);
					m.add(c);
					m.add(as);
					m.add(idRef);
					if (!res.contains(m))
						res.add(m);
				}
			}
			matches.put(c, res);
		}
		return res;
	}
}

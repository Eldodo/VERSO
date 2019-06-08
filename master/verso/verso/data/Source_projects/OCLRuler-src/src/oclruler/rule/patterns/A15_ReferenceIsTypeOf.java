package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Reference;
import oclruler.metamodel.StructuralFeature.SlotMultiplicity;
import oclruler.rule.MMMatch;

/**
 * Occurs when a class contains a reference to a given data type, but a concrete sub type must be always used. THe OCL
 * expression specified the permitted concrete subtype.
 * 
 * Warning : fires for each object which reference 'ref' respect the type rule.
 * 
 * @author batotedo
 *
 */
public class A15_ReferenceIsTypeOf extends Pattern {
	/**
	 * 
	 * @param coll
	 */
	public A15_ReferenceIsTypeOf(Concept context, Concept subA, Reference ref) {
		super("A15_ReferenceIsTypeOf", context);
		addParameter(subA);
		addParameter(ref);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A15_ReferenceIsTypeOf(getContext(), getSubA(), getRef());
		return clone;
	}

	public A15_ReferenceIsTypeOf(MMMatch match) {
		this((Concept) match.get(0), (Concept) match.get(1), (Reference) match.get(2));
	}

	private Concept getSubA() {
		return (Concept) getParameter(0);
	}

	private Reference getRef() {
		return (Reference) getParameter(1);
	}

	static int count = 0;

	@Override
	public String getRawOCLConstraint() {
		count++;
		if (getRef().isMany())
			return selfOrSubname + "." + getRef().getName() + "->forAll(a15" + numid + ":" + getRef().getTypeName() + " | oclIsKindOf(" + getSubA().getName()
					+ "))";
		else
			return selfOrSubname + "." + getRef().getName() + ".oclIsKindOf(" + getSubA().getName() + ")";
		// return "self."+getRef().getName()+"->any()->oclIsKindOf("+getSubA().getName()+")";
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
			ArrayList<Reference> ass = Metamodel.get_T_StructuralFeatures(c.getReferences(), null, SlotMultiplicity.single);
			for (Reference as : ass) {
				ArrayList<Concept> subAs = as.getSourceConcept().getDescendants();
				
				for (Concept subA : subAs) {
					if(!subA.equalNames(c)){
						MMMatch m = new MMMatch(2);
						m.add(c);
						m.add(subA);
						m.add(as);
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

package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.StructuralFeature;
import oclruler.rule.MMMatch;

/**
 * The pattern applies whenever there is a class containing a reference which type is it self. Also, the upper of the
 * reference can be one or many. (OCL upgrade since Cadavid's work)
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A1_AcyclicReference extends Pattern {

	public A1_AcyclicReference(MMMatch match) {
		this((Concept) match.get(0), (StructuralFeature) match.get(1));
	}

	public A1_AcyclicReference(Concept context, StructuralFeature ref) {
		super("A1_AcyclicReference", context);
		addParameter(ref);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A1_AcyclicReference(getContext(), getRef());
		return clone;
	}

	private StructuralFeature getRef() {
		return (StructuralFeature) getParameter(0);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getRef().getName() + "->closure(iterator : " + getRef().getTypeName() + " | iterator." + getRef().getName()
				+ ")->excludes(" + selfOrSubname + ")";
	}

	/**
	 * Parcours les concepts. Pour chaque concept, pour toutes ses features MULTI de type identique (au self) (contents)
	 * pour toutes ses features SINGLE de type identique (au self) (containedIns) Créer un Match (c, content,
	 * containedIn)
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

	/**
	 * Pour le concept fourni en parametre, pour toutes ses features MULTI de type identique (au self) (contents) pour
	 * toutes ses features SINGLE de type identique (au self) (containedIns) Créer un Match (c, content, containedIn)
	 */
	public static ArrayList<MMMatch> getMatches(Concept c) {
		ArrayList<MMMatch> res = matches.get(c);
		if (res == null) {
			res = new ArrayList<MMMatch>();
			ArrayList<StructuralFeature> refs = Metamodel.get_T_StructuralFeatures(c.getStructuralFeatures(), c, null);
			for (StructuralFeature ref : refs) {
				MMMatch m = new MMMatch(2);
				m.add(c);
				m.add(ref);
				if (!res.contains(m))
					res.add(m);
			}
			matches.put(c, res);
		}
		return res;
	}
}

package oclruler.rule.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.StructuralFeature;
import oclruler.metamodel.StructuralFeature.SlotMultiplicity;
import oclruler.rule.MMMatch;

/**
 * Every element of 'contents' has a reference 'containedIn' toward the origin (with ID ?id_context)
 * 
 * One class and two relationships are required ; one pointing to the contained elements ond the other to the container
 * element ; the associated WFR in this case specifies that these relationships are allways opposite.
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A2_AutocontainerOneToMany extends Pattern {

	public A2_AutocontainerOneToMany(Concept context, StructuralFeature contents, StructuralFeature containedIn) {
		super("A2_AutocontainerOneToMany", context);
		if (!contents.isMany())
			throw new IllegalArgumentException("'contents' reference must be many. (" + contents + ")");
		if (containedIn.isMany())
			throw new IllegalArgumentException("'containedIn' reference must NOT be many. (" + containedIn + ")");

		addParameter(contents);
		addParameter(containedIn);
	}

	@Override
	public Pattern clone() {
		Pattern clone = new A2_AutocontainerOneToMany(getContext(), getContents(), getContainedIn());
		return clone;
	}

	public A2_AutocontainerOneToMany(MMMatch match) {
		this((Concept) match.get(0), (StructuralFeature) match.get(1), (StructuralFeature) match.get(2));
	}

	private StructuralFeature getContents() {
		return (StructuralFeature) getParameter(0);
	}

	private StructuralFeature getContainedIn() {
		return (StructuralFeature) getParameter(1);
	}

	@Override
	public String getRawOCLConstraint() {
		return selfOrSubname + "." + getContents().getName() + "->forAll(a : " + getContents().getTypeName() + " | a." + getContainedIn().getName() + " = "
				+ selfOrSubname + ")";
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
	 * Pour le concept passé en paramètre pour toutes ses features MULTI de type identique (au self) (contents) pour
	 * toutes ses features SINGLE de type identique (au self) (containedIns) Créer un Match (c, content, containedIn)
	 */
	public static ArrayList<MMMatch> getMatches(Concept c) {
		ArrayList<MMMatch> res = matches.get(c);
		if (res == null) {
			res = new ArrayList<MMMatch>();
			ArrayList<StructuralFeature> contentss = Metamodel.get_T_StructuralFeatures(c.getStructuralFeatures(), c, SlotMultiplicity.multi);
			for (StructuralFeature contents : contentss) {
				ArrayList<StructuralFeature> containedIns = Metamodel.get_T_StructuralFeatures(c.getStructuralFeatures(), c, SlotMultiplicity.single);
				for (StructuralFeature containedIn : containedIns) {
					MMMatch m = new MMMatch(2);
					m.add(c);
					m.add(contents);
					m.add(containedIn);
					res.add(m);
				}
			}
			matches.put(c, res);
		}
		return res;
	}
}

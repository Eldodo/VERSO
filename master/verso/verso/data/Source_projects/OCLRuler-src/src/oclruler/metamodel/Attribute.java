package oclruler.metamodel;

import org.eclipse.emf.ecore.EAttribute;

public class Attribute extends StructuralFeature {
	
	public Attribute(String name, Concept source, Concept type, SlotMultiplicity card) {
		super(name, source, type, card);
	}
	
	public Attribute(EAttribute eAttribute, Concept e) {
		super(eAttribute, e);
	}

	@Override
	public String toString() {
		return  getSourceClassName() + "-" + name + (isMany() ? "*" : "") + "(" + type.getName()+ ")";
	}
}

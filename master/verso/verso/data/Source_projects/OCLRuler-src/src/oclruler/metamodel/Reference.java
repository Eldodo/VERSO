package oclruler.metamodel;

import org.eclipse.emf.ecore.EReference;


public class Reference extends StructuralFeature {
	
	
	public Reference(String name, Concept source, Concept type, SlotMultiplicity card) {
		super(name, source, type, card);
	}

	public Reference(EReference eReference, Concept e) {
		super(eReference, e);
	}

	@Override
	public String toString() {
		return getSourceClassName()+"." + name + (isMany() ? "*" : "") + "(" + getTypeName() + ")";
	}

	public String printJessId() {
		return (isMany() ? "$" : "") + "?id_" + name;//+this.id;
	}
	public String printJessId_Numeric(int number) {
		return (isMany() ? "$" : "") + "?id_" + number;//+this.id;
	}
		
	
}

package oclruler.metamodel;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;


public class Reference extends StructuralFeature {
	
	
	public Reference(Metamodel metamodel, EReference eReference, Concept e) {
		super(metamodel, eReference, e);
	}
	
	@Override
	public EReference getEModelElement() {
		return (EReference)super.getEModelElement();
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

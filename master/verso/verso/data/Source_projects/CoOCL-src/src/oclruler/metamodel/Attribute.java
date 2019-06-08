package oclruler.metamodel;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EModelElement;

public class Attribute extends StructuralFeature {
	
	
	public Attribute(Metamodel metamodel, EAttribute eAttribute, Concept e) {
		super(metamodel, eAttribute, e);
	}

	@Override
	public String toString() {
		return  getSourceClassName() + "-" + name + (isMany() ? "*" : "") + "(" + type.getName()+ ")";
	}
	
	@Override
	public EAttribute getEModelElement() {
		return (EAttribute)super.getEModelElement();
	}
	
	
}

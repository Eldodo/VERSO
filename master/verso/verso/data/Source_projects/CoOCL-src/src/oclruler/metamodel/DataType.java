package oclruler.metamodel;

import org.eclipse.emf.ecore.EClassifier;

public class DataType extends Concept {

	public DataType(Metamodel metamodel, String name, EClassifier ec) {
		super(metamodel, name, ec);
	}

	public boolean isEnum() {
		return false;
	}

}

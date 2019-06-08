package oclruler.metamodel;

import java.util.HashMap;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EReference;

public abstract class MMElement extends NamedEntity {
	
	protected static HashMap<String, MMElement> instances = new HashMap<>();
	protected String refInstance;
	
	protected Metamodel metamodel;
	
	public MMElement(Metamodel metamodel, String name) {
		super(name);
		this.metamodel = metamodel;
	}	
	
	public abstract EModelElement getEModelElement();
	
	public abstract String refInstance();

	public String registerInstance() {
		instances.put(refInstance(), this);
		return refInstance();
	}
	
	public Metamodel getMetamodel() {
		return metamodel;
	}

	public boolean isConcept() {
		return Metamodel.getMm1().isConcept(this) || Metamodel.getMm2().isConcept(this);
	}
	
//	public static Reference getReference(EReference er) {
//		String search = er.getEType().getName()+"#"+
//	}
}

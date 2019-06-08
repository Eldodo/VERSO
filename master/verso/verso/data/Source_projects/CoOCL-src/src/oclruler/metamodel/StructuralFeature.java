package oclruler.metamodel;


import java.util.logging.Logger;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import oclruler.metamodel.StructuralFeature.SlotMultiplicity;


public abstract class StructuralFeature extends MMElement {
	private static Logger LOGGER = java.util.logging.Logger.getLogger(StructuralFeature.class.getName());

	
	private SlotMultiplicity cardinality;
	protected Concept source;
	protected Concept type;
	
	
	protected EStructuralFeature eStructuralFeature;
	
	
	
	public StructuralFeature(Metamodel metamodel, String name, String source, Concept type, SlotMultiplicity cardinality) {
		this(metamodel, name, metamodel.getConcept(source), type, cardinality);
	}
	
	public StructuralFeature(Metamodel metamodel, EStructuralFeature esf, Concept source ) {
		this(metamodel, esf.getName(), source, metamodel.getConcept(esf.getEType().getName()), (esf.isMany()?SlotMultiplicity.multi:SlotMultiplicity.single));
		eStructuralFeature = esf;
	}
	
	public EStructuralFeature geteStructuralFeature() {
		return eStructuralFeature;
	}
	public boolean hasEStructuralFeature(){
		return eStructuralFeature != null;
	}
	
	@Override
	public EStructuralFeature getEModelElement() {
		return geteStructuralFeature();
	}

	
	public StructuralFeature(Metamodel metamodel, String name, Concept source, Concept type, SlotMultiplicity cardinality) {
		super(metamodel, name);

		this.source = source;
		if(this.source == null)
			LOGGER.warning("StructuralFeature '"+name+"' does not have Concept attached.");
		this.type = type;
		if(this.type == null)
			LOGGER.warning("Concept '"+type+"' not found. StructuralFeature '"+name+"' does not have type.");
		
		this.cardinality = cardinality;
		refInstance = registerInstance();
	}
	
	public Concept getSourceConcept() {
		return source;
	}
	
	public Concept getType() {
		return type;
	}
	
	public String getTypeName(){
		return type.getName();
	}
	
	public String getSourceClassName() {
		return source.getName();
	}
	
	public void setClassName(String className) {
		this.source = metamodel.getConcept(className);
	}
	public void setConcept(Concept c){
		this.source = c;
	}
	
	public SlotMultiplicity getCardinality() {
		return cardinality;
	}
	public boolean isMany() {
		return cardinality.equals(SlotMultiplicity.multi);
	}
	
	public boolean instanceOf(Concept cType){
		return type.instanceOf(cType);
	}
	/**
	 * 
	 * @param type
	 * @return <true> if 'type' is one of all supers of the Concept.
	 */
	public boolean isTypeOf(Concept cType){
		return type.isTypeOf(cType);
	}
	
	public boolean isNumeric() {
		return getType().isNumeric() ;
	}

	public boolean isBoolean() {
		return getType().isBoolean() ;
	}

	
	public enum SlotMultiplicity {
		// Objets directement construits
		single("SLOT"), multi("MULTISLOT");
		private String name = "";

		// Constructeur
		SlotMultiplicity(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || (getClass() != o.getClass()))
			return false;
		StructuralFeature p = (StructuralFeature)o;
//		System.out.println("StructuralFeature.equals("+getSourceConcept()+" | "+p.getSourceConcept()+")");
		if(!getName().equals(p.getName()))
			return false;
		if(!type.equals(p.type) || !source.equals(p.source) )
			return false;
		return true;
	}
	
	@Override
	public String prettyPrint(String tab) {
		return tab + name + (isMany() ? "*" : "") + "(" + getTypeName() + ")";
	}
	
	@Override
	public String simplePrint() {
		return  name + (isMany() ? "*" : "") + "(" + getTypeName() + ")";
	}
	
	public String refInstance() {
		return getSourceClassName()+"#" + name + (isMany() ? "*" : "") + "#" + getTypeName() + "";
	}

	private static Reference getReference(Metamodel metamodel, String name, Concept source, Concept type, boolean multi) {
		MMElement res = (MMElement)instances.get(source.getName()+"#"+name+ (multi ? "*" : "") + "#" + type.getName());
		if(res instanceof Reference)
			return (Reference)res;
		return null;
	}
	public static Reference getReference(Metamodel metamodel, EReference esf, Concept source, Concept type) {
		Reference res = getReference(metamodel, esf.getName(), source, type, esf.isMany());
		if(res == null)
			res = new Reference(metamodel, esf, source);
		return res;
	}
	private static Attribute getAttribute(Metamodel metamodel, String name, Concept source, Concept type, boolean multi) {
		MMElement res = (MMElement)instances.get(source.getName()+"#"+name+ (multi ? "*" : "") + "#" + type.getName());
		if(res instanceof Attribute)
			return (Attribute)res;
		return null;
	}
	public static Attribute getAttribute(Metamodel metamodel, EAttribute ea, Concept source, Concept type) {
		Attribute res = getAttribute(metamodel, ea.getName(), source, type, ea.isMany());
		if(res == null)
			res = new Attribute(metamodel, ea, source);
		return res;
	}

}

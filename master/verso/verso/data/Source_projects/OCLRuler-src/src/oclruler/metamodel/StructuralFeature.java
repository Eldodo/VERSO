package oclruler.metamodel;


import java.util.logging.Logger;

import org.eclipse.emf.ecore.EStructuralFeature;


public abstract class StructuralFeature extends MMElement {
	private static Logger LOGGER = java.util.logging.Logger.getLogger(StructuralFeature.class.getName());

	
	private SlotMultiplicity cardinality;
	protected Concept source;
	protected Concept type;
	
	protected EStructuralFeature eStructuralFeature;
	
	
	public StructuralFeature(String name, String source, Concept type, SlotMultiplicity cardinality) {
		this(name, Metamodel.getConcept(source), type, cardinality);
	}
	
	public StructuralFeature(EStructuralFeature esf, Concept source ) {
		this(esf.getName(), source, Metamodel.getConcept(esf.getEType().getName()), (esf.isMany()?SlotMultiplicity.multi:SlotMultiplicity.single));
		eStructuralFeature = esf;
	}
	
	public EStructuralFeature geteStructuralFeature() {
		return eStructuralFeature;
	}
	public boolean hasEStructuralFeature(){
		return eStructuralFeature != null;
	}

	
	public StructuralFeature(String name, Concept source, Concept type, SlotMultiplicity cardinality) {
		super(name);
		this.source = source;
		if(this.source == null)
			LOGGER.warning("StructuralFeature '"+name+"' does not have Concept attached.");
		this.type = type;
		if(this.type == null){
			LOGGER.warning("Concept '"+type+"' not found. StructuralFeature '"+name+"' does not have type.");
//			throw new IllegalArgumentException("here"); // Debug
		}
		
		this.cardinality = cardinality;
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
		this.source = Metamodel.getConcept(className);
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
	
	@Override
	public String getFullName() {
		return source.getFullName()+NAME_SEPARATOR+getName();
	}
}

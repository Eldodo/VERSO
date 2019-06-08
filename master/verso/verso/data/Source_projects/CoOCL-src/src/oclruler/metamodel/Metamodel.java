package oclruler.metamodel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import oclruler.metamodel.MetamodelMerger.DIFF_TYPE;
import oclruler.metamodel.StructuralFeature.SlotMultiplicity;
import sun.reflect.generics.repository.GenericDeclRepository;
import utils.Config;
import utils.Utils;

public class Metamodel {
	public final static Logger LOGGER = Logger.getLogger(Metamodel.class.getName());
	private  String ROOT_CLASS = "O";

	
	private static boolean FLATTEN_INHERITAGE = true;
	
	private  HashMap<EClass, HashSet<EClass>> inheritage;
	private  HashMap<String, HashSet<String>> inheritageNames;
	private  HashMap<String, HashSet<String>> inheritageAllSuperNames;
	private  HashMap<String, HashSet<String>> inheritageDirectParentNames;

	
	private  HashMap<String, Concept> 	concepts;
	private  HashMap<String, Enum> 		enums;
	private  HashMap<String, Reference> references;
	private  ArrayList<Reference> 		allReferences;
	private  HashMap<String, Attribute> attributes;
	private ArrayList<Attribute> 		allAttributes;

	public  ResourceSetImpl  	resourceSet;
	private  Resource 			metamodelResource;
	
	
	/**	 * <code>EString</code> encapsulation	 */
	public Concept String;
	/**	 * <code>EBoolean</code> encapsulation	 */
	public Concept Boolean;
	/**	 * <code>EFloat</code> encapsulation	 */
	public Concept Float;
	/**	 * <code>EDouble</code> encapsulation	 */
	public Concept Double;
	/**	 * <code>EInt</code> and <code>EInteger</code> encapsulation	 */
	public Concept Integer;
	
	private String name;
	
	public Metamodel(String name, File file) {
		this.ecoreFile = file;
		this.name = name;
	}
	
	static ArrayList<Metamodel> instances;
	static Metamodel mm1, mm2;
	
	public static void init() {
		init(null);
	}
	
	public static void init(String nameMM1) {
		if(nameMM1 != null)
			Config.METAMODEL_NAME = nameMM1;
		
		
		
 		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> map = reg.getExtensionToFactoryMap();
        map.put("xmi",   new XMIResourceFactoryImpl());
        map.put("ecore", new XMIResourceFactoryImpl());
        
		mm2 = new Metamodel(Config.METAMODEL_NAME+"-2", new File(Config.DIR_METAMODELS+Config.METAMODEL_NAME+"-2.ecore"));
		mm2.buildMetamodelResource();
		mm2.inheritageNames = mm2.mapInheritance();
		mm2.concepts = new HashMap<>();
        mm2.enums = new HashMap<>();
        mm2.references = new HashMap<String, Reference>();
        mm2.attributes = new HashMap<String, Attribute>();
        
        mm2.buildConceptsAndReferences();
        LOGGER.fine("Metamodel 2 : ");
        LOGGER.fine("   - Packages : "+mm2.ePackages());
        LOGGER.fine("   - Concepts : "+mm2.concepts.keySet());
        LOGGER.fine("       - Enum : "+mm2.enums.keySet());
        LOGGER.fine(" - References : "+mm2.references.keySet());
        LOGGER.fine(" - Attributes : "+mm2.attributes.keySet());
        
        
		mm1 = new Metamodel(Config.METAMODEL_NAME, new File(Config.DIR_METAMODELS+Config.METAMODEL_NAME+".ecore"));
        mm1.buildMetamodelResource();
        mm1.inheritageNames = mm1.mapInheritance();
        mm1.concepts = new HashMap<>();
        mm1.enums = new HashMap<>();
        mm1.references = new HashMap<String, Reference>();
        mm1.attributes = new HashMap<String, Attribute>();
        
        mm1.buildConceptsAndReferences();

        LOGGER.fine("Metamodel 1 : ");
        LOGGER.fine("   - Packages : "+mm1.ePackages());
        LOGGER.fine("   - Concepts : "+mm1.concepts.keySet());
        LOGGER.fine("       - Enum : "+mm1.enums.keySet());
        LOGGER.fine(" - References : "+mm1.references.keySet());
        LOGGER.fine(" - Attributes : "+mm1.attributes.keySet());

        instances = new ArrayList<>(2);
        instances.add(mm1);
        instances.add(mm2);
       
        
	}
	public  File getEcoreFile() {
		return ecoreFile;
	}
	File ecoreFile;
	
	/**
	 * Load metamodel Ecore file from Config.DIR_METAMODELS.<br/>
	 * Create a ResourceSet and load the ECore resource,<br/>
	 * Register packages.
	 */
	private void buildMetamodelResource(){
		resourceSet = new ResourceSetImpl();
		
		if(ecoreFile == null || !ecoreFile.exists()) {
			LOGGER.severe("Metamodel file not found : '"+ecoreFile.getAbsolutePath()+"'");
			System.exit(1);
		}
			
		URI fileURI = URI.createFileURI(ecoreFile.getAbsolutePath());
		setMetamodelResource(resourceSet.createResource(fileURI));
	    try {
			getMetamodelResource().load(null);
			LOGGER.fine("Loading metamodel : \t"+fileURI+"... Loaded !");
		} catch (IOException e) {
			LOGGER.severe(" !! Loading metamodel : \t"+fileURI+"... Failure ! Unable to load file.  !!");
			e.printStackTrace();
			System.exit(1);
		}
	    
	    //Registering metamodel packages
	    String ps = "Packages : {";
	    for (EPackage ep : ePackages()) {
	    	if(rootPackage == null) rootPackage = ep;
	    	ps += ep.getName()+" ";
		    resourceSet.getPackageRegistry().put(ep.getNsURI(), ep);
	    }
	    LOGGER.config(ps.trim() + "}");
	}
	
	EPackage rootPackage = null;
	public EPackage getRootPackage() {
		return rootPackage;
	}
	/**
	 * 
	 * @param e
	 * @return <code>[idem HashMap]</code>the previous value associated with key, or null if there was no mapping for key. (A null return can also indicate that the map previously associated null with key.)
	 */
	public Concept addConcept(Concept e){
		return concepts.put(e.getName(), e);
	}
	
	public Enum addEnum(Enum e){
		return enums.put(e.getName(), e);
	}
	
	/**
	 * 
	 * @return metamodel's concepts (except <code>EInt, EString, EDouble, EFloat </code>and<code> EBoolean</code>)
	 */
	public HashMap<String, Concept> getConcepts() {
		return concepts;
	}
	
	public boolean isConcept(MMElement mme){
		return getConcepts().containsKey(mme.getName());
	}
	
	public ArrayList<Concept> instantiableConcept;
	/**
	 * 
	 * @return ALL CONCEPTS - No restriction.
	 */
	public Collection<Concept> getInstantiableConcept(){
		return getConcepts().values();
//		if(instantiableConcept == null) {
//			instantiableConcept = new ArrayList<Concept>();
//			for (Concept concept : Metamodel.getConcepts().values()) {
//				if(!concept.getInstatiablePatterns().isEmpty())
//					instantiableConcept.add(concept);
//			}
//		}
//		return instantiableConcept;
	}
	
	public HashMap<DIFF_TYPE, ArrayList<Concept>> diffConcepts(Metamodel mm2) {
		HashMap<DIFF_TYPE, ArrayList<Concept>> dif = new HashMap<>();
		for (DIFF_TYPE dt : DIFF_TYPE.values()) 
			dif.put(dt, new ArrayList<>());
		
		
		ArrayList<Concept> adds = new ArrayList<>();
		for (Concept concept : getConcepts().values()) {
			if(!mm2.getConcepts().values().contains(concept) && !adds.contains(concept)){
				adds.add(concept);
			}
		}
		dif.put(DIFF_TYPE.REMOVE, adds);// elements not exist in mm2
		adds = new ArrayList<>();
		for (Concept concept : mm2.getConcepts().values()) {
			if(!getConcepts().values().contains(concept) && !adds.contains(concept))
				adds.add(concept);
		}
		dif.put(DIFF_TYPE.ADD, adds);
		return dif;
	}
	
	public HashMap<DIFF_TYPE, ArrayList<StructuralFeature>> diffStructuralReferences(Metamodel mm2) {
		HashMap<DIFF_TYPE, ArrayList<StructuralFeature>> diffSFsAll = new HashMap<>();
		for (DIFF_TYPE dt : DIFF_TYPE.values()) 
			diffSFsAll.put(dt, new ArrayList<>());
		
		for (Concept c : getConcepts().values()) {
			Concept c2 = mm2.getConcept(c.getName());
			if(c2 != null){
				HashMap<DIFF_TYPE, ArrayList<StructuralFeature>> diffSFs = c.diffStructuralFeatures(c2);
				for (DIFF_TYPE dt : diffSFs.keySet()) {
					diffSFsAll.get(dt).addAll(diffSFs.get(dt));
				}
			}
		}
		return diffSFsAll;
	}


	
	public HashMap<String, Enum> getEnums() {
		return enums;
	}
	
	public Concept getEnum(String name) {
		return enums.get(name);
	}
	
	public Concept getConcept(String name) {

		if(name.equals("EInt")) name = "EInteger";
		switch (name) {
		case "EInteger":
			return Integer;
		case "EString":
			return String;
		case "EBoolean":
			return Boolean;
		case "EFlaot":
			return Float;
		case "EDouble":
			return Double;
		}
		
		Concept res = concepts.get(name);
		if(res == null)
			LOGGER.config("Concept '"+name+"' not found in metamodel '"+getName()+"'.");
		return res;
	}
	
	/**
	 * 
	 * If type and/or m parameters is/are <code>null</code>, they are not constraint the set sfs.
	 * 
	 * @param sfs THe StructuralFeature to mine
	 * @param type THe type of the StructuralFeatures to cosider
	 * @param m THe multiplicity of the StructuralFeatures to consider
	 * @return
	 */
	public <T extends StructuralFeature> ArrayList<T> get_T_StructuralFeatures(Collection<T> sfs, Concept type, SlotMultiplicity m){
		ArrayList<T> res = new ArrayList<T>();
		for (T sf : sfs) {
			boolean typeOK = (type == null) || (type != null && sf.isTypeOf(type));
			boolean	mOK = (m == null) || (m != null && sf.getCardinality().equals(m));
			if(typeOK && mOK)
				res.add(sf);
		}
		return res;
	}
	public <T extends StructuralFeature> ArrayList<T> get_T_StructuralFeatures(Concept type, SlotMultiplicity m){
		return get_T_StructuralFeatures((Collection<T>)getStructuralFeatures().values(), type, m);
	}
	
	public HashMap<String, StructuralFeature> getStructuralFeatures() {
		HashMap<String, StructuralFeature> res = new HashMap<String, StructuralFeature>();
		res.putAll(references);
		res.putAll(attributes);
		return res;
	}
	public StructuralFeature getStructuralFeature(String name, Concept source) {
		if(source == null)
			return null;
		return source.getStructuralFeature(name);
	}
	public ArrayList<StructuralFeature> getStructuralFeatures(String name) {
		ArrayList<StructuralFeature> res = new ArrayList<>(2);
		for (Concept c : getConcepts().values()) {
			StructuralFeature sf = c.getStructuralFeature(name);
			if(sf != null)
				res.add(sf);
		}
		return res;
	}

	public StructuralFeature getRandomStructuralFeature(){
		return Utils.getRandom(getStructuralFeatures().values());
	}
	
	
	public StructuralFeature getRandomStructuralFeature(Concept type){
		ArrayList<StructuralFeature> sfsT = get_T_StructuralFeatures(getStructuralFeatures().values(), type, null);
		return Utils.getRandom(sfsT);
	}
	
	public ArrayList<Attribute> getAllAttributes() {
		if(allAttributes == null){
			allAttributes = new ArrayList<>();
			for (Concept c : getConcepts().values()) {
				for (Attribute att : c.getAttributes()) {
						allAttributes.add(att);
				}
			}
		}
		return allAttributes;
	}

	public Attribute getAttribute(String name) {
		return attributes.get(name);
	}
	public Attribute getRandomAttribute(){
		return Utils.getRandom(getAllAttributes());
	}
	public Attribute getRandomAttribute(Concept type, SlotMultiplicity m){
		ArrayList<Attribute> sfsT = get_T_StructuralFeatures(getAllAttributes(), type, m);
		return Utils.getRandom(sfsT);
	}

	public ArrayList<Reference> getAllReferences() {
		if(allReferences == null){
			allReferences = new ArrayList<>();
			for (Concept c : getConcepts().values()) {
				for (Reference reference : c.getReferences()) {
						allReferences.add(reference);
				}
			}
		}
		return allReferences;
	}
	public ArrayList<Reference> getAllReferenceswael() {
		if(allReferences == null){
			allReferences = new ArrayList<>();
			for (Concept c : getConcepts().values()) {
				for (Reference reference : c.getReferences()) {
					
						allReferences.add(reference);
					
				}
			}
		}
		return allReferences;
	}
	public Reference getRandomReference(){
		return Utils.getRandom(getAllReferences());
	}
	public Reference getRandomReference(Concept type, SlotMultiplicity m){
		ArrayList<Reference> sfsT = get_T_StructuralFeatures(getAllReferences(), type, m);
		return Utils.getRandom(sfsT);
	}

	
	@SuppressWarnings("unchecked")
	private void buildConceptsAndReferences(){
		concepts = new HashMap<>();
		enums = new HashMap<>();
		
		this.String 	= new Concept(this, "EString");
		this.Boolean	= new Concept(this, "EBoolean");
		this.Float 		= new Concept(this, "EFloat");
		this.Double 	= new Concept(this, "EDouble");
		this.Integer 	= new Concept(this, "EInteger");
//		addConcept(Metamodel.String);
//		addConcept(Metamodel.Boolean);
//		addConcept(Metamodel.Float);
//		addConcept(Metamodel.Double);
//		addConcept(Metamodel.Integer);
		
		ArrayList<EObject> list = new ArrayList<>();
		//TODO Use the resource !
		for (Iterator<EObject> it = getMetamodelResource().getAllContents(); it.hasNext();) 
			list.add((EObject) it.next());
		
		Collections.sort(list, new Comparator<EObject>(){
			@Override
			public int compare(EObject o1, EObject o2) {
				EClass ec1 = o1.eClass();
				EClass ec2 = o2.eClass();
				return ec1.getName().compareTo(ec2.getName());
			}
		});
		
//		System.out.println("Metamodel.buildConceptsAndReferences()");
//		for (EObject eObject : list) {
//			System.out.println(" - "+eObject + " - " +eObject.getClass().getName());
//		}
		
		for (EObject eo : list) {
			EClass ec = eo.eClass();
			if(ec.getName().equals("EClass")){
				Object o = (Object)eo.eGet(ec.getEStructuralFeature("name"));
				
				
				EClassifier ecClass = rootPackage.getEClassifier(((EClassifier)eo).getName());
				
				Concept e = new Concept(this, o.toString(), ecClass);
				addConcept(e);
			} else if(ec.getName().equals("EEnumLiteral")){
				Object o = (Object)eo.eGet(ec.getEStructuralFeature("name"));
//				System.out.println("EnumLiteral Found : "+ eo + " | "+o.toString());
//				o = (Object)eo.eGet(ec.getEStructuralFeature("name"));
				Object ov = (Object)eo.eGet(ec.getEStructuralFeature("value"));
				
				EnumLit eel = new EnumLit(o.toString(), (Integer)ov);
				
//				for (EStructuralFeature esf : ec.getEAllStructuralFeatures()) {
//					System.out.println(" - "+esf);
//				}
				EEnum ee = (EEnum)eo.eGet(ec.getEStructuralFeature("eEnum"));;
				
				((Enum)getConcept(ee.getName())).addLit(eel);;
				
//				System.out.println("eEnum : "+ ee.getName() +" | "+getConcept(ee.getName()));
				
			} else if(ec.getName().equals("EEnum")){
//				System.out.println("Enum Found : "+ eo);
				Object o = (Object)eo.eGet(ec.getEStructuralFeature("name"));
				Enum en = new Enum(this, o.toString(), (EClassifier)eo);
				addConcept(en);
			} else if(ec.getName().equals("EDataType")){
				Object o = (Object)eo.eGet(ec.getEStructuralFeature("name"));
				DataType dt = new DataType(this, o.toString(), (EClassifier) ec);
				addConcept(dt);
//				System.out.println("DataType Found : "+ o.toString());
			} else {
//				System.out.println("Other : "+ eo);
			}
		}		
		
		for (EObject eo : list) {
			EClass ec = eo.eClass();
			if(ec.getName().equals("EClass")){
				
				String conceptName = (String)eo.eGet(ec.getEStructuralFeature("name"));
				Concept source = getConcept(conceptName);
				
				Collection<EReference> erefs = (Collection<EReference>)eo.eGet(ec.getEStructuralFeature(FLATTEN_INHERITAGE?"eAllReferences":"eReferences"));
				for (EReference eReference : erefs) {
//					System.out.println("MM.build: "+conceptName+" -> "+eReference.getEType().getName());
					if(eReference.getEType().eIsProxy()){
						LOGGER.severe("Proxies are not supported."
								+ "\nReference ignored : '"+source.getName()+"."+eReference.getName()+"' is a proxy to "+EcoreUtil.getURI(eReference.getEType())+".");
					} else {
						Concept type = getConcept(eReference.getEType().getName());
						Reference r = StructuralFeature.getReference(this, eReference, source, type);
						
//						Reference r = new Reference(this, eReference, source);
						
						source.putReference(eReference.getName(), r);
						references.put(eReference.getName(), r);
					}
				}
				
				Collection<EAttribute> eatts = (Collection<EAttribute>)eo.eGet(ec.getEStructuralFeature(FLATTEN_INHERITAGE?"eAllAttributes":"eAttributes"));
				for (EAttribute eAttribute : eatts) {
//					System.out.println("MM.build: "+conceptName+" -> "+eAttribute.getEType().getName()+" | "+e+" | "+getConcept(eAttribute.getEType().getName()));
					if(eAttribute.getEType().eIsProxy()){
						LOGGER.severe("Proxies are not supported."
								+ "\nReference ignored : '"+source.getName()+"."+eAttribute.getName()+"' is a proxy to "+EcoreUtil.getURI(eAttribute.getEType())+".");
					} else {
//						Attribute a = new Attribute(this, eAttribute, source);
						Concept type = getConcept(eAttribute.getEType().getName());
						Attribute a =  StructuralFeature.getAttribute(this, eAttribute, source, type);
						source.putAttribute(eAttribute.getName(), a);
						attributes.put(eAttribute.getName(), a);
					}
				}
			} else {
				
			}
			
		}
	}

	private String jessMetamodel;
	public String printJessMetamodel( ) {
		if(jessMetamodel != null)
			return jessMetamodel;
		String res = ";METAMODEL : "+Config.METAMODEL_NAME+"\n";
		
		res += "; JESS TOOLING"+
				 "\n(deftemplate Polymorphem " +
				 "\n  (slot ClassName)" +
				 "\n  (multislot Parents)" +
				 "\n  (multislot Descendants)" +
				 "\n)" +
				 "\n(deftemplate Found " +
				 "\n  (slot name)" +
				 "\n  (multislot values)" +
				 "\n)" +
				 "\n(deftemplate O  " +
				 "\n  (slot ID_Jess)" +
				 "\n  (slot CLASSNAME_Jess)" +
				 "\n  (multislot PARENTS_Jess)" +
				 "\n)\n";

		
		ArrayList<Concept> cs = new ArrayList<>();
		for (Concept concept : getConcepts().values()) {
			for (Concept sup : concept.getSupers()) 
				if(!cs.contains(sup)) cs.add(sup);
			if(!cs.contains(concept)) cs.add(concept);
		}
//		System.out.println("Metamodel.printJessMetamodel()");
//		for (Concept concept : cs) {
//			System.out.println(" - "+concept);
//		}

		for (Concept c : cs) {
			res += "(deftemplate "+c.getName();
			int iSup = 0;
			for (Concept ecSUper : c.getSupers()) 
				res += ((iSup++==0)?" extends ":" ")+ ecSUper.getName() +" ";
			if(iSup == 0) res += " extends "+ROOT_CLASS+" ";
			res += "\n";	
			for (StructuralFeature esf : c.getStructuralFeatures()) {
				String numberType =  getJessTypeage(esf);	
				res += "  ("+ (esf.isMany()?"multi":"")+  "slot "+esf.getName() +" "+numberType+")\n";//+" : "+esf.getEType().getName() +")\n";//TYPAGE ?
			}
			res += ")\n";
		}
		
		
//		for (EPackage ep : Metamodel.ePackages()) {
//			res += "; PACKAGE "+ep.getName()+"\n";
//			for (Iterator<EObject> it = ep.eAllContents(); it.hasNext();) {
//				EObject eo = (EObject) it.next();
//				if(eo instanceof EClass){
//					EClass ec = (EClass)eo;
//					res += "(deftemplate "+ec.getName();//+ " : "+ec.getEAllStructuralFeatures());
//					int iSup = 0;
//					for (EClass ecSUper : ec.getESuperTypes()) 
//						res += ((iSup++==0)?" extends ":" ")+ ecSUper.getName() +" ";
//					if(iSup == 0) res += " extends "+ROOT_CLASS+" ";
//					res += "\n";	
//					
//					if(verboseInheritedFeature){
//						res += "  (slot "+Model.ID_JESS+")\n";
//						res += "  (slot "+Model.CLASSNAME_JESS+")\n";//Class originale statique
////						res += "  (multislot "+Model.PARENTS_JESS+")\n" ;
//					} 
//					for (EStructuralFeature esf : ec.getEStructuralFeatures()) {
//						String numberType =  getType(esf);	/* TODO Typage ? */
//						res += "  ("+ ((esf.isMany()||multi.equals(AllMultiSlot.ALL_MULTI))?"multi":"")+  "slot "+esf.getName() +" "+numberType+")\n";//+" : "+esf.getEType().getName() +")\n";//TYPAGE ?
//					}
//					res += ")\n";
//				}
//			}
//			res += "; end Package "+ep.getName()+"\n";
			
//		}
		res += jessInheritance;
		res += "; end METAMODEL : "+Config.METAMODEL_NAME;
		
		
//		res += Utils.printJessInheritanceStructure();
//		res += Utils.printJessInheritanceFacts();
		//res += "\n"+Utils.printJessPrimitives();
		jessMetamodel = res +"\n";
		return jessMetamodel;
	}
	
	
	public static String getJessTypeage(StructuralFeature esf){
		String res = "";
		if( esf.getType().getName().equalsIgnoreCase("eint") || esf.getType().getName().equalsIgnoreCase("einteger"))
			res += "(type INTEGER)";
		else if( esf.getType().getName().equalsIgnoreCase("edouble") || esf.getType().getName().equalsIgnoreCase("efloat"))
			res += "(type FLOAT)";
		else if( esf.getType().getName().equalsIgnoreCase("elong") )
			res += "(type LONG)";
		return res;
	}
	
	String jessInheritance;

	Set<EPackage> ePackages = null;
	public Set<EPackage> ePackages() {
		if(ePackages == null){
			ePackages = new HashSet<EPackage>();
			for (Iterator<EObject> it = getMetamodelResource().getAllContents(); it.hasNext();) {
				EObject eObject = (EObject) it.next();
				if (eObject instanceof EPackage) {
					ePackages.add((EPackage) eObject);
				}
			}
		}
		return (ePackages);
		
	}
	
	public HashMap<String, HashSet<String>> getInheritageAllSuperNames() {
		return inheritageAllSuperNames;
	}
	public HashMap<String, HashSet<String>> getDirectInheritageParentNames() {
		return inheritageDirectParentNames;
	}

	public HashMap<String, HashSet<String>> mapInheritance(){
		inheritageAllSuperNames = new HashMap<String, HashSet<String>>();
		inheritageDirectParentNames = new HashMap<String, HashSet<String>>();
		inheritage = new HashMap<EClass, HashSet<EClass>>();
		
		//First : references classes
		TreeIterator<EObject> eAllContents = getMetamodelResource().getAllContents();
		while (eAllContents.hasNext()) {
			EObject ecf = eAllContents.next();
			if(ecf instanceof EClass){
				EClass eci = (EClass) ecf;
				inheritage.put(eci, new HashSet<EClass>());	
				inheritageAllSuperNames.put(eci.getName(), new HashSet<String>());
				inheritageDirectParentNames.put(eci.getName(), new HashSet<String>());
				if(!eci.isAbstract()) {
					inheritage.get(eci).add(eci);
				}
			} 
		}
		
		
		/*
		 * EALL PARENTS (ASCENDANTS)
		 */
		for (EClass ec : inheritage.keySet()) {
			for (EClass sup : ec.getEAllSuperTypes()) {
				if(!ec.isAbstract())
					inheritage.get(sup).add(ec);
				inheritageAllSuperNames.get(ec.getName()).add(sup.getName());		
			}
		}
		/*
		 * DIRECT PARENTS
		 */
		for (EClass ec : inheritage.keySet()) {
			for (EClass sup : ec.getESuperTypes()) {
				inheritageDirectParentNames.get(ec.getName()).add(sup.getName());
			}
		}

		if(LOGGER.isLoggable(Level.FINER)){
			String log = "Inheritage :\n";
			log += Utils.printMultimapEClasses(inheritage, "    - ");
			log += "\nParents :\n";
			for (String hs : inheritageAllSuperNames.keySet()) {
				log += hs+" : "+ inheritageAllSuperNames.get(hs) + "\n";
			}
			LOGGER.finer(log);
		}
		inheritageNames = new HashMap<>();
		for (EClass ec : inheritage.keySet()) {
			inheritageNames.put(ec.getName(), new HashSet<String>());
			for (EClass ec2 : inheritage.get(ec)) {
				inheritageNames.get(ec.getName()).add(ec2.getName());
			}
		}
		
		jessInheritance = "(deffacts Inheritance_"+Config.METAMODEL_NAME;
		for (String  name : inheritageNames.keySet()) {
			jessInheritance += "\n   (Polymorphem (ClassName "+name+")";
			
			//Parents
			if(inheritageAllSuperNames.get(name) != null && !inheritageAllSuperNames.get(name).isEmpty()){
				jessInheritance += " (Parents ";
				for (String p : inheritageAllSuperNames.get(name)) 
					jessInheritance += p+" ";
				jessInheritance += ")";
			}
			//Descendants
			if(inheritageNames.get(name) != null && !inheritageNames.get(name).isEmpty()){
				jessInheritance += " (Descendants ";
				for (String p : inheritageNames.get(name)) 
					jessInheritance += p+" ";
				jessInheritance += ")";
			}
			jessInheritance += " )";
		}
		jessInheritance += "\n)";
		
		return inheritageNames;
	}
	
	public HashMap<EClass, HashSet<EClass>> getInheritage() {
		return inheritage;
	}
	
	public void setInheritageNames(HashMap<String, HashSet<String>> inheritage) {
		inheritageNames = inheritage;
	}
	
	public HashMap<String, HashSet<String>> getInheritageNames() {
		return inheritageNames;
	}
	public Resource getMetamodelResource() {
		return metamodelResource;
	}
	public void setMetamodelResource(Resource metamodelResource) {
		this.metamodelResource = metamodelResource;
	}

	public static Metamodel getMm1() {
		return mm1;
	}

	public static Metamodel getMm2() {
		return mm2;
	}

	public String getName() {
		return name;
	}

	public Concept getRandomConcept() {
		return Utils.getRandom(getConcepts().values());
	}
	
	public static Concept getConceptFromMetamodels(String name){
		Concept res = null;
		for (Metamodel metamodel : instances) {
			if( (res = metamodel.getConcept(name)) != null)
				return res;
		}
		return res;
	}
//	public static Concept getStructuralFeatureFromMetamodels(Concept c, String name){
//		Concept res = null;
//		for (Metamodel metamodel : instances) {
//			if( (res = metamodel.getConcept(name)) != null)
//				return res;
//		}
//		return res;
//	}
	
}

package oclruler.metamodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import oclruler.metamodel.Model.AllMultiSlot;
import oclruler.metamodel.StructuralFeature.SlotMultiplicity;
import oclruler.rule.patterns.Pattern;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;
import partitioner.partition.PartitionModel;
import partitioner.partition.composition.AllRangesFragmentSet;
import partitioner.partition.composition.FragmentSet;

public class Metamodel {
	public final static Logger LOGGER = Logger.getLogger(Metamodel.class.getName());
	private static String ROOT_CLASS = "O";
	
	/**
	 * File containing MMElement (Concepts, References, and Attributes) to dismiss during pattern instantiation.
	 */
	public static File COV_DEF_FILE;

	/**
	 * File containing the metamodel (in Ecore).
	 */
	static File ecoreFile;
	static EPackage rootPackage = null;
	static Set<EPackage> ePackages = null;

	private static HashMap<EClass, HashSet<EClass>> inheritage;
	private static HashMap<String, HashSet<String>> inheritageNames;
	private static HashMap<String, HashSet<String>> inheritageAllSuperNames;
	private static HashMap<String, HashSet<String>> inheritageDirectParentNames;

	
	private static HashMap<String, Concept> 	concepts;
	private static HashMap<String, Enum> 		enums;
	private static HashMap<String, Reference> 	references;
	private static ArrayList<Reference> 		allReferences;
	private static HashMap<String, Attribute> 	attributes;
	private static ArrayList<Attribute> 		allAttributes;
	
	
	/**
	 * MMElement (Concepts, References, and Attributes) to dismiss during pattern instantiation.
	 */
	private static ArrayList<MMElement> 		dismissedElements;
	private static ArrayList<MMElement> 		authorizedElements;
	private static ArrayList<Concept> 			authorizedConcepts;

	public static ResourceSetImpl  	resourceSet;
	public static Resource 			metamodelResource;
	
	
	public static FragmentSet fragmentSet = null;
	
	/**	 * <code>EString</code> encapsulation	 */
	public static Concept String;
	/**	 * <code>EBoolean</code> encapsulation	 */
	public static Concept Boolean;
	/**	 * <code>EFloat</code> encapsulation	 */
	public static Concept Float;
	/**	 * <code>EDouble</code> encapsulation	 */
	public static Concept Double;
	/**	 * <code>EInt</code> and <code>EInteger</code> encapsulation	 */
	public static Concept Integer;
	
	/**
	 * 
	 * @param pathName
	 * @return MMElement at the end of the path (see {@link Path#getPath(String)})
	 */
	public static MMElement getMMElement(String pathName){
		Path p = Path.getPath(pathName);
		return p != null ? p.getEnd() : null;
	}
	
	/**
	 * 
	 * @param namedElement
	 * @return Corresponding MMElement
	 */
	public static MMElement getMMElement(ENamedElement namedElement){
		MMElement res = null;
		if(namedElement instanceof EClass)
			res = getConcept(namedElement.getName());
		
		else if(namedElement instanceof EStructuralFeature)
			res = getStructuralFeature((EStructuralFeature)namedElement);
		
		else if(namedElement instanceof EEnum)
			res = getEnum(namedElement.getName());
		
		else System.out.println("     AIE !!!\n     "+namedElement.eClass().getName()+"."+namedElement+"\n     !!! AIE");
		
		return res;
	}
	
	/**
	 * 
	 * @param covDefFile File with coverage definition (comment lines start with "--")
	 * @return A list of MMElement (Elements that will NOT be considered during match computation of PatternType (see {@link Pattern#getMatches(Class) pattern matching} depending on PatternType values)
	 */
	public static ArrayList<MMElement> readCovDefinitionFile(File covDefFile){
		ArrayList<MMElement> res = new ArrayList<>();
		String log = "Will be evicted list:";
		if(covDefFile == null || !covDefFile.exists()){
			res = new ArrayList<>(0);
			log =  "Coverage definition: file not found '"+covDefFile+"'";
		} else {
			try {
				BufferedReader br = new BufferedReader(new FileReader(covDefFile));
				String s = null;
				String sFile = "";
				while( (s = br.readLine()) != null){
					if(!s.trim().startsWith("--") && !s.trim().startsWith("#"))
						sFile += s.trim() + "\n";
				}
				
				for (String s2 : sFile.split("\n")) {
					MMElement mme = getMMElement(s2);
					if(mme != null){
						res.add(mme);
						log += "\n - '"+mme.getFullName()+"'";
					} else {
						log += "\n - '"+s2+"' is not a valid concept name.";
					}
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Config.COV_DEF_LOGGER.config(log);
			
		}
		return res;
	}
	
	/**
	 * Initialisation:
	 * <ol>
	 * 		<li>Load the {@link Resource resource} and prepare {@link ResourceSet resource set}</li>
	 * 		<li>Build Concept and StructuralFeatures</li>
	 * 		<li>Load coverage definition (see {@link #COV_DEF_FILE} and {@link Metamodel#readCovDefinitionFile(File)})</li>
	 * </ol>
	 */
	public static void init() {
 		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> map = reg.getExtensionToFactoryMap();
        map.put("xmi",   new XMIResourceFactoryImpl());
        map.put("ecore", new XMIResourceFactoryImpl());
        
        registerMetamodelResource();
        inheritageNames = Metamodel.mapInheritance(metamodelResource);
        concepts = new HashMap<>();
        enums = new HashMap<>();
        references = new HashMap<String, Reference>();
        attributes = new HashMap<String, Attribute>();
        
        registerConceptsAndStructuralFeatures();
        dismissedElements = new ArrayList<>(0);
        authorizedElements = new ArrayList<>(concepts.size()+references.size()+attributes.size());
        authorizedElements.addAll(concepts.values());
        authorizedElements.addAll(references.values());
        authorizedElements.addAll(attributes.values());
        
        authorizedConcepts =  new ArrayList<>(concepts.values());
        
        
        //COV definition file is on work, only concept are considered so far
        String covDevFileName = Config.getStringParam("COV_DEF_FILE");
        if(covDevFileName == null && COV_DEF_FILE == null)
        	LOGGER.config("Coverage definition file (COV_DEV_FILE) not specified in config file.");
        else {
        	
	        File f = COV_DEF_FILE==null? new File(covDevFileName):COV_DEF_FILE; // COV_DEV_FILE might have been passed as args option 'confg-file'
	        if(!f.exists())
	        	LOGGER.warning("Coverage definition file (COV_DEV_FILE) not found : '"+f.getAbsolutePath()+"'"); 
	        else{
	        	COV_DEF_FILE = f;
	        	dismissedElements = readCovDefinitionFile(f);
	        	for (MMElement mm : dismissedElements) {
					authorizedElements.remove(mm);
					if(mm instanceof Concept)
						authorizedConcepts.remove((Concept)mm);
					//to do StructuralFeature eviction
					
//					else if(mm instanceof Reference)
//						authorizededReferences.remove((Reference)mm);
//					else if(mm instanceof Attribute)
//						authorizededAttributes.remove((Attribute)mm);
	        	}
//				if(evictedElements.size()+authorizededElements.size() != (concepts.keySet().size()+references.keySet().size()+attributes.keySet().size())){
//					LOGGER.severe("Eviction using coverage definition unstable !!!");
//					LOGGER.severe("Total number of elements: "+(concepts.keySet().size()+references.keySet().size()+attributes.keySet().size()));
//					LOGGER.severe("                 Evicted: "+evictedElements);
//					LOGGER.severe("              Authorized: "+authorizededElements);
//					System.err.println("Exit.");
//					System.exit(1);
//				}
	        }
        }
        
        
        PartitionModel partitionModel;
		partitionModel = new PartitionModel();
		partitionModel.extractPartition();
		fragmentSet = new AllRangesFragmentSet(partitionModel);
//		fragmentSet = new AllPartitionsFragmentSet(partitionModel);
        
		
        LOGGER.fine("             -- Metamodel -- ");
        LOGGER.fine("         - Packages : "+ePackages());
        LOGGER.fine("         - Concepts : "+concepts.keySet());
        LOGGER.fine("             - Enum : "+enums.keySet());
        LOGGER.fine("       - References : "+references.keySet());
        LOGGER.fine("       - Attributes : "+attributes.keySet());
        LOGGER.fine(" - Evicted Elements : "+dismissedElements);
	}
	
	public static FragmentSet getFragmentSet() {
		return fragmentSet;
	}
	
	public static File getEcoreFile() {
		return ecoreFile;
	}
	
	
	/**
	 * <ol>
	 * 	  <li>Load metamodel Ecore file from {@link Config#DIR_METAMODELS Config.DIR_METAMODELS}</li>
	 *    <li>Create a ResourceSet and load the ECore resource</li>
	 *    <li>Register packages</li>
	 * </ol>
	 */
	private static void registerMetamodelResource(){
		resourceSet = new ResourceSetImpl();
		ecoreFile = new File(Config.DIR_METAMODELS+Config.METAMODEL_NAME+".ecore");
		if(ecoreFile == null || !ecoreFile.exists()) {
			LOGGER.severe("Metamodel file not found : '"+ecoreFile.getAbsolutePath()+"'");
			System.exit(1);
		}
			
		URI fileURI = URI.createFileURI(ecoreFile.getAbsolutePath());
		metamodelResource =  resourceSet.createResource(fileURI);
	    try {
			metamodelResource.load(null);
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
	    LOGGER.fine(ps.trim() + "}");
	}
	
	public static EPackage getRootPackage() {
		return rootPackage;
	}
	
	public static Set<EPackage> ePackages() {
		if(ePackages == null){
			ePackages = new HashSet<EPackage>();
			for (Iterator<EObject> it = metamodelResource.getAllContents(); it.hasNext();) {
				EObject eObject = (EObject) it.next();
				if (eObject instanceof EPackage) {
					ePackages.add((EPackage) eObject);
				}
			}
		}
		return (ePackages);
		
	}

	/**
	 * 
	 * @param e
	 * @return <code>[idem HashMap]</code>the previous value associated with key, or null if there was no mapping for key. (A null return can also indicate that the map previously associated null with key.)
	 */
	public static Concept addConcept(Concept e){
		return concepts.put(e.getName(), e);
	}
	
	public static Enum addEnum(Enum e){
		return enums.put(e.getName(), e);
	}
	
	/**
	 * 
	 * @return metamodel's concepts (except <code>EInt, EString, EDouble, EFloat </code>and<code> EBoolean</code>)
	 */
	public static HashMap<String, Concept> getAllConcepts() {
		return concepts;
	}
	
	
	public static ArrayList<Concept> getAuthorizedConcepts() {
		return authorizedConcepts;
	}
	
	
	public static Collection<Concept> getUnInstantiableConcept(){
		ArrayList<Concept> res = new ArrayList<Concept>();
			for (Concept concept : Metamodel.getAllConcepts().values()) {
				if(concept.getInstatiablePatterns().isEmpty())
					res.add(concept);
			}
		return res;
	}

	public static HashMap<String, Enum> getEnums() {
		return enums;
	}
	
	public static Concept getEnum(String name) {
		return enums.get(name);
	}
	
	/**
	 * Attention : specific for <code>RoyalAndLoyal</code> Metamodel : "E[Type]Object" types rename "E[Type]"
	 * @param name
	 *            of the Concept
	 * @return <code>null</code> if there is no Concept with the specified name.
	 */
	public static Concept getConcept(String name) {
		
		if(Config.METAMODEL_NAME.equals("RoyalAndLoyal") && name.endsWith("Object") && !name.equalsIgnoreCase("Object")){
			name = name.substring(0, name.length()-"object".length());
		}
		

		if (name.equals("EInt"))
			name = "EInteger";
		
		switch (name) {
		case "EInteger":
			return Integer;
		case "EString":
			return String;
		case "EBoolean":
			return Boolean;
		case "EFloat":
			return Float;
		case "EDouble":
			return Double;
		}

		Concept res = concepts.get(name);
		// if(res == null)
		// LOGGER.config("Concept '"+name+"' not found.");
		return res;
	}

	/**
	 * Select StructuralFeatures in the collection passed in parameter that satisfy type and cardinality.<br/>
	 * Type and cardinality can be null - meaning respectively no restriction on Type and cardinality.
	 * 
	 * @param sfCollection
	 * @param type
	 * @param cardinality
	 * @return List of StructuralFeatures in the collection <code>sfCollection</code> that satisfy type and cardinality.
	 */
	public static <T extends StructuralFeature> ArrayList<T> get_T_StructuralFeatures(Collection<T> sfCollection, Concept type, SlotMultiplicity cardinality) {
		ArrayList<T> res = new ArrayList<T>();
		for (T sf : sfCollection) {
			boolean typeOK = (type == null) || (type != null && sf.isTypeOf(type));
			boolean mOK = (cardinality == null) || (cardinality != null && sf.getCardinality().equals(cardinality));
			if (typeOK && mOK)
				res.add(sf);
		}
		return res;
	}

	public static HashMap<String, StructuralFeature> getStructuralFeatures() {
		HashMap<String, StructuralFeature> res = new HashMap<String, StructuralFeature>();
		res.putAll(references);
		res.putAll(attributes);
		return res;
	}

	/**
	 * 
	 * @param name
	 *            of the StructuralFeature
	 * @return <code>null</code> if there is no StructuralFeature with the specified name.
	 */
	public static StructuralFeature getStructuralFeature(String name) {
		StructuralFeature res = references.get(name);
		if (res == null)
			res = attributes.get(name);
		return res;
	}
	
	/**
	 * 
	 * @param name
	 *            of the StructuralFeature
	 * @return <code>null</code> if there is no StructuralFeature with the specified name.
	 */
	public static StructuralFeature getStructuralFeature(EStructuralFeature esf) {
		Concept source = getConcept(esf.getEContainingClass().getName());
		return source.getStructuralFeature(esf.getName());
	}

	public static StructuralFeature getRandomStructuralFeature() {
		return ToolBox.getRandom(getStructuralFeatures().values());
	}

	public static StructuralFeature getRandomStructuralFeature(Concept type) {
		ArrayList<StructuralFeature> sfsT = get_T_StructuralFeatures(Metamodel.getStructuralFeatures().values(), type, null);
		return ToolBox.getRandom(sfsT);
	}

	public static ArrayList<Attribute> getAllAttributes() {
		if (allAttributes == null) {
			allAttributes = new ArrayList<>();
			for (Concept c : getAllConcepts().values()) {
				for (Attribute att : c.getAttributes()) {
					allAttributes.add(att);
				}
			}
		}
		return allAttributes;
	}

	public static Attribute getAttribute(String name) {
		return attributes.get(name);
	}

	public static Attribute getRandomAttribute() {
		return ToolBox.getRandom(getAllAttributes());
	}

	public static Attribute getRandomAttribute(Concept type, SlotMultiplicity m) {
		ArrayList<Attribute> sfsT = get_T_StructuralFeatures(Metamodel.getAllAttributes(), type, m);
		return ToolBox.getRandom(sfsT);
	}

	public static ArrayList<Reference> getAllReferences() {
		if(allReferences == null){
			allReferences = new ArrayList<>();
			for (Concept c : Metamodel.getAllConcepts().values()) {
				for (Reference reference : c.getReferences()) {
					allReferences.add(reference);
				}
			}
		}
		return allReferences;
	}
	
	public static Reference getReference(String name) {
		return references.get(name);
	}

	public static Reference getRandomReference() {
		return ToolBox.getRandom(getAllReferences());
	}

	public static Reference getRandomReference(Concept type, SlotMultiplicity m) {
		ArrayList<Reference> sfsT = get_T_StructuralFeatures(Metamodel.getAllReferences(), type, m);
		return ToolBox.getRandom(sfsT);
	}

	/**
	 * Parse the resource and register Concept and StructuralFeatures.<br/>
	 * <i>Basic types are added manually.</i>
	 */
	@SuppressWarnings("unchecked")
	private static void registerConceptsAndStructuralFeatures(){
		concepts = new HashMap<>();
		enums = new HashMap<>();
		
		Metamodel.String 	= new Concept("EString");
		Metamodel.Boolean	= new Concept("EBoolean");
		Metamodel.Float 	= new Concept("EFloat");
		Metamodel.Double 	= new Concept("EDouble");
		Metamodel.Integer 	= new Concept("EInteger");
//		addConcept(Metamodel.String);
//		addConcept(Metamodel.Boolean);
//		addConcept(Metamodel.Float);
//		addConcept(Metamodel.Double);
//		addConcept(Metamodel.Integer);
		
		ArrayList<EObject> list = new ArrayList<>();
		// Use the resource !
		for (Iterator<EObject> it = metamodelResource.getAllContents(); it.hasNext();) 
			list.add((EObject) it.next());
		
		Collections.sort(list, new Comparator<EObject>(){
			@Override
			public int compare(EObject o1, EObject o2) {
				EClass ec1 = o1.eClass();
				EClass ec2 = o2.eClass();
				return ec1.getName().compareTo(ec2.getName());
			}
		});
		
		for (EObject eo : list) {
			EClass ec = eo.eClass();
			if(ec.getName().equals("EClass")){
				Object o = (Object)eo.eGet(ec.getEStructuralFeature("name"));
				Concept e = new Concept(o.toString(), (EClassifier)eo);
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
				Enum en = new Enum(o.toString(), (EClassifier)eo);
				enums.put(en.getName(), en);
				addConcept(en);
			} else if(ec.getName().equals("EDataType")){
				Object o = (Object)eo.eGet(ec.getEStructuralFeature("name"));
				DataType dt = new DataType(o.toString(), (EClassifier) ec);
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
				Concept e = getConcept(conceptName);
				Collection<EReference> erefs = (Collection<EReference>)eo.eGet(ec.getEStructuralFeature("eAllReferences"));
				for (EReference eReference : erefs) {
//					System.out.println("MM.build: "+conceptName+" -> "+eReference.getEType().getName());
					if(eReference.getEType().eIsProxy()){
						LOGGER.severe("Proxies are not supported."
								+ "\nReference ignored : '"+e.getName()+"."+eReference.getName()+"' is a proxy to "+EcoreUtil.getURI(eReference.getEType())+".");
					} else {
						Reference r = new Reference(eReference, e);
						e.putReference(eReference.getName(), r);
						references.put(eReference.getName(), r);
					}
				}
				
				Collection<EAttribute> eatts = (Collection<EAttribute>)eo.eGet(ec.getEStructuralFeature("eAllAttributes"));
				for (EAttribute eAttribute : eatts) {
//					System.out.println("MM.build: "+conceptName+" -> "+eAttribute.getEType().getName()+" | "+e+" | "+getConcept(eAttribute.getEType().getName()));
					if(eAttribute.getEType().eIsProxy()){
						LOGGER.severe("Proxies are not supported."
								+ "\nReference ignored : '"+e.getName()+"."+eAttribute.getName()+"' is a proxy to "+EcoreUtil.getURI(eAttribute.getEType())+".");
					} else {
						Attribute a = new Attribute(eAttribute, e);
						e.putAttribute(eAttribute.getName(), a);
						attributes.put(eAttribute.getName(), a);
					}
				}
			} else {
				
			}
			
		}
	}

	/**
	 * Jess allows an legidible syntax to represent metamodels
	 * @return
	 */
	public static String printJessMetamodel() {
		return printJessMetamodel(AllMultiSlot.SINGLE_n_MULTI);
	}
	private static String jessMetamodel;
	public static String printJessMetamodel( AllMultiSlot multi) {
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
		for (Concept concept : getAllConcepts().values()) {
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
				res += "  ("+ ((esf.isMany()||multi.equals(AllMultiSlot.ALL_MULTI))?"multi":"")+  "slot "+esf.getName() +" "+numberType+")\n";//+" : "+esf.getEType().getName() +")\n";//TYPAGE ?
			}
			res += ")\n";
		}
		
		

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
	
	static String jessInheritance;

	
	public static HashMap<String, HashSet<String>> getInheritageAllSuperNames() {
		return inheritageAllSuperNames;
	}
	public static HashMap<String, HashSet<String>> getDirectInheritageParentNames() {
		return inheritageDirectParentNames;
	}

	public static HashMap<String, HashSet<String>> mapInheritance(Resource resource){
		inheritageAllSuperNames = new HashMap<String, HashSet<String>>();
		inheritageDirectParentNames = new HashMap<String, HashSet<String>>();
		inheritage = new HashMap<EClass, HashSet<EClass>>();
		
		//First : references classes
		TreeIterator<EObject> eAllContents = resource.getAllContents();
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
			log += ToolBox.printMultimapEClasses(inheritage, "    - ");
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
	
	public static HashMap<EClass, HashSet<EClass>> getInheritage() {
		return inheritage;
	}
	
	public static void setInheritageNames(HashMap<String, HashSet<String>> inheritage) {
		inheritageNames = inheritage;
	}
	
	public static HashMap<String, HashSet<String>> getInheritageNames() {
		return inheritageNames;
	}

	public static String printCoverageDefinition() {
		String res = "Coverage definition: \n  concepts evicted = {\n";
		for (MMElement mme : dismissedElements) {
			res += "   - " + mme.toString() +" \n";
		}
		return res.trim()+"\n  }";
	}


}

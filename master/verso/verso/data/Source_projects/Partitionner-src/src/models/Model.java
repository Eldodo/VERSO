package models;

import genetic.Gene;

import java.io.IOException;
import java.security.acl.LastOwnerException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
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
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import utils.Config;
import utils.Utils;

/**
 * 
 * @author batotedo
 *
 *
 * Abstract representation of an XMI instance
 */
public class Model implements Gene {
	public final static Logger LOGGER = Logger.getLogger(Model.class.getName());
	public static int nbModels = 0;
	int number;
	
	public static final String SEPARATOR = ".";
	public static final String ENUM_SEPARATOR = ",,";
	public static final String DIEZ = "#";
	public static final String AFFECT = ":";
	public static final String ID_JESS = "ID_Jess";
	public static final String CLASSNAME_JESS = "CLASSNAME_Jess";
	public static final String PARENTS_JESS = "PARENTS_Jess";
	
	ConcurrentHashMap<String, ArrayList<String>> classProperties;
	ArrayList<String> elements;
	HashSet<String> classes;
	String resourceFileName;
	private Resource resource;
	
	
	public static void clean(){
		nbModels = 0;
	}
	
	public Model() {
		this.elements = new ArrayList<>();
		this.classes  = new HashSet<>();
		this.classProperties = new ConcurrentHashMap<>();
		this.features = new ConcurrentHashMap<>();
		this.featureValues = new ConcurrentHashMap<>();
		number = nbModels++;
	}
	
	public String jessification(){
		return jessification(true, false);
	}
	public String jessification(boolean withDefTemplates){
		return jessification(withDefTemplates, false);
	}
	
	/**
	 * Return the model translated into Jess syntax (.cpl).
	 * (Asserting facts)
	 * @param withDefTemplates : if <true>, prints first the deftemplates representing the metamodel.
	 * @param quotedIds : if <true>, prints JESS ids in between double quotes.
	 * @return a String representing the model in Jess.
	 */
	public String jessification(boolean withDefTemplates, boolean quotedIds){
		boolean verboseInheritedFeature = false;
		HashMap<EObject, String> hmEO2ID = new HashMap<>();
		HashMap<EObject, String> hmEO2Str = new HashMap<>();
		int numId = 0;//Utils.getRandomInt(1000);
		
		//Parcours du model
		//  Attribution des identifiants : double-map EClassifier<->String
		for (Iterator<EObject> it = resource.getAllContents(); it.hasNext();) {
			EObject eo = (EObject) it.next();
			String eoName = eo.eClass().getName()+numId;
			hmEO2Str.put(eo, eoName);
			hmEO2ID.put(eo, ""+numId);
			numId++;
		}
		//Parcours du model
		//  Utilisation des identifiants générés pour un slot 'ID_Jess' reservé en Jess
		
		
//		System.out.println("Model.jessification()");
		String res = "";
		
		if(withDefTemplates)
			res += printJessMetamodel(verboseInheritedFeature);
		
		res += "\n(deffacts "+resourceFileName+"\n";
		
		ArrayList<EObject> list = new ArrayList<>();
		//TODO Use the resource !
		for (Iterator<EObject> it = resource.getAllContents(); it.hasNext();) {
			list.add((EObject) it.next());
			
		}
		
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
//			System.out.println("ec : "+ec.getName());
			
			res += "   ("+ec.getName()+ " ";//+ " : "+ec.getEAllStructuralFeatures());
//						res += "  (Class (className "+ec.getName()+ ") ";//+ " : "+ec.getEAllStructuralFeatures());

			
			if(quotedIds)
				res += " ("+ID_JESS+" \""+hmEO2Str.get(eo)+"\") ";
			else
				res += " ("+ID_JESS+" "+hmEO2Str.get(eo)+") ";
			res += " ("+CLASSNAME_JESS+" "+ec.getName()+") ";
			res += " ("+PARENTS_JESS ;
			for (String s : Utils.getInheritageParentsNames().get(ec.getName())) {
				res+= " "+s;
			}
			res +=" ) ";
			
			for (EStructuralFeature esf : ec.getEAllStructuralFeatures()) {
				
				Object o = eo.eGet(esf, true);
				if(o instanceof Collection){
					Collection<Object> co = (Collection<Object>)o;
					if(!(co.isEmpty())){
						res += " ("+esf.getName() +" ";//+" : "+esf.getEType().getName() +")\n";//TYPAGE ?
						String subFeats = "";
						for (Object o2 : co) {
							Object o2_Str = hmEO2Str.get(o2);
							subFeats += " "+ ((o2_Str != null)? o2_Str.toString():
								(o2 instanceof String)?"\""+o2.toString()+"\"":o2.toString());
	//						System.out.println("  o2 : "+hmEO2Str.get(o2));
						}
						res += subFeats.trim()+")";
					}
				} else {
//					System.out.println("  o : "+hmEO2Str.get(o));
					if(hmEO2Str.get(o) != null)
						res += " ("+esf.getName() +" "+ hmEO2Str.get(o) + ")";
				}
			}
			res += " )\n";
		}
		res += "); end MODEL : "+resourceFileName;
		return res + "\n";
	}
	
	
	public static String printJessMetamodel(boolean verboseInheritedFeature) {
		String res = ";METAMODEL : "+Config.METAMODEL_NAME+"\n";
		for (EPackage ep : Utils.ePackages()) {
			res += "; PACKAGE "+ep.getName()+"\n";
			for (Iterator<EObject> it = ep.eAllContents(); it.hasNext();) {
				EObject eo = (EObject) it.next();
				if(eo instanceof EClass){
					EClass ec = (EClass)eo;
					res += "(deftemplate "+ec.getName();//+ " : "+ec.getEAllStructuralFeatures());
					int iSup = 0;
					for (EClass ecSUper : ec.getESuperTypes()) 
						res += ((iSup++==0)?" extends ":" ")+ ecSUper.getName() +" ";
					res += "\n";	
					if(verboseInheritedFeature){
						res += "  (slot "+Model.ID_JESS+")\n";
						res += "  (slot "+Model.CLASSNAME_JESS+")\n";//Class originale statique
						res += "  (multislot "+Model.PARENTS_JESS+")\n" ;
						for (EStructuralFeature esf : ec.getEAllStructuralFeatures()) 
							res += "  ("+ (esf.isMany()?"multi":"")+  "slot "+esf.getName() +")\n";//+" : "+esf.getEType().getName() +")\n";//TYPAGE ?
					} else {
						if(iSup == 0){ //If sup > 0, the template has a super, then the super has the slots ID, SUPER, CLASS etc.
							res += "  (slot "+Model.ID_JESS+")\n";
							res += "  (slot "+Model.CLASSNAME_JESS+")\n";//Class originale statique
							res += "  (multislot "+Model.PARENTS_JESS+")\n" ;
						}
						for (EStructuralFeature esf : ec.getEStructuralFeatures()) 
							res += "  ("+ (esf.isMany()?"multi":"")+  "slot "+esf.getName() +")\n";//+" : "+esf.getEType().getName() +")\n";//TYPAGE ?
						
					}
					
					res += ")\n";
				}
			}
			res += "; end Package "+ep.getName()+"\n";
			
		}
		res += "; end METAMODEL : "+Config.METAMODEL_NAME;
//		res += Utils.printJessInheritanceStructure();
//		res += Utils.printJessInheritanceFacts();
		res += "\n"+Utils.printJessPrimitives();
		return res+"\n";
	}

	
	
	
	static String jClass = "JESS_Class";
	static String jFeat = "JESS_Feature";
	static String jCard = "JESS_Card";
	
	/**
	 * 
	 * NOT WORKING 
	 * 
	 * This is a try to rise the abstraction of the JEss representation.
	 * 
	 * @deprecated
	 * @param withDefTemplates
	 * @param quotedIds
	 * @return
	 */
	public String jessificationUp(boolean withDefTemplates, boolean quotedIds){
		String res = "";
		if(withDefTemplates){
			res +=  ";Class and attributes\n"+
					"(deftemplate "+jClass+"\n"+
					"  (slot clID)"+"\n"+
					"  (slot clType)"+"\n"+
					"  (slot clName)"+"\n"+
					"  (multislot clSupers)"+"\n"+
					"  (multislot clFeatures) )"+"\n"+
					"\n"+
					"(deftemplate "+jFeat+"\n"+
					"  (slot feID)"+"\n"+
					"  (slot feName)"+"\n"+
					"  (slot feInClass)"+"\n"+
					"  (slot feOutClass)"+"\n"+
					"  (slot feInCard)"+"\n"+
					"  (slot feOutCard) )"+"\n"+
					"\n"+
					"(deftemplate "+jCard+"\n"+
					"  (slot caID)"+"\n"+
					"  (slot caUp)"+"\n"+
					"  (slot caBottom) ) "+"\n"+
					"\n"+
					"";
		}
		
		
		ArrayList<EObject> list = new ArrayList<>();
		for (Iterator<EObject> it = resource.getAllContents(); it.hasNext();) {
			list.add((EObject) it.next());
			
		}
		
		HashMap<String, Integer> hm_Str2ID = new HashMap<>();
		HashMap<Integer, String> hm_ID2Str = new HashMap<>();
		
		//Parcours du model
		//  Attribution des identifiants : double-map EClassifier<->String
		int i = 0;
		for (EObject eo : list) {
			String className_i = eo.eClass().getName()+"_"+i;
			
			if(!hm_Str2ID.containsKey(className_i)){
				hm_Str2ID.put(className_i, i);
				hm_ID2Str.put(i, className_i);
			}
			i++;
			
			
			for (EStructuralFeature esf : eo.eClass().getEAllStructuralFeatures()) {//EAll => includes inherited features.
				String feature_i = className_i + SEPARATOR+  esf.getName() ;
				hm_Str2ID.put(feature_i, i);
				hm_ID2Str.put(i, feature_i);
				i++;
			}
		}
		
		//Foreach classname_i.feature -> an ID has been given.
		
		
		System.out.println("Model.jessificationUp()");
		System.out.println("\n"+hm_ID2Str.size() + " - " + hm_Str2ID.size());
		
		//Parcours du model
		//  Utilisation des identifiants générés pour un slot 'ID_Jess' reservé en Jess
		
		
//		System.out.println("Model.jessification()");
		res += "\n(deffacts "+resourceFileName+"\n";
		


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
			String className_i = eo.eClass().getName()+"_"+i;
			
			res += "(Class (Name "+ec.getName()+ ") ";//+ " : "+ec.getEAllStructuralFeatures());
			
			
		}
		
//		for (EObject eo : list) {
//			EClass ec = eo.eClass();
////			System.out.println("ec : "+ec.getName());
//			
//			res += "   (Class (Name "+ec.getName()+ ") ";//+ " : "+ec.getEAllStructuralFeatures());
//			if(quotedIds)
//				res += " ("+ID_JESS+" \""+hm_EO2Str.get(eo.getClass().getName())+"\") ";
//			else
//				res += " ("+ID_JESS+" "+hm_EO2Str.get(eo.getClass().getName())+") ";
//			
//			
//			for (EStructuralFeature esf : ec.getEAllStructuralFeatures()) {
//				
//				Object o = eo.eGet(esf, true);
//				if(o instanceof Collection){
//					Collection<Object> co = (Collection<Object>)o;
//					if(!(co.isEmpty())){
//						res += " ("+esf.getName() +" ";//+" : "+esf.getEType().getName() +")\n";//TYPAGE ?
//						String subFeats = "";
//						for (Object o2 : co) {
//							Object o2_Str = hm_EO2Str.get(o2);
//							subFeats += " "+ ((o2_Str != null)? o2_Str.toString():
//								(o2 instanceof String)?"\""+o2.toString()+"\"":o2.toString());
//	//						System.out.println("  o2 : "+hmEO2Str.get(o2));
//						}
//						res += subFeats.trim()+")";
//					}
//				} else {
////					System.out.println("  o : "+hmEO2Str.get(o));
//					if(hm_EO2Str.get(o) != null)
//						res += " ("+esf.getName() +" "+ hm_EO2Str.get(o) + ")";
//				}
//			}
//			res += " )\n";
//		}
		return res +"); end MODEL : "+resourceFileName;
	}
	@Override
	public boolean equals(Object arg0) {
		if(arg0 == null)
			return false;
		if(! (arg0  instanceof Model) )
			return false;
		return this.resourceFileName.compareTo(((Model)arg0).resourceFileName) == 0;
	}
	
	public void nullify(){
		classProperties = null;
		elements = null;
		classes = null;
		resourceFileName = null;
	}
	
	public Model(Resource resource) throws IOException {
		this();
		this.resourceFileName = resource.getURI().lastSegment();
		this.resource = resource;
		URI fileURI = resource.getURI();
		try {
			resource.load(null);
			if(LOGGER.isLoggable(Level.CONFIG))
				LOGGER.config("Loading model : "+fileURI+"... Loaded.");
			
		} catch (IOException e) {
			LOGGER.severe(" !! Loading model : "+fileURI+"... Failure ! Unable to load file.  !!");
			//e.printStackTrace();
			throw new IOException("ModelNotValidException");
		}
		
		TreeIterator<EObject> eAllContents = resource.getAllContents();
		int i = 0;
		while (eAllContents.hasNext()) {
			EObject eo = eAllContents.next();
			String className = eo.eClass().getName();
			String className_i = eo.eClass().getName()+"_"+i++;
			
			if(!classProperties.containsKey(className_i))
				classProperties.put(className_i, new ArrayList<String>());
		
			for (EStructuralFeature esf : eo.eClass().getEAllStructuralFeatures()) {//EAll => includes inherited features.
				Object o = eo.eGet(esf, true);
				String feature = className + SEPARATOR+ "";
				if(esf instanceof EAttribute && ((EAttribute)esf).getEAttributeType() instanceof EEnum){
					if(esf.isMany()){
						feature = className + SEPARATOR+ esf.getName()+AFFECT;
						String subFeats = "";
						for (Object o2 : (Collection)o) {
							subFeats += o2 + ENUM_SEPARATOR;
						}
						if(subFeats.endsWith(ENUM_SEPARATOR)){//TODO ENUMERATION CAN'T finish with a coma !
							subFeats = subFeats.substring(0, subFeats.length()-ENUM_SEPARATOR.length());
						}
						feature += subFeats;
					}else{// !esf.isMany
						feature += esf.getName()+AFFECT+o;
					}
				} else if(o instanceof Collection){
					feature += DIEZ+esf.getName()+AFFECT+((Collection)o).size();
					
				} else if(o instanceof EObject){
					feature += DIEZ+esf.getName()+AFFECT+"1";
				} else {
					feature += esf.getName()+AFFECT+ o;
				}
				classes.add(className);
				elements.add(feature);
				classProperties.get(className_i).add(feature);
			}
		}
		
		if(LOGGER.isLoggable(Level.CONFIG))
			LOGGER.config(" -> Loaded model : "+simplePrint());
	}
	
	public String getResourceFileName() {
		return resourceFileName;
	}
	public ArrayList<String> getElements() {
		return elements;
	}
	
	public void setElements(ArrayList<String> elements) {
		this.elements = elements;
	}
	
	public ConcurrentHashMap<String, ArrayList<String>> getClassProperties() {
		return classProperties;
	}
	
	public void setClassProperties(HashMap<String, ArrayList<String>> classProperties) {
		this.classProperties = new ConcurrentHashMap<>(classProperties);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Model clone()  {
		Model m = new Model();
		HashMap<String, ArrayList<String>> classProperties2 = new HashMap<>();
		ArrayList<String> elements2 = new ArrayList<>();
		
		for (String string : elements) 
			elements2.add(""+string);
		m.setElements(elements2);
		
		for (String key : classProperties.keySet()) 
			classProperties2.put(key, (ArrayList<String>)classProperties.get(key).clone());
		m.setClassProperties(classProperties2);
		
		m.classes = new HashSet<>(this.classes);
		m.resourceFileName = this.resourceFileName;
		m.features = new ConcurrentHashMap<>(this.features);
		m.featureValues = new ConcurrentHashMap<>(this.featureValues);
		return m;
	}

	public HashSet<String> getClasses() {
		return classes;
	}
	
	ConcurrentHashMap<String, HashSet<String>> features;
	
	public HashSet<String> getFeatures(String className) {
		HashSet<String> res = features.get(className);
		//System.out.println("Model.getFeatures("+features.keySet()+")");
		if(res == null){
			res = new HashSet<>();
			if(Utils.getInheritageNames().get(className) != null ){
				for (String classNameFI : Utils.getInheritageNames().get(className)) {
					for (String elt : elements) {
						if(elt.startsWith(classNameFI)){
							res.add(elt);
						}
					}
				}
			}
			features.put(className, res);
		} 
		return res;
	}
	
	ConcurrentHashMap<AbstractMap.SimpleEntry<String, String>, HashSet<String>> featureValues;
	public HashSet<String> getFeatureValues(String className, String featureName) {
//		System.out.println("Model.getFeatureValues : "+featureValues);
		HashSet<String> features = getFeatures(className);
		HashSet<String> values = featureValues.get(new AbstractMap.SimpleEntry<>(className, featureName));
	
		if(values != null ){
			return values;
		}else {
			values = new HashSet<>();
			for (String feat : features) {
				String tmp = feat + "";
				tmp = tmp.substring(tmp.indexOf(SEPARATOR)+1);
				if(tmp.startsWith(DIEZ))
					tmp = tmp.substring(DIEZ.length());
				
				if(tmp.startsWith(featureName+AFFECT) )
					values.add(tmp.substring(featureName.length()+1));//+1 for the ":"
			}
			featureValues.put(new AbstractMap.SimpleEntry<>(className, featureName), values);
			return values;
		}
	}

	@Override
	public String prettyPrint() {
		String res = getClass().getSimpleName() + "{\n";
		for (String cn : classProperties.keySet()) {
			res += " - " + cn + "\n";
			for (String feat : classProperties.get(cn)) {
				res += "    "+feat+"\n";
			}
		}
		return res + "}";
	}
	public String simplePrint() {
		String res = getClass().getSimpleName() + "{";
		res += "classes : "+classProperties.keySet().size()+", ";
		int props = 0;
		for (String cn : classProperties.keySet()) {
			props += classProperties.get(cn).size();
		}
		res += "properties : "+props;
		return res + "}";
	}

	public int getNbClasses() {
		return classProperties.keySet().size();
	}
	public int getNbProperties() {
		int props = 0;
		for (String cn : classProperties.keySet()) {
			props += classProperties.get(cn).size();
		}
		return props;
	}
	
	@Override
	public String toString() {
		return ""+resourceFileName;
	}


	@Override
	public int size() {
		return classes.size();
	}

}

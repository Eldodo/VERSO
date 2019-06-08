package oclruler.metamodel;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;

import oclruler.rule.struct.Constraint;

/**
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class Model extends NamedEntity {
	public final static Logger LOGGER = Logger.getLogger(Model.class.getName());
	public static int nbModels = 0;
	
	HashMap<Constraint, Integer> oracleFires;
	
	private File 		file;
	private int 		numberOfOracleFire = 0;
	
	private Resource 	resource;
	
	protected String 	jessDeffactsText;
	
	
	/**
	 * Heritage : Class' name -> list of descendants  = {objects "instanceof" that classname} 
	 */
	private HashMap<String, ArrayList<EObject>> eobjs = new HashMap<>();
	

	protected Model(File file) throws InvalidModelException {
		super(file.getName());
		this.name = file.getName().substring(0, file.getName().length()-4);
		this.file = file;
		oracleFires = new HashMap<Constraint, Integer>();
		URI fileURIm = URI.createFileURI(file.getAbsolutePath());
		this.resource = Metamodel.resourceSet.createResource(fileURIm);
		loadResource();
		
		LOGGER.finer(" -> Loaded model : "+this);
	}
	
	
	public boolean hasResource() {
		return resource != null;
	}
	
	
	public ArrayList<EObject> getEobjects() {
		ArrayList<EObject> res = new ArrayList<>();
		for (ArrayList<EObject> eos : eobjs.values()) {
			res.addAll(eos);
		}
		return res;
	}
	
	public Collection<EObject> getEobjects(String className) {
		if(eobjs.get(className) != null)
			return eobjs.get(className);
		else
			return new ArrayList<>();
	}
	
	public HashMap<String, ArrayList<EObject>> getEobjectsMap() {
		return eobjs;
	}
	
	public void addEObject(EObject eo){
		if(eobjs == null)
			eobjs = new HashMap<>();
		String eClassName = eo.eClass().getName();
		if(!eobjs.containsKey(eClassName))
			eobjs.put(eClassName, new ArrayList<>());
		
		for (EClass ec : eo.eClass().getEAllSuperTypes()) {
			if(!eobjs.containsKey(ec.getName()))
				eobjs.put(ec.getName(), new ArrayList<>());
			eobjs.get(ec.getName()).add(eo);
		}
		eobjs.get(eClassName).add(eo);
	}
	
	
	public String prettyPrintEObjects(){
		String res = getFileName()+": EObjects {\n";
		for (String  s : eobjs.keySet()) {
			res += " - "+s + " : ";
			for (EObject eo : eobjs.get(s)) 
				res += eo.eClass().getName()+ " ";
			res += " \n";
		}
		return res + "}";
	}
	
	/**
	 * Compares resource file's name.
	 */
	@Override
	public boolean equals(Object arg0) {
		if(arg0 == null || (getClass() != arg0.getClass()))
			return false;
		return this.getFileName().compareTo(((Model)arg0).getFileName()) == 0;
	}
	
	
	public String getFileName() {
		return file.getName();
	}
	
	public File getFile() {
		return file;
	}
	
	@Override
	public Model clone()  {
		try {
			Model m = new Model(this.file);
			m.resource = this.resource;
			m.jessDeffactsText = jessDeffactsText;
			return m;
		} catch (InvalidModelException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return ""+getFileName();//+"("+resource+")";
	}

	public Resource getResource() {
		return resource;
	}
	
	public int getFireCount() {
		return numberOfOracleFire;
	}
	
	public void addOracleFire(Constraint pt, int fires){
		if(fires == 0) return;
		if(oracleFires.get(pt) == null)
			oracleFires.put(pt, 0);
		oracleFires.put(pt, oracleFires.get(pt)+fires);
		numberOfOracleFire += fires;
		LOGGER.fine("Model("+getName()+").addOracleFire("+pt+", "+ fires+"): "+oracleFires);
	}
	public void clearOracleFire() {
		oracleFires = new HashMap<>();
		numberOfOracleFire = 0;
	}
	public HashMap<Constraint, Integer> getOracleFires() {
		return oracleFires;
	}
	public int getNumberOfOracleFires() {
		return numberOfOracleFire;
	}
	public int getOracleFires(Constraint pt) {
		if(oracleFires.get(pt) == null)
			return 0;
		return oracleFires.get(pt);
	}
	
	public boolean isValid() {
		return numberOfOracleFire == 0;
	}
	
	public enum AllMultiSlot {
		SINGLE_n_MULTI,
		ALL_MULTI;
	}

	public String prettyPrint() {
		return ""+getFileName()+":"+(isValid()?"+":"-");
	}

	/**
	 * 
	 * <ol>
	 *  <li>Load resource file</li>
	 *  <li> {@link #extractContent() Extract content}</li>
     *</ol>
	 * @throws InvalidModelException
	 */
	public void loadResource() throws InvalidModelException {
		if(resource != null && !resource.isLoaded()){
			URI fileURI = resource.getURI();
			try {
				resource.load(null);//Usefull if contents inspection
				if(LOGGER.isLoggable(Level.FINEST))
					LOGGER.finest("Loading model : "+fileURI+"... Loaded.");
			} catch (IOException e) {
				EList<Diagnostic> d = resource.getErrors();
				EList<Diagnostic> d2 = resource.getWarnings();
				throw new InvalidModelException(d, d2);
			} catch (Exception e) {
				System.out.println("File: "+fileURI.toFileString());
				e.printStackTrace();
			}

			TreeIterator<EObject> eAllContents = resource.getAllContents();
			
			eobjs = null;
			while (eAllContents.hasNext()) {
				EObject eo = eAllContents.next();
				addEObject(eo);
			}
		}
		try {
			extractContent();
		} catch (IOException e) {
			throw new InvalidModelException("Content extraction could not finalize.\nIOException: "+e.getMessage());
		}
	}
	
	public void unloadResource()  {
		if(resource != null && resource.isLoaded()){
			resource.unload();
		}
	}

	public void reload() throws InvalidModelException  {
		unloadResource();
		loadResource();
	}
	
	public void addOracleFire(FireMap fm) {
		if(fm.get(this) != null)
			for (Constraint pt : fm.get(this).keySet()) {
				addOracleFire(pt, fm.get(this).get(pt));
			}
		else
			LOGGER.severe("addOracleFire : "+this.getFileName()+" -> "+fm);
	}



	@Override
	public String prettyPrint(String tab) {
		return tab+getFileName();
	}


	@Override
	public String simplePrint() {
		return getFileName();
	}


	public boolean rewriteXMI(String newXMI) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(newXMI);
			bw.close();
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Need {@link Model#extractContent()} called before calling this method to fill the classProperties map.<br/>
	 * Otherwise : NULL POINTER !
	 * @return
	 * @pre classProperties != null
	 */
	public int getNbClasses() {
		return classProperties.keySet().size();
	}
	
	
	/**
	 * Need {@link Model#extractContent()} called before calling this method to fill the classProperties map.<br/>
	 * Otherwise : NULL POINTER !
	 * @return
	 * @pre classProperties != null
	 */
	public int getNbProperties() {
		int props = 0;
		for (String cn : classProperties.keySet()) {
			props += classProperties.get(cn).size();
		}
		return props;
	}


	/**
	 * @param className
	 * @param featureName
	 * @return
	 */
	public HashSet<String> getFeatureValues(String className, String featureName) {
		if(featureValues == null)
			featureValues = new ConcurrentHashMap<>();
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
	
	ConcurrentHashMap<String, HashSet<String>> features;
	public HashSet<String> getFeatures(String className) {
		if(features == null)
			features = new ConcurrentHashMap<>();
		HashSet<String> res = features.get(className);
		if(res == null){
			res = new HashSet<>();
			if(Metamodel.getInheritageNames().get(className) != null ){
				for (String classNameFI : Metamodel.getInheritageNames().get(className)) {
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

	/**
	 * 
	 * @return the number of objects
	 */
	public float size() {
		return eobjs.size();
	}
	
	@SuppressWarnings("unchecked")
	public int extractContent() throws IOException {
		this.resourceFileName = resource.getURI().lastSegment();
		URI fileURI = resource.getURI();
		try {
			if(!resource.isLoaded())
				resource.load(null);
			if(LOGGER.isLoggable(Level.CONFIG))
				LOGGER.config("Loading model : "+fileURI+"... Loaded.");
			
		} catch (IOException e) {
			LOGGER.severe(" !! Loading model : "+fileURI+"... Failure ! Unable to load file.  !!");
			//e.printStackTrace();
			throw new IOException("ModelNotValidException");
		}
		
		classProperties = new ConcurrentHashMap<>();
		classes = new HashSet<>();
		elements = new ArrayList<>();
		
		
		featureValues = null;
		features = null;
		
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
						for (Object o2 : (Collection<Object>)o) {
							subFeats += o2 + ENUM_SEPARATOR;
						}
						if(subFeats.endsWith(ENUM_SEPARATOR)){// ENUMERATION CAN'T finish with a coma !
							subFeats = subFeats.substring(0, subFeats.length()-ENUM_SEPARATOR.length());
						}
						feature += subFeats;
					}else{// !esf.isMany
						feature += esf.getName()+AFFECT+o;
					}
				} else if(o instanceof Collection){
					feature += DIEZ+esf.getName()+AFFECT+((Collection<Object>)o).size();
					
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
		return i;// number of eobject found in resource tree
	}
	
	public static final String SEPARATOR = ".";
	public static final String ENUM_SEPARATOR = ",,";
	public static final String DIEZ = "#";
	public static final String AFFECT = ":";
	
	/**
	 * For each class, its instanciation.
	 */
	ConcurrentHashMap<String, ArrayList<String>> classProperties;
	ConcurrentHashMap<AbstractMap.SimpleEntry<String, String>, HashSet<String>> featureValues;
	ArrayList<String> elements;
	HashSet<String> classes;
	String resourceFileName;


	public String printFeatures(String className) {
		return getFeatures(className).toString();
	}


}

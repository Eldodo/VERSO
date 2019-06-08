package utils;

import genetic.Entity;
import genetic.Evolutioner;
import genetic.Population;
import genetic.fitness.FitnessVector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Model;
import models.ModelSet;

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
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import partition.PartitionModel;
import partition.ocl.OCLPartitionModel;


public class Utils {
	public final static Logger LOGGER = Logger.getLogger(Utils.class.getName());

	private static Random random;
	public static  ResourceSetImpl resourceSet;
	public static  Resource 		metamodelResource;

	private static HashMap<EClass, HashSet<EClass>> inheritage;
	private static HashMap<String, HashSet<String>> inheritageNames;
	private static HashMap<String, HashSet<String>> inheritageParentsNames;

	public static void initMinimal_for_FragmentComputation() {
		
//		Logger.getLogger("").addHandler(LogHandler.getInstance());
	    
        if(!Config.configLoaded) {
	        Config.loadThemAll();
        }
 		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> map = reg.getExtensionToFactoryMap();
        map.put("xmi",   new XMIResourceFactoryImpl());
        map.put("ecore", new XMIResourceFactoryImpl());
        buildMetamodelResource();
        inheritageNames = mapInheritance(Utils.metamodelResource);
		
        LOGGER.info("\n      Metamodel loaded : "+Config.METAMODEL_NAME);
	}

	public static void init() {
		
//		Logger.getLogger("").addHandler(LogHandler.getInstance());
	    
		
        if(!Config.configLoaded) {
	        Config.loadThemAll();
	        if( !ModelSet.getInstancesDirectory().exists() || !ModelSet.getInstancesDirectory().isDirectory() || ModelSet.getInstancesDirectory().listFiles().length <= 0){
	        	System.err.println("Instances' directory '"+ModelSet.getInstancesDirectory().getAbsolutePath()+"' not found or empty.");
	        	System.exit(1);
	        }
        	ModelSet.loadConfig();//Need metamodel resource to be runned.
        }
 		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> map = reg.getExtensionToFactoryMap();
        map.put("xmi",   new XMIResourceFactoryImpl());
        map.put("ecore", new XMIResourceFactoryImpl());
        buildMetamodelResource();
        inheritageNames = mapInheritance(Utils.metamodelResource);
        
        
        
        int nbmodels = 0;
		try {
			nbmodels = ModelSet.getNbModels();
			
		} catch (ExceptionInInitializerError e) {
			LOGGER.severe("Unable to load instances' repertory : '"+(Config.DIR_INSTANCES+Config.METAMODEL_NAME)+"'");
			if(LOGGER.isLoggable(Level.FINE))
				e.printStackTrace();
			System.exit(1);
		}
		
		
		
        LOGGER.info("\n      Metamodel loaded : "+Config.METAMODEL_NAME
        		+"\n              Expected : "+Evolutioner.GENERATION_MAX +"(generation) x "+Population.NB_ENTITIES_IN_POP + " (ModelSet) x "+Population.NB_GENES_IN_ENTITIES + " (Model)"
        		+"\n  Instances' directory : "+ModelSet.getNbModelsInBase()+"["+ModelSet.MIN_SIZE_MODEL+".."+ModelSet.MAX_SIZE_MODEL+"]"+" models from '"+Config.DIR_INSTANCES+Config.METAMODEL_NAME+"'"
        		+"\n             Evaluator : "+Config.DIS_OR_MIN+" with "+Config.FRAGMENT_SET_NAME +" "
           		+"\n         End condition : "+Evolutioner.OBJECTIVES_END_CONDITION_TEXT
		   		+"\n            Objectives : "+FitnessVector.csvHeader
		   		+"\n               Epsilon : "+Config.EPSILON
		   		+"\n                   Log : every "+Evolutioner.CHECK_POINT_GENERATIONS+" generations "+ ((Evolutioner.CHECK_POINT_TIME > 0)? " and "+Evolutioner.CHECK_POINT_TIME+" millis.":"")
		   		+((Evolutioner.MAX_TIME > 0)?"\n                  Time  : "+Evolutioner.MAX_TIME/1000+"s":"")
        		+"\n                 (Seed : "+Config.SEED+")");
	}
	public static <T> T getRandom(T[] c){
		if(c.length <= 0) return null;
		return (T)c[Utils.getRandomInt(c.length)];
	}

	
	private static boolean shortOnly = false; //Testing purpose
	
	public static String buildModelRepositoryStatistics() throws IOException{
		LOGGER.config("Building model repository statistics file for '"+Config.METAMODEL_NAME+"'.");
		boolean tmp = ModelSet.saveResources;
		ModelSet.saveResources = false;
		File fDatas = new File(Config.DIR_INSTANCES+Config.METAMODEL_NAME+"_RepositoryStatistics.log");
		BufferedWriter bw = new BufferedWriter(new FileWriter(fDatas));
		if(!shortOnly) {
			if(!fDatas.exists())
				fDatas.createNewFile();
			else {
				LOGGER.warning("File '"+fDatas.getAbsolutePath()+"' will be overriden.");
				fDatas.delete();
				fDatas.createNewFile();
			}
			bw.append("file;nbClasses;nbProperties\n");
		}
		File instancesDirectory = new File(Config.DIR_INSTANCES+Config.METAMODEL_NAME);
		int nbFiles = instancesDirectory.listFiles().length;

		int sumC = 0, sumP = 0;
		double avgC = 0, avgP = 0;
		double varC = 0, varP = 0;
		int maxC = 0, maxP =0;
		int minC = Integer.MAX_VALUE, minP = Integer.MAX_VALUE;

		int iPercent = 0;
		Model m = null;


		int[][] knowledge = new int[2][nbFiles];
		int CLASSES = 0;
		int PROPERTIES = 1;

		File files[] = Arrays.copyOf(instancesDirectory.listFiles(), nbFiles);
		if(!shortOnly)
			for (int i = 0;  i < nbFiles+0; i++) {
				File f = files[i-0];
				if( !f.isDirectory()){
					m = ModelSet.loadModel(f.getAbsolutePath(), resourceSet);
					if(m != null){
						int classes = m.getNbClasses();
						int nbProperties = m.getNbProperties();
		
						knowledge[CLASSES][i] = classes;
						knowledge[PROPERTIES][i] = nbProperties;
						bw.append(m.getResourceFileName()+";"+classes+";"+nbProperties+"\n");
		
						if(classes > maxC) maxC = classes;
						if(classes < minC) minC = classes;
						sumC += classes;
		
						if(nbProperties > maxP) maxP = nbProperties;
						if(nbProperties < minP) minP = nbProperties;
						sumP += nbProperties;
						m.nullify();
						m=null;
					}
				}
				if( i != 0 && (((i*1.0)/nbFiles)*100)>=iPercent){
					avgC = sumC*1.0/(i+1);
					avgP = sumP*1.0/(i+1);
					LOGGER.config(iPercent+++"% - "+i+" files processed : "+minC+"/"+Utils.format2Decimals((float)avgC)+"/"+maxC+"  -  "+minP+"/"+Utils.format2Decimals((float)avgP)+"/"+maxP);
				}
			}
		bw.close();
		avgC = sumC*1.0/nbFiles;
		avgP = sumP*1.0/nbFiles;
		iPercent = 0;
		for (int i = 0; i < nbFiles+0; i++) {
			varC += Math.pow(knowledge[CLASSES][i]-avgC, 2);
			varP += Math.pow(knowledge[PROPERTIES][i]-avgP, 2);
		}
		varC = varC/nbFiles;
		varP = varP/nbFiles;
		double sigmaC = Math.sqrt(varC);
		double sigmaP = Math.sqrt(varP);
		String res = "Repo :        "+instancesDirectory.getAbsolutePath()+"\n";
		
		
		
		PartitionModel partitionModel;
		if(Config.FRAGMENT_WITH_OCL)
			partitionModel = new OCLPartitionModel();
		else
			partitionModel = new PartitionModel();
		
		partitionModel.extractPartition();
		
		int nbPackages = 0;
		int nbClasses = 0;
		float nbReferences = 0;
		float nbAttributes = 0;
		int nbPartitions = partitionModel.getPartitions().size();
		
		for (Iterator<EObject> it = metamodelResource.getAllContents(); it.hasNext();) {
			EObject eObject = (EObject) it.next();
			if (eObject instanceof EPackage) {
				ePackages.add((EPackage) eObject);
				nbPackages++;
				for (Iterator<EObject> it2 = ((EPackage)eObject).eAllContents(); it2.hasNext();){
					EObject eo = it2.next();
					if(eo instanceof EClass){
						EClass ec = (EClass)eo;
						nbClasses++;
						nbReferences += ec.getEAllReferences().size();
						nbAttributes += ec.getEAllAttributes().size();
					}
				}
			}
		}
		
		nbReferences /= (float)nbClasses;
		nbAttributes /= (float)nbClasses;
		res +=       "         Metamodel : "+Config.METAMODEL_NAME+"\n";
		res +=       "        # packages : "+nbPackages+"\n";
		res +=       "         # classes : "+nbClasses+"\n";
		res +=       "  avg # references : "+nbReferences+"\n";
		res +=       "  avg # attributes : "+nbAttributes+"\n";
		res +=       "      # partitions : "+nbPartitions+"\n";
		
		res +=       "\n          # models : "+nbFiles+"\n";

		String resC = " * Classes *"+"\n";
		resC += "  Avg size : "+avgC+"\n";
		resC += "     Sigma : "+sigmaC+"\n";
		resC += "  Variance : "+varC+"\n";
		resC += "       Min : "+minC+"\n";
		resC += "       Max : "+maxC+"\n";
		resC += "     Total : "+sumC+"\n";

		String resP = " * Properties *"+"\n";
		resP += "  Avg size : "+avgP+"\n";
		resP += "     Sigma : "+sigmaP+"\n";
		resP += "  Variance : "+varP+"\n";
		resP += "       Min : "+minP+"\n";
		resP += "       Max : "+maxP+"\n";
		resP += "     Total : "+sumP+"\n";


		File fStats = new File(Config.DIR_INSTANCES+Config.METAMODEL_NAME+"_RepositoryStatistics_short.log");
		BufferedWriter bwF = new BufferedWriter(new FileWriter(fStats));
		try {

			LOGGER.fine(res + "\n"+resC+"\n"+resP);
			bwF.write(res+"\n"+resC+"\n"+resP);
			bwF.flush();
		} catch (Exception e) {
			LOGGER.severe("Couldn't write failed resources in file '"+fStats.getAbsolutePath()+"'");
			e.printStackTrace();
		} finally {
			bwF.close();
		}
		LOGGER.config("Data written in '"+ fDatas.getAbsolutePath()+"', statistics in '"+fStats.getAbsolutePath()+"'");
		
		ModelSet.saveResources = tmp;
		return res+"\n"+resC+"\n"+resP;
	}
	
	private static void buildMetamodelResource(){
		resourceSet = new ResourceSetImpl();
		URI fileURI = URI.createFileURI(Config.DIR_METAMODELS+Config.METAMODEL_NAME+".ecore");
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
	    for (EPackage ep : ePackages()) 
		    resourceSet.getPackageRegistry().put(ep.getNsURI(), ep);
	}
	
	static Set<EPackage> ePackages = null;
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
	
	public static HashMap<String, HashSet<String>> getInheritageParentsNames() {
		return inheritageParentsNames;
	}
	
	public static HashMap<String, HashSet<String>> mapInheritance(Resource resource){
		inheritageParentsNames = new HashMap<String, HashSet<String>>();
		inheritage = new HashMap<EClass, HashSet<EClass>>();
		
		//First : references classes
		TreeIterator<EObject> eAllContents = resource.getAllContents();
		while (eAllContents.hasNext()) {
			EObject ecf = eAllContents.next();
			if(ecf instanceof EClass){
				EClass eci = (EClass) ecf;
				inheritage.put(eci, new HashSet<EClass>());	
				inheritageParentsNames.put(eci.getName(), new HashSet<String>());
				if(!eci.isAbstract()) {
					inheritage.get(eci).add(eci);
				}
			} 
		}
		
		for (EClass ec : inheritage.keySet()) {
			for (EClass sup : ec.getEAllSuperTypes()) {
				if(!ec.isAbstract()){
					inheritage.get(sup).add(ec);
					inheritageParentsNames.get(ec.getName()).add(sup.getName());
				}
			}
		}
		
		if(LOGGER.isLoggable(Level.FINE)){
			String log = "Inheritage :\n";
			log += Utils.printMultimapEClasses(inheritage, "    - ");
			log += "\nParents :\n";
			for (String hs : inheritageParentsNames.keySet()) {
				log += hs+" : "+ inheritageParentsNames.get(hs) + "\n";
			}
			LOGGER.fine(log);
		}
		inheritageNames = new HashMap<>();
		for (EClass ec : inheritage.keySet()) {
			inheritageNames.put(ec.getName(), new HashSet<String>());
			for (EClass ec2 : inheritage.get(ec)) {
				inheritageNames.get(ec.getName()).add(ec2.getName());
			}
		}
		return inheritageNames;
	}


	public static final boolean isEAttributeANumber(EAttribute ea){
		String name = ea.getEAttributeType().getInstanceClassName();
		if(ea.getEAttributeType() instanceof EEnum)
			return false;
		return name.compareTo(Byte.class.getName()) == 0 || ea.getEAttributeType().getName().compareTo("EByte") == 0
				|| name.compareTo(Short.class.getName()) == 0 || ea.getEAttributeType().getName().compareTo("EShort") == 0
				|| name.compareTo(Integer.class.getName()) == 0 || ea.getEAttributeType().getName().compareTo("EInt") == 0 || ea.getEAttributeType().getName().compareTo("EInteger") == 0
				|| name.compareTo(Long.class.getName()) == 0 || ea.getEAttributeType().getName().compareTo("ELong") == 0
				|| name.compareTo(Float.class.getName()) == 0 || ea.getEAttributeType().getName().compareTo("EFloat") == 0
				|| name.compareTo(Double.class.getName()) == 0 || ea.getEAttributeType().getName().compareTo("EDouble") == 0;
	}
	public static final boolean isEAttributeABoolean(EAttribute ea){
		String name = ea.getEAttributeType().getInstanceClassName();
		if(ea.getEAttributeType() instanceof EEnum)
			return false;
		return name.compareTo(Boolean.class.getName()) == 0 || ea.getEAttributeType().getName().compareTo("EBoolean") == 0;
	}

	
	
	public static HashMap<EClass, HashSet<EClass>> getInheritage() {
		return inheritage;
	}
	public static void setInheritageNames(HashMap<String, HashSet<String>> inheritage) {
		Utils.inheritageNames = inheritage;
	}
	public static HashMap<String, HashSet<String>> getInheritageNames() {
		return inheritageNames;
	}

	public static void initializeRandom() {
		random =  new Random(Config.SEED);
	}

	public static Random getRandom() {
		return random;
	}

	public static void setRandom(Random newRandom) {
		random = newRandom;
	}

	public static double getRandomDouble() {
		return random.nextDouble();
	}

	public static int getRandomInt(int bound) {
		return random.nextInt(bound);
	}

	public static int getRandomInt(int lower, int upper) {
		if(lower == upper)
			return lower;
		return lower + random.nextInt(upper-lower);
	}
	

	public static String printMultimapEClasses(HashMap<EClass, HashSet<EClass>> map, String pre){
		String res = "";
		for (EClass key : map.keySet()) {
			res += pre+key.getName() +":";
			res += printEElements(map.get(key));
			res += "\n";
		}
		return res;
	}
	
	public static String printEElements(Collection<? extends ENamedElement> list){
		String res = "{";
		for (ENamedElement one : list) {
			if(one instanceof EStructuralFeature)
				res += printCard((EStructuralFeature)one);
			res += one.getName()+",";
		}
		if(res.endsWith(","))
			res = res.substring(0, res.length()-1);
		return res+"}";
	} 
	
	public static String printCard(EStructuralFeature esf){
		if(esf.getLowerBound() == 1 && esf.getUpperBound() == 1)
			return "";
		if(esf.getLowerBound() == 0 && esf.getUpperBound() == 1)
			return "?";
		if(esf.getLowerBound() == 1 && esf.getUpperBound() == -1)
			return "+";
		if(esf.getLowerBound() == 0 && esf.getUpperBound() == -1)
			return "*";
		return "";
	}
	public static String printEObject(EObject deoi){
		return printEObject(deoi, false);
	}

	public static String printEObject(EObject deoi, boolean recursive){
		return printEObject(deoi, recursive, "   ");
	}

	public static String printEObject(EObject deoi, boolean recursive, String pre){
		String res = "";
		res += "{DOEI("+deoi.hashCode()+"):"+deoi.eClass().getName()+""/*+deoi.hashCode()*/+" ";//+o.eContents();//+":"
		for (EStructuralFeature esf : deoi.eClass().getEAllStructuralFeatures()) {
			try {
				Object s = deoi.eGet(esf, true);
				
				if(s instanceof DynamicEObjectImpl){//Encapsulating ref in link
	    			ArrayList<DynamicEObjectImpl> l = new ArrayList<>();
	    			l.add((DynamicEObjectImpl)s);
	    			s = l;
	    		}
				if(recursive && EReference.class.getSimpleName().equals(esf.eClass().getName())){
					if(s instanceof Collection){
						for (Iterator iterator2 = ((Collection)s).iterator(); iterator2.hasNext();) {
							DynamicEObjectImpl o2 = (DynamicEObjectImpl) iterator2.next();
							if(o2 != s)
								res += "\n"+pre+printEObject(o2, recursive, pre+"   ");
						}
					} 
				}
			} catch (Exception e) {
				res += "("+ esf.getName() +" : "+"null)";
			}
		}
		if(res.endsWith(","))
			res = res.substring(0, res.length()-1);
		res += "}";
		return res;
	}
	/**
	 * @param logFile - generated log file.
	 * @return
	 */
	public static ArrayList<ArrayList<String>> extractModelsFromLogFile(
			File logFile) {
		ArrayList<ArrayList<String>> fileNames = new ArrayList<>();

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(logFile));

			String line = br.readLine();
			boolean readingModels = false;
			int nbSets = 0;
			int nbModels = 0;
			while (line != null) {
				if(line.trim().isEmpty())
					readingModels = false;
				if(readingModels){
					String models = "";
					Pattern p = Pattern.compile("\\{.*\\}");
					Matcher m=p.matcher(line);
					while(m.find()) 
						models += m.group(0);
					if(!models.trim().isEmpty()){
						nbSets++;
						models = models.substring(1, models.length()-1);
						ArrayList<String> mNames = new ArrayList<>();
						StringTokenizer sTok = new StringTokenizer(models, ",");
						while(sTok.hasMoreTokens()){
							mNames.add(Config.DIR_INSTANCES+Config.METAMODEL_NAME+File.separator+sTok.nextToken());
							nbModels++;
						}
						fileNames.add(mNames);
					}
				}
				if(line.trim().startsWith("Statistics"))
					readingModels = true;
				line = br.readLine();
			} 
			LOGGER.config(" "+nbSets+" sets found, for a total of "+nbModels+" models.");
		} catch (FileNotFoundException e) {
			LOGGER.warning("File '"+logFile.getAbsolutePath()+"' not found.");
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.warning("An error occured while reading the file '"+logFile.getAbsolutePath()+"'.");
			e.printStackTrace();
		} 
		return fileNames;
	}
	public static ArrayList<String> extractModelsFromList(String repository, String listModelNames, String separator){
		ArrayList<String> mNames = new ArrayList<>();
		StringTokenizer sTok = new StringTokenizer(listModelNames, separator);
		while(sTok.hasMoreTokens()){
			mNames.add(repository+sTok.nextToken());
		}
		return mNames;
	}
   public static double format2Decimals(float f){
    	return ((int)(f*100))/100.0;
    }
   // conversion en hh:mn:sec:ms
   public static String formatMillis(long millis){
   	long nb_hh = millis / (60 * 60 * 1000);
//       System.out.println("Heures: " + nb_hh);
    
       long reste_milli = millis - (nb_hh * 60 * 60 * 1000);
       long nb_min = reste_milli / (60 * 1000);
//       System.out.println("Minutes: " + nb_min);
    
       reste_milli = reste_milli - (nb_min * 60 * 1000);
       long nb_sec = reste_milli / 1000;
//       System.out.println("Secondes: " + nb_sec);
       
       reste_milli = reste_milli - (nb_sec * 1000);
       long nb_mil = reste_milli;
//       System.out.println("Millisecondes: " + nb_mil);
       
       return "" + nb_hh + ":" + nb_min + ":" + nb_sec + ":" + nb_mil;
		
   }

	public static void storeNumericResults(File f, Entity e, String timeStamp, String elapsedTime){
		try {
			if(!f.exists()){
				f.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
				bw.append(e.printResultHeader(";"));
				bw.close();
			}
			LOGGER.info("Writing "+f+"  ");
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
			bw.append(e.printResult(";", timeStamp, elapsedTime, false)+"\n");
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		} 
	}
	
	public static Path resolveInstancePath(String fileName){
		return Paths.get(Config.DIR_INSTANCES, Config.METAMODEL_NAME, fileName);
	}
	
	public static long usedMemory() {
		return s_runtime.totalMemory() - s_runtime.freeMemory();
	}

	private static final Runtime s_runtime = Runtime.getRuntime();
	
//	(deftemplate StateMachine
//			(slot id)
//			(slot init)
//			(multislot vertices)
//			(multislot transitions)
//		)


	public static String printMetamodel() {
		String res = Config.METAMODEL_NAME+" ";
		for (EPackage ep : ePackages()) {
			res += ep.getName();
//			for(EClassifier ec : ep.eAllContents()){
//				System.out.println("  - "+ec.getName()+ " : "+ec.getClass().getName());
//				
//			}
			for (Iterator<EObject> it = ep.eAllContents(); it.hasNext();) {
				EObject eo = (EObject) it.next();
				if(eo instanceof EClass){
					EClass ec = (EClass)eo;
					res += "\n  o "+ec.getName();//+ " : "+ec.getEAllStructuralFeatures());
					int iSup = 0;
					for (EClass ecSUper : ec.getESuperTypes()) {
						res += ((iSup++==0)?" extends ":" ")+ ecSUper.getName() +" ";
					}
					for (EStructuralFeature esf : ec.getEAllStructuralFeatures()) {
						res += "\n    - "+esf.getName()+" : "+esf.getEType().getName();
					}
				}
			}
			
		}
		return res;
	}
	public static String printJessInheritanceStructure(){
		String res = ";Inheritacne structure\n"+
				"(deftemplate Inheritance\n"+
				"  (slot Class)"+"\n"+
				"  (multislot supers)"+"\n"+
				"  (multislot children) )"+"\n"+
				"\n";
		return res;
	}
	public static String printJessInheritanceFacts(){
		String res = ";INHERITANCE facts\n(deffacts inheritance\n" ;
		for (String s : Utils.getInheritageNames().keySet()) {
			res += " (Inheritance (Class "+s+") (children";
			for (String s2 : Utils.getInheritageNames().get(s)) {
				res += " "+s2;
			}
			res += ") )\n";
		}
		return res + ")\n";
	}

	public static String printJessPrimitives(){
		String res = "; PRIMITIVES\n;End PRIMITIVES"+
				"\n";
		return res;
	}

}

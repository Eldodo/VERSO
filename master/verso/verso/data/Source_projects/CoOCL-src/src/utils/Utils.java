package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import coocl.ocl.Program;
import oclruler.genetics.Evaluator;
import oclruler.genetics.Evolutioner;
import oclruler.genetics.FitnessVector;
import oclruler.genetics.Population;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.MetamodelMerger;
import oclruler.ui.Ui;
import utils.distance.Cosine;

public class Utils {
	public final static Logger LOGGER = Logger.getLogger("init");

	private static Random random;

	public static String TAB_CHAR = "  ";
	
	public static void initInSilence(String configFilePath) {
		silence();
		init(configFilePath);
		unsilence();
	}

	public static void init() {
		init(null);
	}
	
	/**
	 * file OCL loaded from ConfigFile.OCL_FILE
	 * @param configFilePath if null : ./utils/config.properties
	 */
	public static void init(String configFilePath) {
		init(configFilePath, null);
	}
	
	/**
	 * 
	 * @param configFilePath if null : ./utils/config.properties
	 * @param fileOCL if null : loaded from ConfigFile.OCL_FILE
	 */
	public static void init(String configFilePath, String fileOCL) {
		init(configFilePath, fileOCL, null);
	}
	
	
	public static void init(String configFilePath, String fileOCL, String metamodelName) {
       
        if(!Config.configLoaded) {
        	String filename = configFilePath==null?"./utils/config.properties":configFilePath;
        	
	        Config.loadThemAll(filename);
	        Evaluator.loadConfig();
	        Cosine.loadConfig();
	        FitnessVector.loadConfig();
	        Population.loadConfig();
       		Evolutioner.loadConfig();	//Depends on FitnessVector.NUMBER_OF_OBJECTIVES
        }
       
        Metamodel.init(metamodelName);
        
        String fileOCLname = fileOCL;
        if(fileOCL == null){
        	try {
        		fileOCLname = Config.getStringParam("OCL_FILE");
			} catch (Exception e) {
			} finally {
				if(fileOCLname == null){
					fileOCLname  = Config.DIR_TESTS+Config.METAMODEL_NAME+".ocl";
				}
			}
        }
        Program.loadConfig(new File(fileOCLname));//Depends on metamodel //instantiate MetamodelMerger.instance
       
		String log = 
			       "\n          Metamodel1 loaded : "+Config.METAMODEL_NAME +" ("+Metamodel.getMm1().getMetamodelResource().getURI()+")"
			    +  "\n          Metamodel2 loaded : "+Config.METAMODEL_NAME +" ("+Metamodel.getMm2().getMetamodelResource().getURI()+")"
			    +  "\n                   OCL file : "+Program.OCL_FILE.getAbsolutePath()+(Program.CONSIDER_ALL_CONSTRAINTS?" (All constraints considered)":"")
			    +  "\n          Expected OCL file : "+Program.OCL_EXPECTED_FILE
			    +  "\n                 Objectives : "+FitnessVector.csvHeader
				+  "\n   Generations x Population : "+Evolutioner.GENERATION_MAX+" x "+Population.POPULATION_SIZE 
				+  "\n                      (Seed : "+Config.SEED+")" ;
		
		LOGGER.info(log);
		LOGGER.fine("Initial constraints:\n"+Program.getInitialProgram().prettyPrint("  "));
		LOGGER.fine("Initial merge between models:\n"+MetamodelMerger.getInstance().printMetamodelsDiff());
		LOGGER.finer("Initial evolution footprint on OCL:\n"+MetamodelMerger.getInstance().printOCLFootprint());
		
        if (Config.VERBOSE_ON_UI){ 
//    		subscribeParetoListener(Ui.getInstance().getChartTime());
    		Ui.getInstance().setTextSetting(Config.printPrettySetting());
    		Ui.getInstance().log(log);
			Ui.setPlot2Title("# Fronts");
    		Ui.setPlot3Title("Average # Patterns");
    		Ui.setPlot4Title("Injections");
    		Ui.setPlot5Title("Size Pareto");
    	}

        			
	}
	
	
	public static ArrayList<File> getPrefixedFiles(String prefix, String folderName){
		ArrayList<File> res = new ArrayList<File>();
		File folder = new File(folderName);
		if(!folder.isDirectory())
			throw new IllegalArgumentException("'"+folderName+"' is not a directory.");
		for (File file : folder.listFiles()) {
			if(file.getName().startsWith(prefix))
				res.add(file);
		}
		return res;
	}
	
	public static String readFile(String fileName) throws IOException{
		return readFile(new File(fileName));
	}
	public static String readFile(File file) throws IOException{
		String res = "", s;
		BufferedReader br3 = new BufferedReader(new FileReader(file));
		while ((s = br3.readLine()) != null) 
			res += s + "\n";
		br3.close();s=null;
		return res.trim()+"\n";
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

	/**
	 * 
	 * @return double in [0..1]
	 */
	public static double getRandomDouble() {
		return random.nextDouble();
	}

	/**
	 * Exclusive bound !
	 * @param bound EXCLUSIVE
	 * @return
	 */
	public static int getRandomInt(int bound) {
		return random.nextInt(bound);
	}
	

	/**
	 * 
	 * @param lower INCLUSIVE
	 * @param upper EXCLUSIVE
	 * @return
	 */
	public static int getRandomInt(int lower, int upper) {
		if(lower == upper)
			return lower;
		return lower + random.nextInt(upper-lower);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getRandom(Collection<T> c){
		if(c.size() <= 0) return null;
		return (T)c.toArray()[Utils.getRandomInt(c.size())];
	}
	public static <T> T getRandom(T[] c){
		if(c.length <= 0) return null;
		return (T)c[Utils.getRandomInt(c.length)];
	}
	
	public static <T> ArrayList<Collection<T>> split(Collection<T> c){
		if(c.size() < 0) return null;
		int cutPoint = c.size()/2;
		ArrayList<T> res1 = new ArrayList<T>();
		ArrayList<T> res2 = new ArrayList<T>();
		int i = 0;
		for (T t : c) {
			if(i< cutPoint)
				res1.add(t);
			else
				res2.add(t);
			i++;
		}
		ArrayList<Collection<T>> res = new ArrayList<Collection<T>>(2);
		res.add(res1);
		res.add(res2);
		return res;
	}
	public static <T> ArrayList<Collection<T>> split(T[] c){
		if(c.length < 0) return null;
		int cutPoint = c.length/2;
		ArrayList<T> res1 = new ArrayList<T>();
		ArrayList<T> res2 = new ArrayList<T>();
		int i = 0;
		for (T t : c) {
			if(i< cutPoint)
				res1.add(t);
			else
				res2.add(t);
			i++;
		}
		ArrayList<Collection<T>> res = new ArrayList<Collection<T>>(2);
		res.add(res1);
		res.add(res2);
		return res;
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
	public static String printEStructuralFeatures(EObject eo){
		String res = eo.eClass().getName()+":\n";
		for (EStructuralFeature esf : eo.eClass().getEAllStructuralFeatures()) {
			res += " - "+esf.getName() +": " + eo.eGet(esf)+ " (from "+esf.getContainerClass().getSimpleName() + ")  \n";
		}
		return res;
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
	
	

	
	// conversion en hh:mn:sec:ms
	public static String formatMillis(long millis) {
		long nb_hh = millis / (60 * 60 * 1000);
		// System.out.println("Heures: " + nb_hh);

		long reste_milli = millis - (nb_hh * 60 * 60 * 1000);
		long nb_min = reste_milli / (60 * 1000);
		// System.out.println("Minutes: " + nb_min);

		reste_milli = reste_milli - (nb_min * 60 * 1000);
		long nb_sec = reste_milli / 1000;
		// System.out.println("Secondes: " + nb_sec);

		reste_milli = reste_milli - (nb_sec * 1000);
		long nb_mil = reste_milli;
		// System.out.println("Millisecondes: " + nb_mil);

		return "" + nb_hh + ":" + nb_min + ":" + nb_sec + ":" + nb_mil;
	}

	public static String completeString(String s, int length){
		return completeString(s, length, true);
	}
	public static String completeString(String s, int length, boolean cutExceedent){
		String res = s;
		if(cutExceedent && length < s.length())
			return s.substring(0, length);
		for (int i = 0; i <  length - s.length(); i++) 
			res += " ";
		return res;
	}
	
//	public static void main(String[] args) {
//		printLOC();
//	}

	public static void printLOC(){
		int i;
		try {
			i = countLOC(new File("./src"));
			System.out.println("Main.main(src:"+i+")");
//			i = countLOC(new File("./test"));
//			System.out.println("Main.main(test:"+i+")");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static int countLOC(File f) throws IOException {
		int res = 0;
		if(f.getName().startsWith("result"))
			return 0;
		if(f.isDirectory()){
			for (File f2 : f.listFiles()) {
				res += countLOC(f2);
			}
			System.out.println("Dir:"+f.getCanonicalPath()+" : "+res);
		} else {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = "";
			while((line = br.readLine()) != null){
				if(!line.isEmpty())
					res++;
			}
			br.close();
			System.out.println(f.getCanonicalPath()+" : "+res);
		}
		return res;
	}
	
	static Level logLevel = LOGGER.getLevel();
	
	public static void silence() {
		logLevel = LOGGER.getLevel();
		LOGGER.setLevel(Level.OFF);
	}

	public static void unsilence() {
		LOGGER.setLevel(logLevel);
	}

	public static String capOnFirstLetter(String string) {
		return string.substring(0,1).toUpperCase()+string.substring(1, string.length()).toLowerCase();
	}


}

package utils;

import genetic.Evolutioner;
import genetic.Population;
import genetic.fitness.FitnessObjectiveFactory;
import genetic.fitness.FitnessVector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import partition.PartitionModel;
import partition.composition.FragmentSet;
import partition.ocl.OCLPartitionModel;


public class Config {
	public static int NUMBER_OF_EXECUTIONS = 1;
	public static String DIS_OR_MIN = "MIN";//or "DIS" : Minimality or DISsimilarity
	public static boolean VERBOSE_ON_UI = false;
	public static boolean VERBOSE_ON_FILE  = false;
	public static boolean STORE_RESULT  = false;
	public static boolean STORE_JESS_RESULT  = false;

	private static Logger LOGGER = Logger.getLogger(Config.class.getName());

	public static boolean FRAGMENT_WITH_OCL = true;
	public static String  FRAGMENT_SET_NAME	= "partition.composition.AllRangesFragmentSet";
	public static Class   FRAGMENT_SET		= null;
	
	public static int     NUMBER_OF_OBJECTIVES = 2; //CAREFULL !! TODO Synchronization of static fields unsure.  To be explored !
	public static double  EPSILON = 0.00001;
	public static double  MFRT_COEF = 1/25;
	
	public static String DIR_METAMODELS = "metamodels/";
	
	public static String DIR_RESULTS = "results/";
	public static String DIR_TESTS = "tests/";
	public static String DIR_OUT 	 = "out/";
	public static String DIR_NUM	 = "num/";
	public static String DIR_SOL	 = "res/";
	
	public static String DIR_INSTANCES = "/AtlanModInstatiator/instances/";
	public static String METAMODEL_NAME = "compoSM";
	
	public static double SIZE_EMPHASIS = 1.0;
	public static int SIZE_MARGIN = 0;

	public static long SEED = (long)0;
	
	
	
	
	static Properties prop = new Properties();
	static InputStream input = null;
	public static boolean configLoaded = false;
	public static void loadThemAll() {
		configLoaded = false;
		//Initializing load calls
		String filename = "./utils/config.properties";
		try {

			LOGGER.info("Loading config : " + filename);
			input = Config.class.getClassLoader().getResourceAsStream(filename);
			if (input == null) {
				LOGGER.severe("Sorry, unable to find " + filename);
				return;
			}

			prop.load(input);

			Enumeration<?> e = prop.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = prop.getProperty(key);
				LOGGER.config(key + "\t : " + value);
			}
			loadConfig();
		} catch (IOException ex) {
			LOGGER.severe("Failure on loading file '"+filename+"'");
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Population.loadConfig();
		FitnessObjectiveFactory.loadConfig();
		Evolutioner.loadConfig();
		OCLPartitionModel.loadConfig();
		configLoaded = true;
	}
	public static String printSetting(String escapeChar) {
		String s = escapeChar+"Setting : \n";
		Enumeration<?> e = prop.propertyNames();
		
		ArrayList<String> keyList = new ArrayList<>();
		ArrayList<String> keyOCLList = new ArrayList<>();
		
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if(key.endsWith("OCLTemplate"))
				keyOCLList.add(key);
			else
				keyList.add(key);
		}
		
		Collections.sort(keyList);
		Collections.sort(keyOCLList);
		
		for (String key : keyList) {
			String value = prop.getProperty(key);
			s += escapeChar+" " +key + " : " + value + "\n";
		}
		if(FRAGMENT_WITH_OCL){
			s += "\nOCL Templates :\n";
			for (String key : keyOCLList) {
				String value = prop.getProperty(key);
				s += escapeChar+" " +key + " : " + value + "\n";
			}
		}
		
		return s;
	}
	public static int getIntParam(String key){
		String value = prop.getProperty(key);
		return Integer.parseInt(value.trim());
	}
	public static boolean getBooleanParam(String key){
		String value = prop.getProperty(key);
		return Boolean.parseBoolean(value.trim());
	}
	public static String getStringParam(String key){
		String value = prop.getProperty(key);
		return value;
	}
	public static double getDoubleParam(String key) {
		String value = prop.getProperty(key);
		return Double.parseDouble(value.trim());
	}
	public static long getLongParam(String key) {
		String value = prop.getProperty(key);
		return Long.parseLong(value.trim());
	}
	
	
	
	public static void loadConfig(){
		//First load call
		NUMBER_OF_EXECUTIONS= Config.getIntParam("NUMBER_OF_EXECUTIONS");
		NUMBER_OF_OBJECTIVES= Config.getIntParam("NUMBER_OF_OBJECTIVES");//
		VERBOSE_ON_UI	 	= Config.getBooleanParam("VERBOSE_ON_UI");
		VERBOSE_ON_FILE 	= Config.getBooleanParam("VERBOSE_ON_FILE");
		STORE_RESULT 		= Config.getBooleanParam("STORE_RESULT");
		STORE_JESS_RESULT 		= Config.getBooleanParam("STORE_JESS_RESULT");
		
		FRAGMENT_WITH_OCL	= Config.getBooleanParam("FRAGMENT_WITH_OCL");
		
		EPSILON = 		Config.getDoubleParam("EPSILON");
		MFRT_COEF = 	Config.getDoubleParam("MFRT_COEF");
		SIZE_EMPHASIS = Config.getDoubleParam("SIZE_EMPHASIS");
		
		try {
			SIZE_MARGIN = 	Config.getIntParam("SIZE_MARGIN");
		} catch (Exception e1) {
			LOGGER.warning( "'SIZE_MARGIN' not set, default is 0.");
			SIZE_MARGIN = 0;
		}
		
		DIS_OR_MIN	= 	Config.getStringParam("DIS_OR_MIN");

		FRAGMENT_SET_NAME	= Config.getStringParam("FRAGMENT_SET_NAME");
		try {
			FRAGMENT_SET 	= Class.forName(FRAGMENT_SET_NAME);
		} catch (ClassNotFoundException cnfe) {
			LOGGER.log(Level.SEVERE, "Class not found : '" + FRAGMENT_SET_NAME + "'.",cnfe);
			
			FRAGMENT_SET_NAME = "partition.composition.AllRangesFragmentSet";
			FRAGMENT_SET = partition.composition.AllRangesFragmentSet.class;
		} finally {
			
		}
		String dirMM = Config.getStringParam("DIR_METAMODELS");
		if(new File(dirMM).exists())
			DIR_METAMODELS 		= Config.getStringParam("DIR_METAMODELS");
		else{
			LOGGER.warning("Metamodel directory '"+new File(dirMM).getAbsolutePath()+"' does not exist. Trying '"+new File(DIR_METAMODELS).getAbsolutePath()+"'...");
			if(!new File(DIR_METAMODELS).exists()){
				LOGGER.severe("Metamodel directory '"+DIR_METAMODELS+"' does not exist.");
				throw new IllegalArgumentException("No metamodel directory found");
			} else {
				LOGGER.warning("Metamodel directory selected : '"+new File(DIR_METAMODELS).getAbsolutePath()+"'.");
			}
		}
		
		DIR_INSTANCES 		= Config.getStringParam("DIR_INSTANCES");
		if(! new File(DIR_INSTANCES).exists()){
			LOGGER.severe("Could not open instances repository : '"+new File(DIR_INSTANCES).getAbsolutePath()+"'");
			System.exit(1);
		}
		
		METAMODEL_NAME 		= Config.getStringParam("METAMODEL_NAME");
		
		
		DIR_RESULTS 		= Config.getStringParam("DIR_RESULTS");
		if(! new File(DIR_RESULTS).exists()) new File(DIR_RESULTS).mkdir();
		DIR_TESTS 			= Config.getStringParam("DIR_TESTS");
		if(! new File(DIR_TESTS).exists()) new File(DIR_TESTS).mkdir();
		DIR_OUT 			= buildResultsPath(Config.getStringParam("DIR_OUT"));
		DIR_NUM 			= buildResultsPath(Config.getStringParam("DIR_NUMERIC"));
		DIR_SOL 			= buildResultsPath(Config.getStringParam("DIR_SOLUTIONS"));
		
		
		
		
		try {
			SEED = Config.getLongParam("SEED");
		} catch (Exception e) {
			SEED = new Random().nextLong();
			prop.put("SEED", SEED+"");
			LOGGER.warning("SEED generated randomly : "+SEED);
		} finally {
			Utils.initializeRandom();
		}
}

	private static String buildResultsPath(String insideDir){
		String res = Paths.get(DIR_RESULTS,METAMODEL_NAME).toString();
		if(! new File(res).exists()) new File(res).mkdir();//Creation of 'results/' directory
		res = Paths.get(res,insideDir).toString()+File.separator;
		if(! new File(res).exists()) new File(res).mkdir();
		return res;
	}
	
	
	public static FragmentSet loadFragmentSet(PartitionModel partitionModel){
		FragmentSet instance = null;
		try {
			Constructor<FragmentSet> constructeur = Config.FRAGMENT_SET.getConstructor(new Class[] {	PartitionModel.class });
			 instance = (FragmentSet) constructeur.newInstance(new Object[] {partitionModel});
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return instance;
	}
	public static String printPrettySetting() {//TODO Make it fit better to surrent understanding !! Add SIZE_*, MIN/MAX_SIZE DIS_OR_MIN GEOMETRIC
		String res = "Setting : \n";
//		res += "  Verbose on graph : \t"+Config.VERBOSE_ON_UI+"\n"; //Of course
//		res += "  Verbose on file : \t"+Config.VERBOSE_ON_FILE+"\n";
//		res += "  Verbose result : \t"+Config.STORE_RESULT+((STORE_JESS_RESULT)?" (+Jess)":"")+"\n";
//		
//		res += "\nRepositories : \n";
//		res += "  Metamodel directory : \t"+Config.DIR_METAMODELS+"\n";
//		res += "  Instance directory : \t"+Config.DIR_INSTANCES+"\n";
//		res += "  Output directory : \t"+Config.DIR_OUT+"\n";		
//		res += "  Output directory : \t"+Config.DIR_NUM+"\n";		
//		
//		res += "\nMetier : \n";		
//		res += "  Metamodel name : \t"+Config.METAMODEL_NAME+"\n";
//		res += "  Evaluator : \t\t"+Config.FRAGMENT_SET_NAME+"\n";
//		res += "  Using OCL : \t\t"+Config.FRAGMENT_WITH_OCL+"\n";
//		
//		
//		res += "\nEvolution :\n";
//		res += "  Generations : \t"+Evolutioner.GENERATION_MAX+" generations of "+Population.NB_ENTITIES_IN_POP+" MS x "+Population.NB_GENES_IN_ENTITIES+" models (checkpoint every "+Evolutioner.CHECK_POINT_GENERATIONS+" g.)\n";
//		res += "  Genetic operators rate : crossover : "+Population.CROSSING_RATE+", mutation : "+Population.MUTATE_RATE+"\n";
//
//		
//		res += "\nObjectives : \t\t"+FitnessVector.csvHeader+"\n";
//		res += "  End condition : \t"+Evolutioner.OBJECTIVES_END_CONDITION_TEXT+"\n";
//		res += "  Epsilon : \t\t"+Config.EPSILON+"\n";
//		res += "  MFRT Coefficient : \t"+Config.MFRT_COEF+"\n";
//		res += "  Seed : \t\t"+Config.SEED+"\n";

//		res += "\n"+getOclSetting("");
		
		
		res += "\nRAW :\n" + printSetting("  ");
		
		return res+"\n";
	}
	public static String getOclSetting(String escapeChar){

		String s = escapeChar+"Setting : \n";
		Enumeration<?> e = prop.propertyNames();

		ArrayList<String> keyOCLList = new ArrayList<>();

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if(key.endsWith("OCLTemplate"))
				keyOCLList.add(key);
		}

		Collections.sort(keyOCLList);

		if(FRAGMENT_WITH_OCL){
			s += "OCL Templates :\n";
			for (String key : keyOCLList) {
				String value = prop.getProperty(key);
				s += escapeChar+" " +key + " : " + value + "\n";
			}
		}
		return s;
	}
}

















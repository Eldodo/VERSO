package oclruler.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import oclruler.metamodel.Metamodel;
import oclruler.rule.PatternFactory;
import oclruler.rule.PatternFactory.PatternType;
import oclruler.ui.Ui;

public class Config {

	public static final Logger COV_DEF_LOGGER = Logger.getLogger("coverage.definition");

	public enum TFIDFVariant {
		WITHOUT, CROWDING_DISTANCE, OBJECTIVE;
		
		public boolean isObjective() {
			return this == TFIDFVariant.OBJECTIVE;
		}
		
		public boolean isCrowdingDistance() {
			return this == TFIDFVariant.CROWDING_DISTANCE;
		}

		public String prettyPrint() {
			if(isObjective())
				return OBJECTIVE.toString().toLowerCase();
			else if(isCrowdingDistance())
				return CROWDING_DISTANCE.toString().toLowerCase();
			return "";
		}
	}
	public static TFIDFVariant TFIDF = TFIDFVariant.WITHOUT;
	
	
	public static String OS_HOME = System.getProperty("os.name").toLowerCase().contains("linux")?"/home/batotedo/":"R:/";

	public static boolean IS_RANDOM_RUN = false;

	private static Logger LOGGER = Logger.getLogger(Config.class.getName());

	public static boolean EXPERIMENT_MODE = false;
	public static boolean VERBOSE_ON_FILE = false;
	public static boolean VERBOSE_ON_UI = true;
	
	
	/**
	 * If true, GUI is for editing examples.
	 */
	public static boolean EXAMPLE_EDITION_MODE = false;

	/**
	 * Default is "./oclruler/utils/config.properties"
	 */
	public static String CONFIG_FILE_NAME = "./oclruler/utils/config.properties";
	public static File CONFIG_FILE;	
	
	
	public static long SEED = (long) 0;

	static Properties prop = new Properties();
	static InputStream input = null;
	public static boolean configLoaded = false;

	public static boolean SINBAD = false;
	

	public static String METAMODEL_NAME;
	public static String DIR_METAMODELS;
	
	/**
	 * Initilization in {@link ToolBox#loadArgs(String[]) }
	 */
	public static File DIR_EXPERIMENT; 
	public static File DIR_EXAMPLES;
	public static File DIR_ORACLES;
	public static File DIR_RES;
	public static File DIR_TESTS;
	static File getExampleBaseForExperiment() {
		if(DIR_EXPERIMENT == null)
			return null;
		return Paths.get(DIR_EXPERIMENT.getAbsolutePath(), "_base").toFile();
	}

	public static String[] 	EXPERIMENT_SUB_FOLDERS;
	public static int 		EXPERIMENT_NB;
	public static String 	run_prefix = "run_";
	public static int	 	RUN_NB = 0;
	

	/**
	 * Load all constant from file in arguments.
	 * @param configFile File containing constants
	 */
	public static void loadThemAll(String configFile) {
		configLoaded = false;
		// Initializing load calls
		try {
			input = Config.class.getClassLoader().getResourceAsStream(configFile);
			
			CONFIG_FILE = new File(configFile);
			
			//If condigFile not found, try {project_folder}/src/ configFile (different project configurations...)
			if(!CONFIG_FILE.exists())
				CONFIG_FILE = Paths.get("src", configFile).toFile();
			
			
			if (input == null) {
				LOGGER.severe("Sorry, unable to find " + configFile+"\nExit.");
				System.exit(1);
			}
			Properties preProp = (Properties)prop.clone();
			prop.load(input);
			String s = preProp.size()==1?"s":"";
			if(!preProp.isEmpty())
				LOGGER.info("Command line argument"+s+" override"+(preProp.size()==1?"":"s")+" key"+s+": "+preProp);
			
			prop.putAll(preProp);

			Enumeration<?> e = prop.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = prop.getProperty(key);
				LOGGER.config(key + "\t : " + value);
			}
		} catch (IOException ex) {
			LOGGER.severe("Failure on loading file '" + configFile + "'");
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
	}

	/**
	 * Loaded constants from configFile:
	 * <ul>
	 *  <li>{@link Config#DIR_EXAMPLES DIR_EXAMPLES}</li>
	 *  <li>{@link Config#METAMODEL_NAME METAMODEL_NAME}</li>
	 *  <li>{@link Config#DIR_ORACLES DIR_ORACLES}</li>
	 *  <li>{@link Config#DIR_RESULTS DIR_RESULTS}</li>
	 *  <li>{@link Config#DIR_TESTS DIR_TESTS}</li>
	 *  <li>{@link Config#SEED SEED}</li> 
	 *  <li>{@link Config#VERBOSE_ON_FILE VERBOSE_ON_FILE}</li>
	 *  <li>{@link Config#VERBOSE_ON_UI DIR_EXAMPLES}</li>
	 *  <li>{@link Ui#PRINT_FIRSTS PRINT_FIRSTS}</li>
	 *  <li>{@link Ui#setOpeningTab(String) UI_OPENING_TAB} </li>
	 *  <li>{@link #configLoaded} = <code>true</code></li>
	 *  <li>{@link #SINBAD} (Default= <code>true</code>)</li>
	 *</ul>
	 */
	public static void loadConfig() {
		try {
			SINBAD = getBooleanParam("SINBAD");
		} catch (Exception e1) {
			SINBAD = false;
		}
		
		
		// First load call
// Metamodels
		String dirMM = Config.getStringParam("DIR_METAMODELS");
		if(!dirMM.endsWith(File.separator))
			dirMM += File.separator;
		
		File dirMMf = new File(dirMM);
		if (dirMMf != null && dirMMf.exists())
			DIR_METAMODELS = dirMM;
		else {
			LOGGER.severe("DIR_METAMODELS = '" + dirMMf.getAbsolutePath() + "' does not exist.");
			throw new IllegalArgumentException("No metamodel directory found");
			
		}
		
// Instances
		if(EXPERIMENT_MODE){
			EXPERIMENT_SUB_FOLDERS = Config.DIR_EXPERIMENT.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if(name.equalsIgnoreCase("_results"))
						return false;
					if(dir == null)
						return false;
					return dir.isDirectory();
				}
			});
			
			Arrays.sort(EXPERIMENT_SUB_FOLDERS);
			if(EXPERIMENT_SUB_FOLDERS.length == 0) {
				LOGGER.severe("Experiment folder '"+Config.DIR_EXPERIMENT.getAbsolutePath()+"'does not have sub folders.\nExit.");
			}
			Config.EXPERIMENT_NB = 0;
		} else {
			DIR_EXAMPLES = checkDir(Config.getStringParam("DIR_EXAMPLES"), true);
		}
		
		
		METAMODEL_NAME = Config.getStringParam("METAMODEL_NAME");
		
		File f = getInstancesDirectory();
		if (!f.exists() || !f.exists() || !f.isDirectory() ||f.listFiles().length <= 0) {
			LOGGER.severe("Could not open instances' repository : '" +f.getAbsolutePath() + "'");
		}
		
// Oracles
		DIR_ORACLES = checkDir(Config.getStringParam("DIR_ORACLES"), true);
		if (!getOraclesDirectory().exists()) {
			LOGGER.severe("Could not open oracles repository : '" + getOraclesDirectory().getAbsolutePath() +"'");
		}
		
// Results
		if(!EXPERIMENT_MODE) {
			DIR_RES = checkDir(Config.getStringParam("DIR_RESULTS"));
			if (!DIR_RES.exists())
				DIR_RES.mkdir();
		}
		
// Tests
		DIR_TESTS = checkDir(Config.getStringParam("DIR_TESTS"));
		if (!DIR_TESTS.exists())
			DIR_TESTS.mkdir();
		
//seed
		try {
			SEED = Config.getLongParam("SEED");
		} catch (Exception e) {
			SEED = new Random().nextLong();
			prop.put("SEED", SEED + "");
			LOGGER.warning("SEED generated randomly : " + SEED);
		} finally {
			ToolBox.initializeRandom();
		}
		
// verbose		
		VERBOSE_ON_FILE = Config.getBooleanParam("VERBOSE_ON_FILE");
		VERBOSE_ON_UI   = !SINBAD && Config.getBooleanParam("VERBOSE_ON_UI");
		Ui.PRINT_FIRSTS	= Config.getBooleanParam("PRINT_FIRSTS");
		
		
		if(!SINBAD){
			String tab =  Config.getStringParam("UI_OPENING_TAB");
			Ui.setOpeningTab(tab);
		}
		
		try {
			String objectiveTfidfVariantStr  = Config.getStringParam("OBJECTIVE_TFIDF");
			TFIDF = TFIDFVariant.valueOf(objectiveTfidfVariantStr.toUpperCase());
		} catch (Exception e) {
			LOGGER.warning("");
			TFIDF = TFIDFVariant.WITHOUT;
//			e.printStackTrace();
		}
		configLoaded = true;
	}

	
	public static File checkDir(String fileName) {
		return checkDir(fileName, false);
	}
	
	public static File checkDir(String fileName, boolean exitOnError) {
		File f = new File(fileName);
		if(!f.getAbsoluteFile().exists() || !f.isDirectory()){
			String message = f.exists()? " - Is not a directory.":" - Does not exists.";
			LOGGER.severe("Directory invalid: '"+f.getAbsolutePath()+"'"+message);
			if(exitOnError){
				LOGGER.severe("Exit.");
	    		System.exit(1);
			}
		}
		return f;
	}

	
	public static String printSetting(String escapeChar) {
		String s = escapeChar + "Setting : \n";
		Enumeration<?> e = prop.propertyNames();
		ArrayList<String> keyList = new ArrayList<>();

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			keyList.add(key);
		}
		Collections.sort(keyList);
		for (String key : keyList) {
			String value = prop.getProperty(key);
			s += escapeChar + " " + key + " : " + value + "\n";
		}
		
		s += "\n"+printOclTypes(escapeChar);

		return s;
	}
	public static String printOclTypes(String escapeChar) {
		String res = escapeChar+ " OCL Patterns : \n"+escapeChar+"  - Considered at begining :\n";
		for (PatternType pt : PatternFactory.ocl_choice_list) 
			res += escapeChar + "    + "+pt.getCompleteName() + "\n";
		res += escapeChar + " - Bannished :\n";
		
		ArrayList<PatternType> bs = new ArrayList<>(PatternFactory.getBannishedPatternTypes());
		
		Collections.sort(bs, (PatternType pt1, PatternType pt2) ->  pt1.getCompleteName().compareTo(pt2.getCompleteName()) );
		
		for (PatternType pt : bs) 
			res += escapeChar + "    + "+pt.getCompleteName() + "\n";
		
		return res + "\n";
	}


	public static int getIntParam(String key) {
		String value = prop.getProperty(key);
		return Integer.parseInt(value.trim());
	}

	public static boolean getBooleanParam(String key) {
		String value = prop.getProperty(key);
		return Boolean.parseBoolean(value.trim());
	}

	public static String getStringParam(String key) {
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

	
	/**
	 * TO BE UPDATED : thats just a print list.
	 * @return
	 */
	public static String printPrettySetting() {//to do: Make it fit better to current understanding !! 
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
		res += Metamodel.printCoverageDefinition();
		return res+"\n";
	}
	
	public static File getMetamodelFile() {
		if(metamodelFile == null)
			metamodelFile = new File(DIR_METAMODELS+File.separator+METAMODEL_NAME+".ecore");
		return metamodelFile;
	}
	static File metamodelFile;


	private static void setStringParam(String key, String value) {
		prop.setProperty(key, value);
	}
	
	/**
	 * 
	 * @return {@link Config.DIR_EXAMPLES DIR_EXAMPLES} / {@link Config.METAMODEL_NAME METAMODEL_NAME} /
	 */
	public static File getInstancesDirectory() {
		if (EXPERIMENT_MODE) {
			return checkDir(Config.getExperimentDirectory().getAbsolutePath() + File.separator + Config.METAMODEL_NAME + File.separator);
		} else
			return checkDir(Config.DIR_EXAMPLES.getAbsolutePath() + File.separator + Config.METAMODEL_NAME + File.separator);
	}
	
	public static String getExperiment() {
		return EXPERIMENT_SUB_FOLDERS[EXPERIMENT_NB];
	}
	
	/**
	 * Coupled with {@link #EXPERIMENT_SUB_FOLDERS} and {@link #EXPERIMENT_NB}
	 * @return
	 */
	private static File getExperimentDirectory() {
		return new File(Config.DIR_EXPERIMENT + File.separator + getExperiment());
	}

	/**
	 * Coupled with {@link #EXPERIMENT_SUB_FOLDERS} and {@link #EXPERIMENT_NB}
	 * @return
	 */
	public static String getRunFolderName() {
		return run_prefix + getExperiment();
	}


	/**
	 * 
	 * @return {@link Config.DIR_ORACLES DIR_ORACLES} / 		 
	 */
	public static File getOraclesDirectory(){
		return Config.DIR_ORACLES;
	}

	public static void overrideConfigFileParameter(String parameterName, String value) {
		setStringParam(parameterName, value);
	}

}

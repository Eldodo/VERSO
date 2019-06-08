package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import oclruler.ui.Ui;



public class Config {


	private static Logger LOGGER = Logger.getLogger(Config.class.getName());

	public static boolean VERBOSE_ON_FILE = false;
	public static boolean VERBOSE_ON_UI = true;
	
	public static boolean SINBAD = false;
	public static int NUMBER_OF_EXECUTIONS = 1;

	
	public static long SEED = (long) 0;

	static Properties prop = new Properties();
	public static boolean configLoaded = false;

	

	public static String METAMODEL_NAME;
	public static String DIR_METAMODELS;
	
	public static String DIR_RESULTS;
	public static String DIR_TESTS;
//	public static String DIR_OUT;
//	public static String DIR_NUM;

	public static void loadThemAll(String configFile) {
		configLoaded = false;
		// Initializing load calls
//		String filename = "./utils/config.properties";
		InputStream input = null;
		try {

			LOGGER.info("Loading config : " + configFile);
//			prop.load(new FileInputStream(new File(configFile)) );
			input = Config.class.getClassLoader().getResourceAsStream(configFile);
			if (input == null) {
//				LOGGER.severe("Sorry, unable to load config file : '" + configFile+"' ");
//				System.exit(1);
				input = new FileInputStream(configFile);
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
			LOGGER.severe("Sorry, unable to load config file : '" + configFile+"'\n -> "+ex.getMessage());
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
		configLoaded = true;
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
			s += escapeChar + key + " : " + value + "\n";
		}
		
		return s;
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

	public static void loadConfig() {
		// First load call
//metamodels
		String dirMM = Config.getStringParam("DIR_METAMODELS");
		if (new File(dirMM).exists())
			DIR_METAMODELS = Config.getStringParam("DIR_METAMODELS");
		else {
			LOGGER.warning("Metamodel directory '" + new File(dirMM).getAbsolutePath() + "' does not exist. Trying '" + new File(DIR_METAMODELS).getAbsolutePath() + "'...");
			if (!new File(DIR_METAMODELS).exists()) {
				LOGGER.severe("Metamodel directory '" + DIR_METAMODELS + "' does not exist.");
				throw new IllegalArgumentException("No metamodel directory found");
			} else {
				LOGGER.warning("Metamodel directory selected : '" + new File(DIR_METAMODELS).getAbsolutePath() + "'.");
			}
		}
//instances
		METAMODEL_NAME = Config.getStringParam("METAMODEL_NAME");
		
//results
		DIR_RESULTS = Config.getStringParam("DIR_RESULTS");
		if (!new File(DIR_RESULTS).exists())
			new File(DIR_RESULTS).mkdir();
		DIR_TESTS = Config.getStringParam("DIR_TESTS");
		if (!new File(DIR_TESTS).exists())
			new File(DIR_TESTS).mkdir();
		
//		DIR_OUT = buildResultsPath(Config.getStringParam("DIR_OUT"));
//		DIR_NUM = buildResultsPath(Config.getStringParam("DIR_NUM"));

//seed
		try {
			SEED = Config.getLongParam("SEED");
		} catch (Exception e) {
			SEED = new Random().nextLong();
			prop.put("SEED", SEED + "");
			LOGGER.warning("SEED generated randomly : " + SEED);
		} finally {
			Utils.initializeRandom();
		}
		
		try {
			SINBAD = Config.getBooleanParam("SINBAD");
		} catch (Exception e) {/* NOT ON SINBAD */ SINBAD = false;		}
		
		try {
			NUMBER_OF_EXECUTIONS = Config.getIntParam("NUMBER_OF_EXECUTIONS");
		} catch (Exception e) { NUMBER_OF_EXECUTIONS = 1;		}
		
		
		VERBOSE_ON_FILE = Config.getBooleanParam("VERBOSE_ON_FILE");
		VERBOSE_ON_UI   = Config.getBooleanParam("VERBOSE_ON_UI");
		Ui.PRINT_FIRSTS	= Config.getBooleanParam("PRINT_FIRSTS");
	}

	private static String buildResultsPath(String insideDir) {
		String res = Paths.get(DIR_RESULTS, METAMODEL_NAME).toString();
		if (!new File(res).exists())
			new File(res).mkdir();// Creation of 'results/' directory
		res = Paths.get(res, insideDir).toString() + File.separator;
		if (!new File(res).exists())
			new File(res).mkdir();
		return res;
	}
	
	/**
	 * TO BE UPDATED : thats just a print list.
	 * @return
	 */
	public static String printPrettySetting() {//TODO Make it fit better to current understanding !! 
		String res = "Setting : \n";
		
		res += "\nRAW :\n" + printSetting("  ");
		
		return res+"\n";
	}
	public static File getMetamodelFile() {
		if(metamodelFile == null)
			metamodelFile = new File(DIR_METAMODELS+File.separator+METAMODEL_NAME+".ecore");
		return metamodelFile;
	}
	static File metamodelFile;

}

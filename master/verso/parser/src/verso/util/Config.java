package verso.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;


public class Config {

	public static final Logger COV_DEF_LOGGER = Logger.getLogger("coverage.definition");




	
	
	public static String OS_HOME = System.getProperty("os.name").toLowerCase().contains("linux")?"/home/batotedo/":"R:/";
	
	
	public static final File irFolder = new File("irfolder");

	private static Logger LOGGER = Logger.getLogger(Config.class.getName());

	

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
	 *</ul>
	 */
	public static void loadConfig() {
		try {
			SINBAD = getBooleanParam("SINBAD");
		} catch (Exception e1) {
			SINBAD = false;
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
			s += escapeChar + " " + key + " : " + value + "\n";
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

	
	

	private static void setStringParam(String key, String value) {
		prop.setProperty(key, value);
	}
	
	public static void overrideConfigFileParameter(String parameterName, String value) {
		setStringParam(parameterName, value);
	}

}

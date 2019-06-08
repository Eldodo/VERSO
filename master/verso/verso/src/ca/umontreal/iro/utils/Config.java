package ca.umontreal.iro.utils;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import verso.graphics.primitives.CubeNoCapColored;
import verso.graphics.primitives.Primitive;
import verso.graphics.primitives.PrimitiveColored;


public class Config {
	
	public static String OS_HOME = System.getProperty("os.name").toLowerCase().contains("linux")?"/home/batotedo/":"R:/";
	private static Logger LOGGER = Logger.getLogger(Config.class.getName());
	
	public enum LAYOUTS {
		treemap, coliseum, radial
	}
	
	
	public static PrimitiveColored[] linkMesh = new CubeNoCapColored[] {new CubeNoCapColored (Color.black, Color.yellow), new CubeNoCapColored(Color.black, Color.yellow), new CubeNoCapColored(Color.black, Color.yellow)};
	public static Color linkStartColor = Color.black;
	public static Color linkEndColor = Color.yellow;
	public static Color linkBidirectionalColor = Color.magenta;
	
	
	public static Primitive packageNodeMesh = null;
	public static Primitive elementNodeMesh = null;
	public static float packageNodeSize = 0.5f;
	public static float elementNodeSize = 0.1f;
	public static Color packageNodeColor = Color.green;
	public static Color elementNodeColor = null;

	/** Coliseum layout related	 */
	public static float coliseumLevelHeight = 2.0f;
	
	
	public static boolean straightenControlPoints = true;
	public static int degree = 3;
	public static int nbreSegments = 30;
	public static boolean removeLCA = true;
	public static int nbreSides = 4;
	
	public static boolean straightenControlPoints_TM = true;
	public static int degree_TM = 3;
	public static int nbreSegments_TM = 30;
	public static boolean removeLCA_TM = true;
	public static int nbreSides_TM = 4;


	/**
	 * Default is "./oclruler/utils/config.properties"
	 */
	public static String CONFIG_FILE_NAME = "./oclruler/utils/config.properties";
	public static File CONFIG_FILE;	
	
	private static String IRFOLDER_PATH;
	public static File irFolder;
	
	public static File MISC_FOLDER;
	
	public static String helperPath;
	
	public static String srcFolderPath;
	
	public static long SEED = (long) 0;

	static Properties prop = new Properties();
//	static InputStream input = null;
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
//			input = Config.class.getClassLoader().getResourceAsStream(configFile);
			
			CONFIG_FILE = new File(configFile);
			
			//If condigFile not found, try {project_folder}/src/ configFile (different project configurations...)
			if(!CONFIG_FILE.exists())
				CONFIG_FILE = Paths.get("src", configFile).toFile();
			
//			if (input == null) {
//				LOGGER.severe("Sorry, unable to find " + configFile+"\nExit.");
//				System.exit(1);
//			}
			Properties preProp = (Properties)prop.clone();
//			prop.load(input);
			FileReader in = new FileReader(CONFIG_FILE);
			prop.load(in);
			in.close();
			String s = preProp.size()==1?"s":"";
			if(!preProp.isEmpty())
				LOGGER.info("Command line argument"+s+" override"+(preProp.size()==1?"":"s")+" key"+s+": "+preProp);
			System.out.println("preProp: "+preProp+" ("+preProp.size()+")");
			prop.putAll(preProp);

			Enumeration<?> e = prop.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = prop.getProperty(key);
				LOGGER.config(key + "\t : " + value);
			}
			
			IRFOLDER_PATH = getStringParam("IRFOLDER_PATH");
			if(IRFOLDER_PATH == null) {
				IRFOLDER_PATH = "./verso/irfolder/";
				LOGGER.warning("IRFOLDER_PATH set to "+IRFOLDER_PATH+" by default");
			}setIRFolder();
			
			String miscFolderPath = getStringParam("MISCFOLDER_PATH");
			if(miscFolderPath == null) {
				miscFolderPath = "./verso/data/Data_VERSO_HEB/";
				LOGGER.warning("MISC_FOLDER path set to "+miscFolderPath+" by default");
			}
			MISC_FOLDER = new File(miscFolderPath);
			
			helperPath = getStringParam("helperPath");
			if(helperPath == null) {
				helperPath = "./verso/helper/";
				LOGGER.warning("helperPath path set to "+helperPath+" by default");
			}

			srcFolderPath = getStringParam("srcFolderPath");
			if(srcFolderPath == null) {
				srcFolderPath = "./verso/data/Source_projects";
				LOGGER.warning("srcFolderPath path set to "+srcFolderPath+" by default");
			}
			
		} catch (IOException ex) {
			LOGGER.severe("Failure on loading file '" + configFile + "'");
			ex.printStackTrace();
//		} finally {
//			if (input != null) {
//				try {
//					input.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
		}
	}

	/**
	 * Loaded constants from configFile:
	 * <ul>
	 *  <li>{@link Config#SEED SEED}</li> 
	 *  <li>{@link #SINBAD} (Default= <code>true</code>)</li>
	 *</ul>
	 */
	public static void loadConfig() {
		try {
			SINBAD = getBooleanParam("SINBAD");
		} catch (Exception e1) {
			SINBAD = false;
		}
		
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

	
	/**
	 * TO BE UPDATED : thats just a print list.
	 * @return
	 */
	public static String printPrettySetting() {//to do: Make it fit better to current understanding !! 
		String res = "Setting : \n";
		
		
		res += "\nRAW :\n" + printSetting("  ");
		return res+"\n";
	}
	

	private static void setStringParam(String key, String value) {
		prop.setProperty(key, value);
	}
	

	public static void overrideConfigFileParameter(String parameterName, String value) {
		setStringParam(parameterName, value);
	}
	
	/**
	 * Lors du changement de nom du dossier oé se situe les IR filters change le chemin du dossier
	 * @param path Nouveau chemin
	 * @return le nouveau fichier oé doivent se situer les ir filters
	 */
	public static File setIrFolderPath(String path) {
		IRFOLDER_PATH = path;
		return setIRFolder();
	}
	
	/**
	 * Renvoie le dossier oé se situe les IR filters.
	 * Si ce dossier n'existe pas le crée.
	 * @see #setIrFolderPath(String) pour changer le chemin du dossier
	 * @return Le dossier oé se situe les IR filters
	 */
	private static File setIRFolder() {
		irFolder = new File(IRFOLDER_PATH);
		if(irFolder == null || !irFolder.exists()) {
			System.err.println("Illegal IR folder : '"+irFolder.getAbsolutePath()+"' does not exists.");
//			throw new IllegalStateException("Illegal IR folder : '"+irFolder.getAbsolutePath()+"' does not exists.");
			irFolder.mkdirs();
			System.out.println("Creation of ir filter directory "+irFolder.getAbsolutePath());
			System.out.println("Put IR filters in this directory\n");
		}else if(!irFolder.isDirectory())
			System.err.println("Illegal IR folder : '"+irFolder.getAbsolutePath()+"' is not a folder.");
//			throw new IllegalStateException("Illegal IR folder : '"+irFolder.getAbsolutePath()+"' is not a folder.");
		return irFolder;
	}

}

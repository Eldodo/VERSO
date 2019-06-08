package ca.umontreal.iro.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Logger;


/**
 * 
 * @author Edouard Batot 2017 - batotedo@iro.umontreal.ca
 *
 */
public class ToolBox {
	public final static Logger LOGGER = Logger.getLogger(ToolBox.class.getName());

	public static final String TAB_CHAR = "  ";

	private static Random random;

	/**
	 * 
	 */
	public static void loadConfig() {
		loadConfig(Config.CONFIG_FILE_NAME);
	}

	public static void loadConfig(String configFilePath) {
		if (!Config.configLoaded) {
			Config.CONFIG_FILE_NAME = configFilePath == null ? Config.CONFIG_FILE_NAME : configFilePath;

			LOGGER.info("Loading config : " + Config.CONFIG_FILE_NAME + "\n----------  ");
			Config.loadThemAll(Config.CONFIG_FILE_NAME);
			Config.loadConfig();
			
			
		}
		
	}

	/**
	 * Initialize app with {@link Config#CONFIG_FILE_NAME default configuration file}
	 * 
	 * @return log
	 */
	public static String init() {
		return init(Config.CONFIG_FILE_NAME);
	}

	/**
	 * Initialize app with a specific configuration file.
	 * 
	 * @param configFilePath
	 * @return log
	 */
	public static String init(String configFilePath) {
		loadConfig(configFilePath);

		
		return "init done.";
	}
	
	/**
	 * 
	 * <ol>
	 *  <li>Initialize a seed for Randoms</li>
	 *  <li>{@link #loadConfig()} register all properties in confg file</li>
	 *</ol>
	 */
	public static void initMinimal() {
		initializeRandom();
		loadConfig();
	}
	
	
	public static String START_TIME  = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.FRANCE).format( new Date() );
	public static long   START_LONG  = System.currentTimeMillis();

	public static ArrayList<File> getPrefixedFiles(String prefix, String folderName) {
		ArrayList<File> res = new ArrayList<File>();
		File folder = new File(folderName);
		if (!folder.isDirectory())
			throw new IllegalArgumentException("'" + folderName + "' is not a directory.");
		for (File file : folder.listFiles()) {
			if (file.getName().startsWith(prefix))
				res.add(file);
		}
		return res;
	}
	
	

	public static void copyFile(File toCopy, File targetDir, String newName, boolean fake) {
		java.nio.file.Path pSource = Paths.get(toCopy.getAbsolutePath());
		java.nio.file.Path pTarget = Paths.get(targetDir.getAbsolutePath(), newName);
		if (fake)
			System.out.println("ToolBox.copyFile[FAKE](" + pSource + ", " + pTarget + ") ");
		else
			try {
				Files.copy(pSource, pTarget, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public static void copyFile(File toCopy, File targetDir) {
		copyFile(toCopy, targetDir, toCopy.getName(), false);
	}
	
	public static void copyFile(File toCopy, File targetDir, String newName) {
		copyFile(toCopy, targetDir, newName, false);
	}

	public static File[] listXMIFiles(File folder){
		File[] res = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith("xmi");
			}
		});
		return res;
	}
	

	public static String readFile(String fileName) throws IOException {
		return readFile(new File(fileName));
	}

	public static String readFile(File file) throws IOException {
		String res = "", s;
		BufferedReader br3 = new BufferedReader(new FileReader(file));
		while ((s = br3.readLine()) != null)
			res += s + "\n";
		br3.close();
		s = null;
		return res.trim() + "\n";
	}
	
	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}


	public static void initializeRandom() {
		random = new Random(Config.SEED);
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

	/**
	 * Exclusive bound !
	 * 
	 * @param bound
	 *            EXCLUSIVE
	 * @return
	 */
	public static int getRandomInt(int bound) {
		return random.nextInt(bound);
	}

	/**
	 * 
	 * @param coll
	 * @return -1 if coll is empty.
	 */
	public static int getRandomIdx(Collection<?> coll) {
		if (coll.isEmpty())
			return -1;
		return getRandomInt(coll.size());
	}

	/**
	 * 
	 * @param lower
	 *            INCLUSIVE
	 * @param upper
	 *            EXCLUSIVE
	 * @return
	 */
	public static int getRandomInt(int lower, int upper) {
		if (lower == upper)
			return lower;
		return lower + random.nextInt(upper - lower);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getRandom(Collection<T> c) {
		if (c.size() <= 0)
			return null;
		return (T) c.toArray()[ToolBox.getRandomInt(c.size())];
	}

	public static <T> T getRandom(T[] c) {
		if (c.length <= 0)
			return null;
		return (T) c[ToolBox.getRandomInt(c.length)];
	}

	public static <T> ArrayList<Collection<T>> split(Collection<T> c) {
		if (c.size() < 0)
			return null;
		int cutPoint = c.size() / 2;
		ArrayList<T> res1 = new ArrayList<T>();
		ArrayList<T> res2 = new ArrayList<T>();
		int i = 0;
		for (T t : c) {
			if (i < cutPoint)
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

		

	
	
	public static double format2Decimals(float f) {
		return ((int) (f * 100)) / 100.0;
	}

	/**
	 * Conversion in hh:mn:sec:ms
	 * 
	 * @param millis
	 * @return
	 */
	public static String formatMillis(long millis) {
		long nb_hh = millis / (60 * 60 * 1000);
		long reste_milli = millis - (nb_hh * 60 * 60 * 1000);
		long nb_min = reste_milli / (60 * 1000);
		reste_milli = reste_milli - (nb_min * 60 * 1000);
		long nb_sec = reste_milli / 1000;
		reste_milli = reste_milli - (nb_sec * 1000);
		long nb_mil = reste_milli;
		return "" + nb_hh + ":" + nb_min + "'" + nb_sec + "''" + nb_mil;
	}

	public static String completeString(String s, char c, int length) {
		String res = s;
		for (int i = 0; i < length - s.length(); i++)
			res += c;
		return res;
	}
	
	public static String completeString(String s, int length) {
		return completeString(s, ' ', length);
	}

	


	
//	public static void printArgs(CommandLine cl) {
//		String res = "";
//		for (Option o : cl.getOptions()) {
//				res += " - " +o.getLongOpt()+" : "+ Arrays.toString(o.getValues()) + "\n";
//		}
//		if(!res.isEmpty())
//			System.out.println("Args:\n"+res);
//		else
//			System.out.println("No argument passed.");
//	}
	

	

	

	


	public static void printLOC() {
		int[] i;
		try {
			i = countLOC(new File("./src"));
			System.out.println("Main.main(src:" + i[0] + ") (" + i[1] + " classes)");
			i = countLOC(new File("./test"));
			System.out.println("Main.main(test:" + i[0] + ")");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static int[] countLOC(File f) throws IOException {
		int[] res = new int[] { 0, 0 };
		if (f.getName().endsWith(".java"))
			res[1]++;
		if (f.getName().startsWith("result"))
			return res;
		if (f.isDirectory()) {
			for (File f2 : f.listFiles()) {
				res[0] += countLOC(f2)[0];
				res[1] += countLOC(f2)[1];
			}
			// System.out.println("Dir:"+f.getCanonicalPath()+" : "+res);
		} else {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = "";
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty())
					res[0]++;
			}
			br.close();
			// System.out.println(f.getCanonicalPath()+" : "+res);
		}
		return res;
	}

	public static <T> double[] precisionRecallF(Collection<T> results, Collection<T> expected) {
	
		int recallCnt = 0;
		for (T t : results) {
			if (expected.contains(t))
				recallCnt++;
		}
	
		int precisionCnt = 0;
		for (T t : expected) {
			if (results.contains(t))
				precisionCnt++;
		}
		double precision = (double) precisionCnt / results.size();
		double recall 	 = (double) recallCnt / expected.size();
	
		double F;
		try {
			if (precision + recall == 0)
				F = 0.0;
			else
				F = 2 * ((double) precision * recall) / (precision + recall);
		} catch (Exception e) {
			F = 0.0;
			e.printStackTrace();
		}
	
		return new double[] { precision, recall, F };
	}


	public static File[] listDirecories(File f){
		return listDirecories(f, null);
	}
	
	public static File[] listDirecories(File f, String prefix){
		File[] fs = f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() && pathname.getName().startsWith(prefix != null ? prefix : "");
			}
		});
		if(fs != null)
			Arrays.sort(fs);
		return fs;
	}

	public static File[] listFiles(File f){
		File[] fs =  f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !pathname.isDirectory();
			}
		});
		Arrays.sort(fs);
		return fs;
	}


}

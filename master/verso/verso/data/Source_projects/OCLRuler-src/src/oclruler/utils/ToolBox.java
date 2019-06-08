package oclruler.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import oclruler.genetics.Evaluator;
import oclruler.genetics.EvaluatorOCL;
import oclruler.genetics.Evolutioner;
import oclruler.genetics.FitnessVector;
import oclruler.genetics.GeneticIndividual;
import oclruler.genetics.Oracle;
import oclruler.genetics.OraculizationException;
import oclruler.genetics.OraculizationException.RATIONALE;
import oclruler.genetics.Population;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Model;
import oclruler.metamodel.NamedEntity;
import oclruler.rule.PatternFactory;
import oclruler.rule.Program;
import oclruler.rule.struct.Constraint;
import oclruler.rule.struct.Node;
import oclruler.rule.struct.NodeFactory;
import oclruler.ui.Ui;
import oclruler.utils.Config.TFIDFVariant;

/**
 * 
 * @author Edouard Batot 2017 - batotedo@iro.umontreal.ca
 *
 */
@SuppressWarnings("deprecation")
public class ToolBox {
	public final static Logger LOGGER = Logger.getLogger(ToolBox.class.getName());

	public static final String TAB_CHAR = "  ";

	private static final String SOLUTION_DIR_NAME = "_solutions";
	private static final String ORACLE_DIR_NAME = "_oracle";
	private static final String SETTING_DIR_NAME = "_setting";

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
			
			// WARNING - loadConfig ORDER MATTERS !!
			{
				Program.loadConfig(); // No dependency
				Constraint.loadConfig(); // No dependency
				NodeFactory.loadConfig(); // No dependency
				Node.loadConfig(); // No dependency
				PatternFactory.loadConfig();// No dependency
				FitnessVector.loadConfig();// No dependency
				Population.loadConfig();// No dependency
				Evaluator.loadConfig();// No dependency
				Evolutioner.loadConfig(); // FitnessVector must be configured
			}
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
		Metamodel.init();
		PatternFactory.init();

		ExampleSet.loadConfig();
		String endCondition = "";
		if (Evolutioner.END_CONDITION_TYPE != null) {
			endCondition += Evolutioner.END_CONDITION_TYPE;
			for (int i = 0; i < FitnessVector.OBJECTIVES_CONSIDERED; i++) {
				if (i < Evolutioner.OBJECTIVES_END_CONDITION.length)
					endCondition += " " + Evolutioner.OBJECTIVES_END_CONDITION[i];
				else
					endCondition += " xx";
			}
		} else
			endCondition = "None";
		ExampleSet ms = ExampleSet.getInstance();
		Evaluator eva = EvaluatorOCL.getInstance();
		Oracle o = Oracle.instantiateOracle(ms);
		
		String log = "\n        Metamodel loaded : " + Config.METAMODEL_NAME + " (" + Metamodel.metamodelResource.getURI() + ")"
				+ "\n      Genetic parameters : " + Population.NB_ENTITIES_IN_POP + "e X " + Evolutioner.GENERATION_MAX + " generations max. (cross:"+Population.CROSSING_RATE+",mut:"+Population.MUTATE_RATE+")"
				+ "\n     Examples' directory : " + ExampleSet.getNbModels() + " from '" + Config.getInstancesDirectory().getAbsolutePath() + "'"
				+ "\n                  Oracle : " +  Oracle.getInstance().getConstraints().size() + " rules from '" + Oracle.getInstance().getTextRulesDirName() + "'"
				+ "\n   Objectives considered : " + FitnessVector.OBJECTIVES_CONSIDERED + "" 
				+ "\n           End condition : " + endCondition
				+ (Evolutioner.STEP_BY_STEP ? "\n      (STEP_BY_STEP mode : activated)" : "") 
				+ "\n              Stationary : " + Evolutioner.STATIONARY_TYPE	+ " " + Evolutioner.STATIONARY_TIME + " (" + Evolutioner.STATIONARY_VARIANCE + ")"
				+ (Evaluator.EXECUTION_GRAIN.isRaw() ? "\n               (RAW mode : activated)" : "") 
				+ "\n                   (Seed : " + Config.SEED + ")"
				+ (Evaluator.MULTI_THREAD ? "**" + "\n                   (Multithread)" : "")
				+ (!(Config.TFIDF == Config.TFIDFVariant.WITHOUT) ? "\n                       --\n                   (TFIDF on "+Config.TFIDF.prettyPrint()+")" : "") 
				+ "\n";
		LOGGER.info("\n---------- \nInitialization done. \nSetting : " + log);

		long t = System.currentTimeMillis();
		try {
			o.oraculize(eva);
		} catch (OraculizationException e) {
			LOGGER.severe(e.getMessage());
			if (e.getRationale() != RATIONALE.NO_EXAMPLE ) {
				if(Config.VERBOSE_ON_UI){
					Ui.showExampleDebugUi("No " + (e.getRationale() == RATIONALE.NO_POSITIVE ? "positive" : "negative") + " examples");
					new Scanner(System.in).nextLine();
				}
			}
			e.printStackTrace();
			LOGGER.severe("Exit.");
			System.exit(1);
		}
		LOGGER.finer("Oraculization lasted " + (System.currentTimeMillis() - t) + "ms.");

		LOGGER.fine(o.prettyPrint());
		LOGGER.fine(ms.toString());

		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("Fires :");
			int sum = 0;
			for (Constraint p : o.getConstraints()) {
				LOGGER.finer(" - " + p.getId() + " : " + p.getFires());
				sum += p.getFires();
			}
			LOGGER.finer("    -> " + sum + " fire" + (sum > 1 ? "s" : ""));
		}
		
		
		return log;
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
	
	/**
	 * Directory for result (.../DIR_RES/MM name/TimeStamp)
	 */
//	public static File DIR_RES = null;
	
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
	
	
	/**
	 * Builds DIR_RES from a base dir and a subdir.
	 * @param baseDir
	 * @param insideDir
	 * @return
	 */
	public static File touchResultsMainDirectory(String baseDir, String insideDir) {
		String res = Paths.get(baseDir, Config.METAMODEL_NAME).toString();
		if (!new File(res).exists())
			new File(res).mkdir();// Creation of 'results/MMName' directory
		
		res = Paths.get(res, insideDir).toString() + File.separator;
		Config.DIR_RES = new File(res);
		if (!Config.DIR_RES.exists())
			Config.DIR_RES.mkdir();
		return Config.DIR_RES;
	}
	
	public static File touchResultsMainDirectory(String insideDir) {
		return touchResultsMainDirectory(Config.DIR_RES.getAbsolutePath(), insideDir);
	}
	
	/**
	 * Creates sub directory to DIR_RES
	 * @param name
	 * @return
	 */
	public static File touchResultSubDirectory(String name){
		String resStr = Paths.get(Config.DIR_RES.getAbsolutePath(), name).toString() + File.separator;
		File res = new File(resStr);
		if (!res.exists())
			res.mkdir();
		
		return res;
	}
	
	/**
	 * Files copied in {res}/setting
	 * <ol>
	 *  <li>Config.CONFIG_FILE</li>
	 *  <li>PatternFactory.OCL_CHOICE_FILE</li>
	 *  <li>Metamodel.COV_DEF_FILE</li>
	 *</ol>
	 * @return
	 */
	public static File buildResultsSettingDirectory() {
		File res = touchResultSubDirectory(SETTING_DIR_NAME);
		copyFile(Config.CONFIG_FILE, res);
		if(PatternFactory.OCL_CHOICE_FILE != null)
			copyFile(PatternFactory.OCL_CHOICE_FILE, res);
		if(Metamodel.COV_DEF_FILE != null && Metamodel.COV_DEF_FILE.exists())
			copyFile(Metamodel.COV_DEF_FILE, res);
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
	
	public static File touchSolutionDirectory() {
		return touchResultSubDirectory(SOLUTION_DIR_NAME);
	}

	public static File buildResultsOracleDirectory() {
		File res = touchResultSubDirectory(ORACLE_DIR_NAME);
		
		//Copy XMIs
		ExampleSet.getInstance().copyExamplesTo(res); 
		
		//Print Oracle XMIs' statistics
		File statFile = new File(Paths.get(res.getAbsolutePath(), "OracleStatistics_extended.log").toString());
		File statFileShort = new File(Paths.get(res.getAbsolutePath(), "OracleStatistics.log").toString());
		ModelsRepositoryStatistics.buildAndWriteModelRepositoryStatistics(statFile, statFileShort, res);
		return res;
	}
	
	public static String[] createEvolutionLogFileNames(){
		String run = getRunSubFolderName();
		String base = Config.DIR_RES.getAbsolutePath()+File.separator+run+""+Config.METAMODEL_NAME+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+ToolBox.START_TIME;
		String res1 = base+".log";
		String res2 = base+".data.log";
		int itmp = 1;
    	while(new File(res1).exists() || new File(res2).exists()){
//    		base = Config.DIR_OUT+"log_g_"+Config.METAMODEL_NAME+"_"+evaluator.getClass().getSimpleName()+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+timeStamp2;
    		res1 = base+"-"+itmp+".log";
        	res2 = base+"-"+itmp+".data.log";
        	itmp++;
    	}
		return new String[] { res1, res2};
	}
	
	/**
	 * During experiment, return "run_I", otherwise "".
	 * @return
	 */
	public static String getRunSubFolderName() {
		if(Config.EXPERIMENT_MODE){
			return Config.getRunFolderName()+File.separator;
		}
		return "";
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

	public static HashMap<Model, String> readXMIs(ArrayList<Model> models) {
		HashMap<Model, String> res = new HashMap<>(models.size());
		for (Model m : models) {
			String xmi = "";
			String l = null;
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(m.getFile()));
				while ((l = br.readLine()) != null)
					xmi += l + "\n";
			} catch (IOException e) {
				e.printStackTrace();
			}
			res.put(m, xmi);
		}
		return res;
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
	 * @return -1 if patterns is empty.
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

	public static <T extends NamedEntity, E extends NamedEntity> String printMultimap(WeakHashMap<T, ArrayList<E>> map, String pre) {
		String res = "";
		for (T key : map.keySet()) {
			res += pre + key.getName() + " :" + "\n";
			for (NamedEntity ne : map.get(key)) {
				res += pre + "  " + ne.simplePrint() + "\n";
			}
		}
		return res;
	}

	public static String printMultimapEClasses(HashMap<EClass, HashSet<EClass>> map, String pre) {
		String res = "";
		for (EClass key : map.keySet()) {
			res += pre + key.getName() + ":";
			res += printEElements(map.get(key));
			res += "\n";
		}
		return res;
	}

	public static String printEElements(Collection<? extends ENamedElement> list) {
		String res = "{";
		for (ENamedElement one : list) {
			if (one instanceof EStructuralFeature)
				res += printCard((EStructuralFeature) one);
			res += one.getName() + ",";
		}
		if (res.endsWith(","))
			res = res.substring(0, res.length() - 1);
		return res + "}";
	}

	static HashMap<EClass, Integer> esfSize = new HashMap<>();
	
	public static String printEStructuralFeatures(EObject eo) {
		String res = eo.eClass().getName() + ":\n";
		
		int sizeNameEsf = getSizeEsfNames(eo.eClass());
		
		for (EStructuralFeature esf : eo.eClass().getEAllStructuralFeatures()) {
			Object e = eo.eGet(esf, true);
//			String eget = (!(e instanceof Collection) ? printEObjectNameOrID(e) + "" : "");
			String eget =  printEObjectNameOrID(e);
			if (e != null) {
				eget += " "+printEClasses(e);
			}
			res += " - " + completeString(esf.getName(), sizeNameEsf) + ": " + eget + /* " (from "+esf.getContainerClass().getSimpleName() + ") "+ */" \n";
		}
		return res.trim();
	}
	
	public static HashMap<String, Collection<Object>> getEStructuralFeaturesObjects(EObject eo) {
		return getEStructuralFeaturesObjects(eo, new String[] {});
	}
	public static HashMap<String, Collection<Object>> getEStructuralFeaturesObjects(EObject eo, String[] nameExclusion) {
		HashMap<String, Collection<Object>> res = new HashMap<>();
		for (EStructuralFeature esf : eo.eClass().getEAllStructuralFeatures()) {
			boolean include = true;
			for (String string : nameExclusion) {
				if (string.equals(esf.getName())) {
					include = false;
					break;
				}
			}
			if (include) {
				Collection<Object> eos = getEObjectStructuralFeature(eo, esf.getName());
				res.put(esf.getName(), eos);
			}
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<Object> getEObjectStructuralFeature(EObject eo, String structuralFeatureName){
		if(eo.eClass().getEStructuralFeature(structuralFeatureName) == null)
			return Collections.emptySet();
		Object argument = eo.eGet(eo.eClass().getEStructuralFeature(structuralFeatureName));
		if(argument instanceof Collection)
			return (Collection<Object>)argument; 
		ArrayList<Object> eos = new ArrayList<>(1);
		eos.add((Object)argument);
		return eos;
		
	}


	public static int getSizeEsfNames(EClass ec) {
		int sizeNameEsf = esfSize.getOrDefault(ec, -1);
		if(sizeNameEsf < 0)
			sizeNameEsf = computeSizeEsfNames(ec);
		return sizeNameEsf;
	}

	
	public static int computeSizeEsfNames(EClass ec) {
		int res = -1;
		for (EStructuralFeature esf : ec.getEAllStructuralFeatures()) {
			int tmp = esf.getName().length();
			if(tmp > res)
				res = tmp;
		}
		esfSize.put(ec, res);
		return res;
	}

	/**
	 * Print an object e.<br/>
	 * Depending of the case, print will be:
	 * <ol>
	 * <li>e has an ID attribute</li>
	 * <li>e has a structural reference named "ID"</li>
	 * <li>e has a structural reference named "name"</li>
	 * </ol>
	 * 
	 * @param e
	 * @return "e.[name/ID] (structural feature used)"
	 */
	@SuppressWarnings("unchecked")
	private static String printEObjectNameOrID(Object e) {
		if (e instanceof EObject) {
			EObject eo = (EObject) e;
			for (EStructuralFeature esf : eo.eClass().getEAllStructuralFeatures()) {
				if (esf instanceof EAttribute) {
					if (((EAttribute) esf).isID())
						return eo.eGet(esf) + " (ID:" + esf.getName() + ")";
				}
			}
			for (EStructuralFeature esf : eo.eClass().getEAllStructuralFeatures()) {
				if (esf.getName().equalsIgnoreCase("id"))
					return eo.eGet(esf) + " (" + esf.getName() + ")";
			}
			for (EStructuralFeature esf : eo.eClass().getEAllStructuralFeatures()) {
				if (esf instanceof EAttribute) {
					if (esf.getName().equalsIgnoreCase("name"))
						return eo.eGet(esf) + " (" + esf.getName() + ")";
				}
			}

		}
		if (e instanceof Collection){
			String res = "{";
			int nbObjects = ((Collection<Object>)e).size();
			int i = 0;
			EStructuralFeature esfID = null;
			for (Object o : ((Collection<Object>)e)) {
				
				i++;
				if(o instanceof EObject){
					EObject eo = (EObject)o;
					if(i <= 1){ // Find the ID : isID ? "id" attribute ? "name" attribute ?
						for (EStructuralFeature esf : eo.eClass().getEAllStructuralFeatures()) {
							if (esf instanceof EAttribute && ((EAttribute) esf).isID())
									esfID = esf;
						}
						if(esfID == null){
							EStructuralFeature esfIDStr = eo.eClass().getEStructuralFeature("id");
							EStructuralFeature esfName = eo.eClass().getEStructuralFeature("name");
							if (esfIDStr != null){
								esfID = esfIDStr;
							} else if (esfName != null)
								esfID = esfName;
						}
					}
					if(esfID != null && eo.eClass().getEStructuralFeatures().contains(esfID))
						res += eo.eGet(esfID, false)   + (i<nbObjects?", ":"");
					else
						res += eo + (i<nbObjects?", ":"");
				}
			}
			e = res + "}";
		}
		return e + "";
	}

	/**
	 * @param e
	 * @param eget
	 * @return
	 */
	public static String printEClasses(Object e) {
		String eget = "";
		if (e instanceof EObject)
			eget = " [" + ((EObject) e).eClass().getName() + "]";

		if (e instanceof Collection<?>) {
			@SuppressWarnings("unchecked")
			Collection<EObject> ecoll = ((Collection<EObject>) e);
			eget = ecoll.size() + " objets " + printEClasseList(ecoll);
		}
		return eget;
	}

	public static String printEClasseList(Collection<EObject> collection) {
		HashSet<String> hs = new HashSet<>(1);
		for (EObject eo : collection)
			hs.add(eo.eClass().getName());
		String res = "[";
		if (hs.size() > 0) {
			for (String ecstr : hs)
				res += ecstr + ", ";
			res = res.substring(0, res.length() - 2);
		}
		return res + "]";
	}

	public static String printCard(EStructuralFeature esf) {
		if (esf.getLowerBound() == 1 && esf.getUpperBound() == 1)
			return "";
		if (esf.getLowerBound() == 0 && esf.getUpperBound() == 1)
			return "?";
		if (esf.getLowerBound() == 1 && esf.getUpperBound() == -1)
			return "+";
		if (esf.getLowerBound() == 0 && esf.getUpperBound() == -1)
			return "*";
		return "";
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

	public static void storeNumericResults(File f, GeneticIndividual e, String timeStamp, String elapsedTime) {
		try {
			if (!f.exists()) {
				f.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
				bw.append(GeneticIndividual.printResultHeader(";"));
				bw.close();
			}
			LOGGER.info("Writing " + f + "  ");
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
			bw.append(e.printNumericResult(";", timeStamp, elapsedTime, false) + "\n");
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	
	/*
	 * Command line section
	 */
	
	 static final String O_CONFIG_FILE = "cf";
	 static final String O_CONFIG_FILE_LONG = "config-file";
	 static final String O_TFIDF = "ti";
	 static final String O_TFIDF_LONG = "tfidf";
	 static final String O_RANDOM_RUN = "rnd";
	 static final String O_RANDOM_RUN_LONG = "random-run";
	 static final String O_PATTERNS_FILE = "pf";
	 static final String O_PATTERNS_FILE_LONG = "pattern-selection-file";
	 static final String O_COVDEF_FILE = "cov";
	 static final String O_COVDEF_FILE_LONG = "coverage-definition-file";
	 static final String O_EXAMPLE_EDITION = "exedit";
	 static final String O_EXAMPLE_EDITION_LONG = "example-edition-mode";
	 static final String O_DIR_EXPERIMENT = "ed";
	 static final String O_DIR_EXPERIMENT_LONG = "experiment-directory";
	 static final String O_METAMODEL_NAME = "mm";
	 static final String O_METAMODEL_NAME_LONG = "metamodel-name";
	 static final String O_NUMBER_OF_RUNS = "nor";
	 static final String O_NUMBER_OF_RUNS_LONG = "number-of-runs";

		public static String getHelp_CoommandOptionDirExperiment() {
			Option opt = options.getOption(O_DIR_EXPERIMENT);
			return "'"+opt.getOpt()+"' (long is '"+opt.getLongOpt()+"')\n   "+opt.getDescription();
		}
		public static String getHelp_CoommandOptionMetamodelName() {
			Option opt = options.getOption(O_METAMODEL_NAME);
			return "'"+opt.getOpt()+"' (long is '"+opt.getLongOpt()+"')\n   "+opt.getDescription();
		}
	
	static Options options;
	static CommandLine commandLine;
	/**
	 * 
	 * Load aplication arguments: 
	 * <ol>
	 *  <li>config file to load {@link ToolBox#init(String) }</li>
	 *  <li>pattern file into {@link PatternFactory#OCL_CHOICE_FILE_NAME }</li>
	 *  <li>{@link Config#EXAMPLE_EDITION_MODE} chosen or not</li>
	 *  <li>experiment directory {@link Config#DIR_EXPERIMENT} in case of multi runs</li>
	 * </ol>
	 * 
	 * @param args
	 * @return
	 */
	public static void loadArgs(String[] args) {
		options = configureOptions();
		CommandLineParser parser = new DefaultParser();
		try {
			commandLine = parser.parse(options, args);
			
		    if( commandLine.hasOption( O_CONFIG_FILE ) ) {
		    	Config.CONFIG_FILE_NAME = commandLine.getOptionValue(O_CONFIG_FILE);
		    } else {
		    	//Config.CONFIG_FILE_NAME not changed
		    }
		    if( commandLine.hasOption( O_TFIDF ) ) {
		    	String tfidf = commandLine.getOptionValue(O_TFIDF);
		    	Config.TFIDF = TFIDFVariant.valueOf(tfidf.toUpperCase());
		    	Config.overrideConfigFileParameter("OBJECTIVE_TFIDF", Config.TFIDF.toString());
		    } else {
		    	//Config.OBJECTIVE_TFIDF not changed
		    }
		    
		    if( commandLine.hasOption( O_RANDOM_RUN ) ) {
		    	Config.IS_RANDOM_RUN = commandLine.getOptionValue(O_RANDOM_RUN).equalsIgnoreCase("on");
		    } else {
		    	//Config.IS_RANDOM_RUN not changed
		    }
		    
		    if( commandLine.hasOption( O_PATTERNS_FILE ) ) {
		    	PatternFactory.OCL_CHOICE_FILE_NAME = commandLine.getOptionValue(O_PATTERNS_FILE);
		    	Config.overrideConfigFileParameter("OCL_CHOICE_FILE", PatternFactory.OCL_CHOICE_FILE_NAME);
		    } else {
		    	PatternFactory.OCL_CHOICE_FILE_NAME = null;
		    }
		    
		    if( commandLine.hasOption( O_COVDEF_FILE ) ) {
		    	Metamodel.COV_DEF_FILE = new File(commandLine.getOptionValue(O_COVDEF_FILE));
		    } else {
		    	Metamodel.COV_DEF_FILE = null;
		    }
		    
		    if( commandLine.hasOption( O_EXAMPLE_EDITION ) ) {
		    	Config.EXAMPLE_EDITION_MODE = true;
		    } else {
		    	Config.EXAMPLE_EDITION_MODE = false;
		    }
		    
		    if( commandLine.hasOption( O_DIR_EXPERIMENT ) ) {
		    	Config.DIR_EXPERIMENT = Config.checkDir(commandLine.getOptionValue(O_DIR_EXPERIMENT), true);
		    	Config.overrideConfigFileParameter("DIR_EXPERIMENT", Config.DIR_EXPERIMENT.getAbsolutePath());
		    } else {
		    	//Config.DIR_EXPERIMENT not changed
		    }
		    
		    if( commandLine.hasOption( O_METAMODEL_NAME ) ) {
		    	Config.METAMODEL_NAME = commandLine.getOptionValue(O_METAMODEL_NAME);
		    	Config.overrideConfigFileParameter("METAMODEL_NAME", Config.METAMODEL_NAME);
		    } else {
		    	//Config.DIR_EXPERIMENT not changed
		    }
		    
		} catch (ParseException e) {
			LOGGER.severe("Command line options invalid : "+e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth(140);
			System.out.println();
			formatter.printHelp("java -jar OCLRuler.jar", options, true);
			
//			System.out.println("\nDefault config file is '"+Config.CONFIG_FILE_NAME+"'.\n Continue ?");
//			@SuppressWarnings("resource")
//			String yn = new Scanner(System.in).nextLine();
//			if(! yn.equalsIgnoreCase("y") && !yn.equalsIgnoreCase("yes")) {
//				LOGGER.info("Exit.");
//				System.exit(1);
//			}
		}
	}


	
	public static void printArgs() {
		printArgs(commandLine);
	}
	
	public static void printArgs(CommandLine cl) {
		String res = "";
		for (Option o : cl.getOptions()) {
				res += " - " +o.getLongOpt()+" : "+ Arrays.toString(o.getValues()) + "\n";
		}
		if(!res.isEmpty())
			System.out.println("Args:\n"+res);
		else
			System.out.println("No argument passed.");
	}
	
	private static Options configureOptions() {
		Option tfidfOption = OptionBuilder.create(O_TFIDF);
		tfidfOption.setLongOpt(O_TFIDF_LONG);
		tfidfOption.setArgName("tfidf");
		tfidfOption.setDescription("use <tfidf> ON.");
		tfidfOption.setType(String.class);
		tfidfOption.setArgs(1);

		Option rndrunOption = OptionBuilder.create(O_RANDOM_RUN);
		rndrunOption.setLongOpt(O_RANDOM_RUN_LONG);
		rndrunOption.setArgName("rnd");
		rndrunOption.setDescription("use <random-run> ON.");
		rndrunOption.setType(String.class);
		rndrunOption.setArgs(1);

		Option configFileOption = OptionBuilder.create(O_CONFIG_FILE);
		configFileOption.setLongOpt(O_CONFIG_FILE_LONG);
		configFileOption.setArgName("config-file");
		configFileOption.setDescription("use <config-file>. (defaults use '/bin/utils/config.properties'");
		configFileOption.setType(String.class);
		configFileOption.setArgs(1);

		Option patternsFileOption = OptionBuilder.create(O_PATTERNS_FILE);
		patternsFileOption.setLongOpt(O_PATTERNS_FILE_LONG);
		patternsFileOption.setArgName("logging-file");
		patternsFileOption.setDescription("use <logging-file>. (defaults use '/lib/logging.finest.properties')");
		patternsFileOption.setType(String.class);
		patternsFileOption.setArgs(1);
		
		Option coverageDefinitionOption = OptionBuilder.create(O_COVDEF_FILE);
		coverageDefinitionOption.setLongOpt(O_COVDEF_FILE_LONG);
		coverageDefinitionOption.setArgName("coverage-definition-file");
		coverageDefinitionOption.setDescription("use <coverage-definition-file>. (defaults is none: whole metamodel considered)");
		coverageDefinitionOption.setType(String.class);
		coverageDefinitionOption.setArgs(1);
		
		Option exampleEditionGUIOption = OptionBuilder.create(O_EXAMPLE_EDITION);
		exampleEditionGUIOption.setLongOpt(O_EXAMPLE_EDITION_LONG);
		exampleEditionGUIOption.setArgName("example-edition-mode");
		exampleEditionGUIOption.setDescription("use <example-edition-mode> to open GUI examples edition");
		exampleEditionGUIOption.setType(Boolean.class);
		exampleEditionGUIOption.setArgs(0);
		
		Option exampleDirOption = OptionBuilder.create(O_DIR_EXPERIMENT);
		exampleDirOption.setLongOpt(O_DIR_EXPERIMENT_LONG);
		exampleDirOption.setArgName("experiment-directory");
		exampleDirOption.setDescription("use <experiment-directory>. (defaults is taken from config file)");
		exampleDirOption.setType(String.class);
		exampleDirOption.setArgs(1);
		
		Option mmNameOption = OptionBuilder.create(O_METAMODEL_NAME);
		mmNameOption.setLongOpt(O_METAMODEL_NAME_LONG);
		mmNameOption.setArgName("metamodel-name");
		mmNameOption.setDescription("use <metamodel-name>. (defaults is taken from config file)");
		mmNameOption.setType(String.class);
		mmNameOption.setArgs(1);
		
		Option numberRunsOption = OptionBuilder.create(O_NUMBER_OF_RUNS);
		numberRunsOption.setLongOpt(O_NUMBER_OF_RUNS_LONG);
		numberRunsOption.setArgName("number-of-runs");
		numberRunsOption.setDescription("use <number-of-runs>. (defaults is 3)");
		numberRunsOption.setType(Integer.class);
		numberRunsOption.setArgs(1);
		
		Options options = new Options();
		options.addOption(rndrunOption);
		options.addOption(tfidfOption);
		options.addOption(configFileOption);
		options.addOption(patternsFileOption);
		options.addOption(coverageDefinitionOption);
		options.addOption(exampleEditionGUIOption);
		options.addOption(exampleDirOption);
		options.addOption(mmNameOption);
		options.addOption(numberRunsOption);
		return options;
	}


	
	
	

	public static String getTestModel(int ai, int idx) {
		File f = getTestModelFiles(ai).get(idx);
		String res = "";
		try {
			res = readFile(f.getPath());
		} catch (IOException e) {
			LOGGER.warning("Could not load file '" + f.getPath() + "'.");
			// e.printStackTrace();
		}
		return res.trim();
	}

	public static String getTestModels(int ai) {
		String res = "";
		for (File f : getTestModelFiles(ai)) {
			try {
				res += readFile(f.getPath());
			} catch (IOException e) {
				LOGGER.warning("Could not load file '" + f.getPath() + "'.");
				// e.printStackTrace();
			}
		}
		return res.trim();
	}

	public static ArrayList<File> getTestModelFiles(int ai) {
		return getPrefixedFiles("models", "./test/patterns/A" + ai);
	}

	public static ArrayList<File> getTestExpectedFiles(int ai) {
		return getPrefixedFiles("expected", "./test/patterns/A" + ai);
	}

	public static File getTestRuleFile(int ai) {
		return getPrefixedFiles("rule", "./test/patterns/A" + ai).get(0);
	}

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

	public static int getOptionNumberOfRuns() {
		if(commandLine.hasOption(O_NUMBER_OF_RUNS))
			return Integer.parseInt(commandLine.getOptionValue(O_NUMBER_OF_RUNS));
		return -1;
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

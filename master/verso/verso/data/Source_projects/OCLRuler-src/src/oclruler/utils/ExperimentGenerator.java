package oclruler.utils;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Scanner;
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

import oclruler.metamodel.Metamodel;


/**
 * Generate example directories for experiment<br/><br/>
 * 
 * usage: java -jar OCLRuler.jar -b <base-dir> [-d <disbalance>] -ex <experiment-dir> [-f] -mm <metamodel-name> -nb <<MIN> 
       <STEP> <MAX>> [-pre <arg>]<br/>
 -b,--base-dir <base-dir>                        use <base-dir>.<br/>
 -d,--disbalance <disbalance>                    use <dibalance> = percentage of disbalance in favor of positives (can
                                                 be negative)<br/>
 -ex,--experiment-dir <experiment-dir>           use <experiment-dir>.<br/>
 -f,--force-overwrite                            Overwrite experiment directory without prompt<br/>
 -mm,--metamodel-name <metamodel-name>           use <metamodel-name>.
 -nb,--number-of-examples <<MIN> <STEP> <MAX>>   generate (MAX-MIN)/STEP example sets of size MIN, MIN+STEP, MIN+2*STEP,
                                                 MIN+3*STEP, ..., MAX<br/>
 -inc,--incremental <true / false>               Build experiment sets incrementally (default is false).<br/>
 -pre,--prefix <arg>                             Put a prefix (default is timestamp) to output directory.<br/>


 * @author Edouard Batot 2017 - batotedo@iro.umontreal.ca
 *
 */
@SuppressWarnings("deprecation")
public class ExperimentGenerator {
	public final static Logger LOGGER = Logger.getLogger(ExperimentGenerator.class.getName());
	
	//try a change
	private static final String O_FORCE_OVERWRITE 		= "f";
	private static final String O_FORCE_OVERWRITE_LONG 	= "force-overwrite";
	private static final String O_DISBALANCE	 		= "d";
	private static final String O_DISBALANCE_LONG 		= "disbalance";
	private static final String O_METAMODELNAME 		= "mm";
	private static final String O_METAMODELNAME_LONG 	= "metamodel-name";
	private static final String O_BASE_DIR 				= "b";
	private static final String O_BASE_DIR_LONG 		= "base-dir";
	private static final String O_EXPERIMENT_DIR 		= "ex";
	private static final String O_EXPERIMENT_DIR_LONG 	= "experiment-dir";
	private static final String O_NUMBER_EXAMPLES 		= "nb";
	private static final String O_NUMBER_EXAMPLES_LONG 	= "number-of-examples";
	private static final String O_INCREMENTAL		 	= "inc"; // optional
	private static final String O_INCREMENTAL_LONG 		= "incremental";
	private static final String O_PREFIX 				= "pre"; //optional and positional: must be filled last in command line
	private static final String O_PREFIX_LONG 			= "prefix";
	
	
	/**
	 * Wether or not examples picking positing/negative rate is balanced.
	 */
	private static boolean DISBALANCE = false;
	/**
	 * Magnitude of dibalance (integer percents: 10 mean .10)
	 */
	private static int DISBALANCE_PERCENTAGE = 10;

	
	private static final String SUFFIX_EXAMPLEDIR_NAME = "examples";
	private static final String POSITIVES_DIR_NAME = "positives";
	private static final String NEGATIVES_DIR_NAME = "negatives";

	
	
	public static File EXPERIMENT_DIRECTORY, BASE_DIRECTORY, POS_DIR, NEG_DIR;
	static int MIN, MAX, STEP;
	
	static Options options ;
	static CommandLine commandLine;
	
	
	public static void main(String[] args) {

		options = configureOptions();
		CommandLineParser parser = new DefaultParser();
		try {
			commandLine = parser.parse(options, args);
			
			if(LOGGER.isLoggable(Level.CONFIG)){
				ToolBox.printArgs(commandLine);
			}

			ToolBox.initMinimal();//Might check wrong instance directory due to MMNAME CONFLICT
			Config.METAMODEL_NAME = commandLine.getOptionValue(O_METAMODELNAME).trim(); // Option is required
			Metamodel.init();

			LOGGER.info("");
			LOGGER.info("  - Starts -");
			DISBALANCE = commandLine.hasOption(O_DISBALANCE);
			if(DISBALANCE){
				DISBALANCE_PERCENTAGE = Integer.parseInt(commandLine.getOptionValue(O_DISBALANCE));
				LOGGER.info("Disbalance enabled: "+Math.abs(DISBALANCE_PERCENTAGE)+"% in favor of "+ ((DISBALANCE_PERCENTAGE > 0)? "positives":"negatives"));
			}
			
			// [MIN, STEP, MAX]
			String[] examplesOptionStr = commandLine.getOptionValues(O_NUMBER_EXAMPLES); // Option is required
			MIN = Integer.parseInt(examplesOptionStr[0]);
			STEP = Integer.parseInt(examplesOptionStr[1]);
			MAX = Integer.parseInt(examplesOptionStr[2]);

			String fBaseName = commandLine.getOptionValue(O_BASE_DIR).trim(); // Option is required
			if (!fBaseName.endsWith(File.separator))
				fBaseName += File.separator;
			fBaseName += Config.METAMODEL_NAME + File.separator;

			BASE_DIRECTORY = new File(fBaseName);
			checkBaseDirectory();


			
			int nbPos = ToolBox.listXMIFiles(POS_DIR).length;
			int nbNeg = ToolBox.listXMIFiles(NEG_DIR).length;

			
			LOGGER.info("Metamodel: " + Config.METAMODEL_NAME);
			LOGGER.info("Source examples folder: '" + BASE_DIRECTORY.getAbsolutePath() + "'");
			LOGGER.info(" - "+(nbPos+nbNeg)+" examples (XMI files)");
			LOGGER.info(" - " + nbPos + " positives (in ./"+POSITIVES_DIR_NAME+"/)");
			LOGGER.info(" - " + nbNeg + " positives (in ./"+NEGATIVES_DIR_NAME+"/)");
			LOGGER.info("Sample sizes: [" + MIN + " ." + STEP + ". " + MAX + "]");
			
			
			String fExpName = commandLine.getOptionValue(O_EXPERIMENT_DIR).trim(); // Option is required
			if (!fExpName.endsWith(File.separator))
				fExpName += File.separator;
			if (commandLine.hasOption(O_PREFIX)) {
				String prefix = ToolBox.START_TIME;
				if (commandLine.getOptionValue(O_PREFIX) != null)
					prefix = commandLine.getOptionValue(O_PREFIX);
				fExpName += prefix + "_";
			}
			fExpName += Config.METAMODEL_NAME + File.separator;
			
			
			// 
			boolean incremental = false;
			if( commandLine.hasOption(O_INCREMENTAL) ){
				String incStr = commandLine.getOptionValue(O_INCREMENTAL).trim();
				if(incStr.equalsIgnoreCase("ON") || incStr.equalsIgnoreCase("TRUE"))
					incremental = true;
				LOGGER.info("Incremental: " + (incremental?"On":"Off"));
			}
			
			
			prepareExperimentDirectory(fExpName, commandLine.hasOption(O_FORCE_OVERWRITE));
			
			
			
			if(incremental)
				pickAndCopyExamplesIncremental();
			 else 
				pickAndCopyExamplesRandomly();
			
			
			
			
			
		} catch (ParseException e) {
			LOGGER.severe("Command line options invalid : " + e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth(120);
			formatter.printHelp("java -jar OCLRuler.jar", options, true);
		}
		
		LOGGER.info("Safe exit.");
	}

	/**
	 * Checks wether the experiment directory exists or not.
	 * @param fExpName Name of the experiment directory
	 * @param forceOverWrite
	 */
	public static void prepareExperimentDirectory(String fExpName, boolean forceOverWrite) {
		EXPERIMENT_DIRECTORY = new File(fExpName);
		if (EXPERIMENT_DIRECTORY.exists()) {
			if (forceOverWrite) {
				ToolBox.deleteFolder(EXPERIMENT_DIRECTORY);
				LOGGER.info(" ! force-overwrite ON !");
				LOGGER.info("Experiment directory has been cleaned: '" + EXPERIMENT_DIRECTORY.getAbsolutePath() + "'");
			} else {
				System.out.println("Experiment directory already exists: \n - '" + EXPERIMENT_DIRECTORY.getAbsolutePath() + "'");
				int[] numberOfFiles = countFile(EXPERIMENT_DIRECTORY);
				System.out.println("\nIf you continue, " + numberOfFiles[0] + " folders and " + numberOfFiles[1] + " files will be deleted.");
				System.out.println("Overwrite ? y/n");

				@SuppressWarnings("resource")
				String go = new Scanner(System.in).nextLine();
				if (go.equalsIgnoreCase("y") || go.equalsIgnoreCase("yes")) {
					ToolBox.deleteFolder(EXPERIMENT_DIRECTORY);
				} else {
					LOGGER.severe("Overwrite rejected.\nExecution aborded by user.");
					System.exit(0);
				}
			}
		}
		EXPERIMENT_DIRECTORY.mkdir();
		LOGGER.info("'" + EXPERIMENT_DIRECTORY.getAbsolutePath() + "'");
	}

	public static void checkBaseDirectory() {
		POS_DIR = new File(BASE_DIRECTORY.getAbsolutePath() + File.separator + POSITIVES_DIR_NAME);
		NEG_DIR = new File(BASE_DIRECTORY.getAbsolutePath() + File.separator + NEGATIVES_DIR_NAME);
		if (!BASE_DIRECTORY.exists() || !NEG_DIR.exists() || !POS_DIR.exists()) {
			LOGGER.severe("Base model directory does not exist: \n - '" + BASE_DIRECTORY.getAbsolutePath() + "'\n - '" + POS_DIR.getAbsolutePath()
					+ "'\n - '" + NEG_DIR.getAbsolutePath() + "'");
			LOGGER.severe("Exit.");
			System.exit(0);
		}

		int nbPos = ToolBox.listXMIFiles(POS_DIR).length;
		int nbNeg = ToolBox.listXMIFiles(NEG_DIR).length;
		if (nbPos == 0)
			LOGGER.severe("Base model directory does not contains positive examples: \n - '" + POS_DIR.getAbsolutePath() + "'");
		if (nbNeg == 0)
			LOGGER.severe("Base model directory does not contains negative examples: \n - '" + POS_DIR.getAbsolutePath() + "'");
		if (nbPos == 0 || nbNeg == 0) {
			LOGGER.severe("Exit.");
			System.exit(0);
		}

		if ((nbNeg + nbPos) < MAX) {
			LOGGER.severe("Base model directory does not contains enough examples: " + "\n"
					+ " + '" + BASE_DIRECTORY.getAbsolutePath() + "'" + "\n"
					+ "  - '" + POS_DIR.getAbsolutePath() + "' : " + nbPos + " positives\n"
					+ "  - '" + NEG_DIR.getAbsolutePath() + "' : " + nbNeg + " negatives\n"
					+ "      Total:  " + (nbPos +nbNeg) +"\n"
					+ "   Required: ["+ MIN + " ."+STEP+". "+ MAX+"]");
			LOGGER.severe("Exit.");
			System.exit(0);
		}
	}
	
	/**
	 * <ol>
	 * <li>pick NB examples</li>
	 * <li>copy them in EXPERIMENT_DIRECTORY / NBexamples/</li>
	 * <li>write examples statistics files</li>
	 * </ol>
	 * 
	 * @param nbEx
	 */
	private static void pickAndCopyExamplesRandomly() {
		for (int nbEx = MIN; nbEx <= MAX; nbEx += STEP) {
			ArrayList<File> examplesToCopy = pickExamplesRandomly(nbEx, checkDisbalance(nbEx));
			File f = copyExamples(nbEx, examplesToCopy);
			// generate statistics.
			ModelsRepositoryStatistics.buildAndWriteModelRepositoryStatistics(f);
			LOGGER.config(""+f.getAbsolutePath().substring(EXPERIMENT_DIRECTORY.getAbsolutePath().length()) + ": " + ToolBox.listXMIFiles(f).length + " examples copied");
		}
	}
	
	private static void pickAndCopyExamplesIncremental() {
		ArrayList<File> examplesToCopy = pickExamplesRandomly(MIN);
		do {
//			System.out.println("exs "+examplesToCopy.size()+" :"+examplesToCopy);
			File f = copyExamples(examplesToCopy.size(), examplesToCopy);
			// generate statistics.
			ModelsRepositoryStatistics.buildAndWriteModelRepositoryStatistics(f);
			LOGGER.config(""+f.getAbsolutePath().substring(EXPERIMENT_DIRECTORY.getAbsolutePath().length()) + ": " + ToolBox.listXMIFiles(f).length + " examples copied");
			
			ArrayList<File> plus = pickExamplesRandomly( STEP, examplesToCopy);
			examplesToCopy.addAll(plus);
		} while (examplesToCopy.size() <= MAX);
	}


	/**
	 * 
	 * @param nbEx
	 * @param examplesToCopy
	 * @return the folder in which files have been copied.
	 */
	public static File copyExamples(int nbEx, ArrayList<File> examplesToCopy) {
		File f = new File(EXPERIMENT_DIRECTORY.getAbsolutePath() + File.separator + (nbEx<10?"0":"") + nbEx + SUFFIX_EXAMPLEDIR_NAME);
		f.mkdir();
		f = new File(f.getAbsolutePath() + File.separator + Config.METAMODEL_NAME);
		f.mkdir();
		int nbMoved = 0;
		for (File file : examplesToCopy) {
			ToolBox.copyFile(file, f);
			nbMoved ++;
		}
		LOGGER.fine("" + nbMoved + " examples moved to '" + f.getAbsolutePath() + "'");
		return f;
	}
	
	/**
	 * Pick examples half + / half -
	 * @param nbEx
	 * @return 
	 */
	public static ArrayList<File> pickExamplesRandomly(int nbEx) {
		return pickExamplesRandomly(nbEx, 0.0);
	}
	
	public static ArrayList<File> pickExamplesRandomly(int nbEx, double disbalance) {
		return pickExamplesRandomly(nbEx, null, disbalance);
	}
	
	public static ArrayList<File> pickExamplesRandomly(int nbEx, ArrayList<File> exclusions) {
		return pickExamplesRandomly(nbEx, exclusions, 0.0);
	}
	
	public static ArrayList<File> pickExamplesRandomly(int nbEx, ArrayList<File> exclusions, double disbalance) {
		ArrayList<File> pickeds = new ArrayList<File>(nbEx);

		// pick positives
		int nbPositives = (int)(Math.floor((double)nbEx / 2) + Math.floor(nbEx * disbalance));
		pickeds.addAll( pickExamplesFromDir( nbPositives, POS_DIR, exclusions));

		// pickNegative
		pickeds.addAll( pickExamplesFromDir( nbEx - nbPositives, NEG_DIR, exclusions));

		return pickeds;
	}

	public static ArrayList<File> pickExamplesFromDir(int nbEx, File baseDirectory) {
		return pickExamplesFromDir(nbEx, baseDirectory, null);
	}
	
	/**
	 * 
	 * @param nbEx
	 * @param baseDirectory
	 * @param exclusions (can be null, meaning empty)
	 * @return
	 */
	public static ArrayList<File> pickExamplesFromDir(int nbEx, File baseDirectory, ArrayList<File> exclusions) {
		int numberOfFilesInBase = ToolBox.listXMIFiles(baseDirectory).length;
		ArrayList<File> filesSource = new ArrayList<File>(numberOfFilesInBase);
		 ArrayList<File> examplesToCopy = new ArrayList<>(nbEx);
		for (File file : ToolBox.listXMIFiles(baseDirectory))
			if((exclusions != null && !exclusions.contains(file)) || exclusions == null)
				filesSource.add(file);
		int i = 0;
		while (!filesSource.isEmpty() && examplesToCopy.size() < nbEx) {
			File r = ToolBox.getRandom(filesSource);
			examplesToCopy.add(r);
			filesSource.remove(r);
			i++;
		}
		LOGGER.fine(i + " examples from '"+baseDirectory.getAbsolutePath()+"'.");
		return examplesToCopy;
	}

	public static double checkDisbalance(int nbEx) {
		double disbalance = 0.0;
		if (DISBALANCE) {
			boolean favorsPos = DISBALANCE_PERCENTAGE > 0;
			disbalance = ((double) ToolBox.getRandomInt(0, Math.abs(DISBALANCE_PERCENTAGE))) / 100;
			if (!favorsPos)
				disbalance = -disbalance;
			LOGGER.fine("Disbalance:" + Math.floor(nbEx * disbalance) + (favorsPos?" positives":" negatives"));
		}
		return disbalance;
	}

	private static Options configureOptions() {
		
		Option forceOverwriteOption = OptionBuilder.create(O_FORCE_OVERWRITE);
		forceOverwriteOption.setLongOpt(O_FORCE_OVERWRITE_LONG);
		forceOverwriteOption.setDescription("Overwrite experiment directory without prompt");
		
//		Option disbalanceOption = OptionBuilder.create(O_DISBALANCE);
//		disbalanceOption.setLongOpt(O_DISBALANCE_LONG);
//		disbalanceOption.setDescription("Wether or not examples picking positing/negative rate is balanced.");
		
		Option disbalanceOption = OptionBuilder.create(O_DISBALANCE);
		disbalanceOption.setLongOpt(O_DISBALANCE_LONG);
		disbalanceOption.setArgName("disbalance");
		disbalanceOption.setDescription("use <dibalance> = percentage of disbalance in favor of positives (can be negative)");
		disbalanceOption.setType(Integer.class);
		disbalanceOption.setArgs(1);
//		disbalanceOption.setRequired(true); //No


		Option mmNameOption = OptionBuilder.create(O_METAMODELNAME);
		mmNameOption.setLongOpt(O_METAMODELNAME_LONG);
		mmNameOption.setArgName("metamodel-name");
		mmNameOption.setDescription("use <metamodel-name>.");
		mmNameOption.setType(String.class);
		mmNameOption.setArgs(1);
		mmNameOption.setRequired(true);
		
		Option baseDirOption = OptionBuilder.create(O_BASE_DIR);
		baseDirOption.setLongOpt(O_BASE_DIR_LONG);
		baseDirOption.setArgName("base-dir");
		baseDirOption.setDescription("use <base-dir>.");
		baseDirOption.setType(String.class);
		baseDirOption.setArgs(1);
		baseDirOption.setRequired(true);
		
		Option experimentDirOption = OptionBuilder.create(O_EXPERIMENT_DIR);
		experimentDirOption.setLongOpt(O_EXPERIMENT_DIR_LONG);
		experimentDirOption.setArgName("experiment-dir");
		experimentDirOption.setDescription("use <experiment-dir>.");
		experimentDirOption.setType(String.class);
		experimentDirOption.setArgs(1);
		experimentDirOption.setRequired(true);

		Option incrementalOption = OptionBuilder.create(O_INCREMENTAL);
		incrementalOption.setLongOpt(O_INCREMENTAL_LONG);
		incrementalOption.setArgName("incremental");
		incrementalOption.setDescription("use <true / false>.");
		incrementalOption.setType(String.class);
		incrementalOption.setArgs(1);
		incrementalOption.setOptionalArg(true);

		Option configExamplesOption = OptionBuilder.create(O_NUMBER_EXAMPLES);
		configExamplesOption.setLongOpt(O_NUMBER_EXAMPLES_LONG);
		configExamplesOption.setArgName("<MIN> <STEP> <MAX>");
		configExamplesOption.setDescription("generate (MAX-MIN)/STEP example sets of size MIN, MIN+STEP, MIN+2*STEP, MIN+3*STEP, ..., MAX");
		configExamplesOption.setType(Integer.class);
		configExamplesOption.setArgs(3);
		configExamplesOption.setRequired(true);
		
		Option prefixOption = new Option(O_PREFIX, O_PREFIX_LONG, true, "Put a prefix (default is timestamp) to output directory.");
		prefixOption.setOptionalArg(true);

		
		Options options = new Options();
		options.addOption(forceOverwriteOption);
		options.addOption(disbalanceOption);
		options.addOption(mmNameOption);
		options.addOption(baseDirOption);
		options.addOption(experimentDirOption);
		options.addOption(configExamplesOption);
		options.addOption(incrementalOption);
		options.addOption(prefixOption);
		return options;
	}
	
	

	public static int[] countXMIFile(File folder, boolean xmiOnly) {
		File[] files = folder.listFiles();
		if(xmiOnly)
			files = folder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if(dir.isDirectory())
						return true;
					return name.toLowerCase().endsWith("xmi");
				}
			});
		int[] res = {0, 0};
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					int[] tmp = countFile(f);
					res[0] += tmp[0]+1;
					res[1] += tmp[1];
				} else {
					res[1] += 1;
				}
			}
		}
		return res;
	}
	
	/**
	 * Default : XMI files only.
	 * @param folder
	 * @return list of XMI files contained in the folder (recursively in sub folders too).
	 */
	public static int[] countFile(File folder) {
		return countXMIFile(folder, false);
	}

}

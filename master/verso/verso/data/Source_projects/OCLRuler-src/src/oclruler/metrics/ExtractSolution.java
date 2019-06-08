package oclruler.metrics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import oclruler.genetics.Oracle;
import oclruler.rule.PatternFactory;
import oclruler.rule.Program;
import oclruler.rule.struct.Constraint;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

@SuppressWarnings("deprecation")
public class ExtractSolution {
	public final static Logger LOGGER = Logger.getLogger("extract.solution");

	private static final String SOURCE_FILE_FOR_EXTRACTION = "source_for_extract_#MM#.txt";

	public static String DIR_RESULTS = "R:/EclipseWS/material/OCL_Ruler/results/";
	
	private static String O_METAMODEL_NAME = 			"mm";
	private static String O_METAMODEL_NAME_LONG = 		"metamodel-name";
	private static String METAMODEL_NAME = null;

	private static String O_AUGMENTATION_TYPE = 		"aug";
	private static String O_AUGMENTATION_TYPE_LONG = 	"augmentation-type";
	private static EXPERIMENT_TYPE AUGMENTATION_TYPE = 	null;

	private static String O_EXPERIMENT_NUMBER = 		"exp";
	private static String O_EXPERIMENT_NUMBER_LONG =	"experiment-number";
	private static int EXPERIMENT_NUMBER = -1;

	private static String O_NUMBER_OF_EXAMPLES = 		"exs";
	private static String O_NUMBER_OF_EXAMPLES_LONG = 	"number-of-examples";
	private static int NUMBER_OF_EXAMPLES = -1;

	private static String O_RUN_NUMBER = 				"run";
	private static String O_RUN_NUMBER_LONG = 			"run-number";
	private static int RUN_NUMBER = -1;
	
	private static String O_LEAF_FOLDER = 				"leaf";
	private static String O_LEAF_FOLDER_LONG = 			"leaf-folder";
	private static String LEAF_FOLDER = null;
	
	static enum METAMODEL_NAME {
		Family,
		Bank,
		statemachine;
	}
	
	static enum EXPERIMENT_TYPE {
		Classic,
		Incremental;

		public boolean checkExperiment(int eXPERIMENT_NUMBER) {
			switch (this) {
			case Classic:
				if(eXPERIMENT_NUMBER != 0 && eXPERIMENT_NUMBER != 1)
					throw new IllegalArgumentException("Classic experiment: only experiment 0 or 1. Found: "+eXPERIMENT_NUMBER);
				break;
			case Incremental:
				if(eXPERIMENT_NUMBER < 0 || eXPERIMENT_NUMBER > 3)
					throw new IllegalArgumentException("Incremental experiment: only experiment 0, 1, 2, or 3. Found: "+eXPERIMENT_NUMBER);
				break;
			}
			return true;
		}
	}
	
	public static void main(String[] args) {
		LOGGER.info("[ExtractSolution] Entering... ");
		
		
	
			// Initialization starts
		Config.EXPERIMENT_MODE = false;
		ToolBox.loadArgs(args);
		loadArgs(args);
		
		ToolBox.init();
		
		LOGGER.info("Metamodel is: "+Config.METAMODEL_NAME);

		File folderResults = Paths.get(DIR_RESULTS, "extraction").toFile();
		folderResults.mkdirs();

		String[][] best20 = null;
		Path fileRes = Paths.get(folderResults.getAbsolutePath(), SOURCE_FILE_FOR_EXTRACTION.replaceAll("#MM#", Config.METAMODEL_NAME));
		try {
			List<String> best20_strs = Files.readAllLines(fileRes);
			best20 = new String[best20_strs.size()][4];

			for (int i = 0; i < best20_strs.size(); i++) {
				String[] best = best20_strs.get(i).trim().split("\t");
				best20[i][0] = best[0].trim().substring(0, best[0].length() - 1);
				best20[i][1] = best[0].trim().substring(best[0].length() - 1);
				best20[i][2] = best[1].trim().substring(0, 2);
				best20[i][3] = best[2].trim();
				System.out.println(Arrays.toString(best20[i]));
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		for (int i = 0; i < best20.length; i++) {
			int exp = Integer.parseInt(best20[i][1]) - 1;
			int run = Integer.parseInt(best20[i][3]);
			String incType = getIncrementationTypeFromSPSSName(best20[i][0]);
			String[] args3 = new String[] { 
					"-mm",  Config.METAMODEL_NAME, 
					"-aug", incType, 
					"-exp", exp + "", 
					"-exs", best20[i][2], 
					"-run", run + "", 
					"-leaf", "official" };
			// System.out.println(Arrays.toString(args3));
			loadArgs(args3);
			String dirExperimentPath = (Config.SINBAD ? "/u/batotedo/" : "R:/") + "EclipseWS/material/OCL_Ruler/experiment";
			File dirExperimentResults = Paths
					.get(dirExperimentPath, AUGMENTATION_TYPE.toString(), METAMODEL_NAME,
							AUGMENTATION_TYPE.toString() + (EXPERIMENT_NUMBER > 1 ? EXPERIMENT_NUMBER : "") + "_" + METAMODEL_NAME, "_results", LEAF_FOLDER)
					.toFile();
			// Initialization ends

			String extraction = extractSolution(args3, dirExperimentResults);
			String fileName = "extract_" + args3[1] + "_" + i + "_" + args3[3] + "_exp" + args3[5] + "_" + args3[7] + "exs_run" + args3[9] + ".txt";
			File fOut = Paths.get(folderResults.getAbsolutePath(), fileName).toFile();
			try {
				fOut.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(fOut));
				bw.write(Arrays.toString(args3) + "\n");
				bw.write(extraction + "\n");
				bw.close();
				LOGGER.info("Extraction writen in '" + fOut.getAbsolutePath() + "'");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		
				//To check (good DISCR/FFREC avg)
//		String[] args2 = new String[] { "-mm", "Bank", "-aug", "Classic", 	"-exp", "0", "-exs", "40", "-run", "3",  "-leaf", "official" };
//		String[] args2 = new String[] { "-mm", "Bank", "-aug", "Incremental", 	"-exp", "3", "-exs", "35", "-run", "0",  "-leaf", "official" };
		
//		String[] args2 = new String[] { "-mm", "Bank", "-aug", "Classic", 	"-exp", "2", "-exs", "40", "-run", "0",  "-leaf", "official" };
//		String[] args2 = new String[] { "-mm", "Bank", "-aug", "Incremental", 	"-exp", "3", "-exs", "35", "-run", "0",  "-leaf", "official" };
//		String[] args2 = new String[] { "-mm", "Bank", "-aug", "Incremental", 	"-exp", "3", "-exs", "35", "-run", "4",  "-leaf", "official" };
//		String[] args2 = new String[] { "-mm", "Bank", "-aug", "Classic",  		"-exp", "1", "-exs", "5",  "-run", "1",  "-leaf", "official" };
//		String[] args2 = new String[] { "-mm", "Bank", "-aug", "Classic", 		"-exp", "1", "-exs", "25", "-run", "2",  "-leaf", "official" };
//		String[] args2 = new String[] { "-mm", "Bank", "-aug", "Classic", 		"-exp", "1", "-exs", "25", "-run", "14", "-leaf", "official" };
//		String[] args2 = new String[] { "-mm", "Bank", "-aug", "Classic", 		"-exp", "1", "-exs", "40", "-run", "4", "-leaf", "official" };
//		String[] args2 = new String[] { "-mm", "Bank", "-aug", "Incremental", 	"-exp", "3", "-exs", "40", "-run", "4", "-leaf", "official" };
//		String[] args2 = new String[] { "-mm", "Bank", "-aug", "Incremental", 	"-exp", "3", "-exs", "35", "-run", "0", "-leaf", "official" };
//		String[] args2 = new String[] { "-mm", "Family", "-aug", "Classic", 	"-exp", "2", "-exs", "20", "-run", "0", "-leaf", "official" };
//		String[] args2 = new String[] { "-mm", "Family", "-aug", "Classic", 	"-exp", "2", "-exs", "20", "-run", "1", "-leaf", "official" };
//		String[] args2 = new String[] { "-mm", "statemachine", "-aug", "Classic", 	"-exp", "1", "-exs", "10", "-run", "2", "-leaf", "official" };
//		String extraction = extractSolution(args2);
//		File fREsults = Paths.get(Config.DIR_RES.getAbsolutePath(), "extraction").toFile();
//		fREsults.mkdirs();
		
	}
	
	
	private static String getIncrementationTypeFromSPSSName(String string) {
		if(string.equals("Classic"))
			return EXPERIMENT_TYPE.Classic.name();
		return EXPERIMENT_TYPE.Incremental.name();
	}


	/**
	 * 
	 * Specify MM, augmentation type (Classic/Incremental), experiment number, number of examples, run number, leaf folder.
	 * 
	 * @param args String[] { "-mm", "Bank", "-aug", "Classic", "-exp", "2", "-exs", "40", "-run", "0",  "-leaf", "official" };
	 * @param dirExperimentResults
	 * @return
	 */
	public static String extractSolution(String[] args, File dirExperimentResults) {
		String res = "";
		
		res += ("Solution folder: " + dirExperimentResults.getAbsolutePath() +"\n");

		
		
		MetricExtraction me = new MetricExtraction(dirExperimentResults, NUMBER_OF_EXAMPLES); // MetricExtraction.RUN_FOLDERS_PREFIX+RUN_NUMBER
		res += ("Solution file: \n"+
				Paths.get(dirExperimentResults.getAbsolutePath(), 
						MetricExtraction.RUN_FOLDERS_PREFIX+(NUMBER_OF_EXAMPLES > 10 ? "": "0")+NUMBER_OF_EXAMPLES+"examples", 
						MetricExtraction.RUN_FOLDERS_PREFIX+RUN_NUMBER
						).toFile().getAbsolutePath());
		
		Program oracle = Oracle.getInstance();
		res += printProgram(oracle) + "\n\n";
		
//		System.out.println();
//		System.out.println();
		
		Program solution = me.getProgramRun(RUN_NUMBER);
		res += printProgram(solution) + "\n\n";
		
//		System.out.println();
//		System.out.println();
		
		double[][][] crisscross = me.getCrisscrossMatrice(solution);
		res += MetricExtraction.printMatrice2D(crisscross);
		
		LOGGER.config(res);
		LOGGER.config("[ExtractSolution] Safe exit.");
		return res;
	}

	public static String printProgram(Program prg) {
		String res = prg.getName()+": ("+prg.size()+")\n";
		res += "-------"+"\n";
		int i = 0;
		
		for (Constraint cst : prg.getConstraints()) 
			res +=  " ("+(i++)+") Context " +cst.getContext().getName()+": "+ cst.getOCL() + "\n";
		return res;
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
			
		    if( commandLine.hasOption( O_METAMODEL_NAME ) ) {
		    	METAMODEL_NAME = commandLine.getOptionValue(O_METAMODEL_NAME);
		    	Config.overrideConfigFileParameter("METAMODEL_NAME", METAMODEL_NAME);
		    } else {
		    }
		    
		    if( commandLine.hasOption( O_AUGMENTATION_TYPE ) ) {
		    	try {
		    	AUGMENTATION_TYPE = EXPERIMENT_TYPE.valueOf(commandLine.getOptionValue(O_AUGMENTATION_TYPE));
		    	} catch (Exception e) {
		    		throwInvalidNumberException(O_AUGMENTATION_TYPE_LONG, commandLine.getOptionValue(O_AUGMENTATION_TYPE));
				}
		    } else {
		    }
		    
		    if( commandLine.hasOption( O_LEAF_FOLDER ) ) {
		    	LEAF_FOLDER = commandLine.getOptionValue(O_LEAF_FOLDER);
		    } else {
		    }

			if (commandLine.hasOption(O_EXPERIMENT_NUMBER)) {
				try {
					EXPERIMENT_NUMBER = Integer.parseInt(commandLine.getOptionValue(O_EXPERIMENT_NUMBER));
					
					AUGMENTATION_TYPE.checkExperiment(EXPERIMENT_NUMBER);
				} catch (NumberFormatException e) {
					throwInvalidNumberException(O_EXPERIMENT_NUMBER_LONG, commandLine.getOptionValue(O_EXPERIMENT_NUMBER));
				}
			} else {
			}

			if (commandLine.hasOption(O_NUMBER_OF_EXAMPLES)) {
				try {
					NUMBER_OF_EXAMPLES = Integer.parseInt(commandLine.getOptionValue(O_NUMBER_OF_EXAMPLES));
				} catch (NumberFormatException e) {
					throwInvalidNumberException(O_NUMBER_OF_EXAMPLES_LONG, commandLine.getOptionValue(O_NUMBER_OF_EXAMPLES));
				}
			} else {
			}

			if (commandLine.hasOption(O_RUN_NUMBER)) {
				try {
					RUN_NUMBER = Integer.parseInt(commandLine.getOptionValue(O_RUN_NUMBER));
				} catch (NumberFormatException e) {
					throwInvalidNumberException(O_RUN_NUMBER_LONG, commandLine.getOptionValue(O_RUN_NUMBER));
				}
			} else {
				// Config.DIR_EXPERIMENT not changed
			}

		} catch (ParseException e) {
			LOGGER.severe("Command line options invalid : "+e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth(140);
			System.out.println();
			formatter.printHelp("java -jar OCLRuler.jar", options, true);
			System.out.println("Exit with errors - invalid parameters.");
			System.exit(1);
		}
	}

	public static void throwInvalidNumberException(String arg, String value) throws ParseException{
		throw new ParseException(
				"Argument " + arg + " malformed. Integer expected, found '" + commandLine.getOptionValue(O_EXPERIMENT_NUMBER) + "'.");
	}
	
	public static String printArgs() {
		return printArgs(commandLine);
	}
	
	public static String printArgs(CommandLine cl) {
		String res = "";
		for (Option o : cl.getOptions()) {
				res += " - " +o.getLongOpt()+" : "+ Arrays.toString(o.getValues()) + "\n";
		}
		if(!res.isEmpty())
			return ("Args:\n"+res);
		else
			return ("No argument passed.");
	}
	
	private static Options configureOptions() {
		Option MMNameOption = OptionBuilder.create(O_METAMODEL_NAME);
		MMNameOption.setLongOpt(O_METAMODEL_NAME_LONG);
		MMNameOption.setArgName("metmaodel-name");
		MMNameOption.setDescription("use <metmaodel-name>.");
		MMNameOption.setType(String.class);
		MMNameOption.setRequired(false);
		MMNameOption.setArgs(1);

		Option augmentationTypeOption = OptionBuilder.create(O_AUGMENTATION_TYPE);
		augmentationTypeOption.setLongOpt(O_AUGMENTATION_TYPE_LONG);
		augmentationTypeOption.setArgName("augmentation-type");
		augmentationTypeOption.setDescription("use <augmentation-type> : Classic or Incremental");
		augmentationTypeOption.setType(String.class);
		augmentationTypeOption.setArgs(1);
		augmentationTypeOption.setRequired(false);
		
		Option experimentNumberOption = OptionBuilder.create(O_EXPERIMENT_NUMBER);
		experimentNumberOption.setLongOpt(O_EXPERIMENT_NUMBER_LONG);
		experimentNumberOption.setArgName("experiment-number");
		experimentNumberOption.setDescription("use <experiment-number> Classic:{1,2} Incremental:{1,2,3,4}");
		experimentNumberOption.setType(String.class);
		experimentNumberOption.setArgs(1);
		experimentNumberOption.setRequired(false);
		
		Option numberOfExamplesOption = OptionBuilder.create(O_NUMBER_OF_EXAMPLES);
		numberOfExamplesOption.setLongOpt(O_NUMBER_OF_EXAMPLES_LONG);
		numberOfExamplesOption.setArgName("number-of-examples");
		numberOfExamplesOption.setDescription("use <number-of-examples> pick in {05, 10, 15, 20, 25, 30, 35, 40}");
		numberOfExamplesOption.setType(String.class);
		numberOfExamplesOption.setArgs(1);
		numberOfExamplesOption.setRequired(false);
		
		Option runNumberOption = OptionBuilder.create(O_RUN_NUMBER);
		runNumberOption.setLongOpt(O_RUN_NUMBER_LONG);
		runNumberOption.setArgName("run-number");
		runNumberOption.setDescription("use <run-number>. pick in { 0 .. 9 }");
		runNumberOption.setType(String.class);
		runNumberOption.setArgs(1);
		runNumberOption.setRequired(false);
		
		Option leafOption = OptionBuilder.create(O_LEAF_FOLDER);
		leafOption.setLongOpt(O_LEAF_FOLDER_LONG);
		leafOption.setArgName("leaf-folder");
		leafOption.setDescription("use <leaf-folder>. -> official");
		leafOption.setType(String.class);
		leafOption.setArgs(1);
		leafOption.setRequired(false);
				
		Options options = new Options();
		options.addOption(MMNameOption);
		options.addOption(augmentationTypeOption);
		options.addOption(experimentNumberOption);
		options.addOption(numberOfExamplesOption);
		options.addOption(runNumberOption);
		options.addOption(leafOption);
		return options;
	}

}

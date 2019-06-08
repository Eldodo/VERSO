import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import oclruler.genetics.Evaluator;
import oclruler.genetics.EvaluatorOCL;
import oclruler.genetics.Evolutioner;
import oclruler.genetics.Oracle;
import oclruler.genetics.OraculizationException;
import oclruler.genetics.Population;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Model;
import oclruler.rule.Program;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * Multi execution.
 * @author Edouard Batot 2017 - batotedo@iro.umontreal.ca
 *
 */
public class MainMulti {
	public final static Logger LOGGER = Logger.getLogger(MainMulti.class.getName());
	
	static int NUMBER_OF_RUNS = 3;
	static String[] subFolders;
	
	
	static Evolutioner evo;
	static long run_startingtime;
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args)  {
		LOGGER.info("Entering OCLRuler - Multi Run");
		Config.EXPERIMENT_MODE = true;
		
		ToolBox.loadArgs(args);
		if(LOGGER.isLoggable(Level.CONFIG))
			ToolBox.printArgs();
		
		
		try {
			Config.DIR_RES = buildDirectory(); // DIR_RES / multirun / MM_Name / 
		} catch (IOException e) {
			LOGGER.severe("Could not create result directories. "+e.getLocalizedMessage());
			System.exit(1);
		}
		
		ToolBox.init();
		if(Config.VERBOSE_ON_UI)
			LOGGER.warning("VERBOSE_ON_UI is DISABLED in multi-run.");
		Config.VERBOSE_ON_UI = false;
		
		try {
			
			if(ToolBox.getOptionNumberOfRuns() < 0){
				NUMBER_OF_RUNS = Config.getIntParam("EXPERIMENT_NUMBER_OF_RUNS");
			} else {
				NUMBER_OF_RUNS = ToolBox.getOptionNumberOfRuns();
			}
			
			LOGGER.info("Number of runs for each treatment: "+NUMBER_OF_RUNS);
		} catch (Exception e1) {
			NUMBER_OF_RUNS = 3;
			LOGGER.warning("Number of runs not/mis specified - default is: " + NUMBER_OF_RUNS);
//			e1.printStackTrace();
		}
		
		
		File fResults = new File(Config.DIR_RES.getAbsoluteFile()+File.separator+"results.txt");
		BufferedWriter bwResults = createResultCSVFile(fResults);

		String[] experimentSubDirectories = Config.EXPERIMENT_SUB_FOLDERS;
		LOGGER.finer("Experiment folders: "+Arrays.toString(Config.EXPERIMENT_SUB_FOLDERS));
		int i = 0;
		for (String fname : experimentSubDirectories) {
			Config.EXPERIMENT_NB = i;
//			ToolBox.init();
			
			LOGGER.fine(""+new File(fname).getName());
			//Create file for grouped results (CSV, one line per run)
			
			String res = "";
		
			File outputDirectory = touchOutputDirectory();
			
			/*
			 * NUMBER_OF_EXECS executions sur UN dossier.
			 */
			for (int run = 0; run < NUMBER_OF_RUNS; run++) {
				Config.RUN_NB = run;
				run_startingtime = System.currentTimeMillis();
				res = runOnce(res, outputDirectory, run);
				try {
					bwResults.write(res);
					bwResults.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			i++;
		}
		if(bwResults != null)
			try {
				bwResults.close();
				LOGGER.info("Results written in '"+fResults.getAbsolutePath()+"'");
			} catch (IOException e) { }

		LOGGER.info("Exit OCLRuler - Multi Run");
	}

	public static String runOnce(String res, File outputDirectory, int run) {
		File outputSubDirectory = Paths.get(outputDirectory.getAbsolutePath(), Config.run_prefix + run).toFile();
		if (outputSubDirectory != null && !outputSubDirectory.exists())
			outputSubDirectory.mkdir();
		LOGGER.config("Output directory: " + outputSubDirectory.getAbsolutePath());

		/*
		 * Example set reload from experiment repository
		 */
		ExampleSet.unleashConsideredExamples(); // Update examples considered
		ExampleSet ms = ExampleSet.getInstance(true); // True for the example set to RELOAD the directory
		LOGGER.finer("ExampleSet : " + ms.sizeAll() + " " + ms.isOraculized());
		Evaluator eva = new EvaluatorOCL(ms);

		/*
		 * Setting serialization
		 */
		if (run == 0) {
			String fileNameSettings = outputDirectory.getAbsolutePath() + File.separator + "setting.log";
			File settings = new File(fileNameSettings);
			writeSettingFile(ms, fileNameSettings, settings);
		}

		/*
		 * Oraculization of examples.
		 */
		Oracle o = Oracle.getInstance(true); // True for the oracle to RELOAD with new ExampleSet
		try {
			o.oraculize(eva);
		} catch (OraculizationException e1) {
			LOGGER.severe("Run " + run + ": Oraculization error.");
			e1.printStackTrace();
			res += run + ";;;;;;;" + "\n";
		}

		/*
		 * Evolution
		 */
		Population p0 = Population.createRandomPopulation();
		p0.evaluate(eva, true);
		File fEvolutionerResult = Paths.get(outputSubDirectory.getAbsolutePath(), Config.run_prefix + run+"_compiled.log").toFile();
		File fEvolutionerDataResult = Paths.get(outputSubDirectory.getAbsolutePath(), Config.run_prefix + run+"_data.log").toFile();
		try {
			fEvolutionerResult.createNewFile();
			fEvolutionerDataResult.createNewFile();
		} catch (IOException e1) {
			LOGGER.severe("Run " + run + ": Result files could not be created.");
			e1.printStackTrace();
			return res;
		}
		evo = new Evolutioner(eva, p0, fEvolutionerResult, fEvolutionerDataResult);
		try {
			p0 = evo.evolutionate();
			String resultsLine = computeOneLineFileResults(run, (Program) p0.getBestOnObj(0));
			res = resultsLine + "\n";
//				saveResults(run, p0, o, ms, outputDirectory);
			
		} catch (Exception e) {
			LOGGER.severe("Exception during run " + run + ".");
			e.printStackTrace();
		}
		ms = null;
		eva = null;
		o = null;
		p0 = null;
		System.gc();

		LOGGER.info("Run " + run + ": " + res + " (" + ToolBox.formatMillis(System.currentTimeMillis() - run_startingtime) + ")");
		return res;
	}

	public static void writeSettingFile(ExampleSet ms, String fileNameSettings, File settings) {
		try {
			settings.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(settings));
			bw.write(Config.printSetting("") + "\n\n");
			bw.write("Oracle :\n--------\n" + ms.printOracleDecisions() + "\n");
			bw.close();
			LOGGER.fine("Settings written in " + fileNameSettings);
		} catch (IOException e) {
			LOGGER.info("Exception while writing " + fileNameSettings + "");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	public static BufferedWriter createResultCSVFile(File fResults) {
		BufferedWriter bwResults = null;
		try {
			bwResults = new BufferedWriter(new FileWriter(fResults));
			bwResults.write("experiment;run;time_s;cov_all;cov_pos;cov_neg;size;refinedSize;sizePos;sizeRefinedPos;sizeNeg;sizeRefinedNeg;pp;pn;np;nn;precisionMMElts;recallMMElts;FMMElts\n");
		} catch (IOException e2) {
			LOGGER.severe("File '" + fResults + "' could not be open. Results will not be printed !!!");
			System.out.println("Press <Enter> to continue.");
			new Scanner(System.in).nextLine();
			e2.printStackTrace();
		}
		return bwResults;
	}

	/**
	 * Warning: {@link Config#getRunFolderName()} coupled with {@link #EXPERIMENT_SUB_FOLDERS} and {@link #EXPERIMENT_NB}
	 * @return
	 */
	public static File touchOutputDirectory() {
		File outputDirectory = Paths.get(Config.DIR_RES.getAbsolutePath(), Config.getRunFolderName()).toFile();
		if(!outputDirectory.exists())
			outputDirectory.mkdir();
		return outputDirectory;
	}


	/**
	 * Warning: {@link Config#getExperiment()} coupled with {@link #EXPERIMENT_SUB_FOLDERS} and {@link #EXPERIMENT_NB}
	 * 	
	 * @param run_i Run index
	 * @param p0 Resulting population
	 * @return line of results. <br/> See {@link #createResultCSVFile(File)} for CSV header.

	 */
	private static String computeOneLineFileResults(int run_i, Program prg) {
		Oracle o = Oracle.getInstance();
		// Print line in csv :
		// run_i ; # nn/../pp ; # MM Elts ; (# OCL exactes -> last = Manual input.)
		
		//Header
		String line = Config.getExperiment() + ";" + run_i + ";";
		
		//Time
		line += (System.currentTimeMillis() - run_startingtime)/1000 + ";";
		
		//Coverage values : COVERAGE ALL / POSITIVES / NEGATIVES
		line += o.getExampleSet().getCoverage()[0] + ";";
		line += o.getExampleSet().getCoverage()[1] + ";";
		line += o.getExampleSet().getCoverage()[2] + ";";
		
		//Size : number of models ; number of objects in total
		line += o.getExampleSet().sizeAll() + ";";
		line += o.getExampleSet().refinedSize() + ";";

		//Size positives : number of valid models ; number of objects in total
		line += o.getExampleSet().getAllPositives().size() + ";";
		int sizeRefinedPos = 0;
		for (Model m : o.getExampleSet().getAllPositives()) 
			sizeRefinedPos += m.getNbClasses();
		line += sizeRefinedPos + ";";
		
		//Size negatives : number of invalid models ; number of objects in total
		line += o.getExampleSet().getAllNegatives().size() + ";";
		int sizeRefinedNeg = 0;
		for (Model m : o.getExampleSet().getAllNegatives()) 
			sizeRefinedNeg += m.getNbClasses();
		line += sizeRefinedNeg + ";";
		
		// P[P/N]N
		line += prg.getFitnessVector().printCSVStat() + ";";
		
		// Precision / Recall  of MMElements involved (in comparison with oracle)
		double[] precisionRecallF = ToolBox.precisionRecallF(
				prg.getMMElements(), 
				o.getMMElements()
			);
		line +=  precisionRecallF[0] + ";" + precisionRecallF[1] + ";" + precisionRecallF[2];

		LOGGER.fine(line);
		return line;
	}

	/**
	 * Creates a directory "DIR_EXPERIMENT / _results/ timestamp"
	 * @return The directory
	 * @throws IOException
	 */
	private static File buildDirectory() throws IOException {
		File dirMultiRun = new File(Config.DIR_EXPERIMENT.getAbsolutePath() + File.separator + "_results");
		if (!dirMultiRun.exists())
			dirMultiRun.mkdir();

		String timeStamp2 = ToolBox.START_TIME;
		String fileOutName = dirMultiRun.getAbsolutePath() + File.separator + timeStamp2;
		int itmp = 0;
		while (new File(fileOutName).exists()) {
			timeStamp2 = ToolBox.START_TIME + "-" + (++itmp);
			fileOutName = dirMultiRun.getAbsolutePath() + File.separator + timeStamp2;
		}

		File fileOut = new File(fileOutName);
		if (!fileOut.exists())
			fileOut.mkdir();

		LOGGER.info("Result directory : '" + fileOut.getAbsolutePath() + "'");
		return fileOut;
	}

	/**
	 * 
	 * @param run
	 * @param pN
	 * @param o
	 * @param ms
	 * @param outputSubDirectory 
	 */
	@SuppressWarnings("unused")
	private static void saveResults(int run, Population pN, Oracle o, ExampleSet ms, File outputDirectory) {
		
		String fileName = outputDirectory.getAbsolutePath() + File.separator + Config.run_prefix + run + ".log";

//		GeneticEntity best = pN.getBestOnObj0();
//		Program prg = (Program) best;
//		String pretty = prg.prettyPrint();
//		String ocl = prg.getOCL();

		
		evo.storeGeneralLogInFile(ToolBox.formatMillis(System.currentTimeMillis()-run_startingtime), pN, new File(fileName));
		
//		File res = new File(fileName);
//		try {
//			res.createNewFile();
//			BufferedWriter bw = new BufferedWriter(new FileWriter(res));
//			String wStr = ("*** Runs started on " + ToolBox.START_TIME + "\n*** Execution " + i + " : " + best.printFV() + "\n");
//			wStr += "\n";
//			wStr += (ocl);
//			bw.write(wStr + "\n");
//			
//			bw.close();
//			LOGGER.info("Result of execution " + i + " written in " + fileName);
//		} catch (IOException e) {
//			LOGGER.info("Exception while writing " + fileName + " (execution " + i + ")");
//			e.printStackTrace();
//		}
	}
}

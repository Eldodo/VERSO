package oclruler.metrics;

import static oclruler.metrics.MetricExtraction.LOGGER;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Model;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

public class RunMetricExtraction {
	
	static String rootDir = Config.SINBAD? "/u/batotedo/EclipseWS/":"R:/EclipseWS/";
	
	
	/**
	 * Avoid suffix specification in Config file 
	 */
	public static boolean TESTING = false;
	public static String SUFFIX_FEATURES_ADDED = "featureAdded";
	public static String SUFFIX_CONDENSED = "CONDENSED";
	public static String OUTPUT_EXTENSION = ".csv";

	
	/**
	 * avoid writing resultModified files.
	 */
	public static boolean WRITING = true;
	
	public static void main(String[] args) {
		System.out.println("Entering MetricsExtraction ");
		
		// Initialization starts
		Config.EXPERIMENT_MODE = true;
		ToolBox.loadArgs(args);
		ToolBox.printArgs();
		
		if(Config.DIR_EXPERIMENT == null){
			System.out.println("Argument missing:\n"+ToolBox.getHelp_CoommandOptionDirExperiment());// Ad hoc help
			System.out.println("Exit with error.");
			System.exit(1);
		}
		
		if(Config.METAMODEL_NAME == null){
			System.out.println("Argument missing:\n"+ToolBox.getHelp_CoommandOptionMetamodelName());// Ad hoc help
			System.out.println("Exit with error.");
			System.exit(1);
		}
				
		
		ToolBox.init();
		if(!TESTING)
			SUFFIX_FEATURES_ADDED = Config.getStringParam("SUFFIX_FEATURES_ADDED");
		
		rootDir = Config.SINBAD? "/u/batotedo/EclipseWS/":"R:/EclipseWS/";
		// Initialization ends

		// Variables
		File 		officialResultsFolder = new File(Config.DIR_EXPERIMENT+"/_results/official/");
		File 		officialResultsFile = Paths.get(officialResultsFolder.getAbsolutePath(), MetricExtraction.RESULTS_FILE_NAME).toFile();
		File[] 		officialResultsSubFolders = getRunPrefixedSubFolders(officialResultsFolder);
		File 		testDir = new File(rootDir + "material/instances/__test/"+Config.METAMODEL_NAME+"/");
		ExampleSet 	testExs = new ExampleSet(testDir);
		
		LOGGER.info("Test directory: ["+testExs.sizeAll()+"] examples from "+testDir.getAbsolutePath());
		LOGGER.info("Set up to check \n'"+officialResultsFolder.getAbsolutePath()+"'\n[\n"+Arrays.toString(officialResultsSubFolders).replaceAll(",", "\n")+"\n] examples folders");
		
		//Execution
		LinkedHashMap<String,  String[]> completeCSV_allExperiments = new LinkedHashMap<>(officialResultsSubFolders.length);
		LinkedHashMap<String,  String> completeCSV_allExperiments_avg = new LinkedHashMap<>(officialResultsSubFolders.length);
		String header = null;
		for (File resultFolder : officialResultsSubFolders) {
			LOGGER.config("Processing '"+resultFolder.getAbsolutePath().substring(resultFolder.getAbsolutePath().indexOf("experiment"))+"'");
			
			MetricExtraction metricExtraction = new MetricExtraction(officialResultsFolder, resultFolder.getName());
			if(!officialResultsFile.getAbsolutePath().equals(metricExtraction.getResultFile().getAbsolutePath())){
				System.out.println("AIE ! RewritceCSV.main() line 10X");
				System.out.println(officialResultsFile.getAbsolutePath());
				System.out.println(metricExtraction.getResultFile().getAbsolutePath());
				System.exit(17);
			}
			
			//Ajout F if not yet present.
			String[] csvAndF = metricExtraction.completeCSWithFfromLastTwoColumnsRecallAndPrecision();
			metricExtraction.setCsvLines_original(csvAndF);

			// Ajout du champ "Discrimination" à chaque run
			ExperimentResult results = ExperimentResult.extractDiscrimationRatesFromResultFOlder(testExs, resultFolder);
			String[] csvPlus = metricExtraction.completeCSVWithDiscrimination(results);
			metricExtraction.setCsvLines_original(csvPlus);
			
			//Ajout de la couverture en criss cross des element de règles.
			String[] completeCSV = metricExtraction.completeCSVWithCrossCoverage();
			metricExtraction.setCsvLines_original(completeCSV);
			
			completeCSV_allExperiments.put(resultFolder.getName(), metricExtraction.getCsvLines_original());
			
			
			if(header == null){
				header = metricExtraction.headerLineCSV;
				header += metricExtraction.completeCSWithFfromLastTwoColumnsRecallAndPrecision_header();
				header += metricExtraction.completeCSVWithDiscrimination_header();
				header += metricExtraction.completeCSVWithCrossCoverage_header();
			}
			
			//Average / Max computation
			completeCSV_allExperiments_avg.put(resultFolder.getName(), metricExtraction.condenseResultsIntoAverageAndMaxForExperimentRuns());
		}
		
		// Writing in files
		if(WRITING) {
			File modifiedResultsFile = copyInNewFileWithFeatureAddedSuffix(officialResultsFile, completeCSV_allExperiments, header);
			File condensedResultsFile = printCondensedResultsInFile(officialResultsFile, completeCSV_allExperiments_avg);
			modifiedResultsFile.exists();
			condensedResultsFile.exists();
		}
		
		
		
//		System.out.println(header);
//		print_ExpRunCSV_Map(completeCSV_allExperiments, 0, 1, 12, 13, 14, 15, /*16, 17, 18,*/ 19, 20 );
//		System.out.println(officialResultsFolder);
//		for (File resultFolder : officialResultsSubFolders) {
//			System.out.println(resultFolder);
//		}
			
		System.out.println("MetricExtraction -- Safe Exit");
	}

	private static void printModifiedResultsInFile(LinkedHashMap<String, String[]> completeCSV_allExperiments, File modifiedResultsFile, String newHeader) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(modifiedResultsFile));
			bw.write(newHeader + "\n");
			for (String run : completeCSV_allExperiments.keySet()) {
				for (String line : completeCSV_allExperiments.get(run))
					bw.write(line + "\n");
			}
			LOGGER.info("Modified results written in \n  '" + modifiedResultsFile + "'");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File  copyInNewFileWithFeatureAddedSuffix(File officialResultsFile, LinkedHashMap<String, String[]> completeCSV_allExperiments,
			String newHeader) {
		String path = officialResultsFile.getAbsolutePath();
		int pointPath = path.lastIndexOf(".");
		int suffix = 0;
		File newFile = null;
		String d = new SimpleDateFormat("yyyyMMdd", Locale.FRANCE).format(new Date());
		do {
			newFile = new File(path.substring(0, pointPath) + "_"+SUFFIX_FEATURES_ADDED +"_"+ d + "-" + suffix + OUTPUT_EXTENSION);//+ path.substring(pointPath));
			suffix++;
		} while (newFile.exists());

		printModifiedResultsInFile(completeCSV_allExperiments, newFile, newHeader);
		return newFile;
	}

	private static File printCondensedResultsInFile(File officialResultsFile, LinkedHashMap<String, String> completeCSV_allExperiments_condensed) {
		String path = officialResultsFile.getAbsolutePath();
		int pointPath = path.lastIndexOf(".");
		int suffix = 0;
		File newFile = null;
		String d = new SimpleDateFormat("yyyyMMdd", Locale.FRANCE).format(new Date());
		do {
			newFile = new File(path.substring(0, pointPath) + "_"+SUFFIX_CONDENSED +"_"+ d + "-" + suffix + OUTPUT_EXTENSION);//path.substring(pointPath));
			suffix++;
		} while (newFile.exists());

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
			bw.write(MetricExtraction.getHeaderCondensed() + "\n");
			for (String line : completeCSV_allExperiments_condensed.values())
				bw.write(line + "\n");
			LOGGER.info("Condensend results written in \n  '" + newFile + "'");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newFile;
	}

	
	
	public static void print_ExpRunCSV_Map(LinkedHashMap<String, String[]> completeCSV_allExperiments, int... idx) {
		for (String run : completeCSV_allExperiments.keySet()) {
			System.out.println("  + " + run);
			for (String line : completeCSV_allExperiments.get(run)) {
				String columns[] = line.split(";");
				String toPrint = "";
				for (int i = 0; i < columns.length; i++) {
					for (int j = 0; j < idx.length; j++) {
						if (idx[j] == i)
							toPrint += ";" + columns[i];
					}
				}
				System.out.println("   -" + toPrint);
			}
		}
	}

	public static void print_ExpRunCSV_Map(LinkedHashMap<String, String[]> completeCSV_allExperiments) {
		for (String run : completeCSV_allExperiments.keySet()) {
			System.out.println("  + " + run);
			for (String line : completeCSV_allExperiments.get(run)) {
				System.out.println("   -" + line);
			}
		}
	}
	
	/**
	 * Return all sub directories in officialResultsBase which names start with {@link #RUN_FOLDERS_PREFIX} (sorted by
	 * name).
	 * 
	 * @param officialResultsBase
	 * @return sub directories in officialResultsBase which names start with {@link #RUN_FOLDERS_PREFIX}.
	 */
	public static File[] getRunPrefixedSubFolders(File officialResultsBase) {
		LOGGER.finer(officialResultsBase.getAbsolutePath());
		File[] officialExamplesSubFolders = officialResultsBase.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() && pathname.getName().startsWith(MetricExtraction.RUN_FOLDERS_PREFIX);
			}
		});
		Arrays.sort(officialExamplesSubFolders);
		return officialExamplesSubFolders;
	}
	
	/**
	 * 
	 * Oracle models must appear in run_X_compiled.log file.<br/>
	 * Following the "Oracle :" string.
	 * @param experimentDir
	 * @param resultFolder
	 * @param runNumber
	 * @return
	 */
	public static ArrayList<Model> extractModelsUsedForOracle(ExampleSet exs, File resultFolder) {
		File compiledStatsFile = Paths.get(resultFolder.getAbsolutePath(), "run_0", "run_0_compiled.log").toFile();//Same oracle for each run, we only have to look at first one (#0)
		
		ArrayList<Model> res = new ArrayList<>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(compiledStatsFile));
			String line = br.readLine();
			while((line = br.readLine()) != null){
				if(line.trim().startsWith("Oracle :")){
					while(! (line = br.readLine()).isEmpty()){
						String modelName = line.substring(0, line.indexOf(".xmi")) + ".xmi";
						Model m = exs.getExample(modelName);
						if(null == m) {
							LOGGER.severe("Model '"+modelName+"' not found.");
						} else 
							res.add(m);
					}
					break;
				}
			}
//			System.out.println("MetricExtractor.extractModelUsedForOracle() \n  "+modelsDirectoryPositives.getAbsolutePath());
//			System.out.println(  "  "+modelsDirectoryPositives.listFiles().length);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}



}

package oclruler.metrics.tfidfexperiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import oclruler.genetics.EvaluatorOCL;
import oclruler.genetics.Evolutioner;
import oclruler.genetics.FitnessVector;
import oclruler.genetics.Oracle;
import oclruler.genetics.Oracle.OracleCmp;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.FireMap;
import oclruler.metamodel.Model;
import oclruler.metrics.MetricExtraction;
import oclruler.metrics.ProgramLoader;
import oclruler.rule.Program;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

public class MetricExtractor {
	

	public static void main(String[] args) {
		System.out.println("tfidf-experiment - MetricExtractor\n");
		ToolBox.init();
		
		ArrayList<MetricExtractor> mes = new ArrayList<>();

		String home = Config.OS_HOME + "svn/material/OCL_Ruler/";

		if (args.length > 0)
			home += args[0] + "/";
		else
			home += "results-tfidf/";

		System.out.println("Folder: '" + home + "'");
		File allResultsFolder = new File(home + Config.METAMODEL_NAME);

		File[] listFiles = allResultsFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		
		Arrays.sort(listFiles, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		System.out.println("Processing "+listFiles.length+" files...");
		
		
		for (File f : listFiles) {
			MetricExtractor me = new MetricExtractor(f);
			mes.add(me);
		}
		
		Collections.sort(mes, new Comparator<MetricExtractor>() {
			@Override
			public int compare(MetricExtractor o1, MetricExtractor o2) {
				return o1.name.compareTo(o2.name);
			}
		});
		
		try {
			File fResults = Paths.get(allResultsFolder.getParent(), "results_"+Config.METAMODEL_NAME+".csv").toFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(fResults));
			bw.write(MetricExtractor.getCSVHeader()+"\n");
			
			File fResultsData = Paths.get(allResultsFolder.getParent(), "results_"+Config.METAMODEL_NAME+".data.csv").toFile();
			BufferedWriter bwData = new BufferedWriter(new FileWriter(fResultsData));
			bwData.write(MetricExtractor.getEvolutionDataOneLineCSV_HEADER()+"\n");
			
			for (MetricExtractor me : mes) {
				bw.write(me.getCSV()+"\n");
				bwData.write(me.getEvolutionDataOneLineCSV() + "\n");
//				System.out.println(me.getEvolutionDataOneLineCSV());
			}
			bw.close();
			bwData.close();
			System.out.println("Results writen in \n - '"+fResults.getAbsolutePath()+"' \n - '"+fResultsData.getAbsolutePath()+"'");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("\nSafe exit.");
	}

	
	private static String getCSVHeader() {
		return "ALT;name;generation;DISCR";
	}


	File folder;
	File solutionFolder;
	File solutionFile;
	File solutionData;
	File solutionLog;
	
	String name;
	Program bestProgram;
	int generationNeeded = 0;
	double discrOnTestBench = 0.0;
	String ppnnpnnp;
	private ExampleSet testExs;
	private String evolutionDataOneLineCSV;
	
	private String configuration;
	
	public MetricExtractor(File f) {
		folder = f;
		
		if( !folder.exists() || !folder.isDirectory() )
			throw new IllegalStateException("Folder not working: "+folder.getAbsolutePath());
		
		this.name = folder.getName();
		
		for (File ff : folder.listFiles()) {
			if(ff.getName().endsWith(".data.log"))
				solutionData = ff;
			else if (ff.getName().endsWith(".log"))
				solutionLog = ff;
		}
		
		if( !solutionData.exists()  )
			throw new IllegalStateException("Solution data not working: "+solutionData.getAbsolutePath());
		if( !solutionLog.exists() )
			throw new IllegalStateException("Solution log not working: "+solutionLog.getAbsolutePath());

		
		solutionFolder = Paths.get(folder.getAbsolutePath(), "_solutions").toFile();
		if( !solutionFolder.exists() || !solutionFolder.isDirectory() )
			throw new IllegalStateException("Solution folder not working: "+solutionFolder.getAbsolutePath());
		
		System.out.println("Treatment: "+solutionFolder.getParentFile().getName());

		String conf = solutionFolder.getParentFile().getName();
		if(conf.startsWith("evo_tfidfcd"))
			configuration = "cd";
		else if(conf.startsWith("evo_tfidf"))
			configuration = "obj";
		else if(conf.startsWith("evo_"))
			configuration = "evo";
		else
			System.out.println("Wrong file name :"+conf);
		
		
		for (File ff : solutionFolder.listFiles()) {
			if(ff.getName().startsWith("solution0")) {
				solutionFile = ff;
				break;
			}
		}
		if( !solutionFile.exists() )
			throw new IllegalStateException("Solution file not working: "+solutionFile.getAbsolutePath());
		
		
		
		File testDir = new File( Config.OS_HOME+"svn/material/instances/__test/"+Config.METAMODEL_NAME+"/");
		if( !testDir.exists() || !testDir.isDirectory() )
			throw new IllegalStateException("Test folder not working: "+testDir.getAbsolutePath());
		testExs = new ExampleSet(testDir);

		bestProgram = ProgramLoader.loadProgramFromFile(solutionFile);
		
		try {
			if(solutionFolder.getParentFile().getName().startsWith("rnd"))
				generationNeeded = 2999;
			else
				generationNeeded = Files.readAllLines(solutionData.toPath()).size();
		} catch (IOException e) {
			throw new IllegalStateException("Solution data not working: "+solutionData.getAbsolutePath() + "\n  "+e.getMessage());
		}
		
		FitnessVector fv = new EvaluatorOCL(testExs).evaluate(bestProgram);
		ppnnpnnp = (fv.printCSVStat());
		
		discrOnTestBench = getDiscriminationRateOnTestBench();
		
		try {
			evolutionDataOneLineCSV = getEvolutionDataOneLineCSV();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getEvolutionDataOneLineCSV_HEADER()  {
		String res = "ALT;";
		int i = 0;
		while(i ++ < Evolutioner.GENERATION_MAX-1) {
			if(checkIforCSVLines(i))
				res +=  i+";";
		}
		return res;
	}

	public String getEvolutionDataOneLineCSV() throws IOException {
		if(evolutionDataOneLineCSV == null ) {
			evolutionDataOneLineCSV = "";
			int i = 0;
			for (String l : Files.readAllLines(solutionData.toPath())) {
				i++;
				if(checkIforCSVLines(i))
					evolutionDataOneLineCSV += l.split(";")[1].replaceAll("\\.", ",") +  ";";
			}
			while(i ++ < Evolutioner.GENERATION_MAX-1) {
				if(checkIforCSVLines(i))
					evolutionDataOneLineCSV +=  "1,0;";
			}
			evolutionDataOneLineCSV = configuration+";"+evolutionDataOneLineCSV;
		} 
		return evolutionDataOneLineCSV;
	}
	
	public static boolean checkIforCSVLines(int i) {
		return (i%10 == 0) && i < Evolutioner.GENERATION_MAX / 2;
	}

	public String getCSV() {
		String res = configuration + ";" + name + ";" + generationNeeded + ";" + discrOnTestBench;// +";"+ppnnpnnp;

		return res.replaceAll("\\.", ",");
	}

	private double getDiscriminationRateOnTestBench() {
		FireMap fmBase = MetricExtraction.computeDiscrimination(bestProgram, testExs);

		HashMap<Model, Boolean> validOrNotBase = new HashMap<>(testExs.getAllExamples().size());
		for (Model m : testExs.getAllExamples())
			validOrNotBase.put(m, fmBase.getFiredObjects(m).isEmpty());
		HashMap<Model, OracleCmp> oracleComparisonBase = Oracle.getOracleComparison(testExs.getAllExamples(), fmBase);

		int correctDiscrimination = 0;
		for (Model m : testExs.getAllExamples())
			correctDiscrimination += oracleComparisonBase.get(m).isRight() ? 1 : 0;

		MetricExtraction.LOGGER.finer(
				": " + correctDiscrimination + "      Rate: " + ((double) correctDiscrimination) / testExs.sizeAll());

		double discriminationOnTestBench = ((double) correctDiscrimination) / testExs.sizeAll();
		return discriminationOnTestBench;
	}

}

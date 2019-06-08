package oclruler.metrics;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

import oclruler.genetics.Oracle;
import oclruler.genetics.Oracle.OracleCmp;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.FireMap;
import oclruler.metamodel.Model;
import oclruler.rule.Program;
import oclruler.utils.Config;

public class ExperimentResult {
	String[] csvLines_original;
	
	double[] runsDiscrimination;
	double minDiscrimination, avgDiscrimination, maxDiscrimination;
	String runName;
	
	public ExperimentResult(String run, double[] runsDiscrimination, double minDiscrimination, double avgDiscrimination, double maxDiscrimination) {
		this.runName = run;
		this.runsDiscrimination = runsDiscrimination;
		this.minDiscrimination = minDiscrimination;
		this.avgDiscrimination = avgDiscrimination;
		this.maxDiscrimination = maxDiscrimination;
	}
	public static ExperimentResult extractDiscrimationRatesFromResultFOlder(ExampleSet baseExs, File resultFolder) {
		int runs = RunMetricExtraction.getRunPrefixedSubFolders(resultFolder).length;
		
//		ArrayList<Model> models = extractModelsUsedForOracle(baseExs, resultFolder);

		//Calcul cov and size ?
		
		int minDiscrimination = baseExs.sizeAll(),  maxDiscrimination = -1;
		double avgDiscrimination = 0;
		double[] discrimination = new double[runs];
		for(int runi = 0; runi < runs ; runi++){
			File runFile = Paths.get(resultFolder.getAbsolutePath(), "run_"+runi).toFile();
			Program pBank = ProgramLoader.loadProgramFromFile(runFile);
			FireMap fmBase = MetricExtraction.computeDiscrimination(pBank, baseExs);
			
			HashMap<Model, Boolean> validOrNotBase = new HashMap<>(baseExs.getAllExamples().size());
			for (Model m : baseExs.getAllExamples()) 
				validOrNotBase.put(m, fmBase.getFiredObjects(m).isEmpty());
			HashMap<Model, OracleCmp> oracleComparisonBase = Oracle.getOracleComparison(baseExs.getAllExamples(), fmBase);
			
			int correctDiscrimination = 0;
			for (Model m : baseExs.getAllExamples()) 
				correctDiscrimination += oracleComparisonBase.get(m).isRight()?1:0;
			
			MetricExtraction.LOGGER.finer(resultFolder.getName()+": "+correctDiscrimination+  "      Rate: "+((double)correctDiscrimination)/baseExs.sizeAll());
			
			discrimination[runi] = ((double)correctDiscrimination)/baseExs.sizeAll();
			minDiscrimination = Math.min(correctDiscrimination, minDiscrimination);
			maxDiscrimination = Math.max(correctDiscrimination, maxDiscrimination);
			avgDiscrimination += correctDiscrimination;
		}
		avgDiscrimination = avgDiscrimination / (baseExs.sizeAll() * runs);
		
		MetricExtraction.LOGGER.fine("Stats:");
		MetricExtraction.LOGGER.fine("  min:"+minDiscrimination);
		MetricExtraction.LOGGER.fine("  avg:"+avgDiscrimination);
		MetricExtraction.LOGGER.fine("  max:"+maxDiscrimination);
		
		String name = Config.METAMODEL_NAME + "_" + (resultFolder.getAbsolutePath().contains("Classic")?"Classic":"Incremental") + "_" + resultFolder.getName();
		
		return new ExperimentResult(name, discrimination, minDiscrimination, avgDiscrimination, maxDiscrimination);
	}

	
	public void setCsvLines_original(String[] csvLines_original) {
		this.csvLines_original = csvLines_original;
	}
	
	@Override
	public String toString() {
		return "["+runName+": {"+minDiscrimination+"<"+avgDiscrimination+"<"+maxDiscrimination+"}]";
	}
	
	/**
	 * See {@link #getCSV_header()}.
	 * @return
	 */
	public String getCSV(){
		return minDiscrimination+";"+avgDiscrimination+";"+maxDiscrimination;
	}
	
	public double getRunDiscrimination( int run){
		if(run < 0 || run >= runsDiscrimination.length)
			throw new IllegalArgumentException("Argument 'run' must be a valid run, here, it must be comprised between "+0+" and "+(runsDiscrimination.length-1)+".");
		return runsDiscrimination[run];
	}
	/**
	 * 
	 * @return MIN_DISCRIMINATION;AVG_DISCRIMINATION;MAX_DISCRIMINATION
	 */
	public static String getCSV_header(){
		return "MIN_DISCRIMINATION;AVG_DISCRIMINATION;MAX_DISCRIMINATION";
	}
}

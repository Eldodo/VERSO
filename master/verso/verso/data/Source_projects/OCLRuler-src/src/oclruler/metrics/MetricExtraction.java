package oclruler.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import oclruler.genetics.EvaluatorOCL;
import oclruler.genetics.Oracle;
import oclruler.genetics.OraculizationException;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.FireMap;
import oclruler.metamodel.MMElement;
import oclruler.metamodel.Model;
import oclruler.rule.Program;
import oclruler.rule.struct.Constraint;
import oclruler.utils.ToolBox;

public class MetricExtraction {
	
	public final static Logger LOGGER = Logger.getLogger(MetricExtraction.class.getName());
	
	File 	resultFolder;
	private File resultFile;

	private Oracle 		oracle;
	private ExampleSet examples;
	Program[] 	programs;
	
	File[] 		programFiles;
	String[] 	csvLines_original;
	String 		headerLineCSV;
	boolean headerPLusF = false, headerPlusDiscrimination = false, headerPlusCrossCoverage = false;
	
	public final static String RESULTS_FILE_NAME = "results.txt";
	/** Used for run_XXexamples examples folders and run_I experiment run output folder  */
	public final static String RUN_FOLDERS_PREFIX = "run_";
	
	public File getResultFile() {
		return resultFile;
	}
	
	/**
	 * 
	 * @param rootResultFolder by default, something like "_results/official/" containing run_XXexamples folders.
	 * @param numberOfExamples Number of examples of the targeted experiment.
	 */
	public MetricExtraction(File rootResultFolder, int numberOfExamples) {
		this(rootResultFolder, MetricExtraction.RUN_FOLDERS_PREFIX+(numberOfExamples >= 10 ? numberOfExamples: "0"+numberOfExamples)+"examples");
	}
	
	public MetricExtraction(File rootResultFolder, String subFolder) {
		this.resultFile = new File(rootResultFolder.getAbsolutePath()+File.separator+RESULTS_FILE_NAME);
		this.resultFolder = Paths.get(rootResultFolder.getAbsolutePath(), subFolder).toFile();
		this.oracle = Oracle.getInstance();
		this.examples = ExampleSet.getInstance();
		
		/*
		 * Check parameters
		 */
		if(resultFolder == null || !resultFolder.exists() || !resultFolder.isDirectory()) {
			LOGGER.severe("Metrics extraction impossible, folder is null, empty or is not a drectory : '"+resultFolder.getAbsolutePath()+"'");
			return;
		}
		
		String log0 = "";
		log0 += " - Result folder: "+resultFolder.getAbsolutePath() + "\n";
		log0 += " - Result file:   "+resultFile.getAbsolutePath();
		LOGGER.fine(log0);
		
		// Loads the runs int #programs
		loadPrograms();
		// Fills #csvLines_original and #csvLines_completed 
		readOriginalCSV(subFolder, programs.length);
		
		if(LOGGER.isLoggable(Level.FINER)){
			String log = "Programs extracted: \n";
			for (int i = 0; i < csvLines_original.length; i++) {
				log += " ["+i+"] "+resultFolder.getName()+" - "+programs[i] + ": " +csvLines_original[i]+"\n";
			}
			log = log.trim();
			LOGGER.finer(log);
		}
	}
	
	public File getResultFolder() {
		return resultFolder;
	}
	
	public void setCsvLines_original(String[] csvPlus) {
		csvLines_original = Arrays.copyOf(csvPlus, csvPlus.length);
	}
	public String[] getCsvLines_original() {
		return csvLines_original;
	}
	public void printCsvLines_original() {
		for (String l : csvLines_original) {
			System.out.println(l);
		}
	}

	/**
	 * ATTENTION !!!! <br/>
	 * Assumes that the two last columns of {@link #csvLines_original} contains general solution Recall and Precision on MMelements.
	 * @param results
	 * @return completed CSV lines with F measure added at end
	 */
	public String[] completeCSWithFfromLastTwoColumnsRecallAndPrecision(){
		if(csvLines_original == null){
			System.out.println("CSV not ready yet.");
			return new String[] {};
		}
		if(headerLineCSV == null)
			return csvLines_original;
		if (headerLineCSV.startsWith("recall")) 
			return csvLines_original;
		LOGGER.fine("Adding F to recall and precision.");
		headerPLusF = true;
		String[] res = new String[csvLines_original.length];
		for (int i = 0; i < csvLines_original.length; i++) {
			if(csvLines_original[i] != null){
				String[] allColumns = csvLines_original[i].split(";");
				double precision = Double.parseDouble(allColumns[allColumns.length-2]);
				double recall 	 = Double.parseDouble(allColumns[allColumns.length-1]);
				double F =  (2 * (precision * recall) / (precision + recall));
				res[i]  = csvLines_original[i] + ";" + F;
			}
//			System.out.println(precision +" / " + recall + " = " + F);
		}
		return res;
	}
	public String completeCSWithFfromLastTwoColumnsRecallAndPrecision_header(){
		if(headerLineCSV == null)
			return "";
		if (headerLineCSV.startsWith("recall")) 
			return "";
		return ";FMMElts";
	}
	
	/**
	 * 
	 * @param results
	 * @return completed CSV lines with Discrimination rate added at end
	 */
	public String[] completeCSVWithDiscrimination(ExperimentResult results){
		if(csvLines_original == null){
			System.out.println("CSV not ready yet.");
			return new String[] {};
		}
		headerPlusDiscrimination = true;
		String[] res = new String[csvLines_original.length];
		for (int i = 0; i < csvLines_original.length; i++) 
			res[i] = csvLines_original[i] +";"+ results.getRunDiscrimination(i) ;
		
		return res;
	}
	public String completeCSVWithDiscrimination_header(){
		return ";DISCRIMINATION_RATE";
	}

	public Program getProgramRun(int idx){
		if(programs == null || programs.length == 0){
			loadPrograms();
			if(programs == null || programs.length == 0)
				throw new IllegalArgumentException("Programs not loaded.");
		}
		if(idx >= programs.length)
			throw new IllegalArgumentException("Run "+idx+" does not exist.\nOnly "+programs.length+" runs [0.."+(programs.length-1)+"] in '"+resultFolder+"'.");
		return programs[idx];
	}
	
	public double[][][] getCrisscrossMatrice(int idx){
		return getCrisscrossMatrice(getProgramRun(idx));
	}
	
	public double[][][] getCrisscrossMatrice(Program p1){
		double[][][] crisscross = computeCrissrossCoverage(p1, Oracle.getInstance());
		return crisscross;
	}
	
	/**
	 * Load programs from resultFolder using {@link MetricExtraction#RUN_FOLDERS_PREFIX} to extract run name from sub folders
	 */
	public void loadPrograms() {
		programFiles = this.resultFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(RUN_FOLDERS_PREFIX);//name.endsWith(".ocl");//
			}
		});
//		System.out.println("MetricExtraction.loadPrograms() programFiles: " + Arrays.toString(programFiles));
		Arrays.sort(programFiles, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				int iFile1 = Integer.parseInt(o1.getName().substring(RUN_FOLDERS_PREFIX.length()));
				int iFile2 = Integer.parseInt(o2.getName().substring(RUN_FOLDERS_PREFIX.length()));
				return iFile1 - iFile2;
			}
		});

		programs = new Program[programFiles.length];
		for (int i = 0; i < programFiles.length; i++) 
			programs[i] = ProgramLoader.loadProgramFromFile(programFiles[i], "Prg_"+programFiles[i].getName());
		
		LOGGER.finer(programFiles.length+" runs found");
	}

/**
 * Read original CSV file and store lines in {@link #csvLines_original} and {@link #csvLines_completed}
 * @param subFolder
 * @param numberOfLines
 */
	public void readOriginalCSV(String subFolder, int numberOfLines) {
		csvLines_original = new String[numberOfLines];
		
		String exampleNumberLineCSV = subFolder.substring(RUN_FOLDERS_PREFIX.length()); // extracts "10examples" from "run_10examples" 
		
		ArrayList<String> allLines = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(resultFile));
			String lineTmp = br.readLine();//Passes csv header
			headerLineCSV = lineTmp; //Store csv header
			while ((lineTmp = br.readLine()) != null) {
				allLines.add(lineTmp);
			} 
			
			int i = 0;
			for (String l : allLines) {
				if(i >= numberOfLines)
					break;
				if(!l.startsWith(exampleNumberLineCSV))
					continue;
				else {
					csvLines_original[i] = l;
					i++;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Take the original CSV lines and complete them with
	 * <ol>
	 *  <li>cross coverage : precision recall and F of affected MMElements between solutions and Oracle. (see {@link MetricExtraction#computeCrissrossCoverage(Program, Program)}) </li>
	 *  <li></li>
	 *</ol>
	 * 
	 * @return
	 */
	public String[] completeCSVWithCrossCoverage(){
		if(csvLines_original == null){
			System.out.println("CSV not ready yet.");
			return new String[] {};
		}
		headerPlusCrossCoverage = true;
		String[] csvLines_completed = new String[csvLines_original.length];
		for (int i = 0; i < csvLines_original.length; i++) 
			csvLines_completed[i] = csvLines_original[i];
		
		
		for (int i = 0; i < csvLines_original.length; i++) {
//			System.out.println(" ["+i+"] "+resultFolder.getName()+" - "+programs[i] + ": " +csvLines_completed[i]+"\n");
			double[][][] crossCov = computeCrissrossCoverage(programs[i], oracle);
			
			int programCstFound = 0;
			for (int j = 0; j < crossCov.length; j++) {
				boolean programCstFoundFlag = false;
				for (int k = 0; k < crossCov[j].length && !programCstFoundFlag; k++) {
					double f = crossCov[j][k][2];
					if (f >= 1L) {
						programCstFound++;
						programCstFoundFlag = true;
					}
				}
			}
			int oracleCstFound = 0;
			for (int j = 0; j < crossCov[0].length; j++) {
				boolean oracleCstFoundFlag = false;
				for (int k = 0; k < crossCov.length && !oracleCstFoundFlag; k++) {
					double f = crossCov[k][j][2];
					if (f >= 1L) {
						oracleCstFound++;
						oracleCstFoundFlag = true;
					}
				}
			}
			double recall = (double) programCstFound / crossCov.length;
			double precision = (double) oracleCstFound / crossCov[0].length;
			double F = 2 * ((recall * precision) / (recall + precision));
			
			if(precision == Double.NaN)
				precision = 0L;
			if(recall == Double.NaN)
				recall = 0L;
			if(F == Double.NaN)
				F = 0L;
			
			csvLines_completed[i] += ";"+precision+";"+recall+";"+F;
		}
		return csvLines_completed;
	}
	
	public String completeCSVWithCrossCoverage_header(){
		return ";CRISS_CROSS_Precision;CRISS_CROSS_Recall;CRISS_CROSS_F";
	}

	public static String printMatrice2D(double[][][] crossCov) {
		String res = "";
		String line = " ";
		for (int i = 0; i < crossCov.length; i++)
			line += i;

		res += line + "\n";
		res += " " + ToolBox.completeString("", '-', crossCov.length) + " " + "\n";

		for (int j = 0; j < crossCov[0].length; j++) {
			String l = "|";
			for (int k = 0; k < crossCov.length; k++) {
				double f = crossCov[k][j][2];

				l += (f == 1.0) ? "V" : ((f > 0L) ? String.format("%.0f", f * 10) : " ");

				// l += ((f == 1.0) ? "x" : " ");
			}
			res += (l + "| " + j) + "\n";
		}

		res += (" " + ToolBox.completeString("", '-', crossCov[0].length) + " ");
		return res;
	}
	
	public double[][][][] computeAllCrossCoverage() {
		if(programs.length != programFiles.length || programs.length != csvLines_original.length){
			throw new IllegalArgumentException();
		}
		
		double[][][][] res = new double[programs.length][][][];
		int i = 0;
		for (Program p : programs) {
			res[i] = computeCrissrossCoverage(oracle, p);
			i++;
		}
		return res;
	}
	
	/**
	 * Compute the crossed {@link ToolBox#precisionRecallF(java.util.Set, java.util.Set) precision-recall-F matrix} between two OCL constraints sets (programs).
	 * 
	 * 
	 * @param p1
	 * @param p2
	 * @return Constraint_p1 X Constraint_p2 X (Precision/Recall/F) of mm elements)
	 */
	public double[][][] computeCrissrossCoverage(Program p1, Program p2) {
		// Matrice : Contraintes P1 VS Contraintes P2
		ArrayList<Constraint> constraintsP1 = p1.getConstraints();
		ArrayList<Constraint> constraintsP2 = p2.getConstraints();
		
		double[][][] res = new double[constraintsP1.size()][][];
		int i = 0;
		Set<MMElement> setO = new HashSet<MMElement>();
		Set<MMElement> setP = new HashSet<MMElement>();
		
//		for (Constraint pc : constraintsP2) {
//			System.out.println(pc.getOCL());
//			System.out.println(pc.getMMElements());
//			
//			System.out.println(pc.getLeafs());
//		}
		for (Constraint oc : constraintsP1) {
			setO.addAll(oc.getMMElements());
			
			res[i] = new double[constraintsP2.size()][3];
			int j = 0;
			for (Constraint pc : constraintsP2) {
				
				setP.addAll(pc.getMMElements());
				res[i][j] = ToolBox.precisionRecallF(oc.getMMElements(), pc.getMMElements());
				if(LOGGER.isLoggable(Level.FINE))
					LOGGER.finer(oc.getName()+"|"+pc.getName()+" : "+ Arrays.toString(res[i][j]));
				j++;
			}
			i++;
		}
//		System.out.println("MetricExtraction.computeCrossCoverage()");
//		System.out.println(setO);
//		
//		System.out.println(setP);
//		for (MMElement mmElement : setO) {
//			if(!setP.contains(mmElement))
//				setP.remove(mmElement);
//		}
//		System.out.println(setP);
//		System.out.println();
		return res;
	}
	
	public FireMap computeDiscrimination(Program p) {
		return computeDiscrimination(p, examples);
	}
	
	public static FireMap computeDiscrimination(Program p, ExampleSet exs) {
		// Matrice : Contraintes P1 VS Contraintes P2
		if(!exs.isOraculized()){
			EvaluatorOCL eva = new EvaluatorOCL(exs);
			try {
				Oracle.getInstance().oraculize(eva, exs);
			} catch (OraculizationException e) {
				e.printStackTrace();
			}
		}
		FireMap fm = new FireMap();
		LOGGER.fine(exs.sizeAll()+" examples in '"+exs.getDirectory().getAbsolutePath()+"'");
		for (Model m : exs.getAllExamples()) {
			EvaluatorOCL.execute(fm, m, p);
		}
		return fm;
	}

	public String getNumberofExamples() {
		String nbStr = getResultFolder().getName().substring(MetricExtraction.RUN_FOLDERS_PREFIX.length(), MetricExtraction.RUN_FOLDERS_PREFIX.length()+2);
		return nbStr;
	}

	/**
	 * Take result files and condense results into AVG and MAX values where applicable (See {@link CSV_HEADER#getValuesToCondense()})
	 * @return
	 */
	public String condenseResultsIntoAverageAndMaxForExperimentRuns() {
		String[][] csv = new String[csvLines_original.length][];

		for (int i = 0; i < csv.length; i++)
			csv[i] = csvLines_original[i].split(";");

		double[] max = new double[csv[0].length];
		double[] avg = new double[csv[0].length];
		for (int i = 0; i < avg.length; i++) {
			max[i] = -1;
			avg[i] = 0;
		}

		for (int i = 0; i < csv.length; i++) {
			for (int j = 0; j < csv[i].length; j++) {
				if (CSV_HEADER.isCondensableValue(j)) {
					double v = Double.parseDouble(csv[i][j]);
					if(v == Double.NaN)
						v = 0L;
					if (v > max[j])
						max[j] = v;
					avg[j] += v;
				}
			}
		}
		for (int i = 0; i < avg.length; i++) 
			avg[i] = avg[i] / csvLines_original.length;
		
		
		String res = "";
		for (int i = 0; i < csv[0].length; i++) {
			if (!CSV_HEADER.isCondensableValue(i))
				res += csv[0][i];
			else {
				res += avg[i] + ";" + max[i];
			}
			res += ";";
		}
		res = res.substring(0, res.length()-1);
		return res;
	}
	enum CSV_HEADER_CONDENSED {
		experiment,
		run,
		time_s,
		cov_all,
		cov_pos,
		cov_neg,
		size,
		refinedSize,
		sizePos,
		sizeRefinedPos,
		sizeNeg,
		sizeRefinedNeg,
		pp_avg,pp_max,
		pn_avg,pn_max,
		np_avg,np_max,
		nn_avg,nn_max,
		precisionMMElts_avg,precisionMMElts_max,
		recallMMElts_avg,recallMMElts_max,
		FMMElts_avg,FMMElts_max,
		DISCRIMINATION_RATE_avg,DISCRIMINATION_RATE_max,
		CRISS_CROSS_Precision_avg,CRISS_CROSS_Precision_max,
		CRISS_CROSS_Recall_avg,CRISS_CROSS_Recall_max,
		CRISS_CROSS_F_avg,CRISS_CROSS_F_max;
	}
	enum CSV_HEADER {
		experiment,
		run,
		time_s,
		cov_all,
		cov_pos,
		cov_neg,
		size,
		refinedSize,
		sizePos,
		sizeRefinedPos,
		sizeNeg,
		sizeRefinedNeg,
		pp,
		pn,
		np,
		nn,
		precisionMMElts,
		recallMMElts,
		FMMElts,
		DISCRIMINATION_RATE,
		CRISS_CROSS_Precision,
		CRISS_CROSS_Recall,
		CRISS_CROSS_F;
		
		public static boolean isDoubleValue(int idx){
			for (CSV_HEADER h : CSV_HEADER.getDoubleValues())
				if (h.ordinal() == idx)
					return true;
			return false;
		}
		public static boolean isCondensableValue(int idx){
			for (CSV_HEADER h : CSV_HEADER.getValuesToCondense())
				if (h.ordinal() == idx)
					return true;
			return false;
		}
		public static CSV_HEADER getCSV_HEADER(int idx){
			for (CSV_HEADER ch : CSV_HEADER.values()) 
				if(ch.ordinal() == idx)
					return ch;
			throw new IllegalArgumentException("'"+idx+"' is not a valid CSV_HEADER index.");
		}
		
		public static CSV_HEADER[] getDoubleValues(){
			return new CSV_HEADER[] {
				cov_all,
				cov_pos,
				cov_neg,
				size,
				refinedSize,
				sizePos,
				sizeRefinedPos,
				sizeNeg,
				sizeRefinedNeg,
				pp,
				pn,
				np,
				nn,
				precisionMMElts,
				recallMMElts,
				FMMElts,
				DISCRIMINATION_RATE,
				CRISS_CROSS_Precision,
				CRISS_CROSS_Recall,
				CRISS_CROSS_F
			};
		}
		
		public static CSV_HEADER[] getValuesToCondense(){
			return new CSV_HEADER[] {
					time_s,
					size,
					refinedSize,
					sizePos,
					sizeRefinedPos,
					sizeNeg,
					sizeRefinedNeg,
					pp,
					pn,
					np,
					nn,
					precisionMMElts,
					recallMMElts,
					FMMElts,
					DISCRIMINATION_RATE,
					CRISS_CROSS_Precision,
					CRISS_CROSS_Recall,
					CRISS_CROSS_F};
		}
	}
	public static String getHeaderCondensed() {
		String res = "";
		for (CSV_HEADER_CONDENSED hc : CSV_HEADER_CONDENSED.values()) 
			res+= hc + ";";
		
		res = res.substring(0, res.length()-1);
		return res;
	}

	
}

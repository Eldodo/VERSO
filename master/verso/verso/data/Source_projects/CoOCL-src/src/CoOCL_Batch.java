import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.commons.math3.ml.clustering.CentroidCluster;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import coocl.ocl.Program;
import oclruler.genetics.Evaluator;
import oclruler.genetics.Evolutioner;
import oclruler.genetics.FitnessVector;
import oclruler.genetics.GeneticEntity;
import oclruler.genetics.Population;
import utils.Config;
import utils.Utils;
import utils.distance.DoublePointProgram;

public class CoOCL_Batch {
	static Logger LOGGER = Logger.getLogger(CoOCL_Batch.class.getName());
	static Logger LOGGER_batch = Logger.getLogger("LOGGER_batch");

	public static void main(String[] args) {
		Utils.initInSilence(args[0]);
		
		
		
		int NB_CLUSTERS_LO = 3;
		int NB_CLUSTERS_UP = 20;

		
		
		// int NUMBER_OF_EXECUTIONS = 1;
		String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.FRANCE).format(new Date());
		
		File dir_main = new File(Config.DIR_RESULTS + File.separator + Config.METAMODEL_NAME + File.separator);
		if (!dir_main.exists())
			dir_main.mkdir();

		String base = dir_main.getAbsolutePath() + File.separator + "batch_" + (Config.SINBAD ? "sinbad_" : "") + timeStamp ;
		String fileName = base + ".csv";
		String fileNameSetting =  base + "_setting"+".log";
		int itmp = 1;
		while (new File(fileName).exists() 
				|| new File(fileNameSetting).exists()) {
    		fileName = base+"-"+itmp+".csv";
    		fileNameSetting = base+"-"+itmp+"_setting.log";
        	itmp++;
    	}

		
		LOGGER_batch.info("\n"+timeStamp+"-"+itmp+" : "+Config.NUMBER_OF_EXECUTIONS + " executions.\n"
				+ "Files:\n"
				+ " - "+dir_main.getAbsolutePath()+"\n"
				+ " - "+ fileName +"\n"
				+ " - "+ fileNameSetting);
		
		BufferedWriter bw, bwSettings;
		try {
			File f = new File(fileName);
			f.createNewFile();
			File fSetting = new File(fileNameSetting);
			
			bw = new BufferedWriter(new FileWriter(f));
			String header = "NrulesFoundP;NrulesComposedP;AvgNrulesFoundP";
			for (int i = NB_CLUSTERS_LO; i <= NB_CLUSTERS_UP; i++) {
				header += ";NrulesComposed"+i+"C;AvgNrulesFound"+i+"C";
				header += ";NrulesComposed"+i+"RE;AvgNrulesFound"+i+"RE";
				header += ";NrulesComposed"+i+"RM;AvgNrulesFound"+i+"RM";
			}
			
			
			bw.write(header+"\n");
			
			
			bwSettings = new BufferedWriter(new FileWriter(fSetting));
			bwSettings.write("Run "+timeStamp+"\n\n ** SETTING **\n\n"+Config.printSetting(""));
			bwSettings.write("\n\nExpected:\n"+Program.getExpectedSolution().printExecutableOCL()+"\nErrors: \n"+Program.getExpectedSolution().printSyntaxErrors("  "));
			bwSettings.flush();
			
			int inPareto = 0, foundInCenters = 0;
			long start = System.currentTimeMillis();
			for (int i = 0; i < Config.NUMBER_OF_EXECUTIONS; i++) {
				Utils.init(args[0]);
				

				long t = System.currentTimeMillis();
			
				Evaluator eva = new Evaluator();
				LOGGER_batch.config(i+": Generating initial population of "+Population.POPULATION_SIZE+" entities.");
				Population pop0 = Population.createRandomPopulation(Program.getInitialProgram(), eva);
				LOGGER_batch.config(i+": Evaluating initial population.");
				pop0.evaluate(eva);
	
				Evolutioner evo = new Evolutioner(eva, pop0);
				LOGGER_batch.config(i+": Evolution starts for "+Evolutioner.GENERATION_MAX+" generations...");
				Population popN = evo.evolutionate();
				LOGGER_batch.config(i+": Evolution finished !");
	
				LOGGER_batch.finer(popN.printStatistics());
				String statLine = printStatisticsOfPopulation(popN, NB_CLUSTERS_LO, NB_CLUSTERS_UP, bwSettings);
				bw.append(statLine+"\n");
				bw.flush();
			}
			
			bw.close();
			
			String log = "Results written in: " + f.getAbsolutePath() + " (Setting in: "+fSetting.getAbsolutePath()+")"
					+ "\n Found :"
					+ "\n  - in pareto:  "+inPareto
					+ "\n  - in centers: "+foundInCenters
					+ "\nDuration: "+Utils.formatMillis(System.currentTimeMillis() - start);
			LOGGER_batch.info(log);

			bwSettings.append("\n\n"+log);
			bwSettings.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Exit - sound and safe.");
	}
	
	
		static int ERR_MARGIN = 2;
	
	public static String printStatisticsOfPopulation(Population popN, int nbClustersMin, int nbClustersMax, BufferedWriter out) {
		
		
//		boolean inParetoB = Evolutioner.checkPresenceOfGroundTruthInPopulation(popN, Program.getExpectedSolution()) != null;

//		double minDist = Double.MAX_VALUE, distNSE = Double.MAX_VALUE, avgDist = 0.0;
//		Program closest = null, closestCentres = null, closestNoSyntaxErrors = null;
		;

		
//		boolean[] found = new boolean[Program.getNumberOfContraintes()];
//		for (GeneticEntity ge : popN.getFrontPareto().getEntities()) {
//			Program p = (Program) ge;
//			double distPtoGT = p.getDistance(prgGroundTruth);
//			if (distPtoGT < minDist) {
//				minDist = distPtoGT;
//				closest = p;
//			}
//			if (distPtoGT < distNSE && closestNoSyntaxErrors != null && closestNoSyntaxErrors.getFitnessVector().getValue(FitnessVector.OBJECTIVE.NUMBER_OF_SYNTAX_ERRORS.getIdx()) == 0) {
//				distNSE = distPtoGT;
//				closestNoSyntaxErrors = p;
//			}
//			avgDist += distPtoGT;
//
//			for (int i = 0; i < found.length; i++) {
//				if (!found[i] && p.computeDamerauLevensteinDistances(Program.getExpectedSolution())[i] == 0.0)
//					found[i] = true;
//			}
//		}
//
//		int numberOfConstraintsFound = 0;
//		for (double dist2 : closest.computeDamerauLevensteinDistances(Program.getExpectedSolution())) {
//			if (dist2 <= ERR_MARGIN)
//				numberOfConstraintsFound++;
//		}
//
//		int numberOfConstraintsFound_combined = 0;
//		for (boolean b : found) {
//			if (b)
//				numberOfConstraintsFound_combined++;
//		}
//
//		String line = 
//				//(numberOfConstraintsFound == Program.getNumberOfContraintes() ? 1 : 0) + ";" +
//				numberOfConstraintsFound + ";" + numberOfConstraintsFound_combined 
//		// + ";" +
//		// Arrays.toString(closest.computeDamerauLevensteinDistances(prgGroundTruth))
//		textSolution += "Closest in pareto: " + closest + " " + Arrays.toString(closest.computeDamerauLevensteinDistances(prgGroundTruth)) + ")" + "\n" + closest.printExecutableOCL()
//				+ closest.printSyntaxErrors("") + "\n";
		
		
		ArrayList<Program> paretoFront = new ArrayList<>(popN.getFrontPareto().getEntities().size());
		for (GeneticEntity ge : popN.getFrontPareto().getEntities()) 
			paretoFront.add((Program)ge);
		String[] line_TextSolution = printRecommandationLineAndTextSolution(paretoFront, paretoFront.size(), popN);
		String line = line_TextSolution[0];
		String textSolution = line_TextSolution[1];

		for (int j = nbClustersMin; j <= nbClustersMax; j++) {
			String[] toPrint = printRecommandationLineAndTextSolution(getCenterFromClusters(popN, j), j, popN);
			line += toPrint[0];
			textSolution += toPrint[1];
			toPrint = printRecommandationLineAndTextSolution(getFirsts_MonoValue(popN, j), j, popN);
			line += toPrint[0];
			textSolution += toPrint[1];
			toPrint = printRecommandationLineAndTextSolution(getFirsts_Euclidian(popN, j), j, popN);
			line += toPrint[0];
			textSolution += toPrint[1];
		}
		// if(out != null)
		try {
			out.write("" + line + "\nSolutions:" + textSolution);
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER_batch.config("" + line);
		LOGGER_batch.finer("Solutions:" + textSolution);
		return line;
	}



	private static String[] printRecommandationLineAndTextSolution(List<Program> recomendation, int nbRecommends, Population popN) {
		String line = "", textSolution ="";
		// On recupere les centres des clusters
		
		if(recomendation == null){
			textSolution += "Error sur Execution : avec pop de "+popN.size()+" entities et pareto : "+popN.getFrontPareto().size()+"\n";
			line += "ERR" + 
					";ERR" + 
					";ERR" ;
		} else {
			textSolution += "\n\n -- " + nbRecommends + " recomend:\n";
			int k = 0;
			double minDistC = Double.MAX_VALUE, avgDistC = 0.0;
			boolean foundInCenters = false;
			boolean[] foundC = new boolean[Program.getNumberOfContraintes()];
			int maxNumberOfConstraintsFound = 0;
			Program closestCentres = null;
			float avgNbConstraintFound = 0;
		for (Program p : recomendation) {
			// For each cluster check if solution and compute distance
			// to GroundTruth
			double distPtoGT = p.getDistance(Program.getExpectedSolution());
			if (distPtoGT < minDistC) {
				minDistC = distPtoGT;
				closestCentres = p;
			}
			textSolution += "   - Recomend " + k++ + "  " + p + " : " + Arrays.toString(p.computeDamerauLevensteinDistances(Program.getExpectedSolution())) + ")\n";
			if (p.equals(Program.getExpectedSolution()))
				foundInCenters = true;
			avgDistC += minDistC;

			for (int i = 0; i < foundC.length; i++) {
				if (!foundC[i] && p.computeDamerauLevensteinDistances(Program.getExpectedSolution())[i] <= ERR_MARGIN)
					foundC[i] = true;
			}
			int nbConstraintsFound = 0;
			for (int i = 0; i < foundC.length; i++) {
				if (p.computeDamerauLevensteinDistances(Program.getExpectedSolution())[i] <= ERR_MARGIN)
					nbConstraintsFound++;
			}
			avgNbConstraintFound += nbConstraintsFound;
			maxNumberOfConstraintsFound = Math.max(maxNumberOfConstraintsFound, nbConstraintsFound);
		}
		avgNbConstraintFound = avgNbConstraintFound / recomendation.size();
//				int numberOfConstraintsFoundInCenters = 0;
//				for (double dist2 : closestCentres.computeDamerauLevensteinDistances(Program.getExpectedSolution())) {
//					if (dist2 == 0.0)
//						numberOfConstraintsFoundInCenters++;
//				}

		int numberOfConstraintsFound_combinedC = 0;
		for (boolean b : foundC) {
			if (b)
				numberOfConstraintsFound_combinedC++;
		}
		line += maxNumberOfConstraintsFound + 
				";" + numberOfConstraintsFound_combinedC  +
				";" + avgNbConstraintFound
//				+ ";" +Arrays.toString(closestCentres.computeDamerauLevensteinDistances(prgGroundTruth))
				;
		textSolution += "   - Closest in " + nbRecommends + " recomends: " + closestCentres + " " + Arrays.toString(closestCentres.computeDamerauLevensteinDistances(Program.getExpectedSolution())) + ")" + "\n"
				+ closestCentres.printExecutableOCL() + closestCentres.printSyntaxErrors("") + "\n";

	}
		return new String[] {line, textSolution};
	}



	private static List<Program> getCenterFromClusters(Population popN, int j) {
		ArrayList<Program> res = new ArrayList<>(j);
		List<CentroidCluster<DoublePointProgram>> clustersKM = null;;
		try {
			clustersKM = Evolutioner.clusterFrontParetoFromPopulation(popN, j);
		} catch (Exception e) {
			System.out.println("Error sur Execution : "+j+" clusters avec pop de "+popN.size()+" entities et pareto : "+popN.getFrontPareto().size());
			e.printStackTrace();
			return null;
		}
	
		for (CentroidCluster<DoublePointProgram> clusterKM : clustersKM) {
			// For each cluster check if solution and compute distance
			// to GroundTruth
			DoublePointProgram center = Evolutioner.extractCenterFromCluster(clusterKM);
			res.add(center.getProgram());
		}
		return res;
	}
	private static List<Program> getFirsts_MonoValue(Population popN, int howMany) {
		ArrayList<Program> res = new ArrayList<>(howMany);
		double[] maxObjectives = new double[FitnessVector.NUMBER_OF_OBJECTIVES];
		for (GeneticEntity ge : popN.getEntities()) {
			res.add((Program)ge);
			maxObjectives[0] = Math.max(ge.getFitnessVector().getValue(0), maxObjectives[0]);
		}
		
		Collections.sort(res, GeneticEntity.getMinMaxValueComparator(maxObjectives));
		return res.subList(0, howMany-1);
	}
	
	private static List<Program> getFirsts_Euclidian(Population popN, int howMany) {
		ArrayList<Program> res = new ArrayList<>(howMany);
		for (GeneticEntity ge : popN.getEntities()) {
			res.add((Program)ge);
		}
		Collections.sort(res, (Program o1, Program o2) -> o1.euclidianToOptimum() - o2.euclidianToOptimum() );
		return res.subList(0, howMany-1);
	}
	

}

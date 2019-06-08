import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.commons.math3.ml.clustering.CentroidCluster;

import coocl.ocl.Program;
import oclruler.genetics.Evaluator;
import oclruler.genetics.Evolutioner;
import oclruler.genetics.FitnessVector;
import oclruler.genetics.GeneticEntity;
import oclruler.genetics.Population;
import utils.Config;
import utils.Utils;
import utils.distance.DoublePointProgram;

public class CoOCL_Batch2 {
	static Logger LOGGER = Logger.getLogger(CoOCL_Batch2.class.getName());
	static Logger LOGGER_batch = Logger.getLogger("LOGGER_batch");

	public static void main(String[] args) {
		Utils.initInSilence(args[0]);
		
		
		
		int NB_CLUSTERS_LO = 3;
		int NB_CLUSTERS_UP = 15;

		
		
		// int NUMBER_OF_EXECUTIONS = 1;
		String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.FRANCE).format(new Date());

//		String dir_mainName = Config.DIR_RESULTS + File.separator + Config.METAMODEL_NAME + File.separator + timeStamp ;
//		int itmp = 0;
//		while (new File(dir_mainName).exists()) {
//			dir_mainName = dir_mainName+"-"+ ++itmp;
//    	}
//		File dir_main = new File(dir_mainName);
//		if (!dir_main.exists())
//			dir_main.mkdir();
		
//		String fileName = dir_main + File.separator + "batch_"+ (Config.SINBAD ? "sinbad_" : "") + timeStamp + ".log";
//		String fileNameSetting =  dir_main + File.separator + "batch_"+ (Config.SINBAD ? "sinbad_" : "") + timeStamp + "_setting"+".log";
		
		File dir_main = new File(Config.DIR_RESULTS + File.separator + Config.METAMODEL_NAME + File.separator);
		if (!dir_main.exists())
			dir_main.mkdir();

		String base = dir_main.getAbsolutePath() + File.separator + "batch_" + (Config.SINBAD ? "sinbad_" : "") + timeStamp ;
		String fileName = base + ".log";
		String fileNameSetting =  base + "_setting"+".log";
		int itmp = 1;
		while (new File(fileName).exists() 
				|| new File(fileNameSetting).exists()) {
    		fileName = base+"-"+itmp+".log";
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
			String header = "foundInPareto; #rulesFoundInP ; minToGTInP ; avgToGTInP";
			for (int i = NB_CLUSTERS_LO; i <= NB_CLUSTERS_UP; i++) {
				header += " ; #"+i+"clusters ; foundIn"+i+"Centroids ; #rulesFoundIn"+i+"C ; minToGTIn"+i+"C ; avgToGTIn"+i+"C";
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
				
//				String runDirName = dir_main.getAbsolutePath() + File.separator + "run_"+i + File.separator  ;
//				File dir_run = new File(runDirName);
//				if (!dir_run.exists())
//					dir_run.mkdir();
				
				

				long t = System.currentTimeMillis();
				Program prg0 = Program.getInitialProgram();
				Program prgGroundTruth = Program.getExpectedSolution();
				
//				System.out.println(prg0.getOCL_standaloneExecutable());
//				System.out.println(prgGroundTruth.getOCL_standaloneExecutable());
				
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
//	
//				eva.evaluate(prgGroundTruth);
//				
//				boolean inParetoB = Evolutioner.checkPresenceOfGroundTruthInPopulation(popN, Program.getExpectedSolution()) != null;
//				if(inParetoB) inPareto++;
//				
//				double dist = Double.MAX_VALUE, distNSE = Double.MAX_VALUE;
//				Program closest = null, closestCentres = null, closestNoSyntaxErrors = null;;
//				for (GeneticEntity ge : popN.getFrontPareto().getEntities()) {
//					Program p = (Program) ge;
//					
//					/*
//					 * Store program in dir_run/prgName.ocl
//					 */
//					String filePrgName = dir_run.getAbsolutePath()+ File.separator + p.getName() + ".ocl";
//					File fProgram = new File(filePrgName);
//					fProgram.createNewFile();
//					BufferedWriter bwPrg = new BufferedWriter(new FileWriter(fProgram));
//					bwPrg.write(p.getOCL_standaloneExecutable());
//					bwPrg.close();
//					
//					
//					double distPtoGT = p.getDistance(prgGroundTruth);
//					if(distPtoGT < dist){
//						dist = distPtoGT;
//						closest = p;
//					}
//					if(distPtoGT < distNSE && closestNoSyntaxErrors != null && closestNoSyntaxErrors.getFitnessVector().getValue(FitnessVector.OBJECTIVE.NUMBER_OF_SYNTAX_ERRORS.getIdx()) == 0){
//						distNSE = distPtoGT;
//						closestNoSyntaxErrors = p;
//					}
//				}
//				
//				LOGGER_batch.info("Run "+i+"\n"
//						+ "Files: "+dir_run.listFiles().length+" files in "+dir_run.getAbsolutePath()+"\n");
//			
//				
//				List<CentroidCluster<DoublePointProgram>> clustersKM = Evolutioner.clusterFrontParetoFromPopulation(popN);
//				// On recupere les centres des clusters
//				String textSolution = " ** Execution "+i+" **\n";
//				int j = 0;
//				dist = Double.MAX_VALUE;
//				for (CentroidCluster<DoublePointProgram> clusterKM : clustersKM) {
//					// For each cluster check if solution and compute distance to
//					// GroundTruth
//					DoublePointProgram center = Evolutioner.extractCenterFromCluster(clusterKM);
//					if(center.getProgram().getDistance(prgGroundTruth) < dist){
//						dist = center.getProgram().getDistance(prgGroundTruth);
//						closestCentres = center.getProgram();
//						textSolution += "** ";
//					}
//					textSolution += "Cluster "+ j++ +"  "+center.getProgram() +" : "+Arrays.toString(center.getProgram().computeDamerauLevensteinDistances(prgGroundTruth))+")\n";
//					//					+ "\n"+center.getProgram().printExecutableOCL()
//					//					+ ""+center.getProgram().printSyntaxErrors("")+"\n";
//					
//					if(center.equals(Program.getExpectedSolution()))
//						foundInCenters ++;
//				}
//				
//				textSolution += "Closest in pareto: "+closest+" " +Arrays.toString(closest.computeDamerauLevensteinDistances(prgGroundTruth))+")"+"\n"
//						+ closest.printExecutableOCL()
//						+ closest.printSyntaxErrors("")+"\n";
//				if(closestNoSyntaxErrors != null)
//					textSolution += "Closest in pareto (No syntax errors): "+closestNoSyntaxErrors+" " +Arrays.toString(closestNoSyntaxErrors.computeDamerauLevensteinDistances(prgGroundTruth))+")"+"\n"
//							+ closestNoSyntaxErrors.printExecutableOCL()
//							+ closestNoSyntaxErrors.printSyntaxErrors("")+"\n";
//				else
//					textSolution += "No solution found clear of syntax error.\n";
//				
//					textSolution += "Closest in centers: "+closestCentres+" "+Arrays.toString(closestCentres.computeDamerauLevensteinDistances(prgGroundTruth))+")"+"\n"
//							+ closestCentres.printExecutableOCL()
//							+ closestCentres.printSyntaxErrors("")+"\n";
//					
//				
//				LOGGER_batch.config(inParetoB ? "Ground truth in pareto front":"Ground truth NOT in pareto front");
//				LOGGER_batch.config(i+": From:        \n" + prg0.printExecutableOCL());
//				LOGGER_batch.config(i+": Expected is: \n" + prgGroundTruth.printExecutableOCL());
////				LOGGER_batch.config(i+": Closest is: "+Arrays.toString(closest.computeDamerauLevensteinDistances(prgGroundTruth))+")\n" + closest.printExecutableOCL());
////				LOGGER_batch.config(i+": Closest recommended is: "+Arrays.toString(closestCentres.computeDamerauLevensteinDistances(prgGroundTruth))+")\n" + closestCentres.printExecutableOCL());
//				
//				String textLine = popN.printStatisticsLine() + ";"+Utils.formatMillis(System.currentTimeMillis() - t)+";"+Arrays.toString(closest.computeDamerauLevensteinDistances(prgGroundTruth));
//				LOGGER_batch.info(i+": " + textLine);
//				LOGGER_batch.fine("Solutions:"+textSolution);
//	
//				bw.append(textLine+"\n");
//				bw.flush();
//				bwSettings.append("\n"+textSolution);
//				bwSettings.flush();
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
	
	
	
	public static String printStatisticsOfPopulation(Population popN, int nbClustersMin, int nbClustersMax, BufferedWriter out) {
		int ERR_MARGIN = 2;
		String textSolution = "";
		
		Program prg0 = Program.getInitialProgram();
		Program prgGroundTruth = Program.getExpectedSolution();
			
			boolean inParetoB = Evolutioner.checkPresenceOfGroundTruthInPopulation(popN, Program.getExpectedSolution()) != null;
			
			double minDist = Double.MAX_VALUE, distNSE = Double.MAX_VALUE, avgDist = 0.0;
			Program closest = null, closestCentres = null, closestNoSyntaxErrors = null;;
			
			boolean[] found = new boolean[Program.getNumberOfContraintes()];
			for (GeneticEntity ge : popN.getFrontPareto().getEntities()) {
				Program p = (Program) ge;
				double distPtoGT = p.getDistance(prgGroundTruth);
				if(distPtoGT < minDist){
					minDist = distPtoGT;
					closest = p;
				}
				if(distPtoGT < distNSE && closestNoSyntaxErrors != null && closestNoSyntaxErrors.getFitnessVector().getValue(FitnessVector.OBJECTIVE.NUMBER_OF_SYNTAX_ERRORS.getIdx()) == 0){
					distNSE = distPtoGT;
					closestNoSyntaxErrors = p;
				}
				avgDist += distPtoGT;
				
				for (int i = 0; i < found.length; i++) {
					if (!found[i] && p.computeDamerauLevensteinDistances(Program.getExpectedSolution())[i] == 0.0)
						found[i] = true;
				}
			}
			
			int numberOfConstraintsFound = 0;
			for (double dist2 : closest.computeDamerauLevensteinDistances(Program.getExpectedSolution())) {
				if(dist2 <= ERR_MARGIN)
					numberOfConstraintsFound++;
			}
			
			int numberOfConstraintsFound_combined = 0;
			for (boolean b: found) {
				if(b)
					numberOfConstraintsFound_combined++;
			}
			
			String line = (numberOfConstraintsFound==Program.getNumberOfContraintes() ? 1 : 0) + //inParetoB
					";" + numberOfConstraintsFound +
					";" + numberOfConstraintsFound_combined 
//					+ ";" + Arrays.toString(closest.computeDamerauLevensteinDistances(prgGroundTruth))
					;;
			
			textSolution += "Closest in pareto: " + closest + " " + Arrays.toString(closest.computeDamerauLevensteinDistances(prgGroundTruth)) + ")" + "\n"
				+ closest.printExecutableOCL() + closest.printSyntaxErrors("") + "\n";
		
		
			for (int j = nbClustersMin; j <= nbClustersMax; j++) {
				boolean goOn = true;
				List<CentroidCluster<DoublePointProgram>> clustersKM = null;;
				try {
					clustersKM = Evolutioner.clusterFrontParetoFromPopulation(popN, j);
				} catch (Exception e) {
					System.out.println("Error sur Execution : "+j+" clusters avec pop de "+popN.size()+" entities et pareto : "+popN.getFrontPareto().size());
					e.printStackTrace();
					goOn = false;
				}
				// On recupere les centres des clusters
				textSolution += "\n\n -- " + j + " clusters:\n";
				if(!goOn){
					textSolution += "Error sur Execution : "+j+" clusters avec pop de "+popN.size()+" entities et pareto : "+popN.getFrontPareto().size()+"\n";
					line += ";" + j + 
							";ERR" + 
							";ERR" + 
							";ERR" ;
				} else {
					int k = 0;
					double minDistC = Double.MAX_VALUE, avgDistC = 0.0;
					boolean foundInCenters = false;
					boolean[] foundC = new boolean[Program.getNumberOfContraintes()];
					int maxNumberOfConstraintsFound = 0;
				for (CentroidCluster<DoublePointProgram> clusterKM : clustersKM) {
					// For each cluster check if solution and compute distance
					// to GroundTruth
					DoublePointProgram center = Evolutioner.extractCenterFromCluster(clusterKM);
					double distPtoGT = center.getProgram().getDistance(prgGroundTruth);
					if (distPtoGT < minDistC) {
						minDistC = distPtoGT;
						closestCentres = center.getProgram();
					}
					textSolution += "   - Cluster " + k++ + "  " + center.getProgram() + " : " + Arrays.toString(center.getProgram().computeDamerauLevensteinDistances(prgGroundTruth)) + ")\n";
					if (center.equals(Program.getExpectedSolution()))
						foundInCenters = true;
					avgDistC += minDistC;

					for (int i = 0; i < foundC.length; i++) {
						if (!foundC[i] && center.getProgram().computeDamerauLevensteinDistances(Program.getExpectedSolution())[i] <= ERR_MARGIN)
							foundC[i] = true;
					}
					int nbConstraintsFound = 0;
					for (int i = 0; i < foundC.length; i++) {
						if (center.getProgram().computeDamerauLevensteinDistances(Program.getExpectedSolution())[i] <= ERR_MARGIN)
							nbConstraintsFound++;
					}

					maxNumberOfConstraintsFound = Math.max(maxNumberOfConstraintsFound, nbConstraintsFound);
				}
						
//						int numberOfConstraintsFoundInCenters = 0;
//						for (double dist2 : closestCentres.computeDamerauLevensteinDistances(Program.getExpectedSolution())) {
//							if (dist2 == 0.0)
//								numberOfConstraintsFoundInCenters++;
//						}
		
				int numberOfConstraintsFound_combinedC = 0;
				for (boolean b : found) {
					if (b)
						numberOfConstraintsFound_combinedC++;
				}
				line += ";" + j + 
						";" + (maxNumberOfConstraintsFound==Program.getNumberOfContraintes() ? 1 : 0) + 
						";" + maxNumberOfConstraintsFound + 
						";" + numberOfConstraintsFound_combinedC  
//						+ ";" +Arrays.toString(closestCentres.computeDamerauLevensteinDistances(prgGroundTruth))
						;

				textSolution += "   - Closest in " + j + "centers: " + closestCentres + " " + Arrays.toString(closestCentres.computeDamerauLevensteinDistances(prgGroundTruth)) + ")" + "\n"
						+ closestCentres.printExecutableOCL() + closestCentres.printSyntaxErrors("") + "\n";

			}
			}
//			if(out != null)
				try {
					out.write("" + line+"\nSolutions:" +textSolution);
				} catch (IOException e) { e.printStackTrace();	}
			LOGGER_batch.config("" + line);
			LOGGER_batch.finer("Solutions:" + textSolution);
		return line;
	}

}

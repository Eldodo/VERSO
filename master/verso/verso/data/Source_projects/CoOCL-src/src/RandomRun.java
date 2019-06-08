import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import coocl.ocl.Program;
import oclruler.genetics.Evaluator;
import oclruler.genetics.Evolutioner;
import oclruler.genetics.FitnessVector;
import oclruler.genetics.GeneticEntity;
import oclruler.genetics.Population;
import utils.Config;
import utils.Utils;

public class RandomRun {

	static Logger LOGGER = Logger.getLogger(RandomRun.class.getName());
	static Logger LOGGER_batch = Logger.getLogger("LOGGER_batch");

	public static void main(String[] args) {
		Utils.initInSilence(args[0]);

		String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.FRANCE).format(new Date());

		File dir = new File(Config.DIR_RESULTS + File.separator + Config.METAMODEL_NAME + File.separator);
		if (!dir.exists())
			dir.mkdir();

		String base = dir.getAbsolutePath() + File.separator + "random_" + (Config.SINBAD ? "sinbad_" : "") + timeStamp;
		String fileName = base + ".log";
		String fileNameSetting = base + "_setting" + ".log";
		int itmp = 1;
		while (new File(fileName).exists() || new File(fileNameSetting).exists()) {
			fileName = base + "-" + itmp + ".log";
			fileNameSetting = base + "-" + itmp + "_setting.log";
			itmp++;
		}

		LOGGER_batch.info(Config.NUMBER_OF_EXECUTIONS + " executions.");

		long start = System.currentTimeMillis();
		BufferedWriter bw, bwSettings;
		try {
			File f = new File(fileName);
			f.createNewFile();
			File fSetting = new File(fileNameSetting);

			bw = new BufferedWriter(new FileWriter(f));
			bw.write("foundInPareto; #rulesFound; #rulesCombined\n");

			bwSettings = new BufferedWriter(new FileWriter(fSetting));
			bwSettings.write("Run " + timeStamp + "\n\n ** SETTING **\n\n" + Config.printSetting(""));
			bwSettings.write("\n\nExpected:\n" + Program.getExpectedSolution().printExecutableOCL());
			bwSettings.flush();

			String text = "";
			String textSolutions = "";

			int inPareto = 0;
			for (int i = 0; i < Config.NUMBER_OF_EXECUTIONS; i++) {

				Utils.init(args[0]);
				Evaluator eva = new Evaluator();
				Population popN = Population.createRandomPopulation_forSanityCheck(
						Program.getInitialProgram(),
						Population.POPULATION_SIZE * (Evolutioner.GENERATION_MAX+1) / 2 );

				popN.evaluate(eva);

				long time_t = System.currentTimeMillis();
				popN.fastNonDominantSort2();
				popN.crowdingDistanceAssignement();

				LOGGER_batch.info("Sorting done in " + Utils.formatMillis(System.currentTimeMillis() - time_t));
				LOGGER_batch.info("Pareto size = " + popN.getFrontPareto().size());

				Program best = ((Program) popN.getBest());

				Program prg0 = Program.getInitialProgram();
				Program prgGroundTruth = Program.getExpectedSolution();

				boolean inParetoB = Evolutioner.checkPresenceOfGroundTruthInPopulation(popN, Program.getExpectedSolution()) != null;
				if (inParetoB)
					inPareto++;

				double dist = Double.MAX_VALUE, distNSE = Double.MAX_VALUE;
				Program closest = null, closestNoSyntaxErrors = null;
				;
				for (GeneticEntity ge : popN.getFrontPareto().getEntities()) {
					Program p = (Program) ge;
					double distPtoGT = p.getDistance(prgGroundTruth);
					if (distPtoGT < dist) {
						dist = distPtoGT;
						closest = p;
					}
					if (distPtoGT < distNSE && closestNoSyntaxErrors != null && closestNoSyntaxErrors.getFitnessVector().getValue(FitnessVector.OBJECTIVE.NUMBER_OF_SYNTAX_ERRORS.getIdx()) == 0) {
						distNSE = distPtoGT;
						closestNoSyntaxErrors = p;
					}
				}

				String textSolution = "Closest in pareto: " + closest + " " + Arrays.toString(closest.computeDamerauLevensteinDistances(prgGroundTruth)) + ")" + "\n" + closest.printExecutableOCL()
						+ closest.printSyntaxErrors("") + "\n";
				if (closestNoSyntaxErrors != null)
					textSolution += "Closest in pareto (No syntax errors): " + closestNoSyntaxErrors + " " + Arrays.toString(closestNoSyntaxErrors.computeDamerauLevensteinDistances(prgGroundTruth))
							+ ")" + "\n" + closestNoSyntaxErrors.printExecutableOCL() + closestNoSyntaxErrors.printSyntaxErrors("") + "\n";
				else
					textSolution += "No solution found clear of syntax error.\n";

				LOGGER_batch.config(inParetoB ? "Ground truth in pareto front" : "Ground truth NOT in pareto front");
				LOGGER_batch.config(" From:        \n" + prg0.prettyPrint("  "));
				LOGGER_batch.config(" Expected is: \n" + prgGroundTruth.prettyPrint("  "));
				LOGGER_batch.config(
						" Closest is: " + "(Levenstein distance to ground truth: " + Arrays.toString(closest.computeDamerauLevensteinDistances(prgGroundTruth)) + ")\n" + closest.prettyPrint("  "));
				LOGGER_batch
						.config(" Best is:     " + "(Levenstein distance to ground truth: " + Arrays.toString(best.computeDamerauLevensteinDistances(prgGroundTruth)) + ")\n" + best.prettyPrint("  "));
				LOGGER_batch.finer(" " + best.printModifications_left());
				LOGGER_batch.finer(" " + best.printPastMutations());

				String textLine = ((Evolutioner.checkPresenceOfGroundTruthInPopulation(popN, Program.getExpectedSolution()) != null) ? 1 : 0) + ";";
				double minDist = Double.MAX_VALUE, avgDist = 0.0;
				Program closest0 = null;
				boolean[] found = new boolean[Program.getNumberOfContraintes()];
				for (GeneticEntity ge : popN.getFrontPareto().getEntities()) {
					Program p = (Program) ge;
					double dist0 = p.getDistance(Program.getExpectedSolution());
					if( dist < minDist){
						minDist = p.getDistance(Program.getExpectedSolution());
						closest0 = p;
					}
					avgDist += dist0;
					
					for (int j = 0; j < found.length; j++) {
						if (!found[j] && p.computeDamerauLevensteinDistances(Program.getExpectedSolution())[j] == 0.0)
							found[j] = true;
					}

				}
				int numberOfConstraintsFound = 0;
				for (double dist0 : closest0.computeDamerauLevensteinDistances(Program.getExpectedSolution())) {
					if(dist0 == 0.0)
						numberOfConstraintsFound++;
				}
				
				int numberOfConstraintsFound_combined = 0;
				for (boolean b: found) {
					if(b)
						numberOfConstraintsFound_combined++;
				}

				//mininum and average distance in pareto (to ground truth)
				textLine += numberOfConstraintsFound + ";"+numberOfConstraintsFound_combined
//						+";"+minDist + ";" +  String.format ("%.03f",avgDist/popN.getFrontPareto().size())+";"
//						+ ";" + Utils.formatMillis(System.currentTimeMillis() - time_t) + ";"
//						+ Arrays.toString(closest.computeDamerauLevensteinDistances(prgGroundTruth))
						;

				LOGGER_batch.info(" " + textLine);
				LOGGER_batch.fine("Solutions:" + textSolution);
				LOGGER_batch.finer(best.printModifications_left());
				LOGGER_batch.finer(best.printPastMutations());

				text += textLine + "\n";
				bw.append(textLine + "\n");
				bw.flush();
				bwSettings.append("\n" + textSolution);
				bwSettings.flush();
				textSolutions += "\n" + textSolution;

			}
			bw.close();

			String log = "Results written in: " + f.getAbsolutePath() + " (Setting in: " + fSetting.getAbsolutePath() + ")" + "\n Found :" + "\n  - in pareto:  " + inPareto + "\nDuration: "
					+ Utils.formatMillis(System.currentTimeMillis() - start);
			LOGGER_batch.info(log);

			bwSettings.append("\n\n" + log);
			bwSettings.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Exit - sound and safe.");
	}
}

package oclruler.genetics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import coocl.ocl.Contrainte;
import coocl.ocl.Program;
import oclruler.genetics.Population.Pareto;
import oclruler.ui.Ui;
import utils.Config;
import utils.Utils;
import utils.distance.DoublePointProgram;

public class Evolutioner {


	public static Logger LOGGER = Logger.getLogger(Evolutioner.class.getName());
	
	
	/** Number of generation to wait with same evaluation (average of front Pareto entities) before a new example is added (pos or neg). <br/>To be specified in config file. */
	public static int 	 			STATIONARY_TIME = 10; 
	public static float 			STATIONARY_VARIANCE = 0.001f; 
	public static StationaryType 	STATIONARY_TYPE = StationaryType.BEST;

	public static boolean 	ROULETTE_GEOMETRIC = false;
	public static int 		GENERATION_MAX = 1;
	public static int 		CHECK_POINT_GENERATION = 100;
	public static int 		CHECK_RESULT_IN_UI = 5;
	public static long 		MAX_TIME = 5 * 60 * 1000;//milliseconds
	public static boolean 	STEP_BY_STEP = false;
	public static int 		GENERATIONS_STEP = 0;

	public static String 	END_CONDITION_TYPE = "and";
	public static float[] 	OBJECTIVES_END_CONDITION ;

	public static int NUMBER_OF_CLUSTERS = 5;

	Evaluator evaluator;

	//Evolution state variables
	Population currentPopulation;
	public int generation = 0;
	long startTime, timeSpent;

	String timeStamp = "";
	private float avgGenes;

	//Log files
	//The blah_blah_blah.log writer
	File resultsFile = null;
	String fileOutName;

	//The blah_blah_blah.data.log writer
//	File resultsFileData = null;
//	BufferedWriter bw, bwData;
//	String fileOutDataName;
	
	
	public Evolutioner(Evaluator eva, Population p) {
		this.evaluator = eva;
		eva.setEvo(this);
		currentPopulation = p;
		
    	timeStamp = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.FRANCE).format( new Date() );
    	startTime = System.currentTimeMillis();
    	
//    	String fileNames[] = createFileOutNames();
//       	fileOutName = fileNames[0];
//    	fileOutDataName = fileNames[1];
//    	File[] bws = buildFiles(fileOutName, fileOutDataName);
//		resultsFile = bws[0];
//		try {
//			bw = new BufferedWriter(new FileWriter(resultsFile));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		resultsFileData = bws[1];
//		try {
//			bwData = new BufferedWriter(new FileWriter(resultsFileData));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	

	public Population evolutionate() {
		if(currentPopulation == null){
			LOGGER.warning("No current population.");
			return null;
		}
		return evolutionate(currentPopulation);
	}
	   

	public Population evolutionate(Population pop)  {
		LOGGER.config("Evaluator is " + evaluator.getClass().getSimpleName());
		
		currentPopulation = pop;
		if (Config.VERBOSE_ON_UI && !Config.SINBAD)
			Ui.showUi();

//		LOGGER.config("Evaluation of initial population...");
		long t = System.currentTimeMillis();
		currentPopulation.evaluate(evaluator);
		LOGGER.config("Evaluation of initial population executed in " + Utils.formatMillis(System.currentTimeMillis() - t) + "s");
		LOGGER.finer(currentPopulation.printStatistics(true));

		LOGGER.config("G_X: max/avgs = (" + FitnessVector.csvHeader + ")");

		long startTime = System.currentTimeMillis();
		int totalMutations = 0;
		avgGenes = 0;
		float distanceAvg = 0;
		float[] avgs = new float[FitnessVector.NUMBER_OF_OBJECTIVES];
		double[] firsts = new double[FitnessVector.NUMBER_OF_OBJECTIVES];

		long cumul_time_genetic_op = 0, cumul_time_nonDominant_sort = 0;
		int nextStep = generation + GENERATIONS_STEP;
		
		
		// TODO loop's start
		while (++generation <= GENERATION_MAX && !endCondition(STATIONARY_TYPE.isBest()?currentPopulation.getBest().getFitnessVector().getAllValues():avgs)) {
			Population G0 = currentPopulation;
			long startTimeGeneration = System.currentTimeMillis();
			int numberOfMutation_thisGeneration = 0;

			numberOfMutation_thisGeneration = geneticReproduction(G0, numberOfMutation_thisGeneration);
			totalMutations += numberOfMutation_thisGeneration;

			cumul_time_genetic_op += System.currentTimeMillis() - startTimeGeneration;
			
			long time_t = System.currentTimeMillis();
			G0.fastNonDominantSort2();
			G0.crowdingDistanceAssignement();

			cumul_time_nonDominant_sort += System.currentTimeMillis() - time_t;

			//Cutting population in half into G1.
			G0 = G0.cutPopulationInHalf();

	 		currentPopulation = G0;
	 		System.gc();		 		
//	 		currentPopulation.fastNonDominantSort2();
//	 		currentPopulation.crowdingDistanceAssignement();
	 			 		

	 		long endTime = System.currentTimeMillis();

	 		int nbEntities = currentPopulation.getFrontPareto().size();
 			float[] sums = new float[FitnessVector.NUMBER_OF_OBJECTIVES];
		 		avgs = new 	float[FitnessVector.NUMBER_OF_OBJECTIVES];
			 	firsts = new double[FitnessVector.NUMBER_OF_OBJECTIVES];
			 GeneticEntity maxE = null;
 			int 	sumGenes = 0;
//	 			int 	sumGenesSize = 0;//What is the size of a rule ? Does it mean anything ?
 					distanceAvg = 0;
 			int 	nbDist = 0;//Used to remove the boundaries (2*nb_objectives in fitFunc)
// 			String logg = "";
 			for (int i = 0; i < sums.length; i++) {
 				sums[i] = 0;
 				avgs[i] = 0;
 				firsts[i] = 0;
 				for (GeneticEntity e : currentPopulation.getFrontPareto().getEntities()) {
 					sumGenes += e.size();
 					double v = e.getFitnessVector().getValue(i);
 					if(v > firsts[i]) 
 						firsts[i] = v;
 					sums[i] += v;
 					if( e.getDistance() < (Integer.MAX_VALUE-1000) ){
 						nbDist++;
 						distanceAvg += e.getDistance();
 					}
 				}
 			}
 			
 			
 			for (int i = 0; i < sums.length; i++)
 				avgs[i] = sums[i] / nbEntities;

 			maxE = currentPopulation.getBest();
 			avgGenes = sumGenes / (float)nbEntities;
 			distanceAvg = distanceAvg / nbDist;

 			
 				/*
		 		 * LOG, FILE and UI
		 		 */
		   	//Each generation LOGS
 			if(LOGGER.isLoggable(Level.CONFIG)){//TODO LOGS
	 			String l = generationLog(avgs, startTimeGeneration, numberOfMutation_thisGeneration, endTime, maxE);
	 			if(LOGGER.getLevel().equals(Level.CONFIG))//Else if fine, print in method "generationLog"
	 				LOGGER.config(l);
	 			if(Config.VERBOSE_ON_UI && !Config.SINBAD)
	 				Ui.getInstance().log(l);
 			}
 			
 			//Generation check point LOGS
	 		if(generation % CHECK_POINT_GENERATION == 0 && generation != GENERATION_MAX){
	 			String log =
		   		"\nEvolution ckeck point :"
		    			+"\n  "+generation+" generations "
		    			+"\n  Total #mutations :  "+totalMutations
//			    			+"\n  Total Jess calls :  "+JessExecutor.counter
		    			+"\n"+currentPopulation.printStatistics() //Here we call the sorting:paretos+distance crowding
		    			+"\nBest:"+currentPopulation.getBest().prettyPrint() 
		    			+"Time elapsed : "+Utils.formatMillis(System.currentTimeMillis()-startTime) +" (genetics="+Utils.formatMillis(cumul_time_genetic_op)+", sorting="+Utils.formatMillis(cumul_time_nonDominant_sort)+")";
	 			log += "\n";
	 			LOGGER.config(log);
	 			if(Config.VERBOSE_ON_UI && !Config.SINBAD){
	 				Ui.getInstance().log(log);
	 			}
	 		}
	 		if(!Config.SINBAD && (generation % CHECK_RESULT_IN_UI == 0 || generation == 1) )
	 			Ui.getInstance().setResultingPop(currentPopulation);
 			//end LOGS

 			//FILE
//	 		if(Config.VERBOSE_ON_FILE)
// 				logG_OnFile(bwData, generation, System.currentTimeMillis()-startTime, maxE, avgGenes);
 			//end FILE
 			//UI
	 		if(!Config.SINBAD)
 				logG_OnUi(generation, currentPopulation, maxE, avgs, 0.0f, avgGenes);//4th parameter (0.0f) not used.
	 		//end UI

	   		/* * END LOGS n UI * */
 				
 				
 			//STEP CONTROL 
 				if(STEP_BY_STEP && (generation == nextStep))
 					nextStep = setpControl(startTime, nextStep);
			//end STEP CONTROL

    	}// End generation loop
    	timeSpent  = System.currentTimeMillis() - startTime;
    		    	
    	String log = "\nEvolution ends."
    			+"\n  Evaluator is "+evaluator.getClass().getSimpleName()
    			+"\n  "+generation+" generations"
    			+"\n  Total #mutations :  "+totalMutations
//    			+"\n"+currentPopulation.printStatistics(true)
    			+(Config.VERBOSE_ON_FILE?"\n  Wrote into "+fileOutName:"")
    			+"\n  Time elapsed : "+Utils.formatMillis(System.currentTimeMillis()-startTime);

    	LOGGER.info(log);
    	if(Config.VERBOSE_ON_UI){
    		Ui.getInstance().log(log);
    		Ui.getInstance().setResultingPop(currentPopulation);
    	}

    	if(Config.VERBOSE_ON_FILE && resultsFile != null){
    		printResult(resultsFile);
    	}
//		if(bwData != null) 
//    		try {
//    			bwData.close();
//    		} catch (IOException e1) {
//    			e1.printStackTrace();
//    		}
		return currentPopulation;
	}


	public int geneticReproduction(Population G0, int mutationGeneration) {
		GeneticEntity[] sons2;
		ArrayList<GeneticEntity> fathersRouletteExtended = G0.getRouletteExtendedEntities();
		int rates = 0;
		float avgsSize = 0;
		int nbEnt = 0;
		while (G0.getEntities().size() < Population.POPULATION_SIZE * 2) {
			try {
				sons2 = crossover(fathersRouletteExtended);
			} catch (UnstableStateException e2) {
				// TODO Crossover failed
				sons2 = null;
				e2.printStackTrace();
			}
			if(sons2 != null){
				for (GeneticEntity e : sons2) {
					
					evaluator.evaluate(e);
					int maxi = 0;
					while (maxi++ < 10 && G0.getEntities().contains(e)) {//See Program.equals(Program)
						e = e.clone();
						try {
							if(e.mutate())
								mutationGeneration++;
						} catch (UnstableStateException e1) {
							//TODO Mutation failed
							LOGGER.severe("Mutation threw exception.");
							e1.printStackTrace();
						}
					}
					
					if (e.isModified())
						evaluator.evaluate(e);
					if (!e.isEmpty() && !G0.getEntities().contains(e)) {
						int mutate = 0;
						int i = 0;
						while(mutate == 0 && i++ < 10)
							mutate += mutation(e) ? 1 : 0;
						mutationGeneration += mutate; // MOVED down to avoid useless evaluations [N->N/2] 
																	// no need to mutate the one in the second half
																	// half.
						G0.addEntity(e);
						nbEnt++;
						avgsSize += e.size();
					} else {
						rates++;
						rates = isReproductionMoldingWind(G0, rates);
					}
				}
			}
		}
		LOGGER.finer("Reproduction rates :" + rates);
		return mutationGeneration;
	}


	private String generationLog(float[] avgs, long startTimeGeneration, int mutationGeneration, long endTime, GeneticEntity maxE) {
		String log2 = "G_"+generation+": ";
		log2 += /*"max=("+maxE.printNumericResult(",", "", "", true ) + ")*/ "avgs=(";
		
		String log3 ="";
		for (float f : avgs) 
			log3 += " "+ String.format ("%.02f",f);
		log2 += log3.trim()+ ") |";
		
		log2 = Utils.completeString(log2, 29, false);
		
		double time = (endTime-startTimeGeneration);
		time = time /1000;
		String log = currentPopulation.getParetos().size()+"[";
		for (int i = 0; i < currentPopulation.getParetos().size(); i++) {
			Pareto p = currentPopulation.getPareto(i);
			log += (p.getEntities().size() + " ");
		}
		log2 += " " + log.trim() + "]";
		String ratioMutations = String.format ("%.02f",((float)Contrainte.totalNumberOfChanges()/Contrainte.numberOfAllMutationTries));
		log =  log2+ " (in "+String.format ("%.02f",(time))+"s) "  + Contrainte.totalNumberOfChanges()+"/"+Contrainte.numberOfAllMutationTries+" mutations ("+ratioMutations+")";
				
		LOGGER.fine(log);
		
		
		return log;
	}


	private int setpControl(long startTime, int nextStep) {
		System.out.print("G_"+generation+" ended. Duration total : "+Utils.formatMillis(System.currentTimeMillis()-startTime)+
				"\n   ***   STEP_BY_STEP mode activated ("+GENERATIONS_STEP+" generation"+(GENERATIONS_STEP>1?"s":"")+")  ***"
			  + "\n   * Next step : <#generation> or \"Go!\"");
		@SuppressWarnings("resource")
		String sbs = new Scanner(System.in).nextLine();
		
		int goSteps = -1;
		try{
			goSteps = Integer.parseInt(sbs);
		} catch (NumberFormatException nfe){
			//Who cares
		}
		
		if (goSteps > 0 ) {
			GENERATIONS_STEP = goSteps;
			System.out.println("Continuing for "+GENERATIONS_STEP+" generation"+(GENERATIONS_STEP>1?"s":"")+"...");
		} else {
			if(!sbs.equalsIgnoreCase("GO!")){
				System.out.println("Continuing...");
			} else if(sbs.equalsIgnoreCase("GO!") || goSteps <= 0) {
				STEP_BY_STEP = false;
				System.out.println("STEP_BY_STEP mode deactivated.\nContinuing until the end !");
			}   
		}	
		nextStep += GENERATIONS_STEP;
		return nextStep;
	}


	/**
	 * Prompt "Genetic reproduction is [definitely] molding wind
	 * @param G0
	 * @param moldingLoops_number
	 * @return
	 */
	public int isReproductionMoldingWind(Population G0, int moldingLoops_number) {
		LOGGER.finer("--> Loops: "+moldingLoops_number);
		if(moldingLoops_number%100 == 0 && !(moldingLoops_number%300 == 0) ) LOGGER.warning("Genetic reproduction is molding wind : "+moldingLoops_number+" loops for nothing !");
		if(moldingLoops_number%300 == 0) {
			 LOGGER.warning("Genetic reproduction is definitely molding wind : "+moldingLoops_number+" loops for nothing !");
			 LOGGER.warning("Generation "+generation+" results :\n"+G0.printStatistics());
//			 LOGGER.warning("Continue ?!");
			 
			@SuppressWarnings("resource")
			String scanEntry = " FORCE CONTNUE ";//new Scanner(System.in).nextLine();
			
			Program p = Program.getInitialProgram().clone();
			evaluator.evaluate(p);
			G0.addEntity(p);
			LOGGER.info("Runing slow !!!! \n\n GENETIC REPRODUCTION IS MOLDING WIND, 'program0' reinjected");
			scanEntry = " FORCE CONTNUE ";
			
			
			int numberOfProgramsToInject = 1;
			boolean numberOfGenerationForNextStep_stated = false;
			try {
				numberOfProgramsToInject = Integer.decode(scanEntry);
				numberOfGenerationForNextStep_stated = true;
			} catch (NumberFormatException e1) {
				numberOfProgramsToInject = 1;
			}
			
			
			if(numberOfGenerationForNextStep_stated || scanEntry.toLowerCase().equals("yes")||scanEntry.toLowerCase().equals("y")||scanEntry.toLowerCase().equals("o")||scanEntry.toLowerCase().equals("oui")){
				moldingLoops_number = 0;
				if(!numberOfGenerationForNextStep_stated){
					System.out.println("How many new program to inject ?");
					scanEntry = new Scanner(System.in).nextLine();
					try {
						numberOfProgramsToInject = Integer.decode(scanEntry);
					} catch (NumberFormatException e1) {
						System.out.println("Unreadable : 1 program injected.");
						numberOfProgramsToInject = 1;
					}
				}
				System.out.println("C'est reparti avec "+numberOfProgramsToInject+" nouveau"+((numberOfProgramsToInject==1)?"":"x")+" random Program"+((numberOfProgramsToInject==1)?"":"s")+"...");
				for (int i = 0; i < numberOfProgramsToInject; i++) {
					//TODO Mutate ?
				}
				forceEvaluation(G0);
				
			} else {
				
//				System.exit(1);
				moldingLoops_number = 0;
			}
			
		}
		return moldingLoops_number;
	}



	private boolean mutation(GeneticEntity e) {
//		System.out.println("Evolutioner.mutation("+e.getName()+")");
		boolean res = false;
		if ( Utils.getRandomDouble() < Population.MUTATE_RATE) {
			try {
				res = e.mutate();
			} catch (UnstableStateException e1) {
				//TODO  Mutation failed
				LOGGER.severe("Mutation failed.");
				e1.printStackTrace();
				return false;
			}
			if(e.isModified() && !e.isEmpty())
				evaluator.evaluate(e);
			
		}
		return res;
	}

	public static GeneticEntity[] crossover(ArrayList<GeneticEntity> extendedFathers) throws UnstableStateException {
		//Choix deux fathers à mixer
		//Démultiplier la liste des programs de G0 en fonction de leur Rate et taper random
		GeneticEntity[] fathers = getRandomFathers(extendedFathers);
        if (Utils.getRandomDouble() < Population.CROSSING_RATE /*&& (fathers[0].getGenes().size() > 2) && (fathers[1].getGenes().size() > 2)*/){
        	
        	GeneticEntity[] sons  = null;
        	if(fathers[0].size() > 2 && fathers[1].size() >2 )
        		sons = fathers[0].crossoverDeep(fathers[1]);
        	else
        		sons = fathers[0].crossover(fathers[1]);
    		return sons;
        }
        return fathers;
	}
	
	public static GeneticEntity[] getRandomFathers(ArrayList<GeneticEntity> fathers) {
		int i1 = Utils.getRandomInt(fathers.size());
		int i2 =  Utils.getRandomInt(fathers.size());
		if(i2 == i1)
			i2 = (i1+Utils.getRandomInt(1,3)) % fathers.size();
		
		GeneticEntity[] res =  new GeneticEntity[] {
				fathers.get(i1).clone(),
				fathers.get(i2).clone()
		};
		fathers = null;
		return res;
	}

	

	/**
	 * Return false when time (<code>MAX_TIME</code>) is over.
	 * @param objValues NOT USED
	 * @param startTime
	 * @return
	 */
	private boolean endConditionObjectives(float[] objValues) {
		boolean res = false;
		if(END_CONDITION_TYPE != null)
			switch (END_CONDITION_TYPE.toLowerCase()) {
		   		case "none" :
		   			return res;
		   		case "and" :
		   			res = true;
					for (int i = 0; i < OBJECTIVES_END_CONDITION.length; i++) 
						res &= objValues[i] >= OBJECTIVES_END_CONDITION[i];
					break;
				case "or" :
					res = false;
					for (int i = 0; i < OBJECTIVES_END_CONDITION.length; i++) 
						res |= objValues[i] >= OBJECTIVES_END_CONDITION[i];
					break;
	    	}
		if(LOGGER.isLoggable(Level.FINER)){
			String log = "End condition ("+END_CONDITION_TYPE+") {";
			for (float f : objValues) {
				log += f + " ";
			}
			LOGGER.finer(log+"} -> "+res);
		}
		
		if(res && LOGGER.isLoggable(Level.CONFIG)){
			String log = "End condition reached. (";
			for (float f : objValues) {
				log += f + " ";
			}
			log += ") ";
			LOGGER.config(log);
		}
    	return res;
    }

	boolean endCondExCh_firstReject = false;
	
	private float[] bufAvgs = new float[FitnessVector.NUMBER_OF_OBJECTIVES];
	private int bufTime = -1;
	
	public boolean endCondition(float[] objValues){
		if(bufTime < 0) bufTime = generation;
//		System.out.println("Evolutioner.endCondition("+bufTime+" | "+generation+") "+(generation-bufTime));
		boolean sameAvg = true;
		
		for (int i = 0; i < FitnessVector.NUMBER_OF_OBJECTIVES; i++) 
			sameAvg &= isFinBoundsofF2(bufAvgs[i], objValues[i], STATIONARY_VARIANCE);
//		System.out.println("sameAvg:"+sameAvg);
		if( !sameAvg ){
			bufTime = generation;
			bufAvgs = objValues;
		}
		
		boolean res = endConditionObjectives(objValues);
		
		if( generation - bufTime > STATIONARY_TIME - 3)
			LOGGER.fine("Stationary : " +(generation - bufTime)+" generations.");
		
//		System.out.println("res obj:"+res);
		if(res || (sameAvg && generation-bufTime >= STATIONARY_TIME)){
			bufTime = generation;
		} 
		return res;
	}

	private boolean isFinBoundsofF2(float F, float F2, float bound){
		return F < (F2 + bound) && F > (F2 - bound);
	}
	
	private void forceEvaluation(Population p) {
		LOGGER.fine("Force re-evaluation.");
		p.evaluate(evaluator);
	}

	public Evaluator getEvaluator() {
		return evaluator;
	}

	public Population getCurrentPopulation() {
		return currentPopulation;
	}
	
	
	public static List<CentroidCluster<DoublePointProgram>> clusterFrontParetoFromPopulation(Population popN) {
		return clusterFrontParetoFromPopulation(popN, NUMBER_OF_CLUSTERS);
		
	}
	
	public static List<CentroidCluster<DoublePointProgram>> clusterFrontParetoFromPopulation(Population popN, int numberOfClusters) {
		ArrayList<DoublePointProgram> dataPoints = new ArrayList<>(popN.getFrontPareto().getEntities().size());

		ArrayList<GeneticEntity> pareto = popN.getFrontPareto().getEntities();
		for (GeneticEntity ge : pareto) {
			Program p1 = (Program) ge;
			double[] distances = new double[pareto.size()];
			int i = 0;
			for (GeneticEntity ge2 : pareto) {
				Program p2 = (Program) ge2;
				distances[i++] = p1.computeDamerauLevensteinDistanceSum(p2);
			}
			DoublePointProgram dp = new DoublePointProgram(distances, p1);
			dataPoints.add(dp);
		}

		KMeansPlusPlusClusterer<DoublePointProgram> transformerKM = new KMeansPlusPlusClusterer<>(numberOfClusters);
		List<CentroidCluster<DoublePointProgram>> clustersKM = null;
		try {
			clustersKM = transformerKM.cluster(dataPoints);
		} catch (NumberIsTooSmallException e) {
			System.err.println("Pareto is too small or doesn't exist, please rise population size and/or number of generations (clusters asked="+numberOfClusters+")");
			e.printStackTrace();
			throw e;
		} 
		return clustersKM;
	}


	public static DoublePointProgram extractCenterFromCluster(CentroidCluster<DoublePointProgram> clusterKM) {
		double distanceToCenter = Double.MAX_VALUE;
		DoublePointProgram closestToCenter = null;
		for (DoublePointProgram ddp : clusterKM.getPoints()) {
			double d = ddp.euclidianDistanceToVector(clusterKM.getCenter().getPoint());
			if (d < distanceToCenter) {
				distanceToCenter = d;
				closestToCenter = ddp;
			}
		}
		return closestToCenter;
	}

	public static Program checkPresenceOfGroundTruthInPopulation(Population popN, Program prgGroundTruth) {
		boolean trouvePareto = false;
		Program res = null;
		for (GeneticEntity ge : popN.getFrontPareto().getEntities()) {
			Program p = (Program) ge;

			double[][] distances = p.computeDistances(prgGroundTruth);
			String s = "";
			double sumCosines = 0.0;
			double sumHammings = 0.0;
			double sumLevensts = 0.0;

			for (int j = 0; j < distances[0].length; j++) {
				sumCosines += distances[0][j];
				sumHammings += distances[1][j];
				sumLevensts += distances[1][j];
			}

			if (/*sumCosines / distances[0].length < .01 &&*/ sumHammings < 1) {
				trouvePareto = true;
				res = p;
			}
		}
		return res;
	}

	public static void loadConfig(){
		//Third load call
		STEP_BY_STEP 				= Config.getBooleanParam("STEP_BY_STEP");
		if(STEP_BY_STEP)
			GENERATIONS_STEP 		= Config.getIntParam("GENERATIONS_STEP");
		
		
		GENERATION_MAX 				= Config.getIntParam("GENERATION_MAX");
		CHECK_POINT_GENERATION		= Config.getIntParam("CHECK_POINT_GENERATION");
		try {
			CHECK_RESULT_IN_UI		= Config.getIntParam("CHECK_RESULT_IN_UI");
		} catch (Exception e2) {
			LOGGER.warning("CHECK_RESULT_IN_UI = "+Config.getStringParam("CHECK_RESULT_IN_UI")+" :Refresh result on UI manually.");
			CHECK_RESULT_IN_UI = Integer.MAX_VALUE;
		}
		

		try{
			StringTokenizer st = new StringTokenizer(Config.getStringParam("STATIONARY_TIME"), " ");
			switch (st.nextToken().trim()) {
			case "BEST":
				STATIONARY_TYPE = StationaryType.BEST;
				break;
			case "AVG":
				STATIONARY_TYPE = StationaryType.AVG;
				break;
			default:
				STATIONARY_TYPE = StationaryType.AVG;
				break;
			}
			STATIONARY_TIME = Integer.decode(st.nextToken().trim());
			STATIONARY_VARIANCE = Float.parseFloat(st.nextToken().trim());
				
		}catch (Exception e){
			LOGGER.warning("Stationary time and variance uncorrect. \n Expected is <int> <float> : "
					+ "\n  - <int> = number of generations with same average fitness values ; "
					+ "\n  - <float> tolerated variance in the fitness values.\nSelected : "+STATIONARY_TIME+" "+STATIONARY_VARIANCE);
		}
		
		
		try {
			MAX_TIME			= Config.getIntParam("MAX_TIME")*1000;//Seconds in the config file, millis in the execution
		} catch (Exception e1) {
//			LOGGER.warning("MAX_TIME not specified. Run for "+GENERATION_MAX+" generations.");
			MAX_TIME			= -1;
		}
		ROULETTE_GEOMETRIC 				= Config.getBooleanParam("ROULETTE_GEOMETRIC");
		
		
		try{
			StringTokenizer st = new StringTokenizer(Config.getStringParam("END_CONDITION"), " ");
			String s = st.nextToken().trim().toLowerCase();
			switch (s) {
			case "or" :
			case "and":
				END_CONDITION_TYPE = s.toUpperCase();
				break;
			default:
				END_CONDITION_TYPE = null;
				break;
			}
			ArrayList<Float> tmp = new ArrayList<>(FitnessVector.NUMBER_OF_OBJECTIVES);
			if(END_CONDITION_TYPE != null){
				for (int i = 0; i < FitnessVector.NUMBER_OF_OBJECTIVES && st.hasMoreTokens(); i++) {
					try {
						float f = Float.parseFloat(st.nextToken().trim());
						tmp.add(f);
					} catch (NumberFormatException e) {
						i = FitnessVector.NUMBER_OF_OBJECTIVES;
					}
				}
				OBJECTIVES_END_CONDITION = new float[tmp.size()];
				for (int j = 0; j < tmp.size(); j++) {
					OBJECTIVES_END_CONDITION[j] = tmp.get(j);
				}
			}
			
		}catch (Exception e){
			END_CONDITION_TYPE = null;
		}
		
//		File fo = new File(Config.DIR_OUT);
//	   	if(!fo.exists())
//	   		fo.mkdir();
//	   	File fr = new File(Config.DIR_NUM);
//	   	if(!fr.exists())
//	   		fr.mkdir();
	   	
		Contrainte.MUT_NEW_CONTEXT_EXPAND_PROBABILITY_FOR_ADDED_ELTS = Config.getIntParam("MUT_NEW_CONTEXT_EXPAND_PROBABILITY_FOR_ADDED_ELTS");

		Contrainte.MUT_RENAME_EXPAND_PROBAILITY_ADDED 		= Config.getIntParam("MUT_RENAME_EXPAND_PROBAILITY_ADDED");
		Contrainte.MUT_RENAME_EXPAND_PROBAILITY_SAMETYPE 	= Config.getIntParam("MUT_RENAME_EXPAND_PROBAILITY_SAMETYPE");
		Contrainte.MUT_RENAME_EXPAND_PROBAILITY_SAMESOURCE 	= Config.getIntParam("MUT_RENAME_EXPAND_PROBAILITY_SAMESOURCE");
		Contrainte.MUT_RENAME_EXPAND_PROBAILITY_THEREST 	= Config.getIntParam("MUT_RENAME_EXPAND_PROBAILITY_THEREST");

		Contrainte.MUT_COLLAPSE_PROBABILITY 		= Config.getDoubleParam("MUT_COLLAPSE_PROBABILITY");
		Contrainte.MUT_CHANGE_CONTEXT_INCLUDEALL 	= Config.getBooleanParam("MUT_CHANGE_CONTEXT_INCLUDEALL");
		Contrainte.MUT_CONTEXT_CHANGE_PROBABILITY 	= Config.getDoubleParam("MUT_CONTEXT_CHANGE_PROBABILITY");
	   	
		try {
			String clusters = Config.getStringParam("NUMBER_OF_CLUSTERS");
			try {
				if(clusters.startsWith("%"))
					NUMBER_OF_CLUSTERS = (int)Math.abs(Population.POPULATION_SIZE * (double)Integer.parseInt(clusters.substring(1))/100);
				else 
					NUMBER_OF_CLUSTERS = Integer.parseInt(clusters);
			} catch (NumberFormatException e) {
				LOGGER.warning("Number of clusters illformatted : NUMBER_OF_CLUSTERS="+clusters+" \n Expected is <int> or %<int> : "
						+ "\n  - <int> =  number of clusters ; "
						+ "\n  - %<int> = number of cluster in terms of percent of the pareto."
						+ "\nSelected : "+STATIONARY_TIME+" "+STATIONARY_VARIANCE);
			}
		} catch (Exception e) {
			LOGGER.warning("Number of clusters (NUMBER_OF_CLUSTERS) missing default is: "+NUMBER_OF_CLUSTERS);
		}
		if(NUMBER_OF_CLUSTERS < 5 ){
			NUMBER_OF_CLUSTERS = 5;
			LOGGER.warning("Number of clusters (NUMBER_OF_CLUSTERS) must be >= 5 Default: "+NUMBER_OF_CLUSTERS);
		}
		
	}

	enum StationaryType {
		BEST,
		AVG;

		public boolean isBest() {
			return this.equals(BEST);
		}
	}
	public static void logG_OnFile(BufferedWriter bw, int generation, long timeSpent, GeneticEntity maxE, float avgGenes) {
		if(bw != null){
			String statData = generation+";";
			statData += maxE.printNumericResult(";", "", Utils.formatMillis(timeSpent), false);
			logOnFile(bw, statData);
		} else {
			LOGGER.warning("Log on file impossible : writer is 'null', check code.");
		}
	}
	
	public static void logOnFile(BufferedWriter bw, String log){
		try {
			bw.write(log+"\n");
			bw.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static File[] buildFiles(String fileOutName, String fileOutDataName){
		File f1 = new File(fileOutName), f2 = new File(fileOutDataName);
	   	if(Config.VERBOSE_ON_FILE){
//	   		try {
//	   			BufferedWriter res1 = new BufferedWriter(new FileWriter(f1));
//	   		} catch (IOException e1) {
//	   			LOGGER.severe("Couldn't create file : '"+fileOutName+"'.");
//	   			e1.printStackTrace();
//	   		}
	   		try {
	   			BufferedWriter res2 = new BufferedWriter(new FileWriter(f2));
	   			res2.write("generation;"+FitnessVector.csvHeader+"\n");
	   			res2.flush();
	   			res2.close();
	   		} catch (IOException e1) {
	   			LOGGER.severe("Couldn't create file : '"+fileOutDataName+"'.");
	   			e1.printStackTrace();
	   		}
	   	}
	   	return new File[] { f1, f2 };
	}
	
    private void logG_OnUi(int generation, Population G0, GeneticEntity maxE, float[] avgs, float injectionnow, float avgGenes) {
    	Ui ui = Ui.getInstance();
    	if (Config.VERBOSE_ON_UI && ui != null){
	   		for (int i = 0; i < FitnessVector.NUMBER_OF_OBJECTIVES; i++)  
	   			ui.addObjective(generation, avgs[i], maxE.getFitnessVector().getValue(i), i);
	   		ui.addToGraph3(generation, avgGenes);
	   		ui.addToGraph2(generation, G0.getParetos().size());
	   		ui.addToGraph5(generation, G0.getFrontPareto().size());
 		}
    }

	public void printProgram(File f, Program p) {
		String timeSpent = Utils.formatMillis(System.currentTimeMillis() - startTime);
		String escapeChar = ";";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("  ** Partitionner - "+p.getName()+"'s data **  ");

			bw.write("\n"+escapeChar+"  " + Contrainte.totalNumberOfChanges() + " total changes.");
			bw.write("\n"+escapeChar+"  Time elapsed : " + timeSpent);
			bw.write("\n\n"+escapeChar+"  Stats :" + p.printStats());
			
			

			bw.write("\n\nBest pick in pareto front : " + p + ":" + p.printNumericResult("", "", "" + timeSpent, false) + "\n");
			FitnessVector fv = p.getFitnessVector();
			
			
			
			bw.write("\n\n-- OCL_"+p);
			bw.write(((Program)p).printExecutableOCL());
//			bw.write("\n\n;JESS_"+p);
//			bw.write(((Program)p).getJess());
			bw.flush();
			bw.close();
			LOGGER.info(p.getName()+" written in '"+f.getAbsolutePath()+"'");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public String printSetting() {
		String res = "";
		res += "Date: "+timeStamp+"\n";
		res += "Execution duration: "+Utils.formatMillis(timeSpent)+"\n";
		res += "Files and folders:\n";
		res += " + Metamodels:\n";
		res += "   - "+Config.getMetamodelFile().getAbsolutePath()+"\n";
		
		res += " + OCL: \n";
		res += "   - initial= "+Program.OCL_FILE.getAbsolutePath()+"\n";
		res += "   - expected="+Program.OCL_EXPECTED_FILE.getAbsolutePath()+"\n";
		
		res += " + Tests: "+Config.DIR_TESTS+"\n";
		res += " + Results: "+Config.DIR_RESULTS+"\n";
		
		res += " -- GENETICS --";
		
		
		//Pop size / iterations
		//Genetic rate (cross/mut/mutType)
		//# of clusters
		
		// Et aussi : distance des centroid-ishs au ground truth (+moyenne/ecart type etc.).
		// Solution appartient au Pareto ? true/false
		
		// print centroids (avec precision : distance GT, Fitness(#,#,#)
		
		return res;
	}
	public void printResult(File f) {
		String timeSpentStr = Utils.formatMillis(timeSpent);
		Population p = currentPopulation;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("  ** CoOCL **  ");
			bw.write("\n" + Config.printSetting(" ") + "");

			
			float avgGenes = 0.0f;
			for (GeneticEntity ge : p.getEntities()) {
				avgGenes += ge.size();
			}
			avgGenes = avgGenes / p.size();
			
			bw.write("\nResults :");
			bw.write("\n  Time elapsed : " + timeSpentStr);
			bw.write("\n\n" + p.printStatistics(true));
			
			
			
			//Injections
			
			GeneticEntity best = p.getBest();
//			evaluator.evaluate(best);

			bw.write("\n\nBest pick in pareto front : " + best + ":" + best.printNumericResult("", "", "" + timeSpentStr, false) + "\n");
			FitnessVector fv = best.getFitnessVector();
			
				
			
			bw.write("\n\n--OCL_"+p);
			bw.write(((Program)best).printExecutableOCL());
			bw.flush();
			bw.close();
			LOGGER.info(p.getName()+" written in '"+f.getAbsolutePath()+"'");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String[] createFileOutNames(){
		return createFileOutNames(timeStamp);
	}
	
	public String[] createFileOutNames(String timeStamp){
		String base = Config.DIR_RESULTS+"log_g_"+Config.METAMODEL_NAME+"_"+evaluator.getClass().getSimpleName()+"_"+Evolutioner.GENERATION_MAX+"_"+Population.POPULATION_SIZE+"_"+timeStamp;
		String res1 = base+".log";
		String res2 = base+".data.log";
		int itmp = 1;
    	while(new File(res1).exists() || new File(res2).exists()){
//    		base = Config.DIR_OUT+"log_g_"+Config.METAMODEL_NAME+"_"+evaluator.getClass().getSimpleName()+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+timeStamp2;
    		res1 = base+"-"+itmp+".log";
        	res2 = base+"-"+itmp+".data.log";
        	itmp++;
    	}
		return new String[] { res1, res2};
	}


	public void finalize(Population popN, Program prgGroundTruth) {
		boolean ground = Evolutioner.checkPresenceOfGroundTruthInPopulation(popN, prgGroundTruth) != null;
		if(ground  )
			LOGGER.config("\n    >--  Ground truth present in ParetoFront: "+prgGroundTruth+"  --<\n\n");
		
		
		//On recupere les clusters
		List<CentroidCluster<DoublePointProgram>> clustersKM = Evolutioner.clusterFrontParetoFromPopulation(popN);
		
		//On recupere les centres des clusters
		ArrayList<DoublePointProgram> centers = new ArrayList<>(clustersKM.size());
		for (CentroidCluster<DoublePointProgram> clusterKM : clustersKM) {
			boolean trouve = false;
			Program trouvee = null;
			System.out.println("Cluster ++");
			DoublePointProgram center = Evolutioner.extractCenterFromCluster(clusterKM);
			for (DoublePointProgram point : clusterKM.getPoints()) {
				if(point.getProgram().equals(ground)){
					trouve = true;
					trouvee = point.getProgram();
				}
				System.out.println(" - "+point.getProgram() +"\t("+center.getProgram().computeDamerauLevensteinDistanceSum(point.getProgram())+":"+point.getProgram().computeDamerauLevensteinDistanceSum(prgGroundTruth)+")");
			}
			System.out.println("Center extracted :");
			System.out.println(center.getProgram().printExecutableOCL());
//			if(trouve){
//				System.out.println("------ TROUVE  ---------------");
//				System.out.println(trouvee.printExecutableOCL());
//				System.out.println(prgGroundTruth.printExecutableOCL());
//				System.exit(0);
//			}
		}
		

	}

}

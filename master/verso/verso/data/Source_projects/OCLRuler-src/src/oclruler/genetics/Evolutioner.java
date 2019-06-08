package oclruler.genetics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import oclruler.genetics.Population.Pareto;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Model;
import oclruler.rule.Program;
import oclruler.rule.patterns.Pattern;
import oclruler.rule.struct.Constraint;
import oclruler.ui.Ui;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

public class Evolutioner {
	public static Logger LOGGER = Logger.getLogger(Evolutioner.class.getName());
	
	private static boolean USE_SOLUTION_DIRECTORY = false;
	
	
	/** Number of generation to wait with same evaluation (average of front Pareto entities) before a new example is added (pos or neg). <br/>To be specified in config file. */
	public static int 	 STATIONARY_TIME = 10; 
	public static float STATIONARY_VARIANCE = 0.001f; 
	public static StationaryType STATIONARY_TYPE;

	private static boolean 	ROULETTE_GEOMETRIC = false;
	public static int 		GENERATION_MAX = 1;
	public static int 		CHECK_POINT_GENERATION = 100;
	public static int 		CHECK_RESULT_IN_UI = 5;
	public static long 		MAX_TIME = 5 * 60 * 1000;//milliseconds
	public static boolean 	STEP_BY_STEP = false;
	public static int 		GENERATIONS_STEP = 0;

	public static String END_CONDITION_TYPE = "and";
	public static float[] OBJECTIVES_END_CONDITION ;


	Evaluator evaluator;
	private ExampleSet exampleSet;

	//Evolution state variables
	Population currentPopulation;
	HashMap<Integer, Model> injections;
	public int generation 		= 0;
	long startTime;

	private float avgGenes;

	//Log files
	//The blah_blah_blah.log writer
	File resultsFile = null;
	String logFileName;

	//The blah_blah_blah.data.log writer
	File resultsFileData = null;
	BufferedWriter bw, bwData;
	String logDataFileName;
	
	boolean runs = false;
	boolean pause = false;
	
	
	public Evolutioner(Evaluator eva, Population p, File resultFile, File dataResultFile) {
		setEvaluator(eva);
		eva.setEvo(this);
		
		currentPopulation = p;
		injections = new HashMap<>();
		
		resultsFile = resultFile;
		resultsFileData = dataResultFile;
		
		setUpFileWriters();
	}
	
	public Evolutioner(Evaluator eva, Population p) {
		setEvaluator(eva);
		eva.setEvo(this);
		currentPopulation = p;
		injections = new HashMap<>();
		
		
    	File[] bws = buildFiles();
		resultsFile = bws[0];
		resultsFileData = bws[1];

		setUpFileWriters();
	}
	
	
	public void setUpFileWriters() {
		try {
			bw = new BufferedWriter(new FileWriter(resultsFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			bwData = new BufferedWriter(new FileWriter(resultsFileData));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public Population evolutionate() throws Exception{
		if(currentPopulation == null){
			LOGGER.warning("No current population.");
			return null;
		}
		return evolutionate(currentPopulation);
	}
	   
	

	public Population evolutionate(Population pop)  {
		LOGGER.config("Evaluator is " + evaluator.getClass().getSimpleName());
		
		if(!exampleSet.isOraculized()) {
			throw new IllegalStateException("Oraculization requested before evolution starts.");
		}
		runs = true;
		
		currentPopulation = pop;
		if (Config.VERBOSE_ON_UI)
			Ui.showUi();

		LOGGER.config("Evaluation of initial population...");
		long t = System.currentTimeMillis();
		currentPopulation.evaluate(evaluator);
		currentPopulation.fastNonDominantSort2();
		currentPopulation.crowdingDistanceAssignement();
		
		
		LOGGER.config("Done. (in " + (((double) System.currentTimeMillis() - t) / 1000) + "s)");
		LOGGER.finer(currentPopulation.printStatistics(true));

		LOGGER.config("G_X: max/avgs = (" + FitnessVector.csvHeader + ")");

		long startTime = System.currentTimeMillis();
		int totalMutations = 0;
		avgGenes = 0;
		float distanceAvg = 0;
		float[] avgs = new float[FitnessVector.OBJECTIVES_CONSIDERED];
		double[] firsts = new double[FitnessVector.OBJECTIVES_CONSIDERED];

		long cumul_time_genetic_op = 0, cumul_time_nonDominant_sort = 0;
		int nextStep = generation + GENERATIONS_STEP;
		
		GeneticIndividual maxE_rnd = null;
		GeneticIndividual maxE = null;
		 
		 
		 //QUanf TFIDF_OBJECTIVE_ON getBestOnObj0 is null.
		 //Pareto is null, or empty. Pourquoi ?
		 
		 
		 
		//  loop's start
		boolean continueLoop = ++generation < GENERATION_MAX && 
				!endCondition(STATIONARY_TYPE.isBest()?currentPopulation.getBestOnObj0().getFitnessVector().getValues_Considered():avgs);
		while (continueLoop) {
//			Population G0 = currentPopulation;
			long startTimeGeneration = System.currentTimeMillis();
			int numberOfMutationThisGeneration = 0;

			long time_t = System.currentTimeMillis();

			if(Config.IS_RANDOM_RUN) {
				Population	newPopulation = Population.createRandomPopulation();
				
				newPopulation.evaluate(evaluator);
				newPopulation.fastNonDominantSort2();
				newPopulation.crowdingDistanceAssignement();
				newPopulation = newPopulation.cutPopulationInHalf();
				
				if(maxE_rnd== null || newPopulation.getBestOnObj0().getFitnessVector().getValue(0) > maxE_rnd.getFitnessVector().getValue(0))
					maxE_rnd = newPopulation.getBestOnObj0();
				currentPopulation = newPopulation;
			} else {
				numberOfMutationThisGeneration = geneticReproduction(currentPopulation, numberOfMutationThisGeneration);
	
				long time_genetic_operations = System.currentTimeMillis() - time_t;
				cumul_time_genetic_op += time_genetic_operations;
				time_t = System.currentTimeMillis();
				
				currentPopulation.evaluate(evaluator);
				currentPopulation.fastNonDominantSort2();
				currentPopulation.crowdingDistanceAssignement();
				long time_nonDominant_sorting = System.currentTimeMillis() - time_t;
				cumul_time_nonDominant_sort += time_nonDominant_sorting;
				currentPopulation = currentPopulation.cutPopulationInHalf();
		 		currentPopulation.fastNonDominantSort2();
		 		currentPopulation.crowdingDistanceAssignement();
			}
			
	 		totalMutations += numberOfMutationThisGeneration;

	 		long endTime = System.currentTimeMillis();

	 		int nbEntities = currentPopulation.getFrontPareto().size();
 			float[] sums = new float[FitnessVector.OBJECTIVES_CONSIDERED];
		 		avgs = new 	float[FitnessVector.OBJECTIVES_CONSIDERED];
			 	firsts = new double[FitnessVector.OBJECTIVES_CONSIDERED];
			
 			int 	sumGenes = 0;
//	 			int 	sumGenesSize = 0;//What is the size of a rule ? Does it mean anything ?
 					distanceAvg = 0;
 			int 	nbDist = 0;//Used to remove the boundaries (2*nb_objectives in fitFunc)
// 			String logg = "";
 			for (int i = 0; i < sums.length; i++) {
 				sums[i] = 0;
 				avgs[i] = 0;
 				firsts[i] = 0;
 				for (GeneticIndividual e : currentPopulation.getFrontPareto().getEntities()) {
 					sumGenes += e.size();
//	 					for (Gene g : e.getGenes())
//	 						sumGenesSize += g.size();//g.size == 0 !
 					double v = e.getFitnessVector().getValue(i);
// 					if(i == 1)
// 						logg += ((Program)e).getName()+" : ("+e.size()+"->"+v+" | " + e.isModified()+" | " + (e.size()==e.getFitnessVector().getProgramSize())+")\n";
 					if(v > firsts[i]) firsts[i] = v;

 					sums[i] += v;
 					if( e.getDistance() < (Integer.MAX_VALUE-1000) ){
 						nbDist++;
 						distanceAvg += e.getDistance();
 					}
 				}
 			}
 			
// 			System.out.println("Evolutioner.evolutionate()"+sums[1]+" | n"+logg);
 			
 			for (int i = 0; i < sums.length; i++)
 				avgs[i] = sums[i] / nbEntities;

 			maxE = currentPopulation.getBestOnObj0();
			if(maxE_rnd== null || maxE.getFitnessVector().getValue(0) > maxE_rnd.getFitnessVector().getValue(0))
				maxE_rnd = maxE;
			
			if(Config.IS_RANDOM_RUN)
				maxE = maxE_rnd;
		
 			avgGenes = sumGenes / (float)nbEntities;
 			distanceAvg = distanceAvg / nbDist;

 			
 				/*
		 		 * LOG, FILE and UI
		 		 */
		   	//Each generation LOGS
 			if(LOGGER.isLoggable(Level.CONFIG)){// LOGS
	 			String l = logGeneration(avgs, startTimeGeneration, numberOfMutationThisGeneration, endTime, maxE);
	 			if(LOGGER.getLevel().equals(Level.CONFIG))//Else if fine, print in method "generationLog"
	 				LOGGER.config(l);
	 			if(Config.VERBOSE_ON_UI)
	 				Ui.getInstance().log(l);
 			}
 			
 			//Generation check point LOGS                   (NOT IN EXPERIMENT_MODE)
	 		if(!Config.EXPERIMENT_MODE && generation % CHECK_POINT_GENERATION == 0 && generation != GENERATION_MAX){
	 			String log =
		   		"\nEvolution ckeck point :"
		    			+"\n  "+generation+" generations "
		    			+"\n  Total #mutations :  "+totalMutations
//			    			+"\n  Total Jess calls :  "+JessExecutor.counter
		    			+(Config.EXPERIMENT_MODE?
		    			"\n"+currentPopulation.printOneLineStatistics() //Here we call the sorting:paretos+distance crowding
		    			:"\n"+currentPopulation.printStatistics())
		    			
		    			+"Time elapsed : "+ToolBox.formatMillis(System.currentTimeMillis()-startTime) +" ("+ToolBox.formatMillis(cumul_time_genetic_op)+","+ToolBox.formatMillis(cumul_time_nonDominant_sort)+")";
	 			log += "\n";
	 			LOGGER.config(log);
	 			
	 			if(Config.VERBOSE_ON_UI){
	 				Ui.getInstance().log(log);
	 			}
	 		}
	 		
//	 		if(generation % CHECK_RESULT_IN_UI == 0 || generation == 1)
//	 			Ui.getInstance().updateResultingPop();
 			//end LOGS

 			//FILE
	 		if(Config.VERBOSE_ON_FILE)
 				logG_OnFile(bwData, generation, System.currentTimeMillis()-startTime, maxE, avgGenes);
 			//end FILE
 			//UI
 			logG_OnUi(generation, currentPopulation, maxE, avgs, 0.0f, avgGenes);//4th parameter (0.0f) not used.
	 		//end UI

	   		/* * END LOGS n UI * */
 				
 				
 			//STEP CONTROL 
 				if(STEP_BY_STEP && (generation == nextStep))
 					nextStep = stepControl(startTime, nextStep);
			//end STEP CONTROL
 		    	LOGGER.finest("Fin G"+generation);

 		    	
 		    //Take a break
    		while(pause)
	    		try {
					Thread.sleep(200);
				} catch (Exception e) {
					e.printStackTrace();
				}
					
    		continueLoop = ++generation < GENERATION_MAX && 
    				!endCondition(STATIONARY_TYPE.isBest()?currentPopulation.getBestOnObj0().getFitnessVector().getValues_Considered():avgs);
    	}// End generation loop
		
		if(maxE != null)
			logG_OnUi(generation, currentPopulation, maxE, avgs, 0.0f, avgGenes);//4th parameter (0.0f) not used.
   	
 		runs = false;
   		    	
    	String log = "Evolution ends."
    			+"\n  Evaluator is "+evaluator.getClass().getSimpleName()
    			+" on "+generation+" generations - Time elapsed : "+ToolBox.formatMillis(System.currentTimeMillis()-startTime)+"."
//    			+"\n  Total #mutations :  "+totalMutations
				+ "\n  "
				+ (Config.EXPERIMENT_MODE ? 
						currentPopulation.printOneLineStatistics() // Here we call the sorting:paretos+distance crowding
						: currentPopulation.printStatistics())
//    			+(Config.VERBOSE_ON_FILE?"\n  Wrote into "+logFileName:"")
    			;

    	LOGGER.info(log);
    	if(Config.VERBOSE_ON_UI){
    		Ui.getInstance().log(log);
//    		Ui.getInstance().setResultingPop(currentPopulation);
    	}

    	if(Config.VERBOSE_ON_FILE){
    		printResult();
    	}
		if(bwData != null) 
    		try {
    			bwData.close();
    		} catch (IOException e1) {
    			e1.printStackTrace();
    		}
		
//		System.out.println("1. (Evolutioner.evolutionate) : best = "+currentPopulation.getBestOnObj0().getName()+"\n"+currentPopulation.getBestOnObj0().prettyPrint());
		return currentPopulation;
	}


	public int geneticReproduction(Population G0, int mutationGeneration) {
		GeneticIndividual[] sons2;
		ArrayList<GeneticIndividual> fathersRouletteExtended = getRouletteExtendedEntities(G0);
		int rates = 0;
		while (G0.getEntities().size() < Population.NB_ENTITIES_IN_POP * 2) {

			try {
				sons2 = crossover(fathersRouletteExtended);
			} catch (UnstableStateException e2) {
				//  Crossover failed
				sons2 = null;
				e2.printStackTrace();
			}
			if (sons2 != null) {
				for (GeneticIndividual e : sons2) {
					evaluator.evaluate(e);
					int maxi = 0;
					while (maxi++ < 10 && G0.getEntities().contains(e)) {// See Program.equals(Program)
						e = e.clone();
						try {
							e.mutate();
						} catch (UnstableStateException e1) {
							LOGGER.severe("Mutation failed.");
							e1.printStackTrace();
						}
						mutationGeneration++;
					}
					
					if (e.isModified())
						evaluator.evaluate(e);
					if (!e.isEmpty() && !G0.getEntities().contains(e)) {
						mutationGeneration += mutation(e) ? 1 : 0; // MOVED down to avoid useless evaluations [N->N/2] 
																	// no need to mutate the one in the second half
																	// half.
						G0.addEntity(e);
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


	private String logGeneration(float[] avgs, long startTimeGeneration, int mutationGeneration, long endTime, GeneticIndividual maxE) {
		String log2 = "G_"+generation+": max=(";
		log2 += maxE.printNumericResult(",", "", "", true ) + ") avgs=(";
		
		String log3 ="";
		for (float f : avgs) 
			log3 += " "+ ToolBox.format2Decimals(f);
		log2 += log3.trim()+ ") |";
		
		log2 = ToolBox.completeString(log2, 40);
		
		double time = (endTime-startTimeGeneration);
		time = time /1000;
		String log = currentPopulation.getParetos().size()+"[";
		for (int i = 0; i < currentPopulation.getParetos().size(); i++) {
			Pareto p = currentPopulation.getPareto(i);
			log += (p.getEntities().size() + " ");
		}
		log2 += " " + log.trim() + "]";
		
		log =  log2 + " (in "+(time)+"s) "+ Pattern.numberOfInstances()+" pattern instantiated. Evaluations:"+evaluator.numberOfEvaluations + " in "+ToolBox.formatMillis(evaluator.evaluationTime);
		if(LOGGER.isLoggable(Level.FINE)){
			log +=  " | mut:"+mutationGeneration+" genes:"+ToolBox.format2Decimals(avgGenes) +" ";
			LOGGER.fine(log);
		}else {
			
		}
		
		return log;
	}


	private int stepControl(long startTime, int nextStep) {
		System.out.print("G_"+generation+" ended. Duration total : "+ToolBox.formatMillis(System.currentTimeMillis()-startTime)+
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
		if(moldingLoops_number%100 == 0 && !(moldingLoops_number%1000 == 0) ) LOGGER.warning("Genetic reproduction is molding wind : "+moldingLoops_number+" loops for nothing !");
		if(moldingLoops_number%1000 == 0) {
			 LOGGER.warning("Genetic reproduction is definitely molding wind : "+moldingLoops_number+" loops for nothing !");
			 LOGGER.warning("Generation "+generation+" results :\n"+G0.printStatistics());
			 LOGGER.warning("Continue ?!");
			@SuppressWarnings("resource")
			String scanEntry = new Scanner(System.in).nextLine();
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
					Program p = Program.createRandomProgram();
					G0.addEntity(p);
				}
				forceEvaluation(G0);
				
			} else {
				LOGGER.info("Shut down brutal !");
				System.exit(1);
			}
			
		}
		return moldingLoops_number;
	}



	private boolean mutation(GeneticIndividual e) {
//		System.out.println("Evolutioner.mutation("+e.getName()+")");
		boolean res = false;
		if ( ToolBox.getRandomDouble() < Population.MUTATE_RATE) {
			try {
				res = e.mutate();
			} catch (UnstableStateException e1) {
				//  Mutation failed
				LOGGER.severe("Mutation failed.");
				e1.printStackTrace();
				return false;
			}
			if(e.isModified() && !e.isEmpty())
				evaluator.evaluate(e);
			
		}
		return res;
	}

	private GeneticIndividual[] crossover(ArrayList<GeneticIndividual> extendedFathers) throws UnstableStateException {
		//Choix deux fathers à mixer
		//Démultiplier la liste des programs de G0 en fonction de leur Rate et taper random
		
		GeneticIndividual[] fathers = this.getRandomFathers(extendedFathers);
        if (ToolBox.getRandomDouble() < Population.CROSSING_RATE /*&& (fathers[0].getGenes().size() > 2) && (fathers[1].getGenes().size() > 2)*/){
        	
        	GeneticIndividual[] sons  = null;
        	if(fathers[0].size() > 2 && fathers[1].size() >2 )
        		sons = fathers[0].crossoverDeep(fathers[1]);
        	else
        		sons = fathers[0].crossover(fathers[1]);
    		return sons;
        }
        return fathers;
	}
	
	private GeneticIndividual[] getRandomFathers(ArrayList<GeneticIndividual> fathers) {
		int i1 = ToolBox.getRandomInt(fathers.size());
		int i2 =  ToolBox.getRandomInt(fathers.size());
		if(i2 == i1)
			i2 = (i1+ToolBox.getRandomInt(1,3)) % fathers.size();
		
		GeneticIndividual[] res =  new GeneticIndividual[] {
				fathers.get(i1).clone(),
				fathers.get(i2).clone()
		};
		fathers = null;
		return res;
	}

	private ArrayList<GeneticIndividual> getRouletteExtendedEntities(Population g0){
		ArrayList<GeneticIndividual> extendFromRate = null;
		if(ROULETTE_GEOMETRIC )
			extendFromRate = g0.getEntities_proba_rank_geometric();
		else
			extendFromRate = g0.getEntities_proba_rank();
		return extendFromRate;
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

	private boolean endConditionExamplesInjection(){
		if( !exampleSet.injectionPossible() ) 
			return true;
		else 
			injectExample();
		
		return false;
	}


	enum INJECTION_TYPE { 
		POS, NEG, RND
	}
	
	public void injectPositiveExample(){
		injectExample(INJECTION_TYPE.POS);
	}
	public void injectNegativeExample(){
		injectExample(INJECTION_TYPE.NEG);
	}
	public void injectExample(){
		injectExample(INJECTION_TYPE.RND);
	}
	
	/**
	 * If the injection is possible is not assessed ! To be ensured before calling ! See {@link ExampleSet#injectionPossible()}
	 * @param type
	 */
	public void injectExample(INJECTION_TYPE type) {
		Model injected = null;
		switch (type) {
		case POS:
			injected = exampleSet.injectPositiveExample();
			break;
		case NEG:
			injected = exampleSet.injectNegativeExample();
			break;
		case RND:
			int key = ToolBox.getRandomInt(2);
			if(!exampleSet.injectionPositivePossible() && key == 0) key = 1;
			if(!exampleSet.injectionNegativePossible() && key == 1) key = 0;
			switch (key) {
			case 0:
				injected = exampleSet.injectPositiveExample();
				break;
			case 1:
				injected = exampleSet.injectNegativeExample();
				break;
			}
			break;
		}
			
		for (GeneticIndividual ge : currentPopulation.getEntities()) {
			ge.setModified();
		}
		
//		Model injected = modelSet.injectExample();
		String log1 = "";
		String suf1 = "";
		if(injected.isValid()){
			log1 = "One more positive example considered : '"+injected.getFileName()+"' for a total of "+exampleSet.getPositives().size()+" / "+exampleSet.getAllPositives().size()+".";
			suf1 = "Positive";
		} else {
			log1 = "One more negative example considered : '"+injected.getFileName()+"' for a total of "+exampleSet.getPositives().size()+" / "+exampleSet.getAllNegatives().size()+".";
			suf1 = "Negative";
		}
		
		injections.put(generation, injected);
		
		//log2 = fullfine, log1 = 1 line
		String log2 = log1;
		if(LOGGER.isLoggable(Level.FINE)){
			String s = "";
			for (Model m : exampleSet.getPositives()) 
				s += " "+m.getFileName();
			log2 += "\n+"+exampleSet.getPositives().size()+"{"+s.trim()+"}";
			s= "";
			for (Model m : exampleSet.getNegatives()) 
				s += " "+m.getFileName();
			log2 += "\n-"+exampleSet.getNegatives().size()+"{"+s.trim()+"}";
			LOGGER.fine(log2);
		} else {
			LOGGER.config(log1);
		}
		
		
		if(Config.VERBOSE_ON_UI)
			Ui.getInstance().log(log2);
		
		if(Config.VERBOSE_ON_FILE && bwData != null) {
			try {
				bwData.write("#"+log1+"\n");
				bwData.flush();
			} catch (IOException e) {}
		}
		
		String suf = (!exampleSet.injectionPossible()? "[No more examples to inject]":"" );
		if(!LOGGER.isLoggable(Level.CONFIG))
			LOGGER.info(suf1+" injection : "+". ("+exampleSet.getPositives().size()+"+|-"+exampleSet.getNegatives().size()+") "+ suf);
		
		forceEvaluation(currentPopulation);
	}
	
	private float[] bufAvgs = new float[FitnessVector.OBJECTIVES_CONSIDERED];
	private int bufTime = -1;
	
	public boolean endCondition(float[] objValues){
		if(bufTime < 0) bufTime = generation;
//		System.out.println("Evolutioner.endCondition("+bufTime+" | "+generation+") "+(generation-bufTime));
		boolean sameAvg = true;
		
		for (int i = 0; i < FitnessVector.OBJECTIVES_CONSIDERED; i++) 
			sameAvg &= isFinBoundsofF2(bufAvgs[i], objValues[i], STATIONARY_VARIANCE);
//		System.out.println("sameAvg:"+sameAvg);
		if( !sameAvg ){
			bufTime = generation;
			bufAvgs = objValues;
		}
		boolean res = endConditionObjectives(objValues);
		
		if( generation - bufTime > STATIONARY_TIME - 3 && exampleSet.injectionPossible())
			LOGGER.fine("Stationary : " +(generation - bufTime)+" generations.");
		
//		System.out.println("res obj:"+res);
		if(res || (sameAvg && generation-bufTime >= STATIONARY_TIME)){
//			System.out.println("generation-bufTime >= STATIONARY_TIME +  : "+ (generation-bufTime >= STATIONARY_TIME));
//			System.out.println("injection: "+injectionPossible());
			boolean b = endConditionExamplesInjection();
			
			bufTime = generation;
			res &= b;
		} 
		return res;
	}

	private boolean isFinBoundsofF2(float F, float F2, float bound){
		return F < (F2 + bound) && F > (F2 - bound);
	}
	

	private synchronized void forceEvaluation(Population p) {
		LOGGER.fine("Force re-evaluation.");
		p.evaluate(evaluator, true);
	}

	public Evaluator getEvaluator() {
		return evaluator;
	}

	public ExampleSet getModelSet() {
		return exampleSet;
	}
	
	public Population getCurrentPopulation() {
		return currentPopulation;
	}

	public static void loadConfig(){
		//Third load call
		STEP_BY_STEP 				= Config.getBooleanParam("STEP_BY_STEP");
		if(STEP_BY_STEP)
			GENERATIONS_STEP 		= Config.getIntParam("GENERATIONS_STEP");
		
		
		GENERATION_MAX 				= Config.getIntParam("GENERATION_MAX");
		CHECK_POINT_GENERATION		= Config.getIntParam("CHECK_POINT_GENERATION");
//		try {
//			CHECK_RESULT_IN_UI		= Config.getIntParam("CHECK_RESULT_IN_UI");
//		} catch (Exception e2) {
//			LOGGER.warning("CHECK_RESULT_IN_UI = "+Config.getStringParam("CHECK_RESULT_IN_UI")+" :Refresh result on UI manually.");
//			CHECK_RESULT_IN_UI = Integer.MAX_VALUE;
//		}
		

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
			ArrayList<Float> tmp = new ArrayList<>(FitnessVector.OBJECTIVES_CONSIDERED);
			if(END_CONDITION_TYPE != null){
				for (int i = 0; i < FitnessVector.OBJECTIVES_CONSIDERED && st.hasMoreTokens(); i++) {
					try {
						float f = Float.parseFloat(st.nextToken().trim());
						tmp.add(f);
					} catch (NumberFormatException e) {
						i = FitnessVector.OBJECTIVES_CONSIDERED;
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
	}

	enum StationaryType {
		BEST,
		AVG;

		public boolean isBest() {
			return this.equals(BEST);
		}
	}
	
	public static void logG_OnFile(BufferedWriter bw, int generation, long timeSpent, GeneticIndividual maxE, float avgGenes) {
		if(!Config.VERBOSE_ON_FILE)
			return;
		if(bw != null){
			String statData = generation+";";
			statData += maxE.printNumericResult(";", "", ToolBox.formatMillis(timeSpent), false);
			logOnFile(bw, statData);
		} else {
			LOGGER.warning("Log on file impossible : writer is 'null', check code.");
		}
	}
	
	public static void logOnFile(BufferedWriter bw, String log){
		if(!Config.VERBOSE_ON_FILE)
			return;
		try {
			bw.write(log+"\n");
			bw.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public File[] buildFiles() {
		if (!Config.VERBOSE_ON_FILE)
			return null;
		startTime = System.currentTimeMillis();

		String fileNames[] = ToolBox.createEvolutionLogFileNames();

		logFileName = fileNames[0];
		logDataFileName = fileNames[1];

		File f1 = new File(logFileName), f2 = new File(logDataFileName);
		try {
			BufferedWriter res2 = new BufferedWriter(new FileWriter(f2));
			res2.write("generation;" + FitnessVector.csvHeader + "\n");
			res2.flush();
			res2.close();
		} catch (IOException e1) {
			LOGGER.severe("Couldn't create file : '" + logDataFileName + "'.");
			e1.printStackTrace();
		}
		return new File[] { f1, f2 };
	}
	
	private void logG_OnUi(int generation, Population G0, GeneticIndividual maxE, float[] avgs, float injectionnow, float avgGenes) {
		if (Config.VERBOSE_ON_UI) {
			Ui ui = Ui.getInstance();
			ui.updatePopulationViewer();
			ui.updatePatternTable();
			for (int i = 0; i < FitnessVector.OBJECTIVES_CONSIDERED; i++)
				ui.addObjective(generation, avgs[i], maxE.getFitnessVector().getValue(i), i);
			ui.addToGraph3(generation, avgGenes);
			ui.addToGraph2(generation, G0.getParetos().size());
			ui.addToGraph4(generation, injections.get(generation - 1) != null ? (injections.get(generation - 1).isValid() ? 5.0f : -5.0f) : 0.0f);
			ui.addToGraph5(generation, G0.getFrontPareto().size());

		}
	}
	
	public void printResult() {
		String timeSpent = ToolBox.formatMillis(System.currentTimeMillis() - startTime);
		Population p = currentPopulation;
		
		//copy to setting/ :
		//  - oclpattern file
		//  - covdef file
		//  - config.properties file
		ToolBox.buildResultsSettingDirectory();
		
		//copy models (+statistics) to oracle/
		ToolBox.buildResultsOracleDirectory();
		
		//solution1, solution2, ... solutionx-BEST, ...
		if(Config.EXPERIMENT_MODE){
			
			/*
			 * HEAVILY COUPLED
			 * with Config.EXPERIMENT_{constants}
			 */
			
			
			File base_exp = ToolBox.touchResultSubDirectory(Config.getRunFolderName());
			File run = Paths.get(base_exp.getAbsolutePath(), Config.run_prefix+Config.RUN_NB).toFile();
			if(!run.exists())
				System.out.println("        SHIT !! ");
			
			
			String resStr = Paths.get(run.getAbsolutePath(), USE_SOLUTION_DIRECTORY? "_solution" : "").toString() + File.separator;
			File solutionDir = new File(resStr);
			if (!solutionDir.exists())
				solutionDir.mkdir();
			p.writeSolution(solutionDir, true);
		}else {
			File solutionDir = ToolBox.touchSolutionDirectory();
			p.writeSolution(solutionDir);
		}
		//build stat file from pareto solutions
		//build stat file from best solution
		
		
		
		storeGeneralLogInFile(timeSpent, p);
	}


	public void storeGeneralLogInFile(String timeSpent, Population p) {
		storeGeneralLogInFile(timeSpent, p, this.resultsFile);
		
	}
	public void storeGeneralLogInFile(String timeSpent, Population p, File resultFile) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile));
			bw.write("  ** Partitionner **  ");
			bw.write("\n" + Config.printSetting(" ") + "");

			
			float avgGenes = 0.0f;
			for (GeneticIndividual ge : p.getEntities()) {
				avgGenes += ge.size();
			}
			avgGenes = avgGenes / p.size();
			
			bw.write("\nResults :");
			bw.write("\n  " + Constraint.numberOfInstances() + " patterns used");
			bw.write("\n  Average number of patterns per Program : " + avgGenes);
			bw.write("\n  Time elapsed : " + timeSpent);
			bw.write("\n\n" + p.printStatistics(true));
			
			
			bw.write("\n\nOracle :\n");
			bw.write(exampleSet.printOracleDecisions()+"\n");
			
			//Injections
			if(injections.isEmpty())
				bw.write("\n\nAll examples considered at begining : no injections.\n");
			else {
				bw.write("\n\nInjections :\n");
				String pos = " - positives : ", neg = " - negatives : "; 
				for (Integer i : injections.keySet()) {
					if(injections.get(i).isValid())
						pos += injections.get(i) +"("+i + "), ";
					else neg += injections.get(i) +"("+i + "), ";
				}
				if(pos.lastIndexOf(",") > 0 )
					pos = pos.substring(0, pos.lastIndexOf(","));
				if(neg.lastIndexOf(",") > 0 )
					neg = neg.substring(0, neg.lastIndexOf(","));
				
				bw.write(pos + "\n");
				bw.write(neg + "\n");
			}
			
			GeneticIndividual best = p.getBestOnObj0();
//			evaluator.evaluate(best);

			bw.write("\n\nBest pick in pareto front : " + best + ":" + best.printNumericResult("", "", "" + timeSpent, false) + "\n");
//			FitnessVector fv = best.getFitnessVector();
//			HashSet<Constraint> ptsFired = fv.getFiredConstraints();
//			for (Constraint pat : ((Program)best).getConstraints()) {
//				String suf = ptsFired.contains(pat)?" - FIRE":"";
//				bw.write(pat.printResultPane("  ")+suf+"\n");
//			}		
			
			bw.write("\n\n--OCL_"+p);
			bw.write(((Program)best).getOCL());
			bw.flush();
			bw.close();
			LOGGER.config(p.getName()+" written in '"+resultFile.getAbsolutePath()+"'");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

	public boolean hasStopped() {
		return !runs;
	}

	public void togglePause() {
		pause = !pause;
		if(runs && pause && Config.VERBOSE_ON_UI)
			Ui.getInstance().setTitle(Ui.getInstance().getTitle() + " - Paused");
		else if(runs && !pause && Config.VERBOSE_ON_UI)
			Ui.getInstance().setTitle(Ui.getInstance().getTitle().substring(0, Ui.getInstance().getTitle().length() - " - Paused".length()));
	}


	public boolean isPause() {
		return pause;
	}
	
	public void setEvaluator(Evaluator evaluator) {
		this.evaluator = evaluator;
		this.exampleSet = evaluator.getExampleSet();
	}
}

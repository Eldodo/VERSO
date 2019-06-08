package genetic;

import genetic.Population.Pareto;
import genetic.fitness.FitnessVector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import models.Model;
import models.ModelSet;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import partition.composition.FragmentSet;
import partition.composition.ModelFragment;
import ui.Ui;
import utils.Config;
import utils.Utils;


public class Evolutioner {
	public final static Logger LOGGER = Logger.getLogger(Evolutioner.class.getName());
	public static int CHECK_POINT_GENERATIONS = 100;
	public static long CHECK_POINT_TIME = 60 * 1000;//milliseconds 
	public static long MAX_TIME = 5 * 60 * 1000;//milliseconds 
	public  static int GENERATION_MAX = 1;
	public  static String OBJECTIVES_END_CONDITION_TEXT = "";
	private  static float[] OBJECTIVES_END_CONDITION;
	private  static int OBJECTIVES_END_CONDITION_TYPE = 0;
	private final static int END_CONDITION_TYPE_AND = 0;
	private final static int END_CONDITION_TYPE_OR = 1;
	private final static int END_CONDITION_TYPE_NONE = 2;
	private static boolean ROULETTE_GEOMETRIC = false;
	public static boolean MONO_OBJECTIVE_ON = false;
	
	Evaluator evaluator;
	
	//Evolution state variables
	Population currentPopulation;
	int currentGeneration = 0;
	boolean newCurrentPopulation = true;

	
	//Log files
	//The blah_blah_blah.log writer
	BufferedWriter bw = null;
	
	//The blah_blah_blah.data.log writer
	BufferedWriter bwData = null;

	public static void loadConfig(){
		//Third load call
		GENERATION_MAX 				= Config.getIntParam("GENERATION_MAX");
		CHECK_POINT_GENERATIONS		= Config.getIntParam("CHECK_POINT_GENERATIONS");
		
		try {
			CHECK_POINT_TIME		= Config.getIntParam("CHECK_POINT_TIME")*1000;//Seconds in the config file, millis in the execution
		} catch (Exception e1) {
			LOGGER.warning("CHECK_POINT_TIME not specified. No time evaluation.");
			CHECK_POINT_TIME		= -1;
		}
		try {
			MAX_TIME			= Config.getIntParam("MAX_TIME")*1000;//Seconds in the config file, millis in the execution
		} catch (Exception e1) {
			LOGGER.warning("MAX_TIME not specified. Run for "+GENERATION_MAX+" generations.");
			MAX_TIME			= -1;
		}

		ROULETTE_GEOMETRIC 				= Config.getBooleanParam("ROULETTE_GEOMETRIC");
		
		
		OBJECTIVES_END_CONDITION_TEXT 		= Config.getStringParam("OBJECTIVES_END_CONDITION");
		OBJECTIVES_END_CONDITION = new float[Config.NUMBER_OF_OBJECTIVES];
		try{
			StringTokenizer st = new StringTokenizer(OBJECTIVES_END_CONDITION_TEXT, ";");
			String s = st.nextToken().trim();
			if(s.equalsIgnoreCase("or"))
				OBJECTIVES_END_CONDITION_TYPE = END_CONDITION_TYPE_OR;
			else if(s.equalsIgnoreCase("and"))
				OBJECTIVES_END_CONDITION_TYPE = END_CONDITION_TYPE_AND;
			else if(s.equalsIgnoreCase("none"))
				OBJECTIVES_END_CONDITION_TYPE = END_CONDITION_TYPE_NONE;
			
			else throw new IllegalArgumentException("");
			
			for (int i = 0; i < OBJECTIVES_END_CONDITION.length; i++) 
				OBJECTIVES_END_CONDITION[i] = Float.parseFloat(st.nextToken());
			
		} catch(Exception e){
			LOGGER.severe("Objective end conditions not well formulated : '"+OBJECTIVES_END_CONDITION_TEXT+"', OBJECTIVES_END_CONDITION must be in the form : '{OR,AND};<double>;<double>'");
			LOGGER.severe("Both objective end conditions set to 1.0");
			for (int i = 0; i < OBJECTIVES_END_CONDITION.length; i++) 
				OBJECTIVES_END_CONDITION[i] = (float)1.0;
		}
	}

	public Evaluator getEvaluator() {
		return evaluator;
	}
	
	public Evolutioner(Evaluator ev, Population p) {
		this.evaluator = ev;
		currentPopulation = p;
	}
	String timeStamp = "";
	public String getLastTimeStamp() {
		return timeStamp;
	}
	Pareto pCourant = null;
	private float avgGenes, avgGeneSize;

	
	
   public Population evolutionateMono(){
	   if(currentPopulation == null){
		   LOGGER.warning("No population courante.");
		   return null;
	   }
	   return evolutionateMono(currentPopulation);
   }
	   
   public Population evolutionateMono(Population G0){
		
		MONO_OBJECTIVE_ON = true;
    	//Log files
    	timeStamp = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.FRANCE).format( new Date() );
    	String timeStamp2 = timeStamp;
    	String ev = evaluator.getClass().getSimpleName();
    	if(ev.endsWith("FragmentSet")) ev = ev.substring(0, ev.indexOf("FragmentSet"));
    	String fileOutName = Config.DIR_OUT+"log_mono_"+Config.METAMODEL_NAME+"_"+ev+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_"+timeStamp+".log";
    	String fileOutDataName = Config.DIR_OUT+"log_mono_"+Config.METAMODEL_NAME+"_"+ev+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_"+timeStamp+".data.log";
    	int itmp = 1;
    	while(new File(fileOutName).exists() ||new File(fileOutDataName).exists()){
    		timeStamp2 = timeStamp+"-"+(++itmp);
    		fileOutName = Config.DIR_OUT+"log_mono_"+Config.METAMODEL_NAME+"_"+ev+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_"+timeStamp2+".log";
        	fileOutDataName = Config.DIR_OUT+"log_mono_"+Config.METAMODEL_NAME+"_"+ev+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_"+timeStamp2+".data.log";
    	}
    	timeStamp = timeStamp2;
    	buildFiles(fileOutName, fileOutDataName);
    	//End Log Files
    	
//    	final ui.Ui chart = new ui.Ui(Config.METAMODEL_NAME, ev+ " - " + timeStamp);
    	if (Config.VERBOSE_ON_UI){
    		subscribeParetoListener(Ui.getInstance().getChartTime());
    		Ui.getInstance().setMonoObjective();
    		Ui.getInstance().setTextSetting(Config.printPrettySetting());
    		SwingUtilities.invokeLater(new Runnable() {
    			@Override
    			public void run() {
    				Ui.getInstance().setVisible(true);
    			}
    		});
    	}
    	
    	
    	
    	LOGGER.config("Evaluator is "+evaluator.getClass().getSimpleName());
    	if(Config.VERBOSE_ON_UI)
    		Ui.getInstance().log("Evaluator is "+evaluator.getClass().getSimpleName());
    	G0.evaluate(evaluator);//Give marks to entities
 		currentPopulation = G0;
 		newCurrentPopulation = true;
    	
    	
    	long startTime = System.currentTimeMillis();
		int generation = 0;
		int totalMutations = 0;
		float avgGenes = 0, distanceAvg = 0;
		float[] res = new float[Config.NUMBER_OF_OBJECTIVES];
		//TODO loop's start - Mono
    	while(generation++ < GENERATION_MAX  && !endCondition(res, startTime) ){
    		currentGeneration = generation;
    		long startTimeGeneration = System.currentTimeMillis();
    		
    		getRsGeneration();//Renewing static fields from EMF

    		
	 		Entity[] sons2;
	 		int mutationGeneration = 0;
	 		Population G1 = new Population();
	    	
	 		G0.orderEntitiesWithMonoValue();
	    	for (int i = 0; i < 3; i++) 
				G1.addEntity(G0.getEntities().get(i).clone());
	    	
	    	
	    	ArrayList<Entity> fathersRouletteExtended = getRouletteExtendedEntities(G0);
			while (G1.getEntities().size() < Population.NB_ENTITIES_IN_POP/2) {
				sons2 = crossover(fathersRouletteExtended);
				for (Entity e : sons2){
					mutationGeneration += mutation(e)?1:0;
					G1.addEntity(e);
				}
			}
			
			
	 		G1.orderEntitiesWithMonoValue();
//	 		del_B_Part(G0.getEntities(), (Population.NB_ENTITIES_IN_POP/2));
	 		//Clean to relax memory
	 		
	 		G0 = G1;
	 		currentPopulation = G0;newCurrentPopulation = true;
	 		G1 = null;
	 		System.gc();
	 		totalMutations += mutationGeneration;
	 		
	 		long endTime = System.currentTimeMillis();
	 		/*
	 		 * LOG, FILE and UI
	 		 */
	 		if(generation % CHECK_POINT_GENERATIONS == 0 && generation != GENERATION_MAX){
	 			String log = 
		   		"\nEvolution ckeck point :"
		    			+"\n  "+generation+" generations"
		    			+"\n  Total #mutations :  "+totalMutations
		    			+"\n"+G0.printStatistics() //Here we call the sorting:paretos+distance crowding
		    			+"Time elapsed : "+Utils.formatMillis(System.currentTimeMillis()-startTime);
	 			
	 			
	 			
	 			LOGGER.config(log);
	 			if(Config.VERBOSE_ON_UI){
	 				Ui.getInstance().log(log);
	 				Ui.getInstance().setResultingPop(G0);
	 			}
	 		}

	 		int nbEntities = G0.getEntities().size();
 			float sum,avg,first;
			 Entity maxE = null;
 			int 	sumGenes = 0;
 			int 	sumGenesSize = 0;
 			int 	nbDist = 0;//Used to remove the boundaries (2*nb_objectives in fitFunc)
			sum = 0; 
			avg = 0;
			first = 0;
			for (Entity e : G0.getEntities()) {
				sumGenes += e.getGenes().size();
				for (Gene g : e.getGenes()) 
					sumGenesSize += g.size();
				double v = e.getMonoValue();
				
				if(v > first) {
					first = (float)v;
					maxE = e;
				}
				sum += v;
			}
 			for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) 
				res[i] = (float) maxE.getFitnessVector().getValue(i);
			
 			avgGenes = sumGenes / (float)nbEntities;
 			float avgGeneSize = sumGenesSize / (float)sumGenes;
 			distanceAvg = distanceAvg / nbDist;
 			avg = sum / nbEntities;
 			
 			//FILE
	 			logG_OnFile(bwData, generation, System.currentTimeMillis()-startTime, maxE, avgGenes, avgGeneSize);
 			//end FILE
	 			
	 			
 			//UI
		 		if (Config.VERBOSE_ON_UI && Ui.getInstance() != null){
		 			
		 			Ui.getInstance().addObjective(generation, avg, first, 0);
			   		
		 			Ui.getInstance().addToGraph2(generation, (float)maxE.getFitnessVector().getCoverage());
		 			Ui.getInstance().addToGraph3(generation, (float)maxE.getFitnessVector().getDissimilarity());
		 		}
	 		//end UI
	 		
		   	//LOGS
	 			String log2 = "G_"+generation+": \tobjs = ("+Utils.format2Decimals(avg)+")";
	 			log2 += "  "+maxE.printResult(";", "", "", true);
 				
	 			double time = (endTime-startTimeGeneration);
 				time = time /1000;
 				String log = log2 +" avg dist = "+Utils.format2Decimals(distanceAvg)+"" +"]\tmut:"+mutationGeneration+" genes:"+Utils.format2Decimals(avgGenes) +" (duration:"+(time)+"s) "+ Model.nbModels+" models used";
	 			if(LOGGER.isLoggable(Level.FINER)){
	 				LOGGER.finer(log);
	 			}else {
	 				log = log2 + " (duration:"+(time)+"s) "+ Model.nbModels+" models used ";
	 				LOGGER.fine(log);
	 			}
	 			if(Config.VERBOSE_ON_UI)
	 				Ui.getInstance().log(log);
 			//end LOGS
	   		/* * END LOGS n UI * */
    	}// End generation loop
    	long timeSpent = System.currentTimeMillis()-startTime;
    	notifyEndToParetoListeners(timeSpent);
    	
    	String log = "\nEvolution ends."
    			+"\n  Evaluator is "+evaluator.getClass().getSimpleName()
    			
    			+"\n  "+generation+" generations"
    			+"\n  Total #mutations :  "+totalMutations
    			+"\n"+G0.printStatistics(true)
    			+"\n  Wrote into "+fileOutName  
    			+"\n  Time elapsed : "+Utils.formatMillis(timeSpent);
    	LOGGER.info(log);
    	if(Config.VERBOSE_ON_UI){
    		Ui.getInstance().log(log);
    		Ui.getInstance().setResultingPop(G0);
    	}
    	
    	if(Config.VERBOSE_ON_FILE && bw != null){
    		try {
    			bw.write("  ** Partitionner **  ");
				bw.write("\n"+Config.printSetting(" ")+"");
				
				bw.write("\nResults :");
				bw.write("\n  "+Model.nbModels+" models used from '"+Config.DIR_INSTANCES+Config.METAMODEL_NAME+"'");
				bw.write("\n  Average number of models per ModelSet : "+avgGenes);
				bw.write("\n  Time elapsed : "+Utils.formatMillis(System.currentTimeMillis()-startTime));
				bw.write("\n\n"+G0.printStatistics(true));
				
				FragmentSet fs = (FragmentSet)evaluator;
				String uncovereds = "Uncovereds ("+fs.getUncovereds().size()+") : {";
				for (ModelFragment mf : fs.getUncovereds()) 
					uncovereds += "\n   "+ mf + "";
				bw.write(uncovereds+"\n}\n\n");
				String fragments = "Fragments ("+fs.getFragments().size()+") : {";
				for (ModelFragment mf : fs.getFragments())
					fragments += "\n   "+ mf + "";
				bw.write(fragments+"\n}");
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    		if(bwData != null)
	    		try {
	    			bwData.close();
	    		} catch (IOException e1) {
	    			e1.printStackTrace();
	    		}
    	}
		return G0;

	}
	
	public static void logG_OnFile(BufferedWriter bwData2, int generation, long timeSpent, Entity maxE, float avgGenes, float avgGeneSize) {
		if(Config.VERBOSE_ON_FILE && bwData2 != null){
			
			String statData = generation+";"+maxE.printResult(";", "", Utils.formatMillis(timeSpent), false)+"("+maxE.getMonoValue()+")";
			try {
				bwData2.write(statData+"\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void buildFiles(String fileOutName, String fileOutDataName){
	   	File fo = new File(Config.DIR_OUT);
	   	if(!fo.exists())
	   		fo.mkdir();
	   	File fr = new File(Config.DIR_NUM);
	   	if(!fr.exists())
	   		fr.mkdir();

	   	if(Config.VERBOSE_ON_FILE){
	   		try {
	   			bw = new BufferedWriter(new FileWriter(new File(fileOutName)));
	   		} catch (IOException e1) {
	   			LOGGER.severe("Couldn't create file : '"+fileOutName+"'.");
	   			e1.printStackTrace();
	   		}
	   		try {				
	   			bwData = new BufferedWriter(new FileWriter(new File(fileOutDataName)));
	   			bwData.write("generation;"+FitnessVector.csvHeader+";elapsedTime;nbModelsBest;nbModelsAvg;sizeModelAvg\n");
	   		} catch (IOException e1) {
	   			LOGGER.severe("Couldn't create file : '"+fileOutDataName+"'.");
	   			e1.printStackTrace();
	   		}
	   	}

	}
	
	private	static ResourceSet rsGeneration = new ResourceSetImpl();
	public static ResourceSet getRsGeneration() {
		rsGeneration = new ResourceSetImpl();
		for (EPackage ep : Utils.ePackages()) {
			
			rsGeneration.getPackageRegistry().put(
			    		ep.getNsURI(), ep
		        );
		}
		return rsGeneration;
	}
	
   public Population evolutionate(){
	   if(currentPopulation == null){
		   LOGGER.warning("No population courante.");
		   return null;
	   }
	   return evolutionate(currentPopulation);
   }
   
   public Population evolutionate(Population G0){
    	MONO_OBJECTIVE_ON = false;
    	//Log files
    	timeStamp = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.FRANCE).format( new Date() );
    	String timeStamp2 = timeStamp;
    	String ev = evaluator.getClass().getSimpleName();
    	if(ev.endsWith("FragmentSet")) ev = ev.substring(0, ev.indexOf("FragmentSet"));
    	String fileOutName = Config.DIR_OUT+"log_g_"+Config.METAMODEL_NAME+"_"+ev+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_"+timeStamp+".log";
    	String fileOutDataName = Config.DIR_OUT+"log_g_"+Config.METAMODEL_NAME+"_"+ev+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_"+timeStamp+".data.log";
    	int itmp = 1;
    	while(new File(fileOutName).exists() ||new File(fileOutDataName).exists()){
    		timeStamp2 = timeStamp+"-"+(++itmp);
    		fileOutName = Config.DIR_OUT+"log_g_"+Config.METAMODEL_NAME+"_"+ev+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_"+timeStamp2+".log";
        	fileOutDataName = Config.DIR_OUT+"log_g_"+Config.METAMODEL_NAME+"_"+ev+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_"+timeStamp2+".data.log";
    	}
    	timeStamp = timeStamp2;
    	buildFiles(fileOutName, fileOutDataName);    	
    	//End Log Files
    	
//    	final ui.Ui chart = new ui.Ui(Config.METAMODEL_NAME, ev+ " - " + timeStamp);
    	final ui.Ui ui = Ui.getInstance();
    	if (Config.VERBOSE_ON_UI){
    		subscribeParetoListener(ui.getChartTime());
    		ui.setTextSetting(Config.printPrettySetting());
     		SwingUtilities.invokeLater(new Runnable() {
    			@Override
    			public void run() {
    				ui.setVisible(true);
    			}
    		});
    	}
    	
    	
    	
    	LOGGER.config("Evaluator is "+evaluator.getClass().getSimpleName());
    	if(Config.VERBOSE_ON_UI)
    		ui.log("Evaluator is "+evaluator.getClass().getSimpleName());
    	G0.evaluate(evaluator);//Give marks to entities
    	
    	
    	for (Entity e : G0.entities) {
			if(e.getFitnessVector() == null)
				System.out.println("Evolutioner.evolutionate()l.459:e.getFitnessVector() == null");
		}
    	
    	
    	currentPopulation = G0;newCurrentPopulation = true;
    	long startTime = System.currentTimeMillis();
		int generation =0;
		int totalMutations =0;
		float[]  avgs =   new float[Config.NUMBER_OF_OBJECTIVES];
		double[] firsts = new double[Config.NUMBER_OF_OBJECTIVES];;
		avgGenes =0;
		float distanceAvg =0;
		Population G1 ;
		//TODO loop's start - Multi
    	while(generation++ < GENERATION_MAX && !endCondition(avgs, startTime)){
    		currentGeneration = generation;
    		long startTimeGeneration = System.currentTimeMillis();
	 		Entity[] sons2;
	 		int mutationGeneration = 0;
	 		Entity max = null;
	 		
	 		ArrayList<Entity> fathersRouletteExtended = getRouletteExtendedEntities(G0);
			while (max == null || G0.getEntities().size() < Population.NB_ENTITIES_IN_POP) {
				sons2 = crossover(fathersRouletteExtended);
				
				for (Entity e : sons2){
					mutationGeneration += mutation(e)?1:0;
					G0.addEntity(e);
					
					if(e.getFitnessVector() == null)
			    		evaluator.evaluateCoverage((ModelSet)e);
					
					if(max != null && max.getFitnessVector() == null)
			    		evaluator.evaluateCoverage((ModelSet)max);
					
					if(e.dominates(max))
						max = e;
				}
			}
			G0.addEntity(max);
			
			G0.fastNonDominantSort();
			G0.crowdingDistanceAssignement();
			
			G1 = new Population();
			G1.addEntity(G0.getBest());
			
			Pareto lastPareto = null, pr;
			for (int i = 0; i < G0.paretos.size(); i++) {
				pr = G0.getPareto(i);
				if(G1.getEntities().size() + pr.size() < Population.NB_ENTITIES_IN_POP/2){
					G1.addPareto(pr);
				} else {
					lastPareto = pr;
					break;
				}
			}
			//Cutting last pareto to get POP_SIZE/2 new population.
	 		Collections.sort(lastPareto.entitiesp, Entity.getDescendantRankComparator());
	 		
	 		del_B_Part(lastPareto.entitiesp, (Population.NB_ENTITIES_IN_POP/2)-G1.getEntities().size());
	 		G1.addPareto(lastPareto);
	 		
 		
	 		//Clean to relax memory ??
	 		G0.clean();
	 		G0 = G1;
	 		currentPopulation = G0;newCurrentPopulation = true;
	 		//G1 = null;
	 		System.gc();
	 		totalMutations += mutationGeneration;
	 		
	 		long endTime = System.currentTimeMillis();
	 		
	 		int nbEntities = G0.getFrontPareto().size();
 			float[] sums = new float[Config.NUMBER_OF_OBJECTIVES];
		 		avgs = new 	float[Config.NUMBER_OF_OBJECTIVES];
			 	firsts = new double[Config.NUMBER_OF_OBJECTIVES];
			 Entity maxE = null;
 			int 	sumGenes = 0;
 			int 	sumGenesSize = 0;
 					distanceAvg = 0;
 			int 	nbDist = 0;//Used to remove the boundaries (2*nb_objectives in fitFunc)
 			for (int i = 0; i < sums.length; i++) {
 				sums[i] = 0; 
 				avgs[i] = 0;
 				firsts[i] = 0;
 				for (Entity e : G0.getFrontPareto().getEntities()) {
 					sumGenes += e.getGenes().size();
 					for (Gene g : e.getGenes()) 
 						sumGenesSize += g.size();
 					double v = e.getFitnessVector().getValue(i);
 					
 					if(v > firsts[i]) firsts[i] = v;
 					
 					sums[i] += v;
 					if( e.getDistance() < (Integer.MAX_VALUE-1000) ){
 						nbDist++;
 						distanceAvg += e.getDistance();
 					}
 				}
 			}
 			for (int i = 0; i < sums.length; i++) 
 				avgs[i] = sums[i] / nbEntities;
 			
 			maxE = G0.getBest();
 			
 			avgGenes = sumGenes / (float)nbEntities;
 			avgGeneSize = sumGenesSize / (float)sumGenes;
 			distanceAvg = distanceAvg / nbDist;
 			
 			
 			/*
	 		 * LOG, FILE and UI
	 		 */
	 		if(generation % CHECK_POINT_GENERATIONS == 0 && generation != GENERATION_MAX){
	 			String log = 
		   		"\nEvolution ckeck point :"
		    			+"\n  "+generation+" generations"
		    			+"\n  Total #mutations :  "+totalMutations
		    			+"\n"+G0.printStatistics() //Here we call the sorting:paretos+distance crowding
		    			+"Time elapsed : "+Utils.formatMillis(System.currentTimeMillis()-startTime);
	 			
	 			log += "\n";
	 			evaluator.evaluateCoverage(maxE);
	 			
	 			ModelFragment[] mfsUn = new ModelFragment[evaluator.getUncovereds().size()];
	 			evaluator.getUncovereds().toArray(mfsUn);
	 			Arrays.sort(mfsUn, new Comparator<ModelFragment>() {
					@Override
					public int compare(ModelFragment o1, ModelFragment o2) {
						
						return o1.prettyPrint().compareTo(o2.prettyPrint());
					}
				});
	 			log += "\nUncovered fragments :\n";
	 			for (ModelFragment mfUn : mfsUn) {
					log += " - "+mfUn+"\n";
				}
	 			
	 			LOGGER.config(log);
	 			if(Config.VERBOSE_ON_UI){
		 			ui.log(log);
		 			ui.setResultingPop(G0);
	 			}
	 		}
		
 				
 				
		   	//LOGS
	 			if(LOGGER.isLoggable(Level.CONFIG)){
		 			String log2 = "G_"+generation+": \tobjs = ";
		 			log2 += maxE.printResult(",", "", "", true );
		 			
		 			double time = (endTime-startTimeGeneration);
	 				time = time /1000;
	 				String log = "\t "+G0.getParetos().size()+"[";
		 			for (Pareto p : G0.getParetos()) 
		 					log += (p.getEntities().size() + " ");
		 			
		 			log2 += log + "]";
		 			
		 			if(LOGGER.isLoggable(Level.FINE)){
		 				log =  log2+" " +" \tmut:"+mutationGeneration+" genes:"+Utils.format2Decimals(avgGenes) +" (duration:"+(time)+"s) "+ Model.nbModels+" models used";
		 				LOGGER.finer(log);
		 			}else {
		 				log =  log2 + " (duration:"+(time)+"s) "+ Model.nbModels+" models used";
		 				LOGGER.fine(log);
		 			}
		 			if(Config.VERBOSE_ON_UI)
		 				ui.log(log);
	 			}
 			//end LOGS

 			//FILE
 				logG_OnFile(bwData, generation, System.currentTimeMillis()-startTime, maxE, avgGenes, avgGeneSize);
 			//end FILE
 			//UI
 				logG_OnUi(generation, G0, maxE, avgs, avgGeneSize, avgGenes);
	 		//end UI
	 			
	   		/* * END LOGS n UI * */
    	}// End generation loop
    	
    	long timeSpent = System.currentTimeMillis()-startTime;
    	notifyEndToParetoListeners(timeSpent);
    	
    	String log = "\nEvolution ends."
    			+"\n  Evaluator is "+evaluator.getClass().getSimpleName()
    			
    			+"\n  "+generation+" generations"
    			+"\n  Total #mutations :  "+totalMutations
    			+"\n"+G0.printStatistics(true)
    			+"\n  Wrote into "+fileOutName  
    			+"\n  Time elapsed : "+Utils.formatMillis(System.currentTimeMillis()-startTime);
    	
    	LOGGER.info(log);
    	if(Config.VERBOSE_ON_UI){
    		ui.log(log);
    		ui.setResultingPop(G0);
    	}
    	

    	
    	if(Config.VERBOSE_ON_FILE && bw != null){
    		printResult(bw, evaluator, G0, avgGenes, System.currentTimeMillis() - startTime);
    		
    	}
    		
//		if(bwData != null)
//    		try {
//    			bwData.close();
//    		} catch (IOException e1) {
//    			e1.printStackTrace();
//    		}
		
		return G0;
	}
    
   public static void printResult(BufferedWriter bw, Evaluator evaluator, Population p, float avgGenes, long timePassed){
	   String timeSpent = Utils.formatMillis(timePassed);
	   try {
    			bw.write("  ** Partitionner **  ");
				bw.write("\n"+Config.printSetting(" ")+"");
				
				bw.write("\nResults :");
				bw.write("\n  "+Model.nbModels+" models used from '"+Config.DIR_INSTANCES+Config.METAMODEL_NAME+"'");
				bw.write("\n  Average number of models per ModelSet : "+avgGenes);
				bw.write("\n  Time elapsed : "+timeSpent);
				bw.write("\n\n"+p.printStatistics(true));
				
				Entity best = p.getBest();
				evaluator.evaluateCoverage(best);
				
				bw.write("\n\nBest pick in pareto front : "+best+":"+best.printResult("", "", ""+timeSpent,false)+"\n");
				FragmentSet fs = (FragmentSet)evaluator;
				String uncovereds = "Uncovereds ("+fs.getUncovereds().size()+") : {";
				for (ModelFragment mf : fs.getUncovereds()) 
					uncovereds += "\n   "+ mf + "";
				bw.write(uncovereds+"\n}\n\n");
				String fragments = "Fragments ("+fs.getFragments().size()+") : {";
				for (ModelFragment mf : fs.getFragments())
					fragments += "\n   "+ mf + "";
				bw.write(fragments+"\n}");
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
   }
   
   
    private void logG_OnUi(int generation, Population G0, Entity maxE,
			float[] avgs, float avgGeneSize, float avgGenes) {
    	Ui ui = Ui.getInstance();
    	if (Config.VERBOSE_ON_UI && ui != null){
	   		for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) 
	   			ui.addObjective(generation, avgs[i], maxE.getFitnessVector().getValue(i), i);
	   		
	   		ui.addToGraph2(generation, avgGeneSize);
	   		ui.addToGraph3(generation, avgGenes);
	   		ui.addToGraph4(generation, G0.getParetos().size());
	   		ui.addToGraph5(generation, G0.getFrontPareto().size());
	   		
 		}
    }

	private boolean endCondition(float[] avgs, long startTime) {
		boolean res = false;
		if(MAX_TIME > 0)
			res = System.currentTimeMillis()-startTime >= MAX_TIME;
    	switch (OBJECTIVES_END_CONDITION_TYPE) {
	   		case END_CONDITION_TYPE_NONE :
	   			return res;
	   		case END_CONDITION_TYPE_AND :
	   			boolean res2 = true;
				for (int i = 0; i < OBJECTIVES_END_CONDITION.length; i++) res2 &= avgs[i]>=OBJECTIVES_END_CONDITION[i];
				return res || res2;
			case END_CONDITION_TYPE_OR :
				for (int i = 0; i < OBJECTIVES_END_CONDITION.length; i++) 
					if(avgs[i]>=OBJECTIVES_END_CONDITION[i])
						res = true;
				return res;
    	}
    	return res;
    }
    
	private boolean mutation(Entity e) {
		boolean res = false;
		if ( Utils.getRandomDouble() < Population.MUTATE_RATE) {
			res = e.mutate();
			evaluator.evaluateCoverage((ModelSet)e);
		}
		if(e.fitnessVector == null)
			evaluator.evaluateCoverage((ModelSet)e);
		return res;
	}

	private Entity[] crossover(ArrayList<Entity> extendedFathers) {
		//Choix deux fathers à mixer
		//Démultiplier la liste des programs de G0 en fonction de leur Rate et taper random
		Entity[] fathers = this.getRandomFathers(extendedFathers);
        if (Utils.getRandomDouble() < Population.CROSSING_RATE /*&& (fathers[0].getGenes().size() > 2) && (fathers[1].getGenes().size() > 2)*/){   
        	Entity[] sons = fathers[0].crossover(fathers[1]);
    		for (Entity entity : sons) {
				if(entity.getFitnessVector() == null)
					evaluator.evaluateCoverage((ModelSet)entity);
			}
    		return sons;
        }
        return fathers;        
	}
	
	private ArrayList<Entity> getRouletteExtendedEntities(Population g0){
		ArrayList<Entity> extendFromRate = null;
		if(MONO_OBJECTIVE_ON)
			extendFromRate = g0.getEntities_proba_mono();
		else
			if(ROULETTE_GEOMETRIC && g0.getParetos().size() < 10)
				extendFromRate = g0.getEntities_proba_rank_geometric();//TODO GEO TRUC
			else
				extendFromRate = g0.getEntities_proba_rank();
		return extendFromRate;
	}
	
	private Entity[] getRandomFathers(ArrayList<Entity> fathers) {
		int i1 = Utils.getRandomInt(fathers.size());
		int i2 = Utils.getRandomInt(fathers.size());
		
		Entity[] res =  new Entity[] {
				fathers.get(i1),
				fathers.get(i2)
		};
		
		fathers = null;
		return res;
	}
	
    public List<Gene> get_B_Part_Crossover(ArrayList<Gene> genes, int crossoverPoint){
        return genes.subList(crossoverPoint, genes.size());
    }
    public void del_B_Part(ArrayList<?> genes, int crossoverPoint) {
        int times = genes.size()-crossoverPoint;
        
        for(int i=1; i<=times; i++)
        	genes.remove(crossoverPoint);
    }

	
	/**
	 * Cloning might be long, but is thread safe : consider using a separate thread.
	 * @return true if a change has been detected, false if not.
	 */
	public boolean updatePopulationCourante(){
		boolean res = newCurrentPopulation;
		if(newCurrentPopulation) 
			currentPopulation = currentPopulation.clone();
		newCurrentPopulation = false;
		return res;
	}
	
	public void notifyEndToParetoListeners(long timePassed) {
		updatePopulationCourante();
		for (ParetoListener pl : paretoListeners) {
			pl.notifyEnd(currentPopulation, currentGeneration, avgGenes, avgGeneSize, timePassed);
		}
	}
	
	/**
	 * Notifies listeners IFF there<s a change to notify.
	 * @param timePassed
	 */
	public void notifyParetoListeners(long timePassed) {
		 notifyParetoListeners(timePassed, false);
	}
	
	public void notifyParetoListeners(long timePassed, boolean forceNotification) {
//		System.out.println("Evolutioner.notifyParetoListeners("+Utils.formatMillis(timePassed)+")");
//		long start = System.currentTimeMillis();
//		System.out.println("Time to clone : "+Utils.formatMillis(System.currentTimeMillis() - start));
		if(forceNotification || updatePopulationCourante())
			for (ParetoListener pl : paretoListeners) 
				pl.notify(currentPopulation, currentGeneration, avgGenes, avgGeneSize, timePassed);
	}
	public boolean subscribeParetoListener(ParetoListener pl){
		if(paretoListeners.contains(pl))
			return false;
		return paretoListeners.add(pl);
	}
	public boolean unsubscribeParetoListener(ParetoListener pl){
		if(!paretoListeners.contains(pl))
			return false;
		return paretoListeners.remove(pl);
	}
	ArrayList<ParetoListener> paretoListeners = new ArrayList<>();

	public boolean isSuscribedParetoListener(ParetoListener pl) {
		return paretoListeners.contains(pl);
	}
}

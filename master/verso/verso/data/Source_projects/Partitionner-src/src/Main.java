import genetic.Entity;
import genetic.Evaluator;
import genetic.Evolutioner;
import genetic.ParetoListenerImpl;
import genetic.Population;
import genetic.fitness.FitnessVector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.logging.Logger;

import models.Model;
import models.ModelSet;
import partition.PartitionModel;
import partition.composition.AllPartitionsFragmentSet;
import partition.composition.FragmentSet;
import partition.ocl.OCLPartitionModel;
import utils.Config;
import utils.EvolutionTimeNotifierThread;
import utils.Utils;


public class Main {
	public final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	public static void main(String[] args) {
		if(args.length>0 && args[0].equals("--fragmentExtraction")){
			LOGGER.info("Entering Partitioner (extract partition).");
			extractPartition();
			System.exit(0);
		}
		

		LOGGER.info("Entering Partitioner.");
				Utils.init();	 
		//		File f = new File(Config.DIR_INSTANCES+"/"+Config.METAMODEL_NAME+"/model_9987.xmi");
		//		URI fileURIm = URI.createFileURI(f.getAbsolutePath());
		//		System.out.println(fileURIm);
		//		Resource resource =  Utils.resourceSet.createResource(fileURIm);
		//		Model m = new Model(resource);
				
				//Evaluer un MS :
				
//		testCoverage(new ModelSet(new File("R:/EclipseWS/material/tests/result_CD/")));
//		
////		testCoverage(new ModelSet(getModelSetFromList("R:/EclipseWS/material/tests/result_CD.txt")));
//		
//		extractResultFilesFromList("R:/EclipseWS/material/tests/result_CD/", "R:/EclipseWS/material/tests/result_CD.txt");
//		System.exit(1);
		
		int max = Config.NUMBER_OF_EXECUTIONS;
		if(Config.VERBOSE_ON_UI){
			LOGGER.warning("UI mode activated, number of executions is set to 1.");
			max = 1;
		}
		
		for (int i = 0; i < max; i++) {
			Population.nbPop = 0;//Multi execution concern.
			LOGGER.info("Run "+(i+1)+"/"+max+".");
			System.out.println();
			execute();
			LOGGER.info("End run "+(i+1)+"/"+max+". Sleeps .5 s");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		LOGGER.info("Exit.");

	}
	public static void extractPartition(){
		Utils.initMinimal_for_FragmentComputation();
		PartitionModel partitionModel;
		if(Config.FRAGMENT_WITH_OCL)
			partitionModel = new OCLPartitionModel();
		else
			partitionModel = new PartitionModel();
		
		partitionModel.extractPartition();
		
		
		System.out.println(partitionModel.printXML());
		System.out.println("Main.extractPartition() -- Exit !");
	}

	public static void extractResultFilesFromList(String newDir, String listFileName) {
		CopyOption co = StandardCopyOption.ATOMIC_MOVE;
		File fdir = new File(newDir);
		if(!fdir.exists())
			fdir.mkdir();
		else
			if(!(fdir.list().length == 0)) {
				LOGGER.warning("Target directory '"+newDir +"' is not empty.");
				LOGGER.warning("Erase it ?");
				String sbs = new Scanner(System.in).nextLine();
				switch (sbs.trim().toLowerCase()) {
				case "y":
				case "yes":
					fdir.delete();
					fdir.mkdir();
					LOGGER.config("Done. '"+newDir +"' is clean, empty.");
					break;
				case "n":
				case "no":
					LOGGER.warning("Replace existing files ?");
					sbs = new Scanner(System.in).nextLine();
					switch (sbs.trim().toLowerCase()) {
					case "y":
					case "yes":
						co = StandardCopyOption.REPLACE_EXISTING;
						LOGGER.config("Done.");
						break;
					case "n":
					case "no":
						LOGGER.config("Aborded.");
						System.exit(0);
						break;
					default:
						LOGGER.config("Aborded.");
						System.exit(0);
						break;
					}
					break;
				default:
					LOGGER.config("Aborded.");
					System.exit(0);
					break;
				}
			}
		
		boolean yestoall = false;
		int moved = 0;
		for (String string : getModelSetFromList(listFileName)) {
			File f = new File(string);
			Path source = Paths.get(f.getAbsolutePath());
			Path cible = Paths.get(newDir+f.getName());
			if(!yestoall)
				try {
					LOGGER.warning("Copy "+source+" to "+cible+".\n Ok ? <Yes> or <No> or <YesToAll>");
					String sbs = new Scanner(System.in).nextLine();
					
					switch (sbs.trim().toLowerCase()) {
					case "y":
					case "yes":
						Files.copy(source, cible, co);
						moved++;
						LOGGER.config("Done.");
						break;
					case "n":
					case "no":
						LOGGER.config("Aborded.");
						break;
					case "yestoall":
					case "yta":
						yestoall = true;
						Files.copy(source, cible, co);
						moved++;
						LOGGER.config("Done.");
						LOGGER.config("Going for all.");
						break;
	
					default:
						LOGGER.config("Aborded.");
						break;
					}
					
				} catch (Exception e) {
					LOGGER.config(source+" to "+cible+" -> Aborded. ("+e.getMessage()+")");
//					e.printStackTrace();
				}
			else {
				try {
					Files.copy(source, cible, co);
					moved++;
					LOGGER.config(source+" to "+cible+" -> Done.");
				} catch (IOException e) {
					LOGGER.config(source+" to "+cible+" -> Aborded.");
//					e.printStackTrace();
				}
				
			}
		}
		LOGGER.config(moved +" files moved into "+ newDir + " (contains "+fdir.listFiles().length+" files)");
	}

	public static void testCoverage(ModelSet ms){
//		ModelSet ms = new ModelSet(new File("R:/EclipseWS/tests/coverage/"+Config.METAMODEL_NAME));
		System.out.println(ms.prettyPrint());
		PartitionModel partitionModel;
		if(Config.FRAGMENT_WITH_OCL)
			partitionModel = new OCLPartitionModel();
		else
			partitionModel = new PartitionModel();
		
		partitionModel.extractPartition();
		
		
		FragmentSet fragmentSet = Config.loadFragmentSet(partitionModel);
		FitnessVector fv  = fragmentSet.evaluateCoverage(ms);
		System.out.println("   -> Coverage : "+fv.getCoverage());
	}
	
	public static ArrayList<String> getModelSetFromList(String listFileName){
		String s = "";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(listFileName));
		} catch (FileNotFoundException e) {
			LOGGER.severe("Could not open the list file '"+listFileName+"'");
			return null;
		}
		ArrayList<String> ss = new ArrayList<>();
		try {
			while((s = br.readLine()) != null)
				if(!s.isEmpty())
					ss.add(Config.DIR_INSTANCES+Config.METAMODEL_NAME+"/"+s.trim());
		} catch (IOException e) {
			LOGGER.severe("Error occured during the reading of the list file '"+listFileName+"'. Models might be missing.");
//			e.printStackTrace();
		}
		LOGGER.config(ss.size()+" models file name extracted from "+listFileName);
		return ss;
	}
	
	public static void execute(){
		
		PartitionModel partitionModel;
		if(Config.FRAGMENT_WITH_OCL)
			partitionModel = new OCLPartitionModel();
		else
			partitionModel = new PartitionModel();
		
		partitionModel.extractPartition();
		
		
		System.out.println(partitionModel.printXML());
		System.out.println("Main.execute() -- Exit !");
		System.exit(0);
		
		FragmentSet fragmentSet = Config.loadFragmentSet(partitionModel);
		
		
		
		
		Population pop = Population.createRandomPopulation(Utils.resourceSet);
		
		
		Evolutioner evo = new Evolutioner(fragmentSet, pop);
//		ParetoListenerImpl plImpl = new ParetoListenerImpl(evo);
//		evo.subscribeParetoListener(plImpl);
		
		
		EvolutionTimeNotifierThread timeStampThread = new EvolutionTimeNotifierThread(Evolutioner.CHECK_POINT_TIME, evo);	
		
		long startTime = System.currentTimeMillis();
		Population p = evo.evolutionate();
//		evo.unsubscribeParetoListener(plImpl);
		
		
		timeStampThread.end();
		
		
		String elapsedTime = Utils.formatMillis(System.currentTimeMillis()-startTime);
		
		
		Entity max = p.getBest();
		LOGGER.warning("Seed reminder : "+Config.SEED);
		LOGGER.info("Result = "+max.getFitnessVector());
		
		String ev = evo.getEvaluator().getClass().getSimpleName();
		if(ev.endsWith("FragmentSet")) ev = ev.substring(0, ev.indexOf("FragmentSet"));
		String fileOutName = Config.DIR_NUM+""+Config.METAMODEL_NAME+"_"+ev+"_"+Config.DIS_OR_MIN+"_"+Evolutioner.GENERATION_MAX+"_"+Population.NB_ENTITIES_IN_POP+"_"+Population.NB_GENES_IN_ENTITIES+"_res.res";
		File f = new File(fileOutName);
		
		Utils.storeNumericResults(f, max, evo.getLastTimeStamp(), elapsedTime);
		
		if(Config.STORE_JESS_RESULT)
			p.storeFrontParetoInFileSystem(new File(Config.DIR_SOL), evo.getLastTimeStamp(), Config.STORE_JESS_RESULT);
		
		p = null;
		pop = null;
		evo = null;
		partitionModel = null;
		max = null;
		Model.clean();
		System.gc();
		
	}

}

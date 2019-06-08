import genetic.Entity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import partition.PartitionModel;
import partition.composition.FragmentSet;
import partition.ocl.OCLPartitionModel;

import models.Model;
import models.ModelSet;
import utils.Config;
import utils.Utils;

public class MainTest {
	
	
	
	public static void main(String[] args) {
		Utils.init();
//		System.out.println(ms.getFitnessVector());"log_mono_atl2_AllRanges_801_40_20_20151016-1834.log"
		
		
		
//		getTimeAndSizeFromLog("log_mono_feature_AllRanges_6_40_20_20151023-1803-2.log");
//		getTimeAndSizeFromLog("log_mono_atl2_AllRanges_801_40_20_20151017-1806-2.log");
		
		System.out.println("MainTest.main()");
//		File out = new File(Config.DIR_TESTS+"test.clp");
//
//		int nb_instances = new File(Config.DIR_INSTANCES+"/"+Config.METAMODEL_NAME).listFiles().length;
//		boolean printInTestFile = true;
//		boolean printInConsole = false;
		
//		printJessInstances(out, nb_instances, printInTestFile, printInConsole);
		
		
	
		//Groups TEST
//		int nbSets = 200;
//		int tailleMin = 2;
//		int tailleMax = 100;
//		int nbGroups = 10;
//		System.out.println("Generating groups : ");
//		System.out.println("  " + nbSets + " sets");		
//		System.out.println("  " + nbGroups + " groups");		
//		System.out.println("  size [" + tailleMin + ", "+tailleMax + "] (models)");		
//		ArrayList<ModelSet>[] groups = generateCoverageLevelGroups(nbSets, tailleMin, tailleMax, nbGroups, false);
//		
////		System.out.println();
//		//Pick the first one of each groupe to Jessify it.
//		int i=0;
//		File f;
//		BufferedWriter bw = null;
//		boolean verbose = false;
//		for (ArrayList<ModelSet> arrayList : groups) {
//			System.out.print("Group " + i++ + " : "+ arrayList.size() + " solutions");
//			
//			double cov = 0.0;
//			int nbModels = 0;
//			int nbObjects = 0;
//			int j = 0;
//			for (ModelSet modelSet : arrayList) {
//				if(j++ ==0){
//					f = new File(Config.DIR_TESTS+File.separator+modelSet.getName()+".cpl");
//					try {
//						bw = new BufferedWriter(new FileWriter(f));
//						bw.append(Utils.printJessMetamodel()+"\n");
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					
//					Model[] tmpModels = new Model[modelSet.size()];
//					modelSet.getModels().toArray(tmpModels);
//					
//					Arrays.sort(tmpModels, new Comparator<Model>() {
//						@Override
//						public int compare(Model o1, Model o2) {
//							return o1.size() - o2.size();
//						}
//					});
//					
//					for (Model m : tmpModels) {
//						
//						String s = m.jessification(false);
//						try {
//							bw.append(s+"\n");
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//					try {
//						bw.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//				
//				cov += modelSet.getFitnessVector().getCoverage();
//				nbModels += modelSet.size();
//				nbObjects += modelSet.sizeRefined();
//				
//				if(verbose)
//					System.out.println(" "+ j +". "+modelSet+" "+modelSet.printStats()+ " : "+modelSet.getFitnessVector().getCoverage());
//			}
//			
//			if(arrayList.size() != 0){
//				cov = cov / arrayList.size();
//				nbModels = nbModels / arrayList.size();
//				nbObjects = nbObjects / arrayList.size();
//			}
//			if(verbose)
//				System.out.println("");
//			System.out.println((arrayList.size()!=0)?"  AVG: cov="+Utils.format2Decimals((float)cov)+" #m="+nbModels+" #o="+nbObjects:"");
//		}
		//End first of group jessification
//		//End Group Test
		
		
		
//		//Testing Jessification
		Model m = ModelSet.loadModel(3208);
//		System.out.println(m.getClassProperties());
//		System.out.println(m.prettyPrint());
//		System.out.println(m.jessification());
		
		String dir = Config.DIR_INSTANCES+Config.METAMODEL_NAME+File.separator;
		System.out.println(dir);	
		
		int nbModels = new File(dir).listFiles().length;
		
		try {
			File f = new File(Config.DIR_TESTS+"/tmp.tmp");
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			for (int j = 0; j < 100; j++) {
				bw.append(ModelSet.loadModel(Utils.getRandomInt(nbModels/100)).jessification(j==0) +"\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		
//		printPartitions();
		
		
//		//Testing ModelSet load
//		String dir = Config.DIR_INSTANCES+Config.METAMODEL_NAME+File.separator;
//		System.out.println(dir);	
//		
//		String names = "";
//		int nbModels = new File(dir).listFiles().length;
//		for (int j = 0; j < 100; j++) {
//			int index = Utils.getRandomInt(0, nbModels-1);
//			names += "model_"+((index<10)?"0000":(index<100)?"000":(index<1000)?"00":(index<10000)?"0":"") +index+".xmi,";
//		}
//		
//		ArrayList<String>  listNames = Utils.extractModelsFromList(Config.DIR_INSTANCES+Config.METAMODEL_NAME+File.separator, 
//				names,
////				"model_0"+Utils.getRandomInt(1000, 9000)+".xmi,"+
////				"model_0"+Utils.getRandomInt(1000, 9000)+".xmi,"+
////				"model_0"+Utils.getRandomInt(1000, 9000)+".xmi",				
//				",");
//		
//		System.out.println("MainTest.main()");
//		ModelSet ms = new ModelSet(listNames);
//		System.out.println(ms);
//		PartitionModel partitionModel;
//		if(Config.FRAGMENT_WITH_OCL)
//			partitionModel = new OCLPartitionModel();
//		else
//			partitionModel = new PartitionModel();
//		partitionModel.extractPartition();
//		FragmentSet evaluator = Config.loadFragmentSet(partitionModel);
//		ms.setFitnessVector(evaluator.evaluateCoverage(ms));
		
		
		System.out.println("Exit.");
	}

	public static void printJessInstances(File fileout, int nbInstances, boolean printInTestFile, boolean printInConsole) {
		BufferedWriter bw = null;
		try {
			if(printInTestFile){
				System.out.println("Writing "+nbInstances+" instances in '"+fileout.getAbsolutePath()+"'");
				bw = new BufferedWriter(new FileWriter(fileout));
				bw.append(Model.printJessMetamodel(true)+"\n");
			}
		} catch (IOException e) {e.printStackTrace();}
		
		
		for (int i = 0; i < nbInstances; i++) {
			String jess = pickModelAndConvertToJess(i);
			if(printInConsole)
				System.out.println(jess);
			try {
				if(printInTestFile)
					bw.append(jess+"\n");
			} catch (IOException e) {e.printStackTrace();}
		}
		
		try {
			if(printInTestFile)
				bw.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	 
	public static String printPartitions(){
		System.out.println("MainTest.printPartitions()");
		PartitionModel partitionModel;
		if(Config.FRAGMENT_WITH_OCL)
			partitionModel = new OCLPartitionModel();
		else
			partitionModel = new PartitionModel();
		
		partitionModel.extractPartition();
		FragmentSet fragmentSet = Config.loadFragmentSet(partitionModel);
		System.out.println(fragmentSet.prettyPrint());
		System.out.println();
		System.out.println(partitionModel.prettyPrint());
		
		return partitionModel.prettyPrint();
	}

	public static String pickRandomModelAndConvertToJess(){
		return pickModelAndConvertToJess(Utils.getRandomInt(new File(Config.DIR_INSTANCES+"/"+Config.METAMODEL_NAME).listFiles().length));
	}
	public static String pickModelAndConvertToJess(int idModel){
		Model m = ModelSet.loadModel(idModel);
		return m.jessification(false);
	}
	
	public static ArrayList<ModelSet>[] generateCoverageLevelGroups(int nbSets, int tailleMin, int tailleMax, int nbGroups, boolean verbose) {
		if(verbose)
			System.out.println("MainTest.generateCoverageLevelGroups()");
		
		PartitionModel partitionModel;
		if(Config.FRAGMENT_WITH_OCL)
			partitionModel = new OCLPartitionModel();
		else
			partitionModel = new PartitionModel();
		partitionModel.extractPartition();
		FragmentSet evaluator = Config.loadFragmentSet(partitionModel);
		
		
		boolean printGroupsDetails = false;
		double minCOV = 2.0, maxCOV = -1.0;
		
		ArrayList<ModelSet> mss = new ArrayList<ModelSet>();
		
		float sumSize = 0;
		
		for (int j = 0; j < nbSets; j++) {
			
			ModelSet ms = new ModelSet();
			int nbModels = new File(Config.DIR_INSTANCES+Config.METAMODEL_NAME).listFiles().length;
			int iMax = Utils.getRandomInt(tailleMin, tailleMax);
			for (int i = 0; i < iMax ; i++) {
				ms.addModel(ModelSet.loadModel(Utils.getRandomInt(nbModels)));
			}
			sumSize += ms.size();
//			System.out.println("MS size : "+ms.size() + " / avg = " + (sumSize/(j+1)));
			
			ms.setFitnessVector(evaluator.evaluateCoverage(ms));
			double cov = ms.getFitnessVector().getCoverage();
			if(cov != 0){
				if(cov < minCOV)
					minCOV = cov;
				if(cov > maxCOV)
					maxCOV = cov;
				
				mss.add(ms);
			}
			if(verbose)
				System.out.println(" "+j+". "+ms+" "+ms.printStats()+((cov == 0)?" x ":""));
		}
		Collections.sort(mss, new Comparator<ModelSet>() {
			@Override
			public int compare(ModelSet o1, ModelSet o2) {
				return -Double.compare(o2.getFitnessVector().getCoverage(), o1.getFitnessVector().getCoverage());
			}
		});
		if(verbose)
			System.out.println();
		
		
		ArrayList<ModelSet>[] groups = new ArrayList[nbGroups];
		double values[] = new double[groups.length];
		System.out.println("values : ");
		for (int i = 0; i < values.length; i++) {
			values[i] = minCOV  + ((maxCOV-minCOV)/groups.length)*(i+1);
			if(verbose)
				System.out.print(values[i] + ", ");
			groups[i] = new ArrayList<>();
		}
		if(verbose)
			System.out.println();
		
		for (int i = 0; i < mss.size(); i++) {
			ModelSet ms = mss.get(i);
			double cov = ms.getFitnessVector().getCoverage();
			for (int j = 0; j < values.length; j++) {
				if(cov <= values[j]){
					groups[j].add(ms);
					break;
				}
			}
			if(verbose)
				System.out.println(ms+" "+ms.printStats());
		}
		
		if(verbose)
			System.out.println("\nGroups : ");
		double botom = minCOV;
		for (int i = 0; i < groups.length; i++) {
			if(verbose)
				System.out.println("  o ["+botom+", "+values[i]+"]" + groups[i].size());
			if(printGroupsDetails)
				if(verbose)
					for (ModelSet ms : groups[i]) 
						System.out.println("    "+ms+" "+ms.printStats());
				
			botom = values[i];
		}
		return groups;
	}
	
	/**
	 * 
	 * Need
	 *  - DIR_OUT
	 *  - DIR_INSTANCES
	 *  - METAMODEL_NAME
	 * 
	 * @param fileName (prefixed with DIR_OUT)
	 * @return ;[timeElapsed];[avgNbClassesinstantiated];[avgNbProperties]
	 */
	static String getTimeAndSizeFromLog(String fileName){
		String res = "";
		
//		File logFile = new File("R:/Eclipse/Partitionner/out/log_atl2_AllRanges_1000_30_20_20150629-1640.log");
//		ArrayList<ArrayList<String>> fileNames = Utils.extractModelsFromLogFile(logFile);
		ArrayList<String> listNames = new ArrayList<>();
		
		String l = "", listModels = "", timeElapsed = "", checkRates = "";
		
		File f = new File(Config.DIR_OUT+fileName);
		
		System.out.println("Reading '"+f.getAbsolutePath()+"'...\t");
		try {
			Pattern p=Pattern.compile("\\{.*\\}");
			Scanner scanner = new Scanner(f);
            while (scanner.hasNextLine())      { 
               l = scanner.nextLine();
               if(l.trim().startsWith("Time")){//Time elapsed : 5:28:19:125
					Matcher ma=Pattern.compile("\\:.*").matcher(l);
					 while(ma.find()) 
						 timeElapsed = ma.group().substring(1, ma.group().length()-1).trim();
				}
               if(l.trim().startsWith("Best")){//Time elapsed : 5:28:19:125
					Matcher ma=Pattern.compile("Best\\:[\\S]*,{1}").matcher(l);
					 while(ma.find()) 
						 checkRates = ma.group().substring("Best:".length(), ma.group().length()-1).trim();
				}
             
				if(l.trim().startsWith("Best")){//Best:0.89...8,0.96...4,10   (MS15386:10 Models):(COV:89,DIS:96) rd(0,0)	   10{model_00581.xmi,mo[...]8551.xmi}
					Matcher ma=p.matcher(l);
					 while(ma.find()) 
			            listModels = ma.group().substring(1, ma.group().length()-1);
				}
			}
            scanner.close();
            listNames = Utils.extractModelsFromList(Config.DIR_INSTANCES+Config.METAMODEL_NAME+File.separator, 
    				listModels				
    				,",");

    		ModelSet ms = new ModelSet(listNames);
    		int totalClasses = 0, totalProperties = 0;
    		for (Model m : ms.getModels()) {
    			totalClasses += m.getNbClasses();
    			totalProperties += m.getNbProperties();
    		}
    		float avgClasses = (float)totalClasses / ms.size();
    		float avgProperties = (float)totalProperties / ms.size();
    		res= ";"+timeElapsed+";"+avgClasses+";"+avgProperties;
    		System.out.println("   Average model size : "+checkRates+"..\t"+res);
		} catch (IOException e) {
			System.out.println("WARNING : Could not process file '"+f.getAbsolutePath()+"'");
//			e.printStackTrace();
		}
		
		
		

		return res;
	}
	
	static int countLOC() throws IOException{
		return countLOC(new File("./src"));
	}
	static int countLOC(File f) throws IOException {
		int res = 0;
		if(f.isDirectory()){
			for (File f2 : f.listFiles()) {
				res += countLOC(f2);
			}
			System.out.println("Dir:"+f.getCanonicalPath()+" : "+res);
		} else {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = "";
			while((line = br.readLine()) != null){
				if(!line.isEmpty())
					res++;
			}
			System.out.println(f.getCanonicalPath()+" : "+res);
		}
		return res;
	}
}

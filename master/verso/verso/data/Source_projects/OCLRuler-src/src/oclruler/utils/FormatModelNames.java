package oclruler.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

import oclruler.genetics.Evaluator.GRAIN;
import oclruler.genetics.EvaluatorOCL;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.FireMap;
import oclruler.metamodel.Model;
import oclruler.rule.Program;

public class FormatModelNames {
	public final static Logger LOGGER = Logger.getLogger(FormatModelNames.class.getName());
	
	public void test_renameModelsByFireCount(){
		String modelRepoName = "R:/EclipseWS/material/instances/_new/bpmn";
		String constraintsFileName = "R:/EclipseWS/material/OCL_Ruler/instances/bpmn/OCLexpressions.ocl";
		
		renameModelsByFireCount(modelRepoName, constraintsFileName);

	}
	
	public static void main(String[] args) throws Exception{
		LOGGER.info("Entering FormatModelName (OCLRuler)");
			
		ToolBox.loadConfig();	

		String metamodelName = Config.METAMODEL_NAME;
		if(args.length >0)
			metamodelName = args[0];
		
		String modelRepoName = "R:/EclipseWS/material/instances/__new/"+metamodelName;
//		String modelRepoName = "R:/EclipseWS/material/OCL_Ruler/examples/"+Config.METAMODEL_NAME+"/negatives/";
		renameModelsBySize(modelRepoName);
		System.out.println("Safe exit.");
	}
	
	
	public static void renameModelsBySize(String directory) throws IOException {
		
		
		File instancesDirectory = new File(directory);
		
		System.out.println(instancesDirectory.getAbsolutePath());
		
		int nbFiles = ToolBox.listXMIFiles(instancesDirectory).length;
		System.out.println("Files to move : "+nbFiles);
		System.out.println("Source repository : "+instancesDirectory.getAbsolutePath());
		
	    System.out.println("Allright ? o/n");
	    String on =  new BufferedReader(new InputStreamReader(System.in)).readLine();
	    if(on.trim().equalsIgnoreCase("o")){
			String newDir = instancesDirectory.getAbsolutePath()+File.separator+"jsh"+ToolBox.getRandomInt(1000)+"dhJK";
			File newDirFile = new File(newDir);
			if(newDirFile.exists())
				newDirFile.delete();
			
			newDirFile.mkdir();
	    	System.out.println("First number : ");
		    int first =  Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
	    	System.out.println("Sufix : ");
		    String sufix =  new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
		    if(sufix.length() == 0)
		    	sufix = null;
	    	
		    System.out.println("C'est parti... ");
	    	File flist[] = Arrays.copyOf(ToolBox.listXMIFiles(instancesDirectory), ToolBox.listXMIFiles(instancesDirectory).length);
	    	System.out.println("Ordering files by size...");
	    	
	    	Arrays.sort(flist, new Comparator<File>() {
				@Override
				public int compare(File arg0, File arg1) {
					if(arg0.isDirectory())
						return Integer.MAX_VALUE;
					if(arg1.isDirectory())
						return Integer.MIN_VALUE;
					return (int)(arg0.length() - arg1.length());
				}
			});
	    	
	    	
	    	System.out.println("Copying files to tmp folder...");
	    	int percent10 = 0;
			for (int i = 0; i < flist.length; i++) {
				File f = flist[i-0];
				if(!f.isDirectory()){
					
					int ii = i+first;
					String newName = "model_"+(((ii<10)?"0000":(ii<100)?"000":(ii<1000)?"00":(ii<10000)?"0":"") +ii) + (sufix != null ? "-"+sufix:"") + ".xmi";

					Path source = Paths.get(f.getAbsolutePath());
					Path cible = Paths.get(newDir+File.separator+newName);
					try {
//						System.out.println("From "+source+" to "+cible);
						Files.move(source, cible);
					} catch (Exception e) {
						e.printStackTrace();
						
					}
					f.delete();
				}
				try {
					if(i%(nbFiles/10)==0 && i!= 0)
						System.out.println(++percent10 + "0% processed.");
				} catch(ArithmeticException ae){}
			}
			
			
			
			File flist2[] = Arrays.copyOf(newDirFile.listFiles(), newDirFile.listFiles().length);
			System.out.println("Copying files back to instance directory...");
			for (int i = 0; i < flist2.length; i++) {
				File f = flist2[i];
				
				if(f != null ){
					Path source = Paths.get(f.getAbsolutePath());
					Path cible = Paths.get(instancesDirectory.getAbsolutePath()+File.separator+f.getName());
					try {
//						System.out.println("From "+source+" to "+cible);
						Files.move(source, cible, StandardCopyOption.REPLACE_EXISTING);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			ToolBox.deleteFolder(newDirFile);
			System.out.println("Done !");
	    } else 
	    	System.out.println("Exit.");
	}
	/**
	 * Take all models in the repository which name is passed as first parameter <code>modelRepoName</code> and evaluate each one using the constraints included as ocl files in the directory which name is passed as second parameter <code>constraintsFileName</code>.
	 * <code>constraintsFileName</code> can be the name of a directory : it will be visited recursively, taking account of each .ocl file included in its tree.
	 * @param modelRepoName Directory containing instances to order
	 * @param constraintsFileName File (or directory) containing the constraints used to order the instances
	 */
		@SuppressWarnings("resource")
		private static void renameModelsByFireCount(String modelRepoName, String constraintsFileName) {
			File modelRepo = new File(modelRepoName);
			File constraintsFile = new File(constraintsFileName);
			//Take examples from directory
			
			int nbFiles = modelRepo.listFiles().length;
			LOGGER.info("Files to move : "+nbFiles);
			LOGGER.info("Source repository : "+modelRepo.getAbsolutePath());
			
			LOGGER.info("Allright ? y/n");
			String on =  new Scanner(System.in).nextLine();
		    if(on.trim().equalsIgnoreCase("y") || on.trim().equalsIgnoreCase("Yes")){
			
				String newDir = modelRepo.getAbsolutePath()+File.separator+"jsh"+ToolBox.getRandomInt(10000)+"dhJK";
				File newDirFile = new File(newDir);
				if(newDirFile.exists())
					newDirFile.delete();
				newDirFile.mkdir();
				newDirFile.deleteOnExit();
				LOGGER.info("Ordering files by fire count...");
				ArrayList<Model> ms = orderModelsByFireCOunt(modelRepo, constraintsFile);
				
				LOGGER.info("Rename files ordered by fire count ? y/n");
				on =  new Scanner(System.in).nextLine();
			    if(on.trim().equalsIgnoreCase("y") || on.trim().equalsIgnoreCase("Yes")){
		    	
			    	LOGGER.info("Copying files to tmp folder...");
			    	int percent10 = 0;
					for (int i = 0; i < ms.size(); i++) {
							
						int ii = i;
						String newName = "model_"+(((ii<10)?"0000":(ii<100)?"000":(ii<1000)?"00":(ii<10000)?"0":"") +ii) + ".xmi";
		
						Path source = Paths.get(ms.get(i).getFile().getAbsolutePath());
						Path cible = Paths.get(newDir+File.separator+newName);
						try {
							LOGGER.finer("From "+source+" to "+cible+".");
							Files.move(source, cible);
						} catch (Exception e) {
							e.printStackTrace();
						}
						ms.get(i).getFile().delete();
						if(nbFiles > 100 && i%(nbFiles/10)==0 && i!= 0)
							LOGGER.info(++percent10 + "0% processed.");
					}
					File flist2[] = Arrays.copyOf(newDirFile.listFiles(), newDirFile.listFiles().length);
					LOGGER.info("Copying files back to instance directory...");
					for (int i = 0; i < flist2.length; i++) {
						File f = flist2[i];
						
						if(f != null ){
							Path source = Paths.get(f.getAbsolutePath());
							Path cible = Paths.get(modelRepo.getAbsolutePath()+File.separator+f.getName());
							try {
								LOGGER.finer("From "+source+" to "+cible);
								Files.move(source, cible, StandardCopyOption.REPLACE_EXISTING);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					
				    
					LOGGER.info("Done !");
					LOGGER.info("Results in : "+modelRepo.getAbsolutePath());
			    } else 
			    	LOGGER.info("Exit.");
		    } else 
		    	LOGGER.info("Exit.");
		}
	public static ArrayList<Model> orderModelsByFireCOunt(File modelRepo, File constraintsFile) {
		Program prg = Program.loadProgramFromFile(constraintsFile);
		ArrayList<Model> res = new ArrayList<>();
		HashMap<Integer, ArrayList<Model>> models = new HashMap<>();
		FireMap fmres = new FireMap();
		long t = System.currentTimeMillis();
		LOGGER.info(" Loading models from '"+modelRepo.getAbsolutePath()+"'");
		ExampleSet ms = new ExampleSet(modelRepo);
		LOGGER.info(" Loaded "+ms.size()+" models in "+((float)(System.currentTimeMillis()- t)/1000)+"s");
		t = System.currentTimeMillis();
		LOGGER.info(" Evaluating "+ms.size()+" models.");
		EvaluatorOCL.EXECUTION_GRAIN = GRAIN.FINE;
		int iMax = -1;
		for (Model model : ExampleSet.getExamplesBeingUsed()) {
			FireMap fm = EvaluatorOCL.execute(fmres, model, prg);
			int f = fm.getNumberOfFires(model);
			if(f > iMax )
				iMax = f;
			if(models.get(f) == null )
				models.put(f, new ArrayList<>());
			models.get(f).add(model);
		}
		LOGGER.info(" Evaluated "+ms.size()+" models in "+((float)(System.currentTimeMillis()- t)/1000)+"s");
		LOGGER.info("Result : ");
		for (int i = 0 ; i <= iMax ; i++) {
			ArrayList<Model> mss = models.get(i);
			if(mss != null) {
				LOGGER.info( "  " + i + " fires : "+mss.size()+" -> "+mss);
				for (Model model : mss) {
					res.add(model);
				}
			}
		}
		return res;
	}

}

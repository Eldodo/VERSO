package oclruler.metrics;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;

import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * Copy files into "Recollection" folder.
 * @author Edouard Batot - batotedo@iro.umontreal.ca
 *
 */
public class ResultCollection {
	public static String SUFFIX_FEATURES_ADDED = null;
	public static String SUFFIX_CONDENSED = null;
	public static String OUTPUT_EXTENSION = ".csv";
	
	static String dirExperimentPath = null;
	
	
	public static void main(String[] args) {
		System.out.println("Entering ResultCollection ");
		
		
		String timeStamp = null;//"20171102";
		dirExperimentPath = (Config.SINBAD? "/u/batotedo/":"R:/")+"EclipseWS/material/OCL_Ruler/experiment";

		
		ToolBox.initMinimal();
		SUFFIX_FEATURES_ADDED = Config.getStringParam("SUFFIX_FEATURES_ADDED");
		System.out.println("SUFFIX_FEATURES_ADDED="+SUFFIX_FEATURES_ADDED);
		SUFFIX_CONDENSED = Config.getStringParam("SUFFIX_CONDENSED");
		System.out.println("SUFFIX_CONDENSED="+SUFFIX_CONDENSED);
		
		
		if(SUFFIX_CONDENSED == null)
			System.out.println("SUFFIX_CONDENSED REQUIRED !");
		if(SUFFIX_FEATURES_ADDED == null)
			System.out.println("SUFFIX_FEATURES_ADDED REQUIRED !");
		if(SUFFIX_CONDENSED == null || SUFFIX_FEATURES_ADDED == null){
			System.out.println("Exit !");
			System.exit(1);
		}
		
		// Initialization starts
		Config.EXPERIMENT_MODE = true;

		dirExperimentPath = (Config.SINBAD ? "/u/batotedo/" : "R:/") + "EclipseWS/material/OCL_Ruler/experiment";
		System.out.println("Target directory : '" + dirExperimentPath + "'\n");
		// File dirExperiment = Paths.get(dirExperimentPath).toFile();

		File dirRecollection = Paths.get(dirExperimentPath, "Recollection").toFile();
		if (!dirRecollection.exists())
			dirRecollection.mkdir();

		File[] directories = new File[] { 
				Paths.get(dirExperimentPath, "Classic").toFile(), 
				Paths.get(dirExperimentPath, "Incremental").toFile(), 
			};
		for (File file : directories) {
			System.out.println("Traitement : "+file.getAbsolutePath());
			for (File resFile : getResultFiles(file, timeStamp)) {
				
				String name = recollectionRenaming(resFile, SUFFIX_FEATURES_ADDED);
				System.out.println(name + " \tfrom '" + resFile.getAbsolutePath() + "'");
				ToolBox.copyFile(resFile, dirRecollection, name);
			}
			for (File resFile_c : getResultFiles_condensed(file, timeStamp)) {
				String name = recollectionRenaming(resFile_c, SUFFIX_CONDENSED);
				System.out.println(name + " \tfrom '" + resFile_c.getAbsolutePath() + "'");
				ToolBox.copyFile(resFile_c, dirRecollection, name);
			}
			System.out.println();
		}
	}

	public static String recollectionRenaming(File resFile, String suffix) {
		String path = resFile.getAbsolutePath();
		String name = path.substring(0, path.length() - MetricExtraction.RESULTS_FILE_NAME.length());
		name = name.substring(name.indexOf("experiment") + "experiment".length() + 1).replace("\\", "_");
		name = name.substring(0, name.lastIndexOf("_result"));
		name = name.substring(0, name.lastIndexOf("__"));
		name += "_" + suffix + OUTPUT_EXTENSION;
		return name;
	}
	
	public static HashSet<File> getResultFiles(File f, String timeStamp){
		HashSet<File> res = new HashSet<>();
		File[] files = ToolBox.listFiles(f);
		for (File file : files) {
			String pattern = "results_"+(SUFFIX_FEATURES_ADDED!=null ? SUFFIX_FEATURES_ADDED:"")+(timeStamp!=null ? timeStamp:"");
			if(file.getName().startsWith(pattern)){
				res.add(file);
			}
		}
		
		for (File file : ToolBox.listDirecories(f)) {
			if(!file.getName().endsWith("examples")) // Avoid useless examples directory exploration (they dont contain any result)
			res.addAll(getResultFiles(file, timeStamp));
		}
		return res;
	}
	public static HashSet<File> getResultFiles_condensed(File f, String timeStamp){
		HashSet<File> res = new HashSet<>();
		File[] files = ToolBox.listFiles(f);
		for (File file : files) {
			String pattern = "results_"+SUFFIX_CONDENSED+(timeStamp!=null ? timeStamp:"");
			if(file.getName().startsWith(pattern)){
				res.add(file);
			}
		}
		for (File file : ToolBox.listDirecories(f)) {
			if(!file.getName().endsWith("examples")) // Avoid useless examples directory exploration (they dont contain any result)
			res.addAll(getResultFiles_condensed(file, timeStamp));
		}
		return res;
	}
}

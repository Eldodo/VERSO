package oclruler.metrics;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;

import oclruler.utils.Config;
import oclruler.utils.ToolBox;

public class CleanFeaturedResults {
	
	
	public static void main(String[] args) {
		String dirExperimentPath = (Config.SINBAD? "/u/batotedo/":"R:/")+"EclipseWS/material/OCL_Ruler/experiment";
		HashSet<File> featuredFiles = ResultCollection.getResultFiles(new File(dirExperimentPath), null);
		System.out.println(featuredFiles.size()+ " featured result files found. ");
		for (File file : featuredFiles) {
			System.out.println(file.getAbsolutePath());
		}
		System.out.println("Delete them ?");
		@SuppressWarnings("resource")
		String res = new Scanner(System.in).nextLine();
		if(res.equals("yes")){
			for (File file : featuredFiles) {
				file.delete();
			}
			System.out.println("Deleted !");
		} else {
			System.out.println("Aborded by user");
		}
		
		
	}
	
	public static HashSet<File> getResultFiles(File f, String timeStamp){
		HashSet<File> res = new HashSet<>();
		File[] files = ToolBox.listFiles(f);
		for (File file : files) {
			if(file.getName().startsWith("results_")){
				res.add(file);
			}
		}
		
		for (File file : ToolBox.listDirecories(f)) {
			if(!file.getName().endsWith("examples"))
			res.addAll(getResultFiles(file, timeStamp));
		}
		return res;
	}

}

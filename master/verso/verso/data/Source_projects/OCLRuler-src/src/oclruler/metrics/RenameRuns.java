package oclruler.metrics;

import static oclruler.metrics.MetricExtraction.RESULTS_FILE_NAME;
import static oclruler.metrics.MetricExtraction.RUN_FOLDERS_PREFIX;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * Stand alone class to refactor runs names in accordance with CSV lines.
 * @author batotedo
 *
 */
public class RenameRuns {
	
	static enum METAMODEL {
		Family,
		Bank,
		statemachine;
	}
	
	static enum AUGMENTATION_TYPE {
		Classic,
		Incremental;
	}
	
	/**
	 * e
	 * @param args [Family, Classic, 2, official] or [2, official] on all Experiment type / metamodel
	 */
	public static void main(String[] args) {
		System.out.println("Entering ResultCollection ");
		System.out.println("args: "+Arrays.toString(args));
		
		String dirExperimentPath = (Config.SINBAD? "/u/batotedo/":"R:/")+"EclipseWS/material/OCL_Ruler/experiment";
		
		if(args.length == 4){
			String mmName = args[0];
			String expType = args[1];
			int expNumberMax = Integer.parseInt(args[2]);
			String leafFolder = args[3];
			sortAndSortResultFileAndRunFolders(dirExperimentPath, expType, mmName, expNumberMax, leafFolder);
			
		} else if(args.length == 2 ){
			for (AUGMENTATION_TYPE et : AUGMENTATION_TYPE.values()) {
				int expNumberMax = Integer.parseInt(args[0]);
				String leafFolder = args[1];
				for (METAMODEL mm : METAMODEL.values()) {
					for (int expNumber = 0; expNumber < expNumberMax ; expNumber++) {
						sortAndSortResultFileAndRunFolders(dirExperimentPath, et.name(), mm.name(), expNumber, leafFolder);
					}
				}
			}
		} else {
			System.out.println("Wrong number of parameters. See metrics.RenameRuns");
		}
		
		
		
		
	}

	public static void sortAndSortResultFileAndRunFolders(String dirExperimentPath, String expType, String mmName, int expNumber, String leafFolder) {
		File dirExperimentResults = Paths.get(dirExperimentPath, expType, mmName, expType+(expNumber > 1 ? expNumber: "")+"_"+mmName, "_results", leafFolder).toFile();
		
		File[] runExs = ToolBox.listDirecories(dirExperimentResults, MetricExtraction.RUN_FOLDERS_PREFIX);
		File res = Paths.get(dirExperimentResults.getAbsolutePath(), RESULTS_FILE_NAME).toFile();
		
		if( !res.exists()){
			System.out.println("No result file found in '"+res.getAbsolutePath()+"'");
			return;
		}
		
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(res));
			
			String line = br.readLine(); // remove header
			String header = line ;
			String exNameX = line.split(";")[0], exName = line.split(";")[0];
			int i = 0;
			ArrayList<String> lines = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				if( ! line.trim().isEmpty()){
					String[] values = line.split(";");
					exName = values[0];		
					if(!exNameX.equals(exName)){
						i = 0;
						exNameX = exName;
					}
						
					for (int j = 0; j < values.length; j++) {
						if(j == 1)
							values[j] = i + "";
					}
					
					String resLine = "";
					for (String v : values) 
						resLine += v + ";";
					resLine = resLine.substring(0, resLine.length()-1);
//					System.out.println(values[0] + " -> " + i+" : "+resLine);
					lines.add(resLine);
					i++;
				}
			} 
			br.close();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(res));
//			System.out.println(header+"\n");
			bw.write(header+"\n");
			for (String lineTmp : lines) {
				bw.write(lineTmp+"\n");
			}
			bw.close();
			
			System.out.println(res.getAbsolutePath()+ " overriden");
			for (File runEx : runExs) {
				@SuppressWarnings("unused")
				File[] runExsRenamed = renameRun(runEx);
				System.out.println();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static File[] renameRun(File dir){
		
		File[] runs = ToolBox.listDirecories(dir, RUN_FOLDERS_PREFIX);
		File[] runsRenamed = new File[runs.length];
		
		runs = sortRunFiles(runs);
		for (int i = 0; i < runs.length; i++) {
			runsRenamed[i] = Paths.get(runs[i].getParentFile().getAbsolutePath(), RUN_FOLDERS_PREFIX + i).toFile();
			runs[i].renameTo(runsRenamed[i]);
//			System.out.println(runs[i].getName() +" -> "+runsRenamed[i].getName());
		}
		return runsRenamed;
	}
	
	public static File[] sortRunFiles(File[] runs){
		Arrays.sort(runs, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				int numo1 = Integer.parseInt(o1.getName().substring(RUN_FOLDERS_PREFIX.length()));
				int numo2 = Integer.parseInt(o2.getName().substring(RUN_FOLDERS_PREFIX.length()));
				return numo1 - numo2;
			}
		});
		return runs;
	}
}

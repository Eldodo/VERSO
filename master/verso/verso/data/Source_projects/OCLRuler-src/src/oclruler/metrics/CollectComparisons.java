package oclruler.metrics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;



/**
 * 
 * File name template : "extract_Bank_0_Classic_exp0_40exs_run3.txt" <br/>
 * File content template :
(0) Extravagant <br/>
(1) Match <br/>
(2) Match <br/>
<br/>
[-mm, Family, -aug, Classic, -exp, 0, -exs, 25, -run, 2, -leaf, official] <br/>
[...] 
 * @author najor
 *
 */
public class CollectComparisons {
	public final static Logger LOGGER = Logger.getLogger("collect.comparisons");
		public final static String CSV_SEPARATOR = ";";
	
	
	static File comparisonsFolder = new File("R:\\EclipseWS\\material\\OCL_Ruler\\results\\extraction\\comparisons");

	
	enum RESULT {
		Match(3),
		Proche(1),
		Extravagant(0),
		Implicit(1);
		String augType, exp, exs, run;
		int weight;
		
		private RESULT(int weight) {
			this.weight = weight;
		}
	}
	
	enum MM { Bank, statemachine, Family }
	
	
	
	public static void main(String[] args) {
		LOGGER.info("[CollectComparisons] Entering... ");
		
		
		HashMap<MM, List<CollectComparisons>> map = new HashMap<>();
		
		File[] comparisonFiles = comparisonsFolder.listFiles();
		
		Arrays.sort(comparisonFiles, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().split("_")[2].compareTo(o2.getName().split("_")[2]);
			}
		});
		
		for (File file : comparisonFiles) {
			
			MM mm = MM.valueOf(file.getName().split("_")[1]);
			
			if(map.get(mm) == null)
				map.put(mm, new ArrayList<>());
			
			try {
				CollectComparisons solutionResult = new CollectComparisons(file);
				map.get(mm).add(solutionResult);
			} catch (IOException e) {
				System.out.println("!!! File '"+file.getAbsolutePath()+"' could not be read properly.");
//				e.printStackTrace();
			}
			
			
		}
		
		for (MM mm : map.keySet()) {
			System.out.println(mm);
			System.out.println(map.get(mm).toString().replaceAll(", ", "\n - "));
			try {
				printCSV(map.get(mm), mm);
			} catch (IOException e) {
				System.out.println("!!! "+mm+" CSV file could not be written properly.");
				e.printStackTrace();
			}
		}
		
		LOGGER.info("Safe exit.");
	}

	
	public static void printCSV(List<CollectComparisons> results, MM mm) throws IOException {
		File fCSV = Paths.get(comparisonsFolder.getParent(), "CSV_"+mm+".csv").toFile();
		fCSV.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(fCSV));
		bw.write("SOLUTION_NUMBER" + getCSVHeader() + "\n" );
		int i = 0;
		for (CollectComparisons collectComparisons : results) {
			bw.write(i++ +CSV_SEPARATOR+collectComparisons.getCSVLine()+"\n");
		}
		bw.close();
		
	}
	
	public static String getCSVHeader() {
		return "AVG_MATCH"+CSV_SEPARATOR+"AVG_PROCHE"+CSV_SEPARATOR+"AVG_EXTRAVAGANT"+CSV_SEPARATOR+"AVG_IMPLICIT";
	}
	
	private String getCSVLine() {
		return ""+f(averageMatchCount())+CSV_SEPARATOR+f(averageProcheCount()) +CSV_SEPARATOR+f(averageExtravagantCount())+CSV_SEPARATOR+f(averageImplicitCount());
	}

	List<RESULT> constraintsResult;
	public CollectComparisons(File file) throws IOException {
		String name[] = file.getName().split("_");
		@SuppressWarnings("unused")
		String classement = name[2];
		String augmentationType = name[3];
		String exp = name[4].substring(3);
		String exs = name[5].substring(0, 2);
		String run = name[6].substring(0, name[6].indexOf("."));


		List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
		constraintsResult = new ArrayList<>(5);
		for (String l : lines) {
			if(l.startsWith("(")) {
				l = l.trim().substring(4);// Discard the '(X) '
				RESULT r = RESULT.valueOf(l);
				r.augType = augmentationType;
				r.exp = exp;
				r.exs = exs;
				r.run = run;
				constraintsResult.add(r);
			} else 
				break;
		}
	}

	int countResults(RESULT r){
		int res = 0;
		for (RESULT result : this.constraintsResult) if ( r == result)	res ++;
		return res;
	}
	
	int countMatch() {		return countResults(RESULT.Match);	}
	int countProche() {		return countResults(RESULT.Proche);	}
	int countExtravagant() {return countResults(RESULT.Extravagant);	}
	int countImplicit() {	return countResults(RESULT.Implicit);	}
	
	double averageResultCount(RESULT r) {
		return (double)countResults(r) / (double)constraintsResult.size();
	}
	double averageMatchCount() {		return averageResultCount(RESULT.Match);	}
	double averageProcheCount() {		return averageResultCount(RESULT.Proche);	}
	double averageExtravagantCount() {	return averageResultCount(RESULT.Extravagant);	}
	double averageImplicitCount() {		return averageResultCount(RESULT.Implicit);	}
	
	int getSum() {
		int res = 0;
		for (RESULT result : this.constraintsResult) 	res += result.weight;
		return res;
	}

	double getValue() {
		return getSum() / (double) constraintsResult.size();
	}

	@Override
	public String toString() {
		return "("+f(averageMatchCount())+" "+f(averageProcheCount()) +" "+f(averageExtravagantCount())+" "+f(averageImplicitCount()) + ") "+(averageMatchCount()>=0.5?"*":"") ;//+ (averageExtravagantCount()+averageImplicitCount()+averageMatchCount()+averageProcheCount()) ;
	}

	String f(double d) {
		return String.format("%.1f", d);
	}
	
}

package oclruler.utils;


import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import oclruler.genetics.EvaluatorOCL;
import oclruler.genetics.Oracle;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.FireMap;
import oclruler.metamodel.InvalidModelException;
import oclruler.metamodel.Model;
import oclruler.rule.Program;

@SuppressWarnings("deprecation")
public class DiagnosticTool {
	public final static Logger LOGGER = Logger.getLogger(DiagnosticTool.class.getName());
	private static final String O_PROGRAM_FILE = "p";
	private static final String O_PROGRAM_FILE_LONG = "program-file";
	private static final String O_MODELSET_FILE = "ms";
	private static final String O_MODELSET_FILE_LONG = "modelset-file";
	
	public static void main(String[] args) throws InvalidModelException {
		LOGGER.info("Entering OCL_Ruler - TESTER - XNI-Jess");
		ToolBox.init();
		Program prg  = null;
		ExampleSet ms = null;
		
		Options options = configureOptions();
		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine;
		String programFile;
		try {
			commandLine = parser.parse(options, args);
		    if( commandLine.hasOption( O_PROGRAM_FILE ) ) {
		        programFile = commandLine.getOptionValue(O_PROGRAM_FILE);
				prg = Program.loadProgramFromFile(new File(programFile));
		    } else {
		    	programFile = Config.DIR_ORACLES.getAbsolutePath();
				prg  = Oracle.instantiateOracle(ExampleSet.getInstance());
		    }
		    if(prg != null)
				LOGGER.info("Program loaded : "+prg+" ("+prg.size()+" constraints) from '"+programFile+"'");
		    
		    if( commandLine.hasOption( O_MODELSET_FILE ) ) {
		        String modelSetFile = commandLine.getOptionValue(O_MODELSET_FILE);
		        File fmodels = new File(modelSetFile);
		        if(fmodels.isDirectory())
					try {
						ms = new ExampleSet(new File(modelSetFile));
					} catch (Exception e) {
						throw new ParseException("Option -ms is not a valid Model directory file. ("+e.getMessage()+")");
					}
				else  {
		        	Model m = ExampleSet.loadModel(modelSetFile);
		        	if(m != null){
			        	ms = new ExampleSet();
			        	ms.addModel(m);
		        	} else {
		        		throw new ParseException("Option -ms is not a valid Model file.");
		        	}
		        }
		        if(ms != null)
					LOGGER.info("Model set loaded : "+ms+" from "+modelSetFile);
		    } else {
		    	ms = ExampleSet.getInstance();
		    	LOGGER.info("Defaut model set loaded : "+ms+" from "+Config.getInstancesDirectory().getAbsolutePath());
		    }
		    
		} catch (ParseException e) {
			LOGGER.severe("Command line options invalid : "+e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth(120);
			System.out.println();
			formatter.printHelp("java -jar <this-file.jar>", options, true);
			System.out.println("\nContinue with the default Oracle"+Config.METAMODEL_NAME+" and example set from "+Config.DIR_EXAMPLES+" ?");
			@SuppressWarnings("resource")
			String yn = new Scanner(System.in).nextLine();
			if(! yn.equalsIgnoreCase("y") && !yn.equalsIgnoreCase("yes")) {
				LOGGER.info("Exit.");
				System.exit(1);
			}
		}

			
		ArrayList<Model> poss = new ArrayList<>();
		ArrayList<Model> negs = new ArrayList<>();
		
		String logValidExamles = "valid examples : \n";
		FireMap fm = new FireMap();
		for (Model m : ExampleSet.getExamplesBeingUsed()) {
			EvaluatorOCL.execute(fm, m, prg);
			int f = fm.getNumberOfFires(m);
			if(f == 0){
				poss.add(m);
				logValidExamles += "  "+m.getFileName()+"\n ";
			} else
				negs.add(m);
		}
		if(LOGGER.isLoggable(Level.FINE) && poss.size() < 100)
			LOGGER.config(poss.size()+ " " + logValidExamles.substring(0, logValidExamles.length()-2));
		else
			LOGGER.config(poss.size()+ " valid examples.");
		LOGGER.config(negs.size()+ " invalid examples.");
		
		
		
		
		/*
		 * Print for each pattern the models that fire it.
		 */
//		for (String pName : fm.getReverseMap().keySet()) {
//			System.out.println("Pattern : "+pName);
//			HashMap<Model, Integer> pName_fmd = fm.getReverseMap().get(pName);
//			for (Model mi : pName_fmd.keySet()) {
//				System.out.println(" - Model : "+mi.getFileName()+" -> "+pName_fmd.get(mi)+" fires");
//				//sumFires+=pName_fmd.get(mi);
//			}
//			System.out.println();
//		}
		
		
		
		int i = 0;
		int jumpi = 0;
		boolean repeat = false;
		boolean exit = false;
		for (int j = 0; j < negs.size() && !exit; j++) {
			if(i>= jumpi){
				do{
					LOGGER.info(prg.diagnose(negs.get(i)));
					System.out.println("Next <Enter> or repeat <R>, or quit <Q> ?");
					@SuppressWarnings("resource")
					String c = new Scanner(System.in).nextLine();
					if(c.toLowerCase().equals("q") || c.toLowerCase().equals("Quit")) {
						exit = true;
					}
					repeat = c.toLowerCase().equals("r") || c.toLowerCase().equals("repeat");
					try {
						jumpi = i + Integer.parseInt(c);
		
					} catch (NumberFormatException e) {
						jumpi = 0 ;
					}
				}while(repeat && !exit);
			}
			i++;
		}
		
		LOGGER.info("OCL_Ruler - TESTER - XNI-Jess - EXIT !");
	}
	

	private static Options configureOptions() {
		Option programFileOption = OptionBuilder.create(O_PROGRAM_FILE);
		programFileOption.setLongOpt(O_PROGRAM_FILE_LONG);
		programFileOption.setArgName("program-file");
		programFileOption.setDescription("use <program-file> as constraints' program. (defaults use Oracle"+Config.METAMODEL_NAME+")");
		programFileOption.setType(String.class);
		programFileOption.setArgs(1);

		Option modelsetFileOption = OptionBuilder.create(O_MODELSET_FILE);
		modelsetFileOption.setLongOpt(O_MODELSET_FILE_LONG);
		modelsetFileOption.setArgName("modelset-file");
		modelsetFileOption.setDescription("use <modelset-file> directory. (defaults use '"+Config.DIR_EXAMPLES+"')");
		modelsetFileOption.setType(String.class);
		modelsetFileOption.setArgs(1);
		
		Options options = new Options();
		options.addOption(programFileOption);
		options.addOption(modelsetFileOption);
		return options;
	}
	
	
}


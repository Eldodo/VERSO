package oclruler.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.rule.Program;
import oclruler.rule.patterns.A0_RawText;
import oclruler.rule.struct.Constraint;
import oclruler.rule.struct.Node_DEFAULT;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

public class ProgramLoader {
	public final static Logger LOGGER = Logger.getLogger(ProgramLoader.class.getName());
	
	public static void main(String[] args) throws Exception {
		System.out.println("Entering ProgramLoader Tester");
		System.out.println("Metamodel MUST BE 'statemachine' !");
		ToolBox.init();
		
		//Case 1
		File fResults = new File(Config.DIR_TESTS+"/ProgramLoader/SM_run11_bestsolution_100.ocl");
		Program pLoaded = loadProgramFromFile(fResults);
		if( pLoaded.size() != 4) 
			throw new Exception("Loading '"+fResults.getAbsolutePath()+"' failed\n Expected 4 rules, found "+pLoaded.size()+" !");
		
		//Case 2
		File fResults2 = new File(Config.DIR_TESTS+"/ProgramLoader/SM_run14_bestsolution_100.ocl");
		Program pLoaded2 = loadProgramFromFile(fResults2);
		if( pLoaded2.size() != 3) 
			throw new Exception("Loading '"+fResults2.getAbsolutePath()+"' failed\n Expected 3 rules, found "+pLoaded2.size()+" !");
		
		
		//Case 3 - Family 
//		File fResults3 = new File(Config.DIR_TESTS+"/ProgramLoader/Family_35_06_bestsolution_100.ocl");
//		System.out.println(fResults3.getAbsolutePath());
//		Program pLoaded3 = loadProgramFromFile(fResults3);
//
//		
//		System.out.println(pLoaded3.getMMElements());
//		System.out.println();
//		System.out.println(pLoaded2.getMMElements());
		
		System.out.println("ProgramLoader Tester -- Exit");
	}



	public static Program loadProgramFromFile(File fResults) {
		return loadProgramFromFile(fResults, null);
	}
	
	public static Program loadProgramFromFile(File fResults, String programName) {
		ArrayList<A0_RawText> constraints = loadConstraintsFromFile(fResults);
		Program p = new Program();
		if(programName != null && !programName.isEmpty())
			p.setName(programName);
		for (A0_RawText a0 : constraints) {
			Node_DEFAULT n = new Node_DEFAULT(null, a0);
			Constraint cst = new Constraint(n);
			cst.setName(a0.getFullName());
			boolean b = p.addConstraint(cst);
			if(!b)
				LOGGER.severe("Pattern not included : '"+fResults.getAbsolutePath()+"'");
		}
		return p;
	}

	
	
	/**
	 * Load constraints from a file. The file can be either an OCL (.ocl) file or a directory. If the file given in
	 * parameter is a directory, it will be visited recursively (sub directories and .ocl files).
	 * 
	 * @param f
	 * @return A list of all the constraint found in the file/directory.
	 */
	public static ArrayList<A0_RawText> loadConstraintsFromFile(File f) {
		ArrayList<A0_RawText> res = new ArrayList<>();
		if (f == null) {
			A0_RawText.LOGGER.warning("IllegalArgument : " + f + ". No A0 constraint loaded.");
			return res;
		}
	
		if (f.isDirectory()) {
			A0_RawText.LOGGER.finer("File '" + f.getAbsolutePath() + "' is a directory. ");
			for (File ff : f.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname != null && pathname.exists() && (pathname.getName().endsWith("ocl") || pathname.getName().endsWith("OCL"));
				}
			})) {
				res.addAll(loadConstraintsFromFile(ff));
			}
			return res;
		} else if (f.getName().endsWith("ocl") || f.getName().endsWith("OCL")) {
			// ELSE f is not a directory -> process ocl file.
			String text = "", s;
			ArrayList<String> cleanText = new ArrayList<>();
			try {
				BufferedReader br3 = new BufferedReader(new FileReader(f));
				while ((s = br3.readLine()) != null) {
					s = s.trim();
					if (!s.isEmpty() && !s.startsWith("import") && !s.startsWith("package") && !s.startsWith("endpackage") && !s.startsWith("--")) {
						text += s + "\n";
						cleanText.add(s.trim() + " ");
					}
					if (A0_RawText.LOGGER.isLoggable(Level.FINEST) && s.startsWith("--"))
						A0_RawText.LOGGER.finest("Comment line '" + s + "' ");
				}
				br3.close();
			} catch (IOException e1) {
				A0_RawText.LOGGER.severe("Error during OCL file reading : " + e1.getMessage());
				if (A0_RawText.LOGGER.isLoggable(Level.CONFIG))
					e1.printStackTrace();
			}
			text = text.trim();
			
			String[] contexts = text.split("context");
			for (int i = 1; i < contexts.length; i++) {// First "contexts[0]" always empty.
				String[] constraints = contexts[i].trim().split("inv");
				Concept context = Metamodel.getConcept(constraints[0].trim());
				String cstName = null;
				try {
					cstName = constraints[1].substring(0, constraints[1].indexOf(":")).trim()+"_";
				} catch (Exception e1) {
					//CST HAS NO NAME
				}
				if (context == null) {
					A0_RawText.LOGGER.severe("Constraint unclear ! '" + constraints[0].trim() + "' should be a valid concept.");
				}
				for (int j = 1; j < constraints.length; j++) {
					constraints[j] = constraints[j].trim();
	
					try {
						String constraintName = context.getName() + "_" + constraints[j].substring(0, constraints[j].indexOf(":")).trim();
						String constraint = constraints[j].substring(constraints[j].indexOf(":") + 1).trim();
						if(cstName != null)
							constraintName = cstName;
						A0_RawText a = new A0_RawText(constraintName, context, constraint, f);
						A0_RawText.LOGGER.finer("Constraint added : " + a);
						res.add(a);
					} catch (Exception e) {
						A0_RawText.LOGGER.severe("Parsing fail : " + e.getMessage() + "\n  Context : " + context.getName() + "\n  Constraint : '" + constraints[j]
								+ "' \n  -> Constraint ignored ! <-");
						e.printStackTrace();
					}
	
				}
			}
			A0_RawText.LOGGER.config(res.size() + " constraints added from '" + f.getAbsolutePath() + "'");
		}
		return res;
	
	}
}

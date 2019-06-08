package oclruler.genetics;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import oclruler.genetics.Evaluator.GRAIN;
import oclruler.genetics.OraculizationException.RATIONALE;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.FireMap;
import oclruler.metamodel.Model;
import oclruler.rule.OracleBank;
import oclruler.rule.OracleBpmn;
import oclruler.rule.OracleCD;
import oclruler.rule.OracleFamily;
import oclruler.rule.OracleOCLexpressions;
import oclruler.rule.OracleRoyalAndLoyal;
import oclruler.rule.OracleStatemachine;
import oclruler.rule.Program;
import oclruler.rule.struct.Constraint;
import oclruler.ui.Ui;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public abstract class Oracle extends Program {
	
	public static HashMap<Model, OracleCmp> getOracleComparison(ArrayList<Model> models, FireMap fm){
		HashMap<Model, OracleCmp> oracleComparison = new HashMap<>(models.size());
	
		for (Model m : models) 
			oracleComparison.put(m, OracleCmp.getCmp(m.isValid(), fm.getFiredObjects(m).isEmpty()));
		return oracleComparison;
	}
	
	
	public enum OracleCmp {
		PP("+/+"), NN("-/-"), NP("+/-"), PN("-/+");
		//String res = "["+pp+"|"+pn+" "+np+"|"+nn+"]";
		public static OracleCmp getCmp(boolean oracleDecision, boolean prgDecision) {
			if(oracleDecision)
				if(!prgDecision)
					return PN;
				else
					return PP;
			else
				if(prgDecision)
					return NP;
				else
					return NN;
		}
		String label;
		OracleCmp(String label){
			this.label = label;
		}
		
		@Override
		public String toString() {
			return label;
		}
		
		public boolean isRight(){
			return this == NN || this == PP;
		}
	}

	public final static Logger LOGGER = Logger.getLogger(Oracle.class.getName());
	
	static Oracle instance;

	protected String textRulesDirName ;
	protected ArrayList<File> textRules;
	
	ExampleSet exampleSet;
	Evaluator evaluator;
	
	public Oracle(ExampleSet ms, String textFileName) throws IllegalArgumentException {
		super();
		if(ms != null)
			exampleSet = ms;
		else
			exampleSet = ExampleSet.getInstance();
		textRulesDirName =  Config.getOraclesDirectory().getAbsolutePath()+File.separator+Config.METAMODEL_NAME;
		File dirText = new File(textRulesDirName);
		textRules = getRulesFromTextDirectory(dirText);
		buildPatterns();
		
		
		if(constraints.isEmpty()) {
			throw new IllegalArgumentException("Oracle is empty. Check directory '"+Config.DIR_EXAMPLES+"' + HARD_CODE in "+this.getClass().getCanonicalName());
		}
		if(LOGGER.isLoggable(Level.FINE)){
			LOGGER.fine(constraints.size()+ " contraints added from "+dirText.getAbsolutePath());
			for (Constraint p : constraints) {
				LOGGER.fine(" - "+p.getOCL_inv().replaceAll("\n", "[\\\\n]"));
			}
		}
		LOGGER.config(constraints.size()+ " contraints added.");
	}
	
	public Oracle(ExampleSet ms) throws IllegalArgumentException {
		this(ms, null);
	}
	
	public ExampleSet getExampleSet() {
		return exampleSet;
	}

	public static  ArrayList<File> getRulesFromTextDirectory(File dirText){
		ArrayList<File> res = new ArrayList<>();
		if(dirText.exists() && dirText.isDirectory()){
			File[] fs = dirText.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".xmi") || name.toLowerCase().endsWith(".ocl") || name.toLowerCase().endsWith(".oclxmi");
				}
			});
			for (File file : fs) 
				res.add(file);
			
		} 
		if(!res.isEmpty()) 
			LOGGER.config("Text rule files found : "+res.size()+" files (out of "+dirText.listFiles().length+")");
		else 
			LOGGER.finer("No text rule files.");
		return res;
	}
	
	
	/**
	 * Copy and paste in ProgramLoader (see {@link oclruler.metrics.ProgramLoader#loadConstraintsFromFile(File)}) -- Attention !
	 */
	protected abstract void buildPatterns() ;
	
	/**
	 * Default Evaluator : EvaluatorOCL.getInstance()
	 * @throws OraculizationException
	 */
	public void oraculize() throws OraculizationException {
		oraculize(EvaluatorOCL.getInstance());
	}
	public void oraculize(boolean forceNewEvaluator) throws OraculizationException {
		oraculize(EvaluatorOCL.getInstance(forceNewEvaluator));
	}
	
	public void oraculize(ExampleSet exSet) throws OraculizationException {
		
	}
	public void oraculize(Evaluator eva) throws OraculizationException {
		oraculize(eva, ExampleSet.getInstance());
	}
	
	public void oraculize(Evaluator eva, ExampleSet exSet) throws OraculizationException {
		evaluator = eva;
		
		boolean allNotValid = true;
		boolean allValid = true;
		int positives = 0, negatives = 0;
		
		GRAIN g = Evaluator.EXECUTION_GRAIN;
		Evaluator.EXECUTION_GRAIN = GRAIN.FINE;//Force fine grain to get number of Oracle fires.
		if(exSet.getAllExamples().isEmpty())
			throw new OraculizationException(RATIONALE.NO_EXAMPLE);
		
		for (Model m : exSet.getAllExamples()) {
			m.clearOracleFire();
			FireMap fm = EvaluatorOCL.execute(null, m, this);
			
			for (Constraint p : constraints) {
				int f = fm.getNumberFires(m, p);
				if(f > 0){
					m.addOracleFire(p, f);
					p.cleanFires();
				}
			}
			positives += m.isValid()?1:0;
			negatives += !m.isValid()?1:0;
			allNotValid &= !m.isValid();
			allValid 	&=  m.isValid();
		}
		
		
		if(ExampleSet.POSITIVES_CONSIDERED > positives) ExampleSet.POSITIVES_CONSIDERED = positives;
		if(ExampleSet.NEGATIVES_CONSIDERED > negatives) ExampleSet.NEGATIVES_CONSIDERED = negatives;
		
		
		exSet.getAllPositives().sort( (Model m1, Model m2) -> m1.getFileName().compareTo(m2.getFileName()));
		exSet.getAllNegatives().sort( (Model m1, Model m2) -> m1.getFileName().compareTo(m2.getFileName()));
		
		eva.evaluate(this, true);
		exSet.setOraculized();	
		Evaluator.EXECUTION_GRAIN = g;	
		
		
		
		/*
		 * LOG
		 */
		int maxNameSize = 0;
		for (Model m : exSet.getAllPositives()) 
			if(m.getFileName().length()> maxNameSize) maxNameSize = m.getFileName().length();
		for (Model m :exSet.getAllNegatives()) 
			if(m.getFileName().length()> maxNameSize) maxNameSize = m.getFileName().length();
		
		if(LOGGER.isLoggable(Level.FINE)){
			for (Model m : exSet.getAllExamples()) {
				String log =  ToolBox.completeString(m.getFileName(), maxNameSize)+"   "+(m.isValid()?" >VALID<  ":">INVALID< ")+" ("+m.getFireCount()+" fire"+((m.getFireCount()>1)?"s":"")+")";
				if(LOGGER.isLoggable(Level.FINER)){
					for (Constraint pt : m.getOracleFires().keySet()) {
						log += "\n + "+pt+" -> "+ m.getOracleFires().get(pt);
					}
					LOGGER.finer(log);
				} else 
					LOGGER.fine(log);
			} 
		}
		
		String s = "Results: " +positives+" positives / "+negatives+" negatives"+((ExampleSet.NEGATIVES_CONSIDERED < negatives || ExampleSet.POSITIVES_CONSIDERED < positives)? " ("+ExampleSet.POSITIVES_CONSIDERED+"/"+ExampleSet.NEGATIVES_CONSIDERED+" considered at beginning)":"");
		
		LOGGER.info(s);
		if(Config.VERBOSE_ON_UI)
			Ui.getInstance().appendTextSetting("Oracle: \n-------\n(from "+exSet.getDirectory()+")\n"+exSet.printOracleDecisions()+"\n"+s);
			
		/*
		 * Diversity check
		 */
		if(!Config.EXAMPLE_EDITION_MODE){
			if (allNotValid) {
				throw new OraculizationException(RATIONALE.NO_POSITIVE);
			}
			if (allValid) {
				throw new OraculizationException(RATIONALE.NO_NEGATIVE);
			}
		}

	}
	/**
	 * FOR TEST ONLY </br></br>Please use getInstance() instead.
	 * @param ms
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Oracle instantiateOracle(ExampleSet ms) throws IllegalArgumentException {
		Oracle o = null;
			switch (Config.METAMODEL_NAME) {
			case "statemachine":
				o = new OracleStatemachine(ms);
				break;
			case "Family":
				o = new OracleFamily(ms);
				break;
			case "CD":
				o = new OracleCD(ms);
				break;
			case "OCLexpressions":
				o = new OracleOCLexpressions(ms);
				break;
			case "bpmn":
				o = new OracleBpmn(ms);
				break;
			case "Bank":
				o = new OracleBank(ms);
				break;
			case "RoyalAndLoyal":
				o = new OracleRoyalAndLoyal(ms);
				break;
			default:
				LOGGER.severe("Metamodel named '"+Config.METAMODEL_NAME+"' not registered. Accepted are { statemachine, Family, CD, OCLexpressions, RoyalAndLoyal (,bpmn [GHOST]) }\n Exit");
				System.exit(1);
				break;
			}

//		if(instance == null)
			instance = o;
		
		LOGGER.finer("\nOracle:\n"+ms.printOracleDecisions());
		return o;
	}
	
	public static Oracle getInstance(boolean forceInstantiation) {
		if(forceInstantiation || instance == null)
			instance = instantiateOracle(ExampleSet.getInstance());
		return instance;
	}
	public static Oracle getInstance() {
		return getInstance(false);
	}
	
	public String getTextRulesDirName() {
		return textRulesDirName;
	}
}













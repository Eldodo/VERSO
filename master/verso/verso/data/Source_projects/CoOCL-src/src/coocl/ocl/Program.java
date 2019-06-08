package coocl.ocl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ocl.pivot.utilities.EnvironmentFactory;
import org.eclipse.ocl.pivot.utilities.OCL;
import org.eclipse.ocl.utilities.TypedElement;
import org.eclipse.ocl.OCLInput;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.ecore.CallOperationAction;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.ecore.SendSignalAction;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.expressions.OperationCallExp;
import org.eclipse.ocl.pivot.resource.ASResource;
import org.eclipse.ocl.pivot.util.PivotPlugin;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import com.sun.corba.se.impl.ior.GenericTaggedComponent;

import oclruler.genetics.Gene;
import oclruler.genetics.GeneticEntity;
import oclruler.genetics.UnstableStateException;
import oclruler.metamodel.MMElement;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.MetamodelMerger;
import oclruler.metamodel.MetamodelMerger.DIFF_TYPE;
import oclruler.metamodel.StructuralFeature;
import utils.Config;
import utils.Utils;
import utils.distance.Cosine;

public class Program extends GeneticEntity {
	static Logger LOGGER = Logger.getLogger(Program.class.getName());

	/**
	 * While attempting to mutate a program, the number of attempts to mutate the prgoam into a different form (i.e. the mutant is different (i.e. not equal) to the original program).
	 */
	private static final int MAX_MUTATION_ATTEMPTS = 20;

	static int counter = 0;
	
	/**
	 * Original OCL program (file)
	 */
	public static File OCL_FILE;
	public static File OCL_EXPECTED_FILE;
	/**
	 * Must contain operations used when mutating a contrainte (for now : oclIsTypeOf())
	 */
	public static File OCL_FOR_OPERATIONS_FILE;

	/**
	 * Original OCL program (model)
	 */
	static Program program0;
	
	/**
	 * Keep record of the constraint that the evolution did not affect.
	 */
	static ArrayList<Contrainte> untouchedContraintes;

	/**
	 * If <code>false</code>, the tool will only consider constraint affected by the evolution. <br/>
	 * <code>true</code> should be used for test only (see config file for setting).
	 */
	public static boolean CONSIDER_ALL_CONSTRAINTS = false;

	
	
	
	
	protected Metamodel metamodel;
	private String name;
	private ArrayList<Contrainte> contraintes;

	
	public Program(Metamodel metamodel) {
		name = 				this.getClass().getSimpleName()+"_"+counter;
		this.metamodel = 	metamodel;
		contraintes = 		new ArrayList<>();
		counter++;
	}

	/**
	 * {@link #program0 program0} is initialized in {@link #loadConfig(File)
	 * loadConfig(...)}
	 * 
	 * @return the number of constraint in the original OCL program.
	 */
	public static int getNumberOfContraintes() {
		return program0.size();
	}

	/**
	 * Retourne les contraintes concernées par le mme passé en parametre.
	 * 
	 * @param mme
	 * @return
	 */
	public ArrayList<Contrainte> getContraintesUsingMMElement(MMElement mme){
		if(mme == null)
			return getContraintes();
		ArrayList<Contrainte> res = new ArrayList<>();
		for (Contrainte contrainte : contraintes) {
			HashMap<StructuralFeature, ArrayList<TypedElement<?>>> mmeAffectingOCL = contrainte.getStructuralFeaturesAffectingOCL();
			for (MMElement mme2 : mmeAffectingOCL.keySet()) {
				if(mme.equalNames(mme2))
					if(!res.contains(contrainte))
						res.add(contrainte);
			}
		}
		return res;
	}
	
	public static Program getInitialProgram() {
		return program0;
	}
	static Program expectedSolutionProgram;

	public static Program getExpectedSolution() {
		if(expectedSolutionProgram == null){
			CollectOCLIds<?, ?, ?, ?, ?, ?, ?, ?, ?> collector =  CollectOCLIds.newCollectOCLId(Metamodel.getMm2()); 
			try {
				
				expectedSolutionProgram = collector.load(OCL_EXPECTED_FILE);
				if(!CONSIDER_ALL_CONSTRAINTS) {
					for (Iterator<Contrainte> it = expectedSolutionProgram.contraintes.iterator(); it.hasNext();) {
						Contrainte c = (Contrainte) it.next();
						if(program0.getContrainte(c.number) == null)
							it.remove();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.warning("'OCL_EXPECTED_FILE'="+OCL_EXPECTED_FILE+" not working. No ground truth available for evaluation.");
			}
		}
		return expectedSolutionProgram;
	}
	public static HashMap<String, EOperation> eOperations = new HashMap<>();
	
	
	public static EOperation getEOperation(String name){
//		EOperation res = EcoreUtil.copy(eOperations.get(name));
//		((EClass)eOperations.get(name).eContainer()).getEOperations().add(res);
//		((EClass)eOperations.get(name).eContainer()).getEOperations().remove(eOperations.get(name));
		return eOperations.get(name);
	}
	
	
	public static void loadConfig(File fileOCL){
		OCL_FILE = fileOCL;
		try {
			CONSIDER_ALL_CONSTRAINTS  = Config.getBooleanParam("CONSIDER_ALL_CONSTRAINTS");
		} catch (Exception e1) {
			//Not specifided : constraints considered restricted to the ones impacted by evolution
		}
		try {
			String expected =  Config.getStringParam("OCL_EXPECTED_FILE");
			OCL_EXPECTED_FILE  = new File(expected);
		} catch (Exception e1) {
//			LOGGER.warning("'OCL_EXPECTED_FILE' not set. No ground truth available for evaluation.");
		} finally {
			if(OCL_EXPECTED_FILE == null)
				OCL_EXPECTED_FILE  = new File( Config.DIR_TESTS+Config.METAMODEL_NAME+"-2_expected.ocl");
			if(!OCL_EXPECTED_FILE.exists())
				throw new IllegalArgumentException("OCL_EXPECTED_FILE not found :"+OCL_EXPECTED_FILE.getAbsolutePath());
		}
		
		
		
		MetamodelMerger mmMerger = new MetamodelMerger(Metamodel.getMm1(), Metamodel.getMm2(), Program.OCL_FILE);
		
		MetamodelMerger.setInstance(mmMerger);
		
		
		try {
			CollectOCLIds<?, ?, ?, ?, ?, ?, ?, ?, ?> collector =  CollectOCLIds.newCollectOCLId(Metamodel.getMm1()); 
			counter = 0;
			program0 = collector.load(OCL_FILE);
			
			
			ArrayList<Contrainte> contraintesAffectedByEvolution = new ArrayList<>();
			
			for (MMElement mme : mmMerger.getDiffMMELements(DIFF_TYPE.REMOVE)) {
				for (Contrainte contrainte : program0.getContraintesUsingMMElement(mme)) {
					if(!contraintesAffectedByEvolution.contains(contrainte))
						contraintesAffectedByEvolution.add(contrainte); 
				}
			}
			for (MMElement mme : mmMerger.getDiffMMELements(DIFF_TYPE.CARDINALITY_UP)) {
//				System.out.println(mme);
				for (Contrainte contrainte : program0.getContraintesUsingMMElement(mme)) {
					if(!contraintesAffectedByEvolution.contains(contrainte))
						contraintesAffectedByEvolution.add(contrainte); 
				}
			}
			Collections.sort(contraintesAffectedByEvolution, (Contrainte o1, Contrainte o2) ->  o1.number - o2.number );
			
			untouchedContraintes = new ArrayList<>(program0.contraintes.size()-contraintesAffectedByEvolution.size());
			
			for (Contrainte contrainte : program0.contraintes) {
				if(!contraintesAffectedByEvolution.contains(contrainte))
					untouchedContraintes.add(contrainte);
			}
			
			//untouchedContraintes et contraintesAffectedByEvolution VERIFIED !
			if(!CONSIDER_ALL_CONSTRAINTS)
				program0.setContraintes(contraintesAffectedByEvolution);
			
			if(getExpectedSolution().size() != program0.size())
				LOGGER.severe("Initial OCL program's size differs from expected program solution's size. Please collapse removed contrainte to \"true\"");
			

		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("OCL file not found: '"+OCL_FILE.getAbsolutePath()+"'\n OCL_FILE must be changed in config.properties.");
		} catch (IOException e) {
			throw new IllegalArgumentException("OCL file incorrect: '"+OCL_FILE.getAbsolutePath()+"'.");
		}
		
		try {
			OCL_FOR_OPERATIONS_FILE = new File(Config.DIR_TESTS+"/ForOperations.ocl");
			Program forOperations = CollectOCLIds.newCollectOCLId(Metamodel.getMm2()).load(OCL_FOR_OPERATIONS_FILE);
			EOperation eopOclIsTypeOf = ((OperationCallExp<EClassifier, EOperation>)forOperations.getContrainte("kind").getEBodyExpression()).getReferredOperation();
			eOperations.put("oclIsTypeOf", eopOclIsTypeOf);
		} catch (FileNotFoundException e) {
			LOGGER.severe("OCL operations file not found: '"+OCL_FOR_OPERATIONS_FILE.getAbsolutePath()+"'.");
		} catch (IOException e) {
			LOGGER.severe("OCL operations file incorrect: '"+OCL_FOR_OPERATIONS_FILE.getAbsolutePath()+"'.");
		}

	}
	
	private void setContraintes(ArrayList<Contrainte> contraintes) {
		this.contraintes = contraintes;
	}

	
//	public Program(Metamodel metamodel, File oclFile) throws FileNotFoundException {
//		this(metamodel);
//		
//		if(oclFile.getName().endsWith("ocl"))
//			this.load(metamodel, new FileInputStream(oclFile));
//		else
//			throw new IllegalArgumentException("Wrong file format : '"+oclFile.getAbsolutePath()+"'\n File '*.ocl' expected");
//	}
	
	public int[] getNumberOfChanges(){
		int[] res = new int[contraintes.size()];
		int i = 0;
		for (Contrainte c : contraintes) {
			res[i++] = c.getNumberOfChanges();
		}
		return res;
	}
	public int[] getInformationLoss() {
		int[] res = new int[contraintes.size()];
		int i = 0;
		for (Contrainte c : contraintes) {
			res[i++] = c.getInformationLoss();
		}
		return res;
	}

	
	public int getSyntaxErrorCount() {
		int sum = 0;
		for (Contrainte contrainte : contraintes) {
			sum += contrainte.getSyntaxErrors();
		}
		return sum;
	}

	public int getInfoLossCount() {
		int sum = 0;
		for (Contrainte contrainte : contraintes) {
			sum += contrainte.getInformationLoss();
		}
		return sum;
	}
	
	public double[]  computeHammingDistances(Program p2){
		double[] hammings = new double[Program.getNumberOfContraintes()];
		int i = 0;
		for (Contrainte c1 : contraintes) {
			Contrainte c2 = p2.getContrainte(c1.number);
			
			hammings[i++] = c1.hammingDistance(c2);
			
//			String str1 = c1.getOCL_Body();
//			String str2 = c2.getOCL_Body();
//			int length = str1.length() < str2.length() ? str1.length() : str2.length();
//			System.out.println(
//					" " + Utils.completeString(str1, length) + "  |x|\n " + Utils.completeString(str2, length) + "  |x| " + String.format("%.02f", hammings[i]));

		}
		return hammings;
	}
	public double[] computeDamerauLevensteinDistances(Program p2) {
		double[] levensts = new double[Program.getNumberOfContraintes()];
		int i = 0;
		for (Contrainte c1 : contraintes) {
			Contrainte c2 = p2.getContrainte(c1.number);
			levensts[i++] = c1.damerauLevensteinDistance(c2);
		}
		return levensts;
	}
	public double computeDamerauLevensteinDistanceSum(Program p2) {
		double res = 0.0;
		for (double d : computeDamerauLevensteinDistances(p2)) {
			res += d;
		}
		return res;
	}

	public double[] computeCosineDistances(Program p2) {
		return computeCosineDistances(p2, Cosine.DEFAULT_K);
	}

	public double[] computeCosineDistances(Program p2, int cosineK) {
		double[] cosines = new double[Program.getNumberOfContraintes()];
		int i = 0;
		for (Contrainte c1 : contraintes) {
			Contrainte c2 = p2.getContrainte(c1.number);
			cosines[i++] = c1.cosineDistance(c2, cosineK);
			
//				String str1 = c1.getOCL_Body();
//				String str2 = c2.getOCL_Body();
//				int length = str1.length() > str2.length() ? str1.length() : str2.length();
//				System.out.println(
//						" " + Utils.completeString(str1, length) + "  |x|\n " + Utils.completeString(str2, length) + "  |x| " + String.format("%.02f", cosines[i]));
		}
		return cosines;
	}
	
	public double[][] computeDistances(Program p2) {
		return new double[][] {
				computeCosineDistances(p2), 
				computeHammingDistances(p2), 
				computeDamerauLevensteinDistances(p2)
			};
	}
	public double[] computeDistanceSums(Program p2) {
		double[] res = new double[3];
		int i = 0;
		for (double[] ds : computeDistances(p2)) {
			res[i] = 0;
			for (double d : ds) 
				res[i] += d;
			res[i] /= ds.length;
			i++;
		}
		return res;
	}
	
	/**
	 * Use Cosine distance bewteen
	 */
	@Override
	public double getDistance(GeneticEntity g) {
		double res = 0.0;
		int i = 0;
		for (double d : computeDamerauLevensteinDistances((Program)g)) {
			res += d;
			i++;
		}
		return res/i;
	}
	
	public int euclidianToOptimum() {
		int res = 0;
		res = (int)(fitnessVector.euclidianToOptimum() *100);
		return res;
	}

	public Contrainte getContrainte(int number){
		for (Contrainte contrainte : contraintes) {
			if(contrainte.number == number)
				return contrainte;
		}
		return null;
	}
	
	/**
	 * 
	 * @param name Name of the OCL constraint (between "context" and "inv:") ! !!
	 * @return
	 */
	public Contrainte getContrainte(String name){
		for (Contrainte contrainte : contraintes) {
			if(contrainte.getEConstraint().getName().equals(name))
				return contrainte;
		}
		return null;
	}
	
	/**
	 * DO NOT USE !
	 * @param metamodel
	 * @param is
	 * @return
	 */
	@Deprecated
	public boolean load_Deprecated(Metamodel metamodel, InputStream is) {
		EcoreEnvironmentFactory environmentFactory = new EcoreEnvironmentFactory(metamodel.resourceSet.getPackageRegistry());
		org.eclipse.ocl.OCL<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> ocl = org.eclipse.ocl.OCL.newInstance(environmentFactory);
		
		// get an OCL text file via some hypothetical API
		try {
			HashMap<String, Constraint> constraintMap2 = new HashMap<>();

			// parse the contents as an OCL document

			OCLInput document = new OCLInput(is);

			List<Constraint> constraints = null;
			constraints = ocl.parse(document);

			int i = 0;
			for (Constraint next : constraints) {
				constraintMap2.put(next.getName(), next);
				System.out.println("> Rule   : " + next.getName() + " (on " + next.getSpecification().getContextVariable().getType().getName() + ") ");
				OCLExpression<EClassifier> body = next.getSpecification().getBodyExpression();
//				Contrainte c = new Contrainte(next, i++);
//				addContrainte(c);
				System.out.printf("  - " + next.getSpecification().getContextVariable().getType().getName() + "::%s.body : %s%n%n", next.getName(), body);
			}
			
			CollectOCLIds<?, ?, ?, ?, ?, ?, ?, ?, ?> collector = CollectOCLIds.newCollectOCLId(metamodel);
//			collector.load(OCL_FILE);
			
			System.out.println("1. (Program.load) : "+collector.getConstraints().keySet());
			System.out.println("1. (Program.load) : "+collector.getSfsAffected().keySet());
		} catch (ParserException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public ArrayList<Contrainte> getContraintes() {
		return contraintes;
	}

	public boolean addContrainte(Contrainte c) {
		boolean res = false;
		if(!contraintes.contains(c))
			res = contraintes.add(c);
		return res;
	}
	
	
	/*
	 * Genetic methods start
	 * 
	 */
	
	
	
	@Override
	public GeneticEntity[] crossover(GeneticEntity e) throws UnstableStateException {
		if(contraintes.size() < 2)
			throw new UnstableStateException("Minimum of 2 constraints to apply crossover.");
		Program p2 = (Program)e;
		int cut = Utils.getRandomInt(1, contraintes.size()-1);
		
		Program[] sons = new Program[2];
		sons[0] = new Program(metamodel);
		sons[1] = new Program(p2.metamodel);
		
		for (int i = 0; i < contraintes.size(); i++) {
			if(i<cut){
				sons[0].contraintes.add(p2.contraintes.get(i));
				sons[1].contraintes.add(contraintes.get(i));
			} else {
				sons[0].contraintes.add(contraintes.get(i));
				sons[1].contraintes.add(p2.contraintes.get(i));
			}
		}
		return sons;
	}
	@Override
	public GeneticEntity[] crossoverDeep(GeneticEntity e) throws UnstableStateException {
		return crossover(e);
	}
	@Override
	public boolean mutate() throws UnstableStateException {
		
		//Choisir un mmeAffectingOCL
		//recupérer les contraintes (dynamicAccess)
		//Pour chaque contrainte appeler "mutate avec le mmeAffectingOCL"
		int i = 0;
		boolean res = false;
		Contrainte c = null;
		do{
			ArrayList<Contrainte> cts = new ArrayList<>(contraintes.size());
			for (Contrainte ct : contraintes) {
				if(!ct.getStructuralFeaturesAffectingOCL().isEmpty())
					cts.add(ct);
			}
			c = Utils.getRandom(expandForSyntaxErrorsAvoidance(cts));
			if( c == null )
				c = Utils.getRandom(expandForSyntaxErrorsAvoidance(contraintes));
			
			
						
		}while(!(res = c.mutate()) && ( i++ < MAX_MUTATION_ATTEMPTS));
		return res;
	}
	
	private ArrayList<Contrainte> expandForSyntaxErrorsAvoidance(ArrayList<Contrainte> cts){
		ArrayList<Contrainte> res = new ArrayList<>();
		for (Contrainte c : cts) {
			if(c.getSyntaxErrors() == 0) 
				res.add(c);
			res.add(c);
			res.add(c);
		}
		return res;
	}
	
	
	
	@Override
	public int size() {
		return contraintes.size();
	}
	@Override
	public ArrayList<? extends Gene> getGenes() {
		return getContraintes();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean addGene(Gene g) {
		return addContrainte((Contrainte)g);
	}
	
	@Override
	public Program clone() {
		Program p = new Program(metamodel);
		for (Contrainte contrainte : contraintes) {
			p.addContrainte((Contrainte)contrainte.clone());
		}
		return p;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || (getClass() != o.getClass()))
			return false;
		Program p = (Program) o;
		boolean res = true;
		for (int i = 0; i < contraintes.size(); i++) {
			if(!contraintes.get(i).equals(p.contraintes.get(i)))
				return false;
		}
		return res;
	}
	
	@Override
	public String prettyPrint(String tab) {
		String res = tab+getName()+" "+(fitnessVector != null ? fitnessVector.expandedStat()+" ":"")+"{\n";
		for (Contrainte contrainte : contraintes) {
			res += tab+Utils.TAB_CHAR+" - "+contrainte.getOCL_standaloneExecutable()+"\n";
		}
		String sErr = "";
		for (Contrainte contrainte : contraintes) {
			if(contrainte.getSyntaxErrors()>0)
				sErr += contrainte.printSyntaxErrors(tab)+"\n";
		}
		if(!sErr.isEmpty())
			res += tab+" -- Syntax errors:\n"+sErr+"\n";
		res += "}";
		return res;
	}
	@Override
	public String simplePrint(String tab) {
		String res = "";
		for (Contrainte contrainte : contraintes) 
			res += ", "+contrainte.getName();
		res = res.trim();
		if(res.startsWith(","))
			res = res.substring(2);
		return tab+getName()+"{"+ res + "}";
	}
	@Override
	public String printResultPane() {
		// TODO Auto-generated method stub
		return toString();
	}
	
	public String printContraintes(String tab) {
		String res = tab+getName()+"{\n";
		for (Contrainte contrainte : contraintes) {
			res += tab+Utils.TAB_CHAR+" - "+contrainte.prettyPrint()+"\n";
		}
		res += "}";
		return res;
	}


	
	public String printExecutableOCL() {
		String res = getName()+(fitnessVector!=null ? " ("+fitnessVector.prettyPrint()+")":"")+": \n";
		for (Contrainte contrainte : contraintes) {
			res += "  - "+contrainte.getOCL_standaloneExecutable()+"\n";
		}
		return res;
	}
	public String getOCL_standaloneExecutable() {
		String res = "import '"+Metamodel.getMm2().getEcoreFile().getName()+"' \n"
				+ "package "+Metamodel.getMm2().getRootPackage().getName() + "\n\n"+
				"-- "+getName()+(fitnessVector!=null ? " ("+fitnessVector.prettyPrint()+")":"")+": \n";
		for (Contrainte contrainte : contraintes) {
			res += ""+contrainte.getOCL_standaloneExecutable()+"\n";
		}
		return res + "endpackage \n";
	}

	public String printModifications_left() {
		String res = name+".modifications = {";
		for (Contrainte c : contraintes) {
			res += "\n  " + c.printModifications();
		}
		return res+"\n}";
	}

	public String printPastMutations() {
		String res = name+".mutations = {";
		for (Contrainte c : contraintes) {
			if(c.hasBeenMutated())
				res += "\n  " + c.printPastMutations();
		}
		return res+"\n}";
	}

	public String printSyntaxErrors(String tab) {
		String res = "";
		for (Contrainte c : contraintes) {
			String tmp =  c.printSyntaxErrors(tab);
			if(!tmp.isEmpty())
				res += tmp + "\n";
		}
		if(!res.isEmpty())
			res = "SyntaxErrors:\n"+res;
		return res;
	}



}

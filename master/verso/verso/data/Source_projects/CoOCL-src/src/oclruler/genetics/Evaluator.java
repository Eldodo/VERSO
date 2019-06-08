package oclruler.genetics;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.OCLInput;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.Query;
import org.eclipse.ocl.SemanticException;
import org.eclipse.ocl.ecore.CallOperationAction;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.ecore.EvaluationVisitorImpl;
import org.eclipse.ocl.ecore.PrimitiveType;
import org.eclipse.ocl.ecore.SendSignalAction;
import org.eclipse.ocl.ecore.parser.OCLAnalyzer;
import org.eclipse.ocl.expressions.CallExp;
import org.eclipse.ocl.expressions.IfExp;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.expressions.OperationCallExp;
import org.eclipse.ocl.expressions.VariableExp;
import org.eclipse.ocl.expressions.util.ExpressionsValidator;
import org.eclipse.ocl.helper.ConstraintKind;
import org.eclipse.ocl.helper.OCLHelper;
import org.eclipse.ocl.internal.helper.HelperUtil;
import org.eclipse.ocl.pivot.evaluation.EvaluationVisitor;
import org.eclipse.ocl.util.OCLStandardLibraryUtil;
import org.eclipse.ocl.util.OCLUtil;
import org.eclipse.ocl.utilities.TypedElement;

import coocl.ocl.Contrainte;
import coocl.ocl.Program;
import oclruler.metamodel.Metamodel;
import utils.Config;
import utils.Utils;

public class Evaluator {
	public final static Logger LOGGERstruct = Logger.getLogger(Evaluator.class.getName()+".structure");
	public final static Logger LOGGEReval = Logger.getLogger(Evaluator.class.getName()+".evaluation");

	/**
	 * For testing purpose.
	 */
	Evolutioner evo;
	public void setEvo(Evolutioner evo) {
		this.evo = evo;
	}
	
	
	public static int MAX_EXEC_PER_THREAD 	= 3;
	public static boolean MULTI_THREAD 		= false;
	
	
	/**
	 * 
	 * @param ms : <code>Oraculized</code> model set.
	 */
	public Evaluator() {
		
	}
	
	
	public FitnessVector evaluate(GeneticEntity e) {
		FitnessVector fv;
		if (MULTI_THREAD) {
			fv = getEvaluation_multiThread(e);
			if (LOGGEReval.getLevel()!= null && LOGGEReval.getLevel().equals(Level.FINE))
				LOGGEReval.fine(e.getName() + " : Multi thread evaluation : " + fv);
		} else {
			fv = getEvaluation_monoThread(e);
			if (LOGGEReval.getLevel()!= null && LOGGEReval.getLevel().equals(Level.FINE))
				LOGGEReval.fine(e.getName() + " :  Single thread evaluation : " + fv);
		}
		e.setFitnessVector(fv);
		return fv;
	}
	
	/**
	 * Simulate an evaluation : but JESS is off and avg (0.5/1) values are given.<br/>
	 * Evolutioner 'evo' must be set !
	 * @param e
	 * @return
	 */
	protected FitnessVector evaluateFAKE(GeneticEntity e) {
		if (evo == null) {
			LOGGERstruct.severe("An evolutioner must be set to run FAKE evaluation.");
			System.exit(1);
		}
		try {
			LOGGERstruct.finer("Fake evaluation.");
			Thread.sleep(5);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public static <T> ArrayList<Collection<T>> dichoSplit(Collection<T> c, int max_size) {
		int l = c.size();
		ArrayList<Collection<T>> res = new ArrayList<>();
		if (l <= max_size) {
			res.add(c);
			return res;
		} else {
			res.addAll(dichoSplit(Utils.split(c).get(0), max_size));
			res.addAll(dichoSplit(Utils.split(c).get(1), max_size));
			return res;
		}
	}

	public static <T> ArrayList<Collection<T>> dichoSplit(T[] c, int max_size) {
		int l = c.length;
		ArrayList<Collection<T>> res = new ArrayList<>();
		if (l <= max_size) {
			ArrayList<T> tmp = new ArrayList<T>(c.length);
			for (T t : c)
				tmp.add(t);
			res.add(tmp);
			return res;
		} else {
			res.addAll(dichoSplit(Utils.split(c).get(0), max_size));
			res.addAll(dichoSplit(Utils.split(c).get(1), max_size));
			return res;
		}
	}

	/**
	 * NOT IMPLEMENTED == MONO THREAD
	 * 
	 * To be distributed on more than one CPU !
	 * @param e MUST BE A <code>Program</code>
	 * @return
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public FitnessVector getEvaluation_multiThread(GeneticEntity e) {
//		return getEvaluation_monoThread(e);
		long timeBefore = System.currentTimeMillis();
		Program prg = (Program) e;
		
		Contrainte[] css = (Contrainte[]) prg.getContraintes().toArray(new Contrainte[prg.getContraintes().size()]);
		
//		ArrayList<Contrainte> css = new ArrayList<>(prg.getContraintes().size());
//		css.addAll(prg.getContraintes());
		
		ArrayList<Collection<Contrainte>> contraintess = dichoSplit(css, MAX_EXEC_PER_THREAD);
//		
		if (LOGGERstruct.isLoggable(Level.FINER)) {
			String log = prg.getContraintes().size() + " -> " + contraintess.size() + " (";
			for (Collection<Contrainte> collection : contraintess)
				log += " " + collection.size();
			log += (")");
			LOGGERstruct.finer(log);
		}
		
		ArrayList<Callable<Pair<Integer,Integer>>> execs = new ArrayList<>(contraintess.size());
		for (Collection<Contrainte> collection : contraintess)
			execs.add( new Caller(collection));
		
		// Pool with 4 threads
		ArrayList<Future<Pair<Integer,Integer>>> futures = new ArrayList<>(contraintess.size());
		ExecutorService pool = Executors.newFixedThreadPool(execs.size());
		for (Callable<Pair<Integer,Integer>> call : execs) 
			futures.add(pool.submit(call));
		
		
		
//		try {
//			Thread.sleep(5);
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
		
		Pair<Integer,Integer> syntaxErrors = new Pair<Integer, Integer>(0, 0);
		pool.shutdown();
		try {
			pool.awaitTermination(5000, TimeUnit.MILLISECONDS);
			
			if(LOGGERstruct.isLoggable(Level.FINEST))
				LOGGERstruct.finest("Evaluation's duration: "+Utils.formatMillis(System.currentTimeMillis()-timeBefore));
		
			// Get results 
			
			for (Future<Pair<Integer,Integer>> future : futures)
				try {
//					Pair<Integer, Integer> res = future.get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
//					syntaxErrors.left += res.getLeft();
//					syntaxErrors.right += res.getRight();
					future.get();
				} catch ( ExecutionException e1) {
					e1.printStackTrace();
				}
			

		} catch (InterruptedException ei) {
			// TODO Auto-generated catch block
			ei.printStackTrace();
		}
		FitnessVector fv = new FitnessVector(prg);
		prg.setFitnessVector(fv);
		return fv;
	}

	public class Caller implements Callable<Pair<Integer, Integer>> {
		Collection<Contrainte> contraintes;

		@Override
		public Pair<Integer, Integer> call() throws Exception {
			Pair<Integer, Integer> res = computeSyntaxAndLossCount(contraintes);
			return res;
		}

		public Caller(Collection<Contrainte> cs) {
			super();
			this.contraintes = cs;
		}
	}
	
	public Pair<Integer, Integer> computeSyntaxAndLossCount(Collection<Contrainte> contraintes) {
		long t = System.currentTimeMillis();
		int syntaxErrors = 0;
		int infoLoss = 0;
		for (Contrainte c : contraintes) {
			ArrayList<String> sErr = checkSyntaxOCL(c);
			syntaxErrors += c.setSyntaxErrors(sErr.size());
			c.setSyntaxErrorsText(sErr);
			infoLoss += c.evaluateInformationLoss();
		}
		Pair<Integer, Integer> res = new Pair<Integer, Integer>(syntaxErrors, infoLoss);

		LOGGERstruct.finest(contraintes + " : " + res + " (duration=" + Utils.formatMillis(System.currentTimeMillis() - t) + ")");

		return res;
	}


	/**
	 * To be distributed on more than one CPU !
	 * @param e MUST BE A <code>Program</code>
	 * @return
	 */
	protected FitnessVector getEvaluation_monoThread(GeneticEntity e) {
		// System.out.println("\nEvaluator.getEvaluation_monoThread("+e.getName()+")");
		long timeBefore = System.currentTimeMillis();
		Program prg = (Program) e;

		Pair<Integer, Integer> syntaxCount = computeSyntaxAndLossCount(prg.getContraintes());

//		System.out.println(prg.getSyntaxErrorCount() + ", " + prg.getInfoLossCount() + " || " + syntaxCount);

		FitnessVector fv = new FitnessVector(prg);
		prg.setFitnessVector(fv);
		return fv;
	}



//	static int loop = 1;
	public ArrayList<String> checkSyntaxOCL(Contrainte c){
//		System.out.println("Evaluator.checkSyntaxOCL loop :" + loop);
		ArrayList<String> res = new ArrayList<>(1);
		OCL<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject,  CallOperationAction, SendSignalAction, Constraint, EClass, EObject> ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
	 
		OCLHelper<EClassifier, ?, ?, Constraint> helper = ocl.createOCLHelper();
		String queryStr =  c.getOCL_Body();
//		if(c.getNumber() == 3 
//				&&queryStr.equals(Program.getExpectedSolution().getContrainte(3).getOCL_Body())
//				)
//			System.out.println("1."+queryStr+"\n2."+Program.getExpectedSolution().getContrainte(3).getOCL_Body());
		EPackage ep = Metamodel.getMm2().getRootPackage();
//		System.out.println(ep.getEClassifier(c.getContext().getName()));
		EClassifier ecClass = ep.getEClassifier(c.getEContext().getName());
		
		helper.setContext(ecClass);
		try {
//				System.out.println("        *1* "+c.getName()+" : "+res);
			helper.getSyntaxHelp(ConstraintKind.INVARIANT, queryStr);
			helper.createInvariant(queryStr);
		} catch (ParserException e) {
				Diagnostic diagnostic = e.getDiagnostic();
				ArrayList<String> diagnostics = Evaluator.getDiagnosticList(diagnostic);
				res.addAll(diagnostics);
//				res += diagnostics.size();
//				System.out.println("        *2* "+c.getName()+" : "+res);
				
//				for (String diag : diagnostics) {
//					if(diag.endsWith("(weight)")){
//						System.out.println(("Exception thrown during OCL execution : \nConstraint "+c.getName()+"\nocl: "+queryStr +"\ndiagnostic: "+"("+diagnostics.size()+") \n "+diag.trim()));
//						
//					}
//				}
					
				
				
				if(LOGGEReval.isLoggable(Level.FINER)){
					String strDiags = "";
					for (String diag : diagnostics) 
						strDiags += " - "+diag+"\n";
					LOGGEReval.finer("Exception thrown during OCL execution : \nConstraint "+c.getName()+"\nocl: "+queryStr +"\ndiagnostic: "+"("+diagnostics.size()+") \n "+strDiags.trim());
				}
		} catch (NullPointerException e) {
			res.add(e.getMessage());
			if(LOGGEReval.isLoggable(Level.FINER))
				LOGGEReval.finer("Exception thrown during OCL parsing : \nConstraint "+c.getName()+"\nocl: "+queryStr +"\ndiagnostic: "+ e.getMessage());
		}
		return res;
	}
	
	public static ArrayList<String> getDiagnosticList(Diagnostic d){
		ArrayList<String> res = new ArrayList<>();
		if(!d.getMessage().contains("oclIsTypeOf"))
			res.add(d.getMessage());
		for (Diagnostic dd : d.getChildren()) {
			res.addAll(getDiagnosticList(dd));
		}
		return res;
	}

	
	public static void loadConfig() {
		try {
			MULTI_THREAD 		= Config.getBooleanParam("MULTI_THREAD");
			MAX_EXEC_PER_THREAD = Config.getIntParam("MAX_EXEC_PER_THREAD");
		} catch (Exception e){
			
		}
//		ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
//		helper = ocl.createOCLHelper();
	}

	
	private static BasicDiagnostic diagnoseResource(EObject eObject) {
		BasicDiagnostic diagnosticChain = new BasicDiagnostic();
		Diagnostician.INSTANCE.validate(eObject, diagnosticChain);
		return diagnosticChain;
	}
	
	public class Pair<L,R> {

		  private  L left;
		  private  R right;

		  public Pair(L left, R right) {
		    this.left = left;
		    this.right = right;
		  }

		  public L getLeft() { return left; }
		  public R getRight() { return right; }

		  @Override
		  public int hashCode() { return left.hashCode() ^ right.hashCode(); }

		  @Override
		  public boolean equals(Object o) {
		    if (!(o instanceof Pair)) return false;
		    Pair pairo = (Pair) o;
		    return this.left.equals(pairo.getLeft()) &&
		           this.right.equals(pairo.getRight());
		  }
		  
		  @Override
		public String toString() {
			// TODO Auto-generated method stub
			return "("+left+", "+right+")";
		}

		}
}

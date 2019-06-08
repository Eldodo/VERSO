package oclruler.genetics;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.helper.OCLHelper;

import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.FireMap;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Model;
import oclruler.rule.Program;
import oclruler.rule.patterns.Pattern;

public class EvaluatorOCL extends Evaluator {
	public final static Logger LOGGER = Logger.getLogger(Evaluator.class.getName());

	/**
	 * Get curent Singleton or instantiates a new one if current null or forceNewInstance=true.<br/>
	 * Do not instantiate new ExampleSet (see {@link ExampleSet#getInstance()})
	 * @param forceNewInstantiation
	 * @return
	 */
	public static Evaluator getInstance(boolean forceNewInstantiation){
		if(forceNewInstantiation || instance == null)
			instance = new EvaluatorOCL(ExampleSet.getInstance());
		return instance;
	}
	
	/**
	 * Get curent Singleton or instantiates a new one if current is null.<br/>
	 * Do not instantiate new ExampleSet (see {@link ExampleSet#getInstance()})
	 * @param forceNewInstantiation
	 * @return
	 */
	public static Evaluator getInstance(){
		if(instance == null)
			instance = new EvaluatorOCL(ExampleSet.getInstance());
		return instance;
	}
	
	/**
	 * 
	 * @param ms : <code>Oraculized</code> model set.
	 */
	public EvaluatorOCL(ExampleSet ms) {
		super(ms);
	}
	
	protected  FireMap execute(Collection<Model> modelList, Program prg){
		numberOfEvaluations++;
		long t = System.currentTimeMillis();
		FireMap res = new FireMap();
		Model[] ms = (Model[]) modelList.toArray(new Model[modelList.size()]);
		for (Model model : ms) {
			execute(res, model, prg);
		}
		t = System.currentTimeMillis() - t;
		evaluationTime += t;
		return res;
	}
	
	/**
	 * Execute Program on a Model and fille the FireMap given as first parameter
	 * <em>Attention, that method depends on the GRAIN defined in <code>EXECUTION_GRAIN</code> static var. To compute ALL FIRES, set the <code>EXECUTION_GRAIN</code> to FINE before execution.</em>
	 * @param fm2
	 * @param m
	 * @param prg
	 * @return FireMap concerning only the Model m and Program prg (fm2 not considered).
	 */
	static int loop = 1;
	public static FireMap execute(FireMap fm2, Model m, Program prg){
		FireMap fm = new FireMap();
		boolean toContinue = true;
		OCL<?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject> ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
		OCLExpression<EClassifier> query = null;
		OCLHelper<EClassifier, ?, ?, Constraint> helper = ocl.createOCLHelper();
		oclruler.rule.struct.Constraint lastOCL = null;
		try {
			oclruler.rule.struct.Constraint[] cs = (oclruler.rule.struct.Constraint[]) prg.getConstraints().toArray(new oclruler.rule.struct.Constraint[prg.getConstraints().size()]);
			
			for (oclruler.rule.struct.Constraint ct : cs) {
				if(!ct.equals(lastOCL)){
					EClassifier ecClass = ct.getContext().getEClassifier();
						lastOCL = ct;
					int fires = 0;
					helper.setContext(ecClass);
//					System.out.println(m.prettyPrintEObjects());
//					System.out.println(" -> "+ct);
					try {
						ct.prune();
					} catch (Exception e) {
						e.printStackTrace();
					}
					for (EObject eo : m.getEobjects(ct.getContext().getName())) {
						query = helper.createQuery(ct.getRawOCLConstraint());
	//					Constraint invariant = helper.createInvariant(p.getOCL());
						
						
						
						boolean valid = ocl.check(eo, query);
						
//						if(ct.getRawOCLConstraint().contains("includesAll") && valid){
//							System.out.println(ct.getRawOCLConstraint());
//							System.out.println("  "+ ToolBox.printEStructuralFeatures(eo));;
//						}
						
//						if(ct.getOCL_inv().contains("cycle__5") && !valid){
//							System.out.println("EvaluatorOCL.execute()");
//							System.out.println("!"+ ct.getRawOCLConstraint());
//							System.out.println("!  "+ ToolBox.printEStructuralFeatures(eo));;
//							System.out.println(ecClass.getName());
//						}
						
						if(!valid) {
							fires++;
							fm.addFiredObject(m, eo, ct);
						}
						
						//In RAW grain, the validation stop with the first rule/constraint firing.
						if(!valid && EXECUTION_GRAIN.isRaw()){
							toContinue = false;
							if(LOGGER.isLoggable(Level.FINER))
								LOGGER.finer("Execution is 'raw', '"+m.getFileName()+"' fired rule "+ct.getName()+" -> Invalid. Break.");
							break;
						}
					}
					fm.addFire(m, ct, fires);
					ct.cleanFires();
					if(!toContinue)
						break;
				}
			}
//			if(fm.getFiredObjects(m) != null && fm.getFiredObjects(m).size() > 1)
//				System.out.println("EvaluatorOCL.execute() -> #firedObjects = "+fm.getFiredObjects(m).size());
		} catch (ParserException e) {
				Diagnostic diagnostic = e.getDiagnostic();
				LOGGER.severe("Exception thrown during OCL execution : \nConstraint "+lastOCL.getId()+"\ncontext:"+lastOCL.getContext()+"\ninv:\n"+lastOCL.getOCL() +"\n"+ diagnostic.getMessage());
				System.out.println("root:"+lastOCL.getRoot());
				System.out.println("root.context:"+lastOCL.getRoot().getContext());
				System.out.println("root.context.refs:"+lastOCL.getRoot().getContext().getReferences());
				System.out.println(Metamodel.getUnInstantiableConcept());
//				System.out.println(lastOCL.getRoot().prettyPrint());
//				System.out.println(lastOCL.getRoot().getOCL());
//				System.out.println(rule.struct.Constraint.numberOfInstances());
				e.printStackTrace();
				System.out.println("EvaluatorOCL.execute : Manual exit.");
				System.exit(0);
		}
		if(fm2 != null)
			fm2.merge(fm);
		return fm;
	}
	
	/**
	 * 
	 * @param m Model
	 * @param prg Program
	 * @return List of patterns. Each associated with URIs of the objects that fire it.
	 */
	public static HashMap<oclruler.rule.struct.Constraint, ArrayList<String>> diagnose(Model m, Program prg){
		HashMap<oclruler.rule.struct.Constraint,  ArrayList<String>> res = new HashMap<>();
		
		if(m.getResource() == null){
			LOGGER.severe("Model '"+m+"' : No diagnosis available (JESS models not accepted).");
			return res;
		}
		try {
			m.reload();
		} catch (Exception e1) {
			LOGGER.severe("Couldn't reload model : "+m.getFileName()+" ("+e1.getMessage()+")");
//			e1.printStackTrace();
		}
		int modelURIlength = (m.getResource() != null)?m.getResource().getURI().toString().length()+1:0;// +1 because of the '#' before ids in XMI
		OCL<?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject> ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
		OCLExpression<EClassifier> query = null;
		OCLHelper<EClassifier, ?, ?, Constraint> helper = ocl.createOCLHelper();
		try {
			// create an OCL helper object

			for (oclruler.rule.struct.Constraint p : prg.getConstraints()) {
				EClassifier ecClass = p.getContext().getEClassifier();
				helper.setContext(ecClass);
//				System.out.println(m.prettyPrintEObjects());
//				System.out.println(" -> "+p);
				for (EObject eo : m.getEobjects(p.getContext().getName())) {
					
					query = helper.createQuery(p.getRawOCLConstraint());
//					Constraint invariant = helper.createInvariant(p.getOCL());
					boolean valid = ocl.check(eo, query);
					if (!valid) {
						if(res.get(p) == null) res.put(p, new ArrayList<>());
						String xmiid = EcoreUtil.getURI(eo).toString().substring(modelURIlength);
						res.get(p).add(xmiid);
					}
					
				}
			}

		} catch (ParserException e) {
			Diagnostic diagnostic = e.getDiagnostic();
			LOGGER.severe("Exception thrown during OCL execution : "+ diagnostic.getMessage());
			e.printStackTrace();
		}
		
		for (ArrayList<String> uris : res.values())
			uris.sort( (String o1, String o2) -> o1.compareTo(o2));
		
		return res;
	}

	public static OCLExpression<EClassifier> check(oclruler.rule.struct.Constraint p) throws ParserException {
		OCL<?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject> ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
		OCLHelper<EClassifier, ?, ?, Constraint> helper = ocl.createOCLHelper();
		EClassifier ecClass = p.getContext().getEClassifier();
		helper.setContext(ecClass);
		return helper.createQuery(p.getRawOCLConstraint());
	}

	public static OCLExpression<EClassifier> check(Pattern p) throws ParserException {
		OCL<?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject> ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
		OCLHelper<EClassifier, ?, ?, Constraint> helper = ocl.createOCLHelper();
		EClassifier ecClass = p.getContext().getEClassifier();
		helper.setContext(ecClass);
		return helper.createQuery(p.getRawOCLConstraint());
	}


	public static ArrayList<OCLExpression<EClassifier>> check(Program prg) throws ParserException {
		OCL<?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject> ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
		OCLHelper<EClassifier, ?, ?, Constraint> helper = ocl.createOCLHelper();
		ArrayList<OCLExpression<EClassifier>> res = new ArrayList<>();
		
		for (oclruler.rule.struct.Constraint c : prg.getConstraints()) {
			EClassifier ecClass = c.getContext().getEClassifier();
			helper.setContext(ecClass);
//			System.out.println("context "+ecClass.getName()+"\n"+c.getOCL());
			helper.createQuery(c.getOCL());
		}
		return res;
	}
	
}

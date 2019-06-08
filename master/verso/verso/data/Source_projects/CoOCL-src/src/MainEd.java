import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreFactory;
import org.eclipse.ocl.ecore.IteratorExp;
import org.eclipse.ocl.ecore.PrimitiveType;
import org.eclipse.ocl.expressions.BooleanLiteralExp;
import org.eclipse.ocl.expressions.IfExp;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.expressions.OperationCallExp;
import org.eclipse.ocl.expressions.PropertyCallExp;
import org.eclipse.ocl.expressions.VariableExp;
import org.eclipse.ocl.utilities.ExpressionInOCL;
import org.eclipse.ocl.utilities.TypedElement;

import coocl.ocl.CollectOCLIds;
import coocl.ocl.Contrainte;
import coocl.ocl.Program;
import oclruler.genetics.Evaluator;
import oclruler.genetics.Evolutioner;
import oclruler.genetics.FitnessVector;
import oclruler.genetics.GeneticEntity;
import oclruler.genetics.Population;
import oclruler.genetics.UnstableStateException;
import oclruler.metamodel.Concept;
import oclruler.metamodel.MMElement;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.MetamodelMerger;
import oclruler.metamodel.MetamodelMerger.DIFF_TYPE;
import oclruler.metamodel.Reference;
import oclruler.metamodel.StructuralFeature;
import utils.Config;
import utils.Utils;
import utils.distance.DoublePointProgram;

public class MainEd {


	static Logger LOGGER = Logger.getLogger(MainEd.class.getName());
	static Logger LOGGER_batch = Logger.getLogger("LOGGER_batch");
	
	
	public static void main(String[] args) {
		Utils.init(args[0]);
		
		MetamodelMerger merger = MetamodelMerger.getInstance();
		Program prg0 = Program.getInitialProgram();
		System.out.println(prg0.getOCL_standaloneExecutable());
		System.out.println(Program.getExpectedSolution().getOCL_standaloneExecutable());
		
		prg0.getContrainte(1).mutate();
		prg0.getContrainte(1).mutate();
		prg0.getContrainte(1).mutate();
		prg0.getContrainte(1).mutate();
		prg0.getContrainte(1).mutate();
		
		for (DIFF_TYPE dt : DIFF_TYPE.values()) {
			System.out.println("+ "+dt);
			for (MMElement mme : merger.getDiffMMELements(dt)) {
				System.out.println("  - "+mme);
			}
		}
		System.out.println("Exit - sound and safe.");
		System.exit(0);
		
		for (StructuralFeature sf : merger.getRemovedStructuralFeatures()) {
			Concept c = sf.getType();
			if(c.isEnum()){
				System.out.println(c+":");
				for (String lit : ((oclruler.metamodel.Enum)sf.getType()).literalValues()) {
					System.out.println(lit);
//					System.out.println("  "+Utils.capOnFirstLetter(lit)+sf.getSourceClassName());
				}
				
				
				for (Contrainte cst : prg0.getContraintes()) {
					System.out.println(cst+":");
					System.out.println("   "+ cst.getOCLElementsAffectedByMMElement(sf));;
				}
		
				
			}
		}
		
		
		
		long t = System.currentTimeMillis();
		Evaluator eva = new Evaluator();
		LOGGER_batch.config("Generating initial population of "+Population.POPULATION_SIZE+" entities.");
		Population pop0 = Population.createRandomPopulation(Program.getInitialProgram(), eva);
		LOGGER_batch.config("Evaluating initial population.");
		pop0.evaluate(eva);

		Evolutioner evo = new Evolutioner(eva, pop0);
		LOGGER_batch.config("Evolution starts for "+Evolutioner.GENERATION_MAX+" generations...");
		Population popN = evo.evolutionate();
		LOGGER_batch.config("Evolution finished !");

		LOGGER_batch.finer(popN.printStatistics());

		Program best = ((Program) popN.getBest());

		Program prgGroundTruth = Program.getExpectedSolution();
		eva.equals(prgGroundTruth);
		
		boolean inParetoB = Evolutioner.checkPresenceOfGroundTruthInPopulation(popN, Program.getExpectedSolution()) != null;
		
		double dist = Double.MAX_VALUE;
		Program closest = null;
		for (GeneticEntity ge : popN.getFrontPareto().getEntities()) {
			Program p = (Program) ge;
			if(p.getDistance(prgGroundTruth) < dist){
				dist = p.getDistance(prgGroundTruth);
				closest = p;
			}
		}
		
		
		
		List<CentroidCluster<DoublePointProgram>> clustersKM = Evolutioner.clusterFrontParetoFromPopulation(popN);
		// On recupere les centres des clusters
		String textSolution = " ";
		boolean foundInCenters = false;
		int j = 0;
		for (CentroidCluster<DoublePointProgram> clusterKM : clustersKM) {
			// For each cluster check if solution and compute distance to
			// GroundTruth
			DoublePointProgram center = Evolutioner.extractCenterFromCluster(clusterKM);
			textSolution += "Cluster "+ j++ +"  "+center.getProgram() +" : "+Arrays.toString(center.getProgram().computeDamerauLevensteinDistances(prgGroundTruth))+")";
//					+ "\n"+center.getProgram().printExecutableOCL()
//					+ ""+center.getProgram().printSyntaxErrors("")+"\n";
			
			if(center.equals(Program.getExpectedSolution()))
				foundInCenters = true;
		}
		
		textSolution += "Closest in pareto: "+closest+" : "+Arrays.toString(closest.computeDamerauLevensteinDistances(prgGroundTruth))+"\n"
					+ closest.printExecutableOCL()
					+ closest.printSyntaxErrors("")+"\n";
		
		LOGGER_batch.config(inParetoB ? "Ground truth in pareto front":"Ground truth NOT in pareto front");
		LOGGER_batch.config(foundInCenters ? "Ground truth in centroids":"Ground truth NOT in centroids");
		LOGGER_batch.fine("From:        \n" + prg0.prettyPrint("  "));
		LOGGER_batch.fine("Expected is: \n" + prgGroundTruth.prettyPrint("  "));
		LOGGER_batch.fine("Closest is: "
							+ "(Levenstein distance to ground truth: "+Arrays.toString(closest.computeDamerauLevensteinDistances(prgGroundTruth))+")\n" + closest.prettyPrint("  "));
		LOGGER_batch.fine("Best is:     "
							+ "(Levenstein distance to ground truth: "+Arrays.toString(best.computeDamerauLevensteinDistances(prgGroundTruth))+")\n" + best.prettyPrint("  "));
		LOGGER_batch.finer(""+best.printModifications_left());
		LOGGER_batch.finer(""+best.printPastMutations());
		
		String textLine = popN.printStatisticsLine() + ";"+Utils.formatMillis(System.currentTimeMillis() - t);
		LOGGER_batch.info("" + textLine);
//		LOGGER_batch.info("Solutions:"+textSolution);
		
		System.out.println("Exit !");
	}

	

	
	/**
	 * Create a population of Population.POPULATION_SIZE*Evolutioner.GENERATION_MAX entities.
	 * 
	 * @param args for config
	 */
	
	
	public static void mainEvolution_FindSolutionInParetoFront(String[] args) {
		
		Utils.init(args[0]);
		
		Evaluator eva = new Evaluator();
		System.out.println("MainEd.mainEvolution - Generating initial population");
		Population pop0 = Population.createRandomPopulation(Program.getInitialProgram(), eva);
		System.out.println("MainEd.mainEvolution - Evaluating initial population");
		pop0.evaluate(eva);
		
		Evolutioner evo = new Evolutioner(eva, pop0);
		System.out.println("MainEd.mainEvolution - Evolution starts...");
		Population popN = evo.evolutionate();
		System.out.println("MainEd.mainEvolution - Done !\n\n");
		
		
		
		System.out.println(popN.printStatistics());
		System.out.println();
		
		
		Program best = ((Program)popN.getBest());
		
		Program prg0 = Program.getInitialProgram();
		Program prgGroundTruth = Program.getExpectedSolution();
		
		
		System.out.println("From:        " + prg0.prettyPrint());
		System.out.println("Expected is: " + prgGroundTruth.prettyPrint());
		System.out.println("Best is:     " + best.prettyPrint());
		System.out.println(best.printModifications_left());
		System.out.println(best.printPastMutations());
		
		System.out.println("\n\n\n ---------------- Check ground Truth ----------------\n");
		Evolutioner.checkPresenceOfGroundTruthInPopulation(popN, prgGroundTruth);
		
	}

	


	
	public static void main_(String[] args) {
		Utils.init(args[0], "R:/EclipseWS/material/CoOCL/test/ocl/test_Family_collapse.ocl", "Family");
		
		Program prg0 = Program.getInitialProgram();
		Program prgClone = (Program)prg0.clone();
		
//		Contrainte ct0 = prg0.getContrainte(0);
//		System.out.println(ct0.prettyPrint());
//		MMElement mme0 = Metamodel.getMm1().getConcept("P").getStructuralFeature("age");
//		TypedElement<?> te0 = ct0.getOCLElementsAffectedByMMElement(mme0).get(0);
//		ct0.collapse(mme0, te0);
//		System.out.println();
//		System.out.println("ct0: "+ct0.prettyPrint());
		
		System.out.println(prg0.prettyPrint());
		
		System.out.println();

		Contrainte ct0 = prg0.getContrainte("testPath");
		System.out.println(ct0.getPropertyCallExpInvolved(ct0.getEBodyExpression()));
		
		System.out.println();
		System.out.println("testPermis");
		ct0 = prg0.getContrainte("testPermis");
		System.out.println(ct0.getPropertyCallExpInvolved(ct0.getEBodyExpression()));
		
		System.out.println();
		System.out.println("testPermis2");
		ct0 = prg0.getContrainte("testPermis2");
		System.out.println(ct0.getPropertyCallExpInvolved(ct0.getEBodyExpression()));
		
		System.out.println();
		System.out.println();
		
		
		Contrainte ct1 = prg0.getContrainte(1);
		System.out.println("1. ct1: "+ct1.prettyPrint());
		StructuralFeature mme = Metamodel.getMm1().getConcept("P").getStructuralFeature("age");
		TypedElement<?> te1 = ct1.getOCLElementsAffectedByMMElement(mme).get(0);
		ct1.collapse(mme, te1);
		System.out.println("2. ct1: "+ct1.prettyPrint());
		
		
		System.out.println();
		Contrainte ct2 = prg0.getContrainte("testPermis2");
		System.out.println("1. ct2: "+ct2.prettyPrint());
		StructuralFeature mme2 = Metamodel.getMm1().getConcept("P").getStructuralFeature("permis");
//		System.out.println("affecting ct2 : "+ct2.getOCLElementsAffectedByMMElement(mme2));
		TypedElement<?> te2 = ct2.getOCLElementsAffectedByMMElement(mme2).get(0);
		ct2.collapse(mme2, te2);
		System.out.println("2. ct2: "+ct2.prettyPrint());
		
		
		
		
		Contrainte ct = prg0.getContrainte("testPermis2");
		System.out.println(ct.prettyPrint());
		TypedElement<?> te4 = (TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0).eContents().get(2).eContents().get(2);
		System.out.println(" >> "+te4);
		
		System.out.println("Index path: "+Contrainte.getIndexPath(te4));
		System.out.println(ct.getEConstraint().eContents().get(0));
		System.out.println(ct.getEConstraint().eContents().get(0).eContents().get(0));
		System.out.println(ct.getEConstraint().eContents().get(0).eContents().get(0).eContents().get(2));
		System.out.println(ct.getEConstraint().eContents().get(0).eContents().get(0).eContents().get(2).eContents().get(2));
		System.out.println();
		
		Contrainte ct3 = (Contrainte)ct.clone();
		
		System.out.println("ct3 : "+ct3.prettyPrint());
		
		TypedElement<?> te4eq = (TypedElement<?>)ct3.getEquivalent(te4);
		System.out.println();
		System.out.println(te4eq);
		System.out.println(te4);
		System.out.println(te4eq.eContainer());
		System.out.println(te4.eContainer());
		
		prg0.clone();
	}
	
	public static void main_Clone_n_Equals(String[] args) {
		
		Utils.init(args[0], "R:/EclipseWS/material/CoOCL/test/ocl/test_Family_1.ocl", "Family");
		Program prg = Program.getInitialProgram();
		Program prgClone = (Program)prg.clone();
		
		if(!prg.equals(prgClone))
			throw new IllegalStateException("prg != prg.clone() ! \n\tprg: "+prg+"\n\tclone: "+prgClone);
		
		Contrainte ct = new Contrainte(Metamodel.getMm1().getConcept("P"), "(self.m <> self) or l.k->size() < 20 ");
		Contrainte ctClone = (Contrainte)ct.clone();
		
		if(!ct.equals(ctClone))
			throw new IllegalStateException("ct != ct.clone() ! \n\tct: "+ct+"\n\tclone: "+ctClone);
		
		prg.addContrainte(ct); 
		prgClone.addContrainte(ctClone); 
		
		if(!prg.equals(prgClone))
			throw new IllegalStateException("prg + ct != prgClone + ctClone ! \n\tprg: "+prg+"\n\tclone: "+prgClone);

		// One prg's constraints modified.
		TypedElement<?> te2 = (TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0).eContents().get(1).eContents().get(1);
		te2.eSet(te2.eClass().getEStructuralFeature("referredProperty"), Metamodel.getMm2().getConcept("P").getStructuralFeature("d").geteStructuralFeature());
		
		ct.setModified(true);
		
		
		if(ct.equals(ctClone))
			throw new IllegalStateException("ct.mutate == ctClone ! \n\tct: "+ct+"\n\tclone: "+ctClone);
		
		if(prg.equals(prgClone))
			throw new IllegalStateException("prg + ct.mutate() == prgClone + ctClone ! \n\tprg: "+prg+"\n\tclone: "+prgClone);
		
		
		
		try {
			boolean success = prg.mutate();
			if(success && prg.equals(prgClone))
				throw new IllegalStateException("prg == prg.mutate ! \n\tprg: "+prg+"\n\tclone: "+prgClone);
		} catch (UnstableStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		prgClone = (Program)prg.clone();
		
		if(!prg.equals(prgClone))
			throw new IllegalStateException("prg != prg.clone ! \n\tprg: "+prg+"\n\tclone: "+prgClone);
	}
	
	public static void main_RenameContext(String[] args) {
		
		Utils.init(args[0], "R:/EclipseWS/material/CoOCL/test/ocl/test_Family_1.ocl");
		MetamodelMerger mmMerger = MetamodelMerger.getInstance();
		Program prg0 = Program.getInitialProgram();
		
//		System.out.println("prg0: " + prg0.prettyPrint());
		
		ArrayList<MMElement> cardChangeDownFeatures = null;
		
//		for (DIFF_TYPE dt : DIFF_TYPE.values()) { 
//			if(dt == DIFF_TYPE.ADD) continue;
//			
//			cardChangeDownFeatures = mmMerger.getDiffMMELements(dt);
////			System.out.println("cardChangeDownFeatures: " + cardChangeDownFeatures);
//			for (MMElement sf : cardChangeDownFeatures) {
//				System.out.println(sf + " : "+dt);
//				ArrayList<Contrainte> cts = prg0.getContraintesUsingMMElement(sf);
//				System.out.println("cts: " + cts);
//				for (Contrainte ct : cts) {
//					System.out.println(" + ct: " + cts);
//					ArrayList<TypedElement<?>> tes = ct.getOCLElementsAffectedByMMElement(sf);
//					for (TypedElement<?> te : tes) {
//						TypedElement<?> teFirstBool = getFirstBooleanAncestor(te);
//	//					System.out.println(printConstraintAsXML((OCLExpression<?>)teFirstBool));;
//						System.out.println("  - te: " + tes + " bool:"+teFirstBool);
//					}
//				}
//			}
//		}
//		
//		
//		System.out.println();
//		System.out.println();
//		Utils.init(args[0], "R:/EclipseWS/material/CoOCL/test/ocl/test_Family_2.ocl");
//		 mmMerger = MetamodelMerger.getInstance();
//		 prg0 = Program.getInitialProgram();
//		
//		System.out.println("prg0: " + prg0.prettyPrint());
//		
//		for (DIFF_TYPE dt : DIFF_TYPE.values()) {
//			if(dt == DIFF_TYPE.ADD) continue;
//			cardChangeDownFeatures = mmMerger.getDiffMMELements(dt);
////			System.out.println("cardChangeDownFeatures: " + cardChangeDownFeatures);
//			for (MMElement sf : cardChangeDownFeatures) {
//				System.out.println(sf + " : "+dt);
//				ArrayList<Contrainte> cts = prg0.getContraintesUsingMMElement(sf);
//				for (Contrainte ct : cts) {
//					System.out.println(" + ct: " + cts);
//					ArrayList<TypedElement<?>> tes = ct.getOCLElementsAffectedByMMElement(sf);
//					for (TypedElement<?> te : tes) {
//						TypedElement<?> teFirstBool = getFirstBooleanAncestor(te);
//						System.out.println(printConstraintAsXML((OCLExpression<?>)teFirstBool));;
//						System.out.println("  - te: " + tes + " bool:"+teFirstBool);
//					}
//				}
//			}
//		}
//		
		
		System.out.println();
		System.out.println();
		Utils.init(args[0], "R:/EclipseWS/material/CoOCL/test/ocl/test_FamilyPermis_3.ocl", "FamilyPermis");
		mmMerger = MetamodelMerger.getInstance();
		prg0 = Program.getInitialProgram();
		
		System.out.println("prg0: " + prg0.prettyPrint());
		
//		for (DIFF_TYPE dt : DIFF_TYPE.values()) {
//			if(dt == DIFF_TYPE.ADD) continue;
//			cardChangeDownFeatures = mmMerger.getDiffMMELements(dt);
////			System.out.println("cardChangeDownFeatures: " + cardChangeDownFeatures);
//			for (MMElement sf : cardChangeDownFeatures) {
////				System.out.println(sf + " : "+dt);
//				ArrayList<Contrainte> cts = prg0.getContraintesUsingMMElement(sf);
//				for (Contrainte ct : cts) {
//					ct.changeContextType(false);
//				}
//			}
//		}
//		TypedElement<?> te2 = (TypedElement<?> )prg0.getContrainteByEConstraintName("noMorePermis").getEConstraint().eContents().get(0).eContents().get(0).eContents().get(1);
//		System.out.println(te2);;
//		System.out.println(printEStructuralFeatures(te2));
//		te2.eSet(te2.eClass().getEStructuralFeature("referredProperty"), Metamodel.getMm2().getConcept("P").getStructuralFeature("permis").geteStructuralFeature());
		
		
		Contrainte ct = Utils.getRandom(prg0.getContraintes());
		ct.changeContextType(Metamodel.getMm2().getConcept("P"));
		
		System.out.println(prg0.prettyPrint());
		
		Evaluator eva = new Evaluator();
		FitnessVector fv = eva.evaluate(prg0);
		System.out.println(prg0);
		
		
	}

		
	
	public static void mainENCOURS(String[] args) {
		
		Utils.init(args[0], "R:/EclipseWS/material/CoOCL/test/ocl/test_Family_2.ocl");
		
		MetamodelMerger mmMerger = MetamodelMerger.getInstance();
		Program prg0 = Program.getInitialProgram();
		
		System.out.println("prg0: " + prg0.prettyPrint());
		
		ArrayList<StructuralFeature> cardChangeDownFeatures = mmMerger.getDiffStructuralFeatures(DIFF_TYPE.CARDINALITY_UP);
		
		System.out.println("cardChangeDownFeatures: " + cardChangeDownFeatures);
		for (StructuralFeature sf : cardChangeDownFeatures) {
			System.out.println(sf + " : ");
			ArrayList<Contrainte> cts = prg0.getContraintesUsingMMElement(sf);
			for (Contrainte ct : cts) {
				System.out.println(" + ct: " + cts);
				ArrayList<TypedElement<?>> tes = ct.getOCLElementsAffectedByMMElement(sf);
				for (TypedElement<?> te : tes) {
					TypedElement<?> teFirstBool = getFirstBooleanAncestor(te);
					System.out.println(Contrainte.printConstraintAsXML((OCLExpression<?>)teFirstBool));;
					System.out.println("  - te: " + tes + " bool:"+teFirstBool);
				}
			}
		}
	}
	
	
	
	public static void main5(String[] args) {
		Utils.init();
		Program prg0 = Program.getInitialProgram(); //test_Family_1.ocl
		
		// ref_l est récupérer grace au mmMerger (par exemple)
		StructuralFeature ref_l = Metamodel.getMm1().getConcept("P").getStructuralFeature("age");
//		Contrainte ccc = Utils.getRandom(prg0.getContraintes());
//		System.out.println(ccc);
//		ccc.mutate(ref_l);
//		System.out.println(ccc);
//		System.exit(0);
		
		
		
		
		System.out.println("Contraintes in prg0 affected by "+ref_l.getName());
		for (Contrainte cc : prg0.getContraintesUsingMMElement(ref_l)) {
			System.out.println(" + "+cc.getName()+": ");//+cc.getMMElementsAffectingOCL());
//			for (MMElement mme : cc.getMMElementsAffectingOCL().keySet()) {
//				System.out.println("   + "+mme.simplePrint());
				for (TypedElement<?> te : cc.getOCLElementsAffectedByMMElement(ref_l)) {
					System.out.println("    - "+te);
					
					EObject firstBool = getFirstBooleanAncestor(te);
					if(firstBool == null){
						firstBool = getConstraintAncestor(te);
						
					}
					Object eoParent = firstBool.eContainer();
					
					
					
					if(firstBool instanceof TypedElement<?>)
						collapse((TypedElement<?>)firstBool);
					else if(firstBool instanceof Constraint)
						System.out.println("Constraint to collapse !!");
//					collapse(te);
					System.out.println("    - -> "+eoParent);
				}
//			}
		}
		System.out.println("----------------------               2               --------------\n");
		ref_l = Metamodel.getMm1().getConcept("P").getStructuralFeature("test");
		
		System.out.println("Contraintes in prg0 affected by "+ref_l.getName());
		for (Contrainte cc : prg0.getContraintesUsingMMElement(ref_l)) {
			System.out.println(" + "+cc.getName()+": ");//+cc.getMMElementsAffectingOCL());
//			for (MMElement mme : cc.getMMElementsAffectingOCL().keySet()) {
//				System.out.println("   + "+mme.simplePrint());
				for (TypedElement<?> te : cc.getOCLElementsAffectedByMMElement(ref_l)) {
					System.out.println("    - "+te);
					System.out.println("    - -> "+getFirstBooleanAncestor(te));
				}
//			}
		}
		
		
		System.exit(0);
        Contrainte c = prg0.getContrainte("A11b_inv"); //Première contrainte : 

        Reference ref_d = Metamodel.getMm1().getConcept("P").getReference("l");
        System.out.println(ref_d);
        
        
        MetamodelMerger mmMerger = new MetamodelMerger(Metamodel.getMm1(), Metamodel.getMm2(), Program.OCL_FILE);
        
        
        
        System.out.println("affected by '"+ref_d.getName()+"': "+mmMerger.getAffectedOCLElement(ref_d).size()+" elements");;
        
        for (TypedElement<?> te : mmMerger.getAffectedOCLElement(ref_d)) {
			System.out.println(te + " ("+getConstraintAncestor(te)+")");
		}
        
        System.out.println();
        HashMap<TypedElement<?>, TypedElement<?>> swaps = new HashMap<>();
        for (TypedElement<?> te2 : mmMerger.getAffectedOCLElement(ref_d)) {
        	EObject eParent = te2.eContainer().eContainer().eContainer();
        	
        	
        	if(Utils.getRandomInt(10)>5){
        		StructuralFeature ref_k = Metamodel.getMm1().getConcept("P").getRandomStructuralFeature();
        		System.out.println(" - rename:   "+te2.eGet(te2.eClass().getEStructuralFeature("referredProperty"))+" to "+ref_k.getName());
        		
        		((PropertyCallExp<?, EStructuralFeature>)te2).setReferredProperty(ref_k.geteStructuralFeature());
        		te2.eSet(te2.eClass().getEStructuralFeature("referredProperty"), ref_k.geteStructuralFeature());
        	} else {
        		System.out.println(" - collapse: "+te2);
        		swaps.put(te2, collapse(getFirstBooleanAncestor(te2)));
        	}
        	
		}
        //mise a jour de la liste des elements affectés dans le MmMerger
        mmMerger.swapAffectedElements(swaps);
        
        System.out.println();

        System.out.println("2."+"affected by '"+ref_d.getName()+"': "+mmMerger.getAffectedOCLElement(ref_d).size()+" elements");;
        for (TypedElement<?> te2 : mmMerger.getAffectedOCLElement(ref_d)) {
			System.out.println(te2 + " ("+getConstraintAncestor(te2)+")");
		}
      
        System.out.println(mmMerger.getAffectedOCLElement(ref_d));
//        TypedElement<?> te = (TypedElement<?>)mmMerger.getAffectedOCLElement(ref_d).get(1);
        
        // >> Attention !!! << 
        // Renommer un structFeature revient à modifier le métamodele !
//        EStructuralFeature esf = (EStructuralFeature)te.getReferredProperty();
//        esf.setName("unautrel");
        
        // Récupération d,une autre strucFeature..
//        StructuralFeature ref_k = Metamodel.getMm1().getConcept("P").getStructuralFeature("k");
        
        // .. qu'on plug sur l'élément OCL
//        te.eSet(te.eClass().getEStructuralFeature("referredProperty"), ref_k.geteStructuralFeature());
        
        
        // Encore une fois, avec une autre structFeature ('test' de type boolean) :
//        StructuralFeature ref_test = Metamodel.getMm1().getConcept("P").getStructuralFeature("test");
//        te.eSet(te.eClass().getEStructuralFeature("referredProperty"), ref_test.geteStructuralFeature());
        
//        System.out.println(ref_test.getMetamodel());
//        System.out.println(ref_test.getSourceConcept());
//        ref_test.getSourceConcept().getAllStructuralFeatures();
        
        
        
//        System.out.println(te + " ("+getConstraintAncestor(te)+")");
        
        
//        String s = extractConstraintAsXML(te);
//		System.out.println("Rule :"+s);
	}

	public static void main4(String[] args) {
		Utils.init();
//		Metamodel m = Metamodel.getMm1();
//
//		Concept cP = m.getConcept("P");
//		Concept cM = m.getConcept("M");
//		
//		System.out.println(cP);
//		System.out.println(cP.getReferences());
//		System.out.println(cP.getReferences(cM));
//		EcoreUtil.Copier copi = new EcoreUtil.Copier();
//		
//		
//		Reference a = cP.getReference("k");
//		
//		System.out.println("k: "+a);
//		
//		Population pop = Population.createRandomPopulation(Program.getInitialProgram());
//		Evaluator eva = new Evaluator();
		
		Program prg0 = Program.getInitialProgram(); //test_Family_1.ocl
        Contrainte c = prg0.getContrainte("A11b_inv"); //Première contrainte : 
        Constraint cst = c.getEConstraint();
        System.out.println(cst); //context FF inv A11b_inv: self.age.>(1)
        OperationCallExp<?, ?> superior = (OperationCallExp<?,?>)cst.getSpecification().getBodyExpression();
        System.out.println(superior.eGet(superior.eClass().getEStructuralFeature("source"))); // ?? => self.age :))		System.out.println(suprior.eGet(suprior.eClass().getEStructuralFeature("source")));;
		for (EModelElement emm : cst.getConstrainedElements()) {
			System.out.println(emm);
		}
		VariableExp<?, ?> v = null;
		
		
		
		
//		pop.evaluate(eva);
//		
//		System.out.println(pop.prettyPrint());
//		Evolutioner evo = new Evolutioner(eva, pop);
//		try {
//			evo.evolutionate();
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
		System.out.println("Exit.");
	}
	
	public static void main3(String[] args) throws FileNotFoundException{
		LOGGER.info("Entering CoOCL...");
		Utils.init();
		
		String fileOCL = "";
		String fileOCL2 = "";
		switch (Metamodel.getMm1().getName()) {
		case "Family":
			fileOCL = "/ocl/test_Family_1.ocl";
			fileOCL2 = "/ocl/test_Family_1-2.ocl";
			break;
		case "express":
			fileOCL = "/ocl/express.ocl";
			fileOCL2 = "/ocl/test_Family_1-2.ocl";
			break;
		case "statemachine":
			fileOCL = "/ocl/test_statemachine_1.ocl";
			fileOCL2 = "/ocl/test_Family_1-2.ocl";
			break;
		default:
			throw new IllegalArgumentException("Erreur : wrong MM : "+Metamodel.getMm1().getName());
		}
		Program p1 = null, p2 = null;
		try {
			CollectOCLIds<?, ?, ?, ?, ?, ?, ?, ?, ?> collector = CollectOCLIds.newCollectOCLId(Metamodel.getMm1());
			p1 = collector.load(new File(Config.DIR_TESTS + fileOCL)) ;// new Program(Metamodel.getMm1(), new File(Config.DIR_TESTS + fileOCL));
			p2 = collector.load(new File(Config.DIR_TESTS + fileOCL2));// new Program(Metamodel.getMm1(), new File(Config.DIR_TESTS + fileOCL2));
		} catch (IOException e1) {
			System.err.println("AN ERROR OCCURED WHILE LOADING OCL FILES :\n - '"+Config.DIR_TESTS + fileOCL+"'\n"+Config.DIR_TESTS + fileOCL2+"'");
			System.out.println("p1:"+p1);
			System.out.println("p2:"+p2);
			e1.printStackTrace();
		} 
		
		System.out.println("p1: "+p1.prettyPrint());
		System.out.println("p2: "+p2.prettyPrint());
	
		try {
			Program[] sons = (Program[])p1.crossover(p2);
			System.out.println("son0: "+sons[0].simplePrint());
			System.out.println("son1: "+sons[1].simplePrint());
		} catch (UnstableStateException e) {
			System.out.println("Cross over failed !");
			e.printStackTrace();
		}
		
		
		
//		main2(args);
	}
	
	public static void main1(String[] args) throws FileNotFoundException{
		main3(args);
		LOGGER.info("Entering CoOCL...");
		Utils.init();
		
		Metamodel mm1 = Metamodel.getMm1();
		Metamodel mm2 = Metamodel.getMm2();
	
		
		//Dictionaire de classes
		
		String fileOCL = "";
		switch (mm1.getName()) {
		case "Family":
			fileOCL = "/ocl/test_Family_1.ocl";
			break;
		case "express":
			fileOCL = "/ocl/express.ocl";
			break;
		case "statemachine":
			fileOCL = "/ocl/test_statemachine_1.ocl";
			break;
		default:
			throw new IllegalArgumentException("Erreur : wrong MM : "+mm1.getName());
		}
		
		System.out.println("Dictionnaire de classes :");
		System.out.println("mm1: "+mm1.getConcepts().size()+" classes");
		System.out.println("mm2: "+mm2.getConcepts().size()+" classes");
		
		File oclFile = new File(Config.DIR_TESTS + fileOCL);
		
		
		MetamodelMerger mmMerger = new MetamodelMerger(mm1, mm2, oclFile);

		System.out.println("Affected by Concept removal :");
		for (TypedElement<?> te : mmMerger.getAffectedOCLElements_byRemovedConcepts()) {
			System.out.println(te.getClass().getName()+"\t| "+te);
		}
		System.out.println("Affected by SF removal :");
		for (TypedElement<?> te : mmMerger.getAffectedOCLElements_byRemovedStructuralFeatures()) {
			System.out.println(te.getClass().getName()+"\t| "+te);
			PropertyCallExp<?, ?> pce = (PropertyCallExp<?, ?>)te;
		}
		
//		TypedElement<?> te = mmMerger.getElementAffected(Utils.getRandom(mmMerger.getRemovedStructuralFeatures()));
//		System.out.println("te= "+te);
		
		System.out.println("\n\nNO MAN'S LAND\nv v v v v v v\n");
		
		Constraint c = mmMerger.getConstraint("test");
		System.out.println("Test. (Main.main) : "+c);
		
		//Affichage XML
		String s = Contrainte.printConstraintAsXML(c);
		System.out.println("Rule 'test':"+c);
		//-- end affichage XML
		System.out.println(s);
		
//		BooleanLiteralExp<?> ble = EcoreFactory.eINSTANCE.createBooleanLiteralExp();
//		ble.setBooleanSymbol(true);
		
		System.out.println("getAffectedOCLElements_byRemovedStructuralFeatures");
		for (TypedElement<?> te : mmMerger.getAffectedOCLElements_byRemovedStructuralFeatures()) {
			TypedElement<?> opCallExp = getFirstBooleanAncestor(te);
			System.out.println(" --> \""+te+"\" \taffected --> collapse \""+opCallExp+"\" ");
			collapse(opCallExp);
		}
		

		System.out.println("\n\nNO MAN'S LAND II\nv v v v v v v v\n");

		
		s = Contrainte.printConstraintAsXML(c);
//		System.out.println("Rule 'test':\n"+s);
		
		for (Constraint c2 : mmMerger.getConstraints().values()) {
			OCLExpression<?> exp = c2.getSpecification().getBodyExpression();
			printExpChildren(exp,true, "");
		}
		
		//Build dictionnaries 
		//  mm1.ids
		//  mm2.ids
		//  ocl.ids
		
		//Check what ids in ocl & not in mm2 -> Syntax break
		//Check what ids in mm1 & not in mm2
			
		
		// List unchanged present
		// List changed present
		// List not present
		
		// Loader metamodel (dans init)
		// Loader OCL du metamodel : fichier xmi/ocl
		//  -> Build AST
		
		
		
//		File f = new File(Config.DIR_TESTS + "/ocl/test_Family_1.ocl");
//		System.out.println("\n\n** File 1 : "+f.getAbsolutePath());
//		Program prg;
//		try {
//			prg = new Program(Metamodel.getMm1(), f);
////			String rndOCLPrg = prg.getAsOCLText();
////			System.out.println(rndOCLPrg);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		LOGGER.info("... CoOCL Exit !");
	}
	
	/**
	 * On work.
	 * NOT FINISHED.
	 * 
	 * @param exp
	 * @param recursive
	 * @param tab
	 * @return
	 */
	public static String printExpChildren(OCLExpression<?> exp, boolean recursive, String tab){
		String res = "";
		System.out.println("exp:"+exp);
		
		for (EObject eo : exp.eContents()) {
			if(recursive && (eo instanceof OCLExpression<?>))
				printExpChildren((OCLExpression<?>)eo, recursive, tab);
			System.out.println("  - "+eo);
		}
		return res;
	}
	
	public static BooleanLiteralExp<?> collapse(TypedElement<?> oce){
		LOGGER.finer(" collapse("+oce+")");
		
		EStructuralFeature parentSF = oce.eContainingFeature();
		EObject eoParent = oce.eContainer();
		LOGGER.finer("   Constraint= "+getConstraintAncestor(oce).getName());
		LOGGER.finer("   Parent=     "+eoParent);
		
		BooleanLiteralExp<?> ble = EcoreFactory.eINSTANCE.createBooleanLiteralExp();
		boolean collapseValue = getBooleanValueToInsert(eoParent);
		ble.setBooleanSymbol(collapseValue);
		
//		LOGGER.finer("1. (Main.collapse) : "+eo.eGet(parentSF).getClass().getSimpleName());
		
		Object toCollapse = eoParent.eGet(parentSF);
		
		if(toCollapse instanceof Collection<?>){
//			LOGGER.finer(toCollapse.getClass());
			ArrayList<OCLExpression<?>> al = new ArrayList<>(1);
			al.add(ble);
			eoParent.eSet(parentSF, al);
		} else {
			eoParent.eSet(parentSF, ble);
		}
		
		
		LOGGER.finer("   -  - --->    "+ble.eContainer());
		
		LOGGER.finer("   --> "+eoParent);
		if(eoParent instanceof ExpressionInOCL<?, ?>){
			LOGGER.finer("      -> "+((ExpressionInOCL<?, ?>)eoParent).getBodyExpression());
		}
		LOGGER.finer("\\n");
		return ble;
	}

	public static boolean getBooleanValueToInsert(EObject eoParent) {
		if((eoParent instanceof IteratorExp) || (eoParent instanceof IfExp<?>))
			return true;
		if((eoParent instanceof OperationCallExp<?, ?>))
			return ((EOperation)((OperationCallExp<?,?>)eoParent).getReferredOperation()).getName().equalsIgnoreCase("or");
		if(eoParent instanceof ExpressionInOCL<?, ?>)
			return true;
		return false;
	}
	
	
	/**
	 * Climb up the AST to find the first node of type boolean.
	 * Return <code>null</code> if none found 
	 * (as we are considering invariants only, the <code>null</code> return means that the root of the constraint has been reached (if there is {@link #getConstraintAncestor(TypedElement) such a constraint}) : therefore, the constraint is the first Boolean ancestor).
	 * @param te
	 * @return The first OCLElement of type boolean. 
	 */
	public static TypedElement<?> getFirstBooleanAncestor(TypedElement<?> te){
		EObject eo = te.eContainer();
		Constraint c = getConstraintAncestor(te);
		do {
			if(eo.eClass().getName().equals("TypedElement") && isBoolean((TypedElement<?>) eo )) {
				break;
			} 
		} while((eo = eo.eContainer()) != null);
//		if(eo == null)
//			System.out.println("1. (Main.getFirstBooleanAncestor)"+te+" : "+c);
		return (TypedElement<?>)eo;
	}
	
	public static boolean isBoolean(TypedElement<?> eo){
		boolean res = false;

		System.out.println("1. (MainEd.isBoolean) : "+eo+" : "+eo.getType()+"     /   "+printEStructuralFeatures(eo));
		if(eo.eClass().getName().equals("IfExp") ){
			IfExp<?> ie = ((IfExp<?>)eo);
			res = ((PrimitiveType)ie.getElseExpression().getType()).getName().equalsIgnoreCase("Boolean");
		} else if(eo instanceof OperationCallExp){
			
			res = ((PrimitiveType)((OperationCallExp<?,?>)eo).getType()).getName().equalsIgnoreCase("Boolean");
		}
//		System.out.println("Main.isBoolean("+eo+"): "+res);
		return res;
	}
 
	/**
	 * Climb up the AST tree to find the containing Constraint.
	 * Return <code>null</code> if such a constraint doesn't exist : the OCLElement (TypedElement) can be cut from any constraint.
	 * @param te
	 * @return
	 */
	public static Constraint getConstraintAncestor(TypedElement<?> te){
		EObject eo = te;
		EObject res = null;
		do {
			res = eo;
		} while((eo = eo.eContainer()) != null);
//		System.out.println(res);
//		System.out.println(res.eContainer());
//		System.out.println(res.eClass().getName());
//		System.out.println(printEStructuralFeatures(res));
		if(!res.eClass().getName().equals("Constraint"))
			return null;
		return (Constraint)res;
	}
	public static String printEStructuralFeatures(EObject eo){
		String res = eo.eClass().getName()+":\n";
		for (EStructuralFeature esf : eo.eClass().getEAllStructuralFeatures()) {
			res += " - "+esf.getName() +": " + eo.eGet(esf)+ " (from "+esf.getContainerClass().getSimpleName() + ")  \n";
		}
		return res;
	}
	

}

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.lang.model.element.TypeElement;
import javax.swing.plaf.synth.SynthScrollBarUI;
import javax.swing.plaf.synth.SynthSeparatorUI;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreFactory;
import org.eclipse.ocl.ecore.IteratorExp;
import org.eclipse.ocl.ecore.PrimitiveType;
import org.eclipse.ocl.expressions.BooleanLiteralExp;
import org.eclipse.ocl.expressions.CallExp;
import org.eclipse.ocl.expressions.IfExp;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.expressions.OperationCallExp;
import org.eclipse.ocl.expressions.PropertyCallExp;
import org.eclipse.ocl.expressions.VariableExp;
import org.eclipse.ocl.utilities.ExpressionInOCL;
import org.eclipse.ocl.utilities.TypedElement;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.sun.glass.ui.CommonDialogs.Type;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import coocl.ocl.ASTforOCL;
import coocl.ocl.CollectOCLIds;
import coocl.ocl.Contrainte;
import coocl.ocl.Program;
import oclruler.genetics.Evaluator;
import oclruler.genetics.Evolutioner;
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

public class Main {


	static Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	public static void main_(String[] args) {
		Utils.init(null, "C:/Users/wael/OneDrive/workspaceocl/Materials/OCLCOEVOLUTION/CoOCL/test/ocl/test_Family_1.ocl");
		Program prg0 = Program.getInitialProgram();
		
		Program prgClone = (Program)prg0.clone();
		
		System.out.println("All Constraints");
		for (Contrainte cc : prg0.getContraintes()) {

			System.out.println(cc.prettyPrint());
		}
		System.out.println("");
		System.out.println("");
//		Contrainte cst = prg0.getContrainte(7);
		for (Contrainte cst : prg0.getContraintes()) 
		{
//			Contrainte cst = prg0.getContrainte(0);

			Contrainte cstClone = null;
			do {
				cstClone = (Contrainte) cst.clone();
//				System.out.println();
				System.out.println("1.cst0: " + cstClone.prettyPrint());
				System.out.println("1.cstCl: " + cst.prettyPrint());
				System.out.println("  ->" + cst.equals(cstClone));

				boolean success = cst.mutate();

				System.out.println("2.cst0: " + cstClone.prettyPrint());
				System.out.println("2.cstCl: " + cst.prettyPrint());
				System.out.println("  ->" + cst.equals(cstClone));
				if (success && cstClone.equals(cst))
					throw new IllegalStateException("cst != cst.clone() ! \n\tcst: " + cst.prettyPrint() + "\n\tclone: " + cstClone.prettyPrint());

			} while (!(cst.getStructuralFeaturesAffectingOCL().isEmpty()));
			
			
			System.out.println("\n\n\nFinnish !");
			
			
			System.out.println(prg0.prettyPrint());
			
			for (Contrainte ct : prg0.getContraintes()) {
				System.out.println(ct+" : "+ct.getStructuralFeaturesAffectingOCL());
				
			}
		}
		
		Contrainte cst = prg0.getContrainte(0);
		cst.changeContextType(Metamodel.getMm2().getConcept("Permis"));
		System.out.println("1. (Main.main) : "+cst.prettyPrint());
		Evaluator eva = new Evaluator();
		System.out.println(eva.evaluate(prg0));;

		cst.changeContextType(Metamodel.getMm2().getConcept("F"));
		System.out.println("2. (Main.main) : "+cst.prettyPrint());
		System.out.println(eva.evaluate(prg0));;
		
		System.out.println();
		System.out.println(prg0);
		for (Contrainte ct : prg0.getContraintes()) {
			System.out.println(" - "+ct.getStructuralFeaturesAffectingOCL());
//			if(!ct.getStructuralFeaturesAffectingOCL().isEmpty())
//				cts.add(ct);
		}
		
		
		System.out.println(prgClone.prettyPrint());
		for (Contrainte ct : prgClone.getContraintes()) {
			System.out.println(" - "+ct.getStructuralFeaturesAffectingOCL());
//			if(!ct.getStructuralFeaturesAffectingOCL().isEmpty())
//				cts.add(ct);
		}
		
		Contrainte ct = prgClone.getContrainte(0);
		TypedElement<?> te3 = (TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0).eContents().get(2).eContents().get(1).eContents().get(1);
		System.out.println(te3);
		
//		Contrainte ct0 = prg0.getContrainte(0);
//		TypedElement<?> te30 = (TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0);
//		System.out.println(te3);
		
//		ct.rename(Metamodel.getMm2().getConcept("P").getStructuralFeature("l"), te3);
		
		
		System.out.println(prgClone.getName());
		for (Contrainte ct2 : prgClone.getContraintes()) {
			System.out.println(" - "+ct2.getStructuralFeaturesAffectingOCL());
//			if(!ct.getStructuralFeaturesAffectingOCL().isEmpty())
//				cts.add(ct);
		}
		
		System.out.println("     -             -    ");
		System.out.println("     -             -    ");
		System.out.println("     -    SUITE    -    ");
		System.out.println("     -             -    ");
		
		
		TypedElement<?> te4 = (TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0).eContents().get(2).eContents().get(1).eContents().get(1);
		
		System.out.println(Contrainte.getIndex(te4));
		System.out.println(Contrainte.getIndex((TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0).eContents().get(2).eContents().get(1)));
		System.out.println(Contrainte.getIndex((TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0).eContents().get(2)));
		System.out.println(Contrainte.getIndex((TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0)));
		
		System.out.println(Contrainte.getIndexPath(te4));
		
		
		Contrainte ct2 = (Contrainte)prg0.getContrainte(7);
		System.out.println("aaaaaaaa");
		
		new Evaluator().checkSyntaxOCL(ct2);
		System.out.println("aaaaaaaa");
		System.out.println("    ->  "+ct2.prettyPrint());
		
		TypedElement<?> te4eq = (TypedElement<?>)ct2.getEquivalent(te4);
		System.out.println(te4eq);
		System.out.println(te4);
		System.out.println();
		System.out.println(te4eq.eContainer());
		System.out.println(te4.eContainer());
		
		
		TypedElement<?> tewaeltest=(TypedElement<?>)ct2.getEquivalent(te4);
		System.out.println();
		
		System.out.println("EXIT !");
	}
	
	public static void main(String[] args) {
		Utils.init("./utils/configEd.properties");
		Program prg0 = Program.getInitialProgram();
		
		System.out.println(prg0.prettyPrint());
	
System.out.println("aaaaaaaa");

		//Evaluator.checkSyntaxOCL(prg0.getContrainte(0));
//System.out.println(prg0.getContrainte(0).getMMElementInvolved());
//int occurrences = Collections.frequency(prg0.getContrainte(0).getMMElementInvolved(), prg0.getContrainte(0).getMMElementInvolved().get(0));
//System.out.println("occurrences "+occurrences);
		for (Contrainte ccc : prg0.getContraintes()) {

			// Contrainte ccc=prg0.getContrainte(3);
			System.out.println("IN - "+ccc.prettyPrint());
			// prg0.getContrainte(1).setModified(true);
			ccc.evaluateInformationLoss();
			Contrainte cttest = (Contrainte) ccc.clone();
			cttest.mutate();
//			System.out.println(ccc.prettyPrint());
			System.out.println("aaaaaaaa - MUTATION done");

//			System.out.println(cttest.prettyPrint());

			// cttest.setModified(true);
			// ArrayList<StructuralFeature> test=
			// prg0.getContrainte(0).getMMElementInvolved();
			// System.out.println(test);

			// occurrences = Collections.frequency(test, test.get(0));
			// System.out.println("occurrences "+occurrences);
			cttest.evaluateInformationLoss();
			System.out.println("OUT - "+cttest.prettyPrint());
		}
//cttest.evaluateInformationLoss();
System.exit(0);
		Program prgClone = (Program)prg0.clone();
//		Contrainte ct = prgClone.getContrainteByEConstraintName("test3");
//		
//		System.out.println("     -             -    ");
//		System.out.println("     -             -    ");
//		System.out.println("     -    SUITE    -    ");
//		System.out.println("     -             -    ");
//		
//		System.out.println(ct.prettyPrint());
//		TypedElement<?> te4 = (TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0).eContents().get(2).eContents().get(1).eContents().get(1);
//		
//		System.out.println(Contrainte.getIndex(te4));
//		System.out.println(Contrainte.getIndex((TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0).eContents().get(2).eContents().get(1)));
//		System.out.println(Contrainte.getIndex((TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0).eContents().get(2)));
//		System.out.println(Contrainte.getIndex((TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0)));
//		
//		System.out.println("Index path: "+Contrainte.getIndexPath(te4));
//		
//		System.out.println();
//		Contrainte ct2 = (Contrainte)ct.clone();
//		
//		System.out.println("ct2 : "+ct2.prettyPrint());
//		
//		TypedElement<?> te4eq = ct2.getEquivalent(te4, ct2);
//		System.out.println(te4eq);
//		System.out.println(te4);
//		System.out.println();
//		System.out.println(te4eq.eContainer());
//		System.out.println(te4.eContainer());

		Contrainte ct = prgClone.getContrainte(1);

		System.out.println("     -             -    ");
		System.out.println("     -             -    ");
		System.out.println("     -    SUITE    -    ");
		System.out.println("     -             -    ");
		
		System.out.println(ct.prettyPrint());
		TypedElement<?> te4 = (TypedElement<?> )ct.getEConstraint().eContents().get(0).eContents().get(0).eContents().get(1).eContents().get(1).eContents().get(1);
		System.out.println(" >> "+te4);
		
		System.out.println("Index path: "+Contrainte.getIndexPath(te4));
		
		System.out.println();
		Contrainte ct2 = (Contrainte)ct.clone();
		
		System.out.println("ct2 : "+ct2.prettyPrint());
		
		TypedElement<?> te4eq = (TypedElement<?>)ct2.getEquivalent(te4);
		System.out.println(te4eq);
		System.out.println(te4);
		System.out.println();
		System.out.println(te4eq.eContainer());
		System.out.println(te4.eContainer());
		
		ct2.mutate();
		System.out.println("Resultat :");
		System.out.println("ct2 : "+ct2.prettyPrint());
		

		System.out.println("EXIT !");
	}
	
	public static TypedElement<?> getEqual(TypedElement<?> te, Contrainte c){
		Constraint cst = c.getEConstraint();
		EObject res = cst;
		for (Integer i : getIndexPath(te)) {
			res = res.eContents().get(i);
		}
		return (TypedElement<?>)res;
	}
	
	public static ArrayList<Integer> getIndexPath(TypedElement<?> te){
		ArrayList<Integer> res = new ArrayList<>();
		EObject teo = te;
		while(teo != null ) {
			int idx = getIndex(teo);
			if(idx >= 0)
				res.add(0, idx);
			teo = teo.eContainer();
		} 
		return res;
	}
	
	public static int getIndex(EObject te){
		if(te == null)
			return -1;
		EObject parent = te.eContainer();
		if(parent != null) {
			for (int i = 0; i < parent.eContents().size() ; i++) {
				EObject childParent = parent.eContents().get(i);
				if(childParent.equals(te))
					return i;
			}
		}
		return -1;
	}
	
	public static void  mainsuite(){
		System.exit(0);
		Program prg0 = Program.getInitialProgram();
		
		Program prgClone = (Program)prg0.clone();
		StructuralFeature testw = Metamodel.getMm2().getConcept("P").getStructuralFeature("d");
		System.out.println("testw=" + testw.getName());
		System.out.println("testw=" + testw.getType());

		// System.out.println(Metamodel.getMm2().get_T_StructuralFeatures(testw.getType(),
		// null));;
//		System.out.println("Resultat = " + Contrainte.loadMetamodelElement(testw));

		// System.out.println(Metamodel.getMm2().get_T_StructuralFeatures(Metamodel.getMm2().getConcept("M"),
		// null));;

		System.out.println(Metamodel.getMm2().getStructuralFeatures().values());
		System.exit(0);
		// System.out.println("test"+
		// Metamodel.getMm1().getConcept("Interface").getAllStructuralFeatures());

		// StructuralFeature testw =
		// Metamodel.getMm1().getConcept("P").getStructuralFeature("l");
		// StructuralFeature testw =
		// Metamodel.getMm1().getConcept("Interface").getStructuralFeature("kind");
		// StructuralFeature testw =
		// Metamodel.getMm1().getConcept("Interface").getStructuralFeature("kind");

		System.out.println("Source concept : " + testw.getSourceConcept());

		System.out.println("Type str : " + testw.getType());
		System.out.println("Type str name : " + testw.getTypeName());
		System.out.println("test2" + testw.getName());
		System.out.println("test3" + testw);

//		System.out.println("Resultat = " + Contrainte.loadMetamodelElement(testw));

		// Contrainte ccc = Utils.getRandom(prg0.getContraintes());
		Contrainte ccc = prg0.getContrainte(0);
		Contrainte c2 = null;
		c2 = (Contrainte) ccc.clone();

		StructuralFeature age = Metamodel.getMm1().getConcept("P").getStructuralFeature("l");
		if (!(c2.equals(ccc)))
			System.out.println("OHHH");
		System.out.println(ccc.prettyPrint());

		// for(int i=0;i<7;i++)
		do {
			ccc.mutate();

			System.out.println(ccc.prettyPrint());
			c2 = (Contrainte) ccc.clone();

		} while (!(ccc.getStructuralFeaturesAffectingOCL().isEmpty()));
		System.out.println("Finnish !");
		System.exit(0);

		System.out.println("OCL affected by -> " + age.getName());
		System.out.println("");

		for (Contrainte cc : prg0.getContraintesUsingMMElement(age)) {
			// System.out.println(" + "+cc.getName()+": ");
			System.out.println(cc.prettyPrint());
		}
		System.out.println("");

		Contrainte c = prg0.getContraintesUsingMMElement(age).get(1);
		System.out.println("Contrainte choisie= " + c.prettyPrint());

		/*
		 * for (TypedElement<?> te : c.getOCLElementsAffectedByMMElement(age)) {
		 * System.out.println("    - "+te); collapse(te); }
		 */ System.out.println("");
		System.out.println("Contrainte choisie= " + c.prettyPrint());

		StructuralFeature ref_k = Metamodel.getMm1().getConcept("P").getStructuralFeature("l");

		for (TypedElement<?> te2 : c.getOCLElementsAffectedByMMElement(age)) {
			System.out.println(" - rename:   " + te2.eGet(te2.eClass().getEStructuralFeature("referredProperty")) + " to " + ref_k.getName());
			System.out.println("aa" + te2.eClass().getEStructuralFeature("referredProperty"));
			System.out.println("aa" + te2.eClass().getEStructuralFeature(1));
			EList<EStructuralFeature> cqq = te2.eClass().getEStructuralFeatures();
			// System.out.println("List ="+cqq.get(1));
			System.out.println("ss" + te2.eContents().get(0));

			EStructuralFeature test = te2.eClass().getEStructuralFeature("referredProperty");
			System.out.println("wael");
			System.out.println(printEStructuralFeatures(te2));

			System.out.println("Str feature " + te2.eGet(te2.eClass().getEStructuralFeature(18)));
			Metamodel mm1 = Metamodel.getMm1();

			System.out.println("TEST " + mm1.getStructuralFeatures());
			te2.eSet(te2.eClass().getEStructuralFeature("referredProperty"), ref_k.geteStructuralFeature());
			System.out.println("ff " + te2.getType());

		}

		System.out.println("Contrainte choisie= " + c.prettyPrint());

		Constraint ctest = c.getEConstraint();
		String s = printConstraintAsXML(ctest);
		System.out.println("Rule 'test':" + c);
		// -- end affichage XML
		System.out.println(s);

	}
	
	
	public static void mainaa(String[] args) {
		Utils.init();
		Program prg0 = Program.getInitialProgram(); // test_Family_1.ocl
		System.out.println(prg0.prettyPrint());
		
		Contrainte c = prg0.getContrainte("testSyntaxCount");
		Constraint cst = c.getEConstraint();
		System.out.println(cst);
		System.out.println(printConstraintAsXML(cst));
		
		StructuralFeature ref_k = Metamodel.getMm1().getConcept("F").getStructuralFeature("m");
		StructuralFeature ref_l = Metamodel.getMm1().getConcept("P").getStructuralFeature("test");
		
		TypedElement<?> te = Utils.getRandom(c.getOCLElementsAffectedByMMElement(ref_k));
		
		System.out.println(" - rename:   "+((EObject)te.eGet(te.eClass().getEStructuralFeature("referredProperty"))).eGet(te.eClass().getEStructuralFeature("name"))+" to "+ref_l.getName()+" in : "+te+" / "+getConstraintAncestor(te));
		
//		((PropertyCallExp<?, EStructuralFeature>)te).setReferredProperty(ref_k.geteStructuralFeature());
		te.eSet(te.eClass().getEStructuralFeature("referredProperty"), ref_l.geteStructuralFeature());
		
		te = Utils.getRandom(c.getOCLElementsAffectedByMMElement(ref_k));
		
		System.out.println(" - rename:   "+((EObject)te.eGet(te.eClass().getEStructuralFeature("referredProperty"))).eGet(te.eClass().getEStructuralFeature("name"))+" to "+ref_l.getName()+" in : "+te+" / "+getConstraintAncestor(te));
		
//		((PropertyCallExp<?, EStructuralFeature>)te).setReferredProperty(ref_k.geteStructuralFeature());
		te.eSet(te.eClass().getEStructuralFeature("referredProperty"), ref_l.geteStructuralFeature());
		
		System.out.println(cst);
		System.out.println(printConstraintAsXML(cst));
		
		Evaluator eva = new Evaluator();
		eva.evaluate(prg0);
		
		
		
		
		
		
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

	
	
public static void mained(String[] args) {
		Utils.init();
		Program prg0 = Program.getInitialProgram(); //test_Family_1.ocl
		
		// ref_l est récupérer grace au mmMerger (par exemple)
		StructuralFeature ref_l = Metamodel.getMm1().getConcept("P").getStructuralFeature("age");
		
		

		
		
		System.out.println("Contraintes in prg0");
		for (Contrainte cc : prg0.getContraintes()) {
			System.out.println(" - "+cc.getName()+": "+cc.getStructuralFeaturesAffectingOCL());
			
		}
		
		
		Contrainte ccc = Utils.getRandom(prg0.getContraintes());
		
		Contrainte ccc1=prg0.getContrainte(0);
		Contrainte ccc2=prg0.getContrainte(1);
		System.out.println("ccc2"+ccc1.printResultPane());
		
		System.out.println(ccc1.prettyPrint());
		System.out.println("contrainte test :"+ccc1);
		System.out.println("aa"+ccc1.getOCLElementsAffectedByMMElement(ref_l));
		TypedElement<?> tew =ccc1.getOCLElementsAffectedByMMElement(ref_l).get(0);
		System.out.println("    -wael- "+tew);
	EObject firstBool1 = getFirstBooleanAncestor(tew);
		if(firstBool1 == null){
			firstBool1 = getConstraintAncestor(tew);
			
		}
		collapse(tew);
		System.out.println(ccc1);
		
		System.exit(0);
		//ccc.mutate(ref_l);
		
		System.out.println(ccc);
		
		
	//	System.exit(0);
		
		
		System.out.println("Contraintes in prg0");
		for (Contrainte cc : prg0.getContraintes()) {
			System.out.println(" - "+cc.getName()+": "+cc.getStructuralFeaturesAffectingOCL());
			
		}
		
		System.exit(0);
		
		
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
        		
//        		((PropertyCallExp<?, EStructuralFeature>)te2).setReferredProperty(ref_k.geteStructuralFeature());
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
		String s = printConstraintAsXML(c);
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

		
		s = printConstraintAsXML(c);
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
			if(isBoolean(eo)) {
				break;
			} 
		} while((eo = eo.eContainer()) != null);
//		if(eo == null)
//			System.out.println("1. (Main.getFirstBooleanAncestor)"+te+" : "+c);
		return (TypedElement<?>)eo;
	}
	
	public static boolean isBoolean(EObject eo){
		boolean res = false;
		boolean end = false;
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
	public static String printConstraintAsXML(TypedElement<?> te) {
		System.out.println("Main.printConstraintAsXML()"+te);
		Constraint c = getConstraintAncestor(te);
		if(c == null)
			return "<"+te+" :: No constraint attached>";
		return printConstraintAsXML(c);
	}
	
	public static String printConstraintAsXML(Constraint c) {
		Element xmlAST = (Element) c.getSpecification().accept(ASTforOCL.getEcoreVersion());
		org.jdom2.output.XMLOutputter xmlOutputter = new XMLOutputter( Format.getPrettyFormat());
		String s = xmlOutputter.outputString(xmlAST);
		return s;
	}
	

}

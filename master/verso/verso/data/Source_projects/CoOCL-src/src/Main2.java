import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.lang.model.element.TypeElement;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreFactory;
import org.eclipse.ocl.ecore.IteratorExp;
import org.eclipse.ocl.ecore.PrimitiveType;
import org.eclipse.ocl.expressions.BooleanLiteralExp;
import org.eclipse.ocl.expressions.CallExp;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.expressions.OperationCallExp;
import org.eclipse.ocl.expressions.PropertyCallExp;
import org.eclipse.ocl.utilities.ExpressionInOCL;
import org.eclipse.ocl.utilities.TypedElement;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import coocl.ocl.CollectOCLIds;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.MetamodelMerger;
import oclruler.metamodel.StructuralFeature;
import utils.Config;
import utils.Utils;

public class Main2 {


	static Logger LOGGER = Logger.getLogger(Main2.class.getName());
	
	
	public static void main(String[] args){
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
		
		
		//Affichage XML
		Element xmlAST = (Element) c.getSpecification().accept(CollectOCLIds.newCollectOCLId(mm1));
		org.jdom2.output.XMLOutputter xmlOutputter = new XMLOutputter( Format.getPrettyFormat());
		String s = xmlOutputter.outputString(xmlAST);
		System.out.println("Rule 'test':"+c);
		//-- end affichage XML
		System.out.println(s);
		
//		BooleanLiteralExp<?> ble = EcoreFactory.eINSTANCE.createBooleanLiteralExp();
//		ble.setBooleanSymbol(true);
		
		System.out.println("getAffectedOCLElements_byRemovedStructuralFeatures");
		for (TypedElement<?> te : mmMerger.getAffectedOCLElements_byRemovedStructuralFeatures()) {
			OperationCallExp<?, ?> opCallExp = getFirstBooleanAncestor(te);
			System.out.println(" --> \""+te+"\" \taffected --> collapse \""+opCallExp+"\" ");
			collapse(opCallExp);
		}
		

		System.out.println("\n\nNO MAN'S LAND II\nv v v v v v v v\n");

		
		
		
		xmlAST = (Element) c.getSpecification().accept(CollectOCLIds.newCollectOCLId(mm1));
		
		xmlOutputter = new XMLOutputter( Format.getPrettyFormat());
		s = xmlOutputter.outputString(xmlAST);
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
	
	public static String printExpChildren(OCLExpression<?> exp, boolean recursive, String tab){
		String res = "";
		System.out.println("exp:"+exp);
		
		for (EObject eo : exp.eContents()) {
			System.out.println("  - "+eo);
		}
		return res;
	}
	
	public static boolean collapse(OperationCallExp<?, ?> oce){
		System.out.println("  Main.collapse("+oce+")");
		
		EStructuralFeature parentSF = oce.eContainingFeature();
		EObject eoParent = oce.eContainer();
		System.out.println("   Constraint= "+getConstraintAncestor(oce).getName());
		System.out.println("   Parent=     "+eoParent);
		
		BooleanLiteralExp<?> ble = EcoreFactory.eINSTANCE.createBooleanLiteralExp();
		boolean collapseValue = getBooleanValueToInsert(eoParent);
		ble.setBooleanSymbol(collapseValue);
		
//		System.out.println("1. (Main.collapse) : "+eo.eGet(parentSF).getClass().getSimpleName());
		
		Object toCollapse = eoParent.eGet(parentSF);
		
		if(toCollapse instanceof Collection<?>){
//			System.out.println(toCollapse.getClass());
			ArrayList<OCLExpression<?>> al = new ArrayList<>(1);
			al.add(ble);
			eoParent.eSet(parentSF, al);
		} else {
			eoParent.eSet(parentSF, ble);
		}
		
		System.out.println("   --> "+eoParent);
		if(eoParent instanceof ExpressionInOCL<?, ?>){
			System.out.println("      -> "+((ExpressionInOCL<?, ?>)eoParent).getBodyExpression());
		}
		System.out.println();
		return true;
	}

	public static boolean getBooleanValueToInsert(EObject eoParent) {
		if((eoParent instanceof IteratorExp))
			return true;
		if((eoParent instanceof OperationCallExp<?, ?>))
			return ((EOperation)((OperationCallExp<?,?>)eoParent).getReferredOperation()).getName().equalsIgnoreCase("or");
		if(eoParent instanceof ExpressionInOCL<?, ?>)
			return true;
		return false;
	}
	
	
	
	public static OperationCallExp<?, ?> getFirstBooleanAncestor(TypedElement<?> te){
		EObject eo = te.eContainer();
		
		do {
//			String llog = (eo instanceof OperationCallExp)?""+((EOperation)((OperationCallExp)eo).getReferredOperation()).getName()+" : ":"";
//			if(eo instanceof OperationCallExp) {
//				System.out.println("--> "+((OperationCallExp)eo).getType());
//			}
//			String log = " - "+/*llog +*/eo.getClass().getSimpleName() + "\t| "+eo;
//			if(eo.eContainingFeature()!= null)
//				log += " ("+eo.eContainingFeature().getName()+")";
			
//			System.out.println((eo instanceof CallExp) + log);
			if(isBoolean(eo)) {
//				System.out.println(" ---x \""+eo+"\" => True/False");
				break;
			} 
//			System.out.println(log);
		} while((eo = eo.eContainer()) != null);
		return (OperationCallExp<?, ?>)eo;
	}
	
	public static boolean isBoolean(EObject eo){
		return (eo instanceof OperationCallExp) && ((PrimitiveType)((OperationCallExp<?,?>)eo).getType()).getName().equalsIgnoreCase("Boolean");
	}

	public static Constraint getConstraintAncestor(TypedElement<?> te){
		EObject eo = te.eContainer();
		EObject res = null;
		do {
			res = eo;
		} while((eo = eo.eContainer()) != null);
		return (Constraint)res;
	}

	
}

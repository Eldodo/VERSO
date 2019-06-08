package coocl.ocl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EOperationImpl;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.LookupException;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.cst.CSTNode;
import org.eclipse.ocl.ecore.AnyType;
import org.eclipse.ocl.ecore.CallOperationAction;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.ecore.EcoreFactory;
import org.eclipse.ocl.ecore.EcorePackage;
import org.eclipse.ocl.ecore.IteratorExp;
import org.eclipse.ocl.ecore.PrimitiveType;
import org.eclipse.ocl.ecore.SendSignalAction;
import org.eclipse.ocl.expressions.BooleanLiteralExp;
import org.eclipse.ocl.expressions.EnumLiteralExp;
import org.eclipse.ocl.expressions.IfExp;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.expressions.OperationCallExp;
import org.eclipse.ocl.expressions.PropertyCallExp;
import org.eclipse.ocl.expressions.TypeExp;
import org.eclipse.ocl.expressions.Variable;
import org.eclipse.ocl.expressions.VariableExp;
import org.eclipse.ocl.pivot.Operation;
import org.eclipse.ocl.pivot.library.oclany.OclAnyOclIsTypeOfOperation;
import org.eclipse.ocl.pivot.utilities.TypeUtil;
import org.eclipse.ocl.types.OCLStandardLibrary;
import org.eclipse.ocl.util.OCLStandardLibraryUtil;
import org.eclipse.ocl.util.OCLUtil;
import org.eclipse.ocl.utilities.ExpressionInOCL;
import org.eclipse.ocl.utilities.PredefinedType;
import org.eclipse.ocl.utilities.TypedElement;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import coocl.ocl.Mutation.MutationType;
import oclruler.genetics.Evaluator;
import oclruler.genetics.Gene;
import oclruler.metamodel.Concept;
import oclruler.metamodel.MMElement;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.MetamodelMerger;
import oclruler.metamodel.StructuralFeature;
import oclruler.metamodel.MetamodelMerger.DIFF_TYPE;
import utils.Utils;
import utils.distance.Cosine;
import utils.distance.DamerauLevenshtein;
import utils.distance.Hamming;

public class Contrainte implements Gene  {
	Logger LOGGER = Logger.getLogger("wael");
	Logger logMutation = Logger.getLogger("log.mutation");
	
	/**
	 * Operation code for EOperation creation and comparison (Attention, highly EcoreImpl dependent !)
	 */
	private static final int OPERATION_CODE_EQ = OCLStandardLibraryUtil.getOperationCode("=");
	private static final int OPERATION_CODE_ISTYPEOF = OCLStandardLibraryUtil.getOperationCode("oclIsTypeOf");

	/*
	 * Constantes are loaded from cnfig file in Evolutioner.loadConfig();
	 */
	public static int MUT_NEW_CONTEXT_EXPAND_PROBABILITY_FOR_ADDED_ELTS = 10;

	public static int MUT_RENAME_EXPAND_PROBAILITY_ADDED 		= 10;
	public static int MUT_RENAME_EXPAND_PROBAILITY_SAMETYPE 	= 10;
	public static int MUT_RENAME_EXPAND_PROBAILITY_SAMESOURCE 	= 5;
	public static int MUT_RENAME_EXPAND_PROBAILITY_THEREST 		= 1;

	public static double MUT_COLLAPSE_PROBABILITY 				= 0.5;
	public static boolean MUT_CHANGE_CONTEXT_INCLUDEALL 		= true;
	public static double MUT_CONTEXT_CHANGE_PROBABILITY 		= 0.00;

	//private static final double MUT_CONTEXT_CHANGE_PROBABILITY = 0.01;


	protected static int numberOfAllChanges 		= 0;
	public static int 	 numberOfAllMutationTries 	= 0;
	protected static int numberOfConstraints 		= 0;
	protected static int lastNumber 				= 0;
	
	
	private Constraint cstOcl;
	private int numberOfChanges;
	private String name;
	protected int number;
	protected boolean modified;
	protected String body_ocl;
	
	
	int numberOfDiffs = -1;
	private int syntaxErrors = -1;

	
	/**
	 * liste des elements de l'arbre ocl (values) affectées par les elements du metamodel (key).
	 * Map {MMElement = "OCL elets affectés par ce mme dans la contrainte"}
	 */
	
	//il faut ajouter une liste qui contient les elements modifiés
	HashMap<StructuralFeature, ArrayList<TypedElement<?>>> structuralFeaturesAffectingOCL;
	ArrayList<PropertyCallExp<?, ?>> modifications_encours;
	private ArrayList<StructuralFeature> structuralFeaturesInvolved;
	
	ArrayList<Mutation> pastMutations;
	
	
	public Contrainte(Constraint cst, int number) {
		this.name = "c"+number+"_"+(cst!= null ? cst.getName():"NULL")+"_"+numberOfConstraints++;
		this.number = number;
		lastNumber = number;
		this.numberOfChanges = 0;
		this.cstOcl = cst;
		this.modified = false;
		
		this.structuralFeaturesAffectingOCL = new HashMap<>();
		this.modifications_encours = new ArrayList<>();
		this.pastMutations = new ArrayList<>();
		
	}
	
	/**
	 * PLEASE TESTING ONLY !
	 * @param cst
	 */
	public Contrainte(Concept context, String cst) throws IllegalArgumentException {
		this.name = "c"+number+"_STR_"+context.getName()+"_"+numberOfConstraints++;
		this.number = ++lastNumber;
		this.numberOfChanges = 0;
		this.modified = false;
		
		
		OCL<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject,  CallOperationAction, SendSignalAction, Constraint, EClass, EObject> ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
		ocl.setEvaluationTracingEnabled(true);
		org.eclipse.ocl.helper.OCLHelper<EClassifier, ?, ?, Constraint> helper = ocl.createOCLHelper();
		EClassifier ecClass = context.getEModelElement();
		String queryStr =  cst;// c.getERoot().toString();
//		queryStr = c.getEConstraint().getSpecification().getBodyExpression().toString();
		try {
			helper.setContext(ecClass); 
			cstOcl = helper.createInvariant(queryStr);
			cstOcl.setName("STR_"+context.getName());
		} 
		
		catch (ParserException e) {
				Diagnostic diagnostic = e.getDiagnostic();
				ArrayList<String> diagnostics = Evaluator.getDiagnosticList(diagnostic);
				String strDiags = "";
				for (String diag : diagnostics) 
					strDiags += " - "+diag+"\n";
				throw new IllegalArgumentException("Exception thrown during OCL parsing : \nConstraint "+name+"\nocl: "+queryStr +"\ndiagnostic: "+"("+diagnostics.size()+") \n "+strDiags.trim());
		}
		this.structuralFeaturesAffectingOCL = new HashMap<>();
		this.modifications_encours = new ArrayList<>();
		this.pastMutations = new ArrayList<>();
	}
	
	
	public int getNumberOfChanges() {
		return numberOfChanges;
	}
	
	public Constraint getEConstraint() {
		return cstOcl;
	}

	public OCLExpression<EClassifier> getEBodyExpression(){
		return cstOcl.getSpecification().getBodyExpression();
	}

	public EClassifier getEContext() {
		return  cstOcl.getSpecification().getContextVariable().getType();
	}

	public static int totalNumberOfChanges() {
		return numberOfAllChanges;
	}

	
	
	public String getOCL_Body() {
		if(body_ocl == null || modified){
			body_ocl = printPrettyConstraintAsParseableOCL(cstOcl);
			modified = false;
		}
		return body_ocl;
	}
	
	public String getOCL_standaloneExecutable(){
		return "context "+getConceptTypeName()+" inv "+name+": "+getOCL_Body();
	}
	
	public String getOCL_comparable(){
		return "context "+getConceptTypeName()+" inv "+getOCLName()+": "+getOCL_Body();
	}
	
	/**
	 * Constraints' texts with different length will be completed to the longuest one (with blank spaces).
	 * @param c
	 * @return
	 */
	public double hammingDistance(Contrainte c) {
		String s1 = getOCL_comparable();
		String s2 = c.getOCL_comparable();
		
        return new Hamming(s1, s2).distance();
	}
	
	public double damerauLevensteinDistance(Contrainte c) {
		String s1 = getOCL_comparable();
		String s2 = c.getOCL_comparable();
		
        return new DamerauLevenshtein(s1, s2).distance();
	}
	
	public double cosineDistance(Contrainte c){
		return cosineDistance(c, Cosine.DEFAULT_K);
	}
	
	public double cosineDistance(Contrainte c, int cosineK){
		Cosine cosine = new Cosine(getOCL_comparable(), c.getOCL_comparable(), cosineK);
		return cosine.distance();
	}

	public String prettyPrintWael() {
		return name+":<"+cstOcl.toString()+">";
	}

	
	public String getConceptTypeName(){
		if(cstOcl == null)
			return "<cst is null>";
		return cstOcl.getSpecification().getContextVariable().getType().getName();
	}
	
	public String getOCLName() {
		if(cstOcl == null)
			return "<Constraint is null>";
		return cstOcl.getName();
	}
	
	public int getNumber() {
		return number;
	}
	
	/**
	 * Retourne la liste des element de l'arbre ocl affectées par l'element du metamodel passé en parametre.
	 * @return
	 */
	public HashMap<StructuralFeature, ArrayList<TypedElement<?>>> getStructuralFeaturesAffectingOCL() {
		return structuralFeaturesAffectingOCL;
	}
	
	/**
	 * Return the OCLElements (TypedElement) that involve the MMElement passed in aprameter.
	 * If parameter is <code>null</code>, return all TypedElement ((IS THAT USEFULL ??)).
	 * @param mme
	 * @return a list of the OCLElements (TypedElement) that involve the MMElement passed in aprameter.
	 */
	public ArrayList<TypedElement<?>> getOCLElementsAffectedByMMElement(MMElement mme) {
		if(mme == null){
			ArrayList<TypedElement<?>> res = new ArrayList<>();
			for (MMElement mme2 : structuralFeaturesAffectingOCL.keySet()) {
				for (TypedElement<?> te : structuralFeaturesAffectingOCL.get(mme2)) {
					if(!res.contains(te))
						res.add(te);
				}
			}
			return res;
		}
		
		ArrayList<TypedElement<?>> res = structuralFeaturesAffectingOCL.get(mme);
		return   res != null ? res : new ArrayList<>();
	}

	public boolean addOCLAffected(StructuralFeature mme, TypedElement<?> te){
		if(!structuralFeaturesAffectingOCL.containsKey(mme))
			structuralFeaturesAffectingOCL.put(mme, new ArrayList<>());
		return structuralFeaturesAffectingOCL.get(mme).add(te);
	}
	

	@Override
	public Contrainte clone()  {
		cleanDanglingTypedElements();
		Constraint copie = (Constraint)EcoreUtil.copy(cstOcl);
		Contrainte c = new Contrainte(copie, number);

		for (StructuralFeature sf : structuralFeaturesAffectingOCL.keySet()) {
			for (TypedElement<?> te : structuralFeaturesAffectingOCL.get(sf)) {
				c.addOCLAffected(sf, (TypedElement<?>)c.getEquivalent(te));
			}
		}
		
		for (PropertyCallExp<?, ?> te : modifications_encours) 
			c.modifications_encours.add((PropertyCallExp<?, ?>)c.getEquivalent(te));
		
		for (Mutation m : pastMutations) {
			try {
				c.pastMutations.add(m.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		
		for (String sErr : syntaxErrorsText) 
			c.syntaxErrorsText.add(sErr);
		c.syntaxErrors = this.syntaxErrors;
		
		
		c.numberOfChanges = numberOfChanges;
		return c;
	}
	
	/**
	 * Attention ! Only purpose is to fill the affecting list when cloning.
	 * @param te
	 * @param c
	 * @return
	 */
	public EObject getEquivalent(EObject te){
		EObject res = getEConstraint();
		for (Integer i : getIndexPath(te)) {
			res = res.eContents().get(i);
		}
		return res;
	}
	
	public static ArrayList<Integer> getIndexPath(EObject te){
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
			if(parent.eContents().size() > 1)
				for (int i = 0; i < parent.eContents().size() ; i++) {
					EObject childParent = parent.eContents().get(i);
					if(childParent.equals(te))
						return i;
				}
			else
				return 0;
		}
		return -1;
	}

	
	@Override
	public boolean equals(Object o) {
		if(o == null || (getClass() != o.getClass()))
			return false;
		Contrainte c = (Contrainte) o;
		return c.number == number && getConceptTypeName().equals(c.getConceptTypeName()) && c.getOCL_Body().equals(getOCL_Body());
	}
	
	/*
	 *     v    Genetic methods start    v
	 */
	@Override
	public int size() {
		// TODO A contrainte has no size
		return 0;
	}

	@Override
	public String prettyPrint() {
		return "<CT:"+getOCL_comparable()+">";
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String printResultPane() {
		return prettyPrint();
	}

	@Override
	public String simplePrint() {
		EStructuralFeature esfSearch = null;
		for (EStructuralFeature esf : cstOcl.eClass().getEAllStructuralFeatures()) {
			if(esf.getName().equals("constrainedElements"))
				esfSearch = esf;
		}
		return getOCLName();
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	
	public  EObject getFirstBooleanAncestor(EObject te){
		EObject eo = te;
		EObject eo2 = eo;
		do {
			if(eo instanceof TypedElement && isBoolean((TypedElement<?>) eo )) {
				eo2 = eo;
				break;
			} 
			eo2 = eo;
		} while((eo = eo.eContainer()) != null);
		return eo2;
	}	
	
	
	public  boolean isBoolean(EObject eo){
		boolean res = false;
		if(eo instanceof BooleanLiteralExp)
			return true;
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
	public static Constraint getConstraintAncestor(EObject te){
		if(te == null)
			return null;
		EObject eo = te;
		EObject res = null;
		do {
			res = eo;
		} while((eo = eo.eContainer()) != null);
		if(!res.eClass().getName().equals("Constraint"))
			return null;
		return (Constraint)res;
	}
	
	
	
	
	public boolean collapse(StructuralFeature sfAffecting, EObject te) {
		logMutation.fine("collapse - "+this+" -> "+te);
		boolean res = false;
		
		EObject affectedOCL = null;
		EObject parentBool = getFirstBooleanAncestor(te);
		if(getConstraintAncestor(parentBool) == null){
			//Means the "te" is dangling : it has been cut from its original constraint
//			System.out.println("               -#-#-#==> COLLAPSE EXIT : "+te);
			return false;
		}
		
		boolean collapseValue = parentBool instanceof Constraint ? true : getBooleanValueToInsert(parentBool.eContainer());
		BooleanLiteralExp<EClassifier> ble = EcoreFactory.eINSTANCE.createBooleanLiteralExp();
		ble.setBooleanSymbol(collapseValue);
		
		
		if(parentBool instanceof Constraint) {
			Constraint c = (Constraint)parentBool;
			EcoreFactory.eINSTANCE.createConstraint();
			c.getSpecification().setBodyExpression((OCLExpression<EClassifier>)ble);
			cleanDanglingTypedElements();
			affectedOCL = null;
			res = true;
		} else {
			if(parentBool.eIsSet(parentBool.eClass().getEStructuralFeature("name"))){
				//parentBool is a constraint.
				System.out.println(parentBool);
			} else {
				EStructuralFeature parentSF = parentBool.eContainingFeature();
				EObject eoParent = parentBool.eContainer();
				
				
//				System.out.println("Contrainte.collapse loop :" + loop++ + "    -  "+parentBool.eClass().getName()+"   "+(getConstraintAncestor(parentBool)==null));
				
				
				Object toCollapse = eoParent.eGet(parentSF, false);
	//			if(parentSF.isMany())
				if (parentSF.isMany() || toCollapse instanceof Collection<?>) {
					ArrayList<OCLExpression<?>> al = new ArrayList<>(1);
					al.add(ble);
					eoParent.eSet(parentSF, al);
				} else {
					eoParent.eSet(parentSF, ble);
				}
				affectedOCL = parentBool;
				res = true;
			}
		}
		modified = true;
		if(res && affectedOCL != null) {
			updateAffectingList(sfAffecting, affectedOCL);
			notifyAndStoreMutation(sfAffecting, null, affectedOCL, MutationType.COLLAPSE);
		}
		return res;
	}
	static int loop = 1;
	

	public static boolean getBooleanValueToInsert(EObject eoParent) {
		if(eoParent instanceof BooleanLiteralExp)
			return ((BooleanLiteralExp<?>)eoParent).getBooleanSymbol();
		if((eoParent instanceof IteratorExp) || (eoParent instanceof IfExp<?>))
			return true;
		if((eoParent instanceof OperationCallExp<?, ?>))
			return ((EOperation)((OperationCallExp<?,?>)eoParent).getReferredOperation()).getName().equalsIgnoreCase("or");
		if(eoParent instanceof ExpressionInOCL<?, ?>)
			return true;
		return false;
	}
	
	

	/**
	 * 
	 * @param mme Affecting MMElement
	 * @param newName new StructuralFeature to reference in te2
	 * @param te2 OCLElement which referredProperty is changed
	 * @return 
	 */
	public boolean renameProcess(MMElement mme, StructuralFeature newName, TypedElement<?> te) {
		logMutation.fine(""+this+" -> "+newName+" in "+te);
		if(mme.equals(newName)) {
			System.out.println("Pourri !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			return false;
		}
			
		te.eSet(te.eClass().getEStructuralFeature("referredProperty"), newName.getEModelElement());
		updateAffectingList(mme, te);
		notifyAndStoreMutation(mme, newName, te, MutationType.RENAME);
		return true;
	}
	
	public StructuralFeature getStructuralFeatureInvolved(PropertyCallExp<EClassifier, EStructuralFeature> te ){
		StructuralFeature res = null;
		EStructuralFeature esfReferred = ((PropertyCallExp<EClassifier, EStructuralFeature>)te).getReferredProperty();
		Concept esfType = Metamodel.getConceptFromMetamodels(esfReferred.getEContainingClass().getName());
		
		res = esfType.getMetamodel().getStructuralFeature(esfReferred.getName(), esfType);
		if(res == null) {
//			Metamodel mm = esfType.getMetamodel() == Metamodel.getMm2()? Metamodel.getMm2()
			if(esfType.getMetamodel() == Metamodel.getMm2())
				esfType = Metamodel.getMm1().getConcept(esfReferred.getEContainingClass().getName());
			else
				esfType = Metamodel.getMm2().getConcept(esfReferred.getEContainingClass().getName());
			res = esfType.getMetamodel().getStructuralFeature(esfReferred.getName(), esfType);
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	public boolean mutate() {
		numberOfAllMutationTries++;
//		System.out.println(name+".mutate()");
		boolean res = false;
		if(Utils.getRandomDouble() < MUT_CONTEXT_CHANGE_PROBABILITY) {//mme is a concept
			res = changeContextType();
		} else { 
			
			//Check initial affected elements list for mutation candidate
			StructuralFeature sfAffecting = Utils.getRandom(structuralFeaturesAffectingOCL.keySet());
			EObject eo = Utils.getRandom(this.getOCLElementsAffectedByMMElement(sfAffecting));
			
			//No candidate found in initial list : check modified elements list
			if(eo == null && !modifications_encours.isEmpty()){
				do {
					eo = Utils.getRandom(modifications_encours);
					PropertyCallExp<EClassifier, EStructuralFeature> pce = (PropertyCallExp<EClassifier, EStructuralFeature>)eo;
					sfAffecting = getStructuralFeatureInvolved(pce);
				}while(sfAffecting == null && !modifications_encours.isEmpty());
			}
			
			if (eo == null) {
//				System.out.println("    ->  End Mutation, no 'te' found (initial and modification lists are emtpy) <--");
				return false;
			} 
			boolean eoIsTE =(eo instanceof TypedElement<?>);
//			System.out.println(eo);
			
			if(eoIsTE){
				TypedElement<?> te = (TypedElement<?>)eo;
				if(sfAffecting.getType().isEnum()){
					EObject eoFisrtBool = getFirstBooleanAncestor(te);
//					System.out.println("   ------>>>>>>>>>>    HERE WE ARE   <<<<<<<<<<<-------");
					
					if(eoFisrtBool instanceof OperationCallExp<?, ?>){
						modified = true;
						OperationCallExp<?, EOperation> pceFisrtBool = (OperationCallExp<?, EOperation>) eoFisrtBool;
						if(pceFisrtBool.getReferredOperation().getName().equals("=") && (pceFisrtBool.getArgument().get(0) instanceof EnumLiteralExp<?, ?>)){
							Concept enumLitToConcept = Metamodel.getMm2().getConcept(((EnumLiteralExp<?, EEnumLiteral>)pceFisrtBool.getArgument().get(0)).getReferredEnumLiteral().getName());
							if(enumLitToConcept!= null){
								res = enumMutation(res, pceFisrtBool);
								return res;
							}
						}
					}
				}
			}
			
//			System.out.println("XXXXXXXX. (Contrainte.mutate) : XXXXXXXXXX");
		
			if (Utils.getRandomDouble() < MUT_COLLAPSE_PROBABILITY ) {	// Collapse
				res = collapse(sfAffecting, eo);
			} else { // Rename
				if(eoIsTE){
//					System.out.println("Rename");
					boolean hasMoved = false;
					boolean probaMove = Utils.getRandomDouble() < .995;
					
					if( probaMove ){
//						System.out.println("MOVE");
						Concept sfType2 = null;
						StructuralFeature sfOld = null;
						StructuralFeature sfNew = null;
						StructuralFeature sfIndirect = null;
						if(eo instanceof PropertyCallExp<?, ?>){
							PropertyCallExp<EClassifier, EStructuralFeature> pce = (PropertyCallExp<EClassifier, EStructuralFeature>)eo;
							sfType2 = Metamodel.getMm2().getConcept(pce.getReferredProperty().getEContainingClass().getName());
							sfOld = Metamodel.getMm1().getStructuralFeature(
									pce.getReferredProperty().getName(), 
									 Metamodel.getMm1().getConcept(pce.getReferredProperty().getEContainingClass().getName()));
//							System.out.println("sfOld: " + sfOld);
							if(sfOld != null && MetamodelMerger.getInstance().getRemovedStructuralFeatures().contains(sfOld)) {
								ArrayList<StructuralFeature> sfNews = Metamodel.getMm2().getStructuralFeatures(sfOld.getName());
								for (StructuralFeature sf : (StructuralFeature[]) sfNews.toArray(new StructuralFeature[sfNews.size()])) {
									if(!MetamodelMerger.getInstance().getDiffStructuralFeatures(DIFF_TYPE.ADD).contains(sf))
										sfNews.remove(sf);
								}
//								System.out.println("sfNews: " + sfNews);
								if(sfNews != null && !sfNews.isEmpty()) {
									sfNew = Utils.getRandom(sfNews);
									ArrayList<StructuralFeature> sfIndirects = sfType2.getStructuralFeatures();
									for (StructuralFeature sf : (StructuralFeature[]) sfIndirects.toArray(new StructuralFeature[sfIndirects.size()])) {
										if(!sf.getType().equalNames(sfNew.getSourceConcept()))
											sfIndirects.remove(sf);
									}
//									System.out.println("sfNew: " + sfNew);
//									System.out.println("sfIndirects: " + sfIndirects);
									if(sfIndirects != null && !sfIndirects.isEmpty()){
										sfIndirect = Utils.getRandom(sfIndirects);
										try {
//											System.out.println("1.mutationIndirectMove: "+pce);
											res = indirectionMove(pce, sfAffecting, sfOld, sfIndirect);
											this.modified |= res;
											hasMoved = res;
//											System.out.println("2.mutationIndirectMove: "+pce);
										} catch (Exception e) {
//											System.out.println("EEEEEEEEEhhhhh.");
											e.printStackTrace();
										}
									}
								}
							}
						}
					} 
					
					if( !probaMove || (probaMove && !hasMoved) ) {
						StructuralFeature newElement = Utils.getRandom(loadStructralFeatures(sfAffecting, DIFF_TYPE.ADD));
						
						ArrayList<TypedElement<?>> tes = getOCLElementsAffectedByMMElement(sfAffecting);
	//					System.out.println("Contrainte.getTEsAffectedBy()");
	//					
	//					System.out.println(tes);
						
						if(Utils.getRandomDouble()>0.6){//Simple rename
							res = renameProcess(sfAffecting, newElement, (TypedElement<?>)eo);
						} else { //Multi rename anywhere the MMElement is used in the containing tree.
							for (TypedElement<?> typedElement : (TypedElement<?>[]) tes.toArray(new TypedElement<?>[tes.size()])) {
								res |= renameProcess(sfAffecting, newElement, typedElement);
								numberOfChanges--; //Quick and dirty : we want to count 1 change only for the multi rename.
							}
							numberOfChanges++;
						}
					}
//					res = renameProcess(sfAffecting, newElement, (TypedElement<?>)eo);
				}
			}
		}
		return res;
	}

	public boolean enumMutation(boolean res, OperationCallExp<?, EOperation> pceFisrtBool) {
//							System.out.println("2. (Contrainte.mutate) : "+Utils.printEStructuralFeatures(pceFisrtBool.getReferredOperation()));;
//							System.out.println("IN THE CONDITIONAL");
		OperationCallExp<EClassifier, EOperation> newOpCallExp = EcoreFactory.eINSTANCE.createOperationCallExp();
		
		//kind->self   =>  self | getsource.getsource => getsource
		OCLExpression<EClassifier> newSource =  EcoreUtil.copy(((PropertyCallExp<EClassifier, EOperation>)pceFisrtBool.getSource()).getSource());
		newOpCallExp.setSource(newSource);
		
		
		EOperation eop = Program.getEOperation("oclIsTypeOf");
		newOpCallExp.setOperationCode( OPERATION_CODE_ISTYPEOF);
		newOpCallExp.setReferredOperation(eop);
		
		TypeExp<EClassifier> typeExp = EcoreFactory.eINSTANCE.createTypeExp();
		OCLExpression<?> oclExp = pceFisrtBool.getArgument().get(0);
		Concept enumLitToConcept = Metamodel.getMm2().getConcept(((EnumLiteralExp<?, EEnumLiteral>)oclExp).getReferredEnumLiteral().getName());
		typeExp.setType(enumLitToConcept.getEModelElement());
		typeExp.setReferredType(enumLitToConcept.getEModelElement());
		newOpCallExp.getArgument().add(typeExp);					
		
		
		EStructuralFeature parentSF = pceFisrtBool.eContainingFeature();
		EObject eoParent = pceFisrtBool.eContainer();
		
		if(eoParent != null){
			Object toCollapse = eoParent.eGet(parentSF, false);
			if (parentSF.isMany() || toCollapse instanceof Collection<?>) {
				ArrayList<OCLExpression<?>> al = new ArrayList<>(1);
				al.add(newOpCallExp);
				eoParent.eSet(parentSF, al);
			} else {
				eoParent.eSet(parentSF, newOpCallExp);
			}
			res = true;
		}
		
		this.modified = true;
		return res;
	}
	
	
	/**
	 * @param pce
	 * @param sfIndirect
	 */
	public boolean indirectionMove(PropertyCallExp<EClassifier, EStructuralFeature> pce, StructuralFeature sfAffecting, StructuralFeature sfSource, StructuralFeature sfIndirect) {
//		System.out.println("Contrainte.indirectionMove("+pce+","+sfIndirect+")");
		if(! (pce.getSource() instanceof VariableExp<?, ?>))
			return false;
		
		PropertyCallExp<EClassifier, EStructuralFeature> pceIndirect = EcoreFactory.eINSTANCE.createPropertyCallExp();
		pceIndirect.setName(sfIndirect.getName());
		pceIndirect.setReferredProperty(sfIndirect.geteStructuralFeature());
		pceIndirect.setType(sfIndirect.getSourceConcept().getEModelElement());
		pceIndirect.setSource(pce.getSource());
		pce.setSource(pceIndirect);
		
		updateAffectingList(sfAffecting, sfIndirect.getEModelElement());
		notifyAndStoreMutation(sfSource, sfIndirect, pce, MutationType.MOVE);
		return true;
	}


	
	
	/**
	 * At the bigining, a list of elements affected by the evolution is built up (see {@link #structuralFeaturesAffectingOCL}). This method remove elements which had been mutated (and thus don't exist anymore)
	 * @param mme
	 * @param te OCL element using the <code>mme</code>
	 */
	void updateAffectingList(MMElement mme, EObject te) {
		cleanDanglingTypedElements();
		ArrayList<TypedElement<?>> tes = structuralFeaturesAffectingOCL.get(mme);
		if(tes == null)
			return;
		if(!tes.contains(te))
			return;
		
		//Remove from sublist
		if(te != null)
			tes.remove(te);
		
		if (tes.isEmpty()) 
			structuralFeaturesAffectingOCL.remove(mme);
//		LOGGER.finest("Updated list :" + structuralFeaturesAffectingOCL);
	}

	/**
	 * Remove dangling OCL elements from the affecteing and modified list : lists of ocl element being affected by evolution or modifified by mutation
	 * 
	 * A dangling OCL element is an element that doesn't belong to any Constraint any more (being part of a previous collapsed sub OCL tree or so).
	 */
	public void cleanDanglingTypedElements() {
		HashMap<StructuralFeature, ArrayList<TypedElement<?>>> tmp = new HashMap<>(2);
		for (StructuralFeature mme2 : structuralFeaturesAffectingOCL.keySet()) {
			ArrayList<TypedElement<?>> tesToRemove = new ArrayList<TypedElement<?>>(2);
			ArrayList<TypedElement<?>> testmp = structuralFeaturesAffectingOCL.get(mme2);
			for (TypedElement<?> te2 : testmp) {
				if(getConstraintAncestor(te2) == null) //If no constraint ancestor --> Dangling "te" !!
					tesToRemove.add(te2);
			}			
			tmp.put(mme2, tesToRemove);
		}
		for (StructuralFeature mme2 : tmp.keySet()) {
			for (TypedElement<?> te2 : tmp.get(mme2)) {
				structuralFeaturesAffectingOCL.get(mme2).remove(te2);
			}
			if(structuralFeaturesAffectingOCL.get(mme2).isEmpty())
				structuralFeaturesAffectingOCL.remove(mme2);
		}
		
		EObject[] tmp2 = (EObject[]) modifications_encours.toArray(new EObject[modifications_encours.size()]);
		for (EObject eObject : tmp2) {
			if(getConstraintAncestor(eObject) == null)
				modifications_encours.remove(eObject);
		}
		
	}

	/**
	 * Store mutation for further processing.
	 * @param mme MMElement involved
	 * @param mutatedmme New MMElement
	 * @param te OCL element involved
	 * @param mutationType see {@link MutationType}
	 */
	protected void notifyAndStoreMutation(MMElement mme, MMElement mutatedmme, EObject te, MutationType mutationType) {
//		System.out.println("Contrainte.notifyAndStoreMutation("+mutationType+":"+te+")");
		if (te != null) {
			ArrayList<PropertyCallExp<?, ?>> pces = new ArrayList<>(2);
			pces.addAll(getPropertyCallExpInvolved((TypedElement<?>) te));
			for (PropertyCallExp<?, ?> pce : pces) {
				if (!modifications_encours.contains(pce))
					modifications_encours.add(pce);
			}
		}
		Mutation m = new Mutation(mutationType, te, mutatedmme == null ? null : mutatedmme.getEModelElement(), mme.getEModelElement());
		pastMutations.add(m);
		
		numberOfChanges += mutationType.changeCost();
		numberOfAllChanges++;
		this.modified = true;
	}

	/**
	 * 
	 * @param te
	 * @return a list of the PropertyCallExps found in the EObject tree passed
	 *         in parameter (if not OCL, return an empty list).
	 */
	public ArrayList<PropertyCallExp<EClassifier, EStructuralFeature>> getPropertyCallExpInvolved(EObject te){
		ArrayList<PropertyCallExp<EClassifier, EStructuralFeature>> res = new ArrayList<>();
		if(te instanceof PropertyCallExp<?, ?>)// || te instanceof Variable)
			res.add((PropertyCallExp<EClassifier, EStructuralFeature>)te);
		for (int i = 1; i < te.eContents().size(); i++) {   //eContents().get(0) = GenericType !
			EObject eo = te.eContents().get(i);
//			System.out.println(" - "+i+". "+eo+" "+eo.eClass().getName());
			ArrayList<PropertyCallExp<EClassifier, EStructuralFeature>> res2 =getPropertyCallExpInvolved(eo);
			res.addAll(res2);
		}
//		System.out.println("   ->"+te+">"+res);
		return res;
	}

	
	public ArrayList<StructuralFeature> loadStructralFeatures(StructuralFeature sfAffecting, DIFF_TYPE... dts) {
		MetamodelMerger mmMerger = MetamodelMerger.getInstance();

		ArrayList<StructuralFeature> loadelements = new ArrayList<StructuralFeature>();
		ArrayList<StructuralFeature> elements;
		if(sfAffecting != null) {
			for (DIFF_TYPE dt : dts) {
					elements = mmMerger.getDiffStructuralFeatures(dt);
					for (StructuralFeature sf2 : elements)
						if(!sfAffecting.equalNames(sf2))
							for (int i = 0; i < MUT_RENAME_EXPAND_PROBAILITY_ADDED ; i++)
								loadelements.add(sf2);
			}
			
			
			if(!mmMerger.getRemovedConcepts().contains(sfAffecting.getType()))
				for (StructuralFeature sf2 : Metamodel.getMm1().get_T_StructuralFeatures(sfAffecting.getType(), null)) {
					for (int i = 0; i < MUT_RENAME_EXPAND_PROBAILITY_SAMETYPE; i++)
						if(!sfAffecting.equalNames(sf2))
							loadelements.add(sf2);
				}
				
			if(!mmMerger.getRemovedConcepts().contains(sfAffecting.getSourceConcept()))
				for (StructuralFeature sf2 : Metamodel.getMm1().get_T_StructuralFeatures(sfAffecting.getSourceConcept(), null)) {
					for (int i = 0; i < MUT_RENAME_EXPAND_PROBAILITY_SAMESOURCE; i++)
						if(!sfAffecting.equalNames(sf2))
							loadelements.add(sf2);
				}
		}
		
		for (StructuralFeature sf2 : Metamodel.getMm2().get_T_StructuralFeatures(null, null)) {
			for (int i = 0; i < MUT_RENAME_EXPAND_PROBAILITY_THEREST; i++)
				if(sfAffecting== null || (sfAffecting != null && !sfAffecting.equalNames(sf2)))
					loadelements.add(sf2);
		}
		return loadelements;
	}
	
	
	


	/**
	 * change context with a random concept picked from the ADDED in Evolution (see {@link #changeContextType(boolean) changeContextType(TRUE)}).
	 * 
	 * @return
	 */
	public boolean changeContextType() {
		return changeContextType(MUT_CHANGE_CONTEXT_INCLUDEALL);
	}
	/**
	 * 
	 * Look for a new Concept to affect as context.<br/>
	 * Concept ADDED during evolution get 10x chances to get elected over the other ones.
	 * 
	 * @param forceAdded if <code>true</code>, the new concept is taken ONLY from the added during evolution. 
	 * 
	 * @return true if the context has changed after execution.
	 */
	public boolean changeContextType(boolean forceAdded) {
		logMutation.fine(""+this);
		ArrayList<Concept> roulette = new ArrayList<>();
		for (Concept concept : MetamodelMerger.getInstance().getDiffConcepts(DIFF_TYPE.ADD)) {
			if(!concept.getEModelElement().equals(getEContext()))
				for (int i = 0; i < MUT_NEW_CONTEXT_EXPAND_PROBABILITY_FOR_ADDED_ELTS; i++) 
					roulette.add(concept);
		}
		if(forceAdded)
			for (Concept concept : Metamodel.getMm2().getConcepts().values()) 
				if(!concept.getEModelElement().equals(getEContext()))
					roulette.add(concept);
		Concept c = Utils.getRandom(roulette);
		
		if(c == null)
			return false;
		
		if(c.getEModelElement().getName().equals(cstOcl.getSpecification().getContextVariable().getType().getName()))
			return false;
		
		
		LOGGER.finer("Concept changed to"+c);
		return changeContextType(c);
	}
	
	/**
	 * Modify the context variable of the constraint.
	 * @param <code>newType</code> the new context of the constraint.
	 * @return <code>true</code>
	 */
	public boolean changeContextType(Concept newType) {
		Variable<EClassifier,?> v = cstOcl.getSpecification().getContextVariable();
		v.eSet(v.eClass().getEStructuralFeature("eType"), newType.getEModelElement());
		updateAffectingList(newType, v);
		notifyAndStoreMutation(newType, newType, v, MutationType.CONTEXT_CHANGE);
		return true;
	}
	
	/**
	 * Return true if the TypedElement passed in paramter is the Variable context of the constraint.
	 * @param te
	 * @return  <code>true</code> if the TypedElement passed in paramter is the Variable context of the constraint.
	 */
	public boolean isContextVariable(TypedElement<?> te){
		if(getFirstBooleanAncestor(te) == null || getFirstBooleanAncestor(te) instanceof Constraint)
			return getConstraintAncestor(te) != null && getConstraintAncestor(te).getSpecification().getContextVariable().equals(te);
		return false;
	}

	/**
	 * Unrecommended method.
	 * Prefer {@link #notifyAndStoreMutation(...)}
	 * 
	 * @param b
	 */
	public void setModified(boolean b) {
		this.modified = b;
	}
	
	public String printConstraintAsXML() {
		Element xmlAST = (Element) cstOcl.getSpecification().getBodyExpression().accept(ASTforOCL.getEcoreVersion());
		org.jdom2.output.XMLOutputter xmlOutputter = new XMLOutputter( Format.getPrettyFormat());
		String s = xmlOutputter.outputString(xmlAST);
		return s;
	}
	public String printPrettyConstraintAsParseableOCL() {
		String oclStr = (String) cstOcl.getSpecification().accept(PrettyPrintForOCL.getEcoreVersion());
		return oclStr;
	}

	
	
	public static String printConstraintAsXML(OCLExpression<?> te) {
		Element xmlAST = (Element) te.accept(ASTforOCL.getEcoreVersion());
		org.jdom2.output.XMLOutputter xmlOutputter = new XMLOutputter( Format.getPrettyFormat());
		String s = xmlOutputter.outputString(xmlAST);
		return s;
	}
	
	public static String printConstraintAsXML(Constraint c) {
		Element xmlAST = (Element) c.getSpecification().getBodyExpression().accept(ASTforOCL.getEcoreVersion());
		org.jdom2.output.XMLOutputter xmlOutputter = new XMLOutputter( Format.getPrettyFormat());
		String s = xmlOutputter.outputString(xmlAST);
		return s;
	}
	
	public static String printPrettyConstraintAsParseableOCL(Constraint c) {
		if(c == null)
			return "<Constraint is null>";
		String oclStr = (String) c.getSpecification().getBodyExpression().accept(PrettyPrintForOCL.getEcoreVersion());
		return oclStr;
	}
	

	public int evaluateInformationLoss() {
		int	occurrences=0;
		//if(numberOfDiffs < 0 || modified) {
		
		ArrayList<StructuralFeature> sfmerge = MetamodelMerger.getInstance().getDiffStructuralFeatures(DIFF_TYPE.REMOVE);
		
		Contrainte c0 = Program.getInitialProgram().getContrainte(number);
		ArrayList<StructuralFeature> sfsc0 = c0.getStructuralFeaturesInvolved(sfmerge);
		ArrayList<StructuralFeature> sfsc = getStructuralFeaturesInvolved(sfmerge);
			
			
		ArrayList<StructuralFeature> sfscTEMP = getStructuralFeaturesInvolved(sfmerge);
		ArrayList<StructuralFeature> sfscTEMPc0 = c0.getStructuralFeaturesInvolved(sfmerge);
		
		Iterator<StructuralFeature> iterator2 = sfscTEMP.iterator();
		Iterator<StructuralFeature> iteratorC0 = sfscTEMPc0.iterator();
		
//		 System.out.println("La liste est : "+sfscTEMP);
			
//			System.out.println("sfsc0:   " + sfsc0);
//			System.out.println("sfsc:    " + sfsc);
//			System.out.println("sfmerge: " + sfmerge);
		/*	 while ( iterator2.hasNext() ) {
				 StructuralFeature sf = iterator2.next();
				 System.out.println("sf="+sf);
				 
			 }
			*/
			 
			 
		int allOccurences = 0;

		while (iterator2.hasNext()) {

			StructuralFeature sf = iterator2.next();

			// occurrences = Collections.frequency(sfsc, sf);
			/*
			 * if(!(sfsc0.contains(sf))) numberOfDiffs++;
			 */
			if (sfsc0.contains(sf)) {
//				System.out.println("");
				occurrences = Collections.frequency(sfsc, sf);
				sfscTEMP.removeAll(Collections.singleton(sf));
//				System.out.println("La liste devient : " + sfscTEMP);

//				System.out.println("Occurence of SF : " + sf + " est= " + occurrences);

				iterator2 = sfscTEMP.iterator();
//				System.out.println("occurences of " + sf + " in prog0.contrainte =" + Collections.frequency(sfsc0, sf));
				allOccurences = allOccurences + (Collections.frequency(sfsc0, sf) - occurrences);
			}
		}

		while (iteratorC0.hasNext()) {
			StructuralFeature sf = iteratorC0.next();
			if (!(sfsc.contains(sf))) {
//				System.out.println("Element " + sf + " does not exist in this current constraint");
				allOccurences++;
			}
		}

//		System.out.println(" information loss ="+allOccurences);
//		System.out.println("\n");
			
		//}
		return allOccurences;
	}

	
	
	/**
	 * @return The list of StructuralFeatures involved in the execution of the Contrainte.
	 */
	public ArrayList<StructuralFeature> getStructuralFeaturesInvolved() {
		return getStructuralFeaturesInvolved(new ArrayList<>(1));
	}

	
	/**
	 * 
	 * @param exclusions StructuralFeatures to exclude from the result list.
	 * @return The list of StructuralFeatures involved in the execution of the Contrainte.
	 */
	public ArrayList<StructuralFeature> getStructuralFeaturesInvolved(ArrayList<StructuralFeature> exclusions) {
		
		//if(structuralFeaturesInvolved == null || modified){
			ArrayList<PropertyCallExp<EClassifier, EStructuralFeature>> callExps = getPropertyCallExpInvolved(getEBodyExpression());
			structuralFeaturesInvolved = new ArrayList<>();
			for (PropertyCallExp<EClassifier, EStructuralFeature> propertyCallExp : callExps) {
				StructuralFeature sf = getStructuralFeatureInvolved(propertyCallExp);
				if(sf != null)
					structuralFeaturesInvolved.add(sf);
			}
//			Collections.sort(structuralFeaturesInvolved, (StructuralFeature o1, StructuralFeature o2) ->  o1.toString().compareTo(o2.toString()) );
			Iterator<StructuralFeature> iterator = structuralFeaturesInvolved.iterator();
			while ( iterator.hasNext() ) {
				StructuralFeature sf = iterator.next();
			    if (exclusions.contains(sf)) {
			        iterator.remove();
			    }
			}
			//}
			
		return structuralFeaturesInvolved;
	}

	public int getInformationLoss() {
		return evaluateInformationLoss();
	}

	public int setSyntaxErrors(int checkSyntaxOCL) {
		this.syntaxErrors = checkSyntaxOCL;
		return syntaxErrors;
	}
	
	public int getSyntaxErrors() {
		return syntaxErrors;
	}

	public String printModifications(){
		String res = "";
		for (PropertyCallExp<?, ?> pce : modifications_encours) {
			res += "\n   - ("+((EStructuralFeature)pce.getReferredProperty()).getName()+")";
		}
		return getName()+"{"+res+"  }";
	}


	public String printPastMutations() {
		String res = "";
		for (Mutation m : pastMutations) {
			res += "\n   - "+m.prettyPrint();
		}
		return getName()+"{"+res+"  }";
	}

	public boolean hasBeenMutated() {
		return pastMutations.size() > 0;
	}
	ArrayList<String> syntaxErrorsText = new ArrayList<>(1);
	public void setSyntaxErrorsText(ArrayList<String> sErr) {
		syntaxErrorsText = sErr;
	}

	public String printSyntaxErrors(String tab) {
		if(syntaxErrorsText.isEmpty())
			return "";
		String res = tab+name+" errors:";
		for (String sErr : syntaxErrorsText) {
			res += tab + " - "+sErr;
		}
		return res;
	}
	
}


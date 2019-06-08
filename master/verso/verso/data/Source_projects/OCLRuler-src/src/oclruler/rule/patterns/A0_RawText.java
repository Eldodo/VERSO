package oclruler.rule.patterns;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.ecore.CollectionItem;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.expressions.Variable;

import oclruler.genetics.EvaluatorOCL;
import oclruler.metamodel.Concept;
import oclruler.metamodel.MMElement;
import oclruler.metamodel.Metamodel;
import oclruler.rule.MMMatch;
import oclruler.utils.ToolBox;

/**
 * Text rule. Used for oracle input - not for rule generation/learning.
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class A0_RawText extends Pattern {
	public static Logger LOGGER = Logger.getLogger(A0_RawText.class.getName());

	private String text = "";
	private File f;
	OCLExpression<EClassifier> oclExp;

	public A0_RawText(Concept context, String text, File f) throws ParserException {
		this("A0_TestingText", context, text, f);
	}

	public A0_RawText(String name, Concept context, String text, File f) throws ParserException {
		super(name, context);
		this.f = f;
		this.text = text;
		oclExp = EvaluatorOCL.check(this);
	}
	
	
	static String[] listESFToIgnoreWhileVisitingOCLEObjects = new String[] {
		"name",
		"many",
		"lowerBound",
		"upperBound",
		"ordered",
		"required",
		"unique",
		"startPosition",
		"endPosition",
		"propertyStartPosition",
		"propertyEndPosition",
		"eAnnotations",
		"eGenericType",
		"eType",
		"markedPre",
		"operationCode",
		"eStructuralFeatures",
	};
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<MMElement> getMMElements() {
//		if(!Config.METAMODEL_NAME.equals("Family"))
//			return super.getMMElements();
//		System.out.println("A0_RawText.getMMElements() "+oclExp);
		
		HashMap<String, Collection<Object>> featuresObjects = ToolBox.getEStructuralFeaturesObjects(oclExp, listESFToIgnoreWhileVisitingOCLEObjects);
//		System.out.println(featuresObjects);
		Collection<ENamedElement> eRes = new HashSet<>();
		eRes.add(context.getEClassifier());
		
		for (String esfName : featuresObjects.keySet()) {
			Collection<Object> ftObjects = featuresObjects.get(esfName);
//			System.out.println("  - "+esfName+": "+ftObjects.getClass().getSimpleName()+": "+ftObjects);
			
			if(ftObjects != null){
				for (Object oo : (Collection<?>)ftObjects) {
					if(oo != null) {
//						System.out.println("        : "+oo.getClass().getSimpleName()+": "+ftObjects);
						if(oo instanceof OCLExpression)
							eRes.addAll(getMMElements((OCLExpression<EClassifier>)oo));
						else if((oo instanceof Variable)){
//							//Variables are already declared anyway
						}
							//else System.out.println("Not supposed to get there (A0_RawText l.98)  "+oo.getClass().getName());//EOperation
					}
				}
			} 
		}

		return translateEClassifiers(eRes);
	}
	
	public Collection<MMElement> translateEClassifiers(Collection<ENamedElement> nameds){
		Collection<MMElement> res = new HashSet<>(nameds.size());
		for (ENamedElement n : nameds) {
			if( n == null) continue;
			MMElement mme = Metamodel.getMMElement(n);
			res.add(mme);
		}
		
		return res;
	}
	
	/**
	 * Costly in instanceof
	 * @param oclExpre
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Collection<ENamedElement> getMMElements(OCLExpression<EClassifier> oclExpre) {
		Collection<ENamedElement> res = new HashSet<>();
		HashMap<String, Collection<Object>> featuresObjects = ToolBox.getEStructuralFeaturesObjects(oclExpre, listESFToIgnoreWhileVisitingOCLEObjects);
		for (Collection<Object> ftObjs : featuresObjects.values()) {
			for (Object obj : ftObjs) {
				if(obj instanceof OCLExpression<?>){
					res.addAll(getMMElements((OCLExpression<EClassifier>)obj));
				} else {
					if(obj instanceof ENamedElement){
						if(obj instanceof CollectionItem)
							res.addAll(getMMElements(((CollectionItem)obj).getItem()));
						else if(! (obj instanceof EOperation) && ! (obj instanceof Variable))
							res.add((ENamedElement)obj);
					}  //else 
						//System.out.println("   Ahoui: "+obj + " : " + (obj!=null?obj.getClass():""));
				}
			}
		}
//		System.out.println("A0_RawText.getMMElements("+oclExpre+")" + res);
		return res;
	}
	
	@SuppressWarnings("rawtypes")
	public HashSet<Object> getAllEClassifier(Object eo){
//		System.out.println("A0_RawText.getAllEClassifier("+eo+")");
		HashSet<Object> res = new HashSet<>();
		
		
		if(eo instanceof EObject){
			HashMap<String, Collection<Object>> featuresObjects = ToolBox.getEStructuralFeaturesObjects((EObject)eo, listESFToIgnoreWhileVisitingOCLEObjects);
			for (Object o : featuresObjects.values()) {
				
				if(o != null && o instanceof Collection){
					for (Object oo : (Collection)o) {
						if(oo != null) {
							if(oo instanceof EModelElement) {
								res.add(oo);
							} else 
								res.addAll(getAllEClassifier(oo));
						}
					}
					
				} else if(o != null)
					if(o instanceof EModelElement) {
						res.add(o);
					}else 
						res.addAll(getAllEClassifier(o));
			}
		}
		return res;
	}
	
	
	
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Pattern))
			return false;
		Pattern p = (Pattern) o;
		if (!p.getId().equals(getId()))
			return false;
		return true;
	}

	public static boolean PRINT_FILE_LINK_IN_OCL = false;

	@Override
	public String getRawOCLConstraint() {
		return PRINT_FILE_LINK_IN_OCL ? "-- (from file '" + f.getAbsolutePath() + "')\n" : "" + text;
	}

	/**
	 * Not applicable
	 *
	 * public static ArrayList<MMMatch> getMatches() { return null; }
	 */
	@Override
	public String simplePrint() {
		String res = getName() + "(" + getFileName() + "):{";
		for (MMElement elt : getParameters())
			res += elt.simplePrint() + ", ";
		res = res.substring(0, res.length() - 2) + "}";
		return res;
	}

	public String getFileName() {
		return f.getName();
	}

	public static ArrayList<MMMatch> getMatches() {
		ArrayList<MMMatch> res = new ArrayList<>(0);
		return res;
	}

	public static ArrayList<MMMatch> getMatches(Concept c) {
		ArrayList<MMMatch> res = new ArrayList<>(0);
		return res;
	}
}

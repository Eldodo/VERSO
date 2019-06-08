package coocl.ocl;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.ecore.CallOperationAction;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.ecore.SendSignalAction;
import org.eclipse.ocl.ecore.TypeType;
import org.eclipse.ocl.expressions.AssociationClassCallExp;
import org.eclipse.ocl.expressions.BooleanLiteralExp;
import org.eclipse.ocl.expressions.CollectionItem;
import org.eclipse.ocl.expressions.CollectionLiteralExp;
import org.eclipse.ocl.expressions.CollectionRange;
import org.eclipse.ocl.expressions.EnumLiteralExp;
import org.eclipse.ocl.expressions.IfExp;
import org.eclipse.ocl.expressions.IntegerLiteralExp;
import org.eclipse.ocl.expressions.InvalidLiteralExp;
import org.eclipse.ocl.expressions.IterateExp;
import org.eclipse.ocl.expressions.IteratorExp;
import org.eclipse.ocl.expressions.LetExp;
import org.eclipse.ocl.expressions.MessageExp;
import org.eclipse.ocl.expressions.NullLiteralExp;
import org.eclipse.ocl.expressions.OperationCallExp;
import org.eclipse.ocl.expressions.PropertyCallExp;
import org.eclipse.ocl.expressions.RealLiteralExp;
import org.eclipse.ocl.expressions.StateExp;
import org.eclipse.ocl.expressions.StringLiteralExp;
import org.eclipse.ocl.expressions.TupleLiteralExp;
import org.eclipse.ocl.expressions.TupleLiteralPart;
import org.eclipse.ocl.expressions.TypeExp;
import org.eclipse.ocl.expressions.UnlimitedNaturalLiteralExp;
import org.eclipse.ocl.expressions.UnspecifiedValueExp;
import org.eclipse.ocl.expressions.Variable;
import org.eclipse.ocl.expressions.VariableExp;
import org.eclipse.ocl.utilities.AbstractVisitor;
import org.eclipse.ocl.utilities.ExpressionInOCL;
import org.eclipse.ocl.utilities.PredefinedType;
import org.eclipse.ocl.utilities.UMLReflection;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.State;

import utils.Utils;

public class PrettyPrintForOCL<C, O, P, EL, PM, S, COA, SSA, CT> extends AbstractVisitor<String, C, O, P, EL, PM, S, COA, SSA, CT> {
	static Logger LOGGER = Logger.getLogger(PrettyPrintForOCL.class.getName());

	Environment<?, C, O, P, EL, PM, S, COA, SSA, CT, ?, ?> _env = null;

	public static PrettyPrintForOCL<EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint> getEcoreVersion() {
		Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> auxEnv = EcoreEnvironmentFactory.INSTANCE
				.createEnvironment();
		PrettyPrintForOCL<EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint> res = new PrettyPrintForOCL<EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint>(
				auxEnv);
		return res;
	}

	public static PrettyPrintForOCL<Classifier, Operation, Property, EnumerationLiteral, Parameter, State, CallOperationAction, SendSignalAction, Constraint> getUML2Version() {
		org.eclipse.ocl.uml.OCL umlocl = org.eclipse.ocl.uml.OCL.newInstance();
		Environment<Package, Classifier, Operation, Property, EnumerationLiteral, Parameter, State, org.eclipse.uml2.uml.CallOperationAction, org.eclipse.uml2.uml.SendSignalAction, org.eclipse.uml2.uml.Constraint, Class, EObject> auxEnv = umlocl
				.getEnvironment();
		PrettyPrintForOCL<Classifier, Operation, Property, EnumerationLiteral, Parameter, State, CallOperationAction, SendSignalAction, Constraint> res = new PrettyPrintForOCL(auxEnv);
		return res;
	}

	private final UMLReflection<?, C, O, P, EL, PM, S, COA, SSA, CT> uml;

	/**
	 * Initializes me with my environment.
	 * 
	 * @param env
	 *            my environment
	 */
	protected PrettyPrintForOCL(Environment<?, C, O, P, EL, PM, S, COA, SSA, CT, ?, ?> env) {
		_env = env;
		this.uml = (env == null) ? null : env.getUMLReflection();
	}

	/**
	 * Obtains an instance of the <tt>XMLforOCL</tt> visitor for the specified
	 * environment.
	 * 
	 * @param env
	 *            an OCL environment
	 * 
	 * @return the corresponding instance
	 */
	public static <C, O, P, EL, PM, S, COA, SSA, CT> PrettyPrintForOCL<C, O, P, EL, PM, S, COA, SSA, CT> getInstance(Environment<?, C, O, P, EL, PM, S, COA, SSA, CT, ?, ?> env) {

		return new PrettyPrintForOCL<C, O, P, EL, PM, S, COA, SSA, CT>(env);
	}

	// handlers of leaf-nodes

	/*
	 * described in Sec. "What to do in the handler for a leaf node" of the
	 * accompanying Eclipse technical article
	 */

	@Override
	public String visitVariableExp(VariableExp<C, PM> v) {
		LOGGER.finer(""+v);
//		Variable<C, PM> vd = v.getReferredVariable();
//		Element res = new Element("VariableExp"); //$NON-NLS-1$
//		res.setAttribute("name", vd.getName()); //$NON-NLS-1$
//		addTypeInfo(res, v);
		return v.toString();
	}

	@Override
	public String visitTypeExp(TypeExp<C> t) {
//		Element res = new Element("TypeExp"); //$NON-NLS-1$
//		String name = getName(t.getReferredType());
//		res.setAttribute("referredType", name); //$NON-NLS-1$
//		addTypeInfo(res, t);
		return t.toString();
	}

	@Override
	public String visitUnspecifiedValueExp(UnspecifiedValueExp<C> unspecExp) {
		LOGGER.finer(""+unspecExp);
//		Element res = new Element("UnspecifiedValueExp"); //$NON-NLS-1$
		return unspecExp.toString();
	}

	@Override
	public String visitStateExp(StateExp<C, S> stateExp) {
		LOGGER.finer(""+stateExp);
//		Element res = new Element("StateExp"); //$NON-NLS-1$
//		String name = stateExp.getReferredState().toString();
//		res.setAttribute("state", name); //$NON-NLS-1$
		return ""+stateExp;
	}

	// ...LiteralExp

	@Override
	public String visitIntegerLiteralExp(IntegerLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		return literalExp.toString();
	}

	@Override
	public String visitRealLiteralExp(RealLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		return literalExp.toString();
	}

	@Override
	public String visitStringLiteralExp(StringLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		return literalExp.toString();
	}

	@Override
	public String visitBooleanLiteralExp(BooleanLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		return literalExp.toString();
	}

	// two novelties of OCL 2.0: null and OCL_INVALID literals

	@Override
	public String visitNullLiteralExp(NullLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		return literalExp.toString();
	}

	@Override
	public String visitInvalidLiteralExp(InvalidLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		return literalExp.toString();
	}

	@Override
	public String visitEnumLiteralExp(EnumLiteralExp<C, EL> literalExp) {
		LOGGER.finer(""+literalExp);
//		String name = getName(literalExp.getReferredEnumLiteral()) + "::" //$NON-NLS-1$
//				+ getName(literalExp.getReferredEnumLiteral());
		return literalExp.toString();
	}

	@Override
	public String visitUnlimitedNaturalLiteralExp(UnlimitedNaturalLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		return literalExp.toString();
	}

	// handlers of non-leaf nodes

	/*
	 * described in Sec. "What to do in the handlers of non-leaf nodes" of the
	 * accompanying Eclipse technical article
	 */

	@Override
	protected String handleIfExp(IfExp<C> ifExp, String conditionResult, String thenResult, String elseResult) {
		LOGGER.finer(""+ifExp);

		
		
		return "if "+conditionResult+" then "+thenResult+" else "+elseResult+" endif";
	}

	@Override
	protected String handleIteratorExp(IteratorExp<C, PM> callExp, String sourceResult, List<String> variableResults, String bodyResult) {
		LOGGER.finer(""+callExp);
		
		String vars = "";
		for (String arg : variableResults) {
			vars += ", " + arg;
		}
		if(vars.length()>0)
			vars = vars.substring(2);
		
		
		String res = sourceResult +"->"+callExp.getName()+"("+vars+" | "+bodyResult+")";
		
		return res;
	}

	@Override
	protected String handleAssociationClassCallExp(AssociationClassCallExp<C, P> callExp, String sourceResult, List<String> qualifierResults) {
		LOGGER.finer(""+callExp);
	
		return callExp.toString();
	}

	@Override
	protected String handleCollectionItem(CollectionItem<C> item, String itemResult) {
		LOGGER.finer(""+item);
		return itemResult;
	}

	@Override
	protected String handleCollectionRange(CollectionRange<C> range, String firstResult, String lastResult) {
		LOGGER.finer(""+range);
		return range.toString();
	}

	@Override
	protected String handleCollectionLiteralExp(CollectionLiteralExp<C> literalExp, List<String> partResults) {
		LOGGER.finer(""+literalExp);
		return literalExp.toString();
	}

	@Override
	protected String handleIterateExp(IterateExp<C, PM> callExp, String sourceResult, List<String> variableResults, String resultResult, String bodyResult) {
		LOGGER.config(""+callExp);


		return "handleIterateExp";
	}

	@Override
	protected String handleLetExp(LetExp<C, PM> letExp, String variableResult, String inResult) {
		LOGGER.finer(""+letExp);

		return "let "+variableResult + " in " + inResult;
	}

	@Override
	protected String handleMessageExp(MessageExp<C, COA, SSA> messageExp, String targetResult, List<String> argumentResults) {
		LOGGER.finer(""+messageExp);
		//TODO pas fini
		return targetResult;
	}

	protected O getOperation(COA callOperationAction) {
		return (uml == null) ? null : uml.getOperation(callOperationAction);
	}

	protected C getSignal(SSA sendSignalAction) {
		return (uml == null) ? null : uml.getSignal(sendSignalAction);
	}

	@Override
	protected String handleOperationCallExp(OperationCallExp<C, O> callExp, String sourceResult, List<String> argumentResults) {
		LOGGER.finer(""+callExp+"\n - "+sourceResult+"\n - "+argumentResults);
		String opName = ((EOperation)callExp.getReferredOperation()).getName();
		
		
		if(opName.equalsIgnoreCase("allInstances")){
			//Pause
			int i = 0;
			i++;
		}
		
		String arguments = "";
		for (String arg : argumentResults) 
			arguments += ", " + arg;
		if(arguments.length()>0)
			arguments = arguments.substring(2);
		
		String res = "";
		if(isInfix(callExp)){
			res = sourceResult + " " + opName + " " + arguments;
		} else if(opName.equalsIgnoreCase("allInstances")){
			res = sourceResult+".allInstances()";// argumentResults;
		} else {
			//TODO Must be other exceptions... Attention.
//			System.out.println("Operation not infix. (PrettyPrintforOCL.handleOperationCallExp) : "+opName+" | '"+arguments+"'");
			if(callExp.getOperationCode() == PredefinedType.NOT)
				res = opName + "("+ sourceResult +")";
			else {
				res = sourceResult+"->"+opName+"("+arguments+")";// argumentResults;
			}
		}
		
		return res;

	}

	@Override
	protected String handlePropertyCallExp(PropertyCallExp<C, P> callExp, String sourceResult, List<String> qualifierResults) {
		LOGGER.finer(""+callExp);
		return callExp.toString();
	}

	@Override
	protected String handleTupleLiteralExp(TupleLiteralExp<C, P> literalExp, List<String> partResults) {
		LOGGER.finer(""+literalExp);
		return literalExp.toString();
	}

	@Override
	protected String handleTupleLiteralPart(TupleLiteralPart<C, P> part, String valueResult) {
		LOGGER.finer(""+part);
//		String varName = getName(part);
//		C type = part.getType();
		return part.toString();
	}

	@Override
	protected String handleVariable(Variable<C, PM> variable, String initResult) {
		LOGGER.finer(""+variable);

		
		if(initResult != null){
			C ec = variable.getType();
			String tName = getName(ec);
			String res = variable.getName() + " : "+tName +" = ";
			return res + initResult;
		}
		return variable.toString();
	}

	@Override
	protected String handleConstraint(CT constraint, String specificationResult) {
		LOGGER.finer(""+constraint);
//		Element res = new Element("Constraint");
//		res.addContent(specificationResult);
		return specificationResult;
	}

	@Override
	protected String handleExpressionInOCL(ExpressionInOCL<C, PM> callExp, String contextResult, String resultResult, List<String> parameterResults, String bodyResult) {
		LOGGER.finer(""+callExp);
		C ec = callExp.getContextVariable().getType();
		String tName = getName(ec);
		String name = callExp.eContainer() instanceof Constraint?
				((Constraint)callExp.eContainer()).getName():
				"generatedName"+Utils.getRandomInt(1000000);
		return "context " +tName+" inv "+name+": " +bodyResult;//callExp.toString();

	}




	/**
	 * Null-safe access to the name of a named element.
	 * 
	 * @param named
	 *            a named element or <code>null</code>
	 * @return a name, or the null placeholder if the named element or its name
	 *         be <code>null</code>. i.e., <code>null</code> is never returned
	 */
	protected String getName(Object named) {
		String res = (uml == null) ? XML_NULL_PLACEHOLDER : uml.getName(named);
		if (res == null) {
			res = XML_NULL_PLACEHOLDER;
		}
		return res;
	}

	/**
	 * Null-safe access to the qualified name of a named element.
	 * 
	 * @param named
	 *            a named element or <code>null</code>
	 * @return a qualified name, or the null placeholder if the named element or
	 *         its name be <code>null</code>. i.e., <code>null</code> is never
	 *         returned
	 */
	protected String getQualifiedName(Object named) {
		return (uml == null) ? XML_NULL_PLACEHOLDER : uml.getQualifiedName(named);
	}

	/**
	 * Indicates where a required element in the AST was <code>null</code>, so
	 * that it is evident in the debugger that something was missing. We don't
	 * want just <code>"null"</code> because that would look like the OclVoid
	 * literal.
	 */
	private static String XML_NULL_PLACEHOLDER = "NONE"; //$NON-NLS-1$

	private boolean isStdlibOperation(O o) {
		C declaringClass = uml.getOwningClassifier(o);
		String pName = getName(uml.getPackage(declaringClass));
		boolean res1 = pName.equals("oclstdlib");
		return res1;
	}

	private boolean isInfix(OperationCallExp<C, O> oc) {
		switch (oc.getOperationCode()) {

		case PredefinedType.AND:
		case PredefinedType.OR:
		case PredefinedType.XOR:
		case PredefinedType.IMPLIES:

		case PredefinedType.GREATER_THAN:
		case PredefinedType.GREATER_THAN_EQUAL:
		case PredefinedType.LESS_THAN:
		case PredefinedType.LESS_THAN_EQUAL:

		case PredefinedType.EQUAL:
		case PredefinedType.NOT_EQUAL:

		case PredefinedType.DIVIDE:
		case PredefinedType.MINUS:
		case PredefinedType.PLUS:
		case PredefinedType.TIMES:

			return true;
		}

		return false;
	}

	private boolean isPrefix(OperationCallExp<C, O> oc) {
		if (isInfix(oc)) {
			return false;
		}
		return true;
	}

	private String howManyArgs(O o) {
		int n = uml.getParameters(o).size();
		switch (n) {
		case 0:
			return "zero args"; //$NON-NLS-1$
		case 1:
			return "one arg"; //$NON-NLS-1$
		case 2:
			return "two args"; //$NON-NLS-1$
		default:
			return n + " args"; //$NON-NLS-1$
		}
	}

}

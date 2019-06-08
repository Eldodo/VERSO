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
import org.eclipse.ocl.types.CollectionType;
import org.eclipse.ocl.utilities.AbstractVisitor;
import org.eclipse.ocl.utilities.ExpressionInOCL;
import org.eclipse.ocl.utilities.PredefinedType;
import org.eclipse.ocl.utilities.TypedElement;
import org.eclipse.ocl.utilities.UMLReflection;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.State;
import org.jdom2.Element;

public class ASTforOCL<C, O, P, EL, PM, S, COA, SSA, CT> extends AbstractVisitor<org.jdom2.Element, C, O, P, EL, PM, S, COA, SSA, CT> {
	static Logger LOGGER = Logger.getLogger(ASTforOCL.class.getName());

	Environment<?, C, O, P, EL, PM, S, COA, SSA, CT, ?, ?> _env = null;

	public static ASTforOCL<EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint> getEcoreVersion() {
		Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> auxEnv = EcoreEnvironmentFactory.INSTANCE.createEnvironment();
		ASTforOCL<EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint> res = new ASTforOCL<EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint>(
				auxEnv);
		return res;
	}

	public static ASTforOCL<Classifier, Operation, Property, EnumerationLiteral, Parameter, State, CallOperationAction, SendSignalAction, Constraint> getUML2Version() {
		org.eclipse.ocl.uml.OCL umlocl = org.eclipse.ocl.uml.OCL.newInstance();
		Environment<Package, Classifier, Operation, Property, EnumerationLiteral, Parameter, State, org.eclipse.uml2.uml.CallOperationAction, org.eclipse.uml2.uml.SendSignalAction, org.eclipse.uml2.uml.Constraint, Class, EObject> auxEnv = umlocl
				.getEnvironment();
		ASTforOCL<Classifier, Operation, Property, EnumerationLiteral, Parameter, State, CallOperationAction, SendSignalAction, Constraint> res = new ASTforOCL(auxEnv);
		return res;
	}

	private final UMLReflection<?, C, O, P, EL, PM, S, COA, SSA, CT> uml;

	/**
	 * Initializes me with my environment.
	 * 
	 * @param env
	 *            my environment
	 */
	protected ASTforOCL(Environment<?, C, O, P, EL, PM, S, COA, SSA, CT, ?, ?> env) {
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
	public static <C, O, P, EL, PM, S, COA, SSA, CT> ASTforOCL<C, O, P, EL, PM, S, COA, SSA, CT> getInstance(Environment<?, C, O, P, EL, PM, S, COA, SSA, CT, ?, ?> env) {

		return new ASTforOCL<C, O, P, EL, PM, S, COA, SSA, CT>(env);
	}

	// handlers of leaf-nodes

	/*
	 * described in Sec. "What to do in the handler for a leaf node" of the
	 * accompanying Eclipse technical article
	 */

	@Override
	public Element visitVariableExp(VariableExp<C, PM> v) {
		LOGGER.finer(""+v);
		Variable<C, PM> vd = v.getReferredVariable();
		Element res = new Element("VariableExp"); //$NON-NLS-1$
		res.setAttribute("name", vd.getName()); //$NON-NLS-1$
		addTypeInfo(res, v);
		return res;
	}

	@Override
	public Element visitTypeExp(TypeExp<C> t) {
		LOGGER.finer(""+t);
		Element res = new Element("TypeExp"); //$NON-NLS-1$
		String name = getName(t.getReferredType());
		res.setAttribute("referredType", name); //$NON-NLS-1$
		addTypeInfo(res, t);
		return res;
	}

	@Override
	public Element visitUnspecifiedValueExp(UnspecifiedValueExp<C> unspecExp) {
		LOGGER.finer(""+unspecExp);
		Element res = new Element("UnspecifiedValueExp"); //$NON-NLS-1$
		return res;
	}

	@Override
	public Element visitStateExp(StateExp<C, S> stateExp) {
		LOGGER.finer(""+stateExp);
		Element res = new Element("StateExp"); //$NON-NLS-1$
		String name = stateExp.getReferredState().toString();
		res.setAttribute("state", name); //$NON-NLS-1$
		return res;
	}

	// ...LiteralExp

	@Override
	public Element visitIntegerLiteralExp(IntegerLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		
		Element res = new Element("IntegerLiteralExp"); //$NON-NLS-1$
		res.setAttribute("symbol", Integer.toString(literalExp.getIntegerSymbol())); //$NON-NLS-1$
		addTypeInfo(res, literalExp);
		return res;
	}

	@Override
	public Element visitRealLiteralExp(RealLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		Element res = new Element("RealLiteralExp"); //$NON-NLS-1$
		res.setAttribute("symbol", Double.toString(literalExp.getRealSymbol())); //$NON-NLS-1$
		return res;
	}

	@Override
	public Element visitStringLiteralExp(StringLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		Element res = new Element("StringLiteralExp"); //$NON-NLS-1$
		res.setAttribute("symbol", literalExp.getStringSymbol()); //$NON-NLS-1$
		addTypeInfo(res, literalExp);
		return res;
	}

	@Override
	public Element visitBooleanLiteralExp(BooleanLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		Element res = new Element("BooleanLiteralExp"); //$NON-NLS-1$
		res.setAttribute("symbol", Boolean.toString(literalExp.getBooleanSymbol())); //$NON-NLS-1$
		addTypeInfo(res, literalExp);
		return res;
	}

	// two novelties of OCL 2.0: null and OCL_INVALID literals

	@Override
	public Element visitNullLiteralExp(NullLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		Element res = new Element("NullLiteralExp"); //$NON-NLS-1$
		addTypeInfo(res, literalExp);
		return res;
	}

	@Override
	public Element visitInvalidLiteralExp(InvalidLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		Element res = new Element("InvalidLiteralExp"); //$NON-NLS-1$
		addTypeInfo(res, literalExp);
		return res;
	}

	@Override
	public Element visitEnumLiteralExp(EnumLiteralExp<C, EL> literalExp) {
		LOGGER.finer(""+literalExp);
		Element res = new Element("EnumLiteralExp"); //$NON-NLS-1$
		String name = getName(literalExp.getReferredEnumLiteral()) + "::" //$NON-NLS-1$
				+ getName(literalExp.getReferredEnumLiteral());
		res.setAttribute("literal", name); //$NON-NLS-1$
		return res;
	}

	@Override
	public Element visitUnlimitedNaturalLiteralExp(UnlimitedNaturalLiteralExp<C> literalExp) {
		LOGGER.finer(""+literalExp);
		Element res = new Element("UnlimitedNaturalLiteralExp"); //$NON-NLS-1$
		res.setAttribute("symbol", Integer.toString(literalExp.getIntegerSymbol())); //$NON-NLS-1$
		addTypeInfo(res, literalExp);
		return res;
	}

	// handlers of non-leaf nodes

	/*
	 * described in Sec. "What to do in the handlers of non-leaf nodes" of the
	 * accompanying Eclipse technical article
	 */

	@Override
	protected org.jdom2.Element handleIfExp(IfExp<C> ifExp, Element conditionResult, Element thenResult, Element elseResult) {
		LOGGER.finer(""+ifExp);
		Element res = new Element("IfExp"); //$NON-NLS-1$
		Element eConditionPart = new Element("condition"); //$NON-NLS-1$
		eConditionPart.addContent(conditionResult);
		res.addContent(eConditionPart);

		Element eThenPart = new Element("then"); //$NON-NLS-1$
		eThenPart.addContent(thenResult);
		res.addContent(eThenPart);

		Element eElsePart = new Element("else"); //$NON-NLS-1$
		eElsePart.addContent(elseResult);
		res.addContent(eElsePart);
		return res;
	}

	@Override
	protected Element handleIteratorExp(IteratorExp<C, PM> callExp, Element sourceResult, List<Element> variableResults, Element bodyResult) {
		LOGGER.finer(""+callExp);
		Element res = new Element("IteratorExp"); //$NON-NLS-1$
		res.setAttribute("name", callExp.getName()); //$NON-NLS-1$
		addTypeInfo(res, callExp);
		addSourceInfo(res, sourceResult);

		Element itersE = new Element("iterators"); //$NON-NLS-1$
		for (org.jdom2.Element i : variableResults) {
			itersE.addContent(i);
		}
		res.addContent(itersE);

		Element bodyE = new Element("body"); //$NON-NLS-1$
		bodyE.addContent(bodyResult);
		res.addContent(bodyE);

		return res;
	}

	@Override
	protected Element handleAssociationClassCallExp(AssociationClassCallExp<C, P> callExp, Element sourceResult, List<Element> qualifierResults) {
		LOGGER.finer(""+callExp);
		C ac = callExp.getReferredAssociationClass();
		String name = getName(ac);
		if (callExp.isMarkedPre())
			name = name + "@pre"; //$NON-NLS-1$
		Element res = new Element("AssociationClassCallExp"); //$NON-NLS-1$
		res.setAttribute("name", name); //$NON-NLS-1$
		addTypeInfo(res, callExp);
		addSourceInfo(res, sourceResult);
		
		return res;
	}

	@Override
	protected Element handleCollectionItem(CollectionItem<C> item, Element itemResult) {
		LOGGER.finer(""+item);
		return itemResult;
	}

	@Override
	protected Element handleCollectionRange(CollectionRange<C> range, Element firstResult, Element lastResult) {
		LOGGER.finer(""+range);
		Element res = new Element("CollectionRange"); //$NON-NLS-1$
		res.addContent(firstResult);
		res.addContent(lastResult);
		return res;
	}

	@Override
	protected Element handleCollectionLiteralExp(CollectionLiteralExp<C> literalExp, List<Element> partResults) {
		LOGGER.finer(""+literalExp);
		Element res = new Element("CollectionLiteralExp"); //$NON-NLS-1$
		addTypeInfo(res, literalExp);
		for (org.jdom2.Element p : partResults) {
			res.addContent(p);
		}
		return res;
	}

	@Override
	protected Element handleIterateExp(IterateExp<C, PM> callExp, Element sourceResult, List<Element> variableResults, Element resultResult, Element bodyResult) {
		LOGGER.finer(""+callExp);

		Element res = new Element("IterateExp"); //$NON-NLS-1$
		res.setAttribute("name", getName(callExp)); //$NON-NLS-1$
		addTypeInfo(res, callExp);
		addSourceInfo(res, sourceResult);

		Element eItersPart = new Element("iterators"); //$NON-NLS-1$
		for (org.jdom2.Element eI : variableResults) {
			eItersPart.addContent(eI);
		}

		Element eResultPart = new Element("result"); //$NON-NLS-1$
		eResultPart.addContent(resultResult);

		Element eBodyPart = new Element("body"); //$NON-NLS-1$
		eBodyPart.addContent(bodyResult);

		res.addContent(eItersPart);
		res.addContent(eResultPart);
		res.addContent(eBodyPart);

		return res;
	}

	@Override
	protected Element handleLetExp(LetExp<C, PM> letExp, Element variableResult, Element inResult) {
		LOGGER.finer(""+letExp);
		Element res = new Element("LetExp"); //$NON-NLS-1$
		addTypeInfo(res, letExp);
		res.addContent(variableResult);
		Element eIn = new Element("in"); //$NON-NLS-1$
		if (inResult == null)
			inResult = new Element(XML_NULL_PLACEHOLDER);
		eIn.addContent(inResult);
		res.addContent(eIn);
		return res;
	}

	@Override
	protected Element handleMessageExp(MessageExp<C, COA, SSA> messageExp, Element targetResult, List<Element> argumentResults) {
		LOGGER.finer(""+messageExp);
		Element res = new Element("MessageExp"); //$NON-NLS-1$
		addTypeInfo(res, messageExp);
		Element eTarget = new Element("target"); //$NON-NLS-1$
		eTarget.addContent(targetResult);
		res.addContent(eTarget);

		res.setAttribute("msgType", (messageExp.getType() instanceof CollectionType) ? "^^" : "^"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

		if (messageExp.getCalledOperation() != null) {
			String operationName = getName(getOperation(messageExp.getCalledOperation()));
			res.setAttribute("calledOperation", operationName); //$NON-NLS-1$
		} else if (messageExp.getSentSignal() != null) {
			String signalName = getName(getSignal(messageExp.getSentSignal()));
			res.setAttribute("sentSignal", signalName); //$NON-NLS-1$
		}

		for (Element a : argumentResults) {
			res.addContent(a);
		}

		return res;
	}

	protected O getOperation(COA callOperationAction) {
		return (uml == null) ? null : uml.getOperation(callOperationAction);
	}

	protected C getSignal(SSA sendSignalAction) {
		return (uml == null) ? null : uml.getSignal(sendSignalAction);
	}

	@Override
	protected Element handleOperationCallExp(OperationCallExp<C, O> callExp, Element sourceResult, List<Element> argumentResults) {
		LOGGER.finer(""+callExp);
		O o = callExp.getReferredOperation();
		Element res = new Element("OperationCallExp"); //$NON-NLS-1$
		addTypeInfoToOperationCallExpElem(res, callExp);
		addSourceInfo(res, sourceResult);
		String opName = getName(callExp.getReferredOperation());
		if (callExp.isMarkedPre())
			opName = opName + "@pre"; //$NON-NLS-1$
		res.setAttribute("name", opName); //$NON-NLS-1$
		if (isInfix(callExp)) {
			res.setAttribute("is", "Infix"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			res.setAttribute("is", "Prefix"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		res.setAttribute("has", howManyArgs(o)); //$NON-NLS-1$
		if (isStdlibOperation(o))
			res.setAttribute("an", "StdLibOperation"); //$NON-NLS-1$ //$NON-NLS-2$

		
		
		
		
		/*
		 * for infix operations we leave out an explicit source tag to avoid
		 * clutter
		 */
		if (isInfix(callExp)) {
			for (org.jdom2.Element arg : argumentResults) {
				if (arg != null)
					res.addContent(arg);
			}
			return res;
		}

		if (argumentResults.size() == 0) {
			return res;
		}

		String argsEName = argumentResults.size() == 1 ? "arg" : "args"; //$NON-NLS-1$ //$NON-NLS-2$
		Element argsE = new Element(argsEName);
		for (org.jdom2.Element arg : argumentResults) {
			if (arg != null)
				argsE.addContent(arg);
		}
		res.addContent(argsE);
		return res;

	}

	private void addTypeInfoToOperationCallExpElem(Element res, OperationCallExp<C, O> exp) {
		C sourceType = exp.getSource().getType();
		C resultType = null;
		if (sourceType instanceof PredefinedType) {
			resultType = org.eclipse.ocl.util.TypeUtil.getResultType(_env, sourceType, exp.getReferredOperation());
		}
		String str = getName(resultType);
		res.setAttribute("resultType", str); //$NON-NLS-1$
	}

	@Override
	protected Element handlePropertyCallExp(PropertyCallExp<C, P> callExp, Element sourceResult, List<Element> qualifierResults) {
		LOGGER.finer(""+callExp);
		P a = callExp.getReferredProperty();
		String aName = getName(a);
		if (callExp.isMarkedPre()) {
			aName = aName.toString() + "@pre"; //$NON-NLS-1$
		}
		Element res = new Element("PropertyCallExp"); //$NON-NLS-1$
		res.setAttribute("name", aName); //$NON-NLS-1$
		addTypeInfo(res, callExp);
		addSourceInfo(res, sourceResult);
		return res;
	}

	@Override
	protected Element handleTupleLiteralExp(TupleLiteralExp<C, P> literalExp, List<Element> partResults) {
		LOGGER.finer(""+literalExp);
		Element res = new Element("TupleLiteralExp");//$NON-NLS-1$
		addTypeInfo(res, literalExp);
		for (Element i : partResults) {
			res.addContent(i);
		}
		return res;
	}

	@Override
	protected Element handleTupleLiteralPart(TupleLiteralPart<C, P> part, Element valueResult) {
		LOGGER.finer(""+part);
		String varName = getName(part);
		C type = part.getType();
		Element res = new Element("TupleLiteralPart"); //$NON-NLS-1$
		res.setAttribute("varName", varName);//$NON-NLS-1$
		res.setAttribute("type", getName(type));//$NON-NLS-1$
		if (valueResult != null) {
			res.addContent(valueResult);
		}
		return res;
	}

	@Override
	protected Element handleVariable(Variable<C, PM> variable, Element initResult) {
		LOGGER.finer(""+variable);
		Element res = new Element("Variable"); //$NON-NLS-1$
		res.setAttribute("name", variable.getName());
		addTypeInfo(res, variable);
		if (initResult != null) {
			Element eInitial = new Element("initExpression"); //$NON-NLS-1$
			eInitial.addContent(initResult);
			res.addContent(eInitial);
		} else {
			res.setAttribute("initExpression", "notProvided"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		String repParamName = getName(variable.getRepresentedParameter());
		if (repParamName != null) {
			res.setAttribute("representedParameter", repParamName); //$NON-NLS-1$
		}

		return res;
	}

	@Override
	protected Element handleConstraint(CT constraint, Element specificationResult) {
		LOGGER.finer(""+constraint);
		Element res = new Element("Constraint");
		res.addContent(specificationResult);
		return res;
	}

	@Override
	protected Element handleExpressionInOCL(ExpressionInOCL<C, PM> callExp, Element contextResult, Element resultResult, List<Element> parameterResults, Element bodyResult) {
		LOGGER.finer(""+callExp);
		Element res = new Element("ExpressionInOCL");

		Element cR = new Element("context");
		cR.addContent(contextResult);

		Element rR = new Element("result");
		if(resultResult != null )
			rR.addContent(resultResult);

		Element pR = new Element((parameterResults.size() > 0) ? "parameters" : "noParameters");
		if (parameterResults.size() > 0) {
			for (Element e : parameterResults) {
				pR.addContent(e);
			}
		}

		Element bR = new Element("body");
		bR.addContent(bodyResult);

		res.addContent(cR);
		res.addContent(rR);
		res.addContent(pR);
		res.addContent(bR);

		return res;

	}

	// UTIL

	private void addTypeInfo(org.jdom2.Element res, TypedElement<C> exp) {
		C ec = exp.getType();
		String tName = getName(ec);
		res.setAttribute("type", tName); //$NON-NLS-1$
	}

	private void addSourceInfo(Element res, Element sourceResult) {
		if (sourceResult == null) {
			sourceResult = new Element("NULL");
		}
		Element sourceE = new Element("source");
		sourceE.addContent(sourceResult);
		res.addContent(sourceE);
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

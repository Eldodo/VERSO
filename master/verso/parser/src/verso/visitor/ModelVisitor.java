package verso.visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import verso.model.ClassDef;
import verso.model.Element;
import verso.model.Entity;
import verso.model.InterfaceDef;
import verso.model.Method;
import verso.model.SystemDef;
import verso.model.metric.DecimalNumberMetric;
import verso.model.metric.IntervaleMetricDescriptor;





public class ModelVisitor extends ASTVisitor{

	private SystemDef sysDef;
	private Element currentClass;
	private CompilationUnit comp = null;
	private long currentMiliStart =0;
	
	public void setSystemDef(SystemDef sys)
	{
		this.sysDef = sys;
	}
	
	public boolean visit(CompilationUnit comp)
	{
		this.comp = comp;
		return true;
	}
	public boolean visit(TypeDeclaration node)
	{
		//this.currentMiliStart = System.currentTimeMillis();
		ITypeBinding classBinding = node.resolveBinding();
		if (classBinding == null)
			return false;
		//Skip InnerClasses
		if (classBinding.isNested())
			return false;
		Element classe;
		if (node.isInterface()) {
			classe = new InterfaceDef(classBinding.getQualifiedName());
		} else {
			classe = new ClassDef(classBinding.getQualifiedName());
		}
		CommentVisitor cv = new CommentVisitor(classe);
		for (Object comm : comp.getCommentList())
		{
			((ASTNode)comm).accept(cv);
		}
		MethodCallVisitor mcv = new MethodCallVisitor(classe);
		node.accept(mcv);
		ConditionVisitor condv = new ConditionVisitor(classe);
		node.accept(condv);
		//Package
		
		LineCreatorVisitor lcv = new LineCreatorVisitor();
		comp.accept(lcv);
		classe.setLines(lcv.getLines());
		
		DepthVisitor dv = new DepthVisitor(classe);
		comp.accept(dv);
		AppelMethodeVisitor amv = new AppelMethodeVisitor(classe);
		comp.accept(amv);
		CynthiaVisitor cv1 = new CynthiaVisitor(this.comp);
		cv1.setCurrentClass(classe);
		node.accept(cv1);
		LineTypeVisitor ltv = new LineTypeVisitor(classe);
		comp.accept(ltv);
		
		classe.setPackage(classBinding.getPackage().getName());
		//Ancestors
		AncestorsVisitor anv = new AncestorsVisitor();
		node.accept(anv);
		// Parent
		if (anv.getAncestors().size() > 0)
			classe.setParent(Entity.entities.get(anv.getAncestors().get(0)));//TODO If the named enetity is present in the Map must be tested.
		else
			classe.setParent(null);
		//Interfaces
		ITypeBinding[] interfaces = classBinding.getInterfaces();
		List<String> inters = new ArrayList<String>();
		for (int i = 0; i < interfaces.length; i++) {
			inters.add(interfaces[i].getQualifiedName());
		}
		classe.setInterfaces(inters);
		//Targets
		CBOVisitor cbovisitor = new CBOVisitor();
		node.accept(cbovisitor);
		classe.setTargets(cbovisitor.getCBO());
		
		//Metrics Calculation
		//WMC
		WMCVisitor wmcvisitor = new WMCVisitor();
		node.accept(wmcvisitor);
		
		classe.addMetric(new DecimalNumberMetric<Double>(new IntervaleMetricDescriptor<Double>("WMC",0.0,150.0),(double)wmcvisitor.getWMC()));
		//LCOM5
		LCOMVisitor lcomvisitor = new LCOMVisitor();
		node.accept(lcomvisitor);
		classe.addMetric(new DecimalNumberMetric<Double>(new IntervaleMetricDescriptor<Double>("LCOM5",0.0,2.0), lcomvisitor.getValue()));
		//DIT
		classe.addMetric(new DecimalNumberMetric<Double>(new IntervaleMetricDescriptor<Double>("DIT",0.0,10.0), (double)anv.getAncestors().size()));
		//CBO
		classe.addMetric(new DecimalNumberMetric<Double>(new IntervaleMetricDescriptor<Double>("CBO",0.0,25.0), (double)cbovisitor.getCBO().size()));
		
		//Placing it in the model
		classe.setFileLocation(this.comp.getJavaElement().getResource().getLocation().toFile());
		sysDef.addElement(classe);
		currentClass = classe;
		
		
		return true;
	}
	
	public void endVisit(TypeDeclaration node)
	{
		//System.out.println(node.getName() + " : " + (System.currentTimeMillis() - this.currentMiliStart));
	}
	
	public boolean visit(MethodDeclaration node)
	{
		int startPos = node.getStartPosition();
		int endPos = node.getStartPosition() + node.getLength();
		int firstLine = comp.getLineNumber(startPos);
		int lastLine = comp.getLineNumber(endPos);
		IMethodBinding metBinding = node.resolveBinding();
		if (metBinding == null)
			return false;
		if (!metBinding.getDeclaringClass().isEnum() && metBinding.getDeclaringClass().getQualifiedName().compareTo("") != 0)
		{
			String methodRepresentant = "";
			methodRepresentant = metBinding.getDeclaringClass().getQualifiedName() + "." + metBinding.getName() + "(";
			ITypeBinding[] params = metBinding.getParameterTypes();
			for (int i = 0; i < params.length;i++)
			{
				methodRepresentant += params[i].getQualifiedName();
				if (i < params.length -1)
					methodRepresentant += ",";
			}
			methodRepresentant += ")";
			Method methode = new Method(methodRepresentant);
			String sig = "";
			try{
				sig = ((IMethod)metBinding.getJavaElement()).getSignature();
			}catch(Exception e){System.out.println(e);}
			methode.setSignature(sig);
			// Return Type
			if (node.getReturnType2() != null)
				methode.setReturnType(metBinding.getReturnType().getQualifiedName());
			else
				methode.setReturnType("Constructor");
			
			//Overrides List
			InheritanceMethodVisitor imv = new InheritanceMethodVisitor();
			node.accept(imv);
			methode.setOveriddenFrom(imv.getValue());
			//Targets
			CouplingMethodVisitor cmv = new CouplingMethodVisitor();
			node.accept(cmv);
			methode.setTargets(cmv.getValue());
			
			//Metrics Calculation
			CynthiaVisitor cv1 = new CynthiaVisitor(this.comp);
			cv1.setCurrentMethod(methode);
			cv1.setCurrentClass(currentClass);
			node.accept(cv1);
			
			
			//McCabeComplexity for Methods
			MCMethodVisitor mcv = new MCMethodVisitor();
			node.accept(mcv);
			methode.addMetric(new DecimalNumberMetric<Double>(new IntervaleMetricDescriptor<Double>("MCMethod",0.0,100.0),(double) mcv.getMC()));
			//Cohesion for Methods
			CohesionMethodVisitor comv = new CohesionMethodVisitor();
			node.accept(comv);
			methode.addMetric(new DecimalNumberMetric<Double>(new IntervaleMetricDescriptor<Double>("CohMethod",0.0,1.0), comv.getValue()));
			//Coupling for methods
			methode.addMetric(new DecimalNumberMetric<Double>(new IntervaleMetricDescriptor<Double>("CouplingMethod",0.0,15.0), (double) cmv.getValue().size()));
			//DIT for methods (number of times it overrides)
			methode.addMetric(new DecimalNumberMetric<Double>(new IntervaleMetricDescriptor<Double>("DITMethod",0.0,5.0), (double) imv.getValue().size()));
			
			//Placing it in the model
			currentClass.addMethod(methode);
			sysDef.addMethod(methode);
			if (methode.getName().startsWith("."))
				System.out.println();
		}
		return true;//super.visit(node);
	}
	
}

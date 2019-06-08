package verso.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import verso.model.Element;
import verso.model.MethodInvocationText;

public class MethodCallVisitor extends ASTVisitor{
	
	Element classe = null;
	public MethodCallVisitor(Element classe)
	{
		this.classe = classe;
	}
	
	public boolean visit(MethodInvocation node)
	{
		classe.addMethodCall(new MethodInvocationText(node.getStartPosition(), node.getLength()));
		return false;
	}

}

package verso.visitor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class CouplingMethodVisitor extends ASTVisitor
{
	Set<String> set = new HashSet<String>(); 
	
	public boolean visit(MethodInvocation node) 
	{
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (methodBinding == null)
			return false;
		String methodRepresentant = "";
		methodRepresentant += methodBinding.getDeclaringClass().getQualifiedName() + ".";
		methodRepresentant += methodBinding.getName() + "(";
		ITypeBinding[] args = methodBinding.getParameterTypes();
		for (int i=0;i < args.length; i++)
		{
			methodRepresentant += args[i].getQualifiedName();
			if (i < args.length-1)
				methodRepresentant += ",";
		}
		methodRepresentant += ")";
		set.add(methodRepresentant);
		return super.visit(node);
	}
	
	public Set<String> getValue()
	{
		return set;
	}
}

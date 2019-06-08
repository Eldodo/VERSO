package verso.visitor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

public class CohesionMethodVisitor extends ASTVisitor{
	
	private double CohValue = 0;
	int variableNames = 0;
	Set<String> names = new HashSet<String>();
	int counterName = 0;
	ITypeBinding currentClass;
	public boolean visit(MethodDeclaration node)
	{
		IMethodBinding methodBinding = node.resolveBinding();
		if (methodBinding == null)
			return false;
		currentClass = methodBinding.getDeclaringClass();
		variableNames = methodBinding.getDeclaringClass().getDeclaredFields().length;
		return true;
	}
	
	public void endVisit(MethodDeclaration node)
	{
		//System.out.println(names);
		//System.out.println(variableNames);
		if (variableNames == 0)
			CohValue = 0.0;
		else
			CohValue = (double)names.size()/(double)variableNames;
	}
	
	public boolean visit(SimpleName node)
	{
		IVariableBinding ivb;
		if (node.resolveBinding() != null)
		{
			if (node.resolveBinding().getKind() ==3)
			{
				ivb = (IVariableBinding)node.resolveBinding();
				if (ivb.isField())
				{
					if (currentClass==ivb.getDeclaringClass())
					{
						names.add(node.getFullyQualifiedName());
					}
				}
			}
		}
		return false;
	}
	
	public double getValue()
	{
		return this.CohValue;
	}

}

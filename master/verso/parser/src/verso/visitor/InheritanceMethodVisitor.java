package verso.visitor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class InheritanceMethodVisitor extends ASTVisitor{

	private List<String> overridenFrom = new LinkedList<String>();
	public boolean visit(MethodDeclaration node)
	{
		IMethodBinding methodBinding = node.resolveBinding();
		if (methodBinding == null)
			return false;
		String methodRepresentant = "";
		methodRepresentant = methodBinding.getName();
		ITypeBinding[] parameters = methodBinding.getParameterTypes();
		for (int i =0; i < parameters.length; i++)
		{
			methodRepresentant += parameters[i].getName() + ",";
		}
		checkOverriden(node,methodRepresentant);
		return false;
	}
	
	private void checkOverriden(MethodDeclaration node, String methodRepresentant)
	{
		ITypeBinding superClasse;
		String currentMethod = "";
		IMethodBinding[] methods;
		ITypeBinding[] parameters;
		ITypeBinding[] interfaces = node.resolveBinding().getDeclaringClass().getInterfaces();
		for (int i =0; i < interfaces.length; i++)
		{
			methods = interfaces[i].getDeclaredMethods();
			for (int j =0; j < methods.length; j++)
			{
				currentMethod = methods[j].getName();
				parameters = methods[j].getParameterTypes();
				for (int k =0; k< parameters.length; k++)
				{
					currentMethod += parameters[k].getName() + ",";
				}
				if (currentMethod.compareTo(methodRepresentant)==0)
				{
					overridenFrom.add(methods[j].getDeclaringClass().getQualifiedName());
				}
			}
		}
		superClasse = node.resolveBinding().getDeclaringClass().getSuperclass();
		while (superClasse != null)
		{
			methods = superClasse.getDeclaredMethods();
			for (int j =0; j < methods.length; j++)
			{
				currentMethod = methods[j].getName();
				parameters = methods[j].getParameterTypes();
				for (int k =0; k< parameters.length; k++)
				{
					currentMethod += parameters[k].getName() + ",";
				}
				if (currentMethod.compareTo(methodRepresentant)==0)
				{
					overridenFrom.add(methods[j].getDeclaringClass().getQualifiedName());
				}
			}
			superClasse = superClasse.getSuperclass();
		}
	}
	
	public List<String> getValue()
	{
		return overridenFrom;
	}
}

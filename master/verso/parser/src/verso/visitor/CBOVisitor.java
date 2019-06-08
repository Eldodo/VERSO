package verso.visitor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class CBOVisitor extends ASTVisitor{
	HashSet<String> set = new HashSet<String>(); 
	private String className = "";
	public boolean visit (TypeDeclaration node)
	{
		ITypeBinding classBinding = node.resolveBinding();
		if (classBinding == null)
			return false;
		className = classBinding.getQualifiedName();
		set.add(className);
		return true;
	}
	
	public boolean visit(MethodInvocation node) 
	{
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (methodBinding == null)
			return false;
		if (!methodBinding.getDeclaringClass().isPrimitive())
		{
			ITypeBinding className = methodBinding.getDeclaringClass();
			if (className.isParameterizedType())
				set.add(className.getQualifiedName().substring(0, className.getQualifiedName().indexOf('<')));
			else
				set.add(className.getQualifiedName());
		}
		return false;
	}
	
	public boolean visit(ConstructorInvocation node)
	{
		ITypeBinding className = node.resolveConstructorBinding().getDeclaringClass();
		if (className == null)
			return false;
		if (className.isParameterizedType())
			set.add(className.getQualifiedName().substring(0, className.getQualifiedName().indexOf('<')));
		else
			set.add(className.getQualifiedName());
		return false;
	}
	
	public Set<String> getCBO()
	{
		set.remove(this.className);
		return set;
	}

}

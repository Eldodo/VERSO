package verso.visitor;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class LCOMVisitor extends ASTVisitor{

	double LCOMValue =0;
	Hashtable<String,Integer> list = new Hashtable<String,Integer>();
	Hashtable<String,Boolean> currList = new Hashtable<String,Boolean>();
	int methodCounter = 0;
	ITypeBinding currentClass;
	
	public boolean visit(TypeDeclaration node)
	{
		currentClass = node.resolveBinding();
		if (currentClass == null)
			return false;
		IVariableBinding[] ivbs = currentClass.getDeclaredFields();
		for (int i =0; i < ivbs.length; i++)
		{
			list.put(ivbs[i].getName(), 0);
		}
		
		return true;
	}
	
	public void endVisit(TypeDeclaration node)
	{
		//System.out.println("Nb de méthodes : " + methodCounter);
		if (methodCounter ==1 || list.keySet().size() ==0)
		{
			LCOMValue =0;
			return;
		}
		//System.out.println(list);
		int somme =0;
		for (int i : list.values())
		{
			somme += i - methodCounter;
		}
		//System.out.println("Somme : " + somme);
		//System.out.println("Nombre d'attributs : " + list.keySet().size());
		LCOMValue = (double)somme / (double)list.keySet().size();
		LCOMValue = LCOMValue / (1 - methodCounter);
	}
	
	public boolean visit(MethodDeclaration node)
	{
		methodCounter++;
		NameVisitor nv = new NameVisitor();
		node.accept(nv);
		//System.out.println(nv.getNames());
		
	
		for (String name : nv.getNames())
		{
			if (list.containsKey(name))
			{
				list.put(name, list.get(name)+1);
			}
		}
		return false;
	}
	
	public double getValue()
	{
		return this.LCOMValue;
	}
	
	public class NameVisitor extends ASTVisitor
	{
		Set<String> nameList = new HashSet<String>();
		
		public Set<String> getNames()
		{
			return this.nameList;
		}
		
		public boolean visit(SimpleName node)
		{
			IVariableBinding ivb;
			if (node.resolveBinding() != null)
			{
				if (!node.isDeclaration() && node.resolveBinding().getKind() ==3)
				{
					ivb = (IVariableBinding)node.resolveBinding();
					if (ivb.isField() && ivb.getDeclaringClass() == currentClass)
						nameList.add(node.getFullyQualifiedName());
				}
			}
			return false;
		}
	}
}

package verso.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import verso.model.ConditionStatement;
import verso.model.Element;

public class ConditionVisitor extends ASTVisitor{

	private Element classe = null;
	public ConditionVisitor(Element classe)
	{
		this.classe = classe;
	}
	public boolean visit(IfStatement node)
	{
		classe.addCondition(new ConditionStatement(node.getStartPosition(), node.getLength()));
		return true;
	}
	
	public boolean visit(ForStatement node)
	{
		classe.addCondition(new ConditionStatement(node.getStartPosition(), node.getLength()));
		return true;
	}
	
	public boolean visit(WhileStatement node)
	{
		classe.addCondition(new ConditionStatement(node.getStartPosition(), node.getLength()));
		return true;
	}
	
	public boolean visit(SwitchStatement node)
	{
		classe.addCondition(new ConditionStatement(node.getStartPosition(), node.getLength()));
		return true;
	}
	
	public boolean visit(EnhancedForStatement node)
	{
		classe.addCondition(new ConditionStatement(node.getStartPosition(), node.getLength()));
		return true;
	}
	
	public boolean visit(DoStatement node)
	{
		classe.addCondition(new ConditionStatement(node.getStartPosition(), node.getLength()));
		return true;
	}
}

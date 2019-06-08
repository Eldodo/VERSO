package verso.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.WhileStatement;

public class MCMethodVisitor extends ASTVisitor{
	
	private int mc =1;
	
	public boolean visit(DoStatement node)
	{
		mc++;
		return super.visit(node);
	}
	
	public boolean visit(EnhancedForStatement node)
	{
		mc++;
		return super.visit(node);
	}

	public boolean visit(ForStatement node)
	{
		mc++;
		return super.visit(node);
	}
	
	public boolean visit(IfStatement node)
	{
		mc++;
		return super.visit(node);
	}

	
	public boolean visit(SwitchCase node) 
	{
		mc++;
		return super.visit(node);
	}
	
	public boolean visit(WhileStatement node) 
	{
		mc++;
		return super.visit(node);
	}
	public int getMC()
	{
		return this.mc;
	}

	
}

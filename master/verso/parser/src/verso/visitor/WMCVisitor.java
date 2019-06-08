package verso.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.WhileStatement;


public class WMCVisitor extends ASTVisitor{
	
	private int wmc =0;
	
	public boolean visit(DoStatement node)
	{
		wmc++;
		return super.visit(node);
	}
	
	public boolean visit(EnhancedForStatement node)
	{
		wmc++;
		return super.visit(node);
	}

	public boolean visit(ForStatement node)
	{
		wmc++;
		return super.visit(node);
	}
	
	public boolean visit(IfStatement node)
	{
		wmc++;
		return super.visit(node);
	}

	// C'est le plus 1 du d+1
	public boolean visit(MethodDeclaration node)
	{
		wmc++;
		return super.visit(node);
	}
	public boolean visit(SwitchCase node) 
	{
		wmc++;
		return super.visit(node);
	}
	
	public boolean visit(WhileStatement node) 
	{
		wmc++;
		return super.visit(node);
	}
	public int getWMC()
	{
		return this.wmc;
	}

	
}

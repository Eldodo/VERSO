package verso.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class CompilationUnitExtractor extends ASTVisitor{
	
	private CompilationUnit comp = null;
	
	public boolean visit(CompilationUnit comp)
	{
		this.comp = comp;
		return true;
	}
	
	public CompilationUnit getCompilationUnit()
	{
		return comp;
	}

}

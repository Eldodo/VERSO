package verso.parser;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import verso.model.SystemDef;
import verso.visitor.ModelVisitor;

public class ASTRequestorVerso extends ASTRequestor{

	private SystemDef sysdef = null;
	
	public ASTRequestorVerso(SystemDef sysdef)
	{
		this.sysdef = sysdef;
	}
	public void acceptAST(ICompilationUnit source, CompilationUnit ast)
	{
		ModelVisitor visitor = new ModelVisitor();
		visitor.setSystemDef(this.sysdef);
		ast.accept(visitor);
	}
}

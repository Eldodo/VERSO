package verso.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LineComment;

import verso.model.Element;


public class CommentVisitor extends ASTVisitor{

	CompilationUnit cu = null;
	Element classe = null;
	
	public CommentVisitor(Element classe)
	{
		this.classe = classe;
	}
	public boolean visitComment(Comment node)
	{
		classe.addComment(new verso.model.Comment(node.getStartPosition(),node.getLength()));
		return false;
	}
	
	public boolean visit(BlockComment node)
	{
		return visitComment(node);
	}
	
	public boolean visit(LineComment node)
	{
		return visitComment(node);
	}
}

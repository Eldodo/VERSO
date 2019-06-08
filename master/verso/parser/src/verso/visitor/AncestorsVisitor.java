package verso.visitor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class AncestorsVisitor extends ASTVisitor {

	List<String> ancestors = new LinkedList<String>();

	public boolean visit(TypeDeclaration node) {
		ITypeBinding classBinding = node.resolveBinding();
		if (classBinding == null)
			return false;
		gatherAncestors(classBinding.getSuperclass());
		return false;
	}

	private void gatherAncestors(ITypeBinding node) {
		if (node == null)
			return;
		else {
			ancestors.add(node.getQualifiedName());
			gatherAncestors(node.getSuperclass());
		}
	}

	public List<String> getAncestors() {
		return ancestors;
	}

}

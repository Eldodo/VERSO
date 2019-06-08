package verso.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import verso.model.Element;
import verso.model.Line;
import verso.model.metric.IntegralNumberMetric;
import verso.model.metric.IntervaleMetricDescriptor;

public class DepthVisitor extends ASTVisitor{

	int currentDepth = 0;
	CompilationUnit currentCompUnit = null ;
	int[] lineDepths = null;
	int nbLigne = 0;
	int ligne = 0;
	int startPos = 0;
	int endPos = 0;
	Element classe = null;
	
	public DepthVisitor(Element classe)
	{
		this.classe = classe;
	}
	
	public boolean visit(CompilationUnit node)
	{
		this.currentCompUnit = node;
		//calcul pour la grandeur du tableau
		int endPos = node.getLength()-1 + node.getStartPosition();
		nbLigne = currentCompUnit.getLineNumber(endPos);
		lineDepths = new int[nbLigne];
		return true;
	}
	
	public void endVisit(CompilationUnit node)
	{
		for (int i =0; i < classe.getLines().size(); i++)
		{
			Line l = classe.getLine(i);
			l.addMetric(new IntegralNumberMetric<Integer>(new IntervaleMetricDescriptor<Integer>("LineDepth",0,10), lineDepths[i]));
		}
	}
	
	public boolean visit(IfStatement node)
	{
		startPos = node.getStartPosition();
		endPos = node.getLength()-1 + node.getStartPosition();
		ajout();
		return true;
	}
	
	public void endVisit(IfStatement node)
	{
		currentDepth--;
	}
	
	public boolean visit(WhileStatement node)
	{
		startPos = node.getStartPosition();
		endPos = node.getLength()-1 + node.getStartPosition();
		ajout();
		return true;
	}
	public void endVisit(WhileStatement node)
	{
		currentDepth--;
	}
	public boolean visit(ForStatement node)
	{
		startPos = node.getStartPosition();
		endPos = node.getLength()-1 + node.getStartPosition();
		ajout();
		return true;
	}
	public void endVisit(ForStatement node)
	{
		currentDepth--;
	}
	public boolean visit (DoStatement node)
	{
		startPos = node.getStartPosition();
		endPos = node.getLength()-1 + node.getStartPosition();
		ajout();
		return true;
	}
	public void endVisit (DoStatement node)
	{
		currentDepth--;
	}
	public boolean visit (SwitchStatement node)
	{
		startPos = node.getStartPosition();
		endPos = node.getLength()-1 + node.getStartPosition();
		ajout();
		return true;
	}
	public void endVisit (SwitchStatement node)
	{
		currentDepth--;
	}
	
	public void ajout ()
	{
		currentDepth++;
		ligne = currentCompUnit.getLineNumber(startPos);
		while (ligne<=currentCompUnit.getLineNumber(endPos))
		{
			if (currentDepth>lineDepths[ligne-1])
			{
				lineDepths[ligne-1] = currentDepth;
			}
			ligne++;
		}
	}
}

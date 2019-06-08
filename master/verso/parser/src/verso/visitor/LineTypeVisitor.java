package verso.visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import verso.model.Element;
import verso.model.Line;
import verso.model.metric.OrdinaleMetric;
import verso.model.metric.OrdinaleMetricDescriptor;

public class LineTypeVisitor extends ASTVisitor{
	
	CompilationUnit currentCompUnit = null;
	public List<Integer> lstLineType = new ArrayList<Integer>();
	public List<String> lstLineTypeString = new ArrayList<String>();
	Element classe = null;
	int position = 0;
	String nomMetric = "LineType";
	
	private final int numTryCatchStatement = 1;
	
	private final int numClassDeclaration = 2;
	private final int numMethodDeclaration = 2;
	
	private final int numFieldDeclaration = 3;

	
	private final int numPostfixExpression = 4;
	private final int numAssignment = 4;
	
	private final int numConditionBoucleDeclaration = 5;
	
	private final int numContinueStatement = 6;
	private final int numBreakStatement = 6;
	
	private final int numReturnStatement = 7;

	private final int numClassInstanceCreation = 8;

	private final int numMethodInvocation = 9;
	
	private final String TryCatchStatement = "Try/Catch Statement";
	
	private final String ClassDeclaration = "Declaration (Class or Method)";
	private final String MethodDeclaration = "Declaration (Class or Method)";
	
	private final String FieldDeclaration = "Field Declaration";
	
	private final String PostfixExpression = "Assignement, Increment or Decrement";
	private final String Assignment = "Assignement, Increment or Decrement";
	
	private final String ConditionBoucleDeclaration = "Condition or Loop Declaration";
	
	private final String ContinueStatement = "Continue/Break Statement";
	private final String BreakStatement = "Continue/Break Statement";
	
	private final String ReturnStatement = "Return Statement";

	private final String ClassInstanceCreation = "Class Instance Creation";

	private final String MethodInvocation = "Method Invocation";
	
	String[] tabOrdre = {"Nothing","Try/Catch Statement","Declaration (Class or Method)","Field Declaration","Assignement, Increment or Decrement","Condition or Loop Declaration","Continue/Break Statement","Return Statement","Class Instance Creation","Method Invocation"};
	
	public LineTypeVisitor(){}
	
	public LineTypeVisitor(Element classe)
	{
		this.classe = classe;
		for (int i = 0; i<classe.getLines().size(); i++)
		{
			lstLineType.add(0);
			lstLineTypeString.add("Nothing");
		}
	}
	
	
	public boolean visit(CompilationUnit node)
	{
		this.currentCompUnit = node;
		return true;
	}
	
	public void endVisit(CompilationUnit node)
	{
//		System.out.println("Résultats des types de ligne pour : " + currentCompUnit.getTypeRoot().getElementName());
//		System.out.println("***************************************");
//		for(int i = 0; i<lstLineType.size();i++)
//		{
//			System.out.print(lstLineType.get(i) + " ");
//		}
//		System.out.println("\n***************************************\n");
		List<String> lstOrdre = new ArrayList<String>();
		
		OrdinaleMetricDescriptor omd = new OrdinaleMetricDescriptor(nomMetric,tabOrdre);
		for (int i =0; i < classe.getLines().size(); i++)
		{
			Line l = classe.getLine(i);
			//l.addMetric(new NumberMetric<Integer>(new IntervaleMetricDescriptor<Integer>("LineType",0,7), lstLineType.get(i)));
			l.addMetric(new OrdinaleMetric(omd, lstLineTypeString.get(i)));
			// System.out.println("Ligne " + (i + 1) + " : "+ lstLineTypeInt.get(i));
		}
		/*
		for(int i=0;i<tabOrdre.length;i++)
		{
			Line l = classe.getLine(i);
			l.addMetric(new LegendMetric(new LegendMetricDescriptor(nomMetric,tabOrdre,tabOrdre.length),tabOrdre[i]));
		}
		*/
	}
	
	//Déclaration de la classe
	public boolean visit(TypeDeclaration node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numClassDeclaration)
		{
			lstLineType.set(position, numClassDeclaration);
			lstLineTypeString.set(position,ClassDeclaration);
		}
		return true;
	}
	
	// Conditions et boucles
	public boolean visit(IfStatement node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1; 
		conditionEtBoucle();
		return true;
	}
	
	public boolean visit(ForStatement node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		conditionEtBoucle();
		return true;
	}
	
	public boolean visit(WhileStatement node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		conditionEtBoucle();
		return true;
	}
	public boolean visit(DoStatement node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		conditionEtBoucle();
		return true;
	}
	public boolean visit(SwitchStatement node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		conditionEtBoucle();
		return true;
	}
	
	public void conditionEtBoucle ()
	{
		if (lstLineType.get(position)< numConditionBoucleDeclaration)
		{
			lstLineType.set(position, numConditionBoucleDeclaration);
			lstLineTypeString.set(position,ConditionBoucleDeclaration);
		}
	}
	
	//Return Statement
	public boolean visit(ReturnStatement node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numReturnStatement)
		{
			lstLineType.set(position, numReturnStatement);
			lstLineTypeString.set(position, ReturnStatement);
		}
		return true;
	}
	
	//Déclaration de variable
	public boolean visit(FieldDeclaration node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numFieldDeclaration)
		{
			lstLineType.set(position, numFieldDeclaration);
			lstLineTypeString.set(position,FieldDeclaration);
		}
		return true;
	}
	public boolean visit(VariableDeclarationStatement node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numFieldDeclaration)
		{
			lstLineType.set(position, numFieldDeclaration);
			lstLineTypeString.set(position, FieldDeclaration);
		}
		return true;
	}
	
	//Déclaration de méthode
	public boolean visit(MethodDeclaration node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numMethodDeclaration)
		{
			lstLineType.set(position, numMethodDeclaration);
			lstLineTypeString.set(position, MethodDeclaration);
		}
		return true;
	}
	
	//Invocation de méthode
	public boolean visit(MethodInvocation node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numMethodInvocation)
		{
			lstLineType.set(position, numMethodInvocation);
			lstLineTypeString.set(position, MethodInvocation);
		}
		return true;
	}
	
	//Déclaration d'instance de classe
	public boolean visit(ClassInstanceCreation node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numClassInstanceCreation)
		{
			lstLineType.set(position, numClassInstanceCreation);
			lstLineTypeString.set(position,ClassInstanceCreation);
		}
		return true;
	}
	
	//Assignement, Incrémentation et Décrémentation
	public boolean visit(Assignment node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numAssignment)
		{
			lstLineType.set(position, numAssignment);
			lstLineTypeString.set(position,Assignment);
		}
		return true;
	}
	public boolean visit(PostfixExpression node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numPostfixExpression)
		{
			lstLineType.set(position, numPostfixExpression);
			lstLineTypeString.set(position,PostfixExpression);
		}
		return true;
	}
	
	//Try/Catch Statement
	public boolean visit(TryStatement node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numTryCatchStatement)
		{
			lstLineType.set(position, numTryCatchStatement);
			lstLineTypeString.set(position, TryCatchStatement);
		}
		return true;
	}
	public boolean visit(CatchClause node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numTryCatchStatement)
		{
			lstLineType.set(position, numTryCatchStatement);
			lstLineTypeString.set(position, TryCatchStatement);
		}
		return true;
	}
	
	//Continue/Break Statement
	public boolean visit(ContinueStatement node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numContinueStatement)
		{
			lstLineType.set(position, numContinueStatement);
			lstLineTypeString.set(position,ContinueStatement);
		}
		return true;
	}
	
	public boolean visit(BreakStatement node)
	{
		position = currentCompUnit.getLineNumber(node.getStartPosition())-1;
		if (lstLineType.get(position)< numBreakStatement)
		{
			lstLineType.set(position, numBreakStatement);
			lstLineTypeString.set(position, BreakStatement);
		}
		return true;
	}
	
	public int getLineTypeElement(int i)
	{
		return lstLineType.get(i);
	}
	
	public int getListSize()
	{
		return lstLineType.size();
	}

}

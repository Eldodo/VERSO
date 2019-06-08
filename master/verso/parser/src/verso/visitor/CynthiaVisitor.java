package verso.visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import verso.model.Element;
import verso.model.Method;
import verso.model.SystemManager;
import verso.model.metric.IntegralNumberMetric;
import verso.model.metric.IntervaleMetricDescriptor;

public class CynthiaVisitor extends ASTVisitor{
	
	CompilationUnit currentCompUnit = null;
	int grandeurMethode = 0;
	List<Integer> lstMethodeLigne = new ArrayList<Integer>();
	List<String> lstMethodes = new ArrayList<String>();
	List<Integer> lstNbLigne = new ArrayList<Integer>();
	List<Integer> lstNbCaractere = new ArrayList<Integer>();
	List<Method> lstMet = new ArrayList<Method>();
	Method currentMethod = null;
	Element currentClasse = null;
	
	public CynthiaVisitor(CompilationUnit comp)
	{
		this.currentCompUnit = comp;
	}
	

	public boolean visit(CompilationUnit node)

	{
		currentCompUnit = node;
		return true;	
	}

	public void setCurrentMethod(Method m)
	{
		this.currentMethod = m;
	}
	
	public void setCurrentClass(Element c)
	{
		this.currentClasse = c;
	}
	public void endVisit(CompilationUnit node)
	{
		/*
		System.out.println("***********************");
		System.out.println("Output pour CynthiaVisitor");
		System.out.println("***********************");
		
		System.out.println("Nombre de lignes pour : " + node.getTypeRoot().getElementName() + " : " + nbLigne);
		System.out.println("Nombre de caractères pour : " + node.getTypeRoot().getElementName() + " : " + nbCaractere);
		
		for (int i = 0; i < lstMethodes.size(); i++)
		{
			System.out.println(lstMethodes.get(i));
			System.out.println("Emplacement : " + lstMethodeLigne.get(i));
			System.out.println(lstNbLigne.get(i));
			System.out.println(lstNbCaractere.get(i));
		}
				
		System.out.println("***********************");
		System.out.println("Fin du Output pour CynthiaVisitor");
		System.out.println("***********************");
		
		*/
		
	}
	
	public boolean visit(TypeDeclaration node)//Nb caractères (compte les "Enter" et les tabulations) et nb de lignes
	{
		int startPos = node.getStartPosition();
		int endPos = node.getLength()-1 + node.getStartPosition();
		int nbLigne = currentCompUnit.getLineNumber(endPos) - currentCompUnit.getLineNumber(startPos)+ 1;
		int endCar = node.getLength();
		int nbCaractere = endCar;
		this.currentClasse.addMetric(new IntegralNumberMetric<Integer>(new IntervaleMetricDescriptor<Integer>("LOC",0,1000), nbLigne));
		this.currentClasse.addMetric(new IntegralNumberMetric<Integer>(new IntervaleMetricDescriptor<Integer>("NbCaractere",0,10000), nbCaractere));
		return false;
	}
	
	public boolean visit(MethodDeclaration node)//Nb de caractères et de lignes dans chaque méthode
	{
		int startPos = node.getStartPosition();
		int endPos = node.getLength()-1 + node.getStartPosition();
		
		int lineStart = currentCompUnit.getLineNumber(startPos);
		int lineEnd = currentCompUnit.getLineNumber(endPos);
		int nbLigneM = lineEnd - lineStart + 1;
		for (int i = lineStart; i<lineEnd + 1; i++)
		{
			currentMethod.addLine(currentClasse.getLine(i-1));
		}
		//System.out.println(currentMethod.getName() + " : " + lineStart + " à " + lineEnd + " = " +  currentMethod.getLines().size() + " lignes enregistrées");
		int endCar = node.getLength();
		int nbCaractereM = endCar;
		this.currentMethod.addMetric(new IntegralNumberMetric<Integer>(new IntervaleMetricDescriptor<Integer>("LOCMethod",0,200), nbLigneM));
		this.currentMethod.addMetric(new IntegralNumberMetric<Integer>(new IntervaleMetricDescriptor<Integer>("NbCaractereMethod",0,2000), nbCaractereM));
		return false;
	}
	
}

package verso.visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import verso.model.Element;
import verso.model.Line;
import verso.model.metric.OrdinaleMetric;
import verso.model.metric.OrdinaleMetricDescriptor;

public class AppelMethodeVisitor extends ASTVisitor{
	
	CompilationUnit currentCompUnit = null;
	int ligne = 0;
	ITypeBinding currentClasse;
	IPackageBinding currentPackage;
	List<String> lstClasseAncetre;
	List<Integer> lstLignes = new ArrayList<Integer>();
	List<String> lstNomMethode = new ArrayList<String>();
	List<String> lstProvenance = new ArrayList<String>();
	List<Integer> lstProvInt = new ArrayList<Integer>();
	String nomMethode;
	String nomDeclaringPackage;
	String source = null;
	IMethodBinding met;
	Element classe = null;
	String[] tabOrdre = {"No Method Call","Same Class","Ancestor Class, Same Package","Different Class","Ancestor Class, Different Package","Different Package","Different Library","External Library"};
	
	public AppelMethodeVisitor(Element classe)
	{
		this.classe = classe;
		for (int i = 0; i<classe.getLines().size(); i++)
		{
			lstProvInt.add(0);
			lstProvenance.add("No Method Call");
		}
	}
	
	public boolean visit (CompilationUnit node)
	{
		this.currentCompUnit = node;
		return true;
	}
	
	public void endVisit(CompilationUnit node)
	{
//		System.out.println("Résultats des méthodes appelées pour : " + currentCompUnit.getTypeRoot().getElementName());
//		System.out.println("*************************************************************");
//		
//		for(int i = 0;i<lstLignes.size();i++)
//		{
//			System.out.println("Ligne " + lstLignes.get(i) + " : " + lstNomMethode.get(i) + " : Provenance : " + lstProvenance.get(i));
//		}
//				
//		System.out.println("*************************************************************\n");
//		
		OrdinaleMetricDescriptor omd = new OrdinaleMetricDescriptor("Provenance", tabOrdre);
		for (int i =0; i < classe.getLines().size(); i++)
		{
			Line l = classe.getLine(i);
			l.addMetric(new OrdinaleMetric<String>(omd, lstProvenance.get(i)));
			//l.addMetric(new NumberMetric<Integer>(new IntervaleMetricDescriptor<Integer>("Provenance",0,7), lstProvInt.get(i)));
			//System.out.println(lstProvInt.get(i));
		}
		
	}
	public boolean visit(MethodInvocation node)
	{
		met = node.resolveMethodBinding();
		if (met == null)
			return true;
		ITypeBinding declaringClasse = met.getDeclaringClass();
		nomMethode = met.getName();
		lstNomMethode.add(nomMethode);
		
		IPackageBinding declaringPackage = met.getDeclaringClass().getPackage();
		nomDeclaringPackage = declaringPackage.toString();
		
		int startPos = node.getStartPosition();
		ligne = currentCompUnit.getLineNumber(startPos);
		lstLignes.add(ligne);
		provenance();
		return true;
	}

	public boolean visit(TypeDeclaration node)
	{
		currentClasse = node.resolveBinding();
		return true;
	}
	
	private void provenance()
	{
		String provenance = "Different Package";
		int provInt = 5;
		if (met.getDeclaringClass().getJavaElement().getResource()==null)
		{
			provenance = "External Library";
			provInt = 7;
		}
		if (met.getDeclaringClass().getJavaElement().getResource()!=null)
		{
			source = met.getDeclaringClass().getJavaElement().getResource().getFullPath().toString();
			if (!source.substring(source.length()-4).equals("java"))
			{
				provenance = "Different Library";
				provInt = 6;
			}
		}
		if (currentClasse.getQualifiedName().equals(met.getDeclaringClass().getQualifiedName()))
		{
			provenance = "Same Class";
			provInt = 1;
		}
		if (currentClasse.getPackage().toString().equals(nomDeclaringPackage))
		{
			provenance = "Different Class";
			provInt = 3;
		}
		if (!currentClasse.getQualifiedName().equals(met.getDeclaringClass().getQualifiedName()))
		{
			ITypeBinding nouvelleClasse = currentClasse;
			while (!nouvelleClasse.getQualifiedName().equals(met.getDeclaringClass().getQualifiedName()) && nouvelleClasse.getSuperclass() !=null)
			{
				nouvelleClasse = nouvelleClasse.getSuperclass();
			}
			if (nouvelleClasse.getQualifiedName().equals(met.getDeclaringClass().getQualifiedName()))
				if (currentClasse.getPackage().toString().equals(nomDeclaringPackage))
				{
					provenance = "Ancestor Class, Same Package";
					provInt = 2;
				}
				else
				{
					provenance = "Ancestor Class, Different Package";
					provInt = 4;
				}
		}
		
		lstProvenance.set(ligne - 1, provenance);
		lstProvInt.set(ligne - 1, provInt);
	}
}

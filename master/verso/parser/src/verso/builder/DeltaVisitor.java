package verso.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import verso.model.SystemDef;
import verso.model.SystemManager;
import verso.util.TypeFinder;
import verso.visitor.ModelVisitor;

public class DeltaVisitor implements IResourceDeltaVisitor{

	
	public boolean visit(IResourceDelta delta) throws CoreException 
	{
		//System.out.println("Visiting ..." + delta.getResource().getName());
		if (delta.getResource().getType() == IResource.FILE)
			if (delta.getResource().getFileExtension().compareTo("java") ==0)
			{
				if (delta.getKind() == IResourceDelta.REMOVED)
					remove((IFile)delta.getResource());
				else
					visit((IFile)delta.getResource());
				
			}
		return true;
	}
	
	private void visit(IFile file)
	{
		
		System.out.println(file.getName());
		System.out.println(file.getProjectRelativePath());
		
		ICompilationUnit comp = TypeFinder.findCompilationUnitFromFile(file);
		visitTree(comp);
		//Implanter les informations sur le svn ici en visitant seulement les fichiers intéressants ... peut-être le faire dans le méthode visit Tree
		SystemDef sys = (SystemManager.getSystem(file.getProject().getName()));
		String pacName = TypeFinder.findFilePathfromFile(file);
		if (pacName.compareTo("") == 0)
			pacName = "default";
		sys.setDirtyPackages(pacName, SystemDef.QUALITYMETRIC);
	}
	
	private void remove(IFile file)
	{
		System.out.println("removing : " + file.getName());
		SystemDef sys = SystemManager.getSystem(file.getProject().getName());
		//Should remove file from system at this point
		//Use technique below to identify full path name.
		String pacFrag =TypeFinder.findFilePathfromFile(file);
		String fileName = pacFrag + "." + file.getName();
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		sys.removeElement(fileName);
		//sys.computePackageMetricsQual(pacFrag);
	}

	
	private void visitTree(ICompilationUnit comp)
	{
		ModelVisitor mv = new ModelVisitor();
		SystemDef sys = SystemManager.getSystem(comp.getJavaProject().getProject().getName());
		if (sys == null)return;
		mv.setSystemDef(sys);
		try{
		 @SuppressWarnings("deprecation")
		ASTParser parser = ASTParser.newParser(AST.JLS3);
	     parser.setProject(comp.getJavaProject());
	     parser.setResolveBindings(true);
	     parser.setBindingsRecovery(true);
         comp.open(null);
         parser.setSource(comp);
         ASTNode node = parser.createAST(null);
       	 //System.out.println("On fait un nouveau accept ici!");
         node.accept(mv);
		}catch(Exception e){System.out.println(e);}
		String elemName = "";
		try{
			elemName = comp.getTypes()[0].getFullyQualifiedName();
		}catch(Exception ex){System.out.println(ex);}
		if (!elemName.contains("."))
		{
			elemName = "default." + elemName;
		}
		/*
		//modifier pour appeller l'autre
		ISVNClientAdapter svnc = null;
		Element e = sys.getElement(elemName);
		long currentRev = 0;
		long nextRev = 0;
		try {
			svnc = JhlClientAdapterFactory
					.createSVNClient(JhlClientAdapterFactory
							.getPreferredSVNClientType());
			
			ISVNInfo inf = svnc.getInfo(comp.getResource().getProject().getLocation().toFile());
			currentRev = inf.getCopyRev().getNumber();
			nextRev = inf.getRevision().getNumber();
		} catch (Exception ex) {
			System.out.println(ex);
		}
		if (nextRev > currentRev) {
			SVNMessageLogger.logMessages(currentRev, nextRev, svnc,e, sys);
		}
		sys.setRevision(nextRev , comp.getParent().getResource().getLocation().toFile());
		*/
	}
	
}

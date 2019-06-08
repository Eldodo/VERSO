package verso.parser;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import verso.model.SystemDef;
import verso.model.SystemManager;
import verso.visitor.ModelVisitor;

public class Builder {

	private SystemDef sysdef;
	private IJavaProject project = null;

	private IWorkspaceRoot workspace = null;

	public Builder(IWorkspaceRoot workspace) {
		this.workspace = workspace;
	}

	public void buildVersion(long version, SystemDef sys, IJavaProject project) {
		System.out.println(project.getProject().getName());
		sysdef = new SystemDef(project.getProject().getName());
		sys.addSystemVersion(version, sysdef);
		// SystemManager.addSystem(sysdef);
		try {
			IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
			System.out.println(roots.length);
			for (IPackageFragmentRoot root : roots) {
				buildPackage(root);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Fini le calcul des métriques pour cette version");
	}

	public void build(IJavaProject project) {
		this.project = project;
		try{
			project.open(null);
		}catch(Exception e){e.printStackTrace();}
		
		System.out.println(project.getProject().getName());
		sysdef = new SystemDef(project.getProject().getName());
		SystemManager.addSystem(sysdef);
		try {
			IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
			System.out.println(roots.length);
			for (IPackageFragmentRoot root : roots) {
				buildPackage2(root);//Changer ici, enelver le 2 pour revenir comme avant
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void buildPackage(IPackageFragmentRoot root) {
		IJavaElement[] elems = null;
		try {
			if (root.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					elems = root.getChildren();
					for (IJavaElement elem : elems) {
						// System.out.println(elem);

						parsePackage(elem);

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void buildPackage2(IPackageFragmentRoot root)
	{
		ArrayList<ICompilationUnit> compList = new ArrayList<ICompilationUnit>();
		IJavaElement[] elems = null;
		try {
			if (root.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					elems = root.getChildren();
					for (IJavaElement elem : elems) {
						// System.out.println(elem);
						compList.addAll(Arrays.asList(((IPackageFragment) elem).getCompilationUnits()));	
					}
					this.sysdef.addRootLocation(root.getResource().getLocation().toFile());
					ICompilationUnit[] comps = compList.toArray(new ICompilationUnit[compList.size()]);
					ASTParser parser = ASTParser.newParser(AST.JLS3);
					parser.setProject(project);
						//compilationUnit.open(null);
						try {
							parser.setProject(this.project);
							parser.setResolveBindings(true);
							for (int i = 0 ; i < comps.length; i++)
							{
								comps[i].open(null);
							}
							parser.createASTs(comps, new String[0], new ASTRequestorVerso(sysdef), null);
						} finally {
							for (int i = 0 ; i < comps.length; i++)
							{
								comps[i].close();
							}
						}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void parsePackage(IJavaElement elem) {
		//System.out.println("ParsePackage!!!!!!!!!!!!!!!!!!!!1");
		long startMili = 0;
		ModelVisitor visitor = new ModelVisitor();
		visitor.setSystemDef(sysdef);
		ICompilationUnit[] comps = null;
		try {
			comps = ((IPackageFragment) elem).getCompilationUnits();
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setProject(project);
			
			//parser.setBindingsRecovery(true);
			for (ICompilationUnit compilationUnit : comps) {
				//startMili = System.currentTimeMillis();

				compilationUnit.open(null);
				try {
					parser.setResolveBindings(true);
					parser.setSource(compilationUnit);
					ASTNode node = parser.createAST(null);
					node.accept(visitor);
				} finally {
					compilationUnit.close();
				}
				// System.out.println("On fait un nouveau accept ici!");

				//System.out.println(compilationUnit.getElementName() + " : "
				//		+ (System.currentTimeMillis() - startMili));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

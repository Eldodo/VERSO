package verso.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

public class TypeFinderUtil {
	
	private static String project = "Test";
	
	public static void setProjectName(String projName)
	{
		project = projName;
	}

	public static IType findTypeFromString(String s)
	{
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		IJavaModel model = JavaCore.create(workspace);
		IJavaProject pro = model.getJavaProject(project);
		IType elem = null;
		String name = s;
		if (name.startsWith("default."))
			name = name.substring("default.".length());
		try{
			elem = pro.findType(name);
		}catch(Exception e){System.out.println(e);return null;}
		
		return elem;
	}
	
	public static IJavaElement findJavaElementFromString(String s)
	{
		if (s.compareTo("default") == 0)
			s = "";
		if (s.startsWith("default."))
			s = s.substring("default.".length());
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		IJavaModel model = JavaCore.create(workspace);
		IJavaProject pro = model.getJavaProject(project);
		IPath sourcePath =null;
		s = s.replace(".", "/");
		IJavaElement elem = null;
		sourcePath = new Path("");
		sourcePath = sourcePath.append(s);
		try{
			pro.open(null);
			elem = pro.findElement(sourcePath);
		}catch(Exception e){System.out.println(e);}
		return elem;
	}
	
	public static IResource findIResourcefromString(String s)
	{
		String name = s;
		name = "src." + name;
		name = name.replace(".", "/");
		
		IPath pathName = new Path(name); 
		IResource fold = ResourcesPlugin.getWorkspace().getRoot().getProject(project).getFolder(pathName);
		if (fold == null)
			fold = ResourcesPlugin.getWorkspace().getRoot().getProject(project).getFile(pathName);
		return fold;
	}
	
	public static IPackageFragmentRoot[] getSourceFragmentRoot(boolean all)
	{
		List<IPackageFragmentRoot> rootlst = new ArrayList<IPackageFragmentRoot>();
		IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
		IPackageFragmentRoot[] roots = null;
		try{
			roots = model.getJavaProject(project).getAllPackageFragmentRoots();
			for (IPackageFragmentRoot r : roots)
			{
				if (r.getKind() == IPackageFragmentRoot.K_SOURCE || all)
				{
					rootlst.add(r);
				}
			}
		}catch(Exception e){System.out.println(e);}
		IPackageFragmentRoot[] rootstoReturn = new IPackageFragmentRoot[rootlst.size()];
		int compteur = 0;
		for (IPackageFragmentRoot fr : rootlst)
		{
			rootstoReturn[compteur++] = fr;
		}
		return rootstoReturn;
	}
}

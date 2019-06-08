package verso.util;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import verso.model.Element;
import verso.model.SystemDef;

public class TypeFinder {
	
	public static Element findElementFromChangePath(String path, SystemDef sys)
	{
		Set<Element> possibleElements = new HashSet<Element>();
		Set<Element> nextSet = new HashSet<Element>();
		String elemSimpleName = path.substring(path.lastIndexOf("/") +1);
		elemSimpleName = elemSimpleName.substring(0,elemSimpleName.length()- 5);
		String currElemName = "";
		for (Element e : sys.getAllElements())
		{
			currElemName = e.getName();
			if (currElemName.contains("."))
				currElemName = currElemName.substring(currElemName.lastIndexOf(".")+1);
			if (currElemName.compareTo(elemSimpleName) == 0)
			{
				possibleElements.add(e);
			}
		}
		if (possibleElements.size() == 1)
			return possibleElements.iterator().next();
		else if (possibleElements.size() == 0)
			return null;
		else
		{
			int segNumber = 2;
			String pathNoDot = path.substring(0,path.lastIndexOf(".")+1);
			String[] segPath = pathNoDot.split("[/]");
			
			while (possibleElements.size() > 1)
			{
				if (segNumber > segPath.length)
					break;
				nextSet.clear();
				for (Element e : possibleElements)
				{
					String[] segName = e.getName().split("[.]");
					if (segNumber > segName.length)
						continue;
					if (segPath[segPath.length-segNumber].compareTo(segName[segName.length-segNumber]) == 0)
					{
						nextSet.add(e);
					}
				}
				possibleElements.clear();
				possibleElements.addAll(nextSet);
				segNumber++;
			}
			if (possibleElements.size() == 1)
				return possibleElements.iterator().next();
			return null;
		}	
	}
	
	public static String findFileNameFromIFile(IFile file)
	{
		String toReturn = file.getFullPath().toString();
		toReturn = toReturn.substring(toReturn.indexOf("src/")+ 4);  //BigHack, should remove /src/
		toReturn = toReturn.replace("/", ".");
		toReturn = toReturn.substring(0,toReturn.lastIndexOf("."));
		if (toReturn.indexOf(".") == -1)
			toReturn = "default." + toReturn;
		return toReturn;
	}
	public static String findFilePathfromFile(IFile file)
	{
		String pacFrag = "";
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		IJavaModel model = JavaCore.create(workspace);
		IJavaProject ijp = model.getJavaProject(file.getProject().getName());
		try
		{
			IPackageFragmentRoot[] ipfrs = ijp.getPackageFragmentRoots();
			for (int i =0; i < ipfrs.length; i++)
			{
				if (ipfrs[i].getKind() == IPackageFragmentRoot.K_SOURCE)
				{
					System.out.println(ipfrs[i].getPath());
					if (ipfrs[i].getPath().isPrefixOf(file.getFullPath()))
					{
						for (int j = ipfrs[i].getPath().segmentCount(); j < file.getFullPath().segmentCount()-1; j++)
						{
							pacFrag += file.getFullPath().segment(j) + ".";
						}
						if (pacFrag.length()>1)
							pacFrag = pacFrag.substring(0, pacFrag.length()-1);
					}
				}
			}
		}catch(Exception e){System.out.println(e);}
		return pacFrag;
	}
	
	public static ICompilationUnit findCompilationUnitFromFile(IFile file)
	{
		ICompilationUnit toReturn = null;
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		IJavaModel model = JavaCore.create(workspace);
		IJavaProject ijp = model.getJavaProject(file.getProject().getName());
		//ijp.getPackageFragmentRoot(file.getProjectRelativePath())
		try
		{
			IPackageFragmentRoot[] ipfrs = ijp.getPackageFragmentRoots();
			for (int i =0; i < ipfrs.length; i++)
			{
				if (ipfrs[i].getKind() == IPackageFragmentRoot.K_SOURCE)
				{
					System.out.println(ipfrs[i].getPath());
					if (ipfrs[i].getPath().isPrefixOf(file.getFullPath()))
					{
						String pacFrag = "";
						for (int j = ipfrs[i].getPath().segmentCount(); j < file.getFullPath().segmentCount()-1; j++)
						{
							pacFrag += file.getFullPath().segment(j) + ".";
						}
						if (pacFrag.length()>1)
							pacFrag = pacFrag.substring(0, pacFrag.length()-1);
						IPackageFragment ipf = ipfrs[i].getPackageFragment(pacFrag);
						toReturn = ipf.getCompilationUnit(file.getName());
					}
				}
			}
		}catch(Exception e){System.out.println(e);}
		return toReturn;
	}

}

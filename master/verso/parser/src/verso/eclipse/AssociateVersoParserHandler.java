package verso.eclipse;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class AssociateVersoParserHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("AssociateVersoParserHandler.execute()");
		IWorkbenchPage p = null;
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow[] iwbws = wb.getWorkbenchWindows();
		p = null;
		for (int i =0; i < iwbws.length; i++)
		{
			System.out.println(iwbws[i]);
			p = iwbws[i].getActivePage();
		}
		
		IViewPart vp = p.findView("org.eclipse.jdt.ui.PackageExplorer");
		ISelection s = vp.getSite().getSelectionProvider().getSelection();

		IJavaProject pro = (IJavaProject)((TreeSelection)s).getFirstElement();
		IProject project = pro.getProject();
		
		 try {
		      IProjectDescription description = project.getDescription();
		      String[] natures = description.getNatureIds();
		      String[] newNatures = new String[natures.length + 1];
		      System.arraycopy(natures, 0, newNatures, 0, natures.length);
		      newNatures[natures.length] = "SimpleVersoParser.versoParserNature";
		      description.setNatureIds(newNatures);
		      project.setDescription(description, null);
		   } catch (CoreException e) {
		      // Something went wrong
		   }

		return null;
	}

}

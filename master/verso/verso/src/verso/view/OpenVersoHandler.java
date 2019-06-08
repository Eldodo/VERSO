package verso.view;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import verso.builder.IncrementalBuilder;

public class OpenVersoHandler extends AbstractHandler{

	public Object execute(ExecutionEvent event) throws ExecutionException {
		//System.out.println("Wooohooo!   Open Verso!!!");
		IWorkbenchPage p = null;
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow[] iwbws = wb.getWorkbenchWindows();
		p = null;
		for (int i = 0; i < iwbws.length; i++) {
			System.out.println(iwbws[i]);
			p = iwbws[i].getActivePage();
		}

		IViewPart vp = p.findView("org.eclipse.jdt.ui.PackageExplorer");
		ISelection s = vp.getSite().getSelectionProvider().getSelection();
		IJavaProject pro = (IJavaProject) ((TreeSelection) s).getFirstElement();

		System.out.println("Opening project : " + pro.getResource().getName());
		try{
			pro.getProject().build(IncrementalBuilder.FAKE_BUILD, null);
		}
		catch(Exception e){System.out.println(e);}
		
		
		try{
			IDE.openEditor(p, new VersoInput(pro.getResource().getName()), "verso.versoEditor", true);
			
			}catch(Exception e){System.out.println(e);}
		return null;
	}

}

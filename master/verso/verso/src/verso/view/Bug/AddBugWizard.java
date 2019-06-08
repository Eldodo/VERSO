package verso.view.Bug;

import java.awt.EventQueue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

import verso.builder.IncrementalBuilder;
import verso.model.Bug;
import verso.model.SystemDef;
import verso.representation.cubeLandscape.PluginScene;

public class AddBugWizard extends Wizard{

	AddBugPage page = null;
	SystemDef sys = null;
	PluginScene sce = null;
	Shell shell =null;
	Thread displayThread = null;
	public AddBugWizard(Shell shell,SystemDef sys, Thread displayThread)
	{
		super();
		this.sys = sys;
		page = new AddBugPage("AddBugPage",sys);
		page.createControl(shell);
		this.shell = shell;
		this.addPage(page);
		this.displayThread = displayThread;
	}
	public boolean performFinish() {
		
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		File f = workspace.getLocation().append(sys.getName()).append("save.bug").toFile();
		BufferedWriter bw = null;
		Bug tempBug = new Bug(page.getID(), true, page.getComment(), page.getAuthor(), page.getProg(), new Date(System.currentTimeMillis()), new Date(0), page.getElemLst() );
		try{
		 bw  = new BufferedWriter(new FileWriter(f,true));
		 bw.write(tempBug.toString());
		 sys.addBug(tempBug);
		 bw.flush();
		 bw.close();
		}catch(Exception e){System.out.println();}
		sys.addBug(tempBug);
		sys.computeBugMetrics();
		//trouver pourquoi ça ne fonctionne pas. 
		//call Redisplay
		//IncrementalBuilder.callListeners(sys);
		//System.out.println("je viens ici?"); 
		EventQueue.invokeLater(new DisplayRunner());
		return true;
	}
	
	private class DisplayRunner implements Runnable
	{

		@Override
		public void run() {
			IncrementalBuilder.callListeners(sys);
			
		}
		
	}

}

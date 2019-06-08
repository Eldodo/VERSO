package verso.view.Bug;

import java.awt.EventQueue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

import verso.builder.IncrementalBuilder;
import verso.model.Bug;
import verso.model.SystemDef;

public class ModifyBugWizard extends Wizard{

	SystemDef sys = null;
	ModifySelectBugPage page = null;
	
	public ModifyBugWizard(Shell shell,SystemDef sys)
	{
		super();
		this.sys = sys;
		page = new ModifySelectBugPage("AddBugPage",sys);
		page.createControl(shell);
		this.addPage(page);
	}
	
	public boolean performFinish() 
	{
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		File f = workspace.getLocation().append(sys.getName()).append("save.bug").toFile();
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f,false));
	
		for (Bug tempBug : sys.getAllBugs())
		{
			 bw.write(tempBug.toString());
			 bw.flush();
			
		}
		 bw.close();
		}catch(Exception e){System.out.println(e);}
		sys.computeBugMetrics();
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

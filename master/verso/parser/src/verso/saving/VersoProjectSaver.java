package verso.saving;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import verso.model.SystemManager;
import verso.saving.csv.CsvWriter;

public class VersoProjectSaver {

	public static void save(String projectName) {
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		IProject pro = workspace.getProject(projectName);
		IPath path = pro.getLocation().append(new Path("save.met"));
		
		CsvWriter cw = new CsvWriter(SystemManager.getSystem(projectName), path.toString());
		System.out.println("Printing project : " + projectName + " to file : " + path.toString());
		cw.print();
	}
}

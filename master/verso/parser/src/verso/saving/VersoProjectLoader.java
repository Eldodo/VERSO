package verso.saving;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import verso.model.SystemDef;
import verso.model.SystemManager;
import verso.saving.csv.CsvParser2;

public class VersoProjectLoader {

	public static void load(IProject project) {
		SystemDef sys = null;
		IPath path = project.getLocation().append(new Path("save.met"));
		System.out.println("Loading project : " + project.getName() + " from file : " + path.toString());
		if (path.toFile().exists()) {
			try {
				sys = CsvParser2.parseFile(path.toFile());
				sys.computeSVNMetrics();
				sys.setName(project.getName());
				System.out.println(sys.getName());
				SystemManager.addSystem(sys);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("File does not exist");
		}
		/*
		 * else { try{ project.open(null); project.build(IncrementalBuilder.FULL_BUILD,
		 * IncrementalBuilder.BUILDER_ID, null, null);
		 * //project.build(IncrementalProjectBuilder.FULL_BUILD, null); }catch(Exception
		 * e){System.out.println(e);} }
		 */
	}

}

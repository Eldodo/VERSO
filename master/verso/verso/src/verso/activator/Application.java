package verso.activator;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import verso.representation.cubeLandscape.PluginScene;
import verso.representation.cubeLandscape.SceneLandscape;

public class Application implements IApplication{

	
	public Object start(IApplicationContext context) throws Exception {
		
		ResourcesPlugin.getWorkspace().getRoot().getProject("Test").build(IncrementalProjectBuilder.FULL_BUILD, null);
		
		System.out.println("java.library.path: "+System.getProperty("java.library.path"));
		System.loadLibrary("jogl");
		System.out.println("Passed lib loading");
		PluginScene.main(new String[0]);
		while (SceneLandscape.isRunning)
			Thread.sleep(1000);
		System.out.println("closing");
		return null;
	}

	
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}

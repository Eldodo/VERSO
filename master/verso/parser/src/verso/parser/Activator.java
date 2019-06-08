package verso.parser;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;

import verso.builder.IncrementalBuilder;
import verso.builder.VersoModificationListener;
import verso.model.SystemDef;
import verso.model.SystemManager;
import verso.saving.VersoSaveParticipant;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "SimpleVersoParser";

	// The shared instance
	private static Activator plugin;
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		System.out.println("Ça marche3?");
		ISaveParticipant saveParticipant = new VersoSaveParticipant();
		try{
			ResourcesPlugin.getWorkspace().addSaveParticipant(this, saveParticipant);
		}catch(Exception e){System.out.println(e);}
		try {
			JhlClientAdapterFactory.setup();
		} catch (Exception e) {
			System.out.println(e);
		}
		//ResourcesPlugin.getWorkspace().build(IncrementalBuilder.FAKE_BUILD, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public void addModificationListnener(VersoModificationListener list)
	{
		IncrementalBuilder.addListener(list);
	}
	public SystemDef getSystem(String name)
	{
		return SystemManager.getSystem(name);
	}
	
	public String getProjects()
	{
		return SystemManager.getProjectsName();
	}

}

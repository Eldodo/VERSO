package verso.activator;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin{
	
	private static AbstractUIPlugin def = null;
	public void start(BundleContext context) throws Exception {
		super.start(context);
		System.out.println(this.getBundle().getLocation());
		def = this;
	}
	
	public static AbstractUIPlugin getDefault()
	{
		return def;
	}
	
}

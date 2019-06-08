package verso.representation.cubeLandscape.representationModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import verso.model.SystemDef;

public class TreemapSystemRepresentation /*extends SystemRepresentation*/ {
	/*public TreemapSystemRepresentation(SystemDef system) {
		super(system);
	}
	
	public LinkedList<TreemapPackageRepresentation> getSystemPackages() {
		LinkedList<TreemapPackageRepresentation> systemPackages = new LinkedList<TreemapPackageRepresentation>();
		
		for (TreemapPackageRepresentation p : this.getPackages()) {
			this.getSystemPackages(p, systemPackages);
		}
		
		return systemPackages;
	}
	
	private void getSystemPackages(TreemapPackageRepresentation pack, LinkedList<TreemapPackageRepresentation> systemPackages) {
		systemPackages.add(pack);
		
		for (TreemapPackageRepresentation p : pack.getPackages()) {
			getSystemPackages(p, systemPackages);
		}
	}
	
	@Override
	public void addPackage(TreemapPackageRepresentation p)
	{
		this.packages.put(p.getPackage().getName(), p);
	}
	
	@Override
	public Collection<TreemapPackageRepresentation> getPackages()
	{
		HashSet<TreemapPackageRepresentation> treemapPackages = new HashSet<TreemapPackageRepresentation>();
		for (PackageRepresentation p : this.packages.values()) {
			treemapPackages.add((TreemapPackageRepresentation)p);
		}
		
		return treemapPackages;
	}*/
}

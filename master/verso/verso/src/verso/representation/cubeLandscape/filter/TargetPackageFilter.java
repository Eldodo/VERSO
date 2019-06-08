package verso.representation.cubeLandscape.filter;

import java.util.HashSet;
import java.util.Set;

import verso.representation.IPickable;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.repvisitor.EntityFilterVisitorPackage;

public class TargetPackageFilter extends EntityFilter{
	
	PackageRepresentation pac = null;
	Set<String> filteredPackages = new HashSet<String>();
	
	public TargetPackageFilter(SystemRepresentation sys)
	{
		super(sys);
	}
	
	public void setElement(IPickable er)
	{
		if (er instanceof PackageRepresentation)
		{
			PackageRepresentation e = (PackageRepresentation)er;
			this.filterable = true;
			filteredPackages = e.getPackage().getAllPackageTargets();
			e.select();
			this.filter();
		}
		else
		{
			this.filterable = false;
		}
	}
	
	public void filter()
	{
		if (filterable)
		{
			SystemRepresentation.filterState = true;
			for (PackageRepresentation p : sys.getPackages())
			{
				p.accept(new EntityFilterVisitorPackage(this.filteredPackages));
			}
		}
	}

}

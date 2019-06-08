package verso.representation.cubeLandscape.filter;

import verso.model.Method;
import verso.representation.IPickable;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.repvisitor.EntityFilterVisitorMethod;

public class TargetMethodFilter extends EntityFilter{
	
	public TargetMethodFilter(SystemRepresentation sys)
	{
		super(sys);
	}
	
	public void setElement(IPickable er)
	{
		if (er instanceof MethodRepresentation)
		{
			MethodRepresentation e = (MethodRepresentation)er;
			this.filterable = true;
			e.select();
			filteredClasses = ((Method)e.getElement()).getTargets();
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
				p.accept(new EntityFilterVisitorMethod(this.filteredClasses));
			}
		}
	}
	
}

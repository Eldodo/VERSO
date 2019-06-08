package verso.representation.cubeLandscape.filter;

import java.util.HashSet;
import java.util.Set;

import verso.representation.IPickable;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.repvisitor.EntityFilterVisitorClass;

public class TargetClassFilter extends EntityFilter{
	
	ElementRepresentation element = null;
	Set<String> filteredClasses = new HashSet<String>();
	
	public TargetClassFilter(SystemRepresentation sys)
	{
		super(sys);
	}
	
	public void setElement(IPickable er)
	{
		if (er instanceof ElementRepresentation)
		{
			ElementRepresentation e = (ElementRepresentation)er;
			this.filterable = true;
			filteredClasses = e.getElementModel().getTargets();
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
				p.accept(new EntityFilterVisitorClass(this.filteredClasses));
			}
		}
	}

}

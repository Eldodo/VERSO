package verso.representation.cubeLandscape.representationModel.repvisitor;

import java.util.HashSet;
import java.util.Set;

import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;

public class EntityFilterVisitorClass implements IRepresentationVisitor{

	Set<String> filteredEntities = new HashSet<String>();
	public EntityFilterVisitorClass(Set<String> filteredElements)
	{
		this.filteredEntities = filteredElements;
	}
	
	public void visit(PackageRepresentation p) {
		for (PackageRepresentation pac : p.getPackages())
		{
			pac.accept(this);
		}
		for (ElementRepresentation elem : p.getElements())
		{
			elem.accept(this);
		}
		
	}
	
	
	public void visit(ElementRepresentation e) {
		if (this.filteredEntities.contains(e.getElementModel().getName()))
		{
			e.setFiltered();
		}
	}

	
	public void visit(MethodRepresentation method) {
		
	}

}

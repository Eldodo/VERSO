package verso.representation.cubeLandscape.representationModel.repvisitor;

import java.util.HashSet;
import java.util.Set;

import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;

public class EntityFilterVisitorPackage implements IRepresentationVisitor{

	Set<String> filteredEntities = new HashSet<String>();
	public EntityFilterVisitorPackage(Set<String> filteredElements)
	{
		this.filteredEntities = filteredElements;
	}
	
	public void visit(PackageRepresentation p) {
		if (this.filteredEntities.contains(p.getPackage().getName()))
		{
			p.setFiltered();
		}
	}
	
	public void visit(ElementRepresentation e) {
		
	}

	
	public void visit(MethodRepresentation method) {
		
	}

}

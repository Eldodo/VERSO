package verso.representation.cubeLandscape.representationModel.repvisitor;

import java.util.HashSet;
import java.util.Set;

import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;

public class EntityFilterVisitorAll implements IRepresentationVisitor{

	Set<String> filteredEntities = new HashSet<String>();
	public EntityFilterVisitorAll(Set<String> filteredElements)
	{
		this.filteredEntities = filteredElements;
	}

	public void visit(PackageRepresentation p) {
		if (this.filteredEntities.contains(p.getPackage().getName()))
		{
			p.setFiltered();
		}
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
		
		for (MethodRepresentation met : e.getMethods())
		{
			met.accept(this);
		}
		
	}

	
	public void visit(MethodRepresentation method) {
		if (this.filteredEntities.contains(method.getElement().getName()))
		{
			method.setFiltered();
		}
		
	}

}

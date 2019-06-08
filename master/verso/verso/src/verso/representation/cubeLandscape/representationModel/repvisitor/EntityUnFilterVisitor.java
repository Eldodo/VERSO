package verso.representation.cubeLandscape.representationModel.repvisitor;

import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;

public class EntityUnFilterVisitor implements IRepresentationVisitor{

	public EntityUnFilterVisitor()
	{
	}
	
	public void visit(PackageRepresentation p) {		
		p.setUnFiltered();
		p.unSelect();
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
		e.setUnFiltered();
		e.unSelect();
		for (MethodRepresentation met : e.getMethods())
		{
			met.accept(this);
		}
		
	}

	
	public void visit(MethodRepresentation method) {
		method.unSelect();
		method.setUnFiltered();
	}

}

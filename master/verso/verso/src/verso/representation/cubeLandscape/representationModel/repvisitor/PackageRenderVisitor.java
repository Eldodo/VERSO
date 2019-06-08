package verso.representation.cubeLandscape.representationModel.repvisitor;

import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;

public class PackageRenderVisitor implements IRepresentationVisitor{

	int currentPackageLevel = 0 ;
	
	public PackageRenderVisitor(int level)
	{
		this.currentPackageLevel = level;
	}
	
	public void visit(PackageRepresentation p) {
		if (p.getPackageLevel() == currentPackageLevel)
		{
			p.setRender(true);
		}
		else
		{
			p.setRender(false);
		}
		for (PackageRepresentation pac : p.getPackages())
		{
			visit(pac);
		}
		
	}

	public void visit(ElementRepresentation e) {
		// TODO Auto-generated method stub
		
	}

	public void visit(MethodRepresentation method) {
		// TODO Auto-generated method stub
		
	}

}

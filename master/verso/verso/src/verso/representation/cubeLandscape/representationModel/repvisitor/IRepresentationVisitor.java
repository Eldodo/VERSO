package verso.representation.cubeLandscape.representationModel.repvisitor;

import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.MethodRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;

public interface IRepresentationVisitor {
	
	public void visit(PackageRepresentation p);
	//public void visit(TreemapPackageRepresentation p);
	public void visit(ElementRepresentation e);
	public void visit(MethodRepresentation method);

}

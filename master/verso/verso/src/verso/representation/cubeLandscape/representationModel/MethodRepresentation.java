package verso.representation.cubeLandscape.representationModel;

import verso.graphics.primitives.Cube;
import verso.model.Method;
import verso.representation.cubeLandscape.representationModel.repvisitor.IRepresentationVisitor;

public class MethodRepresentation extends LowLevelElementRepresentation {

	public MethodRepresentation(Method method) {
		super(method);
		this.mesh = new Cube();
	}

	public void accept(IRepresentationVisitor mv) {
		mv.visit(this);
	}

	public String getSimpleName() {
		return this.getElement().getName();
	}

}

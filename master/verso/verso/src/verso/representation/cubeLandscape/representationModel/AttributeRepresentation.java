package verso.representation.cubeLandscape.representationModel;

import verso.graphics.primitives.Cylinder;
import verso.model.Attribute;

public class AttributeRepresentation extends LowLevelElementRepresentation{

	public AttributeRepresentation(Attribute a)
	{
		super(a);
		this.mesh = new Cylinder();
	}
}

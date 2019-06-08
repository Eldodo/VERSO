package verso.representation.cubeLandscape.representationModel;

import java.awt.Color;

import javax.media.opengl.GL;

import verso.graphics.primitives.Arc;
import verso.graphics.primitives.Cylinder;
import verso.graphics.primitives.Primitive;
import verso.model.InterfaceDef;

public class InterfaceRepresentation extends ElementRepresentation {
	protected Primitive border;

	public InterfaceRepresentation(InterfaceDef inter) {
		super(inter);
		this.mesh = new Cylinder();
	}

	public String getName() {
		return "Interface : " + this.getElementModel().getName();
	}

	public String getSimpleName() {
		return this.getElementModel().getName();
	}

	@Override
	protected void setRatio(GL gl) {
		gl.glScaled(ElementRepresentation.Length, 1, ElementRepresentation.Length);

	}

	protected void renderBorders(GL gl, Double borderWidth, Double borderHeight, Color borderColor) {
		// gl.glColor3d(Math.random(), Math.random(), Math.random());

		gl.glColor3d(borderColor.getRed() / 255.0, borderColor.getGreen() / 255.0, borderColor.getBlue() / 255.0);

		this.border = new Arc(borderHeight, 360.0, ElementRepresentation.Length / 2.0 - 0.1,
				ElementRepresentation.Length / 2.0 + borderWidth / 2.0, 10);

		this.border.render(gl);
	}
}

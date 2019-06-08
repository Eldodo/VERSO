package verso.representation.cubeLandscape.representationModel;


import java.awt.Color;

import javax.media.opengl.GL;

import verso.graphics.primitives.Cube;
import verso.graphics.primitives.Primitive;
import verso.graphics.primitives.Teapot;
import verso.model.LibDef;
import verso.representation.Renderable;

public class LibRepresentation extends ElementRepresentation implements Renderable{
	protected Primitive topBorder;
	protected Primitive bottomBorder;
	protected Primitive leftBorder;
	protected Primitive rightBorder;
	
	
	public LibRepresentation(LibDef c)
	{
		super(c);
		this.mesh = new Cube();
		this.topBorder = new Cube();
		this.bottomBorder = new Cube();
		this.leftBorder = new Cube();
		this.rightBorder = new Cube();
	}
	
	@Override
	public float getLevel() {
		return 1;
	}
	
	@Override
	public String toString() {
		return "LibRep<"+this.getElementModel().getName()+">";
	}

	public String getName()
	{
		return "Library : " + this.getElementModel().getName();
	}
	
	public String getSimpleName()
	{
		return this.getElementModel().getName();
	}

	protected void setRatio(GL gl) {
		gl.glScaled(ElementRepresentation.Width , 1, ElementRepresentation.Length);
		
	}
	
	protected void renderBorders(GL gl, Double borderWidth, Double borderHeight, Color borderColor)
	{
		//gl.glColor3d(Math.random(), Math.random(), Math.random());
		
		gl.glColor3d(borderColor.getRed() / 255.0, borderColor.getGreen() / 255.0, borderColor.getBlue() / 255.0);
		
		gl.glPushMatrix();
			//gl.glTranslated(0, 0.5, 0);
			gl.glTranslated(0, 0, ElementRepresentation.Length/2.0);
			//gl.glScaled(sizeX, 0.3, 0.2);
			gl.glScaled(ElementRepresentation.Width + borderWidth, borderHeight, borderWidth);
			gl.glTranslated(0, 0.5, 0);
			this.topBorder.render(gl);
		gl.glPopMatrix();
		
		
		gl.glPushMatrix();
		//gl.glTranslated(0, 0.5, 0);
		//gl.glColor3d(Math.random(), Math.random(), Math.random());
			gl.glTranslated(0, 0, -ElementRepresentation.Length/2.0);
			//gl.glScaled(sizeX, 0.3, 0.2);
			gl.glScaled(Width + borderWidth, borderHeight, borderWidth);
			gl.glTranslated(0, 0.5, 0);
			this.bottomBorder.render(gl);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		//gl.glTranslated(0, 0.5, 0);
		//gl.glColor3d(Math.random(), Math.random(), Math.random());
			gl.glTranslated(-ElementRepresentation.Width/2.0, 0, 0);
			//gl.glScaled(0.2, 0.3, sizeZ);
			gl.glScaled(borderWidth, borderHeight, Length);
			gl.glTranslated(0, 0.5, 0);
			this.leftBorder.render(gl);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		//gl.glTranslated(0, 0.5, 0);
		//gl.glColor3d(Math.random(), Math.random(), Math.random());
			gl.glTranslated(ElementRepresentation.Width/2.0, 0, 0);
			//gl.glScaled(0.2, 0.3, sizeZ);
			gl.glScaled(borderWidth, borderHeight, Length);
			gl.glTranslated(0, 0.5, 0);
			this.rightBorder.render(gl);
		gl.glPopMatrix();
		
	}
	
	
}

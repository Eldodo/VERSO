package verso.graphics.primitives;

import java.awt.Color;

import javax.media.opengl.GL;

import verso.representation.Renderable;

public class CubeNoCapColored extends PrimitiveColored implements Renderable {
	private Color baseColor;
	private Color topColor;
	
	public CubeNoCapColored(Color baseColor, Color topColor) {
		this.baseColor = baseColor;
		this.topColor = topColor;
	}
	
	public Color getBaseColor() {
		return this.baseColor;
	}
	
	public void setBaseColor(Color baseColor) {
		this.baseColor = baseColor;
	}
	
	public Color getTopColor() {
		return this.topColor;
	}
	
	public void setTopColor(Color topColor) {
		this.topColor = topColor;
	}
	
	public String getName()
	{
		return "CubeNoCapColored";
	}
	public void render(GL gl)
	{
		gl.glPushMatrix();
		//float[] color = {1.0f,0.0f,0.0f,1.0f};
		//gl.glShadeModel (GL.GL_SMOOTH);
		//gl.glMaterialfv(GL.GL_FRONT_AND_BACK,GL.GL_DIFFUSE,color,0);
		//gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE);

			gl.glBegin(GL.GL_QUADS);
				//front side
				gl.glNormal3f(0.0f, 0.0f, 1.0f);
				gl.glColor3f(baseColor.getRed()/255.0f, baseColor.getGreen()/255.0f, baseColor.getBlue()/255.0f);
				gl.glVertex3f(-0.5f, -0.5f, 0.5f);
				gl.glVertex3f(0.5f, -0.5f, 0.5f);
				gl.glColor3f(topColor.getRed()/255.0f, topColor.getGreen()/255.0f, topColor.getBlue()/255.0f);
				gl.glVertex3f(0.5f, 0.5f, 0.5f);
				gl.glVertex3f(-0.5f, 0.5f, 0.5f);
				
				//right side
				gl.glNormal3f(1.0f, 0.0f, 0.0f);
				gl.glColor3f(baseColor.getRed()/255.0f, baseColor.getGreen()/255.0f, baseColor.getBlue()/255.0f);
				gl.glVertex3f(0.5f, -0.5f, 0.5f);
				gl.glVertex3f(0.5f, -0.5f, -0.5f);
				gl.glColor3f(topColor.getRed()/255.0f, topColor.getGreen()/255.0f, topColor.getBlue()/255.0f);
				gl.glVertex3f(0.5f, 0.5f, -0.5f);
				gl.glVertex3f(0.5f, 0.5f, 0.5f);
				

				//left side
				gl.glNormal3f(-1.0f, 0.0f, 0.0f);
				gl.glColor3f(baseColor.getRed()/255.0f, baseColor.getGreen()/255.0f, baseColor.getBlue()/255.0f);
				gl.glVertex3f(-0.5f, -0.5f, -0.5f);
				gl.glVertex3f(-0.5f, -0.5f, 0.5f);
				gl.glColor3f(topColor.getRed()/255.0f, topColor.getGreen()/255.0f, topColor.getBlue()/255.0f);
				gl.glVertex3f(-0.5f, 0.5f, 0.5f);
				gl.glVertex3f(-0.5f, 0.5f, -0.5f);

				//back side
				gl.glNormal3f(0.0f, 0.0f, -1.0f);
				gl.glColor3f(baseColor.getRed()/255.0f, baseColor.getGreen()/255.0f, baseColor.getBlue()/255.0f);
				gl.glVertex3f(0.5f, -0.5f, -0.5f);
				gl.glVertex3f(-0.5f, -0.5f, -0.5f);
				gl.glColor3f(topColor.getRed()/255.0f, topColor.getGreen()/255.0f, topColor.getBlue()/255.0f);
				gl.glVertex3f(-0.5f, 0.5f, -0.5f);
				gl.glVertex3f(0.5f, 0.5f, -0.5f);
			gl.glEnd();
		gl.glPopMatrix();
	}
	
	public String getSimpleName() {
		return getName();
	}
}

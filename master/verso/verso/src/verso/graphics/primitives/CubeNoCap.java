package verso.graphics.primitives;

import javax.media.opengl.GL;

import verso.representation.Renderable;

public class CubeNoCap extends Primitive implements Renderable{

	
	public String getName()
	{
		return "CubeNoCap";
	}
	public void render(GL gl)
	{
		//System.out.println("allo");
		
		gl.glPushMatrix();
		//float[] color = {1.0f,0.0f,0.0f,1.0f};
		//gl.glShadeModel (GL.GL_SMOOTH);
		//gl.glMaterialfv(GL.GL_FRONT_AND_BACK,GL.GL_DIFFUSE,color,0);
		//gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE);
		
			gl.glBegin(GL.GL_QUADS);
				/*
				// top side
				//gl.glColor3f(0.0f, 0.0f, 1.0f);
				gl.glNormal3f(0.0f, 1.0f, 0.0f);
				gl.glVertex3f(-0.5f, 0.5f, 0.5f);
				gl.glVertex3f(-0.5f, 0.5f, -0.5f);
				gl.glVertex3f(0.5f, 0.5f, -0.5f);
				gl.glVertex3f(0.5f, 0.5f, 0.5f);
				*/
				//front side
				//gl.glColor3f(1.0f, 0.0f, 0.0f);
				gl.glNormal3f(0.0f, 0.0f, 1.0f);
				gl.glVertex3f(-0.5f, 0.5f, 0.5f);
				gl.glVertex3f(-0.5f, -0.5f, 0.5f);
				gl.glVertex3f(0.5f, -0.5f, 0.5f);
				gl.glVertex3f(0.5f, 0.5f, 0.5f);
				
				//right side
				//gl.glColor3f(0.0f, 1.0f, 0.0f);
				gl.glNormal3f(1.0f, 0.0f, 0.0f);
				gl.glVertex3f(0.5f, -0.5f, -0.5f);
				gl.glVertex3f(0.5f, -0.5f, 0.5f);
				gl.glVertex3f(0.5f, 0.5f, 0.5f);
				gl.glVertex3f(0.5f, 0.5f, -0.5f);
				
				//left side
				//gl.glColor3f(1.0f, 1.0f, 0.0f);
				gl.glNormal3f(-1.0f, 0.0f, 0.0f);
				gl.glVertex3f(-0.5f, -0.5f, -0.5f);
				gl.glVertex3f(-0.5f, -0.5f, 0.5f);
				gl.glVertex3f(-0.5f, 0.5f, 0.5f);
				gl.glVertex3f(-0.5f, 0.5f, -0.5f);
				
				//back side
				//gl.glColor3f(0.0f, 1.0f, 1.0f);
				gl.glNormal3f(0.0f, 0.0f, -1.0f);
				gl.glVertex3f(-0.5f, 0.5f, -0.5f);
				gl.glVertex3f(-0.5f, -0.5f, -0.5f);
				gl.glVertex3f(0.5f, -0.5f, -0.5f);
				gl.glVertex3f(0.5f, 0.5f, -0.5f);
			gl.glEnd();
		gl.glPopMatrix();
		
		//System.out.println("salut");

	}
	
	public String getSimpleName() {
		return getName();
	}

}

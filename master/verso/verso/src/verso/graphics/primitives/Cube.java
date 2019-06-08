package verso.graphics.primitives;

import javax.media.opengl.GL;

import verso.representation.Renderable;

public class Cube extends Primitive implements Renderable{
	
	public static void createDisplayList(GL gl)
	{
		gl.glNewList(2, GL.GL_COMPILE_AND_EXECUTE);
		gl.glPushMatrix();
		//GLUT glut = new GLUT();
		//glut.glutSolidCube(1.0f);
		
			gl.glBegin(GL.GL_QUADS);
				
				// top side
				//gl.glColor3f(0.0f, 0.0f, 1.0f);
				gl.glNormal3f(0.0f, 1.0f, 0.0f);
				gl.glVertex3f(-0.5f, 0.5f, 0.5f);
				gl.glVertex3f(0.5f, 0.5f, 0.5f);
				gl.glVertex3f(0.5f, 0.5f, -0.5f);
				gl.glVertex3f(-0.5f, 0.5f, -0.5f);
				
				// bottom side
				//gl.glColor3f(0.0f, 0.0f, 1.0f);
				gl.glNormal3f(0.0f, -1.0f, 0.0f);
				gl.glVertex3f(-0.5f, -0.5f, 0.5f);
				gl.glVertex3f(0.5f, -0.5f, 0.5f);
				gl.glVertex3f(0.5f, -0.5f, -0.5f);
				gl.glVertex3f(-0.5f, -0.5f, -0.5f);
				
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
				gl.glVertex3f(0.5f, 0.5f, -0.5f);
				gl.glVertex3f(0.5f, 0.5f, 0.5f);
				gl.glVertex3f(0.5f, -0.5f, 0.5f);
				
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
				gl.glVertex3f(0.5f, 0.5f, -0.5f);
				gl.glVertex3f(0.5f, -0.5f, -0.5f);
				gl.glVertex3f(-0.5f, -0.5f, -0.5f);
			gl.glEnd();
			
		gl.glPopMatrix();
		gl.glEndList();
	}

	public String getName() {
		return "Cube";
	}

	public void render(GL gl) {
		gl.glCallList(2);
	}

	public String getSimpleName() {
		return getName();
	}

}

package verso.graphics.primitives;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.GLUT;

import verso.representation.Renderable;


public class Cartesian extends Primitive implements Renderable{
	GLUT glut= new GLUT();
	GLU glu= new GLU();
	
	public void render(GL gl) 
	{
		gl.glPushMatrix();
			gl.glColor3f(1.0f,1.0f,1.0f);
			glut.glutSolidSphere(0.5, 10, 10);
			gl.glColor3f(1.0f,0.0f,0.0f);
			glu.gluCylinder(glu.gluNewQuadric(), 0.3, 0.1, 5.0, 10, 1);
			gl.glRotated(270, 1.0, 0.0, 0.0);
			gl.glColor3f(0.0f,1.0f,0.0f);
			glu.gluCylinder(glu.gluNewQuadric(), 0.3, 0.1, 5.0, 10, 1);
			gl.glRotated(90, 0.0, 1.0, 0.0);
			gl.glColor3f(0.0f,0.0f,1.0f);
			glu.gluCylinder(glu.gluNewQuadric(), 0.3, 0.1, 5.0, 10, 1);
		gl.glPopMatrix();
	}
	
	public String getName()
	{
		return "Cartesian";
	}

	public String getSimpleName() {
		return getName();
	}

}

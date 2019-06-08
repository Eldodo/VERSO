package verso.graphics.primitives;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

public class Cylinder extends Primitive {

	
	public String getName()
	{
		return "Cylinder";
	}
	
	public void render(GL gl) {
		GLU glu = new GLU();
		gl.glPushMatrix();
			gl.glTranslated(0, -0.5, 0);
			gl.glRotated(-90, 1, 0, 0);
			gl.glPushMatrix();
				gl.glTranslated(0, 0, 1);
				//gl.glRotated(180, 1, 0, 0);
				glu.gluDisk(glu.gluNewQuadric(), 0.5, 0, 10, 1);
			gl.glPopMatrix();
			glu.gluCylinder(glu.gluNewQuadric(), 0.5, 0.5, 1, 10, 1);		
		gl.glPopMatrix();
	}

	
	public String getSimpleName() {
		return getName();
	}
	

}

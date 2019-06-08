package verso.graphics.primitives;

import javax.media.opengl.GL;

import com.sun.opengl.util.GLUT;

import verso.representation.Renderable;

public class Teapot extends Primitive implements Renderable {
	double meshSize;
	
	public Teapot(double meshSize) {
		this.meshSize = meshSize;
	}
	
	public String getName() {
		return "Teapot";
	}
	
	public void render(GL gl) {
		GLUT glut = new GLUT();
		glut.glutSolidTeapot(meshSize);
	}
	
	public String getSimpleName() {
		return getName();
	}
}

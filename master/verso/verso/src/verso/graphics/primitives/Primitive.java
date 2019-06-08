package verso.graphics.primitives;

import javax.media.opengl.GL;

import verso.representation.IPickable;
import verso.representation.Renderable;

public abstract class Primitive implements Renderable,IPickable{
	
	public abstract void render(GL gl);


public void setCamDist(double camX, double camY, double camZ){}
}
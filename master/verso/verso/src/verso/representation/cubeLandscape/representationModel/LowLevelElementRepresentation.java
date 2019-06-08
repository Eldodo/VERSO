package verso.representation.cubeLandscape.representationModel;

import javax.media.opengl.GL;

import verso.graphics.VersoScene;
import verso.model.Entity;
import verso.model.LowLevelElement;
import verso.representation.Renderable;

public class LowLevelElementRepresentation extends EntityRepresentation implements Renderable{

	private LowLevelElement element;
	private double twist;
	private double gridSize;
	
	
	public LowLevelElementRepresentation(LowLevelElement element)
	{
		this.element = element;
		this.twist =0;
		this.height =0;
		this.gridSize =0;
	}
	
	public void setGridSize(double grSize)
	{
		this.gridSize = grSize;
	}
	
	public LowLevelElement getElement()
	{
		return this.element;
	}
	public void setTwist(double twist)
	{
		this.twist = twist;
	}
	
	public Entity getEntity()
	{
		return this.element;
	}
	
	public String getName()
	{
		return "Method : " + this.getElement().getName();
	}
	
	public void render(GL gl)
	{
		
		gl.glPushMatrix();
			gl.glTranslated(-(ClassRepresentation.Width)/2.0 + gridSize*0.2, 0.0001, -(ClassRepresentation.Length)/2.0 + gridSize*0.1);
			gl.glScaled(gridSize, gridSize, gridSize);
			gl.glTranslated((double)this.posX, 0, (double)this.posZ);
			gl.glTranslated(0.5, 0.0, 0.5);
			gl.glRotated(twist, 0, 1, 0);
			gl.glScaled(ElementRepresentation.Width, this.height, ElementRepresentation.Length);
			
			
			gl.glTranslated(0, 0.5, 0);
			
			if (this.isSelected)
			{
				gl.glColor3f(0,1,0);
			}
			else if (!this.isFiltered() && SystemRepresentation.filterState)
				gl.glColor3f(this.unsaturatedColor.getRed()/255.0f,this.unsaturatedColor.getGreen()/255.0f,this.unsaturatedColor.getBlue()/255.0f);
			else
				gl.glColor3f(this.color.getRed()/255.0f,this.color.getGreen()/255.0f,this.color.getBlue()/255.0f);
			
			gl.glLoadName(VersoScene.id);
			VersoScene.pickingEntities.put(VersoScene.id++,this);
			this.mesh.render(gl);
		gl.glPopMatrix();
	}
	
}

package verso.representation.cubeLandscape.representationModel;

import java.awt.Color;
import java.io.File;

import javax.media.opengl.GL;

import ca.umontreal.iro.utils.Config;
import verso.Main;
import verso.graphics.primitives.Primitive;
import verso.model.Entity;
import verso.representation.IPickable;
import verso.representation.Renderable;


public class EntityRepresentation implements Renderable,IPickable{

	public static boolean hideFilteredClasses = false;
	protected boolean isFiltered = false;
	protected Color color = new Color(0,0,255);
	protected Color savedColor = null;
	protected Color unsaturatedColor = new Color(0,0,255);
	protected float height;
	protected float posX =0;
	protected float posZ =0;
	
	protected float sizeX =0;
	protected float sizeZ =0;
	
	
	protected int absolutePosX = 0;
	protected int absolutePosZ = 0;
	
	//Ajout Simon
	public int getAbsolutePosX() {
		return absolutePosX;
	}
	
	public String getPath(String folderPath) {
		String path = this.getEntity().getName();
		//TODO project name to CONSTANTIZE
		String packages[] = path.split("\\.");
		String classes[] = path.split("#");
		
		
		
		/*if(classes.length > 1)
			path = path.substring(packages[0].length(), path.length()-classes[1].length()-1);
		else 
			path = path.substring(packages[0].length());*/
		path = path.replaceAll("\\.", "\\"+"/");
		//TODO HARDCODE
		//path = folderPath+packages[0]+"-src/src"+path+".java";
		path = Main.sourceFolder+"/src/"+path+".java";
		System.out.println("ici: "+packages[0]);
		return path;
	}
	
	public int getAbsolutePosZ() {
		return absolutePosZ;
	}
	
	// Ajout Mehdi
	public void setSizeX(float sizeX)
	{
		this.sizeX = sizeX;
	}
	
	public void setSizeZ(float sizeZ)
	{
		this.sizeZ =sizeZ;
	}
	
	public float getSizeX()
	{
		return this.sizeX;
	}
	
	public float getSizeZ()
	{
		return this.sizeZ;
	}
	
	//*********************************************
	
	
	protected Primitive mesh;
	protected boolean isSelected = false;
	/*
	protected double camX =0.0;
	protected double camY =0.0;
	protected double camZ =0.0;
	*/
	public void setFiltered() {
		this.isFiltered = true;
	}

	public void setUnFiltered() {
		this.isFiltered = false;
	}

	public boolean isFiltered() {
		return isFiltered;
	}

	public void setPosX(float posX) {
		this.posX = posX;
	}

	public float getPosX() {
		return this.posX;
	}

	public void select() {
		this.isSelected = true;
	}

	public void unSelect() {
		this.isSelected = false;
	}

	public boolean isSelected() {
		return this.isSelected;
	}

	public void setPosZ(float posZ) {
		this.posZ = posZ;
	}

	public float getPosZ() {
		return this.posZ;
	}

	public void setColor(Color col) {
		float[] hsb;
		this.color = col;
		hsb = Color.RGBtoHSB(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), null);
		this.unsaturatedColor = new Color(Color.HSBtoRGB(hsb[0], 0.35f, hsb[2]));
	}

	// Ajout Simon
	public Color getColor() {
		return this.color;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	// Ajout Simon
	public float getHeight() {
		return this.height;
	}

	public Primitive getMesh() {
		return this.mesh;
	}
	// *************************************

	public void render(GL gl) {

	}

	public void setCamDist(double camX, double camY, double camZ) {
	}

	public Entity getEntity() {
		return null;
	}

	public String getName() {
		return "EntityRepresentation";
	}

	public String getSimpleName() {
		return getName();
	}

	public float getLevel() {
		throw new IllegalStateException("Must overriden in subclasses");
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = true;
		try {
			EntityRepresentation e = (EntityRepresentation) o;
			return e.getSimpleName().equals(this.getSimpleName());
		}
		catch(Exception e) {return false;}
	}
	
	public void saveCurrentColor() {
		savedColor = color;
	}
	
	public void restoreSavedColor() {
		color = savedColor;
	}
	public Color getSavedColor() {
		return savedColor;
	}
}

package verso.representation.cubeLandscape.representationModel;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;

import javax.media.opengl.GL;

import verso.graphics.VersoScene;
import verso.graphics.primitives.Cube;
import verso.graphics.primitives.Primitive;
import verso.model.Package;
import verso.representation.Renderable;

public class TreemapPackageRepresentation extends PackageRepresentation implements Renderable, Comparable<TreemapPackageRepresentation> {	
	public int orientationColor = VERTICAL;
	public int horizontalOrientation = LEFT;
	public int verticalOrientation = UP;
	
	
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;
	public static final int UP = 4;
	public static final int DOWN = 5;
	
	
	
	public int orientation = HORIZONTAL;
	
	public int getOrientation() {
		return this.orientation;
	}
	
	public void setOrientation(int orientation) {
		if (orientation == HORIZONTAL || orientation == VERTICAL) {
			this.orientation = orientation;
		}
	}

	//meshes
	protected Primitive topBorder;
	protected Primitive bottomBorder;
	protected Primitive leftBorder;
	protected Primitive rightBorder;
	
	private TreemapPackageRepresentation pseudoPackage = null;
	
	public TreemapPackageRepresentation(Package packagedef)
	{
		super(packagedef);
		
		this.mesh = new Cube();
		this.topBorder = new Cube();
		this.bottomBorder = new Cube();
		this.leftBorder = new Cube();
		this.rightBorder = new Cube();
	}
	
	/*
	public ElementRepresentation getElement(String elementName)
	{
		return this.getElements();
	}
	
	public Collection<ElementRepresentation> getElements() {
		if (this.elements.size() == 0) {
			if (this.elementsPackage != null) {
				return this.elementsPackage.getElements();
			}
			else {
				return null;
			}
		}
		else {
			return this.elements.values();
		}
	}
	
	public void clearElements() {
		super.clearElements();
		
		if (elementsPackage != null) {
			elementsPackage.clearElements();
		}
	}
	*/
	
	
	public TreemapPackageRepresentation getPackage(String packageName)
	{
		return (TreemapPackageRepresentation)this.packages.get(packageName);
	}
	
	public TreemapPackageRepresentation findPackage(String pacName)
	{
		PackageRepresentation pac = null;
		if (this.packages.containsKey(pacName))
		{
			return (TreemapPackageRepresentation)this.packages.get(pacName);
		}
		else
		{
			
			for (PackageRepresentation p : this.getPackages())
			{
				pac = p.findPackage(pacName);
				if (pac != null)
					break;
			}
		}
		return (TreemapPackageRepresentation)pac;
	}
	
	
	public Collection<TreemapPackageRepresentation> getTreemapPackages()
	{
		LinkedList<TreemapPackageRepresentation> treemapPackages = new LinkedList<TreemapPackageRepresentation>();
		for (PackageRepresentation pac : super.packages.values()) {
			treemapPackages.add((TreemapPackageRepresentation)pac);
		}
		
		return treemapPackages;
	}
	
	public Collection<TreemapPackageRepresentation> getTreemapSubPackages()
	{
		LinkedList<TreemapPackageRepresentation> treemapPackages = new LinkedList<TreemapPackageRepresentation>();
		for (PackageRepresentation pac : super.getSubPackages()) {
			treemapPackages.add((TreemapPackageRepresentation)pac);
		}
		
		return treemapPackages;
	}
	
	
	public void computeAbsolutePosition(int parentPosX, int parentPosZ) {
		this.absolutePosX = parentPosX + (int) this.posX;
		this.absolutePosZ = parentPosZ + (int) this.posZ;

		for (TreemapPackageRepresentation p : this.getTreemapPackages()) {
			p.computeAbsolutePosition(this.absolutePosX, this.absolutePosZ);
		}

		for (ElementRepresentation e : this.getElements()) {
			e.computeAbsolutePosition(this.absolutePosX, this.absolutePosZ);
		}
	}
	
	
	
	/*
	public void accept(IRepresentationVisitor mv)
	{
		mv.visit(this);
	}
	*/
	
	/*
	public TreemapPackageRepresentation copy() {
		TreemapPackageRepresentation packageCopy = new TreemapPackageRepresentation(this.packagedef);
		
		for (PackageRepresentation subPack : this.packages.values()) {
			packageCopy.addPackage(subPack.copy());
		}
		
		return packageCopy;
	}
	*/
	
	
	public void render(GL gl)
	{
		//System.out.println("RENDER TREEMAP PACKAGE REPRESENTATION");
		
		double heightPackage = 0.5;
		
		/*
		if (hideFilteredClasses && (!this.isFiltered() && SystemRepresentation.filterState) && !this.fakePackage && !this.anyChildIsSelected())
				return;
		*/
		
		
		if (this.isFakePackage()) {
			gl.glPushMatrix();
				
				gl.glTranslated((double)this.posX, 0.0, (double)this.posZ);
				
				for (ElementRepresentation elem : elements.values())
				{
					elem.render(gl);
				}
				
				
				for (TreemapPackageRepresentation pack : this.getTreemapPackages())
				{
					pack.render(gl);
				}
								
			gl.glPopMatrix();
			
			return;
		}
		
	
		gl.glPushMatrix();			
			gl.glTranslated((double)this.posX, 0.0, (double)this.posZ);
		
			if (!(this.render))
			{
				/*
				gl.glPushMatrix();
					gl.glTranslated(this.sizeX/2.0, 0, this.sizeZ/2.0);
					renderBorders(gl, 0.25, 0.51, Color.black);
				gl.glPopMatrix();
				*/
				
				gl.glTranslated(0.0, heightPackage, 0.0);
				
				
				for (ElementRepresentation elem : elements.values())
				{
					elem.render(gl);
				}
				
				
				for (TreemapPackageRepresentation pack : this.getTreemapPackages())
				{
					pack.render(gl);
				}

				gl.glTranslated(0.0, -heightPackage, 0.0);
				
				
				
				gl.glScaled(1, heightPackage, 1);
				//gl.glScaled(1, 0.001, 1);
				
				gl.glTranslated(this.sizeX/2.0, 0, this.sizeZ/2.0);
				gl.glTranslated(0, 0.5, 0);
				gl.glScaled(sizeX, 1, sizeZ);


				
				if (this.orientationColor == VERTICAL) {
					gl.glColor3f(0.5f, 0.5f, 0.5f);
				}
				else {
					gl.glColor3f(0.7f, 0.7f, 0.7f);
				}
				
				
				//gl.glColor3f(0.7f,0.7f,0.7f);
				//gl.glColor3d(Math.random(), Math.random(), Math.random());
			
				gl.glLoadName(VersoScene.id);
				VersoScene.pickingEntities.put(VersoScene.id++, this);
				this.mesh.render(gl);
			}
			else {
				
				gl.glPushMatrix();
					gl.glScaled(1, this.height, 1);
					gl.glTranslated(this.sizeX/2.0, 0, this.sizeZ/2.0);
					gl.glTranslated(0, 0.5, 0);
					gl.glScaled(sizeX, 1, sizeZ);
					
					/*
					if (this.isSelected) {
						//gl.glColor3f(0.0f, 1.0f, 1.0f);
						gl.glColor3f((1.0f - this.color.getRed())/255.0f, (1.0f - this.color.getGreen())/255.0f, (1.0f - this.color.getBlue())/255.0f);
						//gl.glColor3f(1.0f, 0.5f, 1.0f);
					}
					else*/
					
					//if (!this.isFiltered() && SystemRepresentation.filterState)
					if (this.isFiltered && SystemRepresentation.filterState)
						gl.glColor3f(this.unsaturatedColor.getRed()/255.0f,this.unsaturatedColor.getGreen()/255.0f,this.unsaturatedColor.getBlue()/255.0f);
					else
						gl.glColor3f(this.color.getRed()/255.0f,this.color.getGreen()/255.0f,this.color.getBlue()/255.0f);
					
					gl.glLoadName(VersoScene.id);
					VersoScene.pickingEntities.put(VersoScene.id++, this);
					this.mesh.render(gl);
				gl.glPopMatrix();
				
				
				
				if (this.isSelected) {
					gl.glPushMatrix();
						gl.glTranslated(this.sizeX/2.0, 0, this.sizeZ/2.0);
						//gl.glTranslated(0.0, this.height, 0.0);
						gl.glColor3f(0.0f, 1.0f, 0.0f);
						this.renderBorders(gl, 0.75, this.height / 3.0, this.bordersColor);
					gl.glPopMatrix();
				}
				
			}
		

		
			/*
			boolean renderSubPackages = false;
			for (PackageRepresentation pack : this.getSubPackages()) {
				if (pack.render) {
					renderSubPackages = true;
					break;
				}
			}
			
	
			
			
			if ((!this.fakePackage && !this.render && renderSubPackages) || (!this.fakePackage && !this.render && this.getElements().size() > 0)) {
				renderBorders(gl, 0.005, 0.005, Color.black);
			}
			*/
		gl.glPopMatrix();
	}
	
	public void renderBorders(GL gl, Double borderWidth, Double borderHeight, Color borderColor)
	{
		//gl.glColor3d(Math.random(), Math.random(), Math.random());
		
		gl.glColor3d(borderColor.getRed() / 255.0, borderColor.getGreen() / 255.0, borderColor.getBlue() / 255.0);
		
		gl.glPushMatrix();
			//gl.glTranslated(0, 0.5, 0);
			gl.glTranslated(0, 0, this.sizeZ/2.0);
			//gl.glScaled(sizeX, 0.3, 0.2);
			gl.glScaled(sizeX + borderWidth, borderHeight, borderWidth);
			gl.glTranslated(0, 0.5, 0);
			this.topBorder.render(gl);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		//gl.glTranslated(0, 0.5, 0);
		//gl.glColor3d(Math.random(), Math.random(), Math.random());
			gl.glTranslated(0, 0, -this.sizeZ/2.0);
			//gl.glScaled(sizeX, 0.3, 0.2);
			gl.glScaled(sizeX + borderWidth, borderHeight, borderWidth);
			gl.glTranslated(0, 0.5, 0);
			this.bottomBorder.render(gl);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		//gl.glTranslated(0, 0.5, 0);
		//gl.glColor3d(Math.random(), Math.random(), Math.random());
			gl.glTranslated(-this.sizeX/2.0, 0, 0);
			//gl.glScaled(0.2, 0.3, sizeZ);
			gl.glScaled(borderWidth, borderHeight, sizeZ);
			gl.glTranslated(0, 0.5, 0);
			this.leftBorder.render(gl);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		//gl.glTranslated(0, 0.5, 0);
		//gl.glColor3d(Math.random(), Math.random(), Math.random());
			gl.glTranslated(this.sizeX/2.0, 0, 0);
			//gl.glScaled(0.2, 0.3, sizeZ);
			gl.glScaled(borderWidth, borderHeight, sizeZ);
			gl.glTranslated(0, 0.5, 0);
			this.rightBorder.render(gl);
		gl.glPopMatrix();
	}

	public void createPseudoPackage()
	{
		int maxX =0;
		int maxZ = 0;
		for (ElementRepresentation elem : this.elements.values())
		{
			if (elem.posX > maxX)
				maxX = (int)elem.posX;
			if (elem.posZ > maxZ)
				maxZ = (int)elem.posZ;
		}
		this.pseudoPackage = new TreemapPackageRepresentation(new Package(this.packagedef.getName()));
		this.pseudoPackage.setSizeX(maxX+1);
		this.pseudoPackage.setSizeZ(maxZ+1);
		for (PackageRepresentation pack : this.packages.values())
		{
			((TreemapPackageRepresentation)pack).createPseudoPackage();
		}
	}

	public int compareTo(TreemapPackageRepresentation arg0) {
		return this.countDescendantClasses() - arg0.countDescendantClasses();
	}
	
	
	public static TreemapPackageRepresentation convertToTreemapPackRep(PackageRepresentation packRep) {
		TreemapPackageRepresentation treemapPackRep = new TreemapPackageRepresentation(packRep.getPackage());
		
		for (ElementRepresentation elementRep : packRep.getElements()) {
			treemapPackRep.addElement(elementRep);
		}
		
		for (PackageRepresentation childPackRep: packRep.getPackages()) {
			treemapPackRep.addPackage(TreemapPackageRepresentation.convertToTreemapPackRep(childPackRep));
		}
		
		return treemapPackRep;
	}
	
}

package verso.representation.cubeLandscape.representationModel.radial;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.media.opengl.GL;

import verso.graphics.VersoScene;
import verso.graphics.primitives.Arc;
import verso.graphics.primitives.Primitive;
import verso.model.Package;
import verso.representation.Renderable;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;

public class RadialPackageRepresentation extends PackageRepresentation implements Renderable, Comparable<RadialPackageRepresentation> {	
	
	public static float BORDERS_SIZE = 0.25f;

	protected Arc packageMesh;
	protected Arc topArc;
	protected Arc bottomArc;
	protected Primitive leftSideMesh;
	protected Primitive rightSideMesh;
	protected float bordersSize;
	protected Color packageMeshColor;
	protected float angleFirstSide;
	protected float angleSecondSide;
	protected float internRadius;
	protected float externRadius;
	protected float layoutInternRadius;
	protected Vector<Double> center;
	
	public HashMap<ElementRepresentation, Float> elementsAngles;

	
	protected RadialPackageRepresentation elementsPackage;
	protected double elementsPackageAngle;
	
	public RadialPackageRepresentation(Package packageDef, Primitive leftSideMesh, Primitive rightSideMesh, float bordersSize, Color packageMeshColor, Color bordersColor, float angleFirstSide, float angleSecondSide, float internRadius, float externRadius, float layoutInternRadius, Vector<Double> center, RadialPackageRepresentation elementsPackage) {
		super(packageDef);

		this.leftSideMesh = leftSideMesh;
		this.rightSideMesh = rightSideMesh;
		this.bordersSize = bordersSize;
		this.packageMeshColor = packageMeshColor;
		//this.bordersColor = bordersColor;
		this.angleFirstSide = angleFirstSide;
		this.angleSecondSide = angleSecondSide;
		this.internRadius = internRadius;
		this.externRadius = externRadius;
		this.layoutInternRadius = layoutInternRadius;
		this.center = center;
		
		this.elementsPackage = elementsPackage;
		
		this.elementsPackageAngle = 0.0;
		if (this.elementsPackage != null) {
			this.elementsPackageAngle = this.elementsPackage.angleSecondSide - this.elementsPackage.angleFirstSide;
		}
		
		this.topArc = new Arc(this.bordersSize, this.angleSecondSide - this.angleFirstSide, this.externRadius - this.bordersSize/2.0, this.externRadius + this.bordersSize/2.0, 50);
		this.bottomArc = new Arc(this.bordersSize, this.angleSecondSide - this.angleFirstSide - this.elementsPackageAngle, this.internRadius - this.bordersSize/2.0, this.internRadius + this.bordersSize/2.0, 50);
		
		
//		double bordersAngle = MathGeometry.arcAngle(this.layoutInternRadius, bordersSize);
		this.packageMesh = new Arc(this.height, this.angleSecondSide - this.angleFirstSide, this.internRadius + this.bordersSize, this.externRadius, 100);
		
		
		this.elementsAngles = new HashMap<ElementRepresentation, Float>();
	}

		
	public RadialPackageRepresentation getPackage(String packageName) {
		return (RadialPackageRepresentation) this.packages.get(packageName);
	}

	public Primitive getLeftSideMesh() {
		return leftSideMesh;
	}

	public void setLeftSideMesh(Primitive leftSideMesh) {
		this.leftSideMesh = leftSideMesh;
	}

	public Primitive getRightSideMesh() {
		return rightSideMesh;
	}

	public void setRightSideMesh(Primitive rightSideMesh) {
		this.rightSideMesh = rightSideMesh;
	}

	public float getBordersSize() {
		return this.bordersSize;
	}

	public void setBordersSize(float bordersSize) {
		this.bordersSize = bordersSize;
		
		this.topArc.setArcHeight(this.bordersSize);
		this.topArc.setInternRadius(this.externRadius);
		this.topArc.setExternRadius(this.externRadius + this.bordersSize);
		
		this.bottomArc.setArcHeight(this.bordersSize);
		this.bottomArc.setInternRadius(this.internRadius);
		this.bottomArc.setExternRadius(this.internRadius + this.bordersSize);
		
		this.updatePackageMesh();
	}

	public Color getPackageMeshColor() {
		return this.packageMeshColor;
	}

	public void setPackageMeshColor(Color packageMeshColor) {
		this.packageMeshColor = packageMeshColor;
	}

	public Color getBordersColor() {
		return this.bordersColor;
	}
	
	public void setBordersColor(Color bordersColor) {
		this.bordersColor = bordersColor;
	}
	
	public float getAngleFirstSide() {
		return angleFirstSide;
	}

	public void setAngleFirstSide(float angleFirstSide) {
		this.angleFirstSide = angleFirstSide;
		this.topArc.setAngleInDegree(this.angleSecondSide - this.angleFirstSide);
		this.bottomArc.setAngleInDegree(this.angleSecondSide - this.angleFirstSide - this.elementsPackageAngle);
		
//		double bordersAngle = MathGeometry.arcAngle(this.layoutInternRadius, bordersSize);
		this.packageMesh.setAngleInDegree(this.angleSecondSide - this.angleFirstSide /*- (bordersAngle * 2)*/);
	}

	public float getAngleSecondSide() {
		return angleSecondSide;
	}

	public void setAngleSecondSide(float angleSecondSide) {
		this.angleSecondSide = angleSecondSide;
		this.topArc.setAngleInDegree(this.angleSecondSide - this.angleFirstSide);
		this.bottomArc.setAngleInDegree(this.angleSecondSide - this.angleFirstSide);
		
//		double bordersAngle = MathGeometry.arcAngle(this.layoutInternRadius, bordersSize);
		this.packageMesh.setAngleInDegree(this.angleSecondSide - this.angleFirstSide /*- (bordersAngle * 2)*/);
	}

	public Vector<Double> getCenter() {
		return center;
	}

	public void setCenter(Vector<Double> center) {
		this.center = center;
		
		for (RadialPackageRepresentation pack : this.getRadialPackages()) {
			pack.setCenter(this.center);
		}
	}

	public float getInternRadius() {
		return internRadius;
	}

	public void setInternRadius(float internRadius) {
		this.internRadius = internRadius;
		
		this.bottomArc.setInternRadius(this.internRadius);
		this.bottomArc.setExternRadius(this.internRadius + this.bordersSize);
		
		
		this.packageMesh.setInternRadius(this.internRadius + this.bordersSize);
	}

	public float getExternRadius() {
		return externRadius;
	}

	public void setExternRadius(float externRadius) {
		this.externRadius = externRadius;
		
		this.topArc.setInternRadius(this.externRadius);
		this.topArc.setExternRadius(this.externRadius + this.bordersSize);
		
		this.packageMesh.setExternRadius(this.externRadius);
	}
	
	public float getLayoutInternRadius() {
		return layoutInternRadius;
	}

	public void setLayoutInternRadius(float layoutInternRadius) {
		this.layoutInternRadius = layoutInternRadius;
		
		//this.packageMesh.setInternRadius(this.layoutInternRadius);
	}

	public void setHeight(float height)
	{
		super.setHeight(height);
		this.packageMesh.setArcHeight(height);
	}
	
	
	public RadialPackageRepresentation findPackage(String pacName)
	{
		PackageRepresentation pac = null;
		if (this.packages.containsKey(pacName))
		{
			return (RadialPackageRepresentation)this.packages.get(pacName);
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
		return (RadialPackageRepresentation)pac;
	}
	
	public Collection<RadialPackageRepresentation> getRadialPackages()
	{
		LinkedList<RadialPackageRepresentation> radialPackages = new LinkedList<RadialPackageRepresentation>();
		for (PackageRepresentation pac : super.packages.values()) {
			radialPackages.add((RadialPackageRepresentation)pac);
		}
		
		return radialPackages;
	}
	
	
	
	
	
	public void setElementsPackage(RadialPackageRepresentation elementsPackage) {
		this.elementsPackage = elementsPackage;
		
		this.elementsPackageAngle = 0.0;
		if (this.elementsPackage != null) {
			this.elementsPackageAngle = this.elementsPackage.angleSecondSide - this.elementsPackage.angleFirstSide;
		}
		
		this.bottomArc.setAngleInDegree(this.angleSecondSide - this.angleFirstSide - this.elementsPackageAngle);
	}
	
	
	/*
	public void updateBottomArc(double degree) {
		this.bottomArc.setAngleInDegree(degree);
	}
	*/
	
	
	
	public RadialPackageRepresentation getElementsPackage() {
		return this.elementsPackage;
	}
	
	
	public Collection<ElementRepresentation> getElements()
	{	
		if (this.elementsPackage != null) {
			return this.elementsPackage.getElements();
		}
		else {
			return this.elements.values();
		}
	}
	
	
	
	
	
	
	
	public int compareTo(RadialPackageRepresentation arg0) {
		return this.countDescendantClasses() - arg0.countDescendantClasses();
	}
	
	
	public void updatePackageMesh() {
//		double bordersAngle = MathGeometry.arcAngle(this.layoutInternRadius, bordersSize);
		this.packageMesh.setAngleInDegree(this.angleSecondSide - this.angleFirstSide /*- (bordersAngle * 2)*/);
		
		this.packageMesh.setInternRadius(this.internRadius + this.bordersSize);
		this.packageMesh.setExternRadius(this.externRadius);
	}
	
	
	/*
	public RadialPackageRepresentation copy() {
		RadialPackageRepresentation(Package packageDef, Primitive leftSideMesh, Primitive rightSideMesh, double bordersSize, Color packageMeshColor, Color bordersColor, double angleFirstSide, double angleSecondSide, double internRadius, double externRadius, double layoutInternRadius, Vector<Double> center, RadialPackageRepresentation elementsPackage) {
		RadialPackageRepresentation packageCopy = new RadialPackageRepresentation(this.packagedef, this.leftSideMesh, this.rightSideMesh. );
		
		for (PackageRepresentation subPack : this.packages.values()) {
			packageCopy.addPackage(subPack.copy());
		}
		
		return packageCopy;
	} 
	*/
	
	
	
	public void render(GL gl) {		
		
		float heightPackage = this.getHeight();
		
		//System.out.println("RENDER RADIAL PACKAGE REPRESENTATION");
		
		if (hideFilteredClasses && (!this.isFiltered() && SystemRepresentation.filterState) && !this.fakePackage && !this.anyChildIsSelected())
			return;
		
		if (!this.render) {
			gl.glPushMatrix();
				gl.glTranslated(0.0, heightPackage, 0.0);
								
				for (PackageRepresentation pack : this.getPackages()) {
					pack.render(gl);
				}
				
				
				
				
				/*
				for (ElementRepresentation elementRep : this.getElements()) {
					elementRep.render(gl);
				}
				*/
				
				if (this.getElementsPackage() != null) {
					for (ElementRepresentation elementRep : this.getElementsPackage().getElements()) {
						elementRep.render(gl);
					}
				}
				
				gl.glTranslated(0.0, -heightPackage, 0.0);
			gl.glPopMatrix();
		}

		/*
		if (this.elementsPackage != null) {
			this.elementsPackage.render(gl);
		}
		*/
		
		gl.glLoadName(VersoScene.id);
		VersoScene.pickingEntities.put(VersoScene.id++, this);
		
		
		gl.glTranslated(this.center.get(0), this.center.get(1), this.center.get(2));
		
		
		if (this.render) {
			//float tempAngleSecondSide = this.angleSecondSide;
			//this.setAngleSecondSide(this.getAngleSecondSide() - MathGeometry.arcAngle(this.internRadius, this.bordersSize));
			
			gl.glColor3f(this.color.getRed()/255.0f, this.color.getGreen()/255.0f, this.color.getBlue()/255.0f);
			gl.glPushMatrix();
				gl.glRotated(-this.angleFirstSide /*- MathGeometry.arcAngle(this.internRadius, this.bordersSize)/2*/, 0, 1, 0);
				this.packageMesh.render(gl);
			gl.glPopMatrix();
			
			//this.setAngleSecondSide(tempAngleSecondSide);
		}
		else {
			switch (orientationColor) {
			case FIRSTCOLOR:
				gl.glColor3f(0.6f, 0.6f, 0.6f);
				break;
			case SECONDCOLOR:
				gl.glColor3f(1.0f, 1.0f, 1.0f);
				break;

			default:
				break;
			}
			
			gl.glPushMatrix();
				gl.glRotated(-this.angleFirstSide, 0, 1, 0);
				
				gl.glScaled(1.0, /*0.1*/ heightPackage, 1.0);

				this.packageMesh.render(gl);
			gl.glPopMatrix();
		}
		


		
		
		
		
		gl.glPushMatrix();
			float epsilon;
			Color currBordersColor;
			
			if (this.isSelected) {
				epsilon = 0.1f;
				currBordersColor = this.bordersColor;
			}
			else {
				epsilon = 0.0f;
				currBordersColor = Color.black;
			}
		
			if (this.isSelected) {
				gl.glPushMatrix();
					gl.glColor3f(currBordersColor.getRed()/255.0f, currBordersColor.getGreen()/255.0f, currBordersColor.getBlue()/255.0f);
					gl.glRotated(-this.angleFirstSide, 0, 1, 0);
					
					this.topArc.setArcHeight(this.bordersSize + epsilon);
					//this.topArc.setInternRadius(this.externRadius - epsilon);
					this.topArc.setExternRadius(this.externRadius + this.bordersSize + epsilon);
					
					this.bottomArc.setArcHeight(this.bordersSize + epsilon);
					this.bottomArc.setInternRadius(this.internRadius - epsilon);
					//this.bottomArc.setExternRadius(this.internRadius + this.bordersSize + epsilon);
					
					
					this.topArc.render(gl);
					this.bottomArc.render(gl);
					
					
					this.topArc.setArcHeight(this.bordersSize);
					//this.topArc.setInternRadius(this.externRadius);
					this.topArc.setExternRadius(this.externRadius + this.bordersSize);
					
					this.bottomArc.setArcHeight(this.bordersSize);
					this.bottomArc.setInternRadius(this.internRadius);
					//this.bottomArc.setExternRadius(this.internRadius + this.bordersSize);
				gl.glPopMatrix();
				
				
	
				
				//Afficher les bordures du côtéss
				if ((this.angleSecondSide - this.angleFirstSide) % 360 != 0) {
					double sidesLength = this.externRadius - this.layoutInternRadius + this.bordersSize + 2*epsilon; //this.internRadius;
//					float bordersSizeAngle = MathGeometry.arcAngle(this.getInternRadius(), this.bordersSize - epsilon);
					
					if (this.rightSideMesh != null) {						
						gl.glPushMatrix();
							gl.glColor3f(currBordersColor.getRed()/255.0f, currBordersColor.getGreen()/255.0f, currBordersColor.getBlue()/255.0f);
							gl.glTranslated(0.0, (this.bordersSize + epsilon)/2.0, 0.0);
							gl.glRotated(-this.angleFirstSide /*+ bordersSizeAngle*/, 0, 1, 0);
							gl.glTranslated(this.layoutInternRadius - epsilon, 0, 0);
							
							gl.glRotated(-90, 0, 0, 1);
							gl.glTranslated(0, sidesLength/2, 0);
							gl.glScaled(this.bordersSize + epsilon, sidesLength, this.bordersSize + epsilon);
							this.rightSideMesh.render(gl);
						gl.glPopMatrix();
					}
			
					if (this.leftSideMesh != null) {	
						gl.glPushMatrix();
							gl.glColor3f(currBordersColor.getRed()/255.0f, currBordersColor.getGreen()/255.0f, currBordersColor.getBlue()/255.0f);
							gl.glTranslated(0.0, (this.bordersSize + epsilon)/2.0, 0.0);
							gl.glRotated(-this.angleSecondSide /*- bordersSizeAngle*/, 0, 1, 0);
							gl.glTranslated(this.layoutInternRadius - epsilon, 0, 0);
							
							gl.glRotated(-90, 0, 0, 1);
							gl.glTranslated(0, sidesLength/2, 0);
	
							gl.glScaled(this.bordersSize + epsilon, sidesLength, this.bordersSize + epsilon);
							this.leftSideMesh.render(gl);
						gl.glPopMatrix();
					}
				}	
			}
			//*******************************************
			
			
			
			
		gl.glPopMatrix();
	}
	
	public static RadialPackageRepresentation convertToRadialPackRep(PackageRepresentation packRep, Primitive leftSideMesh, Primitive rightSideMesh, float meshesSize, Color packageMeshColor, Color bordersColor, float layoutInternRadius, Vector<Double> center) {
		RadialPackageRepresentation radialPackRep = new RadialPackageRepresentation(packRep.getPackage(), leftSideMesh, rightSideMesh, meshesSize, packageMeshColor, bordersColor, 0.0f, 0.0f, 0.0f, 0.0f, layoutInternRadius, center, null);
		
		for (ElementRepresentation elementRep : packRep.getElements()) {
			radialPackRep.addElement(elementRep);
		}
		
		for (PackageRepresentation childPackRep : packRep.getPackages()) {
			radialPackRep.addPackage(RadialPackageRepresentation.convertToRadialPackRep(childPackRep, leftSideMesh, rightSideMesh, meshesSize, packageMeshColor, bordersColor, layoutInternRadius, center));
		}
		
		return radialPackRep;
	}
}

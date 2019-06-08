package verso.representation.cubeLandscape.representationModel;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.media.opengl.GL;

import verso.graphics.VersoScene;
import verso.graphics.primitives.Primitive;
import verso.model.Package;
import verso.representation.cubeLandscape.representationModel.radial.RadialPackageRepresentation;

public class ColiseumPackageRepresentation extends RadialPackageRepresentation {
	
	
	
	
	
	protected float internBorderRadius;
	
	
	
	protected ColiseumPackageRepresentation elementsPackage;
	
	
	
	public ColiseumPackageRepresentation(Package packageDef, Primitive leftSideMesh, Primitive rightSideMesh, float bordersSize, Color packageMeshColor, Color bordersColor, float angleFirstSide, float angleSecondSide, float internRadius, float externRadius, float internBorderRadius, float layoutInternRadius, Vector<Double> center, RadialPackageRepresentation elementsPackage) {
		super(packageDef, leftSideMesh, rightSideMesh, bordersSize, packageMeshColor, bordersColor, angleFirstSide, angleSecondSide, internRadius, externRadius, layoutInternRadius, center, elementsPackage);
	
		this.internBorderRadius = internBorderRadius;
	}
	
	public Collection<ColiseumPackageRepresentation> getColiseumPackages()
	{
		LinkedList<ColiseumPackageRepresentation> coliseumPackages = new LinkedList<ColiseumPackageRepresentation>();
		for (PackageRepresentation pac : super.packages.values()) {
			coliseumPackages.add((ColiseumPackageRepresentation)pac);
		}
		
		return coliseumPackages;
	}
	
	public float getInternBorderRadius() {
		return this.internBorderRadius;
	}
	
	public void setInternBorderRadius(float internBorderRadius) {
		this.internBorderRadius = internBorderRadius;
	}
	
	
	public void setAngleFirstSide(float angleFirstSide) {
		this.angleFirstSide = angleFirstSide;
		this.topArc.setAngleInDegree(this.angleSecondSide - this.angleFirstSide);
		this.bottomArc.setAngleInDegree(this.angleSecondSide - this.angleFirstSide);
		this.packageMesh.setAngleInDegree(this.angleSecondSide - this.angleFirstSide);
	}
	
	public void setAngleSecondSide(float angleSecondSide) {
		this.angleSecondSide = angleSecondSide;
		this.topArc.setAngleInDegree(this.angleSecondSide - this.angleFirstSide);
		this.bottomArc.setAngleInDegree(this.angleSecondSide - this.angleFirstSide);
		this.packageMesh.setAngleInDegree(this.angleSecondSide - this.angleFirstSide);
	}
	
	
	
	public ColiseumPackageRepresentation getElementsPackage() {
		return this.elementsPackage;
	}
	
	public void setElementsPackage(ColiseumPackageRepresentation elementsPackage) {
		this.elementsPackage = elementsPackage;
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
	
	public HashMap<ElementRepresentation, ColiseumPackageRepresentation> getAllElementsWithParentPackage()
	{
		HashMap<ElementRepresentation, ColiseumPackageRepresentation> elems = new HashMap<ElementRepresentation, ColiseumPackageRepresentation>();
		
		for (ElementRepresentation e : this.getElements()) {
			elems.put(e, this);
		}
		
		for (PackageRepresentation p : this.packages.values())
		{
			elems.putAll(((ColiseumPackageRepresentation)p).getAllElementsWithParentPackage());
		}
		return elems;
	}
	
	
	
	public static ColiseumPackageRepresentation convertToColiseumPackRep(PackageRepresentation packRep, Primitive leftSideMesh, Primitive rightSideMesh, float meshesSize, Color packageMeshColor, Color bordersColor, float layoutInternRadius, Vector<Double> center) {
		ColiseumPackageRepresentation coliseumPackRep = new ColiseumPackageRepresentation(packRep.getPackage(), leftSideMesh, rightSideMesh, meshesSize, packageMeshColor, bordersColor, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, layoutInternRadius, center, null);
		
		for (ElementRepresentation elementRep : packRep.getElements()) {
			coliseumPackRep.addElement(elementRep);
		}
		
		for (PackageRepresentation childPackRep : packRep.getPackages()) {
			coliseumPackRep.addPackage(ColiseumPackageRepresentation.convertToColiseumPackRep(childPackRep, leftSideMesh, rightSideMesh, meshesSize, packageMeshColor, bordersColor, layoutInternRadius, center));
		}
		
		return coliseumPackRep;
	}
	
	public void render(GL gl) {
		if (hideFilteredClasses && (!this.isFiltered() && SystemRepresentation.filterState) && !this.fakePackage && !this.anyChildIsSelected())
			return;
		
		if (!this.render) {
			//if (this.getElements().size() > 0) {
			if (this.getElementsPackage() != null) {
				gl.glPushMatrix();
					gl.glTranslated(0.0, this.getHeight(), 0.0);
					
					//for (ElementRepresentation elementRep : this.getElements()) {
					for (ElementRepresentation elementRep : this.getElementsPackage().getElements()) {
						gl.glPushMatrix();
							//if (this.elementsAngles.get(elementRep) != null) {
							if (this.getElementsPackage().elementsAngles.get(elementRep) != null) {
								//gl.glRotatef(-this.elementsAngles.get(elementRep), 0, 1, 0);
								gl.glRotatef(-this.getElementsPackage().elementsAngles.get(elementRep), 0, 1, 0);
							}
						
							elementRep.render(gl);
						gl.glPopMatrix();
					}
				gl.glPopMatrix();
			}
			
			for (PackageRepresentation pack : this.getPackages()) {
				pack.render(gl);
			}
			
		}
		
		gl.glLoadName(VersoScene.id);
		VersoScene.pickingEntities.put(VersoScene.id++, this);
		
		
		gl.glPushMatrix();
			gl.glTranslated(this.center.get(0), this.center.get(1), this.center.get(2));
			
			
			gl.glRotated(-this.angleFirstSide, 0, 1, 0);
			
			if (this.render) {
				gl.glColor3f(this.color.getRed()/255.0f, this.color.getGreen()/255.0f, this.color.getBlue()/255.0f);
				gl.glPushMatrix();
					this.packageMesh.render(gl);
				gl.glPopMatrix();
			}
			else {
				
//				if (this.getPackageLevel() % 2 == 0) {
//					gl.glColor3f(0.0f, 0.0f, 1.0f);
//				}
//				else {
//					gl.glColor3f(1.0f, 0.0f, 1.0f);
//				}
				
				float[] color = ORIENTATION_COLOR.FIRSTCOLOR.getColor();
				switch (orientationColor) {
				case FIRSTCOLOR:
					color = ORIENTATION_COLOR.FIRSTCOLOR.getColor();
					
					break;
				case SECONDCOLOR:
					color = ORIENTATION_COLOR.SECONDCOLOR.getColor();
					break;
				default:
					break;
				}
				System.out.println(this.getSimpleName());
//				if(this.getName().substring(, endIndex))
				
				gl.glColor3f(color[0],color[1],color[2]);
				gl.glPushMatrix();					
					this.packageMesh.setInternRadius(this.internBorderRadius);					
					this.packageMesh.render(gl);
					this.packageMesh.setInternRadius(this.internRadius);
				gl.glPopMatrix();
			}
		gl.glPopMatrix();
	}
}

package verso.representation.cubeLandscape.representationModel.radial;

import java.awt.Color;

import javax.media.opengl.GL;

import verso.graphics.primitives.CubeNoCap;
import verso.graphics.primitives.Primitive;
import verso.representation.Renderable;
import verso.representation.cubeLandscape.representationModel.EntityRepresentation;
import verso.representation.cubeLandscape.representationModel.TreemapPackageRepresentation;



public class PackageDelimiterRepresentation extends EntityRepresentation implements Renderable {
	private TreemapPackageRepresentation pack;
	private float delimiterHeight;
	private float posY;
	
	public PackageDelimiterRepresentation(TreemapPackageRepresentation pack, float posY, float delimiterHeight, Color color) {
		this.pack = pack;
		this.posY = posY;
		this.delimiterHeight = delimiterHeight;
		this.color = color;
	}
	
	public void render(GL gl) {
		Primitive cubeNoCapMesh = new CubeNoCap();
		float sizeEpsilon = 0.1f;
		float newSizeX = (float)pack.getSizeX()-sizeEpsilon;
		float newSizeZ = (float)pack.getSizeZ()-sizeEpsilon;
		
		gl.glPushMatrix();
			gl.glColor3f(color.getRed()/255.0f, color.getGreen()/255.0f, color.getBlue()/255.0f);
			gl.glTranslatef(pack.getAbsolutePosX()+sizeEpsilon/2, posY, pack.getAbsolutePosZ()+sizeEpsilon/2);
			
			//Ramène la base sur l'origine.
			gl.glTranslatef(newSizeX / 2, delimiterHeight / 2, newSizeZ / 2);
			
			gl.glScalef(newSizeX, delimiterHeight,newSizeZ);
			cubeNoCapMesh.render(gl);
		gl.glPopMatrix();
	}
}

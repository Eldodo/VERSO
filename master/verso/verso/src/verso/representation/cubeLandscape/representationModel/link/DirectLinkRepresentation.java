package verso.representation.cubeLandscape.representationModel.link;

import java.awt.Color;

import javax.media.opengl.GL;

import verso.util.MathGeometry;
import verso.graphics.VersoScene;
import verso.graphics.primitives.PrimitiveColored;
import verso.representation.Renderable;

public class DirectLinkRepresentation extends LinkRepresentation implements Renderable {
	private String linkName;
	
	
	public DirectLinkRepresentation(NodeRepresentation startNode, NodeRepresentation endNode, int nbreInLinks, int nbreOutLinks, PrimitiveColored coloredMesh, float meshSize, float lineSize, Color linkStartColor, Color linkEndColor, Boolean isBidirectional, Color linkBidirectionalColor, float linkSaturation) {
		super(startNode, endNode, nbreInLinks, nbreOutLinks, coloredMesh, meshSize, lineSize, linkStartColor, linkEndColor, isBidirectional, linkBidirectionalColor, linkSaturation);
	}
	
	
	
	public void setName(String linkName) {
		this.linkName = linkName;
	}
	
	public String getName() {
		return this.linkName;
	}
	
	
	/*
	public void setColor(Color linkColor) {
		this.linkStartColor = linkColor;
		this.linkEndColor = linkColor;
	}
	*/
	
	
	public void render(GL gl) {
		if (this.isFiltered) {
			return;
		}
		
		gl.glLoadName(VersoScene.id);
		VersoScene.pickingEntities.put(VersoScene.id++, this);
		
		if (this.coloredMesh == null) {
			/*
			gl.glEnable(gl.GL_LINE_SMOOTH);
			gl.glEnable(gl.GL_BLEND);
			gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
			gl.glHint(gl.GL_LINE_SMOOTH_HINT, gl.GL_NICEST);
			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			*/
			
			gl.glLineWidth((float)(this.lineSize));
			gl.glBegin(GL.GL_LINES);
			
			if (this.isBidirectional) {
				gl.glColor3f(linkBidirectionalColor.getRed()/255.0f, linkBidirectionalColor.getGreen()/255.0f, linkBidirectionalColor.getBlue()/255.0f);
			}
			else {
				gl.glColor3f(linkStartColor.getRed()/255.0f, linkStartColor.getGreen()/255.0f, linkStartColor.getBlue()/255.0f);
			}
			
			gl.glVertex3f((float)startNode.getposXd(), (float)startNode.getposYd(), (float)startNode.getposZd());
			
			if (this.isBidirectional) {
				gl.glColor3f(linkBidirectionalColor.getRed()/255.0f, linkBidirectionalColor.getGreen()/255.0f, linkBidirectionalColor.getBlue()/255.0f);
			}
			else {
				gl.glColor3f(linkEndColor.getRed()/255.0f, linkEndColor.getGreen()/255.0f, linkEndColor.getBlue()/255.0f);
			}
			
			gl.glVertex3f((float)endNode.getposXd(), (float)endNode.getposYd(), (float)endNode.getposZd());
			gl.glEnd();
		}
		else {
			//Il faut que la mesh soit placé dans la position qui va permettre de la transformer en la forme qu'on désir, en
			//utilisant le même algorithme de rotation (par ex.: placer la mesh avec la base sur le plan XZ, le centre sur
			//l'origine, orienté vers le "haut").
			
			float lengthX = endNode.getposXd() - startNode.getposXd();
			float lengthY = endNode.getposYd() - startNode.getposYd();
			float lengthZ = endNode.getposZd() - startNode.getposZd();
			float linkLength = (float)Math.sqrt(lengthX*lengthX + lengthY*lengthY + lengthZ*lengthZ);
			//NodeRepresentation newEndNode = new NodeRepresentation(null,null, 0, lengthX, lengthY, lengthZ, null, null);
			float[] rotationParams;
			
			gl.glPushMatrix();
				gl.glTranslated(startNode.getposXd(), startNode.getposYd(), startNode.getposZd());
				
				rotationParams = MathGeometry.getRotationParams(lengthX, lengthY, lengthZ);
				
				if (rotationParams != null) {
					gl.glRotated(rotationParams[0], rotationParams[1], rotationParams[2], rotationParams[3]);
				}
				
				gl.glTranslated(0, linkLength/2, 0);
				gl.glScaled(meshSize, linkLength, meshSize);
				
				if (this.isBidirectional) {
					this.coloredMesh.setBaseColor(this.linkBidirectionalColor);
					this.coloredMesh.setTopColor(this.linkBidirectionalColor);	
				} 
				else {
					this.coloredMesh.setBaseColor(linkStartColor);
					this.coloredMesh.setTopColor(linkEndColor);			
				}
				
				this.coloredMesh.render(gl);
			gl.glPopMatrix();	
		}
	}
	
	
	public DirectLinkRepresentation copyLink() {
		return new DirectLinkRepresentation(this.startNode, this.endNode, this.nbreInLinks, this.nbreOutLinks, this.coloredMesh, this.meshSize, this.lineSize, this.linkStartColor, this.linkEndColor, this.isBidirectional, this.linkBidirectionalColor, this.linkSaturation);
	}
}

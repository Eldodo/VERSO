package verso.graphics.primitives;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;

import javax.media.opengl.GL;

import verso.representation.Renderable;

public class CylinderNoCapColored extends PrimitiveColored implements Renderable {
	private Color baseColor;
	private Color topColor;
	private int nbreSide;
	private double sideAngle;
	private double radius;
	private double height;
	private LinkedList<double[]> topVertices;
	private LinkedList<double[]> baseVertices;
	private LinkedList<double[]> facesNormals;
	
	public CylinderNoCapColored(Color baseColor, Color topColor, int nbreSide) {
		this.baseColor = baseColor;
		this.topColor = topColor;
		this.nbreSide = nbreSide;
		this.sideAngle = 360.0 / nbreSide;
		this.radius = 0.5;
		this.height = 1.0;
		this.topVertices = new LinkedList<double[]>();
		this.baseVertices = new LinkedList<double[]>();
		this.facesNormals = new LinkedList<double[]>();
		updateVerticesAndNormals();
	}
	
	public Color getBaseColor() {
		return this.baseColor;
	}
	
	public void setBaseColor(Color baseColor) {
		this.baseColor = baseColor;
	}
	
	public Color getTopColor() {
		return this.topColor;
	}
	
	public void setTopColor(Color topColor) {
		this.topColor = topColor;
	}
	
	public int getNbreSide() {
		return nbreSide;
	}
	
	public void setNbreSide(int nbreSide) {
		this.nbreSide = nbreSide;
		this.sideAngle = 360.0 / nbreSide;
		updateVerticesAndNormals();
	}

	public String getName()
	{
		return "CylinderNoCapColored";
	}
	
	public void render(GL gl)
	{
		if (nbreSide >= 3) {
			Iterator<double[]> baseVerticesItr = this.baseVertices.iterator();
			Iterator<double[]> topVerticesItr = this.topVertices.iterator();
			Iterator<double[]> facesNormalsItr = this.facesNormals.iterator();
			
			double[] currBaseVertex = baseVerticesItr.next();
			double[] currTopVertex = topVerticesItr.next();
			double[] nextBaseVertex;
			double[] nextTopVertex;
			double[] currFaceNormal;
			
			while (baseVerticesItr.hasNext() && topVerticesItr.hasNext() && facesNormalsItr.hasNext()) {
				nextBaseVertex = baseVerticesItr.next();
				nextTopVertex = topVerticesItr.next();
				currFaceNormal = facesNormalsItr.next();
				
				gl.glPushMatrix();
					gl.glBegin(GL.GL_QUADS); 
						gl.glNormal3f((float)currFaceNormal[0], (float)currFaceNormal[1], (float)currFaceNormal[2]);
						gl.glColor3f(baseColor.getRed()/255.0f, baseColor.getGreen()/255.0f, baseColor.getBlue()/255.0f);
						gl.glVertex3f((float)nextBaseVertex[0], (float)nextBaseVertex[1], (float)nextBaseVertex[2]);
						gl.glVertex3f((float)currBaseVertex[0], (float)currBaseVertex[1], (float)currBaseVertex[2]);
						gl.glColor3f(topColor.getRed()/255.0f, topColor.getGreen()/255.0f, topColor.getBlue()/255.0f);
						gl.glVertex3f((float)currTopVertex[0], (float)currTopVertex[1], (float)currTopVertex[2]);
						gl.glVertex3f((float)nextTopVertex[0], (float)nextTopVertex[1], (float)nextTopVertex[2]);
					gl.glEnd();
				gl.glPopMatrix();
				
				currBaseVertex = nextBaseVertex;
				currTopVertex = nextTopVertex;
			}
		}
	}
	
	public String getSimpleName() {
		return getName();
	}

	private void updateVerticesAndNormals() {
		double[] currBaseVertexCoord = {this.radius*Math.cos(0), -(this.height / 2), this.radius*Math.sin(0)};
		double[] currTopVertexCoord = {currBaseVertexCoord[0], this.height / 2, currBaseVertexCoord[2]};
		double[] nextBaseVertexCoord;
		double[] nextTopVertexCoord;
		double[] u = new double[3];
		double[] v = new double[3];
		
		this.topVertices.clear();
		this.baseVertices.clear();
		this.facesNormals.clear();
		
		this.baseVertices.add(currBaseVertexCoord);
		this.topVertices.add(currTopVertexCoord);
		
		for (int i=0; i<=nbreSide; i++) {
			nextBaseVertexCoord = new double[3];
			nextBaseVertexCoord[0] = this.radius*Math.cos(Math.toRadians(i*this.sideAngle));
			nextBaseVertexCoord[1] = -(this.height / 2);
			nextBaseVertexCoord[2] = this.radius*Math.sin(Math.toRadians(i*this.sideAngle));
			this.baseVertices.add(nextBaseVertexCoord);
			
			nextTopVertexCoord = new double[3];
			nextTopVertexCoord[0] = nextBaseVertexCoord[0];
			nextTopVertexCoord[1] = this.height / 2; 
			nextTopVertexCoord[2] = nextBaseVertexCoord[2];
			this.topVertices.add(nextTopVertexCoord);

			u[0] = currTopVertexCoord[0] - nextTopVertexCoord[0];
			u[1] = currTopVertexCoord[1] - nextTopVertexCoord[1];
			u[2] = currTopVertexCoord[2] - nextTopVertexCoord[2];
			
			v[0] = nextTopVertexCoord[0] - nextBaseVertexCoord[0];
			v[1] = nextTopVertexCoord[1] - nextBaseVertexCoord[1];
			v[2] = nextTopVertexCoord[2] - nextBaseVertexCoord[2];
			
			double[] uXv = {u[1]*v[2] - u[2]*v[1], u[2]*v[0] - u[0]*v[2], u[0]*v[1]-u[1]*v[0]};
			this.facesNormals.add(uXv);
			
			currBaseVertexCoord = nextBaseVertexCoord;
			currTopVertexCoord = nextTopVertexCoord;
		}	
	}
}

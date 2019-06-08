package verso.graphics.primitives;

import java.util.Iterator;
import java.util.LinkedList;

import javax.media.opengl.GL;

import verso.representation.Renderable;

public class Arc extends Primitive implements Renderable {
	private double arcHeight;
	private double angleInDegree;
	private double internRadius;
	private double externRadius;
	private int nbrePoints;

	
	public Arc(double arcHeight, double angleInDegree, double internRadius, double externRadius, int nbrePoints) {
		this.arcHeight = arcHeight;
		this.angleInDegree = angleInDegree;
		this.internRadius = internRadius;
		this.externRadius = externRadius;
		this.nbrePoints = nbrePoints;
	}

	public double getArcHeight() {
		return this.arcHeight;
	}

	public void setArcHeight(double arcHeight) {
		this.arcHeight = arcHeight;
	}
	
	public double getAngleInDegree() {
		return this.angleInDegree;
	}

	public void setAngleInDegree(double angleInDegree) {
		this.angleInDegree = angleInDegree;
	}

	public double getInternRadius() {
		return this.internRadius;
	}

	public void setInternRadius(double internRadius) {
		this.internRadius = internRadius;
	}

	public double getExternRadius() {
		return this.externRadius;
	}
	
	public void setExternRadius(double externRadius) {
		this.externRadius = externRadius;
	}
	
	public int getNbrePoints() {
		return this.nbrePoints;
	}

	public void setNbrePoints(int nbrePoints) {
		this.nbrePoints = nbrePoints;
	}

	public String getName() {
		return "Arc";
	}
	
	public String getSimpleName() {
		return this.getName();
	}
	
	public void render(GL gl) {
		LinkedList<double[]> internPoints = evaluatePointsOnArc(this.angleInDegree, this.internRadius, this.nbrePoints);
		LinkedList<double[]> externPoints = evaluatePointsOnArc(this.angleInDegree, this.externRadius, this.nbrePoints);
		Iterator<double[]> internPointsItr = internPoints.iterator();
		Iterator<double[]> externPointsItr = externPoints.iterator();
		double[] currInternPoint;
		double[] currExternPoint;
		boolean internPointNext;
		
		double[] v;
		double[] rotationParams;
		double segmentLength;
		
		//currPoint = pointsItr.next();
		
		//while (pointsItr.hasNext()) {
			gl.glPushMatrix();
				//nextPoint = pointsItr.next();
				
				currInternPoint = internPointsItr.next();				
				currExternPoint = externPointsItr.next();
				
				//gl.glColor3d(this, arg1, arg2)
				gl.glNormal3d(0.0, 1.0, 0.0);
				//gl.glColor3d(255.0, 0.0, 0.0);
				gl.glBegin(GL.GL_TRIANGLE_STRIP);
					gl.glVertex3d(currExternPoint[0], currExternPoint[1] + this.arcHeight, currExternPoint[2]);
					gl.glVertex3d(currInternPoint[0], currInternPoint[1] + this.arcHeight, currInternPoint[2]);
					
					internPointNext = false;
					while (internPointsItr.hasNext()) {
						if (internPointNext) {
							currInternPoint = internPointsItr.next();
							gl.glVertex3d(currInternPoint[0], currInternPoint[1] + this.arcHeight, currInternPoint[2]);
							internPointNext = false;
						}
						else {
							currExternPoint = externPointsItr.next();
							gl.glVertex3d(currExternPoint[0], currExternPoint[1] + this.arcHeight, currExternPoint[2]);
							internPointNext = true;
						}
					}
				gl.glEnd();
				
				
				internPointsItr = internPoints.iterator();
				//gl.glColor3d(0.0, 255.0, 0.0);
				gl.glBegin(GL.GL_TRIANGLE_STRIP);
					while (internPointsItr.hasNext()) {
						currInternPoint = internPointsItr.next();
						gl.glNormal3d(-currInternPoint[0], 0.0, -currInternPoint[2]);
						gl.glVertex3d(currInternPoint[0], currInternPoint[1] + this.arcHeight, currInternPoint[2]);
						gl.glVertex3d(currInternPoint[0], currInternPoint[1], currInternPoint[2]);
					}
				gl.glEnd();
						
				
				externPointsItr = externPoints.iterator();
				//gl.glColor3d(0.0, 0.0, 255.0);
				gl.glBegin(GL.GL_TRIANGLE_STRIP);
					while (externPointsItr.hasNext()) {
						currExternPoint = externPointsItr.next();
						gl.glNormal3d(currExternPoint[0], 0.0, currExternPoint[2]);
						gl.glVertex3d(currExternPoint[0], currExternPoint[1], currExternPoint[2]);
						gl.glVertex3d(currExternPoint[0], currExternPoint[1] + this.arcHeight, currExternPoint[2]);
					}
				gl.glEnd();
				
				
				gl.glNormal3d(0.0, 0.0, 1.0);
				//gl.glColor3d(100.0, 100.0, 0.0);
				gl.glBegin(GL.GL_QUADS);
					currInternPoint = internPoints.getFirst();
					gl.glVertex3d(currInternPoint[0], currInternPoint[1], currInternPoint[2]);
					gl.glVertex3d(currInternPoint[0], currInternPoint[1] + this.arcHeight, currInternPoint[2]);
					
					currExternPoint = externPoints.getFirst();
					gl.glVertex3d(currExternPoint[0], currExternPoint[1] + this.arcHeight, currExternPoint[2]);
					gl.glVertex3d(currExternPoint[0], currExternPoint[1], currExternPoint[2]);
				gl.glEnd();
				
				
				//gl.glColor3d(0.0, 100.0, 100.0);
				gl.glBegin(GL.GL_QUADS);
					currInternPoint = internPoints.getLast();
					gl.glVertex3d(currInternPoint[0], currInternPoint[1] + this.arcHeight, currInternPoint[2]);
					gl.glVertex3d(currInternPoint[0], currInternPoint[1], currInternPoint[2]);
					
					currExternPoint = externPoints.getLast();
					gl.glVertex3d(currExternPoint[0], currExternPoint[1], currExternPoint[2]);
					gl.glVertex3d(currExternPoint[0], currExternPoint[1] + this.arcHeight, currExternPoint[2]);
				gl.glEnd();
				
				/*
				gl.glTranslated(0.0, (double)this.meshHeight/2.0, 0.0);
				//gl.glTranslated((double)this.meshHeight/2.0, 0.0, (double)this.meshWidth/2.0);
				
				//NodeRepresentation startNode = new NodeRepresentation(null, 0.0, currPoint[0], currPoint[1], currPoint[2], null, null);
				//NodeRepresentation endNode = new NodeRepresentation(null, 0.0, nextPoint[0], nextPoint[1], nextPoint[2], null, null);
				
				
				v = new double[3];
				v[0] = nextPoint[0] - currPoint[0];
				v[1] = nextPoint[1] - currPoint[1];
				v[2] = nextPoint[2] - currPoint[2];
				rotationParams = MathGeometry.getRotationParams(v[0], v[1], v[2]);
				segmentLength = Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
				
				
				gl.glTranslated(currPoint[0], currPoint[1], currPoint[2]);
				
				if (rotationParams != null) {
					gl.glRotated(rotationParams[0], rotationParams[1], rotationParams[2], rotationParams[3]);
				}
				
				gl.glTranslated(0.0, segmentLength/2, 0.0);
	
				double angleVector = Math.acos(v[0]/segmentLength);
				if (v[2] > 0) {
					angleVector *= -1; 
				}
				gl.glRotated(Math.toDegrees(angleVector), 0, 1, 0);
				
				
				gl.glScaled(this.meshHeight, segmentLength, this.meshWidth);
				this.arcMesh.render(gl);
				*/
				
				/*
				DirectLinkRepresentation tempLink = new DirectLinkRepresentation(new CubeNoCapColored(null, null), this.meshSize, Color.green, Color.red, false, Color.magenta, startNode, endNode);
				tempLink.setLineSize(this.meshSize);
				tempLink.render(gl);
				*/
				
				//currPoint = nextPoint;
			gl.glPopMatrix();
		//}
	}
	
	private LinkedList<double[]> evaluatePointsOnArc(double angleInDegree, double radius, int nbreOfPoints) {
		LinkedList<double[]> points = new LinkedList<double[]>();
		double[] currPoint;
		double angleInterval = angleInDegree / nbreOfPoints;
		double currAngle;
		
		for (int i=0; i<=nbreOfPoints; i++)  {
			currAngle = Math.toRadians(i * angleInterval);
			currPoint = new double[3];
			currPoint[0] = radius*Math.cos(currAngle);
			currPoint[1] = 0.0;
			currPoint[2] = radius*Math.sin(currAngle);
			points.add(currPoint);
		}
		
		return points;
	}
	

}

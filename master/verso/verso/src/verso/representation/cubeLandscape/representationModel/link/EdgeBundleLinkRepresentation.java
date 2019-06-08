package verso.representation.cubeLandscape.representationModel.link;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.media.opengl.GL;

import verso.util.MathGeometry;
import verso.graphics.VersoScene;
import verso.graphics.primitives.PrimitiveColored;
import verso.representation.Renderable;

public class EdgeBundleLinkRepresentation extends LinkRepresentation implements Renderable {
	private static final float DEFAULT_BETA = 0.9f;
	public static long totalNanoTime = (long)0.0;
	public static long totalGreaterNanoTime = (long)0.0;
	public static int totalGreaterThanNS = 0;
	
	
	public static long totalMilliTime = (long)0.0;
	public static long totalGreaterMilliTime = (long)0.0;
	public static int totalGreaterThanMS = 0;
	
	
	public boolean animateLink = false;
	public float positionPercentage = 0.0f;
	public float positionAugmentation = 0.01f;
	
	private int nbrePoints;
	private LinkedList<LinkedList<float[]>> renderingPoints;
	private LinkedList<NodeRepresentation> controlPoints;
	private LinkedList<NodeRepresentation> straightenedControlPoints;
	private LinkedList<Float> knotsPoints;
	private LinkedList<float[]> bSplinePoints;
	//private LinkedList<DirectLinkRepresentation>  bSplineSegments;
	private float beta;
	private int degree;
	private int nbreSegments;
	private int lcaIndex;
	private boolean straightenControlPoints;
	private boolean removeLCA;
	
	/*
	private boolean verticalPlanar;
	private boolean horizontalPlanar;
	*/
	
	private float totalEdgeLength;
	private HashMap<float[], Float> nodesEdgeLength = new HashMap<float[], Float>();

	private boolean oldColor = true;
	private boolean oldRenderingPoints = true;
	
	public boolean getOldColor() {
		return this.oldColor;
	}
	
	public void setOldColor(boolean oldColor) {
		this.oldColor = oldColor;
	}
	
	public boolean getOldRenderingPoints() {
		return this.oldRenderingPoints;
	}
	
	public void setOldRenderingPoints(boolean oldRenderingPoints) {
		this.oldRenderingPoints = oldRenderingPoints;
		//this.recreateBSpline();
	}
	
	
	
	public EdgeBundleLinkRepresentation(NodeRepresentation startNode, NodeRepresentation endNode, int nbreInLinks, int nbreOutLinks, int nbrePoints, float meshSize, float lineSize, Color linkStartColor, Color linkEndColor, boolean isBidirectional, Color linkBidirectionalColor, float linkSaturation, LinkedList<NodeRepresentation> controlPoints, boolean straightenControlPoints, int degree, int nbreSegments, int lcaIndex, boolean removeLCA) {
		super(startNode, endNode, nbreInLinks, nbreOutLinks, null, meshSize, lineSize, linkStartColor, linkEndColor, isBidirectional, linkBidirectionalColor, linkSaturation);
		
		this.nbrePoints = nbrePoints;
		this.controlPoints = controlPoints;
		this.straightenControlPoints = straightenControlPoints;
		this.beta = DEFAULT_BETA;
		this.degree = degree;
		this.nbreSegments = nbreSegments;
		this.lcaIndex = lcaIndex;
		this.removeLCA = removeLCA;
		
		
		this.updateLinkEndsColor();
		
		createBSpline();
	}
	

	public float getMeshSize() {
		return this.meshSize;
	}
	
	
	public float getLineSize() {
		return this.lineSize;
	}
	
	
	public void setLinkStartColor(Color linkStartColor) {
		super.setLinkStartColor(linkStartColor);
		this.updateLinkEndsColor();
		
	}

	public void setLinkEndColor(Color linkEndColor) {
		super.setLinkEndColor(linkEndColor);
		this.updateLinkEndsColor();
		
	}
	
	public LinkedList<NodeRepresentation> getControlPoints() {
		return this.controlPoints;
	}
	
	public void setControlPoints(LinkedList<NodeRepresentation> controlPoints, int lcaIndex) {
		this.controlPoints = controlPoints;
		this.lcaIndex = lcaIndex;
		createBSpline();
	}
	
	//*********************************************
	//Pour les méthodes suivantes, vérifier si le paramètre à vraiment changer avant de recréer la B-Spline (car s'il
	//n'a pas changé, alors la spline reste la même)???
	//*********************************************
	
	
	public float getBeta() {
		return this.beta;
	}
	
	public void setBeta(float beta) {
		this.beta = beta;
		if (this.straightenControlPoints) {
			createBSpline();
		} else {
			//Pas besoin, car on crée les straightenedControlPoints lors de la création de la B-Spline. (Pas sûr si je dois
			//modifier createBSpline pour accepter en paramètre les points de contrôle (straightenedControlPoints ou
			//controlPoints).
			//this.straightenedControlPoints = straightenControlPoints(this.controlPoints, this.beta, this.lcaIndex, this.removeLCA);
		}
	}
	
	public int getDegree() {
		return this.degree;
	}
	
	public void setDegree(int degree) {
		if(this.degree == degree)
			return;
		
		this.degree = degree;
		createBSpline();
	}
	
	public int getNbreSegments() {
		return nbreSegments;
	}
	
	public void setNbreSegments(int nbreSegments) {
		if(this.nbreSegments == nbreSegments)
			return;

		this.nbreSegments = nbreSegments;
		createBSpline();
	}
	
	public int getLCAIndex() {
		return this.lcaIndex;
	}
	
	public boolean getStraightenControlPoints() {
		return this.straightenControlPoints;
		
	}
	
	public void setStraightenControlPoints(boolean straightenControlPoints) {
		if(this.straightenControlPoints == straightenControlPoints)
			return;
		
		this.straightenControlPoints = straightenControlPoints;
		createBSpline();
	}
	
	public boolean getRemoveLCA() {
		return removeLCA;
	}
	
	public void setRemoveLCA(boolean removeLCA) {
		if(this.removeLCA == removeLCA)
			return;
		
		this.removeLCA = removeLCA;
		createBSpline();
	}
	
	

	//******* À enlever??? ********
	public void recreateBSpline() {
		createBSpline();
	}
	//*****************************
	
	private void createBSpline() {
		LinkedList<NodeRepresentation> curveControlPoints = new LinkedList<NodeRepresentation>();
		curveControlPoints.addAll(controlPoints);
		
		if (straightenControlPoints) {
			curveControlPoints = straightenControlPoints(curveControlPoints, this.beta, this.removeLCA);
		}
		
		
		if (this.removeLCA) {
			if (curveControlPoints.size() > 3) {
				controlPoints.remove(lcaIndex);
			}
		}
		
		
		//Ajoute des points de contrôles s'il n'y en a pas assez car on a besoin de (p + 1) points identiques au début et
		//à la fin de vecteur de knots afin d'interpoler les points de départ et d'arrivée. Cela nous oblige donc à avoir
		//un nombre minimal de points de contrôle.
		//int nbreMissingPoints = ((this.degree+1)*2 + 1) - (curveControlPoints.size()+this.degree+1);
		//Équivaut à :  m >= (p+1)*2 - 1, donc m - (n + 1 + p) >= 0, étant donnée que n+p+1 doit au minimum être égal à (p+1)*2 - 1.
		int nbreMissingPoints = ((this.degree+1)*2 - 1) - (curveControlPoints.size()+this.degree);
		NodeRepresentation newPoint = curveControlPoints.getFirst();
		for (int i=0; i<nbreMissingPoints; i++) {
			curveControlPoints.addFirst(newPoint.copyNode());
		}
		
		
		this.knotsPoints = evaluateKnotsPoints(curveControlPoints, this.degree);
		
		
		this.bSplinePoints = evaluateBSplinePoints(curveControlPoints, this.knotsPoints, this.nbreSegments, this.degree);
		
		this.evaluateNodesEdgeLength();
		this.renderingPoints = evaluateRenderingPoints(this.bSplinePoints, this.nbrePoints);
	}
	
	private LinkedList<NodeRepresentation> straightenControlPoints(LinkedList<NodeRepresentation> controlPoints, float beta, boolean removeLCA) {	
		LinkedList<NodeRepresentation> straightenedControlPoints = new LinkedList<NodeRepresentation>();
		NodeRepresentation pFirst = controlPoints.getFirst();
		NodeRepresentation pLast = controlPoints.getLast();
		int i = 0;
		
		for (NodeRepresentation currControlPoint : controlPoints) {
			
//			System.out.println("EdgeBundleLinkRepresentation.straightenControlPoints()"+currControlPoint);
			if(currControlPoint!=null)
				straightenedControlPoints.add(currControlPoint.multiply(beta)
					.add(pFirst.add(pLast.substract(pFirst).multiply(i / (controlPoints.size() - 1))).multiply(1 - beta)));
			
			i++;
		}
		
		return straightenedControlPoints;
	}
	
	
	/*
	//Fonction pas très utile, car elle vient briser le regroupement des liens qu'on obtient avec les Edge Bundles.
	private LinkedList<NodeRepresentation> makeVerticalPlanarControlPoints(LinkedList<NodeRepresentation> controlPoints) {
		LinkedList<NodeRepresentation> planarControlPoints = new LinkedList<NodeRepresentation>();
		NodeRepresentation startNode = controlPoints.getFirst();
		NodeRepresentation endNode = controlPoints.getLast();
		
		float[] u = {0.0, 1.0, 0.0};
		float[] v = {endNode.getposXd()-startNode.getposXd(), endNode.getposYd() - startNode.getposYd(), endNode.getposZd() - startNode.getposZd()};
		float lengthV = Math.sqrt((v[0]*v[0] + v[1]*v[1] + v[2]*v[2]));
		
		v[0] /= lengthV;
		v[1] /= lengthV;
		v[2] /= lengthV;		
		
		float[] uXv = {u[1]*v[2] - u[2]*v[1], u[2]*v[0] - u[0]*v[2], u[0]*v[1]-u[1]*v[0]};
		float uXvLength = Math.sqrt(uXv[0]*uXv[0] + uXv[1]*uXv[1] + uXv[2]*uXv[2]);
		uXv[0] /= uXvLength;
		uXv[1] /= uXvLength;
		uXv[2] /= uXvLength;
	
		Iterator<NodeRepresentation> controlPointsItr = controlPoints.iterator();
		NodeRepresentation currNode;
		float[] currNodeVector = new float[3];
		float planeEquationValue;
		float planeDistance;
		while (controlPointsItr.hasNext()) {
			currNode= controlPointsItr.next();
			
			currNodeVector[0] = currNode.getposXd() - startNode.getposXd();
			currNodeVector[1] = currNode.getposYd() - startNode.getposYd();
			currNodeVector[2] = currNode.getposZd() - startNode.getposZd();
			
			planeEquationValue = uXv[0]*currNodeVector[0] + uXv[1]*currNodeVector[1] + uXv[2]*currNodeVector[2];
			planeDistance = planeEquationValue / uXvLength;

			planarControlPoints.add(currNode.add(new NodeRepresentation(null,null, 0.0, -planeDistance*uXv[0], -planeDistance*uXv[1], -planeDistance*uXv[2], null, null)));
		}
		
		return planarControlPoints;
	}
	
	private LinkedList<NodeRepresentation> makeHorizontalPlanarControlPoints(LinkedList<NodeRepresentation> controlPoints) {
		LinkedList<NodeRepresentation> planarControlPoints = new LinkedList<NodeRepresentation>();
		Iterator<NodeRepresentation> controlPointsItr = controlPoints.iterator();
		NodeRepresentation currNode;
		while (controlPointsItr.hasNext()) {
			currNode = controlPointsItr.next();
			planarControlPoints.add(new NodeRepresentation(null,null, 0.0, currNode.getposXd(), controlPoints.getFirst().getposYd(), currNode.getposZd(), null, null));
		}
		
		return planarControlPoints;
	}
	*/
	
	
	
	private LinkedList<Float> evaluateKnotsPoints(LinkedList<NodeRepresentation> controlPoints, int degree) {
		LinkedList<Float> knotsPoints = new LinkedList<Float>();
		int nbreKnotsPoints = controlPoints.size() + degree + 1;
		int nbreClampedKnots = degree+1;	
		
		float knotsInterval = 1.0f / (nbreKnotsPoints - nbreClampedKnots*2 + 1);
		
		for (int i=0; i<nbreClampedKnots; i++) {
			knotsPoints.add(0.0f);
		}
		
		float currKnots = 0.0f;
		for (int i=0; i<nbreKnotsPoints-nbreClampedKnots*2; i++) {
			currKnots += knotsInterval;
			knotsPoints.add(currKnots);
		}
		
		for (int i=0; i<nbreClampedKnots; i++) {			
			knotsPoints.add(1.0f);
		}
		
		return knotsPoints;
	}
	
	
	
	
	
	
	private LinkedList<float[]> evaluateBSplinePoints(LinkedList<NodeRepresentation> controlPoints, LinkedList<Float> knotsPoints, int nbreSegments, int degree) {		
		LinkedList<float[]> bSplinePoints = new LinkedList<float[]>();
		ArrayList<NodeRepresentation> arrayControlPoints = new ArrayList<NodeRepresentation>();
		arrayControlPoints.addAll(controlPoints);
		ArrayList<Float> arrayKnotsPoints = new ArrayList<Float>();
		arrayKnotsPoints.addAll(knotsPoints);
		float epsilon = 0.0000000001f;
		
		float uInterval = 1.0f / nbreSegments;
		float u = 0.0f;
		
		
		float[] currSplinePoint;
		
		
		for (int i=0; i<=nbreSegments; i++) {
			if (u >= 1.0 - epsilon) {
				u = 1.0f - epsilon;
			}
			
			
			
			
			
			float[] tempSplinePoint = computeSplinePoint(arrayControlPoints, arrayKnotsPoints, u, degree);
			
			//long endTime = System.nanoTime();
			
			//EdgeBundleLinkRepresentation.totalNanoTime += (endTime - startTime);
			
			//System.out.println("Time in ms: " + (endTime - startTime));
			
			/*
			if (endTime - startTime >= 1000000) {
				EdgeBundleLinkRepresentation.totalGreaterThanNS++;
				EdgeBundleLinkRepresentation.totalGreaterNanoTime += (endTime - startTime);
				
				System.out.println("Temps (en ms): " + (endTime - startTime) / 1000000.0);
			}
			*/
			
			
			
			
			
			
			//currSplinePoint = new NodeRepresentation(null,null, 0.0, 0.0, 0.0, 0.0, Color.black, null);
			currSplinePoint = new float[3];
			
			
			
			currSplinePoint[0] = tempSplinePoint[0];
			currSplinePoint[1] = tempSplinePoint[1];
			currSplinePoint[2] = tempSplinePoint[2];
			
			
			
			
			
			
			
			
			
			//long startTime = System.nanoTime();
			
			/*
			for (int indexControlPoint = 0; indexControlPoint < controlPoints.size(); indexControlPoint++) {				
				//currSplinePoint = currSplinePoint.add(controlPoints.get(indexControlPoint).multiply(evaluateBasisFunction(arrayKnotsPoints, indexControlPoint, u, degree)));
			
				currControlPoint = controlPoints.get(indexControlPoint);
				currControlPointPos[0] = currControlPoint.getposXd(); currControlPointPos[1] = currControlPoint.getposYd(); currControlPointPos[2] = currControlPoint.getposZd();
				
				basisFunctionValue = evaluateBasisFunction(arrayKnotsPoints, indexControlPoint, u, degree);
				currControlPointPos[0] *= basisFunctionValue; currControlPointPos[1] *= basisFunctionValue; currControlPointPos[2] *= basisFunctionValue;
				currSplinePoint[0] += currControlPointPos[0]; currSplinePoint[1] += currControlPointPos[1]; currSplinePoint[2] += currControlPointPos[2];
			}
			*/
			
			//long endTime = System.nanoTime();
			
			//EdgeBundleLinkRepresentation.totalNanoTime += (endTime - startTime);
			
			/*
			if (endTime - startTime >= 1000000) {
				EdgeBundleLinkRepresentation.totalGreaterThanNS++;
				EdgeBundleLinkRepresentation.totalGreaterNanoTime += (endTime - startTime);
				
				System.out.println("Temps (en ms): " + (endTime - startTime) / 1000000.0);
			}
			*/
			
			
			

			
			
			bSplinePoints.add(currSplinePoint);		
			
			u += uInterval;
		}
		
		return bSplinePoints;
	}
	
	
	
	private float[] computeSplinePoint(ArrayList<NodeRepresentation> controlPoints, ArrayList<Float> knotsPoints, float u, int p) {
		HashMap<Integer, HashMap<Integer, float[]>> copyPoints = new HashMap<Integer, HashMap<Integer, float[]>>();
		HashMap<Integer, float[]> tempPointsList;
		
		int k = 0;
		int h = 0;
		int s = 0;
		float alpha;
		float[] tempPoint;
		
		
		//long firstForStartTime = System.currentTimeMillis();
		
		for (int i = 0; i < knotsPoints.size()-1; i++) {
			if (u > knotsPoints.get(i) && u < knotsPoints.get(i+1)) {
				k = i;
				h = p;
				s = 0;
				break;
			}
			else if (u == knotsPoints.get(i)) {
				k = i;
				
				for (int j = 0; j < knotsPoints.size(); j++) {
					if (knotsPoints.get(j) == u) {
						s++;
					}
					
					if (knotsPoints.get(j) > u) {
						break;
					}
				}
				
				if (s >= p) {
					tempPoint = new float[3];
					
					if (i == 0) {// Source position
						tempPoint[0] = controlPoints.get(0).getposXd() ; 
						tempPoint[1] = controlPoints.get(0).getposYd() ; // Height on plane
						tempPoint[2] = controlPoints.get(0).getposZd() ;
					} 
					else {
						tempPoint[0] = controlPoints.get(controlPoints.size()-1).getposXd();
						tempPoint[1] = controlPoints.get(controlPoints.size()-1).getposYd();
						tempPoint[2] = controlPoints.get(controlPoints.size()-1).getposZd();
					}
					
					return tempPoint;
				}
				
				h = p - s;
				
				break;
			}
		}
		

		
		tempPointsList = new HashMap<Integer, float[]>();
		for (int i = k-s; i >= k-p; i--) {
			tempPoint = new float[3];
			tempPoint[0] = controlPoints.get(i).getposXd();
			tempPoint[1] = controlPoints.get(i).getposYd();
			tempPoint[2] = controlPoints.get(i).getposZd();
			
			tempPointsList.put(i, tempPoint);
		}
		copyPoints.put(0, tempPointsList);
		

		
		for (int r = 1; r <= h; r++) {
			tempPointsList = new HashMap<Integer, float[]>();
			for (int i = k-p+r; i <= k-s; i++) {
				alpha = (u - knotsPoints.get(i)) / (knotsPoints.get(i+p-r+1) - knotsPoints.get(i));
				
				//thirdForStartTime = System.currentTimeMillis();
				
				tempPoint = new float[3];
				tempPoint[0] = ((1 - alpha) * copyPoints.get(r-1).get(i-1)[0]) + (alpha * copyPoints.get(r-1).get(i)[0]);
				tempPoint[1] = ((1 - alpha) * copyPoints.get(r-1).get(i-1)[1]) + (alpha * copyPoints.get(r-1).get(i)[1]);
				tempPoint[2] = ((1 - alpha) * copyPoints.get(r-1).get(i-1)[2]) + (alpha * copyPoints.get(r-1).get(i)[2]);
				
				//thirdForEndTime = System.currentTimeMillis();
				
				tempPointsList.put(i, tempPoint);
			}
			
			copyPoints.put(r, tempPointsList);
		}
		
		
		if (copyPoints.get(p-s).get(k-s) == null) {
			System.out.println("Erreur, null!!!!!!!!!");
		}
		
		return copyPoints.get(p-s).get(k-s);
	}
	
	
	
	
	
	private void evaluateNodesEdgeLength() {
		this.nodesEdgeLength.clear();
		
		Iterator<float[]> splinePointsItr = this.bSplinePoints.iterator();
		float[] previousNode = splinePointsItr.next();
		float[] currentNode;
		float[] currentVector;
		float currentTotalLength = 0.0f;
		
		this.nodesEdgeLength.put(previousNode, 0.0f);
		
		while (splinePointsItr.hasNext()) {
			currentNode = splinePointsItr.next();
			currentVector = new float[3];
			currentVector[0] = currentNode[0] - previousNode[0]; currentVector[1] = currentNode[1] - previousNode[1]; currentVector[2] = currentNode[2] - previousNode[2];
			
			currentTotalLength += Math.sqrt(currentVector[0]*currentVector[0] + currentVector[1]*currentVector[1] + currentVector[2]*currentVector[2]);
			this.nodesEdgeLength.put(currentNode, currentTotalLength);
			previousNode = currentNode;
		}
		
		this.totalEdgeLength = currentTotalLength;
	}
	
	
	
	
	private LinkedList<LinkedList<float[]>> evaluateRenderingPoints(LinkedList<float[]> bSplinePoints, int nbrePoints) {
		float linkRadius = this.meshSize/2.0f;
		float pointAngle = 360.0f / nbrePoints;
		float[][] basePoints = new float[nbrePoints][]; 
		
		for (int i = 0; i < nbrePoints; i++) {
			basePoints[i] = new float[3];
			basePoints[i][0] = linkRadius * (float)Math.cos(Math.toRadians(pointAngle*i));
			basePoints[i][1] = 0.0f;
			basePoints[i][2] = linkRadius * (float)Math.sin(Math.toRadians(pointAngle*i));
		}
		
		
		LinkedList<LinkedList<float[]>> renderingPoints = new LinkedList<LinkedList<float[]>>();
		LinkedList<float[]> currentRenderingPoints, previousRenderingPoints, nextRenderingPoints;
		Iterator<float[]> bSplinePointsItr = bSplinePoints.iterator();
		float[] previousNode = bSplinePointsItr.next();
		float[] currentNode = bSplinePointsItr.next();
		float[] nextNode = null;
		float[] currentVector;
		
		float[] rotationParams;
		float[] previousRotationMatrix, nextRotationMatrix;
		float[] renderingPoint;
		float[] previousPoint, nextPoint, middlePoint;
		
		
		
		
		
		
		currentVector = new float[3];
		currentVector[0] = currentNode[0] - previousNode[0]; currentVector[1] = currentNode[1] - previousNode[1]; currentVector[2] = currentNode[2] - previousNode[2];
		
		rotationParams = MathGeometry.getRotationParams(currentVector[0], currentVector[1], currentVector[2]);
		
		if (rotationParams == null) {
			previousRotationMatrix = MathGeometry.get3DIdentityMatrix();
		}
		else {
			previousRotationMatrix = MathGeometry.get3DRotationMatrix(rotationParams[0], rotationParams[1], rotationParams[2], rotationParams[3]);
		}
		
		currentRenderingPoints = new LinkedList<float[]>();
		for (int i = 0; i < nbrePoints; i++) {
			renderingPoint = MathGeometry.rotate3DPoint(previousRotationMatrix, basePoints[i]);
			renderingPoint[0] += previousNode[0];
			renderingPoint[1] += previousNode[1];
			renderingPoint[2] += previousNode[2];
			
			currentRenderingPoints.add(renderingPoint);;
		}
		
		renderingPoints.addFirst(currentRenderingPoints);
		
		
		
		
		
		
		if (this.oldRenderingPoints) {
			while (bSplinePointsItr.hasNext()) {
				nextNode = bSplinePointsItr.next();
				
				/*
				if (currentNode == bSplinePoints.getLast()) {
					continue;
				}
				*/
				
				currentVector = new float[3];
				currentVector[0] = currentNode[0] - previousNode[0]; currentVector[1] = currentNode[1] - previousNode[1]; currentVector[2] = currentNode[2] - previousNode[2];
				rotationParams = MathGeometry.getRotationParams(currentVector[0], currentVector[1], currentVector[2]);
				
				if (rotationParams == null) {
					previousRotationMatrix = MathGeometry.get3DIdentityMatrix();
				}
				else {
					previousRotationMatrix = MathGeometry.get3DRotationMatrix(rotationParams[0], rotationParams[1], rotationParams[2], rotationParams[3]);
				}
				
				currentRenderingPoints = new LinkedList<float[]>();
				
				for (int i = 0; i < nbrePoints; i++) {
					renderingPoint = MathGeometry.rotate3DPoint(previousRotationMatrix, basePoints[i]);
					renderingPoint[0] += currentNode[0];
					renderingPoint[1] += currentNode[1];
					renderingPoint[2] += currentNode[2];
					
					currentRenderingPoints.add(renderingPoint);
				}
				
				renderingPoints.add(currentRenderingPoints);
				
				previousNode = currentNode;
				currentNode = nextNode;
			}
		}
		else {
			while (bSplinePointsItr.hasNext()) {
				nextNode = bSplinePointsItr.next();
				
				/*
				if (currentNode == bSplinePoints.getLast()) {
					continue;
				}
				*/
				
				currentVector = new float[3];
				currentVector[0] = currentNode[0] - previousNode[0]; 
				currentVector[1] = currentNode[1] - previousNode[1]; 
				currentVector[2] = currentNode[2] - previousNode[2];
				
				
				rotationParams = MathGeometry.getRotationParams(currentVector[0], currentVector[1], currentVector[2]);
				
				if (rotationParams == null) {
					previousRotationMatrix = MathGeometry.get3DIdentityMatrix();
				}
				else {
					previousRotationMatrix = MathGeometry.get3DRotationMatrix(rotationParams[0], rotationParams[1], rotationParams[2], rotationParams[3]);
				}
				
				currentVector = new float[3];
				currentVector[0] = nextNode[0] - currentNode[0]; 
				currentVector[1] = nextNode[1] - currentNode[1]; 
				currentVector[2] = nextNode[2] - currentNode[2];
				
				rotationParams = MathGeometry.getRotationParams(currentVector[0], currentVector[1], currentVector[2]);
				
				if (rotationParams == null) {
					nextRotationMatrix = MathGeometry.get3DIdentityMatrix();
				}
				else {
					nextRotationMatrix = MathGeometry.get3DRotationMatrix(rotationParams[0], rotationParams[1], rotationParams[2], rotationParams[3]);
				}
				
				currentRenderingPoints = new LinkedList<float[]>();
				previousRenderingPoints = new LinkedList<float[]>();
				nextRenderingPoints = new LinkedList<float[]>();
				
				for (int i = 0; i < nbrePoints; i++) {
					renderingPoint = MathGeometry.rotate3DPoint(previousRotationMatrix, basePoints[i]);
					renderingPoint[0] += currentNode[0];
					renderingPoint[1] += currentNode[1];
					renderingPoint[2] += currentNode[2];
					
					previousRenderingPoints.add(renderingPoint);
					
					renderingPoint = MathGeometry.rotate3DPoint(nextRotationMatrix, basePoints[i]);
					renderingPoint[0] += currentNode[0];
					renderingPoint[1] += currentNode[1];
					renderingPoint[2] += currentNode[2];
					
					nextRenderingPoints.add(renderingPoint);
				}
				
				
				for (int i = 0; i < previousRenderingPoints.size(); i++) {
					previousPoint = previousRenderingPoints.get(i);
					nextPoint = nextRenderingPoints.get(i);
					
					middlePoint = new float[3];
					middlePoint[0] = previousPoint[0] + (nextPoint[0] - previousPoint[0])/2.0f;
					middlePoint[1] = previousPoint[1] + (nextPoint[1] - previousPoint[1])/2.0f;
					middlePoint[2] = previousPoint[2] + (nextPoint[2] - previousPoint[2])/2.0f;
					
					currentRenderingPoints.add(middlePoint);
				}
				
				renderingPoints.add(currentRenderingPoints);
				
				previousNode = currentNode;
				currentNode = nextNode;
			}
		}
		
		currentVector = new float[3];
		currentVector[0] = nextNode[0] - previousNode[0]; currentVector[1] = nextNode[1] - previousNode[1]; currentVector[2] = nextNode[2] - previousNode[2];
		rotationParams = MathGeometry.getRotationParams(currentVector[0], currentVector[1], currentVector[2]);
		
		if (rotationParams == null) {
			nextRotationMatrix = MathGeometry.get3DIdentityMatrix();
		}
		else {
			nextRotationMatrix = MathGeometry.get3DRotationMatrix(rotationParams[0], rotationParams[1], rotationParams[2], rotationParams[3]);
		}
		
		currentRenderingPoints = new LinkedList<float[]>();
		for (int i = 0; i < nbrePoints; i++) {
			renderingPoint = MathGeometry.rotate3DPoint(nextRotationMatrix, basePoints[i]);
			renderingPoint[0] += nextNode[0];
			renderingPoint[1] += nextNode[1];
			renderingPoint[2] += nextNode[2];
			
			currentRenderingPoints.add(renderingPoint);;
		}
		
		renderingPoints.addLast(currentRenderingPoints);
		
		return renderingPoints;
	}
	
	
	
	private float evaluateBasisFunction(ArrayList<Float> knotsPoints, int i, float u, int p) {	
		if (p == 0) {
			if (u >= knotsPoints.get(i) && u < knotsPoints.get(i+1)) {			
				return 1.0f;
			}
			else {
				return 0.0f;
			}
		}
		else {
			float firstCoefficient;
			if (knotsPoints.get(i+p) - knotsPoints.get(i) == 0) {
				firstCoefficient = 0.0f;
			}
			else {
				firstCoefficient = (u - knotsPoints.get(i)) / (knotsPoints.get(i+p) - knotsPoints.get(i));
			}
			
			float valeurRecursion_1 = evaluateBasisFunction(knotsPoints, i, u, p-1);
			
			
			float secondCoefficient;
			if (knotsPoints.get(i+p+1) - knotsPoints.get(i+1) == 0) {
				secondCoefficient = 0;
			}
			else {
				secondCoefficient = ((knotsPoints.get(i+p+1) - u) / (knotsPoints.get(i+p+1) - knotsPoints.get(i+1)));
			}
			
			float valeurRecursion_2 = evaluateBasisFunction(knotsPoints, i+1, u, p-1);
			
			return firstCoefficient*valeurRecursion_1 + secondCoefficient*valeurRecursion_2;
		}
	}
	
	private LinkedList<DirectLinkRepresentation> createBSplineSegments(PrimitiveColored coloredMesh, float meshSize, Color linkStartColor, Color linkEndColor, LinkedList<NodeRepresentation> bSplinePoints) {
		LinkedList<DirectLinkRepresentation> bSplineSegments = new LinkedList<DirectLinkRepresentation>();
		Iterator<NodeRepresentation> bSplinePointsItr = bSplinePoints.iterator();
		NodeRepresentation previousNode = bSplinePointsItr.next();
		NodeRepresentation currNode;

		float colorInterval = 1.0f / (bSplinePoints.size()-1);
		int segmentInd = 0;
		
		float endColorPercentage;
		float segmentRedColor;
		float segmentGreenColor;
		float segmentBlueColor;
		
		Color segmentStartColor = linkStartColor;
		Color segmentEndColor;
		
		
		while (bSplinePointsItr.hasNext()) {
			currNode = bSplinePointsItr.next();
			segmentInd++;
			endColorPercentage = segmentInd*colorInterval;
			
			if (endColorPercentage > 1.0) {
				endColorPercentage = 1.0f;
			}
			
			segmentRedColor = linkStartColor.getRed()*(1.0f - endColorPercentage)/255 + linkEndColor.getRed()*endColorPercentage/255;
			segmentGreenColor = linkStartColor.getGreen()*(1.0f - endColorPercentage)/255 + linkEndColor.getGreen()*endColorPercentage/255;
			segmentBlueColor = linkStartColor.getBlue()*(1.0f - endColorPercentage)/255 + linkEndColor.getBlue()*endColorPercentage/255;
			
			if (segmentRedColor > 1.0f) {
				segmentRedColor = 1.0f;
			}
			
			if (segmentGreenColor > 1.0f) {
				segmentGreenColor = 1.0f;
			}
			
			if (segmentBlueColor > 1.0f) {
				segmentBlueColor = 1.0f;
			}
					
			segmentEndColor = new Color(segmentRedColor, segmentGreenColor, segmentBlueColor);
			bSplineSegments.add(new DirectLinkRepresentation(previousNode, currNode, nbreInLinks, nbreOutLinks, coloredMesh, meshSize, lineSize, segmentStartColor, segmentEndColor, isBidirectional, linkBidirectionalColor, linkSaturation));
			segmentStartColor = segmentEndColor;
			
			previousNode = currNode;
		}

		return bSplineSegments;
	}
	
	private void updateLinkEndsColor() {
		Color startColor = new Color(this.linkStartColor.getRed() / 255.0f, this.linkStartColor.getGreen() / 255.0f, this.linkStartColor.getBlue() / 255.0f);
		Color endColor = new Color(this.linkEndColor.getRed() / 255.0f, this.linkEndColor.getGreen() / 255.0f, this.linkEndColor.getBlue() / 255.0f);
		
		float inRatio = (float)this.nbreInLinks / (float)(this.nbreInLinks + this.nbreOutLinks);
		float outRatio = (float)this.nbreOutLinks / (float)(this.nbreInLinks + this.nbreOutLinks);
		
		Color tempStartColor = getColorAt(inRatio, startColor, endColor);
		Color tempEndColor = getColorAt(outRatio, startColor, endColor);
		
		float[] startColorHSB = Color.RGBtoHSB(tempStartColor.getRed(), tempStartColor.getGreen(), tempStartColor.getBlue(), null);
		float[] endColorHSB = Color.RGBtoHSB(tempEndColor.getRed(), tempEndColor.getGreen(), tempEndColor.getBlue(), null);
		
		this.linkStartColor = new Color(Color.HSBtoRGB(startColorHSB[0], this.linkSaturation, startColorHSB[2]));
		this.linkEndColor = new Color(Color.HSBtoRGB(endColorHSB[0], this.linkSaturation, endColorHSB[2]));
	}
	
	private Color evaluatePointColor(int pointPos) {
	
		float endColorPercentage = this.nodesEdgeLength.get(this.bSplinePoints.get(pointPos)) / this.totalEdgeLength;
		
		if (endColorPercentage < (1.0/3.0)) {
			endColorPercentage = 0.0f;
			
			//endColorPercentage /= 2.0;
		}
		else if (endColorPercentage > (2.0/3.0)) {
			endColorPercentage = 1.0f;
			
			//endColorPercentage *= 5.0/4.0;
		}
		else {
			endColorPercentage = (endColorPercentage - (1.0f/3.0f)) * 3;
		}
		
		if (endColorPercentage > 1.0) {
			endColorPercentage = 1.0f;
		}
		
		
		
		return getColorAt(endColorPercentage, this.linkStartColor, this.linkEndColor);
	}
	
	
	private Color getColorAt(float endColorPercentage, Color startColor, Color endColor) {
		float pointRedComponent;
		float pointGreenComponent;
		float pointBlueComponent;
		
		pointRedComponent = (startColor.getRed()*(1.0f - endColorPercentage)) / 255.0f + (endColor.getRed()*endColorPercentage) / 255.0f;
		pointGreenComponent = (startColor.getGreen()*(1.0f - endColorPercentage)) / 255.0f + (endColor.getGreen()*endColorPercentage) / 255.0f;
		pointBlueComponent = (startColor.getBlue()*(1.0f - endColorPercentage)) / 255.0f + (endColor.getBlue()*endColorPercentage) / 255.0f;
		
		if (pointRedComponent > 1.0) {
			pointRedComponent = 1.0f;
		}
		
		if (pointGreenComponent > 1.0) {
			pointGreenComponent = 1.0f;
		}
		
		if (pointBlueComponent > 1.0) {
			pointBlueComponent = 1.0f;
		}
		
		return new Color((float)pointRedComponent, (float)pointGreenComponent, (float)pointBlueComponent);
	}
	
	
	public void render(GL gl) {
		//System.out.println("RENDER EDGE BUNDLE");
		
		if (this.isFiltered) {
			return;
		}		
		
		gl.glLoadName(VersoScene.id);
		VersoScene.pickingEntities.put(VersoScene.id++, this);
		
		
		Iterator<LinkedList<float[]>> renderingPointsItr;
		LinkedList<float[]> currentRenderingPoints;
		float[] vertex1, vertex2, vertex3, vertex4;
		float[] u = new float[3], v = new float[3];	
		int pointPos;
		Color currentPointColor;
		float[] currNode;
		
		int nbrePoints = this.renderingPoints.getFirst().size();
		
		for (int i = 0; i < nbrePoints; i++) {
			gl.glBegin(GL.GL_TRIANGLE_STRIP);
				pointPos = 0;
			
				renderingPointsItr = this.renderingPoints.iterator();
				currentRenderingPoints = renderingPointsItr.next();

				vertex1 = currentRenderingPoints.get((i+1) % currentRenderingPoints.size()); 
				vertex2 = currentRenderingPoints.get(i);

				currNode = this.bSplinePoints.get(pointPos);
				
				u[0] = vertex1[0] - currNode[0];
				u[1] = vertex1[1] - currNode[1];
				u[2] = vertex1[2] - currNode[2];

				v[0] = vertex2[0] - currNode[0];
				v[1] = vertex2[1] - currNode[1];
				v[2] = vertex2[2] - currNode[2];
							
				
				currentPointColor = this.evaluatePointColor(pointPos);
				
				//gl.glColor4f(currentPointColor.getRed()/255.0f, currentPointColor.getGreen()/255.0f, currentPointColor.getBlue()/255.0f, 0.50f);
				gl.glColor3f(currentPointColor.getRed()/255.0f, currentPointColor.getGreen()/255.0f, currentPointColor.getBlue()/255.0f);
				
				gl.glNormal3d(u[0], u[1], u[2]);
				gl.glVertex3d(vertex1[0], vertex1[1], vertex1[2]);
				
				gl.glNormal3d(v[0], v[1], v[2]);
				gl.glVertex3d(vertex2[0], vertex2[1], vertex2[2]);
				
				while (renderingPointsItr.hasNext()) {
					currentRenderingPoints = renderingPointsItr.next();
					pointPos++;
					
					vertex3 = currentRenderingPoints.get((i+1) % currentRenderingPoints.size()); 
					vertex4 = currentRenderingPoints.get(i);
					
					currNode = this.bSplinePoints.get(pointPos);
					
					u[0] = vertex3[0] - currNode[0];
					u[1] = vertex3[1] - currNode[1];
					u[2] = vertex3[2] - currNode[2];
					
					v[0] = vertex4[0] - currNode[0];
					v[1] = vertex4[1] - currNode[1];
					v[2] = vertex4[2] - currNode[2];
					
					currentPointColor = this.evaluatePointColor(pointPos);
						
					//gl.glColor4f(currentPointColor.getRed()/255.0f, currentPointColor.getGreen()/255.0f, currentPointColor.getBlue()/255.0f, 0.50f);
					gl.glColor3f(currentPointColor.getRed()/255.0f, currentPointColor.getGreen()/255.0f, currentPointColor.getBlue()/255.0f);
					
					gl.glNormal3d(v[0], v[1], v[2]);
					gl.glVertex3d(vertex3[0], vertex3[1], vertex3[2]);
					
					gl.glNormal3d(u[0], u[1], u[2]);
					gl.glVertex3d(vertex4[0], vertex4[1], vertex4[2]);
				}
		
			gl.glEnd();
		}
		

		
		
		
		
		
		
		
		//LinkedList<NodeRepresentation> bSplinePoints = evaluateBSplinePoints(this.straightenedControlPoints, this.knotsPoints, this.nbreSegments, this.degree);
		
		
		/*
		LinkedList<DirectLinkRepresentation>  bSplineSegments = createBSplineSegments(this.coloredMesh, this.meshSize, this.linkStartColor, this.linkEndColor, this.bSplinePoints);
		
		for (DirectLinkRepresentation segment : bSplineSegments) {
			segment.render(gl);
		}
		*/
		
	}
	
	
	
	public EdgeBundleLinkRepresentation copyLink() {
		return new EdgeBundleLinkRepresentation(this.startNode, this.endNode, this.nbreInLinks, this.nbreOutLinks, this.nbrePoints, this.meshSize, this.lineSize, this.linkStartColor, this.linkEndColor, this.isBidirectional, this.linkBidirectionalColor, this.linkSaturation, this.controlPoints, this.straightenControlPoints, this.degree, this.nbreSegments, this.lcaIndex, this.removeLCA);
	}
}

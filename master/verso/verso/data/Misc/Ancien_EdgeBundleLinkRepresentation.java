package verso.representation.cubeLandscape.representationModel.link;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.media.opengl.GL;

import verso.graphics.Scene;
import verso.graphics.primitives.PrimitiveColored;
import verso.representation.Renderable;
import verso.util.MathGeometry;

import com.sun.opengl.util.GLUT;

public class EdgeBundleLinkRepresentation extends LinkRepresentation implements Renderable {
	public boolean animateLink = false;
	public double positionPercentage = 0.0;
	public double positionAugmentation = 0.01;
	
	private int nbrePoints;
	private LinkedList<LinkedList<double[]>> renderingPoints;
	private LinkedList<NodeRepresentation> controlPoints;
	private LinkedList<NodeRepresentation> straightenedControlPoints;
	private LinkedList<Double> knotsPoints;
	private LinkedList<NodeRepresentation> bSplinePoints;
	//private LinkedList<DirectLinkRepresentation>  bSplineSegments;
	private double beta;
	private int degree;
	private int nbreSegments;
	private int lcaIndex;
	private boolean straightenControlPoints;
	private boolean removeLCA;
	
	/*
	private boolean verticalPlanar;
	private boolean horizontalPlanar;
	*/
	
	private double totalEdgeLength;
	private HashMap<NodeRepresentation, Double> nodesEdgeLength = new HashMap<NodeRepresentation, Double>();

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
	
	
	
	public EdgeBundleLinkRepresentation(NodeRepresentation startNode, NodeRepresentation endNode, int nbreInLinks, int nbreOutLinks, int nbrePoints, double meshSize, double lineSize, Color linkStartColor, Color linkEndColor, boolean isBidirectional, Color linkBidirectionalColor, LinkedList<NodeRepresentation> controlPoints, boolean straightenControlPoints, double beta, int degree, int nbreSegments, int lcaIndex, boolean removeLCA) {
		super(startNode, endNode, nbreInLinks, nbreOutLinks, null, meshSize, lineSize, linkStartColor, linkEndColor, isBidirectional, linkBidirectionalColor);
		
		if (startNode.getEntityRepresentation().getName().equals("Class : org.jhotdraw.draw.BezierFigure") && endNode.getEntityRepresentation().getName().equals("Package : org.jhotdraw.draw.handle")) {
			System.out.println("Ok, passe!!!");
		}
		
		this.nbrePoints = nbrePoints;
		this.controlPoints = controlPoints;
		this.straightenControlPoints = straightenControlPoints;
		this.beta = beta;
		this.degree = degree;
		this.nbreSegments = nbreSegments;
		this.lcaIndex = lcaIndex;
		this.removeLCA = removeLCA;
		
		
		this.updateLinkEndsColor();
		
		createBSpline();
	}
	
	public void setColoredMesh(PrimitiveColored coloredMesh) {
		super.setColoredMesh(coloredMesh);
		
		/*
		Iterator<DirectLinkRepresentation> directLinksItr = bSplineSegments.iterator();
		while (directLinksItr.hasNext()) {
			directLinksItr.next().setColoredMesh(coloredMesh);
		}
		*/
	}

	public double getMeshSize() {
		return this.meshSize;
	}
	
	public void setMeshSize(double meshSize) {
		super.setMeshSize(meshSize);
		
		/*
		Iterator<DirectLinkRepresentation> directLinksItr = bSplineSegments.iterator();
		while (directLinksItr.hasNext()) {
			directLinksItr.next().setMeshSize(meshSize);
		}
		*/
	}
	
	public double getLineSize() {
		return this.lineSize;
	}
	
	public void setLineSize(double lineSize) {
		super.setLineSize(lineSize);
		
		/*
		Iterator<DirectLinkRepresentation> directLinksItr = bSplineSegments.iterator();
		while (directLinksItr.hasNext()) {
			directLinksItr.next().setLineSize(this.lineSize);
		}
		*/
	}
	
	
	
	//Pas de getColor, car cette méthode existe déjà dans EntityRepresentation et fait déjà ce qu'on veut.
	
	public void setLinkStartColor(Color linkStartColor) {
		super.setLinkStartColor(linkStartColor);
		this.updateLinkEndsColor();
		
		//this.bSplineSegments = createBSplineSegments(this.coloredMesh, this.meshSize, this.linkStartColor, this.linkEndColor, this.bSplinePoints);
		
		/*
		Iterator<DirectLinkRepresentation> directLinksItr = bSplineSegments.iterator();
		while (directLinksItr.hasNext()) {
			directLinksItr.next().setlinkStartColor(this.linkStartColor);
		}
		*/
	}

	public void setLinkEndColor(Color linkEndColor) {
		super.setLinkEndColor(linkEndColor);
		this.updateLinkEndsColor();
		
		//this.bSplineSegments = createBSplineSegments(this.coloredMesh, this.meshSize, this.linkStartColor, this.linkEndColor, this.bSplinePoints);
		
		/*
		Iterator<DirectLinkRepresentation> directLinksItr = bSplineSegments.iterator();
		while (directLinksItr.hasNext()) {
			directLinksItr.next().setlinkEndColor(this.linkEndColor);
		}
		*/
	}
	
	public void setIsBidirectional(boolean isBidirectional) {
		super.setIsBidirectional(isBidirectional);
		
		/*
		Iterator<DirectLinkRepresentation> segmentsItr = this.bSplineSegments.iterator();
		while (segmentsItr.hasNext()) {
			segmentsItr.next().setIsBidirectional(this.isBidirectional);
		}
		*/
	}
	
	public void setLinkBidirectionalColor(Color linkBidirectionalColor) {
		super.setLinkBidirectionalColor(linkBidirectionalColor);
		
		/*
		Iterator<DirectLinkRepresentation> segmentsItr = this.bSplineSegments.iterator();
		while (segmentsItr.hasNext()) {
			segmentsItr.next().setLinkBidirectionalColor(this.linkBidirectionalColor);
		}
		*/
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
	
	
	public double getBeta() {
		return this.beta;
	}
	
	public void setBeta(double beta) {
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
		this.degree = degree;
		createBSpline();
	}
	
	public int getNbreSegments() {
		return nbreSegments;
	}
	
	public void setNbreSegments(int nbreSegments) {
		this.nbreSegments = nbreSegments;
		createBSpline();
	}
	
	public int getLCAIndex() {
		return this.lcaIndex;
	}
	
	//Vraiment nécessaire, car normalement le lcaIndex change seulement lorsque la liste de points de contrôles changent
	//aussi???
	/*
	public void setLCAIndex(int lcaIndex) {
		this.lcaIndex = lcaIndex;
	}
	*/
	
	public boolean getStraightenControlPoints() {
		return this.straightenControlPoints;
		
	}
	
	public void setStraightenControlPoints(boolean straightenControlPoints) {
		this.straightenControlPoints = straightenControlPoints;
		createBSpline();
	}
	
	public boolean getRemoveLCA() {
		return removeLCA;
	}
	
	public void setRemoveLCA(boolean removeLCA) {
		this.removeLCA = removeLCA;
		createBSpline();
	}
	
	
	/*
	public boolean getVerticalPlanar() {
		return this.verticalPlanar;
	}
	
	public void setVerticalPlanar(boolean verticalPlanar) {
		this.verticalPlanar = verticalPlanar;
		createBSpline();
	}
	
	public boolean getHorizontalPlanar() {
		return this.horizontalPlanar;
	}
	
	public void setHorizontalPlanar(boolean horizontalPlanar) {
		this.horizontalPlanar = horizontalPlanar;
		createBSpline();
	}
	*/


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
		
		/*
		if (this.removeLCA) {
			if (curveControlPoints.size() > 3) {
				curveControlPoints = removeLCA(curveControlPoints, this.lcaIndex);
			}
		}
		*/
		
		
		/*
		if (curveControlPoints.size() == 3) {
			curveControlPoints.add(1, curveControlPoints.get(1));
		}
		*/
		
		//this.straightenedControlPoints = curveControlPoints;
		
		
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
		
		
		/*
		if (this.verticalPlanar) {
			curveControlPoints = makeVerticalPlanarControlPoints(curveControlPoints);
		}
		
		if (this.horizontalPlanar) {
			curveControlPoints = makeHorizontalPlanarControlPoints(curveControlPoints);
		}	
		*/
		
		
		this.knotsPoints = evaluateKnotsPoints(curveControlPoints, this.degree);
		
		//long startMilli = System.currentTimeMillis();
		this.bSplinePoints = evaluateBSplinePoints(curveControlPoints, this.knotsPoints, this.nbreSegments, this.degree);
		this.evaluateNodesEdgeLength();
		this.renderingPoints = evaluateRenderingPoints(this.bSplinePoints, this.nbrePoints);
		//System.out.println("Voici les millis!!!!: " + (System.currentTimeMillis() - startMilli));
		
		//this.bSplineSegments = createBSplineSegments(this.coloredMesh, this.meshSize, this.linkStartColor, this.linkEndColor, this.bSplinePoints);
	}
	
	private LinkedList<NodeRepresentation> straightenControlPoints(LinkedList<NodeRepresentation> controlPoints, double beta, boolean removeLCA) {	
		LinkedList<NodeRepresentation> straightenedControlPoints = new LinkedList<NodeRepresentation>();
		NodeRepresentation pFirst = controlPoints.getFirst();
		NodeRepresentation pLast = controlPoints.getLast();
		Iterator<NodeRepresentation> controlPointsItr = controlPoints.iterator();
		NodeRepresentation currControlPoint;
		int i = 0;
		
		while (controlPointsItr.hasNext()) {
			currControlPoint = controlPointsItr.next();
			
			//***************
			//À vérifier ???
			//!removeLCA || i != lcaIndex || controlPoints.size() <= 3
			//***************
			//if () {
			
			straightenedControlPoints.add(currControlPoint.multiply(beta).add((pFirst.add(pLast.substract(pFirst).multiply(i / (controlPoints.size() - 1))).multiply(1 - beta))));
			
			//}
			
			
			i++;
		}
		
		return straightenedControlPoints;
	}
	
	private LinkedList<NodeRepresentation> removeLCA(LinkedList<NodeRepresentation> controlPoints, int lcaIndex) {	
		controlPoints.remove(lcaIndex);
		return controlPoints;
	}
	
	
	/*
	//Fonction pas très utile, car elle vient briser le regroupement des liens qu'on obtient avec les Edge Bundles.
	private LinkedList<NodeRepresentation> makeVerticalPlanarControlPoints(LinkedList<NodeRepresentation> controlPoints) {
		LinkedList<NodeRepresentation> planarControlPoints = new LinkedList<NodeRepresentation>();
		NodeRepresentation startNode = controlPoints.getFirst();
		NodeRepresentation endNode = controlPoints.getLast();
		
		double[] u = {0.0, 1.0, 0.0};
		double[] v = {endNode.getposXd()-startNode.getposXd(), endNode.getposYd() - startNode.getposYd(), endNode.getposZd() - startNode.getposZd()};
		double lengthV = Math.sqrt((v[0]*v[0] + v[1]*v[1] + v[2]*v[2]));
		
		v[0] /= lengthV;
		v[1] /= lengthV;
		v[2] /= lengthV;		
		
		double[] uXv = {u[1]*v[2] - u[2]*v[1], u[2]*v[0] - u[0]*v[2], u[0]*v[1]-u[1]*v[0]};
		double uXvLength = Math.sqrt(uXv[0]*uXv[0] + uXv[1]*uXv[1] + uXv[2]*uXv[2]);
		uXv[0] /= uXvLength;
		uXv[1] /= uXvLength;
		uXv[2] /= uXvLength;
	
		Iterator<NodeRepresentation> controlPointsItr = controlPoints.iterator();
		NodeRepresentation currNode;
		double[] currNodeVector = new double[3];
		double planeEquationValue;
		double planeDistance;
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
	
	
	
	private LinkedList<Double> evaluateKnotsPoints(LinkedList<NodeRepresentation> controlPoints, int degree) {
		LinkedList<Double> knotsPoints = new LinkedList<Double>();
		int nbreKnotsPoints = controlPoints.size() + degree + 1;
		int nbreClampedKnots = degree+1;	
		
		double knotsInterval = 1.0 / (nbreKnotsPoints - nbreClampedKnots*2 + 1);
		
		for (int i=0; i<nbreClampedKnots; i++) {
			knotsPoints.add(0.0);
		}
		
		double currKnots = 0.0;
		for (int i=0; i<nbreKnotsPoints-nbreClampedKnots*2; i++) {
			currKnots += knotsInterval;
			knotsPoints.add(currKnots);
		}
		
		for (int i=0; i<nbreClampedKnots; i++) {			
			knotsPoints.add(1.0);
		}
		
		return knotsPoints;
	}
	
	
	
	
	
	
	private LinkedList<NodeRepresentation> evaluateBSplinePoints(LinkedList<NodeRepresentation> controlPoints, LinkedList<Double> knotsPoints, int nbreSegments, int degree) {		
		LinkedList<NodeRepresentation> bSplinePoints = new LinkedList<NodeRepresentation>();
		ArrayList<Double> arrayKnotsPoints = new ArrayList<Double>();
		arrayKnotsPoints.addAll(knotsPoints);
		double epsilon = 0.0000000001;
		
		double uInterval = 1.0 / nbreSegments;
		double u = 0.0;
		
		
		NodeRepresentation currSplinePoint;;
		
		
		
		for (int i=0; i<=nbreSegments; i++) {
			if (u >= 1.0 - epsilon) {
				u = 1.0 - epsilon;
			}
			
			currSplinePoint = new NodeRepresentation(null,null, 0.0, 0.0, 0.0, 0.0, Color.black, null);
			
			for (int indexControlPoint = 0; indexControlPoint < controlPoints.size(); indexControlPoint++) {				
					currSplinePoint = currSplinePoint.add(controlPoints.get(indexControlPoint).multiply(evaluateBasisFunction(arrayKnotsPoints, indexControlPoint, u, degree)));
			}
			
			bSplinePoints.add(currSplinePoint);		
			
			u += uInterval;
		}
		
		return bSplinePoints;
	}
	
	
	private void evaluateNodesEdgeLength() {
		this.nodesEdgeLength.clear();
		
		Iterator<NodeRepresentation> splinePointsItr = this.bSplinePoints.iterator();
		NodeRepresentation previousNode = splinePointsItr.next();
		NodeRepresentation currentNode;
		NodeRepresentation currentVector;
		double currentTotalLength = 0.0;
		
		this.nodesEdgeLength.put(previousNode, 0.0);
		
		while (splinePointsItr.hasNext()) {
			currentNode = splinePointsItr.next();
			currentVector = currentNode.substract(previousNode);
			currentTotalLength += Math.sqrt(currentVector.getposXd()*currentVector.getposXd() + currentVector.getposYd()*currentVector.getposYd() + currentVector.getposZd()*currentVector.getposZd());
			this.nodesEdgeLength.put(currentNode, currentTotalLength);
			previousNode = currentNode;
		}
		
		this.totalEdgeLength = currentTotalLength;
	}
	
	
	
	
	private LinkedList<LinkedList<double[]>> evaluateRenderingPoints(LinkedList<NodeRepresentation> bSplinePoints, int nbrePoints) {
		double linkRadius = this.meshSize/2.0;
		double pointAngle = 360.0 / nbrePoints;
		double[][] basePoints = new double[nbrePoints][]; 
		
		for (int i = 0; i < nbrePoints; i++) {
			basePoints[i] = new double[3];
			basePoints[i][0] = linkRadius * Math.cos(Math.toRadians(pointAngle*i));
			basePoints[i][1] = 0.0;
			basePoints[i][2] = linkRadius * Math.sin(Math.toRadians(pointAngle*i));
		}
		
		
		LinkedList<LinkedList<double[]>> renderingPoints = new LinkedList<LinkedList<double[]>>();
		LinkedList<double[]> currentRenderingPoints, previousRenderingPoints, nextRenderingPoints;
		Iterator<NodeRepresentation> bSplinePointsItr = bSplinePoints.iterator();
		NodeRepresentation previousNode = bSplinePointsItr.next();
		NodeRepresentation currentNode = bSplinePointsItr.next();
		NodeRepresentation nextNode = null;
		NodeRepresentation currentVector;
		
		double[] rotationParams;
		double[] previousRotationMatrix, nextRotationMatrix;
		double[] renderingPoint;
		double[] previousPoint, nextPoint, middlePoint;
		
		
		
		
		
		
		currentVector = currentNode.substract(previousNode);
		rotationParams = MathGeometry.getRotationParams(currentVector.getposXd(), currentVector.getposYd(), currentVector.getposZd());
		
		if (rotationParams == null) {
			previousRotationMatrix = MathGeometry.get3DIdentityMatrix();
		}
		else {
			previousRotationMatrix = MathGeometry.get3DRotationMatrix(rotationParams[0], rotationParams[1], rotationParams[2], rotationParams[3]);
		}
		
		currentRenderingPoints = new LinkedList<double[]>();
		for (int i = 0; i < nbrePoints; i++) {
			renderingPoint = MathGeometry.rotate3DPoint(previousRotationMatrix, basePoints[i]);
			renderingPoint[0] += previousNode.getposXd();
			renderingPoint[1] += previousNode.getposYd();
			renderingPoint[2] += previousNode.getposZd();
			
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
				
				currentVector = currentNode.substract(previousNode);
				rotationParams = MathGeometry.getRotationParams(currentVector.getposXd(), currentVector.getposYd(), currentVector.getposZd());
				
				if (rotationParams == null) {
					previousRotationMatrix = MathGeometry.get3DIdentityMatrix();
				}
				else {
					previousRotationMatrix = MathGeometry.get3DRotationMatrix(rotationParams[0], rotationParams[1], rotationParams[2], rotationParams[3]);
				}
				
				currentRenderingPoints = new LinkedList<double[]>();
				
				for (int i = 0; i < nbrePoints; i++) {
					renderingPoint = MathGeometry.rotate3DPoint(previousRotationMatrix, basePoints[i]);
					renderingPoint[0] += currentNode.getposXd();
					renderingPoint[1] += currentNode.getposYd();
					renderingPoint[2] += currentNode.getposZd();
					
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
				
				currentVector = currentNode.substract(previousNode);
				rotationParams = MathGeometry.getRotationParams(currentVector.getposXd(), currentVector.getposYd(), currentVector.getposZd());
				
				if (rotationParams == null) {
					previousRotationMatrix = MathGeometry.get3DIdentityMatrix();
				}
				else {
					previousRotationMatrix = MathGeometry.get3DRotationMatrix(rotationParams[0], rotationParams[1], rotationParams[2], rotationParams[3]);
				}
				
				currentVector = nextNode.substract(currentNode);
				rotationParams = MathGeometry.getRotationParams(currentVector.getposXd(), currentVector.getposYd(), currentVector.getposZd());
				
				if (rotationParams == null) {
					nextRotationMatrix = MathGeometry.get3DIdentityMatrix();
				}
				else {
					nextRotationMatrix = MathGeometry.get3DRotationMatrix(rotationParams[0], rotationParams[1], rotationParams[2], rotationParams[3]);
				}
				
				currentRenderingPoints = new LinkedList<double[]>();
				previousRenderingPoints = new LinkedList<double[]>();
				nextRenderingPoints = new LinkedList<double[]>();
				
				for (int i = 0; i < nbrePoints; i++) {
					renderingPoint = MathGeometry.rotate3DPoint(previousRotationMatrix, basePoints[i]);
					renderingPoint[0] += currentNode.getposXd();
					renderingPoint[1] += currentNode.getposYd();
					renderingPoint[2] += currentNode.getposZd();
					
					previousRenderingPoints.add(renderingPoint);
					
					renderingPoint = MathGeometry.rotate3DPoint(nextRotationMatrix, basePoints[i]);
					renderingPoint[0] += currentNode.getposXd();
					renderingPoint[1] += currentNode.getposYd();
					renderingPoint[2] += currentNode.getposZd();
					
					nextRenderingPoints.add(renderingPoint);
				}
				
				
				for (int i = 0; i < previousRenderingPoints.size(); i++) {
					previousPoint = previousRenderingPoints.get(i);
					nextPoint = nextRenderingPoints.get(i);
					
					middlePoint = new double[3];
					middlePoint[0] = previousPoint[0] + (nextPoint[0] - previousPoint[0])/2.0;
					middlePoint[1] = previousPoint[1] + (nextPoint[1] - previousPoint[1])/2.0;
					middlePoint[2] = previousPoint[2] + (nextPoint[2] - previousPoint[2])/2.0;
					
					currentRenderingPoints.add(middlePoint);
				}
				
				renderingPoints.add(currentRenderingPoints);
				
				previousNode = currentNode;
				currentNode = nextNode;
			}
		}
		
		
		/*
		currentRenderingPoints = new LinkedList<double[]>();
		currentNode = bSplinePoints.getFirst();
		for (int i = 0; i < nbrePoints; i++) {
			renderingPoint = new double[3];
			renderingPoint[0] = currentNode.getposXd();
			renderingPoint[1] = currentNode.getposYd();
			renderingPoint[2] = currentNode.getposZd();
			currentRenderingPoints.add(renderingPoint);
		}
		renderingPoints.addFirst(currentRenderingPoints);
		
		currentRenderingPoints = new LinkedList<double[]>();
		currentNode = bSplinePoints.getLast();
		for (int i = 0; i < nbrePoints; i++) {
			renderingPoint = new double[3];
			renderingPoint[0] = currentNode.getposXd();
			renderingPoint[1] = currentNode.getposYd();
			renderingPoint[2] = currentNode.getposZd();
			currentRenderingPoints.add(renderingPoint);
		}
		renderingPoints.addLast(currentRenderingPoints);
		*/


		
		
		
		
		
		currentVector = nextNode.substract(previousNode);
		rotationParams = MathGeometry.getRotationParams(currentVector.getposXd(), currentVector.getposYd(), currentVector.getposZd());
		
		if (rotationParams == null) {
			nextRotationMatrix = MathGeometry.get3DIdentityMatrix();
		}
		else {
			nextRotationMatrix = MathGeometry.get3DRotationMatrix(rotationParams[0], rotationParams[1], rotationParams[2], rotationParams[3]);
		}
		
		currentRenderingPoints = new LinkedList<double[]>();
		for (int i = 0; i < nbrePoints; i++) {
			renderingPoint = MathGeometry.rotate3DPoint(nextRotationMatrix, basePoints[i]);
			renderingPoint[0] += nextNode.getposXd();
			renderingPoint[1] += nextNode.getposYd();
			renderingPoint[2] += nextNode.getposZd();
			
			currentRenderingPoints.add(renderingPoint);;
		}
		
		renderingPoints.addLast(currentRenderingPoints);
		
		
		
		
		
		
		
		
		
		
		
		return renderingPoints;
	}
	
	
	
	private double evaluateBasisFunction(ArrayList<Double> knotsPoints, int i, double u, int p) {	
		if (p == 0) {
			if (u >= knotsPoints.get(i) && u < knotsPoints.get(i+1)) {			
				return 1.0;
			}
			else {
				return 0.0;
			}
		}
		else {
			double firstCoefficient;
			if (knotsPoints.get(i+p) - knotsPoints.get(i) == 0) {
				firstCoefficient = 0.0;
			}
			else {
				firstCoefficient = (u - knotsPoints.get(i)) / (knotsPoints.get(i+p) - knotsPoints.get(i));
			}
			
			double valeurRecursion_1 = evaluateBasisFunction(knotsPoints, i, u, p-1);
			
			
			double secondCoefficient;
			if (knotsPoints.get(i+p+1) - knotsPoints.get(i+1) == 0) {
				secondCoefficient = 0;
			}
			else {
				secondCoefficient = ((knotsPoints.get(i+p+1) - u) / (knotsPoints.get(i+p+1) - knotsPoints.get(i+1)));
			}
			
			double valeurRecursion_2 = evaluateBasisFunction(knotsPoints, i+1, u, p-1);
			
			return firstCoefficient*valeurRecursion_1 + secondCoefficient*valeurRecursion_2;
		}
	}
	
	private LinkedList<DirectLinkRepresentation> createBSplineSegments(PrimitiveColored coloredMesh, double meshSize, Color linkStartColor, Color linkEndColor, LinkedList<NodeRepresentation> bSplinePoints) {
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
			bSplineSegments.add(new DirectLinkRepresentation(previousNode, currNode, nbreInLinks, nbreOutLinks, coloredMesh, meshSize, lineSize, segmentStartColor, segmentEndColor, isBidirectional, linkBidirectionalColor));
			segmentStartColor = segmentEndColor;
			
			previousNode = currNode;
		}

		return bSplineSegments;
	}
	
	private void updateLinkEndsColor() {
		Color startColor = new Color(this.linkStartColor.getRed() / 255.0f, this.linkStartColor.getGreen() / 255.0f, this.linkStartColor.getBlue() / 255.0f);
		Color endColor = new Color(this.linkEndColor.getRed() / 255.0f, this.linkEndColor.getGreen() / 255.0f, this.linkEndColor.getBlue() / 255.0f);
		
		double inRatio = (double)this.nbreInLinks / (double)(this.nbreInLinks + this.nbreOutLinks);
		double outRatio = (double)this.nbreOutLinks / (double)(this.nbreInLinks + this.nbreOutLinks);
		this.linkStartColor = getColorAt(inRatio, startColor, endColor);
		this.linkEndColor = getColorAt(outRatio, startColor, endColor);
	}
	
	private Color evaluatePointColor(int pointPos) {
		/*
		double percentageBrightness = Math.log10(this.nbreInLinks + this.nbreOutLinks) / 2.0;
		
		if (percentageBrightness > 1.0) {
			percentageBrightness = 1.0;
		}
		
		float []linkStartColorHSB = Color.RGBtoHSB(this.linkStartColor.getRed(), this.linkStartColor.getGreen(), this.linkStartColor.getBlue(), null);
		float []linkEndColorHSB = Color.RGBtoHSB(this.linkEndColor.getRed(), this.linkEndColor.getGreen(), this.linkEndColor.getBlue(), null);
		
		
		
		double brightness = 1.0;
		double saturation = 1.0;
		*/
		
		/*
		if (percentageBrightness < 0.5) {
			saturation = 1.0;
			brightness = 0.5 + percentageBrightness;
		}
		else {
			saturation = 1.0 - (percentageBrightness - 0.5);
			brightness = 1.0;
		}		
		*/
		
		/*
		linkStartColorHSB[1] = (float)saturation;
		linkEndColorHSB[1] = (float)saturation;
		
		linkStartColorHSB[2] = (float)brightness;
		linkEndColorHSB[2] = (float)brightness;
		
		
		
		Color newLinkStartColor = new Color(Color.HSBtoRGB(linkStartColorHSB[0], linkStartColorHSB[1], linkStartColorHSB[2]));
		Color newLinkEndColor = new Color(Color.HSBtoRGB(linkEndColorHSB[0], linkEndColorHSB[1], linkEndColorHSB[2]));
		*/
		
		double endColorPercentage = this.nodesEdgeLength.get(this.bSplinePoints.get(pointPos)) / this.totalEdgeLength;
		
		if (endColorPercentage < (1.0/3.0)) {
			endColorPercentage = 0.0;
			
			//endColorPercentage /= 2.0;
		}
		else if (endColorPercentage > (2.0/3.0)) {
			endColorPercentage = 1.0;
			
			//endColorPercentage *= 5.0/4.0;
		}
		else {
			endColorPercentage = (endColorPercentage - (1.0/3.0)) * 3;
		}
		
		if (endColorPercentage > 1.0) {
			endColorPercentage = 1.0;
		}
		
		return getColorAt(endColorPercentage, this.linkStartColor, this.linkEndColor);
	}
	
	
	private Color getColorAt(double endColorPercentage, Color startColor, Color endColor) {
		double pointRedComponent;
		double pointGreenComponent;
		double pointBlueComponent;
		
		pointRedComponent = (startColor.getRed()*(1.0 - endColorPercentage)) / 255.0 + (endColor.getRed()*endColorPercentage) / 255.0;
		pointGreenComponent = (startColor.getGreen()*(1.0 - endColorPercentage)) / 255.0 + (endColor.getGreen()*endColorPercentage) / 255.0;
		pointBlueComponent = (startColor.getBlue()*(1.0 - endColorPercentage)) / 255.0 + (endColor.getBlue()*endColorPercentage) / 255.0;
		
		if (pointRedComponent > 1.0) {
			pointRedComponent = 1.0;
		}
		
		if (pointGreenComponent > 1.0) {
			pointGreenComponent = 1.0;
		}
		
		if (pointBlueComponent > 1.0) {
			pointBlueComponent = 1.0;
		}
		
		return new Color((float)pointRedComponent, (float)pointGreenComponent, (float)pointBlueComponent);
	}
	
	
	public void render(GL gl) {
		//System.out.println("RENDER EDGE BUNDLE");
		
		if (this.isFiltered) {
			return;
		}		
		
		gl.glLoadName(Scene.id);
		Scene.pickingEntities.put(Scene.id++, this);
		
		
		Iterator<LinkedList<double[]>> renderingPointsItr = this.renderingPoints.iterator();
		
		LinkedList<double[]> currentRenderingPoints = renderingPointsItr.next();
		
		
		/*
		NodeRepresentation centralNode = this.bSplinePoints.get(0);
		centralNode.setmeshSize(0.05);
		centralNode.setColor(Color.black);
		centralNode.render(gl);
		
		
		Iterator<NodeRepresentation> centralNodesItr = this.bSplinePoints.iterator();
		NodeRepresentation currentCentralNode = centralNodesItr.next();
		NodeRepresentation nextCentralNode;
		
		while (centralNodesItr.hasNext()) {
			nextCentralNode = centralNodesItr.next();
			
			DirectLinkRepresentation centralLink = new DirectLinkRepresentation(currentCentralNode, nextCentralNode, 10, 10, new CubeNoCapColored(Color.black, Color.black), 0.1, 3.0, Color.black, Color.white, false, null);
			centralLink.render(gl);

			
			currentCentralNode.setColor(Color.yellow);
			currentCentralNode.setmeshSize(0.2);
			currentCentralNode.render(gl);
			
			
			currentCentralNode = nextCentralNode;
		}	
		*/
		
		
		
		
		
		
		
		
		
		
		LinkedList<double[]> nextRenderingPoints;
		double[] vertex1, vertex2, vertex3, vertex4;
		double[] u, v, uXv;	
		int pointPos = 0;
		Color currentPointColor, nextPointColor;
		
		gl.glBegin(GL.GL_QUADS);			
			while (renderingPointsItr.hasNext()) {
				nextRenderingPoints = renderingPointsItr.next();
				
				/*
				if (this.animateLink) {
					this.positionPercentage += this.positionAugmentation;
					
					
				}
				else {
				*/	
					currentPointColor = this.evaluatePointColor(pointPos);
					nextPointColor = this.evaluatePointColor(pointPos+1);
				/*	
				}
				*/
				
				/*
				Color testColor;
				for (int i = 0; i < currentRenderingPoints.size(); i++) {
					testColor = Color.gray;
					
					if (i == 0)
						testColor = Color.pink;
					else if (i == 1)
						testColor = Color.cyan;
					else if (i == 2)
						testColor = Color.magenta;
					else if (i == 3)
						testColor = Color.white;
					
					double[] testVertex = currentRenderingPoints.get(i);
					NodeRepresentation testNode = new NodeRepresentation(null, null, 0.05, testVertex[0], testVertex[1], testVertex[2], testColor, null);
					testNode.render(gl);
				}
				*/
				
				
				
				
				
				
				
				
				
				
				for (int i = 0; i < currentRenderingPoints.size(); i++) {	
					vertex1 = currentRenderingPoints.get((i+1) % currentRenderingPoints.size());
					vertex2 = currentRenderingPoints.get(i);
					vertex3 = nextRenderingPoints.get(i);
					vertex4 = nextRenderingPoints.get((i+1) % currentRenderingPoints.size());
					
					
					u = new double[3];
					v = new double[3];
					
					NodeRepresentation previousNode = this.bSplinePoints.get(pointPos);
					NodeRepresentation nextNode = this.bSplinePoints.get(pointPos+1);
					
					u[0] = previousNode.getposXd();
					u[1] = previousNode.getposYd();
					u[2] = previousNode.getposZd();
					
					
					v[0] = nextNode.getposXd();
					v[1] = nextNode.getposYd();
					v[2] = nextNode.getposZd();
					
					/*
					u = new double[3];
					v = new double[3];
					
					u[0] = vertex2[0] - vertex1[0]; u[1] = vertex2[1] - vertex1[1]; u[2] = vertex2[2] - vertex1[2];
					v[0] = vertex3[0] - vertex2[0]; v[1] = vertex3[1] - vertex2[1]; v[2] = vertex3[2] - vertex2[2];
					
					uXv = MathGeometry.get3DCrossProduct(u, v);
					*/
					
					
					
						//gl.glNormal3d(uXv[0], uXv[1], uXv[2]);
						
						gl.glColor4f(currentPointColor.getRed()/255.0f, currentPointColor.getGreen()/255.0f, currentPointColor.getBlue()/255.0f, 0.50f);
						
						gl.glNormal3d(vertex1[0] - u[0], vertex1[1] - u[1], vertex1[2] - u[2]);
						gl.glVertex3d(vertex1[0], vertex1[1], vertex1[2]);
						
						gl.glNormal3d(vertex2[0] - u[0], vertex2[1] - u[1], vertex2[2] - u[2]);
						gl.glVertex3d(vertex2[0], vertex2[1], vertex2[2]);
						
						gl.glColor4f(nextPointColor.getRed()/255.0f, nextPointColor.getGreen()/255.0f, nextPointColor.getBlue()/255.0f, 0.50f);
						
						gl.glNormal3d(vertex3[0] - v[0], vertex3[1] - v[1], vertex3[2] - v[2]);
						gl.glVertex3d(vertex3[0], vertex3[1], vertex3[2]);
						
						gl.glNormal3d(vertex4[0] - v[0], vertex4[1] - v[1], vertex4[2] - v[2]);
						gl.glVertex3d(vertex4[0], vertex4[1], vertex4[2]);
						
				
				}
				
		
				currentRenderingPoints = nextRenderingPoints;
				pointPos++;
			}
		gl.glEnd();
		

		
		
		
		
		
		
		
		//LinkedList<NodeRepresentation> bSplinePoints = evaluateBSplinePoints(this.straightenedControlPoints, this.knotsPoints, this.nbreSegments, this.degree);
		
		
		/*
		LinkedList<DirectLinkRepresentation>  bSplineSegments = createBSplineSegments(this.coloredMesh, this.meshSize, this.linkStartColor, this.linkEndColor, this.bSplinePoints);
		
		for (DirectLinkRepresentation segment : bSplineSegments) {
			segment.render(gl);
		}
		*/
		
	}
}

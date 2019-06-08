package verso.representation.cubeLandscape.Layout.radial;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import verso.util.MathGeometry;
import verso.model.Package;
import verso.representation.Layout;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation.ORIENTATION_COLOR;
import verso.representation.cubeLandscape.representationModel.radial.RadialPackageRepresentation;

public class RadialLayout extends Layout {
	
	public static Logger LOG = Logger.getLogger(RadialLayout.class.getSimpleName());
	
	public void layout(RadialPackageRepresentation radialPackRep, float classesWidth, float packageWidth, int nbreOfRows, float spacesSize) {
		layout(radialPackRep, buildDefaultOrderedPackages(radialPackRep), classesWidth, packageWidth, nbreOfRows, spacesSize);
	}
	
	public void layout(RadialPackageRepresentation radialPackRep, HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> orderedPackages, float classesWidth, float packageWidth, int nbreOfRows, float spacesSize) {
		int nbreInternClasses = computeNbreInternClasses(radialPackRep, nbreOfRows);
		int nbreSpaces = computeNbreSpaces(radialPackRep);
		
		float layoutInternCirconference = (nbreInternClasses * classesWidth) + (nbreSpaces * spacesSize);
		float layoutInternRadius = layoutInternCirconference / (float)(2*Math.PI);
		float spaceAngle = MathGeometry.arcAngle(layoutInternRadius, spacesSize);
		
		float internPackageWidth;
		if (nbreOfRows*classesWidth > packageWidth) {
			internPackageWidth = nbreOfRows*classesWidth;
		} else {
			internPackageWidth = packageWidth;
		}	
		
		float layoutExternRadius = layoutInternRadius + internPackageWidth + (internPackageWidth * radialPackRep.computeMaxLevel());	

		radialPackRep.setInternRadius(layoutInternRadius);
		radialPackRep.setExternRadius(layoutExternRadius);
		radialPackRep.setAngleFirstSide(0.0f);
		radialPackRep.setLayoutInternRadius(layoutInternRadius);
		radialPackRep.updatePackageMesh();
		
		float angleSecondSide = computePackageSize(radialPackRep, orderedPackages, classesWidth, internPackageWidth, nbreOfRows, spaceAngle, ORIENTATION_COLOR.FIRSTCOLOR);
		
		radialPackRep.setAngleSecondSide(angleSecondSide);
	}
	
	private float computePackageSize(RadialPackageRepresentation radialPackRep, HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> orderedPackages, float classesWidth, float packageWidth, int nbreOfRows, float spaceAngle, ORIENTATION_COLOR levelColor) {
		radialPackRep.orientationColor = levelColor;

		if (!radialPackRep.isFakePackage()) {
			if (levelColor == ORIENTATION_COLOR.FIRSTCOLOR) {
				levelColor = ORIENTATION_COLOR.SECONDCOLOR;
			} else {
				levelColor = ORIENTATION_COLOR.FIRSTCOLOR;
			}
		}

//		float parentAngle = radialPackRep.getAngleSecondSide() - radialPackRep.getAngleFirstSide();
//		float anglePercentage;
		float angleFirstSide = radialPackRep.getAngleFirstSide();
		float angleSecondSide;	
//		int childPackNbreInternClasses;
		
		if (orderedPackages.get(radialPackRep).size() > 0) {
			angleFirstSide += spaceAngle;
		}
		
		for (RadialPackageRepresentation childRadialPackRep : orderedPackages.get(radialPackRep)) {			
			/*
			childPackNbreInternClasses = computeNbreInternClasses(childRadialPackRep, nbreOfRows);
			anglePercentage = (float)childPackNbreInternClasses / (float)nbreInternClasses;
			angleSecondSide = angleFirstSide + anglePercentage * parentAngle;
			*/

			childRadialPackRep.setInternRadius(radialPackRep.getLayoutInternRadius());
			childRadialPackRep.setExternRadius(radialPackRep.getExternRadius() - packageWidth);
			childRadialPackRep.setLayoutInternRadius(radialPackRep.getLayoutInternRadius());
			
			childRadialPackRep.setAngleFirstSide(angleFirstSide);
			
			angleSecondSide = computePackageSize(childRadialPackRep, orderedPackages, classesWidth, packageWidth, nbreOfRows, spaceAngle, levelColor);
			
			childRadialPackRep.setAngleSecondSide(angleSecondSide);
			
			childRadialPackRep.updatePackageMesh();
			
			
			
			angleFirstSide = angleSecondSide + spaceAngle;
		}
		
		
		
		
		RadialPackageRepresentation elementsPackage;
		
		
		
		if (radialPackRep.getElements().size() > 0) {			
			elementsPackage = new RadialPackageRepresentation(new Package(radialPackRep.getName() + "_elements"), null, null, 0.0f, null, null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, radialPackRep.getCenter(), null);
			elementsPackage.addElements(radialPackRep.getElements());
			radialPackRep.clearElements();
			radialPackRep.setElementsPackage(elementsPackage);
		}
		
		
		
		
		
		
		if (radialPackRep.getElementsPackage() != null) {
			elementsPackage = radialPackRep.getElementsPackage();
			
			float epsilon = 0.2f;
			
			int currPackNbreInternClasses = (int)Math.ceil((double)elementsPackage.getElements().size() / (double)nbreOfRows);
			
			//anglePercentage = (float)currPackNbreInternClasses / (float)nbreInternClasses;
			
			angleSecondSide = angleFirstSide + MathGeometry.arcAngle(radialPackRep.getInternRadius(), currPackNbreInternClasses);
			
			float elementsPackAngleFirstSide = angleFirstSide;
			
			
			int elementCounter = 0;
			float currRadius = radialPackRep.getLayoutInternRadius() + classesWidth/2.0f + epsilon;
			float elementAngleInterval = (angleSecondSide - angleFirstSide) / currPackNbreInternClasses;
			float arcLengthDifference;
			
			
			elementsPackage.setAngleFirstSide(elementsPackAngleFirstSide);
			elementsPackage.setAngleSecondSide(angleSecondSide);
			elementsPackage.setInternRadius(radialPackRep.getInternRadius());
			elementsPackage.setExternRadius(radialPackRep.getInternRadius() + ( ((elementsPackage.getElements().size() / currPackNbreInternClasses) + 1) * classesWidth));
			elementsPackage.setLayoutInternRadius(radialPackRep.getLayoutInternRadius());
			
			
			if (currPackNbreInternClasses == 1) {
				float tempElementAngleInterval = elementAngleInterval;
				
				for (ElementRepresentation elementRep : elementsPackage.getElements()) {
					arcLengthDifference = MathGeometry.arcLength(currRadius, angleSecondSide - angleFirstSide) - (currPackNbreInternClasses * classesWidth);
					elementAngleInterval = tempElementAngleInterval + MathGeometry.arcAngle(currRadius, arcLengthDifference / 2.0f);
					
					elementRep.setPosX((currRadius * (float)Math.cos(Math.toRadians(angleFirstSide + elementAngleInterval/2))) - classesWidth/2.0f);
					elementRep.setPosZ((currRadius * (float)Math.sin(Math.toRadians(angleFirstSide + elementAngleInterval/2))) - classesWidth/2.0f);
					
					currRadius += classesWidth;
				}
			}
			else {				
				for (ElementRepresentation elementRep : elementsPackage.getElements()) {
					elementRep.setPosX((currRadius * (float)Math.cos(Math.toRadians(angleFirstSide + elementAngleInterval/2))) - classesWidth/2.0f);
					elementRep.setPosZ((currRadius * (float)Math.sin(Math.toRadians(angleFirstSide + elementAngleInterval/2))) - classesWidth/2.0f);
					angleFirstSide += elementAngleInterval;
					
					
					elementCounter++;
					
					if (elementCounter % currPackNbreInternClasses == 0) {
						angleFirstSide = elementsPackAngleFirstSide;
						currRadius += classesWidth;
						
						arcLengthDifference = MathGeometry.arcLength(currRadius, angleSecondSide - angleFirstSide) - (currPackNbreInternClasses * classesWidth);
						elementAngleInterval = MathGeometry.arcAngle(currRadius, classesWidth + arcLengthDifference / (float)(currPackNbreInternClasses - 1));
					}
				}
			}
		}
		else {
			angleSecondSide = angleFirstSide;
		}
		
		return angleSecondSide;
	}
	
	private int computeNbreInternClasses(RadialPackageRepresentation radialPackRep, int nbreOfRows) {
		int nbreInternClasses = 0;
		
		for (RadialPackageRepresentation radialSubPack : radialPackRep.getRadialPackages()) {
			nbreInternClasses += computeNbreInternClasses(radialSubPack, nbreOfRows);
		}
		
		if (radialPackRep.getElements().size() > 0) {
			nbreInternClasses += (int)Math.ceil((double)radialPackRep.getElements().size() / (double)nbreOfRows);
		}
		
		return nbreInternClasses;
	}
	
	private int computeNbreSpaces(RadialPackageRepresentation radialPackRep) {
		int nbreSpaces = 0;
		
		for (RadialPackageRepresentation radialSubPack : radialPackRep.getRadialPackages()) {
			nbreSpaces += computeNbreSpaces(radialSubPack);
		}
		
		if (radialPackRep.getSubPackages().size() > 0) {
			nbreSpaces += (radialPackRep.getSubPackages().size() + 1);
		}
		
		return nbreSpaces;
	}
	
	
	public HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> computeBestLayout(RadialPackageRepresentation root, float classesWidth, float packageWidth, int nbreOfRows, float spacesSize, int kmax, float temp, float alpha, float tempTreshold) {
		HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> currSolution = buildDefaultOrderedPackages(root);
		HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> bestSolution = copySolution(currSolution);
		RadialPackageRepresentation changedPackage;
		
		layout(root, currSolution, classesWidth, packageWidth, nbreOfRows, spacesSize);
		
		//root.computeAbsolutePosition(0, 0);
		
		float currValue = getSolutionValue(root);
		float nextValue;
		float bestValue = currValue;
		float valueDiff;
		float acceptationProbability = 0.0f;
		
		LOG.config("RadialLayout.computeBestLayout()");
		LOG.config("Initial value: " + bestValue + "\n");
		
		while (temp > tempTreshold) {
			for (int i = 0; i < kmax; i++) {
				changedPackage = findNeighbourSolution(filterSolution(currSolution), currSolution);
				
				layout(root, currSolution, classesWidth, packageWidth, nbreOfRows, spacesSize);
				
				
				//root.computeAbsolutePosition(0, 0);
				
				
				nextValue = getSolutionValue(root);
				valueDiff = currValue - nextValue;

				//LOG.config("Value diff: " + valueDiff);

				if (valueDiff >= 0.0) {
					// if (valueDiff <= 0.0) {
					acceptationProbability = (float) Math.pow(Math.E, -(valueDiff / temp));

					if (!(Math.random() <= acceptationProbability)) {
						reverseSolutionChange(currSolution.get(changedPackage));
					} else {
						currValue = nextValue;
					}
				} else {
					currValue = nextValue;
				}

				if (currValue > bestValue) {
					// if (currValue < bestValue) {
					bestSolution = copySolution(currSolution);
					bestValue = currValue;
				}
			}
			
			temp = alpha * temp;
			
			LOG.config("Température: " + temp);
			LOG.config("Best value: " + bestValue + "\n");
		}
		
		layout(root, bestSolution, classesWidth, packageWidth, nbreOfRows, spacesSize);
		
		return bestSolution;
	}
	

	private HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> filterSolution(HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> solution) {
		HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> filteredSolution = copySolution(solution);
		RadialPackageRepresentation currPack;
		
		for (Iterator<RadialPackageRepresentation> packageItr = filteredSolution.keySet().iterator(); packageItr.hasNext();) {
			currPack = packageItr.next();
			
			if (solution.get(currPack).size() < 2) {
				packageItr.remove();
			}
		}
		
		return filteredSolution;
	}
	
	private void reverseSolutionChange(LinkedList<RadialPackageRepresentation> packagesList) {
		RadialPackageRepresentation tempPackage = packagesList.get(0);
		packagesList.remove(0);
		packagesList.add(tempPackage);
	}
	
	private HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> buildDefaultOrderedPackages(RadialPackageRepresentation root) {
		HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> orderedPackages = new HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>>();
		LinkedList<RadialPackageRepresentation> orderedPackagesList = new LinkedList<RadialPackageRepresentation>();
		
		for (RadialPackageRepresentation subPack : root.getRadialPackages()) {
			orderedPackagesList.add(subPack);
		}
		orderedPackages.put(root, orderedPackagesList);
		
		
		for (RadialPackageRepresentation subPack : root.getRadialPackages()) {
			orderedPackages.putAll(buildDefaultOrderedPackages(subPack));
		}
		
		return orderedPackages;
	}
	
	private RadialPackageRepresentation findNeighbourSolution(HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> filteredPackages, HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> orderedPackages) {
		int randomPackageIndex = (int)(Math.random() * filteredPackages.size());
		RadialPackageRepresentation packagesSet[] = new RadialPackageRepresentation[filteredPackages.keySet().size()];
		filteredPackages.keySet().toArray(packagesSet);
		
		
		RadialPackageRepresentation randomPackage = packagesSet[randomPackageIndex];
		
		LinkedList<RadialPackageRepresentation> randomPackageList = orderedPackages.get(randomPackage);
		
		if (randomPackageList.size() >= 2) {
			int randomInd1 = 0;
			int randomInd2 = 0;

			while (randomInd1 == randomInd2) {
				randomInd1 = (int)(Math.random() * randomPackageList.size());
				randomInd2 = (int)(Math.random() * randomPackageList.size());
			}
			
			RadialPackageRepresentation tempPackage1 = randomPackageList.get(randomInd1);
			RadialPackageRepresentation tempPackage2 = randomPackageList.get(randomInd2);
			
			if (randomInd2 > randomInd1) {
				randomPackageList.remove(randomInd2);
				randomPackageList.remove(randomInd1);
				randomPackageList.add(randomInd1, tempPackage2);
				randomPackageList.add(randomInd2, tempPackage1);
			}
			else {
				randomPackageList.remove(randomInd1);
				randomPackageList.remove(randomInd2);
				randomPackageList.add(randomInd2, tempPackage1);
				randomPackageList.add(randomInd1, tempPackage2);
			}
			

		}
		else {
			LOG.config("Erreur: moins de 2 sous-packages.");
			LOG.config("Nom package: " + randomPackage.getSimpleName());
			LOG.config("Nombre de package: " + randomPackageList.size());
		}
		
		return randomPackage;
	}
	
	private HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> copySolution(HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> orderedPackages) {
		HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>> copy = new HashMap<RadialPackageRepresentation, LinkedList<RadialPackageRepresentation>>();
		LinkedList<RadialPackageRepresentation> tempList;
		
		for (RadialPackageRepresentation pack : orderedPackages.keySet()) {
			tempList = new LinkedList<RadialPackageRepresentation>();
			tempList.addAll(orderedPackages.get(pack));
			copy.put(pack, tempList);
		}
		
		return copy;
	}
	
	
	private float getSolutionValue(RadialPackageRepresentation root) {
		float solutionValue = 0.0f;
		HashMap<String, ElementRepresentation> sortedElements = new HashMap<String, ElementRepresentation>();
		LinkedList<ElementRepresentation> solutionElements = new LinkedList<ElementRepresentation>();
		solutionElements.addAll(root.getAllElements());

		for (ElementRepresentation element : solutionElements) {			
			sortedElements.put(element.getSimpleName(), element);
		}
		
		float[] firstPoint = new float[2];
		float[] secondPoint = new float[2];
		
		for (ElementRepresentation element : solutionElements) {
			for (String targetName : element.getElementModel().getTargets()) {
				//firstPoint[0] = element.getAbsolutePosX();
				//firstPoint[1] = element.getAbsolutePosZ();
				
				firstPoint[0] = element.getPosX();
				firstPoint[1] = element.getPosZ();
				
				if (sortedElements.get(targetName) != null) {
					//secondPoint[0] = sortedElements.get(targetName).getAbsolutePosX();
					//secondPoint[1] = sortedElements.get(targetName).getAbsolutePosZ();
					
					secondPoint[0] = sortedElements.get(targetName).getPosX();
					secondPoint[1] = sortedElements.get(targetName).getPosZ();
				}
				
				solutionValue += MathGeometry.getPositiveDistance(firstPoint, secondPoint);
			}
		}
		
		return solutionValue;
	}
}

package verso.representation.cubeLandscape.Layout.radial;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import verso.util.MathGeometry;
import verso.model.Package;
import verso.representation.cubeLandscape.representationModel.ColiseumPackageRepresentation;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;

public class ColiseumLayout {
	
	public static Logger LOG = Logger.getLogger(ColiseumLayout.class.getSimpleName());

	public void layout(ColiseumPackageRepresentation coliseumPackRep, float classesWidth, float packageWidth, float levelHeight, int nbreOfRows, float spacesSize) {
		layout(coliseumPackRep, buildDefaultOrderedPackages(coliseumPackRep), classesWidth, packageWidth, levelHeight, nbreOfRows, spacesSize);
	}
	
	public void layout(ColiseumPackageRepresentation coliseumPackRep, HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> orderedPackages, float classesWidth, float packageWidth, float levelHeight, int nbreOfRows, float spacesSize) {		
		float internPackageWidth;
		if (nbreOfRows*classesWidth > packageWidth) {
			internPackageWidth = nbreOfRows*classesWidth;
		}
		else {
			internPackageWidth = packageWidth;
		}	
		
		HashMap<ColiseumPackageRepresentation, Float[]> packagesArcsValues = new HashMap<ColiseumPackageRepresentation, Float[]>();
		float layoutInternCirconference = this.computeLayoutInternArcLength(coliseumPackRep, nbreOfRows, classesWidth, spacesSize, packagesArcsValues);
		
		
		int nbreLayoutIteration = 0;
		float angleSecondSide = 0.0f;
		
		while (angleSecondSide < 359.0f && nbreLayoutIteration < 10) {
			float layoutInternRadius = layoutInternCirconference / (float)(2*Math.PI);
			float layoutExternRadius = layoutInternRadius + internPackageWidth + (internPackageWidth * coliseumPackRep.computeMaxLevel());

			coliseumPackRep.setInternRadius(layoutInternRadius);
			coliseumPackRep.setExternRadius(layoutExternRadius);
			coliseumPackRep.setInternBorderRadius(coliseumPackRep.getExternRadius() - internPackageWidth);
			coliseumPackRep.setAngleFirstSide(0.0f);
			coliseumPackRep.setLayoutInternRadius(layoutInternRadius);
			coliseumPackRep.updatePackageMesh();
			
			float rootHeight = coliseumPackRep.getPackageLevel() * levelHeight;
			
			coliseumPackRep.setHeight(rootHeight);
			
			angleSecondSide = computePackageSize(coliseumPackRep, orderedPackages, classesWidth, internPackageWidth, rootHeight, levelHeight, nbreOfRows, PackageRepresentation.ORIENTATION_COLOR.FIRSTCOLOR, packagesArcsValues);
			
			coliseumPackRep.setAngleSecondSide(angleSecondSide);
			
			/*
			float missingAngle = 360.0f - angleSecondSide;
			float missingAngleArcLength = MathGeometry.arcLength(coliseumPackRep.getInternRadius(), missingAngle);
			layoutInternCirconference -= missingAngleArcLength;
			*/
			
			
			/*
			float rootInternArcLength = MathGeometry.arcLength(coliseumPackRep.getExternRadius() - internPackageWidth, coliseumPackRep.getAngleSecondSide());
			layoutInternRadius = (rootInternArcLength / (float)(2*Math.PI)) - (internPackageWidth * coliseumPackRep.computeMaxLevel());
			layoutExternRadius = layoutInternRadius + internPackageWidth + (internPackageWidth * coliseumPackRep.computeMaxLevel());
			*/
			
			
			layoutInternCirconference = MathGeometry.arcLength(layoutInternRadius, coliseumPackRep.getAngleSecondSide());
			//layoutInternCirconference -= (layoutInternCirconference * 0.15);
		}
		
		
		
		
		
		
		
		
		
		
		/*
		int nbreInternClasses = computeNbreInternClasses(coliseumPackRep, nbreOfRows);
		int nbreSpaces = computeNbreSpaces(coliseumPackRep);
		
		float layoutInternCirconference = (nbreInternClasses * classesWidth) + (nbreSpaces * spacesSize);
		float layoutInternRadius = layoutInternCirconference / (float)(2*Math.PI);

		float internPackageWidth;
		if (nbreOfRows*classesWidth > packageWidth) {
			internPackageWidth = nbreOfRows*classesWidth;
		}
		else {
			internPackageWidth = packageWidth;
		}	
		
		float spaceAngle = MathGeometry.arcAngle(layoutInternRadius, spacesSize);
		float layoutExternRadius = layoutInternRadius + internPackageWidth + (internPackageWidth * coliseumPackRep.computeMaxLevel());	
		
		
		LOG.config("Coliseum layout intern radius: " + layoutInternRadius);
		LOG.config("Coliseum layout extern radius: " + layoutExternRadius);
		
		
		coliseumPackRep.setInternRadius(layoutInternRadius);
		coliseumPackRep.setExternRadius(layoutExternRadius);
		coliseumPackRep.setInternBorderRadius(coliseumPackRep.getExternRadius() - internPackageWidth);
		coliseumPackRep.setAngleFirstSide(0.0f);
		coliseumPackRep.setLayoutInternRadius(layoutInternRadius);
		coliseumPackRep.updatePackageMesh();
		
		float rootHeight = coliseumPackRep.getPacLevel() * levelHeight;
		
		coliseumPackRep.setHeight(rootHeight);
		
		float angleSecondSide = computePackageSize(coliseumPackRep, orderedPackages, classesWidth, internPackageWidth, rootHeight, levelHeight, nbreOfRows, spaceAngle, RadialPackageRepresentation.FIRSTCOLOR);
		
		coliseumPackRep.setAngleSecondSide(angleSecondSide);
		*/
		
		
		
		
		
		
		
		
		
		
		
		
		
		/*
		flushElementsPackages(coliseumPackRep);
		
		float internPackageWidth;
		if (nbreOfRows*classesWidth > packageWidth) {
			internPackageWidth = nbreOfRows*classesWidth;
		}
		else {
			internPackageWidth = packageWidth;
		}
		
		////
		float layoutInternRadius = computeInternRadius(coliseumPackRep, coliseumPackRep.computeMaxLevel(), internPackageWidth, classesWidth, nbreOfRows, bordersSize);
		computePackageSize(coliseumPackRep, layoutInternRadius, 0, coliseumPackRep.computeMaxLevel(), classesWidth, internPackageWidth, levelHeight, nbreOfRows, bordersSize);
	
		float openAngle = 360.f - coliseumPackRep.getAngleSecondSide();
		coliseumPackRep.setAngleSecondSide(360.0f);
	
		closePackages(coliseumPackRep, openAngle);
		////
		
		
		setPackagesProperties(coliseumPackRep, classesWidth, packageWidth, nbreOfRows, bordersSize);
		
		placePackages(coliseumPackRep, orderedPackages, internPackageWidth, classesWidth, nbreOfRows);
		*/
	}
	
	
	void flushElementsPackages(ColiseumPackageRepresentation coliseumPackRep) {
		ColiseumPackageRepresentation subPack;
		for (Iterator<ColiseumPackageRepresentation> subPacksItr = coliseumPackRep.getColiseumPackages().iterator(); subPacksItr.hasNext();) {
			subPack = subPacksItr.next();
			
			if (subPack.getSimpleName().contains("_elementsPackage")) {
				coliseumPackRep.addElements(subPack.getElements());
				coliseumPackRep.removePackage(subPack);
			}
			
			flushElementsPackages(subPack);
		}
	}
	
	
	void setPackagesProperties(ColiseumPackageRepresentation root, float classesWidth, float packageWidth, int nbreOfRows, float bordersSize) {
		HashMap<ColiseumPackageRepresentation, Float> packagesArcLength = new HashMap<ColiseumPackageRepresentation, Float>();
		float layoutInternCirconference = computePackageArcLength(root, classesWidth, nbreOfRows, bordersSize, packagesArcLength);
		float layoutInternRadius = (float)(layoutInternCirconference / (2 * Math.PI));
		
		float rootInternRadius = layoutInternRadius + (root.getPackageLevel() * packageWidth);
		computePackagesAngle(root, 360.0f, rootInternRadius, layoutInternRadius, packageWidth, packagesArcLength);
	}
	
	
	
	private float computePackageArcLength(ColiseumPackageRepresentation pack, float classesWidth, int nbreOfRows, float bordersSize, HashMap<ColiseumPackageRepresentation, Float> packagesArcLength) {
		float arcLength = 0.0f;
		
		if (pack.getPackages().size() == 0) {
			arcLength = this.computeNbreInternClasses(pack, nbreOfRows) * classesWidth;
			packagesArcLength.put(pack, arcLength);
			
			return arcLength;
		}
		else {
			for (ColiseumPackageRepresentation subPack : pack.getColiseumPackages()) {
				arcLength += computePackageArcLength(subPack, classesWidth, nbreOfRows, bordersSize, packagesArcLength);
			}
			
			arcLength += ((pack.getColiseumPackages().size() + 1) * bordersSize);
			
			arcLength += this.computeNbreInternClasses(pack, nbreOfRows) * classesWidth;
		
			packagesArcLength.put(pack, arcLength);
			
			return arcLength;
		}
	}
	
	
	private float computePackagesAngle(ColiseumPackageRepresentation pack, float angle, float internRadius, float layoutInternRadius, float packageWidth, HashMap<ColiseumPackageRepresentation, Float> packagesArcLength) {
		pack.setAngleSecondSide(angle);
		
		float currSubPackAngle;
		float minInternRadius = internRadius;
		float currInternRadius;
		
		for (ColiseumPackageRepresentation subPack : pack.getColiseumPackages()) {
			currSubPackAngle = (packagesArcLength.get(subPack) / packagesArcLength.get(pack));
			currInternRadius = minInternRadius = computePackagesAngle(subPack, currSubPackAngle, (internRadius - packageWidth), layoutInternRadius, packageWidth, packagesArcLength);
			
			if (currInternRadius < minInternRadius) {
				minInternRadius = currInternRadius;
			}
		}
		
		pack.setExternRadius(internRadius + packageWidth);
		pack.setInternBorderRadius(internRadius);
		pack.setInternBorderRadius(minInternRadius);
		pack.setLayoutInternRadius(layoutInternRadius);
		
		return minInternRadius;
	}
	
	
	
	
	/*
	private float computeInternRadius(ColiseumPackageRepresentation coliseumPackRep, int maxLevel, float levelWidth, float classesWidth, int nbreOfRows, float bordersSize) {
		HashMap<Integer, LinkedList<ColiseumPackageRepresentation>> sortedPackagesByLevel = new HashMap<Integer, LinkedList<ColiseumPackageRepresentation>>();
		HashMap<ColiseumPackageRepresentation, Float> packagesInternArcLength = new HashMap<ColiseumPackageRepresentation, Float>();
		HashMap<ColiseumPackageRepresentation, Float> packagesExternArcLength = new HashMap<ColiseumPackageRepresentation, Float>();
		
		sortPackagesByLevel(coliseumPackRep, 0, sortedPackagesByLevel);
		
		LinkedList<ColiseumPackageRepresentation> currPackages;
		float layoutRadius = 0.0f;
		float currRadius = 0.0f;
		float currArcLength;
		float totalSubPackArcLength;
		float totalArcLength;
		
		int currLevel = maxLevel; 
		while (currLevel >= 0) {
			currPackages = sortedPackagesByLevel.get(currLevel);
			totalArcLength = 0.0f;
			
			for (ColiseumPackageRepresentation pack : currPackages) {
				if (pack.getPackages().size() == 0) {
					currArcLength = this.computeNbreInternClasses(pack, nbreOfRows) * classesWidth;
					packagesInternArcLength.put(pack, currArcLength);
					totalArcLength += currArcLength;
				}
				else {
					currArcLength = this.computeNbreInternClasses(pack, nbreOfRows) * classesWidth;
					
					totalSubPackArcLength = 0.0f;
					
					for (ColiseumPackageRepresentation subPack : pack.getColiseumPackages()) {
						totalSubPackArcLength += packagesExternArcLength.get(subPack) + (2 * bordersSize);
					}

					currArcLength += totalSubPackArcLength;
					
					
					
					////
					if (currArcLength < totalSubPackArcLength) {
						currArcLength = totalSubPackArcLength;
					}
					////
					
					
					
					packagesInternArcLength.put(pack, currArcLength);
					totalArcLength += currArcLength;
				}
			}
			
			currRadius = totalArcLength / (2 * (float)Math.PI);
			layoutRadius = currRadius - (levelWidth * Math.abs(currLevel - maxLevel));
			
			float currExternArcLength;
			
			for (ColiseumPackageRepresentation pack : currPackages) {

				currExternArcLength = MathGeometry.arcLength(currRadius + levelWidth, MathGeometry.arcAngle(currRadius, packagesInternArcLength.get(pack)));
				packagesExternArcLength.put(pack, currExternArcLength);
			}

			currLevel--;
		}
		

		
		
		
		////
		int i = 0;
		while (sortedPackagesByLevel.get(i) != null) {
			double levelRadius = layoutRadius + (Math.abs(i - maxLevel) * levelWidth);
			double levelArcLength = 0.0;
			
			for (ColiseumPackageRepresentation pack : sortedPackagesByLevel.get(i)) {
				levelArcLength += (this.computeNbreInternClasses(pack, nbreOfRows) * classesWidth);
			}
			
			double levelMinRadius = levelArcLength / (2 * Math.PI);
			
			
			
			LOG.config("");
			LOG.config("Level : " + i);
			LOG.config("Level radius: " + levelRadius);
			LOG.config("Level min radius: " + levelMinRadius);
			LOG.config("");
			
			
			
			i++;
		}
		////
		
		
		
		
		
		return layoutRadius;
	}
	
	private void sortPackagesByLevel(ColiseumPackageRepresentation coliseumPackRep, int currLevel, HashMap<Integer, LinkedList<ColiseumPackageRepresentation>> sortedPackagesByLevel) {
		if (sortedPackagesByLevel.get(currLevel) == null) {
			sortedPackagesByLevel.put(currLevel, new LinkedList<ColiseumPackageRepresentation>());
		}
		
		sortedPackagesByLevel.get(currLevel).add(coliseumPackRep);
		
		for (ColiseumPackageRepresentation subPackRep : coliseumPackRep.getColiseumPackages()) {
			sortPackagesByLevel(subPackRep, currLevel+1, sortedPackagesByLevel);
		}
	}
	
	
	private void computePackageSize(ColiseumPackageRepresentation coliseumPackRep, float layoutInternRadius, int currLevel, int maxLevel, float classesWidth, float levelWidth, float levelHeight, int nbreOfRows, float bordersSize) {
		//coliseumPackRep.setHeight((Math.abs(currLevel - maxLevel) + 1) * levelHeight);
		coliseumPackRep.setLayoutInternRadius(layoutInternRadius);
		
		if (coliseumPackRep.getColiseumPackages().size() == 0) {
			coliseumPackRep.setInternRadius((levelWidth * Math.abs(currLevel - maxLevel)) + layoutInternRadius);
			coliseumPackRep.setExternRadius(coliseumPackRep.getInternRadius() + levelWidth);
			coliseumPackRep.setInternBorderRadius((levelWidth * Math.abs(currLevel - maxLevel)) + layoutInternRadius);
			coliseumPackRep.setAngleFirstSide(0.0f);
		
			float packageArcLength = computeNbreInternClasses(coliseumPackRep, nbreOfRows) * classesWidth;
			coliseumPackRep.setAngleSecondSide(MathGeometry.arcAngle(coliseumPackRep.getInternRadius(), packageArcLength));
		}
		else {
			float smallestInternRadius = Float.MAX_VALUE;
			float childPackagesTotalAngle = 0.0f;
			
			int nextLevel;
			for (ColiseumPackageRepresentation childColiseumPackRep : coliseumPackRep.getColiseumPackages()) {
				if (childColiseumPackRep.isFakePackage()) {
					nextLevel = currLevel;
				}
				else {
					nextLevel = currLevel+1;
				}
				computePackageSize(childColiseumPackRep, layoutInternRadius, nextLevel, maxLevel, classesWidth, levelWidth, levelHeight, nbreOfRows, bordersSize);
				
				if (childColiseumPackRep.getInternRadius() < smallestInternRadius) {
					smallestInternRadius = childColiseumPackRep.getInternRadius();
				}
				
				childPackagesTotalAngle += childColiseumPackRep.getAngleSecondSide() + (MathGeometry.arcAngle(childColiseumPackRep.getExternRadius(), bordersSize*2));
			}

			coliseumPackRep.setInternRadius(smallestInternRadius);
			coliseumPackRep.setExternRadius(coliseumPackRep.getColiseumPackages().iterator().next().getExternRadius() + levelWidth);
			coliseumPackRep.setInternBorderRadius((levelWidth * Math.abs(currLevel - maxLevel)) + layoutInternRadius);
			coliseumPackRep.setAngleFirstSide(0.0f);
		
			float packageArcLength = computeNbreInternClasses(coliseumPackRep, nbreOfRows) * classesWidth;
			float packageAngle = MathGeometry.arcAngle(coliseumPackRep.getExternRadius() - levelWidth, packageArcLength);
			
			
			coliseumPackRep.setAngleSecondSide(packageAngle + childPackagesTotalAngle);
			
			
			////
			if (packageAngle > childPackagesTotalAngle) {
				coliseumPackRep.setAngleSecondSide(packageAngle);
			}
			else {
				coliseumPackRep.setAngleSecondSide(childPackagesTotalAngle);
			}
			////
		}
	}
	
	
	
	
	private void closePackages(ColiseumPackageRepresentation coliseumPackRep, float openAngle) {
		if (coliseumPackRep.getColiseumPackages().size() > 0) {
			float bonusAngle;
			
			if (coliseumPackRep.getColiseumPackages().size() > 1) {
				bonusAngle = (openAngle / 2.0f) / (float)coliseumPackRep.getColiseumPackages().size();
			}
			else {
				bonusAngle = openAngle;
			}
			
			for (ColiseumPackageRepresentation subPack : coliseumPackRep.getColiseumPackages()) {
				subPack.setAngleSecondSide(subPack.getAngleSecondSide() + bonusAngle);
				
				closePackages(subPack, bonusAngle);
			}
		}
	}
	*/
	
	
	
	void placePackages(ColiseumPackageRepresentation coliseumPackRep, HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> orderedPackages, float levelWidth, float classesWidth, int nbreOfRows) {
		float packageArcLength = computeNbreInternClasses(coliseumPackRep, nbreOfRows) * classesWidth;
		float packageAngle = MathGeometry.arcAngle(coliseumPackRep.getExternRadius() - levelWidth, packageArcLength);
		
		if (coliseumPackRep.getColiseumPackages().size() > 0) {
			float subPackagesTotalAngle = 0.0f;
			//for (ColiseumPackageRepresentation childColiseumPackRep : coliseumPackRep.getColiseumPackages()) {
			for (ColiseumPackageRepresentation childColiseumPackRep : orderedPackages.get(coliseumPackRep)) {
				subPackagesTotalAngle += childColiseumPackRep.getAngleSecondSide();
			}
			
			
			float angleDifference;
			float angleInterval;
			float currAngle;
			
			
			
			
			angleDifference = (coliseumPackRep.getAngleSecondSide() - coliseumPackRep.getAngleFirstSide() - packageAngle) - subPackagesTotalAngle;
			
			
			
			if (coliseumPackRep.getAngleSecondSide() == 360.0) {
				angleInterval = angleDifference / coliseumPackRep.getColiseumPackages().size();
				currAngle = coliseumPackRep.getAngleFirstSide();
			}
			else {

				
				
		
				angleInterval = angleDifference / (coliseumPackRep.getColiseumPackages().size() + 1);		
				
				currAngle = coliseumPackRep.getAngleFirstSide() + angleInterval;
				
				
			
				
			}
				
				
				
				
				
			//for (ColiseumPackageRepresentation childColiseumPackRep : coliseumPackRep.getColiseumPackages()) {
			for (ColiseumPackageRepresentation childColiseumPackRep : orderedPackages.get(coliseumPackRep)) {
				childColiseumPackRep.setAngleFirstSide(currAngle);
				childColiseumPackRep.setAngleSecondSide(currAngle + childColiseumPackRep.getAngleSecondSide());
				currAngle = childColiseumPackRep.getAngleSecondSide() + angleInterval;
			}
			
			//for (ColiseumPackageRepresentation childColiseumPackRep : coliseumPackRep.getColiseumPackages()) {
			for (ColiseumPackageRepresentation childColiseumPackRep : orderedPackages.get(coliseumPackRep)) {
				placePackages(childColiseumPackRep, orderedPackages, levelWidth, classesWidth, nbreOfRows);
			}
		}
		

		
		
		
		
		if (coliseumPackRep.getElements().size() > 0) {
			float currPackDisplayInternRadius;
			
			if (coliseumPackRep.getColiseumPackages().size() > 0) {
				currPackDisplayInternRadius = coliseumPackRep.getColiseumPackages().iterator().next().getExternRadius();
			}
			else {
				currPackDisplayInternRadius = coliseumPackRep.getInternRadius();
			}
			
			if (!coliseumPackRep.isFakePackage()) {
				ColiseumPackageRepresentation elementsPackage = new ColiseumPackageRepresentation(new Package(coliseumPackRep.getPackage().getName() + "_elementsPackage") ,null, null, 0.0f, null, null, coliseumPackRep.getAngleSecondSide() - packageAngle, coliseumPackRep.getAngleSecondSide(), currPackDisplayInternRadius, coliseumPackRep.getExternRadius(), currPackDisplayInternRadius, coliseumPackRep.getLayoutInternRadius(), coliseumPackRep.getCenter(), null);

				//elementsPackage.setHeight(coliseumPackRep.getHeight());
				
				elementsPackage.addElements(coliseumPackRep.getElements());
				elementsPackage.setFake();
				
				placePackages(elementsPackage, orderedPackages, levelWidth, classesWidth, nbreOfRows);
				
				coliseumPackRep.addPackage(elementsPackage);
				coliseumPackRep.clearElements();
			}
			else {
				currPackDisplayInternRadius += classesWidth/2.0;
				
				int currPackNbreInternClasses = this.computeNbreInternClasses(coliseumPackRep, nbreOfRows);
				float currPackMinAngle = MathGeometry.arcAngle(currPackDisplayInternRadius, currPackNbreInternClasses * classesWidth); 	
//				float arcAngleDifference = (coliseumPackRep.getAngleSecondSide() - coliseumPackRep.getAngleFirstSide()) - currPackMinAngle;
				
				
				float realNbreOfRows = (float)Math.ceil((double)coliseumPackRep.getElements().size() / (double)this.computeNbreInternClasses(coliseumPackRep, nbreOfRows));
				float realWidth = classesWidth * realNbreOfRows;
	
				if (realWidth < levelWidth) {
					currPackDisplayInternRadius += (levelWidth - realWidth)/2.0;
				}
				
				
				int elementCounter = 0;
				
				
				//double currAngle = coliseumPackRep.getAngleFirstSide() + (arcAngleDifference / 2.0);
				float currAngle = coliseumPackRep.getAngleSecondSide() - packageAngle;
				
				
				float elementAngleInterval = currPackMinAngle / currPackNbreInternClasses;
				float arcLengthDifference;
				
				for (ElementRepresentation elementRep : coliseumPackRep.getElements()) {
					elementRep.setPosX((currPackDisplayInternRadius * (float)Math.cos(Math.toRadians(currAngle + elementAngleInterval/2))) - classesWidth/2.0f);
					elementRep.setPosZ((currPackDisplayInternRadius * (float)Math.sin(Math.toRadians(currAngle + elementAngleInterval/2))) - classesWidth/2.0f);
					
					currAngle += elementAngleInterval;
					
					
					elementCounter++;
					
					if (elementCounter % currPackNbreInternClasses == 0) {
						//currAngle = coliseumPackRep.getAngleFirstSide() + (arcAngleDifference / 2.0);
						currAngle = coliseumPackRep.getAngleSecondSide() - packageAngle;
						
						
						currPackDisplayInternRadius += classesWidth;
						
						arcLengthDifference = MathGeometry.arcLength(currPackDisplayInternRadius, currPackMinAngle) - (currPackNbreInternClasses * classesWidth);
						elementAngleInterval = MathGeometry.arcAngle(currPackDisplayInternRadius, classesWidth + arcLengthDifference / (float)(currPackNbreInternClasses));
					}
				}
			}
		}
	}

	
	
	/*
	private int computeNbreInternClasses(ColiseumPackageRepresentation coliseumPackRep, int nbreOfRows) {
		if (coliseumPackRep.getElements().size() > 0) {
			return (int)Math.ceil((double)coliseumPackRep.getElements().size() / (double)nbreOfRows);
		}
		else {
			return 0;
		}
	}
	*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	private float computePackageSize(ColiseumPackageRepresentation coliseumPackRep, HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> orderedPackages, float classesWidth, float packageWidth, float packageHeight, float levelHeight, int nbreOfRows, float spaceAngle, int levelColor) {
		coliseumPackRep.orientationColor = levelColor;
		
		if (!coliseumPackRep.isFakePackage()) {
			if (levelColor == ColiseumPackageRepresentation.FIRSTCOLOR) {
				levelColor = ColiseumPackageRepresentation.SECONDCOLOR;
			}
			else {
				levelColor = ColiseumPackageRepresentation.FIRSTCOLOR;
			}
		}

		float angleFirstSide = coliseumPackRep.getAngleFirstSide();
		float angleSecondSide;	
		
		if (orderedPackages.get(coliseumPackRep).size() > 0) {
			angleFirstSide += spaceAngle;
		}
		
		for (ColiseumPackageRepresentation childColiseumPackRep : orderedPackages.get(coliseumPackRep)) {			
			childColiseumPackRep.setHeight(packageHeight - levelHeight);
			
			childColiseumPackRep.setExternRadius(coliseumPackRep.getExternRadius() - packageWidth);
			childColiseumPackRep.setInternBorderRadius(childColiseumPackRep.getExternRadius() - packageWidth);
			childColiseumPackRep.setInternRadius(childColiseumPackRep.getExternRadius() - (childColiseumPackRep.getPacLevel() * packageWidth));
			childColiseumPackRep.setLayoutInternRadius(coliseumPackRep.getLayoutInternRadius());	
			childColiseumPackRep.setAngleFirstSide(angleFirstSide);
			
			angleSecondSide = computePackageSize(childColiseumPackRep, orderedPackages, classesWidth, packageWidth, packageHeight - levelHeight, levelHeight, nbreOfRows, spaceAngle, levelColor);
			childColiseumPackRep.setAngleSecondSide(angleSecondSide);
			
			childColiseumPackRep.updatePackageMesh();
			
			angleFirstSide = angleSecondSide + spaceAngle;
		}
		
		
		if (coliseumPackRep.getElements().size() > 0) {
			float epsilon = 0.2f;
			
			int currPackNbreInternClasses = (int)Math.ceil((double)coliseumPackRep.getElements().size() / (double)nbreOfRows);
			
			angleSecondSide = angleFirstSide + MathGeometry.arcAngle(coliseumPackRep.getInternRadius(), currPackNbreInternClasses);
			
			float elementsPackAngleFirstSide = angleFirstSide;
			
			
			int elementCounter = 0;
			float currRadius = (coliseumPackRep.getExternRadius() - packageWidth) + classesWidth/2.0f + epsilon;
			float elementAngleInterval = (angleSecondSide - angleFirstSide) / currPackNbreInternClasses;
			float arcLengthDifference;
			
			if (currPackNbreInternClasses == 1) {
				float tempElementAngleInterval = elementAngleInterval;
				
				for (ElementRepresentation elementRep : coliseumPackRep.getElements()) {
					arcLengthDifference = MathGeometry.arcLength(currRadius, angleSecondSide - angleFirstSide) - (currPackNbreInternClasses * classesWidth);
					elementAngleInterval = tempElementAngleInterval + MathGeometry.arcAngle(currRadius, arcLengthDifference / 2.0f);
					
					elementRep.setPosX((currRadius * (float)Math.cos(Math.toRadians(angleFirstSide + elementAngleInterval/2))) - classesWidth/2.0f);
					elementRep.setPosZ((currRadius * (float)Math.sin(Math.toRadians(angleFirstSide + elementAngleInterval/2))) - classesWidth/2.0f);
					
					currRadius += classesWidth;
				}
			}
			else {				
				for (ElementRepresentation elementRep : coliseumPackRep.getElements()) {
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
	*/
	
	
	
	
	
	
	
	
	private float computePackageSize(ColiseumPackageRepresentation coliseumPackRep, HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> orderedPackages, float classesWidth, float packageWidth, float packageHeight, float levelHeight, int nbreOfRows, PackageRepresentation.ORIENTATION_COLOR levelColor, HashMap<ColiseumPackageRepresentation, Float[]> packagesArcsValues) {
		coliseumPackRep.orientationColor = levelColor;
		
		if (!coliseumPackRep.isFakePackage()) {
			switch (levelColor) {
			case FIRSTCOLOR:
				levelColor = PackageRepresentation.ORIENTATION_COLOR.SECONDCOLOR;
				break;
			case SECONDCOLOR:
				levelColor = PackageRepresentation.ORIENTATION_COLOR.FIRSTCOLOR;
				break;
			default:
				System.out.println("ERRR ColiseumLayout.ComputePackageSize...");
				break;
			}
		}

		float angleFirstSide = coliseumPackRep.getAngleFirstSide();
		float angleSecondSide;	
		float spaceAngle = 0.0f;
		Float[] currPackageArcsValues = packagesArcsValues.get(coliseumPackRep);
		
		
		
		
		if (orderedPackages.get(coliseumPackRep).size() > 0) {
			float spacesSize = (float)(currPackageArcsValues[1] - currPackageArcsValues[0]) / (float)(orderedPackages.get(coliseumPackRep).size() + 1);
			spaceAngle = MathGeometry.arcAngle((float)coliseumPackRep.getInternBorderRadius(), spacesSize);
			angleFirstSide += spaceAngle;

			for (ColiseumPackageRepresentation childColiseumPackRep : orderedPackages.get(coliseumPackRep)) {			
				childColiseumPackRep.setHeight(packageHeight - levelHeight);
				
				childColiseumPackRep.setExternRadius(coliseumPackRep.getExternRadius() - packageWidth);
				childColiseumPackRep.setInternBorderRadius(childColiseumPackRep.getExternRadius() - packageWidth);
				childColiseumPackRep.setInternRadius(childColiseumPackRep.getExternRadius() - (childColiseumPackRep.getPackageLevel() * packageWidth));
				childColiseumPackRep.setLayoutInternRadius(coliseumPackRep.getLayoutInternRadius());	
				childColiseumPackRep.setAngleFirstSide(angleFirstSide);
				
				angleSecondSide = computePackageSize(childColiseumPackRep, orderedPackages, classesWidth, packageWidth, packageHeight - levelHeight, levelHeight, nbreOfRows, levelColor, packagesArcsValues);
				childColiseumPackRep.setAngleSecondSide(angleSecondSide);
				
				childColiseumPackRep.updatePackageMesh();
				
				angleFirstSide = angleSecondSide + spaceAngle;
			}
		}
	
		
		
		
		
		ColiseumPackageRepresentation elementsPackage;
		
		
		
		if (coliseumPackRep.getElements().size() > 0) {
			elementsPackage = new ColiseumPackageRepresentation(new Package(coliseumPackRep.getName() + "_elements"), null, null, 0.0f, null, null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, coliseumPackRep.getCenter(), null);
			elementsPackage.addElements(coliseumPackRep.getElements());
			coliseumPackRep.clearElements();
			coliseumPackRep.setElementsPackage(elementsPackage);
		}

		
		
		if (coliseumPackRep.getElementsPackage() != null) {
			elementsPackage = coliseumPackRep.getElementsPackage();  
			
			int currPackNbreInternClasses = (int)Math.ceil((double)elementsPackage.getElements().size() / (double)nbreOfRows);
	
			if (orderedPackages.get(coliseumPackRep).size() > 0) {
				angleSecondSide = angleFirstSide;
				angleFirstSide = coliseumPackRep.getAngleFirstSide();
			}
			else {
				angleSecondSide = angleFirstSide + MathGeometry.arcAngle(coliseumPackRep.getInternRadius(), currPackNbreInternClasses);
				angleFirstSide = coliseumPackRep.getAngleFirstSide();
			}
			
			
			float elementsPackAngleFirstSide = angleFirstSide;
			
			int elementCounter = 0;
			float currRadius = (float)coliseumPackRep.getInternBorderRadius();
//			float packageAngle = angleSecondSide - angleFirstSide;
			float elementAngle = MathGeometry.arcAngle(currRadius, classesWidth);
			
			
			
			/*
			float epsilon = 0.001f;
			
			float nbreElements = packageAngle / elementAngle;
			
			if (Math.ceil(nbreElements) - nbreElements <= epsilon) {
				currPackNbreInternClasses = (int)Math.ceil(nbreElements);
			}
			else {
				currPackNbreInternClasses = (int)Math.floor(nbreElements);
			}

			
			
			if (currPackNbreInternClasses == 0) {
				currPackNbreInternClasses = 1;
			}
			*/
			

			//float elementAngleInterval = (packageAngle - (currPackNbreInternClasses * elementAngle)) / (currPackNbreInternClasses + 1);;
			float elementAngleInterval = 0.0f;

			
			
			angleFirstSide += elementAngleInterval;
			

			
			elementsPackage.setAngleFirstSide(elementsPackAngleFirstSide);
			elementsPackage.setAngleSecondSide(elementsPackAngleFirstSide + (currPackNbreInternClasses * (elementAngle + elementAngleInterval)));
			elementsPackage.setInternRadius(coliseumPackRep.getInternBorderRadius());
			elementsPackage.setExternRadius(coliseumPackRep.getInternBorderRadius() + ( ((elementsPackage.getElements().size() / currPackNbreInternClasses) + 1) * classesWidth));
			elementsPackage.setInternBorderRadius(coliseumPackRep.getInternBorderRadius());
			elementsPackage.setLayoutInternRadius(coliseumPackRep.getLayoutInternRadius());
			
			
			
			for (ElementRepresentation elementRep :elementsPackage.getElements()) {
				elementsPackage.elementsAngles.put(elementRep, angleFirstSide);
				
				elementRep.setPosX(currRadius);
				elementRep.setPosZ(0.0f);
				angleFirstSide += elementAngle + elementAngleInterval;

				elementCounter++;
				
				if (elementCounter % currPackNbreInternClasses == 0) {
					angleFirstSide = elementsPackAngleFirstSide;
					currRadius += classesWidth;
					
					
					
					
					//elementAngle = MathGeometry.arcAngle(currRadius, classesWidth);
					//elementAngleInterval = (packageAngle - (currPackNbreInternClasses * elementAngle)) / (currPackNbreInternClasses + 1);
					
					
					
					
					angleFirstSide += elementAngleInterval;
				}
			}
		}
		else {
			angleSecondSide = angleFirstSide;
		}
		
		return angleSecondSide;
	}
	
	
	private float computeLayoutInternArcLength(ColiseumPackageRepresentation coliseumPackRep, int nbreOfRows, float classesWidth, float spacesSize, HashMap<ColiseumPackageRepresentation, Float[]> packagesArcsValues) {
		Float[] packageArcsValues = new Float[2];
		float layoutInternArcLength = 0.0f;
		
		if (coliseumPackRep.getColiseumPackages().size() > 0) {
			float subPackagesTotalArcLength = 0.0f;
			float spacesTotalSize = 0.0f;
			
			for (ColiseumPackageRepresentation subPackRep : coliseumPackRep.getColiseumPackages()) {
				subPackagesTotalArcLength += computeLayoutInternArcLength(subPackRep, nbreOfRows, classesWidth, spacesSize, packagesArcsValues);			
			}
		
			packageArcsValues[0] = subPackagesTotalArcLength;
			
			spacesTotalSize = (coliseumPackRep.getColiseumPackages().size() + 1) * spacesSize;
			
			layoutInternArcLength = subPackagesTotalArcLength + spacesTotalSize;
			
			float minimalArcLength = ((int)Math.ceil((double)coliseumPackRep.getElements().size() / (double)nbreOfRows)) * classesWidth;
			
			if (minimalArcLength > layoutInternArcLength) {
				layoutInternArcLength = minimalArcLength;
			}
		}
		else {
			layoutInternArcLength = ((int)Math.ceil((double)coliseumPackRep.getElements().size() / (double)nbreOfRows)) * classesWidth;
			packageArcsValues[0] = 0.0f;
		}
		
		packageArcsValues[1] = layoutInternArcLength;
		
		packagesArcsValues.put(coliseumPackRep, packageArcsValues);
		
		return layoutInternArcLength;
	}
	
	
	
	
	private int computeNbreInternClasses(ColiseumPackageRepresentation coliseumPackRep, int nbreOfRows) {
		int nbreInternClasses = 0;
		
		for (ColiseumPackageRepresentation coliseumSubPack : coliseumPackRep.getColiseumPackages()) {
			nbreInternClasses += computeNbreInternClasses(coliseumSubPack, nbreOfRows);
		}
		
		if (coliseumPackRep.getElements().size() > 0) {
			nbreInternClasses += (int)Math.ceil((double)coliseumPackRep.getElements().size() / (double)nbreOfRows);
		}
		
		return nbreInternClasses;
	}
	
	int computeNbreSpaces(ColiseumPackageRepresentation coliseumPackRep) {
		int nbreSpaces = 0;
		
		for (ColiseumPackageRepresentation coliseumSubPack : coliseumPackRep.getColiseumPackages()) {
			nbreSpaces += computeNbreSpaces(coliseumSubPack);
		}
		
		if (coliseumPackRep.getSubPackages().size() > 0) {
			nbreSpaces += (coliseumPackRep.getSubPackages().size() + 1);
		}
		
		return nbreSpaces;
	}
	
		
	
	public HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> computeBestLayout(ColiseumPackageRepresentation root, float classesWidth, float packageWidth, float levelHeight, int nbreOfRows, float bordersSize, int kmax, float temp, float alpha, float tempTreshold) {
		HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> currSolution = buildDefaultOrderedPackages(root);
		HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> bestSolution = copySolution(currSolution);
		ColiseumPackageRepresentation changedPackage;
		
		layout(root, currSolution, classesWidth, packageWidth, levelHeight, nbreOfRows, bordersSize);
		
		
		//root.computeAbsolutePosition(0, 0);
		
		
		float currValue = getSolutionValue(root);
		float nextValue;
		float bestValue = currValue;
		float valueDiff;
		float acceptationProbability = 0.0f;
		LOG.config("ColiseumLayout.computeBestLayout()");
		LOG.config("Initial value: " + bestValue + "\n");
		
		while (temp > tempTreshold) {
			for (int i = 0; i < kmax; i++) {
				changedPackage = findNeighbourSolution(filterSolution(currSolution), currSolution);
				
				layout(root, currSolution, classesWidth, packageWidth, levelHeight, nbreOfRows, bordersSize);
				
				
				//root.computeAbsolutePosition(0, 0);
				
				
				nextValue = getSolutionValue(root);
				valueDiff = currValue - nextValue;

				//LOG.config("Value diff: " + valueDiff);
				
				
				if (valueDiff >= 0.0) {
				//if (valueDiff <= 0.0) {
					acceptationProbability = (float)Math.pow(Math.E, -(valueDiff / temp));
					
					if (!(Math.random() <= acceptationProbability)) {
						reverseSolutionChange(currSolution.get(changedPackage));
					}
					else {
						currValue = nextValue;
					}
				}
				else {
					currValue = nextValue;
				}
				
				if (currValue > bestValue) {
				//if (currValue < bestValue) {
					bestSolution = copySolution(currSolution);
					bestValue = currValue;
				} 
			}
			
			temp = alpha * temp;
			
			LOG.config("Température: " + temp);
			LOG.config("Best value: " + bestValue + "\n");
		}
		
		layout(root, bestSolution, classesWidth, packageWidth, levelHeight, nbreOfRows, bordersSize);
		
		return bestSolution;
	}
	

	private HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> filterSolution(HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> solution) {
		HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> filteredSolution = copySolution(solution);
		ColiseumPackageRepresentation currPack;
		
		for (Iterator<ColiseumPackageRepresentation> packageItr = filteredSolution.keySet().iterator(); packageItr.hasNext();) {
			currPack = packageItr.next();
			
			if (solution.get(currPack).size() < 2) {
				packageItr.remove();
			}
		}
		
		return filteredSolution;
	}
	
	private void reverseSolutionChange(LinkedList<ColiseumPackageRepresentation> packagesList) {
		ColiseumPackageRepresentation tempPackage = packagesList.get(0);
		packagesList.remove(0);
		packagesList.add(tempPackage);
	}
	
	private HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> buildDefaultOrderedPackages(ColiseumPackageRepresentation root) {
		HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> orderedPackages = new HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>>();
		LinkedList<ColiseumPackageRepresentation> orderedPackagesList = new LinkedList<ColiseumPackageRepresentation>();
		
		for (ColiseumPackageRepresentation subPack : root.getColiseumPackages()) {
			orderedPackagesList.add(subPack);
		}
		orderedPackages.put(root, orderedPackagesList);
		
		
		for (ColiseumPackageRepresentation subPack : root.getColiseumPackages()) {
			orderedPackages.putAll(buildDefaultOrderedPackages(subPack));
		}
		
		return orderedPackages;
	}
	
	private ColiseumPackageRepresentation findNeighbourSolution(HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> filteredPackages, HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> orderedPackages) {
		int randomPackageIndex = (int)(Math.random() * filteredPackages.size());
		ColiseumPackageRepresentation packagesSet[] = new ColiseumPackageRepresentation[filteredPackages.keySet().size()];
		filteredPackages.keySet().toArray(packagesSet);
		
		
		ColiseumPackageRepresentation randomPackage = packagesSet[randomPackageIndex];
		
		LinkedList<ColiseumPackageRepresentation> randomPackageList = orderedPackages.get(randomPackage);
		
		if (randomPackageList.size() >= 2) {
			int randomInd1 = 0;
			int randomInd2 = 0;

			while (randomInd1 == randomInd2) {
				randomInd1 = (int)(Math.random() * randomPackageList.size());
				randomInd2 = (int)(Math.random() * randomPackageList.size());
			}
			
			ColiseumPackageRepresentation tempPackage1 = randomPackageList.get(randomInd1);
			ColiseumPackageRepresentation tempPackage2 = randomPackageList.get(randomInd2);
			
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
	
	private HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> copySolution(HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> orderedPackages) {
		HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>> copy = new HashMap<ColiseumPackageRepresentation, LinkedList<ColiseumPackageRepresentation>>();
		LinkedList<ColiseumPackageRepresentation> tempList;
		
		for (ColiseumPackageRepresentation pack : orderedPackages.keySet()) {
			tempList = new LinkedList<ColiseumPackageRepresentation>();
			tempList.addAll(orderedPackages.get(pack));
			copy.put(pack, tempList);
		}
		
		return copy;
	}
	
	
	private float getSolutionValue(ColiseumPackageRepresentation root) {
		float solutionValue = 0.0f;
		HashMap<String, ElementRepresentation> sortedElements = new HashMap<String, ElementRepresentation>();
		HashMap<ElementRepresentation, ColiseumPackageRepresentation> solutionElements;
		solutionElements = root.getAllElementsWithParentPackage();

		for (ElementRepresentation element : solutionElements.keySet()) {			
			sortedElements.put(element.getSimpleName(), element);
		}
		
		float[] firstPoint = new float[2];
		float[] secondPoint = new float[2];
		
		ColiseumPackageRepresentation currParentPack;
		float elementAngle;
		
		for (ElementRepresentation element : solutionElements.keySet()) {
			for (String targetName : element.getElementModel().getTargets()) {
				//firstPoint[0] = element.getAbsolutePosX();
				//firstPoint[1] = element.getAbsolutePosZ();
				
				currParentPack = solutionElements.get(element);
				elementAngle = currParentPack.getAngleFirstSide() + currParentPack.getElementsPackage().elementsAngles.get(element);
				
				firstPoint = MathGeometry.getPointPosition(element.getPosX(), elementAngle);
				
				if (sortedElements.get(targetName) != null) {
					//secondPoint[0] = sortedElements.get(targetName).getAbsolutePosX();
					//secondPoint[1] = sortedElements.get(targetName).getAbsolutePosZ();
				
					currParentPack = solutionElements.get(sortedElements.get(targetName));
					elementAngle = currParentPack.getAngleFirstSide() + currParentPack.getElementsPackage().elementsAngles.get(sortedElements.get(targetName));
					
					secondPoint = MathGeometry.getPointPosition(sortedElements.get(targetName).getPosX(), elementAngle);
				}
				
				solutionValue += MathGeometry.getPositiveDistance(firstPoint, secondPoint);
			}
		}
		
		
		return solutionValue;
	}
}

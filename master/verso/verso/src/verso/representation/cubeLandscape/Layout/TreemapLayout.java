package verso.representation.cubeLandscape.Layout;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import verso.util.MathGeometry;
import verso.representation.Layout;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.TreemapPackageRepresentation;

public class TreemapLayout extends Layout{
	
	public static Logger LOG = Logger.getLogger(TreemapLayout.class.getSimpleName());

	
	
	private static final int HORIZONTAL =0;
	private static final int VERTICAL = 1;
	
	public static final int LEFT = 2;
	public static final int RIGHT = 3;
	public static final int UP = 4;
	public static final int DOWN = 5;
	

	public void layout(TreemapPackageRepresentation root) {
		layout(root, buildDefaultOrderedPackages(root));
	}

	public void layout(TreemapPackageRepresentation root,
			HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> orderedPackages) {
		int size = (int) Math.ceil(Math.sqrt((double) root.countDescendantClasses()));
		placePackage(root, orderedPackages, size, size, VERTICAL);
		// arrangeLastChild(root, VERTICAL);
	}

	public void paddedLayout(TreemapPackageRepresentation root, float paddingSize, int orientationColor,
			int horizontalOrientation, int verticalOrientation) {
		paddedLayout(root, buildDefaultOrderedPackages(root), paddingSize, orientationColor, horizontalOrientation,
				verticalOrientation);
	}

	public void paddedLayout(TreemapPackageRepresentation root,
			HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> orderedPackages,
			float paddingSize, int orientationColor, int horizontalOrientation, int verticalOrientation) {
		int size = (int) Math.ceil(Math.sqrt((double) root.countDescendantClasses()));
		placePackage(root, orderedPackages, size, size, VERTICAL);
		addPadding(root, orderedPackages, paddingSize, orientationColor, horizontalOrientation, verticalOrientation);
		placePaddedPackages(root, orderedPackages, paddingSize);
	}

	private void placePackage(TreemapPackageRepresentation pack, HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> orderedPackages, int sizeX, int sizeZ, int orientation)
	{
		pack.setOrientation(orientation);
		
		//Class Step
		int currentPosition = 0;
		int size =0;
		float max =0;
		
		
		if (orientation == VERTICAL)
			currentPosition += placeClasses(pack.getElements(), orientation, sizeZ);
		else
			currentPosition += placeClasses(pack.getElements(), orientation, sizeX);
		
		
		if (orientation == VERTICAL)
		{
			//for (TreemapPackageRepresentation p : pack.getTreemapPackages())
			for (TreemapPackageRepresentation p : orderedPackages.get(pack)) {
					if (pack.countDescendantClasses() == 0)
						size = 0;
					else
						size = (int) Math
								.ceil((p.countDescendantClasses() / (double) pack.countDescendantClasses()) * sizeX);
	
					if (size == 0)
						size++;
					p.setPosX(currentPosition);
					p.setPosZ(0);
					placePackage(p, orderedPackages, size, sizeZ, HORIZONTAL);
					currentPosition += (int) p.getSizeX();
			}
			if (pack.getElements().size() < sizeZ)
				max = pack.getElements().size();
			else
				max = sizeZ;
			
			
			//for (TreemapPackageRepresentation p : pack.getTreemapPackages())
			for (TreemapPackageRepresentation p : orderedPackages.get(pack))
			{
				if (p.getSizeZ() > max)
					max = (int)p.getSizeZ();
			}
			
			if (max == 0) max = sizeZ;
			//for (TreemapPackageRepresentation p : pack.getTreemapPackages())
//			for (TreemapPackageRepresentation p : orderedPackages.get(pack))
//			{
				//p.setSizeZ(max);
//			}
			pack.setSizeZ(max);
			if (currentPosition ==0) currentPosition++;
			pack.setSizeX(currentPosition);
		}
		else
		{
			//for (TreemapPackageRepresentation p : pack.getTreemapPackages())
			for (TreemapPackageRepresentation p : orderedPackages.get(pack))
			{
				if (pack.countDescendantClasses() == 0)
					size = 0;
				else
					size = (int)Math.ceil((p.countDescendantClasses()/(double)pack.countDescendantClasses())*sizeZ);
				if (size == 0) size++;
				p.setPosX(0);
				p.setPosZ(currentPosition);
				placePackage(p, orderedPackages, sizeX, size, VERTICAL);
				currentPosition += (int) p.getSizeZ();
			}
			if (pack.getElements().size() < sizeX)
				max = pack.getElements().size();
			else
				max = sizeX;

			
			//for (TreemapPackageRepresentation p : pack.getTreemapPackages())
			for (TreemapPackageRepresentation p : orderedPackages.get(pack))
			{
				if (p.getSizeX() > max)
					max = (int)p.getSizeX();
			}
			
			if (max == 0) max = sizeX;
			//for (TreemapPackageRepresentation p : pack.getTreemapPackages())
			for (TreemapPackageRepresentation p : orderedPackages.get(pack))
			{
				//p.setSizeX(max);
			}
			pack.setSizeX(max);
			if (currentPosition == 0) currentPosition++;
			pack.setSizeZ(currentPosition);
		}
	}
	
	private void arrangeLastChild(TreemapPackageRepresentation pack, int orientation)
	{
		for (Iterator<TreemapPackageRepresentation> i = pack.getTreemapPackages().iterator(); i.hasNext();)
		{
			TreemapPackageRepresentation pr = i.next();
			if (! i.hasNext())
			{
				pr.setSizeZ(pack.getSizeZ()-pr.getPosZ());
				pr.setSizeX(pack.getSizeX()-pr.getPosX());
			}
			if (orientation == VERTICAL)
			{
				pr.setSizeZ(pack.getSizeZ());
				arrangeLastChild(pr, HORIZONTAL);
			}
			else
			{
				pr.setSizeX(pack.getSizeX());
				arrangeLastChild(pr, VERTICAL);
			}
		}
	}
	
	private int placeClasses(Collection<ElementRepresentation> classes, int orientation, int size)
	{
		int i =0;
		int j =0;
		for (Iterator<ElementRepresentation> it = classes.iterator(); it.hasNext();)
		{
			ElementRepresentation currentElem = it.next();
			MethodLayout.placeMethods(currentElem);
			if (orientation == VERTICAL)
			{
				currentElem.setPosX(j);
				currentElem.setPosZ(i);
			}
			else
			{
				currentElem.setPosX(i);
				currentElem.setPosZ(j);
			}
			i++;
			if (i >= size)
			{
				i =0;
				j++;
			}
		}
		if (i ==0) return j;
		return j+1;
	}
	
	
	
	
	private void addPadding(TreemapPackageRepresentation root, HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> orderedPackages, double paddingSize, int orientationColor, int horizontalOrientation, int verticalOrientation) {
		computePaddedSize(root, orderedPackages, paddingSize, orientationColor, horizontalOrientation, verticalOrientation);
		adjustPaddedSize(root, orderedPackages, paddingSize);
	}
	
	private void computePaddedSize(TreemapPackageRepresentation root, HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> orderedPackages, double paddingSize, int orientationColor, int horizontalOrientation, int verticalOrientation) {
		float newSizeX = 0.0f, newSizeZ = 0.0f;
		int newElementsSize;
		
		
		root.orientationColor = orientationColor;
		root.horizontalOrientation = horizontalOrientation;
		root.verticalOrientation = verticalOrientation;
		
		if (!root.isFakePackage()) {
			if (orientationColor == VERTICAL) {
				orientationColor = HORIZONTAL;
			}
			else {
				orientationColor = VERTICAL;
			}
		}
		
		if (root.getPackages().size() > 0) {		
			//************ Calcule les dimensions du packages ******************
			
			if (root.getOrientation() == TreemapPackageRepresentation.VERTICAL) {
				if (!root.isFakePackage()) {
					newSizeX += 2 * paddingSize;
				}
				
				int currHorizontalOrientation = root.horizontalOrientation;
				
				//for (TreemapPackageRepresentation pack : root.getTreemapPackages()) {
				for (TreemapPackageRepresentation pack : orderedPackages.get(root)) {
					this.addPadding(pack, orderedPackages, paddingSize, orientationColor, currHorizontalOrientation, root.verticalOrientation);
					
					if (pack.getSizeZ() > newSizeZ) {
						newSizeZ = pack.getSizeZ();
					}
					
					newSizeX += pack.getSizeX();	
										
					if (currHorizontalOrientation == LEFT) {
						currHorizontalOrientation = RIGHT;
					}
					else {
						currHorizontalOrientation = LEFT;
					}
				}

				newSizeX += (root.getTreemapPackages().size() - 1) * paddingSize;
			
				if(!root.getElements().isEmpty())
					newElementsSize = (root.getElements().size() / (int)newSizeZ) + 1;
				else
					newElementsSize = 0;
					
				if (root.getElements().size() > 0) {
					newSizeX += newElementsSize + paddingSize;
				}
				
				if (!root.isFakePackage()) {
					newSizeZ += 2 * paddingSize;
				}		
			}
			else {
				if (!root.isFakePackage()) {
					newSizeZ += 2 * paddingSize;
				}
				
				int currVerticalOrientation = root.verticalOrientation;
				
				//for (TreemapPackageRepresentation pack : root.getTreemapPackages()) {
				for (TreemapPackageRepresentation pack : orderedPackages.get(root)) {
					addPadding(pack, orderedPackages, paddingSize, orientationColor, root.horizontalOrientation, currVerticalOrientation);
					
					if (pack.getSizeX() > newSizeX) {
						newSizeX = pack.getSizeX();
					}
					
					newSizeZ += pack.getSizeZ();	
					
					if (currVerticalOrientation == UP) {
						currVerticalOrientation = DOWN;
					}
					else {
						currVerticalOrientation = UP;
					}
				}
				
				newSizeZ += (root.getTreemapPackages().size() - 1) * paddingSize;
					
				
				if(!root.getElements().isEmpty())
					newElementsSize = (root.getElements().size() / (int)newSizeX) + 1;
				else
					newElementsSize = 0;
				
				if (root.getElements().size() > 0) {
					newSizeZ += newElementsSize + paddingSize;
				}
				
				if (!root.isFakePackage()) {
					newSizeX += 2 * paddingSize;
				}
			}
			
			//****************************************************************
		
			root.setSizeX(newSizeX);
			root.setSizeZ(newSizeZ);
		}
	}
	
	
	private void adjustPaddedSize(TreemapPackageRepresentation root, HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> orderedPackages, double paddingSize) {
		float maxSize = 0.0f;
		float currPackageSize;
		
		//for (TreemapPackageRepresentation p : root.getTreemapPackages()) {
		for (TreemapPackageRepresentation pack : orderedPackages.get(root)) {
			if (root.orientation == VERTICAL) {
				currPackageSize = pack.getSizeZ();
			}
			else {
				currPackageSize = pack.getSizeX();
			}
			
			if (maxSize < currPackageSize) {
				maxSize = currPackageSize;
			}
		}
		
		//for (TreemapPackageRepresentation p : root.getTreemapPackages()) {
		for (TreemapPackageRepresentation pack : orderedPackages.get(root)) {
			if (pack.isFakePackage()) {
				if (root.orientation == VERTICAL) {
					pack.setSizeZ(maxSize);
				}
				else {
					pack.setSizeX(maxSize);
				}
			}
		}
	}
	
	
	public void placePaddedPackages(TreemapPackageRepresentation root, HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> orderedPackages, float paddingSize) {
		float newPosX = 0.0f, newPosZ = 0.0f;
		float padding = 0.0f;

		
		for (TreemapPackageRepresentation p : orderedPackages.get(root)) {						
			placePaddedPackages(p, orderedPackages, paddingSize);
		}
		
		if (!root.isFakePackage() && root.getPackages().size() > 0) {
			padding = paddingSize;
		}

		
		
		if (root.getElements().size() > 0) {
			if (root.horizontalOrientation == LEFT) {
				newPosX = padding;
			}
			else {
				newPosX = root.getSizeX() - padding - 1.0f;
			}
			
			if (root.verticalOrientation == UP) {
				newPosZ = padding;
			}
			else {
				newPosZ = root.getSizeZ() - padding - 1.0f;
			}
			
			for (ElementRepresentation element : root.getElements()) {
				element.setPosX(newPosX);
				element.setPosZ(newPosZ);
				
				if (root.getOrientation() == VERTICAL) {						
					if (root.verticalOrientation == UP) {
						newPosZ += 1.0;
						
						if (newPosZ > root.getSizeZ() - padding - 1.0) {
							newPosZ = padding;
							
							if (root.horizontalOrientation == LEFT) {
								newPosX += 1.0;	
							}
							else {
								newPosX -= 1.0;
							}
						}
					}
					else {
						newPosZ -= 1.0;
						
						if (newPosZ < padding) {
							newPosZ = root.getSizeZ() - padding - 1.0f;
							
							if (root.horizontalOrientation == LEFT) {
								newPosX += 1.0;
							}
							else {
								newPosX -= 1.0;
							}
						}
					}
				}
				else {
					if (root.horizontalOrientation == LEFT) {
						newPosX += 1.0;
						
						if (newPosX > root.getSizeX() - padding - 1.0) {
							newPosX = padding;
							
							if (root.verticalOrientation == UP) {
								newPosZ += 1.0;	
							}
							else {
								newPosZ -= 1.0;
							}
						}
					}
					else {
						newPosX -= 1.0;
						
						if (newPosX < padding) {
							newPosX = root.getSizeX() - padding - 1.0f;
							
							if (root.verticalOrientation == UP) {
								newPosZ += 1.0;
							}
							else {
								newPosZ -= 1.0;
							}
						}
					}
				}
			}
		}
		
		
		
		
		
		
		if (root.isFakePackage()) {
			/*
			float totalSubPackagesLength = 0.0f;
			float rootFreeSpace = 0.0f;
			
			for (TreemapPackageRepresentation p : orderedPackages.get(root)) {
				if (root.getOrientation() == TreemapPackageRepresentation.VERTICAL) {
					totalSubPackagesLength += p.getSizeX();
				}
				else {
					totalSubPackagesLength += p.getSizeZ();
				}
			}
			
			if (root.getOrientation() == TreemapPackageRepresentation.VERTICAL) {
				rootFreeSpace = root.getSizeX() - totalSubPackagesLength - (padding * 2);
			}
			else {
				rootFreeSpace = root.getSizeZ() - totalSubPackagesLength - (padding * 2);
			}
			
			rootFreeSpace = rootFreeSpace / 3.0f;
			*/
			
			for (TreemapPackageRepresentation p : orderedPackages.get(root)) {						
				if (p.horizontalOrientation == LEFT) {
					newPosX = padding;
				}
				else 
				{
					newPosX = (root.getSizeX() - padding - p.getSizeX());
				}
				
				if (p.verticalOrientation == UP) {
					newPosZ = padding;
				}
				else {
					newPosZ = (root.getSizeZ() - padding - p.getSizeZ());
				}
			
				p.setPosX(newPosX);
				p.setPosZ(newPosZ);	
			}
		}
		else {
			if (root.horizontalOrientation == LEFT) {
				if (root.getOrientation() == TreemapPackageRepresentation.VERTICAL) {
					if (root.getElements().size() == 0) {
						newPosX = padding;
					}
					else {
						newPosX += (paddingSize + 1.0f);
					}
				}
				else {
					newPosX = padding;
				}
			}
			else {
				if (root.getOrientation() == TreemapPackageRepresentation.VERTICAL) {
					if (root.getElements().size() == 0) {
						newPosX = root.getSizeX() - padding;
					}
					else {
						newPosX -= (paddingSize + 1.0f);
					}
				}
				else {
					newPosX = root.getSizeX() - padding;
				}
			}
			
			
			if (root.verticalOrientation == UP) {
				if (root.getOrientation() == TreemapPackageRepresentation.VERTICAL) {
					newPosZ = padding;
				}
				else {
					if (root.getElements().size() == 0) {
						newPosZ = padding;
					}
					else {
						newPosZ += (paddingSize + 1.0);
					}
				}
			}
			else {
				if (root.getOrientation() == TreemapPackageRepresentation.VERTICAL) {
					newPosZ = root.getSizeZ() - padding;
				}
				else {
					if (root.getElements().size() == 0) {
						newPosZ = root.getSizeZ() - padding;
					}
					else {
						newPosZ -= (paddingSize + 1.0);
					}
				}
			}
		
			
			
			if (root.getOrientation() == TreemapPackageRepresentation.VERTICAL) {
				for (TreemapPackageRepresentation pack : orderedPackages.get(root)) {
					if (root.horizontalOrientation == LEFT) {
						pack.setPosX(newPosX);
						newPosX += pack.getSizeX();
						newPosX += paddingSize;
					}
					else {
						newPosX -= pack.getSizeX();
						pack.setPosX(newPosX);
						newPosX -= paddingSize;
					}
					
					
					if (root.verticalOrientation == UP) {
						pack.setPosZ(newPosZ);
					}
					else {
						pack.setPosZ(newPosZ - pack.getSizeZ());
					}
				}
			}
			else {
				for (TreemapPackageRepresentation pack : orderedPackages.get(root)) {
					if (root.horizontalOrientation == LEFT) {
						pack.setPosX(newPosX);
					}
					else {
						pack.setPosX(newPosX - pack.getSizeX());
					}
					
					if (root.verticalOrientation == UP) {						
						pack.setPosZ(newPosZ);	
						newPosZ += pack.getSizeZ();
						newPosZ += paddingSize;
					}
					else {
						newPosZ -= pack.getSizeZ();
						pack.setPosZ(newPosZ);
						newPosZ -= paddingSize;
					}
				}
			}
		}
	}

	
	
	
	
	
	
	
	
	

	
	
	
	
	public HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> computeBestLayout(TreemapPackageRepresentation root, int kmax, double temp, double alpha, double tempTreshold) {
		HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> bestSolution = buildDefaultOrderedPackages(root);
		HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> nextSolution = copySolution(bestSolution);
		TreemapPackageRepresentation changedPackage;
		
		layout(root, bestSolution);
		root.computeAbsolutePosition(0, 0);
		double bestValue = getSolutionValue(root);

		LOG.config("Initial value: " + bestValue + "\n");
		
		double currValue;
		double valueDiff;
		double acceptationProbability = 0.0;
		
		while (temp > tempTreshold) {
			for (int i = 0; i < kmax; i++) {
				changedPackage = findNeighbourSolution(filterSolution(nextSolution), nextSolution);
				
				layout(root, nextSolution);
				root.computeAbsolutePosition(0, 0);
				
				currValue = getSolutionValue(root);
				valueDiff = bestValue - currValue;

				if (valueDiff >= 0.0) {
					acceptationProbability = Math.pow(Math.E, -(valueDiff / temp));
				}
				
				if (valueDiff < 0.0) {
					bestSolution = copySolution(nextSolution);
					bestValue = currValue;
				} 
				else if (!(Math.random() <= acceptationProbability)) {
					reverseSolutionChange(nextSolution.get(changedPackage));
				}
			}
			
			temp = alpha * temp;
			
			LOG.config("Température: " + temp);
			LOG.config("Best value: " + bestValue + "\n");
		}
		
		layout(root, bestSolution);
		
		return bestSolution;
	}
	
	public HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> computeBestLayout(TreemapPackageRepresentation root, float paddingSize, int orientationColor, int horizontalOrientation, int verticalOrientation, int kmax, float temp, float alpha, float tempTreshold) {
		HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> currSolution = buildDefaultOrderedPackages(root);
		HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> bestSolution = copySolution(currSolution);
		TreemapPackageRepresentation changedPackage;
		
		paddedLayout(root, currSolution, paddingSize, orientationColor, horizontalOrientation, verticalOrientation);
		root.computeAbsolutePosition(0, 0);

		double currValue = getSolutionValue(root);
		double bestValue = currValue;
		
		LOG.config("Initial value: " + bestValue + "\n");

		while (temp > tempTreshold) {
			for (int i = 0; i < kmax; i++) {
				changedPackage = findNeighbourSolution(filterSolution(currSolution), currSolution);
				
				paddedLayout(root, currSolution, paddingSize, orientationColor, horizontalOrientation, verticalOrientation);
				root.computeAbsolutePosition(0, 0);
				
				double nextValue = getSolutionValue(root);
				double valueDiff = currValue - nextValue;

				//LOG.config("Value diff: " + valueDiff);
				
				if (valueDiff >= 0.0) {
					// if (valueDiff <= 0.0) {
					double acceptationProbability = Math.pow(Math.E, -(valueDiff / temp));
					if (!(Math.random() <= acceptationProbability)) {
						reverseSolutionChange(currSolution.get(changedPackage));
					} else {
						currValue = nextValue;
					}
				} else {
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
		
		paddedLayout(root, bestSolution, paddingSize, orientationColor, horizontalOrientation, verticalOrientation);
		
		return bestSolution;
	}
	

	private HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> filterSolution(HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> solution) {
		HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> filteredSolution = copySolution(solution);
		TreemapPackageRepresentation currPack;
		
		for (Iterator<TreemapPackageRepresentation> packageItr = filteredSolution.keySet().iterator(); packageItr.hasNext();) {
			currPack = packageItr.next();
			
			if (currPack.getPackages().size() < 2) {
				packageItr.remove();
			}
		}
		
		return filteredSolution;
	}
	
	private void reverseSolutionChange(LinkedList<TreemapPackageRepresentation> packagesList) {
		TreemapPackageRepresentation tempPackage = packagesList.get(0);
		packagesList.remove(0);
		packagesList.add(tempPackage);
	}
	
	
	private HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> buildDefaultOrderedPackages(TreemapPackageRepresentation root) {
		HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> orderedPackages = new HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>>();
		LinkedList<TreemapPackageRepresentation> orderedPackagesList = new LinkedList<TreemapPackageRepresentation>();
		for (TreemapPackageRepresentation subPack : root.getTreemapPackages()) {
			orderedPackagesList.add(subPack);
		}
		orderedPackages.put(root, orderedPackagesList);
		
		
		for (TreemapPackageRepresentation subPack : root.getTreemapPackages()) {
			orderedPackages.putAll(buildDefaultOrderedPackages(subPack));
		}
		
		return orderedPackages;
	}
	
	
	private TreemapPackageRepresentation findNeighbourSolution(HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> filteredPackages, HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> orderedPackages) {
		int randomPackageIndex = (int)(Math.random() * filteredPackages.size());
		TreemapPackageRepresentation packagesSet[] = new TreemapPackageRepresentation[filteredPackages.keySet().size()];
		filteredPackages.keySet().toArray(packagesSet);
		
		
		TreemapPackageRepresentation randomPackage = packagesSet[randomPackageIndex];
		
		/*
		for (TreemapPackageRepresentation pack : packagesSet) {
			if (pack.getSimpleName().equals("org.jhotdraw")) {
				randomPackage = pack;
				break;
			}
		}
		*/
		
		LinkedList<TreemapPackageRepresentation> randomPackageList = orderedPackages.get(randomPackage);
		
		if (randomPackageList.size() >= 2) {
			int randomInd1 = 0;
			int randomInd2 = 0;
			
			/*
			TreemapPackageRepresentation tempPackage = randomPackageList.get(0);
			randomPackageList.remove(0);
			randomPackageList.add(tempPackage);
			*/
			
			
			while (randomInd1 == randomInd2) {
				randomInd1 = (int)(Math.random() * randomPackageList.size());
				randomInd2 = (int)(Math.random() * randomPackageList.size());
			}
			
			TreemapPackageRepresentation tempPackage1 = randomPackageList.get(randomInd1);
			TreemapPackageRepresentation tempPackage2 = randomPackageList.get(randomInd2);
			
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
	
	private HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> copySolution(HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> orderedPackages) {
		HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>> copy = new HashMap<TreemapPackageRepresentation, LinkedList<TreemapPackageRepresentation>>();
		LinkedList<TreemapPackageRepresentation> tempList;
		
		for (TreemapPackageRepresentation pack : orderedPackages.keySet()) {
			tempList = new LinkedList<TreemapPackageRepresentation>();
			tempList.addAll(orderedPackages.get(pack));
			copy.put(pack, tempList);
		}
		
		return copy;
	}
	
	
	private float getSolutionValue(TreemapPackageRepresentation root) {
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
				firstPoint[0] = element.getAbsolutePosX();
				firstPoint[1] = element.getAbsolutePosZ();
				
				if (sortedElements.get(targetName) != null) {
					secondPoint[0] = sortedElements.get(targetName).getAbsolutePosX();
					secondPoint[1] = sortedElements.get(targetName).getAbsolutePosZ();
				}
				
				solutionValue += MathGeometry.getPositiveDistance(firstPoint, secondPoint);
			}
		}
		
		return solutionValue;
	}
	
	
	
	
	
}

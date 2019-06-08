package verso.representation.cubeLandscape.Layout.radial;

import java.awt.Color;
import java.util.HashMap;

import verso.graphics.primitives.Primitive;
import verso.representation.cubeLandscape.Layout.LinkAndNodeLayout;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.link.NodeRepresentation;
import verso.representation.cubeLandscape.representationModel.radial.RadialPackageRepresentation;

public class RadialLinkAndNodeLayout extends LinkAndNodeLayout {
	//Fonctions pour créer les noeuds du système. Le premier élément de la liste retournée contient les noeuds et des packages
	//et le second élément contient ceux des éléments (classes et interfaces).
	public HashMap<PackageRepresentation, NodeRepresentation> createPackagesNodes(SystemRepresentation sysrep, float levelHeight, Primitive packageNodeMesh, float packageNodeSize, Color packageNodeColor) {
		HashMap<PackageRepresentation, NodeRepresentation> packagesNodes = new HashMap<PackageRepresentation, NodeRepresentation>();
		//int maxLevel = sysrep.findMaxLevel();
		int maxLevel = sysrep.getPackages().iterator().next().computeMaxLevel();
		float minY = sysrep.findMinY() + levelHeight;
		//double radius = ((RadialPackageRepresentation)sysrep.getPackages().iterator().next()).getLayoutInternRadius();

		for (PackageRepresentation pack: sysrep.getPackages()) {
			if (!pack.isFakePackage()) {
				packagesNodes.putAll(createPackagesNodes((RadialPackageRepresentation)pack, 0, maxLevel, minY, levelHeight, packageNodeMesh, packageNodeSize, packageNodeColor, null));
			}
		}
		
		return packagesNodes;
	}
	
	@Override
	public HashMap<PackageRepresentation, NodeRepresentation> createPackagesNodes(PackageRepresentation currPack, int currLevel, int maxLevel, float minY, float levelHeight, Primitive packageNodeMesh, float packageNodeSize, Color packageNodeColor, NodeRepresentation parentNode) {
		HashMap<PackageRepresentation, NodeRepresentation> packagesNodes = new HashMap<PackageRepresentation, NodeRepresentation>();
		
		//**********************************
		//À modifier quand je ferai 2 types de systèmes : TreemapSystemRepresentation et RadialSystemRepresentation (changer tous les
		//PackageRepresentation pour RadialPackageRepresentation)
		//**********************************
		float radius = ((RadialPackageRepresentation)currPack).getInternRadius();
		
		
		float packMedianAngle = ((RadialPackageRepresentation)currPack).getAngleFirstSide() + (((RadialPackageRepresentation)currPack).getAngleSecondSide() - ((RadialPackageRepresentation)currPack).getAngleFirstSide()) / 2;
		
		float posX = ((radius / maxLevel) * currLevel) * (float)Math.cos(Math.toRadians(packMedianAngle));
		float posZ = ((radius / maxLevel) * currLevel) * (float)Math.sin(Math.toRadians(packMedianAngle));		
		float posY = minY + Math.abs(currLevel - maxLevel)*levelHeight;		
		
		NodeRepresentation currPackNode = new NodeRepresentation(currPack, packageNodeMesh, packageNodeSize, posX, posY, posZ, packageNodeColor, parentNode);
		packagesNodes.put(currPack, currPackNode);
		
		
		
		
		
		
		RadialPackageRepresentation elementsPack = ((RadialPackageRepresentation)currPack).getElementsPackage();
		
		if (elementsPack != null) {
			float elementPackRadius = elementsPack.getInternRadius() + ((elementsPack.getExternRadius() - elementsPack.getInternRadius()) / 2.0f);
			float elementPackMedianAngle = elementsPack.getAngleFirstSide() + (elementsPack.getAngleSecondSide() - elementsPack.getAngleFirstSide()) / 2;
			float elementPackPosX = elementPackRadius * (float)Math.cos(Math.toRadians(elementPackMedianAngle));
			float elementPackPosZ = elementPackRadius * (float)Math.sin(Math.toRadians(elementPackMedianAngle));
			float elementPackPosY = minY + Math.abs(currLevel - maxLevel)*levelHeight;
			
			NodeRepresentation elementPackNode = new NodeRepresentation(elementsPack, packageNodeMesh, packageNodeSize, elementPackPosX, elementPackPosY, elementPackPosZ, packageNodeColor, currPackNode);
			packagesNodes.put(elementsPack, elementPackNode);
		}
		
		
		
		
		
		
		for (RadialPackageRepresentation pack: ((RadialPackageRepresentation)currPack).getRadialPackages()) {
			packagesNodes.putAll(createPackagesNodes(pack, currLevel+1, maxLevel, minY, levelHeight, packageNodeMesh, packageNodeSize, packageNodeColor, currPackNode));
		}
		
		return packagesNodes;
	}
	
	@Override
	public HashMap<ElementRepresentation, NodeRepresentation> createElementsNodes(SystemRepresentation sysrep, float levelHeight, Primitive elementNodeMesh, float elementNodeSize, Color elementNodeColor) {
		HashMap<ElementRepresentation, NodeRepresentation> elementsNodes = new HashMap<ElementRepresentation, NodeRepresentation>();
		//int maxLevel = sysrep.findMaxLevel();
		int maxLevel = sysrep.getPackages().iterator().next().computeMaxLevel();
		float minY = sysrep.findMinY();
		
		for (PackageRepresentation pack: sysrep.getPackages()) {
			elementsNodes.putAll(createElementsNodes(sysrep, (RadialPackageRepresentation)pack, 0, maxLevel, minY, levelHeight, elementNodeMesh, elementNodeSize, elementNodeColor));
		}
	
		return elementsNodes;
	}
	
	private HashMap<ElementRepresentation, NodeRepresentation> createElementsNodes(SystemRepresentation sysrep, RadialPackageRepresentation currPack, int currLevel, int maxLevel, float minY, float levelHeight, Primitive elementNodeMesh, float elementNodeSize, Color elementNodeColor) {	
		HashMap<ElementRepresentation, NodeRepresentation> elementsNodes = new HashMap<ElementRepresentation, NodeRepresentation>();	

		for (RadialPackageRepresentation pack: currPack.getRadialPackages()) {
			elementsNodes.putAll(createElementsNodes(sysrep, pack, currLevel+1, maxLevel, minY, levelHeight, elementNodeMesh, elementNodeSize, elementNodeColor));
		}

		
		RadialPackageRepresentation elementsPack = currPack.getElementsPackage();
		
		if (elementsPack != null) {
			for (ElementRepresentation element : elementsPack.getElements()) {			
				elementsNodes.put(element, createElementNode(currPack, element, currLevel, maxLevel, minY, levelHeight, elementNodeMesh, elementNodeSize, elementNodeColor, sysrep.getPackageNode(elementsPack)));	
			}
		}
		
		return elementsNodes;
	}
	
	//Il faut centrer les noeuds au centre des classes. Où trouver la valeur de la largeur de la classe ???
	private NodeRepresentation createElementNode(PackageRepresentation parentPack, ElementRepresentation currElement, int currLevel, int maxLevel, float minY, float levelHeight, Primitive elementNodeMesh, float elementNodeSize, Color elementNodeColor, NodeRepresentation parentNode) {
		float posX = parentPack.getAbsolutePosX() + currElement.getPosX() + 0.5f;
		float posZ = parentPack.getAbsolutePosZ() + currElement.getPosZ() + 0.5f;		

		float posY = currElement.getHeight() + (currLevel * parentPack.getHeight());
		
		if (elementNodeColor == null) {
			elementNodeColor = currElement.getColor();
		}
		
		return new NodeRepresentation(currElement,elementNodeMesh, elementNodeSize, posX, posY, posZ, elementNodeColor, parentNode);
	}

	//**********************************************************************************
	
	
	
	
	/*
	//Fonction pour créer les Edge Bundles entre les classes
	public HashMap<LinkKey, EdgeBundleLinkRepresentation> createsEdgesBundles(SystemRepresentation sysrep, int nbrePoints, Color edgeBundleStartColor, Color edgeBundleEndColor, Color bidirectionalColor, boolean straightenControlPoints, double beta, int degree, int nbreSegments, boolean removeLCA, boolean verticalPlanar, boolean horizontalPlanar) {
		HashMap<LinkKey, EdgeBundleLinkRepresentation> nodesEdgeBundles = new HashMap<LinkKey, EdgeBundleLinkRepresentation>();
		HashMap<String, ElementRepresentation> allElementsRepresentation = getAllElementsRepresentation(sysrep);
		Iterator<ElementRepresentation> elementsItr = allElementsRepresentation.values().iterator();
		ElementRepresentation currElement;
		Iterator<String> textTargetsItr;
		ElementRepresentation currLinkedElement;
		NodeRepresentation startNode, endNode;
		double linkWidth;
		double linkLineWidth;
		
		int nbreEdgesBundle = 0;
		
		linkWidth = sysrep.linkMinSize + (1.0 / (double)sysrep.maxNbreLinks) * sysrep.linkMaxSize;
		linkLineWidth = sysrep.linkMinLineSize + (1.0 / (double)sysrep.maxNbreLinks) * sysrep.linkMaxLineSize;
		
		while (elementsItr.hasNext()) {
			currElement = elementsItr.next();
			textTargetsItr = currElement.getElementModel().getTargets().iterator();		
			
			while (textTargetsItr.hasNext()) {			
				currLinkedElement = allElementsRepresentation.get(textTargetsItr.next());
				startNode = sysrep.getElementNode(currElement);
				endNode = sysrep.getElementNode(currLinkedElement);
				
				if (startNode != null && endNode != null && startNode != endNode) {
					LinkedList<Integer> lca = new LinkedList<Integer>();
					LinkedList<NodeRepresentation> path = getNodesPath(sysrep.getEdgeBundlesNode(startNode), sysrep.getEdgeBundlesNode(endNode), lca, sysrep);					
					//path.removeFirst();
					//path.removeLast();
					path.addFirst(startNode);
					path.addLast(endNode);
					
					LinkKey linkKey = new LinkKey(startNode, endNode);
					LinkKey reverseLinkKey = new LinkKey(endNode, startNode);
					
					if (nodesEdgeBundles.get(reverseLinkKey) != null) {
						nodesEdgeBundles.get(reverseLinkKey).setIsBidirectional(true);
					}
					else {
						nodesEdgeBundles.put(linkKey, new EdgeBundleLinkRepresentation(startNode, endNode, 0, 1, nbrePoints, linkWidth, linkLineWidth, edgeBundleStartColor, edgeBundleEndColor, false, bidirectionalColor, path, straightenControlPoints, beta, degree, nbreSegments, (int)lca.getFirst(), removeLCA, verticalPlanar, horizontalPlanar));					
					
						nbreEdgesBundle++;
					}
				}
			}
		}
		
		
		System.out.println("Nodes edges bundles Radial 1: " + nodesEdgeBundles.size());
		
		
		//Crée les liens entre les classes et les packages
		HashMap<LinkKey, LinkRepresentation> copyDirectLinks = new HashMap<LinkKey, LinkRepresentation>();
		Iterator<LinkKey> keysItr = nodesEdgeBundles.keySet().iterator();
		LinkKey currLinkKey;
		while (keysItr.hasNext()) {
			currLinkKey = keysItr.next();
			copyDirectLinks.put(currLinkKey, nodesEdgeBundles.get(currLinkKey));
		}
		
		elementsItr = allElementsRepresentation.values().iterator();
		HashMap<LinkKey, LinkedList<Integer>> elementPackagesDirectLinks = new HashMap<LinkKey, LinkedList<Integer>>();
		
		while (elementsItr.hasNext()) {
			currElement = elementsItr.next();
			
			for (PackageRepresentation pack : sysrep.getPackages()) {
				createsElementPackageDirectLinks(elementPackagesDirectLinks, copyDirectLinks, sysrep, currElement, pack);
			}
		}
		
		Iterator<LinkKey> linksKeyItr = elementPackagesDirectLinks.keySet().iterator();
		LinkKey currKey;
		int currInLinks;
		int currOutLinks;
		EdgeBundleLinkRepresentation currLink;
		
		while (linksKeyItr.hasNext()) {
			currKey = linksKeyItr.next();
			
			currInLinks = elementPackagesDirectLinks.get(currKey).get(0);
			currOutLinks = elementPackagesDirectLinks.get(currKey).get(1);
			
			if (currInLinks != 0 || currOutLinks != 0) {
				double percentageMaxSize = (double)(currInLinks + currOutLinks) / (double)sysrep.maxNbreLinks;
				if (percentageMaxSize > 1.0) {
					percentageMaxSize = 1.0;
				}
				
				linkWidth = sysrep.linkMinSize + (percentageMaxSize * sysrep.linkMaxSize);
				linkLineWidth = sysrep.linkMinLineSize + (percentageMaxSize * sysrep.linkMaxLineSize);
				
				//System.out.println("LinkWidth: " + linkWidth);
				//System.out.println("LinkLineWidth: " + linkLineWidth);
				
				
				LinkedList<Integer> lca = new LinkedList<Integer>();
				LinkedList<NodeRepresentation> path = getNodesPath(sysrep.getEdgeBundlesNode(currKey.getStartNode()), sysrep.getEdgeBundlesNode(currKey.getEndNode()), lca, sysrep);	
				//path.removeFirst();
				//path.removeLast();
				path.addFirst(currKey.getStartNode());
				path.addLast(currKey.getEndNode());
				
				currLink = new EdgeBundleLinkRepresentation(currKey.getStartNode(), currKey.getEndNode(), currInLinks, currOutLinks, nbrePoints, linkWidth, linkLineWidth, Color.green, Color.red, false, null, path, straightenControlPoints, beta, degree, nbreSegments, (int)lca.getFirst(), removeLCA, verticalPlanar, horizontalPlanar);
				nodesEdgeBundles.put(currKey, currLink);
				
				nbreEdgesBundle++;
			}
		}
		
		//***********************************
		
		
		System.out.println("Nodes edges bundles Radial 2: " + nodesEdgeBundles.size());
		
		
		//Crée les liens entre les packages
		HashMap<LinkKey, LinkedList<Integer>> packagesDirectLinks = new HashMap<LinkKey, LinkedList<Integer>>();
		
		copyDirectLinks.clear();
		keysItr = nodesEdgeBundles.keySet().iterator();
		while (keysItr.hasNext()) {
			currLinkKey = keysItr.next();
			copyDirectLinks.put(currLinkKey, nodesEdgeBundles.get(currLinkKey));
		}
		
		
		for (PackageRepresentation pack : sysrep.getPackages()) {
			createsPackagesDirectLinks(packagesDirectLinks, copyDirectLinks, sysrep, pack);
		}
		
		linksKeyItr = packagesDirectLinks.keySet().iterator();
		while (linksKeyItr.hasNext()) {
			currKey = linksKeyItr.next();
			
			currInLinks = packagesDirectLinks.get(currKey).get(0);
			currOutLinks = packagesDirectLinks.get(currKey).get(1);
			
			if (currInLinks != 0 || currOutLinks != 0) {
				linkWidth = sysrep.linkMinSize + ((double)(currInLinks + currOutLinks) / (double)sysrep.maxNbreLinks) * sysrep.linkMaxSize;
				linkLineWidth = sysrep.linkMinLineSize + ((double)(currInLinks + currOutLinks) / (double)sysrep.maxNbreLinks) * sysrep.linkMaxLineSize;
				
				if (linkWidth > sysrep.linkMaxSize) {
					linkWidth = sysrep.linkMaxSize;
				}
				
				if (linkLineWidth > sysrep.linkMaxLineSize) {
					linkLineWidth = sysrep.linkMaxLineSize;
				}
				
				//System.out.println("LinkWidth: " + linkWidth);
				//System.out.println("LinkLineWidth: " + linkLineWidth);
				
				
				LinkedList<Integer> lca = new LinkedList<Integer>();
				LinkedList<NodeRepresentation> path = getNodesPath(sysrep.getEdgeBundlesNode(currKey.getStartNode()), sysrep.getEdgeBundlesNode(currKey.getEndNode()), lca, sysrep);
				//path.removeFirst();
				//path.removeLast();
				path.addFirst(currKey.getStartNode());
				path.addLast(currKey.getEndNode());
				
				currLink = new EdgeBundleLinkRepresentation(currKey.getStartNode(), currKey.getEndNode(), currInLinks, currOutLinks, nbrePoints, linkWidth, linkLineWidth, Color.green, Color.red, false, null, path, straightenControlPoints, beta, degree, nbreSegments, (int)lca.getFirst(), removeLCA, verticalPlanar, horizontalPlanar);
				nodesEdgeBundles.put(currKey, currLink);
			
				nbreEdgesBundle++;
			}
		}
		
		//***********************************
		
		
		System.out.println("Nodes edges bundles Radial 3: " + nodesEdgeBundles.size());
		
		
		//System.out.println("Nombre edges bundle: " + nbreEdgesBundle);
		
		
		return nodesEdgeBundles;
	}
	//**************************************************************
	*/
	
}

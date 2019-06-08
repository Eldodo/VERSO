package verso.representation.cubeLandscape.Layout;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import verso.graphics.primitives.PrimitiveColored;
import verso.representation.Layout;
import verso.representation.Renderable;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.link.DirectLinkRepresentation;
import verso.representation.cubeLandscape.representationModel.link.EdgeBundleLinkRepresentation;
import verso.representation.cubeLandscape.representationModel.link.LinkKey;
import verso.representation.cubeLandscape.representationModel.link.LinkRepresentation;
import verso.representation.cubeLandscape.representationModel.link.NodeRepresentation;

public class LinkAndNodeLayout extends Layout {
	public HashMap<NodeRepresentation, HashMap<NodeRepresentation, Double[]>> createLinksTable(SystemRepresentation sysRep) { //, PrimitiveColored coloredMesh, Color linkStartColor, Color linkEndColor, Color linkBidirectionalColor, int nbrePoints, boolean straightenControlPoints, double beta, int degree, int nbreSegments, boolean removeLCA) {
		HashMap<NodeRepresentation, HashMap<NodeRepresentation, Double[]>> links = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, Double[]>>();
		HashMap<String, ElementRepresentation> allElementsRepresentation = getAllElementsRepresentation(sysRep);
		Iterator<ElementRepresentation> elementsItr = allElementsRepresentation.values().iterator();
		ElementRepresentation currElement;
		Iterator<String> textTargetsItr;
		ElementRepresentation currLinkedElement;
		NodeRepresentation startNode, endNode;
		
		NodeRepresentation startNodeOnPackage, endNodeOnPackage;
		NodeRepresentation nodeOverElement;
		
		double linkWidth;
		double linkLineWidth;

		//Crée les liens entre les classes
		linkWidth = sysRep.linkMinSize;
		linkLineWidth = sysRep.linkMinLineSize + (1.0 / (double)sysRep.maxNbreLinks) * sysRep.linkMaxLineSize;
		
		while (elementsItr.hasNext()) {
			currElement = elementsItr.next();
			textTargetsItr = currElement.getElementModel().getTargets().iterator();		
			
			while (textTargetsItr.hasNext()) {			
				currLinkedElement = allElementsRepresentation.get(textTargetsItr.next());
				startNode = sysRep.getElementNode(currElement);
				endNode = sysRep.getElementNode(currLinkedElement);
				
				if (startNode != null && endNode != null && startNode != endNode) {
					LinkKey linkKey = new LinkKey(startNode, endNode);
					LinkKey reverseLinkKey = new LinkKey(endNode, startNode);
					
					if (links.get(reverseLinkKey) != null) {
						links.get(reverseLinkKey).setIsBidirectional(true);
					}
					else if (getRealParentNode(startNode) == getRealParentNode(endNode)) {
						nodeOverElement = sysRep.getNodeOverElement(startNode);
						if (nodeOverElement != null) {
							startNode = nodeOverElement;
						}
						
						nodeOverElement = sysRep.getNodeOverElement(endNode);
						if (nodeOverElement != null) {
							endNode = nodeOverElement;
						}
						
						//links.put(linkKey, new DirectLinkRepresentation(startNode, endNode, 0, 1, coloredMesh, linkWidth, linkLineWidth, linkStartColor, linkEndColor, false, linkBidirectionalColor));
					}
					else {
						LinkedList<Integer> lca = new LinkedList<Integer>();
						LinkedList<NodeRepresentation> path = getNodesPath(startNode, endNode, lca, sysRep);

						links.put(linkKey, new EdgeBundleLinkRepresentation(startNode, endNode, 0, 1, nbrePoints, linkWidth, linkLineWidth, linkStartColor, linkEndColor, false, linkBidirectionalColor, path, straightenControlPoints, beta, degree, nbreSegments, (int)lca.getFirst(), removeLCA));					
					}
				}
			}
		}
		//***********************************
			
			
			

		//Crée les liens entre les classes et les packages
		HashMap<LinkKey, LinkRepresentation> copyDirectLinks = new HashMap<LinkKey, LinkRepresentation>();
		Iterator<LinkKey> keysItr = links.keySet().iterator();
		LinkKey currLinkKey;
		while (keysItr.hasNext()) {
			currLinkKey = keysItr.next();
			copyDirectLinks.put(currLinkKey, links.get(currLinkKey));
		}
		
		elementsItr = allElementsRepresentation.values().iterator();
		HashMap<LinkKey, LinkedList<Integer>> elementToPackageLinks = new HashMap<LinkKey, LinkedList<Integer>>();
		
		while (elementsItr.hasNext()) {
			currElement = elementsItr.next();
			
			for (PackageRepresentation pack : sysRep.getPackages()) {
				createElementToPackageLinks(elementToPackageLinks, copyDirectLinks, sysRep, currElement, pack);
			}
		}
		
		Iterator<LinkKey> linksKeyItr = elementToPackageLinks.keySet().iterator();
		LinkKey currKey;
		int currInLinks;
		int currOutLinks;
		LinkRepresentation currLink;
		
		
		
		while (linksKeyItr.hasNext()) {			
			currKey = linksKeyItr.next();			
			
			currInLinks = elementToPackageLinks.get(currKey).get(0);
			currOutLinks = elementToPackageLinks.get(currKey).get(1);
			
			if (currInLinks != 0 || currOutLinks != 0) {
				double percentageMaxSize = (double)(currInLinks + currOutLinks) / (double)sysRep.maxNbreLinks;
				if (percentageMaxSize > 1.0) {
					percentageMaxSize = 1.0;
				}
				
				
				linkWidth = sysRep.computeLinkWidth(currInLinks + currOutLinks);
				linkLineWidth = sysRep.linkMinLineSize + (percentageMaxSize * sysRep.linkMaxLineSize);

				
				if (getRealParentNode(currKey.getStartNode()) == getRealParentNode(currKey.getEndNode())) {
					startNodeOnPackage = sysRep.getNodeOnPackage(currKey.getStartNode());
					if (startNodeOnPackage != null) {
						startNode = startNodeOnPackage;
					}
					else {
						startNode = currKey.getStartNode();
					}
					
					endNodeOnPackage = sysRep.getNodeOnPackage(currKey.getEndNode());
					if (endNodeOnPackage != null) {
						endNode = endNodeOnPackage;
					}
					else {
						endNode = currKey.getEndNode();
					}
					
					if (startNodeOnPackage != null || endNodeOnPackage != null) {
						currKey = new LinkKey(startNode, endNode);
					}
					
					//currLink = new DirectLinkRepresentation(currKey.getStartNode(), currKey.getEndNode(), currInLinks, currOutLinks, coloredMesh, linkWidth, linkLineWidth, linkStartColor, linkEndColor, false, null);
					//links.put(currKey, currLink);
				}
				else {
					LinkedList<Integer> lca = new LinkedList<Integer>();
					LinkedList<NodeRepresentation> path = getNodesPath(currKey.getStartNode(), currKey.getEndNode(), lca, sysRep);	
					
					currLink = new EdgeBundleLinkRepresentation(currKey.getStartNode(), currKey.getEndNode(), currInLinks, currOutLinks, nbrePoints, linkWidth, linkLineWidth, linkStartColor, linkEndColor, false, null, path, straightenControlPoints, beta, degree, nbreSegments, (int)lca.getFirst(), removeLCA);
					links.put(currKey, currLink);
				}
			}
		}
		//***********************************
		
		
		
		//Crée les liens entre les packages
		HashMap<LinkKey, LinkedList<Integer>> packageToPackageLinks = new HashMap<LinkKey, LinkedList<Integer>>();
		
		copyDirectLinks.clear();
		keysItr = links.keySet().iterator();
		while (keysItr.hasNext()) {
			currLinkKey = keysItr.next();
			copyDirectLinks.put(currLinkKey, links.get(currLinkKey));
		}
		
		
		for (PackageRepresentation pack : sysRep.getPackages()) {
			createPackageToPackageLinks(packageToPackageLinks, copyDirectLinks, sysRep, pack);
		}
		
		linksKeyItr = packageToPackageLinks.keySet().iterator();
		while (linksKeyItr.hasNext()) {
			currKey = linksKeyItr.next();
			
			currInLinks = packageToPackageLinks.get(currKey).get(0);
			currOutLinks = packageToPackageLinks.get(currKey).get(1);
			
			if (currInLinks != 0 || currOutLinks != 0) {
				double percentageMaxSize = (double)(currInLinks + currOutLinks) / (double)sysRep.maxNbreLinks;
				if (percentageMaxSize > 1.0) {
					percentageMaxSize = 1.0;
				}
				

				linkWidth = sysRep.computeLinkWidth(currInLinks + currOutLinks);
				linkLineWidth = sysRep.linkMinLineSize + ((double)(currInLinks + currOutLinks) / (double)sysRep.maxNbreLinks) * sysRep.linkMaxLineSize;				
				
				
				if (getRealParentNode(currKey.getStartNode()) == getRealParentNode(currKey.getEndNode())) {
					startNodeOnPackage = sysRep.getNodeOnPackage(currKey.getStartNode());
					if (startNodeOnPackage != null) {
						startNode = startNodeOnPackage;
					}
					else {
						startNode = currKey.getStartNode();
					}
					
					endNodeOnPackage = sysRep.getNodeOnPackage(currKey.getEndNode());
					if (endNodeOnPackage != null) {
						endNode = endNodeOnPackage;
					}
					else {
						endNode = currKey.getEndNode();
					}
					
					if (startNodeOnPackage != null || endNodeOnPackage != null) {
						currKey = new LinkKey(startNode, endNode);
					}
					
					//currLink = new DirectLinkRepresentation(currKey.getStartNode(), currKey.getEndNode(), currInLinks, currOutLinks, coloredMesh, linkWidth, linkLineWidth, linkStartColor, linkEndColor, false, null);
					//links.put(currKey, currLink);
				}
				else {
					LinkedList<Integer> lca = new LinkedList<Integer>();
					LinkedList<NodeRepresentation> path = getNodesPath(currKey.getStartNode(), currKey.getEndNode(), lca, sysRep);
					
					currLink = new EdgeBundleLinkRepresentation(currKey.getStartNode(), currKey.getEndNode(), currInLinks, currOutLinks, nbrePoints, linkWidth, linkLineWidth, linkStartColor, linkEndColor, false, null, path, straightenControlPoints, beta, degree, nbreSegments, (int)lca.getFirst(), removeLCA);
					links.put(currKey, currLink);
				}	
			}
		}
		//***********************************

		
		return links;
	}
	
		
		
	/*	
	//Fonction pour créer les liens directs entre les classes et les packages
	public DirectLinkRepresentation createDirectLinks(NodeRepresentation startNode, NodeRepresentation endNode, double linkMinSize, double linkMaxSize, PrimitiveColored coloredMesh, Color linkStartColor, Color linkEndColor, Color bidirectionalColor) {

		//Crée les liens entre les classes
		while (elementsItr.hasNext()) {
			currElement = elementsItr.next();		
			
			textTargetsItr = currElement.getElementModel().getTargets().iterator();
		
			while (textTargetsItr.hasNext()) {			
				currLinkedElement = allElementsRepresentation.get(textTargetsItr.next());				
				
				startNode = sysrep.getElementNode(currElement);
				endNode = sysrep.getElementNode(currLinkedElement);

				if (startNode != null && endNode != null) {
					LinkKey linkKey = new LinkKey(startNode, endNode);
					LinkKey reverseLinkKey = new LinkKey(endNode, startNode);
					
					if (nodesDirectLink.get(reverseLinkKey) != null) {
						nodesDirectLink.get(reverseLinkKey).setIsBidirectional(true);
					}
					else {
						if (startNode != endNode) {
							nodesDirectLink.put(linkKey, new DirectLinkRepresentation(startNode, endNode, 0, 1, coloredMesh, linkWidth, linkLineWidth, linkStartColor, linkEndColor, false, bidirectionalColor));
						}
					}
				}			
			}
		}
		//***********************************
		

		
		
		//Crée les liens entre les classes et les packages
		HashMap<LinkKey, LinkRepresentation> copyDirectLinks = new HashMap<LinkKey, LinkRepresentation>();
		Iterator<LinkKey> keysItr = nodesDirectLink.keySet().iterator();
		LinkKey currLinkKey;
		while (keysItr.hasNext()) {
			currLinkKey = keysItr.next();
			copyDirectLinks.put(currLinkKey, nodesDirectLink.get(currLinkKey));
		}
		
		elementsItr = allElementsRepresentation.values().iterator();
		HashMap<LinkKey, LinkedList<Integer>> elementPackagesDirectLinks = new HashMap<LinkKey, LinkedList<Integer>>();
		
		while (elementsItr.hasNext()) {
			currElement = elementsItr.next();
			
			for (PackageRepresentation pack : sysrep.getPackages()) {
				createElementToPackageLinks(elementPackagesDirectLinks, copyDirectLinks, sysrep, currElement, pack);
			}
		}
		
		Iterator<LinkKey> linksKeyItr = elementPackagesDirectLinks.keySet().iterator();
		LinkKey currKey;
		int currInLinks;
		int currOutLinks;
		DirectLinkRepresentation currLink;
		while (linksKeyItr.hasNext()) {
			currKey = linksKeyItr.next();
			
			currInLinks = elementPackagesDirectLinks.get(currKey).get(0);
			currOutLinks = elementPackagesDirectLinks.get(currKey).get(1);
			
			if (currInLinks != 0 || currOutLinks != 0) {
				linkWidth = sysrep.linkMinSize + (Math.log(currInLinks + currOutLinks) / (double)sysrep.maxNbreLinks) * sysrep.linkMaxSize;
				linkLineWidth = sysrep.linkMinLineSize + (Math.log(currInLinks + currOutLinks) / (double)sysrep.maxNbreLinks) * sysrep.linkMaxLineSize;
				
				if (linkWidth > sysrep.linkMaxSize) {
					linkWidth = sysrep.linkMaxSize;
				}
				
				if (linkLineWidth > sysrep.linkMaxLineSize) {
					linkLineWidth = sysrep.linkMaxLineSize;
				}
				
				currLink = new DirectLinkRepresentation(currKey.getStartNode(), currKey.getEndNode(), currInLinks, currOutLinks, coloredMesh, linkWidth, linkLineWidth, Color.green, Color.red, false, null);
				nodesDirectLink.put(currKey, currLink);
			}
		}
		
		//***********************************
		
		
		
		
		//Crée les liens entre les packages
		HashMap<LinkKey, LinkedList<Integer>> packagesDirectLinks = new HashMap<LinkKey, LinkedList<Integer>>();
		
		copyDirectLinks.clear();
		keysItr = nodesDirectLink.keySet().iterator();
		while (keysItr.hasNext()) {
			currLinkKey = keysItr.next();
			copyDirectLinks.put(currLinkKey, nodesDirectLink.get(currLinkKey));
		}
		
		
		for (PackageRepresentation pack : sysrep.getPackages()) {
			createPackageToPackageLinks(packagesDirectLinks, copyDirectLinks, sysrep, pack);
		}
		
		linksKeyItr = packagesDirectLinks.keySet().iterator();
		while (linksKeyItr.hasNext()) {
			currKey = linksKeyItr.next();
			
			currInLinks = packagesDirectLinks.get(currKey).get(0);
			currOutLinks = packagesDirectLinks.get(currKey).get(1);
			
			if (currInLinks != 0 || currOutLinks != 0) {
				double percentageMaxSize = Math.log(currInLinks + currOutLinks) / (double)sysrep.maxNbreLinks;
				if (percentageMaxSize > 1.0) {
					percentageMaxSize = 1.0;
				}
				
				linkWidth = sysrep.linkMinSize + (percentageMaxSize * sysrep.linkMaxSize);
				linkLineWidth = sysrep.linkMinLineSize + (percentageMaxSize * sysrep.linkMaxLineSize);
				currLink = new DirectLinkRepresentation(currKey.getStartNode(), currKey.getEndNode(), currInLinks, currOutLinks, coloredMesh, linkWidth, linkLineWidth, Color.green, Color.red, false, null);
				nodesDirectLink.put(currKey, currLink);
			}
		}
		
		//***********************************
		
		
		
		return nodesDirectLink;
	}
	//**************************************************************
	
	
	
	
 
	
	
	
	
	//Fonction pour créer les Edge Bundles entre les classes
	public HashMap<LinkKey, EdgeBundleLinkRepresentation> createEdgeBundles(SystemRepresentation sysrep, int nbrePoints, Color edgeBundleStartColor, Color edgeBundleEndColor, Color bidirectionalColor, boolean straightenControlPoints, double beta, int degree, int nbreSegments, boolean removeLCA, boolean verticalPlanar, boolean horizontalPlanar) {
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
		
		
		
		//Crée les liens entre les classes
		linkWidth = sysrep.linkMinSize;
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
					LinkedList<NodeRepresentation> path = getNodesPath(startNode, endNode, lca, sysrep);					
					
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
		//***********************************
		
		
		
		
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
				createElementToPackageLinks(elementPackagesDirectLinks, copyDirectLinks, sysrep, currElement, pack);
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
				
				
				linkWidth = sysrep.computeLinkWidth(currInLinks + currOutLinks);
				linkLineWidth = sysrep.linkMinLineSize + (percentageMaxSize * sysrep.linkMaxLineSize);

				LinkedList<Integer> lca = new LinkedList<Integer>();
				LinkedList<NodeRepresentation> path = getNodesPath(currKey.getStartNode(), currKey.getEndNode(), lca, sysrep);	
				
				currLink = new EdgeBundleLinkRepresentation(currKey.getStartNode(), currKey.getEndNode(), currInLinks, currOutLinks, nbrePoints, linkWidth, linkLineWidth, edgeBundleStartColor, edgeBundleEndColor, false, null, path, straightenControlPoints, beta, degree, nbreSegments, (int)lca.getFirst(), removeLCA, verticalPlanar, horizontalPlanar);
				nodesEdgeBundles.put(currKey, currLink);
				
				nbreEdgesBundle++;
			}
		}
		//***********************************
		
		
		
		//Crée les liens entre les packages
		HashMap<LinkKey, LinkedList<Integer>> packagesDirectLinks = new HashMap<LinkKey, LinkedList<Integer>>();
		
		copyDirectLinks.clear();
		keysItr = nodesEdgeBundles.keySet().iterator();
		while (keysItr.hasNext()) {
			currLinkKey = keysItr.next();
			copyDirectLinks.put(currLinkKey, nodesEdgeBundles.get(currLinkKey));
		}
		
		
		for (PackageRepresentation pack : sysrep.getPackages()) {
			createPackageToPackageLinks(packagesDirectLinks, copyDirectLinks, sysrep, pack);
		}
		
		linksKeyItr = packagesDirectLinks.keySet().iterator();
		while (linksKeyItr.hasNext()) {
			currKey = linksKeyItr.next();
			
			currInLinks = packagesDirectLinks.get(currKey).get(0);
			currOutLinks = packagesDirectLinks.get(currKey).get(1);
			
			if (currInLinks != 0 || currOutLinks != 0) {
				
				linkWidth = sysrep.computeLinkWidth(currInLinks + currOutLinks);
				linkLineWidth = sysrep.linkMinLineSize + ((double)(currInLinks + currOutLinks) / (double)sysrep.maxNbreLinks) * sysrep.linkMaxLineSize;				
				
				LinkedList<Integer> lca = new LinkedList<Integer>();
				LinkedList<NodeRepresentation> path = getNodesPath(currKey.getStartNode(), currKey.getEndNode(), lca, sysrep);
				
				currLink = new EdgeBundleLinkRepresentation(currKey.getStartNode(), currKey.getEndNode(), currInLinks, currOutLinks, nbrePoints, linkWidth, linkLineWidth, edgeBundleStartColor, edgeBundleEndColor, false, null, path, straightenControlPoints, beta, degree, nbreSegments, (int)lca.getFirst(), removeLCA, verticalPlanar, horizontalPlanar);
				nodesEdgeBundles.put(currKey, currLink);
			
				nbreEdgesBundle++;
			}
		}
		//***********************************

		return nodesEdgeBundles;
	}
	//**************************************************************
	*/
	
	
	protected boolean createElementToPackageLinks(HashMap<LinkKey, LinkedList<Integer>> elementToPackageLinks, HashMap<LinkKey, LinkRepresentation> elementsLinks, SystemRepresentation sysrep, ElementRepresentation element, PackageRepresentation pack) {
		LinkedList<Integer> packageInOutLinks = new LinkedList<Integer>();
		LinkedList<LinkKey> subPackagesLinkKeys = new LinkedList<LinkKey>();
		boolean subPackageContainsElement = false;
		
		for (PackageRepresentation p : pack.getPackages()) {
			if (createElementToPackageLinks(elementToPackageLinks, elementsLinks, sysrep, element, p)) {				
				subPackageContainsElement = true;
			}
		}
					
		if (pack.getElement(element.getElementModel().getName()) != null || subPackageContainsElement) {			
			return true;
		} 
		else {
			if (!pack.isFakePackage()) {
				for (PackageRepresentation p : pack.getSubPackages()) {
					subPackagesLinkKeys.add(new LinkKey(sysrep.getElementNode(element), sysrep.getPackageNode(p)));
				}
				
				
				int inLinks = 0;
				int outLinks = 0;
				
				Iterator<LinkKey> subPackagesLinkKeysItr = subPackagesLinkKeys.iterator();
				LinkedList<Integer> currPackageInOutLinks;
				
				while (subPackagesLinkKeysItr.hasNext()) {
					currPackageInOutLinks = elementToPackageLinks.get(subPackagesLinkKeysItr.next());
					
					if (currPackageInOutLinks != null) {
						inLinks += currPackageInOutLinks.get(0);
						outLinks += currPackageInOutLinks.get(1);
					}
					
				}
				
				LinkKey elementsLinkKey;
				LinkKey reverseKey;
				LinkRepresentation elementsLink;
				for (ElementRepresentation elementInPackage : pack.getSubElements()) {
					elementsLinkKey = new LinkKey(sysrep.getElementNode(element), sysrep.getElementNode(elementInPackage));
					reverseKey = new LinkKey(elementsLinkKey.getEndNode(), elementsLinkKey.getStartNode());
					
					elementsLink = elementsLinks.get(elementsLinkKey);
					
					if (elementsLink != null) {					
						if (elementsLink.getIsBidirectional()) {
							inLinks++;
							outLinks++;
						} 
						else {
							outLinks++;
						}	
					}
					else {
						elementsLink = elementsLinks.get(reverseKey);
						if (elementsLink != null) {						
							if (elementsLink.getIsBidirectional()) {
								inLinks++;
								outLinks++;
							}
							else {
								inLinks++;
							}
						}
					}
				}
				
				/*
				if (inLinks != 0 && outLinks != 0) {
					System.out.println("InLinks: " + inLinks);
					System.out.println("OutLinks: " + outLinks);
					System.out.println("");
				}
				*/
				
				packageInOutLinks.add(0, inLinks);
				packageInOutLinks.add(1, outLinks);
				elementToPackageLinks.put(new LinkKey(sysrep.getElementNode(element), sysrep.getPackageNode(pack)), packageInOutLinks);
			}
			
			return false;
		}
	}

	protected void createPackageToPackageLinks(HashMap<LinkKey, LinkedList<Integer>> packagesDirectLinks, HashMap<LinkKey, LinkRepresentation> nodesDirectLinks, SystemRepresentation sysrep, PackageRepresentation pack) {
		LinkedList<Integer> packageInOutLinks;

		for (PackageRepresentation subPack : pack.getPackages()) {
			createPackageToPackageLinks(packagesDirectLinks, nodesDirectLinks, sysrep, subPack);
		}
		
		LinkedList<PackageRepresentation> systemPackages = sysrep.getSystemPackages();
		LinkKey linkKey;
		LinkKey reverseKey;
		LinkRepresentation currDirectLink;
		LinkedList<Integer> currPackLinks;

		if (!pack.isFakePackage()) {
			for (PackageRepresentation p : systemPackages) {
				if (!p.isFakePackage()) {
					int inLinks = 0;
					int outLinks = 0;
					linkKey = new LinkKey(sysrep.getPackageNode(p), sysrep.getPackageNode(pack));
				
					if (packagesDirectLinks.get(linkKey) == null) {
						for (PackageRepresentation subPack : pack.getSubPackages()) {
							linkKey = new LinkKey(sysrep.getPackageNode(subPack), sysrep.getPackageNode(p));
							reverseKey = new LinkKey(linkKey.getEndNode(), linkKey.getStartNode());
							
							currPackLinks = packagesDirectLinks.get(linkKey);
							if (currPackLinks == null) {
								currPackLinks = packagesDirectLinks.get(reverseKey);
							}
							
							if (currPackLinks != null) {
								inLinks += currPackLinks.get(0);
								outLinks += currPackLinks.get(1);
							}
						}
						
						for (ElementRepresentation element : pack.getSubElements()) {
							linkKey = new LinkKey(sysrep.getElementNode(element), sysrep.getPackageNode(p));
							currDirectLink = nodesDirectLinks.get(linkKey);
							if (currDirectLink != null) {
								inLinks += currDirectLink.getNbreInLinks();
								outLinks += currDirectLink.getNbreOutLinks();
							}
						}
					}
					
					packageInOutLinks = new LinkedList<Integer>();
					packageInOutLinks.add(0, inLinks);
					packageInOutLinks.add(1, outLinks);
					linkKey = new LinkKey(sysrep.getPackageNode(pack), sysrep.getPackageNode(p));
					packagesDirectLinks.put(linkKey, packageInOutLinks);
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	protected LinkedList<NodeRepresentation> getNodesPath(NodeRepresentation startNode, NodeRepresentation endNode, LinkedList<Integer> lca, SystemRepresentation sysrep) {
		int startNodeLevel = startNode.getNodeLevel();
		int endNodeLevel = endNode.getNodeLevel();
		LinkedList<NodeRepresentation> startNodePath = new LinkedList<NodeRepresentation>();
		LinkedList<NodeRepresentation> endNodePath = new LinkedList<NodeRepresentation>();
		
		if (startNodeLevel != endNodeLevel) {
			if (startNodeLevel > endNodeLevel) {								
				while (startNodeLevel > endNodeLevel && startNode != null) {
					startNodeLevel--;
					startNodePath.add(startNode);
					startNode = startNode.getParentNode();
				}
			}
			else {
				while (endNodeLevel > startNodeLevel && endNode != null) {
					endNodeLevel--;
					endNodePath.addFirst(endNode);
					endNode = endNode.getParentNode();
				}
			}
		}
		
		while (startNode.getParentNode() != endNode.getParentNode()) {						
			startNodePath.add(startNode);
			endNodePath.addFirst(endNode);
			
			startNode = startNode.getParentNode();
			endNode = endNode.getParentNode();
		}
		
		startNodePath.add(startNode);
		startNodePath.add(startNode.getParentNode());
		endNodePath.addFirst(endNode);
		
		lca.add(startNodePath.size()-1);
		
		startNodePath.addAll(endNodePath);
		
		
		
		//À garder seulement si on ne merge pas les noeuds trop proche
		//return startNodePath;
		//***********************************
		
		
		
		LinkedList<NodeRepresentation> mergedPathNodes = mergePathNodes(startNodePath, 0.1);
		
		NodeRepresentation newStartNode = sysrep.getNodeOnPackage(mergedPathNodes.getFirst());
		if (newStartNode != null) {	
			//Sert à enlever le "double" du noeud sur le package dans le path de noeud
			//mergedPathNodes.removeFirst();
			//***********************************

			mergedPathNodes.addFirst(newStartNode);
		}
		
		NodeRepresentation newEndNode = sysrep.getNodeOnPackage(mergedPathNodes.getLast());
		if (newEndNode != null) {
			//Sert à enlever le "double" du noeud sur le package dans le path de noeud
			//mergedPathNodes.removeLast();
			//***********************************
			
			mergedPathNodes.addLast(newEndNode);
		}
		
		
		
		
		//Code pour enlever le noeud du parent (le vrai parent, celui du faux package qui contient les classes)
		/*
		if (mergedPathNodes.get(0).getEntityRepresentation() instanceof ElementRepresentation) {
			mergedPathNodes.remove(2);
		}
		*/
		//*****************************************************************
		
		
		if (mergedPathNodes.size() > 3) {
			mergedPathNodes.remove(mergedPathNodes.size()-2);
		}
		
		
		
		
		
		if (mergedPathNodes.size() > 3) {
			mergedPathNodes.add(1, mergedPathNodes.get(1));
			//mergedPathNodes(1, mergedPathNodes.get(1));
			//mergedPathNodes(1, mergedPathNodes.get(1));	
		}
		
		return mergedPathNodes;
	}
	
	
	protected LinkedList<NodeRepresentation> mergePathNodes(LinkedList<NodeRepresentation> path, double mergeTreshold) {
		LinkedList<NodeRepresentation> mergedPath = new LinkedList<NodeRepresentation>();
		Iterator<NodeRepresentation> pathItr = path.iterator();
		NodeRepresentation currNode = pathItr.next();
		NodeRepresentation nextNode;
		double []nodesVector = new double[3];
		double nodesDistance;
		
		mergedPath.add(currNode);
		while (pathItr.hasNext()) {
			nextNode = pathItr.next();
			
			nodesVector[0] = currNode.getposXd() - nextNode.getposXd();
			nodesVector[1] = currNode.getposYd() - nextNode.getposYd();
			nodesVector[2] = currNode.getposZd() - nextNode.getposZd();
			nodesDistance = Math.sqrt(nodesVector[0]*nodesVector[0] + nodesVector[1]*nodesVector[1] + nodesVector[2]*nodesVector[2]);
			
			if (nodesDistance >= mergeTreshold) {
				mergedPath.add(nextNode);
				currNode = nextNode;
			}
		}
		
		return mergedPath;
	}
	
	
	
	
	//Fonction utilitaire pour créer le HashMap contenant tous les éléments de la hiérarchie, classés selon leur nom.
	
	protected HashMap<String, ElementRepresentation> getAllElementsRepresentation(SystemRepresentation sysrep) {
		HashMap<String, ElementRepresentation> allElementsRepresentation = new HashMap<String, ElementRepresentation>();
		
		
		int currLevel = 0;
		//int maxLevel = sysrep.findMaxLevel();
		int maxLevel = sysrep.getPackages().iterator().next().computeMaxLevel();
		
		for (PackageRepresentation p : sysrep.getPackages()) {
			allElementsRepresentation.putAll(getAllPackageElementsRepresentation(p, currLevel, maxLevel));
		}
		
		return allElementsRepresentation;
	}

	private HashMap<String, ElementRepresentation> getAllPackageElementsRepresentation(PackageRepresentation pack, int currLevel, int maxLevel) {
		HashMap<String, ElementRepresentation> allPackageElementsRepresentation = new HashMap<String, ElementRepresentation>();
		Iterator<ElementRepresentation> packageElementsItr = pack.getElements().iterator();
		ElementRepresentation currElement;
				
		while (packageElementsItr.hasNext()) {
			currElement = packageElementsItr.next();
			allPackageElementsRepresentation.put(currElement.getElementModel().getName(), currElement);
		}

		for (PackageRepresentation p : pack.getPackages()) {
			allPackageElementsRepresentation.putAll(getAllPackageElementsRepresentation(p, currLevel+1, maxLevel));
		}
		
		return allPackageElementsRepresentation;
	}
	
	protected Color[] getLinkBalancedColors(Color linkStartColor, Color linkEndColor, int inLinks, int outLinks) {
		Color[] balancedColors = new Color[2];
		Color balancedStartColor, balancedEndColor;
		double red, green, blue;
		double ratioIn = (double)inLinks / (double)(inLinks + outLinks);
		double ratioOut = (double)outLinks / (double)(inLinks + outLinks);
		
		red = (ratioOut * (linkStartColor.getRed() / 255.0)) + (ratioIn * (linkEndColor.getRed() / 255.0));
		green = (ratioOut * (linkStartColor.getGreen() / 255.0)) + (ratioIn * (linkEndColor.getGreen() / 255.0));
		blue = (ratioOut * (linkStartColor.getBlue() / 255.0)) + (ratioIn * (linkEndColor.getBlue() / 255.0));
		
		balancedStartColor = new Color((float)red, (float)green, (float)blue);
		
		red = (ratioIn * (linkStartColor.getRed() / 255.0)) + (ratioOut * (linkEndColor.getRed() / 255.0));
		green = (ratioIn * (linkStartColor.getGreen() / 255.0)) + (ratioOut * (linkEndColor.getGreen() / 255.0));
		blue = (ratioIn * (linkStartColor.getBlue() / 255.0)) + (ratioOut * (linkEndColor.getBlue() / 255.0));
		
		balancedEndColor = new Color((float)red, (float)green, (float)blue);
		
		balancedColors[0] = balancedStartColor;
		balancedColors[1] = balancedEndColor;
		
		return balancedColors;
	}
	
	public static NodeRepresentation getRealParentNode(NodeRepresentation node) {
		NodeRepresentation realNode = node.getParentNode();
		
		if (realNode.getEntityRepresentation() instanceof PackageRepresentation) {
			if (((PackageRepresentation)realNode.getEntityRepresentation()).isFakePackage()) {
				realNode = getRealParentNode(realNode);
			}
		}
		
		return realNode;
	}
	
	//******************************************************************************************************************

	
	
	
}

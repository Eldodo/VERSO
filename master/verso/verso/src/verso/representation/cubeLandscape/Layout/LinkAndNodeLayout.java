package verso.representation.cubeLandscape.Layout;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import verso.graphics.primitives.Primitive;
import verso.representation.Layout;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.link.NodeRepresentation;

public abstract class LinkAndNodeLayout extends Layout {
	public abstract HashMap<PackageRepresentation, NodeRepresentation> createPackagesNodes(SystemRepresentation sysrep,
			float levelHeight, Primitive packageNodeMesh, float packageNodeSize, Color packageNodeColor);
	
	
	public abstract HashMap<ElementRepresentation, NodeRepresentation> createElementsNodes(SystemRepresentation sysrep,
			float levelHeight, Primitive elementNodeMesh, float elementNodeSize, Color elementNodeColor);


	public abstract HashMap<PackageRepresentation, NodeRepresentation> createPackagesNodes(
			PackageRepresentation currPack, int currLevel, int maxLevel, float minY, float levelHeight,
			Primitive packageNodeMesh, float packageNodeSize, Color packageNodeColor, NodeRepresentation parentNode);

	public HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> createLinksTable(
			SystemRepresentation sysRep) { 
		HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> linksTable = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>>();
		HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> reverseLinksTable = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>>();
		
		HashMap<String, ElementRepresentation> allElementsRepresentation = getAllElementsRepresentation(sysRep);
		Iterator<ElementRepresentation> elementsItr = allElementsRepresentation.values().iterator();
		

		System.out.println("CRÉATION DES LIENS D'INVOCATION ÉLÉMENTS-ÉLÉMENTS");

		// Crée les liens entre les classes
		while (elementsItr.hasNext()) {
			ElementRepresentation currElement = elementsItr.next();
			Iterator<String> textTargetsItr = currElement.getElementModel().getTargets().iterator();

			while (textTargetsItr.hasNext()) {
				String txtTarget = textTargetsItr.next();
//				System.out.println(txtTarget);
				ElementRepresentation currLinkedElement = allElementsRepresentation.get(txtTarget);
				NodeRepresentation startNode = sysRep.getElementNode(currElement);
				NodeRepresentation endNode = sysRep.getElementNode(currLinkedElement);

				if (startNode != null && endNode != null && startNode != endNode) {
					if (linksTable.get(endNode) != null && linksTable.get(endNode).get(startNode) != null) {
						linksTable.get(endNode).get(startNode)[0] = 1;
					} else {
						
						if (linksTable.get(startNode) == null) 
							linksTable.put(startNode, new HashMap<NodeRepresentation, Integer[]>());
						linksTable.get(startNode).put(endNode, new Integer[] { 0, 1 });

						if (reverseLinksTable.get(endNode) == null) 
							reverseLinksTable.put(endNode, new HashMap<NodeRepresentation, Integer[]>());
						reverseLinksTable.get(endNode).put(startNode, new Integer[] { 1, 0 });
						
					}
				}
			}
		}

		System.out.println("FIN CRÉATION\n");
		
		System.out.println("CRÉATION DES LIENS D'INVOCATION ÉLÉMENTS-PACKAGES");
			
		//Crée les liens entre les classes et les packages
		elementsItr = allElementsRepresentation.values().iterator();		
		while (elementsItr.hasNext()) {
			ElementRepresentation  currElement = elementsItr.next();
			createElementToPackageLinks(linksTable, reverseLinksTable, sysRep, currElement, allElementsRepresentation);
		}
		
		System.out.println("FIN CRÉATION\n");
		
		
		
		
		System.out.println("CRÉATION DES LIENS D'INVOCATION PACKAGES-PACKAGES");
		
		//Crée les liens entre les packages
		for (PackageRepresentation pack : sysRep.getPackages()) {
			createPackageToPackageLinks(linksTable, sysRep, pack);
		}
		
		System.out.println("FIN CRÉATION\n");

		return linksTable;
	}
	
	
	
	
	
	public HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> createParentLinksTable(SystemRepresentation sysRep) {
		HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> parentLinksTable = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>>();
		HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> reverseParentLinksTable = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>>();
		HashMap<String, ElementRepresentation> allElementsRepresentation = getAllElementsRepresentation(sysRep);
		Iterator<ElementRepresentation> elementsItr = allElementsRepresentation.values().iterator();
		ElementRepresentation currElement;
		String elementParent;
		ElementRepresentation currLinkedElement;
		NodeRepresentation startNode, endNode;
		Integer[] currInOut;

		
		
		
		System.out.println("CRÉATION DES LIENS D'HÉRITAGE ÉLÉMENTS-ÉLÉMENTS");
		
		//Crée les liens entre les classes
		while (elementsItr.hasNext()) {
			currElement = elementsItr.next();
			elementParent = currElement.getElementModel().getParentText();		
			
			if (elementParent != null && !elementParent.equals("")) {
				currLinkedElement = allElementsRepresentation.get(elementParent);
				startNode = sysRep.getElementNode(currElement);
				endNode = sysRep.getElementNode(currLinkedElement);
				
				if (startNode != null && endNode != null && startNode != endNode) {					
					if (parentLinksTable.get(endNode) != null && parentLinksTable.get(endNode).get(startNode) != null) {
						parentLinksTable.get(endNode).get(startNode)[0] = 1;
					}
					else {
						if (parentLinksTable.get(startNode) == null) {
							parentLinksTable.put(startNode, new HashMap<NodeRepresentation, Integer[]>());
						}
				
						currInOut = new Integer[2];
						currInOut[0] = 0;
						currInOut[1] = 1;
						parentLinksTable.get(startNode).put(endNode, currInOut);
						
						
						if (reverseParentLinksTable.get(endNode) == null) {
							reverseParentLinksTable.put(endNode, new HashMap<NodeRepresentation, Integer[]>());
						}
						
						currInOut = new Integer[2];
						currInOut[0] = 1;
						currInOut[1] = 0;
						reverseParentLinksTable.get(endNode).put(startNode, currInOut);
					}
				}
			}
		}
		
		System.out.println("FIN CRÉATION\n");
		
		
		
		
		System.out.println("CRÉATION DES LIENS D'HÉRITAGE ÉLÉMENTS-PACKAGES");

		//Crée les liens entre les classes et les packages
		elementsItr = allElementsRepresentation.values().iterator();		
		while (elementsItr.hasNext()) {
			currElement = elementsItr.next();
			createElementToPackageLinks(parentLinksTable, reverseParentLinksTable, sysRep, currElement, allElementsRepresentation);
		}
		
		System.out.println("FIN CRÉATION\n");
		
		
		
		
		System.out.println("CRÉATION DES LIENS D'HÉRITAGE PACKAGES-PACKAGES");

		//Crée les liens entre les packages
		for (PackageRepresentation pack : sysRep.getPackages()) {
			createPackageToPackageLinks(parentLinksTable, sysRep, pack);
		}
		
		System.out.println("FIN CRÉATION\n");
		

		return parentLinksTable;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	protected void createElementToPackageLinks(HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> linksTable, HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> reverseLinksTable, SystemRepresentation sysrep, ElementRepresentation element, HashMap<String, ElementRepresentation> allElementsRepresentation) {
		//boolean subPackageContainsElement = false;
		
		/*
		for (PackageRepresentation p : pack.getPackages()) {
			if (createElementToPackageLinks(linksTable, sysrep, element, p)) {				
				subPackageContainsElement = true;
			}
		}
		*/
		/*
		if (pack.getElement(element.getElementModel().getName()) != null || subPackageContainsElement) {						
			return true;
		} 
		else {
		*/
		
		LinkedList<PackageRepresentation> elementAncestors = sysrep.findPackageAncestors(element);
		HashSet<ElementRepresentation> linkedElements = new HashSet<ElementRepresentation>();
		HashMap<Integer, LinkedList<PackageRepresentation>> linkedPackages = new HashMap<Integer, LinkedList<PackageRepresentation>>();
		PackageRepresentation currPackage;
		NodeRepresentation currPackageNode;
		int currPackLevel;
		
		for (String target : element.getElementModel().getTargets()) {
			linkedElements.add(allElementsRepresentation.get(target));
		}
		
		if (reverseLinksTable.get(sysrep.getElementNode(element)) != null) {
			for (NodeRepresentation key : reverseLinksTable.get(sysrep.getElementNode(element)).keySet()) {
				linkedElements.add((ElementRepresentation)key.getEntityRepresentation());
			}
		}
		
		for (ElementRepresentation currElement : linkedElements) {
			if (currElement != null) {
				currPackageNode = sysrep.getElementNode(currElement).getParentNode();
				currPackage = (PackageRepresentation)currPackageNode.getEntityRepresentation();
				
				while (!elementAncestors.contains(currPackage)) {
					currPackLevel = currPackage.getPackageLevel();
					
					if (linkedPackages.get(currPackLevel) == null) {
						linkedPackages.put(currPackLevel, new LinkedList<PackageRepresentation>());
					}
					
					if (!linkedPackages.get(currPackLevel).contains(currPackage)) {
						linkedPackages.get(currPackLevel).add(currPackage);
					}
					
					currPackageNode = currPackageNode.getParentNode();
					currPackage = (PackageRepresentation)currPackageNode.getEntityRepresentation();
				}
			}
		}
		
		LinkedList<Integer> sortedKeys = new LinkedList<Integer>();
		sortedKeys.addAll(linkedPackages.keySet());
		Collections.sort(sortedKeys);
		
		for (Integer key : sortedKeys) {
			for (PackageRepresentation pack : linkedPackages.get(key)) {
				if (!pack.isFakePackage() && !sysrep.findPackageAncestors(element).contains(pack)) {
					NodeRepresentation startNode, endNode;
					Integer[] inOutLinks = {0, 0};
					Integer[] currInOutLinks;
					
					startNode = sysrep.getElementNode(element);
					for (PackageRepresentation p : pack.getSubPackages()) {
						endNode = sysrep.getPackageNode(p);
						
						if (linksTable.get(startNode) != null) {
							currInOutLinks = linksTable.get(startNode).get(endNode);
							if (currInOutLinks != null) {
								inOutLinks[0] += currInOutLinks[0];
								inOutLinks[1] += currInOutLinks[1];
							}
						}
					}
					
					for (ElementRepresentation elementInPackage : pack.getSubElements()) {
						endNode = sysrep.getElementNode(elementInPackage);
						
						if (linksTable.get(startNode) != null && linksTable.get(startNode).get(endNode) != null) {
							currInOutLinks = linksTable.get(startNode).get(endNode);
							inOutLinks[0] += currInOutLinks[0];
							inOutLinks[1] += currInOutLinks[1];
						}
						else if (linksTable.get(endNode) != null && linksTable.get(endNode).get(startNode) != null) {
							currInOutLinks = linksTable.get(endNode).get(startNode);
							inOutLinks[0] += currInOutLinks[1];
							inOutLinks[1] += currInOutLinks[0];
							
						}
					}
					
					if (inOutLinks[0] > 0 || inOutLinks[1] > 0) {
						endNode = sysrep.getPackageNode(pack);
						
						if (linksTable.get(startNode) == null) {
							linksTable.put(startNode, new HashMap<NodeRepresentation, Integer[]>());
						}
						
						linksTable.get(startNode).put(endNode, inOutLinks);	
					}
				}
			}
		}
	}

	protected void createPackageToPackageLinks(HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> linksTable, SystemRepresentation sysrep, PackageRepresentation pack) {
		for (PackageRepresentation subPack : pack.getPackages()) {
			createPackageToPackageLinks(linksTable, sysrep, subPack);
		}
		
		LinkedList<PackageRepresentation> systemPackages = sysrep.getSystemPackages();

		if (!pack.isFakePackage()) {
			for (PackageRepresentation p : systemPackages) {
				if (!p.isFakePackage() && !sysrep.findPackageDescendants(pack).contains(p)) {
					NodeRepresentation startNode, endNode;
					Integer[] inOutLinks = {0, 0};
					Integer[] currInOutLinks;
					
					startNode = sysrep.getPackageNode(pack);
					endNode = sysrep.getPackageNode(p);
				
					if (linksTable.get(endNode) == null || linksTable.get(endNode).get(startNode) == null) {
						for (PackageRepresentation subPack : pack.getSubPackages()) {
							startNode = sysrep.getPackageNode(subPack);
							endNode = sysrep.getPackageNode(p);
							
							if (linksTable.get(startNode) != null && linksTable.get(startNode).get(endNode) != null) {
								currInOutLinks = linksTable.get(startNode).get(endNode);
								inOutLinks[0] += currInOutLinks[0];
								inOutLinks[1] += currInOutLinks[1];
							}
							else if (linksTable.get(endNode) != null && linksTable.get(endNode).get(startNode) != null){
								currInOutLinks = linksTable.get(endNode).get(startNode);
								inOutLinks[0] += currInOutLinks[1];
								inOutLinks[1] += currInOutLinks[0];
							}
						}
						
						for (ElementRepresentation element : pack.getSubElements()) {
							startNode = sysrep.getElementNode(element);
							endNode = sysrep.getPackageNode(p);
							
							if (linksTable.get(startNode) != null && linksTable.get(startNode).get(endNode) != null) {
								currInOutLinks = linksTable.get(startNode).get(endNode);
								inOutLinks[0] += currInOutLinks[0];
								inOutLinks[1] += currInOutLinks[1];
							}
						}
						
						if (inOutLinks[0] > 0 || inOutLinks[1] > 0) {
							startNode = sysrep.getPackageNode(pack);
							endNode = sysrep.getPackageNode(p);
							
							if (linksTable.get(startNode) == null) {
								linksTable.put(startNode, new HashMap<NodeRepresentation, Integer[]>());
							}
							
							linksTable.get(startNode).put(endNode, inOutLinks);
						}
					}
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	//Fonction utilitaire pour créer le HashMap contenant tous les éléments de la hiérarchie, classés selon leur nom.
	
	public static HashMap<String, ElementRepresentation> getAllElementsRepresentation(SystemRepresentation sysrep) {
		HashMap<String, ElementRepresentation> allElementsRepresentation = new HashMap<String, ElementRepresentation>();
		
		int currLevel = 0;
		//int maxLevel = sysrep.findMaxLevel();
		int maxLevel = sysrep.getPackages().iterator().next().computeMaxLevel();
		
		for (PackageRepresentation p : sysrep.getPackages()) {
			allElementsRepresentation.putAll(getAllPackageElementsRepresentation(p, currLevel, maxLevel));
		}
		
		return allElementsRepresentation;
	}

	private static HashMap<String, ElementRepresentation> getAllPackageElementsRepresentation(PackageRepresentation pack, int currLevel, int maxLevel) {
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
	//******************************************************************************************************************

	
	
	
}

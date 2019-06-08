package verso.representation.cubeLandscape.Layout;

import java.awt.Color;
import java.util.HashMap;

import verso.graphics.primitives.Primitive;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.TreemapPackageRepresentation;
import verso.representation.cubeLandscape.representationModel.link.NodeRepresentation;

public class TreemapLinkAndNodeLayout extends LinkAndNodeLayout {
	//private SystemRepresentation sysrep;
	//private double levelHeight;
	//private double packageNodeSize;
	//private double elementNodeSize;
	
	/*
	public TreemapNodeLayout(SystemRepresentation sysrep) {
		this.sysrep = sysrep;
	}
	*/
	
	
	//Fonctions pour créer les noeuds du système. Le premier élément de la liste retournée contient les noeuds et des packages
	//et le second élément contient ceux des éléments (classes et interfaces).
	public HashMap<PackageRepresentation, NodeRepresentation> createPackagesNodes(SystemRepresentation sysrep, float levelHeight, Primitive packageNodeMesh, float packageNodeSize, Color packageNodeColor) {
		HashMap<PackageRepresentation, NodeRepresentation> packagesNodes = new HashMap<PackageRepresentation, NodeRepresentation>();
		//int maxLevel = sysrep.findMaxLevel();
		int maxLevel = sysrep.getPackages().iterator().next().computeMaxLevel();
		
		//double minY = sysrep.findMinY();
		float minY = sysrep.findMinY() + levelHeight;
		
		for (PackageRepresentation pack: sysrep.getPackages()) {
			packagesNodes.putAll(createPackagesNodes(pack, 0, maxLevel, minY, levelHeight, packageNodeMesh, packageNodeSize, packageNodeColor, null));
		}
		
		return packagesNodes;
	}
	
	public HashMap<PackageRepresentation, NodeRepresentation> createPackagesNodes(PackageRepresentation currPack, int currLevel, int maxLevel, float minY, float levelHeight, Primitive packageNodeMesh, float packageNodeSize, Color packageNodeColor, NodeRepresentation parentNode) {
		TreemapPackageRepresentation treemapCurrPack = (TreemapPackageRepresentation) currPack;
		HashMap<PackageRepresentation, NodeRepresentation> packagesNodes = new HashMap<PackageRepresentation, NodeRepresentation>();
		NodeRepresentation currPackNode;

		if (!treemapCurrPack.isFakePackage() || treemapCurrPack.getElements().size() > 0) {
			float posX = currPack.getAbsolutePosX() + treemapCurrPack.getSizeX() / 2;
			float posZ = currPack.getAbsolutePosZ() + treemapCurrPack.getSizeZ() / 2;

			float posY = minY + Math.abs(currLevel - maxLevel) * levelHeight;

			currPackNode = new NodeRepresentation(currPack, packageNodeMesh, packageNodeSize, posX, posY, posZ,
					packageNodeColor, parentNode);
			packagesNodes.put(treemapCurrPack, currPackNode);
		} else {
			currPackNode = parentNode;
		}

		int nextLevel;
		for (PackageRepresentation pack : treemapCurrPack.getTreemapPackages()) {
			if (!pack.isFakePackage()) {
				nextLevel = currLevel + 1;
			} else {
				nextLevel = currLevel;
			}

			packagesNodes.putAll(createPackagesNodes(pack, nextLevel, maxLevel, minY, levelHeight, packageNodeMesh,
					packageNodeSize, packageNodeColor, currPackNode));
		}

		return packagesNodes;
	}
	
	
	
	public HashMap<ElementRepresentation, NodeRepresentation> createElementsNodes(SystemRepresentation sysrep, float levelHeight, Primitive elementNodeMesh, float elementNodeSize, Color elementNodeColor) {
		HashMap<ElementRepresentation, NodeRepresentation> elementsNodes = new HashMap<ElementRepresentation, NodeRepresentation>();
		//int maxLevel = sysrep.findMaxLevel();
		int maxLevel = sysrep.getPackages().iterator().next().computeMaxLevel();
		
		//double minY = sysrep.findMinY();
		float minY = sysrep.findMinY();
		
		levelHeight = sysrep.stepHeight;
		
		for (PackageRepresentation pack: sysrep.getPackages()) {
			elementsNodes.putAll(createElementsNodes(sysrep, pack, 0, maxLevel, minY, levelHeight, elementNodeMesh, elementNodeSize, elementNodeColor));
		}
	
		return elementsNodes;
	}

	public HashMap<ElementRepresentation, NodeRepresentation> createElementsNodes(SystemRepresentation sysrep, PackageRepresentation currPack, int currLevel, int maxLevel, float minY, float levelHeight, Primitive elementNodeMesh, float elementNodeSize, Color elementNodeColor) {
		HashMap<ElementRepresentation, NodeRepresentation> elementsNodes = new HashMap<ElementRepresentation, NodeRepresentation>();
		// System.out.println("TreemapLinkAndNodeLayout.createElementsNodes("+currPack+")");

		for (PackageRepresentation pack : currPack.getPackages()) {
			elementsNodes.putAll(createElementsNodes(sysrep, pack, currLevel + 1, maxLevel, minY, levelHeight, elementNodeMesh, elementNodeSize,
					elementNodeColor));
		}

		for (ElementRepresentation element : currPack.getElements()) {
			elementsNodes.put(element, createElementNode(sysrep, currPack, element, currLevel + 1, maxLevel, minY, levelHeight, elementNodeMesh,
					elementNodeSize, elementNodeColor, sysrep.getPackageNode(currPack)));
		}

		return elementsNodes;
	}
	
	//Il faut centrer les noeuds au centre des classes. Où trouver la valeur de la largeur de la classe ???
	private NodeRepresentation createElementNode(SystemRepresentation sysrep, PackageRepresentation parentPack, ElementRepresentation currElement, int currLevel, int maxLevel, float minY, float levelHeight, Primitive elementNodeMesh, float elementNodeSize, Color elementNodeColor, NodeRepresentation parentNode) {
		float posX =  (parentPack.getAbsolutePosX() + currElement.getPosX() + (float)ElementRepresentation.Width);
		float posZ = parentPack.getAbsolutePosZ() + currElement.getPosZ() + (float)ElementRepresentation.Length;		
		float posY = (sysrep.findPackageAncestors(currElement).size() * sysrep.stepHeight) + currElement.getHeight(); 
		
		if (elementNodeColor == null) {
			elementNodeColor = currElement.getColor();
		}

		return new NodeRepresentation(currElement, elementNodeMesh, elementNodeSize, posX, posY, posZ, elementNodeColor, parentNode);
	}

	//**********************************************************************************
	
}

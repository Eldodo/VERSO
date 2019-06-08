package verso.representation.cubeLandscape.Layout.radial;

import java.awt.Color;
import java.util.HashMap;

import verso.util.MathGeometry;
import verso.graphics.primitives.Primitive;
import verso.representation.cubeLandscape.Layout.LinkAndNodeLayout;
import verso.representation.cubeLandscape.representationModel.ColiseumPackageRepresentation;
import verso.representation.cubeLandscape.representationModel.ElementRepresentation;
import verso.representation.cubeLandscape.representationModel.PackageRepresentation;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.link.NodeRepresentation;

public class ColiseumLinkAndNodeLayout extends LinkAndNodeLayout {
	//Fonctions pour créer les noeuds du système. Le premier élément de la liste retournée contient les noeuds et des packages
	//et le second élément contient ceux des éléments (classes et interfaces).
	public HashMap<PackageRepresentation, NodeRepresentation> createPackagesNodes(SystemRepresentation sysrep, float levelHeight, Primitive packageNodeMesh, float packageNodeSize, Color packageNodeColor) {
		HashMap<PackageRepresentation, NodeRepresentation> packagesNodes = new HashMap<PackageRepresentation, NodeRepresentation>();
		//int maxLevel = sysrep.findMaxLevel();
		int maxLevel = sysrep.getPackages().iterator().next().computeMaxLevel();

		float minY = sysrep.findColiseumMinY() + levelHeight;
		//double radius = ((RadialPackageRepresentation)sysrep.getPackages().iterator().next()).getLayoutInternRadius();

		for (PackageRepresentation pack: sysrep.getPackages()) {
			if (!pack.isFakePackage()) {
				packagesNodes.putAll(createPackagesNodes((ColiseumPackageRepresentation)pack, 0, maxLevel, minY, levelHeight, packageNodeMesh, packageNodeSize, packageNodeColor, null));
			}
		}
		
		return packagesNodes;
	}
	
	@Override
	public HashMap<PackageRepresentation, NodeRepresentation> createPackagesNodes(PackageRepresentation currPack, int currLevel, int maxLevel, float minY, float levelHeight, Primitive packageNodeMesh, float packageNodeSize, Color packageNodeColor, NodeRepresentation parentNode) {	
		ColiseumPackageRepresentation coliseumCurrPack = (ColiseumPackageRepresentation) currPack;
		HashMap<PackageRepresentation, NodeRepresentation> packagesNodes = new HashMap<PackageRepresentation, NodeRepresentation>();
		
		//**********************************
		//À modifier quand je ferai 2 types de systèmes : TreemapSystemRepresentation et RadialSystemRepresentation (changer tous les
		//PackageRepresentation pour RadialPackageRepresentation)
		//**********************************
		
		
		float radius;
		float packMedianAngle = coliseumCurrPack.getAngleFirstSide() + (coliseumCurrPack.getAngleSecondSide() - coliseumCurrPack.getAngleFirstSide()) / 2;
		float posX, posY, posZ;
		

		//************************
		//À REVÉRIFIER
		//************************
		
		//radius = coliseumCurrPack.getInternRadius();
		radius = coliseumCurrPack.getLayoutInternRadius();
		
		posX = ((radius / maxLevel) * currLevel) * (float)Math.cos(Math.toRadians(packMedianAngle));
		posZ = ((radius / maxLevel) * currLevel) * (float)Math.sin(Math.toRadians(packMedianAngle));		
		
		posY = minY + Math.abs(currLevel - maxLevel)*levelHeight;		
		
		NodeRepresentation currPackNode = new NodeRepresentation(currPack, packageNodeMesh, packageNodeSize, posX, posY, posZ, packageNodeColor, parentNode);
		packagesNodes.put(coliseumCurrPack, currPackNode);
		
		
		ColiseumPackageRepresentation elementsPack = ((ColiseumPackageRepresentation)currPack).getElementsPackage();
		
		
		if (elementsPack != null) {
			float elementPackRadius = elementsPack.getInternRadius() + ((elementsPack.getExternRadius() - elementsPack.getInternRadius()) / 2.0f);
			float elementPackMedianAngle = elementsPack.getAngleFirstSide() + (elementsPack.getAngleSecondSide() - elementsPack.getAngleFirstSide()) / 2;
			float elementPackPosX = elementPackRadius * (float)Math.cos(Math.toRadians(elementPackMedianAngle));
			float elementPackPosZ = elementPackRadius * (float)Math.sin(Math.toRadians(elementPackMedianAngle));
			float elementPackPosY = minY + (Math.abs(currLevel - maxLevel))*levelHeight;
			
			NodeRepresentation elementPackNode = new NodeRepresentation(elementsPack, packageNodeMesh, packageNodeSize, elementPackPosX, elementPackPosY, elementPackPosZ, packageNodeColor, currPackNode);
			packagesNodes.put(elementsPack, elementPackNode);
		}
		
			
		
		int nextLevel;
		for (ColiseumPackageRepresentation pack: coliseumCurrPack.getColiseumPackages()) {
			if (pack.isFakePackage()) {
				nextLevel = currLevel;
			}
			else {
				nextLevel = currLevel + 1;
			}
			
			packagesNodes.putAll(createPackagesNodes(pack, nextLevel, maxLevel, minY, levelHeight, packageNodeMesh, packageNodeSize, packageNodeColor, currPackNode));
		}

		/*
		if (coliseumCurrPack.getElementsPackage() != null && coliseumCurrPack.getElementsPackage().getElements().size() > 0) {
			packagesNodes.putAll(createPackagesNodes(coliseumCurrPack.getElementsPackage(), currLevel, maxLevel, minY, levelHeight, packageNodeMesh, packageNodeSize, packageNodeColor, currPackNode));
		}
		*/
		
		return packagesNodes;
	}
	
	public HashMap<ElementRepresentation, NodeRepresentation> createElementsNodes(SystemRepresentation sysrep, float levelHeight, Primitive elementNodeMesh, float elementNodeSize, Color elementNodeColor) {
		HashMap<ElementRepresentation, NodeRepresentation> elementsNodes = new HashMap<ElementRepresentation, NodeRepresentation>();
		//int maxLevel = sysrep.findMaxLevel();
		int maxLevel = sysrep.getPackages().iterator().next().computeMaxLevel();
		float minY = sysrep.findMinY();
		
		for (PackageRepresentation pack: sysrep.getPackages()) {
			elementsNodes.putAll(createElementsNodes(sysrep, (ColiseumPackageRepresentation)pack, 0, maxLevel, minY, levelHeight, elementNodeMesh, elementNodeSize, elementNodeColor));
		}
	
		return elementsNodes;
	}
	
	public HashMap<ElementRepresentation, NodeRepresentation> createElementsNodes(SystemRepresentation sysrep, ColiseumPackageRepresentation currPack, int currLevel, int maxLevel, float minY, float levelHeight, Primitive elementNodeMesh, float elementNodeSize, Color elementNodeColor) {	
		HashMap<ElementRepresentation, NodeRepresentation> elementsNodes = new HashMap<ElementRepresentation, NodeRepresentation>();	

		int nextCurrLevel;
		for (ColiseumPackageRepresentation subPack: currPack.getColiseumPackages()) {
			if (subPack.isFakePackage()) {
				nextCurrLevel = currLevel;
			}
			else {
				nextCurrLevel = currLevel + 1;
			}
			
			elementsNodes.putAll(createElementsNodes(sysrep, subPack, nextCurrLevel, maxLevel, minY, currPack.getHeight(), elementNodeMesh, elementNodeSize, elementNodeColor));
		}

		ColiseumPackageRepresentation elementsPack = currPack.getElementsPackage();
		
		if (elementsPack != null) {
			for (ElementRepresentation element : elementsPack.getElements()) {			
				elementsNodes.put(element, createElementNode(elementsPack, element, currLevel, maxLevel, minY, currPack.getHeight(), elementNodeMesh, elementNodeSize, elementNodeColor, sysrep.getPackageNode(elementsPack)));	
			}
		}
		
		return elementsNodes;
	}
	
	//Il faut centrer les noeuds au centre des classes. Où trouver la valeur de la largeur de la classe ???
	private NodeRepresentation createElementNode(ColiseumPackageRepresentation parentPack, ElementRepresentation currElement, int currLevel, int maxLevel, float minY, float parentPackHeight, Primitive elementNodeMesh, float elementNodeSize, Color elementNodeColor, NodeRepresentation parentNode) {
		/*
		float posX = parentPack.getAbsolutePosX() + currElement.getPosX() + 0.5f;
		float posZ = parentPack.getAbsolutePosZ() + currElement.getPosZ() + 0.5f;		
		*/
		
		float centerAngle = MathGeometry.arcAngle(currElement.getPosX() + 0.5f, 0.5f);
		
		float posX = (currElement.getPosX() + 0.5f) * (float)Math.cos(Math.toRadians(parentPack.elementsAngles.get(currElement) + centerAngle));
		float posZ = (currElement.getPosX() + 0.5f) * (float)Math.sin(Math.toRadians(parentPack.elementsAngles.get(currElement) + centerAngle));
		
		
		float posY = currElement.getHeight() + parentPackHeight;
		
		if (elementNodeColor == null) {
			elementNodeColor = currElement.getColor();
		}
		
		return new NodeRepresentation(currElement,elementNodeMesh, elementNodeSize, posX, posY, posZ, elementNodeColor, parentNode);
	}
	
	//**********************************************************************************
	
	
}

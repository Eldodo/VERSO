package verso.representation.cubeLandscape.representationModel;

import java.awt.Color;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.media.opengl.GL;

import verso.graphics.primitives.PrimitiveColored;
import verso.model.SystemDef;
import verso.representation.IPickable;
import verso.representation.Renderable;
import verso.representation.cubeLandscape.representationModel.link.EdgeBundleLinkRepresentation;
import verso.representation.cubeLandscape.representationModel.link.LinkKey;
import verso.representation.cubeLandscape.representationModel.link.LinkRepresentation;
import verso.representation.cubeLandscape.representationModel.link.NodeRepresentation;
import verso.representation.cubeLandscape.representationModel.radial.RadialPackageRepresentation;

import com.sun.opengl.util.BufferUtil;

public class SystemRepresentation implements Renderable{	
	protected static int displayListCurrInd = 5;
	
	protected static int getNextFreeDisplayListInd() {
		return displayListCurrInd++;
	}
	
	public void buildDisplayListsIndexes() {
		//this.packageDisplayListInd = getNextFreeDisplayListInd();
		
		//int nbreLinksDisplayLists = (this.linksTable.size() / this.nbreLinksPerDisplayList) + 1;
		//this.linksDisplayListIndexes = new int[nbreLinksDisplayLists];
		
		/*
		for (int i = 0; i < nbreLinksDisplayLists; i++) {
			this.linksDisplayListIndexes[i] = getNextFreeDisplayListInd();
		}
		*/
		
		setDirty();
	}
	
	
	public int nbreLinksPerDisplayList = 1000;
	
	public int packageDisplayListInd = 0;
	protected int[] linksDisplayListIndexes;
	
	
	protected boolean dirtyPackages = true;
	protected boolean dirtyLinks = true;
	
	public void setDirty() {
		this.dirtyPackages = true;
		this.dirtyLinks = true;
	}
	
	
	
	
	
	
	
	
	
	protected PrimitiveColored coloredLinkMesh;
	protected Color linkStartColor, linkEndColor, linkBidirectionalColor;
	protected int nbreSides;
	protected boolean straightenControlPoints;
	protected float beta;
	protected int degree;
	protected int nbreSegments;
	protected boolean removeLCA;
	
	
	
	public int getNbreDisplayedLinks() {
		return this.linksDisplayed.size();
	}
	
	
	public int getNbreLinks() {
		return this.links.size();
	}
	
	public void setEdgeBundleProperties(PrimitiveColored coloredLinkMesh, Color linkStartColor, Color linkEndColor, Color linkBidirectionalColor, int nbreSides, boolean straightenControlPoints, float beta, int degree, int nbreSegments, boolean removeLCA) {
		this.coloredLinkMesh = coloredLinkMesh;
		this.linkStartColor = linkStartColor;
		this.linkEndColor = linkEndColor;
		this.linkBidirectionalColor = linkBidirectionalColor;
		this.nbreSides = nbreSides;
		this.straightenControlPoints = straightenControlPoints;
		this.beta = beta;
		this.degree = degree;
		this.nbreSegments = nbreSegments;
		this.removeLCA = removeLCA;
	}
	
	protected void createLinks() {
		long debutNano = System.nanoTime();
		
		this.links.clear();
		
		Integer[] currInOutLinks;
		float linkWidth;
		float linkLineWidth;
		
		NodeRepresentation nodeOverElement;
		NodeRepresentation nodeOnPackage;
		
		LinkRepresentation currLink;
		LinkKey currKey;
		
		
		for (NodeRepresentation startNode : this.nodesDisplayed) {
			if (this.linksTable.get(startNode) != null) {
				for (NodeRepresentation endNode : this.linksTable.get(startNode).keySet()) {
					if (this.nodesDisplayed.contains(endNode)) {
						currInOutLinks = this.linksTable.get(startNode).get(endNode);
						
						float percentageMaxSize = (float)(currInOutLinks[0] + currInOutLinks[1]) / (float)this.maxNbreLinks;
						if (percentageMaxSize > 1.0) {
							percentageMaxSize = 1.0f;
						}
					
						linkWidth = this.computeLinkWidth(currInOutLinks[0] + currInOutLinks[1]);
						linkLineWidth = this.linkMinLineSize + (percentageMaxSize * this.linkMaxLineSize);

						nodeOverElement = this.getNodeOverElement(startNode);
						if (nodeOverElement != null) {
							startNode = nodeOverElement;
						}
						else {
							nodeOnPackage = this.getNodeOnPackage(startNode);
							if (nodeOnPackage != null) {
								startNode = nodeOnPackage;
							}
						}
						
						
						nodeOverElement = this.getNodeOverElement(endNode);
						if (nodeOverElement != null) {
							endNode = nodeOverElement;
						}
						else {
							nodeOnPackage = this.getNodeOnPackage(endNode);
							if (nodeOnPackage != null) {
								endNode = nodeOnPackage;
							}
						}
						
						if (NodeRepresentation.getRealParentNode(startNode) == NodeRepresentation.getRealParentNode(endNode)) {							
							//currLink = new DirectLinkRepresentation(startNode, endNode, currInOutLinks[0], currInOutLinks[0], null, linkWidth, linkLineWidth, this.linkStartColor, this.linkEndColor, false, null);
							
							//currKey = new LinkKey(startNode, endNode);
							//links.put(currKey, currLink);
						}
						else {
							LinkedList<Integer> lca = new LinkedList<Integer>();
							LinkedList<NodeRepresentation> path = this.getNodesPath(startNode, endNode, lca);	
							
							currLink = new EdgeBundleLinkRepresentation(startNode, endNode, currInOutLinks[0], currInOutLinks[1], this.nbreSides, linkWidth, linkLineWidth, this.linkStartColor, this.linkEndColor, false, null, path, this.straightenControlPoints, this.beta, this.degree, this.nbreSegments, (int)lca.getFirst(), this.removeLCA);
							
							currKey = new LinkKey(startNode, endNode);
							links.put(currKey, currLink);
						}
					}
				}
			}
		}
		
		this.sortLinks();
		
		
		long finNano = System.nanoTime();
		
		System.out.println("\nNumber of links (end): " + getNbreLinks());
		System.out.print("Temps pour calculer les liens: " + ((finNano - debutNano) / 1000000.0));
	}
	
	protected LinkedList<NodeRepresentation> getNodesPath(NodeRepresentation startNode, NodeRepresentation endNode, LinkedList<Integer> lca) {
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

		
		
		/*
		for (NodeRepresentation unNoeud : startNodePath) {
			if (unNoeud == null) {
				System.out.println("");
			}
		}
		
		for (NodeRepresentation unNoeud : endNodePath) {
			if (unNoeud == null) {
				System.out.println("");
			}
		}
		*/
		
		
		startNodePath.add(startNode);
		startNodePath.add(startNode.getParentNode());
		endNodePath.addFirst(endNode);
		
		lca.add(startNodePath.size()-1);
		
		startNodePath.addAll(endNodePath);
		
		
		
		//À garder seulement si on ne merge pas les noeuds trop proche
		//return startNodePath;
		//***********************************
		
		

		
		
		LinkedList<NodeRepresentation> mergedPathNodes = mergePathNodes(startNodePath, 0.1);
		
		NodeRepresentation newStartNode = this.getNodeOnPackage(mergedPathNodes.getFirst());
		if (newStartNode != null) {	
			//Sert à enlever le "double" du noeud sur le package dans le path de noeud
			//mergedPathNodes.removeFirst();
			//***********************************

			mergedPathNodes.addFirst(newStartNode);
		}
		
		NodeRepresentation newEndNode = this.getNodeOnPackage(mergedPathNodes.getLast());
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
			
			if (currNode == null || nextNode == null) {
				System.out.println("Kessé!?");
			}
			
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public int test = 1;
	
	
	
	
	
	
	public void testRecalculEdgeBundles() {
		for (LinkRepresentation l : this.links.values()) {
			if (l instanceof EdgeBundleLinkRepresentation) {
				((EdgeBundleLinkRepresentation) l).recreateBSpline();
			}
		}
	}
	
	
	public boolean displayRoughEdgeBundles = false;
	public int minNbreSegments = 15;
	public int maxNbreSegments = 50;
	public int segmentsInterval = 5;
	
	public void useRoughEdgeBundles() {
		this.displayRoughEdgeBundles = true;
		
		this.links.clear();
		
		for (LinkKey key : this.roughEdgeBundles.keySet()) {
			this.links.put(key, this.roughEdgeBundles.get(key).copyLink());
		}
		
		this.sortLinks();
		
		
		LinkedList<LinkRepresentation> roughLinksDisplayed = new LinkedList<LinkRepresentation>();
		LinkKey currKey = new LinkKey(null, null);
		for (LinkRepresentation link : this.linksDisplayed) {
			currKey.setStartNode(link.getStartNode());
			currKey.setEndNode(link.getEndNode());
			roughLinksDisplayed.add(this.links.get(currKey));
		}
		
		this.linksDisplayed = roughLinksDisplayed;
	}
	
	
	
	
	
	public HashSet<EntityRepresentation> selectedElements = new HashSet<EntityRepresentation>();
	public HashSet<LinkRepresentation> selectedLinks = new HashSet<LinkRepresentation>();
	
	
	
	public static int NO_FILTER = 0;
	public static int FILTER_INTERPACKAGE = 1;
	
	public int linksFilterType = NO_FILTER;
	
	
	
	public static boolean filterState = false;
	protected SystemDef system;
	protected HashMap<String,PackageRepresentation> packages = new HashMap<String,PackageRepresentation>();
	
	//Ajout Simon
	
	
	public float linkMaxSize = 1.0f;
	public float linkMinSize = 0.1f;
	public int maxNbreLinks = 250;
	//public int minNbreLinks = 10;
	
	boolean linearProgression = false;

	
	
	
	
	public void changePackageSelectionColor(Color newColor) {
		for (PackageRepresentation pack : this.packages.values()) {
			changePackageSelectionColor(pack, newColor);
		}
	}
	
	protected void changePackageSelectionColor(PackageRepresentation pack, Color newColor) {
			pack.bordersColor = newColor;
			
			for (ElementRepresentation element : pack.getElements()) {
				element.bordersColor = newColor;
			}
		
			for (PackageRepresentation subPack : pack.getPackages()) {
				changePackageSelectionColor(subPack, newColor);
			}
	}
	
	
	
	
	
	public void updateMaxNbreLinks() {
		int currMaxNbreLinks = 0;
		
		for (LinkRepresentation link : links.values()) {
			if (currMaxNbreLinks < link.getNbreInLinks() + link.getNbreOutLinks()) {
				currMaxNbreLinks = link.getNbreInLinks() + link.getNbreOutLinks();
			}
		}
		
		this.maxNbreLinks = currMaxNbreLinks;
		this.updateLinksWidth();
	}
	
	
	public void updateLinksWidth() {
		for (LinkRepresentation link : links.values()) {			
			link.setMeshSize(computeLinkWidth(link.getNbreInLinks() + link.getNbreOutLinks()));
		}
		
		this.dirtyEdgeBundles = true;
	}
	
	public float computeLinkWidth(int nbreLinks) {
		if (nbreLinks >= this.maxNbreLinks) {
			return this.linkMaxSize;
		}
		
		float linkWidth;
		
		if (this.linearProgression) {
			linkWidth = ((float)nbreLinks / (float)maxNbreLinks) * this.linkMaxSize;
		}
		else {
			linkWidth = ((float)Math.log(nbreLinks) / (float)Math.log(maxNbreLinks)) * this.linkMaxSize;
		}
		
		if (linkWidth < this.linkMinSize) {
			linkWidth = linkMinSize;
		}
		
		return linkWidth;
	}
	
	
	
	
	
	
	public final float linkMaxLineSize = 10.0f;
	public final float linkMinLineSize = 0.0f;
	
	
	
	
	protected float levelHeight = 5.0f;
	
	
	protected HashMap<PackageRepresentation, NodeRepresentation> packagesNodes = new HashMap<PackageRepresentation, NodeRepresentation>();
	protected HashMap<NodeRepresentation, NodeRepresentation> nodesOnPackages = new HashMap<NodeRepresentation, NodeRepresentation>();
	
	protected HashMap<ElementRepresentation, NodeRepresentation> elementsNodes = new HashMap<ElementRepresentation, NodeRepresentation>();	
	protected HashMap<NodeRepresentation, NodeRepresentation> nodesOverElements = new HashMap<NodeRepresentation, NodeRepresentation>();
	
	
	//protected HashMap<LinkKey, DirectLinkRepresentation> directLinks = new HashMap<LinkKey, DirectLinkRepresentation>();
	//protected HashMap<LinkKey, EdgeBundleLinkRepresentation> edgeBundles = new HashMap<LinkKey, EdgeBundleLinkRepresentation>();
	
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> linksTable = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>>();
	
	protected HashMap<LinkKey, LinkRepresentation> links = new HashMap<LinkKey, LinkRepresentation>();
	
	
	protected HashMap<LinkKey, LinkRepresentation> roughEdgeBundles = new HashMap<LinkKey, LinkRepresentation>(); 
	
	
	
	
	/*
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, DirectLinkRepresentation>> sortedDirectLinks = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, DirectLinkRepresentation>>();
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, DirectLinkRepresentation>> reverseSortedDirectLinks = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, DirectLinkRepresentation>>();
	
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, EdgeBundleLinkRepresentation>> sortedEdgeBundles = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, EdgeBundleLinkRepresentation>>();
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, EdgeBundleLinkRepresentation>> reverseSortedEdgeBundles = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, EdgeBundleLinkRepresentation>>();
	*/
	
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> sortedLinks = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>>();
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> reverseSortedLinks = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>>();

	public void sortLinks() {
		HashMap<NodeRepresentation, LinkRepresentation> currSortedLinks;
		for (LinkKey key : this.links.keySet()) {
			currSortedLinks = sortedLinks.get(key.getStartNode());
			
			if (currSortedLinks == null) {
				currSortedLinks = new HashMap<NodeRepresentation, LinkRepresentation>();
				sortedLinks.put(key.getStartNode(), currSortedLinks);
			}
			
			currSortedLinks.put(key.getEndNode(), this.links.get(key));
			

			currSortedLinks = reverseSortedLinks.get(key.getEndNode());
			
			if (currSortedLinks == null) {
				currSortedLinks = new HashMap<NodeRepresentation, LinkRepresentation>();
				reverseSortedLinks.put(key.getEndNode(), currSortedLinks);
			}
			
			currSortedLinks.put(key.getStartNode(), this.links.get(key));
		}
	}
	

	
	
	protected HashMap<NodeRepresentation, NodeRepresentation> edgeBundlesNodes = new HashMap<NodeRepresentation, NodeRepresentation>();
	
	
	public boolean oldColor = true;
	public boolean oldRenderingPoints = true;
	public boolean displayIntraPackageLinks = false;
	public boolean animateLinks = false;
	
	
	
	
	
	
	public float stepHeight = 1.0f;
	public int paddingSize = 1;
	
	
	
	public float findPTMinY() {
		LinkedList<ElementRepresentation> systemElements = new LinkedList<ElementRepresentation>();
		float minY = Float.MIN_VALUE;
		float currMinY;
		
		for (PackageRepresentation pack : this.getPackages()) {
			systemElements.addAll(pack.getAllElements());
		}
		
		for (ElementRepresentation e : systemElements) {
			currMinY = (findPackageAncestors(e).size() * this.stepHeight) + e.getHeight();
			
			if (currMinY > minY) {
				minY = currMinY;
			}
		}
		
		return minY;
	}
	
	
	
	
	
	public LinkedList<PackageRepresentation> findPackageDescendants(PackageRepresentation pack) {
		LinkedList<PackageRepresentation> packageDescendants = new LinkedList<PackageRepresentation>();
		
		packageDescendants.addAll(pack.getSubPackages()) ;
		for (PackageRepresentation subPack : pack.getSubPackages()) {
			packageDescendants.addAll(findPackageDescendants(subPack));
		}
		
		return packageDescendants;
	}
	
	
	public LinkedList<PackageRepresentation> findPackageRenderedDescendants() {
		LinkedList<PackageRepresentation> packageRenderedDescendants = new LinkedList<PackageRepresentation>();
		
		for (PackageRepresentation pack : this.packages.values()) {
			packageRenderedDescendants.addAll(findPackageRenderedDescendants(pack));
		}
		
		return packageRenderedDescendants;
	}
	
	public LinkedList<PackageRepresentation> findPackageRenderedDescendants(PackageRepresentation pack) {
		LinkedList<PackageRepresentation> packageRenderedDescendants = new LinkedList<PackageRepresentation>();
		
		if (pack.isRendered()) {
			packageRenderedDescendants.add(pack);
		}
		else {
			for (PackageRepresentation subPack : pack.getSubPackages()) {
				packageRenderedDescendants.addAll(findPackageRenderedDescendants(subPack));
			}
		}
		
		return packageRenderedDescendants;
	}
	
	
	
	
	
	public LinkedList<ElementRepresentation> findPackageDescendantElements(PackageRepresentation pack) {
		LinkedList<ElementRepresentation> descendantElements = new LinkedList<ElementRepresentation>();
		
		descendantElements.addAll(pack.getElements());
		for (PackageRepresentation subPack : pack.getPackages()) {
			descendantElements.addAll(findPackageDescendantElements(subPack));
		}
		
		return descendantElements;
	}
	
	
	
	
	public LinkedList<ElementRepresentation> findPackageRenderedDescendantElements() {
		LinkedList<ElementRepresentation> descendantElements = new LinkedList<ElementRepresentation>();

		for (PackageRepresentation pack : this.packages.values()) {
			descendantElements.addAll(findPackageRenderedDescendantElements(pack));
		}

		return descendantElements;
	}
	
	public LinkedList<ElementRepresentation> findPackageRenderedDescendantElements(PackageRepresentation pack) {
		LinkedList<ElementRepresentation> descendantElements = new LinkedList<ElementRepresentation>();

		if (!pack.isRendered()) {
			descendantElements.addAll(pack.getElements());
			
			for (PackageRepresentation subPack : pack.getPackages()) {
				descendantElements.addAll(findPackageRenderedDescendantElements(subPack));
			}
		}

		return descendantElements;
	}
	
	
	
	
	
	public LinkedList<PackageRepresentation> findPackageAncestors(ElementRepresentation element) {
		LinkedList<PackageRepresentation> elementAncestors;
		PackageRepresentation elementParentPack = this.findParentPackage(element);
		
		elementAncestors = findPackageAncestors(elementParentPack);
		
		if (!elementParentPack.isFakePackage())
		{
			elementAncestors.addLast(elementParentPack);
		}
		
		return elementAncestors;
	}
	
	public LinkedList<PackageRepresentation> findPackageAncestors(PackageRepresentation pack) {
		LinkedList<PackageRepresentation> packageAncestors;
		
		for (PackageRepresentation p : this.packages.values()) {
			packageAncestors = findPackageAncestors(p, pack);
			
			if (packageAncestors != null) {
				return packageAncestors;
			}
		}
		
		return null;
	}
	
	private LinkedList<PackageRepresentation> findPackageAncestors(PackageRepresentation currPack, PackageRepresentation pack) {
		LinkedList<PackageRepresentation> packageAncestors;
		
		if (currPack.getPackages().size() == 0) {
			return null;
		}
		else if (currPack.getPackages().contains(pack)) {
			packageAncestors = new LinkedList<PackageRepresentation>();
			
			if (!currPack.isFakePackage()) {
				packageAncestors.add(currPack);
			}
			
			return packageAncestors;
		}
		else {		
			for (PackageRepresentation subPack : currPack.getPackages()) {
				packageAncestors = findPackageAncestors(subPack, pack);
				
				if (packageAncestors != null) {
					if (!currPack.isFakePackage()) {
						packageAncestors.addFirst(currPack);
					}
					
					return packageAncestors;
				}
			}
		}
		
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	public int nbreLinks() {
		return this.links.keySet().size();
	}
	
	public void setEdgeBundlesOldColor() {
		for (LinkRepresentation link : this.links.values()) {
			if (link instanceof EdgeBundleLinkRepresentation) {
				((EdgeBundleLinkRepresentation)link).setOldColor(this.oldColor);
			}
		}
	}
	
	public void setEdgeBundlesOldRenderingPoints() {
		for (LinkRepresentation link : this.links.values()) {
			if (link instanceof EdgeBundleLinkRepresentation) {
				((EdgeBundleLinkRepresentation)link).setOldRenderingPoints(this.oldRenderingPoints);
				((EdgeBundleLinkRepresentation)link).recreateBSpline();
			}
		}
	}
	
	
	public NodeRepresentation getEdgeBundlesNode(NodeRepresentation originalNode) {
		return this.edgeBundlesNodes.get(originalNode);
	}
	
	
	public void createEdgeBundlesNodes() {
		edgeBundlesNodes.clear();

		NodeRepresentation newNode;
		NodeRepresentation originalNode;
		NodeRepresentation parentNode;
		
		for (PackageRepresentation pack : this.packages.values()) {
			int maxLevel = pack.computeMaxLevel();
			float levelWidth = ((RadialPackageRepresentation)pack).getLayoutInternRadius() / maxLevel;	
			
			createEdgeBundlesNodes(pack, levelWidth, null);
			
			for (ElementRepresentation element : pack.getAllElements()) {
				originalNode = this.elementsNodes.get(element);
				parentNode = this.edgeBundlesNodes.get(originalNode.getParentNode());
				
				newNode = new NodeRepresentation(element, null, 0.25f, originalNode.getposXd(), originalNode.getposYd(), originalNode.getposZd(), Color.white, parentNode);
				this.edgeBundlesNodes.put(originalNode, newNode);
			}
		}
	}

	private void createEdgeBundlesNodes(PackageRepresentation pack, float levelWidth, NodeRepresentation parentNode) {	
		float packMedianAngle = ((RadialPackageRepresentation)pack).getAngleFirstSide() + (((RadialPackageRepresentation)pack).getAngleSecondSide() - ((RadialPackageRepresentation)pack).getAngleFirstSide()) / 2;
		
		float posX = (levelWidth * pack.getPacLevel()) * (float)Math.cos(Math.toRadians(packMedianAngle));
		float posZ = (levelWidth * pack.getPacLevel()) * (float)Math.sin(Math.toRadians(packMedianAngle));		
		float posY = this.packagesNodes.get(pack).getposYd();
		
		NodeRepresentation newNode = new NodeRepresentation(pack, null, 0.25f, posX, posY, posZ, Color.white, parentNode);
		this.edgeBundlesNodes.put(this.packagesNodes.get(pack), newNode);
		
		for (PackageRepresentation subPack : pack.getSubPackages()) {
			createEdgeBundlesNodes(subPack, levelWidth, newNode);
		}
	}
	
	
	/**************************************************
	protected HashMap<NodeRepresentation, NodeRepresentation> baseNodes = new HashMap<NodeRepresentation, NodeRepresentation>();
	protected HashMap<LinkKey, DirectLinkRepresentation> baseLinks = new HashMap<LinkKey, DirectLinkRepresentation>();
	
	public void updateLinksPos() {
		double currMaxHeight = this.getCurrMaxHeight() + 0.5;
		
		clearBaseNodes();
		this.baseLinks.clear();
		
		for (NodeRepresentation node : this.packagesNodes.values()) {
			if (node.getposYd() < currMaxHeight) {
				updateNodePos(node, currMaxHeight);
			}
		}
		
		for (NodeRepresentation node : this.elementsNodes.values()) {
			if (node.getposYd() < currMaxHeight) {
				updateNodePos(node, currMaxHeight);
			}
		}
		
		
		for (EdgeBundleLinkRepresentation edgeBundle : this.edgeBundlesDisplayed) {
			edgeBundle.recreateBSpline();
		}
	}
	
	
	protected void clearBaseNodes() {
		for (NodeRepresentation node : baseNodes.keySet()) {
			node.setposYd(baseNodes.get(node).getposYd());
		}
		
		baseNodes.clear();
	}
	
	
	protected void updateNodePos(NodeRepresentation node, double nodeNewHeight) {
		NodeRepresentation newBaseNode = new NodeRepresentation(node.getEntityRepresentation(), null, 0.5, node.getposXd(), node.getposYd(), node.getposZd(), Color.yellow, null);
		this.baseNodes.put(node, newBaseNode);
		node.setposYd(nodeNewHeight);
		
		LinkKey newLinkKey = new LinkKey(newBaseNode, node);
		
		
		int totalNbreInLinks = 0;
		int totalNbreOutLinks = 0;
		
		for (DirectLinkRepresentation link : this.directLinksDisplayed) {
			if (link.getStartNode() == node) {
				totalNbreInLinks += link.getNbreInLinks();
				totalNbreOutLinks += link.getNbreOutLinks();
			}
			else if (link.getEndNode() == node) {
				totalNbreInLinks += link.getNbreOutLinks();
				totalNbreOutLinks += link.getNbreInLinks();
			}
		}
		
		if (totalNbreInLinks > 0 || totalNbreOutLinks > 0) {
			System.out.println("In links : " + totalNbreInLinks);
			System.out.println("Out links : " + totalNbreOutLinks);
		}

		double linkWidth = this.linkMinSize + (Math.log(totalNbreInLinks + totalNbreOutLinks) / (double)this.maxNbreLinks) * this.linkMaxSize;
		double linkLineWidth = this.linkMinLineSize + (Math.log(totalNbreInLinks + totalNbreOutLinks) / (double)this.maxNbreLinks) * this.linkMaxLineSize;
		
		if (linkWidth > this.linkMaxSize) {
			linkWidth = this.linkMaxSize;
		}
		
		if (linkLineWidth > this.linkMaxLineSize) {
			linkLineWidth = this.linkMaxLineSize;
		} 
		
		DirectLinkRepresentation newDirectLink = new DirectLinkRepresentation(newBaseNode, node, totalNbreInLinks, totalNbreOutLinks, new CubeNoCapColored(null, null), linkWidth, linkLineWidth, Color.green, Color.red, false, Color.magenta);		
		
		newDirectLink.setName("Test lien");
		
		this.baseLinks.put(newLinkKey, newDirectLink);
	}
	
	public double getCurrMaxHeight() {
		double currMaxHeight = 0.0;
		double tempMaxHeight = 0.0;
		
		for (PackageRepresentation p : this.getPackages()) {
			tempMaxHeight = getCurrMaxHeight(p);
			
			if (tempMaxHeight > currMaxHeight) {
				currMaxHeight = tempMaxHeight;
			}
		}
		
		return currMaxHeight;
	}
	
	private double getCurrMaxHeight(PackageRepresentation pack) {	
		if (pack.isRendered()) {
			return pack.getHeight();
		}
		
		double currMaxHeight = 0.0;
		double tempMaxHeight = 0.0;
		
		for (PackageRepresentation p : pack.getSubPackages()) {
			tempMaxHeight = getCurrMaxHeight(p);
			
			if (tempMaxHeight > currMaxHeight) {
				currMaxHeight = tempMaxHeight;
			}
		}
		
		return currMaxHeight;
	}
	**************************************************/
	
	
	public void expandPackage(PackageRepresentation pack) {	
		pack.setRender(false);
		
		for (PackageRepresentation subPack: pack.getPackages()) {
			if (!subPack.isFakePackage()) {
				subPack.setRender(true);
			}
			else {
				expandPackage(subPack);
			}
		}
	}
	
	public void closePackage(ElementRepresentation element) {
		PackageRepresentation parentPack = findParentPackage(element);
		
		if (parentPack != null && parentPack.isFakePackage()) {
			closePackage(parentPack);
		}
		else {
			if (parentPack != null) {
				parentPack.setRender(true);
				closeSubPackages(parentPack);
			}
		}
	}
	
	public void closePackage(PackageRepresentation pack) {
		PackageRepresentation parentPack;
		
		if (pack.isRendered() || pack.isFakePackage()) {
			parentPack = findParentPackage(pack);
		}
		else {
			parentPack = pack;
		}
		
		
		
		
		if (parentPack != null && parentPack.isFakePackage()) {
			closePackage(parentPack);
		}
		else {
			if (parentPack == null) {
				parentPack = pack;
			}
			
			parentPack.setRender(true);
			closeSubPackages(parentPack);
		}	
	}
	
	protected void closeSubPackages(PackageRepresentation pack) {
		for (PackageRepresentation subPack : pack.getPackages()) {
			subPack.setRender(false);
			closeSubPackages(subPack);
		}
	}
	
	public void setLevelHeight(float levelHeight) {
		if (levelHeight > 0.0f) {
			this.levelHeight = levelHeight;
		}
	}
	
	public float getLevelHeight() {
		return this.levelHeight;
	}
	
	public PackageRepresentation findParentPackage(EntityRepresentation entity) {
		PackageRepresentation parentPackage;
		
		for (PackageRepresentation p : this.getPackages()) {
			parentPackage = findParentPackage(p, p, entity);
			
			if (parentPackage != null) {
				return parentPackage;
			}
		}
		
		return null;
	}
	
	protected PackageRepresentation findParentPackage(PackageRepresentation root, PackageRepresentation currParent, EntityRepresentation entity) {
		PackageRepresentation parentPackage;
		
		
		/*
		if (entity instanceof ElementRepresentation) {
			if (root.getElements().contains(entity)) {
				return currParent;
			}
			else {
				for (PackageRepresentation p : root.getPackages()) {
					if (!p.isFakePackage()) {
						parentPackage = findParentPackage(p, p, entity);
					}
					else {
						parentPackage = findParentPackage(p, root, entity);
					}
					
					if (parentPackage != null) {
						return parentPackage;
					}
				}
			}
			
			return null;
		}
		else if (entity instanceof PackageRepresentation) {
			if (root.getPackages().contains(entity)) {
				return currParent;
			}
			else {
				for (PackageRepresentation p : root.getPackages()) {
					if (!p.isFakePackage()) {
						parentPackage = findParentPackage(p, p, entity);
					}
					else {
						parentPackage = findParentPackage(p, root, entity);
					}
					
					if (parentPackage != null) {
						return parentPackage;
					}
				}
				
				return null;
			}
		}
		*/
		
		
		if ((entity instanceof PackageRepresentation && root.getPackages().contains(entity)) || (entity instanceof ElementRepresentation && root.getElements().contains(entity))) {
			return currParent;
		}
		else {
			for (PackageRepresentation p : root.getPackages()) {
				if (!p.isFakePackage()) {
					parentPackage = findParentPackage(p, p, entity);
				}
				else {
					parentPackage = findParentPackage(p, currParent, entity);
				}
				
				if (parentPackage != null) {
					return parentPackage;
				}
			}
		}

		return null;
	}
	
	protected HashSet<NodeRepresentation> nodesDisplayed = new HashSet<NodeRepresentation>();
	
	/*
	protected LinkedList<DirectLinkRepresentation> directLinksDisplayed = new LinkedList<DirectLinkRepresentation>();
	protected LinkedList<EdgeBundleLinkRepresentation> edgeBundlesDisplayed = new LinkedList<EdgeBundleLinkRepresentation>();
	*/
	
	protected LinkedList<LinkRepresentation> linksDisplayed = new LinkedList<LinkRepresentation>();
	
	
	public void updateNodesDisplayed(Collection<NodeRepresentation> nodesDisplayed) {
		this.nodesDisplayed.clear();
		this.nodesDisplayed.addAll(nodesDisplayed);
	}
	
	public void updateNodesDisplayed() {
		System.out.println("Displayed nodes size: " + nodesDisplayed.size());
		
		nodesDisplayed.clear();
		for (PackageRepresentation pack : this.getPackages()) {
			updateNodesDisplayed(nodesDisplayed, pack);
		}
		
		System.out.println("Displayed nodes size: " + nodesDisplayed.size());
		
		this.createLinks();
	}
	
	protected void updateNodesDisplayed(HashSet<NodeRepresentation> nodesList, PackageRepresentation pack) {
		if (pack.render) {
			nodesList.add(this.getNodeOnPackage(this.getPackageNode(pack)));
		}
		else {
			for (PackageRepresentation p : pack.getPackages()) {
				updateNodesDisplayed(nodesList, p);
			}
			
			for (ElementRepresentation element : pack.getElements()) {
				nodesList.add(this.getElementNode(element));
			}
		}
	}
	
	
	/*
	public void updateDirectLinksDisplayed() {
		LinkKey currKey;
		Iterator<NodeRepresentation> startNodeItr = nodesDisplayed.iterator();
		Iterator<NodeRepresentation> endNodeItr;
		NodeRepresentation currStartNode, currEndNode;
		
		directLinksDisplayed.clear();
		
		while (startNodeItr.hasNext()) {
			currStartNode = startNodeItr.next();
			endNodeItr = nodesDisplayed.iterator();
			while (endNodeItr.hasNext()) {
				currEndNode = endNodeItr.next();
				currKey = new LinkKey(currStartNode, currEndNode);
 				if (directLinks.containsKey(currKey)) {
					directLinksDisplayed.add(directLinks.get(currKey));
				}
			}
		}
	}
	*/
	
	public void updateLinksDisplayed(Collection<LinkRepresentation> linksDisplayed) {
		this.linksDisplayed.clear();
		this.linksDisplayed.addAll(linksDisplayed);
	}
	
	public void updateLinksDisplayed() {
		Iterator<NodeRepresentation> startNodeItr = nodesDisplayed.iterator();
		Iterator<NodeRepresentation> endNodeItr;
		NodeRepresentation currStartNode, currEndNode;
		
		this.linksDisplayed.clear();
		
		while (startNodeItr.hasNext()) {
			currStartNode = startNodeItr.next();
			
			if (this.sortedLinks.get(currStartNode) != null) {
				endNodeItr = this.sortedLinks.get(currStartNode).keySet().iterator();
				
				while (endNodeItr.hasNext()) {
					currEndNode = endNodeItr.next();
					
					if (this.nodesDisplayed.contains(currEndNode)) {
						boolean nodesAreSiblings;
						
						if (NodeRepresentation.getRealParentNode(currStartNode) == NodeRepresentation.getRealParentNode(currEndNode)) {
							nodesAreSiblings = true;
						}
						else {
							nodesAreSiblings = false;
						}
						
						if (!(this.displayIntraPackageLinks ^ nodesAreSiblings)) {
							this.linksDisplayed.add(this.sortedLinks.get(currStartNode).get(currEndNode));
						}
					}
				}
			}
		}
	}
	
	public void updateLinkColor(LinkRepresentation aLink) {
		Color linkStartColor;
		Color linkEndColor;
		int nbreTotalLinks = aLink.getNbreInLinks() + aLink.getNbreOutLinks();
		double inLinksPercentage = (double)aLink.getNbreInLinks() / nbreTotalLinks;
		double outLinksPercentage = (double)aLink.getNbreOutLinks() / nbreTotalLinks;
		
		double[] linkStartColors = new double[3];
		linkStartColors[0] = inLinksPercentage * (aLink.getLinkEndColor().getRed()/255) + outLinksPercentage * (aLink.getLinkStartColor().getRed()/255);
		linkStartColors[1] = inLinksPercentage * (aLink.getLinkEndColor().getGreen()/255) + outLinksPercentage * (aLink.getLinkStartColor().getGreen()/255);
		linkStartColors[2] = inLinksPercentage * (aLink.getLinkEndColor().getBlue()/255) + outLinksPercentage * (aLink.getLinkStartColor().getBlue()/255);
		
		
		double[] linkEndColors = new double[3];
		linkEndColors[0] = inLinksPercentage * (aLink.getLinkStartColor().getRed()/255) + outLinksPercentage * (aLink.getLinkEndColor().getRed()/255);
		linkEndColors[1] = inLinksPercentage * (aLink.getLinkStartColor().getGreen()/255) + outLinksPercentage * (aLink.getLinkEndColor().getGreen()/255);
		linkEndColors[2] = inLinksPercentage * (aLink.getLinkStartColor().getBlue()/255) + outLinksPercentage * (aLink.getLinkEndColor().getBlue()/255);
		
		for (int i=0; i<linkStartColors.length; i++) {
			if (linkStartColors[i] > 1.0) {
				linkStartColors[i] = 1.0;
			}
			
			if (linkEndColors[i] > 1.0) {
				linkEndColors[i] = 1.0;
			}
		}
		
		linkStartColor = new Color((float)linkStartColors[0], (float)linkStartColors[1], (float)linkStartColors[2]);
		linkEndColor = new Color((float)linkEndColors[0], (float)linkEndColors[1], (float)linkEndColors[2]);
		
		aLink.setLinkStartColor(linkStartColor);
		aLink.setLinkEndColor(linkEndColor);
	}

	public LinkedList<PackageRepresentation> getSystemPackages() {
		LinkedList<PackageRepresentation> systemPackages = new LinkedList<PackageRepresentation>();
		
		for (PackageRepresentation p : this.getPackages()) {
			this.getSystemPackages(p, systemPackages);
		}
		
		return systemPackages;
	}
	
	private void getSystemPackages(PackageRepresentation pack, LinkedList<PackageRepresentation> systemPackages) {
		systemPackages.add(pack);
		
		for (PackageRepresentation p : pack.getPackages()) {
			getSystemPackages(p, systemPackages);
		}
	}

	//**************** À terminer!!!! ****************************
	public void updatePackageHeight(float packagesHeight) {
	
		Iterator<PackageRepresentation> packageItr = this.packagesNodes.keySet().iterator();

		/*
		PackageRepresentation currPackage;
		double minY = this.findMinY();
		
		System.out.println("Nbre packages: " + this.packages.values().size());
		*/
		
		while (packageItr.hasNext()) {
			packageItr.next().setHeight(packagesHeight);
			
			
			/*
			currPackage = packageItr.next();			
			
			updatePackageHeight(currPackage, 1, currPackage.computeMaxLevel(), minY);
			*/
			
			
			
			//currPackage.setHeight(packagesNodes.get(currPackage).getposYd());	
			//currPackage.setHeight(minY + findPackageDepth(currPackage)*levelHeight);
		}
	}
	//*************************************************************	
	
	protected void updatePackageHeight(PackageRepresentation pack, int currLevel, int maxLevel, float minY) {			
		pack.setHeight(minY + (-(currLevel - maxLevel) * this.levelHeight));
		currLevel++;
		
		Iterator<PackageRepresentation> packageItr = pack.getSubPackages().iterator();
		while (packageItr.hasNext()) {
			updatePackageHeight(packageItr.next(), currLevel, maxLevel, minY);
		}
	}

	
	
	
	public void updateColiseumPackageHeight(float levelWidth) {
		for (PackageRepresentation pack : this.packages.values()) {
			updateColiseumPackageHeight(pack, pack.computeMaxLevel(), 0, levelWidth);
		}
	}
	
	protected void updateColiseumPackageHeight(PackageRepresentation pack, int maxLevel, int currLevel, float levelWidth) {
		pack.setHeight((Math.abs(currLevel - maxLevel) + 1) * levelWidth);
		
		int nextLevel;
		for (PackageRepresentation subPack : pack.getPackages()) {
			if (subPack.isFakePackage()) {
				nextLevel = currLevel;
			}
			else {
				nextLevel = currLevel+1;
			}
			updateColiseumPackageHeight(subPack, maxLevel, nextLevel, levelWidth);
		}
	}
	
	
	
	public int findPackageDepth(PackageRepresentation pack) {
		if (pack.getPackages().size() == 0) {
			return 1;
		}
		else {
			int maxDepth = 0;
			int currDepth = 0;
			
			for (PackageRepresentation p : pack.getPackages()) {
				currDepth = findPackageDepth(p)+1;
				if (currDepth > maxDepth) {
					maxDepth = currDepth;
				}
			}
			
			return maxDepth;
		}
	}
	
	public void filterAll() {
		for (LinkRepresentation edgeBundle : links.values()) {
			edgeBundle.setFiltered();
		}
		
		for (NodeRepresentation n : this.elementsNodes.values()) {
			n.setFiltered();
			n.getEntityRepresentation().setFiltered();
		}
		for (NodeRepresentation n : this.packagesNodes.values()) {
			n.setFiltered();
			n.getEntityRepresentation().setFiltered();
		}
		for (NodeRepresentation n : this.nodesOnPackages.values()) {
			n.setFiltered();
		}
	}
	
	public void filterByElements(Collection<EntityRepresentation> startElements) {		
		filterAll();
		
		HashMap<Integer, NodeRepresentation> startNodes = new HashMap<Integer, NodeRepresentation>();
		HashSet<NodeRepresentation> unfilteredNodes = new HashSet<NodeRepresentation>();
		NodeRepresentation node;
		
		HashMap<NodeRepresentation, LinkRepresentation> currLinks;
		
		for (IPickable startElement : startElements) {	
			if (startElement instanceof ElementRepresentation) {
				node = elementsNodes.get(startElement);
				startNodes.put(node.hashCode(), node);
			}
			else if (startElement instanceof PackageRepresentation) {
				node = packagesNodes.get(startElement);
				startNodes.put(node.hashCode(), node);
			}
			else {
				node = null;
			}
			
			
			if (node != null) {
				node.getEntityRepresentation().setUnFiltered();
				
				
				//À déplacer dans SceneLandscape ??? (car il y une différence entre isFILTERED et isSELECTED)
				node.getEntityRepresentation().select();
				
				
				unfilteredNodes.add(node);
				
				currLinks = this.sortedLinks.get(node);
				if (currLinks != null) {
					for (LinkRepresentation currLink : currLinks.values()) {
						if (this.linksFilterType == FILTER_INTERPACKAGE) {
							if (startElements.contains(currLink.getEndNode().getEntityRepresentation())) {
								currLink.setUnFiltered();
								unfilteredNodes.add(currLink.getEndNode());
							}
						}
						else {
							currLink.setUnFiltered();
							unfilteredNodes.add(currLink.getEndNode());
						}
					}
				}
				
				if (this.linksFilterType != FILTER_INTERPACKAGE) {
					currLinks = this.reverseSortedLinks.get(node);
					if (currLinks != null) {
						for (LinkRepresentation currLink : currLinks.values()) {
							currLink.setUnFiltered();
							unfilteredNodes.add(currLink.getStartNode());
						}
					}
				}
			}
		}

		for (NodeRepresentation n : unfilteredNodes) {
			n.setUnFiltered();
			n.getEntityRepresentation().setUnFiltered();
			
			if (this.nodesOnPackages.get(n) != null) {
				this.nodesOnPackages.get(n).setUnFiltered();
			}
		}
	}
	
	public void filterByLinks(Collection<LinkRepresentation> linksToFilter) {
		filterAll();

		for (LinkRepresentation link : linksToFilter) {
			link.setUnFiltered();
			
			link.getStartNode().setUnFiltered();
			link.getEndNode().setUnFiltered();
			
			link.getStartNode().getEntityRepresentation().setUnFiltered();
			link.getEndNode().getEntityRepresentation().setUnFiltered();
		}
	}
		
	public void unfilterAll() {
		for (LinkRepresentation link : links.values()) {
			link.setUnFiltered();
		}

		for (NodeRepresentation n : this.elementsNodes.values()) {
			n.setUnFiltered();
			n.getEntityRepresentation().setUnFiltered();
		}
		for (NodeRepresentation n : this.packagesNodes.values()) {
			n.setUnFiltered();
			n.getEntityRepresentation().setUnFiltered();
		}
		
		for (NodeRepresentation n : this.nodesOnPackages.values()) {
			n.setUnFiltered();
		}
	}

	public void unfilterAllLinks() {
		for (LinkRepresentation link : links.values()) {
			link.setUnFiltered();
		}
	}
	
	
	protected int elementsLevelType = 0;
	
	public int getElementsLevelType() {
		return this.elementsLevelType;
	}
	
	public void setElementsLevelType(int levelType) {
		this.elementsLevelType = levelType;
	}
	
	public void changeLevelHeight(float levelHeight) {
		this.levelHeight = levelHeight;
		
		
		//int maxLevel = findMaxLevel();
		int maxLevel = this.getPackages().iterator().next().computeMaxLevel();
		float minY = this.findPTMinY(); //findMinY();

		changeLevelHeight(packagesNodes.values().iterator(), levelHeight, maxLevel, minY);
		
		/*
		if (this.elementsLevelType == 0) {
			changeLevelHeight(elementsNodes.values().iterator(), levelHeight, maxLevel, minY);
		}
		*/
		
		this.dirtyEdgeBundles = true;
	}
	
	protected void changeLevelHeight(Iterator<NodeRepresentation> nodesItr, float levelHeight, int maxLevel, float minY) {
		int currNodeLevel;
		NodeRepresentation currNode;
		NodeRepresentation currNodeParent;

		while (nodesItr.hasNext()) {
			currNode = nodesItr.next();
			currNodeLevel = 1;
			currNodeParent = currNode.getParentNode();
			
			while (currNodeParent != null) {
				currNodeParent = currNodeParent.getParentNode();
				currNodeLevel++;
			}
			
			currNode.setposYd(minY + Math.abs(currNodeLevel - maxLevel)*levelHeight + levelHeight);
		}
	}
	
	public void forceFirstLevel() {
		if (this.elementsLevelType == 1) {
			float minY = findMinY();
			
			Iterator<NodeRepresentation> elementsNodesItr = elementsNodes.values().iterator();
			while (elementsNodesItr.hasNext()) {
				elementsNodesItr.next().setposYd(minY);
			}
			
			this.dirtyEdgeBundles = true;
		}
	}
	
	public void elementsNodesOnClass() {
		if (this.elementsLevelType == 2) {
			Iterator<ElementRepresentation> elementsRepresentationItr = elementsNodes.keySet().iterator();
			ElementRepresentation currElementRepresentation;
			NodeRepresentation currNode;
			while (elementsRepresentationItr.hasNext()) {
				currElementRepresentation = elementsRepresentationItr.next();
				currNode = elementsNodes.get(currElementRepresentation);
				currNode.setposYd(currElementRepresentation.getHeight());
			}
			
			this.dirtyEdgeBundles = true;
		}
	}
	
	
	
	//Variables dirty
	protected boolean dirtyEdgeBundles = false;
	
	public boolean getDirtyEdgeBundles() {
		return this.dirtyEdgeBundles;
	}
	
	public void setDirtyEdgeBundles(boolean dirtyEdgeBundles) {
		this.dirtyEdgeBundles = dirtyEdgeBundles;
	}
	//*******************************************
	
	protected boolean displayLinks = true;
	
	public boolean displayLinks() {
		return displayLinks;
	}

	public void setDisplayLinks(boolean displayLinks) {
		this.displayLinks = displayLinks;
	}

	public SystemRepresentation(SystemDef system)
	{
		this.system = system;
		
		
	}
	
	public SystemDef getSystemDef()
	{
		return this.system;
	}
	
	public void addPackage(PackageRepresentation p)
	{
		this.packages.put(p.getPackage().getName(), p);
	}
	
	public Collection<PackageRepresentation> getPackages()
	{
		return this.packages.values();
	}
	
	//Ajout Simon
	//**********************************************************************************************************************************
	
	//Ajouter une fonction pour vider la liste de packages du système.
	public void clearPackages() {
		this.packages.clear();
	}
	
	
	//Fonctions pour trouver la taille (en y) de la plus grande boîtes (classe).
	public float findMinY() {
		float currMinY = Float.MIN_VALUE;
		
		for (PackageRepresentation p : packages.values()) {
			float newMinY = findMinY(p, currMinY);
			if (newMinY > currMinY) {
				currMinY = newMinY; 
			}
		}
		
		return currMinY;
	}
	
	protected float findMinY(PackageRepresentation currPack, float currMinY) {
		for (PackageRepresentation p : currPack.getPackages()) {
			float newMinY = findMinY(p, currMinY);
			if (newMinY > currMinY) {
				currMinY = newMinY; 
			}
		}
		
		for (ElementRepresentation e : currPack.getElements()) {
			if (e.getHeight() > currMinY) {
				currMinY = e.getHeight();
			}
		}
		
		return currMinY;
	}

	//Fonctions pour gérer la liste de noeuds représentant les packages
	public void clearPackagesNodes() {
		this.packagesNodes.clear();
	}
	
	public void addPackagesNodes(HashMap<PackageRepresentation, NodeRepresentation> packagesNodes) {
		this.packagesNodes.putAll(packagesNodes);
	}
	
	public NodeRepresentation getPackageNode(PackageRepresentation nodePackage) {
		return packagesNodes.get(nodePackage);
	}
	
	public Collection<NodeRepresentation> getPackagesNodes() {
		return packagesNodes.values();
	}
	
	public Collection<PackageRepresentation> getNodesPackages() {
		return packagesNodes.keySet();
	}
	//****************************************
	
	//Fonctions pour gérer la liste de noeuds représentant les packages (qui sont SUR les packages)
	public void clearNodesOnPackages() {
		this.nodesOnPackages.clear();
	}
	
	public void updateNodesOnPackages() {
		NodeRepresentation packageNode;
		NodeRepresentation newNodeOnPackage;
		float nodePosXd, nodePosZd, nodePosYd;
		float epsilon = 0.5f;
		
		this.nodesOnPackages.clear();
		for (PackageRepresentation pack : this.packagesNodes.keySet()) {
			packageNode = this.packagesNodes.get(pack);
			newNodeOnPackage = packageNode.copyNode();
			
			if (pack instanceof RadialPackageRepresentation && !(pack instanceof ColiseumPackageRepresentation)) {
				RadialPackageRepresentation radialPack = (RadialPackageRepresentation)pack;
				float radius = ((RadialPackageRepresentation)this.packages.values().iterator().next()).getLayoutInternRadius();
				float packMedianAngle = radialPack.getAngleFirstSide() + (radialPack.getAngleSecondSide() - radialPack.getAngleFirstSide()) / 2;
				
				nodePosXd = radius * (float)Math.cos(Math.toRadians(packMedianAngle));
				nodePosZd = radius * (float)Math.sin(Math.toRadians(packMedianAngle));
				nodePosYd = pack.getHeight() + epsilon;
				
				newNodeOnPackage.setposXd(nodePosXd);
				newNodeOnPackage.setposZd(nodePosZd);
				newNodeOnPackage.setposYd(nodePosYd);
			}
			else if (pack instanceof ColiseumPackageRepresentation) {
				if (pack instanceof ColiseumPackageRepresentation) {
					ColiseumPackageRepresentation coliseumPack = (ColiseumPackageRepresentation)pack;
					float radius = ((ColiseumPackageRepresentation)pack).getInternRadius();
					float packMedianAngle = coliseumPack.getAngleFirstSide() + (coliseumPack.getAngleSecondSide() - coliseumPack.getAngleFirstSide()) / 2;
					
					nodePosXd = radius * (float)Math.cos(Math.toRadians(packMedianAngle));
					nodePosZd = radius * (float)Math.sin(Math.toRadians(packMedianAngle));
					nodePosYd = pack.getHeight() + epsilon;
					
					newNodeOnPackage.setposXd(nodePosXd);
					newNodeOnPackage.setposZd(nodePosZd);
					newNodeOnPackage.setposYd(nodePosYd);
				}
			}
			else {
				if (this.findPackageAncestors(pack) == null) {
					nodePosYd = pack.getHeight() + epsilon;
				}
				else {
					nodePosYd = (this.findPackageAncestors(pack).size() * this.stepHeight) + pack.getHeight() + epsilon;
				}
			
				newNodeOnPackage.setposYd(nodePosYd);
			}
			
			this.nodesOnPackages.put(packageNode, newNodeOnPackage);
		}
	}
	
	public NodeRepresentation getNodeOnPackage(NodeRepresentation packageNode) {
		return this.nodesOnPackages.get(packageNode);
	}
	//****************************************
	
	
	//Fonctions pour gérer la liste de noeuds au-dessus des classes (utilisées par les liens entre siblings)
	public void clearNodesOverElements() {
		this.nodesOverElements.clear();
	}
	
	public void updateNodesOverElements() {
		for (PackageRepresentation pack : this.packages.values()) {
			updateNodesOverElements(pack);
		}
	}
	
	private void updateNodesOverElements(PackageRepresentation pack) {
		NodeRepresentation currElementNode;
		NodeRepresentation newNodeOverElement;
		float maxHeight = 0.0f;
		float heightDifference;
		
		float epsilon = 0.5f;
		
		for (PackageRepresentation subPack : pack.getPackages()) {
			updateNodesOverElements(subPack);
		}
		
		if (pack.getElements().size() > 0) {
			for (ElementRepresentation element : pack.getElements()) {
				if (element.getHeight() > maxHeight) {
					maxHeight = element.getHeight();
				}
			}
			
			maxHeight += epsilon;
			
			for (ElementRepresentation element : pack.getElements()) {
				currElementNode = this.elementsNodes.get(element);
				heightDifference = maxHeight - element.getHeight();
				newNodeOverElement = new NodeRepresentation(element, currElementNode.getMesh(), currElementNode.getMeshSize(), currElementNode.getposXd(), currElementNode.getposYd() + heightDifference, currElementNode.getposZd(), currElementNode.getColor(), currElementNode.getParentNode());
				this.nodesOverElements.put(currElementNode, newNodeOverElement);
			}
		}	
	}
	
	public NodeRepresentation getNodeOverElement(NodeRepresentation elementNode) {
		return this.nodesOverElements.get(elementNode);
	}
	//****************************************
	
	
	
	
	//Fonctions pour gérer la liste de noeuds représentant les éléments
	public void clearElementsNodes() {
		this.elementsNodes.clear();
	}
	
	public void addElementsNodes(HashMap<ElementRepresentation, NodeRepresentation> elementsNodes) {
		this.elementsNodes.putAll(elementsNodes);
	}
	
	public NodeRepresentation getElementNode(ElementRepresentation nodeElement) {
		return elementsNodes.get(nodeElement);
	}
	
	public Collection<NodeRepresentation> getElementsNodes() {
		return elementsNodes.values();
	}
	
	public Collection<ElementRepresentation> getNodesElements() {
		return elementsNodes.keySet();
	}

	
	
	
	
	public void clearLinksTable() {
		linksTable.clear();
		this.buildDisplayListsIndexes();
	}
	
	public void addLinksTable(HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> linksTable) {
		this.linksTable.putAll(linksTable);
		this.buildDisplayListsIndexes();
	}
	
	public HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> getLinksTable() {
		return this.linksTable;
	}
	
	
	
	
	
	//Fonctions pour gérer la liste de liens
	public void clearLinks() {
		links.clear();
	}
	
	public void addLink(LinkRepresentation newLink) {
		LinkKey linkKey = new LinkKey(newLink.getStartNode(), newLink.getEndNode());
		
		this.links.put(linkKey, newLink);
	}
	
	public void addLinks(HashMap<LinkKey, LinkRepresentation> newLinks) {
		this.links.putAll(newLinks);
	}
	
	public Collection<LinkRepresentation> getLinks() {
		return links.values();
	}
	
	
	public LinkRepresentation getLink(LinkKey key) {
		return this.links.get(key);
	}
	
	public HashMap<NodeRepresentation, LinkRepresentation> findAllLinkStartingWith(NodeRepresentation startNode) {
		return this.sortedLinks.get(startNode);
	}
	
	public HashMap<NodeRepresentation, LinkRepresentation> findAllLinkEndingWith(NodeRepresentation endNode) {
		return this.reverseSortedLinks.get(endNode);
	}
	
	
	
	public void addRoughEdgeBundles(HashMap<LinkKey, LinkRepresentation> edgeBundles) {
		this.roughEdgeBundles.putAll(edgeBundles);
	}
	

	
	
	
	
	public int getNbreDisplayedPolygons() {
		int nbreDisplayedPolygons = 0;		
		int nbreElements = 0;
		
		nbreElements += this.packages.values().iterator().next().countDescendantPackages();
		nbreElements += this.packages.values().iterator().next().countDescendantClasses();
		
		//nbreDisplayedPolygons += nbreElements * 4;
		nbreDisplayedPolygons += (this.linksDisplayed.size() / this.test) * 30 * 4;
		//nbreDisplayedPolygons += (this.linksDisplayed.size() * (3.0 / 4.0)) * 30 * 4;
		
		return nbreDisplayedPolygons;
	}
	
	
	
	public void render(GL gl)
	{
		//System.out.println("RENDU DU SYSTÈME");

		gl.glPushMatrix();
			/*
			PackageRepresentation rootPackage = this.packages.values().iterator().next();
			if (rootPackage instanceof RadialPackageRepresentation) {
				if (((RadialPackageRepresentation)rootPackage).getInternRadius() > 20.0) {
					double scaleRatio = 20.0 / ((RadialPackageRepresentation)rootPackage).getInternRadius();
					gl.glScaled(scaleRatio, scaleRatio, scaleRatio);
				}
			}
			*/
			
			if (this.dirtyPackages) {
				gl.glDeleteLists(this.packageDisplayListInd, 1);
				gl.glNewList(this.packageDisplayListInd, GL.GL_COMPILE);
				
					gl.glPushMatrix();
						for (PackageRepresentation p : packages.values())
						{
							if (p instanceof RadialPackageRepresentation || p instanceof ColiseumPackageRepresentation) {
								for (PackageRepresentation subPack : p.getPackages()) {
									subPack.render(gl);
								}
							}
							else {
								p.render(gl);
							}
						}
					gl.glPopMatrix();
				
				gl.glEndList();
				
				gl.glCallList(this.packageDisplayListInd);
				
				this.dirtyPackages = false;
			}
			else {
				gl.glCallList(this.packageDisplayListInd);
			}
			
			
			
			
			//gl.glPushMatrix();
				
				/*
				for (NodeRepresentation n : this.packagesNodes.values()) {
					
					//if (n.getEntityRepresentation().getName().contains("_elementsPackage")) {
						n.render(gl);
					//}
					
				}
				*/
			
				
				/*
				for (NodeRepresentation n : this.nodesDisplayed) {
					if (n != null) {
						//n.render(gl);
					}
				}
				*/
	
			
			
			//gl.glPopMatrix();
			
			
			
			
			/*
			if (this.displayLinks) {
				if (this.dirtyLinks && this.linksDisplayListIndexes != null && this.linksDisplayListIndexes.length > 0) {
					gl.glPushMatrix();

						int displayedLinksPerList = (int)Math.ceil((double)this.linksDisplayed.size() / (double)this.linksDisplayListIndexes.length);
					
						for (int i = 0; i < this.linksDisplayListIndexes.length; i++) {
							gl.glNewList(this.linksDisplayListIndexes[i], GL.GL_COMPILE);
	
								for (int j = i*displayedLinksPerList; j < (i+1)*displayedLinksPerList; j++) {	
									if (!(j >= linksDisplayed.size())) {
										linksDisplayed.get(j).render(gl);
									}
								}
							
							gl.glEndList();
						}
						
						
						for (int i = 0; i < this.linksDisplayListIndexes.length; i++) {
							gl.glCallList(this.linksDisplayListIndexes[i]);
						}
						
					gl.glPopMatrix();
					
					this.dirtyLinks = false;
				}
				else {
					gl.glPushMatrix();
					
						for (int i = 0; i < this.linksDisplayListIndexes.length; i++) {
							gl.glCallList(this.linksDisplayListIndexes[i]);
						}
					
					gl.glPopMatrix();
				}
			}
			*/
		gl.glPopMatrix();
		
		System.gc();
	}
	
	
	public void refreshLinks(GL gl) {
		for (LinkRepresentation link : this.links.values()) {
			link.render(gl);
		}
	}
	
	
	public void setCamDist(double camX, double camY, double camZ){}
}

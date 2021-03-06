package verso.representation.cubeLandscape.representationModel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.media.opengl.GL;

import verso.graphics.VersoScene;
import verso.graphics.primitives.PrimitiveColored;
import verso.model.SystemDef;
import verso.representation.IPickable;
import verso.representation.Renderable;
import verso.representation.cubeLandscape.representationModel.link.EdgeBundleLinkRepresentation;
import verso.representation.cubeLandscape.representationModel.link.LinkKey;
import verso.representation.cubeLandscape.representationModel.link.LinkRepresentation;
import verso.representation.cubeLandscape.representationModel.link.NodeRepresentation;
import verso.representation.cubeLandscape.representationModel.radial.RadialPackageRepresentation;
import verso.traces.TracesUtil;

public class SystemRepresentation implements Renderable{
	public boolean displayTracesHeatMap = false;
	
	private Map<String,Double> numberOfCalls;
	private javafx.scene.paint.Color firstColor = javafx.scene.paint.Color.web("#ffeda0");
	private javafx.scene.paint.Color secondColor = javafx.scene.paint.Color.web("#f03b20");
	
	public javafx.scene.paint.Color getFirstColor() {
		return firstColor;
	}

	public void setFirstColor(javafx.scene.paint.Color firstColor) {
		this.firstColor = firstColor;
	}

	public javafx.scene.paint.Color getSecondColor() {
		return secondColor;
	}

	public void setSecondColor(javafx.scene.paint.Color secondColor) {
		this.secondColor = secondColor;
	}
	public void setNumberOfCalls(Map<String,Double> map) {
		numberOfCalls = map;
	}
	
	public Map<String,Double> getNumberOfCalls() {
		return numberOfCalls ;
	}
	public enum LINK_TYPE {
		INVOCATION, PARENT, TRACES,NONE
	}
	public LINK_TYPE linksType = LINK_TYPE.INVOCATION;

	
	protected static int displayListCurrInd = 5;
	protected PackageRepresentation rootPackage=null;
	
	protected static int getNextFreeDisplayListInd() {
		return displayListCurrInd++;
	}
	
	public void buildDisplayListsIndexes() {
		this.packageDisplayListInd = getNextFreeDisplayListInd();
		
		int nbreLinksDisplayLists = (this.getSystemNbreLinks() / this.nbreLinksPerDisplayList) + 1;
		this.linksDisplayListIndexes = new int[nbreLinksDisplayLists];
		
		
		for (int i = 0; i < nbreLinksDisplayLists; i++) {
			this.linksDisplayListIndexes[i] = getNextFreeDisplayListInd();
		}
		
		
		setDirty();
	}
	
	public PackageRepresentation getRootPackage() {
		return rootPackage;
	}
	
	public int getSystemNbreLinks() {
		int nbreSystemLinks = 0;
		
		for (NodeRepresentation n : this.linksTable.keySet()) {
			nbreSystemLinks += this.linksTable.get(n).size();
		}
		
		return nbreSystemLinks;
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
	
	public Color parentLinkStartColor = Color.black;
	public Color parentLinkEndColor = Color.green;
	public Color parentLinkBidirectionalColor = linkBidirectionalColor;

	protected int nbreSides;
	protected boolean straightenControlPoints;
	protected int degree;
	protected int nbreSegments;
	protected boolean removeLCA;
	
	
	
	
	public int getNbreDisplayedLinks() {
		return this.linksDisplayed.size();
	}
	
	
	public int getNbreLinks() {
		return this.links.size();
	}
	
	public void setLinksProperties(PrimitiveColored coloredLinkMesh, Color linkStartColor, Color linkEndColor, Color linkBidirectionalColor, int nbreSides, boolean straightenControlPoints, int degree, int nbreSegments, boolean removeLCA) {
		this.coloredLinkMesh = coloredLinkMesh;
		this.linkStartColor = linkStartColor;
		this.linkEndColor = linkEndColor;
		this.linkBidirectionalColor = linkBidirectionalColor;
		this.nbreSides = nbreSides;
		this.straightenControlPoints = straightenControlPoints;
		this.degree = degree;
		this.nbreSegments = nbreSegments;
		this.removeLCA = removeLCA;
	}
	
	protected void createLinks() {
		long debutNano = System.nanoTime();
		
		HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> currLinksTable = null;
		HashMap<LinkKey, LinkRepresentation> currLinks = null;
		HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> currSortedLinks = null;
		HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> currReverseSortedLinks = null;
		
		if(tracesLinksTable!=null) {
			currLinksTable = this.tracesLinksTable;
			currLinks = this.tracesLinks;
			currSortedLinks = this.sortedTracesLinks;
			currReverseSortedLinks = this.reverseSortedTracesLinks;
			createLinks(currLinksTable, currLinks, currSortedLinks, currReverseSortedLinks, this.linkStartColor, this.linkEndColor, this.linkBidirectionalColor);
		
		}
		
		currLinksTable = this.linksTable;
		currLinks = this.links;
		currSortedLinks = this.sortedLinks;
		currReverseSortedLinks = this.reverseSortedLinks;
		createLinks(currLinksTable, currLinks, currSortedLinks, currReverseSortedLinks, this.linkStartColor, this.linkEndColor, this.linkBidirectionalColor);
		
		
		currLinksTable = this.parentLinksTable;
		currLinks = this.parentLinks;
		currSortedLinks = this.sortedParentLinks;
		currReverseSortedLinks = this.reverseSortedParentLinks;
		createLinks(currLinksTable, currLinks, currSortedLinks, currReverseSortedLinks, this.parentLinkStartColor, this.parentLinkEndColor, this.parentLinkBidirectionalColor);
		
		
		
		System.gc();
		
		long finNano = System.nanoTime();
		
		System.out.println("\nNumber of links (end): " + getNbreLinks());
		System.out.print("Temps pour calculer les liens: " + ((finNano - debutNano) / 1000000.0));
	}
	
	
	
	protected void createLinks(HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> currLinksTable, HashMap<LinkKey, LinkRepresentation> currLinks, HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> currSortedLinks, HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> currReverseSortedLinks, Color currLinksStartColor, Color currLinksEndColor, Color currLinksBidirectionalColor) {
		currLinks.clear();
		
		LinkRepresentation currLink;
		LinkKey currKey;
		
		for (NodeRepresentation startNode : this.nodesDisplayed) {
			if (currLinksTable.get(startNode) != null) {
				
				for (NodeRepresentation endNode : currLinksTable.get(startNode).keySet()) {
					if (this.nodesDisplayed.contains(endNode)) {
						Integer[]  currInOutLinks = currLinksTable.get(startNode).get(endNode);

						float percentageMaxSize = (float)(currInOutLinks[0] + currInOutLinks[1]) / (float)this.maxNbreLinks;
						if (percentageMaxSize > 1.0) {
							percentageMaxSize = 1.0f;
						}
					
						float linkWidth = this.computeLinkWidth(currInOutLinks[0] + currInOutLinks[1]);
						float linkLineWidth = this.linkMinLineSize + (percentageMaxSize * this.linkMaxLineSize);
						
						float linkSaturation = this.computeLinkSaturation(1);
						
						//System.out.println("LinkSaturation : " + linkSaturation);
						
						
						
						//***************************************************************************************
						//Marche pas parce qu'on analyse pas les bonnes nodes pour getRealParentNodes!!!!!!!!!!!!
						//
						//(FAIT AUSSI EN SORTE QU'ON A DES EDGE BUNDLES COMMME LIENS DIRECT)
						//***************************************************************************************
						
						
						
						//****************************************************************************************
						//R�GLER LE PROBL�ME DES LIENS EDGE BUNDLES QUI TOUCHENT PAS AUX CLASSES!!!! (ET PACKAGES,
						//MAIS �A C'EST S�REMENT JUSTE LE NOEUD DU PACKAGE QUI EST TROP HAUT).
						//MAIS POUR LES CLASSES, PREND TOUJOURS L'HOSTIE DE NOEUD nodeOverElement!!!!!!!!!!!
						//****************************************************************************************
						
						
							
						float outRatio = 0.0f;
						outRatio = (float)currInOutLinks[1] / (float)(currInOutLinks[0] + currInOutLinks[1]); 
						
						if (startNode.getEntityRepresentation() instanceof ElementRepresentation) {
//							System.out.println("SystemRepresentation.createLinks()");
//							System.out.println("Start node height: " + startNode.getposYd() + "   " + startNode.getParentNode().getposYd()+ "   " + startNode.getNodeLevel());
//							System.out.println("Est dans nodes displayed: " + this.nodesDisplayed.contains(startNode));
//							startNode.setposYd(startNode.getNodeLevel());
						}
						startNode.setposYd( startNode.getEntityRepresentation().getLevel()  + startNode.getEntityRepresentation().height);//TODO HERE WORKS !
						endNode.setposYd(   endNode.getEntityRepresentation().getLevel()  + endNode.getEntityRepresentation().height);
//						try {
//							System.out.println("SystemRepresentation.createLinks() : "+startNode.getEntityRepresentation().height);
//						} catch (Exception e) {
//							System.out.println(e.getMessage());
//						}
						
						LinkedList<Integer> lca = new LinkedList<Integer>();
						LinkedList<NodeRepresentation> controlPoints = this.getNodesPath(startNode, endNode, lca, outRatio);
						//System.out.println( startNode.getEntityRepresentation().getName()+" "+endNode.getEntityRepresentation().getName()+":");
						/*for(NodeRepresentation node : controlPoints) {
								System.out.println(node.getposXd()+","+node.getposYd()+","+node.getposZd());
						}*/
						
						if(currLinksTable == this.tracesLinksTable) {
							Color color = TracesUtil.getColorValueAt(firstColor, secondColor, currLinksTable.get(startNode).get(endNode)[2]/100.0);
							currLink = new EdgeBundleLinkRepresentation(startNode, endNode, currInOutLinks[0],
									currInOutLinks[1], this.nbreSides, linkWidth, linkLineWidth, color,
									color, false, null, linkSaturation, controlPoints, this.straightenControlPoints,
									this.degree, this.nbreSegments, (int) lca.getFirst() , this.removeLCA);
						}
						
						else {
							currLink = new EdgeBundleLinkRepresentation(startNode, endNode, currInOutLinks[0],
									currInOutLinks[1], this.nbreSides, linkWidth, linkLineWidth, currLinksStartColor,
									currLinksEndColor, false, null, linkSaturation, controlPoints, this.straightenControlPoints,
									this.degree, this.nbreSegments, (int) lca.getFirst() , this.removeLCA);
						}

							currKey = new LinkKey(startNode, endNode);
							currLinks.put(currKey, currLink);

						//}
					}
				}
			}
		}
		
		this.sortLinks(currLinks, currSortedLinks, currReverseSortedLinks);
	}
	
	
	
	
	protected LinkedList<NodeRepresentation> getNodesPath(NodeRepresentation startNode, NodeRepresentation endNode, LinkedList<Integer> lca, float outRatio) {
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
		
		
		
		
		
		//� garder seulement si on ne merge pas les noeuds trop proche
		//return startNodePath;
		//***********************************

		
		LinkedList<NodeRepresentation> mergedPathNodes = startNodePath;
		//LinkedList<NodeRepresentation> mergedPathNodes = mergePathNodes(startNodePath, 0.1);
		
		NodeRepresentation newStartNode = this.getNodeOnPackage(mergedPathNodes.getFirst());
		
		if (newStartNode != null) {				
			mergedPathNodes.addFirst(newStartNode);
			
			//Sert � enlever le "double" du noeud sur le package dans le path de noeud
			NodeRepresentation nodeOnPackage = mergedPathNodes.getFirst();
			NodeRepresentation realPackageNode = mergedPathNodes.get(1);	
			
			float[] newOutNodeCoord = new float[3];
					
			newOutNodeCoord[0] = nodeOnPackage.getposXd() + ((realPackageNode.getposXd() - nodeOnPackage.getposXd()) * outRatio);
			newOutNodeCoord[1] = nodeOnPackage.getposYd() + ((realPackageNode.getposYd() - nodeOnPackage.getposYd()) * outRatio);
			newOutNodeCoord[2] = nodeOnPackage.getposZd() + ((realPackageNode.getposZd() - nodeOnPackage.getposZd()) * outRatio);
			
			
			
			NodeRepresentation newOutNode = new NodeRepresentation(realPackageNode.getEntityRepresentation(), realPackageNode.getMesh(), realPackageNode.getMeshSize(), newOutNodeCoord[0], newOutNodeCoord[1], newOutNodeCoord[2], Color.cyan /*realPackageNode.getColor()*/, realPackageNode.getParentNode());
			
			mergedPathNodes.remove(1);
			mergedPathNodes.add(1, newOutNode);
			
			//***********************************

			lca.addFirst(lca.getFirst()+1);
			lca.removeLast();
			
		} else {
			if (mergedPathNodes.size() > 3) {
				NodeRepresentation pathStartNode = mergedPathNodes.getFirst();
				NodeRepresentation outNode = mergedPathNodes.get(1);
				
				float[] newOutNodeCoord = new float[3];
				
				newOutNodeCoord[0] = pathStartNode.getposXd() + ((outNode.getposXd() - pathStartNode.getposXd()) * outRatio);
				newOutNodeCoord[1] = pathStartNode.getposYd() + ((outNode.getposYd() - pathStartNode.getposYd()) * outRatio);
				newOutNodeCoord[2] = pathStartNode.getposZd() + ((outNode.getposZd() - pathStartNode.getposZd()) * outRatio);
				
				NodeRepresentation newOutNode = new NodeRepresentation(outNode.getEntityRepresentation(), outNode.getMesh(), outNode.getMeshSize(), newOutNodeCoord[0], newOutNodeCoord[1], newOutNodeCoord[2], outNode.getColor(), outNode.getParentNode());
				mergedPathNodes.remove(1);
				mergedPathNodes.add(1, newOutNode);
			}
		}

		
		NodeRepresentation newEndNode = this.getNodeOnPackage(mergedPathNodes.getLast());
		if (newEndNode != null) {
			//Sert � enlever le "double" du noeud sur le package dans le path de noeud
			//mergedPathNodes.removeLast();
			//***********************************
				
			mergedPathNodes.addLast(newEndNode);
			
			NodeRepresentation nodeOnPackage = mergedPathNodes.getLast();
			NodeRepresentation realPackageNode = mergedPathNodes.get(mergedPathNodes.size() - 2);
			
			float[] newOutNodeCoord = new float[3];
			
			
			newOutNodeCoord[0] = realPackageNode.getposXd();
			newOutNodeCoord[1] = nodeOnPackage.getposYd() + ((realPackageNode.getposYd() - nodeOnPackage.getposYd()) * (1.0f - outRatio))  ;
			newOutNodeCoord[2] = realPackageNode.getposZd();
				
			
		
			NodeRepresentation newOutNode = new NodeRepresentation(realPackageNode.getEntityRepresentation(), realPackageNode.getMesh(), realPackageNode.getMeshSize(), newOutNodeCoord[0], newOutNodeCoord[1], newOutNodeCoord[2], Color.cyan /*realPackageNode.getColor()*/, realPackageNode.getParentNode());
			
			mergedPathNodes.remove(mergedPathNodes.size() - 2);
			mergedPathNodes.add(mergedPathNodes.size() - 1, newOutNode);
			
			
		} else {
			if (mergedPathNodes.size() > 3) {
				NodeRepresentation pathEndNode = mergedPathNodes.getLast();
				NodeRepresentation outNode = mergedPathNodes.get(mergedPathNodes.size() - 2);
				
				float[] newOutNodeCoord = new float[3];
				
				newOutNodeCoord[0] = pathEndNode.getposXd() + ((outNode.getposXd() - pathEndNode.getposXd()) * (1.0f - outRatio));
				newOutNodeCoord[1] = pathEndNode.getposYd() + ((outNode.getposYd() - pathEndNode.getposYd()) * (1.0f - outRatio)) ;
				newOutNodeCoord[2] = pathEndNode.getposZd() + ((outNode.getposZd() - pathEndNode.getposZd()) * (1.0f - outRatio));
				
				NodeRepresentation newOutNode = new NodeRepresentation(outNode.getEntityRepresentation(), outNode.getMesh(), outNode.getMeshSize(), newOutNodeCoord[0], newOutNodeCoord[1], newOutNodeCoord[2], outNode.getColor(), outNode.getParentNode());
				mergedPathNodes.remove(mergedPathNodes.size() - 2);
				mergedPathNodes.add(mergedPathNodes.size() - 1, newOutNode);	
			}
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
			
			if (currNode != null && nextNode != null) {
				nodesVector[0] = currNode.getposXd() - nextNode.getposXd();
				nodesVector[1] = currNode.getposYd() - nextNode.getposYd();
				nodesVector[2] = currNode.getposZd() - nextNode.getposZd();
				nodesDistance = Math.sqrt(nodesVector[0]*nodesVector[0] + nodesVector[1]*nodesVector[1] + nodesVector[2]*nodesVector[2]);
				
				if (nodesDistance >= mergeTreshold) {
					mergedPath.add(nextNode);
					currNode = nextNode;
				}
			}
		}
		
		return mergedPath;
	}
	
	
	
	
		
	public boolean displayRoughEdgeBundles = false;
	public int minNbreSegments = 15;
	public int maxNbreSegments = 50;
	public int segmentsInterval = 5;
	
	
	
	public HashSet<EntityRepresentation> selectedElements = new HashSet<>();
	public HashSet<LinkRepresentation> selectedLinks = new HashSet<>();
	
	
	
	public LINKFILTER_TYPE linksFilterType = LINKFILTER_TYPE.NO_FILTER;
	
	public enum LINKFILTER_TYPE {
		NO_FILTER, FILTER_INTRASELECTION, FILTER_EXTRASELECTION;
	}
	
	
	public static boolean filterState = false;
	
	protected SystemDef system;
	protected HashMap<String,PackageRepresentation> packages = new HashMap<>();
	
	//Ajout Simon
	
	
	public float linkMaxSize = 3.0f;
	public float linkMinSize = 0.1f;
	public int   maxNbreLinks = 100;
	
	
	public float linkMaxSaturation = 1.0f;
	public float linkMinSaturation = 0.0f;
	public float maxNbreCalls = 1000;
	
	boolean linearProgression = true;

	
	
	
	
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
		
		NodeRepresentation maxStartNode = null;
		NodeRepresentation maxEndNode = null;
		Integer[] maxInsOuts = null;
		
		for (NodeRepresentation startNode : linksTable.keySet()) {
			for (NodeRepresentation endNode : this.linksTable.get(startNode).keySet()) {
				Integer[] insOuts = this.linksTable.get(startNode).get(endNode); 
					
				if (currMaxNbreLinks < (insOuts[0] + insOuts[1])) {
					currMaxNbreLinks = (insOuts[0] + insOuts[1]);
					
					maxStartNode = startNode;
					maxEndNode = endNode;
					maxInsOuts = insOuts;
				}
			}	
		}
		
		
		if(maxStartNode != null && maxEndNode != null) {
			System.out.println("StartNode: " + maxStartNode.getEntityRepresentation().getName());
			System.out.println("EndNode: " + maxEndNode.getEntityRepresentation().getName());
			System.out.println("Ins: " + maxInsOuts[0] + "    Outs: " + maxInsOuts[1]);
		}
		this.maxNbreLinks = currMaxNbreLinks;
		
		//this.updateLinksWidth();
	}
	
	
	public void updateLinksWidth() {
		for (LinkRepresentation link : linksDisplayed) {			
			link.setMeshSize(computeLinkWidth(link.getNbreInLinks() + link.getNbreOutLinks()));
		}
		
		this.dirtyEdgeBundles = true;
	}
	
	public float computeLinkWidth(int nbreLinks) {
		float linkWidth;

		if (this.linearProgression) {
			linkWidth = ((float) nbreLinks / (float) this.maxNbreLinks) * this.linkMaxSize;
		} else {
			linkWidth = ((float) Math.log(nbreLinks) / (float) Math.log(this.maxNbreLinks)) * this.linkMaxSize;
		}

		if (linkWidth < this.linkMinSize)
			linkWidth = this.linkMinSize;
		else if (linkWidth > this.linkMaxSize)
			linkWidth = this.linkMaxSize;

		return linkWidth;
	}

	public float computeLinkSaturation(int nbreCalls) {
		if (nbreCalls >= this.maxNbreCalls) {
			return this.linkMinSaturation;
		}

		float linkSaturation;

		if (this.linearProgression) {
			linkSaturation = this.linkMaxSaturation - ((float) nbreCalls / (float) this.maxNbreCalls);
		} else {
			linkSaturation = this.linkMaxSaturation - ((float) Math.log(nbreCalls) / (float) Math.log(this.maxNbreCalls));
		}

		if (linkSaturation < this.linkMinSaturation) {
			linkSaturation = this.linkMinSaturation;
		}

		return linkSaturation;
	}
	
	
	
	
	
	public final float linkMaxLineSize = 10.0f;
	public final float linkMinLineSize = 0.0f;
	
	
	
	
	protected float levelHeight = 5.0f;
	
	
	protected HashMap<PackageRepresentation, NodeRepresentation> packagesNodes = new HashMap<PackageRepresentation, NodeRepresentation>();
	protected HashMap<NodeRepresentation, NodeRepresentation> nodesOnPackages = new HashMap<NodeRepresentation, NodeRepresentation>();
	
	protected HashMap<ElementRepresentation, NodeRepresentation> elementsNodes = new HashMap<ElementRepresentation, NodeRepresentation>();	
	protected HashMap<NodeRepresentation, NodeRepresentation> nodesOverElements = new HashMap<NodeRepresentation, NodeRepresentation>();
	
	
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> linksTable = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>>();
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> parentLinksTable = new HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>>();
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> tracesLinksTable = null;
	
	public HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> getTracesLinksTable() {
		return tracesLinksTable;
	}

	public void setTracesLinksTable(HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> tracesLinksTable) {
		this.tracesLinksTable = tracesLinksTable;
		if(tracesLinksTable!=null) {
			updateNodesDisplayed();
			updateLinksDisplayed();
		}
	}




	protected HashMap<LinkKey, LinkRepresentation> links = new HashMap< >();
	protected HashMap<LinkKey, LinkRepresentation> parentLinks = new HashMap< >();
	protected HashMap<LinkKey, LinkRepresentation> tracesLinks = new HashMap< >();
	
	protected LinkedList<LinkRepresentation> linksDisplayed = new LinkedList<>();
	protected LinkedList<LinkRepresentation> parentLinksDisplayed = new LinkedList<>();
	protected LinkedList<LinkRepresentation> tracesLinksDisplayed = new LinkedList<>();
	
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> sortedLinks = new HashMap<>();
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> reverseSortedLinks = new HashMap<>();
	
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> sortedParentLinks = new HashMap<>();
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> reverseSortedParentLinks = new HashMap<>();
	
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> sortedTracesLinks = new HashMap<>();
	protected HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> reverseSortedTracesLinks = new HashMap<>();

	
	
	public void sortLinks(HashMap<LinkKey, LinkRepresentation> currLinks, HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> currSortedLinks, HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> currReverseSortedLinks) {
		HashMap<NodeRepresentation, LinkRepresentation> tempSortedLinks;
		
		for (LinkKey key : currLinks.keySet()) {
			tempSortedLinks = currSortedLinks.get(key.getStartNode());
			
			if (tempSortedLinks == null) {
				tempSortedLinks = new HashMap<NodeRepresentation, LinkRepresentation>();
				currSortedLinks.put(key.getStartNode(), tempSortedLinks);
			}
			
			tempSortedLinks.put(key.getEndNode(), currLinks.get(key));
			

			tempSortedLinks = currReverseSortedLinks.get(key.getEndNode());
			
			if (tempSortedLinks == null) {
				tempSortedLinks = new HashMap<NodeRepresentation, LinkRepresentation>();
				currReverseSortedLinks.put(key.getEndNode(), tempSortedLinks);
			}
			
			tempSortedLinks.put(key.getStartNode(), currLinks.get(key));
		}
	}
	

	
	
	protected HashMap<NodeRepresentation, NodeRepresentation> edgeBundlesNodes = new HashMap<NodeRepresentation, NodeRepresentation>();
	
	
	public boolean oldColor = true;
	public boolean oldRenderingPoints = true;
	public boolean displayIntraPackageLinks = false;
	public boolean animateLinks = false;
	
	
	
	
	
	
	public float stepHeight = 1.0f;
	public int paddingSize = 1;
	
	
	
	public float findMinY() {
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
	
	public float findColiseumMinY() {
		LinkedList<PackageRepresentation> systemPackages = new LinkedList<PackageRepresentation>();
		LinkedList<ElementRepresentation> systemElements = new LinkedList<ElementRepresentation>();
		
		float minY = Float.MIN_VALUE;
		float currMinY;
		
		systemPackages.addAll(this.getPackages().iterator().next().getAllPackages().values());
		
		for (PackageRepresentation pack : this.getPackages()) {
			systemElements.addAll(pack.getAllElements());
		}
		
		for (PackageRepresentation p : systemPackages) {			
			if (!p.getSimpleName().equals("root")) {
				currMinY = p.getHeight();
			
				if (currMinY > minY) {
					minY = currMinY;
				}
			}
		}
		
		int maxLevel = this.getPackages().iterator().next().computeMaxLevel();
		int currLevel = 0;
		
		for (ElementRepresentation e : systemElements) {
			currLevel = findPackageAncestors(e).size()-1;
			currMinY = ((Math.abs(currLevel - maxLevel) + 1) * this.stepHeight) + e.getHeight();
			
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
		if(getPackageRepresentationClass() != RadialPackageRepresentation.class)
			return;
		
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
		
		float posX = (levelWidth * pack.getPackageLevel()) * (float)Math.cos(Math.toRadians(packMedianAngle));
		float posZ = (levelWidth * pack.getPackageLevel()) * (float)Math.sin(Math.toRadians(packMedianAngle));		
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
	public void updateNodesAndLinksDisplayed(String name) {
		long beforeNbreMS = System.currentTimeMillis();
		updateNodesDisplayed();
		updateLinksDisplayed();
		long afterNbreMS = System.currentTimeMillis();
		getSystemDef().setName(name);
		System.out.println("\n("+name+") Nombre de liens: " + getNbreLinks());
		System.out.println("("+name+") Temps pour cr�er les liens: " + (afterNbreMS - beforeNbreMS));
	}

	
	
	
	
	public void updateNodesDisplayed() {		
		nodesDisplayed.clear();
		for (PackageRepresentation pack : this.getPackages()) {
			updateNodesDisplayed(nodesDisplayed, pack);
		}
		
		this.createLinks();
	}
	
	protected void updateNodesDisplayed(HashSet<NodeRepresentation> nodesList, PackageRepresentation pack) {
		if (pack.render) {
			//nodesList.add(this.getNodeOnPackage(this.getPackageNode(pack)));
			
			nodesList.add(this.getPackageNode(pack));
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
	
	
	
	public void updateLinksDisplayed(Collection<LinkRepresentation> linksDisplayed) {
		this.linksDisplayed.clear();
		this.linksDisplayed.addAll(linksDisplayed);
	}
	
	
	/**
	 * Updates links depending on linksType attibute.
	 */
	public void updateLinksDisplayed() {
		
		HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> currSortedLinks = null;
		
		if (this.linksType == LINK_TYPE.INVOCATION) {
			currSortedLinks = this.sortedLinks;
		} else if (this.linksType == LINK_TYPE.PARENT) {
			currSortedLinks = this.sortedParentLinks;
		}
		else if (this.linksType == LINK_TYPE.TRACES) {
			currSortedLinks = this.sortedTracesLinks;
		}
		else {
			currSortedLinks = null;
		}
		
		this.linksDisplayed.clear();

		if (currSortedLinks != null) {
			for (NodeRepresentation currStartNode : nodesDisplayed) {
				if (currSortedLinks.get(currStartNode) != null) {
					for (NodeRepresentation currEndNode : currSortedLinks.get(currStartNode).keySet()) {
						
						if (this.nodesDisplayed.contains(currEndNode)) {
							boolean nodesAreSiblings;
							if (NodeRepresentation.getRealParentNode(currStartNode) == NodeRepresentation.getRealParentNode(currEndNode)
									&& currStartNode.getEntityRepresentation().isElement()
									&& currEndNode.getEntityRepresentation().isElement()) {
								nodesAreSiblings = true;
							} else {
								nodesAreSiblings = false;
							}
							
							if (!(this.displayIntraPackageLinks ^ nodesAreSiblings) || linksType == LINK_TYPE.TRACES) {
								this.linksDisplayed.add(currSortedLinks.get(currStartNode).get(currEndNode));
							}
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
		for (PackageRepresentation p : this.getPackages()) 
			this.getSystemPackages(p, systemPackages);
		return systemPackages;
	}
	
	private void getSystemPackages(PackageRepresentation pack, LinkedList<PackageRepresentation> systemPackages) {
		systemPackages.add(pack);
		for (PackageRepresentation p : pack.getPackages()) 
			getSystemPackages(p, systemPackages);
	}

	//**************** � terminer!!!! ****************************
	public void updatePackageHeight(float packagesHeight) {
		if(getPackageRepresentationClass() ==  ColiseumPackageRepresentation.class) {
			updateColiseumPackageHeight(packagesHeight);
		} else {
			
			for (PackageRepresentation pr : packagesNodes.keySet()) 
				pr.setHeight(packagesHeight);
		
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
	
	public ArrayList<EntityRepresentation> getUnfiltredEntityRepresentation() {
		ArrayList<EntityRepresentation> ret = new ArrayList<EntityRepresentation>();
		for(EntityRepresentation er : this.getNodesElements()) {
			if(!er.isFiltered) ret.add(er);
		}
		
		return ret;
	}
	
	public void filterAll() {		
		if (this.linksType == LINK_TYPE.INVOCATION) {
			for (LinkRepresentation edgeBundle : links.values()) {
				edgeBundle.setFiltered();
			}
		} else if (this.linksType == LINK_TYPE.PARENT) {
			for (LinkRepresentation edgeBundle : parentLinks.values()) {
				edgeBundle.setFiltered();
			}
		}else if (this.linksType == LINK_TYPE.TRACES) {
			for (LinkRepresentation edgeBundle : tracesLinks.values()) {
				edgeBundle.setFiltered();
			}
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
	
	public ArrayList<EntityRepresentation> getEntityRepFromString(ArrayList<String> list){
		ArrayList<EntityRepresentation> ret = new ArrayList<EntityRepresentation>();
		for(EntityRepresentation er : this.getNodesElements()) {
			if(list.isEmpty()) break;
			int t = list.size();
			for(String path : list) {
				if(er.getSimpleName().contains(path)) {
					
					ret.add(er);
					list.remove(path);
					break;
				}
			}
		}
		if(!list.isEmpty())System.out.println("Liste non trouv�:");
		for(String path : list) {
			System.out.println("- "+path);
		}
		return ret;
	}
	
	
	public void filterByElements(Collection<EntityRepresentation> startElements) {		
		HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> currSortedLinks = null;
		HashMap<NodeRepresentation, HashMap<NodeRepresentation, LinkRepresentation>> currReverseSortedLinks = null;

		if (this.linksType == LINK_TYPE.INVOCATION) {
			currSortedLinks = this.sortedLinks;
			currReverseSortedLinks = this.reverseSortedLinks;
		} else if (this.linksType == LINK_TYPE.PARENT) {
			currSortedLinks = this.sortedParentLinks;
			currReverseSortedLinks = this.reverseSortedParentLinks;
		}
		else if (this.linksType == LINK_TYPE.TRACES) {
			currSortedLinks = this.sortedTracesLinks;
			currReverseSortedLinks = this.reverseSortedTracesLinks;
		}
		
		if (currSortedLinks != null) {
			filterAll();
			
			HashMap<Integer, NodeRepresentation> startNodes = new HashMap<Integer, NodeRepresentation>();
			HashSet<NodeRepresentation> unfilteredNodes = new HashSet<NodeRepresentation>();
			NodeRepresentation node;
			
			HashMap<NodeRepresentation, LinkRepresentation> currLinks;
			
			for (IPickable startElement : startElements) {	
				if (startElement.isElement()) {
					node = elementsNodes.get(startElement);
					if(node != null)
						startNodes.put(node.hashCode(), node);
				} else if (startElement.isPackage()) {
					node = packagesNodes.get(startElement);
					if(node != null)
						startNodes.put(node.hashCode(), node);
				} else {
					node = null;
				}
				
				
				if (node != null) {
					node.getEntityRepresentation().setUnFiltered();
					
					//� d�placer dans SceneLandscape ??? (car il y une diff�rence entre isFILTERED et isSELECTED)
					node.getEntityRepresentation().select();
					unfilteredNodes.add(node);
					currLinks = currSortedLinks.get(node);
					if (currLinks != null) {
						for (LinkRepresentation currLink : currLinks.values()) {
							if (this.linksFilterType == LINKFILTER_TYPE.FILTER_INTRASELECTION) {
								if (startElements.contains(currLink.getEndNode().getEntityRepresentation())) {
									currLink.setUnFiltered();
									unfilteredNodes.add(currLink.getEndNode());
								}
							}
							else if (this.linksFilterType == LINKFILTER_TYPE.FILTER_EXTRASELECTION){
								if (!startElements.contains(currLink.getEndNode().getEntityRepresentation())) {
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
					
					if (this.linksFilterType != LINKFILTER_TYPE.FILTER_INTRASELECTION) {
						currLinks = currReverseSortedLinks.get(node);
						if (currLinks != null) {
							if (this.linksFilterType == LINKFILTER_TYPE.FILTER_EXTRASELECTION) {
								for (LinkRepresentation currLink : currLinks.values()) {
									if (!startElements.contains(currLink.getStartNode().getEntityRepresentation())) {
										currLink.setUnFiltered();
										unfilteredNodes.add(currLink.getStartNode());
									}
								}
							}
							else {
								for (LinkRepresentation currLink : currLinks.values()) {
									currLink.setUnFiltered();
									unfilteredNodes.add(currLink.getStartNode());
								}
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
		if (this.linksType == LINK_TYPE.INVOCATION) {
			for (LinkRepresentation edgeBundle : links.values()) {
				edgeBundle.setUnFiltered();
			}
		}
		else if (this.linksType == LINK_TYPE.PARENT) {
			for (LinkRepresentation edgeBundle : parentLinks.values()) {
				edgeBundle.setUnFiltered();
			}
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
		float minY = this.findMinY(); //findMinY();

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

	public SystemRepresentation(SystemDef system) {
		this.system = system;
	}

	public SystemDef getSystemDef() {
		return this.system;
	}

	public void addPackage(PackageRepresentation p) {
		this.packages.put(p.getPackage().getName(), p);
	}

	public Class<?> getPackageRepresentationClass() {
		if (packages.isEmpty())
			return PackageRepresentation.class;
		// TODO Looks bad.
		Class<?> res = this.packages.values().iterator().next().getClass();
		return res;
	}

	public Collection<PackageRepresentation> getPackages() {
		return this.packages.values();
	}

	//Ajout Simon
	//**********************************************************************************************************************************
	
	//Ajouter une fonction pour vider la liste de packages du syst�me.
	public void clearPackages() {
		this.packages.clear();
	}
	
	
	//Fonctions pour trouver la taille (en y) de la plus grande bo�tes (classe).
	/*
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
	*/

	//Fonctions pour g�rer la liste de noeuds repr�sentant les packages
	public void clearPackagesNodes() {
		this.packagesNodes.clear();
	}
	
	public void addPackagesNodes(HashMap<PackageRepresentation, NodeRepresentation> packagesNodes) {
		this.packagesNodes.putAll(packagesNodes);
		if(rootPackage==null) {
			for(PackageRepresentation pr : this.packagesNodes.keySet()) {
				if(pr.getName().equals("Package : root"))
					rootPackage = pr;
			}
		}
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
	
	//Fonctions pour g�rer la liste de noeuds repr�sentant les packages (qui sont SUR les packages)
	public void clearNodesOnPackages() {
		this.nodesOnPackages.clear();
	}
	
	public void updateNodesOnPackages() {
		NodeRepresentation packageNode;
		NodeRepresentation newNodeOnPackage;
		float nodePosXd, nodePosZd, nodePosYd;
		float epsilon = 0.0f;
		
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
				
				
				if (this.findPackageAncestors(pack) == null) {
					nodePosYd = pack.getHeight() + epsilon;
				}
				else {
					nodePosYd = (this.findPackageAncestors(pack).size()-1 * this.stepHeight) + pack.getHeight() + epsilon;
				}
				
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
	
	
	//Fonctions pour g�rer la liste de noeuds au-dessus des classes (utilis�es par les liens entre siblings)
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
				if (element.getSimpleName().equals("org.apache.tools.ant.ProjectComponent")) {
					System.out.println("STOP");
				}
				
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
	
	
	
	
	//Fonctions pour g�rer la liste de noeuds repr�sentant les �l�ments
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
	
	
	
	public void clearParentLinksTable() {
		parentLinksTable.clear();
	}
	
	public void addParentLinksTable(HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> parentLinksTable) {
		this.parentLinksTable.putAll(parentLinksTable);
	}
	
	public HashMap<NodeRepresentation, HashMap<NodeRepresentation, Integer[]>> getParentLinksTable() {
		return this.parentLinksTable;
	}
	
	
	
	
	
	//Fonctions pour g�rer la liste de liens
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
	
	
	public int getNbreDisplayedPolygons() {
		int nbreDisplayedPolygons = 0;		
		nbreDisplayedPolygons += (this.linksDisplayed.size() ) * 30 * 4;
		
		return nbreDisplayedPolygons;
	}
	
	
	
	public void render(GL gl)
	{
		gl.glPushMatrix();
			if (this.dirtyPackages || this.dirtyLinks) {
				VersoScene.id =0;
				VersoScene.pickingEntities.clear();
			}
		

			PackageRepresentation rootPackage = this.packages.values().iterator().next();
			if (rootPackage instanceof RadialPackageRepresentation) {
				if (((RadialPackageRepresentation)rootPackage).getInternRadius() > 20.0) {
					double scaleRatio = 20.0 / ((RadialPackageRepresentation)rootPackage).getInternRadius();
					gl.glScaled(scaleRatio, scaleRatio, scaleRatio);
				}
			}
			
			
			if (this.dirtyPackages) {
				System.out.println("RENDU PACKAGES");
				
				renderingPackages(gl);
				
				System.out.println("FIN RENDU DES PACKAGES\n");
			}
			else {
				gl.glCallList(this.packageDisplayListInd);
			}
					
				
			if (this.displayLinks) {
				if (this.dirtyLinks && this.linksDisplayListIndexes != null && this.linksDisplayListIndexes.length > 0) {
					System.out.println("RENDU LIENS");

					renderingLinks(gl);
					
					System.out.println("FIN RENDU DES LIENS\n");
				}
				else {
					gl.glPushMatrix();
						for (int i = 0; i < this.linksDisplayListIndexes.length; i++) 
							gl.glCallList(this.linksDisplayListIndexes[i]);
					gl.glPopMatrix();
				}
			}
			
		gl.glPopMatrix();
		
		/*
		System.out.println("Nombre classes: " + this.elementsNodes.size());
		System.out.println("Nombre de liens: " + this.getNbreLinks());
		*/
	}

	private void renderingLinks(GL gl) {
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

	private void renderingPackages(GL gl) {
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
	
	
	public void refreshLinks(GL gl) {
		for (LinkRepresentation link : this.links.values()) {
			link.render(gl);
		}
	}
	
	
	public void setCamDist(double camX, double camY, double camZ){}
	
	
	public double nbrePossibilities() {
		double nbre = 0;
		for (PackageRepresentation p : this.getPackages()) {
			nbre += nbrePossibilities(p);
		}
		
		return nbre;
	}
	
	public double nbrePossibilities(PackageRepresentation pack) {
		double nbre = factorielle(pack.getPackages().size());

		for (PackageRepresentation p : pack.getPackages()) {
			nbre = nbre * nbrePossibilities(p);
		}
		
		return nbre;
	}
	
	public double factorielle(double nbre) {
		double tempNbre = 1;
		
		for (long i = 1; i <= nbre; i++) {
			tempNbre = tempNbre * i;
		}
		
		return tempNbre;
	}

}

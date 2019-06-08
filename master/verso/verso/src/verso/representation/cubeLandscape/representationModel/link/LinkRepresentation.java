package verso.representation.cubeLandscape.representationModel.link;

import java.awt.Color;

import javax.media.opengl.GL;

import verso.graphics.primitives.PrimitiveColored;
import verso.representation.Renderable;
import verso.representation.cubeLandscape.representationModel.EntityRepresentation;

public abstract class LinkRepresentation extends EntityRepresentation implements Renderable {
	protected NodeRepresentation startNode;
	protected NodeRepresentation endNode;
	protected boolean isBidirectional;
	protected int nbreInLinks;
	protected int nbreOutLinks;
	
	protected PrimitiveColored coloredMesh;
	protected float meshSize;
	protected float lineSize;
	protected Color linkStartColor;
	protected Color linkEndColor;
	protected Color linkBidirectionalColor;
	
	protected float linkSaturation;
	

	/*
	private static double maxMeshSize = 1.0;
	private static double minMeshSize = 0.1;
	private static double meshSizeInterval = 0.01;
	*/
	
	public LinkRepresentation(NodeRepresentation startNode, NodeRepresentation endNode, int nbreInLinks, int nbreOutLinks, PrimitiveColored coloredMesh, float meshSize, float lineSize, Color linkStartColor, Color linkEndColor, Boolean isBidirectional, Color linkBidirectionalColor, float linkSaturation) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.nbreInLinks = nbreInLinks;
		this.nbreOutLinks = nbreOutLinks;
		this.isBidirectional = isBidirectional;
		
		this.coloredMesh = coloredMesh;
		this.meshSize = meshSize;
		this.lineSize = lineSize;
		this.linkStartColor = linkStartColor;
		this.linkEndColor = linkEndColor;
		this.linkBidirectionalColor = linkBidirectionalColor;
		
		this.linkSaturation = linkSaturation;
	}
	
	

	
	
	public void setStartNode(NodeRepresentation startNode) {
		this.startNode = startNode;
	}
	
	public NodeRepresentation getStartNode() {
		return this.startNode;
	}

	public void setEndNode(NodeRepresentation endNode) {
		this.endNode = endNode;
	}
	
	public NodeRepresentation getEndNode() {
		return this.endNode;
	}
	
	public void setNbreInLinks(int nbreInLinks) {
		this.nbreInLinks = nbreInLinks;

		//updateLinkColor();
	}

	public void incNbreInLinks() {
		this.nbreInLinks++;
	
		//updateLinkColor();
	}
	
	public int getNbreInLinks() {
		return this.nbreInLinks;
	}
	
	public void setNbreOutLinks(int nbreOutLinks) {
		this.nbreOutLinks = nbreOutLinks;

		//updateLinkColor();
	}
	
	public void incNbreOutLinks() {
		this.nbreOutLinks++;

		//updateLinkColor();
	}
	
	public int getNbreOutLinks() {
		return this.nbreOutLinks;
	}
	
	public void setIsBidirectional(boolean isBidirectional) {
		this.isBidirectional = isBidirectional;
	}
	
	public boolean getIsBidirectional() {
		return this.isBidirectional;
	}
	
	
	
	
	
	
	
	
	public void setColoredMesh(PrimitiveColored coloredMesh) {
		this.coloredMesh = coloredMesh;
	}
	
	public PrimitiveColored getColoredMesh() {
		return this.coloredMesh;
	}
	
	public void setMeshSize(float meshSize) {
		this.meshSize = meshSize;
	}
	
	public float getMeshSize() {
		return this.meshSize;
	}
	
	public void setLineSize(float lineSize) {
		this.lineSize = lineSize;
	}
	
	public float getLineSize() {
		return this.lineSize;
	}
		
	/*
	public void updateMeshSize() {
		this.meshSize = (this.nbreInLinks + this.nbreOutLinks) * this.meshSizeInterval;
		
		if (this.meshSize < this.minMeshSize) {
			this.meshSize = this.minMeshSize;
		}
		else if (this.meshSize > this.maxMeshSize) {
			this.meshSize = this.maxMeshSize;
		}
		
		this.directLink.setMeshSize(this.meshSize);
	}
	*/

	public void setLinkStartColor(Color linkStartColor) {
		this.linkStartColor = linkStartColor;
		
		//updateLinkColor();
	}
	
	public Color getLinkStartColor() {
		return this.linkStartColor;
	}
	
	public void setLinkEndColor(Color linkEndColor) {
		this.linkEndColor = linkEndColor;
		
		//updateLinkColor();
	}
	
	public Color getLinkEndColor() {
		return this.linkEndColor;
	}
	
	/*
	public void updateLinkColor() {
		Color linkStartColor;
		Color linkEndColor;
		int nbreTotalLinks = this.nbreInLinks + this.nbreOutLinks;
		double inLinksPercentage = (double)this.nbreInLinks / nbreTotalLinks;
		double outLinksPercentage = (double)this.nbreOutLinks / nbreTotalLinks;
		
		double[] linkStartColors = new double[3];
		linkStartColors[0] = inLinksPercentage * (linkEndBaseColor.getRed()/255) + outLinksPercentage * (linkStartBaseColor.getRed()/255);
		linkStartColors[1] = inLinksPercentage * (linkEndBaseColor.getGreen()/255) + outLinksPercentage * (linkStartBaseColor.getGreen()/255);
		linkStartColors[2] = inLinksPercentage * (linkEndBaseColor.getBlue()/255) + outLinksPercentage * (linkStartBaseColor.getBlue()/255);
		
		
		double[] linkEndColors = new double[3];
		linkEndColors[0] = inLinksPercentage * (linkStartBaseColor.getRed()/255) + outLinksPercentage * (linkEndBaseColor.getRed()/255);
		linkEndColors[1] = inLinksPercentage * (linkStartBaseColor.getGreen()/255) + outLinksPercentage * (linkEndBaseColor.getGreen()/255);
		linkEndColors[2] = inLinksPercentage * (linkStartBaseColor.getBlue()/255) + outLinksPercentage * (linkEndBaseColor.getBlue()/255);
		
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
		
		this.directLink.setlinkStartColor(linkStartColor);
		this.directLink.setlinkEndColor(linkEndColor);
	}
	*/
	
	public void setLinkBidirectionalColor(Color linkBidirectionalColor) {
		this.linkBidirectionalColor = linkBidirectionalColor;
	}
	
	public Color getLinkBidirectionalColor() {
		return this.linkBidirectionalColor;
	}
	
	
	
	public abstract LinkRepresentation copyLink();
	
	
	
	public void render(GL gl) {
	}
}

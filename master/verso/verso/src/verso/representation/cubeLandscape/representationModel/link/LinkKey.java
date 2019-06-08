package verso.representation.cubeLandscape.representationModel.link;


public class LinkKey {
	private NodeRepresentation startNode;
	private NodeRepresentation endNode;
	
	public LinkKey(NodeRepresentation startNode, NodeRepresentation endNode)
	{
		this.startNode = startNode;
		this.endNode = endNode;
	}

	public NodeRepresentation getStartNode() {
		return startNode;
	}

	public void setStartNode(NodeRepresentation startNode) {
		this.startNode = startNode;
	}
	
	public NodeRepresentation getEndNode() {
		return endNode;
	}

	public void setEndNode(NodeRepresentation endNode) {
		this.endNode = endNode;
	}
	
	public int hashCode()
	{
		return this.startNode.hashCode() + this.endNode.hashCode();
	}
	
	public boolean equals(Object lk)
	{
		return this.startNode.equals(((LinkKey)lk).getStartNode()) && this.endNode.equals(((LinkKey)lk).getEndNode());
	}
}

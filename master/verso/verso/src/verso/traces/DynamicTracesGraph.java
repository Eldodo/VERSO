package verso.traces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DynamicTracesGraph {
	private Node root;
	private List<Node> nodesSet;
	
	public DynamicTracesGraph(File tracesFile){
		nodesSet = new ArrayList<Node>();
		Node previousNode = null;
		Node currentNode = null;
		if(tracesFile != null) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(tracesFile));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String st; 
			 try {
				while ((st = br.readLine()) != null) {
					st = st.replaceAll("[^a-zA-Z/:]", "");
					st = st.replaceAll("[/]", ".");
					String[] tokens = st.split(":");
					if(tokens[0].equals("Enter")) {
						currentNode = findByName(tokens[1]);
						if(currentNode == null) {
							currentNode = new Node(tokens[1]);
							nodesSet.add(currentNode);
						}
						if(root == null) {
							root = currentNode;
						}
						if(previousNode!=null) {
							previousNode.addCalled(currentNode);
							currentNode.addCaller(previousNode);
						}
						previousNode = currentNode;
						
					}
					else 
						previousNode = findByName(tokens[1]).getLastCaller();
					
				}
			 }catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 
		}
	}
	
	public Node findByName(String name) {
		for(Node n : nodesSet) {
			if(n.getName().equals(name))
				return n;
		}
		return null;
	}
	

}

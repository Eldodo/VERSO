package verso.traces;

import java.util.ArrayList;
import java.util.List;


public class Node {
	private String name;
	private List<Node> callers;
	private List<Node> calleds;
	
	public Node(String name) {
		this.name = name;
		callers = new ArrayList<Node>();
		calleds = new ArrayList<Node>();
	}
	
	public String getName() {
		return name;
	}
	
	public void addCaller(Node caller) {
		callers.add(caller);
	}
	
	public void addCalled(Node called) {
		callers.add(called);
	}
	
	public List<Node> getCallers(){
		return callers;
	}
	
	public List<Node> getCalleds(){
		return calleds;
	}
	
	public Node getLastCaller() {
		return callers.get(callers.size()-1);
	}
	

}

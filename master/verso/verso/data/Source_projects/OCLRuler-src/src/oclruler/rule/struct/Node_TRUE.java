package oclruler.rule.struct;

import oclruler.metamodel.Concept;

/**
 * A basic constraint : <code>true</code>
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class Node_TRUE extends Node {

	public Node_TRUE(Node parent, Concept context) {
		super(parent, context, Type.TRUE );
	}

	@Override
	protected boolean toBePruned() {
		return true;
	}
	
	@Override
	public void prune() throws CollapsingException {
		// Nada que hacer
	}
	
	@Override
	public String getOCL(String tab) {
		return "true";
	}
	
	@Override
	public String printXML() {
		return "<TRUE "+XML_HeaderAttributes()+"/>";
	}
	
}

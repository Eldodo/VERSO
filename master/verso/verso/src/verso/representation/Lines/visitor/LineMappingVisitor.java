package verso.representation.Lines.visitor;

import java.util.ArrayList;
import java.util.List;

import verso.representation.Lines.representationModel.ClassLineRepresentation;
import verso.representation.Lines.representationModel.LineRepresentation;
import verso.representation.cubeLandscape.representationModel.repvisitor.Mapping;

public class LineMappingVisitor{

	private LineMapping map;
	private List<Mapping> lineMapping = new ArrayList<Mapping>();
	
	public void setMapping(LineMapping map)
	{
		this.map = map;
	}
	
	public void visit(LineRepresentation lineRep)
	{	
		//Appliquer le mapping sur les lignes
	}
	
	public void visit(ClassLineRepresentation classLineRep)
	{
		// rappeller la visite sur les enfants...
	}
}

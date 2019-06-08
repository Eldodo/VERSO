package verso.representation.Lines.representationModel;

import java.awt.Color;

import verso.model.Line;
import verso.representation.Lines.visitor.LineMappingVisitor;

public class LineRepresentation {
	private Line line = null;
	private Color color = null;
	
	public LineRepresentation(Line line)
	{
		this.line = line;
	}
	
	public Line getLine()
	{
		return this.line;
	}
	
	public void setColor(Color c)
	{
		this.color = c;
	}
	
	public Color getColor()
	{
		return this.color;
	}
	
	public int getLenght()
	{
		return this.line.getLenght();
	}
	
	public void render()
	{
		System.out.println("rendering");
	}
	
	public void accept(LineMappingVisitor v)
	{
		v.visit(this);
	}
	
	public String getText()
	{
		return this.line.getText();
	}
	
	
}

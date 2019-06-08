package verso.model;




public class Line extends Entity{
	
	int lineStart = 0;
	int lineLenght = 0;
	String text;
	
	public Line(String name) {
		super(name);
	}
	
	public Line(String name, int lineStart, int lineLenght, String text)
	{
		super(name);
		this.lineStart = lineStart;
		this.lineLenght = lineLenght;
		this.text = text;
	}
	
	public int getLineStart()
	{
		return lineStart;
	}
	
	public int getLenght()
	{
		return lineLenght;
	}
	
	
	public Object accept(Visitor v)
	{
		return v.visit(this);
	}
	
	public String getText()
	{
		return this.text;
	}	
		
		
}

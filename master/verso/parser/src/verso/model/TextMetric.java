package verso.model;

public class TextMetric {

	protected int startPos;
	protected int lenght;
	
	public TextMetric(int startPos, int lenght)
	{
		this.startPos = startPos;
		this.lenght = lenght;
	}
	
	public int getStartPos()
	{
		return this.startPos;
	}
	
	public int getLenght()
	{
		return this.lenght;
	}
}

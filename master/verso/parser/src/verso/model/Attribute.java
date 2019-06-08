package verso.model;


public class Attribute extends LowLevelElement{
	
	public Attribute(String name)
	{
		super(name);
	}
	
	public Object accept(Visitor v)
	{
		return v.visit(this);
	}

}

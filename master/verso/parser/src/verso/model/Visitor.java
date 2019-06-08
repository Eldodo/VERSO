package verso.model;


public interface Visitor {
	
	public Object visit(SystemDef system);
	public Object visit(ClassDef classe);
	public Object visit(LibDef lib);
	public Object visit(InterfaceDef inter);
	public Object visit(Method method);
	public Object visit(Attribute attribute);
	public Object visit(Package pack);
	public Object visit (Entity en);
	public Object visit(Line l);
}

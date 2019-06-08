package verso.representation;

import verso.model.Attribute;
import verso.model.ClassDef;
import verso.model.Entity;
import verso.model.InterfaceDef;
import verso.model.Method;
import verso.model.Package;
import verso.model.SystemDef;

public interface Visitor {
	
	public Object visit(SystemDef system);
	public Object visit(ClassDef classe);
	public Object visit(InterfaceDef inter);
	public Object visit(Method method);
	public Object visit(Attribute attribute);
	public Object visit(Package pack);
	public Object visit (Entity en);
}

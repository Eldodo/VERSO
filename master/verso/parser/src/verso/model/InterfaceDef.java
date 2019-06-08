package verso.model;

import java.util.List;

public class InterfaceDef extends Element {

	public InterfaceDef(String name) {
		super(name);
	}

	public Object accept(Visitor v) {
		return v.visit(this);
	}

	@Override
	public String getInterfacesString() {
		return "";
	}

	@Override
	public void setInterfaces(List<String> liste) {
		// On fait rien ...
	}
}

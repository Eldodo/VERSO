package oclruler.metamodel;

public class EnumLit extends NamedEntity {
	int value;
	public EnumLit(String name, int value) {
		super(name);
		this.value = value;
	}

	@Override
	public String prettyPrint(String tab) {
		return tab + "EnumLit:("+name+","+value+")";
	}

	@Override
	public String simplePrint() {
		return name;
	}

}

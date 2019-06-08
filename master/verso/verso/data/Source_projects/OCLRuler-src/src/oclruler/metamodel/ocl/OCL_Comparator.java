package oclruler.metamodel.ocl;

import java.util.ArrayList;

import oclruler.utils.ToolBox;

public class OCL_Comparator extends OCL_Element {

	public OCL_Comparator() {
		super("OCL_Comparator", getRandomComparator());
	}
	
	public OCL_Comparator(COMPARATOR c) {
		super("OCL_Comparator", c);
	}

	@Override
	public String prettyPrint() {
		return "("+name + " : "+((COMPARATOR)value).value()+")";
	}

	@Override
	public String simplePrint() {
		return ((COMPARATOR)value).value();
	}
	
	
	public enum COMPARATOR {
		GT(">"), 
		LT("<"),
		EQ("="), 
		NEQ("<>");

		private String name = "";

		// Constructeur
		COMPARATOR(String name) {
			this.name = name;
		}

		public String value() {
			return name;
		}
	}
	
	public static COMPARATOR getRandomComparator(){
		return ToolBox.getRandom(COMPARATOR.values());
	}
	public static COMPARATOR getRandomComparator(COMPARATOR c){
		COMPARATOR res = null;
		if(c != null){
			ArrayList<COMPARATOR> cmps = new ArrayList<>(COMPARATOR.values().length);
			for (COMPARATOR comparator : COMPARATOR.values()) 
				cmps.add(comparator);
			cmps.remove(c);
			res = ToolBox.getRandom(cmps);
		} else {
			res = getRandomComparator();
		}
		return res;
	}

	public void mutate() {
		value = getRandomComparator((COMPARATOR)getValue());
	}
}

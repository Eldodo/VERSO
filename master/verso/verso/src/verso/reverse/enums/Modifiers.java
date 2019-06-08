package verso.reverse.enums;

import java.util.EnumSet;

import com.github.javaparser.ast.Modifier;

/**
 * Defines all the possible modifiers and their symbols to be used
 * in the grammar
 * @author rishi
 *
 */
public class Modifiers {

	/*PRIVATE(2, "-"),
	PRIVATE_STATIC(10, "- {static}"),
	PRIVATE_FINAL_STATIC(26, ""),
	
	PROTECTED(4, "#"),
	PROTECTED_STATIC(12, "{static}"),
	PROTECTED_ABSTRACT(1028, "# {abstract}"),
	PROTECTED_FINAL_STATIC(28, ""),
	
	PUBLIC(1, "+"),
	PUBLIC_STATIC(9, "+ {static}"),
	PUBLIC_ABSTRACT(1025, "+ {abstract}"),
	PUBLIC_FINAL_STATIC(25, ""),
	
	PACKAGE(0, "~"),
	PACKAGE_STATIC(8, "~ {static}"),
	PACKAGE_ABSTRACT(1024, "~ {abstract}"),
	PACKAGE_FINAL_STATIC(24, "");*/
	
	
	public int modifier;
	public String symbol;
	
	/**
	 * Initializes the modifiers with number and symobol for it 
	 * @param modifier
	 * @param symbol
	 */
	private Modifiers(int modifier, String symbol) {
		this.modifier = modifier;
		this.symbol = symbol;
	}
	
	/**
	 * Returns value for modifier
	 * @param modifier2
	 * @return
	 */
	public static String valueOf(EnumSet<Modifier> modifier){
		String s = "";
		for(Modifier mod : modifier) {
			//System.out.println(mod.asString());
			switch(mod.asString()) {
				case "final":
					return "";
				case "private":
					s+="-";
					break;
				case "protected":
					s+="#";
					break;
				case "public":
					s+="+";
					break;
			}
		}
		for(Modifier mod : modifier) {
			switch(mod.asString()) {
				case "static":
					s+=" {static}";
					break;
				case "abstract":
					s+=" {abstract}";
					break;				
			}
		}
		return s;
	}
}

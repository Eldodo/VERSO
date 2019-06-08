package textEditor.JavaEditor;

/**
 * 
 * @author Maxime Gallais-Jimenez
 *
 * All Java keywords and special characters
 */

public class JavaConstants {

	private static final String operators[] = {
			",", "=", "\\*=", "/=", "%=", "\\+=", "-=", "<<=", ">>=", ">>>=", "&=", "|=", "\\^=", "\\?:", "||",
			"&&", "|", "\\^", "&", "!=", "==", ">=", ">", "<=", "<", "instanceof", "<<", ">>", ">>>", "\\+", "-", "/", "%",
			"\\*", "cast", "\\+", "-", "!", "~", "\\+\\+", "--", "new", "new", "new", "\\+\\+", "--", "field", "method", "\\[\\]",
			"new", "\\+\\+", "--", "convert", "expr", "array", "goto"
		};
		
		private static final String values[] = {
			"Identifier", "boolean", "byte", "char", "short", "int", "long", "float", "double", "string"
		};
		
		private static final String types[] = {
			"byte", "char", "short", "int", "long", "float", "double", "void", "boolean"
		};
		
		private static final String expressions[] = {
			"true", "false", "this", "super"	
		};
		
		private static final String statements[] = {
			"if", "else", "for", "while", "do", "switch", "case", "default", "break", "continue", "return", "try",
			"catch", "finally", "throw", "stat", "expression", "declaration", "declaration"
		};
		
		private static final String declarations[] = {
			"import", "class", "extends", "implements", "interface", "package"
		};
		
		private static final String modifiers[] = {
			"private", "public", "protected", "const", "static", "transient", "synchronized", "native", "final",
			"volatile", "abstract", "strictfp"
		};
		
		private static final String punctuations[] = {
			";", ":", "\\?", "\\{", "\\}", "\\(", "\\)", "\\[", "\\]", "throws"
		};
		
		private static final String specials[] = {
			"error", "comment", "type", "length",
			"inline-return", "inline-method", "inline-new"
		};
		
		public static final String operatorsJava = concatKeyWords(operators);
		public static final String valuesJava = concatKeyWords(values);
		public static final String typesJava = concatKeyWords(types);
		public static final String expressionsJava = concatKeyWords(expressions);
		public static final String statementsJava = concatKeyWords(statements);
		public static final String declarationsJava = concatKeyWords(declarations);
		public static final String modifiersJava = concatKeyWords(modifiers);
		public static final String punctuationsJava = concatKeyWords(punctuations);
		public static final String specialsJava = concatKeyWords(specials);
		
		/**
		 * Concatains keywords to search them in regex
		 * @param keyWords List of keywords
		 * @return The concatenation of all keywords separated by |
		 */
		private static String concatKeyWords(String[] keyWords) {
			String concat = "";
			for(String keyword : keyWords) {
				concat+=keyword+"|";
			}
			return concat.substring(0, concat.length()-1);
		}
}

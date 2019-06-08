package oclruler.ui;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;

public class OCLTokenMaker  extends AbstractTokenMaker {
	
	protected final String operators = "=|><&";
	protected final String separators = "()[]";
	protected final String separators2 = ".,;:";			// Characters you don't want syntax highlighted but separate identifiers.


	private int currentTokenStart;
	private int currentTokenType;

	
	@Override
	public TokenMap getWordsToHighlight() {
	   TokenMap tokenMap = new TokenMap();
	  
	   tokenMap.put("and",  	Token.RESERVED_WORD);
	   tokenMap.put("or",   	Token.RESERVED_WORD);
	   tokenMap.put("implies", 	Token.RESERVED_WORD);
	   tokenMap.put("true", 	Token.RESERVED_WORD);
	   tokenMap.put("false", 	Token.RESERVED_WORD);
	   tokenMap.put("not", 		Token.RESERVED_WORD);

	   tokenMap.put("inv", 		Token.RESERVED_WORD_2);
	   tokenMap.put("context", 	Token.RESERVED_WORD_2);
	   tokenMap.put("import", 	Token.RESERVED_WORD_2);
	   tokenMap.put("package", 	Token.RESERVED_WORD_2);
	   tokenMap.put("endpackage", 	Token.RESERVED_WORD_2);
	  
	   tokenMap.put("forall", 	Token.FUNCTION);
	   tokenMap.put("exists", 	Token.FUNCTION); 
	   tokenMap.put("size",  	Token.FUNCTION);
	   tokenMap.put("allinstances",  Token.FUNCTION);
	   
	   tokenMap.put("self",  Token.VARIABLE);
	   
	   for (char c : separators2.toCharArray()) {
		   tokenMap.put(c+"",  Token.SEPARATOR);
		
	}
	
	   for (Concept c : Metamodel.getAllConcepts().values()) {
		   tokenMap.put(c.getName(),  Token.VARIABLE);
	   }
	   
	   
	   
	   
	   return tokenMap;
	}
	
	@Override
	public void addToken(Segment segment, int start, int end, int tokenType, int startOffset) {
//		System.out.println("TOK: "+segment+" : "+start+"|"+end);
	   // This assumes all keywords, etc. were parsed as "identifiers."
	   if (tokenType==Token.IDENTIFIER) {
	      int value = wordsToHighlight.get(segment, start, end);
	      if (value != -1) {
	         tokenType = value;
	      }
	   }
	   super.addToken(segment, start, end, tokenType, startOffset);
	}

	public Token getTokenList(Segment text, int startTokenType, int startOffset) {
//		System.out.println("OCLTokenMaker.getTokenList("+text+", "+startTokenType+", "+ startOffset+")");
		resetTokenList();
		char[] array = text.array;
		int offset = text.offset;
		int count = text.count;
		int end = offset + count;

		// Token starting offsets are always of the form:
		// 'startOffset + (currentTokenStart-offset)', but since startOffset and
		// offset are constant, tokens' starting positions become:
		// 'newStartOffset+currentTokenStart'.
		int newStartOffset = startOffset - offset;

		currentTokenStart = offset;
		currentTokenType = startTokenType;

		for (int i = offset; i < end; i++) {
			char c = array[i];
			switch (currentTokenType) {
			case Token.NULL:
				currentTokenStart = i; // Starting a new token here.
				switch (c) {
				case ' ':
				case '\t':
					currentTokenType = Token.WHITESPACE;
					break;

				case '"':
					currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					break;

				case '-':
					if(array[i+1] == '-'){
						currentTokenType = Token.COMMENT_EOL;
						break;
					}
					if(array[i+1] == '>'){
						i++;
						currentTokenType = Token.IDENTIFIER;
						break;
					}

				default:
					if (RSyntaxUtilities.isDigit(c)) {
						currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
						break;
					} else if (RSyntaxUtilities.isLetter(c) || c == '_') {
						currentTokenType = Token.IDENTIFIER;
						break;
					}

					// Anything not currently handled - mark as an identifier
					currentTokenType = Token.IDENTIFIER;
					break;

				} // End of switch (c).

				break;

			case Token.WHITESPACE:

				switch (c) {

				case ' ':
				case '\t':
					break; // Still whitespace.

				case '"':
					addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
					currentTokenStart = i;
					currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					break;
				case '.':
					addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
					currentTokenStart = i;
					currentTokenType = Token.NULL;
					break;
				case '-':
					if(array[i+1] == '-'){
						addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
						currentTokenStart = i;
						currentTokenType = Token.COMMENT_EOL;
						break;
					} 
					if(array[i+1] == '>'){
						i++;
						addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
						currentTokenStart = i;
						currentTokenType = Token.NULL;
						break;
					} 
					
				default: // Add the whitespace token and start anew.
					addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
					currentTokenStart = i;

					if (RSyntaxUtilities.isDigit(c)) {
						currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
						break;
					} else if (RSyntaxUtilities.isLetter(c) || c == '_') {
						currentTokenType = Token.IDENTIFIER;
						break;
					}

					// Anything not currently handled - mark as identifier
					currentTokenType = Token.IDENTIFIER;

				} // End of switch (c).

				break;

			default: // Should never happen
			case Token.IDENTIFIER:
//				System.out.println("IDENT: "+c +"    ("+text+")");
				switch (c) {

				case ' ':
				case '\t':
					addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
					currentTokenStart = i;
					currentTokenType = Token.WHITESPACE;
					break;

				case '"':
					addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
					currentTokenStart = i;
					currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					break;
				case '.':
					addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
					i--;
					currentTokenStart = i;
					currentTokenType = Token.NULL;
					break;
					
				case '-':
					if(array[i+1] == '-'){
						addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
						currentTokenStart = i;
						currentTokenType = Token.COMMENT_EOL;
						break;
					}
					if(array[i+1] == '>'){
						i++;
						addToken(text, currentTokenStart, i , Token.IDENTIFIER, newStartOffset + currentTokenStart);
						currentTokenStart = i;
						currentTokenType = Token.NULL;
						break;
					} 
				case '(':
					addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
					i--;
					currentTokenStart = i;
					currentTokenType = Token.NULL;
					break;
				case ':':
					addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
					i--;
					currentTokenStart = i;
					currentTokenType = Token.NULL;
					break;
				
				default:
					if (RSyntaxUtilities.isLetterOrDigit(c) || c == '_') {
						break; // Still an identifier of some type.
					}
					// Otherwise, we're still an identifier (?).

				} // End of switch (c).

				break;

			case Token.LITERAL_NUMBER_DECIMAL_INT:

				switch (c) {

				case ' ':
				case '\t':
					addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
					currentTokenStart = i;
					currentTokenType = Token.WHITESPACE;
					break;

				case '"':
					addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
					currentTokenStart = i;
					currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					break;

				default:

					if (RSyntaxUtilities.isDigit(c)) {
						break; // Still a literal number.
					}

					// Otherwise, remember this was a number and start over.
					addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
					i--;
					currentTokenType = Token.NULL;

				} // End of switch (c).

				break;

			case Token.COMMENT_EOL:
				i = end - 1;
				addToken(text, currentTokenStart, i, currentTokenType, newStartOffset + currentTokenStart);
				// We need to set token type to null so at the bottom we don't
				// add one more token.
				currentTokenType = Token.NULL;
				break;

			case Token.LITERAL_STRING_DOUBLE_QUOTE:
				if (c == '"') {
					addToken(text, currentTokenStart, i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset + currentTokenStart);
					currentTokenType = Token.NULL;
				}
				break;

			} // End of switch (currentTokenType).

		} // End of for (int i=offset; i<end; i++).

		switch (currentTokenType) {

		// Remember what token type to begin the next line with.
		case Token.LITERAL_STRING_DOUBLE_QUOTE:
			addToken(text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart);
			break;

		// Do nothing if everything was okay.
		case Token.NULL:
			addNullToken();
			break;

		// All other token types don't continue to the next line...
		default:
			addToken(text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart);
			addNullToken();

		}

		// Return the first token in our linked list.
		return firstToken;

	}
}

package textEditor;

import javax.swing.text.DefaultStyledDocument;

/**
 * 
 * @author Maxime Gallais-Jimenez
 *
 * Class to give style to Document
 */
@SuppressWarnings("serial")
public class StyledDocument extends DefaultStyledDocument {

	protected boolean inlineComment = false;
	protected boolean multilineComment = false;
	
	/**
	 * Find the index of the end of the text
	 * @param text Text to explore
	 * @param index Offset
	 * @return index of the last character of the text
	 */
	protected int findLastNonWordChar(String text, int index) {
		while (--index >= 0) {
			if (String.valueOf(text.charAt(index)).matches("\\W")) {
				break;
			}
		}
		return index;
	}

	/**
	 * Find the index of the begin of the text
	 * @param text Text to explore
	 * @param index Offset
	 * @return index of the first character of the text
	 */
	protected int findFirstNonWordChar(String text, int index) {
		while (index < text.length()) {
			if (String.valueOf(text.charAt(index)).matches("\\W")) {
				break;
			}
			index++;
		}
		return index;
	}
}

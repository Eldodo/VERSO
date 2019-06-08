package textEditor.JavaEditor;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import textEditor.StyledDocument;
import textEditor.JavaEditor.JavaConstants;

@SuppressWarnings("serial")
public class JavaStyledDocument extends StyledDocument {
	final StyleContext context = StyleContext.getDefaultStyleContext();
	final AttributeSet defaultBlackAttr = context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
	final AttributeSet commentAttr = context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.GRAY);
	private HashMap<String, AttributeSet> colorMap;
	
	public JavaStyledDocument() {
		super();
		colorMap = new HashMap<String, AttributeSet>();
		colorMap.put(JavaConstants.operatorsJava, context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.DARK_GRAY));
		colorMap.put(JavaConstants.valuesJava, context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.BLUE));
		colorMap.put(JavaConstants.typesJava, context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.BLUE));
		colorMap.put(JavaConstants.expressionsJava, context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.PINK));
		colorMap.put(JavaConstants.statementsJava, context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.ORANGE));
		colorMap.put(JavaConstants.declarationsJava, context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.RED));
		colorMap.put(JavaConstants.modifiersJava, context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.MAGENTA));
		colorMap.put(JavaConstants.punctuationsJava, context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.GRAY));
		colorMap.put(JavaConstants.specialsJava, context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.GREEN));
	}
	
	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
		super.insertString(offset, str, a);

		String text = getText(0, getLength());
		int before = findLastNonWordChar(text, offset);
		if (before < 0)
			before = 0;
		int after = findFirstNonWordChar(text, offset + str.length());
		int wordL = before;
		int wordR = before;
		boolean match = false;
		
		while (wordR <= after) {
			if (wordR == after || String.valueOf(text.charAt(wordR)).matches("\\W")) {
				if(wordR < after && text.charAt(wordR) == '/') {
					if(wordR < after && text.charAt(wordR+1) == '/') {
						inlineComment = true;
					}else if(wordR < after && text.charAt(wordR+1) == '*') {
						multilineComment = true;
					}
				}					
				if(wordR < after && inlineComment && text.charAt(wordR) == '\n') {
					setCharacterAttributes(wordL, wordR - wordL, commentAttr, false);
					inlineComment = false;
				}else if(wordR < after && multilineComment && text.charAt(wordR) == '*' && text.substring(wordR).length() > 1 && text.charAt(wordR+1) == '/') {
					setCharacterAttributes(wordL, wordR - wordL, commentAttr, false);
					multilineComment = false;
				}else if(!multilineComment && !inlineComment) {
					for(String keywords : colorMap.keySet()) {
						if (text.substring(wordL, wordR).matches("(\\W)*("+keywords+")")) {
							setCharacterAttributes(wordL, wordR - wordL, colorMap.get(keywords), false);
							match = true;
							break;
						}
					}
					if(!match)
						setCharacterAttributes(wordL, wordR - wordL, defaultBlackAttr, false);
				}else if(multilineComment || inlineComment) {
					setCharacterAttributes(wordL, wordR - wordL, commentAttr, false);
				}
				wordL = wordR;
				match = false;
			}
			wordR++;
		}
	}

	public void remove(int offs, int len) throws BadLocationException {
		super.remove(offs, len);

		String text = getText(0, getLength());
		int before = findLastNonWordChar(text, offs);
		if (before < 0)
			before = 0;
		int after = findFirstNonWordChar(text, offs);
		boolean match = false;
		
		for(String keywords : colorMap.keySet()) {
			if (text.substring(before, after).matches("(\\W)*("+keywords+")")) {
				setCharacterAttributes(before, after - before, colorMap.get(keywords), false);
				match = true;
				break;
			}
		}
		if(!match)
			setCharacterAttributes(before, after - before, defaultBlackAttr, false);
	}
}
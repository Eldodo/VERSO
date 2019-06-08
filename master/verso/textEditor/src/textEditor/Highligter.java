package textEditor;

import java.awt.Color;
import java.util.HashSet;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * 
 * @author Maxime Gallais-Jimenez
 *
 * Class to highlight different keywords
 * 
 * Used in verso project to highlight concepts keywords
 */
public class Highligter {

	HashSet<String> keywords;
	
	JTextComponent textComp;
	
	DefaultHighlighter.DefaultHighlightPainter highlighterFull = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
	DefaultHighlighter.DefaultHighlightPainter highlighterIncomplete = new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN);
		
	public Highligter(HashSet<String> keywords, JTextComponent comp) {
		this.keywords = keywords;
		this.textComp = comp;
		highlight();
	}

	/**
	 * Highlight the keywords
	 */
	private void highlight() {
		try {
			Highlighter hilite = textComp.getHighlighter();
            Document doc = textComp.getDocument();
            String text = doc.getText(0, doc.getLength());
            for(String keyword : keywords) {
            	int pos = 0;
            	while((pos = text.indexOf(keyword, pos)) >= 0) {
            		if(text.substring(pos+keyword.length(), pos+keyword.length()+1).matches("\\W")) {
            			hilite.addHighlight(pos, pos+keyword.length(), highlighterFull);
            		} else {
            			hilite.addHighlight(pos, pos+keyword.length(), highlighterIncomplete);
            		}
            		pos += keyword.length();
            	}
            }
		} catch(BadLocationException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Remove highlights
	 */
	public void removeHighlights() {
        Highlighter hilite = textComp.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (int i = 0; i < hilites.length; i++) {
            if (hilites[i].getPainter() instanceof Highlighter) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }
}

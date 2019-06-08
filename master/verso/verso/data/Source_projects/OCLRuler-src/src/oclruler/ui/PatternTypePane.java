package oclruler.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import oclruler.rule.MMMatch;
import oclruler.rule.PatternFactory.PatternType;
import oclruler.rule.patterns.Pattern;

public class PatternTypePane extends JPanel {
	private static final long serialVersionUID = 2962723264154766525L;
	
	RSyntaxTextArea textArea;
	PatternType patternType;
	public PatternTypePane() {
		super(new BorderLayout());
		
		textArea = new RSyntaxTextArea(20, 100);
		textArea.setEditable(false);
		textArea.setPopupMenu(null);
		add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
	}
	
	public void setPatternType(PatternType patternType) {
		this.patternType = patternType;
		updateTextPane();
	}

	private void updateTextPane() {
		ArrayList<MMMatch> matches = Pattern.getMatches(patternType.getInstanciationClass());
		String text = "Matches for "+patternType.getName()+":\n";
		for (MMMatch mmMatch : matches) {
			text+= mmMatch.toString() + "\n";
		}
		textArea.setText(text);
		textArea.setCaretPosition(0);
	}
	

}

package oclruler.ui;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

import oclruler.genetics.Oracle;
import oclruler.metamodel.Model;
import oclruler.rule.Program;

public class OCLTextArea extends RSyntaxTextArea {
	private static final long serialVersionUID = 8684624227040826958L;
	private Program selectedProgram;
	
	public OCLTextArea() {
		super();
		setEditable(false);
		FoldParserManager.get().addFoldParserMapping("OCL", new OCLFiresFoldParser());
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("OCL", "oclruler.ui.OCLTokenMaker");
		setSyntaxEditingStyle("OCL");
		setPopupMenu(null);
		setCodeFoldingEnabled(true);
	}

	public void setProgram(Program prg) {
		if(prg == null)
			prg = Oracle.getInstance();
			
		selectedProgram = prg;
		setText(selectedProgram.printOCL());
		setCaretPosition(0);
	}
	
	
	public void setProgram(Program prg, Model m) {
		if(prg == null)
			prg = Oracle.getInstance();
		
		selectedProgram = prg;
		String text = selectedProgram.getFitnessVector().printFires(m);
		setText(text);
		setCaretPosition(0);
		repaint();
	}
}

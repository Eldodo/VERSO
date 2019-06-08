package textEditor;

import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * 
 * @author Maxime Gallais-Jimenez
 *
 * Class to represent the Pane where the text is edited
 */

@SuppressWarnings("serial")
public class EditorPane extends JTextPane implements UndoableEdit {

	protected UndoManager undoManager = new UndoManager();
//	protected UndoAction undo;
	
	public EditorPane() {
		super();
	}

	public EditorPane(StyledDocument doc) {
		super(doc);
	}

	//UndoableEdit implementation
	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		return undoManager.addEdit(anEdit);
	}

	@Override
	public boolean canRedo() {
		return undoManager.canRedo();
	}

	@Override
	public boolean canUndo() {
		return undoManager.canUndo();
	}

	@Override
	public void die() {
		undoManager.die();
	}

	@Override
	public String getPresentationName () {
		return undoManager.getPresentationName();
	}

	@Override
	public String getRedoPresentationName() {
		return undoManager.getRedoPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return undoManager.getUndoPresentationName();
	}

	@Override 
	public boolean isSignificant() {
		return undoManager.isSignificant();
	}

	@Override
	public void redo() {
		if(!canRedo())
			throw new CannotRedoException();
		undoManager.redo();
	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return undoManager.replaceEdit(anEdit);
	}

	@Override
	public void undo() {
		if(!canUndo()) 
			throw new CannotUndoException();
		undoManager.undo();
	}

}

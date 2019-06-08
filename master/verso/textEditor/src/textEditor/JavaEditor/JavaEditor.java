package textEditor.JavaEditor;

import java.io.File;

import textEditor.TextEditor;

/**
 * 
 * @author Maxime Gallais-Jimenez
 * 
 * Java code editor
 * 
 * TODO Not working and not useful... To delete
 */
@SuppressWarnings("serial")
public class JavaEditor extends TextEditor {

	public JavaEditor() {
		document = new JavaStyledDocument();
		init();
	}
	
	public JavaEditor(String path) {
		document = new JavaStyledDocument();
		init();
		setFile(path);
	}
	
	public JavaEditor(File file) {
		document = new JavaStyledDocument();
		init();
		setFile(file);
	}
	
}

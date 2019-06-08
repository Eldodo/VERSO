package textEditor;

import java.util.HashSet;

//import textEditor.JavaEditor.JavaEditor;

public class Main {

	public static void main(String[] args) {
		TextEditor te = new TextEditor("C:\\Users\\mgall\\OneDrive\\Bureau\\Toto.java");
		String[] keywords = {"public", "static", "void"};
		HashSet<String> khs = new HashSet<String>();
		for(String k : keywords) {
			khs.add(k);
		}
		te.highlight(khs);
	}

}

package verso.view.helper;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ca.umontreal.iro.utils.Config;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

@SuppressWarnings("serial")
public class MenuHelper implements Initializable {

	private @FXML TextArea textHelp;
	String name;
	File file;
	
	String content = "";
	private static final String noContent = "Sorry no content available for this shortcut.";
	
	public MenuHelper(String name) {
		this(name, Config.helperPath+name+".txt");
	}
	
	public MenuHelper(String name, String path) {
		this(name, new File(path));
	}
	
	public MenuHelper(String name, File file) {
		this.name = name;
		this.file = file;
		try {
			//BufferedReader in = new BufferedReader(new FileReader(file));
			BufferedReader in = new BufferedReader(new InputStreamReader(
	                  new FileInputStream(file), "UTF8"));
			
			String line;
			while((line = in.readLine()) != null) {
				content += line+"\n";
			}
			in.close();
			
		}
		catch(Exception e) {e.printStackTrace();}
	}
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		textHelp.setText(content);
	}
	
	
}

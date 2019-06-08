package verso.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.lucene.search.TopDocs;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import verso.representation.cubeLandscape.Lucene.LuceneReadIndexFilter;
import verso.representation.cubeLandscape.Lucene.LuceneWriteIndexFilter;
import verso.representation.cubeLandscape.filter.SearchFilter;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;

public class LuceneInfoSearch extends LuceneSearch{
	
	public LuceneInfoSearch(String index, FilterView v) {  
		super(index, v);
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//Initialisation de la zone de recherche
		searchIndex.setDisable(contentSearched.getText().equals(""));
		contentSearched.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
		    	searchIndex.setDisable(contentSearched.getText().equals(""));
				//System.out.println(contentSearched.getText());
		    }
		});
		
		//Initialisation du bouton de recherche
		searchIndex.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				search(contentSearched.getText(),  SearchFilter.ALL_FILE);
			}
		});
		
		//Initialisation du bouton de réindexation
		reindexBt.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				SearchFilter.resetIndex(path);
				updateIndexInfo();
			}
		});
		
		//Inisialisation de l'information du l'indexation
		updateIndexInfo();
	}



	



}

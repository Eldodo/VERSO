package verso.view;


import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import verso.model.Filter;
import verso.representation.cubeLandscape.filter.SearchFilter;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;

public class LuceneNameSearch extends LuceneSearch{
	
	
	@FXML private RadioButton classRB, methodRB, allRB, allsystemRB, currentRB;
	
	public LuceneNameSearch(String index, FilterView v) {    
		super(index, v);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//Initialisation des RadioButtons Niveau
		final ToggleGroup group1 = new ToggleGroup();
		classRB.setToggleGroup(group1);
		classRB.setSelected(true);
		methodRB.setToggleGroup(group1);
		allRB.setToggleGroup(group1);
		
		//Initialisation des RadioButtons Set
		final ToggleGroup group2 = new ToggleGroup();
		currentRB.setToggleGroup(group2);
		currentRB.setSelected(true);
		allsystemRB.setToggleGroup(group2);
		
		//Initialisation de la zone de recherche
		searchIndex.setDisable(contentSearched.getText().equals(""));
		contentSearched.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
		    	searchIndex.setDisable(contentSearched.getText().equals(""));
				System.out.println(contentSearched.getText());
		    }
		});

		//Initialisation du bouton de recherche
		searchIndex.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				String level="";
				if(classRB.isSelected()) level = SearchFilter.CLASS_NAME;
				else if(methodRB.isSelected()) level = SearchFilter.METHOD_NAME;
				else if(allRB.isSelected()) level = SearchFilter.ALL_NAME;
				search(contentSearched.getText(), level);
				
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

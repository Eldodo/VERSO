package verso.view;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import verso.model.Filter;
import verso.representation.cubeLandscape.filter.SearchFilter;

public abstract class LuceneSearch implements Initializable{
	
	protected String path;
	protected FilterView view;
	@FXML protected Text lastUpdateTxt;
	@FXML protected Button reindexBt, searchIndex;
	@FXML protected TextField contentSearched;
	
	public LuceneSearch(String index, FilterView v) {
		this.path = index;
		this.view = v;
	}
	
	protected void updateIndexInfo() {
		File indexFolder = new File(path+"\\indexedFiles");
		if(indexFolder.exists()) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			this.lastUpdateTxt.setText("Dernière mise à jour : "+ sdf.format(indexFolder.lastModified()));
		}
		else {
			this.lastUpdateTxt.setText("Jamais indexé.");
		}
	}

	protected void search(String content, String level) {
		//On éffectue la recherche
		SearchFilter sf = SearchFilter.getInstance(path);
		sf.search(content, level);
		updateIndexInfo();
		confirmationDialog(sf);
	}
	
	protected void confirmationDialog(SearchFilter sf) {
		
		String result = sf.toString();
		
		//Initialisation de la fenêtre
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Création du filtre");
		alert.setHeaderText("Voici le résultat de votre recherche");
		
		//Ajout des boutons
		alert.getDialogPane().getButtonTypes().clear();
		ButtonType loginButtonType = new ButtonType("Ajouter", ButtonData.OK_DONE);
		alert.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
		Node loginButton = alert.getDialogPane().lookupButton(loginButtonType);
		loginButton.setDisable(true);
		


		Label label = new Label("Détails de la recherche");
		Label error = new Label("Erreur: Veuillez entrer un nom.");
		error.setTextFill(Color.RED);
		// Create the field
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		TextField field = new TextField();
		field.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
		    	if(field.getText().equals("")) {
		    		loginButton.setDisable(true);
		    		error.setText("Erreur: Veuillez entrer un nom.");
		    	}
		    	else if(view.nameAlreadyExist(field.getText())) {
		    		loginButton.setDisable(true);
		    		error.setText("Erreur: Le nom existe déjà.");
		    	}
		    	else {
		    		loginButton.setDisable(false);
		    		error.setText("");
		    	}
		    }
		});
		grid.add(new Label("Nom du Filtre: "), 0, 0);
		grid.add(field, 1, 0);
		
		//Create the result
		TextArea textArea = new TextArea(result);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(error, 0, 0);
		expContent.add(grid, 0, 1);
		expContent.add(label, 0, 2);
		expContent.add(textArea, 0, 3);
	

		// Set expandable Exception into the dialog pane.
		//alert.getDialogPane().setExpandableContent(expContent);
		alert.getDialogPane().setContent(expContent);
		
		Optional<ButtonType> r = alert.showAndWait();
		if(!r.isPresent()) {
			// alert is exited, no button has been pressed.
		}
		else if(r.get() == loginButtonType) {
			view.addFilter(new Filter(field.getText(), view.getSceneLandscape().getSysRep().getEntityRepFromString(SearchFilter.getResultsPath())));
			close();
		}
		else if(r.get() == ButtonType.CANCEL) {
			
		}
	}
	
	protected  void close() {
		Stage stage = (Stage) lastUpdateTxt.getScene().getWindow();
	    stage.close();
	}
	
}

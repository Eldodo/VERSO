package verso.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Optional;
import org.zeroturnaround.zip.ZipUtil;

import ca.umontreal.iro.utils.Config;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import verso.Main;
import verso.model.Filter;
import verso.representation.cubeLandscape.SceneLandscape;
import verso.representation.cubeLandscape.representationModel.EntityRepresentation;
import verso.reverse.RunUMLParser;
import verso.reverse.main.Counselor;
import verso.view.listview.FilterCell;
import verso.view.listview.FilterCellFactory;

public class FilterView extends BorderPane{
	public static int selectionCpt = 0;
	private SceneLandscape main;
	private ArrayList<Filter> filters;
	private @FXML ListView<Filter> filterList;
	private @FXML MenuButton addButton;
	private @FXML TreeView<String> treeviewFilter;
	private @FXML ListView<String> resultList;
	private @FXML Button plusButton, minusButton, unionButton, interButton, diffButton, diffsymButton;
	private @FXML Label nbrResult;

	public FilterView(SceneLandscape m) {
		filters = new ArrayList<Filter>();
		main = m;
		
		FXMLLoader fxmlLoader = new FXMLLoader(FilterView.class.getResource("FilterView.fxml"));
	    fxmlLoader.setRoot(this);
	    fxmlLoader.setController(this);
	    try {
	        fxmlLoader.load();
	    } catch (IOException exception) {
	        throw new RuntimeException(exception);
	    }
		
		this.setOperationButtonsDisabled(true);
		filterList.setCellFactory(new FilterCellFactory(this));
		filterList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Filter>() {
		    @Override
		    public void changed(ObservableValue<? extends Filter> observable, Filter oldValue, Filter newValue) {
		    	Platform.runLater(new Runnable() {
		    		@Override public void run() {
				    	if(newValue!=null) {
					        System.out.println("ListView selection changed from oldValue = " + oldValue + " to newValue = " + newValue);
					        updateInspector(newValue);
					        resultList.getItems().clear();
					        for(EntityRepresentation result : newValue.getClassList()) {
					        	resultList.getItems().add(result.getSimpleName());
					        	System.out.println(result.getSimpleName());
					        }
					        setOperationButtonsDisabled(false);
					        nbrResult.setText("Nombre de résultat(s): "+newValue.getClassList().size());
				    	}
				    	else {
				    		nbrResult.setText("");
				    		setOperationButtonsDisabled(true);
				    		resultList.getItems().clear();
					        treeviewFilter.setRoot(null);
				    	}
		    	}});
		    }
		});
		treeviewFilter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> old_val, TreeItem<String> new_val) {
            	Platform.runLater(new Runnable() {
            	    @Override public void run() {
            	    	//filterList.getSelectionModel().clearSelection();
            	    	//reloadList();
            	}});
            	//filterList.getSelectionModel().clearSelection();
            }

        });
		addButton.getItems().clear();
		MenuItem infoMenu = new MenuItem("Recherche d'informations");
		MenuItem nomMenu = new MenuItem("Recherche dans les noms");
		MenuItem addSelectionMenu = new MenuItem("Ajouter la selection");
		addButton.getItems().addAll(infoMenu, nomMenu, addSelectionMenu);
		
		infoMenu.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	launchSearchView();
		    }
		});
		nomMenu.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	launchNameSearchView();
		    }
		});
		
		addSelectionMenu.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	ArrayList<EntityRepresentation> selected = new ArrayList<EntityRepresentation>();
	    		for(EntityRepresentation e:main.getSysRep().selectedElements)
	    			selected.add(e);
		    	if(main.isCurrentlyFiltering()) {
		    		ArrayList<EntityRepresentation> targets = main.getSysRep().getUnfiltredEntityRepresentation();
		    		targets.removeAll(selected);
		    		createFilterSelection(selected, targets);
		    	}
		    	else {
		    		createFilterSelection(selected);
		    	}
		    	
		    }
		});
		
		unionButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	composeFilterDialog(Filter.UNION);
		    }
		});
		plusButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	composeFilterDialog(Filter.PLUS);
		    }
		});
		minusButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	composeFilterDialog(Filter.MINUS);
		    }
		});
		interButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	composeFilterDialog(Filter.INTERSECTION);
		    }
		});
		diffButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	composeFilterDialog(Filter.DIFFERENCE);
		    }
		});
		diffsymButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	composeFilterDialog(Filter.DIFFERENCE_SYMETRIQUE);
		    }
		});
	}
	
	public SceneLandscape getSceneLandscape() {
		return this.main;
	}
	
	private void setOperationButtonsDisabled(boolean b) {
		plusButton.setDisable(b);
		minusButton.setDisable(b);
		unionButton.setDisable(b);
		interButton.setDisable(b);
		diffButton.setDisable(b);
		diffsymButton.setDisable(b);
	}
	
	private void updateInspector(Filter f) {
		TreeItem<String> root = getItem(f); 
        treeviewFilter.setRoot(root);
	}
	
	private TreeItem<String> getItem(Filter f){
		TreeItem<String> root = new TreeItem<String>(f.toString());
		root.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/filter.png"),30,30,false, false)));
		if(f.hasChildren()) {
        	TreeItem<String> op = new TreeItem<String>(f.getOperator());
        	if(f.getOperator().equals(Filter.UNION))
        		op.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/union.png"),30,30,false, false)));
        	else if(f.getOperator().equals(Filter.DIFFERENCE))
        		op.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/div.png"),30,30,false, false)));
        	else if(f.getOperator().equals(Filter.DIFFERENCE_SYMETRIQUE))
        		op.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/divsym.png"),30,30,false, false)));
        	else if(f.getOperator().equals(Filter.INTERSECTION))
        		op.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/inter.png"),30,30,false, false)));
        	else if(f.getOperator().equals(Filter.MINUS))
        		op.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/minus.png"),30,30,false, false)));
        	else if(f.getOperator().equals(Filter.PLUS))
        		op.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/plus.png"),30,30,false, false)));
        	root.getChildren().add(op);
        	TreeItem<String> c1 = getItem(f.getChild1());
        	TreeItem<String> c2 = getItem(f.getChild2());
        	op.getChildren().add(c1);
        	op.getChildren().add(c2);
        	op.setExpanded(true);
        }
		root.setExpanded(true);
		return root;
	}
	
	public void filterToUML() {
		Counselor.resetInstance();
		System.out.println("Liste:");
		Filter f = this.filterList.getSelectionModel().getSelectedItem();
		String zipOutput = "tmpReverse.zip";
		String folderOutput = "./tmpReverse/";
		File folder = new File(folderOutput);
		if(folder.exists()) {
			if(!folder.delete()) {//folder is not empty, we need to clean it 
				for(File file : folder.listFiles()) {
					file.delete();
				}
			}
		}
		folder.mkdir();
		for(EntityRepresentation e : f.getClassList()) {
			String entirePath = e.getPath(Config.srcFolderPath);
			File src = new File(entirePath);
			File dest = new File(folderOutput+entirePath.substring(entirePath.lastIndexOf("/")));
			
			try {
				Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
				System.out.println(src.toPath() + " copié dans "+dest.toPath());
			} catch (IOException e1) {
				System.out.println("Le .java est introuvable");
			}
		}
		ZipUtil.pack(new File(folderOutput), new File(zipOutput));
		
		RunUMLParser uml = new RunUMLParser(zipOutput, "test.png");
	}

	/**
	 * Add a filter
	 * @param f
	 */
	public void addFilter(Filter f) {
		filters.add(f);
		this.reloadList();
	}
	
	/**
	 * Rename a filter
	 * @param f
	 */
	public void deleteFilter(Filter f, ArrayList<Filter> fathers) {
		for(Filter ftr : fathers)
			ftr.flat();
		filters.remove(f);
		this.reloadList();
	}
	
	/**
	 * Rename a filter
	 * @param f
	 * @param n
	 */
	public void renameFilter(Filter f, String n) {
		filters.get(filters.indexOf(f)).setName(n);
		this.reloadList();
	}
	
	/**
	 * Flat a filter
	 * @param f
	 */
	public void flatFilter(Filter f) {
		filters.get(filters.indexOf(f)).flat();
		this.reloadList();
	}
	
	/**
	 * Duplicate a filter
	 * @param f
	 * @param n
	 */
	public void duplicateFilter(Filter f, String n) {
		filters.add(filters.get(filters.indexOf(f)).duplicate(n));
		this.reloadList();
	}
	
	/**
	 * Reload the filter list
	 */
	public void reloadList() {
		filterList.getItems().clear();
		for(Filter f : this.filters) {
			filterList.getItems().add(f);
		}
	}
	
	/**
	 * Check if the name of the filter is already given
	 * @param n Name
	 * @return True if the name already exist, else false
	 */
	public boolean nameAlreadyExist(String n) {
		boolean ret = false;
		for(Filter f : this.filters) {
			if(f.getName().equals(n)) {
				ret = true;
				break;
			}
		}
		return ret;
	}
	
	
	/**
	 * 
	 * 
	 * 		Launch the filter views
	 * 
	 * 
	 */
	
	/**
	 * Dialog for Info search
	 */
	private void launchSearchView() {
		try {
			FXMLLoader loader = new FXMLLoader(LuceneInfoSearch.class.getResource("LuceneInfoSearch.fxml"));
            LuceneInfoSearch controller = new LuceneInfoSearch(Main.sourceFolder.getAbsolutePath(), this);
            loader.setController(controller);
            Parent root = loader.load();
			Stage stage = new Stage();
	        stage.setTitle("Recherche d'information");
	        stage.setScene(new Scene(root));
	        stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Dialog for name search
	 */
	private void launchNameSearchView() {
		try {
			FXMLLoader loader = new FXMLLoader(LuceneInfoSearch.class.getResource("LuceneNameSearch.fxml"));
            LuceneNameSearch controller = new LuceneNameSearch(Main.sourceFolder.getAbsolutePath(), this);
            loader.setController(controller);
            Parent root = loader.load();
			Stage stage = new Stage();
	        stage.setTitle("Recherche dans les noms");
	        stage.setScene(new Scene(root));
	        stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void createFilterSelection(ArrayList<EntityRepresentation> selected) {
		Filter f = new Filter("Selection "+selectionCpt, selected);
		selectionCpt++;
		this.addFilter(f);
	}
	
	private void createFilterSelection(ArrayList<EntityRepresentation> selected, ArrayList<EntityRepresentation> targets) {
		Filter f = new Filter("Test", Filter.UNION, new Filter("Sources", selected), new Filter("Targets", targets));
		this.addFilter(f);
	}
	
	
	
	
	
	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 						DIALOGS
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	
	
	
	
	
	
	/**
	 * Dialog for deletion of filter
	 */
	public void deleteFilterDialog() {
		Filter f = filterList.getSelectionModel().getSelectedItem();
		this.deleteFilterDialog(f);
	}
	public void deleteFilterDialog(Filter f) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Supprimer un filtre");
		alert.setHeaderText("Supprimer le filtre "+f.getName());
		
		//Ajout des boutons
		alert.getDialogPane().getButtonTypes().clear();
		ButtonType deleteButtonType = new ButtonType("Supprimer", ButtonData.OK_DONE);
		alert.getDialogPane().getButtonTypes().addAll(deleteButtonType, ButtonType.CANCEL);
		Node renameButton = alert.getDialogPane().lookupButton(deleteButtonType);
		
		Label error = new Label("");
		error.setTextFill(Color.RED);
		
		//On regarde dans tous les filtres, ceux qui ont celui la comme fils
		//Car il faudra les applatir en cas de suppression
		ArrayList<Filter> fathers = new ArrayList<Filter>();
		for(Filter filter : this.filters) {
			if(filter.isParentOf(f))
				fathers.add(filter);
		}
		if(fathers.size()>0) {
			String er = "Attention !\n\""+f.getName()+"\" est utilisé dans la composition de ce(s) filtre(s):";
			for(int i=0; i<fathers.size();i++) {
				if(i!=0)er+=",";
				er+=" \""+fathers.get(i)+"\"";
			}
			er+="\nEn supprimant \""+f.getName()+"\", tous ces filtres seront applatis. Cela signifie qu'ils ne seront plus considérés comme des compositions de deux filtres. Leur résultat est cependant conservé.";
			error.setText(er);
		}
		
		
		
		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(error, 0, 0);
	

		// Set expandable Exception into the dialog pane.
		//alert.getDialogPane().setExpandableContent(expContent);
		alert.getDialogPane().setContent(expContent);
		
		Optional<ButtonType> r = alert.showAndWait();
		if(!r.isPresent()) {
			// alert is exited, no button has been pressed.
		}
		else if(r.get() == deleteButtonType) {
			deleteFilter(f, fathers);
		}
		else if(r.get() == ButtonType.CANCEL) {
			
		}
	}
	
	/**
	 * Dialog to duplicate a filter
	 */
	public void duplicateFilterDialog() {
		Filter f = filterList.getSelectionModel().getSelectedItem();
		this.duplicateFilterDialog(f);
	}
	public void duplicateFilterDialog(Filter f) {
		
		//Initialisation de la fenêtre
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Dupliquer un filtre");
		alert.setHeaderText("Vous êtes sur le point de dupliquer le filtre \""+f.getName()+"\". Cela signifie que vous aller créer un nouveau filtre composé de la même manière que celui ci.");
		alert.getDialogPane().setPrefWidth(600);
		//alert.setResizable(false);
		//Ajout des boutons
		alert.getDialogPane().getButtonTypes().clear();
		ButtonType duplicateButtonType = new ButtonType("Dupliquer", ButtonData.OK_DONE);
		alert.getDialogPane().getButtonTypes().addAll(duplicateButtonType, ButtonType.CANCEL);
		Node duplicateButton = alert.getDialogPane().lookupButton(duplicateButtonType);
		
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
		    		duplicateButton.setDisable(true);
		    		error.setText("Erreur: Veuillez entrer un nom.");
		    	}
		    	else if(nameAlreadyExist(field.getText())) {
		    		duplicateButton.setDisable(true);
		    		error.setText("Erreur: Le nom existe déjà.");
		    	}
		    	else {
		    		duplicateButton.setDisable(false);
		    		error.setText("");
		    	}
		    }
		});
		grid.add(new Label("Nom du Filtre: "), 0, 0);
		grid.add(field, 1, 0);
		

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(error, 0, 0);
		expContent.add(grid, 0, 1);
	

		// Set expandable Exception into the dialog pane.
		//alert.getDialogPane().setExpandableContent(expContent);
		alert.getDialogPane().setContent(expContent);
		
		Optional<ButtonType> r = alert.showAndWait();
		if(!r.isPresent()) {
		}
		else if(r.get() == duplicateButtonType) {
			duplicateFilter(f, field.getText());
		}
		else if(r.get() == ButtonType.CANCEL) {
			
		}
	}
	
	/**
	 * Dialog to flat a filter
	 */
	public void flatFilterDialog() {
		Filter f = filterList.getSelectionModel().getSelectedItem();
		this.flatFilterDialog(f);
	}
	public void flatFilterDialog(Filter f) {
		
		//Initialisation de la fenêtre
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Applatir un filtre");
		alert.setHeaderText("Vous êtes sur le point d'applatir le filtre \""+f.getName()+"\". Cela signifie que le filtre ne sera plus considéré comme une composition de deux filtres. Son résultat est cependant conservé.");
		alert.getDialogPane().setPrefWidth(600);
		//alert.setResizable(false);
		//Ajout des boutons
		alert.getDialogPane().getButtonTypes().clear();
		ButtonType flatButtonType = new ButtonType("Applatir", ButtonData.OK_DONE);
		alert.getDialogPane().getButtonTypes().addAll(flatButtonType, ButtonType.CANCEL);
		Node flatButton = alert.getDialogPane().lookupButton(flatButtonType);
		
		Label label = new Label("Détail du résultat");
		
		TextArea textArea = new TextArea(f.getClassListToString());
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);
		
		

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);
	

		// Set expandable Exception into the dialog pane.
		//alert.getDialogPane().setExpandableContent(expContent);
		alert.getDialogPane().setContent(expContent);
		
		Optional<ButtonType> r = alert.showAndWait();
		if(!r.isPresent()) {
		}
		else if(r.get() == flatButtonType) {
			flatFilter(f);
		}
		else if(r.get() == ButtonType.CANCEL) {
			
		}
	}
	
	/**
	 * Dialog to rename a filter
	 */
	public void renameFilterDialog() {
		Filter f = filterList.getSelectionModel().getSelectedItem();
		this.renameFilterDialog(f);
	}
	public void renameFilterDialog(Filter f) {
		//Initialisation de la fenêtre
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Renommer un filtre");
		alert.setHeaderText("Renommer le filtre "+f.getName());
		
		//Ajout des boutons
		alert.getDialogPane().getButtonTypes().clear();
		ButtonType renameButtonType = new ButtonType("Renommer", ButtonData.OK_DONE);
		alert.getDialogPane().getButtonTypes().addAll(renameButtonType, ButtonType.CANCEL);
		Node renameButton = alert.getDialogPane().lookupButton(renameButtonType);
		renameButton.setDisable(true);
		
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
		    		renameButton.setDisable(true);
		    		error.setText("Erreur: Veuillez entrer un nom.");
		    	}
		    	else if(nameAlreadyExist(field.getText())) {
		    		renameButton.setDisable(true);
		    		error.setText("Erreur: Le nom existe déjà.");
		    	}
		    	else {
		    		renameButton.setDisable(false);
		    		error.setText("");
		    	}
		    }
		});
		grid.add(new Label("Nom du Filtre: "), 0, 0);
		grid.add(field, 1, 0);
		

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(error, 0, 0);
		expContent.add(grid, 0, 1);
	

		// Set expandable Exception into the dialog pane.
		//alert.getDialogPane().setExpandableContent(expContent);
		alert.getDialogPane().setContent(expContent);
		
		Optional<ButtonType> r = alert.showAndWait();
		if(!r.isPresent()) {
			// alert is exited, no button has been pressed.
		}
		else if(r.get() == renameButtonType) {
			renameFilter(f, field.getText());
		}
		else if(r.get() == ButtonType.CANCEL) {
			
		}
	}
	
	
	/**
	 * Dialog to compose a filter
	 * @param op
	 */
	public void composeFilterDialog(String op) {
		Filter f = filterList.getSelectionModel().getSelectedItem();
		this.composeFilterDialog(f, op);
	}
	public void composeFilterDialog(Filter f, String op) {
		//Initialisation de la fenêtre
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Composition de filtre");
		alert.setHeaderText("Création d'un nouveau filtre composé");
		
		//Ajout des boutons
		alert.getDialogPane().getButtonTypes().clear();
		ButtonType addButtonType = new ButtonType("Ajouter", ButtonData.OK_DONE);
		alert.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
		Node addButton = alert.getDialogPane().lookupButton(addButtonType);
		addButton.setDisable(true);
		
		Label error = new Label("Composé des filtres \""+f.getName()+"\" et ");
		error.setTextFill(Color.RED);
		
		ChoiceBox<Filter> choiceFilter = new ChoiceBox<Filter>();
		choiceFilter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Filter>() {
			@Override
			public void changed(ObservableValue<? extends Filter> observable, Filter oldValue, Filter newValue) {
				if(newValue==null) {
					addButton.setDisable(true);
				}
				else {
					addButton.setDisable(!error.getText().equals(""));
				}
			}
		});
		
		// Create the field
		GridPane gridName = new GridPane();
		gridName.setHgap(10);
		gridName.setVgap(10);
		gridName.setPadding(new Insets(20, 150, 10, 10));
		TextField field = new TextField();
		field.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
		    	if(field.getText().equals("")) {
		    		addButton.setDisable(true);
		    		error.setText("Erreur: Veuillez entrer un nom.");
		    	}
		    	else if(nameAlreadyExist(field.getText())) {
		    		addButton.setDisable(true);
		    		error.setText("Erreur: Le nom existe déjà.");
		    	}
		    	else {
		    		if(choiceFilter.getSelectionModel().getSelectedItem()!=null)
		    			addButton.setDisable(false);
		    		error.setText("");
		    	}
		    }
		});
		gridName.add(new Label("Nom du Filtre: "), 0, 0);
		gridName.add(field, 1, 0);
		
		//Create the chooser
		// Create the field
		GridPane gridChoose = new GridPane();
		gridChoose.setHgap(10);
		gridChoose.setVgap(10);
		gridChoose.setPadding(new Insets(20, 150, 10, 10));
		
		choiceFilter.getItems().clear();
		for(Filter tmp : this.filters) {
			if(tmp!=f && !f.isASonOf(tmp)) {
				choiceFilter.getItems().add(tmp);
			}
		}
		
		gridChoose.add(new Label("Composé des filtres \""+f.getName()+"\" et "), 0, 0);
		gridChoose.add(choiceFilter, 1, 0);
		
		
		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(error, 0, 0);
		expContent.add(gridName, 0, 1);
		expContent.add(gridChoose, 0, 2);
	

		// Set expandable Exception into the dialog pane.
		//alert.getDialogPane().setExpandableContent(expContent);
		alert.getDialogPane().setContent(expContent);
		
		Optional<ButtonType> r = alert.showAndWait();
		if(!r.isPresent()) {
			// alert is exited, no button has been pressed.
		}
		else if(r.get() == addButtonType) {
			//renameFilter(f, field.getText());
			this.addFilter(new Filter(field.getText(), op, f, choiceFilter.getValue()));
		}
		else if(r.get() == ButtonType.CANCEL) {
			
		}
	}
	
	// method for showing the content of a filter
	public void showSelection(FilterCell f){
		ArrayList<EntityRepresentation> selection = filters.get(filters.indexOf(f.getItem())).getClassList();
		for(EntityRepresentation e: selection){
			e.select();
		}
		main.getSysRep().setDisplayLinks(false);
		main.redisplay();
		main.getSysRep().setDisplayLinks(true);
		
	}
	
	
}

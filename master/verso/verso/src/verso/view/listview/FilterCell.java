package verso.view.listview;

import java.io.IOException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import verso.model.Filter;
import verso.view.FilterView;

public class FilterCell extends ListCell<Filter> {

    private @FXML Label nameLabel;
    private @FXML MenuButton threeDotsMenu;
    private FilterView view;

    public FilterCell(FilterView v) {
    	view = v;
        loadFXML();
        threeDotsMenu.setVisible(false);
        threeDotsMenu.getItems().clear();
        
        MenuItem rename = new MenuItem("Renommer");
        rename.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				view.renameFilterDialog();
			}
		});
        MenuItem delete = new MenuItem("Supprimer");
        delete.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				view.deleteFilterDialog();
			}
		});
        
        MenuItem duplicate = new MenuItem("Dupliquer");
        duplicate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				view.duplicateFilterDialog();
			}
		});
        
        MenuItem flat = new MenuItem("Applatir");
        flat.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				view.flatFilterDialog();
			}
		});
        
        MenuItem show = new MenuItem("Montrer");
        show.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				view.showSelection(FilterCell.this);
			}
		});
        
        MenuItem uml = new MenuItem("Generer UML");
        uml.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				view.filterToUML();
			}
		});
        
        
        threeDotsMenu.getItems().addAll(rename, delete, duplicate, flat,show, uml);
        
        this.selectedProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(arg2!=arg1) {
					threeDotsMenu.setVisible(arg2);
				}
			}
        });
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FilterCell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(Filter item, boolean empty) {
        super.updateItem(item, empty);

        if(empty) {
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
        else {
            nameLabel.setText(item.getName());
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}
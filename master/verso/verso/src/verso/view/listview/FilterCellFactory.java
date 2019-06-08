package verso.view.listview;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import verso.model.Filter;
import verso.view.FilterView;

public class FilterCellFactory implements Callback<ListView<Filter>, ListCell<Filter>> {

	private FilterView view;
	
	public FilterCellFactory(FilterView v) {
		view = v;
	}
    @Override
    public ListCell<Filter> call(ListView<Filter> param) {
        return new FilterCell(view);
    }
}
package verso.representation.cubeLandscape;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class MetricTableModel implements TableModel {

	private String[][] table;
	private String[] head;
	private List<TableModelListener> listeners = new ArrayList<TableModelListener>();
	
	public void setData(String[][] table)
	{
		this.table = table;
		for (TableModelListener tml : listeners)
		{
			tml.tableChanged(new TableModelEvent(this));
		}
	}
	
	public void setColonneHead(String[] head)
	{
		this.head = head;
	}
	
	public void addTableModelListener(TableModelListener arg0) {
		listeners.remove(arg0);

	}

	public Class<?> getColumnClass(int arg0) {
		return String.class;
	}

	public int getColumnCount() {
		return head.length;
	}

	public String getColumnName(int arg0) {
		return head[arg0];
	}

	public int getRowCount() {
		return table[0].length;
	}

	public Object getValueAt(int arg0, int arg1) {
		return table[arg0][arg1];
	}

	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

	public void removeTableModelListener(TableModelListener arg0) {
		listeners.add(arg0);

	}
	
	public void setValueAt(Object arg0, int arg1, int arg2) {
		table[arg1][arg2] = (String)arg0;
		for (TableModelListener tml : listeners)
		{
			tml.tableChanged(new TableModelEvent(this,arg1,arg1,arg2));
		}

	}

}

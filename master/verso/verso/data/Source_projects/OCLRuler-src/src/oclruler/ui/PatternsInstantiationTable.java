package oclruler.ui;

import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import oclruler.rule.MMMatch;
import oclruler.rule.PatternFactory.PatternType;

public class PatternsInstantiationTable extends JTable    {
	private static final long serialVersionUID = 6064578200765931166L;


	static Color disabledColour = Color.LIGHT_GRAY;
	static Color unabledColour = new Color((int)( 0xf3dcde));
	static Color selectedColour = new Color((int)( 0xC2FAF1));
	static Color selectedDisabledColour = new Color((int)( 0xC2FAF1)).darker().darker();
	static List<Color> rowColours = Arrays.asList(
    		new Color((int)( 0xDFEFF0)),
    		new Color((int)( 0xF6F6F6))
    );

	
	public static final int COL_NAME = 0;
	public static final int COL_INSTANCES = 1;
	public static final int COL_ENABLED = 2;
	public static final int COL_NUMBER_OF_MATCHES = 3;
	String[] columnNames = {"Patterns", "Instantiations", "Enabled", "Nb. of matches"};
	Object[][] data = new Object[PatternType.values().length][columnNames.length];
	int[] matches ;
	private MyTableModel model;


	@SuppressWarnings("unused")
	private PatternsInstantiationPanel panel;
	
	public PatternsInstantiationTable(PatternsInstantiationPanel panel) {
		this.panel = panel;
		
		countPatternsMatches();

		model = new MyTableModel();
	    setModel(model);
	    getColumnModel().getColumn(0).setPreferredWidth(150);
	    getColumnModel().getColumn(1).setPreferredWidth(50);
	    getColumnModel().getColumn(2).setPreferredWidth(20);
	    getColumnModel().getColumn(3).setPreferredWidth(20);
	    
	    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); 
	    
	}
	private Border outside = new MatteBorder(1, 0, 1, 0, Color.RED);
	private Border inside = new EmptyBorder(0, 1, 0, 1);
	private Border highlight = new CompoundBorder(outside, inside);
	
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);
		JComponent jc = (JComponent)c;
		
		//  Alternate row color
		if(data[row][COL_NUMBER_OF_MATCHES] != null && (int)data[row][COL_NUMBER_OF_MATCHES] <= 0)
			jc.setBackground(disabledColour);
		else {
			Color color = row % 2 == 0 ? rowColours.get(0) : rowColours.get(1);
			if(!(boolean)data[row][COL_ENABLED])
				color = unabledColour;
			jc.setBackground(color);
		}
		
		// Add a border to the selected row
		if (isRowSelected(row))
			jc.setBorder( highlight );


		return c;
	}


	public void updateData() {
		int i = 0;
		for (PatternType pt : PatternType.values()) {
			data[i] = new Object[] { pt.getName(), pt.getInstantiations(), PatternType.enabledValues().contains(pt) && matches[i] > 0, matches[i] };
			i++;
		}
		//Update UI and keep selection
		int[] sel = getSelectedRows();
        model.fireTableDataChanged();
        for (int j=0; j<sel.length; j++)
            getSelectionModel().addSelectionInterval(sel[j], sel[j]);
	}
	
	@SuppressWarnings("unchecked")
	private int[] countPatternsMatches() {
		int i = 0;
		matches = new int[PatternType.values().length];
		for (PatternType pt : PatternType.values()) {
			ArrayList<MMMatch> matchlist;
			try {
				 matchlist = (ArrayList<MMMatch>)pt.getInstanciationClass().getMethod("getMatches").invoke(null, (Object[])null);
				 matches[i++] = matchlist.size(); 
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
		return matches;
	}
	
	class MyTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 3715492640070930972L;

		public MyTableModel() {
			super();
			((JComponent) getDefaultRenderer(Boolean.class)).setOpaque(true);
		}
	    
		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public boolean isCellEditable(int row, int col) {
			PatternType pt = PatternType.values()[row];
			if(pt == PatternType.A0_RawText)
				return false;
			return ((int)data[row][COL_NUMBER_OF_MATCHES]) > 0 && col == 2;//colomnNames[Enabled"]
		}

		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			PatternType pt = PatternType.values()[row];
			pt.enable((boolean)value);
//			fireTableCellUpdated(row, col);
			fireTableDataChanged();
		}
		
		 @Override
         public Class<?> getColumnClass(int columnIndex) {
             if(columnIndex == COL_ENABLED)//colomnNames[Enabled"]
                 return Boolean.class;
             return super.getColumnClass(columnIndex);
         }
		@Override
		public void fireTableDataChanged() {
			fireTableChanged(new TableModelEvent(this, // tableModel
					0, // firstRow
					getRowCount() - 1, // lastRow
					TableModelEvent.ALL_COLUMNS, // column
					TableModelEvent.UPDATE)); // changeType
		}
		

	}
	
//	class MyTableCellRenderer extends DefaultTableCellRenderer {
//		private static final long serialVersionUID = -3988659185138146957L;
//
//		@Override
//		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//			MyTableModel model = (MyTableModel) table.getModel();
//			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//			if(isSelected)
//				c.setBackground(selectedColour);
//			else
//				c.setBackground(model.getRowColour(row));
//			return c;
//		}
//	}
}

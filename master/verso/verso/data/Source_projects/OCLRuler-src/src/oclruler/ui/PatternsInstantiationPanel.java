package oclruler.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import oclruler.rule.PatternFactory.PatternType;

public class PatternsInstantiationPanel extends JPanel {
	private static final long serialVersionUID = -5270356526075804525L;
	
	PatternsInstantiationTable table;
	PatternTypePane details;
	
	
	public PatternsInstantiationPanel() {
		super(new BorderLayout());
		
		table = new PatternsInstantiationTable(this);
		JScrollPane scrollPane = new JScrollPane(table);
//		table.setFillsViewportHeight(true);
//		add(scrollPane, BorderLayout.CENTER);
		
		
		details = new PatternTypePane();
//		add(details, BorderLayout.SOUTH);
		
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
		    public void valueChanged(ListSelectionEvent lse) {
				if (!lse.getValueIsAdjusting() && table.getSelectedRow() != -1){
		        	PatternType pt = PatternType.get(table.getSelectedRow());
		            details.setPatternType(pt);
				}
		    }
		});
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, details);
		split.setDividerLocation(340);
		add(split, BorderLayout.CENTER);
	}

	public void updateTable() {
		table.updateData();
	}
}

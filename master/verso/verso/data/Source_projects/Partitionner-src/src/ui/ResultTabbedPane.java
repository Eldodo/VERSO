package ui;

import genetic.Entity;
import genetic.Gene;
import genetic.Population;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class ResultTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = 1L;
	
	ArrayList<ResultPanel> results;
	
	
	public ResultTabbedPane() {
		super();
		this.setTabPlacement(JTabbedPane.LEFT);
	}
	
	public void addResultPane(Entity e){
		ResultPanel rp = new ResultPanel(e);
		this.addTab(e.toString(), rp);
	}
	
	

	public void setResultingPop(Population p) {
		removeAll();
		p.orderEntitiesWithMonoValue();
		for (Entity e : p.getEntities()) {
			addResultPane(e);
		}
	}
	
	class ResultPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		JTextArea titleLabel;
		JButton buttonExportEntity;
		JTextArea textArea;
		Entity e;
		
		public ResultPanel(Entity en) {
			super(new BorderLayout());
			this.e = en;
			
			titleLabel = new JTextArea();
			titleLabel.setText(e.printStats());
			buttonExportEntity = new JButton("Export");
			buttonExportEntity.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					System.out.println("Exporting "+e);
					// Creer repertoire nomMS+timeStamp...
					//mettre les .log et .data.log
					//mettre une copie des xmi (serialize() from entity a overrider).
					
				}
			});
			textArea = new JTextArea();
			
			
			JPanel jpNorth = new JPanel(new BorderLayout());
			jpNorth.add(titleLabel, BorderLayout.CENTER);
			jpNorth.add(buttonExportEntity, BorderLayout.EAST);
			
			this.add(jpNorth, BorderLayout.NORTH);
			this.add(new JScrollPane(textArea), BorderLayout.CENTER);
			
			for (Gene m : e.getGenes()) {
				textArea.append(m.toString()+"\n");
			}
		}
	}

}

package oclruler.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import coocl.ocl.Contrainte;
import coocl.ocl.Program;
import oclruler.genetics.Evolutioner;
import oclruler.genetics.FitnessVector;
import oclruler.genetics.GeneticEntity;
import oclruler.genetics.Population;
import utils.Utils;

public class ResultTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = 1L;
	
	ArrayList<ResultPanel> results;
	
	
	public ResultTabbedPane() {
		super();
		this.setTabPlacement(JTabbedPane.LEFT);
	}
	
	public void addResultPane(GeneticEntity e){
		ResultPanel rp = new ResultPanel(e);
		this.addTab(e.toString(), rp);
	}
	
	@Override
	public Component getTabComponentAt(int index) {
		try{
			return super.getTabComponentAt(index);
		} catch (IndexOutOfBoundsException ioobe ){
			Ui.LOGGER.warning("Internal Swing exception.");
			return null;
		}
	}
	
	public void setResultingPop(Population p) {
		removeAll();
		
		GeneticEntity[] ges = new GeneticEntity[p.getEntities().size()];
		Arrays.sort(ges, GeneticEntity.getMonoValueComparator());
		
		p.getEntities().toArray(ges);
		for (GeneticEntity e : ges) {
			addResultPane(e);
		}
	}
	
	@Override
	public void paint(Graphics g) {
		try {
			super.paint(g);
		} catch (Exception e) {
			//Hiding SWT flaws.
			//TODO BEWARE
//			e.printStackTrace();
		}
	}
	
	class ResultPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		JTextArea titleLabel;
		JButton buttonExportEntity, buttonRefresh;
		JTextArea textArea;
		GeneticEntity e;
		
		public ResultPanel(GeneticEntity en) {
			super(new BorderLayout());
			this.e = en;
			
			titleLabel = new JTextArea();
			titleLabel.setText(en.printStats() + " - Mono = "+String.format ("%.02f",(float)e.getMonoValue()));
			buttonExportEntity = new JButton("Export");
			buttonExportEntity.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {//TODO Export
//					Ui.LOGGER.info("Exporting "+e);
					try {
						Evolutioner evo = Ui.getInstance().getEvolutioner();
						String fileName = evo.createFileOutNames()[0];
						evo.printProgram(new File(fileName), (Program)e);
						
					} catch (NullPointerException e1) {
						Ui.LOGGER.severe("An error occured. Could not write results.");
						e1.printStackTrace();
					}
//					Evolutioner.printResult(new BufferedWriter(new File(fileName), evo.getEvaluator());
					// Creer repertoire nomMS+timeStamp...
					//mettre les .log et .data.log
					//mettre une copie des xmi (serialize() from entity a overrider).
					
				}
			});
			buttonRefresh = new JButton("Refresh");
			buttonRefresh.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {//TODO Export
					Ui.getInstance().updateResultingPop();					
				}
			});
			textArea = new JTextArea();
			
			
			JPanel jpNorth = new JPanel(new BorderLayout());
			jpNorth.add(buttonRefresh, BorderLayout.WEST);
			jpNorth.add(titleLabel, BorderLayout.CENTER);
			jpNorth.add(buttonExportEntity, BorderLayout.EAST);
			
			this.add(jpNorth, BorderLayout.NORTH);
			this.add(new JScrollPane(textArea), BorderLayout.CENTER);
			
			Program prg = (Program)en;
			FitnessVector fv = prg.getFitnessVector();
			
			
			Contrainte[] ps = new Contrainte[prg.size()];//Concurrent access
			prg.getContraintes().toArray(ps);
			for (Contrainte p : ps) {
				textArea.append(p.printResultPane()+"\n");
			}
		}

		

		
	}

}

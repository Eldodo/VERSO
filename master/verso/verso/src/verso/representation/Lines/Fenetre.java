package verso.representation.Lines;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import verso.model.metric.Metric;
import verso.representation.Lines.representationModel.ClassLineRepresentation;

public class Fenetre extends JPanel{
	Panneau pan = new Panneau();
	final JPanel visualizationPan = new JPanel();
	boolean affichageLegende = false;
	JPanel firstPan = new JPanel();
	JComboBox metricNameChoice = new JComboBox();
	static String[] tabGraphicalValueChoice = {"BlueColor", "BlueToRed", "ColorGradation"};
	JComboBox graphicalValueChoice = new JComboBox(tabGraphicalValueChoice);
	LegendPanel panLegende = new LegendPanel();
	static List<String> lstString = new ArrayList<String>();
	ClassLineRepresentation clr = null;
	

	public Fenetre(ClassLineRepresentation clr){
		
		this.clr = clr;
		
		Panneau.getLineMapping().setGraphicalValue(tabGraphicalValueChoice[0]);
		graphicalValueChoice.addItemListener(new ItemStateGraphicalValue());
		
		//Construction du ComboBox avec les noms des métriques
		for (Metric m : clr.getLine(0).getLine().getMetrics())
		{
			System.out.println(m.getName());
			this.metricNameChoice.addItem(m.getName());
		}
		Panneau.getLineMapping().setMetricName(metricNameChoice.getSelectedItem().toString());
		metricNameChoice.addItemListener(new ItemStateMetricName());
		
		
		//Création des deux CheckBox pour affichage de texte et pour la coloration de la ligne entière
		JCheckBox showTextBox = new JCheckBox("Show text");
		showTextBox.addActionListener(new StateListener());
		JCheckBox fillAllRectBox = new JCheckBox("Color all line");
		fillAllRectBox.addActionListener(new StateListener());
		
		
		//Création du label indiquant le zoom actuel et initialisation
		final JLabel label2 = new JLabel("Zoom actuel : 17");
		label2.setPreferredSize(new Dimension(100, 30));
		
		
		//Création du panneau pour la visualisation et d'un scroll
		visualizationPan.setLayout(new BorderLayout());
		final JScrollPane scroll = new JScrollPane();
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.getVerticalScrollBar().setUnitIncrement(15);
		scroll.getViewport().add(pan);
		scroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){public void adjustmentValueChanged(AdjustmentEvent e){scroll.repaint();} });
		scroll.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener(){public void adjustmentValueChanged(AdjustmentEvent e){scroll.repaint();} });
		visualizationPan.add(scroll,BorderLayout.CENTER);
		
		
		//Création du bouton pour afficher la légende
		final JButton boutonLegende = new JButton("Show Legend");
		boutonLegende.setPreferredSize(new Dimension(130,30));
		boutonLegende.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if (boutonLegende.getText().equals("Show Legend"))
				{
					boutonLegende.setText("Hide Legend");
					panLegende.setVisible(true);
					panLegende.setLegendInfo(metricNameChoice.getSelectedItem().toString(), graphicalValueChoice.getSelectedItem().toString());
					panLegende.setVisible(false);
					panLegende.setVisible(true);
					System.out.println("Taille du panLegende : " +panLegende.getSize());
				}
				else
				{
					boutonLegende.setText("Show Legend");
					panLegende.setVisible(false);
				}
				}
			});

		
		//Création du slider pour le zoom des lignes
		final JSlider slide = new JSlider();
		slide.setMaximum(50);
		slide.setMinimum(0);
		slide.setValue(17);
		slide.setPreferredSize(new Dimension(400,50));
		slide.setPaintTicks(true);
		slide.setPaintLabels(true);
		slide.setMajorTickSpacing(5);
		slide.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent event){
				if (((JSlider)event.getSource()).getValue()==0)
					label2.setText("Zoom actuel :   1");
				else if (((JSlider)event.getSource()).getValue()!=0 && ((JSlider)event.getSource()).getValue()<10)
					label2.setText("Zoom actuel :   " + ((JSlider)event.getSource()).getValue());
				else
					label2.setText("Zoom actuel : " + ((JSlider)event.getSource()).getValue());
				if (((JSlider)event.getSource()).getValue()==0)
					Panneau.setTailleLigne(1);
				else
					Panneau.setTailleLigne(((JSlider)event.getSource()).getValue());
				visualizationPan.add(scroll,BorderLayout.CENTER);
				pan.repaint();
				scroll.repaint();
				}
		});
		slide.addMouseWheelListener(new MouseWheelListener(){

			public void mouseWheelMoved(MouseWheelEvent e) {
				// TODO Auto-generated method stub
				slide.setValue(e.getWheelRotation() + slide.getValue());
				
			}});
		
		
		//Création du panneau d'options
		firstPan.add(metricNameChoice);
		firstPan.add(graphicalValueChoice);
		firstPan.add(new JLabel("    ZOOM :  "));
		firstPan.add(slide);
		firstPan.add(label2);
		final JPanel panelBox = new JPanel();
		panelBox.setLayout(new BorderLayout());
		panelBox.add(showTextBox, BorderLayout.NORTH);
		panelBox.add(fillAllRectBox,BorderLayout.SOUTH);
		firstPan.add(panelBox);
		firstPan.add(boutonLegende);
		
		
		//Création de la "fenêtre" (du panneau complet)
		this.setLayout(new BorderLayout());
		this.add(visualizationPan, BorderLayout.CENTER);
		this.add(firstPan, BorderLayout.NORTH);
		this.add(panLegende, BorderLayout.EAST);
		this.setVisible(true);
		this.addComponentListener(new Change()); // pour que le panneau de légende se repaint correctement (étrangement, il ne le fait pas automatiquement)
		
	}
	
	
	class Change implements ComponentListener
	{

		public void componentHidden(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void componentMoved(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void componentResized(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			if (panLegende.isVisible() == true)
			{
				panLegende.setLegendInfo(metricNameChoice.getSelectedItem().toString(), graphicalValueChoice.getSelectedItem().toString());
				panLegende.setVisible(false);
				panLegende.setVisible(true);
			}
		}

		public void componentShown(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	
	public void setClass(ClassLineRepresentation c)
	{
		this.pan.setClass(c);
		this.clr = c;
		this.setLegend();
	}
	
	public void setLegend()
	{
		ClassLineRepresentation c = clr;
		String currentMetric = metricNameChoice.getSelectedItem().toString();
		String currentGraphicalValue = graphicalValueChoice.getSelectedItem().toString();
		
		this.panLegende.setClass(c, currentMetric, currentGraphicalValue);
	}
	
	
	public void paint(Graphics g)
	{
		super.paint(g);
		int maxY = 0;
		for (int i = 0; i < firstPan.getComponents().length;i++)
		{
			int currY = firstPan.getComponents()[i].getBounds().height + firstPan.getComponents()[i].getBounds().y;
			if (currY > maxY)
				maxY = currY;
		}
		Dimension d = firstPan.getPreferredSize();
		firstPan.setPreferredSize(new Dimension(this.getWidth(), maxY));
		doLayout();
	}
	
	class ItemStateMetricName implements ItemListener
	{
		public void itemStateChanged(ItemEvent e) {
			// TODO Auto-generated method stub
			Panneau.getLineMapping().setMetricName(metricNameChoice.getSelectedItem().toString());
			panLegende.setLegendInfo(metricNameChoice.getSelectedItem().toString(), graphicalValueChoice.getSelectedItem().toString());
			//panLegende.repaint();
			if (panLegende.isVisible() == true)
			{
				panLegende.setVisible(false);
				panLegende.setVisible(true);
			}
			pan.repaint();
		}
	}
	
	class ItemStateGraphicalValue implements ItemListener
	{

		public void itemStateChanged(ItemEvent e) {
			// TODO Auto-generated method stub
			Panneau.getLineMapping().setGraphicalValue(e.getItem().toString());
			panLegende.setLegendInfo(metricNameChoice.getSelectedItem().toString(), e.getItem().toString());
			//panLegende.repaint();
			if (panLegende.isVisible() == true)
			{
				panLegende.setVisible(false);
				panLegende.setVisible(true);
			}
			pan.repaint();
		}
		
	}
	class StateListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (((JCheckBox)e.getSource()).getText().equals("Show text"))
			{
				Panneau.setShowText(((JCheckBox)e.getSource()).isSelected());
			}
			if (((JCheckBox)e.getSource()).getText().equals("Color all line"))
			{
				Panneau.setFillAllRect(((JCheckBox)e.getSource()).isSelected());
			}
			visualizationPan.repaint();
		}	
	}
	
}

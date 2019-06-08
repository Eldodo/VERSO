package verso.representation.Lines;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import verso.model.metric.LegendDescriptor;
import verso.model.metric.LegendValue;
import verso.model.metric.Metric;
import verso.representation.Lines.representationModel.ClassLineRepresentation;

public class LegendPanel extends JPanel{
	ClassLineRepresentation clr = null;
	public String currentGraphicalValue = null;
	public String currentMetric = null;
	LegendDescriptor ld = null;
	private List<LegendValue> lstLegendValues = new ArrayList<LegendValue>();
	
	
	
	public LegendPanel() {
		this.setLocation(10, 10);
		this.setVisible(false);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	public void setClass(ClassLineRepresentation clr, String metricName, String graphValue) {
		this.clr = clr;
		this.currentMetric = metricName;
		this.currentGraphicalValue = graphValue;
		setLegendInfo(metricName, graphValue);
	}
	
	public void setLegendInfo(String metricName, String graphValue)
	{
		this.currentMetric = metricName;
		this.currentGraphicalValue = graphValue;
		Metric metric = clr.getLine(0).getLine().getMetric(metricName);
		this.ld = metric.getLegendDescriptor();
		this.lstLegendValues = ld.getList();
		setLegendPanel();
	}
	
	
	public void setLegendPanel()
	{
		this.removeAll();
		this.validate();
		if (ld.getType()==LegendDescriptor.INTERVAL)
		{
			int tailleDegradeIcon = (this.getHeight()-(20*lstLegendValues.size()))/lstLegendValues.size();
			for (int i = 0; i<lstLegendValues.size();i++)
			{
				LegendValue lv = lstLegendValues.get(i);
				this.add(new JLabel(lv.getValueName() + "  ", new IntervalLegendIcon(getColor(currentGraphicalValue, lv.getNormalizedValue()), 20), JLabel.LEFT));
				if (i<lstLegendValues.size()-1)
				{
					this.add(new JLabel(new DegradeIcon(tailleDegradeIcon, lv.getNormalizedValue(), lstLegendValues.get(i+1).getNormalizedValue())));
				}
			}
		}
		if (ld.getType()==LegendDescriptor.NOMINAL)
		{
			for (int i = 0; i<lstLegendValues.size(); i++)
			{
				LegendValue lv = lstLegendValues.get(i);
				this.add(new JLabel(lv.getValueName() + "  ", new NominalLegendIcon(getColor(currentGraphicalValue, lv.getNormalizedValue())), JLabel.LEFT));
			}
		}	
	}
	
	class NominalLegendIcon implements Icon {

		Color c = null;

		public NominalLegendIcon(Color c) {
			this.c = c;
		}

		public int getIconHeight() {
			// TODO Auto-generated method stub
			return 28;
		}

		public int getIconWidth() {
			// TODO Auto-generated method stub
			return 28;
		}

		public void paintIcon(Component arg0, Graphics g, int arg2, int arg3) {
			// TODO Auto-generated method stub
			g.setColor(Color.black);
			g.fillRect(2, 2, 24, 24);
			g.setColor(c);
			g.fillRect(4, 4, 20, 20);
		}

	}

	class IntervalLegendIcon implements Icon {

		Color color = null;
		int size = 0;

		public IntervalLegendIcon(Color color, int size) {
			this.color = color;
			this.size = size;
		}

		public int getIconHeight() {
			// TODO Auto-generated method stub
			return size;
		}

		public int getIconWidth() {
			// TODO Auto-generated method stub
			return 28;
		}

		public void paintIcon(Component arg0, Graphics g, int arg2, int arg3) {
			// TODO Auto-generated method stub
			/*
			 * if (size == 20) { g.setColor(Color.black); g.fillRect(2, 2, 24,
			 * 24); }
			 */
			g.setColor(color);
			g.fillRect(0, 0, 24, size);

		}

	}
	
	class DegradeIcon implements Icon{

		int height = 0;
		double nv1 = 0;
		double nv2 = 0;
		int taille = 0;
		
		public DegradeIcon(int height, double normalizedValue1, double normalizedValue2)
		{
			this.taille = height/10;
			this.nv1 = normalizedValue1;
			this.nv2 = normalizedValue2;
		}
		public int getIconHeight() {
			// TODO Auto-generated method stub
			return 10*taille;
		}

		public int getIconWidth() {
			// TODO Auto-generated method stub
			return 28;
		}

		public void paintIcon(Component arg0, Graphics g, int arg2, int arg3) {
			// TODO Auto-generated method stub
			double diff = (nv2-nv1)/(double)10;
			double value = nv1;
			
			for (int i=0; i<10; i++)
			{
				g.setColor(getColor(currentGraphicalValue, value));
				g.fillRect(0, taille*i, 24, taille);
				value = value+diff;
			}
			
		}
		
	}
	
	
	
	public Color getColor(String graphicalValue, double value) {
		Color toReturn = null;
		if (graphicalValue.equals("BlueColor")) {
			toReturn = new Color((int)(255 - value * 255),(int)(255 - value * 255),255);
		}
		if (graphicalValue.equals("BlueToRed")) {
			Color c = new Color((int)(value*255), 0 ,(int)(255 - value*255));
			float[] hsbColor = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
			c = new Color(Color.HSBtoRGB(hsbColor[0], hsbColor[1]-0.25f, hsbColor[2]));
			toReturn = new Color(c.getRed(), c.getGreen() ,c.getBlue());
		}
		if (graphicalValue.equals("ColorGradation")) {
			Color c;
			if (value < 0.25) {
				c = new Color(0, (int) (value * 4 * 255), 255);
			} else if (value < 0.50) {
				c = new Color(0, 255, (int) (255 - (value - 0.25) * 4 * 255));
			} else if (value < 0.75) {
				c = new Color((int) ((value - 0.50) * 4 * 255), 255, 0);
			} else {
				c = new Color(255, (int) (255 - (value - 0.75) * 4 * 255), 0);
			}
			float[] hsbColor = Color.RGBtoHSB(c.getRed(), c.getGreen(), c
					.getBlue(), null);
			c = new Color(Color.HSBtoRGB(hsbColor[0], hsbColor[1] - 0.30f,
					hsbColor[2]));
			toReturn = new Color(c.getRed(), c.getGreen(), c.getBlue());
		}
		return toReturn;
	}
	

}

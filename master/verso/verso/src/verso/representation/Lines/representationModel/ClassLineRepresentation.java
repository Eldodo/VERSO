package verso.representation.Lines.representationModel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import verso.model.Element;
import verso.representation.Lines.visitor.LineMapping;
import verso.representation.Lines.visitor.LineMappingVisitor;

public class ClassLineRepresentation {

	private List<LineRepresentation> lines = new ArrayList<LineRepresentation>();
	private Element classe = null;
	static Set<Integer> ensembleValeur;
	
	public ClassLineRepresentation(Element classe)
	{
		this.classe = classe;
	}
	
	public void setClasse(Element classe)
	{
		this.classe = classe;
	}
	
	public Element getClasse()
	{
		return this.classe;
	}
	
	public void addLine(LineRepresentation line)
	{
		this.lines.add(line);
	}
	
	public LineRepresentation getLine(int i)
	{
		return lines.get(i);
	}
	
	public List<LineRepresentation> getLines()
	{
		return this.lines;
	}
	
	public void render()
	{	
		///... appeller render sur toutes les lignes de la liste
	}
	
	public void accept(LineMappingVisitor v)
	{
		v.visit(this);
	}
	
	public void applyColorMapping(LineMapping map)
	{
		//map.getGraphicalValue();
		//map.setGraphicalValue("BlueColor");
		int lineNumber = 0;
		for (LineRepresentation l : lines)
		{
			double value = l.getLine().getMetric(map.getMetricName()).getNormalizedValue();
			System.out.println(++lineNumber + " : " + value);
			if (map.getGraphicalValue().equals("BlueColor"))
			{
				//c = new Color((int)(255 - value*255) ,(int)(255 - value*255),255);
				l.setColor(new Color((int)(255 - value*255) ,(int)(255 - value*255),255));
			}
			if (map.getGraphicalValue().equals("BlueToRed"))
			{
				Color c = new Color((int)(value*255), 0 ,(int)(255 - value*255));
				float[] hsbColor = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
				c = new Color(Color.HSBtoRGB(hsbColor[0], hsbColor[1]-0.25f, hsbColor[2]));
				l.setColor(new Color(c.getRed(), c.getGreen() ,c.getBlue()));
			}
			if (map.getGraphicalValue().equals("ColorGradation"))
			{
				Color c  =null;
				if (value < 0.25)
				{
					c = new Color(0,(int)(value*4*255),255);
				}
				else if (value < 0.50)
				{
					c = new Color(0,255,(int)(255-(value-0.25)*4*255));
				}
				else if (value < 0.75)
				{
					c = new Color((int)((value-0.50)*4*255),255,0);
				}
				else
				{
					c = new Color(255,(int)(255-(value-0.75)*4*255),0);
				}
				float[] hsbColor = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
				c = new Color(Color.HSBtoRGB(hsbColor[0], hsbColor[1]-0.25f, hsbColor[2]));
				//Color c2 = new Color(Math.max(0, c.getRed()-150), Math.max(0, c.getGreen()-150),Math.max(0, c.getBlue()-150));
				//c = new Color(c2.getRed() +150,c2.getGreen() +150,c2.getBlue() +150);
				l.setColor(new Color(c.getRed(), c.getGreen() ,c.getBlue()));
			}
			
		}
		
		
	}
	
}

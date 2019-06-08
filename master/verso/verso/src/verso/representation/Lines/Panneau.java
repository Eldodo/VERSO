package verso.representation.Lines;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import verso.representation.Lines.representationModel.ClassLineRepresentation;
import verso.representation.Lines.representationModel.LineRepresentation;
import verso.representation.Lines.visitor.LineMapping;

public class Panneau extends JPanel {

	//List<Integer> liste = new ArrayList<Integer>();
	List<LineRepresentation> list = new ArrayList<LineRepresentation>();
	static int tailleLigne = 17;
	static //static int[] tableau = {0,0,0,0,0,0,0,0,0,0,0};
	private ClassLineRepresentation classe = null;
	static LineMapping lm = new LineMapping();
	static boolean showText = false;
	static boolean fillAllRect = false;
	
	
	public void setClass(ClassLineRepresentation classe)
	{
		this.classe = classe;
		if (classe !=null)
			list = classe.getLines();
		this.repaint();
	}

	
	
	public void paintComponent(Graphics g)
	{
		int largeurMax = 0;
		this.setPreferredSize(new Dimension(largeurMax,list.size()*tailleLigne + 4));
		int i = 1;
		
		this.classe.applyColorMapping(lm);
		for (LineRepresentation lr : list)
		{
			g.setColor(lr.getColor());
			//lstColor.add(lr.getColor());
			if (fillAllRect == true)
			{
				g.fillRect(0, (i-1)*tailleLigne+(int)(tailleLigne/4), this.getWidth(), tailleLigne);
				if (largeurMax<((int)(lr.getLenght()*(((tailleLigne*0.75f)/1.6f))) + (3*tailleLigne/2)))
				{
					largeurMax = ((int)(lr.getLenght()*((tailleLigne*0.75f/1.6f))) + (3*tailleLigne/2));
					this.setPreferredSize(new Dimension(largeurMax,list.size()*tailleLigne + 4));
				}
			}
			if (fillAllRect == false)
			{
				g.fillRect(0, (i-1)*tailleLigne+(int)(tailleLigne/4), (int)(lr.getLenght()*(tailleLigne*0.75f)/(double)1.6f) + (3*tailleLigne/2), tailleLigne);
				if (largeurMax<((int)(lr.getLenght()*(((tailleLigne*0.75f)/1.6f))) + (3*tailleLigne/2)))
				{
					largeurMax = ((int)(lr.getLenght()*(((tailleLigne*0.75f)/1.6f))) + (3*tailleLigne/2));
					this.setPreferredSize(new Dimension(largeurMax,list.size()*tailleLigne + 4));
				}
			}
			String str = String.valueOf(i);
			Font font = new Font("Courier New", Font.PLAIN, (int)(tailleLigne*0.75f));
			g.setFont(font);
			g.setColor(Color.black);
			g.drawString(str, 0, i * tailleLigne);
			i++;
			if(showText == true)
			{
				if (tailleLigne*0.75 > 3)
				{
					g.drawString(lr.getText(), (int)(3*tailleLigne/2), i*tailleLigne-tailleLigne);
				}
			}
		}
	}

	public static void setTailleLigne(int nouvelleTaille) {
		tailleLigne = nouvelleTaille;
	}

	public static LineMapping getLineMapping()
	{
		return lm;
	}
	
	public static void setShowText(boolean b)
	{
		showText = b;
	}
	public static void setFillAllRect(boolean b)
	{
		fillAllRect = b;
	}
}

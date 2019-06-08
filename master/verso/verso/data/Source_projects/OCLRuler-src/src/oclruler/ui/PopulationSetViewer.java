package oclruler.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import oclruler.genetics.Evolutioner;
import oclruler.genetics.GeneticIndividual;
import oclruler.genetics.Oracle;
import oclruler.genetics.Population;
import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.Model;
import oclruler.rule.Program;

public class PopulationSetViewer extends JPanel {
	
	private static final long serialVersionUID = 5358111264633295584L;
	
	Color cRight = new Color((int)( 0xFF9644));
	Color cWrong = new Color((int)( 0x007799));
	Color cUnknown = new Color((int)( 0x666666));
	
	Font fontModels = new Font(Font.SANS_SERIF, Font.BOLD, 13);
	public static Color cValidModel = new Color((int)( 0x083600));
	public static Color cInvalidModel = new Color((int)( 0xa50606));
	Font fontSolution = new Font(Font.SANS_SERIF, Font.BOLD, 13);

	
	Evolutioner evo;
	
//	Controller controller;
	EavesDroper eavesDroper;
	PopulationSetRawViewer rawPopSetViewer;
	ProgramViewer prgViewer;
	ClickedProgram clickedPrg;
	private ExampleSetExplorer exampleSetPane;
	
	public class ClickedProgram{
		protected Program prg;
		protected int idx;
		protected int generation;
		
		public ClickedProgram(Program prg, int idx, int generation) {
			this.prg = prg;
			this.idx = idx;
			this.generation = generation;
		}
	}
	
	public PopulationSetViewer(ExampleSetExplorer exampleSetPane) {
		super(new BorderLayout());

		this.exampleSetPane = exampleSetPane;
		rawPopSetViewer = new PopulationSetRawViewer();

		prgViewer = new ProgramViewer();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setOneTouchExpandable(false);
		splitPane.setContinuousLayout(true);
		splitPane.setDividerSize(5);
		int dividerLocation = Math.min((ExampleSet.getExamplesBeingUsed().size()) * 17, 500);
		
		splitPane.setDividerLocation(dividerLocation); 
		
		splitPane.setLeftComponent(rawPopSetViewer);
		splitPane.setRightComponent(prgViewer);
		
//		controller = new Controller();
		eavesDroper = new EavesDroper();
		
//		add(controller, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
		add(eavesDroper, BorderLayout.SOUTH);
	}

	public PopulationSetViewer(Evolutioner evo, ExampleSetExplorer examplePane) {
		this(examplePane);
		this.evo = evo;
		
	}
	
	GeneticIndividual[] getSortedEntities(){
		GeneticIndividual[] ges = (GeneticIndividual[]) evo.getCurrentPopulation().getEntities().toArray(new GeneticIndividual[evo.getCurrentPopulation().getEntities().size()]);
		Arrays.sort(ges, GeneticIndividual.getValueComparator(0));
		return ges;
	}
	
	Program getProgram(int idx){
		GeneticIndividual[] ges = getSortedEntities();
		if(idx >= 0 && idx < ges.length)
			return (Program)ges[idx];
		return null;
	}

	
	public class PopulationSetRawViewer extends JPanel {
		private static final long serialVersionUID = -7923122473061190221L;
		int w = getWidth()/(ExampleSet.getExamplesBeingUsed().size());
		int h = getHeight()/Population.NB_ENTITIES_IN_POP;

		double mousePosititonY = 0;
		
		
		public PopulationSetRawViewer() {
			super();
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					populationSetRawViewerMouseMove(e);
				}
			});
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					populationSetRawViewerMouseClicked(e);
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					if(clickedPrg != null && evo.generation == clickedPrg.generation)
						mousePosititonY = clickedPrg.idx * h;
					else if(!evo.isPause() && ! evo.hasStopped())
						mousePosititonY = 0;
					updateRaw();
				}
			});
			updateUI();
		}
		
		
		public void populationSetRawViewerMouseMove(MouseEvent e) {
			if(evo.hasStopped() || evo.isPause())
				updateUI();
		}
		
		public void populationSetRawViewerMouseClicked(MouseEvent e) {
			if(SwingUtilities.isRightMouseButton(e)){
				evo.togglePause();
			} else {
				GeneticIndividual[] ges = getSortedEntities();
				int currentPrgIdx = (int)getMousePosition().getY() / h;
				int currentExampleIdx = (int)getMousePosition().getX() / w;
				Model clickedModel = null;
				if(currentExampleIdx >= 0 && currentExampleIdx < ExampleSet.getExamplesBeingUsed().size()){
					clickedModel = ((Model[]) ExampleSet.getExamplesBeingUsed().toArray(new Model[ExampleSet.getExamplesBeingUsed().size()]))[currentExampleIdx];
					exampleSetPane.selectExample(clickedModel);
				}
				
				if(currentPrgIdx >= 0 && currentPrgIdx < ges.length){
					clickedPrg =  new ClickedProgram((Program)ges[currentPrgIdx], currentPrgIdx, evo.generation);
					prgViewer.setProgram(clickedPrg.prg, clickedModel, currentPrgIdx, evo.generation);
					
					
					
				}
			}
		}

		public void paint(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setBackground(Color.BLACK);
			
			if ( evo == null || evo.getCurrentPopulation() == null || !evo.getEvaluator().getExampleSet().isOraculized() || evo.getCurrentPopulation().getEntities().get(0).getFitnessVector()==null){
				drawCenteredString(g, "Loading", 15, fontSolution);
				return;
			}
			
			if(getMousePosition() != null)
				mousePosititonY = getMousePosition().getY();
			else {
				if(clickedPrg != null && evo.generation == clickedPrg.generation)
					mousePosititonY = clickedPrg.idx * h;
				else mousePosititonY = 10000;//out of reach
			}
			
			try {
				w = getWidth() / (ExampleSet.getExamplesBeingUsed().size());
				h = getHeight() / Population.NB_ENTITIES_IN_POP;
				
				GeneticIndividual[] ges = getSortedEntities();
				drawEntities_VS_Examples(g2, ges);
				
				int indexY = (int) mousePosititonY / h  ;
				int hoverY = indexY * h;
				
				
				/* Print hovered solution */
				if(indexY >= 0 && indexY < ges.length)
					drawHoveredProgram(g2, hoverY, (Program)ges[indexY]);
				
				
				/* Print examples' names	 */
				g2.setFont(fontModels);
		        drawExampleNames(g, g2);
				
			} catch (Exception e) {
				System.out.println("Exception printing pop viewer");
				e.printStackTrace();
			}
		}

		public void drawExampleNames(Graphics g, Graphics2D g2) {
			AffineTransform at = new AffineTransform();
			at.setToRotation(Math.toRadians(90), 00, 0);
			g2.setTransform(at);
			int i = 0;
			for (Model m : ExampleSet.getExamplesBeingUsed()) {
				int posX = -w*i++ -5;
				String textModel = "null";
				if(m != null)
					textModel = m.getName();
				int posY = 10;
				if(mousePosititonY < getStringWidth(g, fontModels, "model_000012345") )
					posY = (int)mousePosititonY*h/h + h + getStringHeight(g, fontModels, textModel)+7;
				
				g2.setColor(m.isValid()?cValidModel:cInvalidModel);
				g2.drawString(textModel, posY, posX);
				
			}
		}

		public void drawHoveredProgram(Graphics2D g2, int hoverY, Program hoverededProgram) {
			g2.setColor(Color.BLACK);
			g2.setFont(fontSolution);
			g2.drawLine(0, hoverY, getWidth(), hoverY);
			g2.drawLine(0, hoverY + h, getWidth(), hoverY + h);
			
			String textSolution = hoverededProgram.getName();
			
			if(hoverededProgram.getFitnessVector() != null) {
				String tmp = hoverededProgram.getFitnessVector().printExpandedStat();
			
				String tmp1 = tmp.substring(0, tmp.indexOf(" "));
				String tmp2 = tmp.substring(tmp.indexOf(" ")+1);
				textSolution = tmp1 +" - "+ hoverededProgram.getName()+" - " + tmp2 +" "+ String.format("%.2f", hoverededProgram.getFitnessVector().getValue(0));
			}
			
			int hString = getStringHeight(g2, fontModels, "RR");
			if(mousePosititonY < getStringWidth(g2, fontModels, "model_000012345") )
				drawCenteredString(g2, textSolution, hoverY+h+hString, fontSolution);
			else
				drawCenteredString(g2, textSolution, hoverY-5, fontSolution);
		}


		public void drawEntities_VS_Examples(Graphics2D g2, GeneticIndividual[] ges) {
			int xOff = 0;
			int yOff = 0;
			
//			double[][] solutions_vs_examples = evo.getCurrentPopulation().getSolutions_vs_examples();
//			for (int i = 0; i < solutions_vs_examples.length; i++) {
//				for (int j = 0; j < solutions_vs_examples[i].length; j++) {
//					double ds = solutions_vs_examples[i][j];
//					Color c = cUnknown;
//					if(ds == 1)
//						c = cRight;
//					else if(ds == 0)
//						c = cWrong;
//					g2.setColor(c);
//					g2.fillRect(xOff, yOff, w, h);
//					xOff += w;
//				}
//				yOff += h;
//				xOff = 0;
//			}
			
			for (GeneticIndividual ge : ges) {
				Program prg = (Program) ge;
				for (Model m : ExampleSet.getExamplesBeingUsed()) {
					Color c = cUnknown;
					if(prg.getFitnessVector() !=null){
						Oracle.OracleCmp oc = prg.getFitnessVector().getCmps().get(m);
						if (oc != null) 
							c = oc.isRight()?cRight:cWrong;
					} 
					g2.setColor(c);
					g2.fillRect(xOff, yOff, w, h);
					xOff += w;
				}
				yOff += h;
				xOff = 0;
			}
		}

		public void drawCenteredString(Graphics g, String text, int y, Font font) {
		    // Get the FontMetrics
		    FontMetrics metrics = g.getFontMetrics(font);
		    // Determine the X coordinate for the text
		    int x =  (getWidth() - metrics.stringWidth(text)) / 2;
		    // Set the font
		    g.setFont(font);
		    // Draw the String
		    g.drawString(text, x, y);
		}
	}
	
	public static int getStringWidth(Graphics page, Font f, String s) {
		FontMetrics fm = page.getFontMetrics(f);
		java.awt.geom.Rectangle2D rect = fm.getStringBounds(s, page);
		return (int) Math.round(rect.getWidth());
	}

	public static int getStringHeight(Graphics page, Font f, String s) {
		FontMetrics fm = page.getFontMetrics(f);
		java.awt.geom.Rectangle2D rect = fm.getStringBounds(s, page);
		return (int) Math.round(rect.getHeight());
	}
	
	
	public void updateRaw() {
		rawPopSetViewer.updateUI();
	}

	public void eavesDrops(String string) {
		eavesDroper.setText(string);
	}
	
	
	public void setEvolutioner(Evolutioner evo) {
		this.evo = evo;
		prgViewer.setEvolutioner(evo);
		updateRaw();
	}


}

package oclruler.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import oclruler.genetics.Evolutioner;
import oclruler.genetics.Population;
import oclruler.utils.Config;

public class Ui  extends JFrame {
	private static final long serialVersionUID = -7849901548919749679L;
	public static final Logger LOGGER = Logger.getLogger(Ui.class.getName());;

	public static boolean PRINT_FIRSTS = true;
	
	JTabbedPane tabbedPane;
	

	EvolutionPanel chart;
	PopulationSetViewer popSetViewer;
	ExampleSetExplorer examplesTab;
	PatternsInstantiationPanel patternsPanel;
	JTextArea textSetting;
	JTextArea textLog;
	
	
	
	private static Ui instance;
	protected static ExampleEditionUi debugInstance;
	Evolutioner evo;
	
	static String timeStamp = new SimpleDateFormat("yyyyMMdd-Hmm", Locale.FRANCE).format( new Date() );
	
	public void setEvolutioner(Evolutioner evo) {
		this.evo = evo;
		popSetViewer.setEvolutioner(evo);
	}
	
	public Evolutioner getEvolutioner() {
		return evo;
	}
	
	public static int callNum = 0;
	
	public static Ui getInstance(){
		callNum++;
		if(instance == null){
			instance = new Ui();
		}
		return instance;
	}
	
	public static void setPlotObjectivesTitle(String title){
		getInstance().chart.chartObj.setTitle(title);
	}
	public static void setPlot2Title(String title){
		getInstance().chart.chartSeries2.setTitle(title);
	}
	public static void setPlot3Title(String title){
		getInstance().chart.chartSeries3.setTitle(title);
	}
	public static void setPlot4Title(String title){
		getInstance().chart.chartSeries4.setTitle(title);
	}
	public static void setPlot5Title(String title){
		getInstance().chart.chartSeries5.setTitle(title);
	}

	
	static enum TabTitle {
		Evolution,
		Population,
		Patterns,
		Examples,
		Setting,
		Log;
		static TabTitle DEFAULT_OPENING_TAB = Evolution;
		
		static TabTitle[] orderedValues(){
			Arrays.sort(values(), (TabTitle o1, TabTitle o2) ->  o1.ordinal() - o2.ordinal() );
			return values();
		}
		
		public static void setDefautOpeningTab(TabTitle tt) {
			DEFAULT_OPENING_TAB = tt;
		}
	}
	
	
	private Ui() {
		super("Evolution "+Config.METAMODEL_NAME+" - G:"+Evolutioner.GENERATION_MAX+" E:"+Population.NB_ENTITIES_IN_POP+" "+(Config.TFIDF.prettyPrint())+" - "+timeStamp);
		LOGGER.info("Ui started.");
		JComponent chartPanel = createChartPanel();
		tabbedPane = new JTabbedPane();
		tabbedPane.add(chartPanel, TabTitle.Evolution);
		textSetting = new JTextArea();
		//textConfig.setContentType("text/html");
		textSetting.setEditable(false);

		textLog = new JTextArea();
		//textConfig.setContentType("text/html");
		textLog.setEditable(false);
		textLog.setText("");
		
		
		examplesTab = new ExampleSetExplorer();
		popSetViewer = new PopulationSetViewer(examplesTab);
		
		patternsPanel = new PatternsInstantiationPanel();
		
		
		
		
		tabbedPane.add(popSetViewer, TabTitle.Population.ordinal());
		tabbedPane.add(patternsPanel, TabTitle.Patterns.ordinal());
		tabbedPane.add(examplesTab, TabTitle.Examples.ordinal());
		tabbedPane.add(new JScrollPane(textSetting), TabTitle.Setting.ordinal());
		tabbedPane.add(new JScrollPane(textLog), TabTitle.Log.ordinal());

		for (TabTitle tt : TabTitle.orderedValues()) 
			tabbedPane.setTitleAt(tt.ordinal(), tt.name());
		
		tabbedPane.setSelectedIndex(TabTitle.DEFAULT_OPENING_TAB.ordinal());
		
		add(tabbedPane, BorderLayout.CENTER);

		setSize(900, 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
//		setVisible(true);
	}
	
	private JComponent createChartPanel() {
		
		int v=ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		int h=ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS; 
		chart = new EvolutionPanel(Config.METAMODEL_NAME, timeStamp);
		
		JScrollPane jspGen = new JScrollPane(chart,v,h);
		jspGen.setPreferredSize(new Dimension(600,800));
		jspGen.setBounds(150,670,600,800);
		
		return jspGen;
	}
	
	public void setTextSetting(String text){
		textSetting.setText(text+"\n");
	}
	
	public void appendTextSetting(String text){
		textSetting.setText(textSetting.getText()+text+"\n");
	}
	public void setTextLog(String text){
		textLog.setText(text+"\n");
	}
	
	public void log(String text){
		textLog.append(text+"\n");
	}
	public void addObjective(int generation, float rate, double max, int rang) {
		chart.addObjective(generation, rate, max, rang);
	}
	public void addToGraph2(int generation, float rate) {
		chart.addToGraph2(generation, rate);
	}
	public void addToGraph3(int generation, float rate) {
		chart.addToGraph3(generation, rate);
	}
	public void addToGraph4(int generation, float rate) {
		chart.addToGraph4(generation, rate);
	}
	public void addToGraph5(int generation, float rate) {
		chart.addToGraph5(generation, rate);
	}

	public void removeResultPane() {
		tabbedPane.removeTabAt(3);
	}

	public void setMonoObjective() {
		super.setTitle(super.getTitle()+" - Mono");
		chart.setMonoObjective();
	}

	
	public static void showUi() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Ui.getInstance().setVisible(true);
			}
		});
	}

	public void updatePopulationViewer() {
		popSetViewer.updateRaw();
		popSetViewer.eavesDrops("G"+evo.generation+" - Best "+evo.getCurrentPopulation().getBestOnObj0().getFitnessVector().getValue(0));
	}

	public void updatePatternTable() {
		patternsPanel.updateTable();
	}

	/**
	 * Choose opening tab when loading UI.<br/>
	 * Options are from {@link TabTitle}:
	 * <ul>
	 *   <li>Evolution</li>
	 *   <li>Population</li>
	 *   <li>Patterns</li>
	 *   <li>Exemples</li>
	 *   <li>Setting</li>
	 *   <li>Log</li>
	 * </ul>
	 * 
	 * @param title
	 */
	public static void setOpeningTab(String title) {
		try {
			TabTitle.DEFAULT_OPENING_TAB = TabTitle.valueOf(title);
		} catch (Exception e) {
			LOGGER.config("UI_OPENING_TAB: '"+title+"' is not an option.\nChoices: "+Arrays.toString(TabTitle.values()));
		}
	}

	public static void showExampleDebugUi(String subtitle) {
		if(debugInstance == null)
			debugInstance = new ExampleEditionUi(subtitle);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Ui.debugInstance.setVisible(true);
			}
		});
		
	}

	public static void init(String initLog) {
		if(Config.EXAMPLE_EDITION_MODE){
			Ui.showExampleDebugUi("Example edition mode.");
			new Scanner(System.in).nextLine();//DebugUI has EXIT_ON_CLOSE option on.
		}
        if (Config.VERBOSE_ON_UI){ 
//    		subscribeParetoListener(Ui.getInstance().getChartTime());
    		Ui.getInstance().setTextSetting(Config.printPrettySetting());
    		Ui.getInstance().log(initLog);
			Ui.setPlot2Title("# Fronts");
    		Ui.setPlot3Title("Average # Patterns");
    		Ui.setPlot4Title("Injections");
    		Ui.setPlot5Title("Size Pareto");
    		Ui.showUi();
    	}

	}

}

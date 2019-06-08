package oclruler.ui;

import oclruler.genetics.Evolutioner;
import oclruler.genetics.Population;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import utils.Config;

public class Ui  extends JFrame {
	private static final long serialVersionUID = -7849901548919749679L;
	public static final Logger LOGGER = Logger.getLogger(Ui.class.getName());;

	public static boolean PRINT_FIRSTS = true;
	
	JTabbedPane tabbedPane;
	XYLineChart2 chart;
	JTextArea textSetting;
	JTextArea textLog;
	ResultTabbedPane resultTab;
	private static Ui instance;
	Evolutioner evo;
	
	static String timeStamp = new SimpleDateFormat("yyyyMMdd-Hmm", Locale.FRANCE).format( new Date() );
	
	public void setEvolutioner(Evolutioner evo) {
		this.evo = evo;
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

	private Ui() {
		super("Evolution "+Config.METAMODEL_NAME+" - G:"+Evolutioner.GENERATION_MAX+" E:"+Population.POPULATION_SIZE+" - "+timeStamp);
		
		JComponent chartPanel = createChartPanel();
		tabbedPane = new JTabbedPane();
		tabbedPane.add(chartPanel, 0);
		textSetting = new JTextArea();
		//textConfig.setContentType("text/html");
		textSetting.setEditable(false);
		tabbedPane.add(new JScrollPane(textSetting), 1);

		textLog = new JTextArea();
		//textConfig.setContentType("text/html");
		textLog.setEditable(false);
		textLog.setText("");
		tabbedPane.add(new JScrollPane(textLog), 2);
		
		
		resultTab = new ResultTabbedPane();
		tabbedPane.add(resultTab, 3);

		tabbedPane.setTitleAt(0, "Chart");
		tabbedPane.setTitleAt(1, "Setting");
		tabbedPane.setTitleAt(2, "Log");
		tabbedPane.setTitleAt(3, "Results");
		
		add(tabbedPane, BorderLayout.CENTER);

		setSize(720, 700);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}
	
	public void setResultingPop(Population p){
		resultTab.setResultingPop(p);
	}

	private JComponent createChartPanel() {
		
		int v=ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		int h=ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS; 
		chart = new XYLineChart2(Config.METAMODEL_NAME, timeStamp);
		
		JScrollPane jspGen = new JScrollPane(chart,v,h);
		jspGen.setPreferredSize(new Dimension(600,800));
		jspGen.setBounds(150,670,600,200);
		
//		if(Evolutioner.CHECK_POINT_TIME > 0){
//			JTabbedPane tab = new JTabbedPane();
//		
//			tab.addTab("Gen", jspGen);
//			tab.addTab("Time", jspTime);
//		
//			return tab;
//		} else
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

	public void updateResultingPop() {
		if(evo != null && evo.getCurrentPopulation() != null)
			setResultingPop(evo.getCurrentPopulation());;
	}
	public static void showUi() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Ui.getInstance().setVisible(true);
			}
		});
	}

}

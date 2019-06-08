package ui;

import genetic.Evolutioner;
import genetic.Population;

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

import utils.Config;

public class Ui  extends JFrame {
	private static final long serialVersionUID = -7849901548919749679L;
	public static final Logger LOGGER = Logger.getLogger(Ui.class.getName());;

	JTabbedPane tabbedPane;
	XYLineChart2 chartGen;
	XYLineChartTime chartTime;
	JTextArea textSetting;
	JTextArea textLog;
	ResultTabbedPane resultTab;
	private static Ui instance;
	
	public XYLineChartTime getChartTime() {
		return chartTime;
	}
	
	static String timeStamp = new SimpleDateFormat("yyyyMMdd-Hmm", Locale.FRANCE).format( new Date() );
	
	
	public static Ui getInstance(){
		if(instance == null)
			instance = new Ui();
		return instance;
	}

	private Ui() {
		super("Evolution "+Config.METAMODEL_NAME+" "+Config.DIS_OR_MIN+" - G:"+Evolutioner.GENERATION_MAX+" E:"+Population.NB_ENTITIES_IN_POP+" G:"+Population.NB_GENES_IN_ENTITIES+" - "+timeStamp);
		
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
		chartGen = new XYLineChart2(Config.METAMODEL_NAME, timeStamp);
		
		JScrollPane jspGen = new JScrollPane(chartGen,v,h);
		jspGen.setPreferredSize(new Dimension(600,800));
		jspGen.setBounds(150,670,600,200);
		
		chartTime = new XYLineChartTime(Config.METAMODEL_NAME, timeStamp);
		
		JScrollPane jspTime = new JScrollPane(chartTime,v,h);
		jspTime.setPreferredSize(new Dimension(600,800));
		jspTime.setBounds(150,670,600,200);
	
		
		if(Evolutioner.CHECK_POINT_TIME > 0){
		JTabbedPane tab = new JTabbedPane();
		
			tab.addTab("Gen", jspGen);
			tab.addTab("Time", jspTime);
		
			return tab;
		} else
			return jspGen;
	}
	
	public void setTextSetting(String text){
		textSetting.setText(text+"\n");
	}
	
	public void appendTextConfig(String text){
		textSetting.setText(textSetting.getText()+text+"\n");
	}
	public void setTextLog(String text){
		textLog.setText(text+"\n");
	}
	
	public void log(String text){
		textLog.append(""+text+"\n");
	}
	public void addObjective(int generation, float rate, double max, int rang) {
		chartGen.addObjective(generation, rate, max, rang);
	}
	public void addToGraph2(int generation, float rate) {
		chartGen.addToGraph2(generation, rate);
	}
	public void addToGraph3(int generation, float rate) {
		chartGen.addToGraph3(generation, rate);
	}
	public void addToGraph4(int generation, float rate) {
		chartGen.addToGraph4(generation, rate);
	}
	public void addToGraph5(int generation, float rate) {
		chartGen.addToGraph5(generation, rate);
	}

	public void removeResultPane() {
		tabbedPane.removeTabAt(3);
	}

	public void setMonoObjective() {
		super.setTitle(super.getTitle()+" - Mono");
		chartGen.setMonoObjective();
	}

}

package ui;




import genetic.Evolutioner;
import genetic.Population;
import genetic.fitness.FitnessVector;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import utils.Config;

/**
 * This program demonstrates how to draw XY line chart with XYDataset
 * using JFreechart library.
 * @author www.codejava.net
 *
 */
@Deprecated
public class XYLineChart extends JFrame {
	JTabbedPane tabbedPane;
	JTextArea textSetting;
	JTextArea textLog;
	String metamodelName;
	JFreeChart chartObj ;
	JFreeChart chartSize1 ;
	JFreeChart chartSize2 ;
	JFreeChart chartExtra1 ;
	JFreeChart chartExtra2 ;
	public XYLineChart(String titleName, String timeStamp) {
		super("Evolution "+titleName+" - G:"+Evolutioner.GENERATION_MAX+" E:"+Population.NB_ENTITIES_IN_POP+" G:"+Population.NB_GENES_IN_ENTITIES+" - "+timeStamp);
		metamodelName = titleName;
		JScrollPane chartPanel = createChartPanel();
		tabbedPane = new JTabbedPane();
		tabbedPane.add(chartPanel, 0);
		textSetting = new JTextArea();
		//textConfig.setContentType("text/html");
		textSetting.setEditable(false);
		tabbedPane.add(new JScrollPane(textSetting), 1);

		textLog = new JTextArea();
		//textConfig.setContentType("text/html");
		textLog.setEditable(false);
		tabbedPane.add(new JScrollPane(textLog), 2);
		tabbedPane.setTitleAt(0, "Graph");
		tabbedPane.setTitleAt(1, "Setting");
		tabbedPane.setTitleAt(2, "Log");
		add(tabbedPane, BorderLayout.CENTER);
		
		setSize(720, 700);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
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
	
	public void appendTextLog(String text){
		textLog.setText(textLog.getText()+""+text+"\n");
	}
	
	private JScrollPane createChartPanel() {
		String chartTitle = metamodelName+" - G:"+Evolutioner.GENERATION_MAX+" E:"+Population.NB_ENTITIES_IN_POP+" G:"+Population.NB_GENES_IN_ENTITIES;
		String xAxisLabel = "Generation";
		
		boolean showLegend = true;
		boolean createURL = true;
		boolean createTooltip = true;
		
		XYDataset datasetObj = createDataset();
	
		chartObj = ChartFactory.createXYLineChart(chartTitle, 
				xAxisLabel, "Objective", datasetObj, 
				PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		
		
		
		XYSeriesCollection dataset2 = new XYSeriesCollection();
		series2 = new XYSeries("Avg nb of classes in models");
		dataset2.addSeries(series2);
		chartSize1 = ChartFactory.createXYLineChart("", 
				xAxisLabel, "Avg nb of classes in models", dataset2, 
				PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);


		XYSeriesCollection datasetNbPa = new XYSeriesCollection();
		series3 = new XYSeries("Avg nb of models in sets");
		datasetNbPa.addSeries(series3);
		chartSize2 = ChartFactory.createXYLineChart("", 
				xAxisLabel, "Avg nb of models in sets", datasetNbPa, 
				PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		
		XYSeriesCollection datasetExtra1 = new XYSeriesCollection();
		series4 = new XYSeries("# of paretos");
		datasetExtra1.addSeries(series4);
		chartExtra1 = ChartFactory.createXYLineChart("", 
				xAxisLabel, "# of paretos", datasetExtra1, 
				PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);


		XYSeriesCollection datasetExtra2 = new XYSeriesCollection();
		series5 = new XYSeries("Avg nb of models in front");
		datasetExtra2.addSeries(series5);
		chartExtra2 = ChartFactory.createXYLineChart("", 
				xAxisLabel, "# of models in front", datasetExtra2, 
				PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		
		customizeCharts();
		
		
		
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(new ChartPanel(chartObj), BorderLayout.NORTH);
		
		JPanel jpSizes = new JPanel(new GridLayout(1,0));
		jpSizes.setPreferredSize(new Dimension(300,200));
		jpSizes.add(new ChartPanel(chartSize2));
		jpSizes.add(new ChartPanel(chartSize1));
		jp.add(jpSizes, BorderLayout.CENTER);
		JPanel jpExtras = new JPanel(new GridLayout(1,0));
		jpExtras.setPreferredSize(new Dimension(300,200));
		jpExtras.add(new ChartPanel(chartExtra1));
		jpExtras.add(new ChartPanel(chartExtra2));
		jp.add(jpExtras, BorderLayout.SOUTH);

		int v=ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		int h=ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS; 
		JScrollPane jsp=new JScrollPane(jp,v,h);
		jsp.setPreferredSize(new Dimension(600,800));
		jsp.setBounds(150,670,600,200);

		return jsp;
	}

	public void printInPNG(String fileName) {
		File imageFile = new File(fileName);
		int width = 800;
		int height = 600;
		
		try {
			ChartUtilities.saveChartAsPNG(imageFile, chartObj, width, height);
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}
	XYSeries series2, series3, series4, series5 ;
	ArrayList<XYSeries> seriesObjectives;
	XYLineAndShapeRenderer rendererObj, renderer2, renderer3, renderer4, renderer5;
	
	private XYDataset createDataset() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		seriesObjectives = new ArrayList<XYSeries>();
		
		XYSeries s;
		for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES; i++) {
			String name = "Curb's name";
			switch (i) {
				case FitnessVector.COVERAGE :
					name = "Coverage";
					break;
				case FitnessVector.DISSIMILARITY :
					name = "Dissimilarity";
					break;
				default :
					break;
			}
			s = new XYSeries(name);
			seriesObjectives.add(s);
			XYSeries sMax = new XYSeries(name +" first");
			seriesObjectives.add(sMax);
			dataset.addSeries(s);
			dataset.addSeries(sMax);
		}
		
		return dataset;
	}
	
	public void addObjective(int generation, float rate, double max, int rang) {
		seriesObjectives.get(rang*2).add((double)generation, rate);
		seriesObjectives.get(rang*2+1).add((double)generation, max);
	}
	public void addToGraph2(int generation, float rate) {
		series2.add((double)generation, rate);
	}
	public void addToGraph3(int generation, float rate) {
		series3.add((double)generation, rate);
	}
	public void addToGraph4(int generation, float rate) {
		series4.add((double)generation, rate);
	}
	public void addToGraph5(int generation, float rate) {
		series5.add((double)generation, rate);
	}
	
	private void customizeCharts() {
		XYPlot plotObj = chartObj.getXYPlot();
		rendererObj = new XYLineAndShapeRenderer();
		// sets paint color for each series
		rendererObj.setSeriesPaint(0, Color.PINK);
		rendererObj.setSeriesPaint(1, Color.MAGENTA);
		rendererObj.setSeriesPaint(2, Color.YELLOW);
		rendererObj.setSeriesPaint(3, Color.ORANGE);

		// sets thickness for series (using strokes)
		rendererObj.setSeriesStroke(0, new BasicStroke(2.0f));
		rendererObj.setSeriesShapesVisible(0, false);
		rendererObj.setSeriesStroke(2, new BasicStroke(2.0f));
		rendererObj.setSeriesShapesVisible(2, false);
//		renderer.setSeriesStroke(2, new BasicStroke(2.0f));
		
		for (int i = 0; i < Config.NUMBER_OF_OBJECTIVES*2; i++) { // *2 because we have the rate and the first
			rendererObj.setSeriesShapesVisible(i, false);
		}
		
		
		// sets paint color for plot outlines
		plotObj.setOutlinePaint(Color.BLUE);
		plotObj.setOutlineStroke(new BasicStroke(1.0f));
		
		// sets renderer for lines
		plotObj.setRenderer(rendererObj);
		
		// sets plot background
		plotObj.setBackgroundPaint(Color.DARK_GRAY);
		
		// sets paint color for the grid lines
		plotObj.setRangeGridlinesVisible(true);
		plotObj.setRangeGridlinePaint(Color.BLACK);
		
		
		/*
		 * Dist chart
		 */
		XYPlot plot2 = chartSize1.getXYPlot();
		renderer2 = new XYLineAndShapeRenderer();
		plot2.setRenderer(renderer2);
		plot2.setBackgroundPaint(Color.DARK_GRAY);
		// sets paint color for each series
		renderer2.setSeriesPaint(0, Color.GREEN);
		renderer2.setSeriesShapesVisible(0, false);
		
		/*
		 * NbPa chart
		 */
		XYPlot plot3 = chartSize2.getXYPlot();
		renderer3 = new XYLineAndShapeRenderer();
		plot3.setRenderer(renderer3);
		plot3.setBackgroundPaint(Color.DARK_GRAY);
		// sets paint color for each series
		renderer3.setSeriesPaint(0, Color.GREEN);
		renderer3.setSeriesShapesVisible(0, false);

		/*
		 * NbPa chart
		 */
		XYPlot plot4 = chartExtra1.getXYPlot();
		renderer4 = new XYLineAndShapeRenderer();
		plot4.setRenderer(renderer4);
		plot4.setBackgroundPaint(Color.DARK_GRAY);
		// sets paint color for each series
		renderer4.setSeriesPaint(0, Color.GREEN);
		renderer4.setSeriesShapesVisible(0, false);

		XYPlot plot5 = chartExtra2.getXYPlot();
		renderer5 = new XYLineAndShapeRenderer();
		plot5.setRenderer(renderer5);
		plot5.setBackgroundPaint(Color.DARK_GRAY);
		// sets paint color for each series
		renderer5.setSeriesPaint(0, Color.GREEN);
		renderer5.setSeriesShapesVisible(0, false);
		
	}
	
}
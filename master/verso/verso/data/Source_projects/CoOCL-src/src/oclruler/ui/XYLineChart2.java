package oclruler.ui;

import oclruler.genetics.GeneticEntity;
import oclruler.genetics.Evolutioner;
import oclruler.genetics.Population;
import oclruler.genetics.FitnessVector;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import utils.Config;

public class XYLineChart2 extends JPanel {
	private static final double MAX_Y_AXIS = 1.01;
	private static final double MIN_Y_AXIS = 0.00;
	
	
	
	JFreeChart chartObj ;
	JFreeChart chartSeries2 ;
	JFreeChart chartSeries3 ;
	JFreeChart chartSeries4 ;
	JFreeChart chartSeries5 ;
	
	XYSeries series2, series3, series4, series5 ;
	ArrayList<XYSeries> seriesObjectives;
	XYLineAndShapeRenderer rendererObj, renderer2, renderer3, renderer4, renderer5;

	
	String metamodelName;
	String xAxisLabel = "Generation";
	
	public XYLineChart2(String metamodelName, String timeStamp) {
		super(new BorderLayout());
		
		String chartTitle = metamodelName+" - "+Evolutioner.GENERATION_MAX+"g x "+Population.POPULATION_SIZE+"prg ";
		boolean showLegend = true;
		boolean createURL = true;
		boolean createTooltip = true;
		
		XYDataset datasetObj = createDataset();
	
		chartObj = ChartFactory.createXYLineChart(chartTitle, 
				xAxisLabel, "Objective", datasetObj, 
				PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		this.add(new ChartPanel(chartObj), BorderLayout.NORTH);
		
		
		
		XYSeriesCollection dataset2 = new XYSeriesCollection();
		series2 = new XYSeries("Plot2");
		dataset2.addSeries(series2);
		
		chartSeries2 = ChartFactory.createXYLineChart("", 
				xAxisLabel, "Plot2", dataset2, 
				PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);


		XYSeriesCollection dataset3 = new XYSeriesCollection();
		series3 = new XYSeries("Plot3");
		dataset3.addSeries(series3);
		chartSeries3 = ChartFactory.createXYLineChart("", 
				xAxisLabel, "Plot3", dataset3, 
				PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		
		XYSeriesCollection dataset4 = new XYSeriesCollection();
		series4 = new XYSeries("Plot4");
		dataset4.addSeries(series4);
		chartSeries4 = ChartFactory.createXYLineChart("", 
				xAxisLabel, "Plot4", dataset4, 
				PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);


		XYSeriesCollection dataset5 = new XYSeriesCollection();
		series5 = new XYSeries("Plot5");
		dataset5.addSeries(series5);
		chartSeries5 = ChartFactory.createXYLineChart("", 
				xAxisLabel, "Plot5", dataset5, 
				PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		
		customizeCharts();
		
		this.add(new ChartPanel(chartObj), BorderLayout.NORTH);
		
		JPanel jpSizes = new JPanel(new GridLayout(1,0));
		jpSizes.setPreferredSize(new Dimension(300,200));
		jpSizes.add(new ChartPanel(chartSeries3));
		jpSizes.add(new ChartPanel(chartSeries2));
		this.add(jpSizes, BorderLayout.CENTER);
		JPanel jpExtras = new JPanel(new GridLayout(1,0));
		jpExtras.setPreferredSize(new Dimension(300,200));
		jpExtras.add(new ChartPanel(chartSeries4));
		jpExtras.add(new ChartPanel(chartSeries5));
		this.add(jpExtras, BorderLayout.SOUTH);

	}
	
	private XYDataset createDataset() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		seriesObjectives = new ArrayList<XYSeries>();
		
		XYSeries s;
		for (int i = 0; i < FitnessVector.NUMBER_OF_OBJECTIVES; i++) {
			String curbName = FitnessVector.getObjectiveDescription(i);
			
			s = new XYSeries(curbName);
			seriesObjectives.add(s);
			dataset.addSeries(s);
			
			if(Ui.PRINT_FIRSTS){
				XYSeries sMax = new XYSeries(curbName +" first");
				seriesObjectives.add(sMax);
				dataset.addSeries(sMax);
			}
		}
		
		return dataset;
	}
	
	public void addObjective(int generation, float rate, double max, int rang) {
		if(Ui.PRINT_FIRSTS){
			seriesObjectives.get(rang*2).add((double)generation, rate);
			seriesObjectives.get(rang*2+1).add((double)generation, max);
		} else {
			seriesObjectives.get(rang).add((double)generation, rate);
		}
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
		final NumberAxis rangeAxis = new NumberAxis("Y-Axis");
		rangeAxis.setRange(MIN_Y_AXIS,MAX_Y_AXIS);
		rangeAxis.setTickUnit(new NumberTickUnit(1));
		plotObj.setRangeAxis(rangeAxis);
		
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
		
		int nbCurbs = Ui.PRINT_FIRSTS? FitnessVector.NUMBER_OF_OBJECTIVES*2 : FitnessVector.NUMBER_OF_OBJECTIVES;
		for (int i = 0; i < nbCurbs; i++) { // *2 because we have the rate and the first
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
		XYPlot plot2 = chartSeries2.getXYPlot();
		renderer2 = new XYLineAndShapeRenderer();
		plot2.setRenderer(renderer2);
		plot2.setBackgroundPaint(Color.DARK_GRAY);
		// sets paint color for each series
		renderer2.setSeriesPaint(0, Color.GREEN);
		renderer2.setSeriesShapesVisible(0, false);
		
		/*
		 * NbPa chart
		 */
		XYPlot plot3 = chartSeries3.getXYPlot();
		renderer3 = new XYLineAndShapeRenderer();
		plot3.setRenderer(renderer3);
		plot3.setBackgroundPaint(Color.DARK_GRAY);
		// sets paint color for each series
		renderer3.setSeriesPaint(0, Color.GREEN);
		renderer3.setSeriesShapesVisible(0, false);

		/*
		 * NbPa chart
		 */
		XYPlot plot4 = chartSeries4.getXYPlot();
		renderer4 = new XYLineAndShapeRenderer();
		plot4.setRenderer(renderer4);
		plot4.setBackgroundPaint(Color.DARK_GRAY);
		// sets paint color for each series
		renderer4.setSeriesPaint(0, Color.GREEN);
		renderer4.setSeriesShapesVisible(0, false);

		XYPlot plot5 = chartSeries5.getXYPlot();
		renderer5 = new XYLineAndShapeRenderer();
		plot5.setRenderer(renderer5);
		plot5.setBackgroundPaint(Color.DARK_GRAY);
		// sets paint color for each series
		renderer5.setSeriesPaint(0, Color.GREEN);
		renderer5.setSeriesShapesVisible(0, false);
		
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

	/**
	 * MONO NOT IMPLEMENTED !
	 */
	public void setMonoObjective() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		seriesObjectives = new ArrayList<XYSeries>();
		
		XYSeries s;
			String name = "MONO NOT IMPLEMENTED";//+GeneticEntity.getMonoValueString();
			s = new XYSeries(name);
			seriesObjectives.add(s);
			XYSeries sMax = new XYSeries(name +" first");
			seriesObjectives.add(sMax);
			dataset.addSeries(s);
			dataset.addSeries(sMax);
			
		chartObj = ChartFactory.createXYLineChart("Mono-objective", 
				"Generation", name, dataset, 
				PlotOrientation.VERTICAL, false, true, true);
		this.add(new ChartPanel(chartObj), BorderLayout.NORTH);
	}

}

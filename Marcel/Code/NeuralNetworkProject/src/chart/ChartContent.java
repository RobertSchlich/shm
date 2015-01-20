package chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartContent extends JPanel {

	private static final long serialVersionUID = 1L;

	private String IDordinate = "acceleration in [g]";
	private String IDabszisse = "time in [ms]";
	private String title = "";

	private int nodeIndex;
	private int experimentIndex;

	public ChartContent(int nodeIndex, int experimentIndex, int sample_period) {

		this.nodeIndex = nodeIndex;
		this.experimentIndex = experimentIndex;
		
		title = "experiment nr. " + experimentIndex + " with " + (1000/sample_period) + " Hz";

		ChartPanel chartPanel = createPanel(); // ChartPanel initialisieren
													// und daten laden
		chartPanel.setPreferredSize(new Dimension(400, 400)); // Größe festlegen
		this.setLayout(new BorderLayout(0, 0));
		this.add(chartPanel); // auf Frame anordnen
	}// Ende stand.konstr.

	/**
	 * Creates and formats the chart for the view of the acceleration data.
	 * 
	 * @return The chart panel.
	 */
	private ChartPanel createPanel() {

		// Chart erstellen
		JFreeChart jfreechart = ChartFactory.createXYLineChart( // LineChart zum
																// verbinden der
																// Punkte
				title, // titel
				IDabszisse, // x-achse
				IDordinate, // y-achse
				createData(), // Datensatz
				PlotOrientation.VERTICAL, // ausrichtung plot
				true, // Legende
				true, // Tooltips
				false); // urls

		// Ploteigenschaften
		XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
		xyPlot.setDomainCrosshairVisible(true); // Fadenkreuz
		xyPlot.setRangeCrosshairVisible(true);
		xyPlot.setBackgroundPaint(Color.white); // Hintergrund
		xyPlot.setDomainGridlinePaint(Color.black); // Raster
		xyPlot.setRangeGridlinePaint(Color.black);

		// renderer
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.blue); // Farbe 1. datensatz
		renderer.setSeriesLinesVisible(0, true);// linien sichtbar?
		renderer.setSeriesShapesVisible(0, false);// punkte sichtbar?
		renderer.setSeriesPaint(1, Color.red); // Farbe 2. datensatz
		renderer.setSeriesLinesVisible(1, true);// linien sichtbar?
		renderer.setSeriesShapesVisible(1, false);// punkte sichtbar?
		// Achsenformatieren

		xyPlot.setRenderer(renderer);

		ChartPanel cp = new ChartPanel(jfreechart);
		cp.restoreAutoBounds(); // optimaler zoom

		return cp;

	}// Ende createPanel()

	/**
	 * Collects all the important data for the chart.
	 * 
	 * @return A data set for the creation of the chart.
	 */
	private XYDataset createData() {

		XYSeries measurementSeries = new XYSeries("measurement node " + (nodeIndex+1), false);
		XYSeries calculationSeries = new XYSeries("calculation node " + (nodeIndex+1), false);

		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

		DataReader dr = new DataReader(nodeIndex, experimentIndex);
		
		try {
			dr.readData("measurement");

			ArrayList<Double> acceleration = dr.getAcceleration();
			ArrayList<Integer> time = dr.getTime();

			// gemessene Werte
			for (int i = 0; i < acceleration.size(); i++) {
				double acc = acceleration.get(i);
				int t = time.get(i);
				measurementSeries.add(t, acc);
			}
			
			dr.readData("calculation");

			acceleration = dr.getAcceleration();
			time = dr.getTime();
			
			// berechnete Werte
			for (int i = 0; i < acceleration.size(); i++) {
				double acc = acceleration.get(i);
				int t = time.get(i);
				calculationSeries.add(t, acc);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		xySeriesCollection.addSeries(measurementSeries);
		xySeriesCollection.addSeries(calculationSeries);
		
		return xySeriesCollection;
	}// Ende createData()
	
}

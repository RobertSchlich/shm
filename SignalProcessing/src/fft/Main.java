package fft;

import java.io.FileNotFoundException;
import javax.swing.JFrame;

import org.math.plot.*;

public class Main
{
	public static void main(String[] args) throws FileNotFoundException
    {

		double[] inputReal = GetCSV.Load("/media/katrin/Data/eigeneDateien/Uni/Master/5/SHM/SHMCode/simdata.txt");
		double[] inputImag = new double[inputReal.length];
		boolean DIRECT = Boolean.TRUE;
		double samplerate = 512.; // example samplerate

		// perform fft
		double[] transformedBase = FFTbase.fft(inputReal, inputImag, DIRECT);
		
		// calculate magnitude and frequencies
		double [] magnitude = FFTbase.calculateMagnitude(transformedBase);
		double[] frequency = FFTbase.calculateFrequency(transformedBase, samplerate);
        
        // plot frequency spectrum
        plot(frequency, magnitude);
              
    }
	
	private static void plot(double[] frequency, double[] magnitude)
	{
		Plot2DPanel panel = new Plot2DPanel();
        panel.addLinePlot("Line", frequency, magnitude);
        	
        JFrame  frame= new JFrame("frequency spectrum");
        frame.setContentPane(panel);
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}
}
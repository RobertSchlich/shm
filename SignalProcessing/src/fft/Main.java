package fft;

import java.io.FileNotFoundException;
import javax.swing.JFrame;

import org.math.plot.*;


// Testprogramm f�r die Klasse Fft;
// zus�tzlich wird die Klasse Complex ben�tigt
public class Main
{
	// Für FFTbase
	public static void main(String[] args) throws FileNotFoundException
    {

		double[] inputReal = GetCSVFFTbase.Load("/media/katrin/Data/eigeneDateien/Uni/Master/5/SHM/SHMCode/simdata.txt");
		double[] inputImag = new double[inputReal.length];
		boolean DIRECT = Boolean.TRUE;

		double[] transformedBase = FFTbase.fft(inputReal, inputImag, DIRECT);
		int nummeas = transformedBase.length / 2;

		// calculate magnitude as norm of imaginary and real part 
        double magnitude[] = new double[nummeas];

        for (int j=0; j<nummeas*2; j+=2)
        {
        	magnitude[j/2] = Math.sqrt(  Math.pow(transformedBase[j],2) +  Math.pow(transformedBase[j+1],2) );
        }
        
        // calculate frequencies
    	double frequency[] = new double[nummeas];
        double samplerate = 512.0; //example value
          
        for (int i=0; i<nummeas; i++)
        {
        	frequency[i] = i * samplerate / nummeas;
        }
        
        // plot frequency spectrum
		Plot2DPanel panel = new Plot2DPanel();
        panel.addLinePlot("Line", frequency, magnitude);
        	
        JFrame  frame= new JFrame("frequency spectrum");
        frame.setContentPane(panel);
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
    }
	
	/*
	public static void main(String[] args) throws FileNotFoundException
    {
    
    	Complex[] y = GetCSV.Load("/media/katrin/Data/eigeneDateien/Uni/Master/5/SHM/SHMCode/simdata.txt");
    	
        Complex[] transformed = Fft.transform(y);    // Aufruf von transform

            
        double[] magnitude = Fft.magnitude(transformed);

        double samplerate = 500.0;
        double[] frequency = Fft.freq(transformed, samplerate);
       
        plot(frequency, magnitude);
            
        out(transformed);     
    }
    
    */
	
	private static void plot(double[] frequency, double[] magnitude)
	{
		Plot2DPanel panel = new Plot2DPanel();
        panel.addLinePlot("Line", frequency, magnitude);
        	
        JFrame  frame= new JFrame("Histogram");
        frame.setContentPane(panel);
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}
    

    
    private static void out(Double[] frequency)
    {
        int i, n=frequency.length;
        for (i=0; i<n; i++)
            System.out.println(frequency[i]);
    }
    
}   

package sensornode;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.util.Utils;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.service.BootloaderListenerService;
//import java.io.Writer;


public class MainSpot extends MIDlet {

    private int HOST_PORT = 67;
    private int SAMPLE_PERIOD_LISTENING = 1 * 500;  // in milliseconds
    private int SAMPLE_PERIOD_MEASURING = 13;  // in milliseconds
    private int ARRAY_LENGTH = 512;
    private double SAMPLERATE = 1000. / (double)SAMPLE_PERIOD_MEASURING;
    private double THRESHOLD = 0.2;
    
    protected void startApp() throws MIDletStateChangeException {

        // Listen for downloads/commands over USB connection
    	new BootloaderListenerService().getInstance().start();

    	// initalize communication between spot and base
		Communication communication = new Communication();
		communication.EstablishConnection(HOST_PORT);
    	
        //initialize AccelerationSampler
        AccelerationSampler sensorSampler = new AccelerationSampler();
        while (true){
	        // get acceleration
	        double[] accelerationArray = sensorSampler.getaccelerationArray
					        (SAMPLE_PERIOD_LISTENING, SAMPLE_PERIOD_MEASURING, 
					         ARRAY_LENGTH, THRESHOLD);
	        
	        // perform fft
			double[] transform = FFT.performFFT(accelerationArray, 
									new double[accelerationArray.length], true);
			// calculate magnitude, frequencies and natural frequency
			double[] magnitude = FFT.calcMagnitude(transform);
			double[] frequency = FFT.calcFreq(transform, SAMPLERATE);
			double[] naturalFreq =  FFT.calcNaturalFreq(magnitude, frequency);
			
			
			// print frequency and magnitude array on console
			System.out.println("Magnitude");
			for (int i = 0 ; i < ARRAY_LENGTH; i++){
				System.out.println(magnitude[i]);
			}			
			System.out.println("Frequency");
			for (int i = 0 ; i < ARRAY_LENGTH; i++){
				System.out.println(frequency[i]);
			}
			System.out.println("natural magnitude: " + naturalFreq[0]);
			System.out.println("natural Frequency: " + naturalFreq[1]);
			
			
			/*
			
			// print data to file
			Writer.PrintWriter writer = new Writer.PrintWriter("measurement.txt", "UTF-8");
			
			writer.println("Magnitude");
			for (int i = 0 ; i < ARRAY_LENGTH; i++){
				writer.println(magnitude[i]);
			}	
			writer.println("Frequency");
			for (int i = 0 ; i < ARRAY_LENGTH; i++){
				writer.println(frequency[i]);
			}
			writer.println("natural magnitude: " + naturalFreq[0]);
			writer.println("natural Frequency: " + naturalFreq[1]);
			writer.close();
			
			*/
			
			//send processed data to basestation
			communication.SendMeasurement(naturalFreq[0], naturalFreq[1]);
			Utils.sleep(2000);
		}   
    }
    
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }
    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than 
    	// MIDletStateChangeException
    }
}

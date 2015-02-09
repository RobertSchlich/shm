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


public class MainSpot extends MIDlet {

    private int HOST_PORT = 67;
    private int SAMPLE_PERIOD_LISTENING = 1 * 1000;  // in milliseconds
    private int SAMPLE_PERIOD_MEASURING = 100;  // in milliseconds
    private int ARRAY_LENGTH = 128;
    private double SAMPLERATE = 1000. / (double)SAMPLE_PERIOD_MEASURING;
    private double THRESHOLD = 0.1;
    
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

			//send processed data to database
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

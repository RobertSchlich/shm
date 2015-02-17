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
    private int SAMPLE_PERIOD_MEASURING = 20;  // in milliseconds
    private int ARRAY_LENGTH = 256;
    private double SAMPLERATE = 1000. / (double)SAMPLE_PERIOD_MEASURING;
    private double THRESHOLD = 0.15;
    private int NUMBER_OF_OTHER_SENSORS = 1;
    
    
    private String ourAddress = System.getProperty("IEEE_ADDRESS");
    private String otherAddress = "0014.4F01.0000.763F";
    
    protected void startApp() throws MIDletStateChangeException {

        // Listen for downloads/commands over USB connection
    	new BootloaderListenerService().getInstance().start();
    	// initalize communication between spots
		CommunicationNormal communication = new CommunicationNormal();
		
        //initialize AccelerationSampler
        AccelerationSampler sensorSampler = new AccelerationSampler();
		
       
        while (true){
	        // get acceleration for this sensornode
	        double[] accelerationArray = sensorSampler.getaccelerationArray
					        (SAMPLE_PERIOD_LISTENING, SAMPLE_PERIOD_MEASURING, 
					         ARRAY_LENGTH, THRESHOLD);	        
	        // perform fft
			double[] transform = FFT.performFFT(accelerationArray, 
									new double[accelerationArray.length], true);
			// calculate magnitude, frequencies and natural frequency
			double[] magnitude = FFT.calcMagnitude(transform);
			double[] frequency = FFT.calcFreq(transform, SAMPLERATE);
			// instanziate und initialize measurement object
			Measurement ownMeas =  FFT.calcNaturalFreq(magnitude, frequency, ourAddress);
			System.out.println("I did my FFT!");
			
			// exchange measurement with measurement of other sensors
			communication.ExchangeData(ownMeas, otherAddress, HOST_PORT);
			

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

/**
 * @author Jahr&Schlich
 * 
 */

package sensornodeNN;

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
	
	// constants for measurement
    private int SAMPLE_PERIOD_LISTENING = 1 * 100;  // in milliseconds
    private int SAMPLE_PERIOD_MEASURING = 20;  // in milliseconds
    private int ARRAY_LENGTH = 256;
    private double SAMPLERATE = 1000. / (double)SAMPLE_PERIOD_MEASURING;
    private double THRESHOLD = 0.2;

    // constants for communication
    private String BASE_NAME = "0014.4F01.0000.77BA";
    private String[] SENSOR_NAMES = {"0014.4F01.0000.792D",
    								"0014.4F01.0000.7840",
    								"0014.4F01.0000.7AFA"}
    private String ourAddress = System.getProperty("IEEE_ADDRESS");
    private int HOST_PORT = 67;
    private int HOST_PORT_BASE = 68;
    
    //  constants for neural network
    private int HIDDEN_UNITS = 3;
    private int trainingEvents = 6;

    protected void startApp() throws MIDletStateChangeException {
		ITriColorLED led4 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED4");
		int numberOfSensors = SENSOR_NAMES.length;	
		
        // Listen for downloads/commands over USB connection
    	new BootloaderListenerService().getInstance().start();
    	// initalize communication between spots
		CommunicationNN communication = new CommunicationNN();
		//initialize AccelerationSampler
        AccelerationSampler sensorSampler = new AccelerationSampler();
        
		// NEURAL NETWORK CREATION
		// create descriptor
		NeuralNetworkDescriptor descriptorNN = 
				new NeuralNetworkDescriptor(numberOfSensors, HIDDEN_UNITS, 1);
    	descriptorNN.setSettingsTopologyFeedForward();
    	// create neural network
    	NeuralNetwork nn = new NeuralNetwork(descriptorNN);
    	System.out.println("neural network created");
    	
    	// SAMPLING TRAINING DATA
    	// build input and output 2D arrays with measured data
    	double[][] inputs = new double[trainingEvents][numberOfSensors];
    	double[][] desiredOutputs = new double [trainingEvents][1];
    	
    	/*
    	 * TRAINING PHASE
    	 */
    	
    	// measure, listen to data from other sensors and 
    	//fill inputs and desiredOutputs
    	int event = 0;
        while (event < trainingEvents ){
        	
        	// MEASUREMENTS
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
			
			// write magnitude to NN output array
			desiredOutputs[event][0] = ownMeas.magnitude; 
			System.out.println(ownMeas.address +" magnitude: " + ownMeas.magnitude);
			
			// COMMUNICATION
			Measurement othMeas[] = new Measurement[numberOfSensors];
			// receive measurements from other sensors
			for(int sensor = 0; sensor<numberOfSensors; sensor++){
				//talk to next sensor in list
				String otherAddress = SENSOR_NAMES[sensor];
				othMeas[sensor] = communication.ReceiveData(otherAddress, HOST_PORT);
				
				// write magnitude to NN input array
				inputs[event][sensor] = othMeas[sensor].magnitude;	
				Utils.sleep(500);	
			}
			event++;
        }	

		// NEURAL NETWORK TRAINING
    	// create lesson from collected measurements
    	TrainingSampleLesson lesson = new TrainingSampleLesson(inputs, desiredOutputs);
    	System.out.println("sample lesson created");	    	

        // train the neural network
        NetworkTraining training = new NetworkTraining();
        training.Train(nn, lesson);
        // blink third LED
        led4.setRGB(0, 255, 255); led4.setOn(); Utils.sleep(500); led4.setOff();
        
    	/*
    	 * COLLECTION PHASE
    	 */
        
        while (true){
        	
        	// MEASUREMENT
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
			
			// array as input for neural network
			double[] magnitudes = new double[numberOfSensors];
			// COMMUNICATION
			Measurement othMeas[] = new Measurement[numberOfSensors];
			// receive measurements from other sensors
			for(int sensor = 0; sensor<numberOfSensors; sensor++){
				//talk to next sensor in list
				String otherAddress = SENSOR_NAMES[sensor];
				othMeas[sensor] = communication.ReceiveData(otherAddress, HOST_PORT);
				
				// write magnitude to input arry of neural network
				magnitudes[sensor] = othMeas[sensor].magnitude;	
			}
			
			
			//FAULT DETECTION
			// use neural network to predict magnitude
			ownMeas.prediction = nn.propagate(magnitudes)[0];
			// calculate error between predicted and measured magnitude
			double errorNN = Math.abs(ownMeas.prediction-ownMeas.magnitude) /  
															ownMeas.prediction;
			
			System.out.println("expMeas: " + ownMeas.prediction + " ownMeas: " + 
															ownMeas.magnitude);
			System.out.println("deviation: measured/expected: " + errorNN*100. + "%");
			
			//SEND DATA TO BASESTATION
			Measurement[] allMeas = new Measurement[othMeas.length+1];
			//insert all Measurements in one array
			allMeas[0]=ownMeas;
			for (int i=0; i<othMeas.length;i++) allMeas[i+1]=othMeas[i];
			// transfer all measurements to basestation
			communication.StoreData(allMeas, HOST_PORT_BASE, BASE_NAME);
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

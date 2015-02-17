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
	
	// measurement
    private int SAMPLE_PERIOD_LISTENING = 1 * 500;  // in milliseconds
    private int SAMPLE_PERIOD_MEASURING = 20;  // in milliseconds
    private int ARRAY_LENGTH = 256;
    private double SAMPLERATE = 1000. / (double)SAMPLE_PERIOD_MEASURING;
    private double THRESHOLD = 0.2;

    // communication
    private String BASE_NAME = "0014.4F01.0000.77BA";
    private String[] SENSOR_NAMES = {"0014.4F01.0000.792D",
    								"0014.4F01.0000.7840"};
//    								"0014.4F01.0000.7AFA"}
    private String ourAddress = System.getProperty("IEEE_ADDRESS");
    private int HOST_PORT = 67;
    private int HOST_PORT_BASE = 68;
    
    
    // neural network
    private int HIDDEN_UNITS = 3;
    
    private int trainingEvents = 1;

    protected void startApp() throws MIDletStateChangeException {

        // Listen for downloads/commands over USB connection
    	new BootloaderListenerService().getInstance().start();
    	// initalize communication between spots
		CommunicationNN communication = new CommunicationNN();
		
		//Radiogram for communication to basestation
		//communication.EstablishConnection(HOST_PORT);
		
        //initialize AccelerationSampler
        AccelerationSampler sensorSampler = new AccelerationSampler();
		
    	ITriColorLED led4 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED4");
    	
		int numberOfSensors = SENSOR_NAMES.length;	
		
		// NEURAL NETWORK CREATION
		// descriptor
		NeuralNetworkDescriptor descriptorNN = new NeuralNetworkDescriptor( numberOfSensors, HIDDEN_UNITS,  1);
    	descriptorNN.setSettingsTopologyFeedForward();

    	// create neural network
    	NeuralNetwork nn = new NeuralNetwork(descriptorNN);
    	System.out.println("neural network created");
    	
    	// SAMPLING TRAINING DATA
    	// input and output 2D arrays
    	double[][] inputs = new double[trainingEvents][numberOfSensors];
    	double[][] desiredOutputs = new double [trainingEvents][1];

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
			//System.out.println("I did my FFT!");
			
			// write magnitude to NN output array
			desiredOutputs[event][0] = ownMeas.magnitude; 
			
			// COMMUNICATION
        	
			Measurement othMeas[] = new Measurement[numberOfSensors];
			// receive measurements from other sensors
			for(int sensor = 0; sensor<numberOfSensors; sensor++){
				//talk to next sensor in list
				String otherAddress = SENSOR_NAMES[sensor];
				othMeas[sensor] = communication.ReceiveData(otherAddress, HOST_PORT);
				
				// write magnitude to NN input array
				inputs[event][sensor] = othMeas[sensor].magnitude;	
			}
			
			event++;
        }
			

		// NEURAL NETWORK TRAINING
    	// create lessonMeasurement
    	TrainingSampleLesson lesson = new TrainingSampleLesson(inputs, desiredOutputs);
    	//System.out.println("sample lesson created");	    	

        // train the neural network
        NetworkTraining training = new NetworkTraining();
        training.Train(nn, lesson);
        // blink third LED
        led4.setRGB(0, 255, 255); led4.setOn(); Utils.sleep(500); led4.setOff();
        
        
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
			//System.out.println("I did my FFT!");
			
			double[] magnitudes = new double[numberOfSensors];
			// COMMUNICATION
			Measurement othMeas[] = new Measurement[numberOfSensors];
			// receive measurements from other sensors
			for(int sensor = 0; sensor<numberOfSensors; sensor++){
				//talk to next sensor in list
				String otherAddress = SENSOR_NAMES[sensor];
				othMeas[sensor] = communication.ReceiveData(otherAddress, HOST_PORT);
				
				// write magnitude to NN input array
				magnitudes[sensor] = othMeas[sensor].magnitude;	
			}
			
			
			//FAULT DETECTION
			double expMeas = nn.propagate(magnitudes)[0];
			double errorNN = Math.abs(expMeas-ownMeas.magnitude) /  ownMeas.magnitude;
			
			System.out.println("expMeas: " + expMeas + " ownMeas: " + ownMeas.magnitude);
			System.out.println("deviation measured / expected value" + errorNN*100. + "%!");
			
			double threshold = 0.01;
			
			if (errorNN > threshold) ownMeas.error = -1;
			Measurement NNmeas = new Measurement("NeuralNetwork", expMeas, 0, 0);
			
			
			if (errorNN > threshold) ownMeas.error = -1;
			else ownMeas.error = 1;

			//SEND DATA TO BASESTATION
			Measurement[] allMeas = new Measurement[othMeas.length+2];
			allMeas[0]=NNmeas;
			allMeas[1]=ownMeas;
			
			for (int i=2; i<othMeas.length+2;i++) allMeas[i]=othMeas[i-2];
			
			communication.StoreData(allMeas, HOST_PORT_BASE, BASE_NAME);
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

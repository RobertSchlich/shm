/*
 * SensorSampler.java
 *
 * Copyright (c) 2008-2010 Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package spot.src.org.sunspotworld.demo;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.spot.util.Utils;

import spot.src.org.sunspotworld.demo.communication.Format;
import spot.src.org.sunspotworld.demo.communication.LEDSignals;
import spot.src.org.sunspotworld.demo.communication.NodeCommunication;
import spot.src.org.sunspotworld.demo.communication.ProtocolGenerator;
import spot.src.org.sunspotworld.demo.measurement.Acceleration;
import spot.src.org.sunspotworld.demo.measurement.Calibration;
import spot.src.org.sunspotworld.demo.neuralnetwork.HiddenUnitCalculator;
import spot.src.org.sunspotworld.demo.neuralnetwork.NeuralNetwork;
import spot.src.org.sunspotworld.demo.neuralnetwork.NeuralNetworkDescriptor;
import spot.src.org.sunspotworld.demo.neuralnetwork.NetworkMethods;
import spot.src.org.sunspotworld.demo.spotfinder.NodeIdentifier;
import spot.src.org.sunspotworld.demo.synchronisation.SyncStart;
 
public class SensorSampler extends MIDlet {
    
	//Parameter
    private static int HOST_PORT = 66;
    private static int HIDDEN_UNITS = 2;
    
    private static double LEARNING_RATE = 0.1; 
    private static int SAMPLE_PERIOD = 40;
    private static int AMOUNT_OF_DATA = 512;
    private static double LEARN_LIM = 0.01;
    private static int LEARN_COUNT = 18000;
    private static int FACTOR = 1;
    
    
    void start() throws Exception{
    	
    	//Suche nach BaseStation
    	System.out.println("************************");
    	NodeIdentifier si = new NodeIdentifier(HOST_PORT);
    	System.out.println("\tsending datagrams for collecting other nodes...");
		si.sendSignal();
		System.out.print("\tsending test signal on port: ");
		si.testSignal();
		System.out.println("\treceiving addresses...");
		si.receiveAdresses();
		System.out.println("\treceiving parameters...");
		si.receiveParams();
		System.out.println("************************");
		
		//parameter überschreiben
		LEARNING_RATE = si.getLearningRate();
		SAMPLE_PERIOD = si.getSamplePeriod();
		AMOUNT_OF_DATA = si.getAmountOfData();
		LEARN_LIM = si.getLearnLim();
		LEARN_COUNT = si.getLearnCount();
		FACTOR = si.getFaktor();
		
		ProtocolGenerator protocol = new ProtocolGenerator(si.getPortOut(), si.getIndex(), si.getOtherAddress());
		
    	//neuronales Netzwerk
    	//Netzstruktur erzeugen
    	int sensor_count = si.getAdresses().length-1;	//count of other sensor nodes in the network
    	HIDDEN_UNITS = HiddenUnitCalculator.getHiddenUnits(sensor_count);
    	System.out.println("\tSensor count: " + sensor_count + "\n\thidden units: " + HIDDEN_UNITS);
    	NeuralNetworkDescriptor descriptor = new NeuralNetworkDescriptor( sensor_count, HIDDEN_UNITS,  1);
    	descriptor.setSettingsTopologyFeedForward();
    	
    	//Netzwerk erzeugen
    	System.out.println("\tinitializing neuronal network...");
    	NeuralNetwork nn = new NeuralNetwork(descriptor);
    	System.out.println("************************");
    	
    	//messen
    	//kalibrieren
    	Calibration calibration = new Calibration();
    	double correction = calibration.calibrate(SAMPLE_PERIOD);
    	//Beschleunigungsmesser initialisieren
    	Acceleration acceleration = new Acceleration(AMOUNT_OF_DATA, FACTOR);
    	//Korrekturwert übergeben
    	acceleration.setCorrection(correction);
    	//auf startschuss warten
    	SyncStart ss = new SyncStart(HOST_PORT);
    	ss.waitForSignal();
    	acceleration.startMeasurement(SAMPLE_PERIOD);
    	System.out.println("\tmeasurement completed");
    	System.out.println("************************");
    	
    	//kommunizieren
    	acceleration.setFloatingAverage();
    	double[][] newInput = new double[acceleration.getAccelerationArray().length][si.getAdresses().length-1];
    	double[][] desiredOutput = acceleration.getDesiredOutput();
    	double[] ownMeasurement = acceleration.getAccelerationArray();
    	System.out.println("\tstarting communication...");
    	NodeCommunication sc = new NodeCommunication(si.getPortOut(), si.getIndex(), sensor_count+1, si.getAdresses());
    	sc.communicate(ownMeasurement, newInput, sensor_count+1);
    	
    	//newinput[anzahl messwerte][anzahl sensoren - 1], desout[anzahlMesswerte][anzahl ausgänge]    	
    	NetworkMethods.autoNetworkTrain(nn,newInput,desiredOutput,sensor_count,HIDDEN_UNITS, LEARNING_RATE, LEARN_LIM, LEARN_COUNT);
    	System.out.println("************************");
    	    	
    	while(true){
	    	ss.waitForSignal();	//auf startschuss warten
	    	acceleration.startMeasurement(SAMPLE_PERIOD);
	    	System.out.println("\tmeasurement completed");
	    	acceleration.setFloatingAverage();	    	
	    	double [][] newInputTest = new double[acceleration.getAccelerationArray().length][si.getAdresses().length-1];
	    	double [][] desiredOutputTest = acceleration.getDesiredOutput();
	    	double[] ownMeasurementTest = acceleration.getAccelerationArray();
	    	System.out.println("\tstarting communication...");
	    	sc.communicate(ownMeasurementTest, newInputTest, sensor_count+1);
	    	
	    	//daten an  base station senden								 
	    	protocol.sendMeasuredData(ownMeasurementTest);
	    	
			double[] netout = NetworkMethods.autoNetworkTest(nn,newInputTest);
			Utils.sleep(300);
			
			//daten an  base station senden								 
	    	protocol.sendMeasuredData(netout);
	    	
			System.out.println("\tstarting comparison...");
	    	for(int i=0; i<netout.length; i++){
	    		System.out.println("\tmeasured: " + Format.format(desiredOutputTest[i][0]) + "\t" + " -> " + "calculated: " + Format.format(netout[i]));
	    	}
	    	
	    	System.out.println("************************");
    	}
    	
    }//Ende start()
 
    

    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
    }
 
    protected void pauseApp() {
    }
 
    protected void startApp() throws MIDletStateChangeException {
        SensorSampler rf = new SensorSampler();
        LEDSignals signals = new LEDSignals();
        try{
        	rf.start();
        } catch(Exception e){
        	e.printStackTrace();
        	signals.LEDSignalForException();
        }
    }
}
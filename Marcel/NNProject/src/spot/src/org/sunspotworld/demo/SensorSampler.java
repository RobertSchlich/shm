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

import spot.src.org.sunspotworld.demo.communication.LEDSignals;
import spot.src.org.sunspotworld.demo.communication.SpotCommunication;
import spot.src.org.sunspotworld.demo.measurement.Acceleration;
import spot.src.org.sunspotworld.demo.measurement.Calibration;
import spot.src.org.sunspotworld.demo.neuralnetwork.HiddenUnitCalculator;
import spot.src.org.sunspotworld.demo.neuralnetwork.NeuralNetwork;
import spot.src.org.sunspotworld.demo.neuralnetwork.NeuralNetworkDescriptor;
import spot.src.org.sunspotworld.demo.neuralnetwork.OwnNetworkMethods;
import spot.src.org.sunspotworld.demo.spotfinder.SpotIdentifier;
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
    private static int Faktor=1;
    
    
    void start() throws Exception{
    	
    	//Suche nach BaseStation
    	SpotIdentifier si = new SpotIdentifier(HOST_PORT);
    	System.out.println("Sende Datagram zum sammeln der Spots...");
		si.sendSignal();
		System.out.print("Sende Testsignal... port: ");
		si.testSignal();
		System.out.println("Empfange Adressen...");
		si.receiveAdresses();
		System.out.println("Empfange Parameter...");
		si.receiveParams();
		System.out.println("************************");
		
		//parameter überschreiben
		LEARNING_RATE = si.getLearning_rate();
		SAMPLE_PERIOD = si.getSample_period();
		AMOUNT_OF_DATA = si.getAmount_of_data();
		LEARN_LIM = si.getLEARN_LIM();
		LEARN_COUNT = si.getLEARN_COUNT();
		Faktor = si.getFaktor();
				 
    	//neuronales Netzwerk
    	//Netzstruktur erzeugen
    	int sensor_count = si.getAdresses().length-1;
    	HIDDEN_UNITS = HiddenUnitCalculator.getHiddenUnits(sensor_count);
    	System.out.println("Sensor count: " + sensor_count + "\nhidden units: " + HIDDEN_UNITS);
    	NeuralNetworkDescriptor descriptor = new NeuralNetworkDescriptor( sensor_count, HIDDEN_UNITS,  1);
    	descriptor.setSettingsTopologyFeedForward();
    	
    	//Netzwerk erzeugen
    	System.out.println("erzeuge neronales Netz...");
    	NeuralNetwork nn = new NeuralNetwork(descriptor);
    	System.out.println("erzeuge OwnNetworkMethods...");
    	OwnNetworkMethods onm= new OwnNetworkMethods();
    	System.out.println("************************");
    	
    	//messen
    	//kalibrieren
    	Calibration calibration = new Calibration();
    	double correction = calibration.calibrate(SAMPLE_PERIOD);
    	//Beschleunigungsmesser initialisieren
    	Acceleration acceleration = new Acceleration(AMOUNT_OF_DATA, Faktor);
    	//Korrekturwert übergeben
    	acceleration.setCorrection(correction);
    	//auf startschuss warten
    	SyncStart ss = new SyncStart(HOST_PORT);
    	ss.waitForSignal();
    	acceleration.startMeasurement(SAMPLE_PERIOD);
    	
    	//kommunizieren
    	double[][] newinput = new double[acceleration.getAccelerationArray().length][si.getAdresses().length-1];
    	double[][] desout = new double[acceleration.getAccelerationArray().length][1];
    	double[] temp = new double[acceleration.getAccelerationArray().length];
    	SpotCommunication sc = new SpotCommunication(si.getPortOut(), si.getIndex(), sensor_count+1, si.getAdresses());
    	System.out.println("Eigene Werte");
    	temp=acceleration.getAccelerationArray();
    	for(int i = 0; i<temp.length;i++){
    		desout[i][0]=temp[i];
    	System.out.println("\t"+temp[i]);
    	}
    	sc.communicate(temp, newinput, sensor_count+1);
    	   
    	//eigene werte in desout
    	
    	
    	
    	//newinput[anzahl messwerte][anzahl sensoren - 1], desout[anzahlMesswerte][anzahl ausgänge]
    	
    	onm.autoNetworktrain(nn,newinput,desout,sensor_count,HIDDEN_UNITS, LEARNING_RATE, LEARN_LIM, LEARN_COUNT);
//    	
//
//    	//messen
//    	//... -> desout
//    	
//    	//kommunizieren
//    	//... -> newinput
    	
    	//auf startschuss warten
    	while(true){
    	ss.waitForSignal();
    	acceleration.startMeasurement(SAMPLE_PERIOD);
    	
    	double [][] newinputtest = new double[acceleration.getAccelerationArray().length][si.getAdresses().length-1];
    	double [][] desouttest = new double[acceleration.getAccelerationArray().length][1];
    	double[] temptest = new double[acceleration.getAccelerationArray().length];
    	
    	temptest=acceleration.getAccelerationArray();
    	for(int i = 0; i<temp.length;i++)
    		desouttest[i][0]=temptest[i];
    	
    	sc.communicate(temptest, newinputtest, sensor_count+1);
    	
//bitte netout und das desouttest von 2 zeilen weiter oben visualisieren in einem grafen und die vorm training ermittelten beschleunigungen in einem graphen     	
    	
		double[] netout = onm.autoNetworktest(nn,newinputtest);
    	System.out.println("Messung abgeschlossen, beginne Vergleich");
    	for(int i=0;i<netout.length;i++)
    		System.out.println("Vergleich: "+"gemessen:\t"+desouttest[i][0]+"\t"+" = "+"errechnet:\t"+netout[i]+"\t"+" ?");
    	}
//    	
//    	netmet.autoNetworktest(nn,newinput); //+ mit eigenen werten vergleichen
    	
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
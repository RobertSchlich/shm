package spot.src.org.sunspotworld.demo.neuralnetwork;				// eastereggs inside 



import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
//import com.sun.spot.peripheral.IBattery;
//import com.sun.spot.peripheral.IPowerController;
import com.sun.spot.peripheral.TimerCounterBits;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.sensorboard.peripheral.TriColorLEDArray;
import com.sun.spot.service.BootloaderListenerService;
import com.sun.spot.util.Utils;

public class SensorSampler extends MIDlet implements TimerCounterBits {

    private static final int 	HOST_PORT = 253;	// NOT 0..36
    private static final int 	GRENZE=32;			// how much zero mesurements until start calculating
    private static final double unull=0.05;			// below this all measurements are zero		
    private static final long 	Intervall=25;   	// in ms - measuring intervall 25ms is 40Hz
    private static final double lowPeak=0.20;		// 0..1 to terminate noise
    private static final double bandwithsize=1.0;	// for peak detection - if peaks are nearer then bandwith*bandwithsize then higher peak is saved
    private static final int 	MAXFREQ=23;			// frequency where calculation stops (Hz)
    private static final double divLED=0.015;		// value pro LED
    private static int 	Sensorcount=2;				// later gotten from comunication protokol
    private static final int HiddenUnits=2;			// later calibrated dependend on SensorCount
    
    
   
	
    protected void startApp() throws MIDletStateChangeException {
    	
//    	hier muss jetzt Sensorcount übertragen und HiddenUnit ermittelt werden
    	
/**		Bereich für Objecterzeugung  	
 */
    	
//    	NeuralNetwork
    	NeuralNetworkDescriptor desc = new NeuralNetworkDescriptor(Sensorcount, HiddenUnits,  1);
    	desc.setSettingsTopologyFeedForward(); 
    	NeuralNetwork net = new NeuralNetwork(desc);
    	
//    	Additional Spot Features like blinking
    	Features feature = new Features(); 
        
//    	FastFourierTransformation (includes Sending of Peaks and FFT Data) - muss evtl. geändert werden (Senden anders und an anderer Stelle)
    	FFT fft = new FFT();
    	 
//    	Communication 
    	RadiogramConnection rConSen = null				;												// initiates radioconnection
        Datagram sdg = null;																			// datapackage
        String ourAddress = System.getProperty("IEEE_ADDRESS");											
     
//      LEDArray
        TriColorLEDArray colorLEDArray = (TriColorLEDArray)Resources.lookup(TriColorLEDArray.class);	// to get access to the LEDs
		colorLEDArray.setColor(LEDColor.ORANGE);														// set color
		colorLEDArray.setOff();																			// set LEDs off
      
        System.out.println("Starting sensor sampler application on " + ourAddress + " ...");				
        new com.sun.spot.service.BootloaderListenerService();
		BootloaderListenerService.getInstance().start();												// Listen for downloads/commands over USB connection
		
//		Methods like train or test data with NeuralNetwork
		OwnNetworkMethods netmet= new OwnNetworkMethods();
		
        try {
            // Open up a broadcast connection to the host port
            // where the 'on Desktop' portion of this demo is listening
            rConSen = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
            sdg = rConSen.newDatagram(64); 					 			// broadcast to all who want packages
           
            
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
            notifyDestroyed();
        }
    
        
        
        
        
        
        
        
        
		
/**		Variables
 * 		
 */
		double[][] desout = new double[10][1];
    	double[][] newinput = new double[10][Sensorcount];
    	double divergence=0;
    	double cAcc=0;									// temporary 
        Vector vAcc=new Vector();						// vector acceleration
        int countZero=0,messureCount=0;					// counter for zero measurements and total measurements
        Vector vTime=new Vector();						// vector time
        long Break, 									// time for brake
        cTime=0;										// temporary
      
    	
   
////    	Beispiel zur Darstellung des NeuronalenNetzes wird dann entfernt wenn Kommunikation eingebaut
//    	for(int j=0;j<Sensorcount;j++){
//    		for(int i=0;i<10;i++){
//    			if(j==0) 
//    				newinput[i][j] = (Math.cos(i)+1);
//    			if(j==1) 
//        			newinput[i][j] = (Math.cos(i));
//        			
//    		}
//    	}
//    	for(int i=0;i<10;i++){
//    		desout[i][0] = Math.cos(i)+0.5;}
//		
//        netmet.autoNetworktrain(net,newinput,desout,Sensorcount,HiddenUnits);
//    
//        
//        for(int j=0;j<Sensorcount;j++){
//    		for(int i=0;i<10;i++){
//    			
//    			if(j==0) 
//    				newinput[i][j] = (Math.cos(i)+3);
//    			if(j==1) 
//        			newinput[i][j] = (Math.cos(i)+2);
//    			
//    		}
//        }
//        
//        netmet.autoNetworktest(net,newinput);
//        
//        for(int i=0;i<10;i++){
//    		desout[i][0] = Math.cos(i)+2.5;
//    		System.out.println("erwartet: "+desout[i][0]);}
//        
////        bis hier löschen
        
//	Calibration of the accelerometer
        
        divergence = feature.Calibration(colorLEDArray,divLED);
        
        
        
       
        
        
        
        
        
        
        
        
        
/**		misst 30 sekunden oder bis 64 mal null gemessen wurde
 * 		sodass nicht ewig miss falls schwingung länger dauert
 * 		in 30 sec sind genug elemente fespeichert zum rechnen (noch mit fft testen)
*/
   
     
        while(true){
        try {	
        		
        		cTime=System.currentTimeMillis();					// gets start time
            	vTime.addElement(new Long (cTime));					// saves time to vector
            	cAcc=feature.mess();								// gets acceleration as result
            	
               	if(Math.abs(cAcc)-Math.abs(divergence)<unull) {		// checks if acceleration below minimum
               		cAcc=0;											// sets to zero
               		vAcc.addElement(new Double (cAcc));				// saves to vector
               		countZero++;									// increase counter
               		
               	}else{ 
               		countZero=0;									// otherwise counter is zero again and again...
               		cAcc=cAcc-divergence;							// acceleration minus calibrated divergence
               		vAcc.addElement(new Double (cAcc));				// saves to vector
               		
               	}

               	messureCount++;										// increase counter
               	
               	if((countZero>=GRENZE) || (messureCount>=1536)){	// to do calculation

               		if(messureCount<GRENZE+64){						// we say that a minimum amaount of measurements is needed to do a correct calculation
               			messureCount=0;								// thats why all stuffe turned to zero and is starting again measureing
               			countZero=0;
               			vAcc.removeAllElements();
               			vTime.removeAllElements();
               			cTime=System.currentTimeMillis();
               		}
               		else{											// otherwise sends data to calculating procedure and shows a signal with LED
               		if(messureCount-GRENZE>=64){
               			double[] accLight;
               			accLight=fft.toFFT(vTime,vAcc,messureCount-GRENZE, Intervall,MAXFREQ,lowPeak,bandwithsize,sdg,rConSen);	
               			colorLEDArray.setColor(LEDColor.MAGENTA);
               			colorLEDArray.getLED(1).setOn();
               	        Utils.sleep(1000);
               	        colorLEDArray.setOff();
               			messureCount=0;								// all counters and vectors have to be cleared
               			countZero=0;
               			vAcc.removeAllElements();
               			vTime.removeAllElements();
               			cTime=System.currentTimeMillis();
               			
               			
               		}
               		}
               	}	
               	
//	calculating brake time to get a constant measurement with always the same intervall 
               	
               	Break=(Intervall-1)-(Math.abs(System.currentTimeMillis()-cTime));
//	minus 1 beakause otherwise its everytime 1 more as defined
                Utils.sleep(Break);
              
            }
            	catch (Exception e) {
                System.err.println("Caught " + e + " while collecting/sending sensor sample.");
            
            }
        	
		} 
        
    
    }


    
   
    
    
    
   
 
    
    
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }
    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than MIDletStateChangeException
    }
    



}


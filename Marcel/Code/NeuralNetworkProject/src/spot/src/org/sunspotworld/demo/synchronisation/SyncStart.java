package spot.src.org.sunspotworld.demo.synchronisation;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;

import spot.src.org.sunspotworld.demo.communication.LEDSignals;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.resources.transducers.LEDColor;

public class SyncStart {
	
	private RadiogramConnection rCon;
	private Datagram dg;
	private LEDSignals signals = new LEDSignals();
	
	/**
	 * Opens the radiogram connection which is used for the receiving the synchronization signal.
	 * 
	 * @param HOST_PORT host port for the radiogram connection with the base station
	 */
	public SyncStart(int HOST_PORT){
		
		try {
            // Open up a broadcast connection to the host port
            rCon = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
            dg = rCon.newDatagram(50);
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
        }
		
	}//Ende SyncStart
	
	
	/**
	 * Waiting for the receiving of a radiogram signal from the base station. If the value is true the first led will turn green and the measurement starts.
	 * This process should take the same time on all sensor nodes in order that a synchronization is realized.
	 */
	public void waitForSignal(){
		
		//Signal wartephase
		signals.on(0, LEDColor.ORANGE);
		
		//Kontrollvariable
		boolean ready;
		
		try{
			System.out.println("\twaiting for synchronization signal and starting the measurement...");
    		while(true){
    			dg.reset();					//datagram leeren
    			rCon.receive(dg);			//datagram empfangen
		       	ready = dg.readBoolean();	//boolean lesen
		       	if(ready==true){	//Signal Synchronisation erfolgreich
		    		signals.on(0, LEDColor.GREEN);//kontrolle (Synchronisation bei ready == true)
		       		break;
		       	}
    		 }
    		
		} catch(Exception e){
			e.printStackTrace();
		}
				
	}//Ende waitForSignal()

}

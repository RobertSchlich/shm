package spot.src.org.sunspotworld.demo.synchronisation;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;

import spot.src.org.sunspotworld.demo.communication.LEDSignals;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.resources.transducers.LEDColor;

/* Modul SyncStart wartet bis ein Signal (boolean) vom host gegeben wird 
 * zur Benutzung für Testphase des neuronalen Netzes 
 */

public class SyncStart {
	
	private RadiogramConnection rCon;
	private Datagram dg;
	private LEDSignals signals = new LEDSignals();
	
	//Standardkonstruktor
	public SyncStart(int HOST_PORT){
		
		try {
            // Open up a broadcast connection to the host port
            // where the 'on Desktop' portion of this demo is listening
            rCon = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
            dg = rCon.newDatagram(50);  // only sending 12 bytes of data
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
        }
		
	}//Ende SyncStart
	
	
	//Auf Startschuss warten
	public void waitForSignal(){
		
		//Signal wartephase
		signals.on(0, LEDColor.ORANGE);
		
		//Kontrollvariable
		boolean ready;
		
		try{
			System.out.println("Warte auf Startsignal...");
    		while(true){
    			dg.reset();					//datagram leeren
    			rCon.receive(dg);			//datagram empfangen
		       	ready = dg.readBoolean();	//boolean lesen
		       	if(ready==true){	//Signal Synchronisation erfolgreich
//		    		signals.on(0, LEDColor.GREEN);//kontrolle (Synchronisation bei ready == true)
		       		break;
		       	}
    		 }
    		
		} catch(Exception e){
			e.printStackTrace();
		}
				
	}//Ende waitForSignal()

}

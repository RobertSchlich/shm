package spot.src.org.sunspotworld.demo;

import javax.microedition.io.Datagram;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.LEDColor;

/* Modul SyncStart wartet bis ein Signal (boolean) vom host gegeben wird 
 * zur Benutzung für Testphase des neuronalen Netzes 
 */

public class SyncStart {
	
	private RadiogramConnection rCon;
	private Datagram dg;
	private ITriColorLED led;
	
	//Standardkonstruktor
	public SyncStart(RadiogramConnection rCon, Datagram dg, ITriColorLED led){
		
		//Übergabe der Verbindung (RadiogramConnection, Datagram, LED)
		this.rCon = rCon;
		this.dg = dg;
		this.led = led;
		
	}//Ende SyncStart
	
	
	//Auf Startschuss warten
	public void waitForSignal(){
		
		//Signal wartephase
		led.setColor(LEDColor.ORANGE);
		led.setOn();
		
		//Kontrollvariable
		boolean ready;
		
		try{
			
    		while(true){
    			dg.reset();					//datagram leeren
    			rCon.receive(dg);			//datagram empfangen
		       	ready = dg.readBoolean();	//boolean lesen
		       	if(ready==true)				//kontrolle (Synchronisation bei ready == true)
		       		break;
    		 }
    		
		} catch(Exception e){
			e.printStackTrace();
		}
		
		//Signal Synchronisation erfolgreich
		led.setColor(LEDColor.GREEN);
		
	}//Ende waitForSignal()

}

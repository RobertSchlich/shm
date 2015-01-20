package spot.src.org.sunspotworld.demo.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;

import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.spot.peripheral.radio.NoMeshLayerAckException;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.util.Utils;

public class ProtocolGenerator {

	private RadiostreamConnection sendOn;
    private RadiostreamConnection listenOn;
 
    private DataOutputStream sendOnOutputStream;
    private DataInputStream listenOnInputStream;
    
    private LEDSignals signals = new LEDSignals();
    
    private int defaultPort;
    
    //index des Sensors im Netz
    private int index;
    
    private String addressBaseStation;
    private String ourAddress;
	
    /**
     * Sets the output port and the input port for the sensor.
     *  
     * @param portOut port used by base station
     * @param index index of the sensor in the global address list
     * @param addressBaseStation address of the base station
     */
	public ProtocolGenerator(int portOut, int index, String addressBaseStation) throws IOException{
		
		//ports und adressen übergeben
		defaultPort = portOut;
		this.index = index;
		this.addressBaseStation = addressBaseStation;
		ourAddress = System.getProperty("IEEE_ADDRESS");
		
		//Streams öffnen
		sendOn = (RadiostreamConnection) Connector.open("radiostream://" + this.addressBaseStation + ":" + (defaultPort) );
		sendOnOutputStream = sendOn.openDataOutputStream();
		
		listenOn = (RadiostreamConnection) Connector.open("radiostream://" + this.addressBaseStation + ":" + (defaultPort+1+this.index) );
		listenOnInputStream = listenOn.openDataInputStream();
		
	}//Ende standKonstr.
	
	/**
	 * Sends the measured data to the base station.
	 * Set yellow light on last led and green light if the base station responsed.
	 * 
	 * @param data Array of double values
	 * @throws IOException
	 */
	public void sendMeasuredData(double[] data) throws IOException{
		
		signals.on(7, LEDColor.YELLOW);
		
		while(true){
			
			for(int i=0; i<data.length; i++){
				sendOnOutputStream.writeDouble(data[i]);
			}
			
			try{
				sendOnOutputStream.flush();
			} catch(NoMeshLayerAckException nmlae){
				continue;
			}
			
			try{
				listenOn.setTimeout(3000);
				listenOnInputStream.readBoolean();
			} catch(TimeoutException te){
				signals.blink(0, LEDColor.BLUE);
				continue;
			}
			
			break;
			
		}
		
		signals.on(7, LEDColor.GREEN);
		Utils.sleep(1000);
		signals.offAll();
		
		sendOnOutputStream.close();
		sendOn.close();
		
		listenOnInputStream.close();
		listenOn.close();
		
	}//Ende sendLearnigData()
	
}

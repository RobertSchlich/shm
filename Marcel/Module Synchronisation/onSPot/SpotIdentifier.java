package spot.src.org.sunspotworld.demo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.peripheral.radio.NoMeshLayerAckException;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.util.Utils;

public class SpotIdentifier {

	//Spot-Adresse für Host
	String ourAddress;
	String otherAddress;
	
	private RadiostreamConnection listenOn;
    private RadiostreamConnection sendOn;
 
    private DataInputStream listenOnInputStream;
    private DataOutputStream sendOnOutputStream;
 
    private int portOut;
    private int portIn;
    
    private int spotID;
	
    private int initialBroadCastPort;
    
    private boolean foundByHost = false;
 
    private RadiogramConnection broadcastConnectionIn;
    private DatagramConnection broadcastConnectionOut;

    private ITriColorLED[] led;
    
    private static int MAX_BROADCAST_SIZE;
    
	//Standardkonstruktor
	public SpotIdentifier(ITriColorLED[] led, int port){
		
		//Lesen der eigenen Adresse
		ourAddress = System.getProperty("IEEE_ADDRESS");
		
		//Parameter übergeben
		//port-nummer
		initialBroadCastPort = port;
		//LEDs
		this.led = led;
	
		//BroadcastConnection öffnen (port für eingang = ausgang+1 -> für host umgekehrt)
		try{
			broadcastConnectionIn = (RadiogramConnection) Connector.open("radiogram://:" + (initialBroadCastPort+1) );
	        MAX_BROADCAST_SIZE = broadcastConnectionIn.getMaximumLength();
	        broadcastConnectionOut = (DatagramConnection) Connector.open("radiogram://broadcast:" + initialBroadCastPort);
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
		
		//rote led an
		led[0].setColor(LEDColor.RED);
		led[0].setOn();
		
	}//Ende SpotIdentifier()
	
	
	//permanentes Signal zur base-station um kontakt aufzunehmen
	public void sendSignal() throws Exception{
		
		while(foundByHost!=true){
			
			//eigene Adresse auf broadcast (initialBraodCastPort) senden
			Datagram dgSend = broadcastConnectionOut.newDatagram(broadcastConnectionOut.getMaximumLength());
            dgSend.writeUTF("SPOT" + ourAddress);
            broadcastConnectionOut.send(dgSend);
            
            //message empfangen
            Datagram dgReceive = broadcastConnectionIn.newDatagram(MAX_BROADCAST_SIZE);
            broadcastConnectionIn.receive(dgReceive);
            String message = dgReceive.readUTF();	//nachricht lesen		
            if(message.equals(ourAddress)){
            	otherAddress = dgReceive.getAddress();	//adresse lesen, falls Spot registriert
            	spotID = dgReceive.readInt();			//Spot ID in Host-liste lesen
            	portOut = dgReceive.readInt();			//ein und ausgangsports generieren
            	portIn = portOut + (spotID+1);
            	foundByHost = true;						//Host gefunden auf "wahr"
            	
            	//StreamConnection öffnen
                sendOn = (RadiostreamConnection) Connector.open("radiostream://" + otherAddress + ":" + portOut);
                listenOn = (RadiostreamConnection) Connector.open("radiostream://" + otherAddress + ":" + portIn);
            	
            	LEDSignalForHostFound();				//led-signal geben
            }
			
		}//Ende while(foundByHost!=true)
		
	}//Ende sendSignal()
	
	
	//Led-signal bei gefundenem Host
	private void LEDSignalForHostFound(){
		
		for(int i=0; i<led.length; i++){
			led[i].setColor(LEDColor.GREEN);
			led[i].setOn();
			Utils.sleep(300);
			led[i].setOff();
		}
						
	}//Ende LEDSignalForHostFound()
	
	
	//Testsignal nach erkennen der SPOTs
	public void testSignal() throws Exception{

		led[3].setColor(LEDColor.YELLOW);
		led[4].setColor(LEDColor.BLUE);
		
		//StreamConnection
		sendOnOutputStream = sendOn.openDataOutputStream();
		
		while(true){
			try{
				for(int i=0; i<10; i++){
					led[3].setOn();
					sendOnOutputStream.writeUTF("test-message from spot" + (spotID+1) + ", address: " + ourAddress);
					sendOnOutputStream.flush();
					Utils.sleep(100);
					led[3].setOff();
					Utils.sleep(1000);
				}
			} catch(NoMeshLayerAckException nmlae){
				led[4].setOn();
				Utils.sleep(100);
				led[4].setOff();
				continue;
			}
			break;
		}
		
	}//Ende testSignal()
}

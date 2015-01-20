package spot.src.org.sunspotworld.demo.spotfinder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;

import spot.src.org.sunspotworld.demo.communication.LEDSignals;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.peripheral.radio.NoMeshLayerAckException;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.util.Utils;

public class SpotIdentifier {

	//Spot-Adresse für Host
	String ourAddress;
	String otherAddress;
	String[] otherAdresses;
	double learning_rate;
	int sample_period;
	int amount_of_data;
	double LEARN_LIM;
	int LEARN_COUNT;
	int Faktor;
	
	private RadiostreamConnection listenOn;
    private RadiostreamConnection sendOn;
 
    private DataInputStream listenOnInputStream;
    private DataOutputStream sendOnOutputStream;
 
    private int portOut;
    
	private int portIn;
    
    private int spotID;
    private int spotCount;
    private String[] adresses;
	
    private int initialBroadCastPort;
    
    private boolean foundByHost = false;
    private long timeout = 2000;
 
    private RadiogramConnection broadcastConnectionIn;
    private DatagramConnection broadcastConnectionOut;

    private LEDSignals ledsignals = new LEDSignals();
    
    private static int MAX_BROADCAST_SIZE;
    
	/**
	 * Sets the own address parameter and opens the broadcast connections on the defined port.
	 * LED Signal set red light on led[0]. 
	 * 
	 * @param port port for the default broadcast connection.
	 */
	public SpotIdentifier(int port){
		
		//Lesen der eigenen Adresse
		ourAddress = System.getProperty("IEEE_ADDRESS");
		
		//Parameter übergeben
		//port-nummer
		initialBroadCastPort = port;
	
		//BroadcastConnection öffnen (port für eingang = ausgang+1 -> für host umgekehrt)
		try{
			broadcastConnectionIn = (RadiogramConnection) Connector.open("radiogram://:" + (initialBroadCastPort+1) );
	        MAX_BROADCAST_SIZE = broadcastConnectionIn.getMaximumLength();
	        broadcastConnectionOut = (DatagramConnection) Connector.open("radiogram://broadcast:" + initialBroadCastPort);
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
		
		//rote led an
		ledsignals.on(0, LEDColor.RED);
		
	}//Ende SpotIdentifier()
	
	/**
	 * Sends a permanent signal on the broadcast connection with the own address and waits for a response.
	 * Stops waiting after 2 sec + a random time to avoid overlapping of messages on the base station and sends a new message.
	 * If the spot receives a datagram with its own message, it brakes the loop and reads and sets the parameters: 
	 * spotID, portIn, portOut. Opens the datastream connections sendOn and listenOn for the communication with the base station.
	 * LED signal for a new starting loop sets a blue blink on the last led.
	 * LED signal for found by host sets a green blink on all 8 leds.
	 *   
	 * @throws Exception
	 */
	public void sendSignal() throws Exception{
		
		while(foundByHost!=true){
			
			//eigene Adresse auf broadcast (initialBraodCastPort) senden
			Datagram dgSend = broadcastConnectionOut.newDatagram(broadcastConnectionOut.getMaximumLength());
            dgSend.writeUTF("SPOT" + ourAddress);
            broadcastConnectionOut.send(dgSend);
            
            //Datagram
            Datagram dgReceive = broadcastConnectionIn.newDatagram(MAX_BROADCAST_SIZE);
            //timout
            broadcastConnectionIn.setTimeout(timeout);
            //message empfangen
            try{
            	broadcastConnectionIn.receive(dgReceive);
            } catch(Exception e){
            	ledsignals.blink(7, LEDColor.BLUE);
            	continue;
            }
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
                
                //datagram schließen
                broadcastConnectionIn.close();
                broadcastConnectionOut.close();
            	
            	ledsignals.LEDSignalForHostFound();				//led-signal geben
            }else{
            	System.out.println("Not my address");
            	//zufällige Wartezeit für jeden nicht erkannten Spot (CSMACD)
            	Random r = new Random();
            	r.setSeed(System.currentTimeMillis());
            	Utils.sleep((long)r.nextDouble()*10);
            }
			
		}//Ende while(foundByHost!=true)
		
	}//Ende sendSignal()
	
	
	/**
	 * Sends a test message on the datastream connection to the base station to verify the connection.
	 * LED signal set on led[7] with yellow light, if the test was successful green light.
	 * 
	 * @throws Exception
	 */
	public void testSignal() throws Exception{

		//StreamConnection
		sendOnOutputStream = sendOn.openDataOutputStream();
		System.out.println(sendOn.getLocalPort());
		
		while(true){
			try{
				
				ledsignals.on(7, LEDColor.YELLOW);
				sendOnOutputStream.writeUTF("test-message from spot" + (spotID+1) + ", address: " + ourAddress);
				sendOnOutputStream.flush();
				Utils.sleep(1000);
				ledsignals.on(7, LEDColor.GREEN);
				
			} catch(NoMeshLayerAckException nmlae){
				ledsignals.blink(1, LEDColor.BLUE);
				continue;
			}
			break;
		}
		
	}//Ende testSignal()
	
	/**
	 * Receives the list of addresses of all spots (also the own one) from the base station over the datastream connection.
	 * LED signal set yellow light on led[6], if addresses are received green light. 
	 * 
	 * @throws IOException
	 */
	public void receiveAdresses() throws IOException{
		//kontroole auf offenen stream
		if(listenOn == null)
			return;
		
		ledsignals.on(6, LEDColor.YELLOW);
		
		listenOnInputStream = listenOn.openDataInputStream();
		//Anzahl der spots lesen
		spotCount = listenOnInputStream.readInt();
		//adressliste initialisieren
		adresses = new String[spotCount];
		//adressen empfangen und speichern
		for(int i=0; i<adresses.length; i++){
			adresses[i] = listenOnInputStream.readUTF();
			System.out.println("\t" + (i+1) + ": " + adresses[i]);
		}
		ledsignals.on(6, LEDColor.GREEN);
		
		Utils.sleep(1000);
	}//Ende receiveAdresses()
	
	/**
	 * Receives defined parameters from the base station:
	 * learning rate, sample period, amount of data, learning limit, learning steps, factor for the scaling of acceleration values.
	 *  
	 * 
	 * @throws IOException
	 */
	public void receiveParams() throws IOException{
		//kontroole auf offenen stream
		if(listenOn == null)
			return;
		
		ledsignals.on(5, LEDColor.YELLOW);
		
		//listenOnInputStream = listenOn.openDataInputStream(); -> verbindung bereits in send addresses geöffnet
		//lernrate  lesen
		learning_rate = listenOnInputStream.readDouble();
		System.out.println("\teta: " + learning_rate);
		
		//sample period lesen
		sample_period = listenOnInputStream.readInt();
		System.out.println("\tsample period: " + sample_period);
		
		//anzahl der daten lesen
		amount_of_data = listenOnInputStream.readInt();
		System.out.println("\tamount of data: " + amount_of_data);
		
		//lernschwelle  lesen
		LEARN_LIM = listenOnInputStream.readDouble();
		System.out.println("\tSchwelle: " + LEARN_LIM);
		
		//Lernschritte pro Phase lesen
		LEARN_COUNT = listenOnInputStream.readInt();
		System.out.println("\tLernschritte: " + LEARN_COUNT);
		
		//Faktor zur Skalierung der Beschleunigung pro Phase lesen
		Faktor = listenOnInputStream.readInt();
		System.out.println("\tSkalierungsfaktor: " + Faktor);
		
		
		ledsignals.on(5, LEDColor.GREEN);
				
		Utils.sleep(1000);
		
		ledsignals.off(7);
		ledsignals.off(6);
		ledsignals.off(5);
		
	}//Ende receiveParams()

	public String[] getAdresses() {
		return adresses;
	}
	
	public int getPortOut() {
		return portOut;
	}
	
	public int getIndex() {
		return spotID;
	}

	public double getLearning_rate() {
		return learning_rate;
	}

	public int getSample_period() {
		return sample_period;
	}

	public int getAmount_of_data() {
		return amount_of_data;
	}
	
	public double getLEARN_LIM() {
		return LEARN_LIM;
	}

	public int getLEARN_COUNT() {
		return LEARN_COUNT;
	}
	
	public int getFaktor() {
		return Faktor;
	}
}

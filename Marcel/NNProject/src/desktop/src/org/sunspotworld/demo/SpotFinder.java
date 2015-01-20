package desktop.src.org.sunspotworld.demo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.util.Utils;

public class SpotFinder {

	private ArrayList<String> adresses = new ArrayList<String>();
	
	//BroadCast-Connections
	private RadiogramConnection broadcastConnectionIn;
    private DatagramConnection broadcastConnectionOut;
    private Datagram dgSend;
    private Datagram dgReceive;
    
    //Stream-Connections
    private RadiostreamConnection listenOn;
    private RadiostreamConnection sendOn;
    private DataInputStream listenOnInputStream;
    private DataOutputStream sendOnOutputStream;
    
    private static int MAX_BROADCAST_SIZE;
    
    //parameters
    private int initialBroadCastPort;
    private int streamPort;
    private int searchingTime;
    
    
    public SpotFinder(int HOST_PORT, int streamPort, int searchingTime){
    	
    	initialBroadCastPort = HOST_PORT;
    	this.streamPort = streamPort;
    	this.searchingTime = searchingTime;
    	
    }//ende stand.konstr.
	
	public void searchForSpots() throws Exception {
    	
    	try{
    		//start broadcast-connections
			broadcastConnectionIn = (RadiogramConnection) Connector.open("radiogram://:" + initialBroadCastPort);
	        MAX_BROADCAST_SIZE = broadcastConnectionIn.getMaximumLength();
	        broadcastConnectionOut = (DatagramConnection) Connector.open("radiogram://broadcast:" + (initialBroadCastPort+1) );
	        //Start datagrams
	        dgSend = broadcastConnectionOut.newDatagram(MAX_BROADCAST_SIZE);
	        dgReceive = broadcastConnectionIn.newDatagram(MAX_BROADCAST_SIZE);
	       
	        Thread listenOnSpotsThread = startListenOnSpotsThread();
	        Utils.sleep(searchingTime);
	        listenOnSpotsThread.interrupt();
	        
	        for(int i=0; i<adresses.size(); i++){
	        	openListenStreamConnection(i);
	        	Thread listenOnTestSignalThread = startListenOnTestSignalThread(i);
	        	Utils.sleep(10000);
	        	listenOnTestSignalThread.interrupt();
	        }
	        
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
    	
    }//Ende run()
        
    //sammeln der spotadressen
    private void listenOnSpots() throws Exception{
        
    	//start message um spots in bereitschaft zu setzen
//        dgSend.writeUTF("start");
//        broadcastConnectionOut.send(dgSend);
    		
    	//datagram von Spot x empfangen	
        try{
        	broadcastConnectionIn.receive(dgReceive);
        } catch(InterruptedIOException iioe){
        	System.out.println("listenOnSpots abgebrochen");
        	return;
        }
        String otherAddress = dgReceive.getAddress();
        int spotIndex = 0; 
    	//kontrolle ob adresse in liste vorhanden
        if(addressExist(otherAddress)==false){
        	adresses.add(otherAddress);	//Adresse speichern
        	spotIndex = adresses.size();
            System.out.println("Spot gefunden:\nNr.: " + spotIndex + "\nAdresse: " + otherAddress);
        } else{
        	adresses.indexOf(otherAddress);
	        System.out.println("Sende wiederholt informationen für " + otherAddress);
        }
        //datagram mit informationen für spot
        dgSend.reset();
        dgSend = broadcastConnectionOut.newDatagram(broadcastConnectionOut.getMaximumLength());
        dgSend.writeUTF(otherAddress);			//Spot-adresse
        dgSend.writeInt(adresses.size()-1);		//spot id
        dgSend.writeInt(streamPort);			//stream-port
        broadcastConnectionOut.send(dgSend);
            
        dgReceive.reset();
        dgSend.reset();
                
    }//Ende listeOnSpots()
    
    //kontrolle auf vorhandene adressen
    private boolean addressExist(String address) {
    	
    	boolean exist = false;
    	
    	if(address.equals("0000.0000.0000.0000"))
    		return true;
    	
    	if(adresses.contains(address))
    		exist=true;
    	
		return exist;
	}

    //Testsignal abhören
    private void listenOnTestSignal(int index) throws Exception{
    	 
    	while(true){
    		if(listenOn==null || listenOnInputStream==null){
    			System.out.println("kein Stream verfügbar");
    			Utils.sleep(100);
    			continue;
    		}
    		System.out.print("listen on signal: ");
		    String message;
		    try{
		    	message = listenOnInputStream.readUTF();
		    } catch(InterruptedIOException iioe){
		    	System.out.println("Stream.read() abgebrochen");
		    	break;
		    }
		    System.out.println(message);
		    break;
    	}
    	
    }//Ende listenOnTestSignal()
    
    //Stream-connection öffnen
    private void openListenStreamConnection(int index) throws Exception{
    	System.out.println("Test-Stream " + (index+1) + ":");
    	listenOn = (RadiostreamConnection) Connector.open("radiostream://" + adresses.get(index) + ":" + streamPort);
    	listenOnInputStream = listenOn.openDataInputStream();
    }//Ende openListenStreamConnection()

    //Stream-connection öffnen
    private void openSendStreamConnection(int index) throws Exception{
    	System.out.println("open Send-Stream " + (index+1) + ":");
    	sendOn = (RadiostreamConnection) Connector.open("radiostream://" + adresses.get(index) + ":" + (streamPort+index+1));
    	sendOnOutputStream = sendOn.openDataOutputStream();
    }//Ende openListenStreamConnection()    
    
    //Thread der bestimmte Zeit läuft um SPOTs zu finden
    private Thread startListenOnSpotsThread(){
    	//Thread zur wiederholten suche nach spots
    	Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {

			    	System.out.println("suche nach spots für die nächsten " + (searchingTime/1000) + " sec");
			    	
					while(true){
						listenOnSpots();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
    	t.start();
    	
    	return t;
    	
    }//Ende startListenOnSpotsThread()
	
    //Thread der auf bestimmte Zeit das Testsignal empfängt
    private Thread startListenOnTestSignalThread(final int index){
    	//Thread zum lesen des Testsignals
    	Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					listenOnTestSignal(index);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
    	t.start();
    	
    	return t;
    }//ende startListenOnTestSignalThread()
    
    
    //Adressliste schicken
    public boolean sendAdressesToSpots() throws Exception{
    	
    	//Datagram leeren
    	dgSend.reset();
    	
    	for(int i=0; i<adresses.size(); i++){
    		//verbindung öffnen
    		openSendStreamConnection(i);
    		//controlle auf offene verbindung
    		if(sendOn == null){
    			System.out.println("Spot " + i + " nicht erreichbar");
    			return false;
    		}
    		//Spotanzahl schicken
    		sendOnOutputStream.writeInt(adresses.size());
    		sendOnOutputStream.flush();
    		
    		//adressen in stream schreiben
    		for(String s: adresses)
    			sendOnOutputStream.writeUTF(s);
    		//senden...
    		sendOnOutputStream.close();
    		
    		Utils.sleep(700);
    	}
    	
    	Utils.sleep(2000);
    	
    	return true;
    	
    }//Ende sendAdressesToSpots()
    
    
    public boolean sendParams(double learning_rate, int sample_period, int amount_of_data, double LEARN_LIM, int LEARN_COUNT, int Faktor) throws Exception{
    	
    	for(int i=0; i<adresses.size(); i++){
    		//verbindung öffnen
    		openSendStreamConnection(i);
    		//controlle auf offene verbindung
    		if(sendOn == null){
    			System.out.println("Spot " + i + " nicht erreichbar");
    			return false;
    		}
    		//Spotanzahl schicken
    		sendOnOutputStream.writeDouble(learning_rate);
    		sendOnOutputStream.writeInt(sample_period);
    		sendOnOutputStream.writeInt(amount_of_data);
    		sendOnOutputStream.writeDouble(LEARN_LIM);
    		sendOnOutputStream.writeInt(LEARN_COUNT);
    		sendOnOutputStream.writeInt(Faktor);
    		//senden...
    		sendOnOutputStream.close();
    		
    		Utils.sleep(700);
    	}
    	
    	Utils.sleep(2000);
    	
    	return true;
    	
    }//Ende sendParams()
	
}

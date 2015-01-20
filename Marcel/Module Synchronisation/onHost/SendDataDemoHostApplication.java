/*
 * SendDataDemoHostApplication.java
 *
 * Copyright (c) 2008-2009 Sun Microsystems, Inc.
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
import com.sun.spot.peripheral.ota.OTACommandServer;
import com.sun.spot.util.Utils;


public class SendDataDemoHostApplication {
     
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
    private int initialBroadCastPort = 66;
    private int streamPort = 99;
    private int searchingTime = 10000;
	
	private void run() throws Exception {
    	
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
	        	openStreamConnection(i);
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
        dgSend.writeUTF("start");
        broadcastConnectionOut.send(dgSend);
    		
    	//datagram von Spot x empfangen	
        try{
        	broadcastConnectionIn.receive(dgReceive);
        } catch(InterruptedIOException iioe){
        	System.out.println("Datagram.receive() abgebrochen");
        }
        String otherAddress = dgReceive.getAddress();
            
    	//kontrolle ob adresse in liste vorhanden
        if(addressExist(otherAddress)==false){
        	adresses.add(otherAddress);	//Adresse speichern
            System.out.println("Spot gefunden:\nNr.: " + adresses.size() + "\nAdresse: " + otherAddress);
            //datagram mit informationen für spot
            dgSend.reset();
            dgSend = broadcastConnectionOut.newDatagram(broadcastConnectionOut.getMaximumLength());
            dgSend.writeUTF(otherAddress);			//Spot-adresse
            dgSend.writeInt(adresses.size()-1);		//spot id
            dgSend.writeInt(streamPort);			//stream-port
            broadcastConnectionOut.send(dgSend);             
        }
            
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
    	}
    	
    }//Ende listenOnTestSignal()
    
    //Stream-connection öffnen
    private void openStreamConnection(int index) throws Exception{
    	System.out.println("Test-Stream " + (index+1) + ":");
    	listenOn = (RadiostreamConnection) Connector.open("radiostream://" + adresses.get(index) + ":" + streamPort);
    	listenOnInputStream = listenOn.openDataInputStream();
    }
    
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
    
    /**
     * Start up the host application.
     *
     * @param args any command line arguments
     */
    public static void main(String[] args) throws Exception {
        // register the application's name with the OTA Command server & start OTA running
        OTACommandServer.start("SendDataDemo");

        SendDataDemoHostApplication app = new SendDataDemoHostApplication();
        app.run();
    }
}

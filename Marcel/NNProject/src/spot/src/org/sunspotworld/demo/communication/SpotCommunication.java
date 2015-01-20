package spot.src.org.sunspotworld.demo.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.microedition.io.Connector;

import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.spot.peripheral.radio.NoMeshLayerAckException;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.util.Utils;

public class SpotCommunication {

    private RadiostreamConnection sendOn;
    private RadiostreamConnection listenOn;
 
    private DataOutputStream sendOnOutputStream;
    private DataInputStream listenOnInputStream;
    
    private LEDSignals signals = new LEDSignals();
    
    private int defaultPort;
    
    //index des Sensors im Netz
    private int index;
    
    private int SENSOR_COUNT;
    
    private String[] addresses;
    
    public SpotCommunication(int portOut, int index, int sensorcount, String[] addresses){
    	defaultPort = portOut;
    	this.index = index;
    	SENSOR_COUNT = sensorcount;
    	this.addresses = addresses;
    	
    	System.out.println("ID: " + index);
    	System.out.println("Adressen:");
    	for(int i=0; i<addresses.length; i++){
    		System.out.println(addresses[i]);
    	}
    	
    }//Ende konstr.
    
    /**
     * Sends the measured Values to the other Spots.
     * 
     * @param acc Array of measurements.
     * 
     */
    private void sendValuesToSpots(double[] acc) throws Exception{
    	  	
    	for(int i=0; i<SENSOR_COUNT; i++){
    		
    		if(i==index)
    			continue;
    		
    		System.out.println(" - sende " + i);
    		
    		signals.LEDSignalCommunicationSend(SENSOR_COUNT, i);
    		
			sendOn = (RadiostreamConnection) Connector.open("radiostream://" + addresses[i] + ":" + (defaultPort + index + 1) );
			sendOnOutputStream = sendOn.openDataOutputStream();
			
			listenOn = (RadiostreamConnection) Connector.open("radiostream://" + addresses[i] + ":" + (defaultPort+1+i) );
    		listenOnInputStream = listenOn.openDataInputStream();
			
			while(true){
				try{
					
					Utils.sleep(1000);
					
					for(int iterator = 0; iterator<acc.length; iterator++)
						sendOnOutputStream.writeDouble(acc[iterator]);
					
					sendOnOutputStream.flush();
					
					listenOn.setTimeout(5000);
					listenOnInputStream.readBoolean();
					
					break;
															
				} catch(NoMeshLayerAckException nmlae){
					signals.blink(7, LEDColor.BLUE);
					continue;
				} catch(IndexOutOfBoundsException ioobe){
					break;
				} catch(TimeoutException toe){
					signals.blink(7, LEDColor.BLUE);
					continue;
				}
			}
			
			sendOnOutputStream.close();
			sendOn.close();
			listenOn.close();
			listenOnInputStream.close();
			
			Utils.sleep(700);
			   		
    	}
    	    	
    }//Ende sendValuesToSpots()
    
    
    /**
     * receive the measurement values of the other spots in the network.
     * 
     * @param acc values of the measurement array
     * @param newinput array of external data
     * @param COM_COUNT number of accessed spots in the loop
     * 
     */
    public void communicate(double[] acc, double[][] newinput, int COM_COUNT) throws Exception{
    	   	
    	for(int i=0; i<COM_COUNT; i++){
    		
    		System.out.println("aktueller index: " + i);
    		
    		int usedIndex = i;
    		
    		if(i==index){
    			System.out.println(" - sending values...");
    			sendValuesToSpots(acc);
    			continue;
    		}else{
    			signals.LEDSignalCommunicationReceive(SENSOR_COUNT, i);
    		}
    		
    		if(i>index){
    			usedIndex = i-1;
    		}
    		
    		System.out.println(" - receiving from spot " + (i+1) + " - " + (addresses[i]) + " on port " + (defaultPort+1+i));
    		
    		listenOn = (RadiostreamConnection) Connector.open("radiostream://" + addresses[i] + ":" + (defaultPort+1+i) );
    		listenOnInputStream = listenOn.openDataInputStream();
    		
    		sendOn = (RadiostreamConnection) Connector.open("radiostream://" + addresses[i] + ":" + (defaultPort + index + 1) );
			sendOnOutputStream = sendOn.openDataOutputStream();
    		
    		int timoutcounter = 0;
    		
			while(true){
				
				try{
					
					//listenOn.setTimeout(10000);
					for(int iterator = 0; iterator<acc.length; iterator++){
						double d = listenOnInputStream.readDouble();
						newinput[iterator][usedIndex] = d;
						System.out.println("\t"+d);
					}
					
					sendOnOutputStream.writeBoolean(true);
					sendOnOutputStream.flush();
					
					break;
															
				} catch(NoMeshLayerAckException nmlae){
					signals.blink(7, LEDColor.BLUE);
					continue;
				} catch(IndexOutOfBoundsException ioobe){
					System.out.println("index out of bounds exception beim lesen");
					break;					
				} catch(TimeoutException te){
					timoutcounter++;
					signals.blink(7, LEDColor.RED);
					if(timoutcounter>4){
						System.out.println(" - empfangen abgebrochen");
						break;
					}
				}
			}
			
			System.out.println("\n - daten von " + (i+1) + " vollständig erhalten");
			
			listenOn.close();
	    	listenOnInputStream.close();
	    	sendOn.close();
	    	sendOnOutputStream.close();
	    	
    	}
    	
    	signals.offAll();
    	    	
    }//Ende communicate()
	
}

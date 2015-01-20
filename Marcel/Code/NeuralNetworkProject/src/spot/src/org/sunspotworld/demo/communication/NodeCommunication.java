package spot.src.org.sunspotworld.demo.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.microedition.io.Connector;

import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.spot.peripheral.radio.NoMeshLayerAckException;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.util.Utils;

public class NodeCommunication {

    private RadiostreamConnection sendOn;
    private RadiostreamConnection listenOn;
 
    private DataOutputStream sendOnOutputStream;
    private DataInputStream listenOnInputStream;
    
    private LEDSignals signals = new LEDSignals();
    
    private int defaultPort;    
    private int index;
    private int sensor_count;    
    private String[] addresses;
    
    /**
     * Cares about the communication between the several sensor nodes before learning or comparison in the network.
     * For the communication are only used RadiostreamConnections on a port defined by the index of the node.      
     * 
     * @param portOut 		default port for the RadioStreamConnection to the base station
     * @param index 		index of the sensor node itself
     * @param sensorcount 	count of all nodes in the network
     * @param addresses		addresses of all nodes in the network
     */
    public NodeCommunication(int portOut, int index, int sensorcount, String[] addresses){
    	defaultPort = portOut;
    	this.index = index;
    	sensor_count = sensorcount;
    	this.addresses = addresses;
    }//Ende konstr.
    
    
    /**
     * Sends the measured values to the other sensor nodes in the network using a RadioStream connection.
     * After sending all values the receiving node will send a confirmation.
     * The procedure will be repeated until the confirmation has the value of true.
     * 
     * @param acc measured acceleration values by the node itself 
     * 
     * @throws Exception
     */
    private void sendValuesToSpots(double[] acc) throws Exception{
    	  	
    	for(int i=0; i<sensor_count; i++){
    		
    		if(i==index)
    			continue;
    		
    		System.out.println(" -\tsending on node " + (i+1));
    		
    		signals.LEDSignalCommunicationSend(sensor_count, i);
    		
			sendOn = (RadiostreamConnection) Connector.open("radiostream://" + addresses[i] + ":" + (defaultPort + index + 1) );
			sendOnOutputStream = sendOn.openDataOutputStream();
			
			listenOn = (RadiostreamConnection) Connector.open("radiostream://" + addresses[i] + ":" + (defaultPort+1+i) );
    		listenOnInputStream = listenOn.openDataInputStream();
			
			while(true){
				try{
					
					Utils.sleep(1000);
					
					for(int iterator = 0; iterator<acc.length; iterator++){
						sendOnOutputStream.writeDouble(acc[iterator]);
					}
					
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
     * Receives the measurement values of the other sensor nodes in the network using a RadioStream connection.
     * After receiving all values a the node will send confirmation message to the sending node and start to receive from the next
     * node or sending its own values.
     * 
     * @param acc 		values of the measurement array
     * @param newinput	array of external data
     * @param com_count number of accessed spots in the loop
     * 
     * @throws Exception
     */
    public void communicate(double[] acc, double[][] newinput, int com_count) throws Exception{
    	   	
    	for(int i=0; i<com_count; i++){
    		
    		System.out.println("\tcurrent index: " + i);
    		
    		int usedIndex = i;
    		
    		if(i==index){
    			System.out.println(" -\tsending values...");
    			sendValuesToSpots(acc);
    			continue;
    		}else{
    			signals.LEDSignalCommunicationReceive(sensor_count, i);
    		}
    		
    		if(i>index){
    			usedIndex = i-1;
    		}
    		
    		System.out.println(" -\treceiving from node " + (i+1) + " - " + (addresses[i]) + " on port " + (defaultPort+1+i));
    		
    		listenOn = (RadiostreamConnection) Connector.open("radiostream://" + addresses[i] + ":" + (defaultPort+1+i) );
    		listenOnInputStream = listenOn.openDataInputStream();
    		
    		sendOn = (RadiostreamConnection) Connector.open("radiostream://" + addresses[i] + ":" + (defaultPort + index + 1) );
			sendOnOutputStream = sendOn.openDataOutputStream();
    		
    		int timoutcounter = 0;
    		
			while(true){
				
				try{
					
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
					break;					
				} catch(TimeoutException te){
					timoutcounter++;
					signals.blink(7, LEDColor.RED);
					if(timoutcounter>4){
						break;
					}
				}
			}
			
			System.out.println("\n -\treceiving data from node " + (i+1) + " completed");
			
			listenOn.close();
	    	listenOnInputStream.close();
	    	sendOn.close();
	    	sendOnOutputStream.close();
	    	
    	}
    	
    	signals.offAll();
    	    	
    }//Ende communicate()
	
}

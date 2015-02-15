package sensornode;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.io.j2me.radiostream.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.util.Utils;

import javax.microedition.io.*;

public class CommunicationNormal {
		
	    String ourAddress = System.getProperty("IEEE_ADDRESS");
        ITriColorLED led2 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED2");
        ITriColorLED led3 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED3");
        
        RadiostreamConnection conn;
        DataInputStream dis;
        DataOutputStream dos; 
        
        public void ExchangeData (Measurement ourMeas, String otherAddress, int hostPort) {

        	try {     
        		
        		System.out.println("starting ExchangeData. ");
        		conn = (RadiostreamConnection)Connector.open("radiostream://" + otherAddress + ":" + hostPort);
        		// set timeout for connection
        		conn.setTimeout(5000); 
        		
        		DataInputStream dis = conn.openDataInputStream();
        		DataOutputStream dos = conn.openDataOutputStream();
                 
        		System.out.println("Starting communication on" + ourAddress + " with " + otherAddress);

        		boolean okay = false;
        		
        		        		
        		
        		while(okay == false){
        			try{
	            		dos.writeFloat(ourMeas.frequency);
	    				dos.writeDouble(ourMeas.magnitude);
	    				dos.writeInt(ourMeas.error);
	    				dos.flush();
	    				System.out.println("datastream flushed." );
	    				
	    		        // blink second LED
	    		        led2.setRGB(0, 255, 0);
	    	            led2.setOn();
	    	            
	    				okay = dis.readBoolean();
	    				
	    				System.out.println("okay received." );
	    				
	    		        // blink third LED
	    		        led3.setRGB(0, 255, 255);
	    	            led3.setOn();
	    	          
    				} catch(Exception e){
    					continue;
       			
    				}
        		}
	            
        		dis.close();
        		dos.close();
        		conn.close();
        		
				Utils.sleep(500);
        		led2.setOff();
				led3.setOff();

    		
        		
        	} catch (Exception e) {   
        		 System.err.println("Caught " + e + " in exchanging data.");
        	
        	}
        }
}
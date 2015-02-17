package sensornodeNN;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.io.j2me.radiostream.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.util.Utils;

import javax.microedition.io.*;

public class CommunicationNN {
		
	    String ourAddress = System.getProperty("IEEE_ADDRESS");
        ITriColorLED led2 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED2");
        ITriColorLED led3 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED3");
        ITriColorLED led5 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED5");
        ITriColorLED led6 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED6");
        
        RadiostreamConnection conn;
        DataInputStream dis;
        DataOutputStream dos; 
        
        public Measurement ReceiveData(String otherAddress, int hostPort) {
            //object for other measurement
            Measurement othMeas = new Measurement();
        	try {     
        		
        		//System.out.println("starting ExchangeData. ");
        		conn = (RadiostreamConnection)Connector.open("radiostream://" + otherAddress + ":" + hostPort);
        		// set timeout for connection
        		// conn.setTimeout(10000); 
        		DataInputStream dis = conn.openDataInputStream();
        		DataOutputStream dos = conn.openDataOutputStream();
                 
        		//System.out.println("Starting communication on" + ourAddress + " with " + otherAddress);

				//System.out.println("try to recieve." );	
				// receive data from stream
        		
	            othMeas.address = otherAddress;
	            othMeas.frequency = dis.readFloat();
	    		othMeas.magnitude = dis.readDouble();
	    		othMeas.error = dis.readInt();
	    		
				//System.out.println("datastream received." );
	    		
		        // blink third LED
		        led3.setRGB(0, 255, 255);
	            led3.setOn();
	            
	            // send "okay"
        		dos.writeBoolean(true);
				dos.flush();
				//System.out.println("received message flushed." );
				
		        // blink second LED
		        led2.setRGB(0, 255, 0);
	            led2.setOn();
	            
	            // close streams and connection
        		dis.close();
        		dos.close();
        		conn.close();
        		
        		// turn off the light
        		led2.setOff();
				led3.setOff();
	    		        		
        		
        	} catch (Exception e) {   
        		 System.err.println("Caught " + e + " in exchanging data.");
        	} finally {
	    		return othMeas;
        	}

        }
        
        public void StoreData(Measurement[] allMeas, int hostPort, String baseAddress) {
        	//object for other measurement
        	
        	//System.out.println("allMeas.length= "+ allMeas.length);
        	
        	int meas = 0;
        	
        	while ( meas < allMeas.length){
        		
	        	try {   
	            	//System.out.println("meas= "+ meas);
	            		        		
	        		
	        		conn = (RadiostreamConnection)Connector.open("radiostream://" + baseAddress + ":" + hostPort);
	        		conn.setTimeout(300); 
	        		
	        		DataInputStream dis = conn.openDataInputStream();
	        		DataOutputStream dos = conn.openDataOutputStream();
	        		
	        		boolean okay = false;
	        		
    		        // turn on fifth LED
    		        led5.setRGB(0, 255, 0);
    	            led5.setOn();
    	            
	        		// try to send data until basestation sent okay
	        		while(okay == false){
	        			try{

	        		        led6.setRGB(255, 0, 0);
	        	            led6.setOn();
	        				Utils.sleep(1000);
	        	            led6.setOff();	        				
	        	            
	        	            // send Data
	        				dos.writeUTF(allMeas[meas].address);
	    	        		dos.writeFloat(allMeas[meas].frequency);
	    	        		dos.writeDouble(allMeas[meas].magnitude);
	    	        		dos.writeInt(allMeas[meas].error);
	    	        		
	    					dos.flush();
	    					//System.out.println("flushed to basestation." );
		    				okay = dis.readBoolean();
		    				//System.out.println("okay received." );
	        			} catch(Exception e){
	               		 	System.err.println("Caught " + e + " in exchanging data 1.");
	    					continue;
	    				}
	        			
	        			meas++;

	        		}
        			//turn off
        			led5.setOff();
        			// close streams and connection
	        		dis.close();
	        		dos.close();
	        		conn.close();
	    		
	        	} catch (Exception e) {   
	        		 System.err.println("Caught " + e + " in exchanging data 2.");
	        	}
        	}

        	
        }
}
/**
 * @author Jahr&Schlich
 * 
 */

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
        
        // receive measurements from other sensors
        public Measurement ReceiveData(String otherAddress, int hostPort) {
            //instantiate object for received measurement of other sensor
            Measurement othMeas = new Measurement();
        	try {     

        		conn = (RadiostreamConnection)Connector.open("radiostream://" 
        								+ otherAddress + ":" + hostPort);
        		DataInputStream dis = conn.openDataInputStream();
        		DataOutputStream dos = conn.openDataOutputStream();
                 
				// receive data from stream
	            othMeas.address = otherAddress;
	            othMeas.frequency = dis.readFloat();
	    		othMeas.magnitude = dis.readDouble();
	    		othMeas.error = dis.readInt();

		        // blink third LED to indicate received measurement
		        led3.setRGB(0, 255, 255); led3.setOn();
	            
	            // send "okay", so other sensor node stops to try to send
        		dos.writeBoolean(true);
				dos.flush();

				// blink second LED
		        led2.setRGB(0, 255, 0); led2.setOn();
	            
	            // close streams and connection
        		dis.close(); dos.close(); conn.close();
        		
        		// turn off the light
        		led2.setOff(); led3.setOff();
	    		        		
        	} catch (Exception e) {   
        		 System.err.println("Caught " + e + " in exchanging data.");
        	} finally {
	    		return othMeas;
        	}
        }
        
        // send all measurements to basestation
        public void StoreData(Measurement[] allMeas, int hostPort, String baseAddress) {
        	int meas = 0;
        	// iterate over list of measurements
        	while ( meas < allMeas.length){
	        	try {   
	        		conn = (RadiostreamConnection)Connector.open("radiostream://" 
	        									+ baseAddress + ":" + hostPort);
	        		DataInputStream dis = conn.openDataInputStream();
	        		DataOutputStream dos = conn.openDataOutputStream();
	        		// set timeout to repeat flushing
	        		conn.setTimeout(300); 

	        		// turn on fifth LED
    		        led5.setRGB(0, 255, 0); led5.setOn();
    		        boolean okay = false;
	        		// try to send data until basestation sent okay
	        		while(okay == false){
	        			try{
	        		        led6.setRGB(255, 0, 0); led6.setOn();
	        				
	        	            // write data to stream
	        				dos.writeUTF(allMeas[meas].address);
	    	        		dos.writeFloat(allMeas[meas].frequency);
	    	        		dos.writeDouble(allMeas[meas].magnitude);
	    	        		dos.writeDouble(allMeas[meas].prediction);
	    	        		dos.writeInt(allMeas[meas].error);
	    	        		
	    					dos.flush();
	    					// check if basestation received stream
	    					okay = dis.readBoolean();
		    				led6.setOff();
	        			} catch(Exception e){
	               		 	System.err.println("Caught " + e + " in exchanging data 1.");
	    					continue;
	    				}
	        			meas++;
	        		}
        			//turn off
        			led5.setOff();
        			// close streams and connection
	        		dis.close(); dos.close(); conn.close();
	    		} catch (Exception e) {   
	        		 System.err.println("Caught " + e + " in exchanging data 2.");
	        	}
        	}
        }
}
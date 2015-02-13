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
        
        RadiostreamConnection conn;
        DataInputStream dis;
        DataOutputStream dos; 
        
        public void EstablishStreamConnection(int hostPort, String otherAddress){
            try {
                // Open up a broadcast connection to the host port
                // where the 'on Desktop' portion of this demo is listening
                conn = (RadiostreamConnection)Connector.open("radiostream://" + otherAddress + ":" + hostPort);
                
                DataInputStream dis = conn.openDataInputStream();
                DataOutputStream dos = conn.openDataOutputStream();
                
                System.out.println("Starting communication on" + ourAddress + " with " + otherAddress);
                
                dos.writeUTF("HALLO");
                
                
            } catch (Exception e) {
                System.err.println("Caught " + e + " in connection initialization.");
            }	
        }

        public Measurement ReceiveData(Measurement ourMeas, String otherAddress, int hostPort) {
            //object for other measurement
            Measurement othMeas = new Measurement();
        	try {     
        		
        		System.out.println("starting ExchangeData. ");
        		conn = (RadiostreamConnection)Connector.open("radiostream://" + otherAddress + ":" + hostPort);
        		// set timeout for connection
        		// conn.setTimeout(10000); 
        		DataInputStream dis = conn.openDataInputStream();
        		DataOutputStream dos = conn.openDataOutputStream();
                 
        		System.out.println("Starting communication on" + ourAddress + " with " + otherAddress);

				System.out.println("try to recieve." );	            
			
	            othMeas.address = otherAddress;
	    		othMeas.magnitude = dis.readDouble();
	    		othMeas.frequency = dis.readFloat();
	    		
				System.out.println("datastream received." );
	    		
		        // blink third LED
		        led3.setRGB(0, 255, 255);
	            led3.setOn();
				Utils.sleep(500);
	            led3.setOff();
	            
	            
        		dos.writeBoolean(true);
				dos.flush();
				System.out.println("received message flushed." );
				
		        // blink second LED
		        led2.setRGB(0, 255, 0);
	            led2.setOn();
	            
	            
        		dis.close();
        		dos.close();
        		conn.close();
        		
				Utils.sleep(500);
        		led2.setOff();
				led3.setOff();
	    		        		
        		
        	} catch (Exception e) {   
        		 System.err.println("Caught " + e + " in exchanging data.");
        	} finally {

        		
	    		return othMeas;
        	}

        	
        }
}
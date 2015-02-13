package sensornode;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.util.Utils;

import javax.microedition.io.*;

public class CommunicationBase {
		
		// open radiogramm connection
        RadiogramConnection rCon = null;
        Datagram dg = null;
        String ourAddress = System.getProperty("IEEE_ADDRESS");
        ITriColorLED led = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED1");
        
        public void EstablishConnection(int hostPort){
            try {
                // Open up a broadcast connection to the host port
                // where the 'on Desktop' portion of this demo is listening
                rCon = (RadiogramConnection) Connector.open("radiogram://broadcast" + hostPort);
                dg = rCon.newDatagram(rCon.getMaximumLength());
                System.out.println("Starting communication on " + ourAddress);
            } catch (Exception e) {
                System.err.println("Caught " + e + " in connection initialization.");
            }	
        }
        
        public void SendMeasurement(double magnitude, double frequencyMax) {
        	try {
	        	// Pack time and sensor reading into a radio datagram, send it.
		        dg.reset();
		        dg.writeDouble(magnitude);
		        // cast frequency to double
		        float frequency = (float) frequencyMax;
		        dg.writeFloat(frequency);
		        rCon.send(dg);
		        System.out.println("SENT!");
		        
		        // blink first LED
		        led.setRGB(255, 255, 0);
	            led.setOn();
				Utils.sleep(10);
	            led.setOff();
        	 } catch (Exception e) {   
        		 System.err.println("Caught " + e + " in sending data.");
        	 }
        }
        
}
        
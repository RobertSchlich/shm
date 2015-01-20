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

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;

public class SyncSpots {
   
	// Broadcast port on which we listen for sensor samples
    private int HOST_PORT;
    
    //Standard Konstruktor (Port für Datagram-connection)
    public SyncSpots(int HOST_PORT){
    	this.HOST_PORT = HOST_PORT;
    }//Ende stand.konstr
        
    public void sync() throws Exception {
    
        RadiogramConnection rSend;
        Datagram dgSend;
        
        try {
            //datagram-connection öffnen
            rSend = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
            dgSend = rSend.newDatagram(50);
            
        } catch (Exception e) {
             System.err.println("setUp caught " + e.getMessage());
             throw e;
        }
        
        System.out.println("Sende Startsignal...");

        //Stratsignal senden
        dgSend.reset();
        dgSend.writeBoolean(true);
        rSend.send(dgSend);
        
    }
    
}

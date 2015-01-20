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

import java.text.DateFormat;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.ota.OTACommandServer;
import com.sun.spot.util.Utils;


public class BaseStation {
												
         
    //parameters
    private int HOST_PORT = 66;
    private int streamPort = 99;
    private int searchingTime = 25000;
    
    //*********************************
    private double LEARNING_RATE = 0.15;
    private int SAMPLE_PERIOD = 40;
    private int AMOUNT_OF_DATA = 128;
    private double LEARN_LIM = 0.25;
    private int LEARN_COUNT = 100;
    private int Faktor = 1;				// wird jetzt für die glättung genutzt
    
    
    //*********************************
	
    private int Pause = 100000;
    
	private void run() throws Exception {
		
		//Spots finden und testsignal abrufen
		SpotFinder sf = new SpotFinder(HOST_PORT, streamPort, searchingTime);
		sf.searchForSpots();
		sf.sendAdressesToSpots();
		sf.sendParams(LEARNING_RATE, SAMPLE_PERIOD, AMOUNT_OF_DATA, LEARN_LIM, LEARN_COUNT, Faktor);
		
		System.out.println("Warte auf Kalibrierung...");
		Utils.sleep(18000);
		System.out.println("Noch 2 Sekunden...");
		Utils.sleep(2000);
		
		//synchronisieren (startschuss)
		SyncSpots ss = new SyncSpots(HOST_PORT);
		ss.sync();
		
		
		while(true){   
		Utils.sleep(Pause);
		System.out.println("Noch 10 Sekunden...");
		Utils.sleep(8000);
		System.out.println("Noch 2 Sekunden...");
		Utils.sleep(2000);   
		ss.sync();
		}
	}
    	
    public static void main(String[] args) throws Exception {
        // register the application's name with the OTA Command server & start OTA running
        OTACommandServer.start("BaseStation");

        BaseStation app = new BaseStation();
        app.run();
    }
}

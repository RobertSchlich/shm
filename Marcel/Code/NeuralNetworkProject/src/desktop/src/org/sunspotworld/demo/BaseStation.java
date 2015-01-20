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

import com.sun.spot.peripheral.ota.OTACommandServer;
import com.sun.spot.util.Utils;

public class BaseStation {											
         
    //parameters
    private int HOST_PORT = 66;
    private int STREAM_PORT = 99;
    private int SEARCHING_TIME = 25000;    
    //*********************************
    private double LEARNING_RATE = 0.15;
    private int SAMPLE_PERIOD = 40;
    private int AMOUNT_OF_DATA = 128;
    private double LEARN_LIM = 0.25;
    private int LEARN_COUNT = 100;
    private int FACTOR = 1;				// wird jetzt für die glättung genutzt
    //*********************************
	
    private int pause = 20000;
    
	private void run() throws Exception {
		
		//Nodes finden und testsignal abrufen
		NodeFinder sf = new NodeFinder(HOST_PORT, STREAM_PORT, SEARCHING_TIME);
		sf.searchForNodes();
		sf.sendAdressesToNodes();
		sf.sendParams(LEARNING_RATE, SAMPLE_PERIOD, AMOUNT_OF_DATA, LEARN_LIM, LEARN_COUNT, FACTOR);
		
		System.out.println("Warte auf Kalibrierung...");
		Utils.sleep(10000);
		System.out.println("Noch 2 Sekunden...");
		Utils.sleep(2000);
		
		//synchronisieren (startschuss)
		SyncNodes ss = new SyncNodes(HOST_PORT);
		ss.sync();
		Utils.sleep(pause);
		
		ProtocolReader protocol = new ProtocolReader(AMOUNT_OF_DATA, sf.getAdresses(), STREAM_PORT, SAMPLE_PERIOD);
		
		int iterator = 1;
		while(true){
			System.out.println("Noch 10 Sekunden...");
			Utils.sleep(8000);
			System.out.println("Noch 2 Sekunden...");
			Utils.sleep(2000);
			ss.sync();
			Utils.sleep(pause);
			protocol.collectData(iterator + "_measurement_");
			protocol.collectData(iterator + "_calculation_");
			iterator++;
		}
		
	}
    	
    public static void main(String[] args) throws Exception {
        // register the application's name with the OTA Command server & start OTA running
        OTACommandServer.start("BaseStation");

        BaseStation app = new BaseStation();
        app.run();
    }
}

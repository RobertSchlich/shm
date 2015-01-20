package desktop.src.org.sunspotworld.demo;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;

public class SyncNodes {
   
	// Broadcast port on which we listen for sensor samples
    private int HOST_PORT;
    
    /**
     * Sets the integer value for the port of the radiogram connection.
     * 
     * @param HOST_PORT
     */
    public SyncNodes(int HOST_PORT){
    	this.HOST_PORT = HOST_PORT;
    }//Ende SyncNodes()
        
    /**
     * Opens a radiogram connection and sends a synchronization signal on a defined port to all nodes.
     * 
     * @throws Exception
     */
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

        //Startsignal senden
        dgSend.reset();
        dgSend.writeBoolean(true);
        rSend.send(dgSend);
        
    }//Ende sync()
    
}

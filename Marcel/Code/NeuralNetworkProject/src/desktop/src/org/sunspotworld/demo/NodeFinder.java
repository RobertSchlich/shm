package desktop.src.org.sunspotworld.demo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.util.Utils;

public class NodeFinder {

	private ArrayList<String> adresses = new ArrayList<String>();
	
	//BroadCast-Connections
	private RadiogramConnection broadcastConnectionIn;
    private DatagramConnection broadcastConnectionOut;
    private Datagram dgSend;
    private Datagram dgReceive;
    
    //Stream-Connections
    private RadiostreamConnection listenOn;
    private RadiostreamConnection sendOn;
    private DataInputStream listenOnInputStream;
    private DataOutputStream sendOnOutputStream;
    
    private static int MAX_BROADCAST_SIZE;
    
    //parameters
    private int initialBroadCastPort;
    private int streamPort;
    private int searchingTime;
    
    /**
     * Overwrites the integer values for the opening of the radiogram and radiostream connections and sets the time that is applied for searching the nodes.
     * 
     * @param HOST_PORT used for the radiogram connection
     * @param streamPort used for the radiostream connection
     * @param searchingTime time in milliseconds for searching the nodes
     */
    public NodeFinder(int HOST_PORT, int streamPort, int searchingTime){
    	
    	initialBroadCastPort = HOST_PORT;
    	this.streamPort = streamPort;
    	this.searchingTime = searchingTime;
    	
    }//Ende NodeFinder()
    
    /**
     * Starts all processes of searching for nodes and checking the connections.
     * For this a radiogram connection on the {@code initialBroadCastPort} is used.
     * The {@code listenOnNodesThread} is started an interrupted after a defined time {@code searchingTime}.
     * Afterwards all opened radiostream connections will be tested in the {@code listenOnTestSignalThread}. 
     * 
     * @throws Exception
     */
	public void searchForNodes() throws Exception {
    	
    	try{
    		//start broadcast-connections
			broadcastConnectionIn = (RadiogramConnection) Connector.open("radiogram://:" + initialBroadCastPort);
	        MAX_BROADCAST_SIZE = broadcastConnectionIn.getMaximumLength();
	        broadcastConnectionOut = (DatagramConnection) Connector.open("radiogram://broadcast:" + (initialBroadCastPort+1) );
	        //Start datagrams
	        dgSend = broadcastConnectionOut.newDatagram(MAX_BROADCAST_SIZE);
	        dgReceive = broadcastConnectionIn.newDatagram(MAX_BROADCAST_SIZE);
	       
	        Thread listenOnNodesThread = startListenOnNodesThread();
	        Utils.sleep(searchingTime);
	        listenOnNodesThread.interrupt();
	        
	        for(int i=0; i<adresses.size(); i++){
	        	openListenStreamConnection(i);
	        	Thread listenOnTestSignalThread = startListenOnTestSignalThread(i);
	        	Utils.sleep(10000);
	        	listenOnTestSignalThread.interrupt();
	        }
	        
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
    	
    }//Ende searchForNodes()
        
    /**
     * This function is embedded in the {@code startListenOnNodesThread()}, wich is interrupted after a defined time ({@code searchingTime}).
     * If a signal over the radiogram connection is received, the address of the emitter is saved into the {@code adresses} list.
     * Sending the next message, the found node gets all the defined connection parameters, wich are its special index and the stream port.
     * 
     * @throws Exception
     */
    private void listenOnNodes() throws Exception{
        
    	//datagram von Node x empfangen	
        try{
        	broadcastConnectionIn.receive(dgReceive);
        } catch(InterruptedIOException iioe){
        	System.out.println("listenOnNodes abgebrochen");
        	return;
        }
        String otherAddress = dgReceive.getAddress();
        int nodeIndex = 0; 
    	//kontrolle ob adresse in liste vorhanden
        if(addressExist(otherAddress)==false){
        	adresses.add(otherAddress);	//Adresse speichern
        	nodeIndex = adresses.size();
            System.out.println("Node gefunden:\nNr.: " + nodeIndex + "\nAdresse: " + otherAddress);
        } else{
        	adresses.indexOf(otherAddress);
	        System.out.println("Sende wiederholt informationen für " + otherAddress);
        }
        //datagram mit informationen für node
        dgSend.reset();
        dgSend = broadcastConnectionOut.newDatagram(broadcastConnectionOut.getMaximumLength());
        dgSend.writeUTF(otherAddress);			//node-adresse
        dgSend.writeInt(adresses.size()-1);		//node id
        dgSend.writeInt(streamPort);			//stream-port
        broadcastConnectionOut.send(dgSend);
            
        dgReceive.reset();
        dgSend.reset();
                
    }//Ende listenOnNodes()
    
    /**
     * Checks whether a address exists in the {@code adresses} list or not. 
     * 
     * @param address
     * @return {@code true} if the address exists in the list, {@code false} if the address does not exist in the list.
     */
    private boolean addressExist(String address) {
    	
    	boolean exist = false;
    	
    	if(address.equals("0000.0000.0000.0000"))
    		return true;
    	
    	if(adresses.contains(address))
    		exist=true;
    	
		return exist;
		
	}//Ende addressExist()

    /**
     * Listens to a test signal from  a defined node to check the radiostream connection.
     * 
     * @param index of the node in the {@code adresses} list
     * @throws Exception
     */
    private void listenOnTestSignal(int index) throws Exception{
    	 
    	while(true){
    		if(listenOn==null || listenOnInputStream==null){
    			System.out.println("kein Stream verfügbar");
    			Utils.sleep(100);
    			continue;
    		}
    		System.out.print("listen on signal: ");
		    String message;
		    try{
		    	message = listenOnInputStream.readUTF();
		    } catch(InterruptedIOException iioe){
		    	System.out.println("Stream.read() abgebrochen");
		    	break;
		    }
		    System.out.println(message);
		    break;
    	}
    	
    	listenOnInputStream.close();
    	listenOn.close();
    	
    }//Ende listenOnTestSignal()
    
    /**
     * Opens the radiostream connection and the data input stream for a defined address of a node.
     * 
     * @param index of the node in the {@code adresses} list
     * @throws Exception
     */
    private void openListenStreamConnection(int index) throws Exception{
    	System.out.println("Test-Stream " + (index+1) + ":");
    	listenOn = (RadiostreamConnection) Connector.open("radiostream://" + adresses.get(index) + ":" + streamPort);
    	listenOnInputStream = listenOn.openDataInputStream();
    }//Ende openListenStreamConnection()

    /**
     * Opens the radiostream connection and the data output stream for a defined address of a node.
     * 
     * @param index
     * @throws Exception
     */
    private void openSendStreamConnection(int index) throws Exception{
    	System.out.println("open Send-Stream " + (index+1) + ":");
    	sendOn = (RadiostreamConnection) Connector.open("radiostream://" + adresses.get(index) + ":" + (streamPort+index+1));
    	sendOnOutputStream = sendOn.openDataOutputStream();
    }//Ende openListenStreamConnection()    
    
    /**
     * Runs a Thread executing the {@link #listenOnNodes() listenOnNodes()} function until the Thread is interrupted.
     * 
     * @return The running Thread.
     */
    private Thread startListenOnNodesThread(){
    	//Thread zur wiederholten suche nach nodes
    	Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {

			    	System.out.println("suche nach nodes für die nächsten " + (searchingTime/1000) + " sec");
			    	
					while(true){
						listenOnNodes();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
    	t.start();
    	
    	return t;
    	
    }//Ende startListenOnNodesThread()
	
    /**
     * Runs a Thread executing the {@link #listenOnTestSignal(int) listenOnTestSignal()} function with a defined {@code index} until the Thread is interrupted.
     * 
     * @param index
     * @return The running Thread.
     */
    private Thread startListenOnTestSignalThread(final int index){
    	//Thread zum lesen des Testsignals
    	Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					listenOnTestSignal(index);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
    	t.start();
    	
    	return t;
    }//ende startListenOnTestSignalThread()
    
    
    /**
     * Sends the {@code adresses} list to all nodes to make a communication between the nodes themselves possible.
     * For the transmission of the data the radiostream connection is used.
     * 
     * @throws Exception
     */
    public boolean sendAdressesToNodes() throws Exception{
    	
    	//Datagram leeren
    	dgSend.reset();
    	
    	for(int i=0; i<adresses.size(); i++){
    		//verbindung öffnen
    		openSendStreamConnection(i);
    		//controlle auf offene verbindung
    		if(sendOn == null){
    			System.out.println("Node " + i + " nicht erreichbar");
    			return false;
    		}
    		//Nodeanzahl schicken
    		sendOnOutputStream.writeInt(adresses.size());
    		sendOnOutputStream.flush();
    		
    		//adressen in stream schreiben
    		for(String s: adresses)
    			sendOnOutputStream.writeUTF(s);
    		//senden...
    		sendOnOutputStream.close();
    		
    		Utils.sleep(700);
    	}
    	
    	Utils.sleep(2000);
    	
    	return true;
    	
    }//Ende sendAdressesToNodes()
    
    /**
     * Sends all the defined parameters to all nodes to set them into their system.
     * For the transmission of the data the radiostream connection is used.
     * 
     * @param learning_rate
     * @param sample_period
     * @param amount_of_data
     * @param learn_lim
     * @param learn_count
     * @param factor
     * @return
     * @throws Exception
     */
    public boolean sendParams(double learning_rate, int sample_period, int amount_of_data, double learn_lim, int learn_count, int factor) throws Exception{
    	
    	for(int i=0; i<adresses.size(); i++){
    		//verbindung öffnen
    		openSendStreamConnection(i);
    		//controlle auf offene verbindung
    		if(sendOn == null){
    			System.out.println("Node " + i + " nicht erreichbar");
    			return false;
    		}
    		//Nodeanzahl schicken
    		sendOnOutputStream.writeDouble(learning_rate);
    		sendOnOutputStream.writeInt(sample_period);
    		sendOnOutputStream.writeInt(amount_of_data);
    		sendOnOutputStream.writeDouble(learn_lim);
    		sendOnOutputStream.writeInt(learn_count);
    		sendOnOutputStream.writeInt(factor);
    		//senden...
    		sendOnOutputStream.close();
    		
    		Utils.sleep(700);
    	}
    	
    	sendOnOutputStream.close();
    	sendOn.close();
    	listenOnInputStream.close();
    	listenOn.close();
    	
    	Utils.sleep(2000);
    	
    	return true;
    	
    }//Ende sendParams()
    

	/**
	 * @return the adresses
	 */
	public ArrayList<String> getAdresses() {
		return adresses;
	}


	
}

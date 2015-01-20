package desktop.src.org.sunspotworld.demo;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

import javax.microedition.io.Connector;

import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.util.Utils;

public class ProtocolReader {


    //Stream-Connections
    private RadiostreamConnection listenOn;
    private RadiostreamConnection sendOn;
    private DataInputStream listenOnInputStream;
    private DataOutputStream sendOnOutputStream;
    
    //parameter
    private int length;
    private ArrayList<String> addresses;
    private int defaultPort;
    private int sample_period;
    
    private String directory = System.getProperty("user.home") + "/Documents/SensorValues";
        
    public ProtocolReader(int length, ArrayList<String> addresses, int defaultPort, int sample_period){
    	
    	//werte übergeben
    	this.length = length;
    	this.addresses = addresses;
    	this.defaultPort = defaultPort;
    	this.sample_period = sample_period;
    	
    }//Ende ProtocolReader()
    
    /**
     * Opens radiostream connections for sending an listening to a defined spot.
     * 
     * @param index
     * @throws IOException
     */
    private void openStreams(int index) throws IOException{

    	listenOn = (RadiostreamConnection) Connector.open("radiostream://" + addresses.get(index) + ":" + defaultPort);
    	listenOnInputStream = listenOn.openDataInputStream();
    	
    	sendOn = (RadiostreamConnection) Connector.open("radiostream://" + addresses.get(index) + ":" + (defaultPort + index + 1));
    	sendOnOutputStream = sendOn.openDataOutputStream();
    	
    }//Ende openStreams()
    
    
    /**
     * Opens the different Stream connections and collects the values from the spot.
     * Every value is saved in a .txt-file in the documents folder and can be accessed in the chart program.
     * 
     * @param ID information about the meaning of the data [learning/testing]
     * 
     * @throws IOException
     */
    public void collectData(String ID) throws IOException{
    	
    	ArrayList<Double> values = new ArrayList<Double>();
    	
    	for(String address: addresses){
    		
    		openStreams(addresses.indexOf(address));
    		
    		//loop for cellection of measured values
    		for(int i=0; i<length; i++){
    			values.add(listenOnInputStream.readDouble());
    		}
    		
    		Utils.sleep(1000);
    		
    		//answer for the spot
    		sendOnOutputStream.writeBoolean(true);
    		sendOnOutputStream.flush();
    		
    		Utils.sleep(1000);
    		
    		sendOn.close();
        	listenOnInputStream.close();
        	listenOn.close();
        	sendOnOutputStream.close();
        	
        	writeToFiles(ID, addresses.indexOf(address), values);
    		
    	}
    	
    }//Ende collectData()
	
    /**
     * writes the values in a .txt-file, depending on the defined parameters
     * 
     * @param ID
     * @param index
     * @param values
     * @throws IOException
     */
    private void writeToFiles(String ID, int index, ArrayList<Double> values) throws IOException{
    	
    	String filename = directory + "/" + ID + "sensorValues_" + (index+1) + ".txt";
    	
    	DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator('.');
    	DecimalFormat df = new DecimalFormat("#0.0000", dfs);
    	
    	createFile(filename);
    	
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
		
		writer.write(sample_period + "\n");
		writer.newLine();
		
		for(double d: values){
			writer.write(df.format(d));
			writer.newLine();
		}
		
		writer.close();
    	
    }//Ende writeToFiles()
    
    
    /**
     * Creates the directory and the file for the values.
     * 
     * 
     * @param filename
     * @throws IOException
     */
    private void createFile(String filename) throws IOException{
    	
		File f = new File(directory);
		{
			if (f.isDirectory()) {
			} else {
				f.mkdirs();
			}
		}
		
		PrintWriter create = new PrintWriter(new FileWriter(new File(filename)));
		create.print("");
		create.close();
		
    }//Ende createFile
    
}

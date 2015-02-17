package basestation;

/* Commented imports, because they are unused
import com.sun.spot.io.j2me.radiogram.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
*/

import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.microedition.io.*;

public class MainBase {
    // Broadcast port on which we listen for sensor samples
    private static final int HOST_PORT = 68;
    private static final String DATABASE_URL = "jdbc:mysql://141.54.51.238:3306/";
    private static final String DATABASE_NAME = "SunSPOT";
    private static final String DATABASE_USER = "shm";
    private static final String DATABASE_PASSWORD = "pass";
    private static final String TABLE_NAME = "measurements";
    //private int samplerate = 76;  // in milliseconds
    
    private String otherAddress = "0014.4F01.0000.763F";
    
    String ourAddress = System.getProperty("IEEE_ADDRESS");
    
    private void run() throws Exception {
    	
    	//Establish a radio connection to the sensor nodes
        RadiostreamConnection conn;
        DataInputStream dis;
        DataOutputStream dos; 

        while(true){
        	
	        Measurement othMeas = new Measurement();
	    	try {     
	    		
	    		// COMMUNICATION
	    		System.out.println("starting ExchangeData. ");
	    		conn = (RadiostreamConnection)Connector.open("radiostream://" + otherAddress + ":" + HOST_PORT);
	    		dis = conn.openDataInputStream();
	    		dos = conn.openDataOutputStream();
	             
	    		System.out.println("Starting communication on" + ourAddress + " with " + otherAddress);
	
				System.out.println("try to recieve." );	
				// receive data from stream
	            othMeas.address = dis.readUTF();
	            System.out.println("Adress: " + othMeas.address);
	            
	    		othMeas.frequency = dis.readFloat();
	    		System.out.println("Frequency: " + othMeas.frequency);
	    		
	    		othMeas.magnitude = dis.readDouble();
	    		System.out.println("Magnitude: " + othMeas.magnitude);
	    		
	    		othMeas.error = dis.readInt();
	    		System.out.println("Error: " + othMeas.error);
	    		
				System.out.println("object received." );
	            
	    		//Utils.sleep(1000);
	    		
	            // send "okay"
	    		dos.writeBoolean(true);
				dos.flush();
				System.out.println("received message flushed." );
	    		
	            // close streams and connection
	    		dis.close();
	    		dos.close();
	    		conn.close();
				
				//DATABASE
				//Create a DatabaseHandler, the DatabaseHandler will take care of 
		        // database interaction
		    	DatabaseHandler dbHandler = new DatabaseHandler();
		    	
		    	//Establish a connection with the MySQL-Database
		    	String url = DATABASE_URL + DATABASE_NAME;
		    	dbHandler.getConnection(url, DATABASE_USER, DATABASE_PASSWORD);

		        //Create table with specified name
		        dbHandler.createTableIfNeeded(TABLE_NAME);

		        // Main data collection loop
	            try {
	                // Write measurement to Database
	            	System.out.println("Writing to database!");
	                dbHandler.writeMeasurement(TABLE_NAME, othMeas);
	            } catch (Exception e) {
	                System.err.println("Caught " + e +  " while reading sensor samples.");
	                throw e;
	            }
			
	    	} catch (Exception e) {   
	    		 System.err.println("Caught " + e + " in exchanging data.");
	    	}



        }
    }
  

        
        
   
    public static void main(String[] args) throws Exception {
        // register the application's name with the OTA Command server 
    	// & start OTA running
        //OTACommandServer.start("MainBase");

        MainBase app = new MainBase();
        app.run();
    }
}

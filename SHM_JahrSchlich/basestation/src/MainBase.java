package basestation;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.peripheral.ota.OTACommandServer;
import com.sun.spot.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.microedition.io.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainBase {
    // Broadcast port on which we listen for sensor samples
    private static final int HOST_PORT = 68;
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DATABASE_NAME = "SunSPOT";
    private static final String DATABASE_USER = "shm";
    private static final String DATABASE_PASSWORD = "pass";
    private static final String TABLE_NAME = "measurements";
    private int samplerate = 76;  // in milliseconds
    
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
	    		othMeas.magnitude = dis.readDouble();
	    		othMeas.frequency = dis.readFloat();
	    		
				System.out.println("object received." );
	            
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
        OTACommandServer.start("SendDataDemo");

        MainBase app = new MainBase();
        app.run();
    }
}

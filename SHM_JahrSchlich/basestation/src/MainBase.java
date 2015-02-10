package basestation;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.peripheral.ota.OTACommandServer;

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
    private static final int HOST_PORT = 67;
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DATABASE_NAME = "SunSPOT";
    private static final String DATABASE_USER = "shm";
    private static final String DATABASE_PASSWORD = "pass";
    private static final String TABLE_NAME = "measurements";
    private int samplerate = 76;  // in milliseconds
    
    private void run() throws Exception {
    	
    	//Establish a radio connection to the sensor nodes
    	RadiogramConnection rCon;
        Datagram dg;
        DateFormat fmt = DateFormat.getTimeInstance();
         
        try {
            // Open up a server-side broadcast radiogram connection
            // to listen for sensor readings being sent by different SPOTs
            rCon = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
            dg = rCon.newDatagram(rCon.getMaximumLength());
        } catch (Exception e) {
             System.err.println("setUp caught " + e.getMessage());
             throw e;
        }
        
    	//Create a DatabaseHandler, the DatabaseHandler will take care of 
        // database interaction
    	DatabaseHandler dbHandler = new DatabaseHandler();
    	
    	//Establish a connection with the MySQL-Database
    	String url = DATABASE_URL + DATABASE_NAME;
    	dbHandler.getConnection(url, DATABASE_USER, DATABASE_PASSWORD);

        //Create table with specified name
        dbHandler.createTableIfNeeded(TABLE_NAME);

        // Main data collection loop
        while (true) {
            try {
                // Read sensor sample received over the radio
                rCon.receive(dg);
                String addr = dg.getAddress();  		 // read sender's Id
                double magnitude = dg.readDouble();      // read the magnitude
                float frequency = dg.readFloat();        // read the frequency
                // Write measurement to Database
                dbHandler.writeMeasurement(TABLE_NAME, addr, magnitude, frequency, samplerate);
            } catch (Exception e) {
                System.err.println("Caught " + e +  " while reading sensor samples.");
                throw e;
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

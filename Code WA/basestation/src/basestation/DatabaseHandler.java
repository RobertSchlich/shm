/**
 * @author Jahr&Schlich
 *
 */
package basestation;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;

public class DatabaseHandler {
	
	java.sql.Connection dbConn = null;
	
	public void getConnection(String url, String user, String password) {
        try {            
            // Get a connection to the database for given user/password
        	dbConn = DriverManager.getConnection(url, user, password);

            // Display URL and connection information
        	System.out.println("Database connection etablished.");
            System.out.println("\tURL: " + url);
            System.out.println("\tConnection: " + dbConn);
            
        } catch (Exception e) {
            System.err.println("setUp caught " + e);            
        }
	}
    
	public void createTableIfNeeded(String tableName) throws Exception {
		
		// Get a list of the names of all Tables in the Database
		ResultSet resultSet = dbConn.getMetaData().getTables(null, 
				null, tableName, null);
		// Check if any of the Tables already has the specified name
		if (resultSet.next()) {
			System.out.println("Table " + tableName + " already exists.");
		} else {
			// Create a table with the specified name
			System.out.println("Creating table " + tableName + ".");
			Statement statement = dbConn.createStatement();
			statement.execute("CREATE TABLE "+ tableName + " ("
					+ "Nr INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
					+ "Time TIMESTAMP, "
					+ "SpotID VARCHAR(19), "
					+ "Frequency FLOAT,"					
					+ "Magnitude DOUBLE, "
					+ "Prediction DOUBLE, "					
					+ "Error DOUBLE)");
			statement.close();
		}
		resultSet.close();
	}
	
	
	public void writeMeasurement(String tableName, 
								Measurement meas) throws Exception {
		
		// Get current systemtime as timestamp 
		// to avoid trouble with unsynchronized SunSPOTs
		Calendar calendar = Calendar.getInstance();
        Timestamp now = new Timestamp(calendar.getTime().getTime());
        
        // Calculate the deviation of predicted and measured magnitude
        double error = 0;
        // If no predicted magnitude is given, leave the error at 0
        if (meas.prediction != 0) error = 
        		(Math.abs(meas.magnitude-meas.prediction)
        				/(meas.prediction))*100;
        
        // Write the measurement into the Database
		System.out.println("Writing measurement into Database");
		PreparedStatement preparedStatement = 
				dbConn.prepareStatement("INSERT INTO " + tableName
				+ " (Time, SpotID, Frequency, Magnitude, Prediction, Error) "
				+ "VALUES (?,?,?,?,?,?)");
			preparedStatement.setTimestamp(1, now);
			preparedStatement.setString(2, meas.address);
			preparedStatement.setFloat(3, meas.frequency);
			preparedStatement.setDouble(4, meas.magnitude);			
			preparedStatement.setDouble(5, meas.prediction);
			preparedStatement.setDouble(6, error);
			preparedStatement.execute();
			System.out.println(preparedStatement);
			preparedStatement.close();
	}
}
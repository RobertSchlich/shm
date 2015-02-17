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
            System.err.println("Make sure that mySQL is installed properly \n" +
                    "and has a Test database accessible via the \n" +
                    "default administrator settings on localhost:3306\n");            
        }
	}
    
	public void createTableIfNeeded(String tableName) throws Exception {
		
		// Get a list of the names of all Tables in the Database
		ResultSet resultSet = dbConn.getMetaData().getTables(null, null, tableName, null);
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
					+ "Magnitude DOUBLE, "
					+ "Frequency FLOAT, "
					+ "Error INT)");
			statement.close();
		}
		resultSet.close();
	}
	
	
	public void writeMeasurement(String tableName, 
								Measurement meas) throws Exception {
		
		//Get current systemtime as timestamp to avoid trouble with unsynchronized SunSPOTs
		Calendar calendar = Calendar.getInstance();
        Timestamp now = new Timestamp(calendar.getTime().getTime());
        
		System.out.println("Writing measurement into Database");
		PreparedStatement preparedStatement = dbConn.prepareStatement("INSERT INTO "
				+ tableName
				+ " (Time, SpotID, Magnitude, Frequency, Error) VALUES (?,?,?,?,?)");
			preparedStatement.setTimestamp(1, now);
			preparedStatement.setString(2, meas.address);
			preparedStatement.setDouble(3, meas.magnitude);
			preparedStatement.setFloat(4, meas.frequency);
			preparedStatement.setFloat(5, meas.error);
			preparedStatement.execute();
			System.out.println(preparedStatement);
		preparedStatement.close();
	}
	
}
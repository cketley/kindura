package org.kindura;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * This class is used to handle MySQL database connection.
 * @author Jun Zhang
 */
public class DatabaseConnector {
	
	private Connection connection = null;
    
	/*
	 * Connect to the database.
	 */
	public void connectDatabase(String username, String password, String database) {
    	System.out.println("Start to connect to database server");
    	ConfigurationFileParser cfp = new ConfigurationFileParser();
    	try
        {
            //String url = "jdbc:mysql://localhost:3306/"+database;
            String url = cfp.kinduraParameters.get("MySQLURL")+database;
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            connection = DriverManager.getConnection (url, username, password);
            System.out.println ("Database connection established");
        }
        catch (Exception e)
        {
            System.err.println ("Cannot connect to database server");
        }
    }
    
	/*
	 * Authenticate user with user name and password.
	 */
	public String queryUser(String username, String password) {
    	try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT username, password FROM users");
            System.out.println("SELECT username, password FROM users");
            while (rs.next()) {
            	if (rs.getString("username").equals(username) && rs.getString("password").equals(password)) {
            		return username;
            	}
            }
        }
        catch (Exception e)
        {
            System.err.println ("Query database server error");
        }
        return null;
    }
    
	/*
	 * Terminate the connection to the database.
	 */
	public void disconnectDatabase() {
    	if (connection != null)
        {
            try {
            	connection.close();
            	System.out.println("Database connection terminated");
            }
            catch (Exception e) {
            }
        }
    }
}

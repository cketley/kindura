package org.kindura;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This class is used to authenticate user by checking username and password in the local server's MySQL database.
 * @author Jun Zhang
 */
public class LoginRequestHandler extends HttpServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] usernameAndRole = validateLogin(request.getParameter("username"),request.getParameter("password"));
		
		
		//If the user is authenticated, send the homepage of Kindura Cloud Repository to the user.
		if (usernameAndRole[0] != null) {
			HttpSession session = request.getSession(true);
			session.setAttribute("username", usernameAndRole[0]);
			session.setAttribute("role", usernameAndRole[1]);
			System.out.println("User '"+usernameAndRole[0]+"' is authenticated");
			response.sendRedirect("home.jsp");
		} 
		//If the user is NOT authenticated, send the login error page to the user.
		else {
			System.out.println("User '"+usernameAndRole[0]+"' is NOT authenticated");
			response.sendRedirect("loginerror.jsp");
		}
	}
	
	/*
	 * validate the username and password.
	 */
	public String[] validateLogin(String username, String password) {
		DatabaseConnector databaseConnector = new DatabaseConnector();	
		ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
		//Connect to the database.
		databaseConnector.connectDatabase(configurationFileParser.getKinduraParameters().get("MySQLUsername"), configurationFileParser.getKinduraParameters().get("MySQLPassword"), configurationFileParser.getKinduraParameters().get("MySQLDatabase"));
		
		//Query username and password.
		String[] usernameAndRole = databaseConnector.queryUser(username, password);
		
		//Terminate the connection the database.
		databaseConnector.disconnectDatabase();
		
		return usernameAndRole;
	}
}

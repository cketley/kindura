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
		DatabaseConnector databaseConnector = new DatabaseConnector();	
		ConfigurationFileParser cfp = new ConfigurationFileParser();
		//Connect to the database.
		//databaseConnector.connectDatabase("root", "globaltime", "kindura_users");
		databaseConnector.connectDatabase(cfp.kinduraParameters.get("MySQLUsername"), cfp.kinduraParameters.get("MySQLPassword"), cfp.kinduraParameters.get("MySQLDatabase"));
		
		//Query username and password.
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String returnedusername = databaseConnector.queryUser(username, password);
		
		//If the user is authenticated, send the homepage of Kindura Cloud Repository to the user.
		if (returnedusername != null) {
			HttpSession session = request.getSession(true);
			session.setAttribute("username", returnedusername);
			System.out.println("User '"+returnedusername+"' is authenticated");
			response.sendRedirect("home.jsp");
		} 
		//If the user is NOT authenticated, send the login error page to the user.
		else {
			System.out.println("User '"+returnedusername+"' is NOT authenticated");
			response.sendRedirect("loginerror.jsp");
		}
		
		//Terminate the connection the database.
		databaseConnector.disconnectDatabase();
	}
}

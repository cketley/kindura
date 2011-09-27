<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.*, java.sql.*, java.util.*, java.net.*" %>
<jsp:useBean id="databaseConnector" class="org.kindura.DatabaseConnector" scope="session"/>

<html xmlns="http://www.w3.org/1999/xhtml">
	<body>
		<%
			databaseConnector.connectDatabase("root", "globaltime", "kindura_users");
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String returnedusername = databaseConnector.queryUser(username, password);
			
			//Valid login
			if (returnedusername != null) {
				session.setAttribute("username", returnedusername);
				System.out.println("User '"+returnedusername+"' is authenticated");
				response.sendRedirect("home.jsp");
			} 
			//Invalid login
			else {
				System.out.println("User '"+returnedusername+"' is NOT authenticated");
				response.sendRedirect("loginerror.jsp");
			}
			databaseConnector.disconnectDatabase();
		%>
	</body>
</html>
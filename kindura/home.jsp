<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.*, java.util.*, java.net.*" %>

<%
	//Get the username and set the relevant attribute.
	if (session.getAttribute("username") != null) {
	    // Valid login
		String username = session.getAttribute("username").toString();
        session.setAttribute("username", username);
	} else {
        // Invalid login
        response.sendRedirect("sessiontimeout.jsp");
	}
%>

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>Kindura Cloud Repository</title>
		<link href="<%= request.getContextPath() %>/css/r4r.css" type="text/css" rel="stylesheet" media="screen" />
	</head>
	<body>
		<div id="header">
			<h1>Kindura Cloud Repository</h1>
		</div>
		<table width="100%" border="0">
			<tr valign="top">
				<!--<td style="background-color:#F0F0F0;width:70px;text-align:center;">-->
				<td style="background-color:#F0F0F0;width:70px;text-align:center;">
				
					</br>
					<b>Home</b></br>
					<a href="upload.jsp">Upload</a></br>
					<a href="search.jsp">Search</a></br>
					<a href="help.jsp">Help</a></br></br></br>
					<a href="logout.jsp">Log Out</a>
				</td>
				<td style="height:550px;width:700px;text-align:center;">
					</br></br>
					<b>Welcome to Kindura Cloud Repository.</b>
				</td>
			</tr>	
		</table>
		<p style="font-family:arial;color:#800517;font-size:15px;text-align:center;">
			Kindura Project 2011		
		</p>
	</body>
</html>
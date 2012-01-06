<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.*, java.util.*, java.net.*" %>

<%@ page errorPage="login.jsp" %>

<%
	//Get the username and set the relevant attribute.
	if (session.getAttribute("username") != null) {
	    // Valid login
		String username = session.getAttribute("username").toString();
        session.setAttribute("username", username);
		session.setAttribute("role", session.getAttribute("role").toString());
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
				<td class="menucolumn">
					<br/>
					<br/>
					&nbsp &nbsp<a href="home.jsp" class="LinkToPage">Home</a><br/>
					&nbsp &nbsp<a href="addproject.jsp" class="LinkToPage">Add project</a><br/>
					&nbsp &nbsp<a href="addcollection.jsp" class="LinkToPage">Add collection</a><br/>
					&nbsp &nbsp<a href="search.jsp" class="LinkToPage">Search</a><br/>
					&nbsp &nbsp<a href="viewproject.jsp" class="LinkToPage">View</a><br/>
					&nbsp &nbsp<a href="help.jsp" class="LinkToPage">Help</a></br/></br/></br/>
					&nbsp &nbsp<a href="logout.jsp" class="LinkToPage">Log Out</a>
				</td>
				<td style="background-color:#FFFFFF;height:550px;width:700px;text-align:center;">
					</br></br>
					<font size=4 face="verdana" color=FF0000>
						<b>Error: Project "<%=session.getAttribute("projectname")% class="LinkToPage">" is already existed. Please try again.</b> 
						<br><br>
						<a href="javascript:history.back(-1)">Go back</a>
					</font>
				</td>
			</tr>	
		</table>
		<%@ include file="bottompage.jsp" %>
	</body>
</html>
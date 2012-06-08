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
					&nbsp &nbsp<b class="CurrentPage">Home</b><br/>
					&nbsp &nbsp<a href="addproject.jsp" class="LinkToPage">Add project</a><br/>
					&nbsp &nbsp<a href="addcollection.jsp" class="LinkToPage">Add collection</a><br/>
					&nbsp &nbsp<a href="search.jsp" class="LinkToPage">Search</a><br/>
					&nbsp &nbsp<a href="viewproject.jsp" class="LinkToPage">View</a><br/>
					&nbsp &nbsp<a href="help.jsp" class="LinkToPage">Help</a></br/></br/></br/>
					&nbsp &nbsp<a href="logout.jsp" class="LinkToPage">Log Out</a>
				</td>
				<td style="height:550px;width:700px;">
					<br/>
						<h1 style="color:#F77A52;font-size:18px;text-align:center">Welcome to Kindura Repository:</h1>
					<table width="100%" border="0" cellspacing="15" style="font-size:15px;">
						<!--<tr>
							<td><hr class="SeparatedLine"/></td>
							<td><hr class="SeparatedLine"/></td>
						</tr>-->
						<tr>
							<td style="vertical-align:top;width:200px;"><p style="color:white;background-color:#5D7359;text-align:center"><b>Intro</b></p>
							</td>
							<td>
							<p>
							Pilot the use of hybrid cloud infrastructure to provide repository-focused services to researchers.
							</p>
							Managing research outputs:
							<ul>
							<li>Accessing Data storage services with cloud like interfaces.</li>
							<li>Using intelligent data placement.</li>
							<li>Following pre-defined policies.</li>
							</ul>
							Processing research outputs:
							<ul>
							<li>with common services provided by cloud providers.</li>
							</ul>
							</td>
						</tr>
						<!--<tr>
							<td><hr class="SeparatedLine"/></td>
							<td><hr class="SeparatedLine"/></td>
						</tr>-->
						<tr>
							<td style="vertical-align:top;width:200px;">
							<p style="color:white;background-color:#D6AA5C;text-align:center"><b>News</b></p>
							</td>
							<td>
								<p>
									1. <br/>
									2. <br/>
									3. <br/>
								</p>
							</td>
						</tr>
						<tr>
							<!--<td><hr class="SeparatedLine"/></td>
							<td><hr class="SeparatedLine"/></td>-->
						</tr>
						<tr>
							<td style="vertical-align:top;width:200px;">
							<p style="color:white;background-color:#8C5430;text-align:center"><b>Funders</b>
							</td>
							<td>
								<p>
									1. <br/>
									2. <br/>
									3. <br/>
								</p>
							</td>
						</tr>
						<tr>
							<!--<td><hr class="SeparatedLine"/></td>
							<td><hr class="SeparatedLine"/></td>-->
						</tr>
					</table>
				</td>
			</tr>	
		</table>
		<%@ include file="bottompage.jsp" %>
	</body>
</html>
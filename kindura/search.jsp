<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

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
				<td style="background-color:#F0F0F0;width:70px;text-align:center;">
						</br><a href="home.jsp">Home</a></br>
						<a href="upload.jsp">Upload</a></br>
						<b>Search</b></br>
						<a href="help.jsp">Help</a></br></br></br>
						<a href="logout.jsp">Log Out</a>
				</td>
				<td style="height:550px;width:700px;text-align:center;">
					<div id="search">
						<form action="SearchRequestHandler" method="get">
							<label>Search Term</label>
							<input type="text" id="search_term" name="search_term" value="<%= request.getParameter("search_term") == null ? "" : request.getParameter("search_term") %>"/>
							<select name="search_by" id="search_by">
								<option value="all">All Fields</option>
								<option value="pid">pid</option>
								<option value="label">label</option>
								<option value="creator">creator</option>
								<option value="cdate">created date</option>
								<option value="state">state</option>
							</select>
							<input type="submit" value="Search" id="search_button"/>			  
						</form>
					</div>
					<div id = "result">
						<form method="get" target="_blank" id="export_form">
							<table id="list">	
								<tr>
									<!--<th><input type="checkbox" id="check_all" title="Check all items on this page" /></th>-->
									<th>PID</th>
									<th>Label</th>
									<th>Creator</th>
									<th>Date Uploaded</th>
									<th>Storage Status</th>
									<th>Cost(£)</th>
									<th>Action Required</th>
									<th>View/Download</th>
								</tr>
							</table>
						</form>
					</div>
					<div id="export">
						<br><input type="hidden" id="submit_export" value="Download" disabled="disabled" /></br>
					</div>		
				</td>
			</tr>
		</table>
		<p style="font-family:arial;color:#800517;font-size:15px;text-align:center;">
			Kindura Project 2011		
		</p>
	</body>
</html>
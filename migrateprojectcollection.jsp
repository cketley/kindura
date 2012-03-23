<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page errorPage="login.jsp" %>
	
<%
	//Get the username and set the relevant attribute.
	String username = null;
	if (session.getAttribute("username") != null) {
	    // Valid login
		username = session.getAttribute("username").toString();
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
				
		<link type="text/css" href="css/jquery-ui-1.8.16.custom.css" rel="Stylesheet" />	
		<script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
		<script type="text/javascript" src="js/jquery-ui-1.8.16.custom.min.js"></script>
		<script type="text/javascript" src="js/jquery.ui.datepicker-en-GB.js"></script>

	</head>
	<body>
		<table width="100%" border="0">
			<div id="header">
				<h1>Kindura Cloud Repository</h1>
			</div>
			<tr valign="top">
				<td class="menucolumn">
					<br/>
					<br/>
					&nbsp &nbsp<a href="home.jsp" class="LinkToPage">Home</a><br/>
					&nbsp &nbsp<b>Add project</b><br/>
					&nbsp &nbsp<a href="addcollection.jsp" class="LinkToPage">Add Collection</a><br/>
					&nbsp &nbsp<a href="search.jsp" class="LinkToPage">Search</a><br/>
					&nbsp &nbsp<a href="viewproject.jsp" class="LinkToPage">View</a><br/>
					&nbsp &nbsp<a href="help.jsp" class="LinkToPage">Help</a></br/></br/></br/>
					&nbsp &nbsp<a href="logout.jsp" class="LinkToPage">Log Out</a>
				</td>
				
				<td style="background-color:#FFFFFF;height:550px;width:700px;text-align:center;">
					<form name="projectmetadata" action="MigrateRequestHandler" method="post">
						<br/>
						<h1 style="color:#F77A52;font-size:18px">Add Project:</h1>
						<br/>
						<table align="center" style="text-align:left;" border="0">
							<tr>
								<td>
									<b>Project Name</b> &nbsp &nbsp &nbsp
									<input type="text" name="projectName" style="width:600px"/>
									<img border="0" src="images/questionmark.jpg" title="Name of the data collection" width="15" height="15"/>
									<!--<img border="0" src="images/requiredfield.gif" title="Name of the data collection (Required field)" width="10" height="10"/>-->
									<br/><br/><br/><br/>
								</td>
							</tr>
							<tr>
								<td>
									<!--<b>Description</b> &nbsp &nbsp &nbsp 
									<textarea name="projectdescription" style="heigth:30px;width:610px" title="Description of the data collection" align="ABSBOTTOM"></textarea>
									<img border="0" src="images/questionmark.jpg" title="Description of the data collection" width="15" height="15"/>-->
									<p class="textandtextarea">
										<label for="textarea"><b>Description</b></label> &nbsp &nbsp &nbsp
										<textarea id="projectdescription" style="width:610px;heigth:50px"></textarea>
										<img border="0" src="images/questionmark.jpg" title="Description of the Data Collection" width="15" height="15"/>
									</p>
									<br/><br/><br/>
								</td>
							</tr>							
						</table>

						<!--<table align="center" style="text-align:left;" border="0">
							<tr>
							<td>
							<b>Project Name</b>
							<input type="text" name="projectName" style="width:150px"/>
							<img border="0" src="images/requiredfield.gif" title="Name of the data collection (Required field)" width="10" height="10"/>
							&nbsp &nbsp &nbsp &nbsp &nbsp
							<br/><br/><br/><br/>
							</td>
							<td>
							</tr>
						
						</table>-->
						
						<input type="submit" name="submit" value="Submit"/>
						&nbsp &nbsp &nbsp
						<input type="reset" value="Reset">
						<input type="hidden" name="username" value="<%=username%>" />
						<input type="hidden" name="role" value="<%=session.getAttribute("role").toString()%>" />
						<!--<input type="hidden" name="username" value="javascript:getUserName(this.form);" />-->
					</form>
					<br/>
				</td>
			</tr>
		</table>
		<%@ include file="bottompage.jsp" %>
	</body>
</html>
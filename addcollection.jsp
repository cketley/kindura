<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="java.sql.*, java.util.*, java.net.*, java.io.*" %>
<%@ page import="com.yourmediashelf.fedora.client.*, com.yourmediashelf.fedora.client.request.*, com.yourmediashelf.fedora.client.response.*, com.yourmediashelf.fedora.generated.access.*" %>

<%@ page errorPage="login.jsp" %>

<jsp:useBean id="fedoraServiceManager" class="org.kindura.FedoraServiceManager" scope="session"/>
	
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
					&nbsp &nbsp<a href="addproject.jsp" class="LinkToPage">Add project</a><br/>
					&nbsp &nbsp<b>Add collection</b><br/>
					&nbsp &nbsp<a href="search.jsp" class="LinkToPage">Search</a><br/>
					&nbsp &nbsp<a href="viewproject.jsp" class="LinkToPage">View</a><br/>
					&nbsp &nbsp<a href="help.jsp" class="LinkToPage">Help</a></br/></br/></br/>
					&nbsp &nbsp<a href="logout.jsp" class="LinkToPage">Log Out</a>
				</td>
				
				<td style="background-color:#FFFFFF;height:550px;width:700px;text-align:center;">
					<br/>
					<form name="metadata" method="post">
						<h1 style="color:#F77A52;font-size:18px">Collection metadata:</h1>
						<table align="center" style="text-align:left;" border="0" cellspacing="10">
							<tr>
								<td>
									<b>Collection Title</b>&nbsp &nbsp
									<input type="text" name="collectionName" title="Name of the Data Collection" style="width:600px"/>
									<!--<img border="0" src="images/requiredfield.gif" title="The name of the collection(required filed)" width="9" height="9"/>-->
									<img border="0" src="images/questionmark.jpg" title="The name of the collection" width="15" height="15"/>
									<br/><br/><br/>
								</td>
							</tr>
							<tr>
								<td>
									<b>Associated Project</b>&nbsp &nbsp
									<select name="projectName" style="width:575px">
										<%
											//List<String> projectNames = fedoraServiceManager.getUserDefinedPIDs(username);
											List<String> projectNames = fedoraServiceManager.getprojectNames();
											if (projectNames != null) {
												Iterator<String> iterator = projectNames.iterator();
												out.println("<option></option>");
												String projectName = "";
												while (iterator.hasNext()) {
													projectName = iterator.next();
													out.println("<option value="+projectName+">"+projectName+"</option>");
												}
											} else {
												response.sendRedirect("noprojectexists.jsp");
											}
										%>
									</select>
									<img border="0" src="images/questionmark.jpg" title="Name of the project" width="15" height="15"/>
									<br/><br/><br/>
								</td>
							</tr>
							<tr>
								<td>
									<!--<b>Description</b>&nbsp &nbsp
									<textarea name="collectionDescription" style="width:620px;heigth:50px"></textarea>
									<img border="0" src="images/questionmark.jpg" title="Description of the Data Collection" width="15" height="15"/>-->
									<p class="textandtextarea">
										<label for="textarea"><b>Description</b></label> &nbsp &nbsp
										<textarea name = "collectionDescription" id="collectionDescription" style="width:620px;heigth:50px"></textarea>
										<img border="0" src="images/questionmark.jpg" title="Description of the Data Collection" width="15" height="15"/>
									</p>
									<br/>
								</td>
							</tr>
						</table>
						<table align="center" style="text-align:left;" border="0" cellspacing="10">
							<tr>
								<td>
									<b>Version</b>
									<select name="version" style="width:200px">
										<option></option>
										<option value = "Source data">Source data</option>
										<option value = "Intermediate version">Intermediate version</option>
										<option value = "Milestone version">Milestone version</option>
										<option value = "Reformatted content">Reformatted content</option>
										<option value = "Final version">Final version</option>
									</select>
									<img border="0" src="images/questionmark.jpg" title="Version of the data collection" width="15" height="15"/>
									&nbsp &nbsp &nbsp &nbsp &nbsp
									<br/><br/><br/>
								</td>
								<td>
									<b>Estimated Access Frequency</b>
									<select name="accessFrequency" style="width:190px">
										<option></option>
										<option value = "10+ accesses per day">10+ accesses per day</option>
										<option value = "1-10 accesses per day">1-10 accesses per day</option>
										<option value = "1-10 accesses per week">1-10 accesses per week</option>
										<option value = "1-10 accesses per month">1-10 accesses per month</option>
										<option value = "Infrequent">Infrequent</option>
									</select>
									<img border="0" src="images/questionmark.jpg" title="Estimated access frequency" width="15" height="15"/>
									<br/><br/><br/>
								</td>
							</tr>
						</table>
						<table align="center" style="text-align:left;" border="0" cellspacing="10">
							<tr>
								<td>
									<b>Protective Marking</b> 
									<select name="protectiveMarking" style="width:120px">
										<option></option>
										<option value = "Confidential">Confidential</option>
										<option value = "Internal">Internal</option>
										<option value = "Public">Public</option>
									</select>
									<img border="0" src="images/questionmark.jpg" title="Protective marking" width="15" height="15"/>
									&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp
									<br/><br/><br/>
								</td>
								<td>
									<b>Contains Personal Data?</b>
									<img border="0" src="images/questionmark.jpg" title="Does the data collection contains personal data?" width="15" height="15"/>
									&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp
									<br/>
									<input type="radio" name="personaldata" value="false" checked="yes">No
									<br/>
									<input type="radio" name="personaldata" value="true" />Yes	
								</td>
								<td>
									<b>Image Handling?</b>
									<img border="0" src="images/questionmark.jpg" title="Convert image from one format to another" width="15" height="15"/>
									<br/>
									<input type="checkbox" name="bmptojpg" value="true" />bmp to jpeg
									<br/>
									<input type="checkbox" name="tifftojpg" value="true" />tiff to jpeg
								</td>
							</tr>
						</table>

						<input type="hidden" name="username" value="<%=username%>" />
						<input type="hidden" name="timestamp" value ="<%=new Timestamp(new java.util.Date().getTime())%>" />
						<!--<input type="hidden" name="username" value="javascript:getUserName(this.form);" />-->
					</form>
					<b>
						<h1 style="font-family:arial;color:#F77A52;font-size:18px;text-align:center;">
							Select files to add to collection:		
						</h1>
						<!--<font size=4 face="verdana" color=#F77A52>
							Select files to add to collection
						</font>-->
					</b>
					<APPLET
						CODEBASE = "plugins"
						CODE="wjhk.jupload2.JUploadApplet"
						NAME="JUpload"
						ARCHIVE="wjhk.jupload.jar"
						WIDTH="750"
						HEIGHT="250"
						MAYSCRIPT="true"
						ALT="The java plugin must be installed.">
						<param name="postURL" value="UploadRequestHandler" />
						<param name="showLogWindow" value="false" />
						<param name="showStatusBar" value="true" />
						<param name="lookAndFeel" value="system" />
						<param name="formdata" value="metadata" />
						<param name="nbFilesPerRequest" value="0" />
						<param name="httpUploadParameterName" value="file" />
						<param name="httpUploadParameterType" value="array" />
						<param name="debugLevel" value="95" />
						<!--<param name="afterUploadURL" value="parseRequest.jsp" />
						<param name="URLParam" value="URL Parameter Value"/>-->
						<!--<param name="formdata" value="true" />-->
						
						<!-- parseRequest.jsp?URLParam=URL Parameter Value-->
						<!-- Optionnal, see code comments -->
						Java 1.5 or higher plugin required. 
					</APPLET>
				</td>
			</tr>
		</table>
		<p style="font-family:arial;color:#800517;font-size:12px;text-align:center;">
			Kindura Project 2011		
		</p>
	</body>
</html>
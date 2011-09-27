<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
	//Get the username and set the relevant attribute.
	String username = null;
	if (session.getAttribute("username") != null) {
	    // Valid login
		username = session.getAttribute("username").toString();
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
		<table width="100%" border="0">
			<div id="header">
				<h1>Kindura Cloud Repository</h1>
			</div>
			<tr valign="top">
				<td style="background-color:#F0F0F0;width:70px;text-align:center;">
					</br>
					<a href="home.jsp">Home</a></br>
					<b>Upload</b></br>
					<a href="search.jsp">Search</a></br>
					<a href="help.jsp">Help</a></br></br></br>
					<a href="logout.jsp">Log Out</a>
				</td>
				
				<td style="background-color:#FFFFFF;height:550px;width:700px;text-align:center;">
					<form name="metadata" method="post">
						<br/>
						Collection name <input type="text" name="collectionname" title="Name of the Data Collection"/>
						<img border="0" src="images/requiredfield.gif" title="Required field" width="9" height="9"/>
						&nbsp &nbsp &nbsp
						
						Research funder
						<select name="researchfunder">
							<option>AHRC</option>
							<option>EPSRC</option>
							<option>ESRC</option>
							<option>MRC</option>
							<option>NERC</option>
							<option>Unfunded</option>
							<option>Other funding</option>
						</select>
						&nbsp &nbsp &nbsp
						
						Ownership
						<select name="owner">
							<option>Licensed(Public)</option>
							<option>Licensed(Restricted)</option>
							<option>Unlicensed</option>
						</select>
						<img border="0" src="images/questionmark.jpg" title="" width="15" height="15"/>
						
						<br/><br/><br/><br/>
						
						Project
						<select name="project">
							<!--<option selected="selected"></option>-->
							<option>Licensed(Public)</option>
							<option>Licensed(Restricted)</option>
							<option>Unlicensed</option>
						</select>
						<img border="0" src="images/questionmark.jpg" title="" width="15" height="15"/>
						&nbsp &nbsp &nbsp
						
						Protective marking
						<select name="proectivemarking">
							<option selected="selected">Confidential</option>
							<option>Internal</option>
							<option>Public</option>
						</select>
						<img border="0" src="images/questionmark.jpg" title="Protective marking" width="15" height="15"/>
						&nbsp &nbsp &nbsp
						
						Access requirement
						<select name="accessrequirement"> 
							<option selected="selected">100+ accesses per day</option>
							<option>10 - 100 accesses per day</option>
							<option>10 - 100 accesses per month</option>
							<option>Infrequent</option>
						</select>
						<img border="0" src="images/questionmark.jpg" title="Protective marking" width="15" height="15"/>
						
						<br/><br/><br/><br/>
						
						Description <textarea name="description" rows="3" cols="20" title="Description of the data collection"></textarea>
						<img border="0" src="images/questionmark.jpg" title="Description of the Data Collection" width="15" height="15"/>
						&nbsp &nbsp &nbsp
						
						Collection type
						<select name="collectiontype">
							<option selected="selected">Pre-publication results</option>
							<option>Published results</option>
							<option>Source dataset</option>
							<option>Working dataset</option>
						</select>
						<img border="0" src="images/questionmark.jpg" title="Protective marking" width="15" height="15"/>
						&nbsp &nbsp &nbsp
						<br/><br/><br/><br/>
						
						Format conversion
						<input type="checkbox" name="formatconversion" value="formatconversion"/>Documents Words to PDF<br/>
						&nbsp &nbsp &nbsp &nbsp &nbsp
						<input type="checkbox" name="tifftojpeg" value="tifftojpeg"/>TIFF to JPEG<br/>
						&nbsp &nbsp &nbsp &nbsp &nbsp
						<input type="checkbox" name="tifftopng" value="tifftopng"/>TIFF to PNG<br/>
						<br/>
						<input type="hidden" name="username" value="<%=username%>" />
						<!--<input type="hidden" name="username" value="javascript:getUserName(this.form);" />-->
					</form>
					<br/>
					<b>
						<font size=4 face="verdana" color=FF0000>
							Select files to add to collection
						</font>
					</b></br></br>
					<APPLET
						CODE="wjhk.jupload2.JUploadApplet"
						NAME="JUpload"
						ARCHIVE="wjhk.jupload.jar"
						WIDTH="700"
						HEIGHT="300"
						MAYSCRIPT="true"
						ALT="The java plugin must be installed.">
						<param name="postURL" value="UploadRequestHandler" />
						<param name="showLogWindow" value="false" />
						<param name="showStatusBar" value="false" />
						<param name="lookAndFeel" value="system" />
						<param name="formdata" value="metadata" />
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
		<p style="font-family:arial;color:#800517;font-size:15px;text-align:center;">
			Kindura Project 2011		
		</p>
	</body>
</html>
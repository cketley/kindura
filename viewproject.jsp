<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="java.sql.*, java.util.*, java.net.*, java.io.*" %>
<%@ page import="com.yourmediashelf.fedora.client.*, com.yourmediashelf.fedora.client.request.*, com.yourmediashelf.fedora.client.response.*, com.yourmediashelf.fedora.generated.access.*" %>



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
		<script type="text/javascript">
			function validateSelection() {
				var projectName=document.forms["projectSelection"]["projectName"].value;
				if (projectName==null || projectName=="") {
					alert("Please select a project from the drop-down list.");
					return false;
				}
			}
		</script>
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
					&nbsp &nbsp<b>View</b><br/>
					&nbsp &nbsp<a href="help.jsp" class="LinkToPage">Help</a></br/></br/></br/>
					&nbsp &nbsp<a href="logout.jsp" class="LinkToPage">Log Out</a>
				</td>
				<td style="height:550px;width:700px;text-align:center;">
					<div id="search">
						<form name="projectSelection" action="ViewProjectRequestHandler" onsubmit="return validateSelection()" method="get">
							<label><b>Select Project</b></label>
							<select name= 'projectName' style='width:150px'>
							<option></option>
							<%
								//out.println("<select name= 'projectName' style='width:150px'>");
								//out.println("<option></option>");
								//List<String> projectNames = fedoraServiceManager.getUserDefinedPIDs(username);
								List<String> projectNames = fedoraServiceManager.getprojectNames();
								if (projectNames != null) {
									Iterator<String> iterator = projectNames.iterator();
									String projectName = "";
									while (iterator.hasNext()) {
										projectName = iterator.next();
										out.println("<option value="+projectName+">"+projectName+"</option>");
									}
								}
							%>
							</select>
							<input type="submit" value="View" id="view_button"/>
							<input type="hidden" name="nameSpace" value="root" />
							<input type="hidden" name="parentFolderNameForDuracloud" value="" />
							<input type="hidden" name="parentFolderNameForFedora" value="" />
							<input type="hidden" name="requestType" value="view" />
						</form>
					</div>
				</td>
			</tr>
		</table>
		<%@ include file="bottompage.jsp" %>
	</body>
</html>
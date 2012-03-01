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
		<script src="js/sorttable.js"></script>
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
						<form action="ViewProjectRequestHandler" method="get">
							<a href="javascript:history.back(-1)" class="LinkToPage">
							<!--<img border="0" src="images/parentfolder.png" title="Go back to parent folder" alt="" width="13" height="13" align="absmiddle" background-color="black"/>--><< Go Back
							</a>&nbsp &nbsp &nbsp &nbsp &nbsp
							<label><b>Select Project</b></label>
							<%
								out.println("<select name= 'projectName' style='width:150px'>");
								//List<String> projectNames = fedoraServiceManager.getUserDefinedPIDs(username);
								List<String> projectNames = fedoraServiceManager.getprojectNames();
								if (projectNames != null) {
									Iterator<String> iterator = projectNames.iterator();
									System.out.println(request.getAttribute("projectName"));
									if (request.getAttribute("projectName") != null) {
										out.println("<option>"+request.getAttribute("projectName")+"</option>");
									}
									String nextprojectName = null;
									while (iterator.hasNext()) {
										nextprojectName = (String)iterator.next();
										if (!nextprojectName.equals(request.getAttribute("projectName"))) {
											out.println("<option>"+nextprojectName+"</option>");
										}
									}
								}
							%>
							</select>
							<input type="hidden" name="nameSpace" value="root" />
							<input type="hidden" name="parentFolderNameForDuracloud" value="" />
							<input type="hidden" name="parentFolderNameForFedora" value="" />
							<input type="hidden" name="requestType" value="view" />
							<input type="submit" value="View" id="view_button"/>	
							
						</form>
					</div>
		
					<form method="post" name="download_form" action="DownloadRequestHandler">
						<table class="sortable" id="list" style="text-align:left">	
							<tr>
								<% 	
									int numberOfRows = Integer.valueOf(request.getAttribute("numberOfRows").toString());
									if (numberOfRows > 0) {
										if (request.getAttribute("pid0fedoraObjectType").equals("collection")) {
											out.println("<th>Title</th>");
										} else {
											out.println("<th>Name</th>");
										}
										out.println("<th>Size(Bytes)</th>");
										out.println("<th>Type</th>");
										for (int i=0;i< numberOfRows;i++) {
											if (i % 2 != 0) {
												out.println("<tr style= 'background-color:#F8F8F8;'>");
											}
											else {
												out.println("<tr style= 'background-color:#FFFFFF;'>");
											}
											if (request.getAttribute("pid"+i+"fedoraObjectType").equals("file")) {
												//out.println("<td><a href=DownloadRequestHandler?namespaceandpid="+request.getAttribute("nameSpace"+i)+":"+request.getAttribute("parentFolderNameForDuracloud"+i)+"/"+request.getAttribute("baseFileName"+i)+"&"+request.getAttribute("nameSpace"+i)+request.getAttribute("projectName")+"/"+request.getAttribute("parentFolderNameForDuracloud"+i)+"/"+request.getAttribute("baseFileName"+i)+"="+request.getAttribute("fileExtension"+i)+">"+request.getAttribute("alphaNumericName"+i)+"</a></td>");
												out.println("<td><a href=DownloadRequestHandler?namespaceandpid="+request.getAttribute("collectionName"+i)+":"+request.getAttribute("parentFolderNameForDuracloud"+i)+"/"+request.getAttribute("baseFileName"+i)+"&"+request.getAttribute("collectionName"+i)+request.getAttribute("parentFolderNameForDuracloud"+i)+"/"+request.getAttribute("baseFileName"+i)+"="+request.getAttribute("fileExtension"+i)+">"+request.getAttribute("alphaNumericName"+i)+"</a></td>");
											} else {
												out.println("<td><a href=ViewProjectRequestHandler?nameSpace="+request.getAttribute("nameSpace"+i)+"&projectName="+request.getAttribute("projectName")+"&alphaNumericName="+request.getAttribute("alphaNumericName"+i)+"&requestType=view>"+request.getAttribute("alphaNumericName"+i)+"</a></td>");
											}
											out.println("<td>"+request.getAttribute("fileSize"+i)+"</td>");
											out.println("<td>"+request.getAttribute("pid"+i+"fedoraObjectType")+"</td>");
								%>
										</tr>
								<% 		
										}
									} 
								%>
						</table>
					</form>
				</td>
			</tr>
		</table>
		<%@ include file="bottompage.jsp" %>
	</body>
</html>
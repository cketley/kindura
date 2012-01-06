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
					&nbsp &nbsp<b>Search</b><br/>
					&nbsp &nbsp<a href="viewproject.jsp" class="LinkToPage">View</a><br/>
					&nbsp &nbsp<a href="help.jsp" class="LinkToPage">Help</a></br/></br/></br/>
					&nbsp &nbsp<a href="logout.jsp" class="LinkToPage">Log Out</a>
				</td>
				<td style="height:550px;width:700px;text-align:center;">
					<div id="search">
						<form action="SearchRequestHandler" method="get">
							<a href="javascript:history.back(-1)" class="LinkToPage">
							<!--<img border="0" src="images/parentfolder.png" title="Go back to parent folder" alt="" width="13" height="13" align="absmiddle" background-color="black"/>--><< Go Back
							</a>&nbsp &nbsp &nbsp &nbsp &nbsp
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
							<!--<input type="submit" value="More..." id="resume_search_button" disabled="disabled" />				
							<button class="prev_page" value=">" disabled="disabled">&lt;</button>
							<button class="next_page" value="<" disabled="disabled">&gt;</button>			
							<label for="per_page">Results Per Page</label>                
							<select name="per_page" id="per_page">
								<option value="10">10</option>
								<option value="25">25</option>
								<option value="50">50</option>
								<option value="100">100</option>
							</select>-->       
							<input type="hidden" name="username" value="<%=username%>" />							
						</form>
					</div>
		
					<form method="post" name="download_form" action="DownloadRequestHandler">
						<table class="sortable" id="list" style="text-align:left">	
							<tr>
								<%
									if (request.getAttribute("parentFolderNameForDuracloud0") == "") {
										out.println("<th>Title</th>");
									}
									else {
										out.println("<th>Name</th>");
									}
								%>
								<th>Size(GB)</th>
								<th>Type</th>
								<% 	
									int dataStreamCount = Integer.valueOf(request.getAttribute("dataStreamCount").toString());
									if (dataStreamCount > 0) {
										for (int i=0;i< dataStreamCount;i++) {
											if (i % 2 != 0) {
												out.println("<tr style= 'background-color:#F8F8F8;'>");
											}
											else {
												out.println("<tr style= 'background-color:#FFFFFF;'>");
											}
											//if (request.getAttribute("file"+i) == "") {
											if (request.getAttribute("fileName"+i) == "") {
												out.println("<td><a href=SearchFileRequestHandler?nameSpace="+request.getAttribute("nameSpace"+i)+"&pid="+request.getAttribute("pid"+i)+"&parentFolderNameForDuracloud="+request.getAttribute("fileName"+i)+"&parentFolderNameForFedora="+request.getAttribute("fileName"+i)+"&collectionName="+request.getAttribute("object"+i+"pid")+request.getAttribute("fileName"+i)+">"+request.getAttribute("fileName"+i)+"</a></td>");
											}
											else {
												if (request.getAttribute("fileType"+i) == "File") {
													out.println("<td>"+request.getAttribute("fileName"+i)+"</td>");
												} else if (request.getAttribute("parentFolderNameForDuracloud"+i) == "") {
													out.println("<td><a href=SearchFileRequestHandler?nameSpace="+request.getAttribute("nameSpace"+i)+"&pid="+request.getAttribute("pid"+i)+"&parentFolderNameForDuracloud="+request.getAttribute("fileName"+i)+"&parentFolderNameForFedora="+request.getAttribute("fileName"+i)+"&collectionName="+request.getAttribute("object"+i+"pid")+request.getAttribute("fileName"+i)+">"+request.getAttribute("fileName"+i)+"</a></td>");
												} else {
													out.println("<td><a href=SearchFileRequestHandler?nameSpace="+request.getAttribute("nameSpace"+i)+"&pid="+request.getAttribute("pid"+i)+"&parentFolderNameForDuracloud="+request.getAttribute("parentFolderNameForDuracloud"+i)+"/"+request.getAttribute("fileName"+i)+"&parentFolderNameForFedora="+request.getAttribute("parentFolderNameForFedora"+i)+"_"+request.getAttribute("fileName"+i)+"&collectionName="+request.getAttribute("object"+i+"pid")+request.getAttribute("fileName"+i)+">"+request.getAttribute("fileName"+i)+"</a></td>");
												}
											}
								%>
											<td><%=request.getAttribute("fileSize"+i)%></td>
											<td><%=request.getAttribute("fileType"+i)%></td>
								<%
											/*if (request.getAttribute("fileType"+i) == "File") {
												out.println("<td><a href=DownloadRequestHandler?namespaceandpid="+request.getAttribute("nameSpace"+i)+":"+request.getAttribute("projectName")+"/"+request.getAttribute("parentFolderNameForDuracloud"+i)+"/"+request.getAttribute("fileName"+i)+"&"+request.getAttribute("nameSpace"+i)+request.getAttribute("projectName")+"/"+request.getAttribute("parentFolderNameForDuracloud"+i)+"/"+request.getAttribute("fileName"+i)+"="+request.getAttribute("fileExtension"+i)+">Link</a></td>");
											} else {
												out.println("<td></td>");
											}*/
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
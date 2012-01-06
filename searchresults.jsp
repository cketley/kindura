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
							<label><b>Search Collection</b></label>
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
							<thead>
							<tr>
								<th>Collection</th>
								<th>Creator</th>
								<th>Associated Project</th>
								<th>Description</th>
								<th>Date Uploaded</th>
								<th>Storage Type</th>
								<th>Total Cost(£)</th>
								<th>Action Required</th>
							</tr>
							</thead>
							<tbody>
								<% 	
								if (request.getAttribute("pidCount") != null) {
									int pidCount = Integer.valueOf(request.getAttribute("pidCount").toString());
									if (pidCount > 0) {
										for (int i=0;i< pidCount;i++) {
											if (i % 2 != 0) {
												out.println("<tr style= 'background-color:#F8F8F8;'>");
											}
											else {
												out.println("<tr style= 'background-color:#FFFFFF;'>");
											}
											out.println("<td><a href=ViewProjectRequestHandler?nameSpace="+request.getAttribute("nameSpace"+i)+"&projectName="+request.getAttribute("object"+i+"projectName")+"&alphaNumericName="+request.getAttribute("alphaNumericName"+i)+"&requestType=search>"+request.getAttribute("displayName"+i)+"</a></td>");
								%>
							<!--<tr>-->
									
									
									<!--<td><a href="SearchFileRequestHandler?nameSpace=<%=request.getAttribute("nameSpace"+i)%>&projectName=<%=request.getAttribute("object"+i+"projectName")%>&pid=<%=request.getAttribute("pid"+i)%>&parentFolderNameForDuracloud=<%=request.getAttribute("parentFolderNameForDuracloud"+i)%>&parentFolderNameForFedora=<%=request.getAttribute("parentFolderNameForFedora"+i)%>&collectionName=<%=request.getAttribute("object"+i+"pid")%>"><%=request.getAttribute("object"+i+"pid")%></a></td>-->
									<td><%=request.getAttribute("object"+i+"creator")%></td>
									
									<td onclick="displayProjectInformation()"><%=request.getAttribute("object"+i+"projectName")%><img border="0" src="images/information.png" title="Start date: <%=request.getAttribute("object"+i+"startDate")%>   End date: <%=request.getAttribute("object"+i+"endDate")%>   Project contact: <%=request.getAttribute("object"+i+"projectContact")%>" width="16" height="16" align="right"/></td>
									
									<td><%=request.getAttribute("object"+i+"description")%></td>
									<td><%=request.getAttribute("object"+i+"cDate")%></td>
									<td><%=request.getAttribute("object"+i+"storageType")%></td>
									<td><%=request.getAttribute("object"+i+"collectionCost")%></td>
									<td><%=request.getAttribute("object"+i+"actionRequired")%></td>
								</tr>
								<% 
								
										}
									} 
								}
								%>
							</tbody>
						</table>
						
					</form>
				</td>
			</tr>
		</table>
		<%@ include file="bottompage.jsp" %>
	</body>
</html>
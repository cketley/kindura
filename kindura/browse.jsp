<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
	if (session.getAttribute("username") != null) {
	    // Valid login
		String username = session.getAttribute("username").toString();
        session.setAttribute("username", username);
	} else {
        // Invalid login
        response.sendRedirect("loginerror.jsp");
	}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Kindura Cloud Repository</title>
	<link href="<%= request.getContextPath() %>/css/r4r.css" type="text/css" rel="stylesheet" media="screen" />
	<link href="<%= request.getContextPath() %>/css/custom-theme/jquery-ui-1.8.12.custom.css" type="text/css" rel="stylesheet" media="screen" />
	<script type="application/javascript" src="<%= request.getContextPath() %>/js/jquery-1.5.2.min.js"></script>
	<script type="application/javascript" src="<%= request.getContextPath() %>/js/jquery-ui-1.8.12.custom.min.js"></script>
	<script type="application/javascript" src="<%= request.getContextPath() %>/js/jquery.tmpl.min.js"></script>
	<script type="application/javascript" src="<%= request.getContextPath() %>/js/jquery.cookie.js"></script>
	<script type="application/javascript" src="<%= request.getContextPath() %>/js/demo.js"></script>
	<script id="row-template" type="text/x-jquery-tmpl">
	<tr class="search_item \${oddeven}" id="item_\${pid}" page-data="\${page}">
		<td>
			<input type="checkbox" class="export_pid" name="pid" value="\${pid}" id="pid_\${pid}">
		</td>
		<td class="title">
			<label for="pid_\${pid}">\${title}</label>
		</td>
		<td class="creator">\${creator}</td>
		<td class="date">\${date}</td>
		<td><a target="_blank" href="http://localhost:8080/fedora/get/\${pid}" title="View Fedora Info">fedora info</a></td>
	</tr>-->	
	</script>
</head>
<body>
	<div id="header">
		<h1>Kindura Cloud Repository</h1>
	</div>
    
	<table width="100%" border="0">
			<tr valign="top">
				<!--<td style="background-color:#F0F0F0;width:70px;text-align:top;">-->
				<td style="background-color:#F0F0F0;width:70px;text-align:center;">
						</br><a href="home.jsp">Home</a></br>
						<a href="upload.jsp">Upload</a></br>
						<b>Search</b></br>
						<a href="help.jsp">Help</a></br></br></br>
						<a href="logoff.jsp">Log off</a>
				</td>
				<td style="height:550px;width:700px;text-align:center;">
					<div id="search">
        <!--<form action="results.jsp" method="get" id="search_form">-->
		<form action="searchresults2.jsp" method="post">
            <div class="form_field">
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
				<!--<input type="submit"  value="Search" id="submit_search"/>-->
				<input type="submit" value="Search" id="search_button"/>			  
                <!--<input type="submit" id="resume_search" value="More..."  disabled="disabled" />	-->
				<input type="submit" value="More..." id="resume_search_button" disabled="disabled" />				
                <button class="prev_page" value=">" disabled="disabled">&lt;</button>
                <button class="next_page" value="<" disabled="disabled">&gt;</button>			
                <label for="per_page">Results Per Page</label>                
                <select name="per_page" id="per_page">
                    <option value="10">10</option>
                    <option value="25">25</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                </select>                
                <img class="spinner hidden" id="searching" src="<%= request.getContextPath() %>/images/searching.gif" />
            </div>
        </form>

    </div>
		<form method="get" action="/r4r/Cerif4Ref" target="_blank" id="export_form">
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

			<div id="export">
				<br><input type="hidden" id="submit_export" value="Download" disabled="disabled" /></br>
				<!--<input type="hidden" id="zip" name="zip" value="false" />-->
				<div class="form_field right">
					<!--<input type="checkbox" id="checkzip" value="zip" />
					<label for="checkzip">Zip Output</label>-->
				</div>
				<!--<div>Items selected: <span id="selected_count">0</span></div>-->
				<span class="clear"></span>
			</div>
		</form>
	</td>
			</tr>

			<tr>
				<td colspan="2" style="background-color:#FFFFFF;text-align:center;">
					<font size=2 face="arial" color=#700000>
						Kindura Project 2011
					</font>
				</td>
				
			</tr>
		<!-- </table> -->
</body>
</html>
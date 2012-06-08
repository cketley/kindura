
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	//Get the username and set the relevant attribute.
	if (session.getAttribute("username") != null) {
	    // Valid login
		session.removeAttribute("username");
		session.removeAttribute("role");
		session.invalidate();
	} else {
        // Invalid login
        response.sendRedirect("sessiontimeout.jsp");
	}
%>

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Kindura Cloud Repository</title>
		<link href="<%= request.getContextPath() %>/css/r4r.css" type="text/css" rel="stylesheet" media="screen"/>
	</head>
	<!--<body>
		<table width="100%" border="0">
		<tr valign="top">
			<div id="header">
				<h1>Kindura Cloud Repository</h1>
			</div>
			<td style="background-color:#FFFFFF;width:0px;text-align:center;">
			</td>
				
			<td style="background-color:#FFFFFF;height:550px;width:700px;text-align:center;">
				</br></br></br>
				<b>You are now logged out of Kindura Cloud Repository.</b>
				</br></br></br>
				<a href="login.jsp">Log In</a>
			</td>
		</table>
		<%@ include file="bottompage.jsp" %>
	</body>-->
	<body>
		<div id="header">
			<h1>Kindura Cloud Repository</h1>
		</div>
		
		<br/><br/><br/>
		
		<p style="text-align:center;"><b>You are now logged out of Kindura Cloud Repository.</b>
			</br></br></br>
			<a href="login.jsp" style="text-align:center;">Log In</a>
		</p>
		<br/><br/><br/>
		<%@ include file="bottompage.jsp" %>
	</body>
</html>


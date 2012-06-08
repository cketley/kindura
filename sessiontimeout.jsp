<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Kindura Cloud Repository</title>
		<link href="<%= request.getContextPath() %>/css/r4r.css" type="text/css" rel="stylesheet" media="screen"/>
	</head>
	<body>
		<div id="header">
			<h1>Kindura Cloud Repository</h1>
		</div>
		
		<form action="LoginRequestHandler" method="post" style="text-align:center;"> 
			<br/>
			<font size=4 face="arial" color=FF0000>
				<br/>
				<b>Session Time Out. Please login again.</b>
				<br/><br/><br/>
			</font>
			User name <input type="text" name="username" />&nbsp &nbsp &nbsp &nbsp &nbsp
			Password <input type="password" name="password" /></br></br><br/>
			<input type="submit" value="Login">
		</form>
		
		<br/><br/><br/>
		<%@ include file="bottompage.jsp" %>
	</body>
</html>
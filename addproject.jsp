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
		<script>
		$(function() {
			var dates = $( "#startdate, #enddate" ).datepicker({
				minDate: 0,
				changeMonth: true,
				changeYear: true,
				defaultDate: "+1w",
				changeMonth: true,
				//showOn: "button",
				//buttonImage: "questionmark.jpg",
				onSelect: function( selectedDate ) {
					var option = this.id == "startdate" ? "minDate" : "maxDate",
						instance = $( this ).data( "datepicker" ),
						date = $.datepicker.parseDate(
							instance.settings.dateFormat ||
							$.datepicker._defaults.dateFormat,
							selectedDate, instance.settings );
					dates.not( this ).datepicker( "option", option, date );
				}
			});
		});
		
		</script>
		<script type="text/javascript">
			function validateForm() {
				var projectname = document.forms["projectmetadata"]["projectname"].value;
				var description = document.forms["projectmetadata"]["projectdescription"].value;
				var primaryfunder = document.forms["projectmetadata"]["primaryfunder"].value;
				var department = document.forms["projectmetadata"]["department"].value;
				var projectcontact = document.forms["projectmetadata"]["projectcontact"].value;
				var typeofdata = document.forms["projectmetadata"]["typeofdata"].value;
				var ownership = document.forms["projectmetadata"]["ownership"].value;
				var validformat=/^\d{2}\/\d{2}\/\d{4}$/; //Basic check for format validity
				
				if (projectname == null || projectname == "") {
					alert("Please fill in the 'Project name'.");
					return false;
				}
				if (description == "") {
					alert("Please fill in the 'Description' for the project.");
					return false;
				} 
				if (!validformat.test(document.forms["projectmetadata"]["startdate"].value)) {
					alert("Please select the 'Start date' of the storage period.");
					return false;
				} 
				if (!validformat.test(document.forms["projectmetadata"]["enddate"].value)) {
					alert("Please select the 'End date' of the storage period.");
					return false;
				} 
				if (department == "") {
					alert("Please select the 'Department'");
					return false;
				} 
				if (projectcontact == "") {
					alert("Please fill in the 'Project Contact'.");
					return false;
				}
				if (primaryfunder == "") {
					alert("Please select the 'Primary funder' or select the option of 'Other funding' and then fill in 'Other funding source' if the funder is not listed.");
					return false;
				} else if (primaryfunder == "Other funding")  {
					var otherfunder = document.forms["projectmetadata"]["otherfundingsource"].value;
					if (otherfunder =="") {
						alert("Please fill in 'Other funding source' as 'Other funding' is selected.");
						return false;
					}
				}  
				if (typeofdata == "") {
					alert("Please select the 'Type of Data'.");
					return false;
				} 
				if (ownership == "") {
					alert("Please select the 'Ownership'.");
					return false;
				}
				 
			}
			
			function handleOtherFundingField(){
				if (document.forms["projectmetadata"]["primaryfunder"].value != "Other funding") {
					document.getElementById("otherfundingsourcelabel").style.color = "grey";
					document.forms["projectmetadata"]["otherfundingsource"].disabled = true;
					document.forms["projectmetadata"]["otherfundingsource"].value = "";
				} else {
					document.getElementById("otherfundingsourcelabel").style.color = "black";
					document.forms["projectmetadata"]["otherfundingsource"].disabled = false;
				}
			}
		</script>
		
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
					<form name="projectmetadata" action="AddProjectRequestHandler" onsubmit="return validateForm()" method="post">
						<br/>
						<h1 style="color:#F77A52;font-size:18px">Add Project:</h1>
						<br/>
						<table align="center" style="text-align:left;" border="0">
							<tr>
								<td>
									<b>Project Name</b> &nbsp &nbsp &nbsp
									<input type="text" name="projectname" style="width:600px"/>
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
						<table align="center" style="text-align:left;" border="0">
							<tr>
								<td>
									<b>Start Date</b>&nbsp &nbsp
								
									<input type="text" id="startdate" name="startdate" style="width:200px"/>
									<img border="0" src="images/questionmark.jpg" title="Start date of the storage period" width="15" height="15"/>
									&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp
									<br/><br/>
									<b>End Date</b>&nbsp &nbsp
									<!--<label for="to">to</label>-->
									<input type="text" id="enddate" name="enddate" style="width:208px"/>
									<img border="0" src="images/questionmark.jpg" title="End date of the storage period" width="15" height="15"/>
									<br/><br/><br/><br/>
								</td>
								<td>
									<b>Department</b>&nbsp &nbsp
									<select id="department" name="department" style="width:200px">
										<option></option>
										<option value="Engineering">Engineering</option>
										<option value="Geography">Geography</option>
										<option value="History">History</option>
										<option value="Mathematics">Mathematics</option>
										<option value="Physics">Physics</option>
									</select>
									<img border="0" src="images/questionmark.jpg" title="Name of the department" width="15" height="15"/>
									<br/><br/>
									<b>Project Contact</b>&nbsp &nbsp
									<input type="text" name="projectcontact" title="Contact Email" style="width:168px"/>
									<img border="0" src="images/questionmark.jpg" title="Contact email address" width="15" height="15"/>
									<br/><br/><br/><br/>
								</td>
							</tr>
							
							<tr>
								<td>
									<b>Primary funder</b>
									<select name="primaryfunder" style="width:185px" onclick="javascript:handleOtherFundingField()" onkeyup="javascript:handleOtherFundingField()">
										<option></option>
										<option value="AHRC">AHRC</option>
										<option value="EC">EC</option>
										<option value="EPSRC">EPSRC</option>
										<option value="Institutional">Institutional</option>
										<option value="MRC">MRC</option>
										<option value="NERC">NERC</option>
										<option value="Unfunded">Unfunded</option>
										<option value="Other funding">Other funding</option>
										
									</select>
									<img border="0" src="images/questionmark.jpg" title="The name of the primary funder" width="15" height="15"/>
									&nbsp &nbsp &nbsp
									<br/><br/>
									<label name="otherfundingsourcelabel" id="otherfundingsourcelabel" style="color:grey"><b>Other funding source:</b></label> 
									<input type="text" name="otherfundingsource" style="width:128px" disabled="disabled"/>
									<img border="0" src="images/questionmark.jpg" title="The name of other funder if 'Other funding' is selected" width="15" height="15"/>
									&nbsp &nbsp &nbsp &nbsp &nbsp
									<br/><br/><br/><br/>
								</td>
								<td>
									<b>Type of data</b>&nbsp &nbsp
									<select name="typeofdata" style="width:198px">
										<option></option>
										<option value="Research">Research</option>
										<option value="Corporate">Corporate</option>
									</select>
									<img border="0" src="images/questionmark.jpg" title="Type of the data collection" width="15" height="15"/>
									<br/><br/>
									<b>Ownership</b>
									<select name="ownership" style="width:220px">
										<option></option>
										<option value="Licensed(Public)">Licensed(Public)</option>
										<option value="Licensed(Restricted)">Licensed(Restricted)</option>
										<option value="Unlicensed">Unlicensed</option>
									</select>
									<img border="0" src="images/questionmark.jpg" title="Owner of the data collection" width="15" height="15"/>
									<br/><br/><br/><br/>
								</td>
							</tr>
						</table>
						<!--<table align="center" style="text-align:left;" border="0">
							<tr>
							<td>
							<b>Project Name</b>
							<input type="text" name="projectname" style="width:150px"/>
							<img border="0" src="images/requiredfield.gif" title="Name of the data collection (Required field)" width="10" height="10"/>
							&nbsp &nbsp &nbsp &nbsp &nbsp
							<br/><br/><br/><br/>
							</td>
							<td>
							<b>Primary funder</b>
							<select name="primaryfunder" style="width:150px">
								<option></option>
								<option onmousedown="javascript:disableOtherFundingField()">AHRC</option>
								<option onmousedown="javascript:disableOtherFundingField()">EC</option>
								<option onmousedown="javascript:disableOtherFundingField()">EPSRC</option>
								<option onmousedown="javascript:disableOtherFundingField()">Institutional</option>
								<option onmousedown="javascript:disableOtherFundingField()">MRC</option>
								<option onmousedown="javascript:disableOtherFundingField()">NERC</option>
								<option onmousedown="javascript:disableOtherFundingField()">Unfunded</option>
								<option onmousedown="javascript:enableOtherFundingField()">Other funding</option>
							</select>
							<img border="0" src="images/questionmark.jpg" title="The name of the primary funder" width="15" height="15"/>
							&nbsp &nbsp &nbsp
							<br/><br/><br/><br/>
							</td>
							<td>
							<label name="otherfundingsourcelabel" id="otherfundingsourcelabel" style="color:grey"><b>Other funding source:</b></label> 
							<input type="text" name="otherfundingsource" style="width:150px" disabled="disabled"/>
							<img border="0" src="images/questionmark.jpg" title="The name of other funder if 'Other funding' is selected" width="15" height="15"/>
							&nbsp &nbsp &nbsp &nbsp &nbsp
							<br/><br/><br/><br/>
							</td>
							</tr>
							
							<tr>
							<td>
							<b>Description</b>
							<textarea name="projectdescription" style="heigth:30px;width:163px" title="Description of the data collection"></textarea>
							<img border="0" src="images/questionmark.jpg" title="Description of the data collection" width="15" height="15"/>
							<br/><br/><br/><br/>
							</td>
							
							<td>
							<b>Ownership</b>
							<select name="ownership" style="width:178px">
								<option></option>
								<option>Licensed(Public)</option>
								<option>Licensed(Restricted)</option>
								<option>Unlicensed</option>
							</select>
							<img border="0" src="images/questionmark.jpg" title="Owner of the data collection" width="15" height="15"/>
							<br/><br/><br/><br/>
							</td>
							
							<td>
							<b>Project Contact</b>
							<input type="text" name="projectcontact" title="Contact Email" style="width:198px"/>
							<img border="0" src="images/questionmark.jpg" title="Contact email address" width="15" height="15"/>
							<br/><br/><br/><br/>
							</td>
							</tr>
							<tr>
							<td>
							<b>Department</b>
							<select name="department" style="width:165px">
								<option></option>
								<option>Engineering</option>
								<option>Geography</option>
								<option>History</option>
								<option>Mathematics</option>
								<option>Physics</option>
							</select>
							<img border="0" src="images/questionmark.jpg" title="Name of the department" width="15" height="15"/>
							<br/><br/><br/><br/>
							</td>
							<td>
							<b>Type of data</b>
							<select name="typeofdata" style="width:168px">
								<option></option>
								<option>Research</option>
								<option>Corporate</option>
							</select>
							<img border="0" src="images/questionmark.jpg" title="Type of the data collection" width="15" height="15"/>
							<br/><br/><br/><br/>
							</td>
							</tr>
							<tr>
							<td>
							<b>Start Date:</b>
							
							<input type="text" id="startdate" name="startdate" style="width:165px"/>
							<img border="0" src="images/questionmark.jpg" title="Start date of the storage period" width="15" height="15"/>
							<br/><br/><br/><br/>
							</td>
							<td>
							<b>End Date:</b>
							<input type="text" id="enddate" name="enddate" style="width:180px"/>
							<img border="0" src="images/questionmark.jpg" title="End date of the storage period" width="15" height="15"/>
							<br/><br/><br/><br/>
							</td>
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
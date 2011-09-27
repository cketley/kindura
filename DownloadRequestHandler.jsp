<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page language="java" import="java.io.*, java.sql.*, java.util.*, java.net.*" %>
<%@ page import="org.apache.commons.fileupload.*, org.apache.commons.fileupload.disk.*, org.apache.commons.fileupload.servlet.*, org.duracloud.client.*, org.duracloud.common.model.*, org.duracloud.error.*, org.duracloud.domain.*, com.yourmediashelf.fedora.client.*, com.yourmediashelf.fedora.client.request.*, com.yourmediashelf.fedora.client.response.*, com.yourmediashelf.fedora.generated.access.*, java.awt.Desktop" %>
<jsp:useBean id="durastoreclient" class="org.kindura.DuraStoreClient" scope="session"/>
<jsp:useBean id="fedoraservicemanager" class="org.kindura.FedoraServiceManager" scope="session"/>
<%
	//Get the username and set the relevant attribute.
	if (session.getAttribute("username") != null) {
	    // Valid login
		String username = session.getAttribute("username").toString();
        session.setAttribute("username", username);
	} else {
        // Invalid login
        response.sendRedirect("sessiontimeout.jsp");
	}
	
	String[] nameSpaceAndPid = request.getParameter("namespaceandpid").split(":");
	String fileformat = request.getParameter(nameSpaceAndPid[0]+nameSpaceAndPid[1]);
	
	System.out.println("start to download "+nameSpaceAndPid[0]+" "+nameSpaceAndPid[1]+" "+fileformat);
	
	if (fileformat.equals("null")) {
		response.sendRedirect("downloaderror.jsp");
	} else {
		//DuraCloud credential
		final String DURACLOUD_HOST = "localhost";
		final String DURACLOUD_PORT = "8080";
		final String DURACLOUD_CONTEXT = "durastore";
		final String DURACLOUD_USERNAME = "root";
		final String DURACLOUD_PASSWORD = "rpw";
		
		// Setup connection with DuraCloud.
		ContentStoreManager storeManager = new ContentStoreManagerImpl(DURACLOUD_HOST, DURACLOUD_PORT, DURACLOUD_CONTEXT);
		Credential duracloudCredential = new Credential(DURACLOUD_USERNAME, DURACLOUD_PASSWORD);
		storeManager.login(duracloudCredential);
		ContentStore store = storeManager.getPrimaryContentStore();
		
		String downloadTempDirectory = "C:\Program Files\Apache Software Foundation\Tomcat 6.0\webapps\kindura2\downloadtemp\";
		String downloadDirectory = "downloadtemp/";
		
		//Download the specified file from Cloud to local server and store it in a local temporary folder.
		durastoreclient.downloadFile(nameSpaceAndPid[0], nameSpaceAndPid[1], downloadTempDirectory, fileformat, 1024);
		
		//Redirect the link to the file stored in the local temporary folder.
		response.sendRedirect(downloadDirectory+nameSpaceAndPid[1]+"."+fileformat);
	}	
%>
<html>
	<body>
	</body>
</html>
package org.kindura;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.duracloud.error.ContentStoreException;

/**
 * This class is used to handle file download request.
 * @author Jun Zhang
 */
public class DownloadRequestHandler extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		if (isSessionValid(session) == false) {
			response.sendRedirect("sessiontimeout.jsp");
		}
		else {
		    String username = session.getAttribute("username").toString();
	        session.setAttribute("username", username);
	        
	        String[] nameSpaceAndPid = request.getParameter("namespaceandpid").split(":");
			String fileExtension = request.getParameter(nameSpaceAndPid[0]+nameSpaceAndPid[1]);
			System.out.println("start to download "+nameSpaceAndPid[0]+" "+nameSpaceAndPid[1]+" "+fileExtension);
	        
			if (fileExtension.endsWith("null")) {
				response.sendRedirect("downloaderror.jsp");
			} else {
				downloadFile(response, nameSpaceAndPid, fileExtension);
			}
	    }
	}
	
	/*
	 * Check if session is valid. If yes, return true. Otherwise, return false.
	 */
	private boolean isSessionValid(HttpSession session) {
		//Check if the session is valid.
		if (session.getAttribute("username") != null) {
		    return true;
		} else {
	        return false;
		}
	}
	
	/*
	 * Download the specified file.
	 */
	private void downloadFile(HttpServletResponse response, String[] nameSpaceAndPid, String fileExtension) throws ServletException, IOException {
		ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
		//final String downloadTempDirectory = "C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/kindura2/downloadtemp/";
		final String tempDownloadDirectory = configurationFileParser.getKinduraParameters().get("TempDownloadDirectory");
		//final String displayDownloadDirectory = "downloadtemp/";
		final String displayDownloadDirectory = configurationFileParser.getKinduraParameters().get("DisplayDownloadDirectory");
		
		String revisedFileNameForDownload = reviseFileNameForDownload(nameSpaceAndPid[1]);
		//Download the specified file from Cloud to local server and store it in a local temporary folder.
		try {
			//duraStoreClient = new DuraStoreClient("localhost", "8080", "durastore", "root", "rpw");
			DuraStoreClient duraStoreClient = new DuraStoreClient(configurationFileParser.getKinduraParameters().get("DuraCloudHost"), configurationFileParser.getKinduraParameters().get("DuraCloudPort"), 
					configurationFileParser.getKinduraParameters().get("DuraCloudContext"), configurationFileParser.getKinduraParameters().get("DuraCloudUsername"), configurationFileParser.getKinduraParameters().get("DuraCloudPassword"));
			//duraStoreClient.downloadFile(nameSpaceAndPid[0], nameSpaceAndPid[1], revisedFileNameForDownload, tempDownloadDirectory, fileExtension, Integer.valueOf(cfp.getKinduraParameters().get("NumberOfBytes")));
			//////duraStoreClient.downloadFile(duraStoreClient.defaultContentStore, "root", nameSpaceAndPid[1], revisedFileNameForDownload, tempDownloadDirectory, fileExtension, Integer.valueOf(configurationFileParser.getKinduraParameters().get("NumberOfBytes")));
			duraStoreClient.downloadFile(duraStoreClient.defaultContentStore, nameSpaceAndPid[0], nameSpaceAndPid[1], revisedFileNameForDownload, tempDownloadDirectory, fileExtension, Integer.valueOf(configurationFileParser.getKinduraParameters().get("NumberOfBytes")));
		} catch (ContentStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//Redirect the link to the file stored in the local temporary folder.
		response.sendRedirect(displayDownloadDirectory+revisedFileNameForDownload+"."+fileExtension);
	}
	
	public String reviseFileNameForDownload(String originalFileName) {
    	String revisedFileName = originalFileName.substring(originalFileName.lastIndexOf("/")+1);
    	return revisedFileName;
    }
}

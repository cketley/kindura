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
		
		//Check if the session is valid.
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
			ConfigurationFileParser cfp = new ConfigurationFileParser();
			//final String downloadTempDirectory = "C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/kindura2/downloadtemp/";
			final String tempDownloadDirectory = cfp.kinduraParameters.get("TempDownloadDirectory");
			//final String displayDownloadDirectory = "downloadtemp/";
			final String displayDownloadDirectory = cfp.kinduraParameters.get("DisplayDownloadDirectory");
			
			//Download the specified file from Cloud to local server and store it in a local temporary folder.
			DuraStoreClient duraStoreClient;
			try {
				//duraStoreClient = new DuraStoreClient("localhost", "8080", "durastore", "root", "rpw");
				duraStoreClient = new DuraStoreClient(cfp.kinduraParameters.get("DuraCloudHost"), cfp.kinduraParameters.get("DuraCloudPort"), 
						cfp.kinduraParameters.get("DuraCloudContext"), cfp.kinduraParameters.get("DuraCloudUsername"), cfp.kinduraParameters.get("DuraCloudPassword"));
				duraStoreClient.downloadFile(nameSpaceAndPid[0], nameSpaceAndPid[1], tempDownloadDirectory, fileformat, 1024);
			} catch (ContentStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//Redirect the link to the file stored in the local temporary folder.
			response.sendRedirect(displayDownloadDirectory+nameSpaceAndPid[1]+"."+fileformat);
		}	
	}
}

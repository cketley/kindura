package org.kindura;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yourmediashelf.fedora.client.FedoraClient;

/**
 * This class is used to search data collections' metaData stored in Fedora repository.
 * @author Jun Zhang
 */
public class SearchRequestHandler extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String search_term = request.getParameter("search_term");
		if (search_term == "") {
			search_term = "*";
		}
		
		String search_by = request.getParameter("search_by");
		System.out.println("search_term: "+search_term);
		System.out.println("search_by: "+search_by);
		FedoraServiceManager fedoraServiceManager = new FedoraServiceManager();
		
		ConfigurationFileParser cfp = new ConfigurationFileParser();
		//FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection("http://localhost:8080/fedora/", "fedoraAdmin", "fedoraAdmin");
		FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection(cfp.kinduraParameters.get("FedoraHost"), cfp.kinduraParameters.get("FedoraUsername"), cfp.kinduraParameters.get("FedoraPassword"));
		
		List<String> pids = fedoraServiceManager.getObjectPIDs(fedoraClient, search_term, search_by);
		
		String pid = null;
		String cost = null;
		String action = null;
		String url = null;
		String mimeType = null;
		String fileExtension = null;
		
		String[] splitedNameSpaceAndPid = null;
		System.out.println("pids.size(): "+pids.size());
		
		List<String> attribute = null;
		
		if (pids.size() > 0) {
			request.setAttribute("pidCount", pids.size());
			for (int i=0;i<pids.size();i++) {
				url = fedoraServiceManager.getContentURL(fedoraClient, pids.get(i));
				mimeType = fedoraServiceManager.getMIMEType(fedoraClient, pids.get(i));
				fileExtension = fedoraServiceManager.getFileExtension(fedoraClient, pids.get(i));
				splitedNameSpaceAndPid = pids.get(i).split(":");
				
				//pid = fedoraServiceManager.getObjectAttribute(fedora, pids.get(i), "pid").get(0);
				//pid = pid.substring(1, pid.length()-1);
				//request.setAttribute("object"+i+"pid", pid);
				request.setAttribute("object"+i+"pid", fedoraServiceManager.getObjectAttribute(fedoraClient, pids.get(i), "pid").get(0));
				
				attribute = fedoraServiceManager.getObjectAttribute(fedoraClient, pids.get(i), "label");
				if (attribute.size() > 0) {
					request.setAttribute("object"+i+"label", attribute.get(0));
				}
				else {
					request.setAttribute("object"+i+"label", "");
				}
				
				attribute = fedoraServiceManager.getObjectAttribute(fedoraClient, pids.get(i), "creator");
				if (attribute.size() > 0) {
					request.setAttribute("object"+i+"creator", attribute.get(0));
				}
				else {
					request.setAttribute("object"+i+"creator", "");
				}
				
				attribute = fedoraServiceManager.getObjectAttribute(fedoraClient, pids.get(i), "cDate");
				if (attribute.size() > 0) {
					request.setAttribute("object"+i+"cDate", attribute.get(0));
				}
				else {
					request.setAttribute("object"+i+"cDate", "");
				}
				
				attribute = fedoraServiceManager.getObjectAttribute(fedoraClient, pids.get(i), "state");
				if (attribute.size() > 0) {
					request.setAttribute("object"+i+"state", attribute.get(0));
				}
				else {
					request.setAttribute("object"+i+"state", "");
				}
				
				attribute = fedoraServiceManager.getObjectAttribute(fedoraClient, pids.get(i), "state");
				if (attribute.size() > 0) {
					request.setAttribute("object"+i+"state", attribute.get(0));
				}
				else {
					request.setAttribute("object"+i+"state", "");
				}
				
				cost = fedoraServiceManager.getContentCost(fedoraClient, pids.get(i));
				if (cost != null) {
					request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
				}
				else {
					request.setAttribute("object"+i+"cost", "");
				}
				
				action = fedoraServiceManager.getContentAction(fedoraClient, pids.get(i));
				if (action != null) {
					request.setAttribute("object"+i+"action", fedoraServiceManager.getContentAction(fedoraClient, pids.get(i)));
				}
				else {
					request.setAttribute("object"+i+"action", "");
				}
				
				request.setAttribute("url"+i, url);
				request.setAttribute("mimeType"+i, mimeType);
				request.setAttribute("nameSpace"+i, splitedNameSpaceAndPid[0]);
				request.setAttribute("pid"+i, splitedNameSpaceAndPid[1]);
				request.setAttribute("fileExtension"+i, fileExtension);
			}
		}
		
		
		RequestDispatcher requestDispatcher = request.getRequestDispatcher("searchresults.jsp");
		requestDispatcher.forward(request, response);
	}
}

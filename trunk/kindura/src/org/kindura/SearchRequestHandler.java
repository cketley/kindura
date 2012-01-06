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
 * This class is used to search data collections' metadata stored in Fedora repository.
 * @author Jun Zhang
 */
public class SearchRequestHandler extends HttpServlet {
	
	FedoraServiceManager fedoraServiceManager = new FedoraServiceManager();
	ConfigurationFileParser cfp = new ConfigurationFileParser();
	//FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection(cfp.getKinduraParameters().get("FedoraHost"), cfp.getKinduraParameters().get("FedoraUsername"), cfp.getKinduraParameters().get("FedoraPassword"));
	FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("search_term: "+request.getParameter("search_term"));
		System.out.println("search_by: "+request.getParameter("search_by"));
		
		//get search term and rename it if necessary.
		String search_term = renameSearchTerm(request.getParameter("search_term"));
		
		String search_by = request.getParameter("search_by");
		
		String userName = request.getParameter("username");
		System.out.println("[SearchRequestHandler] userName: "+userName);
		
		List<String> pids = null;
		//List<String> pids = fedoraServiceManager.getObjectPIDs(search_term, search_by);
		if (search_term == "") {
			pids = fedoraServiceManager.getObjectPIDs(userName, "pid");
		} else {
			pids = fedoraServiceManager.getObjectPIDs(search_term, search_by);
		}
		
		setObjectAttributeValues(request, pids);
		
		RequestDispatcher requestDispatcher = request.getRequestDispatcher("searchresults.jsp");
		requestDispatcher.forward(request, response);
	}
	
	/*
	 * Replace "" with "*" in the search term.
	 */
	private String renameSearchTerm(String search_term) {
		String searchTerm = search_term;
		if (searchTerm == "") {
			searchTerm = "*";
		}
		return searchTerm;
	}
	
	/*
	 * This method sets the value of the attribute for the request which is retrieved later in "searchresults.jsp" by passing the request object through the dispatcher method.
	 */
	private void setObjectAttributeValues(HttpServletRequest request, List<String> pids) {
		String projectName = null;
		String collectionName = null;
		String creator = null;
		String collectionDescription = null;
		String storageType = null;
		String projectCost = null;
		String actionRequired = null;
		
		String pid = null;
		
		String url = null;
		String fileExtension = null;
		
		String[] splitedNameSpaceAndPid = null;
		if (pids != null) {
			System.out.println("pids.size(): "+pids.size());
			
			//List<String> attribute = null;
			String attribute = null;
		
			if (pids.size() > 0) {
				request.setAttribute("pidCount", pids.size());
				for (int i=0;i<pids.size();i++) {
					url = fedoraServiceManager.getContentURL(pids.get(i));
					fileExtension = fedoraServiceManager.getADataStream(pids.get(i), "filenameextension");
					splitedNameSpaceAndPid = pids.get(i).split(":");
					
					//request.setAttribute("object"+i+"pid", fedoraServiceManager.getAObjectAttribute(fedoraClient, pids.get(i), "pid"));
					request.setAttribute("object"+i+"pid", splitedNameSpaceAndPid[1]);
									
					attribute = fedoraServiceManager.getAObjectAttribute(pids.get(i), "label");
					if (attribute != null) {
						request.setAttribute("object"+i+"label", attribute);
					}
					else {
						request.setAttribute("object"+i+"label", "");
					}
					
					attribute = fedoraServiceManager.getAObjectAttribute(pids.get(i), "creator");
					if (attribute != null) {
						request.setAttribute("object"+i+"creator", attribute);
					}
					else {
						request.setAttribute("object"+i+"creator", "");
					}
					
					attribute = fedoraServiceManager.getAObjectAttribute(pids.get(i), "cDate");
					if (attribute != null) {
						request.setAttribute("object"+i+"cDate", attribute);
					}
					else {
						request.setAttribute("object"+i+"cDate", "");
					}
					
					attribute = fedoraServiceManager.getAObjectAttribute(pids.get(i), "state");
					if (attribute != null) {
						request.setAttribute("object"+i+"state", attribute);
					}
					else {
						request.setAttribute("object"+i+"state", "");
					}
					
					creator = fedoraServiceManager.getADataStream(pids.get(i), "creator");
					if (creator != null) {
						//request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
						request.setAttribute("object"+i+"creator", creator);
					}
					else {
						request.setAttribute("object"+i+"creator", "");
					} 
					
					projectName = fedoraServiceManager.getADataStream(pids.get(i), "projectName");
					if (projectName != null) {
						//request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
						request.setAttribute("object"+i+"projectName", projectName);
					}
					else {
						request.setAttribute("object"+i+"projectName", "");
					}
					
					collectionDescription = fedoraServiceManager.getADataStream(pids.get(i), "collectionDescription");
					if (projectName != null) {
						//request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
						request.setAttribute("object"+i+"collectionDescription", collectionDescription);
					}
					else {
						request.setAttribute("object"+i+"collectionDescription", "");
					}
					
					storageType = fedoraServiceManager.getADataStream(pids.get(i), "storageType");
					if (projectName != null) {
						//request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
						request.setAttribute("object"+i+"storageType", storageType);
					}
					else {
						request.setAttribute("object"+i+"storageType", "");
					}
					
					
					
					projectCost = fedoraServiceManager.getADataStream(pids.get(i), "projectcost");
					if (projectCost != null) {
						//request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
						request.setAttribute("object"+i+"projectCost", projectCost);
					}
					else {
						request.setAttribute("object"+i+"projectCost", "");
					}
					
					actionRequired = fedoraServiceManager.getADataStream(pids.get(i), "actionRequired");
					if (actionRequired != null) {
						//request.setAttribute("object"+i+"action", fedoraServiceManager.getContentAction(fedoraClient, pids.get(i)));
						request.setAttribute("object"+i+"actionRequired", actionRequired);
					}
					else {
						request.setAttribute("object"+i+"actionRequired", "");
					}
					
					request.setAttribute("url"+i, url);
					//request.setAttribute("mimeType"+i, mimeType);
					request.setAttribute("nameSpace"+i, splitedNameSpaceAndPid[0]);
					request.setAttribute("pid"+i, splitedNameSpaceAndPid[1]);
					request.setAttribute("fileExtension"+i, fileExtension);
					request.setAttribute("parentFolderNameForDuracloud"+i,"");
					request.setAttribute("parentFolderNameForFedora"+i,"");
				}
			}
		}
	}
}

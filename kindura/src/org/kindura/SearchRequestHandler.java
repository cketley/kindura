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
			//pids = fedoraServiceManager.getObjectPIDs(userName, search_by);
			pids = fedoraServiceManager.getObjectNames("", search_by);
		} else {
			//pids = fedoraServiceManager.getObjectPIDs(search_term, search_by);
			pids = fedoraServiceManager.getObjectNames(search_term, search_by);
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
		String projectFunder = null;
		String ownerShip = null;
		String collectionName = null;
		String displayName = null;
		String creator = null;
		String description = null;
		String storageType = null;
		String collectionCost = null;
		String actionRequired = null;
		String fedoraObjectType = null;
		String department = null;
		String typeOfData = null;
		String startDate = null;
		String endDate = null;
		String projectContact = null;
		
		
		String pid = null;
		
		String url = null;
		String fileExtension = null;
		
		String[] splitedNameSpaceAndPid = null;
		if (pids != null) {
			System.out.println("pids.size(): "+pids.size());
			
			//List<String> attribute = null;
			String attribute = null;
			if (pids.size() == 0) {
				request.setAttribute("pidCount", pids.size());
			} else if (pids.size() > 0) {
				int pidCount = 0;
				for (int i=0;i<pids.size();i++) {
					fedoraObjectType = fedoraServiceManager.getADataStream(pids.get(i), "fedoraObjectType");
					//if ((fedoraObjectType != null) && (!fedoraObjectType.equals("file"))) {
					if ((fedoraObjectType != null) && (fedoraObjectType.equals("collection"))) {
						url = fedoraServiceManager.getContentURL(pids.get(i));
						fileExtension = fedoraServiceManager.getADataStream(pids.get(i), "fileExtension");
						splitedNameSpaceAndPid = pids.get(i).split(":");
						
						//request.setAttribute("object"+i+"pid", fedoraServiceManager.getAObjectAttribute(fedoraClient, pids.get(i), "pid"));
						request.setAttribute("object"+pidCount+"pid", splitedNameSpaceAndPid[1]);
										
						attribute = fedoraServiceManager.getAObjectAttribute(pids.get(i), "label");
						if (attribute != null) {
							request.setAttribute("object"+pidCount+"label", attribute);
						} else {
							request.setAttribute("object"+pidCount+"label", "");
						}
						
						/*attribute = fedoraServiceManager.getAObjectAttribute(pids.get(i), "creator");
						System.out.println("[SearchRequestHandler] creator: "+creator);
						if (attribute != null) {
							request.setAttribute("object"+pidCount+"creator", attribute);
						} else {
							request.setAttribute("object"+pidCount+"creator", "");
						}*/
						
						attribute = fedoraServiceManager.getAObjectAttribute(pids.get(i), "cDate");
						if (attribute != null) {
							request.setAttribute("object"+pidCount+"cDate", attribute.substring(0, attribute.indexOf("T")));
						} else {
							request.setAttribute("object"+pidCount+"cDate", "");
						}
						
						attribute = fedoraServiceManager.getAObjectAttribute(pids.get(i), "state");
						if (attribute != null) {
							request.setAttribute("object"+pidCount+"state", attribute);
						} else {
							request.setAttribute("object"+pidCount+"state", "");
						}
						
						creator = fedoraServiceManager.getADataStream(pids.get(i), "creator");
						if (creator != null) {
							//request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
							request.setAttribute("object"+pidCount+"creator", creator);
						} else {
							request.setAttribute("object"+pidCount+"creator", "");
						} 
						
						projectName = fedoraServiceManager.getADataStream(pids.get(i), "projectName");
						if (projectName != null) {
							//request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
							request.setAttribute("object"+pidCount+"projectName", projectName);
						} else {
							request.setAttribute("object"+pidCount+"projectName", "");
						}
						
						//fedoraObjectType = fedoraServiceManager.getADataStream(pids.get(i), "fedoraObjectType");
						if (fedoraObjectType != null) {
							if (fedoraObjectType.equals("project")) {
								description = fedoraServiceManager.getADataStream(pids.get(i), "projectDescription");
							} else if (fedoraObjectType.equals("collection")) {
								description = fedoraServiceManager.getADataStream(pids.get(i), "collectionDescription");
							}
						} 
						//request.setAttribute("object"+pidCount+"description", description);
						if (description != null) {
							//request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
							request.setAttribute("object"+pidCount+"description", description);
						} else {
							request.setAttribute("object"+pidCount+"description", "");
						}
						
						/*request.setAttribute("object"+pidCount+"projectFunder", description);
						
						projectCost = fedoraServiceManager.getADataStream(pids.get(i), "projectCost");
						if (description != null) {
							//request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
							request.setAttribute("object"+pidCount+"description", description);
						} else {
							request.setAttribute("object"+pidCount+"description", "");
						}*/
						
						storageType = fedoraServiceManager.getADataStream(pids.get(i), "storageType");
						if (storageType != null) {
							//request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
							request.setAttribute("object"+pidCount+"storageType", storageType);
						} else {
							request.setAttribute("object"+pidCount+"storageType", "");
						}
						
						collectionCost = fedoraServiceManager.getADataStream(pids.get(i), "collectionCost");
						if (collectionCost != null) {
							//request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
							request.setAttribute("object"+pidCount+"collectionCost", collectionCost);
						} else {
							request.setAttribute("object"+pidCount+"collectionCost", "");
						}
						
						actionRequired = fedoraServiceManager.getADataStream(pids.get(i), "actionRequired");
						if (actionRequired != null) {
							//request.setAttribute("object"+i+"action", fedoraServiceManager.getContentAction(fedoraClient, pids.get(i)));
							request.setAttribute("object"+pidCount+"actionRequired", actionRequired);
						} else {
							request.setAttribute("object"+pidCount+"actionRequired", "");
						}
						
						request.setAttribute("url"+pidCount, url);
						//request.setAttribute("mimeType"+i, mimeType);
						request.setAttribute("nameSpace"+pidCount, splitedNameSpaceAndPid[0]);
						request.setAttribute("alphaNumericName"+pidCount, splitedNameSpaceAndPid[1]);
						//request.setAttribute("fileExtension"+pidCount, fileExtension);
						request.setAttribute("parentFolderNameForDuracloud"+pidCount,"");
						request.setAttribute("parentFolderNameForFedora"+pidCount,"");
						
						if (fedoraObjectType.equals("project")) {
							displayName = fedoraServiceManager.getADataStream(pids.get(i), "projectName");
						} else if (fedoraObjectType.equals("collection")) {
							displayName = fedoraServiceManager.getADataStream(pids.get(i), "collectionName");
						} else if (fedoraObjectType.equals("folder")) {
							displayName = fedoraServiceManager.getADataStream(pids.get(i), "folderName");
						}
						request.setAttribute("displayName"+pidCount, displayName);
						
						startDate = fedoraServiceManager.getADataStream("root:"+projectName, "startDate");
						if (startDate != null) {
							request.setAttribute("object"+pidCount+"startDate", startDate);
						} else {
							request.setAttribute("object"+pidCount+"startDate", "");
						}
						
						endDate = fedoraServiceManager.getADataStream("root:"+projectName, "endDate");
						if (endDate != null) {
							request.setAttribute("object"+pidCount+"endDate", endDate);
						} else {
							request.setAttribute("object"+pidCount+"endDate", "");
						}
						
						projectContact = fedoraServiceManager.getADataStream("root:"+projectName, "projectContact");
						if (startDate != null) {
							request.setAttribute("object"+pidCount+"projectContact", projectContact);
						} else {
							request.setAttribute("object"+pidCount+"projectContact", "");
						}
						
						pidCount++;
					}	
				}
				request.setAttribute("pidCount", pidCount);
			}
		}
	}
}

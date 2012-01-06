package org.kindura;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.generated.access.DatastreamType;

/**
 * This class is used to search data collections' metadata stored in Fedora repository.
 * @author Jun Zhang
 */
public class ViewProjectRequestHandler extends HttpServlet {
	
	FedoraServiceManager fedoraServiceManager = new FedoraServiceManager();
	ConfigurationFileParser cfp = new ConfigurationFileParser();
	//FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection(cfp.getKinduraParameters().get("FedoraHost"), cfp.getKinduraParameters().get("FedoraUsername"), cfp.getKinduraParameters().get("FedoraPassword"));
	FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("[SearchFileRequestHandler] nameSpace: "+request.getParameter("nameSpace"));
		System.out.println("[SearchFileRequestHandler] pid: "+request.getParameter("pid"));
		
		//get search term and rename it if necessary.
		String nameSpace = request.getParameter("nameSpace");
		
		String alphaNumericName = request.getParameter("pid");
		
		//String parentFolderName = request.getParameter("parentFolderName");
		
		String parentFolderNameForDuracloud = request.getParameter("parentFolderNameForDuracloud").replace("_", "/");
		
		String parentFolderNameForFedora = request.getParameter("parentFolderNameForFedora").replace("_", ".");
		
		String collectionName = request.getParameter("collectionName");
		
		System.out.println("[SearchFileRequestHandler] pid "+nameSpace+":"+alphaNumericName);
		
		ArrayList<DatastreamType> datastreams = fedoraServiceManager.getDataStreams(nameSpace+":"+alphaNumericName);
		
		//Set<String> nextLevelFolders = getNextLevelStructure(datastreams, parentFolderName);
		Map<String, String> nextLevelFolders = getNextLevelStructure(datastreams, parentFolderNameForFedora);
		
		//Iterator it = nextLevelFolders.iterator();
		//while (it.hasNext()) {
        //    System.out.println("[SearchFileRequestHandler] folder name: " + (String)it.next());
		//}
		
		//setDataStreamValues(request, nameSpace, alphaNumericName, nextLevelFolders, collectionName);
		setDataStreamValues(request, nameSpace, alphaNumericName, nextLevelFolders, alphaNumericName, parentFolderNameForDuracloud, parentFolderNameForFedora);
		
		RequestDispatcher requestDispatcher = request.getRequestDispatcher("viewprojectfolders.jsp");
		requestDispatcher.forward(request, response);
	}
	
	/*private Set<String> getNextLevelStructure(List<DatastreamType> datastreams, String parentFolderName) {
		Set<String> nextLevelFolders = new HashSet<String>();
		String folderName = null;
		String choppedFolderName = null;
		for (int i=0;i<datastreams.size();i++) {
			folderName = datastreams.get(i).getDsid();
			System.out.println("[SearchFileRequestHandler] folder name "+folderName);
			System.out.println("[SearchFileRequestHandler] parent folder name "+parentFolderName);
			//if ((parentFolderName != "") && folderName.contains("parentFolderName")) {
			if (folderName.contains(".") && (folderName.contains(parentFolderName) || (parentFolderName == ""))) {
				if (parentFolderName != "") {
					//choppedFolderName = folderName.substring(parentFolderName.length()+1);
					choppedFolderName = folderName.substring(folderName.indexOf(parentFolderName)+parentFolderName.length()+1);
				} else {
					choppedFolderName = folderName;
				}
				System.out.println("[SearchFileRequestHandler] folder name after chopping parent folder "+choppedFolderName);
				if (choppedFolderName.contains(".")) {
					choppedFolderName = choppedFolderName.substring(0, choppedFolderName.indexOf("."));
					System.out.println("[SearchFileRequestHandler] folder name after chopping child folder "+choppedFolderName);
					nextLevelFolders.add(choppedFolderName);
				}
				else {
					nextLevelFolders.add(folderName);
				}
			}
			
		}
		return nextLevelFolders;
	}*/
	
	private Map<String, String> getNextLevelStructure(List<DatastreamType> datastreams, String parentFolderNameForFedora) {
		HashMap<String, String> nextLevelFolders = new HashMap<String, String>();
		String folderName = null;
		String folderType = null;
		String choppedFolderName = null;
		for (int i=0;i<datastreams.size();i++) {
			folderName = datastreams.get(i).getDsid();
			System.out.println("[SearchFileRequestHandler] folder name "+folderName);
			System.out.println("[SearchFileRequestHandler] parent folder name for Fedora"+parentFolderNameForFedora);
			//if ((parentFolderName != "") && folderName.contains("parentFolderName")) {
			if (folderName.contains(".") && (folderName.contains(parentFolderNameForFedora) || (parentFolderNameForFedora == ""))) {
				if (parentFolderNameForFedora != "") {
					//choppedFolderName = folderName.substring(parentFolderName.length()+1);
					choppedFolderName = folderName.substring(folderName.indexOf(parentFolderNameForFedora)+parentFolderNameForFedora.length()+1);
				} else {
					choppedFolderName = folderName;
				}
				System.out.println("[SearchFileRequestHandler] folder name after chopping parent folder "+choppedFolderName);
				
				//Check if the next level structure is a folder or a file.
				if (isNextLevelStructureExisted(choppedFolderName) == true) {
					folderType = "Folder";
				} else {
					folderType = "File";
				}
				
				if (choppedFolderName.contains(".")) {
					choppedFolderName = choppedFolderName.substring(0, choppedFolderName.indexOf("."));
					System.out.println("[SearchFileRequestHandler] folder name after chopping child folder "+choppedFolderName);
				}
				nextLevelFolders.put(choppedFolderName, folderType);
			}
			
		}
		return nextLevelFolders;
	}
	
	private boolean isNextLevelStructureExisted(String nameToBeExamined) {
		int count =0;
		System.out.println("[SearchFileRequestHandler] nameToBeExamined: "+nameToBeExamined);
		while(nameToBeExamined.contains(".")) {
			nameToBeExamined = nameToBeExamined.substring(nameToBeExamined.indexOf(".")+1);
		    count++;
		}
		System.out.println("[SearchFileRequestHandler] count "+count);
		if (count > 1) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * This method sets the value of the attribute for the request which is retrieved later in "searchresults.jsp" by passing the request object through the dispatcher method.
	 */
	private void setDataStreamValues(HttpServletRequest request, String nameSpace, String alphaNumericName, Map<String, String> files, String projectName, String parentFolderNameForDuracloud, String parentFolderNameForFedora) {
		//String pid = null;
		//String cost = null;
		//String action = null;
		//String projectName = null;
		
		String fileExtension = null;
		
		String[] splitedNameSpaceAndPid = null;
		System.out.println("files.size(): "+files.size());
		
		//List<String> attribute = null;
		String attribute = null;
		
		if (files.size() == 0) {
			request.setAttribute("dataStreamCount", 0);
		}
		else if (files.size() > 0) {
			request.setAttribute("dataStreamCount", files.size());
			//Iterator it = files.iterator();
			int i = 0;
			String fileName = null;
			String fileType = null;
			String fileSize = null;
			String url = null;
			for (Map.Entry<String, String> entry : files.entrySet()) {
			//while (it.hasNext()) {
				//fileName = (String)it.next();
				fileName = entry.getKey();
				fileType = entry.getValue();
				//projectName = fedoraServiceManager.getADataStream(fedoraClient, nameSpace+":"+alphaNumericName, parentFolderNameForFedora+"."+fileName+".projectName");
				//collectionName = fedoraServiceManager.getADataStream(fedoraClient, nameSpace+":"+alphaNumericName, parentFolderName+"."+fileName+".collectionName");
				//creator = fedoraServiceManager.getADataStream(fedoraClient, nameSpace+":"+alphaNumericName, parentFolderName+"."+fileName+".creator");
				//collectionDescription = fedoraServiceManager.getADataStream(fedoraClient, nameSpace+":"+alphaNumericName, parentFolderName+"."+fileName+".collectionDescription");
				//storageType = fedoraServiceManager.getADataStream(fedoraClient, nameSpace+":"+alphaNumericName, parentFolderName+"."+fileName+".storageType");
				//actionRequired = fedoraServiceManager.getADataStream(fedoraClient, nameSpace+":"+alphaNumericName, parentFolderName+"."+fileName+".actionRequired");
				fileSize = fedoraServiceManager.getADataStream(nameSpace+":"+alphaNumericName, parentFolderNameForFedora+"."+fileName+".fileSize");
				if (fileSize == null) {
					fileSize = "";
				}
				url = fedoraServiceManager.getADataStream(nameSpace+":"+alphaNumericName, parentFolderNameForFedora+"."+fileName+".url");
				fileExtension = fedoraServiceManager.getADataStream(nameSpace+":"+alphaNumericName, parentFolderNameForFedora+"."+fileName+".fileNameExtension");
				System.out.println("[SearchFileRequestHandler] pid "+nameSpace+":"+alphaNumericName);
				System.out.println("[SearchFileRequestHandler] datastream "+parentFolderNameForFedora+"."+fileName+".url");
				System.out.println("[SearchFileRequestHandler] url "+url);
				
				request.setAttribute("projectName", projectName);
				//request.setAttribute("collectionName"+i, collectionName);
				//request.setAttribute("creator"+i, creator);
				//request.setAttribute("collectionDescription", collectionDescription);
				//request.setAttribute("storage"+i, storageType);
				//request.setAttribute("actionRequired"+i, actionRequired);
				
				request.setAttribute("fileName"+i, fileName);
				request.setAttribute("fileType"+i, fileType);
				request.setAttribute("fileSize"+i, fileSize);
				request.setAttribute("url"+i, url);
				
				request.setAttribute("nameSpace"+i, nameSpace);
				request.setAttribute("pid"+i, alphaNumericName);
				request.setAttribute("fileExtension"+i, fileExtension);
				request.setAttribute("parentFolderNameForDuracloud"+i,parentFolderNameForDuracloud);
				request.setAttribute("parentFolderNameForFedora"+i,parentFolderNameForFedora);
				
				
				i++;
				//url = fedoraServiceManager.getContentURL(fedoraClient, datastreams.get(i));
				//fileExtension = fedoraServiceManager.getADataStream(fedoraClient, datastreams.get(i), "filenameextension");
				//splitedNameSpaceAndPid = datastreams.get(i).split(":");
				
				//request.setAttribute("object"+i+"pid", fedoraServiceManager.getAObjectAttribute(fedoraClient, dataStreams.get(i), "pid"));
				
				/*attribute = fedoraServiceManager.getAObjectAttribute(fedoraClient, pids.get(i), "label");
				if (attribute != null) {
					request.setAttribute("object"+i+"label", attribute);
				}
				else {
					request.setAttribute("object"+i+"label", "");
				}
				
				attribute = fedoraServiceManager.getAObjectAttribute(fedoraClient, pids.get(i), "creator");
				if (attribute != null) {
					request.setAttribute("object"+i+"creator", attribute);
				}
				else {
					request.setAttribute("object"+i+"creator", "");
				}
				
				attribute = fedoraServiceManager.getAObjectAttribute(fedoraClient, pids.get(i), "cDate");
				if (attribute != null) {
					request.setAttribute("object"+i+"cDate", attribute);
				}
				else {
					request.setAttribute("object"+i+"cDate", "");
				}
				
				attribute = fedoraServiceManager.getAObjectAttribute(fedoraClient, pids.get(i), "state");
				if (attribute != null) {
					request.setAttribute("object"+i+"state", attribute);
				}
				else {
					request.setAttribute("object"+i+"state", "");
				}
				
				
				
				cost = fedoraServiceManager.getADataStream(fedoraClient, pids.get(i), "cost");
				if (cost != null) {
					//request.setAttribute("object"+i+"cost", fedoraServiceManager.getContentCost(fedoraClient, pids.get(i)));
					request.setAttribute("object"+i+"cost", fedoraServiceManager.getADataStream(fedoraClient, pids.get(i), "cost"));
				}
				else {
					request.setAttribute("object"+i+"cost", "");
				}
				
				action = fedoraServiceManager.getADataStream(fedoraClient, pids.get(i), "action");
				if (action != null) {
					//request.setAttribute("object"+i+"action", fedoraServiceManager.getContentAction(fedoraClient, pids.get(i)));
					request.setAttribute("object"+i+"cost", fedoraServiceManager.getADataStream(fedoraClient, pids.get(i), "action"));
				}
				else {
					request.setAttribute("object"+i+"action", "");
				}
				
				request.setAttribute("url"+i, url);
				//request.setAttribute("mimeType"+i, mimeType);
				request.setAttribute("nameSpace"+i, splitedNameSpaceAndPid[0]);
				request.setAttribute("pid"+i, splitedNameSpaceAndPid[1]);
				request.setAttribute("fileExtension"+i, fileExtension);
				request.setAttribute("parentFolderName", parentFolderName);*/
			}
		}
	}
}

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

import org.duracloud.error.ContentStoreException;

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
		String nameSpace = null;
		String alphaNumericName = null;
		String projectName = null;
		String requestType = null;
		
		System.out.println("[ViewProjectRequestHandler] nameSpace: "+request.getParameter("nameSpace"));
		System.out.println("[ViewProjectRequestHandler] alphaNumericName: "+request.getParameter("alphaNumericName"));
		System.out.println("[ViewProjectRequestHandler] projectName: "+request.getParameter("projectName"));
		
		//get search term and rename it if necessary.
		nameSpace = request.getParameter("nameSpace");
		
		alphaNumericName = request.getParameter("alphaNumericName");
		
		projectName = request.getParameter("projectName");
		
		requestType = request.getParameter("requestType");
		
		System.out.println("[ViewProjectRequestHandler] pid "+nameSpace+":"+alphaNumericName);
		
		//HashMap<String, String> collectionPIDs = fedoraServiceManager.getChildNames(nameSpace+":"+alphaNumericName);
		//setParameters(request, collectionPIDs);
		Map<String, String> collectionPIDs = null;

		if (alphaNumericName == null) {
			collectionPIDs = fedoraServiceManager.getChildPIDs(nameSpace+":"+projectName);
		} else {
			collectionPIDs = fedoraServiceManager.getChildPIDs(nameSpace+":"+alphaNumericName);
		}
		setDataStreamValues(request, collectionPIDs, projectName);
		
		//Map<String, String> nextLevelFolders = getNextLevelStructure(datastreams, parentFolderNameForFedora);
		
		//setDataStreamValues(request, nameSpace, alphaNumericName, nextLevelFolders, alphaNumericName, parentFolderNameForDuracloud, parentFolderNameForFedora);
		/*if (requestType.equals("view")) {
			RequestDispatcher requestDispatcher = request.getRequestDispatcher("viewprojectcollection.jsp");
			requestDispatcher.forward(request, response);
		} else if (requestType.equals("search")) {
			//RequestDispatcher requestDispatcher = request.getRequestDispatcher("drilldownfolder.jsp");
			RequestDispatcher requestDispatcher = request.getRequestDispatcher("viewprojectcollection.jsp");
			requestDispatcher.forward(request, response);
		}*/
		RequestDispatcher requestDispatcher = request.getRequestDispatcher("viewprojectcollection.jsp");
		requestDispatcher.forward(request, response);
	}
	
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
	
	private void setParameters(HttpServletRequest request, Map<String, String> PIDs) {
		int pidCount = 0;
		for (Map.Entry<String, String> entry : PIDs.entrySet()) {
			request.setAttribute("nameSpace"+pidCount, entry.getKey().substring(0,entry.getKey().indexOf(":")));
			request.setAttribute("alphaNumericName"+pidCount, entry.getKey().substring(entry.getKey().indexOf(":")+1));
			request.setAttribute("pid"+pidCount+"fedoraObjectType", entry.getValue());
			pidCount++;
		}
		request.setAttribute("numberOfRows", pidCount);
	}
	
	/*
	 * This method sets the value of the attribute for the request which is retrieved later in "searchresults.jsp" by passing the request object through the dispatcher method.
	 */
	//private void setDataStreamValues(HttpServletRequest request, String nameSpace, String alphaNumericName, Map<String, String> files, String projectName, String parentFolderNameForDuracloud, String parentFolderNameForFedora) {
	private void setDataStreamValues(HttpServletRequest request, Map<String, String> collectionPIDs, String projectName) {
		
		String fileExtension = null;
		
		String[] splitedNameSpaceAndPid = null;
		System.out.println("collectionPIDs.size(): "+collectionPIDs.size());
		
		//List<String> attribute = null;
		String attribute = null;
		
		request.setAttribute("projectName", projectName);
		System.out.println("[ViewProjectRequestHandler] projectName "+projectName);
		
		if (collectionPIDs.size() == 0) {
			request.setAttribute("numberOfRows", 0);
		}
		else if (collectionPIDs.size() > 0) {
			request.setAttribute("numberOfRows", collectionPIDs.size());
			//Iterator it = files.iterator();
			int i = 0;
			/////String projectName = null;
			String collectionName = null;
			String nameSpace = null;
			String pid = null;
			String baseFileName = null;
			String fileType = null;
			String fileSize = null;
			String url = null;
			String alphaNumericName = null;
			String fileOriginalPath;
			String filePathOfDuracloud;
			String originalFileName = null;
			for (Map.Entry<String, String> entry : collectionPIDs.entrySet()) {
				
				pid = entry.getKey();
				fileType = entry.getValue();
				request.setAttribute("pid"+i+"fedoraObjectType", fileType);
				System.out.println("[ViewProjectRequestHandler] fileType: "+fileType);
				
				nameSpace = entry.getKey().substring(0,entry.getKey().indexOf(":"));
				request.setAttribute("nameSpace"+i, nameSpace);
				System.out.println("[ViewProjectRequestHandler] nameSpace "+nameSpace);
				
				if (fileType.equals("project")) {
					alphaNumericName = fedoraServiceManager.getADataStream(pid,"projectName");
				} else if (fileType.equals("collection")) {
					alphaNumericName = fedoraServiceManager.getADataStream(pid,"collectionName");
				} else if (fileType.equals("folder")) {
					alphaNumericName = fedoraServiceManager.getADataStream(pid,"folderName");
				} else if (fileType.equals("file")) {
					alphaNumericName = fedoraServiceManager.getADataStream(pid,"baseFileName");
					
					baseFileName = fedoraServiceManager.getADataStream(pid,"baseFileName");
					request.setAttribute("baseFileName"+i, baseFileName);
					
					fileExtension = fedoraServiceManager.getADataStream(pid, "fileExtension");
					request.setAttribute("fileExtension"+i, fileExtension);
					System.out.println("[ViewProjectRequestHandler] fileExtension "+fileExtension);
					collectionName = fedoraServiceManager.getADataStream(pid, "collectionName");
					request.setAttribute("collectionName"+i, collectionName);
					fileOriginalPath = fedoraServiceManager.getADataStream(pid, "filePath");
					
					ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
					DuraStoreClient duraStoreClient = null;
					try {
						duraStoreClient = new DuraStoreClient(configurationFileParser.getKinduraParameters().get("DuraCloudHost"), configurationFileParser.getKinduraParameters().get("DuraCloudPort"), configurationFileParser.getKinduraParameters().get("DuraCloudContext"), configurationFileParser.getKinduraParameters().get("DuraCloudUsername"), configurationFileParser.getKinduraParameters().get("DuraCloudPassword"));
					} catch (ContentStoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//////filePathOfDuracloud = collectionName+"/"+duraStoreClient.reviseFilePathForDuracloud(fileOriginalPath);
					filePathOfDuracloud = duraStoreClient.reviseFilePathForDuracloud(fileOriginalPath);
					request.setAttribute("parentFolderNameForDuracloud"+i, filePathOfDuracloud);
				}
				alphaNumericName = entry.getKey().substring(entry.getKey().indexOf(":")+1);
				request.setAttribute("alphaNumericName"+i, alphaNumericName);
				System.out.println("[ViewProjectRequestHandler] nameSpace "+alphaNumericName);
				
				fileSize = fedoraServiceManager.getADataStream(pid, "fileSize");
				if (fileSize == null) {
					fileSize = "";
				}
				request.setAttribute("fileSize"+i, fileSize);
				System.out.println("[ViewProjectRequestHandler] fileSize "+fileSize);
				
				/////projectName = fedoraServiceManager.getADataStream(pid, "projectName");
				i++;
			}
		}
	}
}

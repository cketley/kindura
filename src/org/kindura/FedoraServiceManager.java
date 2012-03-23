package org.kindura;

import static com.yourmediashelf.fedora.client.FedoraClient.findObjects;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileUtils;
import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import com.yourmediashelf.fedora.client.request.Ingest;
import com.yourmediashelf.fedora.client.request.ListDatastreams;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import com.yourmediashelf.fedora.client.response.FindObjectsResponse;
import com.yourmediashelf.fedora.client.response.IngestResponse;
import com.yourmediashelf.fedora.client.response.ListDatastreamsResponse;
import com.yourmediashelf.fedora.generated.access.DatastreamType;

/**
 * This class is used to connect Fedora REST API by using MediaShelf.
 * @author Jun Zhang
 */
public class FedoraServiceManager {
	FedoraCredentials fedoracredential = null;
	FedoraClient fedoraClient = null;
	ConfigurationFileParser configurationFileParser = null;
	String duraCloudURL = null;
	String fedoraURL = null;
	
	/*
	 * Get a connection to the Fedora repository.
	 */
	/*public FedoraClient getFedoraConnection() {
    	//Setup connection with Fedora repository.
		FedoraCredentials fedoracredential;
		FedoraClient fedoraClient = null;
		try {
			fedoracredential = new FedoraCredentials(hosturl, username, password);
			fedoraClient = new FedoraClient(fedoracredential);
		} catch (MalformedURLException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return fedoraClient;
    }*/
	
	
	public FedoraServiceManager() {
		configurationFileParser = new ConfigurationFileParser();
		
		try {
			fedoracredential = new FedoraCredentials(configurationFileParser.getKinduraParameters().get("FedoraHost"), configurationFileParser.getKinduraParameters().get("FedoraUsername"), configurationFileParser.getKinduraParameters().get("FedoraPassword"));
			fedoraClient = new FedoraClient(fedoracredential);
			fedoraURL = configurationFileParser.getKinduraParameters().get("FedoraHost");
			duraCloudURL = configurationFileParser.getKinduraParameters().get("DuraCloudHost");
		} catch (MalformedURLException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
    
	public FedoraClient getFedoraConnection() {
		return fedoraClient;
	}
	
	/*
	 * Ingest a new object to the Fedora repository.
	 */
	public boolean ingestObject(String pid, String title, String datastreamid, String directorypath, String url, String mimetype) {
		IngestResponse response;
		try {
			response = new Ingest(pid).label(title).execute(fedoraClient); //Create a new Fedora object.
			FedoraClient.addDatastream(pid, datastreamid).controlGroup("R").dsLabel(url).dsLocation(url).mimeType(mimetype).execute(fedoraClient);
			//response = new Ingest(datastreamid).label(title).execute(fedoraClient);
			//fedoraClient.addRelationship(datastreamid).predicate("isACollectionOf").object(pid).isLiteral(false).execute(fedoraClient);
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	/*
	 * Get the pids of objects which matches search criteria.
	 */
	public List<String> getObjectPIDs(String search_term, String search_by) {
		FindObjectsResponse response = null;
		List<String> pids = null;
		List<String> filteredpids = new ArrayList<String>();
		//List<String> attribute = null;
		String attribute = null;
		//System.out.println("[FedoraServiceManger] search term: "+search_term);
		//System.out.println("[FedoraServiceManger] search by: "+search_by);
		try {
			int maxResults = Integer.valueOf(configurationFileParser.getKinduraParameters().get("FindObjectMaxResults"));
			response = findObjects().pid().terms("*"+search_term+"*").maxResults(maxResults).execute(fedoraClient);
			pids = response.getPids();
			if (search_by.equals("all")) {
				return pids;
			}
			else {
				//System.out.println("[FedoraServiceManger] pid size: "+pids.size());
				if (pids.size() > 0) {
					for (int i=0;i<pids.size();i++) {
				    	//System.out.println("[FedoraServiceManger] pid " + pids.get(i));
				    	attribute = getAObjectAttribute(pids.get(i), search_by);
			    		//System.out.println("[FedoraServiceManager] attribute "+attribute);
			    		//System.out.println("[FedoraServiceManager] search_term "+search_term);
				    	//if (attribute.substring(attribute.indexOf(":")+1).contains(search_term)) {
			    			filteredpids.add(pids.get(i));
							//System.out.println("[FedoraServiceManger] filtered pid " + pids.get(i));
						//} 
					}
					return filteredpids;
				}
			}
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String> getObjectNames(String search_term, String search_by) {
		List<String> filteredPIDs = new ArrayList<String>();
		filteredPIDs = getObjectPIDs(search_term, search_by);
		List<String> filteredNames = new ArrayList<String>();
		String nameSpace = null;
		String objectName = null;
		String fedoraObjectType = null;
		if ((filteredPIDs != null) && (filteredPIDs.size() > 0)) {
			for (int i=0;i<filteredPIDs.size();i++) {
				//System.out.println("[FedoraServiceManager]: objectPID: "+filteredPIDs.get(i));
				objectName = filteredPIDs.get(i);
				fedoraObjectType = getADataStream(filteredPIDs.get(i), "fedoraObjectType");
				//System.out.println("[FedoraServiceManager]: fedoraObjectType: "+fedoraObjectType);
				if (fedoraObjectType != null) {
					if (fedoraObjectType.equals("project")) {
						objectName = getADataStream(filteredPIDs.get(i),"projectName");
					} else if (fedoraObjectType.equals("collection")) {
						objectName = getADataStream(filteredPIDs.get(i),"collectionName");
					} else if (fedoraObjectType.equals("folder")) {
						objectName = getADataStream(filteredPIDs.get(i),"folderName");
					} else if (fedoraObjectType.equals("file")) {
						objectName = getADataStream(filteredPIDs.get(i),"baseFileName");
					}
					nameSpace = filteredPIDs.get(i).substring(0, filteredPIDs.get(i).indexOf(":"));
					objectName = nameSpace+":"+objectName;
				}
				filteredNames.add(objectName);
			}
			
			return filteredNames;
		}
		return null;
	}
	
	/*
	 * Get the pids of objects which created by users.
	 */
	public List<String> getUserDefinedPIDs(String userName) {
		//ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
		//FedoraClient fedoraClient = getFedoraConnection(configurationFileParser.getKinduraParameters().get("FedoraHost"), configurationFileParser.getKinduraParameters().get("FedoraUsername"), configurationFileParser.getKinduraParameters().get("FedoraPassword"));
		FindObjectsResponse response = null;
		List<String> pids = null;
		List<String> filteredpids = new ArrayList<String>();
		//List<String> attribute = null;
		String attribute = null;
		try {
			//System.out.println("[FedoraServiceManager] userName: "+userName);
			response = findObjects().pid().terms("*"+userName+":*").execute(fedoraClient);
			pids = response.getPids();
			if (pids.size() > 0) {
				for (int i=0;i<pids.size();i++) {
			    	//System.out.println("pid " + pids.get(i));
			    	attribute = getADataStream(pids.get(i), "projectName");
			    	//System.out.println("attribute "+attribute);
			   		//System.out.println("search_term "+search_term);
			    	if (attribute != null) {
						filteredpids.add(pids.get(i).substring(pids.get(i).indexOf(":")+1));
						//System.out.println("filtered pid " + pids.get(i));
					} 
				}
				return filteredpids;
			}
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String> getProjectObjectPIDs() {
		FedoraResponse fedoraResponse;
		List<String> projectPIDs = new ArrayList<String>();
		try {
			fedoraResponse = FedoraClient.getRelationships("fedora:root").subject("fedora:root").predicate("fedora:root/isParentOf").execute(fedoraClient);
			Model model = ModelFactory.createDefaultModel();
			model.read(fedoraResponse.getEntityInputStream(), null, FileUtils.langXML);
			
			StmtIterator it = model.listStatements();
			Statement s;
			while (it.hasNext()) {
				s = (Statement) it.next();
//				System.out.println(s.getSubject().toString());
//				System.out.println(s.getPredicate().toString());
//				System.out.println(s.getObject().toString());
				projectPIDs.add(s.getObject().toString());
				//System.out.println();
			}
			return projectPIDs;
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String> getCollectionObjectPIDs(String projectPID) {
		FedoraResponse fedoraResponse;
		List<String> collectionPIDs = new ArrayList<String>();
		try {
			fedoraResponse = FedoraClient.getRelationships(projectPID).subject(projectPID).predicate(projectPID+"/isParentOf").execute(fedoraClient);
			Model model = ModelFactory.createDefaultModel();
			model.read(fedoraResponse.getEntityInputStream(), null, FileUtils.langXML);
			
			StmtIterator it = model.listStatements();
			Statement s;
			while (it.hasNext()) {
				s = (Statement) it.next();
				System.out.println(s.getSubject().toString());
				System.out.println(s.getPredicate().toString());
				System.out.println(s.getObject().toString());
				collectionPIDs.add(s.getObject().toString());
				//System.out.println();
			}
			return collectionPIDs;
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String> getprojectNames() {
		FedoraResponse fedoraResponse;
		List<String> projectNames = new ArrayList<String>();
		String projectName = null;
		try {
			fedoraResponse = FedoraClient.getRelationships("fedora:root").subject("fedora:root").predicate("fedora:root/isParentOf").execute(fedoraClient);
			Model model = ModelFactory.createDefaultModel();
			model.read(fedoraResponse.getEntityInputStream(), null, FileUtils.langXML);
			
			StmtIterator it = model.listStatements();
			Statement s;
			while (it.hasNext()) {
				s = (Statement) it.next();
				System.out.println(s.getSubject().toString());
				System.out.println(s.getPredicate().toString());
				System.out.println(s.getObject().toString());
				projectName = s.getObject().toString().substring(s.getObject().toString().indexOf(":")+1);
				projectNames.add(projectName);
				//System.out.println();
			}
			Collections.sort(projectNames);
			return projectNames;
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * Get a specific attribute of an object which matches the search criteria.
	 */
	//public List<String> etObjectAttribute(FedoraClient fedoraClient, String pid, String search_by) {
	public String getAObjectAttribute(String pid, String search_by) {
		FindObjectsResponse response = null;
		List<String> attribute = null;
		try {
			if (search_by.equals("pid")) {
				response = findObjects().pid().terms(pid).execute(fedoraClient);
			}
			else if (search_by.equals("label")) {
				response = findObjects().pid().label().terms(pid).execute(fedoraClient);
			}
			else if (search_by.equals("description")) {
				response = findObjects().pid().description().terms(pid).execute(fedoraClient);
			}
			else if (search_by.equals("creator")) {
				response = findObjects().pid().creator().terms(pid).execute(fedoraClient);
			}
			else if (search_by.equals("title")) {
				response = findObjects().pid().title().terms(pid).execute(fedoraClient);
			}
			else if (search_by.equals("cDate")) {
				response = findObjects().pid().cDate().terms(pid).execute(fedoraClient);
			}
			else if (search_by.equals("state")) {
				response = findObjects().pid().state().terms(pid).execute(fedoraClient);
			}
			else if (search_by.equals("ownerId")) {
				response = findObjects().pid().ownerId().terms(pid).execute(fedoraClient);
			}
			else if (search_by.equals("date")) {
				response = findObjects().pid().date().terms(pid).execute(fedoraClient);
			}
			else if (search_by.equals("identifier")) {
				response = findObjects().pid().identifier().terms(pid).execute(fedoraClient);
			}
			attribute = response.getObjectField(pid, search_by);
			//System.out.println("attribute size "+attribute.size());
			for (int i=0;i<attribute.size();i++) {
				//System.out.println(pid+"."+search_by+": "+attribute.get(i));
				// TODO why is this empty?
			} 
		}
			catch (FedoraClientException e) {
			// Auto-generated catch block
				e.printStackTrace();
			}
		//return attribute;
		if (attribute.size() > 0) {
			return attribute.get(0);
		}
		else {
			return null;
		}
	}
	
	/*
	 * Get all data streams of an object.
	 */
	public ArrayList<DatastreamType> getDataStreams(String pid) {
		ListDatastreamsResponse response;
		ArrayList<DatastreamType> dsList = null;
		try {
			response = new ListDatastreams(pid).execute(fedoraClient);
			dsList = new ArrayList(response.getDatastreams());
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return dsList;
	}
	
	/*
	 * Get a certain data stream of an object.
	 */
	public String getADataStream(String pid, String dataStreamName) {
		ListDatastreamsResponse response;
		ArrayList <DatastreamType> dsList;
		String datasStreamContent = null;
		try {
			if (isFedoraObjectExisted(pid) == true) {
				response = new ListDatastreams(pid).execute(fedoraClient);
				dsList = new ArrayList(response.getDatastreams());
				if (dsList.size() > 0) {
					for (int i=0;i<dsList.size();i++) {
				    	if (dsList.get(i).getDsid().equals(dataStreamName)) {
				    		datasStreamContent = dsList.get(i).getLabel();
				    		return datasStreamContent;
				    	}
					}
				}
			}
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return datasStreamContent;
	}
	
	/*
	 * Get the content's URL in DuraCloud.
	 */
	public String getContentURL(String nameSpaceAndPid) {
		ListDatastreamsResponse response;
		ArrayList <DatastreamType> dsList;
		String contenturl = null;
		try {
			response = new ListDatastreams(nameSpaceAndPid).execute(fedoraClient);
			dsList = new ArrayList(response.getDatastreams());
			for (int i=0;i<dsList.size();i++) {
		    	if (dsList.get(i).getDsid().equals("url")) {
		    		contenturl = dsList.get(i).getLabel();
		    	}
			}
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return contenturl;
	}
		
	/*
	 * Get the content by using the specified url.
	 */
	public void getContentByURL(String url) {
		URL contenturl;
		try {
			contenturl = new URL(url);
			URLConnection urlconnection = contenturl.openConnection();
	        BufferedReader in = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
	        String inputLine;

	        while ((inputLine = in.readLine()) != null) 
	            //System.out.println(inputLine);
	        in.close();
	        //store.getContent(space,filename);
	    } catch (MalformedURLException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Get the pids of objects which created by users.
	 */
	public List<String> getDownloadPreferences(String fedoraObjectName) {
		//List<String> downloadSequences = new ArrayList<String>();
		
		FedoraResponse fedoraResponse;
		List<String> cloudProviderNames = new ArrayList<String>();
		String projectName = null;
		
		String downloadPreferences = getADataStream(fedoraObjectName, "cloudProviderNames");
		System.out.println("[DownloadRequestHandler]: "+downloadPreferences);
		if (downloadPreferences != null) {
			int nameSeparator = downloadPreferences.indexOf(",");
			while (nameSeparator > 0) {
				cloudProviderNames.add(downloadPreferences.substring(0, nameSeparator));
				downloadPreferences = downloadPreferences.substring(nameSeparator+1);
				nameSeparator = downloadPreferences.indexOf(",");
			}
			cloudProviderNames.add(downloadPreferences);
		}
		return cloudProviderNames;
	}
	
	public boolean isFedoraObjectExisted(String pid) {
		FindObjectsResponse response = null;
		List<String> pids = null;
		System.out.println("[FedoraServiceManger] search pid: "+pid);
		try {
			response = findObjects().pid().terms(pid).execute(fedoraClient);
			pids = response.getPids();
			if (pids.size() > 0) {
				return true;
			}
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void createRootObject(String root) {
		try {
			IngestResponse ingest = new Ingest("fedora:"+root).execute(fedoraClient);
			//String url = "http://localhost:8080/duradmin/";
			//FedoraClient.addDatastream(rootPID, "fedoraObjectType").controlGroup("R").dsLabel("project").dsLocation(url).mimeType("text/xml").execute(fedoraClient);
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createProjectObject(String creator, String root, String projectName, String projectFunder, String projectDescription, String ownerShip, String projectContact, String department, String typeOfData, String startDate, String endDate, String projectCost, String actionRequired, String storageType, String cloudProvider) {
		
		BigDecimal cost;
		cost = BigDecimal.valueOf(Double.parseDouble(projectCost));
		// amounts are rounded up so that very tiny fractions of one penny appear as at
		// least 1p or 1cent.
		cost = cost.setScale(2, BigDecimal.ROUND_UP);

		
		String pid = root+":"+projectName;
		String mimeType = "text/xml";
		String url = "http://" + duraCloudURL + "download/contentItem?spaceId="+creator;
		System.out.println("[FedoraServiceManger] url is " + url);
//		String url = "http://localhost:8080/duradmin/download/contentItem?spaceId="+creator;
		
		//Create a new fedora object if it does not exist in Fedora repository.
		
		try {
			//FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection();
			IngestResponse ingest = new Ingest(pid).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectName").controlGroup("R").dsLabel(projectName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectFunder").controlGroup("R").dsLabel(projectFunder).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectDescription").controlGroup("R").dsLabel(projectDescription).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "ownerShip").controlGroup("R").dsLabel(ownerShip).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectContact").controlGroup("R").dsLabel(projectContact).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "department").controlGroup("R").dsLabel(department).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "typeOfData").controlGroup("R").dsLabel(typeOfData).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "startDate").controlGroup("R").dsLabel(startDate).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "endDate").controlGroup("R").dsLabel(endDate).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "creator").controlGroup("R").dsLabel(creator).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			//FedoraClient.addDatastream(pid, "storageType").controlGroup("R").dsLabel(storageType).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			//FedoraClient.addDatastream(pid, "projectCost").controlGroup("R").dsLabel(cost.toString()).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			//FedoraClient.addDatastream(pid, "actionRequired").controlGroup("R").dsLabel(actionRequired).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "fedoraObjectType").controlGroup("R").dsLabel("project").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			// TODO we need to store the service provider and account details don't we?
			
			FedoraClient.addRelationship(pid).subject(pid).predicate(pid+"/isAChildOf").object("fedora:"+root).execute(fedoraClient);
			FedoraClient.addRelationship("fedora:"+root).subject("fedora:"+root).predicate("fedora:"+root+"/isParentOf").object(pid).execute(fedoraClient);
			
			System.out.println("[FedoraServiceManger] Project "+projectName+" for user "+creator+" created");
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateProjectObject(String creator, String root, String projectName, String projectFunder, String projectDescription, String ownerShip, String projectContact, String department, String typeOfData, String startDate, String endDate, String projectCost, String actionRequired, String storageType, String cloudProvider) {
		
		BigDecimal cost;
		cost = BigDecimal.valueOf(Double.parseDouble(projectCost));
		// amounts are rounded up so that very tiny fractions of one penny appear as at
		// least 1p or 1cent.
		cost = cost.setScale(2, BigDecimal.ROUND_UP);
		
		String pid = root+":"+projectName;
		String mimeType = "text/xml";
		String url = "http://" + duraCloudURL + "/download/contentItem?spaceId="+creator;
//		String url = "http://localhost:8080/duradmin/download/contentItem?spaceId="+creator;
		
		//Create a new fedora object if it does not exist in Fedora repository.
		
		try {
			//FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection();
			FedoraClient.addDatastream(pid, "projectName").controlGroup("R").dsLabel(projectName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectFunder").controlGroup("R").dsLabel(projectFunder).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectDescription").controlGroup("R").dsLabel(projectDescription).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "ownerShip").controlGroup("R").dsLabel(ownerShip).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectContact").controlGroup("R").dsLabel(projectContact).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "department").controlGroup("R").dsLabel(department).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "typeOfData").controlGroup("R").dsLabel(typeOfData).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "startDate").controlGroup("R").dsLabel(startDate).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "endDate").controlGroup("R").dsLabel(endDate).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "creator").controlGroup("R").dsLabel(creator).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			//FedoraClient.addDatastream(pid, "storageType").controlGroup("R").dsLabel(storageType).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			//FedoraClient.addDatastream(pid, "projectCost").controlGroup("R").dsLabel(cost.toString()).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			//FedoraClient.addDatastream(pid, "actionRequired").controlGroup("R").dsLabel(actionRequired).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "fedoraObjectType").controlGroup("R").dsLabel("project").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addRelationship(pid).subject(pid).predicate(pid+"/isAChildOf").object("fedora:"+root).execute(fedoraClient);
			FedoraClient.addRelationship("fedora:"+root).subject("fedora:"+root).predicate("fedora:"+root+"/isParentOf").object(pid).execute(fedoraClient);
			
			System.out.println("[FedoraServiceManger] Project "+projectName+" for user "+creator+" updated");
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createCollectionObject(String collectionName, String root, String projectName, String estimatedaccessFrequency, String collectionDescription, String protectiveMarking, String version, String creator, String storageType, String collectionCost, String actionRequired, String timeStamp, String cloudProviderNames, String collectionCostCurrency, String svcPrvAccountDetails, Double storageUsed) {
		
		String pid = projectName+":"+collectionName;
		String mimeType = "text/xml";
		String url = "http://" + duraCloudURL + "/download/contentItem?spaceId="+creator;
//		String url = "http://localhost:8080/duradmin/download/contentItem?spaceId="+creator;
		
		try {
			//FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection();
			IngestResponse ingest = new Ingest(pid).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectName").controlGroup("R").dsLabel(projectName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionName").controlGroup("R").dsLabel(collectionName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "estimatedaccessFrequency").controlGroup("R").dsLabel(estimatedaccessFrequency).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionDescription").controlGroup("R").dsLabel(collectionDescription).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "protectiveMarking").controlGroup("R").dsLabel(protectiveMarking).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "version").controlGroup("R").dsLabel(version).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "creator").controlGroup("R").dsLabel(creator).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "storageType").controlGroup("R").dsLabel(storageType).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionCost").controlGroup("R").dsLabel(collectionCost).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "actionRequired").controlGroup("R").dsLabel(actionRequired).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "timeStamp").controlGroup("R").dsLabel(timeStamp).dsLocation(url).mimeType(mimeType).execute(fedoraClient);

			FedoraClient.addDatastream(pid, "collectionTotSize").controlGroup("R").dsLabel(String.valueOf(storageUsed)).dsLocation(url).mimeType(mimeType).execute(fedoraClient);

			FedoraClient.addDatastream(pid, "collectionCostCurrency").controlGroup("R").dsLabel(collectionCostCurrency).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "svcPrvAccountDetails").controlGroup("R").dsLabel(svcPrvAccountDetails).dsLocation(url).mimeType(mimeType).execute(fedoraClient);

			FedoraClient.addDatastream(pid, "cloudProviderNames").controlGroup("R").dsLabel(cloudProviderNames).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "fedoraObjectType").controlGroup("R").dsLabel("collection").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addRelationship(pid).subject(pid).predicate(pid+"/isAChildOf").object(root+":"+projectName).execute(fedoraClient);
			FedoraClient.addRelationship(root+":"+projectName).subject(root+":"+projectName).predicate(root+":"+projectName+"/isParentOf").object(pid).execute(fedoraClient);
			
			System.out.println("[FedoraServiceManger] Collection "+collectionName+" for project "+projectName+" created");
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateCollectionObject(String collectionName, String root, String projectName, String estimatedaccessFrequency, String collectionDescription, String protectiveMarking, String version, String creator, String storageType, String collectionCost, String actionRequired, String timeStamp, String cloudProviderNames, String collectionCostCurrency, String svcPrvAccountDetails, Double storageUsed) {
		
		String pid = projectName+":"+collectionName;
		String mimeType = "text/xml";
//		String url = "http://localhost:8080/duradmin/download/contentItem?spaceId="+creator;
		String url = "http://" + duraCloudURL + "/download/contentItem?spaceId="+creator;
		
		try {
			//FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection();
			FedoraClient.addDatastream(pid, "projectName").controlGroup("R").dsLabel(projectName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionName").controlGroup("R").dsLabel(collectionName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "estimatedaccessFrequency").controlGroup("R").dsLabel(estimatedaccessFrequency).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionDescription").controlGroup("R").dsLabel(collectionDescription).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "protectiveMarking").controlGroup("R").dsLabel(protectiveMarking).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "version").controlGroup("R").dsLabel(version).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "creator").controlGroup("R").dsLabel(creator).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "storageType").controlGroup("R").dsLabel(storageType).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionCost").controlGroup("R").dsLabel(collectionCost).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "actionRequired").controlGroup("R").dsLabel(actionRequired).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "timeStamp").controlGroup("R").dsLabel(timeStamp).dsLocation(url).mimeType(mimeType).execute(fedoraClient);

			FedoraClient.addDatastream(pid, "collectionTotSize").controlGroup("R").dsLabel(String.valueOf(storageUsed)).dsLocation(url).mimeType(mimeType).execute(fedoraClient);

			FedoraClient.addDatastream(pid, "collectionCostCurrency").controlGroup("R").dsLabel(collectionCostCurrency).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "svcPrvAccountDetails").controlGroup("R").dsLabel(svcPrvAccountDetails).dsLocation(url).mimeType(mimeType).execute(fedoraClient);

			FedoraClient.addDatastream(pid, "cloudProviderNames").controlGroup("R").dsLabel(cloudProviderNames).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "fedoraObjectType").controlGroup("R").dsLabel("collection").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addRelationship(pid).subject(pid).predicate(pid+"/isAChildOf").object(root+":"+projectName).execute(fedoraClient);
			FedoraClient.addRelationship(root+":"+projectName).subject(root+":"+projectName).predicate(root+":"+projectName+"/isParentOf").object(pid).execute(fedoraClient);
			
			System.out.println("[FedoraServiceManger] Collection "+collectionName+" for project "+projectName+" updated");
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handleCollectionObject(String userName, String projectName, String collectionName, String collectionPID, String estimatedaccessFrequency, String collectionDescription, String protectiveMarking, String version, String timeStamp, String cloudProviderNames, Double cheaperCost, String cheaperCostCurrency, String svcPrvAcctDetails, Double collTotStorageUsed, String opsFlag) {
		//Create a new collection object for the collection if it does not exist in Fedora repository.

		BigDecimal collCost;
		collCost = BigDecimal.valueOf(cheaperCost);
		// amounts are rounded up so that very tiny fractions of one penny appear as at
		// least 1p or 1cent.
		collCost = collCost.setScale(2, BigDecimal.ROUND_UP);
		String collectionCost = String.valueOf(collCost);

		// TODO don't know where this info comes from
		String storageType = "fast";
		// TODO migration is not yet implemented
		String actionRequired = opsFlag;
//		String storageType = generateRandomStorageType();
//		String actionRequired = generateRandomAction();
		
		if ((isFedoraObjectExisted(collectionPID) == true) && (getADataStream(collectionPID, "projectName").equals(projectName))) {
			System.out.println("Collection pid "+collectionPID+" exists");
			//throw new FedoraClientException("Collection name is already existed");
			updateCollectionObject(collectionName, "root", projectName, estimatedaccessFrequency, collectionDescription, protectiveMarking, version, userName, storageType, collectionCost, actionRequired, timeStamp, cloudProviderNames, cheaperCostCurrency, svcPrvAcctDetails, collTotStorageUsed);
			
		} else {
			System.out.println("Collection pid "+collectionPID+" does NOT exist");
			//IngestResponse ingest = new Ingest(collectionPID).execute(fedoraClient);
			
			createCollectionObject(collectionName, "root", projectName, estimatedaccessFrequency, collectionDescription, protectiveMarking, version, userName, storageType, collectionCost, actionRequired, timeStamp, cloudProviderNames, cheaperCostCurrency, svcPrvAcctDetails, collTotStorageUsed);
			//FedoraClient.addDatastream(nameSpaceAndPid, baseFilename+".cost").controlGroup("R").dsLabel(cost.toString()).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
		} 
	}
	
	public String createFolderObject(String projectName, String collectionName, String parentFolderName, String folderName, String parentPID) {
		String pid = null;
		if (parentFolderName == "") {
			pid = collectionName+":"+folderName;
		} else {
			pid = parentFolderName+":"+folderName;
		}
			
		String mimeType = "text/xml";
		String url = "http://" + duraCloudURL + "/download/contentItem?spaceId="+folderName;
//		String url = "http://localhost:8080/duradmin/download/contentItem?spaceId="+folderName;
		
		try {
			int count = 1;
			if (isFedoraObjectExisted(pid) == true) {
				while (isFedoraObjectExisted(pid+count) == true) {
					count++;
				}
				pid = pid+count;
			}
			IngestResponse response = new Ingest(pid).execute(fedoraClient);
			System.out.println("Generated PID " + pid + " Response PID: " + response.getPid());
			
			FedoraClient.addDatastream(pid, "projectName").controlGroup("R").dsLabel(projectName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionName").controlGroup("R").dsLabel(collectionName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "folderName").controlGroup("R").dsLabel(folderName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "fedoraObjectType").controlGroup("R").dsLabel("folder").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addRelationship(pid).subject(pid).predicate(pid+"/isAChildOf").object(parentPID).execute(fedoraClient);
			FedoraClient.addRelationship(parentPID).subject(parentPID).predicate(parentPID+"/isParentOf").object(pid).execute(fedoraClient);
			
			System.out.println("[FedoraServiceManger] Folder "+folderName+" for collection "+collectionName+" created");
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return pid;
	}
	
	//public void updateFolderObject(String projectName, String collectionName, String parentFolderName, String folderName, String parentPID, String folderPID) {
	public void updateFolderObject(String projectName, String collectionName, String parentFolderName, String folderName, String parentPID) {
			
		String pid = null;
		if (parentFolderName == "") {
			pid = collectionName+":"+folderName;
		} else {
			pid = parentFolderName+":"+folderName;
		}
		//String pid = folderPID;
			
		String mimeType = "text/xml";
		String url = "http://" + duraCloudURL + "/download/contentItem?spaceId="+folderName;
//		String url = "http://localhost:8080/duradmin/download/contentItem?spaceId="+folderName;
		
		try {
			FedoraClient.addDatastream(pid, "projectName").controlGroup("R").dsLabel(projectName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionName").controlGroup("R").dsLabel(collectionName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "folderName").controlGroup("R").dsLabel(folderName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "fedoraObjectType").controlGroup("R").dsLabel("folder").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addRelationship(pid).subject(pid).predicate(pid+"/isAChildOf").object(parentPID).execute(fedoraClient);
			FedoraClient.addRelationship(parentPID).subject(parentPID).predicate(parentPID+"/isParentOf").object(pid).execute(fedoraClient);
			
			System.out.println("[FedoraServiceManger] Folder "+folderName+" for collection "+collectionName+" updated");
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public HashMap<String, String> handleFolderObject(String projectName, String collectionName, String collectionPID, String fileName, String fileOriginalPath) {
		//Create a new object for the folder if it does not exist in Fedora repository.
		String filePathOfFedora = reviseFilePathForFedora(fileOriginalPath);
		String tempFileOriginalPath = filePathOfFedora;
		String parentFolderName = "";
		String parentPID = collectionPID;
		//String folderName = null;
		String folderName = tempFileOriginalPath;
		String folderPID = null;
		HashMap<String, String> parentFolderNameAndPID = new HashMap<String, String>();
		while (tempFileOriginalPath.contains(".")) {
			folderName = tempFileOriginalPath.substring(0, tempFileOriginalPath.indexOf("."));
			System.out.println("folderName: "+folderName);
			if (parentFolderName == "") {
				folderPID = collectionName+":"+folderName;
			} else {
				folderPID = parentFolderName+":"+folderName;
			}
			System.out.println("!!!!!collectionName: "+collectionName);
			System.out.println("!!!!!folderPID: "+folderPID);
			System.out.println("!!!!!datastreamName: "+getADataStream(folderPID, "collectionName"));
			
			/*List<String> comparisonFolderPIDList = getObjectPIDs(folderPID,"pid");
			Iterator iterator = comparisonFolderPIDList.iterator();
			String comparisonFolderPID = null;
			boolean isFolderPIDFound = false;
			while ((iterator.hasNext()) && (isFolderPIDFound == false)) {
				comparisonFolderPID = (String) iterator.next();
				if (getADataStream(comparisonFolderPID, "collectionName").equals(collectionName) && getADataStream(comparisonFolderPID, "projectName").equals(projectName)) {
					updateFolderObject(projectName, collectionName, parentFolderName, folderName, parentPID, comparisonFolderPID);
					isFolderPIDFound = true;
				}
			}
			if (isFolderPIDFound == false) {
				folderPID = createFolderObject(projectName, collectionName, parentFolderName, folderName, parentPID);
			}*/
			
			boolean checkNextFolderPID = true;
			boolean isFolderObjectUpdated = false;
			String tempFolderPID = folderPID;
			String tempFolderName = folderName;
			String tempParentPID = parentPID;
			int count = 0;
			do {
				System.out.println("isFedoraObjectExisted for tempFolderPID is " + isFedoraObjectExisted(tempFolderPID));
				if (isFedoraObjectExisted(tempFolderPID) == true) {
// TODO problems with nulls here
					System.out.println("getADataStream for tempFolderPID " + collectionName + "is " + getADataStream(tempFolderPID, "collectionName"));
					System.out.println("getADataStream for tempFolderPID " + projectName + "is " + getADataStream(tempFolderPID, "projectName"));
					if (getADataStream(tempFolderPID, "collectionName") == null) {
						// maybe this is the end condition?
						checkNextFolderPID = false;
						isFolderObjectUpdated = false;				
					} else if (getADataStream(tempFolderPID, "projectName") == null) {
						// maybe this is the end condition?
						checkNextFolderPID = false;
						isFolderObjectUpdated = false;				
					} else if (getADataStream(tempFolderPID, "collectionName").equals(collectionName) && getADataStream(tempFolderPID, "projectName").equals(projectName)) {
						//updateFolderObject(projectName, collectionName, parentFolderName, tempFolderName, tempParentPID);
						checkNextFolderPID = false;
						isFolderObjectUpdated = true;
					}
				} else {
					checkNextFolderPID = false;
				}
				if (checkNextFolderPID == true) {
					count++;
					tempFolderPID = folderPID+count;
					tempFolderName = folderName+count;
				}
			} while (checkNextFolderPID == true);
			if (isFolderObjectUpdated == false) {
				folderPID = createFolderObject(projectName, collectionName, parentFolderName, folderName, parentPID);
			} else {
				folderPID = tempFolderPID;
			}
			
			/*if ((isFedoraObjectExisted(folderPID) == true) && getADataStream(folderPID, "collectionName").equals(collectionName) && getADataStream(folderPID, "projectName").equals(projectName)) {
				updateFolderObject(projectName, collectionName, parentFolderName, folderName, parentPID);
			} else {
				folderPID = createFolderObject(projectName, collectionName, parentFolderName, folderName, parentPID);
			}*/
			
			tempFileOriginalPath = tempFileOriginalPath.substring(tempFileOriginalPath.indexOf(".")+1);
			parentFolderName = folderName;
			parentPID = folderPID;
		}
		System.out.println("[UploadRequestHandler] tempFileOriginalPath1: "+tempFileOriginalPath);
		System.out.println("[UploadRequestHandler] parentFolderName1: "+parentFolderName);
		System.out.println("[UploadRequestHandler] parentPID1: "+parentPID);
		System.out.println("[UploadRequestHandler] fileName1: "+fileName);
		folderName = tempFileOriginalPath;
		if (parentFolderName == "") {
			folderPID = collectionName+":"+folderName;
		} else {
			folderPID = parentFolderName+":"+folderName;
		}
		
		boolean checkNextFolderPID = true;
		boolean isFolderObjectUpdated = false;
		String tempFolderPID = folderPID;
		String tempFolderName = folderName;
		String tempParentPID = parentPID;
		int count = 0;
		do {
			if (isFedoraObjectExisted(tempFolderPID) == true) {
				if (getADataStream(tempFolderPID, "collectionName") == null) {
					// maybe this is the end condition?
					checkNextFolderPID = false;
					isFolderObjectUpdated = false;				
				} else if (getADataStream(tempFolderPID, "projectName") == null) {
					// maybe this is the end condition?
					checkNextFolderPID = false;
					isFolderObjectUpdated = false;				
				} else if (getADataStream(tempFolderPID, "collectionName").equals(collectionName) && getADataStream(tempFolderPID, "projectName").equals(projectName)) {
					//updateFolderObject(projectName, collectionName, parentFolderName, tempFolderName, tempParentPID);
					checkNextFolderPID = false;
					isFolderObjectUpdated = true;
				}
			} else {
				checkNextFolderPID = false;
			}
			if (checkNextFolderPID == true) {
				count++;
				tempFolderPID = folderPID+count;
				tempFolderName = folderName+count;
			}
		} while (checkNextFolderPID == true);
		if (isFolderObjectUpdated == false) {
			folderPID = createFolderObject(projectName, collectionName, parentFolderName, folderName, parentPID);
		} else {
			folderPID = tempFolderPID;
		}
		
		/*if ((isFedoraObjectExisted(folderPID) == true) && getADataStream(folderPID, "collectionName").equals(collectionName) && getADataStream(folderPID, "projectName").equals(projectName)) {
			updateFolderObject(projectName, collectionName, parentFolderName, folderName, parentPID);
		} else {
			folderPID = createFolderObject(projectName, collectionName, parentFolderName, folderName, parentPID);
		}*/
		
		parentFolderName = folderName;
		parentPID = folderPID;
		
		System.out.println("[UploadRequestHandler] tempFileOriginalPath2: "+tempFileOriginalPath);
		System.out.println("[UploadRequestHandler] parentFolderName2: "+parentFolderName);
		System.out.println("[UploadRequestHandler] parentPID2: "+parentPID);
		System.out.println("[UploadRequestHandler] fileName2: "+fileName);
		
		parentFolderNameAndPID.put(parentFolderName, parentPID);
		return parentFolderNameAndPID;
	}
	
	public void createFileObject(String nameSpace, String projectName, String collectionName, String fileOriginalPath, String parentFolderName, String parentPID, String baseFileName, String fileExtension, String fileSize) {
		String pid = parentFolderName+":"+baseFileName;
		String mimeType = "text/xml";
		String url = "http://" + duraCloudURL + "/download/contentItem?spaceId="+nameSpace+"&contentId="+projectName+"/"+collectionName+"/"+fileOriginalPath+"/"+baseFileName+"/"+fileExtension+"&storeID=0&attachment=true";
//		String url = "http://localhost:8080/duradmin/download/contentItem?spaceId="+nameSpace+"&contentId="+projectName+"/"+collectionName+"/"+fileOriginalPath+"/"+baseFileName+"/"+fileExtension+"&storeID=0&attachment=true";
		
		try {
			int count = 1;
			if (isFedoraObjectExisted(pid) == true) {
				while (isFedoraObjectExisted(pid+count) == true) {
					count++;
				}
				pid = pid+count;
			}
			IngestResponse response = new Ingest(pid).execute(fedoraClient);
			System.out.println("Generated PID " + pid + " Response PID: " + response.getPid());
			
			//IngestResponse ingest = new Ingest(fedoraClient.getNextPID().namespace(projectName).numPIDs(1)).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectName").controlGroup("R").dsLabel(projectName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionName").controlGroup("R").dsLabel(collectionName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "folderName").controlGroup("R").dsLabel(parentFolderName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "baseFileName").controlGroup("R").dsLabel(baseFileName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "fileExtension").controlGroup("R").dsLabel(fileExtension).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "filePath").controlGroup("R").dsLabel(fileOriginalPath).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "fileSize").controlGroup("R").dsLabel(fileSize).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "fedoraObjectType").controlGroup("R").dsLabel("file").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addRelationship(pid).subject(pid).predicate(pid+"/isAChildOf").object(parentPID).execute(fedoraClient);
			FedoraClient.addRelationship(parentPID).subject(parentPID).predicate(parentPID+"/isParentOf").object(pid).execute(fedoraClient);
			
			
			System.out.println("[FedoraServiceManger] file "+baseFileName+" for collection "+collectionName+" created");
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*FedoraClient.addDatastream(nameSpaceAndPid, collectionName+"."+filePathOfFedora+"."+baseFilename+".url").controlGroup("R").dsLabel(fileUrl).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
	FedoraClient.addDatastream(nameSpaceAndPid, collectionName+"."+filePathOfFedora+"."+baseFilename+".fileNameExtension").controlGroup("R").dsLabel(fileNameExtension).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
	FedoraClient.addDatastream(nameSpaceAndPid, collectionName+"."+filePathOfFedora+"."+baseFilename+".filePath").controlGroup("R").dsLabel(fileOriginalPath).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
	FedoraClient.addDatastream(nameSpaceAndPid, collectionName+"."+filePathOfFedora+"."+baseFilename+".fileSize").controlGroup("R").dsLabel(String.valueOf(fileSize)).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
	*/
	public void updateFileObject(String nameSpace, String projectName, String collectionName, String fileOriginalPath, String parentFolderName, String parentPID, String filePID, String baseFileName, String fileExtension, String fileSize) {
		
		String pid = filePID;
		String mimeType = "text/xml";
		String url = "http://" + duraCloudURL + "/download/contentItem?spaceId="+nameSpace+"&contentId="+projectName+"/"+collectionName+"/"+fileOriginalPath+"/"+baseFileName+"/"+fileExtension+"&storeID=0&attachment=true";
//		String url = "http://localhost:8080/duradmin/download/contentItem?spaceId="+nameSpace+"&contentId="+projectName+"/"+collectionName+"/"+fileOriginalPath+"/"+baseFileName+"/"+fileExtension+"&storeID=0&attachment=true";
		
		try {
			//IngestResponse ingest = new Ingest(fedoraClient.getNextPID().namespace(projectName).numPIDs(1)).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectName").controlGroup("R").dsLabel(projectName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionName").controlGroup("R").dsLabel(collectionName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "folderName").controlGroup("R").dsLabel(parentFolderName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "baseFileName").controlGroup("R").dsLabel(baseFileName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "fileExtension").controlGroup("R").dsLabel(fileExtension).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "filePath").controlGroup("R").dsLabel(fileOriginalPath).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "fileSize").controlGroup("R").dsLabel(fileSize).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "fedoraObjectType").controlGroup("R").dsLabel("file").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addRelationship(pid).subject(pid).predicate(pid+"/isAChildOf").object(parentPID).execute(fedoraClient);
			FedoraClient.addRelationship(parentPID).subject(parentPID).predicate(parentPID+"/isParentOf").object(pid).execute(fedoraClient);
			
			
			System.out.println("[FedoraServiceManger] file "+baseFileName+" for collection "+collectionName+" updated");
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handleFileObject(String nameSpace, String projectName, String collectionName, String parentFolderName, String parentFolderPID, String baseFileName, String fileName, String filePID, String fileOriginalPath, String fileNameExtension, String fileSize) {
		List<String> tempObjects = getObjectPIDs(parentFolderName+":", "pid");
		String tempFilePath = null;
		String tempFilePID = null;
		String tempFileName = null;
		boolean isFileExisted = false;
		if (tempObjects != null) {
			
			Iterator iterator = tempObjects.iterator();
			while (iterator.hasNext() && (isFileExisted == false)) {
				tempFilePID = iterator.next().toString();
				System.out.println("[FedoraServiceManager] !!!!!tempFilePID: "+tempFilePID);
				tempFileName = getADataStream(tempFilePID, "baseFileName");
				System.out.println("!!!!! tempFileName: "+tempFileName);
				System.out.println("!!!!! baseFileName: "+baseFileName);
				if ((tempFileName != null) && (tempFileName.equals(baseFileName))) {
					tempFilePath = getADataStream(tempFilePID, "filePath");
					System.out.println("[FedoraServiceManager] tempFilePath: "+tempFilePath);
					System.out.println("[FedoraServiceManager] filePath: "+fileOriginalPath);
					if ((tempFilePath != null) && (tempFilePath.equals(fileOriginalPath)) && (getADataStream(tempFilePID, "collectionName").equals(collectionName)) && getADataStream(tempFilePID, "projectName").equals(projectName)) {
						isFileExisted = true;
					}
				}
			}
		}
		if (isFileExisted == false) {
			System.out.println("[FedoraServiceManager] file "+fileName+" does NOT exist");
			//fedoraServiceManager.createFileObject(nameSpace, projectName, collectionName, fileOriginalPath, filePathOfFedora, baseFilename, fileNameExtension, String.valueOf(fileSize));
			createFileObject(nameSpace, projectName, collectionName, fileOriginalPath, parentFolderName, parentFolderPID, baseFileName, fileNameExtension, String.valueOf(fileSize));
		} else {
			filePID = tempFilePID;
			System.out.println("[FedoraServiceManager] file "+fileName+" exists");
			updateFileObject(nameSpace, projectName, collectionName, fileOriginalPath, parentFolderName, parentFolderPID, filePID, baseFileName, fileNameExtension, String.valueOf(fileSize));
		}
	}
	
	public void updateFileURL(String collectionName, String originalFileName, String convertedFileName){
		
	}
	
	public void createFedoraObject(String userName, String projectName, String projectDescription, String storageType, String actionRequired, String svcPrvAccountDetails) {
		
		// TODO this method doesn't seem to be referenced from anywhere
		// so i won't assign a cost to it
		Double cheaperCost = 0.0;
		BigDecimal cost;
		cost = BigDecimal.valueOf(cheaperCost);
		// amounts are rounded up so that very tiny fractions of one penny appear as at
		// least 1p or 1cent.
		cost = cost.setScale(2, BigDecimal.ROUND_UP);
		
		String pid = userName+":"+projectName;
		String mimeType = "text/xml";
		String url = "http://" + duraCloudURL + "/download/contentItem?spaceId="+userName;
//		String url = "http://localhost:8080/duradmin/download/contentItem?spaceId="+userName;
		
		//Create a new fedora object if it does not exist in Fedora repository.
		
		try {
			//FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection();
			IngestResponse ingest = new Ingest(pid).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectName").controlGroup("R").dsLabel(projectName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectcost").controlGroup("R").dsLabel(cost.toString()).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "creator").controlGroup("R").dsLabel(userName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectName").controlGroup("R").dsLabel(projectName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionDescription").controlGroup("R").dsLabel(projectDescription).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "storageType").controlGroup("R").dsLabel(storageType).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionCost").controlGroup("R").dsLabel(cost.toString()).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "actionRequired").controlGroup("R").dsLabel(actionRequired).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "cloudProvider").controlGroup("R").dsLabel(svcPrvAccountDetails).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			System.out.println("[AddProjectRequesthandler] Project "+projectName+" for user "+userName+" created");
			
//			double cloudProviderChooser = Math.random();
//			if (cloudProviderChooser < 0.25) {
//				FedoraClient.addDatastream(pid, "cloudProvider").controlGroup("R").dsLabel("iRODs").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
//			} else if (cloudProviderChooser < 50) {
//				FedoraClient.addDatastream(pid, "cloudProvider").controlGroup("R").dsLabel("Amazon S3").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
//			} else if (cloudProviderChooser < 75) {
//				FedoraClient.addDatastream(pid, "cloudProvider").controlGroup("R").dsLabel("RackSpace").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
//			} else if (cloudProviderChooser < 75) {
//				FedoraClient.addDatastream(pid, "cloudProvider").controlGroup("R").dsLabel("Microsoft Azure").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
//			}
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public TreeMap<String, String> getChildPIDs(String pid) {
		TreeMap<String, String> childPIDs = new TreeMap<String, String>();
		FedoraResponse fedoraResponse = null;
		String childPID = null;
		String fedoraObjectType = null;
		try {
			fedoraResponse = FedoraClient.getRelationships(pid).subject(pid).predicate(pid+"/isParentOf").execute(fedoraClient);
			Model model = ModelFactory.createDefaultModel();
			model.read(fedoraResponse.getEntityInputStream(), null, FileUtils.langXML);
			
			StmtIterator it = model.listStatements();
			Statement s;
			while (it.hasNext()) {
				s = (Statement) it.next();
				//System.out.println(s.getSubject().toString());
				//System.out.println(s.getPredicate().toString());
				childPID = s.getObject().toString();
				System.out.println("[FedoraServiceManager]: childPID: "+childPID);
				fedoraObjectType = getADataStream(childPID, "fedoraObjectType");
				System.out.println("[FedoraServiceManager]: fedoraObjectType: "+fedoraObjectType);
				childPIDs.put(childPID, fedoraObjectType);
			}
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return childPIDs;
	}
	
	public HashMap<String, String> getChildNames(String pid) {
		HashMap<String, String> childNames = new HashMap<String, String>();
		FedoraResponse fedoraResponse = null;
		String childPID = null;
		String childName = null;
		String fedoraObjectType = null;
		try {
			fedoraResponse = FedoraClient.getRelationships(pid).subject(pid).predicate(pid+"/isParentOf").execute(fedoraClient);
			Model model = ModelFactory.createDefaultModel();
			model.read(fedoraResponse.getEntityInputStream(), null, FileUtils.langXML);
			
			StmtIterator it = model.listStatements();
			Statement s;
			while (it.hasNext()) {
				s = (Statement) it.next();
				//System.out.println(s.getSubject().toString());
				//System.out.println(s.getPredicate().toString());
				childPID = s.getObject().toString();
				System.out.println("[FedoraServiceManager]: childPID: "+childPID);
				fedoraObjectType = getADataStream(childPID, "fedoraObjectType");
				System.out.println("[FedoraServiceManager]: fedoraObjectType: "+fedoraObjectType);
				if (fedoraObjectType.equals("project")) {
					childName = getADataStream(childPID,"projectName");
				} else if (fedoraObjectType.equals("collection")) {
					childName = getADataStream(childPID,"collectionName");
				} else if (fedoraObjectType.equals("folder")) {
					childName = getADataStream(childPID,"folderName");
				} else if (fedoraObjectType.equals("file")) {
					childName = getADataStream(childPID,"baseFileName");
				}
				childName = childPID.substring(0,childPID.indexOf(":"))+":"+childName;
				childNames.put(childName, fedoraObjectType);
			}
		} catch (FedoraClientException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return childNames;
	}
	
	public String reviseFilePathForFedora(String fileOriginalPath) {
		String revisedFilePath = fileOriginalPath;
		if (revisedFilePath.endsWith("\\")) {
			revisedFilePath = revisedFilePath.substring(0, revisedFilePath.length()-1);
		}
		revisedFilePath = revisedFilePath.replace(":", "");
		System.out.println("[UploadRequestHandler] revised file path 1: "+revisedFilePath);
		revisedFilePath = revisedFilePath.replace("\\", ".");
		System.out.println("[UploadRequestHandler] revised file path 2: "+revisedFilePath);
		revisedFilePath = revisedFilePath.replace("/", ".");
		System.out.println("[UploadRequestHandler] revised file path 3: "+revisedFilePath);
		revisedFilePath = revisedFilePath.replace(" ", "");
		System.out.println("[UploadRequestHandler] revised file path 4: "+revisedFilePath);
		revisedFilePath = revisedFilePath.toLowerCase();
		System.out.println("[UploadRequestHandler] revised file path 5: "+revisedFilePath);
		return revisedFilePath;
	}
	
	private BigDecimal generateRandomCost() {
		//Generate a random cost value between 0 and 100.
		BigDecimal cost = new BigDecimal(Math.random());
		BigDecimal factor = new BigDecimal(100);
		cost = cost.multiply(factor);
		// amounts are rounded up so that very tiny fractions of one penny appear as at
		// least 1p or 1cent.
		cost = cost.setScale(2, BigDecimal.ROUND_UP);
		return cost;
	}
	
	private String generateRandomStorageType() {
		//Generate a random cost value between 0 and 100.
		BigDecimal cost = generateRandomCost();
		
		String storageType = null;
		//Generate a random storage type based on the cost.
		if (Double.valueOf(cost.toString()) > 66.6) {
			storageType = "Fast";
		} else if (Double.valueOf(cost.toString()) > 33.3) {
			storageType = "Medium";
		} else {
			storageType = "Slow";
		}
		return storageType;
	}
	
	private String generateRandomAction() {
		BigDecimal cost = generateRandomCost();
		String action = null;
		//Generate a random action.
		double actionValue = Math.random();
		if (Double.valueOf(cost.toString()) > 0.5) {
			action = "Migrate";
		} else {
			action = "None";
		}
		return action;
	}
}

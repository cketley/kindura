package org.kindura;

import static com.yourmediashelf.fedora.client.FedoraClient.findObjects;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import com.yourmediashelf.fedora.client.request.Ingest;
import com.yourmediashelf.fedora.client.request.ListDatastreams;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fedoraClient;
    }*/
	
	public FedoraClient getFedoraConnection() {
		return fedoraClient;
	}
	
	public FedoraServiceManager() {
		ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
		
		try {
			fedoracredential = new FedoraCredentials(configurationFileParser.getKinduraParameters().get("FedoraHost"), configurationFileParser.getKinduraParameters().get("FedoraUsername"), configurationFileParser.getKinduraParameters().get("FedoraPassword"));
			fedoraClient = new FedoraClient(fedoracredential);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			// TODO Auto-generated catch block
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
		try {
			response = findObjects().pid().terms("*"+search_term+"*").execute(fedoraClient);
			pids = response.getPids();
			if (search_by.equals("all")) {
				return pids;
			}
			else {
				if (pids.size() > 0) {
					for (int i=0;i<pids.size();i++) {
				    	//System.out.println("pid " + pids.get(i));
				    	attribute = getAObjectAttribute(pids.get(i), search_by);
			    		//System.out.println("attribute "+attribute);
			    		//System.out.println("search_term "+search_term);
				    	if (attribute.toString().contains(search_term)) {
							filteredpids.add(pids.get(i));
							//System.out.println("filtered pid " + pids.get(i));
						} 
					}
					return filteredpids;
				}
			}
		} catch (FedoraClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			    	attribute = getADataStream(pids.get(i), "projectname");
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
			// TODO Auto-generated catch block
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
			} 
		}
			catch (FedoraClientException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
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
		String contentaction = null;
		try {
			response = new ListDatastreams(pid).execute(fedoraClient);
			dsList = new ArrayList(response.getDatastreams());
			for (int i=0;i<dsList.size();i++) {
		    	if (dsList.get(i).getDsid().equals(dataStreamName)) {
		    		contentaction = dsList.get(i).getLabel();
		    		return contentaction;
		    	}
			}
		} catch (FedoraClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return contentaction;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return contenturl;
	}
		
	/*public String composeURL(String space, String filename) {
		String url = null;
		//url = "http://localhost:8080/durastore/"+space+"/"+filename+"?storeID=0";
		url = "http://localhost:8080/duradmin/#0/"+space+"/"+filename;
		url = "http://localhost:8080/duradmin/download/contentItem?spaceId="+space+"&contentId="+filename+"&storeID=0&attachment=true";
		return url;
	}*/
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

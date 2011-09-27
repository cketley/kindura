package org.kindura;

import static com.yourmediashelf.fedora.client.FedoraClient.findObjects;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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
    
	/*
	 * Get a connection to the Fedora repository.
	 */
	public FedoraClient getFedoraConnection(String hosturl, String username, String password) {
    	//Setup connection with Fedora repository.
		FedoraCredentials fedoracredential;
		FedoraClient fedora = null;
		try {
			fedoracredential = new FedoraCredentials(hosturl, username, password);
			fedora = new FedoraClient(fedoracredential);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fedora;
    }
    
	/*
	 * Ingest a new object to the Fedora repository.
	 */
	public boolean ingestObject(FedoraClient fedora, String namespaceandpid, String title, String datastreamid, String directorypath, String url, String mimetype) {
		IngestResponse response;
		try {
			response = new Ingest(namespaceandpid).label(title).execute(fedora); //Create a new Fedora object.
			fedora.addDatastream(namespaceandpid, datastreamid).controlGroup("R").dsLabel(url).dsLocation(url).mimeType(mimetype).execute(fedora);
		} catch (FedoraClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	/*
	 * Get the pids of objects which matches search criteria.
	 */
	public List<String> getObjectPIDs(FedoraClient fedora, String search_term, String search_by) {
		FindObjectsResponse response = null;
		List<String> pids = null;
		List<String> filteredpids = new ArrayList<String>();
		List<String> attribute = null;
		try {
			response = findObjects().pid().terms("*"+search_term+"*").execute(fedora);
			pids = response.getPids();
			if (search_by.equals("all")) {
				return pids;
			}
			else {
				if (pids.size() > 0) {
					for (int i=0;i<pids.size();i++) {
				    	System.out.println("pid " + pids.get(i));
				    	attribute = getObjectAttribute(fedora, pids.get(i), search_by);
			    		//System.out.println("attribute "+attribute);
			    		//System.out.println("search_term "+search_term);
				    	if (attribute.toString().contains(search_term)) {
							filteredpids.add(pids.get(i));
							System.out.println("filtered pid " + pids.get(i));
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
	 * Get a certain attribute of an object which mataches search criteria.
	 */
	public List<String> getObjectAttribute(FedoraClient fedora, String pid, String search_by) {
		FindObjectsResponse response = null;
		List<String> attribute = null;
		try {
			if (search_by.equals("pid")) {
				response = findObjects().pid().terms(pid).execute(fedora);
			}
			else if (search_by.equals("label")) {
				response = findObjects().pid().label().terms(pid).execute(fedora);
			}
			else if (search_by.equals("description")) {
				response = findObjects().pid().description().terms(pid).execute(fedora);
			}
			else if (search_by.equals("creator")) {
				response = findObjects().pid().creator().terms(pid).execute(fedora);
			}
			else if (search_by.equals("title")) {
				response = findObjects().pid().title().terms(pid).execute(fedora);
			}
			else if (search_by.equals("cDate")) {
				response = findObjects().pid().cDate().terms(pid).execute(fedora);
			}
			else if (search_by.equals("state")) {
				response = findObjects().pid().state().terms(pid).execute(fedora);
			}
			else if (search_by.equals("ownerId")) {
				response = findObjects().pid().ownerId().terms(pid).execute(fedora);
			}
			else if (search_by.equals("date")) {
				response = findObjects().pid().date().terms(pid).execute(fedora);
			}
			else if (search_by.equals("identifier")) {
				response = findObjects().pid().identifier().terms(pid).execute(fedora);
			}
			attribute = response.getObjectField(pid, search_by);
			System.out.println("attribute size "+attribute.size());
			for (int i=0;i<attribute.size();i++) {
				System.out.println(pid+"."+search_by+": "+attribute.get(i));
			} 
		}
			catch (FedoraClientException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return attribute;
	}
	
	/*
	 * Get all data streams of an object.
	 */
	public ArrayList<DatastreamType> getDataStreams(FedoraClient fedora, String namespaceandpid) {
		ListDatastreamsResponse response;
		ArrayList<DatastreamType> dsList = null;
		try {
			response = new ListDatastreams(namespaceandpid).execute(fedora);
			dsList = new ArrayList(response.getDatastreams());
		} catch (FedoraClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dsList;
	}
	
	/*
	 * Get the content's URL in DuraCloud.
	 */
	public String getContentURL(FedoraClient fedora, String namespaceandid) {
		ListDatastreamsResponse response;
		ArrayList <DatastreamType> dsList;
		String contenturl = null;
		try {
			response = new ListDatastreams(namespaceandid).execute(fedora);
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
	
	/*
	 * Get the MIME type attribute of an object.
	 */
	public String getMIMEType(FedoraClient fedora, String namespaceandid) {
		ListDatastreamsResponse response;
		ArrayList <DatastreamType> dsList;
		String contenttype = null;
		try {
			response = new ListDatastreams(namespaceandid).execute(fedora);
			dsList = new ArrayList(response.getDatastreams());
			for (int i=0;i<dsList.size();i++) {
		    	if (dsList.get(i).getDsid().equals("url")) {
		    		contenttype = dsList.get(i).getMimeType();
		    	}
			}
		} catch (FedoraClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return contenttype;
	}
	
	/*
	 * Get the file extension attribute of an object.
	 */
	public String getFileExtension(FedoraClient fedora, String namespaceandid) {
		ListDatastreamsResponse response;
		ArrayList <DatastreamType> dsList;
		String fileformat = null;
		try {
			response = new ListDatastreams(namespaceandid).execute(fedora);
			dsList = new ArrayList(response.getDatastreams());
			for (int i=0;i<dsList.size();i++) {
		    	if (dsList.get(i).getDsid().equals("fileformat")) {
		    		fileformat = dsList.get(i).getLabel();
		    	}
			}
		} catch (FedoraClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileformat;
	}
	
	/*
	 * Get the cost attribute of an object.
	 */
	public String getContentCost(FedoraClient fedora, String namespaceandid) {
		ListDatastreamsResponse response;
		ArrayList <DatastreamType> dsList;
		String contentcost = null;
		try {
			response = new ListDatastreams(namespaceandid).execute(fedora);
			dsList = new ArrayList(response.getDatastreams());
			for (int i=0;i<dsList.size();i++) {
		    	if (dsList.get(i).getDsid().equals("cost")) {
		    		contentcost = dsList.get(i).getLabel();
		    	}
			}
		} catch (FedoraClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return contentcost;
	}

	/*
	 * Get the action attribute of an object.
	 */
	public String getContentAction(FedoraClient fedora, String namespaceandid) {
		ListDatastreamsResponse response;
		ArrayList <DatastreamType> dsList;
		String contentaction = null;
		try {
			response = new ListDatastreams(namespaceandid).execute(fedora);
			dsList = new ArrayList(response.getDatastreams());
			for (int i=0;i<dsList.size();i++) {
		    	if (dsList.get(i).getDsid().equals("action")) {
		    		contentaction = dsList.get(i).getLabel();
		    	}
			}
		} catch (FedoraClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return contentaction;
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
	            System.out.println(inputLine);
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

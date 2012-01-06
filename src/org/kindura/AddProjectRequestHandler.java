package org.kindura;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.request.Ingest;
import com.yourmediashelf.fedora.client.response.IngestResponse;

public class AddProjectRequestHandler extends HttpServlet {
	FedoraServiceManager fedoraServiceManager;
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userName = request.getParameter("username");
		String projectName = request.getParameter("projectname");
		String role = request.getParameter("role");
		String projectDescription = request.getParameter("projectdescription");
		String storageType = null;
		//Generate a random storage type based on the cost.
		double cost = Math.random();
		if (cost > 0.66) {
			storageType = "Fast";
		} else if (cost > 0.33) {
			storageType = "Medium";
		} else {
			storageType = "Slow";
		}
		String actionRequired = null;
		double migration = Math.random();
		if (migration > 0.5) {
			actionRequired = "Migrate";
		} else {
			actionRequired = "None";
		}
		System.out.println("[AddProjectRequesthandler] User Name: "+userName);
		System.out.println("[AddProjectRequesthandler] Project Name: "+projectName);
		HttpSession session = request.getSession(true);
		session.setAttribute("username", userName);
		session.setAttribute("projectname", projectName);
		session.setAttribute("role", role);
		
		//Setup connection with Fedora repository.
		fedoraServiceManager = new FedoraServiceManager();
		//FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection("http://localhost:8080/fedora/", "fedoraAdmin", "fedoraAdmin");
		
		String pid = userName+":"+projectName;
		
		if (isFedoraObjectExisted(pid) == true) {
			response.sendRedirect("projectexisted.jsp");
		}
		else {
			createFedoraObject(userName, projectName, projectDescription, storageType, actionRequired);
			response.sendRedirect("projectadded.jsp");
		}
		
	}
	public boolean isFedoraObjectExisted(String pid) {
		if (fedoraServiceManager.getObjectPIDs(pid, "pid") == null) {
			System.out.println("[AddProjectRequesthandler] Object pid does NOT exists");
			return false;
		}
		else {
			System.out.println("[AddProjectRequesthandler] Object pid exists");
			return true;
		}
	}
	
	public void createFedoraObject(String userName, String projectName, String projectDescription, String storageType, String actionRequired) {
		
		//Generate a random cost value between 0 and 100.
		BigDecimal cost = new BigDecimal(Math.random());
		BigDecimal factor = new BigDecimal(100);
		cost = cost.multiply(factor);
		cost = cost.setScale(2, BigDecimal.ROUND_HALF_UP);
		
		String pid = userName+":"+projectName;
		String mimeType = "text/xml";
		String url = "http://localhost:8080/duradmin/download/contentItem?spaceId="+userName;
		
		//Create a new fedora object if it does not exist in Fedora repository.
		
		try {
			FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection();
			IngestResponse ingest = new Ingest(pid).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectname").controlGroup("R").dsLabel(projectName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectcost").controlGroup("R").dsLabel(cost.toString()).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			FedoraClient.addDatastream(pid, "creator").controlGroup("R").dsLabel(userName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "projectName").controlGroup("R").dsLabel(projectName).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "collectionDescription").controlGroup("R").dsLabel(projectDescription).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "storageType").controlGroup("R").dsLabel(storageType).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			//FedoraClient.addDatastream(pid, "collectionCost").controlGroup("R").dsLabel(actionRequired).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
			FedoraClient.addDatastream(pid, "actionRequired").controlGroup("R").dsLabel(actionRequired).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			
			System.out.println("[AddProjectRequesthandler] Project "+projectName+" for user "+userName+" created");
			
			double cloudProviderChooser = Math.random();
			if (cloudProviderChooser < 0.25) {
				FedoraClient.addDatastream(pid, "cloudProvider").controlGroup("R").dsLabel("iRODs").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			} else if (cloudProviderChooser < 50) {
				FedoraClient.addDatastream(pid, "cloudProvider").controlGroup("R").dsLabel("Amazon S3").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			} else if (cloudProviderChooser < 75) {
				FedoraClient.addDatastream(pid, "cloudProvider").controlGroup("R").dsLabel("RackSpace").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			} else if (cloudProviderChooser < 75) {
				FedoraClient.addDatastream(pid, "cloudProvider").controlGroup("R").dsLabel("Microsoft Azure").dsLocation(url).mimeType(mimeType).execute(fedoraClient);
			}
		} catch (FedoraClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

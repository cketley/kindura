package org.kindura;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AddProjectRequestHandler extends HttpServlet {
	FedoraServiceManager fedoraServiceManager;
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userName = request.getParameter("username");
		String projectName = request.getParameter("projectname");
		String primaryFunder = request.getParameter("primaryfunder");
		String otherFunder = request.getParameter("otherfundingsource");
		String projectDescription = request.getParameter("projectdescription");
		String owner = request.getParameter("ownership");
		String projectContact = request.getParameter("projectcontact");
		String department = request.getParameter("department");
		String typeOfData = request.getParameter("typeofdata");
		String startDate = request.getParameter("startdate");
		String endDate = request.getParameter("enddate");
		String role = request.getParameter("role");
		
		String storageType = null;
		String root = "root";
		String rootPID = "fedora:root";
		
		BigDecimal projectCost = new BigDecimal(Math.random());
		BigDecimal factor = new BigDecimal(100);
		projectCost = projectCost.multiply(factor);
		projectCost = projectCost.setScale(2, BigDecimal.ROUND_HALF_UP);
		
		//Generate a random storage type based on the cost.
		double storage = Math.random();
		if (storage > 0.66) {
			storageType = "Fast";
		} else if (storage > 0.33) {
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
		
		String cloudProvider = null;
		double provider = Math.random();
		if (provider > 0.75) {
			cloudProvider = "Amazon S3";
		} else if (provider > 0.5) {
			cloudProvider = "Amazon Reduced Redundency";
		} else if (provider > 0.25) {
			cloudProvider = "Rackspace";
		} else {
			cloudProvider = "Windows Azure";
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
		
		String pid = root+":"+projectName;
		
		if (fedoraServiceManager.isFedoraObjectExisted(rootPID) == false) {
			fedoraServiceManager.createRootObject(root);
			System.out.println("[AddProjectRequesthandler] Root object created");
		} else {
			System.out.println("[AddProjectRequesthandler] Root object exists");
		}
		
		if (fedoraServiceManager.isFedoraObjectExisted(pid) == true) {
			response.sendRedirect("projectexisted.jsp");
		}
		else {
			//fedoraServiceManager.createFedoraObject(userName, projectName, projectDescription, storageType, actionRequired);
			if (primaryFunder != null) {
				fedoraServiceManager.createProjectObject(userName, root, projectName, primaryFunder, projectDescription, owner, projectContact, department, typeOfData, startDate, endDate, projectCost.toString(), actionRequired, storageType, cloudProvider);
				response.sendRedirect("projectadded.jsp");
			} else {
				fedoraServiceManager.createProjectObject(userName, root, projectName, otherFunder, projectDescription, owner, projectContact, department, typeOfData, startDate, endDate, projectCost.toString(), actionRequired, storageType, cloudProvider);
				response.sendRedirect("projectadded.jsp");
			}
		}
		
	}
}

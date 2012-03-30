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
		String projectName = request.getParameter("projectName");
		String primaryFunder = request.getParameter("primaryfunder");
		String otherFunder = request.getParameter("otherfundingsource");
		String projectDescription = request.getParameter("projectdescription");
		String owner = request.getParameter("ownership");
		String projectContact = request.getParameter("projectcontact");
		String department = request.getParameter("department");
		String typeOfData = request.getParameter("typeofdata");
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		String role = request.getParameter("role");
		
		String root = "root";
		String rootPID = "fedora:root";
		
		BigDecimal projectCost;
		projectCost = BigDecimal.valueOf(Double.parseDouble("0.0"));
		// amounts are rounded up so that very tiny fractions of one penny appear as at
		// least 1p or 1cent. 
		// In this case zero should appear as zero.
		projectCost = projectCost.setScale(2, BigDecimal.ROUND_UP);

		String storageType = "";
		String actionRequired = "unknown";
		String cloudProvider = "unknown";
		
		System.out.println("[AddProjectRequesthandler] User Name: "+userName);
		System.out.println("[AddProjectRequesthandler] Project Name: "+projectName);
		HttpSession session = request.getSession(true);
		session.setAttribute("username", userName);
		session.setAttribute("projectName", projectName);
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

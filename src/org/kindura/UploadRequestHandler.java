package org.kindura;


/*
Copyright 2011 Jun Zhang, Kings College London 
and copyright 2012 Cheney Ketley, employee of Science & Technology Facilities Council and
subcontracted to Kings College London.
This file is part of Kindura.

Kindura is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Kindura is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Kindura.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.request.GetNextPID;
import com.yourmediashelf.fedora.client.request.Ingest;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import com.yourmediashelf.fedora.client.response.IngestResponse;
import com.yourmediashelf.fedora.generated.access.DatastreamType;

/**
 * This class is used to handle file upload request.
 * @author Jun Zhang
 */
public class UploadRequestHandler extends HttpServlet {
	//Setup connection with Fedora repository.
	FedoraServiceManager fedoraServiceManager = new FedoraServiceManager();
	FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection();
	
	private static final boolean debug = false;
	private static final boolean verbose = true;

	// the version of this application that interacts with a human will be
	// defaulted to ingest, but the command-line version could be other 
	// values
	private String operationFlag = "ingest";
	
	Double storageUsedTot = 0.0;
	

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//Set the temporary folder to store the uploaded file(s).
		//String tempDirectory = "C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/kindura2/uploadtemp/";
		ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
		String tempUploadDirectory = configurationFileParser.getKinduraParameters().get("TempUploadDirectory");
		
		//This String is used to store the value of parameter 'username' sent by upload.jsp.
		String userName = null;
		
		//This String is used to store the value of parameter 'pathinfo[]' sent by upload.jsp.
		String projectName = null;
		String collectionName = null;
		String collectionDescription = null;
		//String actionRequired = null;
		String fileOriginalPath = null;
		String filePathOfDuraCloud = null;
		String filePathOfFedora = null;
		//String fileUrl = null;
		String estimatedaccessFrequency = null;
		String protectiveMarking = null;
		String version = null;
		String timeStamp = null;
		String expiryDate = null;
		String researchfunder = null;
		boolean bmpToJPG = false;
		boolean tiffToJPG = false;
		boolean personaldata = false;
		File uploadFile = null;
		
		//Initialization for chunk management.
		boolean bLastChunk = false;
		int numChunk = 0;

		String ingestData = "";
		Double suggestedIngestVal = 0.0;
		String suggestedCurr = "";
		String suggestedSP = "";
		String suggestedReg = "";
		String suggestedPayPlan = "";
		String suggestedReplicas = "";

		//CAN BE OVERRIDEN BY THE postURL PARAMETER: if error=true is passed as a parameter on the URL
		boolean generateError = false;  
		boolean generateWarning = false;
		boolean sendRequest = false;  

		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();

		Enumeration<String> headers = request.getHeaderNames();
		if (debug) {System.out.println("[UploadRequestHandler]  ------------------------------ ");};
		if (debug) {System.out.println("[UploadRequestHandler]  Headers of the received request:");};
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			if (debug) {System.out.println("[UploadRequestHandler]  " + header + ": " + request.getHeader(header));};	  	
		}
		if (debug) {System.out.println("[UploadRequestHandler]  ------------------------------ ");};

		try {
			// Get URL Parameters.
			Enumeration paraNames = request.getParameterNames();
			if (debug) {System.out.println("[UploadRequestHandler]  ------------------------------ ");};
			if (debug) {System.out.println("[UploadRequestHandler]  request is " + request);};
			if (debug) {System.out.println("[UploadRequestHandler]  Parameters: ");};
			String parameterName;
			String parameterValue;
			while (paraNames.hasMoreElements()) {
				parameterName = (String)paraNames.nextElement();
				parameterValue = request.getParameter(parameterName);
				if (debug) {System.out.println("[UploadRequestHandler] " + parameterName + " = " + parameterValue);};
				if (parameterName.equals("jufinal")) {
					bLastChunk = parameterValue.equals("1");
				} else if (parameterName.equals("jupart")) {
					numChunk = Integer.parseInt(parameterValue);
				}
				//For debug convenience, putting error=true as a URL parameter, will generate an error
				//in this response.
				if (parameterName.equals("error") && parameterValue.equals("true")) {
					generateError = true;
				}
	
				//For debug convenience, putting warning=true as a URL parameter, will generate a warning
				//in this response.
				if (parameterName.equals("warning") && parameterValue.equals("true")) {
					generateWarning = true;
				}
		
				//For debug convenience, putting readRequest=true as a URL parameter, will send back the request content
				//into the response of this page.
				if (parameterName.equals("sendRequest") && parameterValue.equals("true")) {
					sendRequest = true;
				}
	
			}
			if (debug) {System.out.println("[UploadRequestHandler]  ------------------------------ ");};
	
			int ourMaxMemorySize  = 10000000;
			int ourMaxRequestSize = 2000000000;
	
			// create a CostOptimiser
			CostOptimiser costOpt = new CostOptimiser();

			// Create a factory for disk-based file items
			DiskFileItemFactory factory = new DiskFileItemFactory();
	
			// Set factory constraints
			factory.setSizeThreshold(ourMaxMemorySize);
			factory.setRepository(new File(tempUploadDirectory));
	
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
	
			// Set overall request size constraint
			upload.setSizeMax(ourMaxRequestSize);
			
			// Parse the request
			if (sendRequest) {
				//For debug only. Should be removed for production systems. 
				if (debug) {System.out.println("[UploadRequestHandler] ==========================================================================="); };
				if (debug) {System.out.println("[UploadRequestHandler] Sending the received request content: "); };
				InputStream is = request.getInputStream();
				int c;
				while ( (c=is.read()) >= 0) {
					out.write(c);
				}//while
				is.close();
				if (debug) {System.out.println("[UploadRequestHandler] ==========================================================================="); };
			} else if (! request.getContentType().startsWith("multipart/form-data")) {
				if (debug) {System.out.println("[UploadRequestHandler] No parsing of uploaded file: content type is " + request.getContentType()); };
			} else { 
				// Use apache commons fileUpload here. JUpload is handled elsewhere.
				// we can parse the request only once
				List userData = null;
				try {
					userData = upload.parseRequest(request);
				} catch (FileUploadException e) {
					e.printStackTrace();
				}
				// this iterator is to read-ahead to get Collection total size
				Iterator trundle = userData.iterator();
				FileItem userResponse;
				// this iterator is for actually processing the files in the list
				Iterator iter = userData.iterator();
				FileItem fileItem;

				
				if (debug) {System.out.println("[UploadRequestHandler]  userData is " + userData);};
				if (debug) {System.out.println("[UploadRequestHandler]  Let's read the sent data   (" + userData.size() + " items)");};
				
				Map<String, String> inputMetadata = new HashMap<String, String>();
				LinkedList<String> concludedIngestList = new LinkedList<String>();
				LinkedList<String> concludedMigrationList = new LinkedList<String>();
				LinkedList<String> concludedDropList = new LinkedList<String>();


				// we are going to now iterate thru the list twice over.
				// This is because we need to have the overall picture of 
				// space used plus the user-selected parameters to be able to 
				// decide on an appropriate Service Provider and physical 
				// datacentre (for regulatory reasons). 
				// This SP + Region information will be required by each file
				// as it is stored later on during the second iteration.
				// But if the user-input data is invalid we get strange results
				// We will just have to live with that - there's a limit to what
				// we can do without tying ourselves in knots
				String serviceProviderAccount1 = "";
				String serviceProviderAccount2 = "";
				String serviceProviderAccount3 = "";
				String serviceProviderAccount4 = "";
				String serviceProviderAccount5 = "";
				String serviceProviderAccount6 = "";

				while (trundle.hasNext()) {
					userResponse = (FileItem) trundle.next();

					if (debug) {System.out.println("[UploadRequestHandler] current userResponse is " + userResponse);};

					//Handle the metadata from the form field of "upload.jsp".
					if (userResponse.isFormField()) {
						if (debug) {System.out.println("[UploadRequestHandler] (form field) " + userResponse.getFieldName() + " = " + userResponse.getString());};

						//If we receive the md5sum parameter, we've read finished to read the current file. It's not
						//a very good (end of file) signal. Will be better in the future ... probably !
						//Let's put a separator, to make output easier to read.
						if (userResponse.getFieldName().equals("md5sum[]")) { 
							if (debug) {System.out.println("[UploadRequestHandler]  ------------------------------ ");};
						} else if (userResponse.getFieldName().equals("username")) {
							userName = userResponse.getString();
							if (userName == null) {
								response.sendRedirect("sessiontimeout.jsp");
							}
						} else if (userResponse.getFieldName().equals("projectName")) {
							projectName = userResponse.getString();
						} else if (userResponse.getFieldName().equals("collectionName")) {
							collectionName = userResponse.getString();
						} else if (userResponse.getFieldName().equals("accessFrequency")) {
							estimatedaccessFrequency = userResponse.getString();
						} else if (userResponse.getFieldName().equals("collectionDescription")) {
							collectionDescription = userResponse.getString();
						} else if (userResponse.getFieldName().equals("protectiveMarking")) {
							protectiveMarking = userResponse.getString();
						} else if (userResponse.getFieldName().equals("version")) {
							version = userResponse.getString();
						} else if (userResponse.getFieldName().equals("pathinfo[]")) {
							fileOriginalPath = userResponse.getString();
						} else if (userResponse.getFieldName().equals("bmptojpg")) {
							bmpToJPG = Boolean.valueOf(userResponse.getString());
							if (debug) {System.out.println("[UploadRequestHandler] bmptojpg: "+bmpToJPG);};
						} else if (userResponse.getFieldName().equals("tifftojpg")) {
							tiffToJPG = Boolean.valueOf(userResponse.getString());
							if (debug) {System.out.println("[UploadRequestHandler] tifftojpg: "+tiffToJPG);};
						} else if (userResponse.getFieldName().equals("personaldata")) {
							personaldata = Boolean.valueOf(userResponse.getString());
							if (debug) {System.out.println("[UploadRequestHandler] personaldata: "+personaldata);};
						} else if (userResponse.getFieldName().equals("timestamp")) {
							timeStamp = userResponse.getString();
							if (debug) {System.out.println("[UploadRequestHandler] timestamp: "+timeStamp);};
						} 

						// For each field store the field info in the metadata for the rule engine.
						inputMetadata.put(userResponse.getFieldName(), userResponse.getString());

					} else {
						String parentFolderName = null;
						String parentFolderPID = null;
						String fieldName = userResponse.getFieldName();
						String fileName = userResponse.getName();
						//Delete all spaces in the file name.
						fileName = fileName.replace(" ", "");
						String mimeType = userResponse.getContentType();
						String fileNameExtension = fileName.substring(fileName.lastIndexOf(".")+1);
						String fileSize = String.valueOf(userResponse.getSize());
						// accumulate the total amount of disk space used
						storageUsedTot += Integer.valueOf(fileSize);
							
						//Set the namespace of the content.
						//////String nameSpace = userName;
						String nameSpace = collectionName;
						//Get the base file name of the file.
						// Allow for multiple dot characters within the name
						int dot = fileName.lastIndexOf(".");
						String baseFileName = fileName.substring(0, dot);
						//Generate the namespace. To create a new Fedora object for each file, use baseFilename. To create a new Fedora object for each data collection, use collectionName. 
						//String nameSpaceAndPid = nameSpace + ":" + baseFilename;
						//String nameSpaceAndPid = nameSpace + ":" + collectionName;
						//String nameSpaceAndPid = userName+":"+projectName;
						//String nameSpaceAndPid = projectName+":"+collectionName;

						String filePID = collectionName+":"+filePathOfFedora+"."+baseFileName;

						// bring in the metadata from the project table
						String keyPart = "";
						String valPart = "";
						Integer extractTxtPosition = 0;
						String leftoverText = "";
						String currentProjectKey = "";
						String currentProjectValue = "";
						String remainingText = "";
						
						// extract the previously-stored data for the project as a whole
						// from fedora and put it into the metadata hashmap
						List<String> projectPids = fedoraServiceManager.getProjectObjectPIDs();
						Iterator<String> scrutinise = projectPids.iterator();
						String currentProjectPid = "";
						String currentProjectPidValue = "";
						while (scrutinise.hasNext()) {
							currentProjectPid = (String)scrutinise.next();
							if (debug) {System.out.println("[UploadRequestHandler]currentProjectPid = " + currentProjectPid);};
							
							extractTxtPosition = currentProjectPid.indexOf(":") + 1;
							if (debug) {System.out.println("[UploadRequestHandler]extractTxtPosition = " + extractTxtPosition);};
							currentProjectPidValue = currentProjectPid.substring(extractTxtPosition, currentProjectPid.length());
							if (debug) {System.out.println("[UploadRequestHandler]currentProjectPidValue = " + currentProjectPidValue);};
							if (debug) {System.out.println("[UploadRequestHandler]projectName = " + projectName);};
							if ( currentProjectPidValue.equals(projectName)) {
								if (debug) {System.out.println("[UploadRequestHandler]currentProjectPidValue = " + currentProjectPidValue);};
								break;
//								inputMetadata.put(currentProjectKey, currentProjectValue);	
							}
							
						}
						// this reads all available info about the project
						ArrayList<DatastreamType> projectMetadata = fedoraServiceManager.getDataStreams(currentProjectPid);
						if (projectMetadata != null) {
							// TODO sort out the indexing on projectMetadata.size()
							// there's something funny about the projectMetadata.size(), so hardcode it as 12
								for (int i=0;i<12;i++) {
									keyPart = projectMetadata.get(i).getDsid();
									valPart = projectMetadata.get(i).getLabel();	
									if (debug) {System.out.println("[UploadRequestHandler] keyPart = " + keyPart);};
									if (debug) {System.out.println("[UploadRequestHandler] valPart = " + valPart);};
									inputMetadata.put(keyPart, valPart);									
								}
						}
						
					} 
				} // while

				// TODO the file size on disk is Bytes, but we need it in TB for the
				// drools band selection which is a design weakness to be fixed one day
				storageUsedTot /= 1000000000000.0;
				// there might already be a value here so just overwrite it
				inputMetadata.put("storageUsed", String.valueOf(storageUsedTot));
				// transfers and requests are inferred from accessFrequency
				// transfers are not munged with accessFrequency because makes
				// no difference to the decision on which SP to use

				// ingest means take in new data
				if ( operationFlag .equals("ingest") ) {
					// TODO need a better way to handle transfer-in costs on ingest
					// We assume no transfer-out costs for the ingest and usually 
					// service providers set transfer-in costs as zero
					inputMetadata.put("transfersUsed", String.valueOf(0.0));
					// the other data required is held in inputMetadata
					concludedIngestList.clear();
					concludedMigrationList.clear();
					concludedDropList.clear();
					costOpt.suggestService(operationFlag, inputMetadata, concludedIngestList, concludedMigrationList, concludedDropList);
					if (verbose) {
						ingestData = "";
						suggestedIngestVal = 0.0;
						suggestedCurr = "";
						suggestedSP = "";
						suggestedReg = "";
						suggestedPayPlan = "";
						suggestedReplicas = "";

						Iterator wrigglelator = concludedIngestList.iterator();
						while (wrigglelator.hasNext()) {

							ingestData = (String)wrigglelator.next();
							suggestedIngestVal = Double.valueOf(costOpt.popString(ingestData, 1));
							suggestedCurr = costOpt.popString(ingestData, 6);
							suggestedSP = costOpt.popString(ingestData, 2);
							suggestedReg = costOpt.popString(ingestData, 3);
							suggestedPayPlan = costOpt.popString(ingestData, 4);
							// this is how many copies are offered by Service Provider
							suggestedReplicas = costOpt.popString(ingestData, 5);
							System.out.println("[UploadRequestHandler] --------------- " );

							System.out.println("[UploadRequestHandler] suggestedSP = " + suggestedSP );
							System.out.println("[UploadRequestHandler] suggestedReg = " + suggestedReg );
							System.out.println("[UploadRequestHandler] suggestedPayPlan = " + suggestedPayPlan );
							System.out.println("[UploadRequestHandler] suggestedReplicas = " + suggestedReplicas );
							System.out.println("[UploadRequestHandler] suggestedIngestVal = " + suggestedIngestVal );
							System.out.println("[UploadRequestHandler] suggestedCurr = " + suggestedCurr );

						}							
					};
				} else if (operationFlag .equals("migrate-across") ) {
					// migrate-across means remove from one and upload to a cheaper location
					// shouldn't be here
					;
				} else if (operationFlag .equals("migrate-down") ) {	
					// migrate-down means data is no longer in active use
					// shouldn't be here
					;
				} else if (operationFlag .equals("migrate-up") ) {	
					// migrate-up means data was inactive but is now active, or error recovery
					// shouldn't be here
					;
				} else if (operationFlag .equals("retrieval") ) {
					// retrieval means data to be copied down to local disk
					// shouldn't be here
					;
				} 

				// now iterate through the same list again to do duracloud/fedora 
				// operations with knowledge of the Service provider and 
				// physical datacentre (= Region) to send it to

				while (iter.hasNext()) {
					fileItem = (FileItem) iter.next();
					
					if (debug) {System.out.println("[UploadRequestHandler] current fileItem is " + fileItem);};

					//Handle the metadata from the form field of "upload.jsp".
					if (fileItem.isFormField()) {
						if (debug) {System.out.println("[UploadRequestHandler] (form field) " + fileItem.getFieldName() + " = " + fileItem.getString());};
	
						//If we receive the md5sum parameter, we've read finished to read the current file. It's not
						//a very good (end of file) signal. Will be better in the future ... probably !
						//Let's put a separator, to make output easier to read.
						if (fileItem.getFieldName().equals("md5sum[]")) { 
							if (debug) {System.out.println("[UploadRequestHandler]  ------------------------------ ");};
						} else if (fileItem.getFieldName().equals("username")) {
							userName = fileItem.getString();
							if (userName == null) {
								response.sendRedirect("sessiontimeout.jsp");
							}
						} else if (fileItem.getFieldName().equals("projectName")) {
							projectName = fileItem.getString();
						} else if (fileItem.getFieldName().equals("collectionName")) {
							collectionName = fileItem.getString();
						} else if (fileItem.getFieldName().equals("accessFrequency")) {
							estimatedaccessFrequency = fileItem.getString();
						} else if (fileItem.getFieldName().equals("collectionDescription")) {
							collectionDescription = fileItem.getString();
						} else if (fileItem.getFieldName().equals("protectiveMarking")) {
							protectiveMarking = fileItem.getString();
						} else if (fileItem.getFieldName().equals("version")) {
							version = fileItem.getString();
						} else if (fileItem.getFieldName().equals("pathinfo[]")) {
							fileOriginalPath = fileItem.getString();
						} else if (fileItem.getFieldName().equals("bmptojpg")) {
							bmpToJPG = Boolean.valueOf(fileItem.getString());
						} else if (fileItem.getFieldName().equals("tifftojpg")) {
							tiffToJPG = Boolean.valueOf(fileItem.getString());
							if (debug) {System.out.println("[UploadRequestHandler] tifftojpg: "+tiffToJPG);};
						} else if (fileItem.getFieldName().equals("personaldata")) {
							personaldata = Boolean.valueOf(fileItem.getString());
						} else if (fileItem.getFieldName().equals("timestamp")) {
							timeStamp = fileItem.getString();
							if (debug) {System.out.println("[UploadRequestHandler] timestamp: "+timeStamp);};
						}
						// this was already done previously
//Store metadata from the web page "upload.jsp" for the rule engine.
//						inputMetadata.put(fileItem.getFieldName(), fileItem.getString());
					} 
					//Handle the uploaded file.
					else {
						// Setup a connection with DuraCloud.
						DuraStoreClient duraStoreClient = new DuraStoreClient(configurationFileParser.getKinduraParameters().get("DuraCloudHost"), configurationFileParser.getKinduraParameters().get("DuraCloudPort"), configurationFileParser.getKinduraParameters().get("DuraCloudContext"), configurationFileParser.getKinduraParameters().get("DuraCloudUsername"), configurationFileParser.getKinduraParameters().get("DuraCloudPassword"));
						
						String collectionPID = projectName+":"+collectionName;
						if (collectionName.equals("")) {
							throw new NullPointerException("Please input the 'Collection Title'.");
						} else if (projectName.equals("")) {
							throw new NullPointerException("Please select the 'Associated Project'.");
						} else if (collectionDescription.equals("")) {
							throw new NullPointerException("Please input the 'Description'.");
						} else if (version.equals("")) {
							throw new NullPointerException("Please select the 'Version'.");
						} else if (estimatedaccessFrequency.equals("")) {
							throw new NullPointerException("Please select the 'Estimated Access Frequency'.");
						} else if (protectiveMarking.equals("")) {
							throw new NullPointerException("Please select the 'Protective Marking'.");
						}
						else {
							//Get storage decisions from the rule engine.
							List<String> spaceIterator = duraStoreClient.defaultContentStore.getSpaces();
							if (spaceIterator.contains(collectionName)) {
								if (debug) {System.out.println("[UploadRequestHandler] collection name "+collectionName+" exists in DuraCloud.");};
								List<String> fedoraCollectionObject = fedoraServiceManager.getObjectPIDs("*"+":"+collectionName, "pid");
								for (String fedoraCollectionIterator : fedoraCollectionObject) {
									if (!fedoraServiceManager.getADataStream(fedoraCollectionIterator, "timeStamp").equals(timeStamp))	{
										if (debug) {System.out.println("[UploadRequestHandler] time stamp does NOT match");};
										throw new CollectionExistedException("Collection name "+collectionName+" is already existed. Please use another name.");
									} else {
										if (debug) {System.out.println("[UploadRequestHandler] time stamp matches");};
									}
								}
								/*if (fedoraServiceManager.isFedoraObjectExisted(collectionPID) == true) {
									if (debug) {System.out.println("[UploadRequestHandler] new timeStamp: "+timeStamp);};
									if (debug) {System.out.println("[UploadRequestHandler] existing timestamp: "+fedoraServiceManager.getADataStream(collectionPID, "timeStamp"));};
									if (!fedoraServiceManager.getADataStream(collectionPID, "timeStamp").equals(timeStamp)) {
										if (debug) {System.out.println("[UploadRequestHandler] time stamp does NOT match");};
										throw new CollectionExistedException("Collection name "+collectionName+" is already existed. Please use another name.");
									} else {
										if (debug) {System.out.println("[UploadRequestHandler] time stamp matches");};
									}
								}*/
							}
						}
						//Print out the metadata stored in the Map.
						if (verbose) {System.out.println("metadata stored in the Map for the rule engine:");};
						for (Map.Entry<String, String> entry : inputMetadata.entrySet()) {
							if (verbose) {System.out.println("[Metadata] Key="+entry.getKey()+" Value="+entry.getValue());};
						}
						
						//Ok, we've got a file. Let's process it.
						//Again, for all informations of what is exactly a FileItem, please
						//have a look to http://jakarta.apache.org/commons/fileupload/
						//
						String parentFolderName = null;
						String parentFolderPID = null;
						String fieldName = fileItem.getFieldName();
						String fileName = fileItem.getName();
						//Delete all spaces in the file name.
						fileName = fileName.replace(" ", "");
						String mimeType = fileItem.getContentType();
						String fileNameExtension = fileName.substring(fileName.lastIndexOf(".")+1);
						String fileSize = String.valueOf(fileItem.getSize());				
						//Set the namespace of the content.
						//////String nameSpace = userName;
						String nameSpace = collectionName;
						//Get the base file name of the file.
						int dot = fileName.lastIndexOf(".");
						String baseFileName = fileName.substring(0, dot);
						//Generate the namespace. To create a new Fedora object for each file, use baseFilename. To create a new Fedora object for each data collection, use collectionName. 
						//String nameSpaceAndPid = nameSpace + ":" + baseFilename;
						//String nameSpaceAndPid = nameSpace + ":" + collectionName;
						//String nameSpaceAndPid = userName+":"+projectName;
						//String nameSpaceAndPid = projectName+":"+collectionName;
						
						String filePID = collectionName+":"+filePathOfFedora+"."+baseFileName;
						
						if (debug) {System.out.println("[UploadRequestHandler] File Format: " + fileNameExtension);};
						if (debug) {System.out.println("[UploadRequestHandler] Field Name: " + fieldName);};
						if (debug) {System.out.println("[UploadRequestHandler] File Name: " + fileName);};
						if (debug) {System.out.println("[UploadRequestHandler] MIME Type: " + mimeType);};
						if (debug) {System.out.println("[UploadRequestHandler] Size (Bytes): " + fileSize);};

						//If we are in chunk mode, we add ".partN" at the end of the file, where N is the chunk number.
						//String uploadedFilename = fileItem.getName() + ( numChunk>0 ? ".part"+numChunk : "") ;
						String uploadedFilename = fileName + ( numChunk>0 ? ".part"+numChunk : "") ;
						uploadFile = new File(tempUploadDirectory + (new File(uploadedFilename)).getName());
						if (debug) {System.out.println("[UploadRequestHandler] File Out: " + uploadFile.toString());};
						// write the file
						fileItem.write(uploadFile);	        
						
					
						///////////////////////////////////////////////////////////////////////////////////////////////////////
						//DuraCloud operations.
						//
						
						ingestData = "";
						suggestedIngestVal = 0.0;
						suggestedCurr = "";
						suggestedSP = "";
						suggestedReg = "";
						suggestedPayPlan = "";
						suggestedReplicas = "";
						
						Iterator wanderator = concludedIngestList.iterator();
						while (wanderator.hasNext()) {

							ingestData = (String)wanderator.next();
							suggestedIngestVal = Double.valueOf(costOpt.popString(ingestData, 1));
							suggestedCurr = costOpt.popString(ingestData, 6);
							suggestedSP = costOpt.popString(ingestData, 2);
							suggestedReg = costOpt.popString(ingestData, 3);
							suggestedPayPlan = costOpt.popString(ingestData, 4);
							// this is how many copies are offered by Service Provider
							suggestedReplicas = costOpt.popString(ingestData, 5);

							// this munges the filepath of the file so fedora can handle it correctly
							filePathOfDuraCloud = duraStoreClient.reviseFilePathForDuracloud(fileOriginalPath);
							//Create a new namespace if the namespace does not exist.
							// The nameSpace variable passed across is actually the collection name.
							Map<String, String> spaceMetadata = new HashMap<String, String>();
							// TODO ugly bodge - amazon has one region per account so we have multiple accounts
							if ( (suggestedSP .equals("Amazon S3")) && (suggestedPayPlan .equals("Pay as you go - reduced redundancy")) ) {
								if (duraStoreClient.isNameSpaceExisted("Amazon S3 RRS", nameSpace) == false) {
									duraStoreClient.createNamespace("Amazon S3 RRS", nameSpace, spaceMetadata);								
								}
								duraStoreClient.uploadFile(duraStoreClient.amazonS3RRSContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
							}
							if ( (suggestedSP .equals("Amazon S3")) &&  ! (suggestedPayPlan .equals("Pay as you go - reduced redundancy"))  ) {
								if (duraStoreClient.isNameSpaceExisted("Amazon S3", nameSpace) == false) {
									duraStoreClient.createNamespace("Amazon S3", nameSpace, spaceMetadata);
								}
								duraStoreClient.uploadFile(duraStoreClient.amazonS3ContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
							}
							if ( (suggestedSP .equals("Rackspace Cloud Files")) ) {
								if (duraStoreClient.isNameSpaceExisted("Rackspace Cloud Files", nameSpace) == false) {
									duraStoreClient.createNamespace("Rackspace Cloud Files", nameSpace, spaceMetadata);									
								}
								duraStoreClient.uploadFile(duraStoreClient.rackSpaceContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
							}
							if ( (suggestedSP .equals("iRODS")) ) {
								if (duraStoreClient.isNameSpaceExisted("iRODS", nameSpace) == false) {
									duraStoreClient.createNamespace("iRODS", nameSpace, spaceMetadata);									
								}
								duraStoreClient.uploadFile(duraStoreClient.iRODSContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
							}
							if ( (suggestedSP .equals("Google Cloud Storage")) ) {
								if (duraStoreClient.isNameSpaceExisted("Google Cloud Storage", nameSpace) == false) {
									duraStoreClient.createNamespace("Google Cloud Storage", nameSpace, spaceMetadata);
								}
								duraStoreClient.uploadFile(duraStoreClient.googleCloudStorageContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
							}
							if ( (suggestedSP .equals("Azure")) ) {
								if (duraStoreClient.isNameSpaceExisted("Azure", nameSpace) == false) {
									duraStoreClient.createNamespace("Azure", nameSpace, spaceMetadata);									
								}
								duraStoreClient.uploadFile(duraStoreClient.azureContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
							}
							if ( (suggestedSP .equals("SDSC")) ) {
								if (duraStoreClient.isNameSpaceExisted("SDSC", nameSpace) == false) {
									duraStoreClient.createNamespace("SDSC", nameSpace, spaceMetadata);
								}
								duraStoreClient.uploadFile(duraStoreClient.sdscContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
							}


							//Upload the file to the Cloud.
							//duraStoreClient.uploadFile(duraStoreClient.defaultContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());

//							duraStoreClient.uploadFile(duraStoreClient.amazonS3ContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
//							duraStoreClient.uploadFile(duraStoreClient.rackSpaceContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());

							//duraStoreClient.uploadFile(duraStoreClient.rackSpaceContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());

							System.out.println("filePathOfDuraCloud/fileName "+filePathOfDuraCloud+"/"+fileName);

							if ((bmpToJPG == true) || (tiffToJPG == true)) {
								if (debug) {System.out.println("[UploadRequestHandler] bmp to jpg is "+bmpToJPG+" tiffToJPG is "+tiffToJPG+". Image transformer is started.");};
								DuraServiceClient duraServiceClient = new DuraServiceClient();
								duraServiceClient.runImageTransformerOverSpace(collectionName, collectionName, filePathOfDuraCloud+"/"+fileName, bmpToJPG, tiffToJPG);
								if (isFileConverted(duraStoreClient.defaultContentStore, collectionName, filePathOfDuraCloud+"/"+fileName) == true) {
									if (debug) {System.out.println("[UploadRequestHandler] file "+filePathOfDuraCloud+"/"+fileName+" has been converted. Change file extension in Fedora repository.");};
									fileNameExtension = "jpg";
									duraStoreClient.defaultContentStore.deleteContent(collectionName, filePathOfDuraCloud+"/"+fileName);
									if (debug) {System.out.println("[UploadRequestHandler] file "+filePathOfDuraCloud+"/"+fileName+" has been deleted.");};
								} else {
									if (debug) {System.out.println("[UploadRequestHandler] file "+filePathOfDuraCloud+"/"+fileName+" has NOT been converted.");};
								}
							} 
						
						}
						//
						// End of DuraCloud operations.
						///////////////////////////////////////////////////////////////////////////////////////////////////////

						
						///////////////////////////////////////////////////////////////////////////////////////////////////////
						//Fedora operations.
						//
						
						ingestData = "";
						suggestedIngestVal = 0.0;
						suggestedCurr = "";
						suggestedSP = "";
						suggestedReg = "";
						suggestedPayPlan = "";
						suggestedReplicas = "";
						serviceProviderAccount1 = "";
						serviceProviderAccount2 = "";
						serviceProviderAccount3 = "";
						serviceProviderAccount4 = "";
						serviceProviderAccount5 = "";
						serviceProviderAccount6 = "";

						Integer spCount = 0;
						Iterator wanderise = concludedIngestList.iterator();
						while (wanderise.hasNext()) {

							ingestData = (String)wanderise.next();
							suggestedIngestVal = Double.valueOf(costOpt.popString(ingestData, 1));
							suggestedSP = costOpt.popString(ingestData, 2);
							suggestedReg = costOpt.popString(ingestData, 3);
							suggestedPayPlan = costOpt.popString(ingestData, 4);
							suggestedCurr = costOpt.popString(ingestData, 6);
							// this is how many copies are offered by Service Provider
							suggestedReplicas = costOpt.popString(ingestData, 5);
							
							// we have only 255 characters to play with...
							switch (spCount) 
							{
							case 0 : 
								serviceProviderAccount1 = suggestedSP + "|" + suggestedReg + "|" + suggestedPayPlan;		
								break;
							case 1 : 
								serviceProviderAccount2 = suggestedSP + "|" + suggestedReg + "|" + suggestedPayPlan;		
								break;
							case 2 : 
								serviceProviderAccount3 = suggestedSP + "|" + suggestedReg + "|" + suggestedPayPlan;		
								break;
							case 3 : 
								serviceProviderAccount4 = suggestedSP + "|" + suggestedReg + "|" + suggestedPayPlan;		
								break;
							case 4 :
								serviceProviderAccount5 = suggestedSP + "|" + suggestedReg + "|" + suggestedPayPlan;		
								break;
							case 5 : 
								serviceProviderAccount6 = suggestedSP + "|" + suggestedReg + "|" + suggestedPayPlan;		
							}
							spCount++;

				    		if ( ! ( inputMetadata.get( "collectionStgExpiryDate" ) == null) ) {
				    			if ( ! inputMetadata.get( "collectionStgExpiryDate" ) .isEmpty()) {
				    				expiryDate = inputMetadata.get( "collectionStgExpiryDate" );
				    			} else {
				    				expiryDate = "01/01/2012";
				    			}
				    		} else {
				    			expiryDate = "01/01/2012";
				    		}				
							
							// increment the total size of the collection when it already exists
				    		Double tempCollTotSize = 0.0;
				    		if ( ! ( inputMetadata.get( "collectionTotSize" ) == null) ) {
				    			if ( ! inputMetadata.get( "collectionTotSize" ) .isEmpty()) {
				    				// convert to TB from Bytes
				    				tempCollTotSize = Double.valueOf(inputMetadata.get( "collectionTotSize" )) / 1000000000000.0;
				    			} else {
				    				tempCollTotSize = 0.0;
				    			}
				    		} else {
				    			tempCollTotSize = 0.0;
				    		}				
							tempCollTotSize += storageUsedTot;
							// convert to Bytes from TB - precision errors are likely
							// TODO need better way to handle precision
							tempCollTotSize *= 1000000000000.0;

							fedoraServiceManager.handleCollectionObject(userName, projectName, collectionName, collectionPID, estimatedaccessFrequency, collectionDescription, protectiveMarking, version, timeStamp, suggestedSP, suggestedIngestVal, suggestedCurr, serviceProviderAccount1, serviceProviderAccount2, serviceProviderAccount3, serviceProviderAccount4, serviceProviderAccount5, serviceProviderAccount6, tempCollTotSize, operationFlag, expiryDate);

							HashMap<String, String> parentFolderNameAndPID = fedoraServiceManager.handleFolderObject(projectName, collectionName, collectionPID, fileName, fileOriginalPath);

							for (Map.Entry<String, String> entry : parentFolderNameAndPID.entrySet()) {
								parentFolderName = entry.getKey();
								parentFolderPID = entry.getValue();
							}

							fedoraServiceManager.handleFileObject(nameSpace, projectName, collectionName, parentFolderName, parentFolderPID, baseFileName, baseFileName, filePID, fileOriginalPath, fileNameExtension, fileSize);
						}
						
						//			
						// End of Fedora operations.
						//////////////////////////////////////////////////////////////////////////////////////
						
						
						
						
							
						//////////////////////////////////////////////////////////////////////////////////////
						//Chunk management: if it was the last chunk, let's recover the complete file
						//by concatenating all chunk parts.
						//
						if (bLastChunk) {	        
							if (debug) {System.out.println("[UploadRequestHandler] Last chunk received: let's rebuild the complete file (" + fileName + ")");};
							//First: construct the final filename.
							FileInputStream fis;
							FileOutputStream fos = new FileOutputStream(tempUploadDirectory + fileName);
							int nbBytes;
							byte[] byteBuff = new byte[1024];
							for (int i=1; i<=numChunk; i+=1) {
								fileName = fileName + ".part" + i;
								if (debug) {System.out.println("[UploadRequestHandler] " + "  Concatenating " + fileName);};
								fis = new FileInputStream(tempUploadDirectory + fileName);
								while ( (nbBytes = fis.read(byteBuff)) >= 0) {
									//out.println("[upload.jsp] " + "     Nb bytes read: " + nbBytes);
									fos.write(byteBuff, 0, nbBytes);
								}
								fis.close();
							}
							fos.close();
						}
	
						//
						// End of chunk management
						//////////////////////////////////////////////////////////////////////////////////////
	
						fileItem.delete();
					}	
					
					if (iter.hasNext()) {
						if (debug) {System.out.println("[UploadRequestHandler] next fileItem is available");};
					} else {
						if (debug) {System.out.println("[UploadRequestHandler] next fileItem is not available.");};
					}
					
				}//while
			}
	
			if (generateWarning) {
				out.println("WARNING: just a warning message.\\nOn two lines!");
			}
	
			if (debug) {System.out.println("[UploadRequestHandler] " + "Let's write a status, to finish the server response :");};
	
			if (generateError) { 
				System.out.println("ERROR: this is a test error");
			} else {
				out.println("SUCCESS");
				System.out.println("SUCCESS");
			}

		}catch(Exception e){
			System.out.println("");
			out.println("ERROR: Exception e = " + e.toString());
			System.out.println("ERROR: Exception e = " + e.toString());
			System.out.println("");
			//response.sendRedirect("sessiontimeout.jsp");
		}
	}
	
	/*public boolean isFileConverted(ContentStore contentStore, String collectionName, boolean bmpToJPG, boolean tiffToJPG) {
    	try {
			FedoraServiceManager fedoraServiceManager = new FedoraServiceManager();
    		Iterator<String> iterator = contentStore.getSpaceContents(collectionName);
    		List<String> originalFiles = new ArrayList<String>();
			List<String> convertedFiles = new ArrayList<String>();
			while (iterator.hasNext()) {
				String originalFileName = iterator.next();
				String convertedFileName = null;
				if ((bmpToJPG == true) && (originalFileName.substring(originalFileName.lastIndexOf(".")+1).equals("bmp"))) {
					convertedFileName = originalFileName.substring(0, originalFileName.indexOf("."))+".jpg";
					if (convertedFiles.contains(convertedFileName)) {
						return true;
					} else {
						originalFiles.add(originalFileName);
					}
				}
				if ((tiffToJPG == true) && (originalFileName.substring(originalFileName.lastIndexOf(".")+1).equals("tiff"))) {
					convertedFileName = originalFileName.substring(0, originalFileName.indexOf("."))+".jpg";
					if (convertedFiles.contains(convertedFileName)) {
						return true;
					} else {
						originalFiles.add(originalFileName);
					}
				}
				if (originalFileName.substring(originalFileName.lastIndexOf(".")+1).equals("jpg")) {
					convertedFileName = originalFileName;
					originalFileName = originalFileName.substring(0, originalFileName.indexOf("."))+".jpg";
					if (convertedFiles.contains(convertedFileName)) {
						return true;
					} else {
						convertedFiles.add(originalFileName);
					}
				}
			}
		} catch (ContentStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
    }*/
	public boolean isFileConverted(ContentStore contentStore, String collectionName, String originalFileName) {
		String convertedFileName = originalFileName.substring(0, originalFileName.lastIndexOf("."))+".jpg";
		if (debug) {System.out.println("[UploadRequestHandler] converted file name: "+convertedFileName);};
		try {
			Iterator<String> iterator = contentStore.getSpaceContents(collectionName);
			while (iterator.hasNext()) { 
				if (iterator.next().equals(convertedFileName)) {
					return true;
				}
			}
		} catch (ContentStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public String getOperationFlag() {
		return operationFlag;
	}

	public void setOperationFlag(String operationFlag) {
		this.operationFlag = operationFlag;
	}

}

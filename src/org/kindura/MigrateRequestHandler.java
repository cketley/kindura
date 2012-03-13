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
 * This class is used to handle file migration request.
 * @author Jun Zhang
 */
public class MigrateRequestHandler extends HttpServlet {
	//Setup connection with Fedora repository.
	FedoraServiceManager fedoraServiceManager = new FedoraServiceManager();
	FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection();
	
	private String operationFlag = "migrate-across";
	
	Double storageUsedTot = 0.0;
	

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//Set the temporary folder to store the uploaded file(s).
		//String tempDirectory = "C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/kindura2/uploadtemp/";
		ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
		String tempUploadDirectory = configurationFileParser.getKinduraParameters().get("TempUploadDirectory");
		
		//This String is used to store the value of parameter 'username' sent by MigrateRequestHandler.java.
		String userName = null;
		
		//This String is used to store the value of parameter 'pathinfo0' sent by MigrateRequestHandler.java.
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
		String researchfunder = null;
		boolean bmpToJPG = false;
		boolean tiffToJPG = false;
		boolean personaldata = false;
		File uploadFile = null;
		
		//Initialization for chunk management.
		boolean bLastChunk = false;
		int numChunk = 0;

		//CAN BE OVERRIDEN BY THE postURL PARAMETER: if error=true is passed as a parameter on the URL
		boolean generateError = false;  
		boolean generateWarning = false;
		boolean sendRequest = false;  

		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();

		Enumeration<String> headers = request.getHeaderNames();
		System.out.println("[MigrateRequestHandler.java]  ------------------------------ ");
		System.out.println("[MigrateRequestHandler.java]  Headers of the received request:");
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			System.out.println("[MigrateRequestHandler.java]  " + header + ": " + request.getHeader(header));  	  	
		}
		System.out.println("[MigrateRequestHandler.java]  ------------------------------ "); 

		try {
			// Get URL Parameters.
			Enumeration paraNames = request.getParameterNames();
			System.out.println("[MigrateRequestHandler.java]  ------------------------------ ");
			System.out.println("[MigrateRequestHandler.java]  Parameters: ");
			String parameterName;
			String parameterValue;
			while (paraNames.hasMoreElements()) {
				parameterName = (String)paraNames.nextElement();
				parameterValue = request.getParameter(parameterName);
				System.out.println("[MigrateRequestHandler.java] " + parameterName + " = " + parameterValue);
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
			System.out.println("[MigrateRequestHandler.java]  ------------------------------ ");
	
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
				System.out.println("[MigrateRequestHandler.java] ==========================================================================="); 
				System.out.println("[MigrateRequestHandler.java] Sending the received request content: "); 
				InputStream is = request.getInputStream();
				int c;
				while ( (c=is.read()) >= 0) {
					out.write(c);
				}//while
				is.close();
				System.out.println("[MigrateRequestHandler.java] ==========================================================================="); 
			} else if (! request.getContentType().startsWith("multipart/form-data")) {
				System.out.println("[MigrateRequestHandler.java] No parsing of uploaded file: content type is " + request.getContentType()); 
			} else { 
				List /* FileItem */ userData = upload.parseRequest(request);
				// Process the uploaded items
				Iterator trundle = userData.iterator();
				FileItem userResponse;
				//File outputFile;
				System.out.println("[MigrateRequestHandler.java]  Let's read the sent data   (" + userData.size() + " items)");
				
				Map<String, String> inputMetadata = new HashMap<String, String>();
				LinkedList<String> concludedIngestList = new LinkedList<String>();
				LinkedList<String> concludedMigrationList = new LinkedList<String>();

				String serviceProviderAccount = "";
				Double minimalCost = 0.0;
				String minimalCostCurrency = "";

				while (trundle.hasNext()) {
					userResponse = (FileItem) trundle.next();

					//Handle the metadata from the form field of "MigrateRequestHandler.java".
					if (userResponse.isFormField()) {
						System.out.println("[MigrateRequestHandler.java] (form field) " + userResponse.getFieldName() + " = " + userResponse.getString());

						if (userResponse.getFieldName().equals("username")) {
							userName = userResponse.getString();
							if (userName == null) {
								response.sendRedirect("sessiontimeout.jsp");
							}
						}

						List<String> projectNames = fedoraServiceManager.getprojectNames();
						if (projectNames != null) {
							Iterator<String> iterator = projectNames.iterator();
							System.out.println(request.getAttribute("projectName"));
							if (request.getAttribute("projectName") != null) {
								out.println("<option>"+request.getAttribute("projectName")+"</option>");
							}
							String nextprojectName = null;
							while (iterator.hasNext()) {
								nextprojectName = (String)iterator.next();
								if (!nextprojectName.equals(request.getAttribute("projectName"))) {
									out.println("<option>"+nextprojectName+"</option>");
								}
							}
						}
						int numberOfRows = Integer.valueOf(request.getAttribute("numberOfRows").toString());
						if (numberOfRows > 0) {
							if (request.getAttribute("pid0fedoraObjectType").equals("collection")) {
								out.println("<th>Title</th>");
							} else {
								out.println("<th>Name</th>");
							}
							out.println("<th>Size(Bytes)</th>");
							out.println("<th>Type</th>");
							for (int i=0;i< numberOfRows;i++) {
								if (i % 2 != 0) {
									out.println("<tr style= 'background-color:#F8F8F8;'>");
								}
								else {
									out.println("<tr style= 'background-color:#FFFFFF;'>");
								}
								if (request.getAttribute("pid"+i+"fedoraObjectType").equals("file")) {
									//out.println("<td><a href=DownloadRequestHandler?namespaceandpid="+request.getAttribute("nameSpace"+i)+":"+request.getAttribute("parentFolderNameForDuracloud"+i)+"/"+request.getAttribute("baseFileName"+i)+"&"+request.getAttribute("nameSpace"+i)+request.getAttribute("projectName")+"/"+request.getAttribute("parentFolderNameForDuracloud"+i)+"/"+request.getAttribute("baseFileName"+i)+"="+request.getAttribute("fileExtension"+i)+">"+request.getAttribute("alphaNumericName"+i)+"</a></td>");
									out.println("<td><a href=MigrateRequestHandler?namespaceandpid="+request.getAttribute("collectionName"+i)+":"+request.getAttribute("parentFolderNameForDuracloud"+i)+"/"+request.getAttribute("baseFileName"+i)+"&"+request.getAttribute("collectionName"+i)+request.getAttribute("parentFolderNameForDuracloud"+i)+"/"+request.getAttribute("baseFileName"+i)+"="+request.getAttribute("fileExtension"+i)+">"+request.getAttribute("alphaNumericName"+i)+"</a></td>");
								} else {
									out.println("<td><a href=MigrateProjectRequestHandler?nameSpace="+request.getAttribute("nameSpace"+i)+"&projectName="+request.getAttribute("projectName")+"&alphaNumericName="+request.getAttribute("alphaNumericName"+i)+"&requestType=view>"+request.getAttribute("alphaNumericName"+i)+"</a></td>");
								}
								out.println("<td>"+request.getAttribute("fileSize"+i)+"</td>");
								out.println("<td>"+request.getAttribute("pid"+i+"fedoraObjectType")+"</td>");
		
							}
						} 

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
							System.out.println("[UploadRequestHandler]currentProjectPid = " + currentProjectPid);
							
							extractTxtPosition = currentProjectPid.indexOf(":") + 1;
							System.out.println("[UploadRequestHandler]extractTxtPosition = " + extractTxtPosition);
							currentProjectPidValue = currentProjectPid.substring(extractTxtPosition, currentProjectPid.length());
							System.out.println("[UploadRequestHandler]currentProjectPidValue = " + currentProjectPidValue);
							System.out.println("[UploadRequestHandler]projectName = " + projectName);
							if ( currentProjectPidValue.equals(projectName)) {
								System.out.println("[UploadRequestHandler]currentProjectPidValue = " + currentProjectPidValue);
								break;
//								inputMetadata.put(currentProjectKey, currentProjectValue);	
							}
							
						}
						ArrayList<DatastreamType> projectMetadata = fedoraServiceManager.getDataStreams(currentProjectPid);
						if (projectMetadata != null) {
							// TODO sort out the indexing on projectMetadata.size()
							// there's something funny about the projectMetadata.size(), so hardcode it as 12
								for (int i=0;i<12;i++) {
									keyPart = projectMetadata.get(i).getDsid();
									valPart = projectMetadata.get(i).getLabel();	
									System.out.println("[UploadRequestHandler] keyPart = " + keyPart);
									System.out.println("[UploadRequestHandler] valPart = " + valPart);
									inputMetadata.put(keyPart, valPart);									
								}
						}
						// TODO the file size on disk is Bytes, but we need it in TB for the
						// drools band selection which is a design weakness to be fixed one day
						storageUsedTot /= 1000000000000.0;
						inputMetadata.put("storageUsed", String.valueOf(storageUsedTot));
						// requests are inferred from accessFrequency
						// transfers are not munged with accessFrequency because makes
						// no difference to the decision on which SP to use
						

						if ( operationFlag .equals("ingest") ) {
							// ingest means take in new data
							// shouldn't be here
							break;
						} else if (operationFlag .equals("migrate-across") ) {
							// migrate-across means remove from one and upload to a cheaper location
							inputMetadata.put("transfersUsed", String.valueOf(storageUsedTot));
							concludedIngestList.clear();
							concludedMigrationList.clear();
							costOpt.suggestService(operationFlag, inputMetadata, concludedIngestList, concludedMigrationList);
							minimalCostCurrency = getCheapestMigrationValueCurrency();
							// TODO bodge this to US dollars for now
							minimalCostCurrency = "US$";
							minimalCost = getCheapestMigrationValue();
							serviceProviderAccount = getCheapestMigrationKey();
						} else if (operationFlag .equals("migrate-down") ) {	
							// migrate-down means data is no longer in active use
						} else if (operationFlag .equals("migrate-up") ) {	
							// migrate-up means data was inactive but is now active, or error recovery
						} else if (operationFlag .equals("retrieval") ) {
							// retrieval means data to be copied down to local disk
							
							// shouldn't be here
							break;
						} 
					} 
				} // while
				
				// now iterate through the same list again to do duracloud/fedora 
				// operations with knowledge of the Service provider and 
				// physical datacentre (= Region) to send it to
//				List /* FileItem */ items = upload.parseRequest(request);
//				List /* FileItem */ userData = upload.parseRequest(request);
				// Process the uploaded items
//				Iterator iter = items.iterator();
				Iterator iter = userData.iterator();
				FileItem fileItem;

				while (iter.hasNext()) {
					fileItem = (FileItem) iter.next();
					
					//Handle the metadata from the form field of "MigrateRequestHandler.java".
					if (fileItem.isFormField()) {
						System.out.println("[MigrateRequestHandler.java] (form field) " + fileItem.getFieldName() + " = " + fileItem.getString());
	
					} 
					//Handle the uploaded file.
					else {
						// Setup a connection with DuraCloud.
						DuraStoreClient duraStoreClient = new DuraStoreClient(configurationFileParser.getKinduraParameters().get("DuraCloudHost"), configurationFileParser.getKinduraParameters().get("DuraCloudPort"), configurationFileParser.getKinduraParameters().get("DuraCloudContext"), configurationFileParser.getKinduraParameters().get("DuraCloudUsername"), configurationFileParser.getKinduraParameters().get("DuraCloudPassword"));
						
						String collectionPID = projectName+":"+collectionName;
							//Get storage decisions from the rule engine.
							List<String> spaceIterator = duraStoreClient.defaultContentStore.getSpaces();
							if (spaceIterator.contains(collectionName)) {
								System.out.println("[UploadRequestHandler] collection name "+collectionName+" exists in DuraCloud.");
								List<String> fedoraCollectionObject = fedoraServiceManager.getObjectPIDs("*"+":"+collectionName, "pid");
								for (String fedoraCollectionIterator : fedoraCollectionObject) {
									if (!fedoraServiceManager.getADataStream(fedoraCollectionIterator, "timeStamp").equals(timeStamp))	{
										System.out.println("[UploadRequestHandler] time stamp does NOT match");
										throw new CollectionExistedException("Collection name "+collectionName+" is already existed. Please use another name.");
									} else {
										System.out.println("[UploadRequestHandler] time stamp matches");
									}
								}
								/*if (fedoraServiceManager.isFedoraObjectExisted(collectionPID) == true) {
									System.out.println("[UploadRequestHandler] new timeStamp: "+timeStamp);
									System.out.println("[UploadRequestHandler] existing timestamp: "+fedoraServiceManager.getADataStream(collectionPID, "timeStamp"));
									if (!fedoraServiceManager.getADataStream(collectionPID, "timeStamp").equals(timeStamp)) {
										System.out.println("[UploadRequestHandler] time stamp does NOT match");
										throw new CollectionExistedException("Collection name "+collectionName+" is already existed. Please use another name.");
									} else {
										System.out.println("[UploadRequestHandler] time stamp matches");
									}
								}*/
							}
						String parentFolderName = null;
						String parentFolderPID = null;
						String fieldName = fileItem.getFieldName();
						String fileName = fileItem.getName();
						//Delete all spaces in the file name.
						fileName = fileName.replace(" ", "");
						String mimeType = fileItem.getContentType();
						String fileNameExtension = fileName.substring(fileName.indexOf(".")+1);
						String fileSize = String.valueOf(fileItem.getSize());				
						//Set the namespace of the content.
						//////String nameSpace = userName;
						String nameSpace = collectionName;
						//Get the base file name of the file.
						int dot = fileName.indexOf(".");
						String baseFileName = fileName.substring(0, dot);
						//Generate the namespace. To create a new Fedora object for each file, use baseFilename. To create a new Fedora object for each data collection, use collectionName. 
						//String nameSpaceAndPid = nameSpace + ":" + baseFilename;
						//String nameSpaceAndPid = nameSpace + ":" + collectionName;
						//String nameSpaceAndPid = userName+":"+projectName;
						//String nameSpaceAndPid = projectName+":"+collectionName;
						
						String filePID = collectionName+":"+filePathOfFedora+"."+baseFileName;
						
						System.out.println("[MigrateRequestHandler.java] File Format: " + fileNameExtension);
						System.out.println("[MigrateRequestHandler.java] Field Name: " + fieldName);
						System.out.println("[MigrateRequestHandler.java] File Name: " + fileName);
						System.out.println("[MigrateRequestHandler.java] MIME Type: " + mimeType);
						System.out.println("[MigrateRequestHandler.java] Size (Bytes): " + fileSize);

						//If we are in chunk mode, we add ".partN" at the end of the file, where N is the chunk number.
						//String uploadedFilename = fileItem.getName() + ( numChunk>0 ? ".part"+numChunk : "") ;
						String uploadedFilename = fileName + ( numChunk>0 ? ".part"+numChunk : "") ;
						uploadFile = new File(tempUploadDirectory + (new File(uploadedFilename)).getName());
						System.out.println("[MigrateRequestHandler.java] File Out: " + uploadFile.toString());
						// write the file
						fileItem.write(uploadFile);	        
						
					
						///////////////////////////////////////////////////////////////////////////////////////////////////////
						//DuraCloud operations.
						//
						filePathOfDuraCloud = duraStoreClient.reviseFilePathForDuracloud(fileOriginalPath);
						//Create a new namespace if the namespace does not exist.
						Map<String, String> spaceMetadata = new HashMap<String, String>();
						if (duraStoreClient.isNameSpaceExisted("Amazon S3", nameSpace) == false) {
							duraStoreClient.createNamespace("Amazon S3", nameSpace, spaceMetadata);
						}
						if (duraStoreClient.isNameSpaceExisted("RackSpace", nameSpace) == false) {
							duraStoreClient.createNamespace("RackSpace", nameSpace, spaceMetadata);
						}
						if (duraStoreClient.isNameSpaceExisted("iRODS", nameSpace) == false) {
							duraStoreClient.createNamespace("iRODS", nameSpace, spaceMetadata);
						}
						
				
						//Upload the file to the Cloud.
						//duraStoreClient.uploadFile(duraStoreClient.defaultContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
						
						duraStoreClient.uploadFile(duraStoreClient.amazonS3ContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
						duraStoreClient.uploadFile(duraStoreClient.rackSpaceContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
						
						//duraStoreClient.uploadFile(duraStoreClient.rackSpaceContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
						
						System.out.println("filePathOfDuraCloud/fileName "+filePathOfDuraCloud+"/"+fileName);
						
						if ((bmpToJPG == true) || (tiffToJPG == true)) {
							System.out.println("[UploadRequestHandler] bmp to jpg is "+bmpToJPG+" tiffToJPG is "+tiffToJPG+". Image transformer is started.");
							DuraServiceClient duraServiceClient = new DuraServiceClient();
							duraServiceClient.runImageTransformerOverSpace(collectionName, collectionName, filePathOfDuraCloud+"/"+fileName, bmpToJPG, tiffToJPG);
							if (isFileConverted(duraStoreClient.defaultContentStore, collectionName, filePathOfDuraCloud+"/"+fileName) == true) {
								System.out.println("[UploadRequestHandler] file "+filePathOfDuraCloud+"/"+fileName+" has been converted. Change file extension in Fedora repository.");
								fileNameExtension = "jpg";
								duraStoreClient.defaultContentStore.deleteContent(collectionName, filePathOfDuraCloud+"/"+fileName);
								System.out.println("[UploadRequestHandler] file "+filePathOfDuraCloud+"/"+fileName+" has been deleted.");
							} else {
								System.out.println("[UploadRequestHandler] file "+filePathOfDuraCloud+"/"+fileName+" has NOT been converted.");
							}
						} 
						//
						// End of DuraCloud operations.
						///////////////////////////////////////////////////////////////////////////////////////////////////////
						
						///////////////////////////////////////////////////////////////////////////////////////////////////////
						//Fedora operations.
						//
						
						fedoraServiceManager.handleCollectionObject(userName, projectName, collectionName, collectionPID, estimatedaccessFrequency, collectionDescription, protectiveMarking, version, timeStamp, minimalCost, minimalCostCurrency, serviceProviderAccount, storageUsedTot, operationFlag);
						fedoraServiceManager.handleCollectionObject(userName, projectName, collectionName, collectionPID, estimatedAccessFrequency, collectionDescription, protectiveMarking, version, timeStamp, "RackSpace");
						
						HashMap<String, String> parentFolderNameAndPID = fedoraServiceManager.handleFolderObject(projectName, collectionName, collectionPID, fileName, fileOriginalPath);
						
						for (Map.Entry<String, String> entry : parentFolderNameAndPID.entrySet()) {
							parentFolderName = entry.getKey();
							parentFolderPID = entry.getValue();
						}
						
						fedoraServiceManager.handleFileObject(nameSpace, projectName, collectionName, parentFolderName, parentFolderPID, baseFileName, baseFileName, filePID, fileOriginalPath, fileNameExtension, fileSize);
						
						//			
						// End of Fedora operations.
						//////////////////////////////////////////////////////////////////////////////////////
						
						
						
						
							
						//////////////////////////////////////////////////////////////////////////////////////
						//Chunk management: if it was the last chunk, let's recover the complete file
						//by concatenating all chunk parts.
						//
						if (bLastChunk) {	        
							System.out.println("[MigrateRequestHandler.java] Last chunk received: let's rebuild the complete file (" + fileName + ")");
							//First: construct the final filename.
							FileInputStream fis;
							FileOutputStream fos = new FileOutputStream(tempUploadDirectory + fileName);
							int nbBytes;
							byte[] byteBuff = new byte[1024];
							for (int i=1; i<=numChunk; i+=1) {
								fileName = fileName + ".part" + i;
								System.out.println("[MigrateRequestHandler.java] " + "  Concatenating " + fileName);
								fis = new FileInputStream(tempUploadDirectory + fileName);
								while ( (nbBytes = fis.read(byteBuff)) >= 0) {
									//out.println("[MigrateRequestHandler.java] " + "     Nb bytes read: " + nbBytes);
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
				}//while
			}
	
			if (generateWarning) {
				out.println("WARNING: just a warning message.\\nOn two lines!");
			}
	
			System.out.println("[MigrateRequestHandler.java] " + "Let's write a status, to finish the server response :");
	
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
		System.out.println("[UploadRequestHandler] converted file name: "+convertedFileName);
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

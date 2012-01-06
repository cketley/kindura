package org.kindura;

import java.io.*;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.request.Ingest;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import com.yourmediashelf.fedora.client.response.IngestResponse;

/**
 * This class is used to handle file upload request.
 * @author Jun Zhang
 */
public class UploadRequestHandler extends HttpServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//Set the temporary folder to store the uploaded file(s).
		//String tempDirectory = "C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/kindura2/uploadtemp/";
		ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
		String tempUploadDirectory = configurationFileParser.getKinduraParameters().get("TempUploadDirectory");
		
		//This String is used to store the value of parameter 'username' sent by upload.jsp.
		String userName = null;
		
		//This String is used to store the value of parameter 'pathinfo0' sent by upload.jsp.
		String projectName = null;
		String collectionName = null;
		String creator = null;
		String collectionDescription = null;
		String storageType = null;
		String actionRequired = null;
		String fileOriginalPath = null;
		String filePathForDuracloud = null;
		String filePathForFedora = null;
		String fileUrl = null;
		
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
		System.out.println("[upload.jsp]  ------------------------------ ");
		System.out.println("[upload.jsp]  Headers of the received request:");
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			System.out.println("[upload.jsp]  " + header + ": " + request.getHeader(header));  	  	
		}
		System.out.println("[upload.jsp]  ------------------------------ "); 

		try {
			// Get URL Parameters.
			Enumeration paraNames = request.getParameterNames();
			System.out.println("[upload.jsp]  ------------------------------ ");
			System.out.println("[upload.jsp]  Parameters: ");
			String parameterName;
			String parameterValue;
			while (paraNames.hasMoreElements()) {
				parameterName = (String)paraNames.nextElement();
				parameterValue = request.getParameter(parameterName);
				System.out.println("[upload.jsp] " + parameterName + " = " + parameterValue);
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
			System.out.println("[upload.jsp]  ------------------------------ ");
	
			int ourMaxMemorySize  = 10000000;
			int ourMaxRequestSize = 2000000000;
	
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			//Some code below is directly taken from the jakarta fileupload common classes
			//All informations, and download, available here : http://jakarta.apache.org/commons/fileupload/
			///////////////////////////////////////////////////////////////////////////////////////////////////////
	
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
				System.out.println("[upload.jsp] ==========================================================================="); 
				System.out.println("[upload.jsp] Sending the received request content: "); 
				InputStream is = request.getInputStream();
				int c;
				while ( (c=is.read()) >= 0) {
					out.write(c);
				}//while
				is.close();
				System.out.println("[upload.jsp] ==========================================================================="); 
			} else if (! request.getContentType().startsWith("multipart/form-data")) {
				System.out.println("[upload.jsp] No parsing of uploaded file: content type is " + request.getContentType()); 
			} else { 
				List /* FileItem */ items = upload.parseRequest(request);
				// Process the uploaded items
				Iterator iter = items.iterator();
				FileItem fileItem;
				//File outputFile;
				System.out.println("[upload.jsp]  Let's read the sent data   (" + items.size() + " items)");
				
				Map<String, String> inputMetadata = new HashMap<String, String>();
					
				while (iter.hasNext()) {
					fileItem = (FileItem) iter.next();
					
					//Handle the metadata from the form field of "upload.jsp".
					if (fileItem.isFormField()) {
						System.out.println("[upload.jsp] (form field) " + fileItem.getFieldName() + " = " + fileItem.getString());
	
						//If we receive the md5sum parameter, we've read finished to read the current file. It's not
						//a very good (end of file) signal. Will be better in the future ... probably !
						//Let's put a separator, to make output easier to read.
						if (fileItem.getFieldName().equals("md5sum[]")) { 
							System.out.println("[upload.jsp]  ------------------------------ ");
						} else if (fileItem.getFieldName().equals("username")) {
							userName = fileItem.getString();
							if (userName == null) {
								response.sendRedirect("sessiontimeout.jsp");
							}
						} else if (fileItem.getFieldName().equals("projectname")) {
							projectName = fileItem.getString();
						} else if (fileItem.getFieldName().equals("collectionname")) {
							collectionName = fileItem.getString();
							if (collectionName.equals("")) {
								throw new NullPointerException("Collection name is null");
							}
						} else if (fileItem.getFieldName().equals("collectiondescription")) {
							collectionDescription = fileItem.getString();
						} else if (fileItem.getFieldName().equals("pathinfo0")) {
							fileOriginalPath = fileItem.getString();
							filePathForDuracloud = reviseFilePathForDuracloud(fileOriginalPath);
							filePathForFedora = reviseFilePathForFedora(fileOriginalPath);
						}    
						
						//Store metadata from the web page "upload.jsp" for the rule engine.
						inputMetadata.put(fileItem.getFieldName(), fileItem.getString());
					} 
					//Handle the uploaded file.
					else {
						//Print out the metadata stored in the Map.
						System.out.println("metadata stored in the Map for the rule engine:");
						for (Map.Entry<String, String> entry : inputMetadata.entrySet()) {
							System.out.println("[Metadata] Key="+entry.getKey()+" Value="+entry.getValue());
						}
						
						//Ok, we've got a file. Let's process it.
						//Again, for all informations of what is exactly a FileItem, please
						//have a look to http://jakarta.apache.org/commons/fileupload/
						//
						String fieldName = fileItem.getFieldName();
						String fileName = fileItem.getName();
						//Delete all spaces in the file name.
						fileName = fileName.replace(" ", "");
						String mimeType = fileItem.getContentType();
						String fileNameExtension = fileName.substring(fileName.indexOf(".")+1);
						long fileSize = fileItem.getSize();				
						//Set the namespace of the content as the username.
						String nameSpace = userName;
						//Get the base file name of the file.
						int dot = fileName.indexOf(".");
						String baseFilename = fileName.substring(0, dot);
						//Generate the namespace. To create a new Fedora object for each file, use baseFilename. To create a new Fedora object for each data collection, use collectionName. 
						//String nameSpaceAndPid = nameSpace + ":" + baseFilename;
						//String nameSpaceAndPid = nameSpace + ":" + collectionName;
						String nameSpaceAndPid = userName+":"+projectName;
						
						System.out.println("[upload.jsp] File Format: " + fileNameExtension);
						System.out.println("[upload.jsp] Field Name: " + fieldName);
						System.out.println("[upload.jsp] File Name: " + fileName);
						System.out.println("[upload.jsp] MIME Type: " + mimeType);
						System.out.println("[upload.jsp] Size (Bytes): " + fileSize);
						
						//If we are in chunk mode, we add ".partN" at the end of the file, where N is the chunk number.
						//String uploadedFilename = fileItem.getName() + ( numChunk>0 ? ".part"+numChunk : "") ;
						String uploadedFilename = fileName + ( numChunk>0 ? ".part"+numChunk : "") ;
						uploadFile = new File(tempUploadDirectory + (new File(uploadedFilename)).getName());
						System.out.println("[upload.jsp] File Out: " + uploadFile.toString());
						// write the file
						fileItem.write(uploadFile);	        
						
						RuleEngineTrigger ruleEngineTrigger = new RuleEngineTrigger();
						ruleEngineTrigger.triggerRuleEngine(inputMetadata, nameSpaceAndPid);
						
						///////////////////////////////////////////////////////////////////////////////////////////////////////
						//DuraCloud operations.
						//

						// Setup a connection with DuraCloud.
						//DuraStoreClient duraStoreClient = new DuraStoreClient("localhost", "8080", "durastore", "root", "rpw");
						DuraStoreClient duraStoreClient = new DuraStoreClient(configurationFileParser.getKinduraParameters().get("DuraCloudHost"), configurationFileParser.getKinduraParameters().get("DuraCloudPort"), configurationFileParser.getKinduraParameters().get("DuraCloudContext"), configurationFileParser.getKinduraParameters().get("DuraCloudUsername"), configurationFileParser.getKinduraParameters().get("DuraCloudPassword"));
						

						//Create a new namespace if the namespace does not exist.
						if (duraStoreClient.isNameSpaceExisted(nameSpace) == false) {
							Map<String, String> spaceMetadata = new HashMap<String, String>();
							
							//spaceMetadata.put("x-amz-storage-class", "STANDARD");
							duraStoreClient.createNamespace(nameSpace, spaceMetadata, "Amazon S3");
						}
						
						/*System.out.println("[upload.jsp] nameSpace+/+fileOriginalPath"+nameSpace+"/"+fileOriginalPath);
						if (duraStoreClient.isNameSpaceExisted(nameSpace+"/"+fileOriginalPath) == false) {
							Map<String, String> spaceMetadata = new HashMap<String, String>();
							
							//spaceMetadata.put("x-amz-storage-class", "STANDARD");
							duraStoreClient.createNamespace(nameSpace+"/"+fileOriginalPath, spaceMetadata, "Amazon S3");
						}*/
						
						//Upload the file to the Cloud.
						duraStoreClient.uploadFile(nameSpace, projectName+"/"+collectionName+"/"+filePathForDuracloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
						//
						// End of DuraCloud operations.
						///////////////////////////////////////////////////////////////////////////////////////////////////////
						
						///////////////////////////////////////////////////////////////////////////////////////////////////////
						//Fedora operations.
						//
										
						//Setup connection with Fedora repository.
						FedoraServiceManager fedoraServiceManager = new FedoraServiceManager();
						//FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection("http://localhost:8080/fedora/", "fedoraAdmin", "fedoraAdmin");
						//FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection(configurationFileParser.getKinduraParameters().get("FedoraHost"), configurationFileParser.getKinduraParameters().get("FedoraUsername"), configurationFileParser.getKinduraParameters().get("FedoraPassword"));
						FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection();
						
						
						
						
						
						//Create Fedora object and store it in Fedora repository.
						fileUrl = "http://localhost:8080/duradmin/download/contentItem?spaceId="+nameSpace+"&contentId="+projectName+"/"+collectionName+"/"+filePathForDuracloud+"/"+fileName+"&storeID=0&attachment=true";
						
						//Generate a random cost value between 0 and 100.
						BigDecimal cost = new BigDecimal(Math.random());
						BigDecimal factor = new BigDecimal(100);
						cost = cost.multiply(factor);
						cost = cost.setScale(2, BigDecimal.ROUND_HALF_UP);
						
						creator = userName;
						
						//Generate a random storage type based on the cost.
						if (Double.valueOf(cost.toString()) > 66.6) {
							storageType = "Fast";
						} else if (Double.valueOf(cost.toString()) > 33.3) {
							storageType = "Medium";
						} else {
							storageType = "Slow";
						}
						
						//Generate a random action.
						double actionValue = Math.random();
						if (actionValue > 0.5) {
							actionRequired = "Migrate";
						} else {
							actionRequired = "None";
						}
						
						
						
						//System.out.println("[upload.jsp] url: " + url);
						//System.out.println("[upload.jsp] mime type: " + mimeType);
						//System.out.println("[upload.jsp] search_term: "+nameSpaceAndPid);
						//fedoraServiceManager.getADataStream(fedoraClient, nameSpaceAndPid, "url");
						
						
						//Create a new fedora object if it does not exist in Fedora repository.
						if (fedoraServiceManager.getObjectPIDs(nameSpaceAndPid, "pid") == null) {
							System.out.println("Object pid does NOT exists");
							IngestResponse ingest = new Ingest(nameSpaceAndPid).execute(fedoraClient);
							FedoraClient.addDatastream(nameSpaceAndPid, baseFilename+".cost").controlGroup("R").dsLabel(cost.toString()).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
							
							
						}
						else {
							System.out.println("Object pid exists");
						}
						
						IngestResponse ingest = new Ingest(nameSpace+":"+collectionName).execute(fedoraClient);
						FedoraClient.addRelationship(nameSpace+":"+collectionName).predicate("isACollectionOf").object(nameSpaceAndPid).isLiteral(true).execute(fedoraClient);
						//FedoraResponse fedoraresponse = fedoraClient.execute(addRelationship(nameSpace+":"+collectionName).subject("subject").predicate("isACollectionOf").object(nameSpaceAndPid));
						
						//Add new datastream(s) to the Fedora object. If the datastream(s) exists, the content in the datastream will be modified according to the new information.
						/*FedoraClient.addDatastream(nameSpaceAndPid, "url").controlGroup("R").dsLabel(url).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, "filenameextension").controlGroup("R").dsLabel(fileNameExtension).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, "cost").controlGroup("R").dsLabel(cost.toString()).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, "filepath").controlGroup("R").dsLabel(fileOriginalPath).dsLocation(url).mimeType(mimeType).execute(fedoraClient);
						*/
						
						/*FedoraClient.addDatastream(nameSpaceAndPid, "creator").controlGroup("R").dsLabel(creator).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, "projectName").controlGroup("R").dsLabel(projectName).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, "collectionDescription").controlGroup("R").dsLabel(collectionDescription).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, "storageType").controlGroup("R").dsLabel(storageType).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, "collectionCost").controlGroup("R").dsLabel(cost.toString()).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, "actionRequired").controlGroup("R").dsLabel(actionRequired).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						*/
						
						/*FedoraClient.addDatastream(nameSpaceAndPid, collectionName+"."+baseFilename+".url").controlGroup("R").dsLabel(fileUrl).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, collectionName+"."+baseFilename+".fileNameExtension").controlGroup("R").dsLabel(fileNameExtension).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, collectionName+"."+baseFilename+".filePath").controlGroup("R").dsLabel(fileOriginalPath).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, collectionName+"."+baseFilename+".fileSize").controlGroup("R").dsLabel(String.valueOf(fileSize)).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						*/
						
						FedoraClient.addDatastream(nameSpaceAndPid, collectionName+"."+filePathForFedora+"."+baseFilename+".url").controlGroup("R").dsLabel(fileUrl).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, collectionName+"."+filePathForFedora+"."+baseFilename+".fileNameExtension").controlGroup("R").dsLabel(fileNameExtension).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, collectionName+"."+filePathForFedora+"."+baseFilename+".filePath").controlGroup("R").dsLabel(fileOriginalPath).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						FedoraClient.addDatastream(nameSpaceAndPid, collectionName+"."+filePathForFedora+"."+baseFilename+".fileSize").controlGroup("R").dsLabel(String.valueOf(fileSize)).dsLocation(fileUrl).mimeType(mimeType).execute(fedoraClient);
						
						
						//			
						// End of Fedora operations.
						//////////////////////////////////////////////////////////////////////////////////////
						
							
						//////////////////////////////////////////////////////////////////////////////////////
						//Chunk management: if it was the last chunk, let's recover the complete file
						//by concatenating all chunk parts.
						//
						if (bLastChunk) {	        
							System.out.println("[upload.jsp] Last chunk received: let's rebuild the complete file (" + fileName + ")");
							//First: construct the final filename.
							FileInputStream fis;
							FileOutputStream fos = new FileOutputStream(tempUploadDirectory + fileName);
							int nbBytes;
							byte[] byteBuff = new byte[1024];
							for (int i=1; i<=numChunk; i+=1) {
								fileName = fileName + ".part" + i;
								System.out.println("[upload.jsp] " + "  Concatenating " + fileName);
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
				}//while
			}
	
			if (generateWarning) {
				out.println("WARNING: just a warning message.\\nOn two lines!");
			}
	
			System.out.println("[upload.jsp] " + "Let's write a status, to finish the server response :");
	
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
	
	private String reviseFilePathForDuracloud(String fileOriginalPath) {
		String revisedFilePath = fileOriginalPath;
		if (revisedFilePath.endsWith("\\")) {
			revisedFilePath = revisedFilePath.substring(0, revisedFilePath.length()-1);
		}
		revisedFilePath = revisedFilePath.replace(":", "");
		System.out.println("[UploadRequestHandler] revised file path 1: "+revisedFilePath);
		revisedFilePath = revisedFilePath.replace("\\", "/");
		System.out.println("[UploadRequestHandler] revised file path 2: "+revisedFilePath);
		revisedFilePath = revisedFilePath.replace(" ", "");
		System.out.println("[UploadRequestHandler] revised file path 3: "+revisedFilePath);
		revisedFilePath = revisedFilePath.toLowerCase();
		System.out.println("[UploadRequestHandler] revised file path 4: "+revisedFilePath);
		return revisedFilePath;
	}
	
	private String reviseFilePathForFedora(String fileOriginalPath) {
		String revisedFilePath = fileOriginalPath;
		if (revisedFilePath.endsWith("\\")) {
			revisedFilePath = revisedFilePath.substring(0, revisedFilePath.length()-1);
		}
		revisedFilePath = revisedFilePath.replace(":", "");
		System.out.println("[UploadRequestHandler] revised file path 1: "+revisedFilePath);
		revisedFilePath = revisedFilePath.replace("\\", ".");
		System.out.println("[UploadRequestHandler] revised file path 2: "+revisedFilePath);
		revisedFilePath = revisedFilePath.replace(" ", "");
		System.out.println("[UploadRequestHandler] revised file path 3: "+revisedFilePath);
		revisedFilePath = revisedFilePath.toLowerCase();
		System.out.println("[UploadRequestHandler] revised file path 4: "+revisedFilePath);
		return revisedFilePath;
	}
}

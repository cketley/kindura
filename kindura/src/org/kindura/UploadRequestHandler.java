package org.kindura;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
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
 * This class is used to handle file upload request.
 * @author Jun Zhang
 */
public class UploadRequestHandler extends HttpServlet {
	//Setup connection with Fedora repository.
	FedoraServiceManager fedoraServiceManager = new FedoraServiceManager();
	FedoraClient fedoraClient = fedoraServiceManager.getFedoraConnection();
	
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
		String collectionDescription = null;
		//String actionRequired = null;
		String fileOriginalPath = null;
		String filePathOfDuraCloud = null;
		String filePathOfFedora = null;
		//String fileUrl = null;
		String estimatedAccessFrequency = null;
		String protectiveMarking = null;
		String version = null;
		String timeStamp = null;
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
						} else if (fileItem.getFieldName().equals("accessfrequency")) {
							estimatedAccessFrequency = fileItem.getString();
						} else if (fileItem.getFieldName().equals("collectiondescription")) {
							collectionDescription = fileItem.getString();
						} else if (fileItem.getFieldName().equals("protectivemarking")) {
							protectiveMarking = fileItem.getString();
						} else if (fileItem.getFieldName().equals("version")) {
							version = fileItem.getString();
						} else if (fileItem.getFieldName().equals("pathinfo0")) {
							fileOriginalPath = fileItem.getString();
						} else if (fileItem.getFieldName().equals("bmptojpg")) {
							bmpToJPG = Boolean.valueOf(fileItem.getString());
							System.out.println("[UploadRequestHandler] bmptojpg: "+bmpToJPG);
						} else if (fileItem.getFieldName().equals("tifftojpg")) {
							tiffToJPG = Boolean.valueOf(fileItem.getString());
							System.out.println("[UploadRequestHandler] tifftojpg: "+tiffToJPG);
						} else if (fileItem.getFieldName().equals("personaldata")) {
							personaldata = Boolean.valueOf(fileItem.getString());
							System.out.println("[UploadRequestHandler] personaldata: "+personaldata);
						} else if (fileItem.getFieldName().equals("timestamp")) {
							timeStamp = fileItem.getString();
							System.out.println("[UploadRequestHandler] timestamp: "+timeStamp);
						}
						
						//Store metadata from the web page "upload.jsp" for the rule engine.
						inputMetadata.put(fileItem.getFieldName(), fileItem.getString());
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
						} else if (estimatedAccessFrequency.equals("")) {
							throw new NullPointerException("Please select the 'Estimated Access Frequency'.");
						} else if (protectiveMarking.equals("")) {
							throw new NullPointerException("Please select the 'Protective Marking'.");
						}
						else {
							List<String> spaceIterator = duraStoreClient.defaultContentStore.getSpaces();
							if (spaceIterator.contains(collectionName)) {
								System.out.println("[UploadRequestHandler] collection name "+collectionName+" exists in Dura Cloud.");
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
						}
						
						//Print out the metadata stored in the Map.
						System.out.println("metadata stored in the Map for the rule engine:");
						for (Map.Entry<String, String> entry : inputMetadata.entrySet()) {
							System.out.println("[Metadata] Key="+entry.getKey()+" Value="+entry.getValue());
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
						ruleEngineTrigger.triggerRuleEngine(inputMetadata, collectionPID);
						
						
						///////////////////////////////////////////////////////////////////////////////////////////////////////
						//DuraCloud operations.
						//

						
						filePathOfDuraCloud = duraStoreClient.reviseFilePathForDuracloud(fileOriginalPath);
						//Create a new namespace if the namespace does not exist.
						if (duraStoreClient.isNameSpaceExisted(nameSpace) == false) {
							Map<String, String> spaceMetadata = new HashMap<String, String>();
							duraStoreClient.createNamespace(nameSpace, spaceMetadata, "Amazon S3");
						}
						
						//Upload the file to the Cloud.
						duraStoreClient.uploadFile(duraStoreClient.defaultContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
						
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
						
						fedoraServiceManager.handleCollectionObject(userName, projectName, collectionName, collectionPID, estimatedAccessFrequency, collectionDescription, protectiveMarking, version, timeStamp);
						
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
}

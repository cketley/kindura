package org.kindura;


/*
Copyright 2012 Cheney Ketley, employee of Science & Technology Facilities Council and
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
import org.apache.commons.codec.binary.Hex;

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

	private static final boolean debug = false;
	private static final boolean verbose = true;

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
		String expiryDate = null;
		String researchfunder = null;
		boolean bmpToJPG = false;
		boolean tiffToJPG = false;
		boolean personaldata = false;
		File uploadFile = null;

		String nameSpace = "";

		String migrationData = "";
		Double suggestedMigrationVal = 0.0;
		String suggestedCurr = "";
		String suggestedSP = "";
		String suggestedReg = "";
		String suggestedPayPlan = "";
		String suggestedReplicas = "";

		String dropData = "";
		Double dropseyMigrationVal = 0.0;
		String dropseyCurr = "";
		String dropseySP = "";
		String dropseyReg = "";
		String dropseyPayPlan = "";
		String dropseyReplicas = "";

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

			Map<String, String> inputMetadata = new HashMap<String, String>();
			LinkedList<String> concludedIngestList = new LinkedList<String>();
			LinkedList<String> concludedMigrationList = new LinkedList<String>();
			LinkedList<String> concludedDropList = new LinkedList<String>();

			String serviceProviderAccount1 = "";
			String serviceProviderAccount2 = "";
			String serviceProviderAccount3 = "";
			String serviceProviderAccount4 = "";
			String serviceProviderAccount5 = "";
			String serviceProviderAccount6 = "";
			Double minimalCost = 0.0;
			String minimalCostCurrency = "";

			// Setup a connection with DuraCloud.
			DuraStoreClient duraStoreClient = new DuraStoreClient(configurationFileParser.getKinduraParameters().get("DuraCloudHost"), configurationFileParser.getKinduraParameters().get("DuraCloudPort"), configurationFileParser.getKinduraParameters().get("DuraCloudContext"), configurationFileParser.getKinduraParameters().get("DuraCloudUsername"), configurationFileParser.getKinduraParameters().get("DuraCloudPassword"));

			List<String> projectNames = fedoraServiceManager.getprojectNames();
			if (projectNames != null) {
				Iterator<String> iterator = projectNames.iterator();
				String nextprojectName = null;
				while (iterator.hasNext()) {
					nextprojectName = (String)iterator.next();

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
						System.out.println("[MigrateRequestHandler]currentProjectPid = " + currentProjectPid);

						extractTxtPosition = currentProjectPid.indexOf(":") + 1;
						System.out.println("[MigrateRequestHandler]extractTxtPosition = " + extractTxtPosition);
						currentProjectPidValue = currentProjectPid.substring(extractTxtPosition, currentProjectPid.length());
						System.out.println("[MigrateRequestHandler]currentProjectPidValue = " + currentProjectPidValue);
						//							System.out.println("[MigrateRequestHandler]nextprojectName = " + nextprojectName);
						if ( currentProjectPidValue.equals(nextprojectName)) {
							System.out.println("[MigrateRequestHandler]currentProjectPidValue = " + currentProjectPidValue);
							break;
							//								inputMetadata.put(currentProjectKey, currentProjectValue);	
						}

					}

					// this retrieves all data about the Project from Fedora
					ArrayList<DatastreamType> projectMetadata = fedoraServiceManager.getDataStreams(currentProjectPid);
					if (projectMetadata != null) {
						// TODO sort out the indexing on projectMetadata.size()
						// there's something funny about the projectMetadata.size(), so hardcode it as 12
						for (int i=0;i<projectMetadata.size();i++) {
							//							for (int i=0;i<12;i++) {
							keyPart = projectMetadata.get(i).getDsid();
							valPart = projectMetadata.get(i).getLabel();	
							System.out.println("[MigrateRequestHandler] Project keyPart = " + keyPart);
							System.out.println("[MigrateRequestHandler] Project valPart = " + valPart);
							inputMetadata.put(keyPart, valPart);									
						}
						// retrieve all Collection PIDS for the Project
						List<String> collectionPids = fedoraServiceManager.getCollectionObjectPIDs(currentProjectPid);
						Iterator<String> lookup = collectionPids.iterator();
						String currentCollectionPid = "";
						String currentCollectionPidValue = "";
						while (lookup.hasNext()) {
							currentCollectionPid = (String)lookup.next();
							System.out.println("[MigrateRequestHandler]currentCollectionPid = " + currentCollectionPid);

							extractTxtPosition = currentCollectionPid.indexOf(":") + 1;
							System.out.println("[MigrateRequestHandler]extractTxtPosition = " + extractTxtPosition);
							currentCollectionPidValue = currentCollectionPid.substring(extractTxtPosition, currentCollectionPid.length());
							System.out.println("[MigrateRequestHandler]currentCollectionPidValue = " + currentCollectionPidValue);

							String collectionPID = currentCollectionPid;
							collectionName = currentCollectionPidValue;

							// this retrieves all data about the Collection from Fedora
							ArrayList<DatastreamType> collectionMetadata = fedoraServiceManager.getDataStreams(collectionPID);
							if (collectionMetadata != null) {
								// TODO sort out the indexing on collectionMetadata.size()
								// there's something funny about the collectionMetadata.size(), so hardcode it as 23
								for (int i=0;i<collectionMetadata.size();i++) {
									//								for (int i=0;i<23;i++) {
									keyPart = collectionMetadata.get(i).getDsid();
									valPart = collectionMetadata.get(i).getLabel();	
									System.out.println("[MigrateRequestHandler] Collection keyPart = " + keyPart);
									System.out.println("[MigrateRequestHandler] Collection valPart = " + valPart);
									inputMetadata.put(keyPart, valPart);									
								}

								if ( ! ( inputMetadata.get( "collectionTotSize" ) == null) ) {
									if ( ! inputMetadata.get( "collectionTotSize" ) .isEmpty()) {
										storageUsedTot = Double.valueOf(inputMetadata.get( "collectionTotSize" ).toString())  / 1000000000000.0;
									} else {
										storageUsedTot = 0.0;
									}
								} else {
									storageUsedTot = 0.0;
								}				
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
									concludedDropList.clear();

									costOpt.suggestService(operationFlag, inputMetadata, concludedIngestList, concludedMigrationList, concludedDropList);


									if (verbose) {

										migrationData = "";
										suggestedMigrationVal = 0.0;
										suggestedCurr = "";
										suggestedSP = "";
										suggestedReg = "";
										suggestedPayPlan = "";
										suggestedReplicas = "";

										Iterator wrigglelator = concludedMigrationList.iterator();
										while (wrigglelator.hasNext()) {

											migrationData = (String)wrigglelator.next();
											suggestedMigrationVal = Double.valueOf(costOpt.popString(migrationData, 1));
											suggestedCurr = costOpt.popString(migrationData, 6);
											suggestedSP = costOpt.popString(migrationData, 2);
											suggestedReg = costOpt.popString(migrationData, 3);
											suggestedPayPlan = costOpt.popString(migrationData, 4);
											// this is how many copies are offered by Service Provider
											suggestedReplicas = costOpt.popString(migrationData, 5);
											System.out.println("[MigrateRequestHandler] Migrate to : --------------- " );

											System.out.println("[MigrateRequestHandler] suggestedSP = " + suggestedSP );
											System.out.println("[MigrateRequestHandler] suggestedReg = " + suggestedReg );
											System.out.println("[MigrateRequestHandler] suggestedPayPlan = " + suggestedPayPlan );
											System.out.println("[MigrateRequestHandler] suggestedReplicas = " + suggestedReplicas );
											System.out.println("[MigrateRequestHandler] suggestedIngestVal = " + suggestedMigrationVal );
											System.out.println("[MigrateRequestHandler] suggestedCurr = " + suggestedCurr );

										}							

										dropData = "";
										dropseyMigrationVal = 0.0;
										dropseyCurr = "";
										dropseySP = "";
										dropseyReg = "";
										dropseyPayPlan = "";
										dropseyReplicas = "";

										Iterator wrigglism = concludedDropList.iterator();
										while (wrigglism.hasNext()) {

											dropData = (String)wrigglism.next();
											dropseyMigrationVal = Double.valueOf(costOpt.popString(dropData, 1));
											dropseyCurr = costOpt.popString(dropData, 6);
											dropseySP = costOpt.popString(dropData, 2);
											dropseyReg = costOpt.popString(dropData, 3);
											dropseyPayPlan = costOpt.popString(dropData, 4);
											// this is how many copies are offered by Service Provider
											dropseyReplicas = costOpt.popString(dropData, 5);
											System.out.println("[MigrateRequestHandler] Migrate from : --------------- " );

											System.out.println("[MigrateRequestHandler] dropseySP = " + dropseySP );
											System.out.println("[MigrateRequestHandler] dropseyReg = " + dropseyReg );
											System.out.println("[MigrateRequestHandler] dropseyPayPlan = " + dropseyPayPlan );
											System.out.println("[MigrateRequestHandler] dropseyReplicas = " + dropseyReplicas );
											System.out.println("[MigrateRequestHandler] dropseyIngestVal = " + dropseyMigrationVal );
											System.out.println("[MigrateRequestHandler] dropseyCurr = " + dropseyCurr );

										}		

									}

									///////////////////////////////////////////////////////////////////////////////////////////////////////
									//DuraCloud operations.
									//

									nameSpace = inputMetadata.get("collectionName");

									migrationData = "";
									suggestedMigrationVal = 0.0;
									suggestedCurr = "";
									suggestedSP = "";
									suggestedReg = "";
									suggestedPayPlan = "";
									suggestedReplicas = "";
									dropData = "";
									dropseyMigrationVal = 0.0;
									dropseyCurr = "";
									dropseySP = "";
									dropseyReg = "";
									dropseyPayPlan = "";
									dropseyReplicas = "";

									Iterator wandalita = concludedDropList.iterator();
									Iterator wanderator = concludedMigrationList.iterator();
									while (wanderator.hasNext()) {

										migrationData = (String)wanderator.next();
										if (wandalita.hasNext()) {
											// TODO allow many-to-many drops versus adds.
											// Some eagle-eyed observers will have noticed that if we are dropping more than we are adding then some old
											// data will remain in Fedora because the loop is driven by the adds. We really ought to fix this sometime.
											dropData = (String)wandalita.next();
										}

										//String hexString = Hex.encodeHexString(myString.getBytes(/* charset */));

										suggestedMigrationVal = Double.valueOf(costOpt.popString(migrationData, 1));
										suggestedCurr = costOpt.popString(migrationData, 6);
										suggestedSP = costOpt.popString(migrationData, 2);
										suggestedReg = costOpt.popString(migrationData, 3);
										suggestedPayPlan = costOpt.popString(migrationData, 4);
										// this is how many copies are offered by Service Provider
										suggestedReplicas = costOpt.popString(migrationData, 5);

										if (concludedDropList.size() < 1) {
											// hopefully the update of dura will be ignored if from equals to SP
											dropseyMigrationVal = 0.0;
											dropseySP = suggestedSP;
											dropseyReg = suggestedReg;
											dropseyPayPlan = suggestedPayPlan;
										} else {
											dropseyMigrationVal = Double.valueOf(costOpt.popString(dropData, 1));
											dropseySP = costOpt.popString(dropData, 2);
											dropseyReg = costOpt.popString(dropData, 3);
											dropseyPayPlan = costOpt.popString(dropData, 4);
										}

										// this munges the filepath of the file so fedora can handle it correctly
										// but we don't use it at the moment because we don't have a way to remove files
// 											filePathOfDuraCloud = duraStoreClient.reviseFilePathForDuracloud(fileOriginalPath);
										//Create a new namespace if the namespace does not exist.
										// The nameSpace variable passed across is actually the collection name.
										Map<String, String> spaceMetadata = new HashMap<String, String>();
										if ( (suggestedSP .equals("Amazon S3")) && (suggestedPayPlan .equals("Pay as you go - reduced redundancy")) ) {
											if (duraStoreClient.isNameSpaceExisted("Amazon S3 RRS", nameSpace) == false) {
												duraStoreClient.createNamespace("Amazon S3 RRS", nameSpace, spaceMetadata);								
											}
											// TODO we don't have any way to delete from a service provider at the moment
											//									duraStoreClient.removeFile(duraStoreClient.amazonS3RRSContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
										}
										if ( (suggestedSP .equals("Amazon S3")) &&  ! (suggestedPayPlan .equals("Pay as you go - reduced redundancy"))  ) {
											if (duraStoreClient.isNameSpaceExisted("Amazon S3", nameSpace) == false) {
												duraStoreClient.createNamespace("Amazon S3", nameSpace, spaceMetadata);
											}
											//									duraStoreClient.removeFile(duraStoreClient.amazonS3ContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
										}
										if ( (suggestedSP .equals("Rackspace Cloud Files")) ) {
											if (duraStoreClient.isNameSpaceExisted("Rackspace Cloud Files", nameSpace) == false) {
												duraStoreClient.createNamespace("Rackspace Cloud Files", nameSpace, spaceMetadata);									
											}
											//									duraStoreClient.removeFile(duraStoreClient.rackSpaceContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
										}
										if ( (suggestedSP .equals("iRODS")) ) {
											if (duraStoreClient.isNameSpaceExisted("iRODS", nameSpace) == false) {
												duraStoreClient.createNamespace("iRODS", nameSpace, spaceMetadata);									
											}
											//									duraStoreClient.removeFile(duraStoreClient.iRODSContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
										}
										if ( (suggestedSP .equals("Google Cloud Storage")) ) {
											if (duraStoreClient.isNameSpaceExisted("Google Cloud Storage", nameSpace) == false) {
												duraStoreClient.createNamespace("Google Cloud Storage", nameSpace, spaceMetadata);
											}
											//									duraStoreClient.removeFile(duraStoreClient.googleCloudStorageContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
										}
										if ( (suggestedSP .equals("Azure")) ) {
											if (duraStoreClient.isNameSpaceExisted("Azure", nameSpace) == false) {
												duraStoreClient.createNamespace("Azure", nameSpace, spaceMetadata);									
											}
											//									duraStoreClient.removeFile(duraStoreClient.azureContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
										}
										if ( (suggestedSP .equals("SDSC")) ) {
											if (duraStoreClient.isNameSpaceExisted("SDSC", nameSpace) == false) {
												duraStoreClient.createNamespace("SDSC", nameSpace, spaceMetadata);
											}
											//									duraStoreClient.removeFile(duraStoreClient.sdscContentStore, nameSpace, filePathOfDuraCloud+"/"+fileName, uploadFile, fileItem.getSize(), fileItem.getContentType());
										}

										// migrate all files for the collection
										duraStoreClient.migrateDataCollection( dropseySP, nameSpace, suggestedSP, nameSpace);


									}
									//
									// End of DuraCloud operations.
									///////////////////////////////////////////////////////////////////////////////////////////////////////


									///////////////////////////////////////////////////////////////////////////////////////////////////////
									//Fedora operations.
									//

									migrationData = "";
									suggestedMigrationVal = 0.0;
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
									Iterator wanderise = concludedMigrationList.iterator();
									while (wanderise.hasNext()) {

										migrationData = (String)wanderise.next();

										suggestedMigrationVal = Double.valueOf(costOpt.popString(migrationData, 1));
										suggestedCurr = costOpt.popString(migrationData, 6);
										suggestedSP = costOpt.popString(migrationData, 2);
										suggestedReg = costOpt.popString(migrationData, 3);
										suggestedPayPlan = costOpt.popString(migrationData, 4);
										// this is how many copies are offered by Service Provider
										suggestedReplicas = costOpt.popString(migrationData, 5);

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
											break;
										}
										spCount++;

										expiryDate = inputMetadata.get("collectionStgExpiryDate");

										// TODO there doesnt seem to be a way to delete orphaned migrated data left in Fedora
										fedoraServiceManager.handleCollectionObject(userName, projectName, collectionName, collectionPID, estimatedaccessFrequency, collectionDescription, protectiveMarking, version, timeStamp, suggestedSP, suggestedMigrationVal, suggestedCurr, serviceProviderAccount1, serviceProviderAccount2, serviceProviderAccount3, serviceProviderAccount4, serviceProviderAccount5, serviceProviderAccount6, storageUsedTot, operationFlag, expiryDate);

										// the data files are not altered within Fedora, only Durastore

									}
									//			
									// End of Fedora operations.
									//////////////////////////////////////////////////////////////////////////////////////

								} else if (operationFlag .equals("migrate-down") ) {	
									// migrate-down means data is no longer in active use
									// so we would use the collectionStgExpiryDate from Fedora to locate and remove it
								} else if (operationFlag .equals("migrate-up") ) {	
									// migrate-up means data was inactive but is now active, or error recovery
								} else if (operationFlag .equals("retrieval") ) {
									// retrieval means data to be copied down to local disk

									// shouldn't be here
									break;
								} 
							} 
						}
					}

				}
			}


			System.out.println("[MigrateRequestHandler.java] " + "Let's write a status, to finish the server response :");
			out.println("SUCCESS");
			System.out.println("SUCCESS");

		}
		catch(Exception e){
			System.out.println("");
			out.println("ERROR: Exception e = " + e.toString());
			System.out.println("ERROR: Exception e = " + e.toString());
			System.out.println("");
			//response.sendRedirect("sessiontimeout.jsp");
		}
	}


	public boolean isFileConverted(ContentStore contentStore, String collectionName, String originalFileName) {
		String convertedFileName = originalFileName.substring(0, originalFileName.lastIndexOf("."))+".jpg";
		System.out.println("[MigrateRequestHandler] converted file name: "+convertedFileName);
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

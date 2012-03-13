package org.kindura;
/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreImpl;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class is used to connect DuraStore REST API.
 * @author Jun Zhang
 */
public class DuraStoreClient {
    ContentStoreManager contentStoreManager;
    ContentStore defaultContentStore;
    ContentStore amazonS3ContentStore;
    ContentStore rackSpaceContentStore;
    Credential duracloudCredential;
        
    private Document dom;
    
    public DuraStoreClient(String duracloud_host, String duracloud_port, String duracloud_context, String duracloud_username, String duracloud_password) throws ContentStoreException {
    	contentStoreManager = new ContentStoreManagerImpl(duracloud_host, duracloud_port, duracloud_context);
    	//contentStoreManager = new ContentStoreManagerImpl("kindura.duracloud.org", "443", "durastore");
        duracloudCredential = new Credential(duracloud_username, duracloud_password);
    	//duracloudCredential = new Credential("junz", "p1ssw2rd");
        contentStoreManager.login(duracloudCredential);
        defaultContentStore = contentStoreManager.getPrimaryContentStore();

        amazonS3ContentStore = contentStoreManager.getContentStore("0");
        rackSpaceContentStore = contentStoreManager.getContentStore("1");
        
    }
    
    public boolean isCloudProviderExisted(String cloudProviderName) {
		//ContentStore contentStore = contentStoreManager.getContentStore("32");
		String cloudProviderID = getCloudProviderID("localhost", cloudProviderName);
		if (cloudProviderID != null) {
			System.out.println("[DuraStoreClient] cloudProviderID: "+cloudProviderID);
			return true;
		} else {
			System.out.println("[DuraStoreClient] cloudProvider does NOT exist");
			return false;
		}
    }
    
    public boolean isNameSpaceExisted(String cloudProviderName, String nameSpace) {
    	List<String> storeIDs = null;
		try {
			//storeIDs = defaultContentStore.getSpaces();
			if (isCloudProviderExisted(cloudProviderName) == true) {
				if (cloudProviderName.equals("Amazon S3")) {
					storeIDs = amazonS3ContentStore.getSpaces();
				}
				else if (cloudProviderName.equals("RackSpace")) {
					storeIDs = rackSpaceContentStore.getSpaces();
				}
			}
			
			Iterator iterator = storeIDs.iterator();
	    	boolean spaceFound = false;
	    	while(iterator.hasNext() && (spaceFound == false)) {

	    	    String name = (String)iterator.next(); 
	    	    if (name.equals(nameSpace)) {
	    	    	spaceFound = true;
	    	    	System.out.println("space "+nameSpace+" exist");
	    	    	return true;
	    	    }
	    	}
	    	System.out.println("space "+nameSpace+" does not exist");
	    	return false;
		} catch (ContentStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
		return false;
    }
    
    public String getCloudProviderID(String duracloud_host, String cloudProviderName) {
    	int cloudProviderID;
    	System.out.println("[DuraStoreClient] cloud provider name: "+cloudProviderName);
    	if (duracloud_host.equals("localhost") || duracloud_host.equals("127.0.0.1") || duracloud_host.equals("137.73.172.82")) {
    		if (cloudProviderName.equals("Amazon S3")) {
        		cloudProviderID = 0;
        		return String.valueOf(cloudProviderID);
        	}
    		else if (cloudProviderName.equals("RackSpace")) {
    			cloudProviderID = 1;
    			return String.valueOf(cloudProviderID);
    		}
    	} else if (duracloud_host.equals("kindura.duracloud.org")) {
    		if (cloudProviderName.equals("Amazon S3")) {
        		cloudProviderID = 32;
        		return String.valueOf(cloudProviderID);
        	} else if (cloudProviderName.equals("RackSpace")) {
        		cloudProviderID = 33;
        		return String.valueOf(cloudProviderID);
        	}
    	}
    	return null;
    }
    
    public ContentStore getCloudContentStore(String cloud_host, String cloud_port, String cloud_context, String cloudProviderID, String cloud_username, String cloud_password) {
    	try {
        	ContentStoreManager contentStoreManager = new ContentStoreManagerImpl(cloud_host, cloud_port, cloud_context);
            ContentStore contentStore;
            Credential duracloudCredential;
            contentStoreManager = new ContentStoreManagerImpl(cloud_host, cloud_port, cloud_context);
        	//contentStoreManager = new ContentStoreManagerImpl("kindura.duracloud.org", "443", "durastore");
            duracloudCredential = new Credential(cloud_username, cloud_password);
        	//duracloudCredential = new Credential("junz", "p1ssw2rd");
            contentStoreManager.login(duracloudCredential);
			contentStore = contentStoreManager.getContentStore(cloudProviderID);
			return contentStore;
		} catch (ContentStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    public void createNamespace(String cloudProviderName, String nameSpace, Map<String, String> spaceMetadata) {
    	//Map<String, String> spaceMetadata = new HashMap<String, String>();
		try {
			//contentStore = contentStoreManager.getPrimaryContentStore();
			//defaultContentStore.createSpace(nameSpace, spaceMetadata);
			if (isCloudProviderExisted(cloudProviderName) == true) {
				if (cloudProviderName.equals("Amazon S3")) {
					amazonS3ContentStore.createSpace(nameSpace, spaceMetadata);
				}
				else if (cloudProviderName.equals("RackSpace")) {
					rackSpaceContentStore.createSpace(nameSpace, spaceMetadata);
				}
			}
			//rackSpaceContentStore.createSpace(nameSpace, spaceMetadata);
		} catch (ContentStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void uploadFile(ContentStore contentStore, String nameSpace, String fileName, File uploadFile, long fileSize, String fileType) {
    	FileInputStream in;
		try {
			in = new FileInputStream(uploadFile.toString());
			Map<String, String> contentMetadata = new HashMap<String, String>();
			contentMetadata.put(nameSpace, fileName);
			//contentMetadata.put("x-amz-storage-class", "STANDARD");
			contentStore.addContent(nameSpace, fileName, in, fileSize, fileType, null, contentMetadata);
			
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ContentStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		contentStoreManager.logout();
    }
    
    public boolean downloadFile(ContentStore contentStore, String nameSpace, String fileName, String revisedFileNameForDownload, String destinationPath, String fileExtension, int numberOfBytes) {
    	Content content = new Content();
    	try {
			if (fileExtension != "") {
				content = contentStore.getContent(nameSpace, fileName+"."+fileExtension);
			} else {
				content= contentStore.getContent(nameSpace, fileName);
			}
	    	System.out.println("[DuraStoreClient] content.getId(): "+content.getId());
	    	InputStream inputStream = content.getStream();
	    	//System.out.println("[DuraStoreClient] content.getMetadata: "+content.getMetadata());
	    	File destinationFile = new File(destinationPath+revisedFileNameForDownload+"."+fileExtension);
	    	System.out.println("[DuraStoreClient] destinationFile: "+destinationPath+revisedFileNameForDownload+"."+fileExtension);
	    	FileOutputStream outputStream = new FileOutputStream(destinationFile);
	    	byte[] readData = new byte[numberOfBytes];
			int i = inputStream.read(readData);
			while (i != -1) {
				outputStream.write(readData, 0, i);
				i = inputStream.read(readData);
			}
			inputStream.close();
			outputStream.close();
			return true;
		}
    	catch (ContentStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
    }
    
    public String reviseFilePathForDuracloud(String fileOriginalPath) {
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
    
    public Iterator<String> getFileNames(ContentStore contentStore, String nameSpace) {
    	try {
			Iterator spaceContents = contentStore.getSpaceContents(nameSpace);
			
			/*while (spaceContents.hasNext()) {
				String fileName = (String)spaceContents.next();
				System.out.println("[DuraStoreClient] "+nameSpace+" file:"+fileName);
			}*/
			return spaceContents;
		} catch (ContentStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    public void migrateDataCollection(String originalCloudProvider, String originalNameSpace, String newCloudProvider, String newNameSpace) {
    	if (isCloudProviderExisted(originalCloudProvider) == true) {
    		System.out.println("[DuraStoreClient] original cloud provider: "+originalCloudProvider+" exists.");
    		if (isCloudProviderExisted(newCloudProvider) == true) {
        		System.out.println("[DuraStoreClient] new cloud provider: "+newCloudProvider+" exists.");
        		System.out.println("Data collection "+originalNameSpace+" from "+originalCloudProvider+" will be migrated to the space "+newNameSpace+" of "+newCloudProvider);
        		JFrame frame = new JFrame();
        		JOptionPane.showMessageDialog(frame, "Data collection from the space '"+originalNameSpace+"' of '"+originalCloudProvider+"' will be migrated to the space '"+newNameSpace+"' of '"+newCloudProvider+"'.");
        		String fullFileName = null;
        		String baseFileName = null;
        		String fileExtension = null;
        		//Iterator spaceContents = getFileNames(defaultContentStore, originalNameSpace);
        		Iterator spaceContents = null;
        		if (originalCloudProvider.equals("Amazon S3")) {
        			spaceContents = getFileNames(amazonS3ContentStore, originalNameSpace);
        		}
        		if (originalCloudProvider.equals("RACKSPACE")) {
        			spaceContents = getFileNames(rackSpaceContentStore, originalNameSpace);
        		}
        		
				ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
				String tempDownloadDirectory = configurationFileParser.getKinduraParameters().get("TempDownloadDirectory");
				System.out.println("[DuraStoreClient] Download directory: "+tempDownloadDirectory);
				//String displayDownloadDirectory = configurationFileParser.getKinduraParameters().get("DisplayDownloadDirectory");
				
				while (spaceContents.hasNext()) {
					fullFileName = (String)spaceContents.next();
					if (fullFileName.contains(".")) {
						baseFileName = fullFileName.substring(0, fullFileName.indexOf("."));
						fileExtension = fullFileName.substring(fullFileName.indexOf(".")+1);
					} else {
						baseFileName = fullFileName;
						fileExtension = "";
					}
					//duraStoreClient.downloadFile("root", nameSpaceAndPid[1], revisedFileNameForDownload, tempDownloadDirectory, fileExtension, Integer.valueOf(configurationFileParser.getKinduraParameters().get("NumberOfBytes")));
					System.out.println("[duraStoreClient] Start to download file: "+fullFileName);
					    
					//downloadFile(defaultContentStore, originalNameSpace, baseFileName, baseFileName, tempDownloadDirectory, fileExtension, Integer.valueOf(configurationFileParser.getKinduraParameters().get("NumberOfBytes")));
					if (originalCloudProvider.equals("Amazon S3")) {
						downloadFile(amazonS3ContentStore, originalNameSpace, baseFileName, baseFileName, tempDownloadDirectory, fileExtension, Integer.valueOf(configurationFileParser.getKinduraParameters().get("NumberOfBytes")));
					}
	        		if (originalCloudProvider.equals("RackSpace")) {
	        			downloadFile(rackSpaceContentStore, originalNameSpace, baseFileName, baseFileName, tempDownloadDirectory, fileExtension, Integer.valueOf(configurationFileParser.getKinduraParameters().get("NumberOfBytes")));
					}
					
					File uploadFile = new File(tempDownloadDirectory + (new File(fullFileName)).getName());
					
					uploadFile(defaultContentStore, newNameSpace, fullFileName, uploadFile, uploadFile.length(), "text/plain");
					if (originalCloudProvider.equals("Amazon S3")) {
						uploadFile(amazonS3ContentStore, newNameSpace, fullFileName, uploadFile, uploadFile.length(), "text/plain");
					}
	        		if (originalCloudProvider.equals("RackSpace")) {
	        			uploadFile(rackSpaceContentStore, newNameSpace, fullFileName, uploadFile, uploadFile.length(), "text/plain");
					}
	        		
				}
				
				
				
    		} else {
        		System.out.println("[DuraStoreClient] new cloud provider: "+newCloudProvider+" does NOT exist");
        	}
    	} else {
    		System.out.println("[DuraStoreClient] original cloud provider: "+originalCloudProvider+" does NOT exist");
    	}
    }
}

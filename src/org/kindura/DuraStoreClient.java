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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class is used to connect DuraStore REST API.
 * @author Jun Zhang
 */
public class DuraStoreClient {
    ContentStore contentStore;
    ContentStoreManager contentStoreManager;
    Credential duracloudCredential;
        
    private Document dom;
    
    public DuraStoreClient(String duracloud_host, String duracloud_port, String duracloud_context, String duracloud_username, String duracloud_password) throws ContentStoreException {
    	contentStoreManager =
            new ContentStoreManagerImpl(duracloud_host, duracloud_port, duracloud_context);
        duracloudCredential = new Credential(duracloud_username, duracloud_password);
        contentStoreManager.login(duracloudCredential);
        contentStore = contentStoreManager.getPrimaryContentStore();
    }
    
    public boolean isNameSpaceExisted(String namespace) throws ContentStoreException {
    	List<String> storeIDs = contentStore.getSpaces();
    	Iterator iterator = storeIDs.iterator();
    	boolean spaceFound = false;
    	while(iterator.hasNext() && (spaceFound == false)) {

    	    String name = (String)iterator.next(); 
    	    if (name.equals(namespace)) {
    	    	spaceFound = true;
    	    	System.out.println("space "+namespace+" exist");
    	    	return true;
    	    }
    	}
    	System.out.println("space "+namespace+" does not exist");
    	return false;
    }
    
    public void createNamespace(String nameSpace, Map<String, String> spaceMetadata, String cloudProvider) {
    	//Map<String, String> spaceMetadata = new HashMap<String, String>();
		try {
			//contentStore = contentStoreManager.getPrimaryContentStore();
			contentStore.createSpace(nameSpace, spaceMetadata);
		} catch (ContentStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void uploadFile(String nameSpace, String fileName, File uploadFile, long fileSize, String fileType) {
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
    
    
    
    public void downloadFile(String nameSpace, String fileName, String revisedFileNameForDownload, String destinationPath, String fileExtension, int numberOfBytes) {
    	Content content = new Content();
    	try {
			content = contentStore.getContent(nameSpace, fileName+"."+fileExtension); 
	    	System.out.println("[DuraStoreClient] content.getId(): "+content.getId());
	    	InputStream inputStream = content.getStream();
	    	System.out.println("[DuraStoreClient] content.getMetadata: "+content.getMetadata());
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
		}
    	catch (ContentStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

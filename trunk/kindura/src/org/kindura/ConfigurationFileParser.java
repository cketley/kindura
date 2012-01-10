package org.kindura;



import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is used to parse the configuration file "kinduraconfiguration.xml".
 * @author Jun Zhang
 */
public class ConfigurationFileParser extends DefaultHandler{

	private String tempValue;
	
	private Map<String, String> kinduraParameters;
	
	//public ConfigurationFileParser(){
		
	//}
	
	private void parseDocument() {
		
		//get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
		
			//get a new instance of parser
			SAXParser sp = spf.newSAXParser();
			
			//parse the file and also register this class for call backs
			sp.parse("webapps/kindura6/kinduraconfiguration.xml", this);
			
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	//Event Handlers
	public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) throws SAXException {
			    
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		tempValue = new String(ch,start,length);
	}
	
	public void endElement(String uri, String localName, String qualifiedName) throws SAXException {
		kinduraParameters.put(qualifiedName, tempValue);
	}
	
	public Map<String, String> getKinduraParameters() {
		kinduraParameters = new HashMap<String, String>();
		parseDocument();
		return kinduraParameters;
	}
}
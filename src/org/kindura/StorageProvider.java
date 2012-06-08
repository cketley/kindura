package org.kindura;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import org.kindura.StorageProviderTier;
import org.kindura.StorageProviderConstraint;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import javax.xml.stream.XMLInputFactory;

public class StorageProvider
{
    private ArrayList<StorageProviderTier> tiers = new ArrayList<StorageProviderTier>();
    public String name = "";
	public String description = "";
	private ArrayList<StorageProviderConstraint> constraints = new ArrayList<StorageProviderConstraint>();

	public StorageProvider( String xmlFilename )
	{
		// Initialising a StorageProvider object from an XML file following the Alvarez schema
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		InputStream input = null;
		try
		{
			input = new FileInputStream( new File( xmlFilename ) );
		} catch ( java.io.FileNotFoundException e )
		{
			System.out.println("Cannot open file " + xmlFilename );
			System.exit(1);
		}
		XMLEventReader xmlEventReader = null;
		try
		{
			xmlEventReader = inputFactory.createXMLEventReader(input);
		} catch ( javax.xml.stream.XMLStreamException e )
		{
			System.out.println("Could not parse XML: " + e.toString() );
			System.exit(1);
		}
		
		// Now loop through events generated by the XML parser and extract salient information
		// to populate the member variables of this new StorageProvider object
		while ( xmlEventReader.hasNext() )
		{
			try
			{
					XMLEvent event = xmlEventReader.nextEvent();
					if ( event.isStartElement() )
					{
							StartElement startElement = event.asStartElement();
							// First off, get the name and description of the provider
							if ( startElement.getName().getLocalPart() == "CloudProvider" )
							{
									Iterator<Attribute> attributes = startElement.getAttributes();
									while ( attributes.hasNext() )
									{
											Attribute attribute = attributes.next();
											if ( attribute.getName().toString().equals("ID") )
											{
													this.name = attribute.getValue();
											}
											if ( attribute.getName().toString().equals("Name") )
											{
													this.description = attribute.getValue();
											}
									}
							}

							// Next get the maximum number of replicas
							if ( startElement.getName().getLocalPart() == "Replication" )
							{
									Iterator<Attribute> attributes = startElement.getAttributes();
									while ( attributes.hasNext() )
									{
											Attribute attribute = attributes.next();
											if ( attribute.getName().toString().equals("MaxNumberOfReplicas") )
											{
													this.addConstraint( StorageProviderConstraintEnum.MAXREPLICAS, attribute.getValue() );
											}
									}
							}

							// Now get the regions available
							// Add them as constraints of type StorageProvider.REGION
							if ( startElement.getName().getLocalPart() == "Region" )
							{
									Iterator<Attribute> attributes = startElement.getAttributes();
									while ( attributes.hasNext() )
									{
											Attribute attribute = attributes.next();
											if ( attribute.getName().toString().equals("ID") )
											{
													this.addConstraint( StorageProviderConstraintEnum.REGION, attribute.getValue() );
											}
									}
							}

			}	
			} catch ( javax.xml.stream.XMLStreamException e )
			{
				System.out.println("Error when parsing: " + e.toString() );
				System.exit(1);
			}
		}
	}

    public void addConstraint( StorageProviderConstraintEnum constraintType, String constraintValue )
    {
        this.constraints.add( new StorageProviderConstraint( constraintType, constraintValue ) );
    }

    public ArrayList<StorageProviderConstraint> getConstraints()
	{
		return constraints;
	}

    public ArrayList<StorageProviderTier> getTiers()
    {
        return tiers;
    }
}

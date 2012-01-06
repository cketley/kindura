package org.kindura.rules;

import org.kindura.rules.Project;
import org.kindura.rules.StorageProvider;
import org.kindura.rules.StorageProviderConstraintEnum.*;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.lang.Boolean.*;

public class UploadCollection
{
	static SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
	private String name = "";
    private Project project;
    private String funder = "";
    private ArrayList<StorageProvider> providers = new ArrayList<StorageProvider>();
    private ArrayList<StorageProviderTier> tiers = new ArrayList<StorageProviderTier>();
    private Calendar appraisalDate = Calendar.getInstance();
    private int minCopies = 0;
    private Calendar uploadTime = Calendar.getInstance();
    
    private String version;
    private String sensitivity;
    private String frequency;
	private String type;

    public UploadCollection( Map metadata ) 
	{
		// Create a new UploadCollection object from a list of metadata
		this.funder = metadata.get( "researchfunder" ).toString();
		this.project = new Project( metadata.get( "project").toString(),"Other" );
		this.frequency = metadata.get( "accessrequirement" ).toString();
		this.name = metadata.get( "collectionname" ).toString();
		this.version = metadata.get( "collectionversion" ).toString();
		this.type = metadata.get("collectiontype").toString();
		// Speeling mistake in this key?
		this.sensitivity = metadata.get( "proectivemarking" ).toString();
		this.uploadTime = uploadDateFromString( metadata.get( "filemodificationdate0" ).toString() );
	}

	public Calendar uploadDateFromString( String uploadDate )
	{
		Calendar cal = Calendar.getInstance();
		Date d1 = new Date();
		try
		{
			d1 = df.parse( uploadDate );
		} catch ( ParseException p )
		{
			System.out.println("Bad date " + uploadDate + " given");
		}
		cal.setTime( d1 );
		return cal;
	}

	public void setAppraisalDate( int numYears )
	{
		// Add the specified number of years to the upload date to give the appraisal date	
		this.appraisalDate = (Calendar) this.uploadTime.clone();
		this.appraisalDate.add( Calendar.YEAR, numYears );
	}

	public void limitStorage( StorageProviderConstraintEnum constraintType, String constraintValue )
	{
		// Limit the list of storage providers permitted according to the rules
		Iterator<StorageProvider> iter = this.providers.iterator();

		Boolean found = Boolean.FALSE;
		while ( iter.hasNext() )
		{
			found = Boolean.FALSE;
			StorageProvider p = iter.next();
			System.out.println("Checking provider: " + p.name );
			for ( StorageProviderConstraint c: p.getConstraints() )
			{
				if ( c.getType() == constraintType && c.getValue().equals( constraintValue ) )
				{
					System.out.println("Found matching constraint " + c.getType() + " = " + c.getValue() );
					found = Boolean.TRUE;
				}
			}
			if ( found == Boolean.FALSE )
			{
				System.out.println("Removing provider " + p.name );
				iter.remove();
			}
		}
	}

	public void addStorage( StorageProvider provider )
	{
		this.providers.add( provider );
	}

	public ArrayList<StorageProvider> getProviders()
	{
		return providers;
	}

	public void setType( String type )
	{
		this.type = type;
	}
	
	public String getType()
	{
		return type;
	}

 	public void setUploadTime( Calendar date )
	{
		this.uploadTime = date;
	}

	public Calendar getUploadTime()
	{
		return uploadTime;
	}

    public void setProject( Project project )
    {
        this.project = project;
    }

    public Project getProject()
    {
        return project;
    }

	public void setVersion( String version )
	{
		this.version = version;
	}

	public String getVersion()
	{
		return version;
	}

	public void setFrequency( String frequency )
	{
		this.frequency = frequency;
	}

	public String getFrequency()
	{
		return frequency;
	}

	public void setSensitivity( String sensitivity )
	{
		this.sensitivity = frequency;
	}

	public String getSensitivity()
	{
		return sensitivity;
	}

    public void setFunder( String funder )
    {
        this.funder = funder;
    }

    public String getFunder()
    {
        return funder;
    }
    
    public void addTier( StorageProviderTier tier )
    {
        this.tiers.add (tier );
    }

	public ArrayList<StorageProviderTier> getTiers()
	{
		return tiers;
	}

    public Calendar getAppraisalDate()
    {
        return appraisalDate;
    }
	
	public String getAppraisalDateAsString()
	{
		String output = df.format( this.appraisalDate.getTime() );
		return output;
	}

    public void setMinCopies( int copies )
    {
        this.minCopies = copies;
    }
	public int getMinCopies()
	{
		return minCopies;
	}
}

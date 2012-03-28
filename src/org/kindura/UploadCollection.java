package org.kindura;

/*
Copyright 2011 Roger Downing and 2012 Cheney Ketley, employees of Science & Technology Facilities Council and
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
import org.kindura.Project;
import org.kindura.StorageProvider;
import org.kindura.StorageProviderConstraintEnum.*;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Iterator;
import java.lang.Boolean.*;

public class UploadCollection
{
	private static final boolean debug = false;

	static SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy");
//	private String name = "";
    private Project project;
    private String funder = "";
    private Map<String,String> parentMetadata;
    private ArrayList<StorageProvider> providers = new ArrayList<StorageProvider>();
    private ArrayList<StorageProviderTier> tiers = new ArrayList<StorageProviderTier>();
    private Calendar appraisalDate = Calendar.getInstance();
    private Calendar nowDate = Calendar.getInstance();

    private Integer minCopies = 0;
    private Calendar projEndDate = Calendar.getInstance();
    
    private String version;
    private String sensitivity;
    private String frequency;
	private String type;
	private String billingRegion;
	private String serviceProviderList;
	private String regionName;
	private String regionCode;
    private String totaliseTrigger;
    private Integer uniqueRuleGenerator = 0;
    private Integer howLongMonths =0;
   
	private StatefulKnowledgeSession myDroolsSession;
    private FactHandle handleUploadCollection;
//    private FactHandle handlePricing;
    
    private Boolean iAmBlank;

    private Pricing childPrcng;
    private UploadCollection thisUpldCollection;
    private CostOptimiser parentCostOpt;
 	
	
	public UploadCollection() {

    	if (debug) {System.out.println("[UploadCollection]: ----- UploadCollection constructor -----");};
    	iAmBlank = true;
	};
	
    public UploadCollection( Map<String,String> metadata, Pricing pricerUp ) 
    {

    	if (debug) {System.out.println("[UploadCollection]: ----- UploadCollection overloaded constructor -----");};

    	// java doesnt like this hash map to be null
    	try
    	{
    		// Create a new UploadCollection object from a list of metadata
    		if ( ! ( metadata.get( "projectFunder" ) == null) ) {
    			if ( ! metadata.get( "projectFunder" ) .isEmpty()) {
    				funder = metadata.get( "projectFunder" );
    			} else {
    				funder = "none";
    			}
    		} else {
    			funder = "blank";
    		}			
    		if ( ! ( metadata.get( "projectFunder" ) == null) ) {
    			if ( ! metadata.get( "projectFunder" ) .isEmpty()) {
    				if ( ! ( metadata.get( "projectName" ) == null) ) {
    					if ( ! metadata.get( "projectName" ) .isEmpty()) {
    						project = new Project( metadata.get( "projectName").toString(), metadata.get( "projectFunder" ).toString());
    					} else {
    						project = new Project ("missing", "blank");
    					}
    				} else {
    					project = new Project ("unknown", "blank");
    				} 
    			} else {
    				project = new Project ("empty", "blank");
    			} 
    		} else {
    			project = new Project ("zilch", "blank");
    		}


    		//			if ( ! metadata.get( "projectFunder" ) .isEmpty()) {
    		//				if ( ! metadata.get( "projectName" ) .isEmpty()) {
    		//					this.project = new Project( metadata.get( "projectName").toString(), metadata.get( "projectFunder" ).toString());
    		//				} else {
    		//					this.project = new Project ("missing", "blank");
    		//				}
    		//			} else {
    		//				this.project = new Project ("unknown", "blank");
    		//			}

    		if ( ! ( metadata.get( "accessFrequency" ) == null) ) {
    			if ( ! metadata.get( "accessFrequency" ) .isEmpty()) {
    				frequency = metadata.get( "accessFrequency" );
    			} else {
    				frequency = "blank";
    			}
    		} else {
    			frequency = "empty";
    		}				

    		if ( ! ( metadata.get( "version" ) == null) ) {
    			if ( ! metadata.get( "version" ) .isEmpty()) {
    				version = metadata.get( "version" );
    			} else {
    				version = "blank";
    			}
    		} else {
    			version = "empty";
    		}				

    		if ( ! ( metadata.get( "typeOfData" ) == null) ) {
    			if ( ! metadata.get( "typeOfData" ) .isEmpty()) {
    				type = metadata.get( "typeOfData" );
    			} else {
    				type = "blank";
    			}
    		} else {
    			type = "empty";
    		}				

    		if ( ! ( metadata.get( "protectiveMarking" ) == null) ) {
    			if ( ! metadata.get( "protectiveMarking" ) .isEmpty()) {
    				sensitivity = metadata.get( "protectiveMarking" );
    			} else {
    				sensitivity = "blank";
    			}
    		} else {
    			sensitivity = "empty";
    		}				


    		if ( ! ( metadata.get( "endDate" ) == null) ) {
    			if ( ! metadata.get( "endDate" ) .isEmpty()) {
    				projEndDate = uploadDateFromString( metadata.get( "endDate" ).toString());
    			} else {
    				projEndDate = uploadDateFromString("01/01/2012 1:00:00");
    			}
    		} else {
    			projEndDate = uploadDateFromString("01/01/2012 1:00:00");
    		}				
    	} catch ( Exception e )
    	{
    		System.err.println("Caught error on hash map constructor - setting to blanks" + e.toString() );
    		this.funder = "";
    		this.project = new Project( "", "Other" );
    		this.frequency = "";
    		//			this.name = "";
    		this.version = "";
    		this.type = "";
    		this.sensitivity = "";
    		this.projEndDate = uploadDateFromString("01/01/2012 1:00:00");
    	}

    	// clear out the nulls - drools doesnt like them
    	totaliseTrigger = "none yet";
    	regionCode = "";
    	regionName = "";
    	billingRegion = "";


    	// remember where to find the pricing object
    	childPrcng = pricerUp;

    	// remember where to find the metadata object
    	parentMetadata = metadata;

    	iAmBlank = true;

    }

	public Calendar uploadDateFromString( String uploadDate )
	{
		Calendar cal = Calendar.getInstance();
		
		Date d1 = new Date();
		try
		{	
			d1 = df.parse( uploadDate ) ;
		} catch ( ParseException p )
		{
			if (debug) {System.out.println("[UploadCollection]: Bad date " + uploadDate + " given");};
		}
		cal.setTime( d1 );
		return cal;
	}

	public void setAppraisalDate( int numYears )
	{
		
		// this is called from within KinduraRules.drl
		
		// Add the specified number of years to the upload date to give the appraisal date	
		appraisalDate = (Calendar) this.projEndDate.clone();	
		
		if ( parentCostOpt.getOpsFlag().equals("ingest") ) {
			Integer numMonths = numYears * 12;
			howLongMonths = numMonths;
			childPrcng.setHowLongMonths(numMonths);
			appraisalDate.add( Calendar.YEAR, numYears );
			parentMetadata.put("StorageExpiryDate", getAppraisalDateAsString() );
			// TODO the length of time to charge for is calculated against Now thru StorageExpiryDate
			// so what we have here is wrong
		} else {
    		if ( ! ( parentMetadata.get( "StorageExpiryDate" ) == null) ) {
    			if ( ! parentMetadata.get( "StorageExpiryDate" ) .isEmpty()) {
    				appraisalDate = uploadDateFromString( parentMetadata.get( "StorageExpiryDate" ).toString());
    			} else {
    				// StorageExpiryDate might be missing so use project endDate instead
    				appraisalDate = uploadDateFromString(parentMetadata.get( "endDate" ).toString());
    			}
    		} else {
    			appraisalDate = uploadDateFromString(parentMetadata.get( "endDate" ).toString());
    		}				
			howLongMonths = getMonthsDifference(nowDate, appraisalDate);
			if (howLongMonths < 1) {
				// might get negative numbers if we used project endDate
				howLongMonths = 0;
			}
			childPrcng.setHowLongMonths(howLongMonths);
//			appraisalDate.add( Calendar.MONTH, howLongMonths );
//			parentMetadata.put("StorageExpiryDate", getAppraisalDateAsString() );
		};
		
	}
	
	@SuppressWarnings("deprecation")
	public static final int getMonthsDifference(Calendar nowDate2, Calendar appraisalDate2) {

//		if (debug) {System.out.println("[UploadCollection]: nowDate2 is " + SimpleDateFormat("dd/mm/yyyy").parse(nowDate2));};
//		if (debug) {System.out.println("[UploadCollection]: appraisalDate2 is " + appraisalDate2.toString());};
		int m1 = 0;
		int m2 = 0;
		Date x;
//		try {
			x = nowDate2.getTime();
//			x = new SimpleDateFormat("dd/mm/yyyy").parse(nowDate2.toString());
			m1 = x.getYear() * 12 + x.getMonth();
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		Date y;
//		try {
			y = appraisalDate2.getTime();
//			y = new SimpleDateFormat("dd/mm/yyyy").parse(appraisalDate2.toString());
			m2 = y.getYear() * 12 + y.getMonth();
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		
	    return m2 - m1 + 1;
	}

	public String getRegionName () {
		// translates the region code into a region name as per price list
	
		return this.regionName;
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
			if (debug) {System.out.println("[UploadCollection]: Checking provider: " + p.name );};
			for ( StorageProviderConstraint c: p.getConstraints() )
			{
				if ( c.getType() == constraintType && c.getValue().equals( constraintValue ) )
				{
					if (debug) {System.out.println("[UploadCollection]: Found matching constraint " + c.getType() + " = " + c.getValue() );};
					found = Boolean.TRUE;
				}
			}
			if ( found == Boolean.FALSE )
			{
				if (debug) {System.out.println("[UploadCollection]: Removing provider " + p.name );};
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

 	public void setprojEndDate( Calendar date )
	{
		this.projEndDate = date;
	}

	public Calendar getprojEndDate()
	{
		return projEndDate;
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
		this.sensitivity = sensitivity;
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

	public StorageProviderTier getTiers(StorageProviderTier tier)
	{
		return tier;
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
//        childPrcng.setReplicas = String.valueOf(copies);
    }
	public int getMinCopies()
	{
		return minCopies;
	}

	/**
	 * @return the billingRegion
	 */
	public String getBillingRegion() {
		return billingRegion;
	}

	/**
	 * @param billingRegion the billingRegion to set
	 */
	public void setBillingRegion(String billingRegion) {
		// don't confuse it with region code which originates in KinduraRules.drl 
		// and is of form EU, EEC, Local
		// this is BillingRegion which is like "US West (Northern California)"
		// BillingRegion sets UploadCollection.regionName and also sets
		// Pricing.regionName
		this.regionName = billingRegion;
		this.billingRegion = billingRegion;

		iAmBlank = false;
		childPrcng.setRegionName(billingRegion);

		if (debug) {System.out.println("[UploadCollection]: Billing region set to " + billingRegion);};	
	}

	/**
	 * @return the serviceProvider
	 */
	public String getServiceProviderList() {
		return serviceProviderList;
	}

	/**
	 * @param serviceProvider the serviceProvider to set
	 */
	public void setServiceProviderList(String spList) {
		
		// the ServiceProvider was spat out by the Region spreadsheet
		// so we use this to trigger the build of a new set of keys 
		// and store the data
		
		this.serviceProviderList = spList;

		
		// tell the Pricing about the serviceProvider
		// because the spreadsheet looks there for its key not here
		// This will not force a trigger
		childPrcng.setServiceProviderList(spList);
		if (debug) {System.out.println("[UploadCollection]: Service Provider set to " + spList);};
		
	}

	private void populatePrcngServiceProvider(Pricing pricerUp, String spList) {
		// tell the Pricing about the serviceProvider
		// because the spreadsheet looks there for its key not here
		// This will not force a trigger
		pricerUp.setServiceProviderList(spList);

	}


	/**
	 * @return the regionCode
	 */
	public String getRegionCode() {
		return regionCode;
	}

	/**
	 * @param regionCode the regionCode to set
	 */
	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
		

		if (debug) {System.out.println("[UploadCollection]: Region code set to " + regionCode);};
		// region code originates in KinduraRules.drl and is of form EU, EEC, Local, WORLD
		// don't confuse it with BillingRegion which is like "US West (Northern California)"
	}


	/**
	 * @return the myDroolsSession
	 */
	public StatefulKnowledgeSession getMyDroolsSession() {
		return myDroolsSession;
	}

	/**
	 * @param myDroolsSession the myDroolsSession to set
	 */
	public void setMyDroolsSession(StatefulKnowledgeSession myDroolsSession) {
		this.myDroolsSession = myDroolsSession;
	}

	/**
	 * @return the handleUploadCollection
	 */
	public FactHandle getHandleUploadCollection() {
		return handleUploadCollection;
	}

	/**
	 * @param handleUploadCollection the handleUploadCollection to set
	 */
	public void setHandleUploadCollection(FactHandle handleUploadCollection) {
		this.handleUploadCollection = handleUploadCollection;
	}

//	/**
//	 * @return the handlePricing
//	 */
//	public FactHandle getHandlePricing() {
//		return handlePricing;
//	}
//
//	/**
//	 * @param handlePricing the handlePricing to set
//	 */
//	public void setHandlePricing(FactHandle handlePricing) {
//		this.handlePricing = handlePricing;
//	}

	/**
	 * @return the totaliseTrigger
	 */
	public String getTotaliseTrigger() {
		return totaliseTrigger;
	}

	/**
	 * @param totaliseTrigger the totaliseTrigger to set
	 */

	/**
	 * @return the iAmBlank
	 */
	public Boolean getIAmBlank() {
		return iAmBlank;
	}

	/**
	 * @param iAmBlank the iAmBlank to set
	 */
	public void setIAmBlank(Boolean iAmEmpty) {
		this.iAmBlank = iAmEmpty;
	}
	
	// TODO sort out this 
//	public void walkMap () {
//		// For both the keys and values of a map
//		for (Iterator it=map.entrySet().iterator(); it.hasNext(); ) {
//			Map.Entry entry = (Map.Entry)it.next();
//			Object key = entry.getKey();
//			Object value = entry.getValue();
//
//		}
//    myTotaliser.calcTotalCost();
//    if (debug) {System.out.println( "after makeTotaliser" );};
//
//
//    if (debug) {System.out.println( "Service Total: " + myTotaliser.getServiceTotal() );};
//
//	}

	/**
	 * @return the childPrcng
	 */
	public Pricing getChildPrcng() {
		return childPrcng;
	}

	/**
	 * @param childPrcng the childPrcng to set
	 */
	public void setChildPrcng(Pricing currPrcng) {
		this.childPrcng = currPrcng;
	}

	/**
	 * @return the thisUpldCollection
	 */
	public UploadCollection getThisUpldCollection() {
		return thisUpldCollection;
	}

	/**
	 * @param thisUpldCollection the thisUpldCollection to set
	 */
	public void setThisUpldCollection(UploadCollection thisUpldCollection) {
		this.thisUpldCollection = thisUpldCollection;
	}

	/**
	 * @return the parentCostOpt
	 */
	public CostOptimiser getParentCostOpt() {
		return parentCostOpt;
	}

	/**
	 * @param parentCostOpt the parentCostOpt to set
	 */
	public void setParentCostOpt(CostOptimiser parentCostOpt) {
		this.parentCostOpt = parentCostOpt;
	}

	private boolean checkDuplicateFactsRegulatory(HashMap<String,String>  regulatoryData) {
		
		String keyVal = billingRegion + "|" + uniqueRuleGenerator;
		if (regulatoryData.containsKey(keyVal)) { return true; };
		regulatoryData.put(keyVal, "Fact inserted");
		return false;
	}

	/**
	 * @param totaliseTrigger the totaliseTrigger to set
	 */
	public void setTotaliseTrigger(String totaliseTrigger) {
		
		// the totaliseTrigger was spat out by the Regulatory spreadsheet
		if (debug) {System.out.println("[UploadCollection]: Triggering on " + serviceProviderList);};

		// push the data down to the pricing object
		childPrcng.setRegionName(regionName);
		childPrcng.setHowLongMonths(howLongMonths);
		
		
//		if (serviceProviderList == "dummy") {
//			iAmBlank = true;
//		}
		if (checkDuplicateFactsRegulatory(parentCostOpt.getDuplicateRegulatoryHashMap())) {
			if (debug) {System.out.println("[UpldCllctn]: skipping due to duplicate key found");};
			iAmBlank = true;			
		};
		// if i am blank or duplicate do not try to insert me
		if ( ! iAmBlank ) {
			if (debug) {System.out.println("UpldCllctn: +++ Inserting UploadCollection at setTotaliseTrigger +++");};
			if (debug) {System.out.println("UpldCllctn: Keys are :-");};
			if (debug) {System.out.println("UpldCllctn: regionCode: " + this.regionCode);};
			if (debug) {System.out.println("UpldCllctn: regionName: " + this.regionName);};
			if (debug) {System.out.println("UpldCllctn: billingRegion: " + this.billingRegion);};
			if (debug) {System.out.println("UpldCllctn: serviceProviderList: " + this.serviceProviderList);};
			if (debug) {System.out.println("UpldCllctn: sensitivity: " + this.sensitivity);};
			if (debug) {System.out.println("UpldCllctn: minCopies: " + this.minCopies);};
			if (debug) {System.out.println("UpldCllctn: ");};

			if (debug) {System.out.println("UpldCllctn: Keys are :-");};
			if (debug) {System.out.println("UpldCllctn: Prcng - serviceProvider: " + childPrcng.getServiceProvider());};
			if (debug) {System.out.println("UpldCllctn: Prcng - regionName: " + childPrcng.getRegionName());};
			if (debug) {System.out.println("UpldCllctn: Prcng - paymentPlan: " + childPrcng.getPaymentPlan());};
			if (debug) {System.out.println("UpldCllctn: Prcng - featureSet: " + childPrcng.getFeatureSet());};
			if (debug) {System.out.println("UpldCllctn: Prcng - subfeature: " + childPrcng.getSubfeature());};
			if (debug) {System.out.println("UpldCllctn: Prcng - replicas: " + childPrcng.getReplicas());};
			if (debug) {System.out.println("UpldCllctn: Prcng - storageUsed: " + childPrcng.getStorageUsed());};
			if (debug) {System.out.println("UpldCllctn: Prcng - requestsUsed: " + childPrcng.getRequestsUsed());};
			if (debug) {System.out.println("UpldCllctn: Prcng - transfersUsed: " + childPrcng.getTransfersUsed());};
			if (debug) {System.out.println("UpldCllctn: Prcng - servicebusUsed: " + childPrcng.getServicebusUsed());};
			if (debug) {System.out.println("UpldCllctn: Prcng - storagetransactionsUsed: " + childPrcng.getStoragetransactionsUsed());};
			if (debug) {System.out.println("UpldCllctn: ");};

			childPrcng.writeServiceMatrix(childPrcng);
			if (debug) {System.out.println("UpldCllctn: Pricing Inserted");};

		};

//		UploadCollection myCollection = new UploadCollection( parentMetadata, childPrcng );
		// tell Pricing where the current UploadCollection object is
//		childPrcng.setOurUpldCollectn(myCollection);
//		childPrcng.setParentUploadCollection(myCollection);
		childPrcng.setOurUpldCollectn(this);
		childPrcng.setParentUploadCollection(this);
//		// ok,i've forgotten where the webapp lives, so ask my earlier self
//		myCollection.parentCostOpt = this.parentCostOpt;
//		this.parentCostOpt = this.parentCostOpt;
		
		
		// and then update the linked list with the Pricing data
		// childPrcng is given to us when the constructor for UploadCollection is run
//		updateChildPrcngList(myCurrentPositionPrcngList, childPrcng);
	}

	public void setUniqueRuleGenerator(Integer uniqueRuleGenerator) {
		this.uniqueRuleGenerator = uniqueRuleGenerator;
	}

	public Integer getUniqueRuleGenerator() {
		return uniqueRuleGenerator;
	};

	
}

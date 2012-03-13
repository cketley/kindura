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

import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.event.rule.DebugWorkingMemoryEventListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.drools.runtime.rule.*;
//import org.drools.FactHandle;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.CommandFactory;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
//import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;



/**
 * This represents the business arrangements for the storage consumed by us
 */
public class Pricing {

	
	private static final boolean debug = false;
	private StatefulKnowledgeSession myKsession;
	private UploadCollection parentUploadCollection;
	private CostOptimiser grandparentCostOpt;
	
//    private Totaliser currentTotBySP;
    
    private Totaliser myTotaliser;

    
    private String callUsMessage = "";
    private String debugTracer = "Default";
    private String serviceProvider = "";
    private String featureSet = "";
    private String paymentPlan = "";
    private String regionName = "";
    private String regionCode = "";
    private String subfeature = "";
    private String replicas = "";
    private Double storageUsed = 0.0;
    private Double requestsUsed = 0.0;
    private Double transfersUsed = 0.0;
    private Double servicebusUsed = 0.0;
    private Double storagetransactionsUsed = 0.0;
    private String flags = "";
    private Integer howLongMonths = 0; 
    private Integer uniqueRuleGenerator = 0;
    private String enabledFlag = "Enabled";

    
    private String totaliseTrigger = "";
    private Boolean flagTotalisePrevious = false;
    private Boolean iAmBlank;
    
    private KnowledgeRuntimeLogger myLogger;
        
    
    public Pricing () {
    	
    	iAmBlank = true;
    
    	if (debug) {System.out.println("[Pricing]: ----- Pricing constructor -----");};
    	
    };
    
    public void destructorPricing () {

//    	myLogger.close();

    	myKsession.dispose();
    }


    
    public void constructorCostingObjects() {

    	iAmBlank = true;
        

    }

    public void constructorFilterObjects(Map<String,String> metadata, UploadCollection upldCollectn) {
    	// this is not a proper constructor
    	
    	Double stgUsed = 0.0;
    	stgUsed = Double.valueOf(metadata.get( "storageUsed" ).toString() );
    	
    	setStorageUsed(stgUsed);
    	
    	Double xferUsed = 0.0;
    	xferUsed = Double.valueOf(metadata.get( "transfersUsed" ).toString() );
    	
    	setTransfersUsed(xferUsed);
    	
    	// dream up a number of requests based on the accessFrequency
    	Double rqstsUsed = 1.0;
    	Double rqstsUsedEstimate = 0.0;
    	rqstsUsedEstimate = applyFrequencyOfAccess(rqstsUsed, upldCollectn);
    	
//    	metadata.put("requestsUsed", String.valueOf(rqstsUsedEstimatate));    	
    	setRequestsUsed(rqstsUsedEstimate);
   	
    }
    
    
    public Integer initDrools(Map<String,String> metadata, UploadCollection upldColl) {
    

    	// this inserts the initial set of Facts
    	upldColl.setHandleUploadCollection( myKsession.insert(upldColl) );

    	return 1;
    }

  
    public Integer loadDrools(UploadCollection collectn) {
    	
    	if (debug) {System.out.println( "before new session");};
    	
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        myKsession = kbase.newStatefulKnowledgeSession(); 
        // tell UploadCollection about the drools session
        collectn.setMyDroolsSession(myKsession);

        // this is not particularly useful
//        KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(myKsession, "C:\\Documents and Settings\\ycn94546\\My Documents\\Project_work\\kindura\\kindness\\src\\org\\kindura\\runlog_KinduraTest03.xml");
//        myLogger = logger;
        
        if (debug) {System.out.println( "before prep of knowledge builder" );};


        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        if (debug) {System.out.println( "before add of KinduraRules.drl" );};
      kbuilder.add(ResourceFactory.newClassPathResource("org/kindura/KinduraRules.drl", getClass() ), ResourceType.DRL);
      if ( kbuilder.hasErrors() ) {
      	if (debug) {System.out.println( "Error loading KinduraRules.drl");};
          System.err.print( kbuilder.getErrors() );
          return -1;
      }
      

    
//      if (debug) {System.out.println( "before add of rules flow" );};
//
//      kbuilder.add( ResourceFactory.newClassPathResource( "org/kindura/KinduraFlows.rf",
//      		getClass() ),  ResourceType.DRF );
//      if ( kbuilder.hasErrors() ) {
//        	if (debug) {System.out.println( "Error loading KinduraFlows.rf");};
//          System.err.print( kbuilder.getErrors() );
//          return -1;
//      }
//
//
//      if (debug) {System.out.println( "after add of rules flow" );};

//     // setup the debug listeners
//
//        myKsession.addEventListener( new DebugAgendaEventListener() );
//
//        myKsession.addEventListener( new DebugWorkingMemoryEventListener() );
        
        DecisionTableConfiguration dtableconfiguration = KnowledgeBuilderFactory.newDecisionTableConfiguration();
        dtableconfiguration.setInputType( DecisionTableInputType.XLS );

        if (debug) {System.out.println( "before add of spreadsheet - Amazon" );};
        kbuilder.add( ResourceFactory.newClassPathResource("org/kindura/KinduraPricing_amazon_V0.09.xls", getClass() ),
ResourceType.DTABLE,
dtableconfiguration );
        if ( kbuilder.hasErrors() ) {
            System.err.print( kbuilder.getErrors() );
            return -1;
        }
        if (debug) {System.out.println( "before add of spreadsheet - azure" );};
        kbuilder.add( ResourceFactory.newClassPathResource("org/kindura/KinduraPricing_azure_V0.09.xls", getClass() ),
ResourceType.DTABLE,
dtableconfiguration );
        if ( kbuilder.hasErrors() ) {
            System.err.print( kbuilder.getErrors() );
            return -1;
        }
        if (debug) {System.out.println( "before add of spreadsheet - rackspace" );};
        kbuilder.add( ResourceFactory.newClassPathResource("org/kindura/KinduraPricing_rackspace_V0.09.xls", getClass() ),
ResourceType.DTABLE,
dtableconfiguration );
        if ( kbuilder.hasErrors() ) {
            System.err.print( kbuilder.getErrors() );
            return -1;
        }
        if (debug) {System.out.println( "before add of spreadsheet - irods" );};
        kbuilder.add( ResourceFactory.newClassPathResource("org/kindura/KinduraPricing_irods_V0.09.xls", getClass() ),
ResourceType.DTABLE,
dtableconfiguration );
        if ( kbuilder.hasErrors() ) {
            System.err.print( kbuilder.getErrors() );
            return -1;
        }
        if (debug) {System.out.println( "before add of spreadsheet - sdsc" );};
        kbuilder.add( ResourceFactory.newClassPathResource("org/kindura/KinduraPricing_SDSC_V0.09.xls", getClass() ),
ResourceType.DTABLE,
dtableconfiguration );
        if ( kbuilder.hasErrors() ) {
            System.err.print( kbuilder.getErrors() );
            return -1;
        }
        if (debug) {System.out.println( "before add of spreadsheet - region codes");};
        kbuilder.add( ResourceFactory.newClassPathResource("org/kindura/KinduraRegulatoryRegion_V0.9.1.xls", getClass() ),
ResourceType.DTABLE,
dtableconfiguration );
        if ( kbuilder.hasErrors() ) {
        	if (debug) {System.out.println( "Error loading regulatory region");};
            System.err.print( kbuilder.getErrors() );
            return -1;
        }
     
       
        if (debug) {System.out.println( "before add of spreadsheet - Service provider matrix");};
        // careful - slightly different version number here
        kbuilder.add( ResourceFactory.newClassPathResource("org/kindura/KinduraServiceMatrix_V0.9.2.xls", getClass() ),
ResourceType.DTABLE,
dtableconfiguration );
        if ( kbuilder.hasErrors() ) {
        	if (debug) {System.out.println( "Error loading service provider matrix");};
            System.err.print( kbuilder.getErrors() );
            return -1;
        }
        if (debug) {System.out.println( "after add of all of the spreadsheets" );};
        
    
       
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        if (debug) {System.out.println( "after create new session" );};
        
        
        return 1;

    }
    
    public Integer runDrools() {

        if (debug) {System.out.println( "before fire rules" );};

        myKsession.fireAllRules();
        
//        myKsession.execute( Arrays.asList( new Object[]{myTotaliser} ) );
        if (debug) {System.out.println( "after fire rules" );};

        return 1;

    }

	/**
	 * @return the callUsMessage
	 */
	public String getCallUsMessage() {
		return callUsMessage;
	}

	/**
	 * @param callUsMessage the callUsMessage to set
	 */
	public void setCallUsMessage(String callUsMsg) {
		this.callUsMessage = callUsMsg;
		myTotaliser.setCallUsMessage(callUsMsg);
	}

	/**
	 * @return the debugTracer
	 */
	public String getDebugTracer() {
		return debugTracer;
	}

	/**
	 * @param debugTracer the debugTracer to set
	 */
	public void setDebugTracer(String tracer) {
		this.debugTracer = tracer;
		myTotaliser.setDebugTracer(tracer);
	}



	/**
	 * @return the regionName
	 */
	public String getRegionName() {
		return regionName;
	}

	/**
	 * @param regionName the regionName to set
	 */
	public void setRegionName(String regName) {
		this.regionName = regName;
		if (debug) {System.out.println("[Pricing]: regionName set to " + regionName);};
		iAmBlank = false;		
		myTotaliser.setRegionName(regName);
//		
//		// force a new Pricing object to be made
//		setTotaliseTrigger("dummy");

	}



	/**
	 * @return the howLongMonths
	 */
	public Integer getHowLongMonths() {
		return howLongMonths;
	}

	/**
	 * @param howLongMonths the howLongMonths to set
	 */
	public void setHowLongMonths(Integer howLongMths) {
		this.howLongMonths = howLongMths;
		myTotaliser.setHowLongMonths(howLongMths);
	}
	

	/**
	 * @return the serviceProvider
	 */
	public String getServiceProvider() {
		return serviceProvider;
	}

	/**
	 * @param serviceProvider the serviceProvider to set
	 */
	public void setServiceProvider(String svcProvider) {
		serviceProvider = svcProvider;
		
		// store the data in Totaliser
		myTotaliser.setServiceProviderName(svcProvider);
		iAmBlank = false;
		
		if (debug) {System.out.println("[Pricing]: Service Provider set to " + svcProvider);};
	}


	private void populateTotaliserServiceProvider(Pricing pricedUp, String spList) {
		myTotaliser.setServiceProviderName(spList);
	}
	
	public void setServiceProviderList(String svcProvider) {
		serviceProvider = svcProvider;
		// store the data in Totaliser
		myTotaliser.setServiceProviderName(serviceProvider);
		iAmBlank = false;
		if (debug) {System.out.println("[Pricing]: Service Provider List set to " + svcProvider);};
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
	}

	public Totaliser getMyTotaliser() {
		return myTotaliser;
	}

	public void setMyTotaliser(Totaliser myTotaliser) {
		this.myTotaliser = myTotaliser;
	}

	/**
	 * @return the totaliseTrigger
	 */
	public String getTotaliseTrigger() {
		return totaliseTrigger;
	}

	/**
	 * @param totaliseTrigger the totaliseTrigger to set
	 */
	public void setTotaliseTrigger(String totaliseTrigger) {
		
		// the totaliseTrigger was spat out by the ServiceMatrix spreadsheet
		// so we feed this back in as another Pricing object and fire 
		// the rules with its new data to trigger the build of a new Totaliser
		// which will fire on this set of keys and store the data

		// it doesnt matter what value the totaliseTrigger has - it's just an event

		if (debug) {System.out.println("[Pricing]: before insert pricer");};

		// tell the Totaliser where to find me
		myTotaliser.setParentPricing(this);
		// tell the Totaliser where to find my parent
		myTotaliser.setGrandparentUploadCollection(parentUploadCollection);

//		if (serviceProvider == "dummy") {
//			iAmBlank = true;
//		}
		if (parentUploadCollection.getIAmBlank()) {
			iAmBlank = true;
		}
		if (checkDuplicateFactsPricing(grandparentCostOpt.getDuplicatePricingHashMap())) {
			if (debug) {System.out.println("[Pricing]: skipping due to duplicate key found");};
			iAmBlank = true;			
		};
		// if i am blank or duplicate do not try to insert me
		if ( ! iAmBlank ) {
			if (debug) {System.out.println("[Pricing]: +++ Inserting Pricing at pricer trigger +++");};
			if (debug) {System.out.println("[Pricing]: Keys are :-");};
			
			if (debug) {System.out.println("[Pricing]: serviceProvider: " + serviceProvider);};
			if (debug) {System.out.println("[Pricing]: regionName: " + regionName);};
			if (debug) {System.out.println("[Pricing]: paymentPlan: " + paymentPlan);};
			if (debug) {System.out.println("[Pricing]: featureSet: " + featureSet);};
			if (debug) {System.out.println("[Pricing]: subfeature: " + subfeature);};
			if (debug) {System.out.println("[Pricing]: replicas: " + replicas);};
			if (debug) {System.out.println("[Pricing]: storageUsed: " + storageUsed);};
			if (debug) {System.out.println("[Pricing]: requestsUsed: " + requestsUsed);};
			if (debug) {System.out.println("[Pricing]: transfersUsed: " + transfersUsed);};
			if (debug) {System.out.println("[Pricing]: servicebusUsed: " + servicebusUsed);};
			if (debug) {System.out.println("[Pricing]: storagetransactionsUsed: " + storagetransactionsUsed);};

			if (debug) {System.out.println("[Pricing]: ");};
			
			if (debug) {System.out.println("[Pricing]: +++ Inserting Totaliser at pricer trigger +++");};
			if (debug) {System.out.println("[Pricing]: Keys are :-");};
			if (debug) {System.out.println("[Pricing]: Totaliser will be serviceProvider: " + myTotaliser.getServiceProviderName());};
			if (debug) {System.out.println("[Pricing]: Totaliser will be regionName: " + myTotaliser.getRegionName());};
			if (debug) {System.out.println("[Pricing]: Totaliser will be paymentPlan: " + myTotaliser.getPlanType());};
			if (debug) {System.out.println("[Pricing]: Totaliser will be featureSet: " + myTotaliser.getFeatureType());};
			if (debug) {System.out.println("[Pricing]: Totaliser will be subfeature: " + myTotaliser.getSubfeatureType());};
			if (debug) {System.out.println("[Pricing]: Totaliser will be replicas: " + myTotaliser.getReplicas());};
			if (debug) {System.out.println("[Pricing]: Totaliser will be flags: " + myTotaliser.getFlags());};
			if (debug) {System.out.println("[Pricing]: ");};

//			myKsession.insert(myTotaliser);

			// don't leave this null because it will cause problems with drools 
			myTotaliser.setOurKsession(myKsession);
			// insert a Totaliser Fact to trigger the pricing process
			// This will force a trigger, except if it's blank
			myTotaliser.setTotaliseTrigger("dummy");
			if (debug) {System.out.println("[Pricing]: Totaliser Inserted");};

			// Now spit out a New Pricing object  
			Pricing currentPrcng = new Pricing();
			currentPrcng.setIAmBlank(true);
			// tell the new Totaliser where to find the new Pricing
			myTotaliser.setParentPricing(currentPrcng);
			// tell the new Totaliser where to find my parent
			myTotaliser.setGrandparentUploadCollection(parentUploadCollection);
			// tell the new Pricing where to find the parent, grandparent and totaliser
			currentPrcng.parentUploadCollection = this.parentUploadCollection;
			currentPrcng.grandparentCostOpt = this.grandparentCostOpt;
			currentPrcng.myTotaliser = this.myTotaliser;
			// tell the new Pricing the amounts to be used
			// TODO other amounts need to be added in
			currentPrcng.setStorageUsed(storageUsed);
			// tell the new Pricing the number of months to calculate against
			currentPrcng.setHowLongMonths(howLongMonths);
			// tell the old Upload Collection where to find the new me
			parentUploadCollection.setChildPrcng(currentPrcng);

		};
	}

	public void writeServiceMatrix(Pricing cstOpt) {
		if (debug) {System.out.println("[Pricing]: Start insert of Service Matrix with regionName set to " + regionName);};
		// blank out the unnecessary stuff so that we don't get duplicate Inserts of Facts
		serviceProvider = "";
		paymentPlan = "";
		featureSet = "";
		subfeature = "";
		replicas = "";
		// should have been carried over from the initial template Pricing
//		storageUsed = 0.0;
	    requestsUsed = 0.0;
//	    transfersUsed = 0.0;
	    servicebusUsed = 0.0;
	    storagetransactionsUsed = 0.0;
	    // only want the enabled lines
	    setEnabledFlag("Enabled");
//	    if (serviceProvider == "dummy") {
//			iAmBlank = true;
//		}
		if (parentUploadCollection.getIAmBlank()) {
			if (debug) {System.out.println("[Pricing]: skipping due to blankflag");};
			iAmBlank = true;
		}
		if (checkDuplicateFactsPricing(grandparentCostOpt.getDuplicateServiceMatrixHashMap())) {
			if (debug) {System.out.println("[Pricing]: skipping due to duplicate key found");};
			iAmBlank = true;			
		};
		// if i am blank or duplicate do not try to insert me
		if ( ! iAmBlank ) {
			if (debug) {System.out.println("[Pricing]: +++ Inserting Pricing for Service Matrix trigger +++");};
			if (debug) {System.out.println("[Pricing]: Keys are :-");};
			if (debug) {System.out.println("[Pricing]: serviceProvider: " + serviceProvider);};
			if (debug) {System.out.println("[Pricing]: regionName: " + regionName);};
			if (debug) {System.out.println("[Pricing]: paymentPlan: " + paymentPlan);};
			if (debug) {System.out.println("[Pricing]: featureSet: " + featureSet);};
			if (debug) {System.out.println("[Pricing]: subfeature: " + subfeature);};
			if (debug) {System.out.println("[Pricing]: replicas: " + replicas);};
			if (debug) {System.out.println("[Pricing]: storageUsed: " + storageUsed);};
			if (debug) {System.out.println("[Pricing]: requestsUsed: " + requestsUsed);};
			if (debug) {System.out.println("[Pricing]: transfersUsed: " + transfersUsed);};
			if (debug) {System.out.println("[Pricing]: servicebusUsed: " + servicebusUsed);};
			if (debug) {System.out.println("[Pricing]: storagetransactionsUsed: " + storagetransactionsUsed);};
			if (debug) {System.out.println("[Pricing]: ");};

			// this gets lost somehow
			// don't leave this null because it will cause problems with drools 
			myKsession = parentUploadCollection.getMyDroolsSession();

			myKsession.insert(this);			
			if (debug) {System.out.println("[Pricing]: Inserted");};			

//			// tell the Totaliser where to find my parent
//			myTotaliser.setGrandparentUploadCollection(parentUploadCollection);
//			myTotaliser.setGreatgrandparentCostOpt(grandparentCostOpt);

			// we have to make a new Pricing object on each index because
			// drools can see only the address of the object
			// Now spit out a New Pricing object  
			Pricing currentPrcng = new Pricing();
			// add it to a linked list that the Pricing objects and
			// by implication the Totaliser objects will hang off of
			currentPrcng.setIAmBlank(true);
			// tell the Totaliser where to find me
			myTotaliser.setParentPricing(this);
			// tell the new Pricing where to find the parent and grandparent and so on
			currentPrcng.parentUploadCollection = this.parentUploadCollection;
			currentPrcng.grandparentCostOpt = this.grandparentCostOpt;
			currentPrcng.myTotaliser = this.myTotaliser;
			currentPrcng.myKsession = this.myKsession;
			currentPrcng.myLogger = this.myLogger;
			// tell the new Pricing the amounts to be used
			// TODO other amounts need to be added in
			currentPrcng.setStorageUsed(storageUsed);
			// tell the new Pricing the number of months to calculate against
			currentPrcng.setHowLongMonths(howLongMonths);

		};
		
		if (debug) {System.out.println("[Pricing]: end insert of Service Matrix with Service Provider set to " + serviceProvider);};
	}

	private boolean checkDuplicateFactsPricing(HashMap<String,String>  cstOptData) {
		
		if (debug) {System.out.println("[Pricing]: Hashmap key is :-" + serviceProvider + "|" + regionName + "|" + uniqueRuleGenerator + "|" + paymentPlan + "|" + featureSet + "|" + subfeature + "|" + replicas + "|" + String.valueOf(storageUsed) + "|" + String.valueOf(requestsUsed) + "|" + String.valueOf(transfersUsed) + "|" + String.valueOf(servicebusUsed) + "|" + String.valueOf(storagetransactionsUsed) + "|" + flags);};
//		String keyVal = serviceProvider + "|" + regionName + "|" + paymentPlan + "|" + featureSet + "|" + subfeature + "|" + replicas + "|" + String.valueOf(storageUsed) + "|" + String.valueOf(requestsUsed) + "|" + String.valueOf(transfersUsed) + "|" + String.valueOf(servicebusUsed) + "|" + String.valueOf(storagetransactionsUsed) + "|" + flags;
		String keyVal = serviceProvider + "|" + regionName + "|" + uniqueRuleGenerator + "|" + paymentPlan + "|" + featureSet + "|" + subfeature + "|" + replicas + "|" + flags;
		if (cstOptData.containsKey(keyVal)) { return true; };
		cstOptData.put(keyVal, "Fact inserted");
		return false;
	}

	/**
	 * @return the flagTotalisePrevious
	 */
	public Boolean getFlagTotalisePrevious() {
		return flagTotalisePrevious;
	}

	/**
	 * @param flagTotalisePrevious the flagTotalisePrevious to set
	 */
	public void setFlagTotalisePrevious(Boolean flagTotalisePrevious) {
		this.flagTotalisePrevious = flagTotalisePrevious;
	}


	/**
	 * @return the parentUploadCollection
	 */
	public UploadCollection getOurUpldCollectn() {
		return parentUploadCollection;
	}

	/**
	 * @param parentUploadCollection the parentUploadCollection to set
	 */
	public void setOurUpldCollectn(UploadCollection parentUpldCollection) {
		this.parentUploadCollection = parentUpldCollection;
	}


	/**
	 * @return the paymentPlan
	 */
	public String getPaymentPlan() {
		return paymentPlan;
	}

	/**
	 * @param paymentPlan the paymentPlan to set
	 */
	public void setPaymentPlan(String paymentPlan) {
		this.paymentPlan = paymentPlan;
		populateTotaliserPaymentPlan(this, this.paymentPlan);
		iAmBlank = false;
//		updateCurrentPricerList(myCurrentPositionPricerList, this);
		if (debug) {System.out.println("[Pricing]: Payment plan set to " + paymentPlan);};

	}

	private void populateTotaliserPaymentPlan(Pricing pricedUp,
			String payPlan) {
		myTotaliser.setPlanType(payPlan);

	}

	/**
	 * @return the feature
	 */
	public String getFeatureSet() {
		return featureSet;
	}

	/**
	 * @param feature the feature to set
	 */
	public void setFeatureSet(String feature) {
		this.featureSet = feature;
		populateTotaliserFeatureSet(this, this.featureSet);
		iAmBlank = false;
//		updateCurrentPricerList(myCurrentPositionPricerList, this);
		if (debug) {System.out.println("[Pricing]: FeatureSet set to " + feature);};
	}

	private void populateTotaliserFeatureSet(Pricing pricedUp,
			String feature) {
		myTotaliser.setFeatureType(feature);

	}

	/**
	 * @return the subfeature
	 */
	public String getSubfeature() {
		return subfeature;
	}

	/**
	 * @param subfeature the subfeature to set
	 */
	public void setSubfeature(String subfeature) {
		this.subfeature = subfeature;
		populateTotaliserSubfeature(this, this.subfeature);
		iAmBlank = false;
//		updateCurrentPricerList(myCurrentPositionPricerList, this);
		if (debug) {System.out.println("[Pricing]: Subfeature set to " + subfeature);};
	}

	private void populateTotaliserSubfeature(Pricing pricedUp,
			String subfeat) {
		myTotaliser.setSubfeatureType(subfeat);

	}

	/**
	 * @return the replicas
	 */
	public String getReplicas() {
		return replicas;
	}

	/**
	 * @param replicas the replicas to set
	 */
	public void setReplicas(String replicas) {
		// the spreadsheet must always have a value in the Action column 
		// otherwise it does not fire the rule 
		if ( replicas == "not relevant") {
			replicas = "0";
		};
		if ( replicas == "") {
			replicas = "0";
		};
		
		this.replicas = replicas;
		
		populateTotaliserReplicas(this, this.replicas);
		iAmBlank = false;
//		updateCurrentPricerList(myCurrentPositionPricerList, this);
		if (debug) {System.out.println("[Pricing]: Replicas set to " + replicas);};
	}



	private void populateTotaliserReplicas(Pricing pricedUp,
			String repl) {
		myTotaliser.setReplicas(Integer.valueOf(repl) );

	}
	
	public boolean getIAmBlank() {
		return iAmBlank;
	}

	public void setIAmBlank(Boolean flag) {
		this.iAmBlank = flag;
	}
	public UploadCollection getParentUploadCollection() {
		return parentUploadCollection;
	}

	public void setParentUploadCollection(UploadCollection mother) {
		this.parentUploadCollection = mother;

	}

	/**
	 * @return the grandparentCostOpt
	 */
	public CostOptimiser getGrandparentCostOpt() {
		return grandparentCostOpt;
	}

	/**
	 * @param grandparentCostOpt the grandparentCostOpt to set
	 */
	public void setGrandparentCostOpt(CostOptimiser grandparentCostOpt) {
		this.grandparentCostOpt = grandparentCostOpt;
	}

	/**
	 * @return the storageUsed
	 */
	public Double getStorageUsed() {
		return storageUsed;
	}

	/**
	 * @param storageUsed the storageUsed to set
	 */
	public void setStorageUsed(Double storageUsed) {
		this.storageUsed = storageUsed;
		// can't tell the totaliser yet because it doesnt exist at this stage

	}

	/**
	 * @return the requestsUsed
	 */
	public Double getRequestsUsed() {
		return requestsUsed;
	}

	/**
	 * @param requestsUsed the requestsUsed to set
	 */
	public void setRequestsUsed(Double requestsUsed) {
		this.requestsUsed = requestsUsed;
		myTotaliser.setRequestsUsed(requestsUsed);
	}

	/**
	 * @return the transfersUsed
	 */
	public Double getTransfersUsed() {
		return transfersUsed;
	}

	/**
	 * @param transfersUsed the transfersUsed to set
	 */
	public void setTransfersUsed(Double transfersUsed) {
		this.transfersUsed = transfersUsed;
		myTotaliser.setTransfersUsed(transfersUsed);
	}

	/**
	 * @return the servicebusUsed
	 */
	public Double getServicebusUsed() {
		return servicebusUsed;
	}

	/**
	 * @param servicebusUsed the servicebusUsed to set
	 */
	public void setServicebusUsed(Double servicebusUsed) {
		this.servicebusUsed = servicebusUsed;
		myTotaliser.setServicebusUsed(servicebusUsed);
	}

	/**
	 * @return the storagetransactionsUsed
	 */
	public Double getStoragetransactionsUsed() {
		return storagetransactionsUsed;
	}

	/**
	 * @param storagetransactionsUsed the storagetransactionsUsed to set
	 */
	public void setStoragetransactionsUsed(Double storagetransactionsUsed) {
		this.storagetransactionsUsed = storagetransactionsUsed;
		myTotaliser.setStoragetransactionsUsed(storagetransactionsUsed);
	}

	public void setUniqueRuleGenerator(Integer uniqueRuleGenerator) {
		this.uniqueRuleGenerator = uniqueRuleGenerator;
	}

	public Integer getUniqueRuleGenerator() {
		return uniqueRuleGenerator;
	}

	public String getEnabledFlag() {
		return enabledFlag;
	}

	public void setEnabledFlag(String enabledFlag) {
		this.enabledFlag = enabledFlag;
	}
	

	public Double applyFrequencyOfAccess(Double serviceSubtot, UploadCollection childCollection) {
		// TODO look up how to do Jens's integrals
		
		// TODO tweak these usage multipliers
		
		if (debug) {System.out.println("[Pricing]: serviceSubtot is :-" + serviceSubtot);};
		// we need to factor in the day-to-day access so do it here.
		// The numbers will be arbitrary estimates.
		Double percentageViewed = 2.0; // percentage of data downloaded in time period
		                               // based on a month's total as baseline.
		                               // Tweak this before you tweak the accessMultiplier
		Double accessMultiplier = 0.0; // How much more activity versus one month
		Double serviceTotal = 0.0;
		
		// this assumes billing is based on the calendar month, which 
		// may not always be the case
		if ( childCollection.getFrequency() .equals("10+ accesses per day") ) {
			// based on 30 days * 10 * 2
			accessMultiplier = 600.0; 
			serviceTotal = serviceSubtot * percentageViewed * accessMultiplier / 100;
		} else if ( childCollection.getFrequency() .equals("1-10 accesses per day") ) {
			// based on 30 days * 10 * 1
			accessMultiplier = 300.0; 
			serviceTotal = serviceSubtot * percentageViewed * accessMultiplier / 100;
		} else if ( childCollection.getFrequency() .equals("1-10 accesses per week") ) {
			// based on a seventh of the above
			accessMultiplier = 40.0; 
			serviceTotal = serviceSubtot * percentageViewed * accessMultiplier / 100;
		} else if ( childCollection.getFrequency() .equals("1-10 accesses per month") ) {
			// based on 30 / 30 days * 10 * 1
			accessMultiplier = 10.0; 
			serviceTotal = serviceSubtot * percentageViewed * accessMultiplier / 100;
		} else if ( childCollection.getFrequency() .equals("Infrequent") ) {
			// arbitrary number
			accessMultiplier = 0.1; 
			serviceTotal = serviceSubtot * percentageViewed * accessMultiplier / 100;
		}

		if (debug) {System.out.println("[Pricing]: accessMultiplier is :-" + accessMultiplier);};
		if (debug) {System.out.println("[Pricing]: new serviceTotal is :-" + serviceTotal);};

		return serviceTotal;
	}

	
}

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
import org.kindura.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CostOptimiser
{
	private static final boolean debug = true;

//	public static void main( String args[] )
//	{
//		CostOptimiser launcher = new CostOptimiser();
//		launcher.runKindness();
//	}

	public  CostOptimiser() {

		status = 0;

	}

	private Integer status;
	private Map<String, String> myMetadata;
	private Pricing myPricing;
	private Totaliser myTotaliser;
	private HashMap<String,String> rollupTotaliserHashMap = new HashMap<String,String>(1000);
	private HashMap<String,String> rollupServiceHashMap = new HashMap<String,String>(300);
	private HashMap<String,String> duplicateTotaliserHashMap = new HashMap<String,String>(1000);
	private HashMap<String,String> duplicatePricingHashMap = new HashMap<String,String>(1000);
	private HashMap<String,String> duplicateServiceMatrixHashMap = new HashMap<String,String>(300);
	private HashMap<String,String> duplicateRegulatoryHashMap = new HashMap<String,String>(300);




	public void suggestService(Map<String, String> metadata, UploadRequestHandler mother) {

		String xmlPath = "C:\\Documents and Settings\\ycn94546\\My Documents\\Project_work\\kindura\\kindness\\src\\org\\kindura\\";
		
		StorageProvider provider1 = new StorageProvider( xmlPath + "Amazon.xml" );
		StorageProvider provider2 = new StorageProvider( xmlPath + "Azure.xml" );
		StorageProvider provider3 = new StorageProvider( xmlPath + "Irods-modified.xml" );
		StorageProvider provider4 = new StorageProvider( xmlPath + "Rackspace-modified.xml" );

		StorageProvider[] providers = new StorageProvider[ 4 ];
		providers[0] = provider1;
		providers[1] = provider2;
		providers[2] = provider3;
		providers[3] = provider4;

		// now remember where to find me
		myMetadata = metadata;

		// this will make a Pricing which is empty ready to be populated
		Pricing pricer = new Pricing();
		// now remember where to find me
		myPricing = pricer;

		// create a Totaliser object so we can start storing stuff there later
		Totaliser totaliserItem = new Totaliser(myPricing);
		myTotaliser = totaliserItem;
		myTotaliser.setGreatgrandparentWebApp(this);

		// tell the Pricing the current Totaliser
		myPricing.setMyTotaliser(totaliserItem);

		myPricing.constructorFilterObjects(myMetadata);

		UploadCollection myCollection = new UploadCollection( myMetadata, myPricing );
		myPricing.setOurUpldCollectn(myCollection);
		status = myPricing.loadDrools(myCollection);
		myCollection.setChildPrcng(myPricing);
		myCollection.setThisUpldCollection(myCollection);
		myCollection.setParentWebApp(this);

		myPricing.setGrandparentWebApp(this);

		status = pricer.initDrools(myMetadata, myCollection);
		// force a dummy Fact in the working memory
		myCollection.setServiceProviderList("dummy");
		
		if (debug) {System.out.println("***");};
		if (debug) {System.out.println("*** Doing first fire - load up sensitivity");};
		if (debug) {System.out.println("***");};
		status = pricer.runDrools();

		myCollection.getMyDroolsSession().update(myCollection.getHandleUploadCollection(), myCollection);

		if (debug) {System.out.println("***");};
		if (debug) {System.out.println("*** Doing second fire - sensitivity and region code to billing region and SP");};
		if (debug) {System.out.println("***");};
		/// now run it again to pick up the results from the first insert
		status = pricer.runDrools();

		myCollection.getMyDroolsSession().update(myCollection.getHandleUploadCollection(), myCollection);

		if (debug) {System.out.println("***");};
		if (debug) {System.out.println("*** Doing third fire - billing region and SP propagated to Pay plan and Features");};
		if (debug) {System.out.println("***");};
		/// now run it again to pick up more results from the second insert
		status = pricer.runDrools();

		myCollection.getMyDroolsSession().update(myCollection.getHandleUploadCollection(), myCollection);

		if (debug) {System.out.println("Collection details....:");};
		if (debug) {System.out.println("Appraisal: " + myCollection.getAppraisalDateAsString() );};
		if (debug) {System.out.println("Copies: " + myCollection.getMinCopies() );};
		if (debug) {System.out.println("Storage tiers: ");};
		for ( StorageProviderTier resultingTier: myCollection.getTiers() )
		{
			if (debug) {System.out.println("\t" + resultingTier);};
		}	
		if (debug) {System.out.println("New provider list:");};
		for ( StorageProvider p : myCollection.getProviders() )
		{
			if (debug) {System.out.println( "Name: " + p.name );};
			for ( StorageProviderConstraint c: p.getConstraints() )
			{
				if (debug) {System.out.println("Constraint: " + c.getType() + " = " + c.getValue() );};
			}
			if (debug) {System.out.println("");};
		}
		for ( StorageProvider p: providers )
		{
			myCollection.addStorage( p );
			if (debug) {System.out.println("Added storage " + p.name );};
		}

		rollupTots(mother, myCollection);

		// TODO - a proper destructor here 
		myPricing.destructorPricing();

	}

	/**
	 * @return the rollupTotaliserHashMap
	 */
	public HashMap<String,String> getRollupTotaliserHashMap() {
		return rollupTotaliserHashMap;
	}

	/**
	 * @param rollupTotaliserHashMap the rollupTotaliserHashMap to set
	 */
	public void setRollupTotaliserHashMap(HashMap<String,String> rollupTotaliserHashMap) {
		this.rollupTotaliserHashMap = rollupTotaliserHashMap;
	}
	/**
	 * @return the duplicatePricingHashMap
	 */
	public HashMap<String,String> getDuplicatePricingHashMap() {
		return duplicatePricingHashMap;
	}

	/**
	 * @param duplicatePricingHashMap the duplicatePricingHashMap to set
	 */
	public void setDuplicatePricingHashMap(HashMap<String,String> duplicatePricingHashMap) {
		this.duplicatePricingHashMap = duplicatePricingHashMap;
	}

	/**
	 * @return the duplicateServiceMatrixHashMap
	 */
	public HashMap<String,String> getDuplicateServiceMatrixHashMap() {
		return duplicateServiceMatrixHashMap;
	}

	/**
	 * @param duplicateServiceMatrixHashMap the duplicateServiceMatrixHashMap to set
	 */
	public void setDuplicateServiceMatrixHashMap(HashMap<String,String> duplicateServiceMatrixHashMap) {
		this.duplicateServiceMatrixHashMap = duplicateServiceMatrixHashMap;
	}

	/**
	 * @return the duplicateRegulatoryHashMap
	 */
	public HashMap<String,String> getDuplicateRegulatoryHashMap() {
		return duplicateRegulatoryHashMap;
	}

	/**
	 * @param duplicateRegulatoryHashMap the duplicateRegulatoryHashMap to set
	 */
	public void setDuplicateRegulatoryHashMap(HashMap<String,String> duplicateRegulatoryHashMap) {
		this.duplicateRegulatoryHashMap = duplicateRegulatoryHashMap;
	}

	/**
	 * @return the duplicateTotaliserHashMap
	 */
	public HashMap<String,String> getDuplicateTotaliserHashMap() {
		return duplicateTotaliserHashMap;
	}

	/**
	 * @param duplicateTotaliserHashMap the duplicateTotaliserHashMap to set
	 */
	public void setDuplicateTotaliserHashMap(HashMap<String,String> duplicateTotaliserHashMap) {
		this.duplicateTotaliserHashMap = duplicateTotaliserHashMap;
	}


	private Double applyFrequencyOfAccess(Double serviceSubtot, UploadCollection childCollection) {
		// TODO look up how to do Jens's integrals
		
		// TODO tweak these usage multipliers
		
		// we need to factor in the day-to-day access so do it here
		Double percentageViewed = 2.0; // percentage of data downloaded in time period
		                               // based on a month's total as baseline.
		                               // Tweak this before you tweak the accessMultiplier
		Double accessMultiplier = 0.0; // How much more activity versus one month
		Double serviceTotal = 0.0;
		
		if ( childCollection.getFrequency() .equals("10+ accesses per day") ) {
			accessMultiplier = 300.0; 
			serviceTotal = serviceSubtot * percentageViewed * accessMultiplier;
		} else if ( childCollection.getFrequency() .equals("1-10 accesses per day") ) {
			accessMultiplier = 150.0; 
			serviceTotal = serviceSubtot * percentageViewed * accessMultiplier;
		} else if ( childCollection.getFrequency() .equals("1-10 accesses per week") ) {
			accessMultiplier = 30.0; 
			serviceTotal = serviceSubtot * percentageViewed * accessMultiplier;
		} else if ( childCollection.getFrequency() .equals("1-10 accesses per month") ) {
			accessMultiplier = 5.0; 
			serviceTotal = serviceSubtot * percentageViewed * accessMultiplier;
		} else if ( childCollection.getFrequency() .equals("Infrequent") ) {
			accessMultiplier = 0.1; 
			serviceTotal = serviceSubtot * percentageViewed * accessMultiplier;
		}

		return serviceTotal;
	}

	private void rollupTots(UploadRequestHandler rqstHandler, UploadCollection myCollctn) {
		String keyPart = "";
		String valPart = "";
		String backendTxt = "";
		String frontendTxt = "";
		String frontendTxt1 = "";
		String frontendTxt2 = "";
		String frontendTxtSP = "";
		String frontendTxtPP = "";
		String frontendTxtREG = "";
		Integer backendTxtPosition = 0;
		Integer backendTxtLength = 0;
		Integer keyPartLength = 0;
		Integer frontendTxtLength = 0;
		Integer frontendTxtPosition = 0;
		Integer frontendTxtSPpos = 0;
		String serviceProviderAccount = "";

		Integer firstpartTxtLength = 0;
		Integer firstpartTxtPosition = 0;
		String firstpartTxt = "";

		Integer secondpartTxtLength = 0;
		Integer secondpartTxtPosition = 0;
		String secondpartTxt = "";

		Integer thirdpartTxtLength = 0;
		Integer thirdpartTxtPosition = 0;
		String thirdpartTxt = "";

		Integer fourthpartTxtLength = 0;
		Integer fourthpartTxtPosition = 0;
		String fourthpartTxt = "";
		
		String txtSP = "";
		String txtReg = "";
		String txtPayPlan = "";

		Double calcStoragePriceSubtotal = 0.0;
		Double calcRequestsPriceSubtotal = 0.0;
		Double calcTransfersPriceSubtotal = 0.0;
		Double calcServicebusPriceSubtotal = 0.0;
		Double calcStoragetransactionsPriceSubtotal = 0.0;
		Double calcCostPerReplica = 0.0;
		Double calcIngestTotal = 0.0;
		Double calcMigrationTotal = 0.0;
		Double valNumber = 0.0;
		String valText = "";

		if (debug) {System.out.println("[Cost Optimiser]: *** before walk rollupTotaliserHashMap");};
		Iterator walk = this.getRollupTotaliserHashMap().entrySet().iterator();
		while (walk.hasNext()) {
			Map.Entry pairs = (Map.Entry)walk.next();

			calcStoragePriceSubtotal = 0.0;
			calcRequestsPriceSubtotal = 0.0;
			calcTransfersPriceSubtotal = 0.0;
			calcServicebusPriceSubtotal = 0.0;
			calcStoragetransactionsPriceSubtotal = 0.0;
			calcIngestTotal = 0.0;
			calcMigrationTotal = 0.0;
			calcCostPerReplica = 0.0;

			keyPart = (String) pairs.getKey();
			valPart = (String) pairs.getValue();
			if (debug) {System.out.println("[Cost Optimiser]: keyPart is " + keyPart);};
			if (debug) {System.out.println("[Cost Optimiser]: valPart is " + valPart);};

			// finds the last occurance of the separator
			backendTxtPosition = keyPart.lastIndexOf("|") + 1;
			backendTxtLength = keyPart.length() - backendTxtPosition;
			frontendTxtPosition = backendTxtPosition - 2;
			frontendTxt = keyPart.substring(0, frontendTxtPosition);
			backendTxt = keyPart.substring(backendTxtPosition, backendTxtPosition + backendTxtLength);

			firstpartTxtPosition = frontendTxt.indexOf("|");
			firstpartTxtLength = frontendTxt.length() - firstpartTxtPosition;
			txtSP = frontendTxt.substring(0, firstpartTxtPosition);
			firstpartTxt = frontendTxt.substring(firstpartTxtPosition + 1, frontendTxt.length());

			secondpartTxtPosition = firstpartTxt.indexOf("|");
			secondpartTxtLength = firstpartTxt.length() - secondpartTxtPosition;
			txtReg = firstpartTxt.substring(0, secondpartTxtPosition);
			secondpartTxt = firstpartTxt.substring(secondpartTxtPosition + 1, firstpartTxt.length());

			thirdpartTxtPosition = secondpartTxt.indexOf("|");
			thirdpartTxtLength = secondpartTxt.length() - thirdpartTxtPosition;	
			txtPayPlan = secondpartTxt.substring(0, thirdpartTxtPosition);
			thirdpartTxt = secondpartTxt.substring(thirdpartTxtPosition + 1, secondpartTxt.length());

			fourthpartTxtPosition = thirdpartTxt.indexOf("|");
			fourthpartTxtLength = thirdpartTxt.length() - fourthpartTxtPosition;			
			fourthpartTxt = thirdpartTxt.substring(fourthpartTxtPosition + 1, thirdpartTxt.length());
			
			serviceProviderAccount = txtSP + "|" + txtReg + "|" + txtPayPlan;
			
//			if (debug) {System.out.println("[Cost Optimiser]: keyPart.length() is " + keyPart.length());};
//			if (debug) {System.out.println("[Cost Optimiser]: backendTxtPosition is " + backendTxtPosition);};
//			if (debug) {System.out.println("[Cost Optimiser]: backendTxtLength is " + backendTxtLength);};
//			if (debug) {System.out.println("[Cost Optimiser]: backendTxt is " + backendTxt);};
//
//			if (debug) {System.out.println("[Cost Optimiser]: frontendTxtPosition is " + frontendTxtPosition);};
//			if (debug) {System.out.println("[Cost Optimiser]: frontendTxtLength is " + frontendTxtLength);};
//			if (debug) {System.out.println("[Cost Optimiser]: frontendTxt is " + frontendTxt);};
//
//			if (debug) {System.out.println("[Cost Optimiser]: firstpartTxtPosition is " + firstpartTxtPosition);};
//			if (debug) {System.out.println("[Cost Optimiser]: firstpartTxtLength is " + firstpartTxtLength);};
//			if (debug) {System.out.println("[Cost Optimiser]: firstpartTxt is " + firstpartTxt);};
//
//			if (debug) {System.out.println("[Cost Optimiser]: secondpartTxtPosition is " + secondpartTxtPosition);};
//			if (debug) {System.out.println("[Cost Optimiser]: secondpartTxtLength is " + secondpartTxtLength);};
//			if (debug) {System.out.println("[Cost Optimiser]: secondpartTxt is " + secondpartTxt);};
//
//			if (debug) {System.out.println("[Cost Optimiser]: thirdpartTxtPosition is " + thirdpartTxtPosition);};
//			if (debug) {System.out.println("[Cost Optimiser]: thirdpartTxtLength is " + thirdpartTxtLength);};
//			if (debug) {System.out.println("[Cost Optimiser]: thirdpartTxt is " + thirdpartTxt);};
//
//			if (debug) {System.out.println("[Cost Optimiser]: fourthpartTxtPosition is " + fourthpartTxtPosition);};
//			if (debug) {System.out.println("[Cost Optimiser]: fourthpartTxtLength is " + fourthpartTxtLength);};
//			if (debug) {System.out.println("[Cost Optimiser]: fourthpartTxt is " + fourthpartTxt);};
			if ( backendTxt .equals("storagePriceSubtotal")) {
				calcStoragePriceSubtotal = Double.parseDouble(valPart);
			} else {
				if ( backendTxt .equals("requestsPriceSubtotal")) {
					calcRequestsPriceSubtotal = Double.parseDouble(valPart);
				} else {
					if ( backendTxt .equals("transfersPriceSubtotal")) {
						calcTransfersPriceSubtotal = Double.parseDouble(valPart);
						// apply a usage multiplier against the transfer rates
						calcTransfersPriceSubtotal = applyFrequencyOfAccess(calcTransfersPriceSubtotal, myCollctn);
					} else {
						if ( backendTxt .equals("servicebusPriceSubtotal")) {
							calcServicebusPriceSubtotal = Double.parseDouble(valPart);
						} else {
							if ( backendTxt .equals("storagetransactionsPriceSubtotal")) {
								calcStoragetransactionsPriceSubtotal = Double.parseDouble(valPart);
							} else {
								if ( backendTxt .equals("costPerReplica")) {
									calcCostPerReplica = Double.parseDouble(valPart);
								}
							}
						}
					}
				}
			}
			if (debug) {System.out.println("[Cost Optimiser]: calcStoragePriceSubtotal is " + calcStoragePriceSubtotal + " for key " + keyPart ); };
			if (debug) {System.out.println("[Cost Optimiser]: calcCostPerReplica is " + calcCostPerReplica + " for key " + keyPart ); };
			if (debug) {System.out.println("[Cost Optimiser]: calcTransfersPriceSubtotal is " + calcTransfersPriceSubtotal + " for key " + keyPart ); };
			if (debug) {System.out.println("[Cost Optimiser]:   getting value from " + valPart ); };

			calcIngestTotal = calcStoragePriceSubtotal + calcRequestsPriceSubtotal + calcTransfersPriceSubtotal + calcServicebusPriceSubtotal + calcStoragetransactionsPriceSubtotal;
			calcMigrationTotal = calcTransfersPriceSubtotal + calcStoragePriceSubtotal + calcRequestsPriceSubtotal + calcServicebusPriceSubtotal + calcStoragetransactionsPriceSubtotal;

			frontendTxt1 = serviceProviderAccount + "|IngestTotal";
			if ( getRollupServiceHashMap().containsKey(frontendTxt1)) { 
				valText = getRollupServiceHashMap().get(frontendTxt1);
				valNumber = Double.parseDouble(valText);
				valNumber += calcIngestTotal;
				getRollupServiceHashMap().put(frontendTxt1, String.valueOf(valNumber));
			} else {
				getRollupServiceHashMap().put(frontendTxt1, String.valueOf(calcIngestTotal));					
			}

			frontendTxt2 = serviceProviderAccount + "|MigrationTotal";
			if ( getRollupServiceHashMap().containsKey(frontendTxt2)) { 
				valText = getRollupServiceHashMap().get(frontendTxt2);
				valNumber = Double.parseDouble(valText);
				valNumber += calcMigrationTotal;
				getRollupServiceHashMap().put(frontendTxt2, String.valueOf(valNumber));
			} else {
				getRollupServiceHashMap().put(frontendTxt2, String.valueOf(calcMigrationTotal));					
			}
		}

		
		if (debug) {System.out.println("[Cost Optimiser]: *** before walk rollupTotaliserHashMap");};
		Iterator trot = this.getRollupServiceHashMap().entrySet().iterator();
		while (trot.hasNext()) {
			Map.Entry pairs = (Map.Entry)trot.next();
			keyPart = (String) pairs.getKey();
			valPart = (String) pairs.getValue();

			if (debug) {System.out.println("[Cost Optimiser]: keyPart is " + keyPart);};
			if (debug) {System.out.println("[Cost Optimiser]: valPart is " + valPart);};
			// finds the last occurance of the separator
			backendTxtPosition = keyPart.lastIndexOf("|") + 1;
			backendTxtLength = keyPart.length() - backendTxtPosition;
			backendTxt = keyPart.substring(backendTxtPosition, keyPart.length());
			frontendTxtSPpos = backendTxtPosition - 1;
			frontendTxtSP = keyPart.substring(0, frontendTxtSPpos);
			
			if ( backendTxt .equals("IngestTotal")) {
				calcIngestTotal = Double.parseDouble(valPart);
				if (debug) {System.out.println("[Cost Optimiser]: calcIngestTotal is " + calcIngestTotal + " for key " + keyPart);};
				int compareDouble = Double.compare(calcIngestTotal, rqstHandler.getCheapestIngestValue());
				if ( (calcIngestTotal > 0.0) && ( compareDouble < 0 ) ) {
					rqstHandler.setCheapestIngestValue(calcIngestTotal);
					// nb we store the other key without the trailing Ingest text
					rqstHandler.setCheapestIngestKey(frontendTxtSP);
					// TODO bodge up the currency for now
					rqstHandler.setCheapestIngestValueCurrency("US$");
					if (debug) {System.out.println("[Cost Optimiser]: cheaper Ingest with " + rqstHandler.getCheapestIngestValue() + " for key " + rqstHandler.getCheapestIngestKey());};
				}
			} else {
				if ( backendTxt .equals("MigrationTotal")) {
					calcMigrationTotal = Double.parseDouble(valPart);
					if (debug) {System.out.println("[Cost Optimiser]: calcMigrationTotal is " + calcMigrationTotal + " for key " + keyPart);};
					int compareDouble2 = Double.compare(calcMigrationTotal, rqstHandler.getCheapestMigrationValue());
					if ( (calcMigrationTotal > 0.0) && ( compareDouble2 < 1 ) ) {
						rqstHandler.setCheapestMigrationValue(calcMigrationTotal);
						// nb we store the other key without the trailing Migration text
						rqstHandler.setCheapestMigrationKey(frontendTxtSP);
						// TODO bodge up the currency for now
						rqstHandler.setCheapestMigrationValueCurrency("US$");
						if (debug) {System.out.println("[Cost Optimiser]: cheaper Migration with " + rqstHandler.getCheapestMigrationValue() + " for key " + rqstHandler.getCheapestMigrationKey());};
					}
				}
			}
		}
		if (debug) {System.out.println("[Cost Optimiser]: *** end walk rollupTotaliserHashMap");};
		if (debug) {System.out.println("[Cost Optimiser]: Overall cheapest Ingest is " + rqstHandler.getCheapestIngestValue() + " for key " + rqstHandler.getCheapestIngestKey());};
		if (debug) {System.out.println("[Cost Optimiser]: Overall cheapest Migration is " + rqstHandler.getCheapestMigrationValue() + " for key " + rqstHandler.getCheapestMigrationKey());};

	}

	
	/**
	 * @return the rollupServiceHashMap
	 */
	public HashMap<String,String> getRollupServiceHashMap() {
		return rollupServiceHashMap;
	}

	/**
	 * @param rollupServiceHashMap the rollupServiceHashMap to set
	 */
	public void setRollupServiceHashMap(HashMap<String,String> rollupServiceHashMap) {
		this.rollupServiceHashMap = rollupServiceHashMap;
	}

}

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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class CostOptimiser
{
	private static final boolean debug = true;

	public  CostOptimiser() {

		status = 0;
		sortedIngestList.clear();
		sortedMigrationList.clear();	

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

	private LinkedList<String> sortedIngestList = new LinkedList<String>();
	private LinkedList<String> sortedMigrationList = new LinkedList<String>();
	
	private String opsFlag;



	public void suggestService(String opFlag, Map<String, String> metadata, LinkedList<String> conclusionIngestList, LinkedList<String> conclusionMigrationList, LinkedList<String> conclusionDropList) {

		setOpsFlag(opFlag);

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
		myTotaliser.setGreatgrandparentCostOpt(this);

		// tell the Pricing the current Totaliser
		myPricing.setMyTotaliser(totaliserItem);


		UploadCollection myCollection = new UploadCollection( myMetadata, myPricing );
		myPricing.setOurUpldCollectn(myCollection);
		myPricing.constructorFilterObjects(myMetadata, myCollection);
		status = myPricing.loadDrools(myCollection);
		myCollection.setChildPrcng(myPricing);
		myCollection.setThisUpldCollection(myCollection);
		myCollection.setParentCostOpt(this);

		myPricing.setGrandparentCostOpt(this);

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

//		myCollection.getMyDroolsSession().update(myCollection.getHandleUploadCollection(), myCollection);
//
//		if (debug) {System.out.println("***");};
//		if (debug) {System.out.println("*** Doing third fire - billing region and SP propagated to Pay plan and Features");};
//		if (debug) {System.out.println("***");};
//		/// now run it again to pick up more results from the second insert
//		status = pricer.runDrools();
//
//		myCollection.getMyDroolsSession().update(myCollection.getHandleUploadCollection(), myCollection);

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

		rollupTots(myCollection);
		sortedIngestList.clear();
		sortedMigrationList.clear();
		conclusionIngestList.clear();
		conclusionMigrationList.clear();
		conclusionDropList.clear();
		buildCostList(myCollection, sortedIngestList, sortedMigrationList);
		sortCostList(myCollection, sortedIngestList, sortedMigrationList, conclusionDropList);
		calcReplicas(myCollection, metadata, sortedIngestList, sortedMigrationList, conclusionIngestList, conclusionMigrationList, conclusionDropList);
//		decideCostList(myCollection, sortedIngestList, sortedMigrationList);
		// sort it again just in case
		// no don't sort it 
//		sortCostList(myCollection, conclusionIngestList, conclusionMigrationList, conclusionDropList);

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

	public String getOpsFlag() {
		return opsFlag;
	}

	public void setOpsFlag(String opsFlag) {
		this.opsFlag = opsFlag;
	}

	private void rollupTots(UploadCollection myCollctn) {

		// this code needs to be refactored - pop and push onto a stack would be better
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
		
		String txtSP = "";
		String txtReg = "";
		String txtPayPlan = "";
		String txtFeature = "";
		String txtSubfeature = "";
		String txtReplicas = "";

		Double calcStoragePriceSubtotal = 0.0;
		Double calcRequestsPriceSubtotal = 0.0;
		Double calcTransfersPriceSubtotal = 0.0;
		Double calcServicebusPriceSubtotal = 0.0;
		Double calcStoragetransactionsPriceSubtotal = 0.0;
		Double calcCostPerReplica = 0.0;
		Double calcIngestTotal = 0.0;
		Double calcMigrationTotal = 0.0;
		Double calcIngestApportioned = 0.0;
		Double calcMigrationApportioned = 0.0;
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

			// finds the last occurrence of the separator
			backendTxtPosition = keyPart.lastIndexOf("|") + 1;
			backendTxtLength = keyPart.length() - backendTxtPosition;
			frontendTxtPosition = backendTxtPosition - 2;
			frontendTxt = keyPart.substring(0, frontendTxtPosition);
			backendTxt = keyPart.substring(backendTxtPosition, backendTxtPosition + backendTxtLength);

			txtSP = popString(keyPart, 1);
			txtReg = popString(keyPart, 2);
			txtPayPlan = popString(keyPart, 3);
			txtFeature = popString(keyPart, 4);
			txtSubfeature = popString(keyPart, 5);
			txtReplicas = popString(keyPart, 6);
						
			serviceProviderAccount = txtSP + "|" + txtReg + "|" + txtPayPlan;
			
			if ( backendTxt .equals("storagePriceSubtotal")) {
				calcStoragePriceSubtotal = Double.parseDouble(valPart);
			} else {
				if ( backendTxt .equals("requestsPriceSubtotal")) {
					calcRequestsPriceSubtotal = Double.parseDouble(valPart);
					// apply a usage multiplier against the request amounts
					calcRequestsPriceSubtotal = myPricing.applyFrequencyOfAccess(calcRequestsPriceSubtotal, myCollctn);
				} else {
					if ( backendTxt .equals("transfersPriceSubtotal")) {
						calcTransfersPriceSubtotal = Double.parseDouble(valPart);
						// apply a usage multiplier against the transfer rates
						calcTransfersPriceSubtotal = myPricing.applyFrequencyOfAccess(calcTransfersPriceSubtotal, myCollctn);
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

			// most service providers have transfer-in cost of zero
			// so we just add up both transfer-in and transfer-out costs that drools has 
			// found into the same subtotal for migration even though having both is impossible

			calcIngestTotal = calcStoragePriceSubtotal + calcRequestsPriceSubtotal + calcTransfersPriceSubtotal + calcServicebusPriceSubtotal + calcStoragetransactionsPriceSubtotal;
			calcMigrationTotal = calcTransfersPriceSubtotal + calcStoragePriceSubtotal + calcRequestsPriceSubtotal + calcServicebusPriceSubtotal + calcStoragetransactionsPriceSubtotal;
			calcIngestApportioned = calcCostPerReplica + calcRequestsPriceSubtotal + calcTransfersPriceSubtotal + calcServicebusPriceSubtotal + calcStoragetransactionsPriceSubtotal;
			calcMigrationApportioned = calcTransfersPriceSubtotal + calcCostPerReplica + calcRequestsPriceSubtotal + calcServicebusPriceSubtotal + calcStoragetransactionsPriceSubtotal;

			if (debug) {System.out.println("[Cost Optimiser]: calcIngestTotal is " + calcIngestTotal + " for key " + keyPart ); };
			if (debug) {System.out.println("[Cost Optimiser]: calcMigrationTotal is " + calcMigrationTotal + " for key " + keyPart ); };

			
			// TODO replicas should not be in the key
			frontendTxt1 = serviceProviderAccount + "|" + txtReplicas + "|IngestTotal";
			if ( getRollupServiceHashMap().containsKey(frontendTxt1)) { 
				valText = getRollupServiceHashMap().get(frontendTxt1);
				valNumber = Double.parseDouble(valText);
				valNumber += calcIngestTotal;
				getRollupServiceHashMap().put(frontendTxt1, String.valueOf(valNumber));
			} else {
				getRollupServiceHashMap().put(frontendTxt1, String.valueOf(calcIngestTotal));					
			}
			frontendTxt1 = serviceProviderAccount + "|" + txtReplicas + "|IngestApportioned";
			if ( getRollupServiceHashMap().containsKey(frontendTxt1)) { 
				valText = getRollupServiceHashMap().get(frontendTxt1);
				valNumber = Double.parseDouble(valText);
				valNumber += calcIngestApportioned;
				getRollupServiceHashMap().put(frontendTxt1, String.valueOf(valNumber));
			} else {
				getRollupServiceHashMap().put(frontendTxt1, String.valueOf(calcIngestApportioned));					
			}

			//			sortedIngestList.add(calcIngestTotal + "|" + serviceProviderAccount + "|" + "US$");

			frontendTxt2 = serviceProviderAccount + "|" + txtReplicas + "|MigrationTotal";
			if ( getRollupServiceHashMap().containsKey(frontendTxt2)) { 
				valText = getRollupServiceHashMap().get(frontendTxt2);
				valNumber = Double.parseDouble(valText);
				valNumber += calcMigrationTotal;
				getRollupServiceHashMap().put(frontendTxt2, String.valueOf(valNumber));
			} else {
				getRollupServiceHashMap().put(frontendTxt2, String.valueOf(calcMigrationTotal));					
			}
			frontendTxt2 = serviceProviderAccount + "|" + txtReplicas + "|MigrationApportioned";
			if ( getRollupServiceHashMap().containsKey(frontendTxt2)) { 
				valText = getRollupServiceHashMap().get(frontendTxt2);
				valNumber = Double.parseDouble(valText);
				valNumber += calcMigrationApportioned;
				getRollupServiceHashMap().put(frontendTxt2, String.valueOf(valNumber));
			} else {
				getRollupServiceHashMap().put(frontendTxt2, String.valueOf(calcMigrationApportioned));					
			}
//			sortedMigrationList.add(calcMigrationTotal + "|" + serviceProviderAccount + "|" + "US$");
		}

		if (debug) {System.out.println("[Cost Optimiser]: *** after walk rollupTotaliserHashMap");};		

	}

	private void buildCostList(UploadCollection myCollctn, LinkedList<String> sortedIngestList, LinkedList<String> sortedMigrationList) {

		String keyPart = "";
		String valPart = "";
		String backendTxt = "";
		String frontendTxtSP = "";

		Double calcIngestTotal = 0.0;
		Double calcMigrationTotal = 0.0;
	
		BigDecimal cost;
		String costSortable;

		// the previous loop rolled up the totals by SP/region/Payment plan.
		// Now we go through it building a list of costs plus 
		// SP/region/Payment as key ready for sorting into cost sequence.
		if (debug) {System.out.println("[Cost Optimiser]: *** before walk rollupServiceHashMap");};
		Iterator trot = this.getRollupServiceHashMap().entrySet().iterator();
		while (trot.hasNext()) {
			Map.Entry pairs = (Map.Entry)trot.next();
			keyPart = (String) pairs.getKey();
			valPart = (String) pairs.getValue();

			if (debug) {System.out.println("[Cost Optimiser]: keyPart is " + keyPart);};
			if (debug) {System.out.println("[Cost Optimiser]: valPart is " + valPart);};
			
			// this is operFlag type
			backendTxt = dequeueString(keyPart, 1);
			// this is SP, Reg, PayPlan, Replicas
			// TODO replicas should not be in the key
			frontendTxtSP = popString(keyPart, 1) + "|" + popString(keyPart, 2) + "|" + popString(keyPart, 3) + "|" + popString(keyPart, 4);
			if (debug) {System.out.println("[Cost Optimiser]: backendTxt is " + backendTxt);};
			if (debug) {System.out.println("[Cost Optimiser]: frontendTxtSP is " + frontendTxtSP);};
			
			
			if ( backendTxt .equals("IngestApportioned")) {
				calcIngestTotal = Double.parseDouble(valPart);
				cost = BigDecimal.valueOf(calcIngestTotal);
				// amounts are rounded up so that very tiny fractions of one penny appear as at
				// least 1p or 1cent.
				cost = cost.setScale(2, BigDecimal.ROUND_UP);
				costSortable = String.format("%10.02f", cost);

				if (debug) {System.out.println("[Cost Optimiser]: calcIngestTotal is " + calcIngestTotal + " for key " + frontendTxtSP);};
				if (debug) {System.out.println("[Cost Optimiser]: rounded Cost is " + costSortable );};
				// store all lines for ingest - decide later which to use
				// force currency to US$ for now
				sortedIngestList.add(costSortable + "|" + frontendTxtSP + "|" + "US$");
			} else {
				if ( backendTxt .equals("MigrationApportioned")) {
					calcMigrationTotal = Double.parseDouble(valPart);
					cost = BigDecimal.valueOf(calcMigrationTotal);
					// amounts are rounded up so that very tiny fractions of one penny appear as at
					// least 1p or 1cent.
					cost = cost.setScale(2, BigDecimal.ROUND_UP);
					costSortable = String.format("%10.02f", cost);

					if (debug) {System.out.println("[Cost Optimiser]: calcMigrationTotal is " + calcMigrationTotal + " for key " + frontendTxtSP);};
					if (debug) {System.out.println("[Cost Optimiser]: rounded Cost is " + costSortable );};
					// store all migrations 
					// force currency to US$ for now
					sortedMigrationList.add(costSortable + "|" + frontendTxtSP + "|" + "US$");
				}
			}
		}		
		if (debug) {System.out.println("[Cost Optimiser]: *** after walk rollupServiceHashMap");};		

	}

	private void sortCostList(UploadCollection myCollection, LinkedList<String> unsortedIngestList, LinkedList<String> unsortedMigrationList, LinkedList<String> unsortedDropList) {

		// we're not storing objects in the list because we'd have to have
		// a special comparator to do the sort
		
		// sort this into ascending cost sequence
		Collections.sort(unsortedIngestList);
		Collections.sort(unsortedMigrationList);
		Collections.sort(unsortedDropList);
		
//		if (debug) {System.out.println("[Cost Optimiser]: Overall cheapest Ingest is " + rqstHandler.getCheapestIngestValue() + " for key " + rqstHandler.getCheapestIngestKey());};
//		if (debug) {System.out.println("[Cost Optimiser]: Overall cheapest Migration is " + rqstHandler.getCheapestMigrationValue() + " for key " + rqstHandler.getCheapestMigrationKey());};

	}


	private void calcReplicas(UploadCollection myCollctn, Map<String, String> inputMetadata, LinkedList<String> sortedIngestList, LinkedList<String> sortedMigrationList, LinkedList<String> conclusionIngestList, LinkedList<String> conclusionMigrationList, LinkedList<String> conclusionDropList) {

		// this is how many copies are demanded by user
		Integer copiesSoFar = myCollctn.getMinCopies();
		
		String migrData = "";
		Double offeredIngestVal = 0.0;
		Double offeredMigrVal = 0.0;
		String offeredCurr = "";
		String offeredSP = "";
		String offeredReg = "";
		String offeredPayPlan = "";
		String offeredReplicas = "";
		
		// old data refers to data already present in cloud so not relevant to ingest
		String oldSvcPrvRegAcct = "";
		String oldSP = "";
		String oldReg = "";
		String oldPayPlan = "";
		String offeredSvcPrvRegAcct = "";
		
		String frontendTxt1 = "";
		
		BigDecimal cost;
		String costSortableIngest;
		String costSortableMigr;

		
		boolean first_time_thru = true;
			
		if ( ! inputMetadata.containsKey("svcPrvAccountDetails") ) {
			oldSvcPrvRegAcct = "|||";
		} else {
			oldSvcPrvRegAcct = inputMetadata.get("svcPrvAccountDetails");			
		};
		oldSP = popString(oldSvcPrvRegAcct,1);
		oldReg = popString(oldSvcPrvRegAcct,2);
		oldPayPlan = popString(oldSvcPrvRegAcct,3);			
		
		Iterator wander = sortedMigrationList.iterator();
		while ( (wander.hasNext()) && (copiesSoFar > 0)){
			
			migrData = (String)wander.next();
			
			offeredSP = popString(migrData, 2);
			offeredReg = popString(migrData, 3);
			offeredPayPlan = popString(migrData, 4);
			offeredCurr = popString(migrData, 6);
			// this is how many copies are offered by Service Provider
			offeredReplicas = popString(migrData, 5);
			
			// we have to lookup the full cost to replace the cost per replica
			frontendTxt1 = offeredSP + "|" + offeredReg + "|" + offeredPayPlan + "|" + offeredReplicas + "|IngestTotal";
			if ( getRollupServiceHashMap().containsKey(frontendTxt1)) { 
				offeredIngestVal = Double.valueOf(getRollupServiceHashMap().get(frontendTxt1));
			} else {
				offeredIngestVal = 0.0;
			}
			frontendTxt1 = offeredSP + "|" + offeredReg + "|" + offeredPayPlan + "|" + offeredReplicas + "|MigrationTotal";
			if ( getRollupServiceHashMap().containsKey(frontendTxt1)) { 
				offeredMigrVal = Double.valueOf(getRollupServiceHashMap().get(frontendTxt1));
			} else {
				offeredMigrVal = 0.0;
			}
			offeredSvcPrvRegAcct = offeredSP + "|" + offeredReg + "|" + offeredPayPlan;
			
			// amounts are rounded up so that very tiny fractions of one penny appear as at
			// least 1p or 1cent.
			cost = BigDecimal.valueOf(offeredIngestVal);
			cost = cost.setScale(2, BigDecimal.ROUND_UP);
			costSortableIngest = String.format("%08.02f", cost);
			cost = BigDecimal.valueOf(offeredMigrVal);
			cost = cost.setScale(2, BigDecimal.ROUND_UP);
			costSortableMigr = String.format("%08.02f", cost);

			
			if ( (first_time_thru) && (offeredSvcPrvRegAcct .equals(oldSvcPrvRegAcct)) ) {
				// the original prices are the best - so quit looking
				break;
			} else if (offeredSvcPrvRegAcct .equals(oldSvcPrvRegAcct)) {
				// ignore this item - it is itself
				continue;
			} else if ( (offeredSP .equals(oldSP)) && ! (offeredReg .equals(oldReg)) ) {
				// ignore this item - we can't do regional transfers inside one service provider at the 
				// moment because it confuses fedora
				continue;
			} else if ( (getOpsFlag() .equals("ingest")) && ! (offeredIngestVal > 0.0)) {
				// ignore items with zero amounts - they must be corrupt
				continue;
				// this is for migrations
			} else if ( ! (getOpsFlag() .equals("ingest")) && ! (offeredMigrVal > 0.0)) {
				// ignore items with zero amounts - they must be corrupt
				continue;
			}
			if (copiesSoFar > Integer.parseInt(offeredReplicas) ) {
				// this datacentre does not offer enough replicas to consume all of this SP line so we need another datacentre to store it
				copiesSoFar -= Integer.parseInt(offeredReplicas);
				
			} else {
				// this datacentre offers more than enough replicas to consume all of this SP line
				copiesSoFar = 0;
			};
			if (getOpsFlag() .equals("ingest")) {
				conclusionIngestList.add(costSortableIngest + "|" + offeredSP + "|" + offeredReg + "|" + offeredPayPlan + "|" + Integer.parseInt(offeredReplicas) + "|" + offeredCurr);
			} else {
				conclusionMigrationList.add(costSortableMigr + "|" + offeredSP + "|" + offeredReg + "|" + offeredPayPlan + "|" + Integer.parseInt(offeredReplicas) + "|" + offeredCurr);
				conclusionDropList.add(costSortableMigr + "|" + offeredSP + "|" + offeredReg + "|" + offeredPayPlan + "|" + Integer.parseInt(offeredReplicas) + "|" + offeredCurr);
			};
			
			first_time_thru = false;

		}
	}
	

	public String popString(String migrData, Integer seq) {

		// positions are relative to 1
		Integer x = 1;
		if ( migrData .isEmpty()) {
			migrData = "|";
		}
		String txtPop = migrData;
		String tmpPop = txtPop;

		for (x = 1 ; x < seq ; x++ ){
			// force an exit when no more data to pop
			if ( ! ( (tmpPop.indexOf("|")) > 0 )) {
				x = seq;
				continue;
			} else {
				try {
					tmpPop = tmpPop.substring(tmpPop.indexOf("|") + 1, tmpPop.length());			
				} catch (Exception e) {
					tmpPop = "";
				}
			}
		};
		if ( ! ( (tmpPop.indexOf("|")) > 0 )) {
			tmpPop = tmpPop.substring(0, tmpPop.length());
		} else {
			try {
				txtPop = tmpPop.substring( 0, tmpPop.indexOf("|") );
			} catch (Exception e) {
				txtPop = "";
			}
		}

		return txtPop;

	}


	public String dequeueString(String migrData, Integer seq) {

		// positions are relative to 1
		Integer x = 1;
		if ( migrData .isEmpty()) {
			migrData = "|";
		}
		String txtPop = migrData;
		String tmpPop = txtPop;

		for (x = 1 ; x < seq ; x++ ){
			// force an exit when no more data to dequeue
			if ( ! ( (tmpPop.lastIndexOf("|")) > 0 )) {
				x = seq;
				continue;
			} else {
				try {
					tmpPop = tmpPop.substring(0, tmpPop.lastIndexOf("|") - 1);
				} catch (Exception e) {
					txtPop = "";
				}
			}
		};
		if ( ! ( (tmpPop.lastIndexOf("|")) > 0 )) {
			tmpPop = tmpPop.substring(0, tmpPop.length());
		} else {
			try {
				txtPop = tmpPop.substring(tmpPop.lastIndexOf("|") + 1, tmpPop.length());
			} catch (Exception e) {
				txtPop = "";
			}
		}
		return txtPop;

	}

//	private void decideCostList(UploadCollection myCollection, LinkedList<String> sortedIngestList, LinkedList<String> sortedMigrationList) {
//		// TODO Auto-generated method stub
//		
//	}


}

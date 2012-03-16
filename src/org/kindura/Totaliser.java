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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.drools.runtime.StatefulKnowledgeSession;


/**
 * This represents the stepping of the price of storage with other data
 * to make decisions around it
 */
public class Totaliser {

	
	private static final boolean debug = true;

	private StatefulKnowledgeSession ourKsession;
	private UploadCollection grandparentUploadCollection;
	private Pricing parentPricing;
	private CostOptimiser greatgrandparentCostOpt;

    private Double storageUsed = 0.0;
    private Double requestsUsed = 0.0;
    private Double transfersUsed = 0.0;
    private Double servicebusUsed = 0.0;
    private Double storagetransactionsUsed = 0.0;
    private Integer howLongMonths = 0; 
    
    private String basis = "";
    private String currency = "";
    private String timeUnit = "";
    private String flags = "";
    private Double price = 0.00;

    private String bandDescription = "";
    private String sizeUnitUsage = "";
    private String sizeUnitPricing = "";
    private String sizeUnitBanding = "";
    
    private Double storagePriceSubtotal = 0.0;
    private Double requestsPriceSubtotal = 0.0;
    private Double transfersPriceSubtotal = 0.0;
    private Double servicebusPriceSubtotal = 0.0;
    private Double storagetransactionsPriceSubtotal = 0.0;
    private Double costPerReplica = 0.0;
    private Double exitPriceSubtotal = 0.0;
    private Double serviceTotal = 0.0;

    private Double bandStart = 0.0;
    private Double bandEnd = 0.0;
    private Double storageCalc = 0.0;
    private Double serviceTotalIntegral = 0.0;

    private Double sizeUnitBandingNumber;
    private Double sizeUnitPricingNumber;
    private Double sizeUnitUsageNumber;
//    private Double bandToPriceMultiplier;
    private Double bandToUsageMultiplier;
    private Double priceToUsageMultiplier;
    
    private String callUsMessage = "";
    private String debugTracer = "Default";
    private String featureType = "";
    private String planType = "";
    private String regionName = "";
    private String serviceProviderName = "";
    private String subfeatureType = "";
    private Integer replicas = 0;
    private String totaliseTrigger = "";
    
    private Boolean iAmBlank;

    
    
    public Totaliser () {
    	if (debug) {System.out.println("[Totaliser]: ----- Totaliser constructor -----");};
    }    	
        
        public Totaliser (Pricing parentPricing) {
    	iAmBlank = true;

//    	bandToPriceMultiplier = 0.0;
    	bandToUsageMultiplier = 0.0;
    	priceToUsageMultiplier = 0.0;
    	sizeUnitBandingNumber = 0.0;
    	sizeUnitPricingNumber = 0.0;
    	sizeUnitUsageNumber = 0.0;
    	
    	// pull down the amounts into totaliser because drools reads it from there
    	setStorageUsed(parentPricing.getStorageUsed());
    	// TODO other amounts not set at the moment
    	
    	if (debug) {System.out.println("[Totaliser]: ----- Totaliser overloaded constructor -----");};

    }

    public void constructorTotaliserObjects () {
    	
    	iAmBlank = true;
        
        storagePriceSubtotal = 0.0;
        storagetransactionsPriceSubtotal = 0.0;
        servicebusPriceSubtotal = 0.0;
        transfersPriceSubtotal = 0.0;
        requestsPriceSubtotal = 0.0;
        this.setExitPriceSubtotal(0.0, 0, 0);
       
    	if (debug) {System.out.println("[Totaliser]: ----- Totaliser overloaded constructor -----");};

    }
   
    
    public String getBasis() {
        return basis;
    }
    public void setBasis(String basis) {
        if (debug) {System.out.println( "basis is : " + basis);};
        this.basis = basis;
    }
    
    
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        if (debug) {System.out.println( "currency is : " + currency);};
        this.currency = currency;
    }
    
    
    public String getTimeUnit() {
        return timeUnit;
    }
    public void setTimeUnit(String timeUnit) {
        if (debug) {System.out.println( "timeUnit is : " + timeUnit);};
        this.timeUnit = timeUnit;
    }

    
    
    public String getFlags() {
        return flags;
    }
    public void setFlags(String flags) {
        this.flags = flags;
    }


	/**
	 * @return the sizeUnitUsage
	 */
	public String getSizeUnitUsage() {
		return sizeUnitUsage;
	}

	/**
	 * @param sizeUnitUsage the sizeUnitUsage to set
	 */
	public void setSizeUnitUsage(String sizeUnitUsage) {
		this.sizeUnitUsage = sizeUnitUsage;
        if (debug) {System.out.println( "sizeUnitUsage is : " + sizeUnitUsage);};
	}

	/**
	 * @return the sizeUnitPricing
	 */
	public String getSizeUnitPricing() {
		return sizeUnitPricing;
	}

	/**
	 * @param sizeUnitPricing the sizeUnitPricing to set
	 */
	public void setSizeUnitPricing(String sizeUnitPricing) {
		this.sizeUnitPricing = sizeUnitPricing;
        if (debug) {System.out.println( "sizeUnitPricing is : " + sizeUnitPricing);};
	}

	/**
	 * @return the sizeUnitBanding
	 */
	public String getSizeUnitBanding() {
		return sizeUnitBanding;
	}

	/**
	 * @param sizeUnitBanding the sizeUnitBanding to set
	 */
	public void setSizeUnitBanding(String sizeUnitBanding) {
		this.sizeUnitBanding = sizeUnitBanding;
        if (debug) {System.out.println( "sizeUnitBanding is : " + sizeUnitBanding);};
	}

	/**
	 * @return the howLongMonths
	 */
	public Integer getHowLongMonths() {
		howLongMonths = parentPricing.getHowLongMonths();
		return howLongMonths;
	}

	/**
	 * @param howLongMonths the howLongMonths to set
	 */
	public void setHowLongMonths(Integer howLongMonths) {
		this.howLongMonths = howLongMonths;
        if (debug) {System.out.println( "howLongMonths is : " + howLongMonths);};
	}

    /**
	 * @return the storagePriceSubtotal
	 */
	public Double getStoragePriceSubtotal() {
		return storagePriceSubtotal;
	}

	/**
	 * @param storagePriceSubtotal the storagePriceSubtotal to set
	 */
	public void setStoragePriceSubtotal(Double storagePriceSubtotal) {
		this.serviceTotal += storagePriceSubtotal;
		this.storagePriceSubtotal = storagePriceSubtotal;
	}

	/**
	 * @return the requestsPriceSubtotal
	 */
	public Double getRequestsPriceSubtotal() {
		return requestsPriceSubtotal;
	}

	/**
	 * @param requestsPriceSubtotal the requestsPriceSubtotal to set
	 */
	public void setRequestsPriceSubtotal(Double requestsPriceSubtotal) {
		this.serviceTotal += requestsPriceSubtotal;
		this.requestsPriceSubtotal = requestsPriceSubtotal;
	}

	/**
	 * @return the transfersPriceSubtotal
	 */
	public Double getTransfersPriceSubtotal() {
		return transfersPriceSubtotal;
	}

	/**
	 * @param transfersPriceSubtotal the transfersPriceSubtotal to set
	 */
	public void setTransfersPriceSubtotal(Double transfersPriceSubtotal) {
		this.serviceTotal += transfersPriceSubtotal;
		this.transfersPriceSubtotal = transfersPriceSubtotal;
	}

	/**
	 * @return the servicebusPriceSubtotal
	 */
	public Double getServicebusPriceSubtotal() {
		return servicebusPriceSubtotal;
	}

	/**
	 * @param servicebusPriceSubtotal the servicebusPriceSubtotal to set
	 */
	public void setServicebusPriceSubtotal(Double servicebusPriceSubtotal) {
		this.serviceTotal += servicebusPriceSubtotal;
		this.servicebusPriceSubtotal = servicebusPriceSubtotal;
	}

	/**
	 * @return the storagetransactionsPriceSubtotal
	 */
	public Double getStoragetransactionsPriceSubtotal() {
		return storagetransactionsPriceSubtotal;
	}

	/**
	 * @param storagetransactionsPriceSubtotal the storagetransactionsPriceSubtotal to set
	 */
	public void setStoragetransactionsPriceSubtotal(
			Double storagetransactionsPriceSubtotal) {
		this.serviceTotal += storagetransactionsPriceSubtotal;
		this.storagetransactionsPriceSubtotal = storagetransactionsPriceSubtotal;
	}

	/**
	 * @return the exitPriceSubtotal
	 */
	public Double getExitPriceSubtotal() {
		return exitPriceSubtotal;
	}
	
	/**
	 * @param exitPriceSubtotal the exitPriceSubtotal to set
	 */
	public void setExitPriceSubtotal(Double exitPriceSubtotal, Integer calcStorage, Integer howLongMonths) {
		this.serviceTotal += calcStorage * exitPriceSubtotal * getHowLongMonths();
		this.exitPriceSubtotal = exitPriceSubtotal;
	}
	
	public Double getServiceTotal() {
		return serviceTotal;
	}
	/**
	 * @param serviceTotal the serviceTotal to set
	 */
	public void setServiceTotal(Double serviceTotal) {
		this.serviceTotal = serviceTotal;
	}

    
	/**
	 * @return the bandDescription
	 */
	public String getBandDescription() {
		return bandDescription;
	}

	/**
	 * @param bandDescription the bandDescription to set
	 */
	public void setBandDescription(String bandDescription) {
		this.bandDescription = bandDescription;
        if (debug) {System.out.println( "band description is : " + bandDescription);};
	}

    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double basePrice) {
        this.price = basePrice;
    }

	public void accumStorageLine(Double price) {
		// this is for storage calculations
		Double calcStorage = Double.valueOf(storageUsed);
		
		if (getBandDescription() == "") {
			bandStart = 0.0;
			bandEnd = 0.0;
		} else {
			Integer bandBeginLength = 0;
			Integer bandFinitoPosition = 0;
			Integer bandFinitoLength = 0;
			bandBeginLength = getBandDescription().indexOf(",");
			bandFinitoLength = getBandDescription().length() - bandBeginLength - 1;
			bandFinitoPosition = getBandDescription().length() - bandFinitoLength;
			bandStart = Double.parseDouble(new String(getBandDescription().substring(0, bandBeginLength)));
			if (debug) {System.out.println("[Totaliser]: bandStart is " + bandStart);};
			bandEnd = Double.parseDouble(new String(getBandDescription().substring(bandFinitoPosition, getBandDescription().length())));
			if (debug) {System.out.println("[Totaliser]: bandEnd is " + bandEnd + "$");};
		};

		bandToUsageMultiplier = adjustBandVersusUsage();
		priceToUsageMultiplier = adjustUsageVersusPrice();

		
		// should always be true because the rules should be feeding us only that data
		if (calcStorage > ( bandStart * bandToUsageMultiplier ) ) {
			if (calcStorage <= ( bandEnd * bandToUsageMultiplier )) {
				calcStorage -= ( bandStart * bandToUsageMultiplier );
			} else { 
					calcStorage = ( bandEnd * bandToUsageMultiplier ) - ( bandStart * bandToUsageMultiplier );
			};
		};
		
		storagePriceSubtotal = calcStorage * getHowLongMonths() * price * priceToUsageMultiplier;   
		if (debug) {System.out.println( "bandToUsageMultiplier : " + bandToUsageMultiplier );};
		if (debug) {System.out.println( "priceToUsageMultiplier : " + priceToUsageMultiplier );};
		if (debug) {System.out.println( "Size calcStorage : " + calcStorage );};
		if (debug) {System.out.println( "Months() : " + this.getHowLongMonths() );};
		if (debug) {System.out.println( "price : " + price );};
		if (debug) {System.out.println( "*** storagePriceSubtotal : " + this.storagePriceSubtotal );};
		     
		// TODO sometimes we get replicas with zero value coming thru which can't be right in this method
		addLineItem(greatgrandparentCostOpt.getRollupTotaliserHashMap(), "storagePriceSubtotal", storagePriceSubtotal);
		if ( replicas < 1) {
			costPerReplica = storagePriceSubtotal;
		} else {
			costPerReplica = storagePriceSubtotal / replicas;
		}
		if (debug) {System.out.println( "*** costPerReplica : " + this.costPerReplica );};
		addLineItem(greatgrandparentCostOpt.getRollupTotaliserHashMap(), "costPerReplica", costPerReplica);
    }

	public void accumTransferLine(Double price) {
		// this is for Transfer calculations
		Double calcTransfer = Double.valueOf(transfersUsed);

		if (getBandDescription() == "") {
			bandStart = 0.0;
			bandEnd = 0.0;
		} else {
			Integer bandBeginLength = 0;
			Integer bandFinitoPosition = 0;
			Integer bandFinitoLength = 0;
			bandBeginLength = getBandDescription().indexOf(",");
			bandFinitoLength = getBandDescription().length() - bandBeginLength - 1;
			bandFinitoPosition = getBandDescription().length() - bandFinitoLength;
			bandStart = Double.parseDouble(new String(getBandDescription().substring(0, bandBeginLength)));
			if (debug) {System.out.println("[Totaliser]: bandStart is " + bandStart);};
			bandEnd = Double.parseDouble(new String(getBandDescription().substring(bandFinitoPosition, getBandDescription().length())));
			if (debug) {System.out.println("[Totaliser]: bandEnd is " + bandEnd + "$");};
		};

		bandToUsageMultiplier = adjustBandVersusUsage();
		priceToUsageMultiplier = adjustUsageVersusPrice();

		// should always be true because the rules should be feeding us only that data
		if (calcTransfer > ( bandStart * bandToUsageMultiplier ) ) {
			if (calcTransfer <= ( bandEnd * bandToUsageMultiplier )) {
				calcTransfer -= ( bandStart * bandToUsageMultiplier );
			} else { 
				calcTransfer = ( bandEnd * bandToUsageMultiplier ) - ( bandStart * bandToUsageMultiplier );
			};
		};

		// do not accumulate transfer-out for ingest
		// TODO reinstate no transfer-out accum for ingest
//		if ((getSubfeatureType() .equals("Transfers out") ) && (greatgrandparentCostOpt.getOpsFlag() .equals("ingest"))) {
//			;
//		} else {
			transfersPriceSubtotal += calcTransfer * getHowLongMonths() * price * priceToUsageMultiplier;   
			if (debug) {System.out.println( "bandToUsageMultiplier : " + bandToUsageMultiplier );};
			if (debug) {System.out.println( "priceToUsageMultiplier : " + priceToUsageMultiplier );};
			if (debug) {System.out.println( "calcTransfer : " + calcTransfer );};
			if (debug) {System.out.println( "getHowLongMonths() : " + this.getHowLongMonths() );};
			if (debug) {System.out.println( "price : " + price );};
			if (debug) {System.out.println( "transfersPriceSubtotal : " + this.transfersPriceSubtotal );};
			addLineItem(greatgrandparentCostOpt.getRollupTotaliserHashMap(), "transfersPriceSubtotal", transfersPriceSubtotal);
			//		costPerReplica = transfersPriceSubtotal / replicas;
			//		addLineItem(greatgrandparentCostOpt.getRollupTotaliserHashMap(), "transfersCostPerReplica", costPerReplica);				
//		}
	}


	public void accumStoragetransactionsLine(Double price) {
		// this is for Storagetransactions calculations
		Double calcStoragetransactions = Double.valueOf(storagetransactionsUsed);
		
		if (getBandDescription() == "") {
			bandStart = 0.0;
			bandEnd = 0.0;
		} else {
			Integer bandBeginLength = 0;
			Integer bandFinitoPosition = 0;
			Integer bandFinitoLength = 0;
			bandBeginLength = getBandDescription().indexOf(",");
			bandFinitoLength = getBandDescription().length() - bandBeginLength - 1;
			bandFinitoPosition = getBandDescription().length() - bandFinitoLength;
			bandStart = Double.parseDouble(new String(getBandDescription().substring(0, bandBeginLength)));
			if (debug) {System.out.println("[Totaliser]: bandStart is " + bandStart);};
			bandEnd = Double.parseDouble(new String(getBandDescription().substring(bandFinitoPosition, getBandDescription().length())));
			if (debug) {System.out.println("[Totaliser]: bandEnd is " + bandEnd + "$");};
		};

		bandToUsageMultiplier = adjustBandEventVersusUsage();
		priceToUsageMultiplier = adjustUsageVersusPriceEvent();
		
		// should always be true because the rules should be feeding us only that data
		if (calcStoragetransactions > ( bandStart * bandToUsageMultiplier ) ) {
			if (calcStoragetransactions <= ( bandEnd * bandToUsageMultiplier )) {
				calcStoragetransactions -= ( bandStart * bandToUsageMultiplier );
			} else { 
					calcStoragetransactions = ( bandEnd * bandToUsageMultiplier ) - ( bandStart * bandToUsageMultiplier );
			};
		};
		
		storagetransactionsPriceSubtotal = calcStoragetransactions * getHowLongMonths() * price * priceToUsageMultiplier;   
		if (debug) {System.out.println( "bandToUsageMultiplier : " + bandToUsageMultiplier );};
		if (debug) {System.out.println( "priceToUsageMultiplier : " + priceToUsageMultiplier );};
		if (debug) {System.out.println( "calcStoragetransactions : " + calcStoragetransactions );};
		if (debug) {System.out.println( "getHowLongMonths() : " + this.getHowLongMonths() );};
		if (debug) {System.out.println( "price : " + price );};
		if (debug) {System.out.println( "StoragetransactionsPriceSubtotal : " + this.storagetransactionsPriceSubtotal );};
		addLineItem(greatgrandparentCostOpt.getRollupTotaliserHashMap(), "storagetransactionsPriceSubtotal", storagetransactionsPriceSubtotal);
//		costPerReplica = storagetransactionsPriceSubtotal / replicas;
//		addLineItem(greatgrandparentCostOpt.getRollupTotaliserHashMap(), "costPerReplica", costPerReplica);       
    }

	public void accumServicebusLine(Double price) {
		// this is for Servicebus calculations
		Double calcServicebus = Double.valueOf(servicebusUsed);
		
		if (getBandDescription() == "") {
			bandStart = 0.0;
			bandEnd = 0.0;
		} else {
			Integer bandBeginLength = 0;
			Integer bandFinitoPosition = 0;
			Integer bandFinitoLength = 0;
			bandBeginLength = getBandDescription().indexOf(",");
			bandFinitoLength = getBandDescription().length() - bandBeginLength - 1;
			bandFinitoPosition = getBandDescription().length() - bandFinitoLength;
			bandStart = Double.parseDouble(new String(getBandDescription().substring(0, bandBeginLength)));
			if (debug) {System.out.println("[Totaliser]: bandStart is " + bandStart);};
			bandEnd = Double.parseDouble(new String(getBandDescription().substring(bandFinitoPosition, getBandDescription().length())));
			if (debug) {System.out.println("[Totaliser]: bandEnd is " + bandEnd + "$");};
		};

		bandToUsageMultiplier = adjustBandEventVersusUsage();
		priceToUsageMultiplier = adjustUsageVersusPriceEvent();
		
		// should always be true because the rules should be feeding us only that data
		if (calcServicebus > ( bandStart * bandToUsageMultiplier ) ) {
			if (calcServicebus <= ( bandEnd * bandToUsageMultiplier )) {
				calcServicebus -= ( bandStart * bandToUsageMultiplier );
			} else { 
					calcServicebus = ( bandEnd * bandToUsageMultiplier ) - ( bandStart * bandToUsageMultiplier );
			};
		};
		
		servicebusPriceSubtotal = calcServicebus * getHowLongMonths() * price * priceToUsageMultiplier;   
		if (debug) {System.out.println( "bandToUsageMultiplier : " + bandToUsageMultiplier );};
		if (debug) {System.out.println( "priceToUsageMultiplier : " + priceToUsageMultiplier );};
		if (debug) {System.out.println( "calcServicebus : " + calcServicebus );};
		if (debug) {System.out.println( "getHowLongMonths() : " + this.getHowLongMonths() );};
		if (debug) {System.out.println( "price : " + price );};
 		if (debug) {System.out.println( "ServicebusPriceSubtotal : " + this.servicebusPriceSubtotal );};
		addLineItem(greatgrandparentCostOpt.getRollupTotaliserHashMap(), "servicebusPriceSubtotal", servicebusPriceSubtotal);
//		costPerReplica = servicebusPriceSubtotal / replicas;
//		addLineItem(greatgrandparentCostOpt.getRollupTotaliserHashMap(), "costPerReplica", costPerReplica);
    }

	public void accumRequestsLine(Double price) {
		// this is for Requests calculations
		Double calcRequests = Double.valueOf(requestsUsed);
		
		if (getBandDescription() == "") {
			bandStart = 0.0;
			bandEnd = 0.0;
		} else {
			Integer bandBeginLength = 0;
			Integer bandFinitoPosition = 0;
			Integer bandFinitoLength = 0;
			bandBeginLength = getBandDescription().indexOf(",");
			bandFinitoLength = getBandDescription().length() - bandBeginLength - 1;
			bandFinitoPosition = getBandDescription().length() - bandFinitoLength;
			bandStart = Double.parseDouble(new String(getBandDescription().substring(0, bandBeginLength)));
			if (debug) {System.out.println("[Totaliser]: bandStart is " + bandStart);};
			bandEnd = Double.parseDouble(new String(getBandDescription().substring(bandFinitoPosition, getBandDescription().length())));
			if (debug) {System.out.println("[Totaliser]: bandEnd is " + bandEnd + "$");};
		};

		bandToUsageMultiplier = adjustBandEventVersusUsage();
		priceToUsageMultiplier = adjustUsageVersusPriceEvent();
		
		// should always be true because the rules should be feeding us only that data
		if (calcRequests > ( bandStart * bandToUsageMultiplier ) ) {
			if (calcRequests <= ( bandEnd * bandToUsageMultiplier )) {
				calcRequests -= ( bandStart * bandToUsageMultiplier );
			} else { 
					calcRequests = ( bandEnd * bandToUsageMultiplier ) - ( bandStart * bandToUsageMultiplier );
			};
		};
		
		requestsPriceSubtotal = calcRequests * getHowLongMonths() * price * priceToUsageMultiplier;   
		if (debug) {System.out.println( "bandToUsageMultiplier : " + bandToUsageMultiplier );};
		if (debug) {System.out.println( "priceToUsageMultiplier : " + priceToUsageMultiplier );};
		if (debug) {System.out.println( "calcRequests : " + calcRequests );};
		if (debug) {System.out.println( "getHowLongMonths() : " + this.getHowLongMonths() );};
		if (debug) {System.out.println( "price : " + price );};
		if (debug) {System.out.println( "requestsPriceSubtotal : " + this.requestsPriceSubtotal );};
		addLineItem(greatgrandparentCostOpt.getRollupTotaliserHashMap(), "requestsPriceSubtotal", requestsPriceSubtotal);
//		costPerReplica = requestsPriceSubtotal / replicas;
//		addLineItem(greatgrandparentCostOpt.getRollupTotaliserHashMap(), "costPerReplica", costPerReplica);
    }


	public Double adjustBandVersusUsage () {
		Double multiplier = 0.0;
		
		adjustUsageVersusPriceCalcBanding();
		adjustUsageVersusPriceCalcPricing();
		adjustUsageVersusPriceCalcUsage();

		return multiplier = sizeUnitBandingNumber / sizeUnitUsageNumber ;

	}

	public Double adjustUsageVersusPrice () {
		Double multiplier = 0.0;
		adjustUsageVersusPriceCalcBanding();
		adjustUsageVersusPriceCalcPricing();
		adjustUsageVersusPriceCalcUsage();

		return multiplier = sizeUnitUsageNumber / sizeUnitPricingNumber;

	}


	public void adjustUsageVersusPriceCalcBanding() {
		Datasize datasizeBand = Datasize.valueOf(getSizeUnitBanding());
		switch ( datasizeBand ) {
		case TiB :
			this.sizeUnitBandingNumber = 1024.0 * 1024.0 * 1024.0 * 1024.0;
			break;
		case TB :
			this.sizeUnitBandingNumber = 1000000000000.0;
			break;
		case GiB :
			this.sizeUnitBandingNumber = 1024.0 * 1024.0 * 1024.0;
			break;
		case GB :
			this.sizeUnitBandingNumber = 1000000000.0;
			break;
		case MiB :
			this.sizeUnitBandingNumber = 1024.0 * 1024.0;
			break;
		case MB :
			this.sizeUnitBandingNumber = 1000000.0;
			break;
		case KiB :
			this.sizeUnitBandingNumber = 1024.0;
			break;
		case KB :
			this.sizeUnitBandingNumber = 1000.0;
			break;
			// TODO add the cases for PB, PiB, null=Missing
		default :
			this.sizeUnitBandingNumber = 1.0;
			break;
		}
	}

	public void adjustUsageVersusPriceCalcPricing() {
		Datasize datasizePrice = Datasize.valueOf(getSizeUnitPricing());
		switch ( datasizePrice ) {
		case TiB :
			this.sizeUnitPricingNumber = 1024.0 * 1024.0 * 1024.0 * 1024.0;
			break;
		case TB :
			this.sizeUnitPricingNumber = 1000000000000.0;
			break;
		case GiB :
			this.sizeUnitPricingNumber = 1024.0 * 1024.0 * 1024.0;
			break;
		case GB :
			this.sizeUnitPricingNumber = 1000000000.0;
			break;
		case MiB :
			this.sizeUnitPricingNumber = 1024.0 * 1024.0;
			break;
		case MB :
			this.sizeUnitPricingNumber = 1000000.0;
			break;
		case KiB :
			this.sizeUnitPricingNumber = 1024.0;
			break;
		case KB :
			this.sizeUnitPricingNumber = 1000.0;
			break;
		default :
			this.sizeUnitPricingNumber = 1.0;
			break;
		}

	}
	public void adjustUsageVersusPriceCalcUsage() {
		Datasize datasizeUsage = Datasize.valueOf(getSizeUnitUsage());
		switch ( datasizeUsage ) {
		case TiB :
			this.sizeUnitUsageNumber = 1024.0 * 1024.0 * 1024.0 * 1024.0;
			break;
		case TB :
			this.sizeUnitUsageNumber = 1000000000000.0;
			break;
		case GiB :
			this.sizeUnitUsageNumber = 1024.0 * 1024.0 * 1024.0;
			break;
		case GB :
			this.sizeUnitUsageNumber = 1000000000.0;
			break;
		case MiB :
			this.sizeUnitUsageNumber = 1024.0 * 1024.0;
			break;
		case MB :
			this.sizeUnitUsageNumber = 1000000.0;
			break;
		case KiB :
			this.sizeUnitUsageNumber = 1024.0;
			break;
		case KB :
			this.sizeUnitUsageNumber = 1000.0;
			break;
		default :
			this.sizeUnitUsageNumber = 1.0;
			break;
		}
	}

	public Double adjustBandEventVersusUsage () {
		Double multiplier = 0.0;

		adjustUsageVersusPriceEventCalcBanding();
		adjustUsageVersusPriceEventCalcPricing();
		adjustUsageVersusPriceEventCalcUsage();

		return multiplier = sizeUnitBandingNumber / sizeUnitUsageNumber ;

	}
	public Double adjustUsageVersusPriceEvent () {
		Double multiplier = 0.0;
		
		adjustUsageVersusPriceEventCalcBanding();
		adjustUsageVersusPriceEventCalcPricing();
		adjustUsageVersusPriceEventCalcUsage();
		
		return multiplier = sizeUnitUsageNumber / sizeUnitPricingNumber;
	}

	public void adjustUsageVersusPriceEventCalcBanding() {
		Eventsize eventsizeBand = Eventsize.valueOf(getSizeUnitBanding());
		switch ( eventsizeBand ) {
		case M :
			this.sizeUnitBandingNumber = 1000000.0;
			break;
		case K :
			this.sizeUnitBandingNumber = 1000.0;
			break;
		case OneM :
			this.sizeUnitBandingNumber = 1000000.0;
			break;
		case OneK :
			this.sizeUnitBandingNumber = 1000.0;
			break;
		case TenK :
			this.sizeUnitBandingNumber = 10000.0;
			break;
		case HundredK :
			this.sizeUnitBandingNumber = 100000.0;
			break;
		case One :
			this.sizeUnitBandingNumber = 1.0;
			break;
		default :
			this.sizeUnitBandingNumber = 1.0;
			break;
		}
	}
	public void adjustUsageVersusPriceEventCalcPricing() {

		Eventsize eventsizePrice = Eventsize.valueOf(getSizeUnitPricing());
		switch ( eventsizePrice ) {
		case M :
			this.sizeUnitPricingNumber = 1000000.0;
			break;
		case K :
			this.sizeUnitPricingNumber = 1000.0;
			break;
		case TenK :
			this.sizeUnitPricingNumber = 10000.0;
			break;
		case HundredK :
			this.sizeUnitPricingNumber = 100000.0;
			break;
		case One :
			this.sizeUnitPricingNumber = 1.0;
			break;
		default :
			this.sizeUnitPricingNumber = 1.0;
			break;
		}
	}
	public void adjustUsageVersusPriceEventCalcUsage() {
		Eventsize eventsizeUsage = Eventsize.valueOf(getSizeUnitUsage());
		switch ( eventsizeUsage ) {
		case M :
			this.sizeUnitUsageNumber = 1000000.0;
			break;
		case K :
			this.sizeUnitUsageNumber = 1000.0;
			break;
		case TenK :
			this.sizeUnitUsageNumber = 10000.0;
			break;
		case HundredK :
			this.sizeUnitUsageNumber = 100000.0;
			break;
		case One :
			this.sizeUnitUsageNumber = 1.0;
			break;
		default :
			this.sizeUnitUsageNumber = 1.0;
			break;
		}
	}
	
    public String getCallUsMessage() {
        return callUsMessage;
    }
    public void setCallUsMessage(String callUsMsg) {
        this.callUsMessage = callUsMsg;
        if (debug) {System.out.println("[Totaliser]: CallUsMessage is " + callUsMsg);};
        
    }

    public String getDebugTracer() {
        return debugTracer;
    }
    public void setDebugTracer(String tracer) {
        this.debugTracer = tracer;
        if (debug) {System.out.println("[Totaliser]: DebugTracer is " + tracer);};
    }

    public String getFeatureType() {
        return featureType;
    }
    public void setFeatureType(String featureType) {
        this.featureType = featureType;
        iAmBlank = false;
        if (debug) {System.out.println("[Totaliser]: FeatureType is " + featureType);};
    }
    public String getPlanType() {
        return planType;
    }
    public void setPlanType(String planType) {
        this.planType = planType;
        iAmBlank = false;
        if (debug) {System.out.println("[Totaliser]: PlanType is " + planType);};
    }
    public String getRegionName() {
        return regionName;
    }
    public void setRegionName(String regionName) {
        this.regionName = regionName;
        if (debug) {System.out.println("[Totaliser]: RegionName is " + regionName);};
    }
    public String getServiceProviderName() {
        return serviceProviderName;
    }
    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
        if (debug) {System.out.println("[Totaliser]: ServiceProviderName is " + serviceProviderName);};
    }
    public String getSubfeatureType() {
        return subfeatureType;
    }
    public void setSubfeatureType(String subfeatureType) {
        this.subfeatureType = subfeatureType;
        iAmBlank = false;
        if (debug) {System.out.println("[Totaliser]: SubfeatureType is " + subfeatureType);};
    }
    public Integer getReplicas() {
        return replicas;
    }
    public void setReplicas(Integer replicas) {
    	// TODO the value of this field is not usually returned by the spreadsheet
    	// soon enough for all Totaliser lines for the same key to pick it up, 
    	// meaning that incorrect values can get propagated through.
        this.replicas = replicas;
        iAmBlank = false;
        if (debug) {System.out.println("[Totaliser]: Replicas is " + replicas);};
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
		this.totaliseTrigger = totaliseTrigger;
		// the purpose of this is to force the insert of the data to 
		// working memory and whatever else we might need to do on
		// each line
    
		serviceProviderName = parentPricing.getServiceProvider();
		regionName = parentPricing.getRegionName();
		planType = parentPricing.getPaymentPlan();
		featureType = parentPricing.getFeatureSet();
		subfeatureType = parentPricing.getSubfeature();
//		replicas = grandparentUploadCollection.getMinCopies();
		if ( parentPricing.getReplicas() == "not relevant") {
			replicas = 0;
		} else {
			if ( parentPricing.getReplicas() == "") {
				replicas = 0;
			} else {
				replicas = Integer.valueOf(parentPricing.getReplicas());
			}
		};
		if ( parentPricing.getStorageUsed() .equals(null) ) {
			storageUsed = 0.0;
		} else {
			storageUsed = parentPricing.getStorageUsed();
		};
		if ( parentPricing.getRequestsUsed() .equals(null) ) {
			requestsUsed = 0.0;
		} else {
			requestsUsed = parentPricing.getRequestsUsed();
		};
		if ( parentPricing.getTransfersUsed() .equals(null) ) {
			transfersUsed = 0.0;
		} else {
			transfersUsed = parentPricing.getTransfersUsed();
		};
		if ( parentPricing.getServicebusUsed() .equals(null) ) {
			servicebusUsed = 0.0;
		} else {
			servicebusUsed = parentPricing.getServicebusUsed();
		};
		if ( parentPricing.getStoragetransactionsUsed() .equals(null) ) {
			storagetransactionsUsed = 0.0;
		} else {
			storagetransactionsUsed = parentPricing.getStoragetransactionsUsed();
		};
		// not used so dummy it
		flags = " ";
	
		if (serviceProviderName == "dummy") {
			iAmBlank = true;
		}	
		if (parentPricing.getIAmBlank()) {
			iAmBlank = true;
		}
		if (checkDuplicateFacts(greatgrandparentCostOpt.getDuplicateTotaliserHashMap())) {
			if (debug) {System.out.println("[Totaliser]: skipping due to duplicate key found");};
			iAmBlank = true;			
		};
		// if i am blank or duplicate do not try to insert me
		if (debug) {System.out.println("[Totaliser]: before insert totaliser at totaliser");};
		if ( ! iAmBlank ) {
			if (debug) {System.out.println("[Totaliser]: +++ Inserting Totaliser at totaliser +++");};
			if (debug) {System.out.println("[Totaliser]: Keys are :-");};
			if (debug) {System.out.println("[Totaliser]: serviceProviderName: " + serviceProviderName);};
			if (debug) {System.out.println("[Totaliser]: regionName: " + regionName);};
			if (debug) {System.out.println("[Totaliser]: planType: " + planType);};
			if (debug) {System.out.println("[Totaliser]: featureType: " + featureType);};
			if (debug) {System.out.println("[Totaliser]: subfeatureType: " + subfeatureType);};
			if (debug) {System.out.println("[Totaliser]: replicas: " + replicas);};
			if (debug) {System.out.println("[Totaliser]: flags: " + flags);};
			if (debug) {System.out.println("[Totaliser]: storageUsed: " + storageUsed);};
			if (debug) {System.out.println("[Totaliser]: requestsUsed: " + requestsUsed);};
			if (debug) {System.out.println("[Totaliser]: transfersUsed: " + transfersUsed);};
			if (debug) {System.out.println("[Totaliser]: servicebusUsed: " + servicebusUsed);};
			if (debug) {System.out.println("[Totaliser]: storagetransactionsUsed: " + storagetransactionsUsed);};
			if (debug) {System.out.println("[Totaliser]: ");};
			
			ourKsession.insert(this);
			if (debug) {System.out.println("[Totaliser]: Inserted");};
		};
				    	
    	Totaliser totaliserItem = new Totaliser();
    	// tell the Pricing that I am current
        parentPricing.setMyTotaliser(totaliserItem);
        
        // clear down the replicas count because it can propagate the wrong answer
        // due to a timing problem
        setReplicas(0);
        
        totaliserItem.grandparentUploadCollection = this.grandparentUploadCollection;
        totaliserItem.greatgrandparentCostOpt = this.grandparentUploadCollection.getParentCostOpt();
//        totaliserItem.greatgrandparentCostOpt = this.greatgrandparentCostOpt;
	}
	
	/**
	 * @return the ourKsession
	 */
	public StatefulKnowledgeSession getOurKsession() {
		return ourKsession;
	}

	/**
	 * @param ourKsession the ourKsession to set
	 */
	public void setOurKsession(StatefulKnowledgeSession ourKsession) {
		this.ourKsession = ourKsession;
	}


	/**
	 * @return the grandparentUploadCollection
	 */
	public UploadCollection getOurUploadCollection() {
		return grandparentUploadCollection;
	}


	/**
	 * @param grandparentUploadCollection the grandparentUploadCollection to set
	 */
	public void setGrandparentUploadCollection(UploadCollection grandparentUploadCollection) {
		this.grandparentUploadCollection = grandparentUploadCollection;
	}


	/**
	 * @return the parentPricing
	 */
	public Pricing getParentPricing() {
		return parentPricing;
	}


	/**
	 * @param parentPricing the parentPricing to set
	 */
	public void setParentPricing(Pricing parentPricing) {
		this.parentPricing = parentPricing;
	}


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

	private boolean checkDuplicateFacts(HashMap<String,String>  totaliserData) {
		if (debug) {System.out.println("[Totaliser]: Hashmap key is :-" + serviceProviderName + "|" + regionName + "|" + planType + "|" + featureType + "|" + subfeatureType + "|" + flags);};
		String keyVal = serviceProviderName + "|" + regionName + "|" + planType + "|" + featureType + "|" + subfeatureType + "|" + flags;
//		String keyVal = serviceProviderName + "|" + regionName + "|" + planType + "|" + featureType + "|" + subfeatureType + "|" + String.valueOf(replicas) + "|" + String.valueOf(storageUsed) + "|" + String.valueOf(requestsUsed) + "|" + String.valueOf(transfersUsed) + "|" + String.valueOf(servicebusUsed) + "|" + String.valueOf(storagetransactionsUsed) + "|" + flags;
		if (totaliserData.containsKey(keyVal)) { return true; };
		totaliserData.put(keyVal, "Fact inserted");
		return false;
	}

	private boolean addLineItem(HashMap<String,String> totaliserData, String keyText, Double valNumber) {
		String keyVal = serviceProviderName + "|" + regionName + "|" + planType + "|" + featureType + "|" + subfeatureType + "|" + String.valueOf(replicas) + "|" + flags + "|" + keyText;
		if (debug) {System.out.println("[Totaliser]: keyVal is " + keyVal);};
		Double calcNewVal = 0.0;
		String valText = "";
		if ( ! totaliserData.containsKey(keyVal)) { 
			if (debug) {System.out.println("[Totaliser]: Inserting rollupTotaliserHashMap");};
			valText = String.valueOf(valNumber);
			if (debug) {System.out.println("[Totaliser]: valText is " + valText);};
			totaliserData.put(keyVal, valText);			
		} else {
			if (debug) {System.out.println("[Totaliser]: Updating rollupTotaliserHashMap");};
			valText = totaliserData.get(keyVal);
			calcNewVal = valNumber + Double.valueOf(valText);
			valText = String.valueOf(calcNewVal);
			if (debug) {System.out.println("[Totaliser]: valNumber is " + valNumber);};
			if (debug) {System.out.println("[Totaliser]: valText is " + valText);};
			totaliserData.put(keyVal, valText);			
		};
		return true;
	}

	/**
	 * @return the greatgrandparentCostOpt
	 */
	public CostOptimiser getGreatgrandparentCostOpt() {
		return greatgrandparentCostOpt;
	}

	/**
	 * @param greatgrandparentCostOpt the greatgrandparentCostOpt to set
	 */
	public void setGreatgrandparentCostOpt(CostOptimiser greatgrandparentCostOpt) {
		this.greatgrandparentCostOpt = greatgrandparentCostOpt;
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
        if (debug) {System.out.println( "storageUsed is : " + storageUsed);};
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
        if (debug) {System.out.println( "requestsUsed is : " + requestsUsed);};
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
        if (debug) {System.out.println( "transfersUsed is : " + transfersUsed);};
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
        if (debug) {System.out.println( "servicebusUsed is : " + servicebusUsed);};
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
        if (debug) {System.out.println( "storagetransactionsUsed is : " + storagetransactionsUsed);};
	}

}


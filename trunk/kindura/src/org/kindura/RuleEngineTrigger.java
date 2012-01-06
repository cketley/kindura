package org.kindura;

import java.util.Map;

import com.yourmediashelf.fedora.client.FedoraClient;

/**
 * This class is used to pass the metadata to the rule engine for filtering Cloud providers.
 * @author Jun Zhang
 */
public class RuleEngineTrigger {
	FedoraServiceManager fedoraServiceManager;
	//ConfigurationFileParser cfp;
	//FedoraClient fedoraClient;

	public RuleEngineTrigger() {
		fedoraServiceManager = new FedoraServiceManager();
		//cfp = new ConfigurationFileParser();
		//fedoraClient = fedoraServiceManager.getFedoraConnection(cfp.getKinduraParameters().get("FedoraHost"), cfp.getKinduraParameters().get("FedoraUsername"), cfp.getKinduraParameters().get("FedoraPassword"));
	}
	/*
	 * Check if the data collection exists. 
	 */
	public boolean isDataCollectionExisted(String pid) {
		if (fedoraServiceManager.getObjectPIDs(pid, "pid") != null) {
			System.out.println("Data collection "+pid+" exists");
			return true;
		}
		return false;
	}
	
	/*
	 * Trigger the rule engine if the cloud provider has not been chosen for the data collection. 
	 */
	public String[] triggerRuleEngine(Map<String, String> inputMetadata, String pid) {
		String[] suitableCloudProviders = new String[10];
		if (isDataCollectionExisted(pid) == false) {
			//Trigger the rule engine.
			System.out.println("Trigger the rule engine");
			//
		}
		//If the cloud provider has been chosen the data collection, get the cloud providers' information.
		else {
			System.out.println("Cloud provider: "+fedoraServiceManager.getADataStream(pid, "cloudprovider"));
			suitableCloudProviders[0] = fedoraServiceManager.getADataStream(pid, "cloudprovider");
		}
		return suitableCloudProviders;
	}
}

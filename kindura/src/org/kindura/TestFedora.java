package org.kindura;

import java.net.MalformedURLException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileUtils;
import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import com.yourmediashelf.fedora.client.request.Ingest;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import com.yourmediashelf.fedora.client.response.IngestResponse;

public class TestFedora {
	public static void main(String[] args) {
		FedoraCredentials credentials;
		FedoraClient fedora;
		try {
			credentials = new FedoraCredentials("http://localhost:8080/fedora", "fedoraAdmin", "fedoraAdmin");
			fedora = new FedoraClient(credentials);
			//IngestResponse response = new Ingest("test:parent").label("parent").execute(fedora);
			//response = new Ingest("test:child1").label("child1").execute(fedora);
			//response = new Ingest("test:child2").label("child2").execute(fedora);
			
			FedoraResponse fedoraResponse = null;
			
			fedoraResponse = FedoraClient.addRelationship("test:child1").subject("test:child1").predicate("test:child1/isAChildOf").object("test:parent").execute(fedora);
			fedoraResponse = FedoraClient.addRelationship("test:child2").subject("test:child2").predicate("test:child2/isAChildOf").object("test:parent").execute(fedora);
			
			fedoraResponse = FedoraClient.addRelationship("test:parent").subject("test:parent").predicate("test:parent/isParentOf").object("test:child1").execute(fedora);
			fedoraResponse = FedoraClient.addRelationship("test:parent").subject("test:parent").predicate("test:parent/isParentOf").object("test:child2").execute(fedora);
			
			fedoraResponse = FedoraClient.getRelationships("test:parent").subject("test:parent").predicate("test:parent/isParentOf").execute(fedora);
			Model model = ModelFactory.createDefaultModel();
			model.read(fedoraResponse.getEntityInputStream(), null, FileUtils.langXML);
			StmtIterator it = model.listStatements();
			Statement s;
			/*while (it.hasNext()) {
				s = (Statement) it.next();
				System.out.println(s.getSubject().toString());
				System.out.println(s.getPredicate().toString());
				System.out.println(s.getObject().toString());
				System.out.println();
			}*/
			
			fedoraResponse = FedoraClient.getRelationships("test:child1").subject("test:child1").predicate("test:child1/isAChildOf").execute(fedora);
			
			model.read(fedoraResponse.getEntityInputStream(), null, FileUtils.langXML);
			it = model.listStatements();
			while (it.hasNext()) {
				s = (Statement) it.next();
				System.out.println(s.getSubject().toString());
				System.out.println(s.getPredicate().toString());
				System.out.println(s.getObject().toString());
				System.out.println();
			}
			
			/*fedoraResponse = FedoraClient.getRelationships("test:child2").subject("test:child2").predicate("test:child2/isAChildOf").execute(fedora);
			
			model.read(fedoraResponse.getEntityInputStream(), null, FileUtils.langXML);
			it = model.listStatements();
			while (it.hasNext()) {
				s = (Statement) it.next();
				System.out.println(s.getSubject().toString());
				System.out.println(s.getPredicate().toString());
				System.out.println(s.getObject().toString());
			}*/
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FedoraClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

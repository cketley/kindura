package org.kindura.rules;

import org.kindura.rules.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class runTest
{
		public static void main( String args[] )
		{
				StorageProvider provider1 = new StorageProvider( "/home/rd73/Work/Kindura/RuleEngine/uk/ac/kcl/kindura/Amazon-modified.xml" );
				StorageProvider provider2 = new StorageProvider( "/home/rd73/Work/Kindura/RuleEngine/uk/ac/kcl/kindura/Azure-modified.xml" );

				StorageProvider[] providers = new StorageProvider[ 2 ];
				providers[0] = provider1;
				providers[1] = provider2;

				Map<String,String>  metadata = new HashMap<String,String>();
				metadata.put( "accessrequirement", "Daily" );
				metadata.put( "collectionname", "Test collection" );
				metadata.put( "collectiontype", "Research" );
				metadata.put( "collectionversion", "First version" );
				metadata.put( "project", "Kindura" );
				metadata.put( "researchfunder", "AHRC" );
				metadata.put( "proectivemarking", "Medical" );
				metadata.put( "filemodificationdate0","06/07/2011 12:23:38" );
				UploadCollection thisCollection = new UploadCollection( metadata );
				for ( StorageProvider p: providers )
				{
					thisCollection.addStorage( p );
					System.out.println("Added storage " + p.name );
				}

				try
				{
						testEngine t = new testEngine();
						t.assignTests( thisCollection );
						System.out.println("Collection details....:");
						System.out.println("Appraisal: " + thisCollection.getAppraisalDateAsString() );
					    System.out.println("Copies: " + thisCollection.getMinCopies() );
						System.out.println("Storage tiers: ");
						for ( StorageProviderTier resultingTier: thisCollection.getTiers() )
						{
							System.out.println("\t" + resultingTier);
						}	
						System.out.println("New provider list:");
						for ( StorageProvider p : thisCollection.getProviders() )
						{
							System.out.println( "Name: " + p.name );
							for ( StorageProviderConstraint c: p.getConstraints() )
							{
								System.out.println("Constraint: " + c.getType() + " = " + c.getValue() );
							}
							System.out.println("");
						}
				} catch ( Exception e )
				{
						System.err.println("Caught error " + e.toString() );
						e.printStackTrace();
						System.exit(0);
				}
		}
}

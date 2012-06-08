package org.kindura;

import org.drools.RuleBase;
import org.drools.compiler.*;
import org.drools.event.*;
import org.drools.WorkingMemory;
import org.drools.RuleBaseFactory;
import org.drools.rule.Package;
import org.kindura.WorkingEnvironmentCallback;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.Reader;

public class RulesEngine {
   private RuleBase rules;
   private boolean debug = false;

   public RulesEngine(String rulesFile) throws Exception {
      super();
	  // Use package builder to build up a rule package
	  PackageBuilder builder = new PackageBuilder();
      try
      {
			  // Read in the rules source file
			  Reader source = new FileReader( rulesFile );
			  System.out.println("Got rules file: " + rulesFile );
			  // This parses and compiles in one step
			  builder.addPackageFromDrl(source);
      } catch ( Exception e )
      {
            System.err.println("Caught exception " + e.toString() );
			e.printStackTrace();
      }
	  // Get the compiled package
	  PackageBuilderErrors errors = builder.getErrors();
	  System.out.println( errors.toString() );
	  Package pkg = builder.getPackage();
	  // Add the package to a rulebase (deploy the rule package).
	  rules = RuleBaseFactory.newRuleBase();
	  rules.addPackage(pkg);

   }
   public void executeRules(WorkingEnvironmentCallback callback) {
		   WorkingMemory workingMemory = rules.newStatefulSession();
		   if (debug) {
				   workingMemory
						   .addEventListener(new DebugWorkingMemoryEventListener());
		   }
		   callback.initEnvironment(workingMemory);
//	TODO i'm convinced this shouldn't be here
		   // workingMemory.fireAllRules();
   }
}

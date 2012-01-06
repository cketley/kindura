package org.kindura.rules;

import org.kindura.rules.*;
import java.util.ArrayList;
import java.util.Calendar;
import org.drools.WorkingMemory;

public class testEngine {

   private RulesEngine rulesEngine;

   public testEngine() throws Exception {
      super();
      rulesEngine = new RulesEngine("/home/rd73/Work/Kindura/RuleEngine/uk/ac/kcl/kindura/KinduraRules.drl");
//      rulesEngine = new RulesEngine("/home/rd73/Work/Kindura/RuleEngine/uk/ac/kcl/kindura/test.drl");
   }

   public void assignTests( final UploadCollection collection ) {
      rulesEngine.executeRules( new WorkingEnvironmentCallback() 
		{
			public void initEnvironment( WorkingMemory workingMemory )
			{
//				workingMemory.setGlobal( "providers", providers );
				workingMemory.insert( collection );
			};
		}
	);

   }

}

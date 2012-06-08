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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
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
import org.drools.compiler.DecisionTableFactory;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
//import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;


/**
 * This represents the business arrangements for the storage consumed by us
 */
public class ShowDRL {
	public static void main( String args[] )
	{
        ShowDRL launcher = new ShowDRL();
        launcher.runKink();
    }
    
    public  Integer runKink() {

    	Integer status = 0;

	
StatefulKnowledgeSession myKsession;



    	System.out.println( "before new session");
    	
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        myKsession = kbase.newStatefulKnowledgeSession(); 


        System.out.println( "before prep of knowledge builder" );


        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        
        DecisionTableConfiguration dtconf =KnowledgeBuilderFactory
        .newDecisionTableConfiguration();
        dtconf.setInputType( DecisionTableInputType.XLS );

        try {
			String drlString = DecisionTableFactory
			.loadFromInputStream(ResourceFactory
			.newClassPathResource("org/kindura/KinduraServiceMatrix_V0.9.2.xls")
			.getInputStream(), dtconf);
			System.out.println( "drl rules are " + drlString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
   
    return 1; 
    }

}




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
import java.util.ArrayList;
import java.util.Calendar;
import org.drools.WorkingMemory;

public class PrepKbase {

	
   private RulesEngine rulesEngine;

   public PrepKbase() throws Exception {
	   super();
	   String drlPath = "C:\\Documents and Settings\\ycn94546\\My Documents\\Project_work\\kindura\\kindness\\src\\org\\kindura\\";
	   rulesEngine = new RulesEngine(drlPath + "KinduraRules.drl");
   }

   public void prepEnvironment( final UploadCollection collection ) {
	   rulesEngine.executeRules( new WorkingEnvironmentCallback() 
	   {
		   public void initEnvironment( WorkingMemory workingMemory )
		   {
			   //workingMemory.setGlobal( "providers", providers );
			   workingMemory.insert( collection );
		   };
	   }
			   );

   }

}

package org.kindura.rules;

import org.drools.WorkingMemory;
import org.drools.FactException;

public interface WorkingEnvironmentCallback {
   void initEnvironment(WorkingMemory workingMemory) throws FactException;
}
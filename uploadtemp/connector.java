import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.solr.update.DocumentBuilder;
import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;
import org.ow2.bonita.definition.activity.Task;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.AccessorUtil;


public class AuthenticateUser extends ProcessConnector {
	
	private static final String MYSQL_USERNAME = "root";
    private static final String MYSQL_PASSWORD = "globaltime";
    private static final String MYSQL_DATABASE = "kindura_users";
	    
	@Override
	protected void executeConnector() throws Exception {
		// TODO Auto-generated method stub
		authenticateUsernamePassword();
	}

	@Override
	protected List<ConnectorError> validateValues() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String authenticateUsernamePassword () {
    	APIAccessor a = AccessorUtil.getAPIAccessor();
		QueryRuntimeAPI q = a.getQueryRuntimeAPI();
		
		ActivityInstanceUUID instanceUUID;
	    String whatAction;
	    Object currentActivity;
       
	    RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
	    QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
	    
	    Set<ProcessInstance> processInstances =
	        queryRuntimeAPI.getProcessInstances();
	    
	    Iterator myIterator = processInstances.iterator();
	    
	    //Iterator<TaskInstance> myIterator =
	      //queryRuntimeAPI.getTaskList(ActivityState.READY).iterator();
	     
	    while (myIterator.hasNext()){

	      currentActivity = myIterator.next();
	      instanceUUID = currentActivity.getUUID();
	      try {
				Object user = q.getVariable(instanceUUID,"username");
				String usernameInString = (String)user;
				writeToFile(usernameInString);
			} catch (ActivityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (VariableNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		
		//ActivityInstance ai = q.get
			//q.getActivityInstances("MyProcessDiagram4");
		
		
		
		//ActivityInstanceUUID auuid = q.getActivityInstance();
		
		//Object o = q.getVariable(activityUUID, "myInteger");
		//Long l= (Long)o;
		//long number = l.longValue();
		
		
		
		/*DatabaseConnector.openDatabase(MYSQL_USERNAME, MYSQL_PASSWORD, MYSQL_DATABASE);
		String returnedusername = DatabaseConnector.queryUser(username, password);
		DatabaseConnector.closeDatabase();
		if (returnedusername != null) {
			System.out.println("User "+username+" is authenticated");
			return username;
		}
		else {
			System.out.println("User "+username+" is NOT authenticated");
			return null;
		}*/
		
		return null;
    }
	
	public void writeToFile(String username) {
	    FileWriter writer;
	    try {
	      writer = new FileWriter("c:/myFile.txt");
	      writer.write(username);
	      writer.close();
	    } catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }

	  }
}

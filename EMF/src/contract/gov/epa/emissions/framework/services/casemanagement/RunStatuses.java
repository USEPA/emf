package gov.epa.emissions.framework.services.casemanagement;

import java.util.ArrayList;
import java.util.List;

public class RunStatuses {
    
    private static final String [] statusList;
    
    static
    {
        List<String> list = new ArrayList<String>();

        list.add("Not Started");
        list.add("Running");
        list.add("Failed");
        list.add("Complete");
        
        statusList = new String[4];
        list.toArray(statusList);
    }
        
    public static String[] all() {
        return statusList;
    }
}

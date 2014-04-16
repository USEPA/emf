package gov.epa.emissions.commons.db.version;

import java.util.HashMap;

public class SortOrderUtility {

    HashMap sortOrderMap = new HashMap();
    
    public SortOrderUtility() {
        super();
        setUpSortOrdersForTables();
    }

    private void setUpSortOrdersForTables() {

        sortOrderMap.put("Temporal Cross-Reference","SCC, Monthly_Code, Weekly_Code, Diurnal_Code");
        sortOrderMap.put("Monthly - Temporal Profile", "Code");
        sortOrderMap.put("Weekly - Temporal Profile","Code");
        sortOrderMap.put("Diurnal - Temporal Profile","Code");

        sortOrderMap.put("ORL Point","FIPS, SCC, PLANTID, POINTID");
        sortOrderMap.put("ORL NonPoint","FIPS, SCC");
        sortOrderMap.put("ORL NonRoad","FIPS, SCC");
        sortOrderMap.put("ORL OnRoad","FIPS, SCC");
        
    }

    public String getSortOrderForTableType(String tableType){
        return (String)sortOrderMap.get(tableType);
    }
}

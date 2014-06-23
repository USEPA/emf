package gov.epa.emissions.framework.services.qa;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Enumeration;


public class QAVersionedQuery {
    
    //Added class to handle the version parsing for multiple versioned datasets.
    
    String finalAliasString;
    
    public String query(Hashtable<String, String []> tableValuesAliasesVersions){
        finalAliasString = "";
        
        // Create String arrays to hold up to the maximum number of possible String arrays
        // in the hashtable
        
        Enumeration<String> hashKeys = tableValuesAliasesVersions.keys();
        
        //String values;
        //String aliases;
        //String versions;
        //String dataSetId;
        //String versionsPath;
        int i = 0;
        while (hashKeys.hasMoreElements()) {
          
            // Populate the String arrays from the hashtable if they exist
       
            String key = hashKeys.nextElement();
            String [] valuesAliasesVersions = tableValuesAliasesVersions.get(key);
            
            //System.out.println("The key value pair is " + valuesAliasesVersions);
            
            //String values = valuesAliasesVersions[0];
            //System.out.println(values);
            String aliases = valuesAliasesVersions[1];
            String versions = valuesAliasesVersions[2];
            String dataSetId = valuesAliasesVersions[3];
            String versionsPath = valuesAliasesVersions[4];
            
            String deleteClause = createDeleteClause(versionsPath,aliases);
            String aliasString="";
            if (aliases.length() > 0)
            aliasString = aliases + ".";
        
            // TBD: If dataset type does not have multiple datasets in a table, don't need datasetIdClause
        
            // This is the only way I could see to add the AND's in the correct place as needed.  Modified to create
            // different finalAliasString when the version of the last dataset is 0, because without that the query has an extra AND
            
            //if (i == tableValuesAliasesVersions.size() - 1 ) {
            if (i == tableValuesAliasesVersions.size() - 1 && Integer.parseInt(versions) != 0){
                finalAliasString += " ( (" + aliasString + "version is null) OR ( " + aliasString + "version IN (" + versionsPath + ")" + deleteClause + " AND " + aliasString +datasetIdClause(dataSetId ) + ") ) "; 
                //finalAliasString += " (" + aliasString + "version IN (" + versionsPath + ")" + deleteClause + " AND " + aliasString +datasetIdClause(dataSetId ) + ") ";  
                }
            else if (i == tableValuesAliasesVersions.size() - 1 && Integer.parseInt(versions) == 0) {
                finalAliasString += " ( (" + aliasString + "version is null) OR ( " + aliasString + "version IN (" + versionsPath + ")" + deleteClause + aliasString + datasetIdClause(dataSetId ) + ") ) ";
                //finalAliasString += " (" + aliasString + "version IN (" + versionsPath + ")" + deleteClause + aliasString + datasetIdClause(dataSetId ) + ") ";
            }
            else{
                finalAliasString += " ( (" + aliasString + "version is null) OR ( " + aliasString + "version IN (" + versionsPath + ")" + deleteClause + aliasString + datasetIdClause(dataSetId ) + ") ) AND "; 
                //finalAliasString += " (" + aliasString + "version IN (" + versionsPath + ")" + deleteClause + aliasString + datasetIdClause(dataSetId ) + ") AND ";             }
               }
                i++;
            //System.out.println("The string is " + finalAliasString);
        }
        return finalAliasString;
     }

    private String datasetIdClause(String datasetID) {
       
        return "dataset_id=" + datasetID;
        
    }

    private String createDeleteClause(String versions, String alias) {
        // Added aliasstring
        String aliasString="";
        if (alias.length() > 0)
            aliasString = alias + ".";
        StringBuffer buffer = new StringBuffer();
        buffer.append(" AND ");

        StringTokenizer tokenizer = new StringTokenizer(versions, ",");
        
        // e.g.: delete_version NOT SIMILAR TO '(6|6,%|%,6,%|%,6)'
        while (tokenizer.hasMoreTokens()) {
            String version = tokenizer.nextToken();
            if (!version.equals("0"))  // don't need to check to see if items are deleted from version 0
            {
                String regex = "(" + version + "|" + version + ",%|%," + version + ",%|%," + version + ")";
                //buffer.append(" AND " + aliasString + "delete_versions NOT SIMILAR TO '" + regex + "'");
                buffer.append( aliasString + "delete_versions NOT SIMILAR TO '" + regex + "'");

                if (tokenizer.hasMoreTokens())
                    buffer.append(" AND ");
            }
        }
        return buffer.toString();
    }

}

package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class SQLMultiInvSumProgramQuery extends SQLQAProgramQuery{
    
    private ArrayList<String> allDatasetNames = new ArrayList<String>();
    
    public SQLMultiInvSumProgramQuery(HibernateSessionFactory sessionFactory, String emissioDatasourceName, String tableName, QAStep qaStep) {
        super(sessionFactory,emissioDatasourceName,tableName,qaStep);
    }
        
    public String createInvSumProgramQuery() throws EmfException {
        
        String programArguments = qaStep.getProgramArguments();
        
        //get applicable tables from the program arguments
        String inventoriesToken = "";
        String invtableToken = "";
        String summaryTypeToken = "State+SCC";
        String invTableDatasetName = "";
        
        int index1 = programArguments.indexOf(QAStep.invTableTag);
        int index2 = programArguments.indexOf(QAStep.summaryTypeTag);
        inventoriesToken = programArguments.substring(0, index1).trim();
        invtableToken = programArguments.substring(index1 + QAStep.invTableTag.length(), index2 == -1 ? programArguments.length() : index2);
        
        if (index2 != -1) {
            summaryTypeToken = programArguments.substring(index2 + QAStep.summaryTypeTag.length()).trim();
        } 
        //default just in case...
        if (summaryTypeToken.trim().length() == 0)
            summaryTypeToken = "State+SCC";

         //parse inventories names...
        if (inventoriesToken.length() > 0) {
            StringTokenizer tokenizer2 = new StringTokenizer(inventoriesToken, "\n");
            tokenizer2.nextToken();
            while (tokenizer2.hasMoreTokens()) {
                String datasetName = tokenizer2.nextToken().trim();
                if (datasetName.length() > 0){
                    datasetNames.add(datasetName);
                    allDatasetNames.add(datasetName);
                }
            }
        } else {
            //see if there are tables to build the query with, if not throw an exception
            throw new EmfException("There are no datasets specified.");
        }

         //parse inventory table name...
         StringTokenizer tokenizer3 = new StringTokenizer(invtableToken, "\n");
         while (tokenizer3.hasMoreTokens()) {
             invTableDatasetName  = tokenizer3.nextToken().trim();
             datasetNames.add(invTableDatasetName);
             if (invTableDatasetName.length() > 0) hasInvTableDataset = true;
         }
         
         checkDataset();
         
        //Create the query template and add placeholders (#, @!@, @@@, ...) for sql to be inserted in a later steps
         String outerQuery = "select @!@, " 
             + "te.poll, "
             + "coalesce(" + (hasInvTableDataset ? "i.name" : "p.pollutant_code_desc") + ", 'AN UNSPECIFIED DESCRIPTION') as poll_desc, "
             + "sum(coalesce(" + (hasInvTableDataset ? "cast(i.factor as double precision) * ann_emis" : "null") + ", ann_emis)) as ann_emis, "
             + "sum(coalesce(" + (hasInvTableDataset ? "cast(i.factor as double precision) * avd_emis" : "null") + ", avd_emis)) as avd_emis "
             + "\nfrom (#) as te " 
             + (hasInvTableDataset ? "\nleft outer join\n $DATASET_TABLE[\"" + invTableDatasetName + "\", 1] i \non te.poll = i.cas " : "\nleft outer join reference.pollutant_codes p \non te.poll = p.pollutant_code ") 
             + " \ngroup by @@@, te.poll, coalesce(" + (hasInvTableDataset ? "i.name" : "p.pollutant_code_desc") + ", 'AN UNSPECIFIED DESCRIPTION')"
             + " \norder by @@@, te.poll, coalesce(" + (hasInvTableDataset ? "i.name" : "p.pollutant_code_desc") + ", 'AN UNSPECIFIED DESCRIPTION')";
         //, i.name, sum(cast(i.factor as double precision) * mo_emis)
         outerQuery = query(outerQuery, true);

        //build inner sql statement with the datasets specified, make sure and unionize (append) the tables together
        String innerSQL = "";
        for (int j = 0; j < allDatasetNames.size(); j++) {
            innerSQL += (j > 0 ? " \nunion all " : "") + createDatasetQuery(allDatasetNames.get(j).toString().trim());
        }

        //replace # symbol with the unionized fire datasets query
        outerQuery = outerQuery.replaceAll(QAStep.poundQueryTag, innerSQL);

        //replace @!@ symbol with main columns in outer select statement
        String sql = "";
        if (summaryTypeToken.equals("State+SCC")) 
            sql = "te.fipsst, te.scc";
        else if (summaryTypeToken.equals("State")) 
            sql = "te.fipsst";
        else if (summaryTypeToken.equals("County")) 
            sql = "te.fips";
        else if (summaryTypeToken.equals("County+SCC")) 
            sql = "te.fips, te.scc";
        else if (summaryTypeToken.equals("SCC")) 
            sql = "te.scc";
        outerQuery = outerQuery.replaceAll("@!@", sql);

        //replace !@! symbol with main columns in inner select statement
        if (summaryTypeToken.equals("State+SCC")) 
            sql = "substr(fips, 1, 2) as fipsst, scc";
        else if (summaryTypeToken.equals("State")) 
            sql = "substr(fips, 1, 2) as fipsst";
        else if (summaryTypeToken.equals("County")) 
            sql = "fips";
        else if (summaryTypeToken.equals("County+SCC")) 
            sql = "fips, scc";
        else if (summaryTypeToken.equals("SCC")) 
            sql = "scc";
        outerQuery = outerQuery.replaceAll("!@!", sql);
        
        //replace @@@ symbol with group by columns in outer select statement
        if (summaryTypeToken.equals("State+SCC")) 
            sql = "te.fipsst, te.scc";
        else if (summaryTypeToken.equals("State")) 
            sql = "te.fipsst";
        else if (summaryTypeToken.equals("County")) 
            sql = "te.fips";
        else if (summaryTypeToken.equals("County+SCC")) 
            sql = "te.fips, te.scc";
        else if (summaryTypeToken.equals("SCC")) 
            sql = "te.scc";
        outerQuery = outerQuery.replaceAll("@@@", sql);
        
        //replace !!! symbol with group by columns in inner select statement
        if (summaryTypeToken.equals("State+SCC")) 
            sql = "substr(fips, 1, 2), scc";
        else if (summaryTypeToken.equals("State")) 
            sql = "substr(fips, 1, 2)";
        else if (summaryTypeToken.equals("County")) 
            sql = "fips";
        else if (summaryTypeToken.equals("County+SCC")) 
            sql = "fips, scc";
        else if (summaryTypeToken.equals("SCC")) 
            sql = "scc";
        outerQuery = outerQuery.replaceAll("!!!", sql);

        //return the built query
        return outerQuery;
    }

        private String createDatasetQuery(String datasetName) throws EmfException {

           String sql = "";
           
           sql = "\nselect !@!, trim(poll) as poll, sum(ann_emis) as ann_emis, sum(avd_emis) as avd_emis  \nfrom $DATASET_TABLE[\"" + 
               datasetName + "\", 1] m \ngroup by !!!, trim(poll) ";

           sql = query(sql, false);

           return sql;
        }

        private String query(String partialQuery, boolean createClause) throws EmfException {
            
            SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissionDatasourceName, tableName );
            return parser.parse(partialQuery, createClause);
        }
        
}

package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class SQLNInvDiffProgramQuery extends SQLQAProgramQuery{
    
    ArrayList<String> allDatasetNames = new ArrayList<String>();
    
    public SQLNInvDiffProgramQuery(HibernateSessionFactory sessionFactory, String emissioDatasourceName, String tableName, QAStep qaStep) {
        super(sessionFactory,emissioDatasourceName,tableName,qaStep);
    }

    public String createInvDiffProgramQuery() throws EmfException {
        
        String programArguments = qaStep.getProgramArguments();
        
        //get applicable tables from the program arguments
        String invBaseToken = "";
        String invEmisToken = "ann";
        String invtableToken = "";
        String summaryTypeToken = "State+SCC";
        String invTableDatasetName = "";
        String aliasedPollList = "";
        String aliasedFipsList = "";
        String aliasedFipsStList = "";
        String aliasedSCCList = "";
        String sqlAnnEmisList = "";
        
//        int indexBase = programArguments.indexOf(invTag);
        int indexInvTable = programArguments.indexOf(QAStep.invTableTag);
        int emisIndex = programArguments.indexOf(QAStep.emissionTypeTag);
        int indexSumType = programArguments.indexOf(QAStep.summaryTypeTag);
       
        if (indexInvTable != -1 && emisIndex !=-1){
            invBaseToken = programArguments.substring(0, indexInvTable).trim();
            invtableToken = programArguments.substring(indexInvTable+ QAStep.invTableTag.length(), emisIndex);
            invEmisToken = programArguments.substring(emisIndex + QAStep.emissionTypeTag.length(), indexSumType == -1 ? programArguments.length() : indexSumType);
        }
        if (indexSumType != -1) {
            summaryTypeToken = programArguments.substring(indexSumType + QAStep.summaryTypeTag.length()).trim();
        } 
        //default just in case...
         
        if (invEmisToken.trim().startsWith("Average"))
            invEmisToken = "avd";
        if (invEmisToken.trim().startsWith("Annual"))
            invEmisToken = "ann";
        
        if (summaryTypeToken.trim().length() == 0)
            summaryTypeToken = "State+SCC";

         //parse inventories names for base and compare...
        if (invBaseToken.length() > 0 ) {
            StringTokenizer tokenizer2 = new StringTokenizer(invBaseToken, "\n");
            tokenizer2.nextToken();  // skip -inventoy tag
            while (tokenizer2.hasMoreTokens()) {
                String datasetName = tokenizer2.nextToken().trim();
                if (datasetName.length() > 0){
                    allDatasetNames.add(datasetName);
                    datasetNames.add(datasetName);
                }
            }
        } else {
            //see if there are tables to build the query with, if not throw an exception
            throw new EmfException("There are no ORL Inventory datasets specified (base).");
        }
        
         //parse inventory table name...
         StringTokenizer tokenizer3 = new StringTokenizer(invtableToken,"\n");
         while (tokenizer3.hasMoreTokens()) {
             invTableDatasetName  = tokenizer3.nextToken().trim();
             if (invTableDatasetName.length() > 0) {
                 hasInvTableDataset = true;
                 datasetNames.add(invTableDatasetName);
             }
         }
         
         checkDataset();

         //build aliased comma delimited list for use in SELECT and GROUP BY clause
         for (int j = 0; j < allDatasetNames.size(); j++) {
             aliasedPollList += (j > 0 ? "," : "") + "t" + j + ".poll";
             aliasedFipsList += (j > 0 ? "," : "") + "t" + j + ".fips";
             aliasedFipsStList += (j > 0 ? "," : "") + "t" + j + ".fipsst";
             aliasedSCCList += (j > 0 ? "," : "") + "t" + j + ".scc";
             String datasetName = allDatasetNames.get(j).toString().trim();
             //make sure dataset name only includes SQL friendly characters...
             for (int i = 0; i < datasetName.length(); i++) {
                 if (!Character.isLetterOrDigit(datasetName.charAt(i))) {
                     datasetName = datasetName.replace(datasetName.charAt(i), '_');
                 }
             }
             datasetName = datasetName.replaceAll(" ", "_");
             sqlAnnEmisList += (j > 0 ? "," : "") + "sum(coalesce(" + (hasInvTableDataset ? "cast(i.factor as double precision) * t" + j + "." + invEmisToken + "_emis" : "null") + ", t" + j + "." + invEmisToken + "_emis)) as \"" + datasetName + "\"";
         }

         String diffQuery = "select @!@, " 
             + " \ncoalesce(" + aliasedPollList + ") as poll,"
//             + " \ncoalesce(" + (hasInvTableDataset ? "i.name" : "null") + ", " + aliasedPollList + ") as poll,"
             + " \ncoalesce(" + (hasInvTableDataset ? "i.name" : "p.pollutant_code_desc") + ", 'AN UNSPECIFIED DESCRIPTION') as " + (hasInvTableDataset ? "smoke_name" : "poll_desc") + ", "
             + sqlAnnEmisList
             + " \nfrom # "
             + (hasInvTableDataset ? "\nleft outer join\n $DATASET_TABLE[\"" + invTableDatasetName + "\", 1] i \non coalesce(" + aliasedPollList + ") = i.cas " : "\nleft outer join reference.pollutant_codes p \non coalesce(" + aliasedPollList + ") = p.pollutant_code ") 
             + " \ngroup by @@@, " + "coalesce(" + aliasedPollList + ")" + "," + "coalesce(" + (hasInvTableDataset ? "i.name" : "p.pollutant_code_desc") + ", 'AN UNSPECIFIED DESCRIPTION')"
//             + " \ngroup by @@@, " + "coalesce(" + (hasInvTableDataset ? "i.name" : "null") + ", " + aliasedPollList + ")" + "," + "coalesce(" + (hasInvTableDataset ? "i.name" : "p.pollutant_code_desc") + ", 'AN UNSPECIFIED DESCRIPTION')"
             + " \norder by @@@, " + "coalesce(" + aliasedPollList + ")" + "," + "coalesce(" + (hasInvTableDataset ? "i.name" : "p.pollutant_code_desc") + ", 'AN UNSPECIFIED DESCRIPTION')";

         diffQuery = query(diffQuery, true);

        //build inner sql statement with the datasets specified, make sure and full outer join the tables together
         String innerSQLBase = "";
         for (int j = 0; j < allDatasetNames.size(); j++) {
             innerSQLBase += (j > 0 ? " \nfull outer join " : "") 
                 + "(" + createDatasetQuery(allDatasetNames.get(j).toString().trim(), invEmisToken.trim())
                 + ") as t" + j + (j > 0 ? " \non " : "");
             if (j > 0) {
                 if (summaryTypeToken.equals("State+SCC")) 
                     innerSQLBase += "t" + j + ".fipsst=t" + (j-1) + ".fipsst and t" + j + ".scc=t" + (j-1) + ".scc";
                 else if (summaryTypeToken.equals("State")) 
                     innerSQLBase += "t" + j + ".fipsst=t" + (j-1) + ".fipsst";
                 else if (summaryTypeToken.equals("County")) 
                     innerSQLBase += "t" + j + ".fips=t" + (j-1) + ".fips";
                 else if (summaryTypeToken.equals("County+SCC")) 
                     innerSQLBase += "t" + j + ".fips=t" + (j-1) + ".fips and t" + j + ".scc=t" + (j-1) + ".scc";
                 else if (summaryTypeToken.equals("SCC")) 
                     innerSQLBase += "t" + j + ".scc=t" + (j-1) + ".scc";
                 innerSQLBase += " and t" + j + ".poll=t" + (j-1) + ".poll";
             }
         }

         //replace #base symbol with the unionized fire datasets query
         diffQuery = diffQuery.replaceAll("#", innerSQLBase);

         String sql = "";


         //replace @!@ symbol with main columns in outer select statement
         if (summaryTypeToken.equals("State+SCC")) 
             sql = "coalesce(" + aliasedFipsStList + ") as fipsst, coalesce(" + aliasedSCCList + ") as scc";
         else if (summaryTypeToken.equals("State")) 
             sql = "coalesce(" + aliasedFipsStList + ") as fipsst";
         else if (summaryTypeToken.equals("County")) 
             sql = "coalesce(" + aliasedFipsList + ") as fips";
         else if (summaryTypeToken.equals("County+SCC")) 
             sql = "coalesce(" + aliasedFipsList + ") as fips, coalesce(" + aliasedSCCList + ") as scc";
         else if (summaryTypeToken.equals("SCC")) 
             sql = "coalesce(" + aliasedSCCList + ") as scc";
         diffQuery = diffQuery.replaceAll("@!@", sql);

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
         diffQuery = diffQuery.replaceAll("!@!", sql);
         
         //replace @@@ symbol with group by columns in outer select statement
         if (summaryTypeToken.equals("State+SCC")) 
             sql = "coalesce(" + aliasedFipsStList + "), coalesce(" + aliasedSCCList + ")";
         else if (summaryTypeToken.equals("State")) 
             sql = "coalesce(" + aliasedFipsStList + ")";
         else if (summaryTypeToken.equals("County")) 
             sql = "coalesce(" + aliasedFipsList + ")";
         else if (summaryTypeToken.equals("County+SCC")) 
             sql = "coalesce(" + aliasedFipsList + "), coalesce(" + aliasedSCCList + ")";
         else if (summaryTypeToken.equals("SCC")) 
             sql = "coalesce(" + aliasedSCCList + ")";
         diffQuery = diffQuery.replaceAll("@@@", sql);
         
         //replace !!@ symbol with group by columns in inner select statement
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
         diffQuery = diffQuery.replaceAll("!!@", sql);

        //return the built query
        return diffQuery;
    }

    
    private String createDatasetQuery(String datasetName, String emisType) throws EmfException {

        String sql = "";
        sql = "\nselect !@!, trim(poll) as poll, sum(ann_emis) as ann_emis, sum(avd_emis) as avd_emis  \nfrom $DATASET_TABLE[\"" + 
        datasetName + "\", 1] m  \ngroup by !!@, trim(poll) ";
        sql = query(sql, false);
        return sql;
    }

    private String query(String partialQuery, boolean createClause) throws EmfException {

        SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissionDatasourceName, tableName );
        return parser.parse(partialQuery, createClause);
    }

}

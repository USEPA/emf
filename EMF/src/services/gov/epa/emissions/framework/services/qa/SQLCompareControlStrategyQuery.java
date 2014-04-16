package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class SQLCompareControlStrategyQuery extends SQLQAProgramQuery {
    ArrayList<String> baseDatasetNames = new ArrayList<String>();
    
    ArrayList<String> compareDatasetNames = new ArrayList<String>();
    
    //private boolean hasInvTableDataset;
    
    public SQLCompareControlStrategyQuery(HibernateSessionFactory sessionFactory, String emissioDatasourceName, String tableName, QAStep qaStep) {
        super(sessionFactory,emissioDatasourceName,tableName,qaStep);
    }
        
    public String createCompareQuery() throws EmfException {
        
        String programArguments = qaStep.getProgramArguments();
        
        //get applicable tables from the program arguments
        String invBaseToken = "";
        String invCompareToken ="";
        String invtableToken = "";
        String summaryTypeToken = "Plant";
        //String invTableDatasetName = "";
        
        int indexBase = programArguments.indexOf(QAStep.invBaseTag);
        int indexCompare = programArguments.indexOf(QAStep.invCompareTag);
        int indexInvTable = programArguments.indexOf(QAStep.invTableTag);
        int indexSumType = programArguments.indexOf(QAStep.summaryTypeTag);
        
        if (indexBase !=-1  && indexCompare !=-1 && indexInvTable != -1){
            invBaseToken = programArguments.substring(0, indexCompare).trim();
            invCompareToken = programArguments.substring(indexCompare, indexInvTable).trim();
            invtableToken = programArguments.substring(indexInvTable + QAStep.invTableTag.length(), indexSumType == -1 ? programArguments.length() : indexSumType);
        }
        if (indexSumType != -1) {
            summaryTypeToken = programArguments.substring(indexSumType + QAStep.summaryTypeTag.length()).trim();
        } 
        //default just in case...
        if (summaryTypeToken.trim().length() == 0)
            summaryTypeToken = "Plant";

         //parse inventories names for base and compare...
        if (invBaseToken.length() > 0 ) {
            StringTokenizer tokenizer2 = new StringTokenizer(invBaseToken, "\n");
            tokenizer2.nextToken();
            while (tokenizer2.hasMoreTokens()) {
                String datasetName = tokenizer2.nextToken().trim();
                if (datasetName.length() > 0){
                    baseDatasetNames.add(datasetName);
                    datasetNames.add(datasetName);
                }
            }
            
        } else {
            //see if there are tables to build the query with, if not throw an exception
            throw new EmfException("There are no control strategy detailed datasets specified (base).");
        }
        
        // get compare datasets
       if ( invCompareToken.length() > 0 ) {
            StringTokenizer tokenizer2 = new StringTokenizer(invCompareToken, "\n");
            tokenizer2.nextToken();
            while (tokenizer2.hasMoreTokens()) {
                String datasetName = tokenizer2.nextToken().trim();
                if (datasetName.length() > 0){
                    compareDatasetNames.add(datasetName);
                    datasetNames.add(datasetName);
                }
            }
        } else {
            //see if there are tables to build the query with, if not throw an exception
            throw new EmfException("There are no control strategy deetailed datasets specified (compare).");
        }
       
        checkDataset();

         //parse inventory table name...
         StringTokenizer tokenizer3 = new StringTokenizer(invtableToken);
         while (tokenizer3.hasMoreTokens()) {
             //invTableDatasetName  = tokenizer3.nextToken().trim();
             //if (invTableDatasetName.length() > 0) hasInvTableDataset = true;
         }
         String diffQuery = "select @@!, " 
             +  " \n sum(a.final_emissions) as base_ann_emis, " 
             +  " \n sum(b.final_emissions) as comp_ann_emis, " 
             +  " \n sum(a.final_emissions) - sum(b.final_emissions) as diff_ann_emis, " 
             +  " \n sum(a.emis_reduction) as base_emis_red, " 
             +  " \n sum(b.emis_reduction) as comp_emis_red, " 
             +  " \n sum(a.emis_reduction) - sum(b.emis_reduction) as diff_emis_red, " 
             + " \n case when coalesce(sum(a.inv_emissions),0) <> 0 then sum(a.emis_reduction) / sum(a.inv_emissions) * 100 else null end "
             + " \n- case when coalesce(sum(b.inv_emissions),0) <> 0 then sum(b.emis_reduction) / sum(b.inv_emissions) * 100 else null end as diff_pct_red, "
             + " \n sum(a.annual_cost) as base_ann_cost, "
             + " \n sum(b.annual_cost) as comp_ann_cost, "
             + " \n sum(a.annual_cost) - sum(b.annual_cost) as diff_ann_cost, "
             + " \n case when coalesce(sum(a.emis_reduction),0) <> 0 then sum(a.annual_cost) / sum(a.emis_reduction) else null end as base_cpt, " 
             + " \n case when coalesce(sum(b.emis_reduction),0) <> 0 then sum(b.annual_cost) / sum(b.emis_reduction) else null end as comp_cpt, " 
             + " \n case when coalesce(sum(a.emis_reduction),0) <> 0 then sum(a.annual_cost) / sum(a.emis_reduction) else null end "
             + " \n- case when coalesce(sum(b.emis_reduction),0) <> 0 then sum(b.annual_cost) / sum(b.emis_reduction) else null end as diff_cpt, " 
             + " \n sum(a.annual_oper_maint_cost) - sum(b.annual_oper_maint_cost) as diff_opmaint_cost,  "
             + " \n sum(a.annualized_capital_cost) - sum(b.annualized_capital_cost) as diff_annualized_cap_cost, "
             + " \n sum(a.total_capital_cost) - sum(b.total_capital_cost) as diff_ann_cap_cost "
             
             + " \nfrom $DATASET_TABLE[\"" + baseDatasetNames.get(0)+"\", 1] a "
             + " \nfull outer join $DATASET_TABLE[\""+compareDatasetNames.get(0) + "\", 1] b "
             + " \non !!! and a.poll = b.poll "
             + joinReferences(summaryTypeToken)
             + " \ngroup by @@@, " + " coalesce(a.poll, b.poll) "
             + " \norder by !!@, " + " coalesce(a.poll, b.poll) ";

         diffQuery = query(diffQuery, true);


       String sql = "";
       
     //replace @@! symbol with main columns in outer select statement
       if (summaryTypeToken.equals("Plant")) 
           sql = "coalesce(a.plantid, b.plantid) as plant_id, "
               + "coalesce(a.plant, b.plant) as plant_name, " 
               + "coalesce(a.poll, b.poll) as pollutant, "
               + "coalesce(avg(a.xloc), avg(b.xloc)) as longitude, "
               + "coalesce(avg(a.yloc), avg(b.yloc)) as latitude ";
       else if (summaryTypeToken.equals("State")) 
           sql = "substring(coalesce(a.fips, b.fips), 1, 2)::character varying(2) as fipsst, "
               + "fipscode.state_name, "
               + "coalesce(a.poll, b.poll) as pollutant, "
               + "avg(fipscode.state_maxlon - fipscode.state_minlon) as longitude, "
               + "avg(fipscode.state_maxlat - fipscode.state_minlat) as latitude ";
       else if (summaryTypeToken.equals("County")) 
           sql = "coalesce(a.fips, b.fips) as fips, fipscode.state_county_fips_code_desc, "
               + "coalesce(a.poll, b.poll) as pollutant, "
               + "fipscode.centerlon as longitude, "
               + "fipscode.centerlat as latitude ";
       else if (summaryTypeToken.equals("SCC")) 
           sql = "coalesce(a.scc, b.scc) as scc, "
               + "coalesce(a.poll, b.poll) as pollutant ";
       diffQuery = diffQuery.replaceAll("@@!", sql);

       //replace !!! symbol with conditions when join table base with compare   
       if (summaryTypeToken.equals("Plant")) 
           sql = " a.fips = b.fips and coalesce(a.plantid,'') = coalesce(b.plantid,'') ";
       else if (summaryTypeToken.equals("State")) 
           sql = " substring(a.fips, 1, 2) = substring(b.fips, 1, 2) ";
       else if (summaryTypeToken.equals("County")) 
           sql = " b.fips=a.fips ";
       else if (summaryTypeToken.equals("SCC")) 
           sql = "a.scc = b.scc ";
       diffQuery = diffQuery.replaceAll("!!!", sql);

       //replace @@@ symbol with group by columns in outer select statement
       if (summaryTypeToken.equals("Plant")) 
           sql =  "coalesce(a.fips, b.fips), "
               + "coalesce(a.plantid, b.plantid), "
               + "coalesce(a.plant, b.plant) ";
       else if (summaryTypeToken.equals("State")) 
           sql = "substring(coalesce(a.fips, b.fips), 1, 2), "
               + "fipscode.state_name";
       else if (summaryTypeToken.equals("County")) 
           sql = "coalesce(a.fips, b.fips), fipscode.centerlon, fipscode.centerlat, "
               + "fipscode.state_county_fips_code_desc";
       else if (summaryTypeToken.equals("SCC")) 
           sql = "coalesce(a.scc, b.scc) ";
       
       diffQuery = diffQuery.replaceAll("@@@", sql);
       
       //replace !!@ symbol with group by columns in outer select statement
       if (summaryTypeToken.equals("Plant")) 
           sql = "coalesce(a.plant, b.plant) ";
       else if (summaryTypeToken.equals("State")) 
           sql = "fipscode.state_name";
       else if (summaryTypeToken.equals("County")) 
           sql = "coalesce(a.fips, b.fips)";
       else if (summaryTypeToken.equals("SCC")) 
           sql = "coalesce(a.scc, b.scc)";
       
       diffQuery = diffQuery.replaceAll("!!@", sql);
       //return the built query
       return diffQuery;
    }

    private String joinReferences(String summaryType){
        String sql = "";
        
        if (summaryType.equals("Plant") || summaryType.equals("SCC")) 
            return sql;
        else if (summaryType.equals("State")) 
            sql = " left outer join reference.fips fipscode "
                + " on substring(fipscode.state_county_fips, 1, 2) = substring(coalesce(a.fips, b.fips), 1, 2)"
                + " and fipscode.country_num = '0'";
        else if (summaryType.equals("County")) 
            sql = " left outer join reference.fips fipscode on fipscode.state_county_fips = coalesce(a.fips, b.fips)"
                 +" and fipscode.country_num = '0'";
        //sql = query(sql, false);

        return sql;
    }
    
    private String query(String partialQuery, boolean createClause) throws EmfException {

        SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissionDatasourceName, tableName );
        return parser.parse(partialQuery, createClause);
    }

}

package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.hibernate.Session;

public class SQLAnnualStateSummariesCrosstabQuery extends SQLQAProgramQuery{
    
    public static final String smkRptTag = "-smkrpt";

    public static final String coStCyTag = "-costcy";

    public static final String pollListTag = "-polllist";

    public static final String specieListTag = "-specielist";

    public static final String exclPollTag = "-exclpoll";

    public static final String sortPollTag = "-sortpoll";

    public SQLAnnualStateSummariesCrosstabQuery(HibernateSessionFactory sessionFactory, DbServer dbServer, 
            String emissioDatasourceName, String tableName, 
            QAStep qaStep) {
        super(sessionFactory,emissioDatasourceName,tableName,qaStep);
    }

    private String[] parseSwitchArguments(String programSwitches, int beginIndex, int endIndex) {
        List<String> inventoryList = new ArrayList<String>();
        String value = "";
        String valuesString = "";
        
        valuesString = programSwitches.substring(beginIndex, endIndex);
        StringTokenizer tokenizer2 = new StringTokenizer(valuesString, "\n");
        tokenizer2.nextToken(); // skip the flag

        while (tokenizer2.hasMoreTokens()) {
            value = tokenizer2.nextToken().trim();
            if (!value.isEmpty())
                inventoryList.add(value);
        }
        return inventoryList.toArray(new String[0]);
    }
    
    public String createQuery() throws EmfException {
        String sql = "";
        String programArguments = qaStep.getProgramArguments();
        
        int smkRptIndex = programArguments.indexOf(smkRptTag);
        int coStCyIndex = programArguments.indexOf(coStCyTag);
        int pollListIndex = programArguments.indexOf(pollListTag);
//        int specieListIndex = programArguments.indexOf(specieListTag);
//        int exclPollIndex = programArguments.indexOf(exclPollTag);
//        int sortPollIndex = programArguments.indexOf(sortPollTag);

        String[] smkRptNames = null; 
        String coStCyName = null;
        String[] polls = {};
//        String[] species = {};
//        String[] exclPolls = {};
//        String[] sortPolls = {};

        String[] arguments;
        String version;
        String table;
        Map<String, String> sortedColumnMap = new TreeMap<String, String>();

        if (smkRptIndex != -1) {
            arguments = parseSwitchArguments(programArguments, smkRptIndex, programArguments.indexOf("\n-", smkRptIndex) != -1 ? programArguments.indexOf("\n-", smkRptIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                smkRptNames = arguments;
                for (String item: smkRptNames)
                    datasetNames.add(item);
            }
        }
        if (coStCyIndex != -1) {
            arguments = parseSwitchArguments(programArguments, coStCyIndex, programArguments.indexOf("\n-", coStCyIndex) != -1 ? programArguments.indexOf("\n-", coStCyIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                coStCyName = arguments[0];
                datasetNames.add(coStCyName);
            }
        }
        if (pollListIndex != -1) {
            arguments = parseSwitchArguments(programArguments, pollListIndex, programArguments.indexOf("\n-", pollListIndex) != -1 ? programArguments.indexOf("\n-", pollListIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                polls = arguments;
                for (String poll : polls)
                    sortedColumnMap.put(poll, poll);
            }

        }

        //validate everything has been specified...
        String errors = "";
        //make sure all datasets were specified, look at the names
        if (smkRptNames == null) {
            errors = "Missing " + DatasetType.smkmergeRptStateAnnualSummary + " dataset(s). ";
        }
        if (coStCyName == null || coStCyName.length() == 0) {
            errors += "Missing " + DatasetType.countryStateCountyNamesAndDataCOSTCY + " dataset. ";
        }
        if ((polls == null || polls.length == 0)
             /*&& (species == null || species.length == 0)*/) {
            errors += "Missing pollutants and species to include in report. ";
        }

        //make sure there are no duplicate pollutant in the poll and species list
        if (polls != null && polls.length > 0) {
            Map<String, String> map = new TreeMap<String, String>();
            for (String poll : polls) {
                if (map.containsKey(poll))
                    errors += "Pollutants list already has, " + poll + ". No duplicates are allowed in the pollutant list. ";
                map.put(poll, poll);
            }
        }
        
        
        //go ahead and throw error from here, no need to validate anymore if the above is not there...
        if (errors.length() > 0) {
            throw new EmfException(errors);
        }
        
        checkDataset();

        //make sure the all the datasets actually exist
        EmfDataset[] smkRpts = new EmfDataset[] {};
        if (smkRptNames != null) {
            smkRpts = new EmfDataset[smkRptNames.length];
            for (int i = 0; i < smkRptNames.length; i++) {
                smkRpts[i] = getDataset(smkRptNames[i]);
            }
        }

        //make sure country, state, county data dataset exists
        EmfDataset coStCy = null;
        coStCy = getDataset(coStCyName);
        String coStCyTableName = "emissions.state";
        String coStCyVersion = new VersionedQuery(version(coStCy.getId(), coStCy.getDefaultVersion()), "costcy").query();    
        
        //Outer SELECT clause, DISTINCT ON makes sure only one tolerance record is applied...
        sql = "select sector,\n"
                + "fipscost,\n"
                + "state_name \n";
        for (String poll : polls) {
            sql += ",coalesce(\"" + poll + "\",0.0::double precision) as \"" + poll + "\" \n";
        }
//        for (String specie : species) {
//            sql += ",\"" + specie + "\" \n";
//        }

        //union together all smkmerge reports and aggregate to the sector, fipsst, state_name, smoke_name level
        sql += "from crosstab(' \n";

        int i = 0;

//not needed, we need to include all pollutants, if we exclude certain pollutants then we might not see any 
//results for if a certain report if we happen to exclude all pollutants for that report, at a minimum we want 
//to show a record with no emission values
//        //build sql filter for excluding certain polls from the report.
//        String exclPollsSql = "";
//        if (exclPolls.length > 0) {
//            exclPollsSql = "smk.species not in ( ";
//            for (String poll : exclPolls) {
//                exclPollsSql += (i > 0 ? "," : "") + "''" + poll.replace("'","''") + "''";
//                ++i;
//            }
//            exclPollsSql += ") \n";
//        }

//not needed, we need to include all pollutants, if we exclude certain pollutants then we might not see any 
//results for if a certain report if we happen to exclude all pollutants for that report, at a minimum we want 
//to show a record with no emission values
//        //build sql filter for including certain polls from the report.
//        //reset counter
//        i = 0;
//        String inclPollsSql = "";
//        inclPollsSql = "smk.species in ( ";
//        for (String poll : sortedColumnMap.values()) {
//            inclPollsSql += (i > 0 ? "," : "") + "''" + poll.replace("'","''") + "''";
//            ++i;
//        }
//        inclPollsSql += ") \n";

        //reset counter
        i = 0;
        for (EmfDataset dataset : smkRpts) {
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion()), "smk").query();
            table = qualifiedEmissionTableName(dataset);
            if (i > 0) sql += " union all \n";

            sql += "select sector || (trim(TO_CHAR(coalesce(costcy.countrycode, 0), ''0''))::character varying(1) || trim(TO_CHAR(costcy.statecode, ''00'')))::character varying(3) || smk.state as id, \n"
            + "sector, \n"
            + "(trim(TO_CHAR(coalesce(costcy.countrycode, 0), ''0''))::character varying(1) || trim(TO_CHAR(costcy.statecode, ''00'')))::character varying(3) as fipscost, \n"
            + "smk.state as state_name, \n"
            + "smk.species as attr, \n"
            + "ann_emis as value \n"
            + "from " + table + " smk \n"
            + "inner join " + coStCyTableName + " costcy \n"
            + "on costcy.statename = smk.state \n"
            + "where " + version.replace("'", "''") + " \n"
            + "and " + coStCyVersion.replace("'", "''") + " \n";
//            + "and " + inclPollsSql + " \n"
//            
//            if (exclPolls.length > 0) {
//                sql += "and " + exclPollsSql + " \n";
//            }
            ++i;
        }

        sql += "order by id, attr \n";
        sql += "','\n";
        //reset counter
        i = 0;
        for (String column : sortedColumnMap.values()) {
            sql += (i > 0 ? "union " : "") + "select ''" + column + "'' \n";
            ++i;
        }
        sql += "order by 1\n"
            + "') as c(\n"
            + "id character varying(1000),\n"
            + "sector character varying(255),\n"
            + "fipscost character varying(3),\n"
            + "state_name character varying(255)\n";
        for (String column : sortedColumnMap.values()) {
            sql += ",\"" + column + "\" double precision \n";
        }
        
        sql += ") \n"
            + "order by sector, fipscost, state_name \n";
        
        
//        
//        sql = query(sql, true);
        sql = "CREATE TABLE " + emissionDatasourceName + "." + tableName + " AS " + sql;
        System.out.println(sql);
        
        return sql;
    }

    protected String query(String partialQuery, boolean createClause) throws EmfException {

        SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissionDatasourceName, tableName );
        return parser.parse(partialQuery, createClause);
    }

}

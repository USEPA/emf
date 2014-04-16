package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.hibernate.Session;

public class SQLCreateMoEmisByCtyFromAnnEmisQuery extends SQLQAProgramQuery{
    
    private DbServer dbServer;

    public static final String smkRptTag = "-smkrpt";
    
    public static final String temporalProfileTag = "-temporal";

    public static final String yearTag = "-year";

    ArrayList<String> baseDatasetNames = new ArrayList<String>();
    
    ArrayList<String> compareDatasetNames = new ArrayList<String>();
    
    public SQLCreateMoEmisByCtyFromAnnEmisQuery(HibernateSessionFactory sessionFactory, DbServer dbServer, 
            String emissioDatasourceName, String tableName, 
            QAStep qaStep) {
        super(sessionFactory,emissioDatasourceName,tableName,qaStep);
        this.dbServer = dbServer;
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

    public String createCompareQuery() throws EmfException {
        String sql = "";
        String programArguments = qaStep.getProgramArguments();
        
        int smkRptIndex = programArguments.indexOf(smkRptTag);
        int temporalProfileIndex = programArguments.indexOf(temporalProfileTag);
        int yearIndex = programArguments.indexOf(yearTag);
        String temporalProfileName = null;
        String[] smkRptNames = null; 
        String yearAsString = null;
        Integer year = null;
        String[] arguments;
        String version;
        String table;


        if (temporalProfileIndex != -1) {
            arguments = parseSwitchArguments(programArguments, temporalProfileIndex, programArguments.indexOf("\n-", temporalProfileIndex) != -1 ? programArguments.indexOf("\n-", temporalProfileIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                temporalProfileName = arguments[0];
                datasetNames.add(temporalProfileName);
            }
        }
        if (smkRptIndex != -1) {
            arguments = parseSwitchArguments(programArguments, smkRptIndex, programArguments.indexOf("\n-", smkRptIndex) != -1 ? programArguments.indexOf("\n-", smkRptIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                smkRptNames = arguments;
                for (String item: smkRptNames)
                    datasetNames.add(item);
            }
        }
        
        if (yearIndex != -1) {
            arguments = parseSwitchArguments(programArguments, yearIndex, programArguments.indexOf("\n-", yearIndex) != -1 ? programArguments.indexOf("\n-", yearIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) if (arguments[0] != null && arguments[0].trim().length() > 0) yearAsString = arguments[0].trim();
        }
        
        //validate everything has been specified...
        String errors = "";
        //make sure all dataset were specified, look at the names
        if (temporalProfileName == null) {
            errors = "Missing temporal profile dataset. ";
        }
        if (smkRptNames == null || smkRptNames.length == 0) {
            errors += "Missing SMOKE Report Dataset(s). ";
        }
        if (yearAsString == null) {
            errors += "Missing SMOKE Report Dataset(s) year. ";
        } else {
            try {
                year = Integer.parseInt(yearAsString);
            } catch (NumberFormatException ex) {
                errors += "The year is not valid format, it must be a number. ";
            }
        }
        
        if (errors.length() > 0) {
            throw new EmfException(errors);
        }
        
        checkDataset();
        //make sure the all the datasets actually exist
        EmfDataset temporalProfile = getDataset(temporalProfileName);
       
        String temporalProfileTable = qualifiedEmissionTableName(temporalProfile);
        String temporalProfileVersion = new VersionedQuery(version(temporalProfile.getId(), temporalProfile.getDefaultVersion()), "t").query();

        EmfDataset[] smkRpts = new EmfDataset[smkRptNames.length];
        for (int i = 0; i < smkRptNames.length; i++) {
            smkRpts[i] = getDataset(smkRptNames[i]);
        }
        
        //look to see if the sector has been specified and also see if the sector 
        //is associated with more than one dataset
        Map<String, EmfDataset> sectorMap = new HashMap<String, EmfDataset>();
        for (EmfDataset dataset : smkRpts) {
            String sector = getDatasetSector(dataset);
            if (sector == null || sector.trim().length() == 0)
                throw new EmfException("Dataset, " + dataset.getName() + ", is missing the sector.");
            if (!sectorMap.containsKey(sector)) {
                sectorMap.put(sector, dataset);
            } else {
                throw new EmfException("Another dataset, " + sectorMap.get(sector).getName() + ", is already using this sector, " + sector + ".  A paticular sector can only be associated to one SMOKE Report.");
            }
        }        
        
        String sumWeightsForYear = "(jan + feb + mar + apr + may + jun + jul + aug + sep + oct + nov + dece)";
        
        String factor = "1 \n"
                + "/ \n"
                + "(jan::double precision / " + sumWeightsForYear + " * 31 + \n"
                + "feb::double precision / " + sumWeightsForYear + " * case when ((i.year % 4 = 0) and (i.year % 100 != 0)) or (i.year % 400 = 0) then 29 else 28 end + \n"
                + "mar::double precision / " + sumWeightsForYear + " * 31 + \n"
                + "apr::double precision / " + sumWeightsForYear + " * 30 + \n"
                + "may::double precision / " + sumWeightsForYear + " * 31 + \n"
                + "jun::double precision / " + sumWeightsForYear + " * 30 + \n"
                + "jul::double precision / " + sumWeightsForYear + " * 31 + \n"
                + "aug::double precision / " + sumWeightsForYear + " * 31 + \n"
                + "sep::double precision / " + sumWeightsForYear + " * 30 + \n"
                + "oct::double precision / " + sumWeightsForYear + " * 31 + \n"
                + "nov::double precision / " + sumWeightsForYear + " * 30 + \n"
                + "dece::double precision / " + sumWeightsForYear + " * 31) \n";
        
        sql = "select \n"
                + "i.label as sector, \n"
                + "i.region as fips, \n"
                + "i.state, \n"
                + "i.county, \n"
                + "i.variable as poll, \n"
                + "m.month_no, \n"
                + "i.year, \n"
                + "sum(data_value \n"
                + "* case  \n"
                + "when m.month_no = 1 then  \n"
                + "jan::double precision / " + sumWeightsForYear + "  \n"
                + "* 31  \n"
                + "* " + factor
                + "when m.month_no = 2 then  \n"
                + "feb::double precision / " + sumWeightsForYear + " \n"
                + "* case when ((i.year % 4 = 0) and (i.year % 100 != 0)) or (i.year % 400 = 0) then 29 else 28 end \n"
                + "* " + factor + "\n"
                + "when m.month_no = 3 then \n"
                + "mar::double precision / " + sumWeightsForYear + "\n"
                + "* 31 \n"
                + "* " + factor + "\n"
                + "when m.month_no = 4 then \n"
                + "apr::double precision / " + sumWeightsForYear + "\n"
                + "* 30 \n"
                + "* " + factor + "\n"
                + "when m.month_no = 5 then \n"
                + "may::double precision / " + sumWeightsForYear + " \n"
                + "* 31 \n"
                + "* " + factor + "\n"
                + "when m.month_no = 6 then\n"
                + "jun::double precision / " + sumWeightsForYear + " \n"
                + "* 30 \n"
                + "* " + factor + "\n"
                + "when m.month_no = 7 then\n"
                + "jul::double precision / " + sumWeightsForYear + " \n"
                + "* 31 \n"
                + "* " + factor + "\n"
                + "when m.month_no = 8 then\n"
                + "aug::double precision / " + sumWeightsForYear + "\n"
                + "* 31 \n"
                + "* " + factor + "\n"
                + "when m.month_no = 9 then\n"
                + "sep::double precision / " + sumWeightsForYear + "\n"
                + "* 30 \n"
                + "* " + factor + "\n"
                + "when m.month_no = 10 then\n"
                + "oct::double precision / " + sumWeightsForYear + "\n"
                + "* 31 \n"
                + "* " + factor + "\n"
                + "when m.month_no = 11 then\n"
                + "nov::double precision / " + sumWeightsForYear + "\n"
                + "* 30 \n"
                + "* " + factor + "\n"
                + "when m.month_no = 12 then\n"
                + "dece::double precision / " + sumWeightsForYear + "\n"
                + "* 31 \n"
                + "* " + factor + "\n"
                + "end) as emission\n"
                + "from (\n";
        

        int i = 0;
        for (EmfDataset dataset : smkRpts) {
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
            table = qualifiedEmissionTableName(dataset);
            if (i > 0) sql += " union all \n";
            sql += "select\n"
                + "label,\n"
                + "region,\n"
                + "state,\n"
                + "county,\n"
                + "variable,\n"
                + "data_value,\n"
                + "monthly_prf,\n"
                + year + "::integer as \"year\"\n"
                + "from \n"
                + table + "\n"
                + "where " + version + "\n";
            ++i;
        }
        
        
            sql += ") i\n"
                + "inner join " + temporalProfileTable + " t\n"
                + "on t.code = i.monthly_prf::integer\n"
                + "-- create monthly rows...\n"
                + "cross join (\n"
                + "select 1::integer as month_no\n"
                + "union all select 2::integer\n"
                + "union all select 3::integer\n"
                + "union all select 4::integer\n"
                + "union all select 5::integer\n"
                + "union all select 6::integer\n"
                + "union all select 7::integer\n"
                + "union all select 8::integer\n"
                + "union all select 9::integer\n"
                + "union all select 10::integer\n"
                + "union all select 11::integer\n"
                + "union all select 12::integer\n"
                + ") m\n"
                + "where " + temporalProfileVersion + "\n"
                + "group by i.label,\n"
                + "i.region,\n"
                + "i.state,\n"
                + "i.county,\n"
                + "i.variable,\n"
                + "m.month_no,\n"
                + "i.year\n"
                + "order by i.label,\n"
                + "i.region,\n"
                + "i.variable,\n"
                + "i.year,\n"
                + "m.month_no";

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
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

    private String getDatasetSector(EmfDataset dataset) throws EmfException {
        String sector = null;
        //try and get sector from dataset
        if (dataset.getSectors() != null && dataset.getSectors().length > 0)
            sector = dataset.getSectors()[0].getName();
        
        //if no sector found associated with dataset, then lets look at the datasets data
        String datasetTable = qualifiedEmissionTableName(dataset);
        String datasetVersion = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();

        String query = "select distinct label from " + datasetTable + " where " + datasetVersion + ";";
        ResultSet rs = null;
        //System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = dbServer.getEmissionsDatasource().query().executeQuery(query);
            while (rs.next()) {
                sector = rs.getString(1);
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }
        return sector;
    }

    protected boolean checkTableForColumns(String table, String colList) throws EmfException {
        String query = "select public.check_table_for_columns('" + table + "', '" + colList + "', ',');";
        ResultSet rs = null;
        boolean tableHasColumns = false;
        //System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = dbServer.getEmissionsDatasource().query().executeQuery(query);
            while (rs.next()) {
                tableHasColumns = rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }
        return tableHasColumns;
    }
}

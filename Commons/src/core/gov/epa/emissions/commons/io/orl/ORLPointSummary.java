package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.io.importer.DatasetLoader;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.SummaryTable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ORLPointSummary implements SummaryTable {

    private Datasource emissionsDatasource;

    private Datasource referenceDatasource;

    private Dataset dataset;

    public ORLPointSummary(Datasource emissionDatasource, Datasource referenceDatasource, Dataset dataset) {
        this.emissionsDatasource = emissionDatasource;
        this.referenceDatasource = referenceDatasource;
        this.dataset = dataset;
    }

    public void createSummary() throws ImporterException {
        DatasetLoader loader = new DatasetLoader(dataset);
        dataset.setSummarySource(loader.summarySource());
        String orlTable = emissionsDatasource.getName() + "." + dataset.getInternalSources()[0].getTable();
        String summaryTable = emissionsDatasource.getName() + "." + dataset.getSummarySource().getTable();

        try {
            // get the pollutant CAS codes
            ResultSet rs = emissionsDatasource.query().executeQuery("SELECT DISTINCT(" + "POLL" + ") FROM " + orlTable);
            rs.last();
            int numOfPollutants = rs.getRow();
            String[] pollutants = new String[numOfPollutants];
            rs.first();
            for (int index = 0; index < pollutants.length; index++) {
                pollutants[index] = rs.getString("POLL");
                rs.next();
            }
            rs.close();

            // we only want the primary column in the summary table
            String emissionCol = null;
            if (dataset.getTemporalResolution().equalsIgnoreCase("annual")) {
                emissionCol = "ANN_EMIS";
            } else {
                emissionCol = "AVD_EMIS";
            }
            // rather than continuously checking for dataset type,
            // check once and set all necessary variables and parameters
            String summarySelectDistinct = null;
            String summaryFromSelectDistinct = " FROM " + referenceDatasource.getName() + ".fips as f, ";
            String tempSelectDistinct = null;
            String tempFromSelectDistinct = null;

            // String createIndex = " (INDEX orl_key (" + FIPS + ", " + PLANTID
            // + ", " + POINTID + ", "
            // + STACKID + ", " + SEGMENT + ", " + SCC + "))";
            summarySelectDistinct = " SELECT DISTINCT f." + "state_abbr" + " as " + "State" + ", " + "e." + "FIPS" + " as "
                    + "FIPS" + ", e." + "PLANTID" + " as " + "Plant_Id" + ", e." + "POINTID" + " as " + "Point_Id" + ", e."
                    + "STACKID" + " as " + "Stack_Id" + ", e." + "SEGMENT" + " as " + "Segment" + ", e." + "SCC" + " as "
                    + "SCC" + ", e." + "PLANT" + " as " + "Plant" + ", e." + "ERPTYPE" + " as " + "Emissons_Release_Point"
                    + ", e." + "SRCTYPE" + " as " + "Source_Type" + ", e." + "STKHGT" + " as " + "Height" + ", e."
                    + "STKDIAM" + " as " + "Diameter" + ", e." + "STKTEMP" + " as " + "Exit_Temp" + ", e." + "STKFLOW"
                    + " as " + "Exit_Flow_Rate" + ", e." + "STKVEL" + " as " + "Exit_Vel" + ", e." + "SIC" + " as " + "SIC"
                    + ", e." + "MACT" + " as " + "MACT" + ", e." + "NAICS" + " as " + "NAICS" + ", e." + "CTYPE" + " as "
                    + "Coordinate_System_Type" + ", e." + "XLOC" + " as " + "X_Coord" + ", e." + "YLOC" + " as "
                    + "Y_Coord" + ", e." + "UTMZ" + " as " + "UTM_Zone" + ", ";
            summaryFromSelectDistinct += "(SELECT DISTINCT " + "FIPS" + ", " + "PLANTID" + ", " + "POINTID" + ", "
                    + "STACKID" + ", " + "SEGMENT" + ", " + "SCC" + ", " + "PLANT" + ", " + "ERPTYPE" + ", " + "SRCTYPE"
                    + ", " + "STKHGT" + ", " + "STKDIAM" + ", " + "STKTEMP" + ", " + "STKFLOW" + ", " + "STKVEL" + ", "
                    + "SIC" + ", " + "MACT" + ", " + "NAICS" + ", " + "CTYPE" + ", " + "XLOC" + ", " + "YLOC" + ", "
                    + "UTMZ" + " FROM " + orlTable + " ) e ";
            tempSelectDistinct = " SELECT DISTINCT e." + "FIPS" + " as " + "FIPS" + ", e." + "PLANTID" + " as "
                    + "Plant_Id" + ", e." + "POINTID" + " as " + "Point_Id" + ", e." + "STACKID" + " as " + "Stack_Id"
                    + ", e." + "SEGMENT" + " as " + "Segment" + ", e." + "SCC" + " as " + "SCC" + ", ";
            tempFromSelectDistinct = " FROM (SELECT DISTINCT " + "FIPS" + ", " + "PLANTID" + ", " + "POINTID" + ", "
                    + "STACKID" + ", " + "SEGMENT" + ", " + "SCC" + " FROM " + orlTable + " ) e ";

            // if there is a large number of tables, join small
            // groups to form subsets, then join the subsets
            final int MAX_POLLUTANTS_JOIN = 50;
            String[] tempTableNames = null;
            String[] tempTableQueries = null;
            List tempTableIndexList = new ArrayList();

            // these variables depend on whether or not we have subsets
            String summaryTableJoinPart = "";
            String summaryTableSelectPart = "";
            String summaryTableAndPart = "";

            // split into subgroups and use temp tables
            if (numOfPollutants > MAX_POLLUTANTS_JOIN) {
                tempTableNames = new String[(int) Math.ceil(numOfPollutants / (double) MAX_POLLUTANTS_JOIN)];
                tempTableQueries = new String[tempTableNames.length];

                // get the names of the temp tables
                List tableNames = emissionsDatasource.tableDefinition().getTableNames();
                for (int i = 0, nextTemp = 0; i < tempTableNames.length; i++, nextTemp++) {
                    String tempTableName = summaryTable + "_temp" + nextTemp;
                    while (tableNames.contains(tempTableName.toLowerCase())) {
                        nextTemp++;
                        tempTableName = summaryTable + "_temp" + nextTemp;
                    }
                    tempTableNames[i] = tempTableName;
                }

                // the select and join portions of the temporary table CREATE
                // statements
                String[] tempTableSelectParts = new String[tempTableNames.length];
                String[] tempTableJoinParts = new String[tempTableNames.length];
                Arrays.fill(tempTableSelectParts, "");
                Arrays.fill(tempTableJoinParts, "");
                for (int index = 0, i = 0; index < pollutants.length; index++, i = (index / MAX_POLLUTANTS_JOIN)) {
                    // when using number (CAS) as column name, enclose in
                    // slanted
                    // quotes
                    String cleanPoll = pollutants[index].replace('-', '_');
                    tempTableSelectParts[i] += cleanPoll + "." + emissionCol + " as " + cleanPoll + ", ";
                    summaryTableSelectPart += "t" + i + "." + cleanPoll + " as " + cleanPoll + ", ";

                    // get FIPS, PLANTID, POINTID, STACKID, SEGMENT, SCC and CAS for
                    // pollutant

                    tempTableJoinParts[i] = tempTableJoinParts[i] + "LEFT JOIN (SELECT " + "FIPS" + ", " + "PLANTID" + ", "
                            + "POINTID" + ", " + "STACKID" + ", " + "SEGMENT" + ", " + "SCC" + ", " + emissionCol
                            + " FROM " + orlTable + " WHERE " + "POLL" + " = '" + pollutants[index] + "') " + cleanPoll
                            + " ON (e." + "FIPS" + " = " + cleanPoll + "." + "FIPS" + " AND e." + "PLANTID" + " = "
                            + cleanPoll + "." + "PLANTID" + " AND e." + "POINTID" + " = " + cleanPoll + "." + "POINTID"
                            + " AND e." + "STACKID" + " = " + cleanPoll + "." + "STACKID" + " AND e." + "SEGMENT" + " = "
                            + cleanPoll + "." + "SEGMENT" + " AND e." + "SCC" + " = " + cleanPoll + "." + "SCC" + ") ";
                    summaryTableAndPart += "e." + "FIPS" + " = t" + i + "." + "FIPS" + " AND e." + "PLANTID" + " = t" + i
                            + "." + "Plant_Id" + " AND e." + "POINTID" + " = t" + i + "." + "Point_Id" + " AND e."
                            + "STACKID" + " = t" + i + "." + "Stack_Id" + " AND e." + "SEGMENT" + " = t" + i + "."
                            + "Segment" + " AND e." + "SCC" + " = t" + i + "." + "SCC" + " AND ";
                }

                // the temporary table CREATE statements
                for (int i = 0; i < tempTableNames.length; i++) {
                    tempTableSelectParts[i] = tempTableSelectParts[i].substring(0, tempTableSelectParts[i].length() - 2);
                    summaryTableJoinPart += tempTableNames[i] + " as t" + i + ", ";

                    // TODO: create index after table creation
                    tempTableQueries[i] = "CREATE TABLE " + tempTableNames[i] + " AS " + tempSelectDistinct
                            + tempTableSelectParts[i] + tempFromSelectDistinct + tempTableJoinParts[i];

                    // schema.table -> schema_table
                    String indexPrefix = tempTableNames[i].replace('.', '_');
                    tempTableIndexList.add("CREATE INDEX " + indexPrefix + "_orl_key ON " + tempTableNames[i] + "("
                            + "FIPS" + ", " + "SCC" + ")");
                }
            }
            // don't need to create temp tables
            else {
                // for each pollutant CAS code
                for (int i = 0; i < numOfPollutants; i++) {
                    // when using number (CAS) as column name, enclose in
                    // slanted
                    // quotes
                    String cleanPoll = "_" + pollutants[i].replace('-', '_');
                    summaryTableSelectPart += cleanPoll + "." + emissionCol + " as " + cleanPoll + ", ";
                    // get FIPS, PLANTID, POINTID, STACKID, SEGMENT, SCC and CAS
                    // for pollutant
                    summaryTableJoinPart += "LEFT JOIN (SELECT " + "FIPS" + ", " + "PLANTID" + ", " + "POINTID" + ", "
                            + "STACKID" + ", " + "SEGMENT" + ", " + "SCC" + ", " + emissionCol + " FROM " + orlTable
                            + " WHERE " + "POLL" + " = '" + pollutants[i] + "') " + cleanPoll + " ON (e." + "FIPS" + " = "
                            + cleanPoll + "." + "FIPS" + " AND e." + "PLANTID" + " = " + cleanPoll + "." + "PLANTID"
                            + " AND e." + "POINTID" + " = " + cleanPoll + "." + "POINTID" + " AND e." + "STACKID" + " = "
                            + cleanPoll + "." + "STACKID" + " AND e." + "SEGMENT" + " = " + cleanPoll + "." + "SEGMENT"
                            + " AND e." + "SCC" + " = " + cleanPoll + "." + "SCC" + ") ";
                }
            }

            // FIXME: drop all the tables before creating them
            // create the temp tables first, if needed
            if (tempTableNames != null) {
                for (int i = 0; i < tempTableQueries.length; i++) {
                    emissionsDatasource.query().execute(tempTableQueries[i]);
                    emissionsDatasource.query().execute((String) tempTableIndexList.get(i));
                }
                // will need comma after select distinct
                summaryFromSelectDistinct += ", ";
                // won't need comma after last table join
                summaryTableJoinPart = summaryTableJoinPart.substring(0, summaryTableJoinPart.length() - 2);
            }
            // the summary table CREATE statement
            summaryTableSelectPart = summaryTableSelectPart.substring(0, summaryTableSelectPart.length() - 2);
            String query = "CREATE TABLE " + summaryTable + /* createIndex + */" AS " + summarySelectDistinct
                    + summaryTableSelectPart + summaryFromSelectDistinct + summaryTableJoinPart + " WHERE (e." + "FIPS"
                    + " = f." + "state_county_fips" + " AND " + summaryTableAndPart + "f.country_code='US')";

            // create the actual table

            emissionsDatasource.query().execute(query);

            // drop the temp tables, if needed
            if (tempTableNames != null) {
                for (int i = 0; i < tempTableNames.length; i++) {
                    emissionsDatasource.tableDefinition().dropTable(tempTableNames[i]);
                }
            }
        } catch (Exception e) {
            throw new ImporterException("Error in create summary table-"+ e.getMessage());
        }
    }
}

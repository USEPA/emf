package gov.epa.emissions.commons.io.nif.nonpointNonroad;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.io.importer.DatasetLoader;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.SummaryTable;

import java.sql.ResultSet;

public class NIFNonpointNonRoadSummary implements SummaryTable {

    private Dataset dataset;

    private Datasource emissionDatasource;

    private Datasource referenceDatasource;

    public NIFNonpointNonRoadSummary(Datasource emission, Datasource reference, Dataset dataset) {
        this.emissionDatasource = emission;
        this.referenceDatasource = reference;
        this.dataset = dataset;
    }

    public void createSummary() throws ImporterException {
        DatasetLoader loader = new DatasetLoader(dataset);
        dataset.setSummarySource(loader.summarySource());
        String emTable = emissionDatasource.getName() + "." + emissionRecordsTable(dataset);
        String epTable = emissionDatasource.getName() + "." + emissionProcessTable(dataset);
        String summaryTable = emissionDatasource.getName() + "." + dataset.getSummarySource().getTable();

        try {
            table(emissionDatasource, dataset);
            ResultSet rs = emissionDatasource.query().executeQuery("SELECT DISTINCT(pollutant_code) FROM " + emTable);
            rs.last();
            int numOfPollutants = rs.getRow();
            rs.first();
            String[] pollutants = new String[numOfPollutants];

            String selectPart = "";
            String joinPart = "";
            String cleanPoll;

            for (int i = 0; i < numOfPollutants; i++) {
                pollutants[i] = rs.getString("pollutant_code");
                cleanPoll = pollutants[i].replace('-', '_');
                selectPart = selectPart + cleanPoll + ".emission_value as " + cleanPoll + ", ";
                joinPart = joinPart + "LEFT JOIN (SELECT state_county_fips, scc, emission_value FROM " + emTable
                        + " WHERE pollutant_code = '" + pollutants[i] + "') " + cleanPoll
                        + " ON (e.state_county_fips = " + cleanPoll + ".state_county_fips AND e.scc = " + cleanPoll
                        + ".scc)";
                rs.next();
            }
            rs.close();

            selectPart = selectPart.substring(0, selectPart.length() - 2);

            String query = "CREATE TABLE " + summaryTable + " AS SELECT DISTINCT f.state_abbr as STATE, "
                    + "e.state_county_fips as FIPS, " + "e.scc as SCC, " + "ep.mact_code as MACT, "
                    + "sic_code as SIC, " + "naics_code as NAICS, " + selectPart + " FROM " + epTable + " as ep, "
                    + referenceDatasource.getName() + ".fips as f, " + "(SELECT DISTINCT state_county_fips, scc"
                    + " FROM " + emTable + ") e " + joinPart
                    + " WHERE (e.state_county_fips = f.state_county_fips AND f.country_code='US') "
                    + "AND (e.state_county_fips=ep.state_county_fips AND e.scc=ep.scc)";

            emissionDatasource.query().execute(query);
        } catch (Exception e) {
            throw new ImporterException("Error in create summary table-"+ e.getMessage());
        }
    }

    private String emissionProcessTable(Dataset dataset) {
        String emissionProcessIdentifier = "emission process";
        return tableFromInternalSource(dataset, emissionProcessIdentifier);
    }

    private String emissionRecordsTable(Dataset dataset) {
        String emissionRecordsIdentifier = "emission records";
        return tableFromInternalSource(dataset, emissionRecordsIdentifier);
    }

    private String tableFromInternalSource(Dataset dataset, String identifier) {
        InternalSource[] sources = dataset.getInternalSources();
        for (int i = 0; i < sources.length; i++) {
            String type = sources[i].getType().toLowerCase();
            if (type.indexOf(identifier) != -1) {
                return sources[i].getTable();
            }
        }
        return null;
    }

    private void table(Datasource emissionDatasource, Dataset dataset) throws Exception {
        String summaryTable = dataset.getSummarySource().getTable();
        TableDefinition tableDefinition = emissionDatasource.tableDefinition();
        if (tableDefinition.tableExists(summaryTable)) {
            throw new ImporterException("Table '" + summaryTable
                    + "' already exists. Must either overwrite table or choose new name.");
        }
    }

}

package gov.epa.emissions.commons.io.nif.point;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.io.importer.DatasetLoader;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.SummaryTable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NIFPointSummary implements SummaryTable {

    private Dataset dataset;

    private Datasource emissionDatasource;

    private Datasource referenceDatasource;

    public NIFPointSummary(Datasource emission, Datasource reference, Dataset dataset) {
        this.emissionDatasource = emission;
        this.referenceDatasource = reference;
        this.dataset = dataset;
    }

    public void createSummary() throws ImporterException {
        DatasetLoader loader = new DatasetLoader(dataset);
        dataset.setSummarySource(loader.summarySource());

        String emTable = emissionDatasource.getName() + "." + emissionRecordsTable(dataset);
        String epTable = emissionDatasource.getName() + "." + emissionProcessTable(dataset);
        String erTable = emissionDatasource.getName() + "." + emissionReleaseTable(dataset);
        String euTable = emissionDatasource.getName() + "." + emissionUnitTable(dataset);
        String summaryTable = emissionDatasource.getName() + "." + dataset.getSummarySource().getTable();

        try {
            table(emissionDatasource, dataset);
            ResultSet rs = emissionDatasource.query().executeQuery("SELECT DISTINCT(pollutant_code ) FROM " + emTable);
            rs.last();
            int numOfPollutants = rs.getRow();
            rs.first();
            String[] pollutants = new String[numOfPollutants];

            String selectPart = "";
            String joinPart = "";
            String pollutant;

            for (int i = 0; i < numOfPollutants; i++) {
                pollutants[i] = rs.getString("pollutant_code");
                pollutant = pollutants[i].replace('-', '_');
                selectPart = selectPart + pollutant + ".emission_value as " + pollutant + ", ";
                joinPart = joinPart + "LEFT JOIN (SELECT " + "state_county_fips" + ", " + "state_facility_id" + ", "
                        + "emission_unit_id" + ", " + "emission_process_id" + ", " + "emission_point_id" + ","
                        + "emission_value" + " FROM " + emTable + " WHERE " + "pollutant_code" + " = '" + pollutants[i]
                        + "') " + pollutant + " ON (e." + "state_county_fips" + " = " + pollutant + "."
                        + "state_county_fips" + " AND e." + "state_facility_id" + " = " + pollutant + "."
                        + "state_facility_id" + " AND e." + "emission_unit_id" + " = " + pollutant + "."
                        + "emission_unit_id" + " AND e." + "emission_process_id" + " = " + pollutant + "."
                        + "emission_process_id" + " AND e." + "emission_point_id" + " = " + pollutant + "."
                        + "emission_point_id" + ") ";
                rs.next();
            }
            rs.close();

            selectPart = selectPart.substring(0, selectPart.length() - 2);

            String query = "CREATE TABLE " + summaryTable + " AS SELECT DISTINCT f." + "state_abbr" + " as " + "State"
                    + ", " + "e." + "state_county_fips" + " as " + "FIPS" + ", e." + "state_facility_id" + " as "
                    + "Facility" + ", e." + "emission_unit_id" + " as " + "Unit" + ", e." + "emission_process_id" + " as "
                    + "Process" + ", e." + "emission_point_id" + " as " + "Point" + ", ep." + "scc" + " as " + "SCC"
                    + ", ep." + "mact_code" + " as " + "MACT" + ", eu." + "unit_sic_code" + " as " + "SIC" + ", eu."
                    + "unit_naics_code" + " as " + "NAICS" + ", er." + "exit_gas_temp" + " as " + "Exit_Temp" + ", er."
                    + "exit_gas_velocity" + " as " + "Exit_Vel" + ", er." + "exit_gas_flow_rate" + " as "
                    + "Exit_Flow_Rate" + ", er." + "stack_height" + " as " + "Height" + ", er." + "stack_diameter" + " as "
                    + "Diameter" + ", er." + "x_coordinate" + " as " + "X_Coord" + ", er." + "y_coordinate" + " as "
                    + "Y_Coord" + ", " + selectPart + " FROM " + referenceDatasource.getName() + ".fips as f, " + epTable
                    + " as ep, " + euTable + " as eu, " + erTable + " as er, " + "(SELECT DISTINCT " + "state_county_fips"
                    + ", " + "state_facility_id" + ", " + "emission_unit_id" + ", " + "emission_process_id" + ", "
                    + "emission_point_id" + " FROM " + emTable + " ) e " + joinPart + " WHERE (e." + "state_county_fips"
                    + " = f." + "state_county_fips" + " AND f.country_code='US') AND (e." + "state_county_fips" + " = ep."
                    + "state_county_fips" + " AND e." + "state_facility_id" + " = ep." + "state_facility_id" + " AND e."
                    + "emission_unit_id" + " = ep." + "emission_unit_id" + " AND e." + "emission_process_id" + " = ep."
                    + "emission_process_id" + " AND e." + "emission_point_id" + " = ep." + "emission_point_id"
                    + ") AND (e." + "state_county_fips" + " = eu." + "state_county_fips" + " AND e." + "state_facility_id"
                    + " = eu." + "state_facility_id" + " AND e." + "emission_unit_id" + " = eu." + "emission_unit_id"
                    + ") AND (e." + "state_county_fips" + " = er." + "state_county_fips" + " AND e." + "state_facility_id"
                    + " = er." + "state_facility_id" + " AND e." + "emission_point_id" + " = er." + "emission_point_id"
                    + ")";

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

    private String emissionUnitTable(Dataset dataset) {
        String emissionProcessIdentifier = "emission unit";
        return tableFromInternalSource(dataset, emissionProcessIdentifier);
    }

    private String emissionReleaseTable(Dataset dataset) {
        String emissionProcessIdentifier = "emission release";
        return tableFromInternalSource(dataset, emissionProcessIdentifier);
    }

    private String tableFromInternalSource(Dataset dataset, String identifier) {
        // FIXME: this is not a good way to identify the table
        InternalSource[] sources = dataset.getInternalSources();
        for (int i = 0; i < sources.length; i++) {
            String type = sources[i].getType().toLowerCase();
            if (type.indexOf(identifier) != -1) {
                return sources[i].getTable();
            }
        }
        return null;
    }

    private void table(Datasource emissionDatasource, Dataset dataset) throws SQLException, Exception {
        String summaryTable = dataset.getSummarySource().getTable();
        TableDefinition tableDefinition = emissionDatasource.tableDefinition();
        if (tableDefinition.tableExists(summaryTable)) {
            throw new Exception("Table '" + summaryTable
                    + "' already exists. Must either overwrite table or choose new name.");
        }
    }

}

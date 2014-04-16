package gov.epa.emissions.commons.io.speciation;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.generic.GenericExporter;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SpeciationComboProfileExporter extends GenericExporter {

    public SpeciationComboProfileExporter(Dataset dataset, String rowFilters, DbServer dbServer, 
            Integer optimizedBatchSize) {
        super(dataset, rowFilters, dbServer, new SpeciationComboProfileFileFormat(dbServer.getSqlDataTypes()), optimizedBatchSize);
    }

    public SpeciationComboProfileExporter(Dataset dataset, String rowFilters, DbServer dbServer,
            DataFormatFactory factory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        super(dataset, rowFilters, dbServer, new SpeciationComboProfileFileFormat(dbServer.getSqlDataTypes()), factory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
    }

    // NOTE: overwrite so that trailing blank columns are truncated
    protected void writeDataCols(String[] cols, ResultSet data, PrintWriter writer) throws SQLException {
        int endCol = cols.length - 1;
        int colCheckNum = startColNumber - 1;
        boolean continueCheck = true;
        String toWrite = "";

        for (int i = endCol; i > colCheckNum; i--) {
            String value = formatValue(i, data);

            if (continueCheck && (value == null || value.isEmpty()))
                continue;
            
            continueCheck = false;
            toWrite = value + (toWrite.isEmpty() ? "" : delimiter + toWrite);
        }

        writer.write(toWrite);
    }

}

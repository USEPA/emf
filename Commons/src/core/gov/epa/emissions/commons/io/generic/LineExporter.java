package gov.epa.emissions.commons.io.generic;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LineExporter extends GenericExporter {

    public LineExporter(Dataset dataset, String rowFilters, DbServer dbServer, Integer optimizedBatchSize) {
        super(dataset, rowFilters, dbServer, new LineFileFormat(dbServer.getSqlDataTypes()), optimizedBatchSize);
    }

    public LineExporter(Dataset dataset, String rowFilters, DbServer dbServer, DataFormatFactory formatFactory,
            Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        super(dataset, rowFilters, dbServer, new LineFileFormat(dbServer.getSqlDataTypes()), formatFactory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
    }

    protected void writeDataCols(String[] cols, ResultSet data, PrintWriter writer) throws SQLException {
        writer.write(data.getString(startColNumber));
    }

    protected int startCol(String[] cols) {
        if (isTableVersioned(cols))
            return 6; // shifted by "Obj_Id", "Record_Id",
        // "Dataset_Id", "Version", "Delete_Versions", "Line_Number"

        return 3; // shifted by "Obj_Id", "Record_Id", "Line_Number"
    }

}

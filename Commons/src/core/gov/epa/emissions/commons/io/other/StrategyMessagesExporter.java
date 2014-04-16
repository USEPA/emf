package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.generic.GenericExporter;

import java.io.PrintWriter;
import java.sql.SQLException;

public class StrategyMessagesExporter extends GenericExporter {
    public StrategyMessagesExporter(Dataset dataset, String rowFilters, DbServer dbServer, Integer optimizedBatchSize) {
        super(dataset, rowFilters, dbServer, new StrategyMessagesFileFormat(dbServer.getSqlDataTypes()), optimizedBatchSize);
        setup();
    }
    
    public StrategyMessagesExporter(Dataset dataset, String rowFilters, DbServer dbServer, 
            DataFormatFactory factory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        super(dataset, rowFilters, dbServer, new StrategyMessagesFileFormat(dbServer.getSqlDataTypes()), factory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
        setup();
    }
    
    private void setup(){
        this.setDelimiter(",");
    }
    
    protected void writeHeaders(PrintWriter writer, Dataset dataset) throws SQLException {
        writer.print(dataset.getDescription());
        printExportInfo(writer);
    }
       
}

package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.generic.GenericExporter;

public class DaySpecPointInventoryExporter extends GenericExporter {
    
    public DaySpecPointInventoryExporter(Dataset dataset, String rowFilters, DbServer dbServer, Integer optimizedBatchSize) {
        super(dataset, rowFilters, dbServer, new DaySpecPointInventoryFileFormat(dbServer.getSqlDataTypes()), optimizedBatchSize);
        super.setDelimiter("");
    }
    
    public DaySpecPointInventoryExporter(Dataset dataset, String rowFilters, DbServer dbServer, 
            DataFormatFactory factory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        super(dataset, rowFilters, dbServer, new DaySpecPointInventoryFileFormat(dbServer.getSqlDataTypes()), factory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
        super.setDelimiter("");
    }
}

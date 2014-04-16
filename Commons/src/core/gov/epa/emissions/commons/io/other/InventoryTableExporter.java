package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.generic.GenericExporter;

public class InventoryTableExporter extends GenericExporter {
    
    public InventoryTableExporter(Dataset dataset, String rowFilters, DbServer dbServer, Integer optimizedBatchSize) {
        super(dataset, rowFilters, dbServer, new InventoryTableFileFormat(dbServer.getSqlDataTypes(), 1), optimizedBatchSize);
        setDelimiter("");
    }
    
    public InventoryTableExporter(Dataset dataset, String rowFilters, DbServer dbServer, 
            DataFormatFactory factory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        super(dataset, rowFilters, dbServer, new InventoryTableFileFormat(dbServer.getSqlDataTypes(), 1), factory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
        setDelimiter("");
    } 
    
    protected int startCol(String[] cols) {
        if (isTableVersioned(cols))
            return 6; //shifted by "Obj_Id", "Record_Id", 
                      //"Dataset_Id", "Version", "Delete_Versions", "Line_Number"

        return 3; //shifted by "Obj_Id", "Record_Id", "Line_Number"
    }
}

package gov.epa.emissions.commons.io.spatial;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.generic.GenericExporter;

public class SpatialSurrogatesExporter extends GenericExporter {
    public SpatialSurrogatesExporter(Dataset dataset, String rowFilters, DbServer dbServer, Integer optimizedBatchSize) {
        super(dataset, rowFilters, dbServer, new SpatialSurrogatesFileFormat(dbServer.getSqlDataTypes()), optimizedBatchSize);
    }
    
    public SpatialSurrogatesExporter(Dataset dataset, String rowFilters, DbServer dbServer, 
            DataFormatFactory factory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        super(dataset, rowFilters, dbServer, new SpatialSurrogatesFileFormat(dbServer.getSqlDataTypes()), factory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
    }

}

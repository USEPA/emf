package gov.epa.emissions.commons.io.temporal;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.io.other.CountryStateCountyDataExporter;

import java.io.PrintWriter;

public class TemporalProfileExporter extends CountryStateCountyDataExporter {
    
    
    public TemporalProfileExporter(Dataset dataset, String rowFilters, DbServer dbServer, Integer optimizedBatchSize) {
        this(dataset, rowFilters, dbServer,new NonVersionedDataFormatFactory(),optimizedBatchSize, null, null, null);
    }

    public TemporalProfileExporter(Dataset dataset, String rowFilters, DbServer dbServer, 
            DataFormatFactory dataFormatFactory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        super(dataset, rowFilters, dbServer, dataFormatFactory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
    }
    
    protected void writeData(PrintWriter writer, Dataset dataset, Datasource datasource, boolean comments) throws Exception {
        InternalSource[] sources = dataset.getInternalSources();

        for(int i = 0; i < sources.length; i++){
            String sectionName = sources[i].getTable().replace('_', ' ');
            writer.println("/" + sectionName + "/");
            this.fileFormat = getFileFormat(sectionName);
            writeResultSet(writer, sources[i], datasource, comments, sectionName);
            writer.println("/END/");
        }
    }
    
    protected FileFormat getFileFormat(String fileFormatName) {
        TemporalFileFormatFactory factory = new TemporalFileFormatFactory(types);

        return factory.get(fileFormatName);
    }
    
}

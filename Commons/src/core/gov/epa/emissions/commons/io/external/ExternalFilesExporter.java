package gov.epa.emissions.commons.io.external;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;

import java.io.File;

public class ExternalFilesExporter implements Exporter {

    private ExternalSource[] srcs;
    
    private int count = 0;

    public ExternalFilesExporter(Dataset dataset, String rowFilters, DbServer dbServer, Integer optimizedBatchSize) {
        this(dataset, rowFilters, dbServer, new NonVersionedDataFormatFactory(), optimizedBatchSize, null, null, null);
    }

    public ExternalFilesExporter(Dataset dataset, String rowFilters, DbServer dbServer,
            DataFormatFactory formatFactory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        //no-op
    }
    
    //NOTE: this must be called before export() is called to instantiate the external sources list.
    public void setExternalSources(ExternalSource[] srcs) {
        this.srcs = srcs;
    }

    public void export(File file) throws ExporterException {
        verifyExistance(srcs);
    }

    private void verifyExistance(ExternalSource[] srcs) throws ExporterException {
        if (srcs == null)
            throw new ExporterException("External sources not specified.");
        
        for (int i = 0; i < srcs.length; i++) {
            String fileName = srcs[i].getDatasource();
            
            if (!new File(fileName).exists())
                throw new ExporterException("The file " + fileName + " doesn't exist.");
            
            this.count = i + 1;
        }
        
    }

    public long getExportedLinesCount() {
        return count; // index starts 0
    }

}

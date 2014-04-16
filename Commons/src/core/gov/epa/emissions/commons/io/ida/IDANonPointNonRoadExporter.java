package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.File;

public class IDANonPointNonRoadExporter implements Exporter {

    private IDAExporter delegate;

    public IDANonPointNonRoadExporter(Dataset dataset, String rowFilters, DbServer dbServer,
            Integer optimizedBatchSize) throws ImporterException {
        delegate = new IDAExporter(dataset, rowFilters, dbServer, fileFormat(dbServer.getSqlDataTypes()), optimizedBatchSize);
    }

    public IDANonPointNonRoadExporter(Dataset dataset, String rowFilters, DbServer dbServer,
            DataFormatFactory dataFormatFactory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) throws ImporterException {
        delegate = new IDAExporter(dataset, rowFilters, dbServer, fileFormat(dbServer.getSqlDataTypes()), dataFormatFactory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
    }

    private IDAFileFormat fileFormat(SqlDataTypes sqlDataTypes) {
        return new IDANonPointNonRoadFileFormat(sqlDataTypes);
    }

    public void export(File file) throws ExporterException {
        delegate.export(file);

    }

    public long getExportedLinesCount() {
        return delegate.getExportedLinesCount();
    }

}

package gov.epa.emissions.commons.io.external;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.FilesFromPattern;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.File;

public abstract class AbstractExternalFilesImporter implements Importer {

    protected String importerName;
    
    protected ExternalSource[] extSrc;

    private File[] files;

    private Dataset dataset;

    public AbstractExternalFilesImporter(File folder, String[] filePatterns, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataType) throws ImporterException {
        this(folder, filePatterns, dataset, dbServer, sqlDataType, null);
    }

    public AbstractExternalFilesImporter(File folder, String[] filePatterns, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes, DataFormatFactory factory) throws ImporterException {
        this.dataset = dataset;
        files = new FilesFromPattern(folder, filePatterns, dataset).files();
        importerName = "Abstract External Files Importer";
    }

    public void run() {
        extSrc = new ExternalSource[files.length];

        for (int i = 0; i < files.length; i++) {
            extSrc[i] = new ExternalSource(files[i].getAbsolutePath());
            extSrc[i].setListindex(i);
            extSrc[i].setDatasetId(dataset.getId());
        }
    }
    
    public ExternalSource[] getExternalSources() {
        return extSrc;
    }

}

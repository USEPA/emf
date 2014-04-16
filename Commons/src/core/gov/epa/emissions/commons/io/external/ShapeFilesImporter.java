package gov.epa.emissions.commons.io.external;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.File;

public class ShapeFilesImporter extends AbstractExternalFilesImporter {

    public ShapeFilesImporter(File folder, String[] filePatterns, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes) throws ImporterException {
        this(folder, filePatterns, dataset, dbServer, sqlDataTypes, null);
    }

    public ShapeFilesImporter(File folder, String[] filePatterns, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes, DataFormatFactory factory) throws ImporterException {
        super(folder, filePatterns, dataset, dbServer, sqlDataTypes, factory);
        importerName = "Shape Files Importer";
    }
}

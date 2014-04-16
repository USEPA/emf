package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetTypeUnit;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.NonVersionedTableFormat;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.File;

public class ORLMergedImporter implements Importer {

    private ORLImporter delegate;

    public ORLMergedImporter(File folder, String[] filePatterns, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes) throws ImporterException {
        FileFormatWithOptionalCols fileFormat = new ORLMergedFileFormat(sqlDataTypes);
        TableFormat tableFormat = new NonVersionedTableFormat(fileFormat, sqlDataTypes);

        create(folder, filePatterns, dataset, dbServer, fileFormat, tableFormat);
    }

    public ORLMergedImporter(File folder, String[] filePatterns, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes, DataFormatFactory factory) throws ImporterException {
        FileFormatWithOptionalCols fileFormat = new ORLMergedFileFormat(sqlDataTypes, factory.defaultValuesFiller());
        TableFormat tableFormat = factory.tableFormat(fileFormat, sqlDataTypes);

        create(folder, filePatterns, dataset, dbServer, fileFormat, tableFormat);
    }

    private void create(File folder, String[] filePatterns, Dataset dataset, DbServer dbServer,
            FileFormatWithOptionalCols fileFormat, TableFormat tableFormat) throws ImporterException {
        DatasetTypeUnit formatUnit = new DatasetTypeUnit(tableFormat, fileFormat);
        delegate = new ORLImporter(folder, filePatterns, dataset, formatUnit, dbServer.getEmissionsDatasource());
    }

    public void run() throws ImporterException {
        delegate.run();
        
        delegate.postRun();
    }

}

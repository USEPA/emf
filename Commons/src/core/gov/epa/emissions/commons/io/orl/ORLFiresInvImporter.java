package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetTypeUnit;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.NonVersionedTableFormat;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.FillDefaultValues;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.File;

public class ORLFiresInvImporter implements Importer {

    private ORLImporter delegate;

    public ORLFiresInvImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes) throws ImporterException {
        FileFormatWithOptionalCols fileFormat = fileFormat(sqlDataTypes);
        TableFormat tableFormat = new NonVersionedTableFormat(fileFormat(sqlDataTypes), sqlDataTypes);

        create(folder, filenames, dataset, dbServer, fileFormat, tableFormat);
    }

    public ORLFiresInvImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes, DataFormatFactory factory) throws ImporterException {
        FileFormatWithOptionalCols fileFormat = fileFormat(sqlDataTypes, factory.defaultValuesFiller());
        TableFormat tableFormat = factory.tableFormat(fileFormat, sqlDataTypes);

        create(folder, filenames, dataset, dbServer, fileFormat, tableFormat);
    }

    private FileFormatWithOptionalCols fileFormat(SqlDataTypes sqlDataTypes, FillDefaultValues filler) {
        return new ORLFiresInvFileFormat(sqlDataTypes, filler);
    }

    private void create(File folder, String[] filePatterns, Dataset dataset, DbServer dbServer,
            FileFormatWithOptionalCols fileFormat, TableFormat tableFormat) throws ImporterException {
        DatasetTypeUnit formatUnit = new DatasetTypeUnit(tableFormat, fileFormat);
        delegate = new ORLImporter(folder, filePatterns, dataset, formatUnit, dbServer.getEmissionsDatasource());
    }

    private FileFormatWithOptionalCols fileFormat(SqlDataTypes sqlDataTypes) {
        return new ORLFiresInvFileFormat(sqlDataTypes);
    }

    public void run() throws ImporterException {
        delegate.run();
    }
}

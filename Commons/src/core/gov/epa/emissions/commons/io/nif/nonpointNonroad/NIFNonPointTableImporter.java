package gov.epa.emissions.commons.io.nif.nonpointNonroad;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.nif.NIFTableImporter;

public class NIFNonPointTableImporter implements Importer {

    private NIFTableImporter delegate;

    public NIFNonPointTableImporter(String[] tables, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes)
            throws ImporterException {
        this(tables, dataset, dbServer, sqlDataTypes, new NonVersionedDataFormatFactory());
    }

    public NIFNonPointTableImporter(String[] tables, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes,
            DataFormatFactory factory) throws ImporterException {
        NIFNonPointTableDatasetTypeUnits units = new NIFNonPointTableDatasetTypeUnits(tables, dbServer, sqlDataTypes,
                factory);
        delegate = new NIFTableImporter(tables, dataset, units, dbServer);
    }

    public void run() throws ImporterException {
        delegate.run();
    }
}

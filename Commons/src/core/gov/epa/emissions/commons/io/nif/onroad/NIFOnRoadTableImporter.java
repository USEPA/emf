package gov.epa.emissions.commons.io.nif.onroad;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.io.nif.NIFTableImporter;

public class NIFOnRoadTableImporter implements Importer {

    private NIFTableImporter delegate;

    public NIFOnRoadTableImporter(String[] tables, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes)
            throws ImporterException {
        this(tables, dataset, dbServer, sqlDataTypes, new NonVersionedDataFormatFactory());
    }

    public NIFOnRoadTableImporter(String[] tables, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes, 
            DataFormatFactory factory) throws ImporterException {
        delegate = new NIFTableImporter(tables, dataset, new NIFOnRoadTableDatasetTypeUnits(tables, dbServer, sqlDataTypes, 
                factory), dbServer);
    }

    public void run() throws ImporterException {
        delegate.run();
    }

}

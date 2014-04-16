package gov.epa.emissions.commons.io.nif.nonpointNonroad;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.io.nif.NIFTableImporter;

public class NIFNonRoadTableImporter implements Importer {

    private NIFTableImporter delegate;

    public NIFNonRoadTableImporter(String[] tables, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes) 
            throws ImporterException {
        this(tables, dataset, dbServer, sqlDataTypes, new NonVersionedDataFormatFactory());
    }
    
    public NIFNonRoadTableImporter(String[] tables, Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes,
            DataFormatFactory factory) throws ImporterException {
        delegate = new NIFTableImporter(tables, dataset, new NIFNonRoadTableDatasetTypeUnits(tables, dbServer, sqlDataTypes,
                factory), dbServer);
    }

    public void run() throws ImporterException {
        delegate.run();
    }

    public InternalSource[] internalSources() {
        return delegate.internalSources();
    }

}

package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.data.Dataset;

public interface DataLoader {

    void load(Reader reader, Dataset dataset, String table) throws ImporterException;

}
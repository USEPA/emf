package gov.epa.emissions.commons.io.importer;


public interface Importer {
    /**
     * Imports a file into database
     * For inventory files, table has to be created based on the dataset name
     * For ancilliary files importer will expect the tables to be there
     */
    void run() throws ImporterException;

}

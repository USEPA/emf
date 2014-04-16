package gov.epa.emissions.commons.io.importer;


public interface ImporterPostProcess {
    /**
     * Includes a place to run post processes after the import process
     */
    void postRun() throws ImporterException;

}

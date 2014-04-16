package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.framework.services.EmfException;

public class CMImportInputRules {

    public void validate(String directory, String[] files) throws EmfException {
        validateDirectory(directory);
        validateFiles(files);
        checkForMinAndMaxFiles(files.length);
    }

    private void checkForMinAndMaxFiles(int size) throws EmfException {
        if (size > 8 || size < 4)
            throw new EmfException("The control measure importer requires between 3 and 6 files");
    }

    private void validateFiles(String[] files) throws EmfException {
        if (files == null || files.length == 0) {
            throw new EmfException("A Filename should be specified");
        }

    }

    private void validateDirectory(String directory) throws EmfException {
        if (directory == null || directory.trim().length() == 0) {
            throw new EmfException("A Folder should be specified");
        }

    }

}

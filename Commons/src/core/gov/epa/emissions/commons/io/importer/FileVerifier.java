package gov.epa.emissions.commons.io.importer;

import java.io.File;

public class FileVerifier {

    public void shouldExist(File file) throws ImporterException {
        if (!file.exists()) {
            throw new ImporterException("The file '" + file + "' does not exist");
        }

        if (!file.isFile()) {
            throw new ImporterException("The file '" + file + "' is not a file");
        }
    }

    public void shouldHaveOneFile(String[] filePatterns) throws ImporterException {
        if (filePatterns.length > 1) {
            throw new ImporterException("Too many parameters. Requires only one file.");
        }
        if (filePatterns[0].length() == 0) {
            throw new ImporterException("Requires a filename");
        }
    }

}

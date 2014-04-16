package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;

import java.io.File;

public class FilesFromPattern {

    private File[] files;

    public FilesFromPattern(File folder, String[] filePatterns,Dataset dataset) throws ImporterException{
        DatasetType datasetType = dataset.getDatasetType();
        if (filePatterns[0].length() == 0) {
            throw new ImporterException("No file pattern or filename is specified");
        }

        int minFiles = datasetType.getMinFiles();
        files = extractFileNames(folder, filePatterns);
        if (files.length < minFiles) {
            throw new ImporterException(datasetType.getName()+ " importer requires " + minFiles + " files");
        } 
    }

    public File[] files(){
        return files;
    }

    private File[] extractFileNames(File folder, String[] filePattern) throws ImporterException {
        if (filePattern == null || filePattern.length == 0) {
            throw new ImporterException("There are no files found in the directory '" + folder.getAbsolutePath());
        }
        
        File[] files = new File[filePattern.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(folder, filePattern[i]);
        }
        return files;
    }
}

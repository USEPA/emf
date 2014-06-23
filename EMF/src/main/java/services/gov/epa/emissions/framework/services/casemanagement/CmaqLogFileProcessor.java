package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.framework.services.EmfException;

import java.io.File;
import java.util.List;

public class CmaqLogFileProcessor {

    private File logFile;
    private List<String> varNames;

    public CmaqLogFileProcessor(String filePath) {
        logFile = new File(filePath);
    }
    
    public List<String> getVariables() throws EmfException {
        processFile(this.logFile);
        
        return varNames;
    }

    private void processFile(File logFile) throws EmfException {
        // NOTE Auto-generated method stub
        throw new EmfException("under construction...");
    }
}

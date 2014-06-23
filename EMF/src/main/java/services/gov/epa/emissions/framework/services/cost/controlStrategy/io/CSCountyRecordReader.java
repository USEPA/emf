package gov.epa.emissions.framework.services.cost.controlStrategy.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;

public class CSCountyRecordReader {

    private int fipsColIndex;
    
    public CSCountyRecordReader(CSCountyFileFormat fileFormat) {
        //
    }

    public String parse(Record record, int lineNo) throws ImporterException {
        try {
            return getFips(record.getTokens(), lineNo);
        } catch (ImporterException e) {
            throw e;
        }
    }

    public void setFIPsColIndex(int index) {
        fipsColIndex = index;
    }

    private String getFips(String[] tokens, int lineNo) throws ImporterException {
        if (tokens.length - 1 < fipsColIndex) 
            throw new ImporterException("Missing FIPs column.");
        
        String fips = tokens[fipsColIndex].replaceAll("\"", "");

        return fips;
    }

}
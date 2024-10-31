package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.CoSTConstants;
import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.csv.CSVReader;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

public class CMSummaryImporter {

    private File file;

    private CMSummaryRecordReader cmSummaryRecord;

    private EntityManagerFactory entityManagerFactory;

    private User user;
    
    private ArrayList abbreviations;
    
    private boolean forScan = false;
    
    
    public CMSummaryImporter(File file, CMFileFormat fileFormat, User user,
            EntityManagerFactory entityManagerFactory) {
        this.file = file;
        this.user = user;
        this.entityManagerFactory = entityManagerFactory;
        this.abbreviations = new ArrayList();
        cmSummaryRecord = new CMSummaryRecordReader(fileFormat, user, entityManagerFactory);
    }

    public void run(Map controlMeasures) throws ImporterException, FileNotFoundException {
        addStatus("Started reading Summary file");

        CSVReader reader = new CSVReader(new FileReader( file));
        //read the first header line...
        reader.read();
        for (Record record = reader.read(); reader.hasNext(); record = reader.read()) {
            ControlMeasure cm = cmSummaryRecord.parse(record, reader.lineNumber());
            if (cm != null)
            {
                if (abbreviations.contains(cm.getAbbreviation()))
                    addStatus("Error: Duplicate abbreviation: "+cm.getAbbreviation());
                
                if (cm.getAbbreviation().length()>CoSTConstants.CM_ABBREV_LEN) //10)
                    addStatus("Error: abbreviation exceeds " + CoSTConstants.CM_ABBREV_LEN + " characters: "+cm.getAbbreviation());
                
                controlMeasures.put(cm.getAbbreviation(), cm);
                abbreviations.add(cm.getAbbreviation());
            }    
        }

        if (cmSummaryRecord.getErrorCount() > 0) {
            addStatus("Failed to import control measure records, " + cmSummaryRecord.getErrorCount() + " errors were found.");
            throw new ImporterException("Failed to import control measure records, " + cmSummaryRecord.getErrorCount() + " errors were found.");
        }
        addStatus("Finished reading Summary file");
        forScan = false;
    }

    private void addStatus(String message) {
        if ( !forScan) {
            setStatus(message);
        }
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImportDetailMsg");
        endStatus.setMessage(message + "\n");
        endStatus.setTimestamp(new Date());

        new StatusDAO(entityManagerFactory).add(endStatus);
    }

    public void setForScan(boolean forScan) {
        this.forScan = forScan;
    }

    public boolean isForScan() {
        return forScan;
    }

}

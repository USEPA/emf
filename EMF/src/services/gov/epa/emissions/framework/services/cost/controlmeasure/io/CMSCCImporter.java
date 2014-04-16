package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.Date;
import java.util.Map;

public class CMSCCImporter{

    private File file;
    
    private CMSCCRecordReader sccReader;

    private User user;

    private HibernateSessionFactory sessionFactory;
    
    public CMSCCImporter(File file, CMSCCsFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) {
        this.file = file;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.sccReader = new CMSCCRecordReader(fileFormat, user, sessionFactory);
    }

    public void run(Map controlMeasures) throws ImporterException {
        addStatus("Started reading SCC file");
        CMCSVFileReader reader = new CMCSVFileReader(file);
        for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
            sccReader.parse(controlMeasures,record, reader.lineNumber());
        }
        if (sccReader.getErrorCount() > 0) {
            addStatus("Failed to import control measure SCC records, " + sccReader.getErrorCount() + " errors were found.");
            throw new ImporterException("Failed to import control measure SCC records, " + sccReader.getErrorCount() + " errors were found.");
        }
        addStatus("Finished reading SCC file");
    }
    
    private void addStatus(String message) {
        setStatus(message);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImportDetailMsg");
        endStatus.setMessage(message + "\n");
        endStatus.setTimestamp(new Date());

        new StatusDAO(sessionFactory).add(endStatus);
    }

}

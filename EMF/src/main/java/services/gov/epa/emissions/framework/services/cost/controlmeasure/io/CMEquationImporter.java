package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.Date;
import java.util.Map;

public class CMEquationImporter{

    private File file;
    
    private CMEquationRecordReader equationReader;

    private User user;

    private HibernateSessionFactory sessionFactory;
    
    public CMEquationImporter(File file, CMEquationFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) throws EmfException {
        this.file = file;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.equationReader = new CMEquationRecordReader(fileFormat, user, sessionFactory);
    }

    public void run(Map controlMeasures) throws ImporterException {
        addStatus("Started reading Equation Variable file");
        CMCSVFileReader reader = new CMCSVFileReader(file);
        for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
            equationReader.parse(controlMeasures, record, 
                    reader.lineNumber());
        }
        if (equationReader.getErrorCount() > 0) {
            addStatus("Failed to import control measure Equation records, " + equationReader.getErrorCount() + " errors were found.");
            throw new ImporterException("Failed to import control measure Equation records, " + equationReader.getErrorCount() + " errors were found.");
        }
        addStatus("Finished reading Equation Variable file");
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

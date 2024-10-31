package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.csv.CSVReader;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

public class CMPropertyImporter{

    private File file;
    
    private CMPropertyRecordReader propertyReader;

    private User user;

    private EntityManagerFactory entityManagerFactory;
    
    public CMPropertyImporter(File file, CMPropertyFileFormat fileFormat, 
            User user, EntityManagerFactory entityManagerFactory) {
        this.file = file;
        this.user = user;
        this.entityManagerFactory = entityManagerFactory;
        this.propertyReader = new CMPropertyRecordReader(fileFormat, user, entityManagerFactory);
    }

    public void run(Map controlMeasures) throws ImporterException, FileNotFoundException {
        addStatus("Started reading properties file");
        CSVReader reader = new CSVReader(new FileReader( file));
        //read the first header line...
        reader.read();
        for (Record record = reader.read(); reader.hasNext(); record = reader.read()) {
            propertyReader.parse(controlMeasures, record, 
                    reader.lineNumber());
        }
        if (propertyReader.getErrorCount() > 0) {
            addStatus("Failed to import control measure property records, " + propertyReader.getErrorCount() + " errors were found.");
            throw new ImporterException("Failed to import control measure property records, " + propertyReader.getErrorCount() + " errors were found.");
        }
        addStatus("Finished reading properties file");
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

        new StatusDAO(entityManagerFactory).add(endStatus);
    }

}

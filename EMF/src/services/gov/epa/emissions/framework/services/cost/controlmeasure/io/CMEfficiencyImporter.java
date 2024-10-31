package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTableReader;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

public class CMEfficiencyImporter {

    private File file;

    private CMEfficiencyRecordReader cmEfficiencyReader;

    private User user;

    private EntityManagerFactory entityManagerFactory;

    private CMCSVFileReader reader;
    
    private boolean end;
    
    private int recordParseCount;

    public CMEfficiencyImporter(File file, CMFileFormat fileFormat, User user,
            EntityManagerFactory entityManagerFactory, DbServer dbServer) throws EmfException {
        this.file = file;
        this.user = user;
        this.entityManagerFactory = entityManagerFactory;
        CostYearTable costYearTable = null;
        try {
            CostYearTableReader reader = new CostYearTableReader(dbServer, CostYearTable.REFERENCE_COST_YEAR);
            costYearTable = reader.costYearTable();
            costYearTable.setTargetYear(CostYearTable.REFERENCE_COST_YEAR);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } 
        this.cmEfficiencyReader = new CMEfficiencyRecordReader(fileFormat, user, entityManagerFactory, costYearTable);
    }

//    public void run(Map controlMeasures) throws ImporterException {
//        addStatus("Start reading Efficiency file");
//        if (reader == null ) reader = new CMCSVFileReader(file);
//        for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
//            cmEfficiencyReader.parse(controlMeasures, record, reader.lineNumber());
//        }
//        addStatus("Finished reading Efficiency file");
//    }

    public EfficiencyRecord[] parseEfficiencyRecords(Map controlMeasures) throws ImporterException {
        int recordCount = 0;
        List effRecs = new ArrayList();
        if (recordParseCount == 0) addStatus("Start reading Efficiency file");
        if (reader == null ) reader = new CMCSVFileReader(file);
        for (Record record = reader.read(); !(end = record.isEnd()); record = reader.read()) {
            EfficiencyRecord efficiencyRecord;
            try {
                efficiencyRecord = cmEfficiencyReader.parseEfficiencyRecord(controlMeasures, record, reader.lineNumber());
            } catch (EmfException e) {
                throw new ImporterException(e.getMessage());
            }
            if (efficiencyRecord != null) {
                effRecs.add(efficiencyRecord);
                recordParseCount++;
                recordCount++;
                //process 20000 at a time...
                if (recordCount == 20000) break;
            }
        }
        if (cmEfficiencyReader.getErrorCount() > 0) {
            addStatus("Failed to import control measure efficiency records, " + cmEfficiencyReader.getErrorCount() + " errors were found.");
            throw new ImporterException("Failed to import control measure efficiency records, " + cmEfficiencyReader.getErrorCount() + " errors were found.");
        }
        addStatus("Finished reading segment of Efficiency file - " + recordParseCount + " lines read, saving efficiency records to the database");
        return (EfficiencyRecord[]) effRecs.toArray(new EfficiencyRecord[0]);
    }

    public boolean isEnd() {
        return end;
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

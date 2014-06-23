package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;

public class CMImporters {

    private File[] files;

    private Record[] records;

    private CMSummaryImporter summaryImporter;

    private CMEfficiencyImporter efficiencyImporter;

    private CMSCCImporter sccImporter;

    private CMReferenceImporter referenceImporter;

    private CMEquationImporter equationImporter;

    private CMPropertyImporter propertyImporter;
    
    private HibernateSessionFactory sessionFactory;

    private User user;
    
    private DbServer dbServer;

    public CMImporters(File[] files, Record[] records, User user, HibernateSessionFactory sessionFactory, DbServer dbServer) throws EmfException {
        this.files = files;
        this.records = records;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServer = dbServer;
        summaryImporter = createSummaryImporter();
        efficiencyImporter = createEfficiencyImporter();
        sccImporter = createSCCImporter();
        referenceImporter = createReferenceImporter();
        equationImporter = createEquationImporter();
        propertyImporter = createPropertyImporter();
    }

    public CMSummaryImporter summaryImporter() {
        return summaryImporter;
    }

    public CMEfficiencyImporter efficiencyImporter() {
        return efficiencyImporter;
    }

    public CMSCCImporter sccImporter() {
        return sccImporter;
    }

    public CMReferenceImporter referenceImporter() {
        return referenceImporter;
    }

    public CMEquationImporter equationImporter() {
        return equationImporter;
    }

    public CMPropertyImporter propertyImporter() {
        return propertyImporter;
    }

    private CMSummaryImporter createSummaryImporter() throws EmfException {
        CMSummaryFileFormat fileFormat = new CMSummaryFileFormat();
        CMSummaryFileFormatv2 fileFormatv2 = new CMSummaryFileFormatv2();
        String[] cols = fileFormat.cols();
        String[] colsv2 = fileFormatv2.cols();
        for (int i = 0; i < records.length; i++) {
            if (matches(cols, records[i].getTokens())) {
                return new CMSummaryImporter(files[i], fileFormat, user, sessionFactory);
            } else if (matches(colsv2, records[i].getTokens())) {
                return new CMSummaryImporter(files[i], fileFormatv2, user, sessionFactory); 
            }
        }

        throw new EmfException("Failed to import control measures: Control Measure Summary file is required, the file is missing or has the wrong format, expected header format: " + getHeaderFormat(cols));
    }

    private CMEfficiencyImporter createEfficiencyImporter() throws EmfException {
        CMEfficiencyFileFormat fileFormat = new CMEfficiencyFileFormat();
        CMEfficiencyFileFormatv2 fileFormatv2 = new CMEfficiencyFileFormatv2();
        CMEfficiencyFileFormatv3 fileFormatv3 = new CMEfficiencyFileFormatv3();
        String[] cols = fileFormat.cols();
        String[] colsv2 = fileFormatv2.cols();
        String[] colsv3 = fileFormatv3.cols();
        for (int i = 0; i < records.length; i++) {
            if (matches(cols, records[i].getTokens())) {
                return new CMEfficiencyImporter(files[i], fileFormat, user, sessionFactory, dbServer);
            } else if (matches(colsv2, records[i].getTokens())){
                return new CMEfficiencyImporter(files[i], fileFormatv2, user, sessionFactory, dbServer); 
            }else if (matches(colsv3, records[i].getTokens())) {
                return new CMEfficiencyImporter(files[i], fileFormatv3, user, sessionFactory, dbServer);
            }
        }
        throw new EmfException("Failed to import control measures: Control Measure Efficiency file is required, the file is missing or has the wrong format, expected header format: " + getHeaderFormat(colsv3));

    }

    private CMSCCImporter createSCCImporter() throws EmfException {
        CMSCCsFileFormat fileFormat = new CMSCCsFileFormat();
        String[] cols = fileFormat.cols();
        for (int i = 0; i < records.length; i++) {
            if (matches(cols, records[i].getTokens())) {
                return new CMSCCImporter(files[i], fileFormat, user, sessionFactory);
            }
        }

        throw new EmfException("Failed to import control measures: Control Measure SCC file is required, the file is missing or has the wrong format, expected header format: " + getHeaderFormat(cols));

    }

    private CMEquationImporter createEquationImporter() throws EmfException {
        CMEquationFileFormat fileFormat = new CMEquationFileFormat();
        String[] cols = fileFormat.cols();
        for (int i = 0; i < records.length; i++) {
            if (matches(cols, records[i].getTokens())) {
                return new CMEquationImporter(files[i], fileFormat, user, sessionFactory);
            }
        }
        return null;
        //don't throw an error this file is optional...
//        throw new EmfException("Failed to import control measures: Control Measure Equation file is required, the file is missing or has the wrong format, expected header format: " + getHeaderFormat(cols));
    }

    private CMPropertyImporter createPropertyImporter() {
        CMPropertyFileFormat fileFormat = new CMPropertyFileFormat();
        String[] cols = fileFormat.cols();
        for (int i = 0; i < records.length; i++) {
            if (matches(cols, records[i].getTokens())) {
                return new CMPropertyImporter(files[i], fileFormat, user, sessionFactory);
            }
        }
        return null;
        //don't throw an error this file is optional...
//        throw new EmfException("Failed to import control measures: Control Measure Equation file is required, the file is missing or has the wrong format, expected header format: " + getHeaderFormat(cols));
    }

    private CMReferenceImporter createReferenceImporter() {
        
        CMReferenceFileFormat fileFormat = new CMReferenceFileFormat();
        String[] cols = fileFormat.cols();
        for (int i = 0; i < records.length; i++) {
            if (matches(cols, records[i].getTokens())) {
                return new CMReferenceImporter(files[i], fileFormat, this.user, this.sessionFactory);
            }
        }
        return null;
    }

    private String getHeaderFormat(String[] cols) {
        String header = cols[0];
        for (int i = 1; i < cols.length; i++) {
            header += "," + cols[i];
        }
        return header;
    }
    
    private boolean matches(String[] cols, String[] tokens) {
        if (cols.length != tokens.length)
            return false;

        for (int i = 0; i < cols.length; i++) {
            if (!cols[i].equalsIgnoreCase(tokens[i]))
                return false;
        }

        return true;
    }

}

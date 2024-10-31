package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

import java.io.File;
import java.io.IOException;

import javax.persistence.EntityManagerFactory;

public class ControlMeasuresImportIdentifier {

    private File[] files;

    private EntityManagerFactory entityManagerFactory;

    private User user;
    private DbServer dbServer;

    public ControlMeasuresImportIdentifier(File[] files, User user, EntityManagerFactory entityManagerFactory, DbServer dbServer) {
        this.files = files;
        this.user = user;
        this.entityManagerFactory = entityManagerFactory;
        this.dbServer = dbServer;
        this.entityManagerFactory = entityManagerFactory;
    }

    public CMImporters cmImporters() throws EmfException {
        Record[] records = new Record[files.length];
        for (int i = 0; i < files.length; i++) {
            records[i] = firstRecord(files[i]);
        }
        return new CMImporters(files, records, user, entityManagerFactory, dbServer);

    }

    private Record firstRecord(File file) throws EmfException {
        CMCSVFileReader fileReader = null;
        try {
            fileReader = new CMCSVFileReader(file);
            return new Record(fileReader.getCols());
        } catch (ImporterException e) {
            throw new EmfException("Could not read file: " + file.getAbsolutePath());
        } finally {
            close(file, fileReader);
        }
    }

    private void close(File file, CMCSVFileReader fileReader) throws EmfException {
        try {
            if (fileReader != null)
                fileReader.close();
        } catch (IOException e) {
            throw new EmfException("Could not close file: " + file.getAbsolutePath());
        }
    }

}

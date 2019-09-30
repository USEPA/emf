package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImporterFactory {
    private static Log log = LogFactory.getLog(ImporterFactory.class);

    private DbServer newDBInstance;

    private SqlDataTypes sqlDataTypes;
    
    public ImporterFactory() throws Exception {
       this(DbServerFactory.get());
    }
    
    public ImporterFactory(DbServerFactory dbServerFactory) throws Exception {
        this.newDBInstance = new EmfDbServer(dbServerFactory);
    }
    
    public ImporterFactory(DbServer dbServer) throws Exception {
        this.newDBInstance = dbServer;
     }

    public Importer createVersioned(EmfDataset dataset, File folder, String[] fileNames) throws Exception {
        return createVersioned(dataset, folder, fileNames, 0);
    }

    public Importer createVersioned(EmfDataset dataset, File folder, String[] fileNames, int targetVersion) throws Exception {
        this.sqlDataTypes = newDBInstance.getSqlDataTypes();
        
        Importer importer = create(dataset, folder, fileNames);
        return new VersionedImporter(importer, dataset, newDBInstance, lastModifiedDate(folder, fileNames), targetVersion);
    }

    private Date lastModifiedDate(File folder, String[] fileNames) {
        long mostLastModified = -1;
        for (int i = 0; i < fileNames.length; i++) {
            long lastModified = new File(folder, fileNames[i]).lastModified();
            if (lastModified > mostLastModified)
                mostLastModified = lastModified;
        }
        return new Date(mostLastModified);
    }

    private Importer create(EmfDataset dataset, File folder, String[] filePatterns) throws ImporterException {
        try {
            return doCreate(dataset, folder, filePatterns);
        } catch (ClassNotFoundException e) {
            log.error("Failed to create importer.", e);
            throw new ImporterException("Importer class name not found (either from database or commons.jar)--" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to create importer: " + e.getMessage(), e);
            throw new ImporterException(e.getMessage());
        }
    }

    private Importer doCreate(EmfDataset dataset, File folder, String[] fileNames) throws Exception {
        String importerClassName = dataset.getDatasetType().getImporterClassName();
        
        if (importerClassName == null || importerClassName.trim().isEmpty())
            throw new Exception("Importer class name not specified with this dataset type.");
        
        Class importerClass = Class.forName(importerClassName);

        Class[] classParams = new Class[] { File.class, String[].class, Dataset.class, DbServer.class,
                SqlDataTypes.class, DataFormatFactory.class };
        Object[] params = new Object[] { folder, fileNames, dataset, newDBInstance, sqlDataTypes,
                new VersionedDataFormatFactory(null, dataset) };

        Constructor importerConstructor = importerClass.getDeclaredConstructor(classParams);
        
        try {
            Importer theImporter = (Importer) importerConstructor.newInstance(params);
            return theImporter;
        }
        catch (java.lang.reflect.InvocationTargetException ite)
        {
            Throwable cause = ite.getCause();
            if (cause != null)
            {
                throw new Exception(cause.getMessage());
            }       
        } 
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        
        return null;
    }
    
    public void closeDbConnection() throws Exception {
        if ((this.newDBInstance != null) && (this.newDBInstance.isConnected())) 
            this.newDBInstance.disconnect();
    }
}

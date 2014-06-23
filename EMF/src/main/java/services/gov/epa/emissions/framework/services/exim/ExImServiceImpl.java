package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfServiceImpl;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.FileDownloadDAO;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.Date;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

public class ExImServiceImpl extends EmfServiceImpl implements ExImService {
    private static int svcCount = 0;

    private String svcLabel = null;

    public String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    private ManagedImportService managedImportService;

    private ManagedExportService exportService;
    
    private FileDownloadDAO fileDownloadDAO;
    
    public ExImServiceImpl() throws Exception {
        this(DbServerFactory.get(), HibernateSessionFactory.get());
    }

    protected void finalize() throws Throwable {
        svcCount--;
        if (DebugLevels.DEBUG_4())
            System.out.println(">>>> Destroying object: " + myTag());
        super.finalize();
    }

    public ExImServiceImpl(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) throws Exception {
        if (DebugLevels.DEBUG_4())
            System.out.println(myTag());
        init(dbServerFactory, sessionFactory);
        myTag();
    }

    private void init(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        setProperties(sessionFactory);
        this.fileDownloadDAO = new FileDownloadDAO(sessionFactory);
        exportService = new ManagedExportService(dbServerFactory, sessionFactory);
        managedImportService = new ManagedImportService(dbServerFactory, sessionFactory);
    }

    private void setProperties(HibernateSessionFactory sessionFactory) {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty batchSize = new EmfPropertiesDAO().getProperty("export-batch-size", session);
            EmfProperty eximTempDir = new EmfPropertiesDAO().getProperty("ImportExportTempDir", session);

            if (eximTempDir != null)
                System.setProperty("IMPORT_EXPORT_TEMP_DIR", eximTempDir.getValue());

            if (batchSize != null)
                System.setProperty("EXPORT_BATCH_SIZE", batchSize.getValue());
        } finally {
            session.close();
        }
    }

    public void exportDatasets(User user, EmfDataset[] datasets, Version[] versions, String dirName, String prefix,
            boolean overwrite, String rowFilters, EmfDataset filterDataset,
            Version filterDatasetVersion, String filterDatasetJoinCondition, String colOrders, String purpose) throws EmfException {
        if (DebugLevels.DEBUG_4())
            System.out.println(">>## calling export datasets in eximSvcImp: " + myTag() + " for datasets: "
                    + datasets.toString());
        String submitterId;
//        if (rowFilters.isEmpty() && colOrders.isEmpty())
//            submitterId = exportService.exportForClient(user, datasets, versions, dirName, purpose, overwrite);
//        else
        submitterId = exportService.exportForClient(user, datasets, versions, dirName, 
                    prefix, rowFilters, filterDataset,
                    filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose, overwrite);
        if (DebugLevels.DEBUG_4())
            System.out.println("In ExImServiceImpl:exportDatasets() SUBMITTERID= " + submitterId);
        if (DebugLevels.DEBUG_21())
            System.out.println("rowFilters: "+rowFilters+" colOrders: "+colOrders);
    }

    public void downloadDatasets(User user, EmfDataset[] datasets, Version[] versions, String dirName, String prefix,
            boolean overwrite, String rowFilters, EmfDataset filterDataset,
            Version filterDatasetVersion, String filterDatasetJoinCondition, String colOrders, String purpose) throws EmfException {
        if (DebugLevels.DEBUG_4())
            System.out.println(">>## calling export datasets in eximSvcImp: " + myTag() + " for datasets: "
                    + datasets.toString());
        String submitterId;
//        if (rowFilters.isEmpty() && colOrders.isEmpty())
//            submitterId = exportService.exportForClient(user, datasets, versions, dirName, purpose, overwrite);
//        else
        submitterId = exportService.downloadForClient(user, datasets, versions, dirName, 
                    prefix, rowFilters, filterDataset,
                    filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose);
        if (DebugLevels.DEBUG_4())
            System.out.println("In ExImServiceImpl:exportDatasets() SUBMITTERID= " + submitterId);
        if (DebugLevels.DEBUG_21())
            System.out.println("rowFilters: "+rowFilters+" colOrders: "+colOrders);
    }

    public void importDatasets(User user, String folderPath, String[] filenames, DatasetType datasetType) throws EmfException {
        try {
            String submitterID = managedImportService.importDatasetsForClient(user, folderPath, filenames, datasetType);
            if (DebugLevels.DEBUG_4())
                System.out.println("In ExImServiceImpl:importDatasets() SUBMITTERID = " + submitterID);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public void importDataset(User user, String folderPath, String[] filenames, DatasetType datasetType,
            String datasetName) throws EmfException {
        try {
            String submitterID = managedImportService.importDatasetForClient(user, folderPath, filenames, datasetType, datasetName);
            if (DebugLevels.DEBUG_4())
                System.out.println("In ExImServiceImpl:importDataset() SUBMITTERID = " + submitterID);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public String[] getFilenamesFromPattern(String folder, String pattern) throws EmfException {
        return managedImportService.getFilenamesFromPattern(folder, pattern);
    }

    public Version getVersion(Dataset dataset, int version) throws EmfException {
        return exportService.getVersion(dataset, version);
    }

    public void exportDatasetids(User user, Integer[] datasetIds, Version[] versions, String folder, String prefix,
            boolean overwrite, String rowFilters, EmfDataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition,
            String colOrders, String purpose) throws EmfException {
        int numOfDS = datasetIds.length;
        EmfDataset[] datasets = new EmfDataset[numOfDS];
        DataServiceImpl ds = new DataServiceImpl();

        // Sift through and choose only those whose id matches
        // one of the list of dataset ids in the array.
        /** * this has caused the sequence of the retrieved datasets is different from the original one in datasetIds ** */

        // for (int i=0; i<rawDatasets.length;i++){
        // if (dsetIds.contains(new Integer (rawDatasets[i].getId()))){
        // rawDatasets[i].setAccessedDateTime(new Date());
        // ar.add(rawDatasets[i]);
        // }

        try {
            for (int i = 0; i < numOfDS; i++)
                datasets[i] = ds.getDataset(new Integer(datasetIds[i]));

            // if Vservion[] is not specified, get the default versions from datasets themselves
            if (versions == null) {
                Version[] defaultVersions = new Version[numOfDS];

                for (int j = 0; j < numOfDS; j++)
                    defaultVersions[j] = getVersion(datasets[j], datasets[j].getDefaultVersion());

                exportDatasets(user, datasets, defaultVersions, folder, prefix, overwrite, rowFilters, filterDataset, filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose);
                return;
            }

            // Invoke the local method that uses the datasets
            exportDatasets(user, datasets, versions, folder, prefix, overwrite, rowFilters, filterDataset, filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose);
        } catch (RuntimeException e) {
            // NOTE Auto-generated catch block
            //e.printStackTrace();
            throw new EmfException("Error exporting dataset. " + e.getMessage());
        }
    }

    public void exportDatasetids(User user, Integer[] datasetIds, String folder, 
            boolean overwrite, String rowFilters, String colOrders, String purpose) throws EmfException {
        // if Vservion[] is not specified, get the default versions from datasets themselves
        if (DebugLevels.DEBUG_4())
            System.out.println("ExImService:exportDatasetids() called.");
        exportDatasetids(user, datasetIds, null, folder, null, overwrite, rowFilters, null, null, null, colOrders, purpose);
        if (DebugLevels.DEBUG_4())
            System.out.println("ExImService:exportDatasetids() exited.");
    }

    // Export with overwrite using default dataset version

//    public void exportDatasetidsWithOverwrite(User user, Integer[] datasetIds, String folder, String purpose)
//            throws EmfException {
//        exportDatasetidsWithOverwrite(user, datasetIds, null, folder, purpose);
//    }

    public String printStatusExportTaskManager() throws EmfException {
        return exportService.printStatusExportTaskManager() ;
    }

    public String printStatusImportTaskManager() throws EmfException {
        return managedImportService.printStatusImportTaskManager() ;
    }

    public void downloadDatasets(User user, Integer[] datasetIds, Version[] versions, String prefix, String rowFilters,
            EmfDataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition,
            String colOrders, String purpose) throws EmfException {
        
//        exportDatasetids(user, datasetIds, versions, , prefix, true, rowFilters, filterDataset, filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose);
        int numOfDS = datasetIds.length;
        EmfDataset[] datasets = new EmfDataset[numOfDS];
        DataServiceImpl ds = new DataServiceImpl();

        // Sift through and choose only those whose id matches
        // one of the list of dataset ids in the array.
        /** * this has caused the sequence of the retrieved datasets is different from the original one in datasetIds ** */

        // for (int i=0; i<rawDatasets.length;i++){
        // if (dsetIds.contains(new Integer (rawDatasets[i].getId()))){
        // rawDatasets[i].setAccessedDateTime(new Date());
        // ar.add(rawDatasets[i]);
        // }

        try {
            for (int i = 0; i < numOfDS; i++)
                datasets[i] = ds.getDataset(new Integer(datasetIds[i]));

            // if Vservion[] is not specified, get the default versions from datasets themselves
            if (versions == null) {
                Version[] defaultVersions = new Version[numOfDS];

                for (int j = 0; j < numOfDS; j++)
                    defaultVersions[j] = getVersion(datasets[j], datasets[j].getDefaultVersion());

                downloadDatasets(user, datasets, defaultVersions, this.fileDownloadDAO.getDownloadExportFolder() + "/" + user.getUsername() + "/", prefix, true, rowFilters, filterDataset, filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose);
                return;
            }

            // Invoke the local method that uses the datasets
            downloadDatasets(user, datasets, versions, this.fileDownloadDAO.getDownloadExportFolder() + "/" + user.getUsername() + "/", prefix, true, rowFilters, filterDataset, filterDatasetVersion, filterDatasetJoinCondition, colOrders, purpose);
        } catch (RuntimeException e) {
            // NOTE Auto-generated catch block
            //e.printStackTrace();
            throw new EmfException("Error exporting dataset. " + e.getMessage());
        }
    }
}

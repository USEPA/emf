package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.external.ExternalFilesExporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.FileDownloadDAO;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.UserDao;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.EmfPropertyDao;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.spring.AppConfig;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.ExportTaskManager;
import gov.epa.emissions.framework.tasks.Task;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Conrad F. D'Cruz
 * 
 */
@Service("exportTask")
@Scope("prototype")
public class ExportTask extends Task implements IExportTask {

//    private ServletRequestAttributes attributes;
    
    private ExportTaskService exportTaskService;

    private Services services;

    @Autowired
    public void setExportTaskService(ExportTaskService exportTaskService) {
        this.exportTaskService = exportTaskService;
    }

    @PostConstruct
    public void init() {
        // Grab current thread local request attributes.
        // These are available because we are not in the new 
        // thread yet.
//        attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
//        this.emfPropertyDao = (EmfPropertyDao) context.getBean("emfPropertyDao");
//        this.fileDownloadDAO = (FileDownloadDAO) context.getBean("fileDownloadDAO");
        log.info("init");
    }

    @Override
    public boolean isEquivalent(Task task) {
        ExportTask etsk = (ExportTask) task;
        boolean eq = false;
        if (this.file.getAbsolutePath().equals(etsk.getFile().getAbsolutePath())) {
            eq = true;
        }

        // NOTE Auto-generated method stub
        return eq;
    }

    private static Log log = LogFactory.getLog(ExportTask.class);

    private File file;

    private LoggingServiceImpl loggingService;

    private EmfDataset dataset;

    private DatasetType type;

    private AccessLog accesslog;

    private HibernateSessionFactory sessionFactory;

    private Version version;

    private ExternalSource[] extSrcs;

    private DbServerFactory dbFactory;

    private int sleepAfterExport = 0;
    
    private String colOrders="";
     
    private String rowFilters="";

    private DatasetDAO datasetDao;
    
    private EmfDataset filterDataset;

    private Version filterDatasetVersion;

    private String filterDatasetJoinCondition;

    private boolean download;

    private FileDownloadDAO fileDownloadDAO;

    @Autowired
    public void setFileDownloadDAO(final FileDownloadDAO fileDownloadDAO) {
        this.fileDownloadDAO = fileDownloadDAO;
    }
    
    private EmfPropertyDao emfPropertyDao;
    
    @Autowired
    public void setEmfPropertyDao(EmfPropertyDao emfPropertyDao) {
        this.emfPropertyDao = emfPropertyDao;
    }

    private UserDao userDao;
    
    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ExportTask() {
        //
    }
    
    protected ExportTask(User user, File file, EmfDataset dataset, Services services, AccessLog accesslog, // BUG3589 need to know where the task constructed in case job
            DbServerFactory dbFactory, HibernateSessionFactory sessionFactory, Version version) {
        super();
        createId();
        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> " + createId());
        this.user = user;
        this.file = file;
        this.dataset = dataset;
        this.services = services;
        this.type = dataset.getDatasetType();
        this.statusServices = services.getStatus();
        this.loggingService = services.getLoggingService();
        this.dbFactory = dbFactory;
        this.accesslog = accesslog;
        this.sessionFactory = sessionFactory;
//        this.fileDownloadDAO = new FileDownloadDAO(sessionFactory);
        this.version = version;
        this.datasetDao = new DatasetDAO();
//        this.exportTaskService = new ExportTaskServiceImpl(user, file, dataset, services, accesslog, rowFilters, colOrders, dbFactory, sessionFactory, version, dataset, version, filterDatasetJoinCondition, download, filterDatasetJoinCondition, colOrders);
    }
    
    protected ExportTask(User user, File file, EmfDataset dataset, Services services, AccessLog accesslog,
            String rowFilters, String colOrders,
            DbServerFactory dbFactory, HibernateSessionFactory sessionFactory, Version version, EmfDataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        this(user, file, dataset, services, accesslog, dbFactory, sessionFactory, version);
        this.rowFilters = rowFilters;
        this.colOrders = colOrders;
        this.filterDataset = filterDataset;
        this.filterDatasetVersion = filterDatasetVersion;
        this.filterDatasetJoinCondition = filterDatasetJoinCondition;
    }

    protected ExportTask(User user, File file, EmfDataset dataset, Services services, AccessLog accesslog,
            String rowFilters, String colOrders,
            DbServerFactory dbFactory, HibernateSessionFactory sessionFactory, Version version, EmfDataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition, boolean download) {
        this(user, file, dataset, services, accesslog, rowFilters, colOrders, dbFactory, sessionFactory, version, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
        this.download = download;
    }

    @Override
    public void init(User user, File file, EmfDataset dataset, Services services, AccessLog accesslog,
            String rowFilters, String colOrders,
            DbServerFactory dbFactory, HibernateSessionFactory sessionFactory, Version version, EmfDataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition, boolean download) {
        createId();
        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> " + createId());
        this.user = user;
        this.file = file;
        this.dataset = dataset;
        this.type = dataset.getDatasetType();
        this.services = services;
        this.statusServices = services.getStatus();
        this.loggingService = services.getLoggingService();
        this.dbFactory = dbFactory;
        this.accesslog = accesslog;
        this.sessionFactory = sessionFactory;
//        this.fileDownloadDAO = new FileDownloadDAO(sessionFactory);
        this.version = version;
        this.datasetDao = new DatasetDAO();

        this.rowFilters = rowFilters;
        this.colOrders = colOrders;
        this.filterDataset = filterDataset;
        this.filterDatasetVersion = filterDatasetVersion;
        this.filterDatasetJoinCondition = filterDatasetJoinCondition;

        this.download = download;
    } 

    public void run() {
        log.info("start run");
        exportTaskService.init(user, file, dataset, services, accesslog, rowFilters, colOrders, dbFactory, sessionFactory, version, filterDataset, filterDatasetVersion, filterDatasetJoinCondition, download, this.taskId, this.submitterId);
        exportTaskService.run();
        if ( 1 == 1 ) return;
//        RequestContextHolder.setRequestAttributes(attributes);
        //try something
//        log.info("this.userDao.all().size() = " + this.userDao.all().size()) ;
        
        DbServer dbServer = null;
        Session session = sessionFactory.getSession();
        this.sleepAfterExport = sleepAfterExport(session);
        extSrcs = getExternalSrcs(session);

        if (DebugLevels.DEBUG_1())
            System.out.println(">>## ExportTask:run() " + createId() + " for datasetId: " + this.dataset.getId());
        if (DebugLevels.DEBUG_1())
            System.out.println("Task#" + taskId + " RUN @@@@@ THREAD ID: " + Thread.currentThread().getId());

        if (DebugLevels.DEBUG_1())
            if (DebugLevels.DEBUG_1())
                System.out.println("Task# " + taskId + " running");
        
        Date start = new Date();

        try {
            setStartStatus();
            accesslog.setTimestamp(start);
            long exportedLineCount = 0;
            accesslog.setFolderPath(file.getAbsolutePath());

            if (file.exists()) {
                setStatus("completed", "FILE EXISTS: Completed export of " + dataset.getName() + " to "
                        + file.getAbsolutePath() + " in " + accesslog.getTimereqrd() + " seconds."
                        + (download ? "  The file will start downloading momentarily, see the Download Manager for the download status." : ""));

            } else {
                dbServer = this.dbFactory.getDbServer();
                VersionedExporterFactory exporterFactory = new VersionedExporterFactory(dbServer, dbServer
                        .getSqlDataTypes(), batchSize(session));
                Exporter exporter = exporterFactory.create(dataset, version, rowFilters, colOrders, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);

                if (exporter instanceof ExternalFilesExporter)
                    ((ExternalFilesExporter) exporter).setExternalSources(extSrcs);

                exporter.export(file);

                exportedLineCount = exporter.getExportedLinesCount();
                String lineCompare=compareDatasetRecordsNumbers(exportedLineCount, session, dbServer);
                if (DebugLevels.DEBUG_1())
                    printLogInfo(accesslog);               
                if (exportedLineCount == 0){
                    throw new Exception("ERROR: "+dataset.getName()+
                            " will not be exported because no records satisfied the filter " );
                }

                accesslog.setEnddate(new Date());
                accesslog.setLinesExported(exportedLineCount);

                String msghead = "Completed export of " + dataset.getName();
                String msgend = " in " + accesslog.getTimereqrd() + " seconds.";
 
                if (type.getExporterClassName().endsWith("ExternalFilesExporter")) {
                    System.out.println(msghead + msgend);
                    setStatus("completed", msghead + msgend);
                    accesslog.setFolderPath("");
                } else{
                    System.out.println(msghead + " to " + file.getAbsolutePath() + msgend + "\n"+ lineCompare );
                    setStatus("completed", msghead + " to " + file.getAbsolutePath() + msgend + (download ? "  The file will start downloading momentarily, see the Download Manager for the download status." : "") + "\n"+ lineCompare );
                }
                
                // for bug 3589
                if ( !DebugLevels.DEBUG_24()) {
                    // NOTE: want to check if accesslog exists for the same dataset, version, and description.
                    // If it is there, don't set accesslog.

                    String query = "SELECT obj.id from " + AccessLog.class.getSimpleName() + " obj WHERE obj.datasetId = "
                    + accesslog.getDatasetId() + " AND obj.version = '" + accesslog.getVersion() + "' "
                    + "AND obj.description = '" + accesslog.getDescription() + "'";
                    List<?> list = session.createQuery(query).list();

                    if (list == null || list.size() == 0) {
                        loggingService.setAccessLog(accesslog);
                    }
                }
                
            } // else of if file exists

            //if they request a download then queue it up so the client's
            //download manager will pick up the new download request...
            if (download) {
                String username = user.getUsername();
                log.info("fileDownloadDAO.add");
                //lets add a filedownload item for the user, so they can download the file
                fileDownloadDAO.add(user, new Date(), 
                        /*getDownloadExportFolder() + */file.getAbsolutePath(),
                        getDownloadExportRootURL() + "/" + username + "/" + file.getName(), //http://localhost:8080/exports
                        "Dataset Export", false);
            }

            if ( DebugLevels.DEBUG_24()) {
                // NOTE: want to check if accesslog exists for the same dataset, version, and description.
                // If it is there, don't set accesslog.

                String query = "SELECT obj.id from " + AccessLog.class.getSimpleName() + " obj WHERE obj.datasetId = "
                + accesslog.getDatasetId() + " AND obj.version = '" + accesslog.getVersion() + "' "
                + "AND obj.description = '" + accesslog.getDescription() + "'";
                List<?> list = session.createQuery(query).list();

                if (list == null || list.size() == 0) {
                    loggingService.setAccessLog(accesslog);
                }
            }

            if (DebugLevels.DEBUG_4())
                System.out.println("#### Task #" + taskId
                        + " has completed processing making the callback to ExportTaskManager THREAD ID: "
                        + Thread.currentThread().getId());
        } catch (Exception e) {
            if (file.exists())
                file.delete();
            setErrorStatus(e, "");
        } finally {
            try {
                //We want to record the dataset access time anyways
                dataset.setAccessedDateTime(start);
                session.clear();
                updateDataset(dataset, session);
                
                // check for isConnected before disconnecting
                if ((dbServer != null) && (dbServer.isConnected()))
                    dbServer.disconnect();

                if (session != null && session.isConnected())
                    session.close();

                if (this.sleepAfterExport > 0) {
                    Thread.sleep(this.sleepAfterExport * 1000);
                    log.warn("ExportTask sleeps " + sleepAfterExport + " seconds after export.");
                }
            } catch (Exception e) {
                log.error("Error closing db connections.", e);
            }
        }
//        RequestContextHolder.resetRequestAttributes();
   }

    @Override
    public String[] quickRunExternalExport() {
        DbServer dbServer = null;
        Session session = sessionFactory.getSession();
        extSrcs = getExternalSrcs(session);
        String[] sts = new String[2];

        if (DebugLevels.DEBUG_1())
            System.out.println(">>## ExportTask:run() " + createId() + " for datasetId: " + this.dataset.getId());
        if (DebugLevels.DEBUG_1())
            System.out.println("Task#" + taskId + " RUN @@@@@ THREAD ID: " + Thread.currentThread().getId());

        if (DebugLevels.DEBUG_1())
            if (DebugLevels.DEBUG_1())
                System.out.println("Task# " + taskId + " running");

        Date start = new Date();
        
        try {
            accesslog.setTimestamp(start);
            long exportedLineCount = 0;
            accesslog.setFolderPath("");

            dbServer = this.dbFactory.getDbServer();
            VersionedExporterFactory exporterFactory = new VersionedExporterFactory(dbServer, dbServer
                    .getSqlDataTypes(), batchSize(session));
            Exporter exporter = exporterFactory.create(dataset, version, rowFilters, colOrders, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);

            if (exporter instanceof ExternalFilesExporter)
                ((ExternalFilesExporter) exporter).setExternalSources(extSrcs);

            exporter.export(file);

            exportedLineCount = exporter.getExportedLinesCount();

            if (DebugLevels.DEBUG_1())
                printLogInfo(accesslog);

            accesslog.setEnddate(new Date());
            accesslog.setLinesExported(exportedLineCount);

            String msghead = "Completed export of " + dataset.getName();
            String msgend = " in " + accesslog.getTimereqrd() + " seconds.";

            sts[0] = "completed";
            sts[1] = msghead + msgend;

            // NOTE: want to check if accesslog exists for the same dataset, version, and description.
            // If it is there, don't set accesslog.

            String query = "SELECT obj.id from " + AccessLog.class.getSimpleName() + " obj WHERE obj.datasetId = "
                    + accesslog.getDatasetId() + " AND obj.version = '" + accesslog.getVersion() + "' "
                    + "AND obj.description = '" + accesslog.getDescription() + "'";
            List<?> list = session.createQuery(query).list();

            if (list == null || list.size() == 0) {
                loggingService.setAccessLog(accesslog);
            }

            return sts;
        } catch (Exception e) {
            sts[0] = "failed";
            sts[1] = "Export failure. " + ((e == null) ? "" : e.getMessage());
            return sts;
        } finally {
            try {
              //We want to record the dataset access time anyways
                dataset.setAccessedDateTime(start);
                session.clear();
                updateDataset(dataset, session);
                
                // check for isConnected before disconnecting
                if ((dbServer != null) && (dbServer.isConnected()))
                    dbServer.disconnect();

                if (session != null && session.isConnected())
                    session.close();
            } catch (Exception e) {
                log.error("Error closing db connections.", e);
            }
        }
    }

    @Override
    public boolean fileExists() {
        return file != null && file.exists() && !type.isExternal();
    }

    @Override
    public boolean isExternal() {
        return type.isExternal();
    }
    
    private void updateDataset(EmfDataset ds, Session session) throws Exception {
        datasetDao.updateDSPropNoLocking(ds, session);
    }

    private ExternalSource[] getExternalSrcs(Session session) {
        return datasetDao.getExternalSrcs(dataset.getId(), -1, null, session);
    }

    private void printLogInfo(AccessLog log) {
        String info = "Exported dataset: " + log.getDatasetname() + "; version: " + log.getVersion() + "; start date: "
                + log.getTimestamp() + "; end date: " + log.getEnddate() + "; time required (seconds): "
                + log.getTimereqrd() + "; user: " + log.getUsername() + "; path: " + log.getFolderPath()
                + "; details: " + log.getDetails();
        System.out.println(info);
        // setStatus(info);
    }

    private String compareDatasetRecordsNumbers(long linesExported, Session session, DbServer dbServer)
            throws Exception {
        DatasetType type = dataset.getDatasetType();
        String importerclass = (type == null ? "" : type.getImporterClassName());
        importerclass = (importerclass == null ? "" : importerclass.trim());

        if (importerclass.equals("gov.epa.emissions.commons.io.temporal.TemporalProfileImporter")
                || importerclass.equals("gov.epa.emissions.commons.io.other.CountryStateCountyDataImporter"))
            return "";

        long records = 0;

        try {
            records = datasetDao.getDatasetRecordsNumber(dbServer, session, dataset, version);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error determining number of records: " + e.getMessage());
        }

        if (records != linesExported)
            return "No. of records in database: " + records + "; " + "Exported: " +linesExported;

        return "No. of records exported: " + linesExported;
    }

    private void setErrorStatus(Exception e, String message) {
        if (log != null && file != null && e != null) {
            log.error("Problem attempting to export file : " + file + " " + message+ " Criteria: "+rowFilters +"", e);
        } else if (e != null) {
            if (message != null)
                System.out.println("Message = " + message);
            e.printStackTrace();
        }
        setStatus("failed", "Export failure. " + message + ((e == null) ? "" : e.getMessage()) + (rowFilters.length()>0? "\nCriteria: "+rowFilters :""));
    }

    private void setStartStatus() {
        if (type.getExporterClassName().endsWith("ExternalFilesExporter"))
            setStatus("started", "Started exporting " + dataset.getName());
        else
            setStatus("started", "Started exporting " + dataset.getName() + " to " + file.getAbsolutePath());
    }

    private void setStatus(String statusMessage, String message) {
//        Status status = new Status();
//        status.setUsername(user.getUsername());
//        status.setType("Export");
//        status.setMessage(message);
//        status.setTimestamp(new Date());
//
//        this.statusServices.add(status);
//        this.exportTaskManager.callBackFromThread(taskId, this.submitterId, statusMessage, Thread.currentThread().getId(), message);
    }

    @Override
    protected void finalize() throws Throwable {
        taskCount--;
        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> Destroying object: " + createId());
        super.finalize();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public EmfDataset getDataset() {
        return dataset;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    private int batchSize(Session session) throws Exception {
        try {
            String batchSize = System.getProperty("EXPORT_BATCH_SIZE");

            if (batchSize != null)
                return Integer.parseInt(batchSize);

            EmfProperty property = new EmfPropertiesDAO().getProperty("export-batch-size", session);
            return Integer.parseInt(property.getValue());
        } catch (Exception e) {
            log.error("Error getting batch size for export. ", e);
            throw new Exception(e.getMessage());
        }

    }

    private int sleepAfterExport(Session session) {
        int value = 2;

        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("SECONDS_TO_WAIT_AFTER_EXPORT", session);
            value = Integer.parseInt(property.getValue());
        } catch (Exception e) {
            return value; // Default value for maxpool and poolsize
        }

        return value;
    }

    @Override
    public String getPropertyValue(String name) {
        EmfProperty property = emfPropertyDao.getProperty(name);
        return property != null ? property.getValue() : null;
    }

    @Override
    public String getDownloadExportFolder() {
        return getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_FOLDER);
    }

    @Override
    public String getDownloadExportRootURL() {
        return getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_ROOT_URL);
    }
}

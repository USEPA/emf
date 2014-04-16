package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.FileDownload;
import gov.epa.emissions.framework.services.basic.FileDownloadDAO;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

public class ExportQAStepTask implements Runnable {

    private QAStep qastep;

    private User user;

    private StatusDAO statusDao;
    
    private FileDownloadDAO fileDownloadDao;

    private Log log = LogFactory.getLog(ExportQAStepTask.class);

    private File file;

    private HibernateSessionFactory sessionFactory;
    
    private DbServerFactory dbServerFactory;

    private QAStepResult result;
    
    private String dirName;
    
    private String fileName;
    
    private boolean overide;
    
    private boolean verboseStatusLogging = true;

    private boolean download = false;

    private String rowFilter;

    public ExportQAStepTask(String dirName, String fileName, 
            boolean overide, QAStep qaStep, 
            User user, HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.dirName = dirName;
        this.fileName = fileName;
        this.overide = overide;
        this.qastep = qaStep;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        this.statusDao = new StatusDAO(sessionFactory);
        this.fileDownloadDao = new FileDownloadDAO(sessionFactory);
    }

    public ExportQAStepTask(String dirName, String fileName, 
            boolean overide, QAStep qaStep, 
            User user, HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory, boolean verboseStatusLogging, String rowFilter) {
        this(dirName, fileName, 
                overide, qaStep, 
                user, sessionFactory, dbServerFactory);
        this.rowFilter = rowFilter;
        this.verboseStatusLogging = verboseStatusLogging;
    }

    public ExportQAStepTask(String dirName, String fileName, 
            boolean overide, QAStep qaStep, 
            User user, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory, boolean verboseStatusLogging,
            boolean download, String rowFilter) {
        this(dirName, fileName, 
                overide, qaStep, 
                user, sessionFactory, dbServerFactory, verboseStatusLogging, rowFilter);
        this.download  = download;
    }

    public void run() {        
        String suffix = "";
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            getStepResult();
            file = exportFile(dirName);
            file.setReadable(true, false);
            file.setWritable(true, false);
            String fileAbsolutePath = file.getAbsolutePath();
            String fileName = fileAbsolutePath.substring(fileAbsolutePath.lastIndexOf("/")+1, fileAbsolutePath.length());
            Exporter exporter = new DatabaseTableCSVExporter(dbServer.getEmissionsDatasource().getName() + "." + result.getTable(), dbServer.getEmissionsDatasource(), rowFilter);
            suffix = suffix();
            prepare(suffix);
            exporter.export(file);

            if (download) {
                //lets add a filedownload item for the user, so they can download the file
                fileDownloadDao.add(user, new Date(), file.getName(), "QA Step - CSV", overide);
            }

            complete(suffix);
        } catch (Exception e) {
            logError("Failed to export QA step : " + qastep.getName() + suffix, e);
            setStatus("Failed to export QA step " + qastep.getName() + suffix + ". Reason: " + e.getMessage());
        } finally {
            disconnect(dbServer); // Note: to disconnect db server from within the exporter (not obvious).
        }
    }

    private void disconnect(DbServer dbServer) {
        try {
            dbServer.disconnect();
        } catch (Exception e) {
            logError("Failed to close a connetion. " + qastep.getName(), e);
            setStatus("Failed to close a connetion. Reason: " + e.getMessage());
        }
    }

    private void prepare(String suffixMsg) {
        if (verboseStatusLogging)
            setStatus("Started exporting QA step '" + qastep.getName() + "'" + suffixMsg);
    }

    private void complete(String suffixMsg) {
        if (verboseStatusLogging)
            setStatus("Completed exporting QA step '" + qastep.getName() + "'" + suffixMsg
                    + (download ? ".  The file will start downloading momentarily, see the Download Manager for the download status." : ""));
    }

    private void logError(String message, Exception e) {
        log.error(message, e);

    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("ExportQAStep");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);

    }

    private String suffix() {
        return " for Version '" + versionName() + "' of Dataset '" + datasetName() + "' to "+
        file.getAbsolutePath();
    }

    private String versionName() {
        Session session = sessionFactory.getSession();
        try {
            return new Versions().get(qastep.getDatasetId(), qastep.getVersion(), session).getName();
        } finally {
            session.close();
        }
    }

    private String datasetName() {
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO();
            return dao.getDataset(session, qastep.getDatasetId()).getName();
        } finally {
            session.close();
        }
    }

    private void getStepResult() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            result = new QADAO().qaStepResult(qastep, session);
            if (result == null || result.getTable() == null)
                throw new EmfException("You have to first run the QA Step before export");
        } finally {
            session.close();
        }
    }

    private File exportFile(String dirName) throws EmfException {
        File f = new File(validateDir(dirName), fileName());

        if (download)
            return f;
        
        if (!overide && f.exists()) {
            throw new EmfException("The file " + f.getAbsolutePath() + " already exists.");
        }
        return f;
    }

    private String fileName() {
        if ( fileName == null || fileName.trim().length() == 0)
            return result.getTable() + ".csv";
        return fileName + ".csv";
    }

    private File validateDir(String dirName) throws EmfException {
        File file = new File(dirName);

        //don't check if exists when downloading, just create
        if (download) {
            file.mkdir();
            file.setReadable(true, false);
            file.setWritable(true, false);
            return file;
        }

        if (!file.exists() || !file.isDirectory()) {
            log.error("Folder " + dirName + " does not exist");
            throw new EmfException("Folder does not exist: " + dirName);
        }
        return file;
    }
}

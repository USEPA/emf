package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.FileDownloadDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;

public class CMExportTask implements Runnable {

    private static Log log = LogFactory.getLog(CMExportTask.class);

//    private StatusDAO statusDao;

    private File folder;
    
    private String prefix;

    private int[] controlMeasureIds;

    private User user;

    private HibernateSessionFactory sessionFactory;

    private ControlMeasureDAO controlMeasureDao;
    
    private DbServerFactory dbServerFactory;

    private boolean download;

    private FileDownloadDAO fileDownloadDAO;
    
    public CMExportTask(File folder, String prefix, int[] controlMeasureIds, User user, HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory, boolean download) {
        this.folder = folder;
        this.prefix = prefix;
        this.user = user;
        this.download = download;
        this.controlMeasureIds = controlMeasureIds;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        this.controlMeasureDao = new ControlMeasureDAO();
//        this.statusDao = new StatusDAO(sessionFactory);
        this.fileDownloadDAO = new FileDownloadDAO();
    }

    public void run() {
        Session session = sessionFactory.getSession();
        try {
            session.setFlushMode(FlushMode.MANUAL);
//            prepare();
            String[] selectedAbbrevAndSCCs = getSelectedAbbrevAndSCCs(controlMeasureIds);
            ControlMeasuresExporter exporter = new ControlMeasuresExporter(folder, prefix, getControlMeasures(controlMeasureIds, session), selectedAbbrevAndSCCs, user, sessionFactory, dbServerFactory);
            exporter.run();
            
            if (download) {
                for (String fileName : exporter.getFileNames()) {
                    fileDownloadDAO.add(user, new Date(), fileName, "Dataset Export", false, session);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logError("Failed to export control measures", e); // FIXME: report generation
//            setStatus("Failed to export all control measures: " + e.getMessage());
        } finally {
//            session.flush();
            session.close();
        }
    }

    private ControlMeasure[] getControlMeasures(int[] ids, Session session) {
        List cmList = new ArrayList();
        int size = ids.length;
        
        for (int i = 0; i < size; i++)
            cmList.add(controlMeasureDao.current(ids[i], session));
        
        return (ControlMeasure[])cmList.toArray(new ControlMeasure[0]);
    }

    private String[] getSelectedAbbrevAndSCCs(int[] measureIds) throws EmfException {
        List selectedSccs = new ArrayList();
        DbServer dbServer = dbServerFactory.getDbServer();
        for (int i = 0; i < measureIds.length; i++)
            selectedSccs.addAll(Arrays.asList(controlMeasureDao.getCMAbbrevAndSccs(measureIds[i], dbServer)));
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                throw new EmfException(e.getMessage());
            }
        return (String[])selectedSccs.toArray(new String[0]);
    }

//    private void prepare() {
//        addStartStatus();
//    }

//    private void addStartStatus() {
//        setStatus("Started exporting control measures");
//    }

//    private void addCompletedStatus(int noOfMeasures) {
//        setStatus("Completed exporting " + noOfMeasures + " control measures");
//    }

//    private void setStatus(String message) {
//        Status endStatus = new Status();
//        endStatus.setUsername(user.getUsername());
//        endStatus.setType("CMExport");
//        endStatus.setMessage(message);
//        endStatus.setTimestamp(new Date());
//
//        statusDao.add(endStatus);
//    }

    private void logError(String messge, Exception e) {
        log.error(messge, e);
    }

}

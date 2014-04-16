package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyMeasure;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class CMImportTask implements Runnable {

    private File folder;

    private String[] files;

    private User user;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private StatusDAO statusDao;

    private boolean truncate;

    private int[] sectorIds;

    public CMImportTask(File folder, String[] files, User user, boolean truncate, int[] sectorIds,
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.folder = folder;
        this.files = files;
        this.user = user;
        this.truncate = truncate;
        this.sectorIds = sectorIds;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        this.statusDao = new StatusDAO(sessionFactory);
    }

    public void run() {
        //if truncate measures, lets first backup the existing measures
        //then purge measures by sector(s)
        if (truncate) {
            Session session = sessionFactory.getSession();
            
            DbServer dbServer = dbServerFactory.getDbServer();

            try {
                
                List<ControlMeasure> controlMeasures = new ControlMeasureDAO().getControlMeasureBySectors(sectorIds, session);
                int[] ids = new int[controlMeasures.size()];
                
                String cmMsg = "Control Measures to be deleted: " + controlMeasures.size() + "\n";
                for (int i = 0; i < controlMeasures.size(); i++) {
                    ids[i] = controlMeasures.get(i).getId();
                    cmMsg += "  Abbrev: " + controlMeasures.get(i).getAbbreviation();
                    cmMsg += "; Name: " + controlMeasures.get(i).getName() + "\n";
                    //setDetailStatus( cmMsg ); // in case there are many measures to be deleted, send msg one by one
                }
                setDetailStatus( cmMsg ); 
                
                EmfProperty property = new EmfPropertiesDAO().getProperty("COST_CMDB_BACKUP_FOLDER", session);
                String backupFolder = property.getValue();
                CMExportTask exportTask = new CMExportTask(new File(backupFolder), CustomDateFormat.format_YYDDHHMMSS(new Date()), ids, user,
                        sessionFactory, dbServerFactory);
                exportTask.run();
                
                //look for dependencies on Control Strategies and Control Programs
                //if they're are dependent strategies, then finalize these so they can't be used in the future
                ControlStrategyDAO csDAO = new ControlStrategyDAO();
                List<ControlStrategy> cs = //new ControlStrategyDAO().getControlStrategiesByControlMeasures(ids, session);
                    csDAO.getControlStrategiesByControlMeasures(ids, session);
                
                String msg = "There are " + cs.size() + " control strategies that have dependent measures that will be purged.  These strategies will be finalized.";
                setStatus( msg);
                msg += "\n";
                for ( ControlStrategy s : cs) {
                    if ( s!= null) {
                        msg += "  " + s.getName() + "\n";
                    }
                }
                setDetailStatus( msg);
                
                cmMsg = "";
                for ( ControlStrategy s : cs) {
                    Date now = new Date();
                    cmMsg = ""; //s.getDescription();
                    cmMsg += "A CMDB Import (on : " + now + ") has caused some measures\nassigned to this strategy to be purged from the system.  This strategy\nwill be finalized to ensure this won't be accessible during a strategy analysis run.\n";
                    int numCMToBeDeleted = this.getNumControlMeasuresDeleted(s, ids);
                    cmMsg += "Measures Deleted: " + numCMToBeDeleted + "\n";
                    cmMsg += "Control Technolgies Affected: \n";
                    cmMsg += this.getControlTechnologiesAffected(s, ids);
                    if ( DebugLevels.DEBUG_23()) {
                        setDetailStatus( "  " + s.getName() + ": \n" + cmMsg + "\n"); // for debug
                    }
                    //s.setDescription( desc);
                    //s.setIsFinal( true);
                    csDAO.finalizeControlStrategy(s.getId(), cmMsg, session, ids);
                }
                
                if ( DebugLevels.DEBUG_23()) {
                    System.out.println("===== 1 =====");
                }
                
                ControlProgramDAO cpDAO = new ControlProgramDAO();
                List<ControlProgram> cp = //new ControlStrategyDAO().getControlStrategiesByControlMeasures(ids, session);
                    cpDAO.getControlProgramsByControlMeasures(ids, session);
                
                msg = "There are " + cp.size() + " Programs affected:";
                setStatus( msg);
                msg += "\n";
                for ( ControlProgram p : cp) {
                    if ( p!= null) {
                        msg += "  " + p.getName() + "\n";
                    }
                }
                setDetailStatus( msg);
                
                for ( ControlProgram p : cp) {
                    Date now = new Date();
                    cmMsg = ""; //s.getDescription();
                    cmMsg += "A CMDB Import (on : " + now + ") has caused some measures\nassigned to this strategy to be purged from the system.  This strategy\nwill be finalized to ensure this won't be accessible during a strategy analysis run.\n";
                    int numCMToBeDeleted = this.getNumControlMeasuresDeleted(p, ids);
                    cmMsg += "Measures Deleted: " + numCMToBeDeleted + "\n";
                    cmMsg += "Control Technolgies Affected: \n";
                    cmMsg += this.getControlTechnologiesAffected(p, ids);
                    if ( DebugLevels.DEBUG_23()) {
                        setDetailStatus( ">>> " + p.getName() + ": \n" + cmMsg + "\n"); // for debug
                    }
                    //s.setDescription( desc);
                    //s.setIsFinal( true);
                    cpDAO.updateControlProgram(p.getId(), cmMsg, session, ids);
                } 
                
                if ( DebugLevels.DEBUG_23()) {
                    System.out.println("===== 2 =====");
                }

                new ControlMeasureDAO().remove(sectorIds, sessionFactory, dbServer);
                
                if ( DebugLevels.DEBUG_23()) {
                    System.out.println("===== 3 =====");
                }
                
            } catch (Exception e) {
                if ( DebugLevels.DEBUG_23()) {
                    System.out.println("Exception occured: " + e.getMessage());
                }
                e.printStackTrace();
                setDetailStatus("Exception occured: " + e.getMessage());
            } finally {
                session.close();
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        try {
            ControlMeasuresImporter importer = null;
            try {
                importer = new ControlMeasuresImporter(folder, files, user, truncate, sectorIds, sessionFactory, dbServerFactory);
            } catch (Exception e) {
                setDetailStatus(e.getMessage());
                setStatus(e.getMessage());
            }
            if (importer != null)
                importer.run();
        } catch (Exception e) {
            //
        } finally {
            //
        }
    }
    
    public int getControlMeasureCountInSummaryFile(boolean purge, int [] sectorIDs, String folderPath, String[] fileNames, User user)  {
        try {
            ControlMeasuresImporter importer = null;
            try {
                importer = new ControlMeasuresImporter(folder, files, user, truncate, sectorIds, sessionFactory, dbServerFactory);
            } catch (Exception e) {
                setDetailStatus(e.getMessage());
                setStatus(e.getMessage());
            }
            if (importer != null) {
                importer.setForScan( true);
                int count = importer.getControlMeasureCountInSummaryFile();
                importer.setForScan( false);
                return count;
            }
        } catch (Exception e) {
            setDetailStatus(e.getMessage());
            setStatus(e.getMessage());
        } finally {
            //
        }
        return 0;
  }

    private void setDetailStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImportDetailMsg");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImport");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }
    
    private int getNumControlMeasuresDeleted( ControlStrategy cs, int [] cmIDs) {
        
        if ( cs == null || cmIDs == null || cmIDs.length == 0)
            return 0;
        
        ControlStrategyMeasure[] csCMs = cs.getControlMeasures();
        if ( csCMs == null || csCMs.length == 0) {
            return 0;
        }
        
        int count = 0;
        for ( int i = 0; i<csCMs.length; i++) {
            int id = csCMs[i].getControlMeasure().getId();
            boolean toBeDeleted = arrayContains( cmIDs, id);
            if ( toBeDeleted ) {
                count ++;
            }
        }
        return count;
    }
    
    private String getControlTechnologiesAffected(ControlStrategy cs, int [] cmIDs){
        if ( cs == null || cmIDs == null || cmIDs.length == 0)
            return "";
        
        ControlStrategyMeasure[] csms = cs.getControlMeasures();
        if ( csms == null || csms.length == 0) {
            return "";
        }
        
        String techNames = "";
        List<Integer> ctIDList = new ArrayList<Integer>();
        
        for ( int i = 0; i<csms.length; i++) {
            if ( csms[i] == null) {
                continue;
            }
            LightControlMeasure lcm = csms[i].getControlMeasure();
            if ( lcm == null) {
                continue;
            }
            int cmId = lcm.getId();
            boolean toBeDeleted = arrayContains( cmIDs, cmId);
            if ( toBeDeleted) {
                ControlTechnology ct = lcm.getControlTechnology();
                if (ct != null) {
                    int id = ct.getId();
                    if ( !ctIDList.contains( id)) {
                        ctIDList.add( id);
                        techNames += "  " + ct.getName() + "\n";
                    }
                }
            }
        }
        
        if ( DebugLevels.DEBUG_23()) {
            System.out.println( techNames);        
        }
        
        return techNames;        
    }
    
    private int getNumControlMeasuresDeleted( ControlProgram cp, int [] cmIDs) {
        if ( cp == null || cmIDs == null || cmIDs.length == 0) {
            return 0;
        }
        ControlMeasure[] cpCMs = cp.getControlMeasures();
        if ( cpCMs == null || cpCMs.length == 0) {
            return 0;
        }
        int count = 0;
        for ( int i = 0; i<cpCMs.length; i++) {
            if ( cpCMs[i] == null) {
                continue;
            }
            int id = cpCMs[i].getId();
            boolean toBeDeleted = arrayContains( cmIDs, id);
            if ( toBeDeleted ) {
                count ++;
            }
        }
        return count;
    }
    
    private String getControlTechnologiesAffected(ControlProgram cp, int [] cmIDs){
        if ( cp == null || cmIDs == null || cmIDs.length == 0)
            return "";
        
        ControlMeasure[] cpCMs = cp.getControlMeasures();
        if ( cpCMs == null || cpCMs.length == 0) {
            return "";
        }
        
        String techNames = "";
        List<Integer> ctIDList = new ArrayList<Integer>();
        
        for ( int i = 0; i<cpCMs.length; i++) {
            
            if ( cpCMs[i] == null) {
                continue;
            }
            int cmId = cpCMs[i].getId();
            boolean toBeDeleted = arrayContains( cmIDs, cmId);
            if ( toBeDeleted) {
                ControlTechnology ct = cpCMs[i].getControlTechnology();
                int id = ct.getId();
                if ( !ctIDList.contains( id)) {
                    ctIDList.add( id);
                    techNames += "  " + ct.getName() + "\n";
                }                
            }
        }
        
        if ( DebugLevels.DEBUG_23()) {
            System.out.println( techNames);        
        }
        
        return techNames;        
    }
    
    private boolean arrayContains( int [] cmIDs, int id) {
        if ( cmIDs == null || cmIDs.length == 0) {
            return false;
        }
        boolean toBeDeleted = false;
        for ( int j=0; j<cmIDs.length; j++) {
            if ( cmIDs[j] == id) {
                toBeDeleted = true;
                break;
            }
        }
        return toBeDeleted;
    }

}

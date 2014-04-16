package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class DeleteQAStepsTask implements Runnable {

    private static Log LOG = LogFactory.getLog(DeleteQAStepsTask.class);
    
    private HibernateSessionFactory sessionFactory;
    
    private DbServerFactory dbServerFactory;
    
    private QADAO dao;
    
    private StatusDAO statusDAO; // = new StatusDAO(sessionFactory);
    
    private User user;
    
    private QAStep[] steps;
    
    private int datasetId;
    
    public DeleteQAStepsTask(QAStep[] steps, int datasetId,
            User user, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory)
    {
        this.steps = steps;
        this.datasetId = datasetId;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        dao = new QADAO();
        statusDAO = new StatusDAO(sessionFactory);
    }
    
    public void run() {
        try {
            deleteQASteps(user, steps, datasetId);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            LOG.error("Failed to delete QA steps: ", e);
            setStatus(user, "Failed to delete QA steps: " + e.getMessage());
        }
    }
    
    public void deleteQASteps(User user, QAStep[] steps, int datasetId) throws EmfException { //BUG3615
        
        StatusDAO statusDAO = new StatusDAO(sessionFactory);
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType("DeleteQASteps");
        status.setMessage("Start to delete QA Steps.");
        status.setTimestamp(new Date());
        statusDAO.add(status);
        
        Session session = sessionFactory.getSession();
        DatasetDAO datasetDAO = new DatasetDAO();
        EmfDataset dataset = datasetDAO.obtainLocked(user, datasetDAO.getDataset(session, datasetId), session);
        
        DbServer dbServer = dbServerFactory.getDbServer();
        Datasource emfDatasource = dbServer.getEmfDatasource();
        List<QAStep> stepsToBeDeleted = new ArrayList<QAStep>();
        int total = 0;
        String stepsReferenced = "";
        int numStepsReferenced = 0;
        String stepsFailedDependencyCheck = "";
        int numStepsFailedDependencyCheck = 0;
        String sql = "";
        for ( QAStep step : steps) {
            total++;
            sql = "select s.id, s.dataset_id, s.name from emf.qa_steps s where ";
            sql += "(s.program_arguments ~* '(.*[[.$.]])DATASET_QASTEP[[.[.]]([[:space:]]*)\\\"CURRENT_DATASET\\\"([[:space:]]*),([[:space:]]*)\""; 
            sql += step.getName();
            sql += "\\\"([[:space:]]*)[[.].]](.*)' and s.dataset_id = ";
            sql += step.getDatasetId() + ") ";
            sql += " or ";
            sql += "(s.program_arguments ~* '(.*[[.$.]])DATASET_QASTEP[[.[.]]([[:space:]]*)\\\"";
            sql += dataset.getName();
            sql += "\\\"([[:space:]]*),([[:space:]]*)\""; 
            sql += step.getName();
            sql += "\\\"([[:space:]]*)[[.].]](.*)') ";
            sql += " or ";
            sql += "(s.program_arguments ~* '(.*[[.$.]])DATASET_QASTEP_VERSION[[.[.]]([[:space:]]*)\\\"CURRENT_DATASET\\\"([[:space:]]*),([[:space:]]*)\""; 
            sql += step.getName();
            sql += "\\\"([[:space:]]*),([[:space:]]*)([0-9]+)[[.].]](.*)' and s.dataset_id = ";
            sql += step.getDatasetId() + ") ";
            sql += " or ";
            sql += "(s.program_arguments ~* '(.*[[.$.]])DATASET_QASTEP_VERSION[[.[.]]([[:space:]]*)\\\"";
            sql += dataset.getName();
            sql += "\\\"([[:space:]]*),([[:space:]]*)\"";
            sql += step.getName();
            sql += "\\\"([[:space:]]*),([[:space:]]*)([0-9]+)[[.].]](.*)') ";
            sql += ";";

            try {
                ResultSet rs = emfDatasource.query().executeQuery(sql);

                if ( rs.next()) { // not empty
                    stepsReferenced += "\"" + step.getName() + "\" referenced by QA step \"" + rs.getString("name") + "\" for dataset \""; 
                    EmfDataset qaDataset = datasetDAO.getDataset(session, rs.getInt("dataset_id"));
                    stepsReferenced += qaDataset.getName() + "\'";
                    while ( rs.next()) {
                        stepsReferenced += ", ";
                        stepsReferenced += " \"" + rs.getString("name") + "\" for dataset \""; 
                        qaDataset = datasetDAO.getDataset(session, rs.getInt("dataset_id"));
                        stepsReferenced += qaDataset.getName() + "\"";
                    }
                    stepsReferenced +=". ";
                    numStepsReferenced++;
                } else { //empty
                    stepsToBeDeleted.add(step);
                }
            }catch (SQLException e) {
                LOG.error( "Error when running query - " + sql + " - to check QA Step " + step.getName() + "'s dependences: ", e); 
                setStatus( user, "Error when running query - " + sql + " - to check QA Step " + step.getName() + "'s dependences: " + e.getMessage() + ". This step won't be deleted.");
                stepsFailedDependencyCheck += (numStepsFailedDependencyCheck==0 ? "" : ",") + step.getName();
                numStepsFailedDependencyCheck++;
            } catch (Exception e) {
                LOG.error( "Error when checking QA Step " + step.getName() + "'s dependences: ", e); 
                setStatus( user, "Error when checking QA Step " + step.getName() + "'s dependences: " + e.getMessage() + ". This step won't be deleted.");
                stepsFailedDependencyCheck += (numStepsFailedDependencyCheck==0 ? "" : ",") + step.getName();
                numStepsFailedDependencyCheck++;
            }
        }
        
        String msg = "";
        msg += stepsFailedDependencyCheck.equals("") ? "" : 
            "Failed to check " + numStepsFailedDependencyCheck + " steps' dependences - they will not be deleted: " + 
            stepsFailedDependencyCheck + ".";
        msg += msg.equals("") ? "" : "\n";
        msg += stepsReferenced.equals("") ? "" : 
            "" + numStepsReferenced + " steps are referenced by some other QA steps and will not be deleted: " + 
            stepsReferenced;
        msg += msg.equals("") ? "" : "\n";
        msg += stepsToBeDeleted.size() > 0 ? 
               "To delete " + stepsToBeDeleted.size() + " QA steps..." :
               "No QA steps will be deleted.";
        setStatus( user, msg);
        
        int succeeded=0;
        int failed = 0;
        try {
            if (dbServer!=null && dbServer.isConnected()) {
                dbServer.disconnect();
            }
        } catch (Exception e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }
        for ( QAStep step : stepsToBeDeleted) {
            try {
                dbServer = dbServerFactory.getDbServer();
                this.removeQAResultTable(step, dbServer);
                try {
                    dao.deleteQAStep(step, session);
                    succeeded++;
                } catch (RuntimeException e) {
                    failed++;
                    LOG.error("Failed to delete QA Step " + step.getName(), e);
                    setStatus(user,"Failed to delete QA Step " + step.getName() + ": " + e.getMessage());
                }
            } catch (EmfException e) {
                failed++;
                LOG.error("Failed to remove result tables for QA Step " + step.getName(), e);
                setStatus(user,"Failed to remove result tables for QA Step " + step.getName() + ": " + e.getMessage());
            } catch ( Exception e) {
                failed++;
                LOG.error("Failed to delete QA Step " + step.getName(), e);
                setStatus(user,"Failed to delete " + step.getName() + ": " + e.getMessage());
            }
            
        }
        
        session.close();
        
        msg = "Completed deleting the QA steps: \n";
        msg += "Total number of steps: " + total + "\n"; 
        msg += numStepsFailedDependencyCheck==0 ? "" : 
               "Failed to check dependences for " + numStepsFailedDependencyCheck + " QA steps - not deleted.\n";
        msg += numStepsReferenced==0 ? "" :
               "" + numStepsReferenced + " QA Steps referenced by other QA steps - not deleted.\n";
        msg += succeeded==0 ? "" : 
               "Successfully deleted " + succeeded + " QA Steps\n";
        msg += failed==0 ? "" :
               "Failed to delete " + failed + " QA Steps\n";
        setStatus(user,msg);
        
    }
    
    private void setStatus( User user, String msg) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType("DeleteQASteps");
        status.setMessage(msg);
        status.setTimestamp(new Date());
        statusDAO.add(status);
    }
    
    private void removeQAResultTable(QAStep step, DbServer dbServer) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            QAStepResult result = dao.qaStepResult(step, session);

            if (result == null)
                return;

            String table = result.getTable();

            if (table != null && !table.trim().isEmpty()) {
                TableCreator tableCreator = new TableCreator(dbServer.getEmissionsDatasource());

                if (tableCreator.exists(table.trim())) {
                    tableCreator.drop(table.trim());
                }
            }

            dao.removeQAStepResult(result, session);
        } catch (Exception e) {
            LOG.error("Cannot drop result table for QA step: " + step.getName(), e);
            throw new EmfException("Cannot drop result table for QA step: " + step.getName());
        } finally {
            try {
                session.close();

                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                throw new EmfException(e.getMessage());
            }
        }
    }

}

package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.util.UniqueID;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class SQLQAProgramRunner implements QAProgramRunner {

    protected DbServer dbServer;

    protected QAStep qaStep;

    protected TableCreator tableCreator;

    protected EntityManagerFactory entityManagerFactory;

    public SQLQAProgramRunner(DbServer dbServer, EntityManagerFactory entityManagerFactory, QAStep qaStep) {
        this.dbServer = dbServer;
        this.entityManagerFactory = entityManagerFactory;
        this.qaStep = qaStep;
        this.tableCreator = new TableCreator(dbServer.getEmissionsDatasource());
    }

    public void run() throws EmfException {
        String programArguments = qaStep.getProgramArguments();
        if (programArguments == null || programArguments.trim().length() == 0) {
            throw new EmfException("Please specify the sql query");
        }
        String tableName = tableName(qaStep);
        String query = query(dbServer, qaStep, tableName);
        printQuery(query);
        try {
            dropTable(getExistedTableName(qaStep));
            dbServer.getEmissionsDatasource().query().execute(query);
            success(qaStep, tableName);

            // Changed this code by using e.message to pull out more detailed failure information.
        } catch (Exception e) {
            failure(qaStep);
            // throw new EmfException("Check the query - " + query);
            throw new EmfException("Check the query - " + e.getMessage());

        }
    }

    public void printQuery(String query) {
        if (DebugLevels.DEBUG_0())
            System.out.println("\nQA Step '" + qaStep.getName() + "' query: " + query);
    }

    public void dropTable(String tableName) throws EmfException {
        if (tableName == null || tableName.isEmpty())
            return;

        try {
            if (tableCreator.exists(tableName)) {
                tableCreator.drop(tableName);
            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    protected void success(QAStep qaStep, String tableName) {
        updateQAStepResult(qaStep, "Success", tableName, new Date());
    }

    protected void failure(QAStep qaStep) {
        updateQAStepResult(qaStep, "Failed", null, null);
    }
    
    private void updateQAStepResult(QAStep qaStep, String status, String tableName, Date date) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            QADAO qadao = new QADAO();
            QAStepResult result = qadao.qaStepResult(qaStep, entityManager);
            if (result == null) {
                result = new QAStepResult(qaStep);
            }
            result.setTableCreationStatus(status);
            result.setTable(tableName);
            result.setTableCreationDate(date);
            qadao.updateQAStepResult(result, entityManager);
        } finally {
            entityManager.close();
        }
    }

    // Modified SQLQueryParser constructor to reflect the changes to the actual class -- added arguments
    // for version and entityManagerFactory

    protected String query(DbServer dbServer, QAStep qaStep, String tableName) throws EmfException {
        SQLQueryParser parser = new SQLQueryParser(qaStep, tableName, dbServer.getEmissionsDatasource().getName(),
                dataset(qaStep), version(qaStep), entityManagerFactory);
        return parser.parse();
    }

    protected EmfDataset dataset(QAStep qaStep) {
        DatasetDAO dao = new DatasetDAO();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getDataset(entityManager, qaStep.getDatasetId());
        } finally {
            entityManager.close();
        }
    }

    protected Version version(QAStep qaStep) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return new Versions().get(qaStep.getDatasetId(), qaStep.getVersion(), entityManager);
        } finally {
            entityManager.close();
        }
    }

    protected String tableName(QAStep qaStep) {

        // System.out.println("The input is:" + qaStep);
        // String formattedDate = CustomDateFormat.format_YYYYMMDDHHMMSS(new Date());
        String uID = UniqueID.getUniqueID();
        String table = "QA_DSID" + qaStep.getDatasetId() + "_V" + qaStep.getVersion() + "_" + uID; //formattedDate;

        
        if (table.length() > 63) { // postgresql table name max length is 63
//          int space = table.length() - 63;
          table = table.substring(0, 63);
      }

        for (int i = 0; i < table.length(); i++) {
            if (!Character.isLetterOrDigit(table.charAt(i))) {
                table = table.replace(table.charAt(i), '_');
            }
        }
        // System.out.println("Table name is: " + table);
        //make sure the table name is a max of six characters
        return table.replaceAll(" ", "_");
    }

//    public static void main(String[] args) {
//        String str = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";
//        System.out.println(str.substring(0,(str.length() >= 63 ? 63 : str.length()) ) + " " + str.substring(0,(str.length() >= 63 ? 63 : str.length()) ).length());
//    }
//
    protected String getExistedTableName(QAStep qaStep) {
        QAStepResult result = getResult(qaStep);

        return (result != null) ? result.getTable() : null;
    }

    private QAStepResult getResult(QAStep qaStep) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            QADAO qadao = new QADAO();
            return qadao.qaStepResult(qaStep, entityManager);
        } finally {
            entityManager.close();
        }
    }

}
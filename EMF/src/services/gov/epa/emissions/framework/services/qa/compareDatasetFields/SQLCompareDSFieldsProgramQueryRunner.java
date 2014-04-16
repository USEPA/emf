package gov.epa.emissions.framework.services.qa.compareDatasetFields;

import java.sql.SQLException;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.SQLQAProgramRunner;

public class SQLCompareDSFieldsProgramQueryRunner extends SQLQAProgramRunner {
    
    private String emissioDatasourceName;

    public SQLCompareDSFieldsProgramQueryRunner(DbServer dbServer, HibernateSessionFactory sessionFactory, QAStep qaStep) {
        super(dbServer, sessionFactory, qaStep);
        this.emissioDatasourceName = dbServer.getEmissionsDatasource().getName();
    }
    
    protected String query(DbServer dbServer, QAStep qaStep, String tableName) throws EmfException {
        SQLCompareDatasetFieldsProgramQuery parser = new SQLCompareDatasetFieldsProgramQuery(sessionFactory, dbServer.getEmissionsDatasource(), emissioDatasourceName, tableName, qaStep);
        String sql = "";
        try {
            sql = parser.createProgramQuery();
        } catch (SQLException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return sql;
    }
    
    public void run() throws EmfException{
        // Add the stuff from the superclass, and modify it to take in the datasets as program 
        // arguments.  Then pass it into the runner.  the output for the runner should go to the db
        // for processing.
        
        String programArguments = qaStep.getProgramArguments();
        if (programArguments == null || programArguments.trim().length() == 0) {
            throw new EmfException("Please specify the QA Program \"Compare Dataset Ascii Values\" configuration settings in the program argument.");
        }
        
        //System.out.println("The input to tableName is: " + qaStep);
        String tableName = tableName(qaStep);
        //System.out.println("The tableName is: " + tableName);
        String query = query(dbServer, qaStep, tableName);
        printQuery(query);
        try {
            dropTable(getExistedTableName(qaStep));
            dbServer.getEmissionsDatasource().query().execute(query);
            success(qaStep, tableName);
        // Changed as per SQLQAProgramRunner
        } catch (Exception e) {
            failure(qaStep);
            //throw new EmfException("Check the query - " + query);
            throw new EmfException("Check the query - " + e.getMessage());
        }
    }
}

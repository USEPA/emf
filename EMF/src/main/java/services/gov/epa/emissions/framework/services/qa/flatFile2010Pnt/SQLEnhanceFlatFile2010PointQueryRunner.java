package gov.epa.emissions.framework.services.qa.flatFile2010Pnt;

import java.sql.SQLException;
import java.util.Date;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.SQLQAProgramRunner;
import gov.epa.emissions.framework.services.qa.comparedatasets.SQLCompareDatasetsProgramQuery;

public class SQLEnhanceFlatFile2010PointQueryRunner extends SQLQAProgramRunner{
    
    private String emissioDatasourceName;
    
    private StatusDAO statusDao;
    
    public SQLEnhanceFlatFile2010PointQueryRunner(DbServer dbServer, HibernateSessionFactory sessionFactory, QAStep qaStep) {
        super(dbServer, sessionFactory, qaStep);
        this.emissioDatasourceName = dbServer.getEmissionsDatasource().getName();
        this.statusDao = new StatusDAO(sessionFactory);
    }
    
    protected String query(DbServer dbServer, QAStep qaStep, String tableName) throws EmfException {
        SQLEnhanceFlatFile2010PointQuery parser = new SQLEnhanceFlatFile2010PointQuery(sessionFactory, emissioDatasourceName, tableName, qaStep, dbServer.getEmissionsDatasource());
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
            throw new EmfException("Please specify the QA Program \"Enhance Flat File 2010 Point\" configuration settings in the program argument.");
        }
        
        //System.out.println("The input to tableName is: " + qaStep);
        String tableName = tableName(qaStep);
        //System.out.println("The tableName is: " + tableName);
        //setStatus( "Constructing query...");
        String query = query(dbServer, qaStep, tableName);
        printQuery(query);
        setStatus( "Executing query...");
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
    
    private void setStatus(String message) {
        
        Status endStatus = new Status();
        endStatus.setUsername(qaStep.getWho());
        endStatus.setType("RunQAStep");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);

    }    
}

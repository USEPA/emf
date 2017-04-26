package gov.epa.emissions.framework.services.module;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.util.Date;

import org.hibernate.Session;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

class ModuleRunnerContext {
    
    private ModuleRunnerTask task;
    private Module module;

    // initialized by start()
    private Date startDate;
    private String timeStamp;
    private String userTimeStamp;
    private String tempUserPassword;

    // initialized by createHistory()
    private History history;
    
    public ModuleRunnerContext(ModuleRunnerTask task, Module module) {
        this.task = task;
        this.module = module;
    }

    public void start() {
        startDate = new Date();
        timeStamp = CustomDateFormat.format_HHMMSSSS(startDate);
        userTimeStamp = task.getUser().getUsername() + "_" + timeStamp;
        // Generate random password by choosing 30 * 5 = 150 bits from a cryptographically
        // secure random bit generator and encoding them in base-32.
        // 128 bits is considered to be cryptographically strong.
        // Each digit in a base 32 number can encode 5 bits, so 150 bits results in 30 characters.
        // This encoding is compact and efficient, with 5 random bits per character.
        tempUserPassword = new BigInteger(32 * 5, new SecureRandom()).toString(32);
    }

    public void stop() {
        Date stopDate = new Date();
        long durationSeconds = (stopDate.getTime() - startDate.getTime() + 999) / 1000 + 1;
        history.setDurationSeconds((int)durationSeconds);
        history = getModulesDAO().updateHistory(history, getSession());
    }
    
    protected void createHistory() throws EmfException {
        history = new History();
        history.setModule(module);
        history.setStatus(History.STARTED);
        history.setCreator(getUser());
        history.setCreationDate(startDate);
        
        module.addModuleHistory(history);
        module = getModulesDAO().updateModule(module, getSession());
    }
    
    // public accessors
    
    public ModuleRunnerTask getTask() {
        return task;
    }

    public User getUser() {
        return task.getUser();
    }

    public DatasetDAO getDatasetDAO() {
        return task.getDatasetDAO();
    }
    
    public StatusDAO getStatusDAO() {
        return task.getStatusDAO();
    }

    public ModulesDAO getModulesDAO() {
        return task.getModulesDAO();
    }
    
    public HibernateSessionFactory getHibernateSessionFactory() {
        return task.getHibernateSessionFactory();
    }

    public Session getSession() {
        return task.getSession();
    }

    public DbServerFactory getDbServerFactory() {
        return task.getDbServerFactory();
    }

    public DbServer getDbServer() {
        return task.getDbServer();
    }

    public Connection getConnection() {
        return task.getConnection();
    }

    public Datasource getDatasource() {
        return task.getDatasource();
    }
    
    public boolean getVerboseStatusLogging() {
        return task.getVerboseStatusLogging();
    }
    
    public Module getModule() {
        return module;
    }

    // history accessors
    
    public String getStatus() {
        return history.getStatus();
    }

    public void setStatus(String status) {
        history.setStatus(status);
    }

    public String getResult() {
        return history.getResult();
    }

    public void setResult(String result) {
        history.setResult(result);
    }

    // other accessors

    public ModuleTypeVersion getModuleTypeVersion() {
        return module.getModuleTypeVersion();
    }

    public History getHistory() {
        return history;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getUserTimeStamp() {
        return userTimeStamp;
    }

    public String getTempUserPassword() {
        return tempUserPassword;
    }
}

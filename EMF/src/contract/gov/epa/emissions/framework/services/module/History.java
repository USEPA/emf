package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

public class History implements Serializable, Comparable<History> {

    // status
    public static final String STARTED         = "STARTED";
    public static final String SETUP_SCRIPT    = "SETUP_SCRIPT";
    public static final String USER_SCRIPT     = "USER_SCRIPT";
    public static final String TEARDOWN_SCRIPT = "TEARDOWN_SCRIPT";
    public static final String COMPLETED       = "COMPLETED";
    
    // result
    public static final String TIMEOUT     = "TIMEOUT";
    public static final String INCANCELLED = "CANCELLED";
    public static final String FAILED      = "FAILED";
    public static final String SUCCESS     = "SUCCESS";
    
    // log type
    public static final String DEBUG    = "DEBUG";
    public static final String INFO     = "INFO";
    public static final String WARNING  = "WARNING";
    public static final String ERROR    = "ERROR";
    
    private int id;

    private Module module;

    private int runId; // index

    private String setupScript;

    private String userScript;

    private String teardownScript;

    private String logMessages;

    private String status; // 'STARTED', 'SETUP_SCRIPT', 'USER_SCRIPT', 'TEARDOWN_SCRIPT', 'COMPLETED'

    private String result; // null, 'TIMEOUT', 'CANCELLED', 'FAILED', 'SUCCESS'

    private String errorMessage;
    
    private int durationSeconds;
    
    private User creator;

    private Date creationDate;

    private String comment;
    
    private Map<String, HistoryDataset> historyDatasets;
    private Map<String, HistoryParameter> historyParameters;
    private Map<String, HistorySubmodule> historySubmodules;
    private Map<String, HistoryInternalDataset> historyInternalDatasets;
    private Map<String, HistoryInternalParameter> historyInternalParameters;

    public History() {
        setHistoryDatasets(new HashMap<String, HistoryDataset>());
        setHistoryParameters(new HashMap<String, HistoryParameter>());
        setHistorySubmodules(new HashMap<String, HistorySubmodule>());
        setHistoryInternalDatasets(new HashMap<String, HistoryInternalDataset>());
        setHistoryInternalParameters(new HashMap<String, HistoryInternalParameter>());
    }

    public History(int id) {
        this();
        this.setId(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public int getRunId() {
        return runId;
    }

    public void setRunId(int runId) {
        this.runId = runId;
    }

    public String getSetupScript() {
        return setupScript;
    }

    public void setSetupScript(String setupScript) {
        this.setupScript = setupScript;
    }

    public String getUserScript() {
        return userScript;
    }

    public void setUserScript(String userScript) {
        this.userScript = userScript;
    }

    public String getTeardownScript() {
        return teardownScript;
    }

    public void setTeardownScript(String teardownScript) {
        this.teardownScript = teardownScript;
    }

    public String getLogMessages() {
        return logMessages;
    }

    public void setLogMessages(String logMessages) {
        this.logMessages = logMessages;
    }

    public void addLogMessage(String logType, String logMessage) {
//      String date = CustomDateFormat.format_yyyy_MM_dd_HHmmssSSS(new Date());
        SimpleDateFormat dateFormatter = new SimpleDateFormat();
        dateFormatter.applyPattern("HH:mm:ss.SSS");
        String date = dateFormatter.format(new Date());
        String header = date + " [" + logType + "] ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < header.length(); i++)
            sb.append(' ');
        String indent = sb.toString();
        String[] lines = logMessage.split("\n");
        StringBuilder formattedMessage = new StringBuilder();
        for(String line : lines) {
            formattedMessage.append(header + line + "\n");
            header = indent;
        }
        if (this.logMessages == null)
            this.logMessages  = formattedMessage.toString();
        else
            this.logMessages += formattedMessage.toString();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, HistoryDataset> getHistoryDatasets() {
        return historyDatasets;
    }

    public void setHistoryDatasets(Map<String, HistoryDataset> historyDatasets) {
        this.historyDatasets = historyDatasets;
    }

    public Map<String, HistoryParameter> getHistoryParameters() {
        return historyParameters;
    }

    public void setHistoryParameters(Map<String, HistoryParameter> historyParameters) {
        this.historyParameters = historyParameters;
    }

    public Map<String, HistorySubmodule> getHistorySubmodules() {
        return historySubmodules;
    }

    public void setHistorySubmodules(Map<String, HistorySubmodule> historySubmodules) {
        this.historySubmodules = historySubmodules;
    }

    public void addHistorySubmodule(HistorySubmodule historySubmodule) {
        historySubmodule.setHistory(this);
        this.historySubmodules.put(historySubmodule.getSubmodulePath(), historySubmodule);
    }

    public Map<String, HistoryInternalDataset> getHistoryInternalDatasets() {
        return historyInternalDatasets;
    }

    public void setHistoryInternalDatasets(Map<String, HistoryInternalDataset> historyInternalDatasets) {
        this.historyInternalDatasets = historyInternalDatasets;
    }

    public Map<String, HistoryInternalParameter> getHistoryInternalParameters() {
        return historyInternalParameters;
    }

    public void setHistoryInternalParameters(Map<String, HistoryInternalParameter> historyInternalParameters) {
        this.historyInternalParameters = historyInternalParameters;
    }

    @Override
    public int compareTo(History o) {
        return ((Integer)getRunId()).compareTo(o.getRunId());
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HistorySubmodule implements Serializable {

    // By default, in PostgreSQL, NAMEDATALEN is 64 so the maximum identifier length is 63 bytes.
    public static final int MAX_NAME_LEN = 63; // NAMEDATALEN-1

    private int id;

    private History history;

    private String submodulePath; // slash delimited list of submodule ids (at least one)

    private String submodulePathNames; // slash delimited list of submodule names (at least one)

    private String setupScript;

    private String userScript;

    private String teardownScript;

    private String logMessages;

    private String status; // 'STARTED', 'SETUP_SCRIPT', 'USER_SCRIPT', 'TEARDOWN_SCRIPT', 'COMPLETED'

    private String result; // null, 'TIMEOUT', 'CANCELLED', 'FAILED', 'SUCCESS'

    private String errorMessage;
    
    private int durationSeconds;
    
    private Date creationDate;

    private String comment;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }

    public String getSubmodulePath() {
        return submodulePath;
    }

    public void setSubmodulePath(String submodulePath) {
        this.submodulePath = submodulePath;
    }

    public String getSubmodulePathNames() {
        return submodulePathNames;
    }

    public void setSubmodulePathNames(String submodulePathNames) {
        this.submodulePathNames = submodulePathNames;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.RemoteCommand;
import gov.epa.emissions.framework.tasks.CaseJobTaskManager;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CaseJobTask extends Task {
    
    private static Log log = LogFactory.getLog(CaseJobTask.class);

    private User user = null;

    private int numDepends=0;
    
    // private String runRedirect = ">&"; // shell specific redirect

    private String jobFileContent = null;

    private String logFile = null;

    private String jobFile = null;

    private String queueOptions = null;

    private String hostName = null;

    private int jobId;

    private String jobName = null;

    private int caseId;
    
    private String caseName;

    private String exportTaskSubmitterId = null;

    boolean exportsSuccess = false;

    boolean dependenciesSet = false;

    private String jobkey;

    private String runRedirect = ">&"; // shell specific redirect
    
    private static final String lineSep = System.getProperty("line.separator");
    
    private String qId;

    public String getJobkey() {
        return jobkey;
    }

    public void setJobkey(String jobkey) {
        this.jobkey = jobkey;
    }

    public boolean isExportsSuccess() {
        return exportsSuccess;
    }

    public void setExportsSuccess(boolean exportsSuccess) {
        this.exportsSuccess = exportsSuccess;
    }

    public boolean isDependenciesSet() {
        return dependenciesSet;
    }

    public void setDependenciesSet(boolean dependenciesSet) {
        this.dependenciesSet = dependenciesSet;
    }

    public String getExportTaskSubmitterId() {
        return exportTaskSubmitterId;
    }

    public void setExportTaskSubmitterId(String exportTaskSubmitterId) {
        this.exportTaskSubmitterId = exportTaskSubmitterId;
    }

    public CaseJobTask(int jobId, int caseId, User user) {
        super();
        createId();
        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> " + createId());
        this.user = user;
        this.jobId = jobId;
        this.caseId = caseId;
        log.info("created a CaseJobTask: " + this.taskId + " for caseId: " + this.caseId);
    }

    @Override
    public boolean isReady() {

        // Modify the algorithm to use all parameters that indicate rediness
        // Initial algorithm is simple. CJT isReadyFlag at the get-go
        this.isReadyFlag = this.dependenciesSet && this.exportsSuccess;

        return this.isReadyFlag;
    }

    public void run() {
        String status = null;
        String msg1 = "";
        String msg2 = "";
        String msgType1 = "i";
        String msgType2 = "e";
        String[] msgs = null;
        String[] types = null;

        System.out.println("@@@@ CASE job task RUNNING jobId= " + jobId + " jobName= " + jobName + " caseId= " + caseId
                + " CaseJobTask id= " + this.getTaskId() + " now running in Thread id= "
                + Thread.currentThread().getId());

//        String status = "completed";
//        String mesg = " was pseudo successfull";

        try {
            this.createJobFile();
            status = "completed";
        } catch (Exception e) {
            log.error("Exception while creating JobFile when running CaseJobTask. See stacktrace for details");
            e.printStackTrace();
            
            status = "failed";
            String mesg = "Error creating job script " + this.jobFile + ": " + e.getMessage();
            notifyManager(status, new String[] {mesg}, new String[]{"e"}, false);
            return;
        }

        // Create an execution string and submit job to the queue,
        // if the key word $EMF_JOBLOG is in the queue options,
        // replace w/ log file

        String executionStr = null;
        String qOptions = this.queueOptions;
        String queueOptionsLog = qOptions.replace("$EMF_JOBLOG", this.logFile);

        if (queueOptionsLog.equals("")) {
            executionStr = this.jobFile;
        } else {
            executionStr = queueOptionsLog + " " + this.jobFile;
        }

        /*
         * execute the job script Note if hostname is localhost this is done locally w/o ssh and stdout and stderr is
         * redirected to the log. This redirect is currently shell specific (should generalize) if hostname is not
         * localhost it is through ssh
         */
        String username = this.user.getUsername();
        try {
            if (hostName.equals("localhost")) {
                // execute on local machine
                executionStr = executionStr + " " + this.runRedirect + " " + this.logFile;
                msg1 = "Submitted job to " + hostName + ". Execution string: " + executionStr + lineSep;
                InputStream inStream = RemoteCommand.executeLocal(executionStr);
                processLogs(executionStr, inStream, "localhost", true);
            } else {
                // execute on remote machine and log stdout
                msg1 = "Submitted job to " + hostName + ". Execution string: " + executionStr + lineSep;
                InputStream inStream = RemoteCommand.execute(username, hostName, executionStr);
                processLogs(executionStr, inStream, hostName, false);
                // capture PBSqueueId and send back to case job submitter
                // TODO:
            }

            msgs = new String[] {msg1};
            types = new String[] {msgType1};
            status = "completed";
        } catch (Exception e) {
            log.error("Error executing job file: " + jobFile + " Execution string= " + executionStr);
            e.printStackTrace();
            status = "failed";
            msg2 = e.getMessage();
            msgs = new String[] {msg1, msg2};
            types = new String[] {msgType1, msgType2};
        }

        notifyManager(status, msgs, types, true);
    }

    private void notifyManager(String status, String[] msgs, String[] msgTypes, boolean regHistory) {
        try {
            CaseJobTaskManager.callBackFromThread(this.taskId, this.submitterId, status, msgs, msgTypes, regHistory);
        } catch (EmfException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void processLogs(String executionStr, InputStream inStream, String host, boolean local) throws EmfException {
        String outTitle = "stdout from (" + host + "): " + executionStr;
        qId = RemoteCommand.logStdout(outTitle, inStream, local);
    }

    /**
     * Write out the contents of the string (jobFileContent) to the file (jobFile)
     * 
     * @throws EmfException
     */
    private void createJobFile() throws EmfException {
        FileOutputStream out; // declare a file output object
        PrintStream p; // declare a print stream object
        File outFile = null;

        try {
            outFile = new File(jobFile);

            // Create a new file output stream
            // connected to jobFile
            out = new FileOutputStream(outFile);

            // Connect print stream to the output stream
            p = new PrintStream(out);

            p.println(this.jobFileContent);
            p.flush();
            p.close();

            // Make script executable
            outFile.setExecutable(true, false);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
            if (outFile != null && !outFile.canWrite())
            {
                throw new EmfException("EMF tomcat does not have permission to create the job file: "
                    +outFile);
            }
            throw new EmfException("In createJobFile: Error writing jobFile: " + jobFile);
        }
    }

    public void setRunRedirect(String runRedirect) {
        // this.runRedirect = runRedirect;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public void setJobFile(String jobFile) {
        this.jobFile = jobFile;
    }

    public void setQueueOptions(String queueOptions) {
        this.queueOptions = queueOptions;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setJobFileContent(String jobFileContent) {
        this.jobFileContent = jobFileContent;
    }
    
    public String getQId() {
        return this.qId;
    }
    
    // ***********************************************************
    // FIXME: After code is working remove everything below
    public String getJobFileContent() {
        return jobFileContent;
    }

    public String getLogFile() {
        return logFile;
    }

    public String getJobFile() {
        return jobFile;
    }

    public String getQueueOptions() {
        return queueOptions;
    }

    public String getHostName() {
        return hostName;
    }
    // FIXME: After code is working remove everything ABOVE
    // ***********************************************************

    public int getNumDepends() {
        return numDepends;
    }

    public void setNumDepends(int numDepends) {
        this.numDepends = numDepends;
    }


    @Override
    public int compareTo(Object o) {
        CaseJobTask second = (CaseJobTask) o;
       int thisDependsSize = this.numDepends;
       int secondDependsSize = second.getNumDepends();
       
       if (thisDependsSize < secondDependsSize){
           return -1;
       }else if (secondDependsSize < thisDependsSize){
           return 1;
       }
       return 0;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }


}

package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseObjectManager;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;

public class EditJobsTabPresenterImpl implements EditJobsTabPresenter {

    private Case caseObj;

    private EditJobsTabView view;

    private CaseObjectManager caseObjectManager = null;

    private CaseInput[] inputsBySelectedJobs;

    private CaseParameter[] parametersBySelectedJobs;

    private List<Integer> jobs2Cancel = new ArrayList<Integer>();
    
    private String smokeHost;

    private EmfSession session;

    private String smokeUser;
    
    private boolean smkPassRegistered = true; //set to false after all security features implemented.

    public EditJobsTabPresenterImpl(EmfSession session, EditJobsTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
        this.caseObjectManager = CaseObjectManager.getCaseObjectManager(session);
    }

    public void display(CaseEditorPresenter parentPresenter) {
        view.display(session, caseObj, this, parentPresenter);
    }

    public void doSave() throws EmfException {
        String caseOutputDir = view.getCaseOutputFileDir();
        if (caseOutputDir != null)
            caseObj.setOutputFileDir(caseOutputDir);
        this.caseObjectManager.refreshJobList();
        view.refresh();
    }

    public void doSave(CaseJob[] jobs) throws EmfException {
        for (CaseJob job : jobs)
            service().updateCaseJobStatus(job);

        view.refresh();
    }

    public void addNewJobDialog(NewJobView dialog) {
        dialog.register(this);
        dialog.display();
    }

    public CaseJob addNewJob(CaseJob job) throws EmfException {
        CaseJob newJob = service().addCaseJob(session.user(), job);
        
        if (newJob == null)
            throw new EmfException("Can't add job " + job.getName() + ".");
        
        this.caseObjectManager.refreshJobList();

        if (newJob.getCaseId() == caseObj.getId()) {
            view.refresh();
        }

        return newJob;
    }

    private CaseService service() {
        return session.caseService();
    }

    public void refreshView() {
        view.refresh();
    }

    public boolean jobsUsed(CaseJob[] jobs) throws EmfException {
        if (jobs.length == 0)
            return false;

        int[] jobIds = new int[jobs.length];

        for (int i = 0; i < jobs.length; i++)
            jobIds[i] = jobs[i].getId();

        inputsBySelectedJobs = service().getCaseInputs(jobs[0].getCaseId(), jobIds);
        parametersBySelectedJobs = service().getCaseParameters(jobs[0].getCaseId(), jobIds);

        return (inputsBySelectedJobs != null && inputsBySelectedJobs.length > 0)
                || (parametersBySelectedJobs != null && parametersBySelectedJobs.length > 0);
    }

    public void removeJobs(CaseJob[] jobs) throws EmfException {
        if (inputsBySelectedJobs != null && inputsBySelectedJobs.length > 0)
            service().removeCaseInputs(session.user(), inputsBySelectedJobs);

        if (parametersBySelectedJobs != null && parametersBySelectedJobs.length > 0)
            service().removeCaseParameters(session.user(), parametersBySelectedJobs);

        service().removeCaseJobs(session.user(), jobs);
        this.caseObjectManager.refreshJobList();
    }

    public void editJob(CaseJob job, EditCaseJobView jobEditor) throws EmfException {
        EditJobPresenter presenter = new EditCaseJobPresenterImpl(jobEditor, view, this, session);
        presenter.display(job);
    }

    public List<CaseJob> copyJobs2CurrentCase(int caseId, List<CaseJob> jobs) throws Exception {
        List<CaseJob> added = new ArrayList<CaseJob>();

        if (caseId == this.caseObj.getId()) {
            for (CaseJob job : jobs)
                added.add(addNewJob(setJobValuesB4Copy(caseId, job)));
        }

        return added;
    }

    public CaseJob setJobValuesB4Copy(int caseId, CaseJob job) throws Exception {
        CaseJob newJob = (CaseJob) DeepCopy.copy(job);
        newJob.setCaseId(caseId);
        newJob.setName(getUniqueNewName("Copy of " + job.getName()));
        newJob.setJobkey(null); // jobkey supposedly generated when it is run
        newJob.setRunstatus(null);
        newJob.setRunLog(null);
        newJob.setRunStartDate(null);
        newJob.setRunCompletionDate(null);
        newJob.setRunJobUser(null); // not running at this moment
        return newJob;
    }

    public void copyJobs(int caseId, List<CaseJob> jobs) throws Exception {
        CaseJob[] jobsArray = jobs.toArray(new CaseJob[0]);
        User user = session.user();

        for (int i = 0; i < jobs.size(); i++) {
            jobsArray[i].setParentCaseId(this.caseObj.getId());
            jobsArray[i].setRunJobUser(null); // not running at this moment
            jobsArray[i].setUser(user); // job owner changes
        }

        service().addCaseJobs(user, caseId, jobsArray);
    }

    public CaseJob[] getCaseJobs() throws EmfException {
        return service().getCaseJobs(caseObj.getId());
    }

    public Object[] getAllCaseNameIDs() throws EmfException {
        return service().getAllCaseNameIDs();
    }

    public void addJobFields(CaseJob job, JComponent container, JobFieldsPanel jobFieldsPanel) throws EmfException {
        JobFieldsPanelPresenter jobFieldsPresenter = new JobFieldsPanelPresenter(jobFieldsPanel, session, this, caseObj);
        jobFieldsPresenter.display(job, container);
    }

    public Case getCaseObj() {
        return this.caseObj;
    }

    public void runJobs(CaseJob[] jobs) throws EmfException {
        Integer[] jobIds = new Integer[jobs.length];

        for (int i = 0; i < jobs.length; i++) {
            if (jobs[i].getExecutable() == null)
                throw new EmfException("Job " + jobs[i].getName() + " doesn't have a valid executable file.");
            jobIds[i] = new Integer(jobs[i].getId());
        }

        System.out.println("Start running jobs");
        service().runJobs(jobIds, caseObj.getId(), session.user()); // BUG3589
        view.refresh();
        System.out.println("Finished running jobs");
    }

    public Host[] getJobHosts() throws EmfException {
        caseObjectManager.refresh();
        return this.caseObjectManager.getJobHosts();
    }

    public Sector[] getJobSectors() throws EmfException {
        caseObjectManager.refresh();
        return this.caseObjectManager.getSectors();
    }

    public GeoRegion[] getGeoRegions() {
        List<GeoRegion> all = new ArrayList<GeoRegion>();
        all.add(new GeoRegion(""));
        all.addAll(Arrays.asList(caseObj.getRegions()));

        return all.toArray(new GeoRegion[0]);
    }

    public String getJobsStatus(CaseJob[] jobs) throws EmfException {
        List<String> ok = new ArrayList<String>();
        List<String> cancel = new ArrayList<String>();
        List<String> warning = new ArrayList<String>();
        jobs2Cancel.clear();

        for (int i = 0; i < jobs.length; i++) {
            int jobId = jobs[i].getId();
            JobRunStatus statusObj = service().getCaseJob(jobId).getRunstatus();
            String status = (statusObj == null ? "" : statusObj.getName());

            if (status == null || status.trim().isEmpty())
                ok.add(status);

            if (status != null && status.equalsIgnoreCase("Not Started"))
                ok.add(status);

            if (status != null && status.equalsIgnoreCase("Quality Assured"))
                ok.add(status);

            if (status != null && status.equalsIgnoreCase("Completed"))
                warning.add(status);

            if (status != null && status.equalsIgnoreCase("Failed"))
                warning.add(status);

            if (status != null && status.equalsIgnoreCase("Running")) {
                cancel.add(status);
                jobs2Cancel.add(jobId);
            }

            if (status != null && status.equalsIgnoreCase("Submitted")) {
                cancel.add(status);
                jobs2Cancel.add(jobId);
            }

            if (status != null && status.equalsIgnoreCase("Exporting")) {
                cancel.add(status);
                jobs2Cancel.add(jobId);
            }

            if (status != null && status.equalsIgnoreCase("Waiting")) {
                cancel.add(status);
                jobs2Cancel.add(jobId);
            }
        }

        if (ok.size() == jobs.length)
            return "OK";

        if (cancel.size() > 0)
            return "CANCEL";

        return "WARNING";
    }

    private String getUniqueNewName(String name) throws Exception {
        List<String> names = new ArrayList<String>();

        List<CaseJob> allJobs = Arrays.asList(view.caseJobs());

        for (Iterator<CaseJob> iter = allJobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            if (job.getName().startsWith(name)) {
                names.add(job.getName());
            }
        }

        if (names.size() == 0)
            return name;

        return name + " " + getSequence(name, names);
    }

    private int getSequence(String stub, List<String> names) {
        int sequence = names.size() + 1;
        String integer = "";

        try {
            for (Iterator<String> iter = names.iterator(); iter.hasNext();) {
                integer = iter.next().substring(stub.length()).trim();

                if (!integer.isEmpty()) {
                    int temp = Integer.parseInt(integer);

                    if (temp == sequence)
                        ++sequence;
                    else if (temp > sequence)
                        sequence = temp + 1;
                }
            }

            return sequence;
        } catch (Exception e) {
            return Math.abs(new Random().nextInt());
        }
    }

    public String validateJobs(CaseJob[] jobs) throws EmfException {
        List<Integer> ids = new ArrayList<Integer>();

        for (CaseJob job : jobs)
            ids.add(new Integer(job.getId()));

        return service().validateJobs(ids.toArray(new Integer[0]));
    }

    public void checkIfLockedByCurrentUser() throws EmfException {
        Case reloaded = service().reloadCase(caseObj.getId());

        if (reloaded.isLocked() && !reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current case object expired. User " + reloaded.getLockOwner()
                    + " has it now.");
    }

    public void refreshJobList() throws EmfException {
        this.caseObjectManager.refreshJobList();
    }

    public synchronized JobRunStatus[] getRunStatuses() throws EmfException {
        return CaseObjectManager.getCaseObjectManager(session).getJobRunStatuses();
    }

    public String cancelJobs(List<CaseJob> jobs) {
        try {
            String status = getJobsStatus(jobs.toArray(new CaseJob[0]));
             
            if (status != null && status.equalsIgnoreCase("OK"))
                return "None of the selected jobs is in an active state. No job is canceled.";

            if (status.equalsIgnoreCase("CANCEL")) {
                int count = service().cancelJobs(getJobIds(jobs2Cancel), session.user());
                refreshJobList();
                return count + " jobs have been successfully canceled.";
            }
            refreshJobList();
            return "No job has been canceled.";
        } catch (EmfException e) {
            String msg = e.getMessage();

            if (msg == null || msg.trim().isEmpty())
                msg = "error cancelling jobs.";

            if (msg.length() > 80)
                msg = msg.substring(0, 78) + "...";

            if (!msg.endsWith("."))
                msg += ".";

            return "Warning: " + msg;
        }
    }

    private int[] getJobIds(List<Integer> jobIds) {
        int size = jobIds.size();

        if (jobIds == null || size == 0)
            return new int[0];

        int[] ids = new int[size];

        for (int i = 0; i < size; i++)
            ids[i] = jobIds.get(i);

        return ids;
    }

    public void modifyJobs(ModifyJobsDialog dialog) {
        dialog.register(this);
        dialog.display();
    }

    public String isGeoRegionInSummary(int selectedCaseId, GeoRegion[] georegions) throws EmfException {
        return service().isGeoRegionInSummary(selectedCaseId, georegions);
    }
    
    public GeoRegion[] getGeoregion(List<CaseJob> jobs){
        
        List<GeoRegion>  regions = new ArrayList<GeoRegion>();

        for (int i = 0; i < jobs.size(); i++){
            GeoRegion region = jobs.get(i).getRegion();
            if (region != null && !(regions.contains(region)))
                regions.add(region);
        }
        return regions.toArray(new GeoRegion[0]);
    }

    public boolean passwordRegistered() throws EmfException {
        if (smkPassRegistered)
            return true;
        
        if (smokeHost == null || smokeHost.trim().isEmpty())
            return false;
        
        UserService secServ = session.userService();
        
        if (session.getPublicKey() == null) {
            session.setPublicKey(secServ.getEncodedPublickey());
        }
            
        smkPassRegistered = secServ.passwordRegistered(smokeUser, smokeHost);
        
        return smkPassRegistered;
    }

    public void setSmokeLogin(String username, String host, char[] passcode) throws EmfException {
        smokeUser = username;
        smokeHost = host;
        
        UserService secServ = session.userService();
        
        if (session.getPublicKey() == null) {
            session.setPublicKey(secServ.getEncodedPublickey());
        }
        
        session.setEncryptedPassword(passcode);
        secServ.updateEncryptedPassword(host, username, session.getEncryptedPassword());
        smkPassRegistered = true;
    }

    public CaseJob[] getCaseJobsFromManager() throws EmfException {
        return caseObjectManager.getCaseJobs(caseObj.getId());
    }   

}

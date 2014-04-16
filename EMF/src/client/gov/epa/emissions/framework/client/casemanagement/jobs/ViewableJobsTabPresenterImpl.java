package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseObjectManager;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.util.ArrayList;
import java.util.List;

public class ViewableJobsTabPresenterImpl implements EditJobsTabPresenter{

    private Case caseObj;

    private ViewableJobsTab view;
    
    private List<Integer> jobs2Cancel = new ArrayList<Integer>();

    private EmfSession session;

    private String smokeHost;

    private String smokeUser;

    private boolean smkPassRegistered;

    public ViewableJobsTabPresenterImpl(EmfSession session, ViewableJobsTab view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.display(caseObj, this);
    }

    public void doSave(CaseJob[] jobs) throws EmfException {
        for (CaseJob job : jobs)
            service().updateCaseJobStatus(job);
    }


    public CaseJob addNewJob(CaseJob job) {
        return null;
    }

    private CaseService service() {
        return session.caseService();
    }

    public boolean jobsUsed(CaseJob[] jobs) {
        return false;
    }

    public void removeJobs(CaseJob[] jobs) {
        //service().removeCaseJobs(jobs);
    }

    public void editJob(CaseJob job, EditCaseJobView jobEditor) throws EmfException {
        EditJobPresenter presenter = new EditCaseJobPresenterImpl(jobEditor,this, session);
        presenter.display(job);
    }

    @SuppressWarnings("unused")
    public void copyJob2CurrentCase(int caseId, CaseJob job, EditCaseJobView jobEditor) throws Exception {
 //
    }

    public CaseJob[] getCaseJobs() throws EmfException {
        return service().getCaseJobs(caseObj.getId());
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

    public String validateJobs(CaseJob[] jobs) throws EmfException {
        List<Integer> ids = new ArrayList<Integer>();
        
        for (CaseJob job : jobs)
            ids.add(new Integer(job.getId()));
        
        return service().validateJobs(ids.toArray(new Integer[0]));
    }

    public void addNewJobDialog(NewJobView view) {
        // NOTE Auto-generated method stub
        
    }

    public synchronized JobRunStatus[] getRunStatuses() throws EmfException {
        return CaseObjectManager.getCaseObjectManager(session).getJobRunStatuses();
    }

    public void doSave() {
        // NOTE Auto-generated method stub
        
    }

    public void checkIfLockedByCurrentUser() {
        // NOTE Auto-generated method stub
        
    }

    public Object[] getAllCaseNameIDs() throws EmfException {
        return service().getAllCaseNameIDs();
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

    @SuppressWarnings("unused")
    public void addNewSectorToSummary(CaseJob job) {
        // NOTE Auto-generated method stub
    }
    
    @SuppressWarnings("unused")
    public void addNewRegionToSummary(CaseJob job) {
        // NOTE Auto-generated method stub
    }

    public void display(CaseEditorPresenter parentPresenter) {
        // NOTE Auto-generated method stub
        
    }

    public void refreshJobList() {
        // NOTE Auto-generated method stub
        
    }

    public String cancelJobs(List<CaseJob> jobs) {
        try {
            String status = getJobsStatus(jobs.toArray(new CaseJob[0]));

            if (status != null && status.equalsIgnoreCase("OK"))
                return "None of the selected jobs is in an active state. No job is canceled.";

            if (status.equalsIgnoreCase("CANCEL")) {
                int count = service().cancelJobs(getJobIds(jobs2Cancel), session.user());
                return count + " jobs have been successfully canceled.";
            }
            
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
        // NOTE Auto-generated method stub
        
    }

    public List<CaseJob> copyJobs2CurrentCase(int caseId, List<CaseJob> jobs) throws Exception {
        // NOTE Auto-generated method stub
        return null;
    }

    public CaseJob setJobValuesB4Copy(int caseId, CaseJob job) throws Exception {
        // NOTE Auto-generated method stub
        return null;
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
        return null;
    }
    
}

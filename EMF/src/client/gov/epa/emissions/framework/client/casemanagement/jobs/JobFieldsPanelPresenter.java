package gov.epa.emissions.framework.client.casemanagement.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseObjectManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.data.GeoRegion;

import javax.swing.JComponent;

public class JobFieldsPanelPresenter {

    private EmfSession session;

    private JobFieldsPanelView view;

    private EditJobsTabPresenter parentPresenter;

    private Case caseObj;
    
    private CaseObjectManager caseObjectManager = null;

    public JobFieldsPanelPresenter(JobFieldsPanelView jobFields, EmfSession session,
            EditJobsTabPresenter parentPresenter, Case caseObj) {
        this.session = session;
        this.view = jobFields;
        this.parentPresenter = parentPresenter;
        this.caseObj = caseObj;
        this.caseObjectManager = CaseObjectManager.getCaseObjectManager(session);
    }

    public void display(CaseJob job, JComponent container) throws EmfException {
        view.observe(this);
        view.display(caseObj, job, container);
    }

    public synchronized Sector[] getSectors() throws EmfException {
        return caseObjectManager.getCaseSectors(caseObj);
    }

    public synchronized Host[] getHosts() throws EmfException {
        return caseObjectManager.getJobHosts();
    }

    public synchronized Host getHost(Object selected) throws EmfException {
        return caseObjectManager.getOrAddHost(selected);
    }
    
    public synchronized JobRunStatus[] getRunStatuses() throws EmfException {
        return caseObjectManager.getJobRunStatuses();
    }

    private CaseService caseService() {
        return session.caseService();
    }

    public CaseJob doSave() throws EmfException {
        CaseJob job = view.setFields();
        caseService().saveCaseJobFromClient(session.user(), job);
        return job; 
    }

    public boolean checkDuplication(CaseJob job) throws EmfException {
        CaseJob[] existedJobs = parentPresenter.getCaseJobs();
        return contains(job, existedJobs);
    }

    private boolean contains(CaseJob job, CaseJob[] existedJobs) {
        String newArgs = job.getArgs();
        Sector newSector = job.getSector();
        Executable newExec = job.getExecutable();

        for (int i = 0; i < existedJobs.length; i++) {
            String existedArgs = existedJobs[i].getArgs();
            Sector existedSector = existedJobs[i].getSector();
            Executable existedExec = existedJobs[i].getExecutable();

            if (job.getId() != existedJobs[i].getId()
                    && job.getVersion() == existedJobs[i].getVersion()
                    && ((newArgs == null && existedArgs == null) || (newArgs != null && newArgs
                            .equalsIgnoreCase(existedArgs)))
                    && ((newExec == null && existedExec == null) || (newExec != null) && newExec.equals(existedExec))
                    && ((newSector == null && existedSector == null) || (newSector != null && newSector
                            .equals(existedSector)))) {
                return true;
            }
        }

        return false;
    }

    public void doValidateFields() throws EmfException {
        view.setFields(); // FIXME: should do more check here
    }

    public String[] getExistedJobs() throws EmfException {
        CaseJob[] existedJobs = parentPresenter.getCaseJobs();
        String[] names = new String[existedJobs.length];
        
        for (int i = 0; i < existedJobs.length; i++)
            names[i] = existedJobs[i].getName();
        
        return names;
    }
    
    public CaseJob[] getAllValidJobs(int jobId) throws EmfException {
        return caseService().getAllValidJobs(jobId, caseObj.getId());
    }

    public CaseJob[] getDependentJobs(int jobId) throws EmfException {
        if (jobId <= 0)
            return new CaseJob[0];
        
        return caseService().getDependentJobs(jobId);
    }

    public DependentJob[] dependentJobs(Object[] jobs) {
        DependentJob[] dependentJobs = new DependentJob[jobs.length];
        
        for (int i = 0; i < jobs.length; i++)
            dependentJobs[i] = new DependentJob(((CaseJob)jobs[i]).getId());
        
        return dependentJobs;
    }

    public GeoRegion[] getGeoRegions() {
        List<GeoRegion> all = new ArrayList<GeoRegion>();
        all.add(new GeoRegion(""));
        all.addAll(Arrays.asList(caseObj.getRegions()));
        
        return all.toArray(new GeoRegion[0]);
    }

}

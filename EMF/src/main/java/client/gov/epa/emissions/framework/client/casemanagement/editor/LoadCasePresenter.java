package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

public class LoadCasePresenter {
    private Case currentCase;

    private EmfSession session;

    private static String lastFolder = null;

    public LoadCasePresenter(EmfSession session, Case caseObj) {
        this.currentCase = caseObj;
        this.session = session;
    }

    public void display(LoadCaseDialog view) {
        view.observe(this);
        view.setMostRecentUsedFolder(getFolder());

        view.display();
    }

    private String getFolder() {
        return (lastFolder != null) ? lastFolder : getDefaultFolder();
    }

    public String loadCase(String path, CaseJob job) throws EmfException  {
        int jobId = (job == null) ? 0 : job.getId();
        return session.caseService().loadCMAQCase(path, jobId, currentCase.getId(), session.user());
    }

    private String getDefaultFolder() {
        return "";
    }

    public CaseJob[] getJobs() throws EmfException{
        return session.caseService().getCaseJobs(currentCase.getId());
    }
    
    public String getDefaultBaseFolder() {
        String folder = session.preferences().inputFolder();
        if (folder == null || folder.trim().isEmpty())
            folder = "";// default, if unspecified

        return folder;
    }

}

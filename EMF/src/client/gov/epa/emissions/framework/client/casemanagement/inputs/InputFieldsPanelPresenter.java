package gov.epa.emissions.framework.client.casemanagement.inputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseObjectManager;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.SubDir;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.editor.DataEditorService;

import javax.swing.JComponent;

public class InputFieldsPanelPresenter {

    private EmfSession session;

    private InputFieldsPanelView view;

    private int caseId;

    public static final String ALL_FOR_SECTOR = "All jobs for sector";

    private CaseObjectManager caseObjectManager = null;

    private CaseInput currentInput = null;
    
    private Case caseObj; 

    public InputFieldsPanelPresenter(Case caseObj, InputFieldsPanelView inputFields, EmfSession session) {
        this.session = session;
        this.view = inputFields;
        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
        this.caseObjectManager = CaseObjectManager.getCaseObjectManager(session);
    }

    public void display(CaseInput input, JComponent container, int modelToRunId) throws EmfException {
        currentInput = input;
        view.observe(this);
        view.display(input, container, modelToRunId, session);
    }

    public InputName[] getInputNames(int modelToRunId) throws EmfException {
        return caseObjectManager.getInputNames(modelToRunId);
    }

    public Sector[] getSectors() throws EmfException {
//        return caseObjectManager.getSectorsWithAll();
        return caseObjectManager.getCaseSectors(caseObj);
    }

    public CaseProgram[] getPrograms(int modelToRunId) throws EmfException {
        return caseObjectManager.getPrograms(modelToRunId);
    }

    public SubDir[] getSubdirs(int modelToRunId) throws EmfException {
        return caseObjectManager.getSubDirs(modelToRunId);
    }

    public InputEnvtVar[] getEnvtVars(int modelToRunId) throws EmfException {
        return caseObjectManager.getInputEnvtVars(modelToRunId);
    }

    public CaseJob[] getCaseJobs() throws EmfException {
        return caseObjectManager.getCaseJobsWithAll(caseId);
    }

    public DatasetType[] getDSTypes() throws EmfException {
        return caseObjectManager.getDatasetTypes();
    }

    public EmfDataset[] getDatasets(DatasetType type) throws EmfException {
        if (type == null)
            return new EmfDataset[0];

        return dataService().getDatasets(type);
    }

    public Version[] getVersions(EmfDataset dataset) throws EmfException {
        if (dataset == null) {
            return new Version[0];
        }
        return dataEditorService().getVersions(dataset.getId());
    }

    private DataService dataService() {
        return session.dataService();
    }

    private DataEditorService dataEditorService() {
        return session.dataEditorService();
    }

    public void doSave() throws EmfException {
        this.currentInput = view.setFields();
        session.caseService().updateCaseInput(session.user(), currentInput);
    }

    public Sector getUpdatedSector() {
        return currentInput.getSector();
    }
    
    public GeoRegion getUpdatedRegion() {
        return currentInput.getRegion();
    }

    public void doValidateFields() throws EmfException {
        view.setFields(); // FIXME: should do more check here
    }

    public InputName getInputName(Object selected, int modelToRunId) throws EmfException {
        return caseObjectManager.getOrAddInputName(selected, modelToRunId);
    }

    public InputEnvtVar getInputEnvtVar(Object selected, int modelToRunId) throws EmfException {
        return caseObjectManager.getOrAddInputEnvtVar(selected, modelToRunId);
    }

    public CaseProgram getCaseProgram(Object selected, int modelToRunId) throws EmfException {
        return caseObjectManager.getOrAddProgram(selected, modelToRunId);
    }

    public SubDir getSubDir(Object selected, int modelToRunId) throws EmfException {
        return caseObjectManager.getOrAddSubDir(selected, modelToRunId);
    }

    public int getJobIndex(int caseJobId, CaseJob[] jobs) // throws EmfException
    {
        // CaseJob[] jobs = session.caseService().getCaseJobs(caseId);
        // AME: don't go get the jobs from the server again!
        // QH: This will use the cached list of jobs and won't get the newest list of jobs for the session.

        if (caseJobId == 0)
            return 0;

        for (int i = 0; i < jobs.length; i++)
            if (jobs[i].getId() == caseJobId)
                return i; // because of the default "All jobs" job is not in db

        return 0;
    }

    public String getJobName(int jobId) throws EmfException {
        if (jobId == 0)
            return "All jobs for sector";

        CaseJob job = session.caseService().getCaseJob(jobId);
        if (job == null)
            throw new EmfException("Cannot retrieve job (id = " + jobId + ").");
        return job.getName();
    }

    public EmfDataset getDataset(int id) throws EmfException {
        return session.dataService().getDataset(id);
    }

    public void viewDataset(int datasetId, EmfConsole parentConsole, DesktopManager desktopManager) throws EmfException {
        PropertiesViewPresenter datasetViewPresenter = new PropertiesViewPresenter(getDataset(datasetId), session);
        DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
        datasetViewPresenter.doDisplay(view);
    }
    
    public GeoRegion[] getGeoRegions() {
        List<GeoRegion> all = new ArrayList<GeoRegion>();
        all.add(new GeoRegion(""));
        //all.addAll(Arrays.asList(caseObjectManager.getGeoRegions()));
        all.addAll(Arrays.asList(caseObj.getRegions()));
        return all.toArray(new GeoRegion[0]);
    }

}

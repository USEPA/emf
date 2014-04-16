package gov.epa.emissions.framework.client.casemanagement.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseObjectManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.editor.DataEditorService;

import javax.swing.JComponent;

public class ParameterFieldsPanelPresenter {

    private EmfSession session;

    private ParameterFieldsPanelView view;
    
    private CaseParameter currentParameter;
    
    private Case caseObj; 

    private int caseId;
    
    private CaseObjectManager caseObjectManager = null;

    public ParameterFieldsPanelPresenter(Case caseObj, ParameterFieldsPanelView inputFields, EmfSession session) {
        this.session = session;
        this.view = inputFields;
        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
        this.caseObjectManager = CaseObjectManager.getCaseObjectManager(session);
    }

    public void display(CaseParameter param, int model_id, JComponent container) throws EmfException {
        currentParameter = param;
        view.observe(this);
        view.display(param, model_id, container);
    }

    public ParameterName[] getParameterNames(int model_id) throws EmfException {
        return caseObjectManager.getParameterNames(model_id);
    }

    public Sector[] getSectors() throws EmfException {
        return caseObjectManager.getSectorsWithAll();
    }

    public CaseProgram[] getPrograms(int model_id) throws EmfException {
        return caseObjectManager.getPrograms(model_id);
    }

    public ParameterEnvVar[] getEnvtVars(int model_id) throws EmfException {
        return caseObjectManager.getParameterEnvVars(model_id);
    }

    public CaseJob[] getCaseJobs() throws EmfException {
        return caseObjectManager.getCaseJobsWithAll(caseId);
    }
    
    public int getJobId(CaseJob job) {
        if (job.getName().equalsIgnoreCase(caseObjectManager.getJobForAll().getName()))
            return 0;
        
        return job.getId();
    }

    public DatasetType[] getDSTypes() {
        return session.getLightDatasetTypes();
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

        return dataEditorServive().getVersions(dataset.getId());
    }

    private DataCommonsService dataCommonsService() {
        return session.dataCommonsService();
    }

    private DataService dataService() {
        return session.dataService();
    }

    private DataEditorService dataEditorServive() {
        return session.dataEditorService();
    }

    public void doSave() throws EmfException {
        currentParameter = view.setFields();
        session.caseService().updateCaseParameter(session.user(), currentParameter);
    }
    
    public Sector getUpdatedSector() {
        return currentParameter.getSector();
    }
    
    public GeoRegion getUpdatedRegion() {
        return currentParameter.getRegion();
    }

    public void doValidateFields() throws EmfException {
        view.setFields(); // FIXME: should do more check here
    }

    public ParameterName getParameterName(Object selected, int modelId) throws EmfException {
        return caseObjectManager.getOrAddParameterName(selected, modelId);
    }

    public ParameterEnvVar getParameterEnvtVar(Object selected, int modelId) throws EmfException {
        return caseObjectManager.getOrAddParameterEnvtVar(selected, modelId);
    }

    public CaseProgram getCaseProgram(Object selected, int model_id) throws EmfException {
        return caseObjectManager.getOrAddProgram(selected, model_id);
    }

    public int getJobIndex(int caseJobID) throws EmfException {
        CaseJob[] jobs = caseObjectManager.getCaseJobsWithAll(caseId);

        for (int i = 0; i < jobs.length; i++)
            if (jobs[i].getId() == caseJobID)
                return i;

        return 0;
    }

    public ValueType[] getValueTypes() throws EmfException {
        return caseObjectManager.getParameterValueTypes();
    }
    
    public GeoRegion[] getGeoRegions() {
        List<GeoRegion> all = new ArrayList<GeoRegion>();
        all.add(new GeoRegion(""));
        all.addAll(Arrays.asList(caseObj.getRegions()));
        
        return all.toArray(new GeoRegion[0]);
    }

}

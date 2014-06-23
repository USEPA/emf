package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorView;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SensitivityPresenter {
    private SensitivityView view;

    private EmfSession session;

    private CaseManagerPresenter managerPresenter;

    private int defaultPageSize = 20;

    public SensitivityPresenter(EmfSession session, SensitivityView view, CaseManagerPresenter managerPresenter) {
        this.session = session;
        this.view = view;
        this.managerPresenter = managerPresenter;
    }

    public void doDisplay(Case case1, CaseManagerPresenter parentPresenter) {
        view.observe(this, parentPresenter);
        view.display(case1);
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public Case addSensitivities(int parentCaseId, int templateCaseId, int[] jobIds, String jobGroup,
            Case setSensitivityCase, GeoRegion geoRegion) throws EmfException {
        setSensitivityCase.setLastModifiedBy(session.user());
        setSensitivityCase.setLastModifiedDate(new Date());

        return service().addSensitivity2Case(session.user(), parentCaseId, templateCaseId, jobIds, jobGroup,
                setSensitivityCase, geoRegion);
    }

    public Case doSave(int parentCaseId, int templateCaseId, int[] jobIds, String jobGroup, Case newCase)
            throws EmfException {
        if (isDuplicate(newCase))
            throw new EmfException("A Case named '" + newCase.getName() + "' already exists.");

        newCase.setCreator(session.user());
        newCase.setLastModifiedBy(session.user());
        newCase.setLastModifiedDate(new Date());

        Case loaded = service().mergeCases(session.user(), parentCaseId, templateCaseId, jobIds, jobGroup, newCase);
        // closeView();
        managerPresenter.addNewCaseToTableData(loaded);
        return loaded;
    }

    private boolean isDuplicate(Case newCase) throws EmfException {
        Case[] cases = service().getCases();
        for (int i = 0; i < cases.length; i++) {
            if (cases[i].getName().equals(newCase.getName()))
                return true;
        }

        return false;
    }

    private CaseService service() {
        return session.caseService();
    }

    public void copyCase(int caseId) throws EmfException {
        service().copyCaseObject(new int[] { caseId }, session.user());
    }

    public Case updateCase(Case caseObj) throws EmfException {
        Case locked = service().obtainLocked(session.user(), caseObj);
        return service().updateCase(locked);
    }

    public void editCase(CaseEditorView caseView, Case caseObj) throws EmfException {
        CaseEditorPresenter presenter = new CaseEditorPresenterImpl(caseObj, session, caseView, managerPresenter);
        presenter.doDisplay();
    }

    public Case[] getCases(CaseCategory category) throws EmfException {
        if (category == null)
            return new Case[0];

        if (category.getName().equals("All"))
            return service().getCases();

        return service().getCases(category);
    }

    public CaseJob[] getCaseJobs(Case caseObj) throws EmfException {
        return service().getCaseJobs(caseObj.getId());
    }

    public CaseJob[] getCaseJobs(Case caseObj, GeoRegion region, Sector[] sectors) throws EmfException {
        CaseJob[] jobs = getCaseJobs(caseObj);
        List<CaseJob> filteredJobs1 = new ArrayList<CaseJob>();
        filteredJobs1.addAll(new ArrayList<CaseJob>(Arrays.asList(jobs)));

        // Find jobs contain selected sectors
        List<CaseJob> filteredJobs2 = new ArrayList<CaseJob>();

        if (filteredJobs1.size() > 0 && sectors != null && sectors.length > 0) {
            for (Sector sector : sectors) {
                for (CaseJob job : filteredJobs1) {
                    if (job.getSector() != null && job.getSector().equals(sector))
                        filteredJobs2.add(job);
                }
            }
        }

        // Find jobs contain selected georegions
        List<CaseJob> filteredJobs3 = new ArrayList<CaseJob>();
        List<CaseJob> toFilter = (filteredJobs2.size() == 0) ? filteredJobs1 : filteredJobs2;

        if (toFilter.size() > 0 && region != null) {
            for (CaseJob job : toFilter) {
                GeoRegion temp = job.getRegion();
                
                if (temp != null && !temp.equals(region) && !temp.equals(GeoRegion.generic_grid)) continue;
                    
                if (temp != null) job.setRegion(region);
                    
                filteredJobs3.add(job);
            }
        }

        if (filteredJobs3.size() > 0)
            return filteredJobs3.toArray(new CaseJob[0]);

        return filteredJobs1.toArray(new CaseJob[0]);
    }

    public EmfSession getSession() {
        return this.session;
    }

    public void doDisplaySetCaseWindow(Case newCase, String title, EmfConsole parentConsole,
            DesktopManager desktopManager, CaseManagerPresenter parentPresenter, List<CaseInput> existingInputs,
            List<CaseParameter> existingParas) throws EmfException {
        SetCaseView view = new SetCaseWindow(title, parentConsole, desktopManager, existingInputs, existingParas);
        SetCasePresenter presenter = new SetCasePresenterImpl(newCase, view, session, parentPresenter);
        presenter.display();
    }

    public Case[] getSensitivityCases(int parentCaseId) throws EmfException {
        return service().getSensitivityCases(parentCaseId);
    }

    public Object[] getJobGroups(Case selectedCase) throws EmfException {
        return service().getJobGroups(selectedCase.getId());
    }

    public CaseInput[] getCaseInput(int caseId, Sector sector, boolean showAll) throws EmfException {
        return service().getCaseInputs(defaultPageSize, caseId, sector, "", showAll);
    }

    public CaseParameter[] getCaseParameters(int caseId, Sector sector, boolean showAll) throws EmfException {
        // return service().getCaseParameters(defaultPageSize, caseId, sector, showAll);
        if (sector == null)
            return new CaseParameter[0];

        if (sector.compareTo(new Sector("All", "All")) == 0)
            sector = null; // to trigger select all on the server side

        // return service().getcasgetCaseParameters(caseId, sector, showAll);
        return service().getCaseParameters(defaultPageSize, caseId, sector, "", showAll);
    }

    public String[] getGridNameValues(String[] names, int caseId, int modelId) throws EmfException {
        if (modelId <= 0)
            throw new EmfException("Please specify parent case run model.");

        String[] namevalues = new String[names.length];

        for (int i = 0; i < names.length; i++) {
            ParameterEnvVar var = new ParameterEnvVar(names[i].toUpperCase().replace(' ', '_'));
            var.setModelToRunId(modelId);
            CaseParameter param = service().getCaseParameter(caseId, var);
            String value = (param == null || param.getValue() == null || param.getValue().trim().isEmpty() ? "Not available"
                    : param.getValue());
            namevalues[i] = names[i] + " (" + value + ")";
        }

        return namevalues;

    }

}

package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;
import gov.epa.emissions.framework.services.sms.SectorScenarioService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditSectorScenarioPresenterImpl  implements EditSectorScenarioPresenter {

    protected EditSectorScenarioView view; 
    protected EditSectorScenarioSummaryTabView summaryTabView;
    private EditSectorScenarioInputsTabView inputsTabView;
    private EditSectorScenarioOptionsTabView optionsTabView;
    private EditSectorScenarioOutputsTabView outputsTabView;
    protected EmfSession session;

    //protected SectorScenarioManagerPresenter managerPresenter;
    
    protected List presenters;

    private EditSectorScenarioSummaryTabPresenter summaryTabPresenter;
    
    private EditSectorScenarioInputsTabPresenter inputsTabPresenter;
    
    private EditSectorScenarioOptionsTabPresenter optionsTabPresenter;
    
    private EditSectorScenarioOutputsTabPresenter outputsTabPresenter;

    protected SectorScenario sectorScenario;
    
    public EditSectorScenarioPresenterImpl(SectorScenario sectorScenario, EmfSession session, 
            EditSectorScenarioView view ) {
        this.session = session;
        this.view = view;
        this.sectorScenario = sectorScenario;
        this.presenters = new ArrayList();
    }

    public void doDisplay() throws EmfException {
        view.observe(this);

        //  make sure the editor is EITHER the admin or creator   
        
        if (!sectorScenario.getCreator().equals(session.user()) && !session.user().isAdmin()) {// view mode, locked by another user
            view.notifyEditFailure(sectorScenario);
            return;
        }
        
        sectorScenario = session.sectorScenarioService().obtainLocked(session.user(), sectorScenario.getId());
        
        if (!sectorScenario.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(sectorScenario);
            return;
        }
       
        view.display(sectorScenario);
    }

    public void doClose() throws EmfException {
        service().releaseLocked(session.user(), sectorScenario.getId());
        closeView();
    }
    
    private void closeView() {
        view.disposeView();
    }   

    public SectorScenarioOutput[] getOutputs() throws EmfException {
        return service().getSectorScenarioOutputs(sectorScenario.getId());
    }

    public void set(EditSectorScenarioSummaryTabView view) {
        this.summaryTabView = view;
        this.summaryTabPresenter = new EditSectorScenarioSummaryTabPresenterImpl(session, summaryTabView);
        view.observe(summaryTabPresenter);
        presenters.add(summaryTabPresenter);
    }
    
    public void set(EditSectorScenarioInputsTabView view) {
        this.inputsTabView = view;
        this.inputsTabPresenter = new EditSectorScenarioInputsTabPresenterImpl(session, inputsTabView);
        view.observe(inputsTabPresenter);
        presenters.add(inputsTabPresenter);
    }
    
    public void set(EditSectorScenarioOptionsTabView view) {
        this.optionsTabView = view;
        this.optionsTabPresenter = new EditSectorScenarioOptionsTabPresenter(session, optionsTabView);
        view.observe(optionsTabPresenter);
        presenters.add(optionsTabPresenter);
    }
    
    public void set(EditSectorScenarioOutputsTabView view) {
        this.outputsTabView = view;
        this.outputsTabPresenter = new EditSectorScenarioOutputsTabPresenter(session, outputsTabView);
        view.observe(outputsTabPresenter);
        presenters.add(outputsTabPresenter);
    }   
       
    public void doRefresh() throws EmfException {
        SectorScenario ss = service().getById(sectorScenario.getId());
        SectorScenarioOutput[] ssO = service().getSectorScenarioOutputs(ss.getId());
        
        view.refresh(ss, ssO);
        
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            EditSectorScenarioTabPresenter element = (EditSectorScenarioTabPresenter) iter.next();
            element.doRefresh(ss, ssO);
        }
    }

    public DatasetType getDatasetType(String name) {
        return session.getLightDatasetType(name);
    }
    
    public Version[] getVersions(EmfDataset dataset) throws EmfException 
    {
        if (dataset == null) {
            return new Version[0];
        }
        return session.dataEditorService().getVersions(dataset.getId());
    }

    public void doLoad(String tabTitle) {
        // NOTE Auto-generated method stub
        
    }
    
    public EmfSession getSession(){
        return session;
    }

    public void doSave(SectorScenario sectorScenario) throws EmfException {
        saveTabs(sectorScenario);
        validateNameAndAbbre(sectorScenario);       
        sectorScenario=service().updateSectorScenarioWithLock(sectorScenario);
    }
    
    private void saveTabs(SectorScenario sectorScenario) throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            EditSectorScenarioTabPresenter element = (EditSectorScenarioTabPresenter) iter.next();
            element.doSave(sectorScenario);
        }
    }
    
    private void validateNameAndAbbre(SectorScenario sectorScenario) throws EmfException {
        // emptyName
        String name = sectorScenario.getName();
        String abbre = sectorScenario.getAbbreviation();
        if (name.trim().equals(""))
            throw new EmfException("Empty string is not allowed for the name.");

        if (abbre.trim().equals(""))
            throw new EmfException("Empty string is not allowed for the abbre.");
    }
    
    private SectorScenarioService service(){    
        return session.sectorScenarioService();
    }

    public Project[] getProjects() throws EmfException {
        return session.getProjects();
    }

    public void fireTracking() {
        view.signalChanges();
    }
    
    public EmfDataset getDataset(int id) throws EmfException {
        return session.dataService().getDataset(id);
    }

    public void runSectorScenario(int sectorScenarioId) throws EmfException {
        session.sectorScenarioService().runSectorScenario(session.user(), sectorScenarioId);
    }

    public void stopRunningSectorScenario(int sectorScenarioId) throws EmfException {
        session.sectorScenarioService().stopRunSectorScenario(sectorScenarioId);
    }
}

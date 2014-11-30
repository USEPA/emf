package gov.epa.emissions.framework.client.tempalloc.editor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocationService;

public class TemporalAllocationPresenter {

    private EmfSession session;
    
    private TemporalAllocationView view;
    
    private TemporalAllocation temporalAllocation;
    
    private List<TemporalAllocationTabPresenter> tabPresenters;
    
    private boolean viewOnly;
    
    public final String SUMMARY_TAB = "Summary";
    public final String INVENTORIES_TAB = "Inventories";
    public final String TIMEPERIOD_TAB = "Time Period";
    public final String PROFILES_TAB = "Profiles";
    public final String OUTPUT_TAB = "Output";
    
    public TemporalAllocationPresenter(TemporalAllocation temporalAllocation, EmfSession session, 
            TemporalAllocationView view) {
        this.temporalAllocation = temporalAllocation;
        this.session = session;
        this.view = view;
        this.tabPresenters = new ArrayList<TemporalAllocationTabPresenter>();
        this.viewOnly = false;
    }
    
    public TemporalAllocationPresenter(TemporalAllocation temporalAllocation, EmfSession session,
            TemporalAllocationView view, boolean viewOnly) {
        this(temporalAllocation, session, view);
        this.viewOnly = viewOnly;
    }
    
    public void doDisplay() throws EmfException {
        view.observe(this);
        
        if (isEditing()) {
            temporalAllocation = service().obtainLocked(session.user(), temporalAllocation.getId());
            
            if (!temporalAllocation.isLocked(session.user())) {
                view.notifyLockFailure(temporalAllocation);
                return;
            }
        } else {
            // retrieve fresh copy from database
            temporalAllocation = service().getById(temporalAllocation.getId());
        }
        
        view.display(temporalAllocation);
    }
    
    public void doDisplayNew() {
        view.observe(this);
        view.display(temporalAllocation);
    }

    public void doClose() throws EmfException {
        if (isEditing() && temporalAllocation.getId() != 0)
            service().releaseLocked(session.user(), temporalAllocation.getId());
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave() throws EmfException {
        saveTabs();
        temporalAllocation.setLastModifiedDate(new Date());
        
        // check for duplicate name
        String name = temporalAllocation.getName();
        int id = service().isDuplicateName(name);
        if (id != 0 && temporalAllocation.getId() != id) {
            throw new EmfException("A Temporal Allocation named '" + name + "' already exists.");
        }
        
        if (temporalAllocation.getId() == 0) {
            id = service().addTemporalAllocation(temporalAllocation);
            temporalAllocation = service().obtainLocked(session.user(), id);
        } else {
            temporalAllocation = service().updateTemporalAllocationWithLock(temporalAllocation);
        }
        updateTabs(temporalAllocation);
    }

    protected void saveTabs() throws EmfException {
        for (TemporalAllocationTabPresenter element : tabPresenters) {
            element.doSave();
        }
    }
    
    private void updateTabs(TemporalAllocation temporalAllocation) {
        for (TemporalAllocationTabPresenter element : tabPresenters) {
            element.updateView(temporalAllocation);
        }
    }
    
    private void refreshTabs() {
        for (TemporalAllocationTabPresenter element : tabPresenters) {
            element.doRefresh();
        }
    }
    
    private TemporalAllocationService service() {
        return session.temporalAllocationService();
    }
    
    public void set(String tabName, TemporalAllocationTabView view) throws EmfException {
        TemporalAllocationTabPresenter presenter = null;
        if (SUMMARY_TAB.equals(tabName)) {
            presenter = new TemporalAllocationSummaryTabPresenter(view);
        } else if (INVENTORIES_TAB.equals(tabName)) {
            presenter = new TemporalAllocationInventoriesTabPresenter(view);
        } else if (TIMEPERIOD_TAB.equals(tabName)) {
            presenter = new TemporalAllocationTimePeriodTabPresenter(view);
        } else if (PROFILES_TAB.equals(tabName)) {
            presenter = new TemporalAllocationProfilesTabPresenter(view);
        } else if (OUTPUT_TAB.equals(tabName)) {
            presenter = new TemporalAllocationOutputTabPresenter(view);
        }
        if (presenter != null) {
            presenter.doDisplay();
            tabPresenters.add(presenter);
        }
    }
    
    public void runTemporalAllocation() throws EmfException {
        service().runTemporalAllocation(session.user(), temporalAllocation);
    }
    
    public Version[] getVersions(EmfDataset dataset) throws EmfException 
    {
        if (dataset == null) {
            return new Version[0];
        }
        return session.dataEditorService().getVersions(dataset.getId());
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
    
    public String getTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        String tableName = "";
        if (internalSources.length > 0)
            tableName = internalSources[0].getTable();
        return tableName;
    }
    
    public void doRefresh() throws EmfException {
        temporalAllocation = service().getById(temporalAllocation.getId());
        updateTabs(temporalAllocation);
        refreshTabs();
        view.refresh(temporalAllocation);
    }
    
    public void doPrepareRun() throws EmfException {
        for (TemporalAllocationTabPresenter element : tabPresenters) {
            element.doPrepareRun();
        }
    }
    
    public boolean isEditing() {
        return !viewOnly;
    }
}

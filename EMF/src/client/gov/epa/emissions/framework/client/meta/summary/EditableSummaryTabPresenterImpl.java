package gov.epa.emissions.framework.client.meta.summary;

import java.awt.Cursor;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class EditableSummaryTabPresenterImpl implements EditableSummaryTabPresenter {

    private EditableSummaryTabView view;

    private EmfDataset dataset;
    
    private EmfSession session;

    public EditableSummaryTabPresenterImpl(EmfDataset dataset, EditableSummaryTabView view, EmfSession session) {
        this.dataset = dataset;
        this.view = view;
        this.session = session; 
        view.observe(this);
    }

    public void doSave() throws EmfException {
        view.save(dataset);
        verifyEmptyName(dataset);

    }

    private void verifyEmptyName(EmfDataset dataset) throws EmfException {
        if (dataset.getName().trim().equals("")) {
            throw new EmfException("Name field should be a non-empty string.");
        }
    }   

    public void checkIfLockedByCurrentUser() throws EmfException{
        EmfDataset reloaded = session.dataService().getDataset(dataset.getId());
        if (!reloaded.isLocked())
            throw new EmfException("Lock on current dataset object expired. " );  
        if (!reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current dataset object expired. User " + reloaded.getLockOwner()
                    + " has it now.");    
    }

    public EmfDataset reloadDataset() throws EmfException {
        return session.dataService().getDataset(dataset.getId());
    }
    
    public Version[] getVersions() throws EmfException{
        return session.dataEditorService().getVersions(dataset.getId());
    }

    @Override
    public Project[] getProjects() {
        return session.getProjects();
    }

    @Override
    public Object[] saveProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void saveDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public Object[] swProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void swDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub     
    }

    @Override
    public Object[] refreshProcessData() throws EmfException {
        reloadDataset();
        return getVersions();
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.doRefresh(dataset, (Version[]) objs);
    }

}

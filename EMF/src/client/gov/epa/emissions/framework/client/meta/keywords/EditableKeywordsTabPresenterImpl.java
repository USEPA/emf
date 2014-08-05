package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;

import java.util.Set;
import java.util.TreeSet;

public class EditableKeywordsTabPresenterImpl implements EditableKeywordsTabPresenter {

    private EditableKeywordsTabView view;

    private EmfDataset dataset;
    
    private Keywords masterKeywords;
    
    private EmfSession session; 

    public EditableKeywordsTabPresenterImpl(EditableKeywordsTabView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.session = session; 
    }

    public void display(Keywords masterKeywords) {
        this.masterKeywords = masterKeywords;
        view.observe(this);
        view.display(dataset, masterKeywords);
    }


    public void doSave() throws EmfException {
        KeyVal[] updates = view.updates();
        verifyDuplicates(updates);

        dataset.setKeyVals(updates);
    }

    private void verifyDuplicates(KeyVal[] updates) throws EmfException {
        Set set = new TreeSet();
        for (int i = 0; i < updates.length; i++) {
            String name = updates[i].getKeyword().getName();
            if (!set.add(name))
                throw new EmfException("duplicate keyword '" + name + "'");
        }
    }

    public void checkIfLockedByCurrentUser() throws EmfException {
        EmfDataset reloaded = session.dataService().getDataset(dataset.getId());
        if (!reloaded.isLocked())
            throw new EmfException("Lock on current dataset object expired. " );  
        if (!reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current dataset object expired. User " + reloaded.getLockOwner()
                    + " has it now.");        
    }

    @Override
    public Object[] saveProcessData() throws EmfException {
        return null;
    }

    @Override
    public void saveDisplay(Object[] objs) throws EmfException {
        //
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
        this.dataset = session.dataService().getDataset(dataset.getId());
        return null;
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.doRefresh(dataset, masterKeywords);
    }

}

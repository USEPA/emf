package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.List;

public class DatasetTypesManagerPresenter implements RefreshObserver {

    private DatasetTypesManagerView view;

    private EmfSession session;

    public DatasetTypesManagerPresenter(EmfSession session, DatasetTypesManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(service().getDatasetTypes());
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

    public void doClose() {
        view.disposeView();
    }

    public void doEdit(DatasetType type, EditableDatasetTypeView editable, ViewableDatasetTypeView viewable)
            throws EmfException {
        EditableDatasetTypePresenter p = new EditableDatasetTypePresenterImpl(session, editable, viewable, type);
        edit(p);
    }

    void edit(EditableDatasetTypePresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    public void doView(DatasetType type, ViewableDatasetTypeView viewable) throws EmfException {
        ViewableDatasetTypePresenter p = new ViewableDatasetTypePresenterImpl(viewable, type);
        view(p);
    }

    void view(ViewableDatasetTypePresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    public void displayNewDatasetTypeView(NewDatasetTypeView view) {
        NewDatasetTypePresenter presenter = new NewDatasetTypePresenter(session, view);
        presenter.doDisplay();
    }
    
    public void doRemove(DatasetType[] types) throws EmfException {

        DatasetType[] lockedTypes = getLockedTypes(types);
        
        if (lockedTypes == null)
            return;
        
        try {
            service().deleteDatasetTypes(session.user(), lockedTypes);
        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        } finally {
            releaseLocked(lockedTypes);
        }
    }
    
    private DatasetType[] getLockedTypes(DatasetType[] types) throws EmfException{
        List<DatasetType> lockedList = new ArrayList<DatasetType>();
        for (int i=0; i < types.length; i++){
            DatasetType locked = service().obtainLockedDatasetType(session.user(), types[i]);
            if (locked == null) {
                releaseLocked(lockedList.toArray(new DatasetType[0]));
                return null;
            }
            lockedList.add(locked);
        }
        return lockedList.toArray(new DatasetType[0]);
    }
    
    private void releaseLocked(DatasetType[] lockedTypes) {
        if (lockedTypes.length == 0)
            return;
        
        for(int i = 0; i < lockedTypes.length; i++) {
            try {
                service().releaseLockedDatasetType(session.user(), lockedTypes[i]);
            } catch (Exception e) { //so that it go release locks continuously
                e.printStackTrace();
            }
        }
    }

    public void doRefresh() throws EmfException {
        view.refresh(service().getDatasetTypes());
    }

}

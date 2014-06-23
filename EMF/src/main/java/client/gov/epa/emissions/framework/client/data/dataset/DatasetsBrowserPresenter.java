package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportView;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportView;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditorView;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenterImpl;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataPresenter;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.List;

public class DatasetsBrowserPresenter implements RefreshObserver {

    private DatasetsBrowserView view;

    private EmfSession session;

    public DatasetsBrowserPresenter(EmfSession session) {
        this.session = session;
    }
    
    public EmfSession getSession(){
        return session; 
    }

    public void doDisplay(DatasetsBrowserView view) throws EmfException {
        this.view = view;
        view.observe(this);
        view.display(new EmfDataset[0]);
    }

    public void doClose() {
        view.disposeView();
    }

    public void doExport(ExportView exportView, ExportPresenter presenter, EmfDataset[] datasets) {
        if (datasets.length == 0) {
            view.showMessage("To Export, you will need to select at least one Dataset"); // external type allowed to export now
            return;
        }

        view.clearMessage();
        presenter.display(exportView);
    }

    public void doRefresh() throws EmfException {
        view.refresh(getDatasets(view.getNameContains(), session.user().getId()));
        view.clearMessage();
    }
    
    public void refreshViewOnSearch(EmfDataset[] datasets, DatasetType type, String nameFilter) {
        //DatasetType dsType = getDstype(datasets, type);
        view.setDSTypeSelection(0);
        view.refresh(datasets);
        view.clearMessage();
    }
    
    public DatasetType getDstype(EmfDataset[] datasets, DatasetType type) {
        if (datasets.length == 0)
            return type;
        
        DatasetType temp = datasets[0].getDatasetType();
        
        for (EmfDataset ds : datasets)
            if (!ds.getDatasetType().equals(temp))
                return new DatasetType("All");
            
        return temp;
    }

    public DatasetType[] getDSTypes() {
//        long start = System.currentTimeMillis();
//        System.out.println("getDSTypes.start " + start);
//        DatasetType[] dSTypes = session.getLightDatasetTypes();
//        System.out.println("getDSTypes.stop light " + (System.currentTimeMillis() - start) + " ms");
////        start = System.currentTimeMillis();
////        DatasetType[] dSTypes = session.dataCommonsService().getDatasetTypes(session.user().getId());
//        System.out.println("getDSTypes.stop not light " + (System.currentTimeMillis() - start) + " ms");
        
        return session.getLightDatasetTypes();//dSTypes//session.dataCommonsService().getLightDatasetTypes(session.user().getId());
    }
    
    public User[] getUsers() throws EmfException {
        return session.userService().getUsers();
    }
    
    private EmfDataset[] getDatasets(String nameContains, int userId) throws EmfException {
        return dataService().getDatasets(nameContains, userId);
    }

    private DataService dataService() {
        return session.dataService();
    }
    
    public User getUser() {
        return session.user();
    }

    public void doDisplayPropertiesEditor(DatasetPropertiesEditorView propertiesEditorView, EmfDataset dataset)
            throws EmfException {
        //pass in the full object, not the light version stored in the manager table
        dataset = getDataset(dataset.getId());
        PropertiesEditorPresenter presenter = new PropertiesEditorPresenterImpl(dataset, propertiesEditorView, session);
        doDisplayPropertiesEditor(presenter);
    }

    void doDisplayPropertiesEditor(PropertiesEditorPresenter presenter) throws EmfException {
        view.clearMessage();
        presenter.doDisplay();
    }

    public void doImport(ImportView importView, ImportPresenter importPresenter) {
        view.clearMessage();
        importPresenter.display(importView);
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        //pass in the full object, not the light version stored in the manager table
        dataset = getDataset(dataset.getId());
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public void doDisplayVersionedData(VersionedDataView versionsView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        //pass in the full object, not the light version stored in the manager table
        dataset = getDataset(dataset.getId());
        VersionedDataPresenter presenter = new VersionedDataPresenter(dataset, session);
        presenter.display(versionsView);
    }

    public void doDeleteDataset(EmfDataset[] datasets) throws EmfException {
        view.clearMessage();
        EmfDataset[] lockedDatasets = getLockedDatasets(datasets);
        
        if (lockedDatasets == null)
            return;
        
        try {
            dataService().deleteDatasets(getUser(), lockedDatasets);
        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        } finally {
            releaseLocked(lockedDatasets);
        }
    }
    
    private EmfDataset[] getLockedDatasets(EmfDataset[] datasets) throws EmfException {
        List<EmfDataset> lockedList = new ArrayList<EmfDataset>();
        
        for (int i = 0; i < datasets.length; i++) {
            EmfDataset locked = obtainDatasetLocks(datasets[i]);
            
            if (locked == null) {
                releaseLocked(lockedList.toArray(new EmfDataset[0]));
                return null;
            }
            
            lockedList.add(locked);
        }
        
        return lockedList.toArray(new EmfDataset[0]);
    }

    private EmfDataset obtainDatasetLocks(EmfDataset dataset) throws EmfException {
        EmfDataset locked = dataService().obtainLockedDataset(getUser(), dataset);
        if (!locked.isLocked(getUser())) {// view mode, locked by another user
            view.notifyLockFailure(dataset);
            return null;
        }
        
        return locked;
    }
    
    private void releaseLocked(EmfDataset[] lockedDatasets) {
        if (lockedDatasets.length == 0)
            return;
        
        for(int i = 0; i < lockedDatasets.length; i++) {
            try {
                dataService().releaseLockedDataset(session.user(), lockedDatasets[i]);
            } catch (Exception e) { //so that it go release locks continuously
                e.printStackTrace();
            }
        }
    }

    public EmfDataset[] getEmfDatasets(DatasetType type, String nameContains) throws EmfException {
        if (type.getName().equalsIgnoreCase("Select one"))
            return new EmfDataset[0];
        
        if (type.getName().equalsIgnoreCase("All"))
            return getDatasets(nameContains, session.user().getId());
        
        return dataService().getDatasetsWithFilter(type.getId(), nameContains);
    }
    
    public EmfDataset[] advSearch4Datasets(EmfDataset ds, String qaStep, String qaArgument, int[] usedByCasesId, String dataValueFilter, boolean unconditional) throws EmfException {
        return dataService().findDatasets(ds, qaStep, qaArgument, usedByCasesId, 
                dataValueFilter, unconditional, session.user().getId());
    }

    public void purgeDeletedDatasets() throws EmfException {
        dataService().purgeDeletedDatasets(getUser());
    }
    
    public EmfDataset getDataset(int datasetId) throws EmfException {
        return dataService().getDataset(datasetId);
    }
    
    public int getNumOfDeletedDatasets() throws EmfException {
        return dataService().getNumOfDeletedDatasets(getUser());
    }
    
    public int getNumOfDatasets(DatasetType type, String nameContains) throws EmfException {
        if (type.getName().equalsIgnoreCase("Select one"))
            return 0;
        
        if (type.getName().equalsIgnoreCase("All"))
            return dataService().getNumOfDatasets(nameContains, session.user().getId());
        
        return dataService().getNumOfDatasets(type.getId(), nameContains);
    }

    public boolean isAdminUser() {
        User user = session.user();
        
        return user.getUsername().equals("admin") && user.isAdmin();
    }
    
    public EmfDataset[] getSelected() {
        return view.getSelected();
    }

    public Keyword[] getKeywords() throws EmfException {
        return session.dataCommonsService().getKeywords();
    }
    
    public synchronized Project[] getProjects() throws EmfException {
        return session.getProjects();
    }

    public DatasetType getDSType() {
        return view.getSelectedDSType();
    }
    
    public void notifyAdvancedSearchOff() {
        view.notifyAdvancedSearchOff();
    }
    
}

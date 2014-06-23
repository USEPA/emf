package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.viewer.DataView;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.DoubleValue;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;

import java.util.ArrayList;
import java.util.List;

public class AppendDataViewPresenter {
    
    private EmfDataset dataset;
    
    private EmfSession session;
    
    private AppendDataWindowView view;
    
    private DataService dataService;
    
    private User user;

    public AppendDataViewPresenter(EmfDataset dataset, AppendDataWindowView view, EmfSession session) {
        //super(dataset, session);
        this.dataset=dataset;
        this.session = session;
        this.view = view;
        this.dataService = session.dataService();
        this.user = session.user();
    }
    
    public void displayView() {
        view.observe(this);
        view.display();
    }
    
    public void appendData(int srcDSid, int srcDSVersion, String filter, int targetDSid, int targetDSVersion, DoubleValue startLineNum) throws EmfException {
        dataService.appendData(session.user(),srcDSid, srcDSVersion, filter, targetDSid, targetDSVersion, startLineNum);
    }
    
    public Version[] getVersions(int dsId) throws EmfException {
        return session.dataEditorService().getVersions(dsId);
    }
    
    public EmfDataset getDataset(int datasetId) throws EmfException{
        return dataService.getDataset(datasetId);
    }
    
    public Version[] getTargetDatasetNonFinalVersions() throws EmfException {
        List<Version> nonFinalVersions = new ArrayList<Version>();
        
        Version[] allVersions = getVersions(dataset.getId());
        
        for(Version version : allVersions)
            if (!version.isFinalVersion())
                nonFinalVersions.add(version);
        
        return nonFinalVersions.toArray(new Version[0]);
    }
    
    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
    
    public void doView(Version version, String table, DataView view) throws EmfException {
        if (!version.isFinalVersion())
            throw new EmfException("Cannot view a Version that is not Final. Please choose edit for Version "+
                    version.getName());

        DataViewPresenter presenter = new DataViewPresenter(dataset, version, table, view, session);
        presenter.display();
    }

    public boolean isLineBased(){
        String importerClass = dataset.getDatasetType().getImporterClassName();
        
      return importerClass.equals("gov.epa.emissions.commons.io.generic.LineImporter");
    }
    
    public EmfDataset getDataset() {
        return this.dataset;
    }
    
    public EmfSession getSession() {
        return this.session;
    }
    
    public void addRevision(Revision revision) throws EmfException {
        session.dataCommonsService().addRevision(revision);
    }
    
    public void checkIfDeletable(EmfDataset localDataset) throws EmfException {
        if (localDataset.getId() == this.dataset.getId())
            throw new EmfException("Cannot delete current target dataset.");
        
        String currentUser = user.getUsername();
        
        if (!currentUser.equals(localDataset.getCreator()))
            throw new EmfException("Current user is not the creator.");
               
        dataService.checkIfDeletable(user, localDataset.getId());
    }

    public EmfDataset getDataset(String sourceDSName) throws EmfException {
        return dataService.getDataset(sourceDSName);
    }
    
    public void deleteDataset(EmfDataset dataset) throws EmfException {
        EmfDataset locked = dataService.obtainLockedDataset(user, dataset);
        
        if (!locked.isLocked(user)) // view mode, locked by another user
            throw new EmfException("Couldn't get a lock to remove dataset.");
        
        try {
            dataService.deleteDatasets(user, new EmfDataset[]{locked});
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            dataService.releaseLockedDataset(user, locked);
        }
    }
    
    public boolean checkTableDefinitions(EmfDataset srcDS, EmfDataset targetDS) throws EmfException {
        boolean sameDefinition = false;
        
        try {
            sameDefinition = dataService.checkTableDefinitions(srcDS.getId(), targetDS.getId());
            return sameDefinition;
        } catch (Exception e) {
            throw new EmfException("Please check your source dataset table columns carefully before append: " + e.getMessage());
        } 
    }
}

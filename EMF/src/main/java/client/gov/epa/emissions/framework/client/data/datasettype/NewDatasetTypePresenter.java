package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.util.Date;
import java.util.HashMap;

public class NewDatasetTypePresenter {
       
    private NewDatasetTypeView view;

    private EmfSession session;

    private HashMap<String, String> mapImport, mapExport;

    public NewDatasetTypePresenter(EmfSession session, NewDatasetTypeView view) {
        this.session = session;
        this.view = view;
        mapImport = new HashMap<String, String>();
        mapExport = new HashMap<String, String>();
        setMap();
    }

    private void setMap() {
        mapImport.put(DatasetType.EXTERNAL, DatasetType.EXTERNAL_IMPORTER);
        mapImport.put(DatasetType.CSV, DatasetType.CSV_IMPORTER);
        mapImport.put(DatasetType.LINE_BASED, DatasetType.LINE_IMPORTER);
        mapImport.put(DatasetType.SMOKE, DatasetType.SMOKE_IMPORTER);
        mapImport.put(DatasetType.FLEXIBLE, DatasetType.FLEXIBLE_IMPORTER);
        
        mapExport.put(DatasetType.EXTERNAL, DatasetType.EXTERNAL_EXPORTER);
        mapExport.put(DatasetType.CSV, DatasetType.CSV_EXPORTER);
        mapExport.put(DatasetType.LINE_BASED, DatasetType.LINE_EXPORTER);
        mapExport.put(DatasetType.SMOKE, DatasetType.SMOKE_EXPORTER);
        mapExport.put(DatasetType.FLEXIBLE, DatasetType.FLEXIBLE_EXPORTER);
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave(String name, String desc, String minfiles, String maxfiles, String type, XFileFormat fileFormat, String formatFile) throws EmfException {
        if (type.equals(DatasetType.FLEXIBLE))
            saveTypeWithFileFormat(name, desc, type, fileFormat, formatFile, new Date());
        else {
            DatasetType newType = setNewDatasetType(name, desc, minfiles, maxfiles, type, new Date());
            service().addDatasetType(newType);
        }
       
        closeView();
    }

    private DatasetType setNewDatasetType(String name, String desc, String minfiles, String maxfiles, 
            String type, Date date) {
        DatasetType newType = new DatasetType(name);
        newType.setDescription(desc);
        newType.setMinFiles(Integer.parseInt(minfiles));
        newType.setMaxFiles(Integer.parseInt(maxfiles));
        newType.setDefaultSortOrder("");
        if (type.equalsIgnoreCase(DatasetType.LINE_BASED))
            newType.setDefaultSortOrder("Line_Number");
        newType.setImporterClassName(mapImport.get(type));
        newType.setExporterClassName(mapExport.get(type));
        newType.setExternal(type.equalsIgnoreCase(DatasetType.EXTERNAL));
        
        newType.setCreationDate(date);
        newType.setLastModifiedDate(date);
        newType.setCreator(session.user());

        return newType;
    }
    
    private void saveTypeWithFileFormat(String name, String desc, String type, 
            XFileFormat fileFormat, String formatFile, Date date) throws EmfException {
        DatasetType newType = setNewDatasetType(name, desc, 1+"", 1+"", type, date);
        newType.setTablePerDataset(1);
        fileFormat.setCreator(session.user());
        fileFormat.setLastModifiedDate(date);
        fileFormat.setDateAdded(date);
        
        service().addDatasetTypeWithFileFormat(newType, fileFormat, formatFile);
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

}

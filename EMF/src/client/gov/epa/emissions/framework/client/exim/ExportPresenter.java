package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetVersion;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface ExportPresenter {

    void notifyDone();

    void display(ExportView view);

//    void doExportWithOverwrite(EmfDataset[] datasets, String folder, 
//            String rowFilters, String colOrders, String purpose, boolean overwrite) throws EmfException;

    void doExport(EmfDataset[] datasets, Version[] versions, String folder, 
            String prefix, String rowFilters, DatasetVersion filterDatasetVersion, String filterDatasetJoinCondition, String colOrders, String purpose, boolean overwrite, boolean download) throws EmfException;

    void setLastFolder( String lastfolder);
    
    String getLastFolder();
    
    Version[] getVersions(EmfDataset dataset) throws EmfException;
}
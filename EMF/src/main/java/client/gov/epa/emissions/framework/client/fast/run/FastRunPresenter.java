package gov.epa.emissions.framework.client.fast.run;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.fast.ExportPresenter;
import gov.epa.emissions.framework.client.fast.ExportView;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunTabView;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastOutputExportWrapper;

import java.util.List;

public interface FastRunPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void doRun() throws EmfException;

    void doRefresh() throws EmfException;

    void addTab(FastRunTabView view) throws EmfException;

    void fireTracking();

    boolean hasResults();

    DatasetType getDatasetType(String dataset) throws EmfException;

    Version[] getVersions(EmfDataset dataset) throws EmfException;

    void doViewData(int id) throws EmfException;

    void doExport(ExportView exportView, ExportPresenter presenter, List<FastOutputExportWrapper> outputExportWrappers)
            throws EmfException;

    void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException;
}

package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditorView;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface ViewControlStrategyOutputTabPresenter extends ViewControlStrategyTabPresenter {

    void doExport(EmfDataset[] datasets, String folder) throws EmfException;

    void doAnalyze(String controlStrategyName, EmfDataset[] datasets) throws EmfException;

    void doDisplay(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) throws EmfException;

    void setLastFolder(String folder);

    String folder();

    void doInventory(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults,
            String namePrefix) throws EmfException;

    void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException;

    void doDisplayPropertiesEditor(DatasetPropertiesEditorView editor, EmfDataset dataset) throws EmfException;

    long getTableRecordCount(EmfDataset dataset) throws EmfException;

    Version[] getVersions(int datasetId) throws EmfException;
}
package gov.epa.emissions.framework.client.cost.controlprogram.viewer;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface ControlProgramPresenter {

    void doDisplay() throws EmfException;

    void doDisplayNew();

    void doClose() throws EmfException;

    void setMeasuresTab(ControlProgramMeasuresTab view) throws EmfException;

    void setSummaryTab(ControlProgramSummaryTab view) throws EmfException;

    void setTechnologiesTab(ControlProgramTechnologiesTab view) throws EmfException;

    void fireTracking();

    DatasetType getDatasetType(String name) throws EmfException;

    Version[] getVersions(EmfDataset dataset) throws EmfException;

    EmfDataset[] getDatasets(DatasetType type) throws EmfException;

    EmfDataset getDataset(int id) throws EmfException;

    boolean hasResults();
}

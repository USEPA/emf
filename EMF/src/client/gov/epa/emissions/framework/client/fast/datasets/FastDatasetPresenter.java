package gov.epa.emissions.framework.client.fast.datasets;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface FastDatasetPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void fireTracking();

    boolean hasResults();

    DatasetType getDatasetType(String dataset) throws EmfException;

    Version[] getVersions(EmfDataset dataset) throws EmfException;
}

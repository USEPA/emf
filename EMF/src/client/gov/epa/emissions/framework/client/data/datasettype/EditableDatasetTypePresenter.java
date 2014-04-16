package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.framework.services.EmfException;

public interface EditableDatasetTypePresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave(String name, String description, KeyVal[] keyVals, String sortOrder)
            throws EmfException;

}

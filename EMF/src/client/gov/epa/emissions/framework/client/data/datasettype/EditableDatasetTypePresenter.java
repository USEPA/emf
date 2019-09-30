package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.framework.services.EmfException;

public interface EditableDatasetTypePresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave(String name, String description, KeyVal[] keyVals, String sortOrder
            , QAStepTemplate[] QAStepTemps, Column[] columns)
            throws EmfException;

}

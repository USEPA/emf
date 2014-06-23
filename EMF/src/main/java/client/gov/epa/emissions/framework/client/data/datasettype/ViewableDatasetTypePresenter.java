package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.framework.services.EmfException;

public interface ViewableDatasetTypePresenter {

    public abstract void doDisplay() throws EmfException;

    public abstract void doClose();

}
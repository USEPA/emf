package gov.epa.emissions.framework.client.fast.datasets;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastDatasetWrapper;

public interface FastDatasetView extends ManagedView {

    void observe(FastDatasetPresenter presenter);

    void display(FastDatasetWrapper wrapper);

    void refresh(FastDatasetWrapper wrapper);

    void save(FastDatasetWrapper wrapper) throws EmfException;
    
    void signalChanges();

    void showError(String message);

    void showMessage(String message);

    void clearMessage();
}

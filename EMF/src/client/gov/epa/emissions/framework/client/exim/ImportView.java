package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.ManagedView;

public interface ImportView extends ManagedView {
    void register(ImportPresenter presenter);

    void setDefaultBaseFolder(String folder);

    void setMessage(String message);

}

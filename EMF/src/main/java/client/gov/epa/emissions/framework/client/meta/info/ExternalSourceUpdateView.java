package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.framework.client.ManagedView;

public interface ExternalSourceUpdateView extends ManagedView {

    void observe(ExternalSourceUpdatePresenter presenter);

    void setMostRecentUsedFolder(String mostRecentUsedFolder);

}

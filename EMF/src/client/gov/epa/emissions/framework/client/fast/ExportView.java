package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.client.ManagedView;

public interface ExportView extends ManagedView {

    void observe(ExportPresenter presenter);

    void setMostRecentUsedFolder(String mostRecentUsedFolder);

}

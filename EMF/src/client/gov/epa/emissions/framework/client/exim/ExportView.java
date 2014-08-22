package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.ManagedView;

public interface ExportView extends ManagedView {

    void observe(ExportPresenter presenter);

    void setMostRecentUsedFolder(String lastFolder);

}

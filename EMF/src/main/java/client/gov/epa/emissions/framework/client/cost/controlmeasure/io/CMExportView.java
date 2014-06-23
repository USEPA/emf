package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.framework.client.ManagedView;

public interface CMExportView extends ManagedView {

    void observe(CMExportPresenter presenter);

    void setMostRecentUsedFolder(String mostRecentUsedFolder);

}

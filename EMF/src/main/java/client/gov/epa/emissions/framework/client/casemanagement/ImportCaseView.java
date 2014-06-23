package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.ManagedView;

public interface ImportCaseView extends ManagedView {
    void register(ImportCasePresenter presenter);

    void setDefaultBaseFolder(String folder);

    void setMessage(String message);

}

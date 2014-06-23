package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.ManagedView;

public interface CaseDescriptionView extends ManagedView { // BUG3621

    void display();

    void observe(CaseDescriptionPresenter presenter);
}
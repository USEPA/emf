package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.ManagedView;

public interface NewCaseView extends ManagedView {

    void observe(NewCasePresenter presenter);

    void display();

}
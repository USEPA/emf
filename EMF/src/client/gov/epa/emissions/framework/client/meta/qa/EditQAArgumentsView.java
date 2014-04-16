package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.client.ManagedView;

public interface EditQAArgumentsView extends ManagedView {

    void display();

    void observe(EditQAArgumentsPresenter presenter);
}

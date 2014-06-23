package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

public interface WindowMenuView {

    void register(ManagedView view);

    void unregister(ManagedView view);

    void setWindowMenuViewPresenter(WindowMenuPresenter presenter);

    void addPermanently(ManagedView managedView);
}

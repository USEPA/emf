package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

public interface EmfDesktop {
    void add(ManagedView view);

    void ensurePresence(ManagedView view);
}

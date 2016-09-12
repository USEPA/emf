package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.ManagedView;

public interface HistoryDetailsView extends ManagedView {

    void observe(HistoryDetailsPresenter presenter);
}

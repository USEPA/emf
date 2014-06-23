package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.ManagedView;

public interface ViewableSectorView extends ManagedView {
    void observe(ViewableSectorPresenter presenter);

    void display(Sector sector);
}

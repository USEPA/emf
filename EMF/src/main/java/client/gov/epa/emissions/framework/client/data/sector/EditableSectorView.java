package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.ManagedView;

public interface EditableSectorView extends ManagedView {
    void observe(EditableSectorPresenter presenter);

    void display(Sector sector);
}

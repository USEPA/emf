package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.ManagedView;

public interface NewSectorView extends ManagedView {

    void observe(NewSectorPresenter presenter);

    void display(Sector sector);

    void disposeView();

}
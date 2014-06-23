package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;

public interface SectorsManagerView extends ManagedView {
    void observe(SectorsManagerPresenter presenter);

    void display(Sector[] sectors);

    EmfConsole getParentConsole();

    void refresh(Sector[] sectors);
}

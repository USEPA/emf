package gov.epa.emissions.framework.client.sms.sectorscenario.viewer;

import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioView;

public interface ViewSectorScenarioView extends EditSectorScenarioView {

    void observe(ViewSectorScenarioPresenter presenter);
    
    void viewOnly();
}

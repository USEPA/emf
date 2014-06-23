package gov.epa.emissions.framework.client.sms.sectorscenario;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.sms.sectorscenario.base.NewSectorScenarioView;
import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioView;
import gov.epa.emissions.framework.client.sms.sectorscenario.viewer.ViewSectorScenarioView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;

public interface SectorScenarioManagerPresenter {

    void display() throws EmfException;

    void doRefresh() throws EmfException;

    void doClose();

    void doNew(NewSectorScenarioView view) throws EmfException;

    void doEdit(EditSectorScenarioView view, SectorScenario sectorScenario) throws EmfException;

    void doView(ViewSectorScenarioView view, SectorScenario sectorScenario) throws EmfException;

    void doRemove(int[] ids) throws EmfException;
    
    void addNewSSToTableData(SectorScenario sectorScenario);

    void doSaveCopiedSectorScenarios(int id, User creator) throws EmfException;

}
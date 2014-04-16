package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;

public class EditSectorScenarioSummaryTabPresenterImpl implements EditSectorScenarioSummaryTabPresenter {
    
    protected EmfSession session;
    
    private EditSectorScenarioSummaryTabView view;
    
    public EditSectorScenarioSummaryTabPresenterImpl(EmfSession session, EditSectorScenarioSummaryTabView view) {
        this.session = session;
        this.view = view;
    }

    public void doSave(SectorScenario sectorScenario) throws EmfException{
        view.save(sectorScenario);
    }

    public Project[] getProjects() throws EmfException {
       return session.getProjects();
    }

    public void doRefresh(SectorScenario sectorScenario, SectorScenarioOutput[] sectorScenarioOutputs) throws EmfException {
        view.refresh(sectorScenario, null);     
    }


    public void doViewOnly() {
        view.viewOnly();  
    }
}

package gov.epa.emissions.framework.client.sms.sectorscenario;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.sms.sectorscenario.base.NewSectorScenarioPresenter;
import gov.epa.emissions.framework.client.sms.sectorscenario.base.NewSectorScenarioPresenterImpl;
import gov.epa.emissions.framework.client.sms.sectorscenario.base.NewSectorScenarioView;
import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioPresenter;
import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioPresenterImpl;
import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioView;
import gov.epa.emissions.framework.client.sms.sectorscenario.viewer.ViewSectorScenarioPresenter;
import gov.epa.emissions.framework.client.sms.sectorscenario.viewer.ViewSectorScenarioPresenterImpl;
import gov.epa.emissions.framework.client.sms.sectorscenario.viewer.ViewSectorScenarioView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenarioService;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class SectorScenarioManagerPresenterImpl implements RefreshObserver, SectorScenarioManagerPresenter {
    private SectorScenarioManagerView view;

    private EmfSession session;

    public SectorScenarioManagerPresenterImpl(EmfSession session, SectorScenarioManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void display() throws EmfException {
        view.observe(this);
        SectorScenario[] sectorScenarios= service().getSectorScenarios();
        view.display(sectorScenarios);
    }

    private SectorScenarioService service() {
        return session.sectorScenarioService();
    }

    public void doRefresh() throws EmfException {
        view.refresh(service().getSectorScenarios());
    }

    public void doClose() {
        view.disposeView();
    }

    public void doNew(NewSectorScenarioView view) throws EmfException {
        NewSectorScenarioPresenter presenter = new NewSectorScenarioPresenterImpl(view, session, this);
        presenter.display();

    }

    public void doEdit(EditSectorScenarioView view, SectorScenario sectorScenario) throws EmfException {
        EditSectorScenarioPresenter presenter = new EditSectorScenarioPresenterImpl(sectorScenario, session, view);
        displayEditor(presenter);
    }

    public void doView(ViewSectorScenarioView view, SectorScenario sectorScenario) throws EmfException {

        ViewSectorScenarioPresenter presenter = new ViewSectorScenarioPresenterImpl(sectorScenario, session, view);
        displayViewer(presenter);
    }

    void displayEditor(EditSectorScenarioPresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    void displayViewer(ViewSectorScenarioPresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    public void doRemove(int[] ids) throws EmfException {
        service().removeSectorScenarios(ids, session.user());
    }

    public void doSaveCopiedSectorScenarios(int id, User creator) throws EmfException {
        service().copySectorScenario(id, session.user());
    }
    
    public void addNewSSToTableData(SectorScenario sectorScenario){
        view.addNewSSToTableData(sectorScenario);
    }

}

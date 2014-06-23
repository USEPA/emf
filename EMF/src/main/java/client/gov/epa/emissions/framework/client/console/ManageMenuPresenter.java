package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.admin.UsersManagerPresenter;
import gov.epa.emissions.framework.client.admin.UsersManagerView;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerView;
import gov.epa.emissions.framework.client.cost.controlmeasure.ControlMeasuresManagerPresenter;
import gov.epa.emissions.framework.client.cost.controlmeasure.ControlMeasuresManagerView;
import gov.epa.emissions.framework.client.cost.controlprogram.ControlProgramManagerPresenter;
import gov.epa.emissions.framework.client.cost.controlprogram.ControlProgramManagerView;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategiesManagerPresenter;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategiesManagerPresenterImpl;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategyManagerView;
import gov.epa.emissions.framework.client.data.dataset.DatasetsBrowserPresenter;
import gov.epa.emissions.framework.client.data.dataset.DatasetsBrowserView;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypesManagerPresenter;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypesManagerView;
import gov.epa.emissions.framework.client.data.sector.SectorsManagerPresenter;
import gov.epa.emissions.framework.client.data.sector.SectorsManagerView;
import gov.epa.emissions.framework.client.fast.MPSDTManagerPresenter;
import gov.epa.emissions.framework.client.fast.MPSDTManagerPresenterImpl;
import gov.epa.emissions.framework.client.fast.MPSDTManagerView;
import gov.epa.emissions.framework.client.sms.sectorscenario.SectorScenarioManagerPresenter;
import gov.epa.emissions.framework.client.sms.sectorscenario.SectorScenarioManagerPresenterImpl;
import gov.epa.emissions.framework.client.sms.sectorscenario.SectorScenarioManagerView;
import gov.epa.emissions.framework.services.EmfException;

public class ManageMenuPresenter {

    private ManageMenuView view;

    private EmfSession session;

    public ManageMenuPresenter(ManageMenuView view, EmfSession session) {
        this.view = view;
        this.session = session;
    }

    public void observe() {
        view.observe(this);
    }

    public void doDisplayUserManager(UsersManagerView view) throws EmfException {
        UsersManagerPresenter presenter = new UsersManagerPresenter(session, session.userService());
        presenter.display(view);
    }

    public void doDisplayDatasetsBrowser(DatasetsBrowserView view) throws EmfException {
        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(session);
        presenter.doDisplay(view);
    }

    public void doDisplaySectors(SectorsManagerView view) throws EmfException {
        SectorsManagerPresenter presenter = new SectorsManagerPresenter(session, view, session.dataCommonsService());
        presenter.doDisplay();
    }

    public void doDisplayDatasetTypesManager(DatasetTypesManagerView view) throws EmfException {
        DatasetTypesManagerPresenter presenter = new DatasetTypesManagerPresenter(session, view);
        presenter.doDisplay();
    }

    public void doDisplayCases(CaseManagerView view) {
        new CaseManagerPresenterImpl(session, view).display();
    }

    public void doDisplayControlMeasuresManager(ControlMeasuresManagerView view) throws EmfException {
        ControlMeasuresManagerPresenter presenter = new ControlMeasuresManagerPresenter(session);
        presenter.doDisplay(view);
    }

    public void doDisplayControlStrategies(ControlStrategyManagerView view) throws EmfException {
        ControlStrategiesManagerPresenter presenter = new ControlStrategiesManagerPresenterImpl(session, view);
        presenter.display();
    }

    public void doDisplayControlPrograms(ControlProgramManagerView view) throws EmfException {
        ControlProgramManagerPresenter presenter = new ControlProgramManagerPresenter(session, view);
        presenter.display();
    }
    
    public void doDisplaySectorScenarios(SectorScenarioManagerView view) throws EmfException {
        SectorScenarioManagerPresenter presenter = new SectorScenarioManagerPresenterImpl(session,view);
        presenter.display();
    }

    public void doDisplayFast(MPSDTManagerView view) throws EmfException {

        MPSDTManagerPresenter presenter = new MPSDTManagerPresenterImpl(this.session, view);
        presenter.doDisplay();
    }

    public String getPropertyValue(String name) throws EmfException {
        return session.userService().getPropertyValue(name);
    }
}

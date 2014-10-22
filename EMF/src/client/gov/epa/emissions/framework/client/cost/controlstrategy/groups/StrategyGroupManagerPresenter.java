package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class StrategyGroupManagerPresenter implements RefreshObserver {

    private StrategyGroupManagerView view;

    private EmfSession session;
    
    public StrategyGroupManagerPresenter(EmfSession session, StrategyGroupManagerView view) {
        this.session = session;
        this.view = view;
    }
    
    public void display() throws EmfException {
        view.display(service().getStrategyGroups());
        view.observe(this);
    }
    
    private ControlStrategyService service() {
        return session.controlStrategyService();
    }
    
    public void doRefresh() throws EmfException {
        view.refresh(service().getStrategyGroups());
    }

    public void doClose() {
        view.disposeView();
    }
    
    public void doNew(StrategyGroupView view) {
        StrategyGroup group = new StrategyGroup();
        group.setName("");
        StrategyGroupPresenter presenter = new StrategyGroupPresenter(group, session, view);
        presenter.doDisplayNew();
    }
    
    public void doEdit(StrategyGroupView view, StrategyGroup group) throws EmfException {
        StrategyGroupPresenter presenter = new StrategyGroupPresenter(group, session, view);
        presenter.doDisplay();
    }
}

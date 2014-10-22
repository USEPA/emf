package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;

public class StrategyGroupPresenter {

    private EmfSession session;
    
    private StrategyGroupView view;
    
    private StrategyGroup strategyGroup;
    
    private List<StrategyGroupTabPresenter> tabPresenters;
    
    public final String STRATEGIES_TAB = "Control Strategies";
    public final String NOTES_TAB = "Name & Notes";
    
    public StrategyGroupPresenter(StrategyGroup strategyGroup, EmfSession session, StrategyGroupView view) {
        this.strategyGroup = strategyGroup;
        this.session = session;
        this.view = view;
        this.tabPresenters = new ArrayList<StrategyGroupTabPresenter>();
    }
    
    public void doDisplay() throws EmfException {
        view.observe(this);
        
        strategyGroup = service().obtainLockedGroup(session.user(), strategyGroup.getId());
        
        if (!strategyGroup.isLocked(session.user())) {
            view.notifyLockFailure(strategyGroup);
            return;
        }
        
        view.display(strategyGroup);
    }
    
    public void doDisplayNew() {
        view.observe(this);
        view.display(strategyGroup);
    }

    public void doClose() throws EmfException {
        if (strategyGroup.getId() != 0)
            service().releaseLockedGroup(session.user(), strategyGroup.getId());
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave() throws EmfException {
        saveTabs();
        
        // check for duplicate name
        String name = strategyGroup.getName();
        int id = service().isDuplicateGroupName(name);
        if (id != 0 && strategyGroup.getId() != id) {
            throw new EmfException("A Control Strategy Group named '" + name + "' already exists.");
        }
        
        if (strategyGroup.getId() == 0) {
            id = service().addStrategyGroup(strategyGroup);
            strategyGroup = service().obtainLockedGroup(session.user(), id);
        } else {
            strategyGroup = service().updateStrategyGroupWithLock(strategyGroup);
        }
        updateTabs(strategyGroup);
    }

    protected void saveTabs() throws EmfException {
        for (StrategyGroupTabPresenter element : tabPresenters) {
            element.doSave();
        }
    }
    
    private void updateTabs(StrategyGroup strategyGroup) {
        for (StrategyGroupTabPresenter element : tabPresenters) {
            element.updateView(strategyGroup);
        }
    }
    
    private ControlStrategyService service() {
        return session.controlStrategyService();
    }
    
    public void set(String tabName, StrategyGroupTabView view) throws EmfException {
        StrategyGroupTabPresenter presenter = null;
        if (STRATEGIES_TAB.equals(tabName)) {
            presenter = new StrategyGroupStrategiesTabPresenter(view);
        } else if (NOTES_TAB.equals(tabName)) {
            presenter = new StrategyGroupNotesTabPresenter(view);
        }
        if (presenter != null) {
            presenter.doDisplay();
            tabPresenters.add(presenter);
        }
    }
}

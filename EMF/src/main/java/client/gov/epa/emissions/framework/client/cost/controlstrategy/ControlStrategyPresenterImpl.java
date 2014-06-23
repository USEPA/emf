package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;

import java.util.Date;

public class ControlStrategyPresenterImpl implements ControlStrategyPresenter {

    private EmfSession session;

    private ControlStrategyView view;

    private ControlStrategiesManagerPresenter managerPresenter;

    public ControlStrategyPresenterImpl(EmfSession session, ControlStrategyView view,
            ControlStrategiesManagerPresenter controlStrategiesManagerPresenter) {
        this.session = session;
        this.view = view;
        this.managerPresenter = controlStrategiesManagerPresenter;
    }

    public void doDisplay() {
        view.observe(this, managerPresenter);
        view.display();
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public int doSave(ControlStrategy newControlStrategy) throws EmfException {
        validateName(newControlStrategy);

        newControlStrategy.setCreator(session.user());
        newControlStrategy.setLastModifiedDate(new Date());

        int csId = service().addControlStrategy(newControlStrategy);
        closeView();
        managerPresenter.doRefresh();
        return csId;
    }

    private void validateName(ControlStrategy controlStrategy) throws EmfException {
        // emptyName
        String name = controlStrategy.getName();
        if (name.trim().equals(""))
            throw new EmfException("Empty string is not allowed for the name.");

        if (isDuplicate(name))
            throw new EmfException("A Control Strategy named '" + name + "' already exists.");
    }

    private boolean isDuplicate(String name) throws EmfException {
        int id = service().isDuplicateName(name);
        return (id != 0);
//        ControlStrategy[] controlStrategies = service().getControlStrategies();
//        for (int i = 0; i < controlStrategies.length; i++) {
//            if (controlStrategies[i].getName().equals(name))
//                return true;
//        }
//        return false;
    }

    private ControlStrategyService service() {
        return session.controlStrategyService();
    }

}

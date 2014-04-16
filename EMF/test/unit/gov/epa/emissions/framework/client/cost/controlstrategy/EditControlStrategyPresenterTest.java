package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyPresenter;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyPresenterImpl;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategySummaryTabView;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import org.jmock.Mock;
import org.jmock.core.Constraint;

public class EditControlStrategyPresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveLockControlStrategyAndDisplayViewOnDisplay() throws EmfException {

        Mock controlStrategy = mock(ControlStrategy.class);
        stub(controlStrategy, "isLocked", Boolean.TRUE);

        Mock result = mock(ControlStrategyResult.class);
        Mock view = mock(EditControlStrategyView.class);
        expects(view, 1, "display", new Constraint[] { same(controlStrategy.proxy()), same(result.proxy()) });

        Mock service = mock(ControlStrategyService.class);
        Mock session = mock(EmfSession.class);
        stub(session, "controlStrategyService", service.proxy());
        stub(session, "user", new User());
        expects(service, 1, "obtainLocked", controlStrategy.proxy());
        service.expects(once()).method("controlStrategyResults").with(same(controlStrategy.proxy())).will(
                returnValue(result.proxy()));
        EditControlStrategyPresenter p = new EditControlStrategyPresenterImpl(
                (ControlStrategy) controlStrategy.proxy(), (EmfSession) session.proxy(), (EditControlStrategyView) view
                        .proxy(), null);
        expects(view, 1, "observe", same(p));

        p.doDisplay();
    }

    public void testShouldCloseControlStrategyViewOnClose() throws EmfException {
        Mock view = mock(EditControlStrategyView.class);
        expects(view, 1, "disposeView");

        Mock summaryTabView = mock(EditControlStrategySummaryTabView.class);

        Mock service = mock(ControlStrategyService.class);
        Mock session = mock(EmfSession.class);
        stub(session, "controlStrategyService", service.proxy());
        expects(service, 1, "releaseLocked");

        EditControlStrategyPresenter p = new EditControlStrategyPresenterImpl(new ControlStrategy(), (EmfSession) session.proxy(),
                (EditControlStrategyView) view.proxy(), null);
        p.set((EditControlStrategySummaryTabView) summaryTabView.proxy());

        p.doClose();
    }

    public void testShouldSaveControlStrategyAndCloseViewOnSave() throws EmfException {
        Mock view = mock(EditControlStrategyView.class);

        Mock service = mock(ControlStrategyService.class);
        ControlStrategy comtrolStrategy = new ControlStrategy("name");
        expects(service, 1, "updateControlStrategyWithLock", same(comtrolStrategy));
        stub(service, "isDuplicateName", new Integer(0));
        stub(service, "getControlStrategies", new ControlStrategy[0]);

        Mock session = mock(EmfSession.class);
        stub(session, "controlStrategyService", service.proxy());
        stub(session, "user", new User());

        Mock managerPresenter = mock(ControlStrategiesManagerPresenter.class);
        //DCD 2/2/07 we don't want to refresh the manager anymore...
        //        expects(managerPresenter, 1, "doRefresh");

        EditControlStrategyPresenter p = new EditControlStrategyPresenterImpl(comtrolStrategy, (EmfSession) session
                .proxy(), (EditControlStrategyView) view.proxy(), (ControlStrategiesManagerPresenter) managerPresenter
                .proxy());

        p.doSave(comtrolStrategy);
    }

    public void testShouldRaiseErrorIfDuplicateControlStrategyNameOnSave() {

        Mock view = mock(EditControlStrategyView.class);

        Mock service = mock(ControlStrategyService.class);

        ControlStrategy duplicateControlStrategy = new ControlStrategy("controlStrategy2");
        duplicateControlStrategy.setId(1243);
        ControlStrategy controlStrategyObj = new ControlStrategy("controlStrategy2");
        controlStrategyObj.setId(9324);

        ControlStrategy[] controlStrategies = new ControlStrategy[] { new ControlStrategy("controlStrategy1"),
                duplicateControlStrategy, controlStrategyObj };
        stub(service, "isDuplicateName", new Integer(1));
        stub(service, "getControlStrategies", controlStrategies);

        Mock session = mock(EmfSession.class);
        stub(session, "controlStrategyService", service.proxy());

        EditControlStrategyPresenter p = new EditControlStrategyPresenterImpl(controlStrategyObj, (EmfSession) session
                .proxy(), (EditControlStrategyView) view.proxy(), null);

        try {
            p.doSave(controlStrategyObj);
        } catch (EmfException e) {
            assertEquals("A Control Strategy named 'controlStrategy2' already exists.", e.getMessage());
            return;
        }

        fail("Should have raised an error if name is duplicate");
    }
}

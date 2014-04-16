package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jmock.Mock;

public class ControlMeasuresEditorPresenterTest extends EmfMockObjectTestCase {

    private Mock view;

    private EditorControlMeasurePresenterImpl presenter;

    private ControlMeasure measure;

    private Mock controlMeasureService;

    private Mock session;
    
    private Mock managerPresenter;

    protected void setUp() {
        measure = new ControlMeasure();
        measure.setName("");
        view = mock(ControlMeasureView.class);
        controlMeasureService = mock(ControlMeasureService.class);
        
        managerPresenter = mock(RefreshObserver.class);
        managerPresenter.stubs().method("doRefresh");

        session = mock(EmfSession.class);
        session.stubs().method("controlMeasureService").withNoArguments().will(returnValue(controlMeasureService.proxy()));

        ControlMeasureView viewProxy = (ControlMeasureView) view.proxy();
        EmfSession sessionProxy = (EmfSession) session.proxy();
        
        presenter = new EditorControlMeasurePresenterImpl(measure, viewProxy, sessionProxy, (RefreshObserver)managerPresenter.proxy());
    }

    public void testShouldDisplayViewOnDisplayAfterObtainingLock() throws Exception {
        User owner = new User();
        owner.setUsername("name");
        measure.setLockOwner(owner.getUsername());
        measure.setLockDate(new Date());
        
        controlMeasureService.stubs().method("obtainLockedMeasure").with(eq(owner), eq(measure.getId())).will(returnValue(measure));

        session.stubs().method("user").withNoArguments().will(returnValue(owner));

        ControlMeasureView viewProxy = (ControlMeasureView) view.proxy();
        EmfSession sessionProxy = (EmfSession) session.proxy();
        presenter = new EditorControlMeasurePresenterImpl(measure, viewProxy, sessionProxy, (RefreshObserver)managerPresenter.proxy());

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(measure));

        presenter.doDisplay();
    }

    public void testShouldUpdateDatasetRefreshDatasetsBrowserAndCloseWindowOnSave() throws Exception {
        Mock sccTabView = mock(ControlMeasureSccTabView.class);
        Scc[] sccs = new Scc[]{};
        expects(sccTabView,1, "sccs",sccs);
        presenter.set((ControlMeasureSccTabView) sccTabView.proxy());
        controlMeasureService.expects(once()).method("updateMeasure").with(eq(measure),eq(sccs));
        expects(view, "disposeView");
        presenter.save(measure, (ControlMeasureService) controlMeasureService.proxy(), presenters(), (ControlMeasureView) view
                .proxy(), true);
    }

    private List presenters() {
        List presenters = new ArrayList();
        presenters.add(summaryMockForSave());
        return presenters;
    }

    private EditableCMSummaryTabPresenter summaryMockForSave() {
        Mock summaryTab = mock(EditableCMSummaryTabPresenter.class);
        summaryTab.expects(once()).method("doSave");
        return (EditableCMSummaryTabPresenter) summaryTab.proxy();
    }

}

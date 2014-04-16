package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;

public class SetQAStatusPresenter {

    private SetQAStatusView view;

    private QAStep[] steps;

    private EmfSession session;

    private EditableQATabView tabView;

    public SetQAStatusPresenter(SetQAStatusView view, QAStep[] steps, EditableQATabView tabView, EmfSession session) {
        this.view = view;
        this.steps = steps;
        this.tabView = tabView;
        this.session = session;
    }

    public void display() {
        view.observe(this);
        view.display(steps, session.user());
    }

    public void doSave() throws EmfException {
        view.save();
        session.qaService().updateWitoutCheckingConstraints(steps);
        tabView.refresh();
        doClose();
    }

    public void doClose() {
        view.disposeView();
    }
}

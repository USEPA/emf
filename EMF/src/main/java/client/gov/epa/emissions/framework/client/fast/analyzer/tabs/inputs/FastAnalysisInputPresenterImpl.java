package gov.epa.emissions.framework.client.fast.analyzer.tabs.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastAnalysisInput;
import gov.epa.emissions.framework.services.fast.FastService;

public class FastAnalysisInputPresenterImpl implements FastAnalysisInputPresenter {

    private EmfSession session;

    private FastAnalysisInputView view;

    private FastAnalysisInput input;

    private boolean hasResults = false;

    public FastAnalysisInputPresenterImpl(int id, EmfSession session, FastAnalysisInputView view) {

        this.session = session;
        this.view = view;
    }

    public void doDisplay() {

        // this.view.observe(this);
        //
        // this.input = this.getService().obtainLockedInput(this.session.user(), this.id);
        //
        // if (!this.input.isLocked(this.session.user())) {
        // this.view.notifyLockFailure(this.input);
        // } else {
        // this.view.display(this.input);
        // }
    }

    public void doCreate() {

        view.observe(this);
        view.display(this.input);
    }

    public FastAnalysis getAnalysis(int id) throws EmfException {
        return this.getService().getFastAnalysis(id);
    }

    public void doClose() {

        // /*
        // * only release if its an existing program
        // */
        // if (this.input.getId() != 0) {
        // this.getService().releaseLockedInput(this.session.user(), this.input.getId());
        // }
        //
        // this.closeView();
    }

//    private void closeView() {
//        this.view.disposeView();
//    }

    public void doSave() {
        //
        // this.saveInput();
        // this.input = getService().updateInputWithLock(this.session.user(), this.input);
    }

    protected void saveInput() {
        //
    }

    private FastService getService() {
        return this.session.fastService();
    }

    public void fireTracking() {
        view.signalChanges();
    }

    public boolean hasResults() {
        return this.hasResults;
    }
}

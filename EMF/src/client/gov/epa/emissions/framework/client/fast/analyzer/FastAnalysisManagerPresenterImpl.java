package gov.epa.emissions.framework.client.fast.analyzer;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.fast.analyzer.create.FastAnalysisCreatorPresenterImpl;
import gov.epa.emissions.framework.client.fast.analyzer.create.FastAnalysisCreatorWindow;
import gov.epa.emissions.framework.client.fast.analyzer.edit.FastAnalysisEditorPresenterImpl;
import gov.epa.emissions.framework.client.fast.analyzer.edit.FastAnalysisEditorWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastService;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class FastAnalysisManagerPresenterImpl implements RefreshObserver, FastAnalysisManagerPresenter {

    private FastAnalysisManagerView view;

    private EmfSession session;

    private FastAnalysis[] analyses = new FastAnalysis[0];

    public FastAnalysisManagerPresenterImpl(EmfSession session, FastAnalysisManagerView view) {

        this.session = session;
        this.view = view;
    }

    public void display() throws EmfException {

        view.display(service().getFastAnalyses());
        view.observe(this);
    }

    private FastService service() {
        return session.fastService();
    }

    public void doRefresh() throws EmfException {
        view.refresh(service().getFastAnalyses());
    }

    public void doClose() {
        //
    }

    public void doNew() throws EmfException {

        FastAnalysisView creatorView = new FastAnalysisCreatorWindow(this.view.getDesktopManager(), session, this.view
                .getParentConsole());
        FastAnalysisPresenter presenter = new FastAnalysisCreatorPresenterImpl(session, creatorView, this);
        presenter.doDisplay();
    }

    public void doView(int id) throws EmfException {

        throw new EmfException("View not implemented.");
    }

    public void doEdit(int id) throws EmfException {

        FastAnalysisView editorView = new FastAnalysisEditorWindow(this.view.getDesktopManager(), session, this.view
                .getParentConsole());
        FastAnalysisPresenter presenter = new FastAnalysisEditorPresenterImpl(id, session, editorView, this);
        presenter.doDisplay();
    }

    public void doRemove(int[] ids) throws EmfException {
        service().removeFastAnalyses(ids, this.session.user());
    }

    public void doSaveCopiedAnalysis(int id, User creator) throws EmfException {
        service().copyFastAnalysis(id, this.session.user());
    }

    public void doAnalysis(int[] ids) throws EmfException {

        for (int id : ids) {
            service().runFastAnalysis(this.session.user(), id);
        }
    }

    public void doExport(int[] id) throws EmfException {
        throw new EmfException("Export not implemented.");
    }

    public FastAnalysis[] getAnalyses() {
        return this.analyses;
    }

    public void loadAnalyses() throws EmfException {
        this.analyses = session.fastService().getFastAnalyses();
    }
}

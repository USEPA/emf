package gov.epa.emissions.framework.client.fast.run;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.fast.run.create.FastRunCreatorPresenterImpl;
import gov.epa.emissions.framework.client.fast.run.create.FastRunCreatorWindow;
import gov.epa.emissions.framework.client.fast.run.edit.FastRunEditorPresenterImpl;
import gov.epa.emissions.framework.client.fast.run.edit.FastRunEditorWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.services.fast.FastService;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.List;

public class FastRunManagerPresenterImpl implements RefreshObserver,
		FastRunManagerPresenter {

	private FastRunManagerView view;

	private EmfSession session;

	private FastRun[] runs = new FastRun[0];

	public FastRunManagerPresenterImpl(EmfSession session,
			FastRunManagerView view) {

		this.session = session;
		this.view = view;
	}

	public void display() throws EmfException {

		view.display(service().getFastRuns());
		view.observe(this);
	}

	private FastService service() {
		return session.fastService();
	}

	public void doRefresh() throws EmfException {
		view.refresh(service().getFastRuns());
	}

	public void doClose() {
	    //
	}

	public void doNew() throws EmfException {

		FastRunView creatorView = new FastRunCreatorWindow(this.view
				.getDesktopManager(), session, this.view.getParentConsole());
		FastRunPresenter presenter = new FastRunCreatorPresenterImpl(session,
				creatorView, this);
		presenter.doDisplay();
	}

	public void doView(FastRun fastRun) throws EmfException {
		throw new EmfException("View not implemented.");
	}

	public void doEdit(FastRun fastRun) throws EmfException {

		FastRunView editorView = new FastRunEditorWindow(this.view
				.getDesktopManager(), session, this.view.getParentConsole());
		FastRunPresenter presenter = new FastRunEditorPresenterImpl(fastRun
				.getId(), session, editorView, this);
		presenter.doDisplay();
	}

	public void doRemove(List<FastRun> fastRuns) throws EmfException {

		int[] ids = new int[fastRuns.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = fastRuns.get(i).getId();
		}

		service().removeFastRuns(ids, this.session.user());
	}

	public void doSaveCopiedRun(FastRun fastRun, User creator)
			throws EmfException {
		service().copyFastRun(fastRun.getId(), this.session.user());
	}

	public void doExecuteRuns(List<FastRun> fastRuns, User creator)
			throws EmfException {

		for (FastRun fastRun : fastRuns) {
			service().runFastRun(this.session.user(), fastRun.getId());
		}
	}

	public void doExportRuns(List<FastRun> runs, User creator)
			throws EmfException {
		throw new EmfException("Export not implemented yet.");
	}

	public FastRun[] getRuns() {
		return this.runs;
	}

	public void loadRuns() throws EmfException {
		this.runs = session.fastService().getFastRuns();
	}
}

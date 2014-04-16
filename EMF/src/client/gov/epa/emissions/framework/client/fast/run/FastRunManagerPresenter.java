package gov.epa.emissions.framework.client.fast.run;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastRun;

import java.util.List;

public interface FastRunManagerPresenter {

    void doRefresh() throws EmfException;

    void doClose();

    void doNew() throws EmfException;

    void doEdit(FastRun run) throws EmfException;

    void doView(FastRun run) throws EmfException;

    void doRemove(List<FastRun> runs) throws EmfException;

    void doSaveCopiedRun(FastRun run, User creator) throws EmfException;

    void doExecuteRuns(List<FastRun> runs, User creator) throws EmfException;

    void doExportRuns(List<FastRun> runs, User creator) throws EmfException;

    void display() throws EmfException;

    void loadRuns() throws EmfException;
}
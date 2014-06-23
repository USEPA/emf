package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorService;

public class ClosingRule {

    private EditableTablePresenter tablePresenter;

    private DataAccessToken token;

    private DataEditorView view;

    private EmfSession session;

    public ClosingRule() {// To support unit testing
    }

    public ClosingRule(DataEditorView view, EditableTablePresenter tablePresenter, EmfSession session,
            DataAccessToken token) {
        this.view = view;
        this.tablePresenter = tablePresenter;
        this.session = session;
        this.token = token;
    }

    public boolean hasChanges() throws EmfException {
        return tablePresenter.hasChanges() || dataEditorService().hasChanges(token) || view.hasReplacedValues();
    }

    private DataEditorService dataEditorService() {
        return session.dataEditorService();
    }

    private DataCommonsService dataCommonsService() {
        return session.dataCommonsService();
    }

    public void close(User user, boolean askForRevision) throws EmfException {
        if (shouldCancelClose())
            return;

        proceedWithClose(user, askForRevision);
    }

    public boolean shouldCancelClose() throws EmfException {
        return hasChanges() && !view.confirmDiscardChanges();
    }

    public void proceedWithClose(User user, boolean askForRevision) throws EmfException {
        if (askForRevision)
            saveRevision();

        dataEditorService().closeSession(user, token);
        view.disposeView();
    }

    private void saveRevision() throws EmfException {
        if (!view.verifyRevisionInput())
            throw new EmfException("Please enter revision information before closing");

        DataCommonsService service = dataCommonsService();
        service.addRevision(view.revision());
    }
}
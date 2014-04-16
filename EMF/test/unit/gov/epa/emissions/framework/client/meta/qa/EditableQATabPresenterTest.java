package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.qa.QAService;

import org.jmock.Mock;
import org.jmock.core.Constraint;

public class EditableQATabPresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveViewOnDisplay() throws EmfException {
        Mock view = mock(EditableQATabView.class);

        Mock qaService = mock(QAService.class);
        EmfDataset dataset = new EmfDataset();
        dataset.setId(101);
        QAStep[] steps = new QAStep[0];
        stub(qaService, "getQASteps", dataset, steps);

        Mock dataEditorService = mock(DataEditorService.class);
        Version[] versions = new Version[0];
        stub(dataEditorService, "getVersions", versions);

        Mock session = mock(EmfSession.class);
        stub(session, "qaService", qaService.proxy());
        stub(session, "dataEditorService", dataEditorService.proxy());

        EditableQATabPresenter presenter = new EditableQATabPresenterImpl(dataset, (EmfSession) session.proxy(),
                (EditableQATabView) view.proxy());
        expectsOnce(view, "observe", presenter);
        expectsOnce(view, "display", new Constraint[] { same(dataset), same(steps), same(versions) });

        presenter.display();
    }

    public void testShouldAddNewQAStepOnAddUsingTemplate() {
        EmfDataset dataset = new EmfDataset();
        DatasetType type = new DatasetType();
        type.setQaStepTemplates(new QAStepTemplate[] { new QAStepTemplate() });
        dataset.setName("test");
        dataset.setDatasetType(type);

        QAStep[] steps = { new QAStep(), new QAStep() };

        Mock newQAStepview = mock(NewQAStepView.class);
        expects(newQAStepview, 1, "display", new Constraint[] { same(dataset), same(dataset.getDatasetType()) });
        stub(newQAStepview, "shouldCreate", Boolean.TRUE);
        expects(newQAStepview, 1, "steps", steps);

        Mock tabview = mock(EditableQATabView.class);
        expects(tabview, 1, "addFromTemplate", same(steps));

        EditableQATabPresenter presenter = new EditableQATabPresenterImpl(dataset, null, (EditableQATabView) tabview
                .proxy());

        presenter.doAddUsingTemplate((NewQAStepView) newQAStepview.proxy());
    }

    public void testShouldAddNewQAStepOnAddCustomized() throws EmfException {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");

        Version[] versions = {};
        QAProgram[] programs = {};

        Mock tabview = mock(EditableQATabView.class);
        EditableQATabView tabViewProxy = (EditableQATabView) tabview.proxy();

        Mock qaService = mock(QAService.class);
        expects(qaService, 1, "getQAPrograms", programs);
        Mock session = mock(EmfSession.class);
        expects(session, 1, "qaService", qaService.proxy());

        Mock newQAStepview = mock(NewCustomQAStepView.class);

        NewCustomQAStepPresenter cutomViewPresenter = new NewCustomQAStepPresenter((NewCustomQAStepView) newQAStepview
                .proxy(), dataset, versions, tabViewProxy, (EmfSession) session.proxy());
        expects(newQAStepview, 1, "observe", new Constraint[] { same(cutomViewPresenter) });
        expects(newQAStepview, 1, "display", new Constraint[] { same(dataset), same(programs), eq(versions),
                same(tabViewProxy), same(session.proxy()) });

        EditableQATabPresenterImpl presenter = new EditableQATabPresenterImpl(dataset, (EmfSession) session.proxy(),
                tabViewProxy);

        presenter.doAddCustomized((NewCustomQAStepView) newQAStepview.proxy(), cutomViewPresenter);
    }

    public void testShouldUpdateQAStepOnPerform() throws EmfException {
        EmfDataset dataset = new EmfDataset();
        QAStep step = new QAStep();
        User user = new User();
        QAProgram[] programs = {};
        QAStepResult result = new QAStepResult();
        Mock qaService = mock(QAService.class);
        expects(qaService, 1, "getQAPrograms", programs);
        qaService.expects(once()).method("getQAStepResult").with(same(step)).will(returnValue(result));
        Mock session = mock(EmfSession.class);
        expects(session, 1, "qaService", qaService.proxy());

        stub(session, "user", user);
        setPreferences(session);
        Mock view = mock(EditQAStepView.class);
        expectsOnce(view, "display", new Constraint[] { same(step), same(result), same(programs), same(dataset),
                same(""), same(session.proxy()) });
        expects(view, "observe");
        expectsOnce(view, "setMostRecentUsedFolder", "");

        EditableQATabPresenterImpl presenter = new EditableQATabPresenterImpl(dataset, (EmfSession) session.proxy(),
                null);

        presenter.doEdit(step, (EditQAStepView) view.proxy(), "");
    }

    private void setPreferences(Mock session) {
        Mock prefs = mock(UserPreference.class);
        session.stubs().method("preferences").will(returnValue(prefs.proxy()));
        prefs.stubs().method("mapLocalOutputPathToRemote").will(returnValue(""));
        prefs.stubs().method("outputFolder").will(returnValue(""));
    }

    public void testShouldSetQAStepStatusToViewOnDoSetStatus() {
        Mock qaStatusView = mock(SetQAStatusView.class);
        expects(qaStatusView, 1, "display");
        expects(qaStatusView, "observe");

        Mock session = mock(EmfSession.class);
        stub(session, "user", new User());

        EditableQATabPresenter presenter = new EditableQATabPresenterImpl(null, (EmfSession) session.proxy(), null);

        QAStep[] steps = {};
        presenter.doSetStatus((SetQAStatusView) qaStatusView.proxy(), steps);
    }

}
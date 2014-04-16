package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabPresenter;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabView;
import gov.epa.emissions.framework.client.meta.qa.EditableQATabPresenter;
import gov.epa.emissions.framework.client.meta.revisions.RevisionsTabView;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenter;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.editor.Revision;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jmock.Mock;
import org.jmock.core.constraint.IsInstanceOf;

public class PropertiesEditorPresenterTest extends EmfMockObjectTestCase {

    private Mock view;

    private PropertiesEditorPresenterImpl presenter;

    private EmfDataset dataset;

    private Mock dataService;

    private Mock dataCommonsService;

    private Mock session;

    protected void setUp() {
        dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        view = mock(DatasetPropertiesEditorView.class);

        dataService = mock(DataService.class);
        dataCommonsService = mock(DataCommonsService.class);
        dataCommonsService.stubs().method("getKeywords").withNoArguments().will(returnValue(new Keyword[0]));

        session = mock(EmfSession.class);
        session.stubs().method("dataService").withNoArguments().will(returnValue(dataService.proxy()));
        session.stubs().method("dataCommonsService").withNoArguments().will(returnValue(dataCommonsService.proxy()));
        session.stubs().method("dataEditorService").withNoArguments().will(returnValue(null));

        DatasetPropertiesEditorView viewProxy = (DatasetPropertiesEditorView) view.proxy();
        EmfSession sessionProxy = (EmfSession) session.proxy();
        presenter = new PropertiesEditorPresenterImpl(dataset, viewProxy, sessionProxy);
    }

    public void testShouldCloseViewAndReleaseLockOnNotifyClose() throws Exception {
        view.expects(once()).method("disposeView");
        dataService.expects(once()).method("releaseLockedDataset").with(same(dataset)).will(returnValue(dataset));

        presenter.doClose();
    }

    public void testShouldDisplayViewOnDisplayAfterObtainingLock() throws Exception {
        User owner = new User();
        owner.setUsername("name");
        dataset.setLockOwner(owner.getUsername());
        dataset.setLockDate(new Date());

        dataService.expects(once()).method("obtainLockedDataset").with(same(owner), same(dataset)).will(
                returnValue(dataset));

        session.stubs().method("user").withNoArguments().will(returnValue(owner));

        Mock editorService = mock(DataEditorService.class);
        Version[] versions = new Version[0];
        stub(editorService, "getVersions", versions);
        session.stubs().method("dataEditorService").will(returnValue(editorService.proxy()));

        DatasetPropertiesEditorView viewProxy = (DatasetPropertiesEditorView) view.proxy();
        presenter = new PropertiesEditorPresenterImpl(dataset, viewProxy, (EmfSession) session.proxy());

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(dataset), same(versions));

        presenter.doDisplay();
    }

    public void testShouldRaiseErrorOnDisplayIfFailedToObtainLock() throws Exception {
        User owner = new User();
        owner.setUsername("owner");
        owner.setName("owner");
        dataset.setLockOwner(owner.getUsername());
        dataset.setLockDate(new Date());

        User user = new User();
        user.setUsername("user");

        dataService.expects(once()).method("obtainLockedDataset").with(same(user), same(dataset)).will(
                returnValue(dataset));

        session.stubs().method("user").withNoArguments().will(returnValue(user));

        DatasetPropertiesEditorView viewProxy = (DatasetPropertiesEditorView) view.proxy();
        presenter = new PropertiesEditorPresenterImpl(dataset, viewProxy, (EmfSession) session.proxy());

        view.expects(once()).method("notifyLockFailure").with(same(dataset));
        view.expects(once()).method("observe").with(same(presenter));
        presenter.doDisplay();
    }

    public void testShouldUpdateDatasetRefreshDatasetsBrowserAndCloseWindowOnSave() throws Exception {
        dataService.expects(once()).method("updateDataset").with(eq(dataset));
        expects(view, "disposeView");

        presenter.save(dataset, (DataService) dataService.proxy(), presenters(), (DatasetPropertiesEditorView) view
                .proxy());
    }

    private List presenters() {
        List presenters = new ArrayList();
        presenters.add(summaryMockForSave());
        presenters.add(keywordsMockForSave());
        presenters.add(notesMockForSave());
        presenters.add(qaStepMockForSave());
        return presenters;
    }

    public void testShouldUpdateDatasetWithChangesFromTabsAndSaveDatasetOnUpdate() throws Exception {
        dataService.expects(once()).method("updateDataset").with(eq(dataset));
        expects(view, "disposeView");

        presenter.save(dataset, (DataService) dataService.proxy(), presenters(), (DatasetPropertiesEditorView) view
                .proxy());
    }

    private EditableQATabPresenter qaStepMockForSave() {
        Mock mock = mock(EditableQATabPresenter.class);
        mock.expects(once()).method("doSave");
        return (EditableQATabPresenter) mock.proxy();
    }

    private EditNotesTabPresenter notesMockForSave() {
        Mock notesTab = mock(EditNotesTabPresenter.class);
        notesTab.expects(once()).method("doSave");
        return (EditNotesTabPresenter) notesTab.proxy();
    }

    private EditableKeywordsTabPresenter keywordsMockForSave() {
        Mock keywordsTab = mock(EditableKeywordsTabPresenter.class);
        keywordsTab.expects(once()).method("doSave");
        return (EditableKeywordsTabPresenter) keywordsTab.proxy();
    }

    private EditableSummaryTabPresenter summaryMockForSave() {
        Mock summaryTab = mock(EditableSummaryTabPresenter.class);
        summaryTab.expects(once()).method("doSave");
        return (EditableSummaryTabPresenter) summaryTab.proxy();
    }

    public void testShouldDisplayErrorMessageOnDatasetsBrowserIfGettingUpdatedDatasetsFailOnSave() throws Exception {
        dataService.expects(once()).method("updateDataset").with(eq(dataset));
        view.expects(once()).method("disposeView");

        presenter.save(dataset, (DataService) dataService.proxy(), presenters(), (DatasetPropertiesEditorView) view
                .proxy());
    }

    public void testShouldDisplayNotesTabOnSetNotesTab() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(EditNotesTabView.class);
        view.expects(once()).method("display");

        Mock dataCommons = mock(DataCommonsService.class);
        dataCommons.stubs().method(ANYTHING);
        session.stubs().method("dataCommonsService").will(returnValue(dataCommons.proxy()));

        Mock dataEditor = mock(DataEditorService.class);
        dataEditor.stubs().method(ANYTHING);
        session.stubs().method("dataEditorService").will(returnValue(dataEditor.proxy()));
        session.stubs().method("user");

        PropertiesEditorPresenter presenter = new PropertiesEditorPresenterImpl(dataset, null, (EmfSession) session
                .proxy());

        presenter.set((EditNotesTabView) view.proxy());
    }

    public void testShouldDisplayQATabOnSetQATab() throws Exception {
        PropertiesEditorPresenterImpl presenter = new PropertiesEditorPresenterImpl(null, null, null);

        Mock qaPresenter = mock(EditableQATabPresenter.class);
        qaPresenter.expects(once()).method("display");

        presenter.set((EditableQATabPresenter) qaPresenter.proxy());
    }
    
    public void testShouldDisplayDataTabOnSetDataTab() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(DataTabView.class);
        view.expects(once()).method("display").with(same(dataset));
        view.expects(once()).method("observe").with(new IsInstanceOf(DataTabPresenter.class));

        Mock session = mock(EmfSession.class);
        session.stubs().method("dataViewService");
        
        PropertiesEditorPresenter presenter = new PropertiesEditorPresenterImpl(dataset, (DatasetPropertiesEditorView)this.view.proxy(), (EmfSession) session.proxy());

        presenter.set((DataTabView) view.proxy());
    }
    
    public void testShouldDisplayRevisionsTabOnSetRevisionsTab() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(RevisionsTabView.class);
        view.expects(once()).method("display");

        Mock session = mock(EmfSession.class);
        Mock service = mock(DataCommonsService.class);
        Revision[] revisions = new Revision[0];
        service.stubs().method("getRevisions").will(returnValue(revisions));
        session.stubs().method("dataCommonsService").will(returnValue(service.proxy()));

        PropertiesEditorPresenter presenter = new PropertiesEditorPresenterImpl(dataset, (DatasetPropertiesEditorView)this.view.proxy(), (EmfSession) session.proxy());

        presenter.set((RevisionsTabView) view.proxy());
    }
}

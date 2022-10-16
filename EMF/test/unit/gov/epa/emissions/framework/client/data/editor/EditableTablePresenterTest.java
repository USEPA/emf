package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.data.TablePaginator;
import gov.epa.emissions.framework.client.data.TablePresenterDelegate;
import gov.epa.emissions.framework.client.data.viewer.TablePresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataEditorService;

import org.jmock.Mock;

public class EditableTablePresenterTest extends EmfMockObjectTestCase {

    public void testShouldDelegateOnApplyConstraints() throws EmfException {
        Mock delegate = mock(TablePresenterDelegate.class);
        expects(delegate, 1, "setRowAndSortFilter");
        expects(delegate,1,"updateFilteredCount");
        
        TablePresenter p = new EditableTablePresenterImpl((TablePresenterDelegate) delegate.proxy(), null, null, parentPresenter());

        String rowFilter = "rowFilter";
        String sortOrder = "col2";
        p.doApplyConstraints(rowFilter, sortOrder);
    }

    private DataEditorPresenter parentPresenter() {
        Mock parentPresenter = mock(DataEditorPresenter.class);
        expects(parentPresenter,1,"doSave");
        return (DataEditorPresenter) parentPresenter.proxy();
    }

    public void testShouldDelegateOnTotalRecords() throws Exception {
        Mock delegate = mock(TablePresenterDelegate.class);
        stub(delegate, "totalRecords", Integer.valueOf(28));

        TablePresenter p = new EditableTablePresenterImpl((TablePresenterDelegate) delegate.proxy(), null, null,null);

        assertEquals(28, p.totalRecords());
    }

    public void testShouldDelegateAndSubmitChangesOnDisplayFirstPageOnFirstNextCall() throws Exception {
        Mock view = mock(EditorPanelView.class);
        stub(view, "changeset", new ChangeSet());

        Mock delegate = mock(TablePresenterDelegate.class);
        expects(delegate, 1, "updateFilteredCount");
        expects(delegate, 1, "doDisplayNext");

        TablePresenter p = new EditableTablePresenterImpl((TablePresenterDelegate) delegate.proxy(),
                (EditorPanelView) view.proxy(), null, null);

        p.doDisplayNext();
    }

    public void testShouldSubmitChangesAndDelegateOnDisplaySpecifiedPage() throws Exception {
        Mock view = mock(EditorPanelView.class);
        stub(view, "changeset", new ChangeSet());

        Mock delegate = mock(TablePresenterDelegate.class);
        expects(delegate, 1, "updateFilteredCount");
        expects(delegate, 1, "doDisplay", eq(Integer.valueOf(21)));

        TablePresenter p = new EditableTablePresenterImpl((TablePresenterDelegate) delegate.proxy(),
                (EditorPanelView) view.proxy(), null, null);

        p.doDisplay(21);
    }

    private Mock mockViewWithChanges(int recordsCount) {
        Mock view = mock(EditorPanelView.class);
        stub(view, "changeset", new ChangeSet());
        view.stubs().method("updateFilteredRecordsCount").with(eq(Integer.valueOf(recordsCount)));

        return view;
    }

    public void testShouldDisplayPageWithRecord() throws Exception {
        Mock view = mockViewWithChanges(20);

        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayPageWithRecord");
        stub(paginator, "totalRecords", Integer.valueOf(20));
        stub(paginator, "isCurrent", Boolean.FALSE);

        TablePresenter p = new EditableTablePresenterImpl(null, (TablePaginator) paginator.proxy(), null,
                (EditorPanelView) view.proxy(), null, null);

        p.doDisplayPageWithRecord(21);
    }

    public void testShouldDisplayFirstPage() throws Exception {
        Mock view = mockViewWithChanges(20);

        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayFirst");
        stub(paginator, "totalRecords", Integer.valueOf(20));

        TablePresenter p = new EditableTablePresenterImpl(null, (TablePaginator) paginator.proxy(), null,
                (EditorPanelView) view.proxy(), null, null);

        p.doDisplayFirst();
    }

    public void testShouldApplyDefaultSortOrderOnDisplay() throws Exception {
        Mock view = mockViewWithChanges(20);

        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "token");
        stub(paginator, "totalRecords", Integer.valueOf(20));
        expects(paginator, "doDisplayFirst");

        Mock datasetType = mock(DatasetType.class);
        String sortOrder = "sort-order";
        stub(datasetType, "getDefaultSortOrder", sortOrder);

        Mock service = mock(DataEditorService.class);

        Mock tableMetadata = mock(TableMetadata.class);
        stub(tableMetadata, "containsCol", sortOrder, Boolean.TRUE);
        TableMetadata tableMetadataProxy = (TableMetadata) tableMetadata.proxy();

        EditableTablePresenter p = new EditableTablePresenterImpl((DatasetType) datasetType.proxy(),
                (TablePaginator) paginator.proxy(), tableMetadataProxy, (EditorPanelView) view.proxy(),
                (DataEditorService) service.proxy(), null);

        Page page = new Page();
        service.expects(once()).method("applyConstraints").with(ANYTHING, eq(""), eq(sortOrder))
                .will(returnValue(page));
        expects(view, 1, "observe", same(p));

        p.display();
    }

    public void testShouldDisplayLastPage() throws Exception {
        Mock view = mockViewWithChanges(20);
        view.stubs().method("scrollToPageEnd").withNoArguments();

        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayLast");
        stub(paginator, "totalRecords", Integer.valueOf(20));

        TablePresenter p = new EditableTablePresenterImpl(null, (TablePaginator) paginator.proxy(), null,
                (EditorPanelView) view.proxy(), null, null);

        p.doDisplayLast();
    }

    public void testShouldReloadCurrentPage() throws Exception {
        Mock delegate = mock(TablePresenterDelegate.class);
        expects(delegate, 1, "reloadCurrent");

        EditableTablePresenterImpl p = new EditableTablePresenterImpl((TablePresenterDelegate) delegate.proxy(), null,
                null, null);

        p.reloadCurrent();
    }

}

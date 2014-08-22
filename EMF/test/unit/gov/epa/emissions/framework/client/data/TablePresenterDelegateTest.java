package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataAccessService;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorService;

import org.jmock.Mock;
import org.jmock.core.constraint.IsInstanceOf;

public class TablePresenterDelegateTest extends EmfMockObjectTestCase {

    public void testShouldDisplayPageOneAfterApplyingConstraintsOnApplyConstraints() throws EmfException {
        Mock view = mock(TableView.class);
        Mock service = mock(DataEditorService.class);

        Mock tableMetadata = mock(TableMetadata.class);
        TableMetadata tableMetadataProxy = (TableMetadata) tableMetadata.proxy();

        Mock paginator = mock(TablePaginator.class);
        stub(paginator, "token", new DataAccessToken());
        Integer filtered = new Integer(10);
        stub(paginator, "totalRecords", filtered);

        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, (TablePaginator) paginator.proxy(),
                tableMetadataProxy, (TableView) view.proxy(), (DataAccessService) service.proxy(), null, null);

        String rowFilter = "rowFilter";
        String sortOrder = "col2";
        Page page = new Page();
        service.expects(once()).method("applyConstraints").with(new IsInstanceOf(DataAccessToken.class), eq(rowFilter),
                eq(sortOrder)).will(returnValue(page));
        view.expects(once()).method("display").with(same(page));
        stub(tableMetadata, "containsCol", sortOrder, Boolean.TRUE);

        view.expects(once()).method("updateFilteredRecordsCount").with(eq(filtered));

        p.doApplyConstraints(rowFilter, sortOrder, false);
    }

    public void testShouldRaiseExceptionIfInvalidColsAreSpecifiedInSortOrderOnApplyConstraints() {
        Mock view = mock(TableView.class);
        Mock service = mock(DataEditorService.class);

        String[] cols = { "col1", "col2", "col3" };
        TableMetadata tableMetadata = tableMetadata(cols);

        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, null, tableMetadata, (TableView) view.proxy(),
                (DataAccessService) service.proxy(), null, null);

        String sortOrder = "invalid-row";
        p.doApplyConstraints(null, sortOrder, false);

        fail("Should have raised an exception when Sort Order contains invalid cols");
    }

    public void testShouldApplyDefaultSortOrderOnDisplay() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "token");
        stub(paginator, "totalRecords", new Integer(20));
        expects(paginator, "doDisplayFirst");

        Mock datasetType = mock(DatasetType.class);
        String sortOrder = "sort-order";
        stub(datasetType, "getDefaultSortOrder", sortOrder);

        Mock service = mock(DataAccessService.class);

        Mock tableMetadata = mock(TableMetadata.class);
        stub(tableMetadata, "containsCol", sortOrder, Boolean.TRUE);

        TableMetadata tableMetadataProxy = (TableMetadata) tableMetadata.proxy();

        TablePresenterDelegate p = new TablePresenterDelegateImpl((DatasetType) datasetType.proxy(),
                (TablePaginator) paginator.proxy(), tableMetadataProxy, null, (DataAccessService) service.proxy(), null, null);

        Page page = new Page();
        service.expects(once()).method("applyConstraints").with(ANYTHING, eq(""), eq(sortOrder))
                .will(returnValue(page));

        p.display();
    }

    public void testShouldRaiseExceptionIfOneOfColsInSortOrderIsInvalidOnApplyConstraints() {
        Mock view = mock(TableView.class);
        Mock service = mock(DataEditorService.class);

        Mock tableMetadata = mock(TableMetadata.class);
        TableMetadata tableMetadataProxy = (TableMetadata) tableMetadata.proxy();

        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, null, tableMetadataProxy, (TableView) view
                .proxy(), (DataAccessService) service.proxy(), null, null);

        String sortOrder = "col3, invalid-row";
        stub(tableMetadata, "containsCol", "col3", Boolean.TRUE);
        stub(tableMetadata, "containsCol", "invalid-row", Boolean.FALSE);
        p.doApplyConstraints(null, sortOrder, false);

        fail("Should have raised an exception when Sort Order contains invalid cols");
    }

    public void testShouldIgnoreWhenSortOrderIsEmptyOnApplyConstraints() throws EmfException {
        Mock view = mock(TableView.class);
        Mock service = mock(DataEditorService.class);

        String[] cols = { "col1", "col2", "col3" };
        TableMetadata tableMetadata = tableMetadata(cols);

        Mock paginator = mock(TablePaginator.class);
        stub(paginator, "token", new DataAccessToken());
        Integer filtered = new Integer(10);
        stub(paginator, "totalRecords", filtered);

        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, (TablePaginator) paginator.proxy(),
                tableMetadata, (TableView) view.proxy(), (DataAccessService) service.proxy(), null, null);

        String rowFilter = "rowFilter";
        String sortOrder = "   ";
        service.expects(once()).method("applyConstraints").with(new IsInstanceOf(DataAccessToken.class), eq(rowFilter),
                eq(sortOrder));
        view.expects(once()).method("display");
        view.expects(once()).method("updateFilteredRecordsCount").with(eq(filtered));

        p.doApplyConstraints(rowFilter, sortOrder, false);
    }

    private TableMetadata tableMetadata(String[] cols) {
        TableMetadata table = new TableMetadata();
        for (int i = 0; i < cols.length; i++) {
            table.addColumnMetaData(new ColumnMetaData(cols[i], "java.lang.String", 10));
        }
        return table;
    }

    public void testShouldFetchTotalRecords() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        stub(paginator, "token", new DataAccessToken());
        Integer filtered = new Integer(28);
        stub(paginator, "totalRecords", filtered);

        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, (TablePaginator) paginator.proxy(), null, null,
                null, null, null);

        assertEquals(28, p.totalRecords());
    }

    public void testShouldDisplaySpecifiedPage() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplay");

        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, (TablePaginator) paginator.proxy(), null, null,
                null, null, null);
        p.doDisplay(21);
    }

    public void testShouldDisplayPageWithRecord() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayPageWithRecord");
        stub(paginator, "isCurrent", Boolean.FALSE);

        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, (TablePaginator) paginator.proxy(), null, null,
                null, null, null);
        p.doDisplayPageWithRecord(21);
    }

    public void testShouldAbortIfRecordIsOnCurrentPageOnDisplayPageWithRecord() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        stub(paginator, "isCurrent", Boolean.TRUE);
        
        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, (TablePaginator) paginator.proxy(), null, null,
                null, null, null);
        p.doDisplayPageWithRecord(21);
    }

    public void testShouldAbortIfRecordIsZeroOnDisplayPageWithRecord() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        
        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, (TablePaginator) paginator.proxy(), null, null,
                null, null, null);
        p.doDisplayPageWithRecord(0);
    }

    public void testShouldDisplayFirstPage() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayFirst");

        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, (TablePaginator) paginator.proxy(), null, null,
                null, null, null);
        p.doDisplayFirst();
    }

    public void testShouldDisplayPreviousPage() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayPrevious");

        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, (TablePaginator) paginator.proxy(), null, null,
                null, null, null);
        p.doDisplayPrevious();
    }

    public void testShouldDisplayNextPage() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayNext");

        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, (TablePaginator) paginator.proxy(), null, null,
                null, null, null);
        p.doDisplayNext();
    }

    public void testShouldDisplayLastPage() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayLast");

        TablePresenterDelegate p = new TablePresenterDelegateImpl(null, (TablePaginator) paginator.proxy(), null, null,
                null, null, null);
        p.doDisplayLast();
    }

}

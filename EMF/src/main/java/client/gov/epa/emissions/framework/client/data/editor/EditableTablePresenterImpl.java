package gov.epa.emissions.framework.client.data.editor;

import java.awt.Container;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.data.TablePaginator;
import gov.epa.emissions.framework.client.data.TablePaginatorImpl;
import gov.epa.emissions.framework.client.data.TablePresenterDelegate;
import gov.epa.emissions.framework.client.data.TablePresenterDelegateImpl;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.editor.DataViewService;
import gov.epa.emissions.framework.ui.MessagePanel;

public class EditableTablePresenterImpl implements EditableTablePresenter {

    private EditorPanelView view;

    private DataEditorService service;

    private TablePresenterDelegate delegate;

    private DataEditorPresenter parentPresenter;
    
    private int totalRecs; 

    public EditableTablePresenterImpl(DatasetType datasetType, DataAccessToken token, TableMetadata tableMetadata,
            EditorPanelView view, DataEditorService service, DataViewService dataViewService, DataEditorPresenter parentPresenter, Container parentContainer, MessagePanel messagePanel) {
        this(datasetType, new TablePaginatorImpl(token, view, service, parentContainer, messagePanel), tableMetadata, view, service, dataViewService, parentPresenter, parentContainer, messagePanel);
    }

    public EditableTablePresenterImpl(DatasetType datasetType, TablePaginator paginator, TableMetadata tableMetadata,
            EditorPanelView view, DataEditorService service, DataViewService dataViewService, DataEditorPresenter parentPresenter, Container parentContainer, MessagePanel messagePanel) {
        this(new TablePresenterDelegateImpl(datasetType, paginator, tableMetadata, view, service, dataViewService, parentContainer, messagePanel), view, service,
                parentPresenter);
    }

    public EditableTablePresenterImpl(TablePresenterDelegate delegate, EditorPanelView view, DataEditorService service,
            DataEditorPresenter parentPresenter) {
        this.service = service;
        this.view = view;
        this.delegate = delegate;
        this.parentPresenter = parentPresenter;
    }

    public void display() throws EmfException {
        view.observe(this);
        delegate.display();
    }

    public void reloadCurrent() throws EmfException {
        delegate.reloadCurrent();
    }

    public void doDisplayNext() throws EmfException {
        submitChanges();
        delegate.doDisplayNext();
    }

    public void doDisplayPrevious() throws EmfException {
        submitChanges();
        delegate.doDisplayPrevious();
    }

    public void doDisplay(int pageNumber) throws EmfException {
        submitChanges();
        delegate.doDisplay(pageNumber);
    }

    public void doDisplayFirst() throws EmfException {
        submitChanges();
        delegate.doDisplayFirst();
    }

    public void doDisplayLast() throws EmfException {
        submitChanges();
        delegate.doDisplayLast();
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        submitChanges();
        delegate.doDisplayPageWithRecord(record);
    }

    public int totalRecords() throws EmfException {
        return delegate.totalRecords();
    }

    public void setTotalRecords(int totalRecords){
        totalRecs = totalRecords;
    }
    
    public int getTotalRecords(){
        return totalRecs ;
    }
    
    public boolean submitChanges() throws EmfException {
        boolean dataWasChanged = false;
        ChangeSet changeset = view.changeset();
        if (changeset.hasChanges()) {
            service.submit(token(), changeset, delegate.pageNumber());
            changeset.clear();
            dataWasChanged = true;
        }
        delegate.updateFilteredCount(getTotalRecords());
        return dataWasChanged;
    }

    private DataAccessToken token() {
        return delegate.token();
    }

    public boolean hasChanges() {
        return view.changeset().hasChanges();
    }

    public void doApplyConstraints(String rowFilter, String sortOrder) throws EmfException {
        delegate.setRowAndSortFilter(rowFilter, sortOrder);
        parentPresenter.doSave();
        delegate.updateFilteredCount(getTotalRecords());
    }

    public void doApplyFormat() throws EmfException {
        delegate.doApplyFormat();
    }
}

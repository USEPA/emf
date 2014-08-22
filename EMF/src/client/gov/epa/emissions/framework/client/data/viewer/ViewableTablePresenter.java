package gov.epa.emissions.framework.client.data.viewer;

import java.awt.Container;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.data.TablePaginator;
import gov.epa.emissions.framework.client.data.TablePaginatorImpl;
import gov.epa.emissions.framework.client.data.TablePresenterDelegate;
import gov.epa.emissions.framework.client.data.TablePresenterDelegateImpl;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataAccessService;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataViewService;
import gov.epa.emissions.framework.ui.MessagePanel;

public class ViewableTablePresenter implements TablePresenter {

    private ViewerPanelView view;

    private TablePresenterDelegate delegate;
    
    private int totalRecs; 

    public ViewableTablePresenter(DatasetType datasetType, DataAccessToken token, TableMetadata tableMetadata,
            ViewerPanelView view, DataAccessService service, DataViewService dataViewService, Container parentContainer, MessagePanel messagePanel) {
        this(datasetType, new TablePaginatorImpl(token, view, dataViewService, parentContainer, messagePanel), tableMetadata, view, service, parentContainer, messagePanel);
    }
    
    public ViewableTablePresenter(DatasetType datasetType, TablePaginator paginator, TableMetadata tableMetadata,
            ViewerPanelView view, DataAccessService service, Container parentContainer, MessagePanel messagePanel) {
        this(new TablePresenterDelegateImpl(datasetType, paginator, tableMetadata, view, service, parentContainer, messagePanel), view);
    }

    public ViewableTablePresenter(TablePresenterDelegate delegate, ViewerPanelView view) {
        this.view = view;
        this.delegate = delegate;
    }

    public void display() throws EmfException {
        view.observe(this);
        delegate.display();
    }

    public void doDisplayNext() throws EmfException {
        delegate.doDisplayNext();
    }

    public void doDisplayPrevious() throws EmfException {
        delegate.doDisplayPrevious();
    }

    public void doDisplay(int pageNumber) throws EmfException {
        delegate.doDisplay(pageNumber);
    }

    public void doDisplayFirst() throws EmfException {
        delegate.doDisplayFirst();
    }

    public void doDisplayLast() throws EmfException {
        delegate.doDisplayLast();
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
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

    public void doApplyConstraints(String rowFilter, String sortOrder) {
        delegate.doApplyConstraints(rowFilter, sortOrder, false);
    }

    public void doApplyFormat() throws EmfException {
        delegate.doApplyFormat();
    }
    
//    public String getRowFilter(){
//        return rowFilter; 
//    }
}

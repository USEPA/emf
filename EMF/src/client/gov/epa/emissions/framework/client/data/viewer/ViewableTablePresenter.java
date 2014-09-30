package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.data.TablePaginator;
import gov.epa.emissions.framework.client.data.TablePaginatorImpl;
import gov.epa.emissions.framework.client.data.TablePresenterDelegate;
import gov.epa.emissions.framework.client.data.TablePresenterDelegateImpl;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataAccessService;
import gov.epa.emissions.framework.services.editor.DataAccessToken;

public class ViewableTablePresenter implements TablePresenter {

    private ViewerPanelView view;

    private TablePresenterDelegate delegate;
    
    private int totalRecs; 

    public ViewableTablePresenter(DatasetType datasetType, DataAccessToken token, TableMetadata tableMetadata,
            ViewerPanelView view, DataAccessService service) {
        this(datasetType, new TablePaginatorImpl(token, view, service), tableMetadata, view, service);
    }
    
    public ViewableTablePresenter(DatasetType datasetType, TablePaginator paginator, TableMetadata tableMetadata,
            ViewerPanelView view, DataAccessService service) {
        this(new TablePresenterDelegateImpl(datasetType, paginator, tableMetadata, view, service), view);
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

    public void doApplyConstraints(String rowFilter, String sortOrder) throws EmfException {
        delegate.doApplyConstraints(rowFilter, sortOrder);
    }

    public void doApplyFormat() throws EmfException {
        delegate.doApplyFormat();
    }
    
//    public String getRowFilter(){
//        return rowFilter; 
//    }
}

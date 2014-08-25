package gov.epa.emissions.framework.client.data.editor;

import java.awt.Container;
import java.util.concurrent.ExecutionException;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.data.TablePaginator;
import gov.epa.emissions.framework.client.data.TablePaginatorImpl;
import gov.epa.emissions.framework.client.data.TablePresenterDelegate;
import gov.epa.emissions.framework.client.data.TablePresenterDelegateImpl;
import gov.epa.emissions.framework.client.swingworker.GenericSwingWorker;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.ui.MessagePanel;

public class EditableTablePresenterImpl implements EditableTablePresenter {

    private EditorPanelView view;

    private DataEditorService service;

    private TablePresenterDelegate delegate;

    private DataEditorPresenter parentPresenter;
    
    private int totalRecs; 

    private Container parentContainer;
    private MessagePanel messagePanel;
    
    public EditableTablePresenterImpl(DatasetType datasetType, DataAccessToken token, TableMetadata tableMetadata,
                    EditorPanelView view, DataEditorService service, DataEditorPresenter parentPresenter, Container parentContainer, MessagePanel messagePanel) {
            this(datasetType, new TablePaginatorImpl(token, view, service, parentContainer, messagePanel), tableMetadata, view, service, parentPresenter, parentContainer, messagePanel);
            this.parentContainer = parentContainer;
            this.messagePanel = messagePanel;
    }

    public EditableTablePresenterImpl(DatasetType datasetType, TablePaginator paginator, TableMetadata tableMetadata,
                    EditorPanelView view, DataEditorService service, DataEditorPresenter parentPresenter, Container parentContainer, MessagePanel messagePanel) {
            this(new TablePresenterDelegateImpl(datasetType, paginator, tableMetadata, view, service, parentContainer, messagePanel), view, service,
                parentPresenter);
            this.parentContainer = parentContainer;
            this.messagePanel = messagePanel;
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
            new GenericSwingWorker<Void>(parentContainer, messagePanel) {
        
                    @Override
                    public Void doInBackground() throws EmfException {
                        submitChanges();
                        return null;
                    }
        
                    @Override
                    public void done() {
                        try {
                            get();
        //                    delegate.updateFilteredCount(getTotalRecords());
                            delegate.doDisplayNext();
                        } catch (InterruptedException | ExecutionException e) {
                            messagePanel.setError("Could not display next: " + e.getMessage());
                        } catch (EmfException e) {
                            e.printStackTrace();
                            messagePanel.setError("Could not display next: " + e.getMessage());
                        }
                    }
        
                }.execute();
    }

    public void doDisplayPrevious() throws EmfException {
            new GenericSwingWorker<Void>(parentContainer, messagePanel) {
        
                    @Override
                    public Void doInBackground() throws EmfException {
                        submitChanges();
                        return null;
                    }
        
                    @Override
                    public void done() {
                        try {
                            get();
        //                    delegate.updateFilteredCount(getTotalRecords());
                            delegate.doDisplayPrevious();
                        } catch (InterruptedException | ExecutionException e) {
                            messagePanel.setError("Could not display next: " + e.getMessage());
                        } catch (EmfException e) {
                            e.printStackTrace();
                            messagePanel.setError("Could not display next: " + e.getMessage());
                        }
                    }
                }.execute();
    }

    public void doDisplay(final int pageNumber) throws EmfException {
                new GenericSwingWorker<Void>(parentContainer, messagePanel) {
        
                    @Override
                    public Void doInBackground() throws EmfException {
                        submitChanges();
                        return null;
                    }
        
                    @Override
                    public void done() {
                        try {
                            get();
        //                    delegate.updateFilteredCount(getTotalRecords());
                            delegate.doDisplay(pageNumber);
                        } catch (InterruptedException | ExecutionException e) {
                            messagePanel.setError("Could not display next: " + e.getMessage());
                        } catch (EmfException e) {
                            e.printStackTrace();
                            messagePanel.setError("Could not display next: " + e.getMessage());
                        }
                    }
        
                }.execute();
    }

    public void doDisplayFirst() throws EmfException {
            new GenericSwingWorker<Void>(parentContainer, messagePanel) {
        
                    @Override
                    public Void doInBackground() throws EmfException {
                        submitChanges();
                        return null;
                    }
        
                    @Override
                    public void done() {
                        try {
                            get();
        //                    delegate.updateFilteredCount(getTotalRecords());
                            delegate.doDisplayFirst();
                        } catch (InterruptedException | ExecutionException e) {
                            messagePanel.setError("Could not display next: " + e.getMessage());
                        } catch (EmfException e) {
                            e.printStackTrace();
                            messagePanel.setError("Could not display next: " + e.getMessage());
                        } 
                    }
        
                }.execute();
    }

    public void doDisplayLast() throws EmfException {
            new GenericSwingWorker<Void>(parentContainer, messagePanel) {
        
                    @Override
                    public Void doInBackground() throws EmfException {
                        submitChanges();
                        return null;
                    }
        
                    @Override
                    public void done() {
                        try {
                            get();
        //                    delegate.updateFilteredCount(getTotalRecords());
                            delegate.doDisplayLast();
                        } catch (InterruptedException | ExecutionException e) {
                            messagePanel.setError("Could not display next: " + e.getMessage());
                        } catch (EmfException e) {
                            e.printStackTrace();
                            messagePanel.setError("Could not display next: " + e.getMessage());
                        }
                    }
        
                }.execute();
    }

    public void doDisplayPageWithRecord(final int record) throws EmfException {
            new GenericSwingWorker<Void>(parentContainer, messagePanel) {
    
                @Override
                public Void doInBackground() throws EmfException {
                    submitChanges();
                    return null;
                }
    
                @Override
                public void done() {
                    try {
                        get();
    //                    delegate.updateFilteredCount(getTotalRecords());
                        delegate.doDisplayPageWithRecord(record);
                    } catch (InterruptedException | ExecutionException e) {
                        messagePanel.setError("Could not display next: " + e.getMessage());
                    } catch (EmfException e) {
                        e.printStackTrace();
                        messagePanel.setError("Could not display next: " + e.getMessage());
                    }
                }
    
            }.execute();
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
        return dataWasChanged;
    }

    private DataAccessToken token() {
        return delegate.token();
    }

    public boolean hasChanges() {
        return view.changeset().hasChanges();
    }

    public void doApplyConstraints(final String rowFilter, final String sortOrder) {
            class ApplyConstraintsSwingWorker extends GenericSwingWorker<Void> {
    //        new GenericSwingWorker<Void>(parentContainer, messagePanel) {
    
                private boolean hasChanges;
    
                public ApplyConstraintsSwingWorker(Container parentContainer, MessagePanel messagePanel) {
                    super(parentContainer, messagePanel);
                    delegate.clear();
                    hasChanges = view.changeset().hasChanges(); 
                }
    
                
                @Override
                public Void doInBackground() throws EmfException {
                    if (hasChanges)
                        parentPresenter.doSave();
                    return null;
                }
    
                @Override
                public void done() {
                    try {
                        get();
                        if (!hasChanges)
                            delegate.doApplyConstraints(rowFilter, sortOrder, true);
    
                    } catch (InterruptedException | ExecutionException e) {
                        messagePanel.setError("Could not apply constraints: " + e.getMessage());
                    }
                }
    
            };
            new ApplyConstraintsSwingWorker(parentContainer, messagePanel).execute();
    
    //        delegate.setRowAndSortFilter(rowFilter, sortOrder);
    //        
    //        try {
    //            SwingUtilities.invokeAndWait(new Runnable() {
    //                
    //                @Override
    //                public void run() {
    //                    delegate.updateFilteredCount(getTotalRecords());
    //                }
    //            });
    //        } catch (InvocationTargetException | InterruptedException e) {
    //           throw new EmfException(e.getMessage(), e);
    //        }
    }

    public void doApplyFormat() throws EmfException {
        delegate.doApplyFormat();
    }
    
    public void clear() {
        delegate.clear();
    }
}

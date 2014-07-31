package gov.epa.emissions.framework.client.swingworker;

import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Container;
import java.awt.Cursor;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public class GenericSwingWorker<E> extends SwingWorker<E, Void> {
    protected Container parentContainer;
    protected MessagePanel messagePanel;
    private GenericSwingWorkerPresenter<E> presenter;

    public GenericSwingWorker(Container parentContainer, MessagePanel messagePanel, GenericSwingWorkerPresenter<E> presenter) {
        this(parentContainer, messagePanel);
        this.presenter = presenter;
    }

    public GenericSwingWorker(Container parentContainer, MessagePanel messagePanel) {
        this.parentContainer = parentContainer;
        this.messagePanel = messagePanel;
        this.parentContainer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(parentContainer, false);
    }

    /*
     * Main task. Executed in background thread. Get objects.  
     * don't update gui here
     */
    @Override
    public E doInBackground() throws EmfException {    
        return (this.presenter != null ? presenter.getGenericSwingWorkerData() : null);
    }

    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
        try {
            //make sure something didn't happen
            E obj = get();
            if (this.presenter != null) 
                presenter.genericSwingWorkerIsDone(obj);
        } catch (InterruptedException e1) {
            messagePanel.setError(e1.getMessage());
//            setErrorMsg(e1.getMessage());
        } catch (ExecutionException e1) {
            messagePanel.setError(e1.getMessage());
//            setErrorMsg(e1.getCause().getMessage());
        } finally {
            finalize();
        }
    }
    
    public void finalize() {
        ComponentUtility.enableComponents(parentContainer, true);
        this.parentContainer.setCursor(null); //turn off the wait cursor
    }
}

 
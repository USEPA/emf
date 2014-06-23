package gov.epa.emissions.framework.client.swingworker;

import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;

import java.awt.Container;
import java.awt.Cursor;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public class SwingWorkerTasks extends SwingWorker<Object[], Void> {
    private Container parentContainer;
    private SwingWorkerPresenter presenter;

    public SwingWorkerTasks(Container parentContainer, SwingWorkerPresenter presenter) {
        this.parentContainer = parentContainer;
        this.presenter = presenter;    
        this.parentContainer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(parentContainer, false);
    }

    /*
     * Main task. Executed in background thread. Get objects.  
     * don't update gui here
     */
    @Override
    public Object[] doInBackground() throws EmfException {    
        return presenter.swProcessData();
    }

    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
        try {
            //make sure something didn't happen
            try {
                presenter.swDisplay(get());
            } catch (EmfException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
            
        } catch (InterruptedException e1) {
//            messagePanel.setError(e1.getMessage());
//            setErrorMsg(e1.getMessage());
        } catch (ExecutionException e1) {
//            messagePanel.setError(e1.getCause().getMessage());
//            setErrorMsg(e1.getCause().getMessage());
        } finally {
//            this.parentContainer.setCursor(null); //turn off the wait cursor
//            this.parentContainer.
            ComponentUtility.enableComponents(parentContainer, true);
            this.parentContainer.setCursor(null); //turn off the wait cursor
        }
    }
}

 
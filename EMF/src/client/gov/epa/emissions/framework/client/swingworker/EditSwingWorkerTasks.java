package gov.epa.emissions.framework.client.swingworker;

import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Container;
import java.awt.Cursor;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public class EditSwingWorkerTasks extends SwingWorker<Object[], Void> {
    private Container parentContainer;
    private HeavySwingWorkerPresenter presenter;
    private MessagePanel messagePanel;

    public EditSwingWorkerTasks(Container parentContainer, MessagePanel messagePanel, HeavySwingWorkerPresenter presenter) {
        this.parentContainer = parentContainer;
        this.presenter = presenter;    
        this.messagePanel = messagePanel;
    }

    /*
     * Main task. Executed in background thread. Get objects.  
     * don't update gui here
     */
    @Override
    public Object[] doInBackground() throws EmfException  {   
        this.parentContainer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(parentContainer, false);
        messagePanel.clear();
        return presenter.editProcessData();
    }

    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
        try {
            //make sure something didn't happen
            presenter.editDisplay(get());
            
        } catch (InterruptedException e1) {
//            messagePanel.setError(e1.getMessage());
//            setErrorMsg(e1.getMessage());
        } catch (ExecutionException e1) {
//            messagePanel.setError(e1.getCause().getMessage());
//            setErrorMsg(e1.getCause().getMessage());
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            e.printStackTrace();
        } finally {
//            this.parentContainer.setCursor(null); //turn off the wait cursor
//            this.parentContainer.
            ComponentUtility.enableComponents(parentContainer, true);
            this.parentContainer.setCursor(null); //turn off the wait cursor
        }
    }
}

 
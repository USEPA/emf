package gov.epa.emissions.framework.client.meta.logs;

import java.awt.BorderLayout;
import java.awt.Cursor;

import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.swingworker.RefreshSwingWorkerTasks;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class LogsTab extends JPanel implements LogsTabView, RefreshObserver{

    private EmfConsole parentConsole;
    
    private SelectableSortFilterWrapper table; 
    
    private LogsTabPresenter presenter;
    
    protected MessagePanel messagePanel;

    public LogsTab(EmfConsole parentConsole, MessagePanel messagePanel) {
        super.setName("logsTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(AccessLog[] accessLogs) {
        super.removeAll();
        super.add(createAccessLogsLayout(accessLogs));
    }

    private JPanel createAccessLogsLayout(AccessLog[] logs) {
        JPanel layout = new JPanel(new BorderLayout(5, 10));
        layout.setBorder(new Border("Access Logs"));

        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        layout.add(tablePanel(logs));

        return layout;
    }

    private JPanel tablePanel(AccessLog[] logs) {
        JPanel tablePanel = new JPanel(new BorderLayout());
        //SimpleTableModel wrapperModel = new SimpleTableModel(model);
        if ( table ==null )
            table = new SelectableSortFilterWrapper(parentConsole, new LogsTableData(logs), null);
        else
             table.refresh(new LogsTableData(logs)); 
        tablePanel.add(table);
        return tablePanel;
    }
    
    public void doRefresh() throws EmfException {
        
        try {
            new RefreshSwingWorkerTasks(this, messagePanel, presenter).execute();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    public void doRefresh(AccessLog[] logs){
        super.removeAll();
        super.add(createAccessLogsLayout(logs));
        super.validate();
    }
    

    public void observe(LogsTabPresenter presenter) {
       this.presenter =presenter;
        
    } 

}

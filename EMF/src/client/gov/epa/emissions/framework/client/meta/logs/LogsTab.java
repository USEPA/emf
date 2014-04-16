package gov.epa.emissions.framework.client.meta.logs;

import java.awt.BorderLayout;
import java.awt.Cursor;

import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class LogsTab extends JPanel implements LogsTabView, RefreshObserver{

    private EmfConsole parentConsole;
    
    private SelectableSortFilterWrapper table; 
    
    private LogsTabPresenter presenter;

    public LogsTab(EmfConsole parentConsole) {
        super.setName("logsTab");
        this.parentConsole = parentConsole;

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
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            super.removeAll();
            super.add(createAccessLogsLayout(presenter.getLogs()));
            super.validate();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public void observe(LogsTabPresenter presenter) {
       this.presenter =presenter;
        
    } 

}

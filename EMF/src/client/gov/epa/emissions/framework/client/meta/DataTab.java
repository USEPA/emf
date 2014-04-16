package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.versions.VersionsPanel;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.awt.BorderLayout;
import java.awt.Cursor;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class DataTab extends JPanel implements DataTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private DataTabPresenter presenter;
    
    private EmfDataset dataset; 

    private boolean editable;
    
    public DataTab(EmfConsole parentConsole, DesktopManager desktopManager, MessagePanel messagePanel, boolean editable) {

        setName("dataTab");
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
        this.messagePanel = messagePanel;
        this.editable = editable;
    }

    public void observe(DataTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(EmfDataset dataset) {
        this.dataset = dataset; 
        
        createLayout();
    }

    private void createLayout() {
        super.setLayout(new BorderLayout());
        //super.add(messagePanel, BorderLayout.PAGE_START);
        
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(versionsPanel());

        super.add(container);
    }

    private VersionsPanel versionsPanel() {
        VersionsPanel versionsPanel = new VersionsPanel(dataset, messagePanel, parentConsole, desktopManager, editable);
        try {
            presenter.displayVersions(versionsPanel);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        return versionsPanel;
    }

    public void doRefresh(){
        try {           
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            this.dataset = presenter.reloadDataset();
            super.removeAll();
            createLayout();
            super.validate();
            messagePanel.setMessage("Finished loading dataset versions.");
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }        
    }

}

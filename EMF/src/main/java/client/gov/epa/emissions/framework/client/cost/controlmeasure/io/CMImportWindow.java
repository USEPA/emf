package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.ImportButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.YesNoDialog;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class CMImportWindow extends ReusableInteralFrame implements CMImportView, RefreshObserver {

    private CMImportPresenter presenter;

    private MessagePanel messagePanel;

    private CMImportInputPanel importInputPanel;
    
    private Button importButton;

    private boolean importing;

    private EmfSession session;
    
    private EmfConsole parentConsole;
    
    public CMImportWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session) {
        super("Import Control Measures", new Dimension(700, 600), desktopManager);
        super.setName("importControlMeasures");

        this.parentConsole = parentConsole;
        this.session = session;
        this.getContentPane().add(createLayout());
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel(new BorderLayout());
        // panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        importInputPanel = new CMImportInputPanel(parentConsole, messagePanel, session);

        panel.add(messagePanel, BorderLayout.NORTH);
        panel.add(importInputPanel);
        panel.add(createButtonsPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

       importButton = new ImportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doImport();
            }
        });
        container.add(importButton);
        getRootPane().setDefaultButton(importButton);

        Button importStatusButton = new RefreshButton("Import Status", 16, true, this,
                "Refresh Control Measure Import Status", messagePanel);

        container.add(importStatusButton);

        Button done = new Button("Done", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doDone();
            }
        });
        container.add(done);
        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private void doImport() {
        try {
            importButton.setEnabled(false);
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            
            // check if do purge
            int [] sectorIDs = null;
            if ( this.importInputPanel.toPurge()) {
                sectorIDs = this.importInputPanel.getSectorIDs();
            }
            
            presenter.doImport(this.importInputPanel.toPurge(), sectorIDs, importInputPanel.folder(), importInputPanel.files());
            
            importing = true;
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void doDone() {
        if (confirmImport()) {
            presenter.doDone();
        } /*else
            presenter.doDone();*/  
    }

    public void register(CMImportPresenter presenter) {
        this.presenter = presenter;
        importInputPanel.register(presenter);
    }

    public void setDefaultBaseFolder(String folder) {
        importInputPanel.setDefaultBaseFolder(folder);
    }

    public void setMessage(String message) {
        importInputPanel.setStartImportMessage(message);

    }

    public void doRefresh() throws EmfException {
        importing = false;
        doImportStatus();
    }

    protected void doImportStatus() throws EmfException {
        Status[] importStatus = presenter.getImportStatus();
        StringBuffer sb = new StringBuffer();
        for (int i = importStatus.length - 1; i >= 0 && importStatus.length > 0; i--) {
            sb.append(importStatus[i].getMessage());
        }
        
        importInputPanel.addStatusMessage(sb.toString());
    }

    public void windowClosing() {
        if (confirmImport()) {
            super.windowClosing();
        }
    }

    private boolean confirmImport() {
        if (importing) {
            String message = "Control measures are being imported, and you will lose status messages if you close this window." + System.getProperty("line.separator")
            + "Click the Import Status button to get the status of the import process.\n"+
            "Click yes, if you to don't care to see the messages for the import process.";
            String title = "Ignore import status messages?";
            YesNoDialog dialog = new YesNoDialog(this, title, message);
            return dialog.confirm();
        }
        return true;
    }
    
    public boolean confirmToPurge( String msg) {
        
        int n = JOptionPane.showConfirmDialog(
                this,
                msg,
                "Purge existing control measures?",
                JOptionPane.YES_NO_OPTION);
        if ( n == JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }
 
}
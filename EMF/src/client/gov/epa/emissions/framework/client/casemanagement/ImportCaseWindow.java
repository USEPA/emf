package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.ImportButton;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ImportCaseWindow extends ReusableInteralFrame implements ImportCaseView {

    private ImportCasePresenter presenter;
    
    private JButton importButton;

    private MessagePanel messagePanel;

    private DataCommonsService service;

    private ImportCasePanel importInputPanel;
    
    private EmfConsole parentConsole;

    public ImportCaseWindow(DataCommonsService service, DesktopManager desktopManager, EmfConsole parent) {
        super("Import Cases", new Dimension(700, 320), desktopManager);
        super.setName("importCases");

        this.service = service;
        this.parentConsole = parent;

        this.getContentPane().add(createLayout());
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        importInputPanel = new ImportCasePanel(service, messagePanel, parentConsole);
        panel.add(messagePanel);
        panel.add(importInputPanel);
        panel.add(createButtonsPanel());

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
                importCase();
            }
        });
        container.add(importButton);
        getRootPane().setDefaultButton(importButton);

        JButton done = new Button("Done", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.finish();
            }
        });
        done.setMnemonic('D');
        container.add(done);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private void importCase() {
        try {
            presenter.importCase(importInputPanel.folder(), importInputPanel.files());
            messagePanel.setMessage("Successfully imported the case.");
            importButton.setEnabled(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentConsole, e.getMessage(), "Import Case", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void register(ImportCasePresenter presenter) {
        this.presenter = presenter;
        importInputPanel.register(presenter);
    }

    public void setDefaultBaseFolder(String folder) {
        importInputPanel.setDefaultBaseFolder(folder);
    }

    public void setMessage(String message) {
        importInputPanel.setMessage(message);

    }
}

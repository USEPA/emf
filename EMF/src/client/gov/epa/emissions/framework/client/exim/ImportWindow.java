package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.ImportButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
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
import javax.swing.JPanel;

public class ImportWindow extends ReusableInteralFrame implements ImportView {

    private ImportPresenter presenter;

    private MessagePanel messagePanel;

    private DataCommonsService service;

    private ImportInputPanel importInputPanel;

    private EmfSession session;
    
    public ImportWindow(EmfSession session, DataCommonsService service, DesktopManager desktopManager, EmfConsole parent, DatasetType dsType) throws EmfException {
        super("Import Datasets", new Dimension(700, 420), desktopManager);
        super.setName("importDatasets");

        this.service = service;
        this.session = session;
        this.getContentPane().add(createLayout(parent, dsType));
    }

    private JPanel createLayout(EmfConsole parent, DatasetType dsType) throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        importInputPanel = new ImportInputPanel(session, service, messagePanel, parent, dsType);
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

        JButton importButton = new ImportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doImport();
            }
        });
        container.add(importButton);
        getRootPane().setDefaultButton(importButton);

        JButton done = new Button("Done", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doDone();
            }
        });
        container.add(done);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private void doImport() {
        try {
            if (!importInputPanel.isCreateMutlipleDatasets()) {
                presenter.doImport(importInputPanel.folder(), importInputPanel.files(), importInputPanel.datasetType(),
                        importInputPanel.datasetName());
            } else {
                presenter.doImport(importInputPanel.folder(), importInputPanel.files(), importInputPanel.datasetType());
            }
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void register(ImportPresenter presenter) {
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

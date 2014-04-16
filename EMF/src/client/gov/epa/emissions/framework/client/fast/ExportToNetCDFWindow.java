package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastOutputExportWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextField;

public class ExportToNetCDFWindow extends ExportWindow {

    private List<FastOutputExportWrapper> outputs;

    private SingleLineMessagePanel messagePanel;

    private JTextField folderTextField;

    private ExportPresenter presenter;

    // private JCheckBox overwriteCheckbox;

    private JButton exportButton;

//    private DataCommonsService service;
//
//    private EmfConsole parentConsole;

    public ExportToNetCDFWindow(List<FastOutputExportWrapper> outputs, DesktopManager desktopManager, EmfConsole parentConsole,
            EmfSession session) {
        super(outputs, desktopManager, parentConsole,
                session);
    }

    protected void doExport() {
        try {
            validateFolder(folderTextField.getText());

            // if (!overwriteCheckbox.isSelected()) {
            // presenter.doExport(this.outputs, folderTextField.getText());
            // } else {
            presenter.doExportWithOverwrite(this.outputs, folderTextField.getText());
            // }

            messagePanel.setMessage("Started export. Please monitor the Status window "
                    + "to track your Export request.");

            exportButton.setEnabled(false);
        } catch (EmfException e) {
            e.printStackTrace();
            messagePanel.setError(e.getMessage());
        }
    }
}

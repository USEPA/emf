package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.swingworker.RefreshSwingWorkerTasks;
import gov.epa.emissions.framework.client.swingworker.SwingWorkerTasks;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class ViewableInputsTab extends EditInputsTab {

    public ViewableInputsTab(EmfConsole parentConsole, MessagePanel messagePanel, DesktopManager desktopManager) {
        super(parentConsole, messagePanel, desktopManager);
        super.setName("viewInputsTab");    
        super.setLayout(new BorderLayout());
    }
    
    public void doDisplay(EditInputsTabPresenter presenter){
        this.presenter = presenter;
        new SwingWorkerTasks(this, presenter).execute();
    }
    

    public void display(EmfSession session, Case caseObj) {
        super.removeAll();
        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
         
        this.session = session;
        this.inputDir = new TextField("inputdir", 50);
        inputDir.setText(caseObj.getInputFileDir());
        inputDir.setEditable(false);
        super.add(createLayout(new CaseInput[0], parentConsole), BorderLayout.CENTER);
        messagePanel.setMessage("Please select a sector to see full list of inputs.");

    } 

    private JPanel createLayout(CaseInput[] inputs, EmfConsole parentConsole) {
        layout = new JPanel(new BorderLayout());

        layout.add(createFolderNSectorPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(inputs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel controlPanel() {
        Insets insets = new Insets(1, 2, 1, 2);
        JPanel container = new JPanel();

        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);
        SelectAwareButton view = new SelectAwareButton("View", viewAction(), table, confirmDialog);
        view.setMargin(insets);
        container.add(view);

        Button copy = new Button("Copy", copyAction(presenter));
        copy.setMargin(insets);
        copy.setMnemonic('C');
        container.add(copy);

        Button viewDS = new ViewButton("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doDisplayInputDatasetsPropertiesViewer();
            }
        });
        viewDS.setMargin(insets);
        container.add(viewDS);

        Button export = new ExportButton("Export Inputs", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doExportInputDatasets(getSelectedInputs());
            }
        });
        export.setMargin(insets);
        container.add(export);

        Button findRelated = new Button("Find", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewCasesReleatedToDataset();
            }
        });
        findRelated.setMargin(insets);
        findRelated.setMnemonic('F');
        container.add(findRelated);

        showAll = new JCheckBox("Show All", false);
        showAll.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    if (sectorsComboBox != null)
                        sectorsComboBox.setSelectedItem(new Sector("All", "All"));
                    new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
                    messagePanel.clear();
                } catch (Exception ex) {
                    setErrorMessage(ex.getMessage());
                }
            }
        });
        container.add(showAll);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    private Action viewAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    doView();
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
        return action;
    }

    private void doView() throws EmfException {
        List inputs = getSelectedInputs();

        if (inputs.size() == 0) {
            messagePanel.setMessage("Please select input(s) to edit.");
            return;
        }

        for (Iterator iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = (CaseInput) iter.next();
            String title = "View Case Input:" + input.getName() + "(" + input.getId() + ")(" + caseObj.getName() + ")";
            EditCaseInputView inputEditor = new EditCaseInputWindow(title, desktopManager, parentConsole);
            presenter.doEditInput(input, inputEditor);
            inputEditor.viewOnly(title);
        }
    }
    
 
    public void doRefresh() throws EmfException {
        try {
            new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
            clearMessage();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

}

package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//import java.util.ArrayList;
//import java.util.List;

public class ControlMeasureReferencesTab extends EmfPanel implements ControlMeasureTabView {

    private ControlMeasurePresenter controlMeasurePresenter;

    private ControlMeasure measure;

    private Button addButton;

    private Button editButton;

    private Button viewButton;

    private Button removeButton;

    private JPanel mainPanel;

    private JPanel tablePanel;

    private CMReferenceTableData tableData;

    private ManageChangeables changeables;

    private SelectableSortFilterWrapper table;

    public ControlMeasureReferencesTab(ControlMeasure measure, ManageChangeables changeables,
            MessagePanel messagePanel, EmfConsole parent, ControlMeasurePresenter controlMeasurePresenter,
            DesktopManager desktopManager) {

        super("CMReference tab", parent, desktopManager, messagePanel);
        this.mainPanel = new JPanel(new BorderLayout());

        this.measure = measure;
        this.changeables = changeables;
        this.controlMeasurePresenter = controlMeasurePresenter;

        this.tablePanel = new JPanel(new BorderLayout());
        doLayout(measure);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    private void doLayout(ControlMeasure measure) {

        updateMainPanel(measure.getReferences());
        mainPanel.setBorder(BorderFactory.createTitledBorder("References"));
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(buttonPanel(), BorderLayout.SOUTH);
    }

    private void updateMainPanel(Reference[] references) {

        tablePanel.removeAll();
        tableData = new CMReferenceTableData(references);
        table = new SelectableSortFilterWrapper(this.getParentConsole(), tableData, null);
        tablePanel.add(table);
        tablePanel.validate();
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        addButton = new AddButton(addAction());
        panel.add(addButton);
        editButton = new EditButton(editAction());
        panel.add(editButton);
        viewButton = new ViewButton(viewAction());
        viewButton.setVisible(false);
        panel.add(viewButton);
        removeButton = new RemoveButton(removeAction());
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private List<Reference> getSelectedReferences() {
        return (List<Reference>) this.table.selected();
    }

    private Action addAction() {
        return new AbstractAction() {

            public void actionPerformed(ActionEvent event) {
                doAdd();
            }
        };
    }

    private Action editAction() {

        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {

                // if this is a new measure, make sure we save it before we proceed.
                // the efficiency editor needs to have a measure in the db first!
                if (measure.getId() == 0) {

                    try {
                        controlMeasurePresenter.doSave(true);
                    } catch (EmfException e1) {
                        showError("Cannot save control measure: " + e1.getMessage());
                        return;
                    }
                }

                List<Reference> references = getSelectedReferences();

                if (references.isEmpty()) {
                    showError("Please select an item to edit.");
                    return;
                }

                for (Reference reference : references) {
                    doEdit(reference);
                }
            }
        };
    }

    private Action viewAction() {

        return new AbstractAction() {

            public void actionPerformed(ActionEvent event) {

                List<Reference> references = getSelectedReferences();

                if (references.isEmpty()) {

                    showError("Please select an item to view.");
                    return;
                }

                for (Reference reference : references) {
                    doView(reference);
                }
            }
        };
    }

    protected void doAdd() {

        this.clearMessage();

        MeasureReferenceView view = new MeasureReferenceAddWindow("Add Reference", this.getDesktopManager(), this
                .getSession());
        MeasureReferencePresenter presenter = new MeasureReferencePresenter(this, view);

        presenter.display(measure);
    }

    protected void doEdit(Reference reference) {

        this.clearMessage();

        MeasureReferenceView view = new MeasureReferenceWindow("Edit Reference", this.changeables, this
                .getDesktopManager(), this.getSession());
        MeasureReferencePresenter presenter = new MeasureReferencePresenter(this, view);

        presenter.display(measure, reference);
    }

    protected void doView(Reference reference) {

        this.clearMessage();

        MeasureReferenceView view = new MeasureReferenceWindow("View Reference", this.changeables, this
                .getDesktopManager(), this.getSession());
        MeasureReferencePresenter presenter = new MeasureReferencePresenter(this, view);

        presenter.display(measure, reference);
        view.viewOnly();
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {

                try {

                    clearMessage();
                    doRemove();

                } catch (Exception e1) {
                    showError("Could not remove equation type");
                }

            }
        };
    }

    private void doRemove() {

        List<Reference> references = this.getSelectedReferences();

        // nothing was selected so show a message
        if (references.isEmpty()) {

            this.showError("Please select an item to remove.");
            return;
        }

        String title = "Warning";
        String message = "Are you sure you want to remove the reference?";
        int selection = JOptionPane.showConfirmDialog(this.getParentConsole(), message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {

            modify();
            tableData.remove(references);
            refresh();
        }
    }

    public void modify() {
        controlMeasurePresenter.doModify();
    }

    public void viewOnly() {

        addButton.setVisible(false);
        editButton.setVisible(false);
        removeButton.setVisible(false);
        viewButton.setVisible(true);
    }

    public void refresh() {
        tableData.refresh();
        table.refresh(tableData);
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    public void refresh(ControlMeasure measure) {

        // measure.setEquationTypes(cmEquationTypes);

    }

    public void add(Reference reference) {
        
        if (tableData.contains(reference)) {
            this.showMessage("Control Measure already contains added reference.");
        }
        else {

            tableData.add(reference);
            refresh();
            modify();
        }
    }

    public void save(ControlMeasure measure) {
        measure.setReferences(tableData.sources());
    }
}

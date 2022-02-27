package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.DoubleValue;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class AppendDataWindow extends DisposableInteralFrame implements AppendDataWindowView {

    private AppendDataViewPresenter presenter;

    private SingleLineMessagePanel messagePanel;

    private EmfConsole parentConsole;

    private JPanel layout;

    private EmfDataset sourceDataset = null;

    private ComboBox sourceVersionBox;

    private JTextField sourceDatasetField;

    private JTextField sourceFilterField;

    private JTextField startLineField;

    private ComboBox targetDatasetVerison;

    private JCheckBox deleteDSCheckBox;

    private TextArea what;

    private TextArea why;

    private Button okButton;

    private DoubleValue startLineNum;
    
    private VersionedDataView parentView; 

    public AppendDataWindow(EmfConsole parentConsole, DesktopManager desktopManager, 
            VersionedDataView parentView) {
        super("Append Data Window", new Dimension(700, 450), desktopManager);
        this.parentView = parentView;
        this.parentConsole = parentConsole;
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void display() {
        setTitle("Append Data into Dataset: " + presenter.getDataset().getName());
        setName("Append Data into Dataset: " + presenter.getDataset().getId());
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();

        layout.add(messagePanel);
        layout.add(sourcePanel());
        layout.add(targePanel());
        layout.add(revisionPanel());
        layout.add(createButtonPanel());

        super.display();
    }

    private Component createUpperPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        // panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Source
        // Dataset", 0, 0, Font.decode(""), Color.BLUE));

        sourceDatasetField = new JTextField(40);
        sourceDatasetField.setName("sourceDataset");
        Button setButton = new Button("Set", new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    selectSourceDataset();
                } catch (Exception e) {
                    setErrorMsg(e.getMessage());
                }
            }
        });
        setButton.setMnemonic(KeyEvent.VK_S);
        JPanel sourceDatasetPanel = new JPanel(new BorderLayout(2, 0));
        sourceDatasetPanel.add(sourceDatasetField, BorderLayout.LINE_START);
        sourceDatasetPanel.add(setButton, BorderLayout.CENTER);
        // sourceDatasetPanel.add(view, BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Dataset Name", sourceDatasetPanel, panel);

        sourceVersionBox = new ComboBox("Select a version", new Version[0]);
        layoutGenerator.addLabelWidgetPair("Source Version", sourceVersionBox, panel);

        sourceFilterField = new JTextField(40);
        layoutGenerator.addLabelWidgetPair("Data Filter", sourceFilterField, panel);

        deleteDSCheckBox = new JCheckBox();
        deleteDSCheckBox.setSelected(false);
        layoutGenerator.addLabelWidgetPair("Delete after append?", deleteDSCheckBox, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private JPanel sourcePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Source Dataset",
                0, 0, Font.decode(""), Color.BLUE));

        panel.add(createUpperPanel(), BorderLayout.CENTER);
        panel.add(viewButtonPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private Component viewButtonPanel() {
        JPanel panel = new JPanel();
        Button view = new Button("View Dataset", new AbstractAction() {
            // Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    viewSourceDataset();
                } catch (Exception e) {
                    setErrorMsg("Can not view source dataset: " + e.getMessage());
                }
            }
        });
        view.setMnemonic(KeyEvent.VK_V);
        // view.setEnabled(false);
        panel.add(view);
        return panel;
    }

    private Component targePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Target Dataset",
                0, 0, Font.decode(""), Color.BLUE));
        if (presenter.isLineBased())
            panel.add(linePanel(), BorderLayout.NORTH);

        JPanel pairPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        // pairPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Target
        // Dataset", 0, 0, Font.decode(""), Color.BLUE));

        Version[] versions = null;
        try {
            versions = presenter.getTargetDatasetNonFinalVersions();
        } catch (EmfException e) {
            setErrorMsg(e.getMessage());
        }

        targetDatasetVerison = new ComboBox("Select a version", versions);
        targetDatasetVerison.setToolTipText("Select a nonfinal version of target dataset to append rows into");
        layoutGenerator.addLabelWidgetPair("Nonfinal Version  ", targetDatasetVerison, pairPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(pairPanel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        panel.add(pairPanel, BorderLayout.CENTER);
        return panel;
    }

    // top part of target dataset
    private JPanel linePanel() {
        JPanel panel = new JPanel(new BorderLayout(30, 0));
        panel.add(createStartLinePanel(), BorderLayout.LINE_START);
        return panel;
    }

    private JPanel createStartLinePanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        startLineField = new JTextField(15);
        startLineField.setName("startLineField");
        // startLineField.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Insert after Line Number", startLineField, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad
        return panel;
    }

    private JPanel revisionPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),
                "Revision Information", 0, 0, Font.decode(""), Color.BLUE));

        what = new TextArea("", "", 30, 2);
        panel.add(labelValuePanel("What was added", ScrollableComponent.createWithVerticalScrollBar(what)));

        why = new TextArea("", "", 30, 2);
        panel.add(labelValuePanel("Why it was added", ScrollableComponent.createWithVerticalScrollBar(why)));

        return panel;
    }

    private JPanel labelValuePanel(String labelText, JComponent widget) {
        BorderLayout bl = new BorderLayout(3, 4);
        JPanel panel = new JPanel(bl);
        JLabel label = new JLabel(labelText, JLabel.CENTER);
        panel.add(label, BorderLayout.NORTH);
        panel.add(widget, BorderLayout.CENTER);

        return panel;
    }

    private Component createButtonPanel() {
        JPanel panel = new JPanel();

        okButton = new Button("OK", okAction());
        panel.add(okButton);
        okButton.setMnemonic(KeyEvent.VK_O);
        Button closeButton = new Button("Close", closeWindowAction());
        panel.add(closeButton);
        closeButton.setMnemonic(KeyEvent.VK_L);
        return panel;
    }

    public void observe(AppendDataViewPresenter presenter) {
        this.presenter = presenter;
    }

    private void selectSourceDataset() throws Exception {
        clearMsgPanel();

        DatasetType[] datasetTypes = new DatasetType[] { presenter.getDataset().getDatasetType() };
        InputDatasetSelectionDialog view = new InputDatasetSelectionDialog(parentConsole);
        InputDatasetSelectionPresenter srcDSPresenter = new InputDatasetSelectionPresenter(view,
                presenter.getSession(), datasetTypes);
        srcDSPresenter.display(datasetTypes[0], true);
        // get sourceDataset from light dataset
        if (view.shouldCreate()){
            EmfDataset[] datasets = view.getDatasets();
            sourceDataset = (datasets == null || datasets.length == 0) ? null : presenter.getDataset(datasets[0].getId());

            if (sourceDataset != null) {
                sourceDatasetField.setText(sourceDataset == null ? "" : sourceDataset.getName());
                sourceVersionBox.resetModel(presenter.getVersions(sourceDataset.getId()));
            }
        }
    }

    private void viewSourceDataset() throws EmfException {
        if (sourceDataset == null)
            throw new EmfException("Please set a source dataset");
        if (sourceVersionBox.getSelectedItem() == null)
            throw new EmfException("Please select a source dataset version");

        try {
            clearMsgPanel();
            if (sourceDataset.getInternalSources().length > 1) {
                DatasetPropertiesViewer view = new DatasetPropertiesViewer(presenter.getSession(), parentConsole,
                        desktopManager);
                presenter.doDisplayPropertiesView(view, sourceDataset);
            } else if (sourceDataset.getInternalSources().length == 1) {
                String table = sourceDataset.getInternalSources()[0].getTable();
                DataViewer view = new DataViewer(sourceDataset, parentConsole, desktopManager, sourceFilterField
                        .getText());
                presenter.doView((Version) sourceVersionBox.getSelectedItem(), table, view);
            } else
                messagePanel.setError("Could not open viewer.This is an external file.");
        } catch (EmfException e) {
            // e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMsgPanel();
                String msg = "Appending data finished. ";

                try {
                    validateSelections();
                } catch (Exception e) {
                    setErrorMsg(e.getMessage());
                    return;
                }

                try {
                    boolean sameDef = presenter.checkTableDefinitions(sourceDataset, presenter.getDataset());

                    if (!sameDef) {
                        String defMsg = "Source dataset and target dataset have different table definitions.\nWould you like to proceed to append data anyway?";
                        int answer = JOptionPane.showConfirmDialog(parentConsole, defMsg, title,
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                        if (answer != JOptionPane.YES_OPTION)
                            return;
                    }
                } catch (Exception e) {
                    setErrorMsg(e.getMessage());
                    return;
                }

                try {
                    presenter.appendData(sourceDataset.getId(), ((Version) sourceVersionBox.getSelectedItem())
                            .getVersion(), sourceFilterField.getText(), presenter.getDataset().getId(),
                            ((Version) targetDatasetVerison.getSelectedItem()).getVersion(), startLineNum);
                    okButton.setEnabled(false);
                } catch (Exception e) {
                    setErrorMsg(e.getMessage());
                    return;
                }

                try {
                    saveRevision();
                } catch (Exception e) {
                    msg += " Error set revision: " + e.getMessage();
                }

                if (deleteDSCheckBox.isSelected()) {
                    try {
                        presenter.deleteDataset(sourceDataset);
                    } catch (EmfException e) {
                        msg += " Error deleting dataset: " + e.getMessage();
                    }
                }
                setMsg(msg);
            }
        };
    }

    private void validateSelections() throws EmfException {
        String sourceDSName = sourceDatasetField.getText();

        if (sourceDSName == null || sourceDSName.trim().isEmpty())
            throw new EmfException("Please specify a valid source dataset.");

        if (sourceDataset == null)
            sourceDataset = presenter.getDataset(sourceDSName);

        if (sourceDataset == null)
            throw new EmfException("Specified source dataset doesn't exist.");

        if (sourceVersionBox.getSelectedItem() == null)
            throw new EmfException("Please specify a valid source dataset version.");

        if (deleteDSCheckBox.isSelected()) {
            try {
                presenter.checkIfDeletable(sourceDataset);
            } catch (Exception e) {
                throw new EmfException("Cannot delete after append: " + e.getMessage());
            }
        }

        if (targetDatasetVerison.getItemCount() < 2)
            throw new EmfException(
                    "Target dataset doesn't have a valid non-final version. Please create one if needed.");

        if (targetDatasetVerison.getSelectedItem() == null)
            throw new EmfException("Please specify a valid non-final version for target dataset.");

        if (presenter.isLineBased()) {
            String startLine = startLineField.getText();

            if (startLine == null || startLine.trim().isEmpty())
                startLine = "-1.0";

            try {
                startLineNum = new DoubleValue(Double.parseDouble(startLine.trim()));
            } catch (Exception e) {
                throw new EmfException("Error parsing Append after Line Number field: " + e.getMessage() + ".");
            }
        }
    }

    private void saveRevision() throws EmfException {
        Revision revision = new Revision();
        revision.setCreator(presenter.getSession().user());
        revision.setDatasetId(presenter.getDataset().getId());
        revision.setDate(new Date());
        revision.setVersion(((Version) targetDatasetVerison.getSelectedItem()).getVersion());
        revision.setWhat(what.getText());
        revision.setWhy(why.getText());
        revision.setReferences("");

        presenter.addRevision(revision);
    }

    private Action closeWindowAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                if (!okButton.isEnabled())
                    parentView.refresh();
                disposeView();
            }
        };
    }

    private void clearMsgPanel() {
        messagePanel.clear();
    }

    private void setErrorMsg(String errorMsg) {
        messagePanel.setError(errorMsg);
    }

    private void setMsg(String msg) {
        messagePanel.setMessage(msg);
    }
}

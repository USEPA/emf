package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.data.DataHeaderPref;
import gov.epa.emissions.framework.client.data.DoubleRenderer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class DataSortFilterPanelViewer extends JPanel {

    private TextArea rowFilter;

    private TextArea sortOrder;

    private MessagePanel messagePanel;

    private EmfDataset dataset;

    private JPanel actionPanel;

    private boolean forEditor = true;

    private TextField decimalPlacesField;

    private JButton formatButton;

    private JCheckBox groupCheckBox;
    
    private JCheckBox resetViewCheckBox;

    private DoubleRenderer doubleRenderer;
    
    private DataHeaderPref headerPref;

    public DataSortFilterPanelViewer(MessagePanel messagePanel, EmfDataset dataset, String rowFilters,
            DoubleRenderer doubleRenderer, DataHeaderPref headerPref) {

        this.messagePanel = messagePanel;
        this.dataset = dataset;
        this.doubleRenderer = doubleRenderer;
        this.headerPref = headerPref;

        super.setLayout(new BorderLayout(5, 5));
        super.add(sortFilterPanel(rowFilters), BorderLayout.CENTER);
        super.add(controlPanel(), BorderLayout.EAST);
        super.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private JPanel sortFilterPanel(String rowFilters) {

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints labelGBC = new GridBagConstraints();
        GridBagConstraints fieldGBC = new GridBagConstraints();
        GridBagConstraints buttonGBC = new GridBagConstraints();

        labelGBC.gridx = 0;
        labelGBC.anchor = GridBagConstraints.EAST;
        labelGBC.insets = new Insets(2, 2, 2, 2);

        fieldGBC.gridx = 1;
        fieldGBC.weightx = 1;
        fieldGBC.gridwidth = 5;
        fieldGBC.fill = GridBagConstraints.BOTH;

        buttonGBC.gridx = 2;
        buttonGBC.weightx = 0;
        buttonGBC.insets = new Insets(2, 2, 2, 5);

        panel.add(new Label("Sort Order"), labelGBC);

        sortOrder = new TextArea("sortOrder", dataset.getDatasetType().getDefaultSortOrder(), 25, 2);
        sortOrder.setToolTipText(sortOrder.getText());
        panel.add(ScrollableComponent.createWithVerticalScrollBar(sortOrder), fieldGBC);

        labelGBC.gridy = 1;
        panel.add(new Label("Row Filter"), labelGBC);

        fieldGBC.gridy = 1;
        rowFilter = new TextArea("rowFilter", rowFilters, 25, 2);
        rowFilter.setToolTipText(rowFilter.getText());
        panel.add(ScrollableComponent.createWithVerticalScrollBar(rowFilter), fieldGBC);

        labelGBC.gridy = 2;
        panel.add(new Label("Decimal Places"), labelGBC);

        fieldGBC.gridy = 2;
        fieldGBC.weightx = 0;
        fieldGBC.gridwidth = 1;
        decimalPlacesField = new TextField("decimalPlaces", 10);
        decimalPlacesField.setText(Integer.toString(doubleRenderer.getDecimalPlaces()));
        decimalPlacesField.setToolTipText("Number of decimal places to display");
        panel.add(decimalPlacesField, fieldGBC);

        buttonGBC.gridy = 2;
        buttonGBC.weightx = 0;
        buttonGBC.gridwidth = 1;

        groupCheckBox = new JCheckBox("Show Commas");
        groupCheckBox.setSelected(doubleRenderer.isGroup());
        groupCheckBox.setToolTipText("Group large numbers using commas");
        panel.add(groupCheckBox, buttonGBC);
        
        buttonGBC.gridy = 2;
        buttonGBC.gridx = 3;
        buttonGBC.weightx = 0;
        buttonGBC.gridwidth = 1;
        formatButton = new JButton();
        panel.add(formatButton, buttonGBC);
        
        buttonGBC.gridy = 2;
        buttonGBC.gridx = 4;
        buttonGBC.weightx = 0;
        buttonGBC.gridwidth = 1;
        resetViewCheckBox = new JCheckBox("");
        resetViewCheckBox.setSelected(headerPref.getResetView());
        
//        hideColCheckBox.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent event) {
//                doApplyFormat(presenter);
//            }
//        });
        
        resetViewCheckBox.setToolTipText("Hide columns using preference");
        panel.add(resetViewCheckBox, buttonGBC);
        
        return panel;
    }

    private JPanel controlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        actionPanel = new JPanel(new GridLayout(3, 1));
        panel.add(actionPanel);

        return panel;
    }

    public void init(final TablePresenter presenter) {

        this.formatButton.setAction(new AbstractAction("Format") {
            public void actionPerformed(ActionEvent e) {
                doApplyFormat(presenter);
            }
        });
        this.formatButton.setToolTipText("Apply the new format settings to the table");
        
        this.resetViewCheckBox.setAction(new AbstractAction("Reset View") {
            public void actionPerformed(ActionEvent e) {
                doApplyFormat(presenter);
            }
        });
        
        Button apply = new Button("Apply", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doApplyConstraints(presenter);
            }
        });
        apply.setToolTipText("Apply the Row Filter & Sort Order constraints to the table");
        actionPanel.add(apply);
    }

    private void doApplyFormat(final TablePresenter presenter) {

        messagePanel.clear();     

        try {
            String text = decimalPlacesField.getText();
            String trim = text.trim();
            int decimalPlaces = Integer.parseInt(trim);

            if (decimalPlaces < 0) {
                throw new RuntimeException();
            }

            this.doubleRenderer.setGroup(this.groupCheckBox.isSelected());
            this.doubleRenderer.setDecimalPlaces(decimalPlaces);
          
            this.headerPref.setHideCols(this.resetViewCheckBox.isSelected());
            
            presenter.doApplyFormat();
        } catch (Exception e) {
            messagePanel.setError("'Decimal Places' must be an integer value greater than, or equal to 0.");
        }
    }

    private void doApplyConstraints(final TablePresenter presenter) {
        try {

            messagePanel.clear();

            String rowFilterValue = rowFilter.getText().trim();
            String sortOrderValue = sortOrder.getText().trim();
            presenter.doApplyConstraints(rowFilterValue, sortOrderValue);

            if (rowFilterValue.length() == 0)
                rowFilterValue = "No filter";
            String sortMessage = sortOrderValue;
            if (sortMessage.length() == 0)
                sortMessage = "No sort";

            if (isForEditor())
                messagePanel.setMessage("Saved any changes and applied Sort '" + sortMessage + "' and Filter '"
                        + rowFilterValue + "'");
        } catch (EmfException ex) {
            messagePanel.setError(ex.getMessage());
        }
    }

    public void setSortFilter(String filter) {
        rowFilter.setText(filter);
    }

    public JTextArea getRowFilter() {
        return rowFilter;
    }

    public JTextArea getSortOrder() {
        return sortOrder;
    }

    public boolean isForEditor() {
        return forEditor;
    }

    public void setForEditor(boolean forEditor) {
        this.forEditor = forEditor;
    }

}

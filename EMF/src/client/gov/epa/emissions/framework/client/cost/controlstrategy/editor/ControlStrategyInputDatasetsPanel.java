package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.EditableTablePanel;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public class ControlStrategyInputDatasetsPanel extends JPanel implements Editor {

    private EditableTablePanel editableTablePanel;
    
    public ControlStrategyInputDatasetsPanel(EditableInputDatasetTableData tableData, ManageChangeables changeablesList, EmfConsole parent) {
        editableTablePanel = new EditableTablePanel("Input Datasets", tableData, changeablesList, parent);
//        editableTablePanel.setColumnEditor(cellEditor(keywords), 1, "Select from the list");

        super.setLayout(new BorderLayout());
        super.add(editableTablePanel, BorderLayout.CENTER);
    }

//    private TableCellEditor cellEditor(Version[] versions) {
//        JComboBox comboBox = new JComboBox();
//        comboBox.setEditable(true);
//
//        for (int i = 0; i < versions.length; i++)
//            comboBox.addItem(versions[i].getName());
//
//        return new DefaultCellEditor(comboBox);
//    }

    public void commit() {
        editableTablePanel.commit();
    }
}

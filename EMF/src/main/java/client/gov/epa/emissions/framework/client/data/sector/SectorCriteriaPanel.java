package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.EditableTablePanel;

import java.awt.BorderLayout;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class SectorCriteriaPanel extends JPanel implements  Editor {
    
    private EditableTablePanel editableTablePanel;

    public SectorCriteriaPanel(String label, SectorCriteriaTableData tableData, ManageChangeables changeablesList, EmfConsole parent) {
        editableTablePanel = new EditableTablePanel(label, tableData, changeablesList, parent);
        editableTablePanel.setColumnEditor(typesCellEditor(), 1, "Select from the list");

        super.setLayout(new BorderLayout());
        super.add(editableTablePanel, BorderLayout.CENTER);
    }

    private TableCellEditor typesCellEditor() {
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("SCC");
        comboBox.addItem("NAICS");
        comboBox.addItem("SIC");
        comboBox.addItem("IPM Flag");// True/False values

        return new DefaultCellEditor(comboBox);
    }

    public void commit() {
        editableTablePanel.commit();
    }

}

package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeyValueTableData;
import gov.epa.emissions.framework.ui.EditableTablePanel;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class DatasetTypeColumnsPanel extends JPanel implements Editor {

    private EditableTablePanel editableTablePanel;

    private JComboBox classFormatterComboBox;

    private JComboBox mandatoryComboBox;

    public DatasetTypeColumnsPanel(EditableColumnTableData tableData, ManageChangeables changeablesList, EmfConsole parent) {
        editableTablePanel = new EditableTablePanel(null, tableData, changeablesList, parent);
        editableTablePanel.setColumnEditor(mandatoryEditor(), 4, "Select from the list");
        editableTablePanel.setColumnEditor(classFormatterEditor(), 6, "Select from the list");
        super.setLayout(new BorderLayout());
        super.add(editableTablePanel, BorderLayout.CENTER);
    }

//    CharFormatter
//    IntegerFormatter
//    LongFormatter
//    RealFormatter
//    SmallIntegerFormatter
//    StringFormatter
//    NullFormatter


    private TableCellEditor classFormatterEditor() {
        classFormatterComboBox = new JComboBox();
//        classFormatterComboBox.setEditable(false);
        classFormatterComboBox.setEditable(true);
        classFormatterComboBox.addItem("");
        classFormatterComboBox.addItem("CharFormatter");
        classFormatterComboBox.addItem("IntegerFormatter");
        classFormatterComboBox.addItem("LongFormatter");
        classFormatterComboBox.addItem("RealFormatter");
        classFormatterComboBox.addItem("SmallIntegerFormatter");
        classFormatterComboBox.addItem("StringFormatter");

        return new DefaultCellEditor(classFormatterComboBox);
    }

    private TableCellEditor mandatoryEditor() {
        mandatoryComboBox = new JComboBox();
        mandatoryComboBox.setEditable(true);
        mandatoryComboBox.addItem("true");
        mandatoryComboBox.addItem("false");

        return new DefaultCellEditor(mandatoryComboBox);
    }

    public void commit() {
        editableTablePanel.commit();
    }
}

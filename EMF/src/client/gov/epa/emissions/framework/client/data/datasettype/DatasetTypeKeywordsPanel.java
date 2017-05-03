package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeyValueTableData;
import gov.epa.emissions.framework.ui.EditableTablePanel;

import java.awt.BorderLayout;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class DatasetTypeKeywordsPanel extends JPanel implements Editor {

    private EditableTablePanel editableTablePanel;
    
    public DatasetTypeKeywordsPanel(EditableKeyValueTableData tableData, Keyword[] keywords, ManageChangeables changeablesList, EmfConsole parent) {
        editableTablePanel = new EditableTablePanel(null, tableData, changeablesList, parent);
        editableTablePanel.setColumnEditor(cellEditor(keywords), 1, "Select from the list");

        super.setLayout(new BorderLayout());
        super.add(editableTablePanel, BorderLayout.CENTER);
    }

    private TableCellEditor cellEditor(Keyword[] keywords) {
        JComboBox comboBox = new JComboBox();
        comboBox.setEditable(true);

        for (int i = 0; i < keywords.length; i++)
            comboBox.addItem(keywords[i].getName());

        return new DefaultCellEditor(comboBox);
    }

    public void commit() {
        editableTablePanel.commit();
    }
}

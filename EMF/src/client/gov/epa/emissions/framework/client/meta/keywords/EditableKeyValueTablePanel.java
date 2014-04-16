package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.EditableTablePanel;
import gov.epa.emissions.framework.ui.TableData;

import javax.swing.JScrollPane;

public class EditableKeyValueTablePanel extends EditableTablePanel {

    public EditableKeyValueTablePanel(String label, EditableKeyValueTableData tableData, ManageChangeables changeablesList, EmfConsole parent) {
        super(label, tableData, changeablesList, parent);
    }
    
    protected JScrollPane table(TableData tableData) {
        tableModel = new KeywordTableModel((EditableKeyValueTableData) tableData);
        table = new EditableTable(tableModel);
        changeablesList.addChangeable(table);

        return new JScrollPane(table);
    }
    
}

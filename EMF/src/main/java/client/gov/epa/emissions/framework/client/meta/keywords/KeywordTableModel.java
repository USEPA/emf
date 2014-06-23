package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.framework.ui.EditableEmfTableModel;

public class KeywordTableModel extends EditableEmfTableModel {

    public KeywordTableModel(EditableKeyValueTableData tableData) {
        super(tableData);
    }

    public boolean isCellEditable(int row, int col) {
        return ((EditableKeyValueTableData) tableData).isEditable(row, col);
    }

}

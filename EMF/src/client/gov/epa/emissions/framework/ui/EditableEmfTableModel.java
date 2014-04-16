package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.EditableTableModel;

public class EditableEmfTableModel extends EmfTableModel implements EditableTableModel {

    private EditableTableData editableTableData;

    public EditableEmfTableModel(EditableTableData tableData) {
        super(tableData);
        this.editableTableData = tableData;
    }

    public boolean shouldTrackChange(int column) {
        return editableTableData.shouldTrackChange(column);
    }

}

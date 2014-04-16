package gov.epa.emissions.framework.ui;

public abstract class AbstractEditableTableData extends AbstractTableData implements EditableTableData {

    public boolean shouldTrackChange(int col) {
        return col == 0 ? false : true;
    }

}

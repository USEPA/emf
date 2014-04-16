package gov.epa.emissions.framework.ui;

public interface EditableTableData extends TableData {
    boolean shouldTrackChange(int col);
}

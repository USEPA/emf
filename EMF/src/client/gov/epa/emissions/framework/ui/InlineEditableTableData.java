package gov.epa.emissions.framework.ui;

public interface InlineEditableTableData extends EditableTableData {

    void addBlankRow();

    void removeSelected();

    int getSelectedCount();

}

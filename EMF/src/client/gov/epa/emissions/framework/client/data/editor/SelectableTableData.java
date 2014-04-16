package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.framework.ui.EditableTableData;

public interface SelectableTableData extends EditableTableData {

    void addBlankRow(int row);

    void removeSelected();
    
    void selectAll();
    
    void clearAll();

}

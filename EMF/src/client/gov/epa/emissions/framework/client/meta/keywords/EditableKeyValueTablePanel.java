package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.EditableTablePanel;

public class EditableKeyValueTablePanel extends EditableTablePanel {

    public EditableKeyValueTablePanel(String label, EditableKeyValueTableData tableData, ManageChangeables changeablesList, EmfConsole parent) {
        super(label, tableData, changeablesList, parent);
        table.getAccessibleContext().setAccessibleName("List of keywords specific to this dataset");
    }

}

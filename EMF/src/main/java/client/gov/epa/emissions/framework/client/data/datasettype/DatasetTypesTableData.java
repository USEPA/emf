package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class DatasetTypesTableData extends AbstractTableData {
    private List rows;

    public DatasetTypesTableData(DatasetType[] types) {
        this.rows = createRows(types);
    }

    public String[] columns() {
        return new String[] { "Name", "# Keywords", "# QA Step Templates", "Min Files", "Max Files", "Description" };
    }

    public Class getColumnClass(int col) {
        if (col == 0 || col == 5)
            return String.class;

        return Integer.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(DatasetType[] types) {
        List rows = new ArrayList();

        for (int i = 0; i < types.length; i++) {
            DatasetType element = types[i];
            Object[] values = { element.getName(), new Integer(element.getKeyVals().length), 
                    new Integer(element.getQaStepTemplates().length), new Integer(element.getMinFiles()),
                    new Integer(element.getMaxFiles()), getShortDescription(element) };

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }
    
    private String getShortDescription(DatasetType type) {
        String description = type.getDescription();
        
        if(description != null && description.length() > 50)
            return description.substring(0, 46) + " ...";
        
        return description;
    }

}

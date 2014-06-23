package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class KeyValueTableData extends AbstractTableData {
    private List rows;

    public KeyValueTableData(KeyVal[] values) {
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Keyword", "Value" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(KeyVal[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private ViewableRow row(KeyVal val) {
        return new ViewableRow(val, new Object[] { val.getKeyword().getName(), val.getValue() });
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

}

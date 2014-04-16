package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class InternalSourcesTableData extends AbstractTableData {

    private List rows;

    public InternalSourcesTableData(InternalSource[] internalSources) {
        this.rows = createRows(internalSources);
    }

    public String[] columns() {
        return new String[] { "Table", "Type", "Size", "Source" };
    }

    public List rows() {
        return rows;
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(InternalSource[] sources) {
        List rows = new ArrayList();

        for (int i = 0; i < sources.length; i++) {
            InternalSource element = sources[i];
            Object[] values = { element.getTable(), element.getType(),
                    new Long(element.getSourceSize()), element.getSource() };

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

//    private String concat(String[] cols) {
//        StringBuffer buf = new StringBuffer();
//        for (int i = 0; i < cols.length; i++) {
//            buf.append(cols[i]);
//            if ((i + 1) < cols.length)
//                buf.append(", ");
//        }
//
//        return buf.toString();
//    }

}

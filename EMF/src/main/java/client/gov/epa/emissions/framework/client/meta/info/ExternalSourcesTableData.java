package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class ExternalSourcesTableData extends AbstractTableData {

    private List rows;

    public ExternalSourcesTableData(ExternalSource[] sources) {
        this.rows = createRows(sources);
    }

    public String[] columns() {
        return new String[] { "Source" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(ExternalSource[] sources) {
        List rows = new ArrayList();

        for (int i = 0; i < sources.length; i++) {
            Object[] values = { sources[i].getDatasource() };

            Row row = new ViewableRow(sources[i], values);
            rows.add(row);
        }

        return rows;
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

}

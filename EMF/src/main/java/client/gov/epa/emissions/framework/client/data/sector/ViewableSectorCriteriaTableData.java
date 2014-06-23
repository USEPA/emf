package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.SectorCriteria;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class ViewableSectorCriteriaTableData extends AbstractTableData {
    private List rows;

    public ViewableSectorCriteriaTableData(SectorCriteria[] criteria) {
        this.rows = createRows(criteria);
    }

    public String[] columns() {
        return new String[] { "Type", "Criterion" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(SectorCriteria[] criteria) {
        List rows = new ArrayList();
        for (int i = 0; i < criteria.length; i++)
            rows.add(row(criteria[i]));

        return rows;
    }

    private ViewableRow row(SectorCriteria criterion) {
        return new ViewableRow(criterion, new Object[] { criterion.getType(), criterion.getCriteria() });
    }

    public Class getColumnClass(int col) {
        return String.class;
    }
}

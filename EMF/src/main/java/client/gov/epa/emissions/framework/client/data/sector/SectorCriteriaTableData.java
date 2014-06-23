package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.SectorCriteria;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.InlineEditableTableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SectorCriteriaTableData extends AbstractEditableTableData implements InlineEditableTableData {
    private List rows;

    public SectorCriteriaTableData(SectorCriteria[] criteria) {
        this.rows = createRows(criteria);
    }

    public String[] columns() {
        return new String[] { "Select", "Type", "Criterion" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return true;
    }

    private List createRows(SectorCriteria[] criteria) {
        List rows = new ArrayList();
        for (int i = 0; i < criteria.length; i++)
            rows.add(row(criteria[i]));

        return rows;
    }

    public void remove(SectorCriteria criterion) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            SectorCriteria source = (SectorCriteria) row.source();
            if (source == criterion) {
                rows.remove(row);
                return;
            }
        }
    }

    public void add(SectorCriteria criterion) {
        rows.add(row(criterion));
    }

    private EditableRow row(SectorCriteria criterion) {
        RowSource source = new SectorCriterionRowSource(criterion);
        return new EditableRow(source);
    }

    private SectorCriteria[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            SectorCriterionRowSource rowSource = (SectorCriterionRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (SectorCriteria[]) selected.toArray(new SectorCriteria[0]);
    }

    public void remove(SectorCriteria[] criteria) {
        for (int i = 0; i < criteria.length; i++)
            remove(criteria[i]);
    }

    public SectorCriteria[] sources() {
        List sources = sourcesList();
        return (SectorCriteria[]) sources.toArray(new SectorCriteria[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            SectorCriterionRowSource rowSource = (SectorCriterionRowSource) row.rowSource();
            sources.add(rowSource.source());
        }
        return sources;
    }

    public void addBlankRow() {
        SectorCriteria sectorCriteria = new SectorCriteria();
        sectorCriteria.setType("");
        sectorCriteria.setCriteria("");

        add(sectorCriteria);
    }

    public void removeSelected() {
        remove(getSelected());
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;

        return String.class;
    }
}

package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlTechnologyTableData extends AbstractTableData {

    private List rows;

    public ControlTechnologyTableData(ControlTechnology[] controlTechnologies) {
        rows = createRows(controlTechnologies);
    }

    private List createRows(ControlTechnology[] controlTechnologies) {
        List rows = new ArrayList();
        for (int i = 0; i < controlTechnologies.length; i++) {
            Row row = row(controlTechnologies[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(ControlTechnology controlTechnology) {
        String[] values = { controlTechnology.getName(), controlTechnology.getDescription() };
        return new ViewableRow(controlTechnology, values);
    }

    public String[] columns() {
        return new String[] { "Name", "Description" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public void add(ControlTechnology[] sccs) {
        for (int i = 0; i < sccs.length; i++) {
            Row row = row(sccs[i]);
            if (!rows.contains(row))
                rows.add(row);
        }
        refresh();
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public ControlTechnology[] sources() {
        List sources = sourcesList();
        return (ControlTechnology[]) sources.toArray(new ControlTechnology[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(ControlTechnology record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            ControlTechnology source = (ControlTechnology) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(ControlTechnology[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);

        refresh();
    }

}

package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlProgramTechnologyTableData extends AbstractTableData {

    private List<Row> rows;

    public ControlProgramTechnologyTableData(ControlTechnology[] controlTechnologys) {
        rows = createRows(controlTechnologys);
    }

    private List<Row> createRows(ControlTechnology[] controlTechnologys) {
        List<Row> rows = new ArrayList<Row>();
        for (int i = 0; i < controlTechnologys.length; i++) {
            Row row = row(controlTechnologys[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(ControlTechnology controlTechnology) {
        Object[] values = { controlTechnology.getName(), controlTechnology.getDescription() };
        return new ViewableRow(controlTechnology, values);
    }

   
    public String[] columns() {
        return new String[] { "Name", "Desc" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List<Row> rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public void add(ControlTechnology[] cm) {
        for (int i = 0; i < cm.length; i++) {
            Row row = row(cm[i]);
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

package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PollutantTableData extends AbstractTableData {

    private List rows;

    public PollutantTableData(Pollutant[] pollutants) {
        rows = createRows(pollutants);
    }

    private List createRows(Pollutant[] pollutants) {
        List rows = new ArrayList();
        for (int i = 0; i < pollutants.length; i++) {
            Row row = row(pollutants[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(Pollutant pollutant) {
        String[] values = { pollutant.getName(), pollutant.getDescription() };
        return new ViewableRow(pollutant, values);
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

    public void add(Pollutant[] cms) {
        for (int i = 0; i < cms.length; i++) {
            Row row = row(cms[i]);
            if (!rows.contains(row))
                rows.add(row);
        }
        refresh();
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public Pollutant[] sources() {
        List sources = sourcesList();
        return (Pollutant[]) sources.toArray(new Pollutant[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(Pollutant record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            Pollutant source = (Pollutant) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(Pollutant[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);

        refresh();
    }

}

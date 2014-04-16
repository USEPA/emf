package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SCCTableData extends AbstractTableData {

    private List rows;
    private Boolean lightVersion = null;
    
    public SCCTableData(Scc[] sccs) {
        rows = createRows(sccs);
    }

    public SCCTableData(Scc[] sccs, boolean lightVersion) {
        this.lightVersion = lightVersion;
        rows = createRows(sccs);
    }

    private List<Row> createRows(Scc[] sccs) {

        List<Row> rows = new ArrayList();
        for (int i = 0; i < sccs.length; i++) {
            Row row = row(sccs[i]);
            rows.add(row);
        }

        /*
         * Sort by scc
         */
        Collections.sort(rows, new Comparator<Row>() {

            public int compare(Row r1, Row r2) {

                String sccCode1 = (String) r1.getValueAt(0);
                String sccCode2 = (String) r2.getValueAt(0);

                return sccCode1.compareTo(sccCode2);
            }
        });

        return rows;
    }

    private Row row(Scc scc) {
        String[] values = { };
        if (lightVersion != null && lightVersion)
            values = new String[] { scc.getCode(), scc.getDescription() };
        else
            values = new String[] { scc.getCode(), scc.getDescription(), scc.getSector(), scc.getEi_category(), scc.getScc_l1(),
                scc.getScc_l2(), scc.getScc_l3(), scc.getScc_l4(), scc.getLast_inventory_year(), scc.getMap_to(), scc.getCreated_date(),
                scc.getRevised_date(), scc.getOption_group(), scc.getOption_set(), scc.getShort_name() };
        return new ViewableRow(scc, values);
    }

    public String[] columns() {
        if (lightVersion != null && lightVersion)
            return new String[] { "SCC", "description" };
        return new String[] { "SCC", "description", "sector","ei_category","scc_l1","scc_l2","scc_l3","scc_l4","last_inventory_year","map_to","created_date","revised_date","option_group","option_set","short_name"};
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

    public void add(Scc[] sccs) {
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

    public Scc[] sources() {
        List sources = sourcesList();
        return (Scc[]) sources.toArray(new Scc[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(Scc record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            Scc source = (Scc) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(Scc[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);

        refresh();
    }

}
